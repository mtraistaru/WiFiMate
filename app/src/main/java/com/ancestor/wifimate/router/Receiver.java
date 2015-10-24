package com.ancestor.wifimate.router;

import android.util.Log;
import android.widget.Toast;

import com.ancestor.wifimate.activity.MessageActivity;
import com.ancestor.wifimate.activity.WiFiDirectActivity;
import com.ancestor.wifimate.config.Configuration;
import com.ancestor.wifimate.fragment.DeviceDetailFragment;
import com.ancestor.wifimate.router.tcp.TCPReceiver;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * The main receiver runnable of the application.
 *
 * Created by Mihai.Traistaru on 23.10.2015
 */
public class Receiver implements Runnable {

    private static final String TAG = Receiver.class.getName();

    /**
     * Flag if the receiver has been running to prevent overzealous thread spawning.
     */
    public static boolean running = false;

    /**
     * A reference to the main wiFiDirectActivity of the app.
     */
    static WiFiDirectActivity wiFiDirectActivity;

    /**
     * Constructor with a reference to the main wiFiDirectActivity of the app.
     *
     * @param wiFiDirectActivity the main wiFiDirectActivity of the app.
     */
    public Receiver(WiFiDirectActivity wiFiDirectActivity) {
        Receiver.wiFiDirectActivity = wiFiDirectActivity;
        running = true;
    }

    /**
     * Displays a notification when a peer joined.
     *
     * @param MAC the MAC address of the one that joined.
     */
    public static void notifyPeerJoined(String MAC) {

        final String message;
        final String msg;
        message = msg = MAC + " has joined.";
        final String name = MAC;
        wiFiDirectActivity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (wiFiDirectActivity.isVisible) {
                    Toast.makeText(wiFiDirectActivity, message, Toast.LENGTH_LONG).show();
                } else {
                    MessageActivity.addMessage(name, msg);
                }
            }
        });
    }

    /**
     * Displays a notification when a peer left.
     *
     * @param MAC the MAC address of the one that left.
     */
    public static void notifyPeerLeft(String MAC) {

        final String message;
        final String msg;
        message = msg = MAC + " has left.";
        final String name = MAC;
        wiFiDirectActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (wiFiDirectActivity.isVisible) {
                    Toast.makeText(wiFiDirectActivity, message, Toast.LENGTH_LONG).show();
                } else {
                    MessageActivity.addMessage(name, msg);
                }
            }
        });
    }

    /**
     * Update the list of peers on the front page
     */
    public static void updatePeerList() {
        if (wiFiDirectActivity == null)
            return;
        wiFiDirectActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                DeviceDetailFragment.updateGroupChatMembersMessage();
            }

        });
    }

    public void run() {

        /*
         * A queue for the received packets
		 */
        ConcurrentLinkedQueue<Packet> packetQueue = new ConcurrentLinkedQueue<>();

		/*
         * Receiver thread
		 */
        new Thread(new TCPReceiver(Configuration.RECEIVE_PORT, packetQueue)).start();

        Packet packet;

		/*
         * Keep going through packets
		 */
        do {
            /*
             * If the queue is empty, sleep to give up CPU cycles
			 */
            while (packetQueue.isEmpty()) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Log.e(TAG, "TCPReceiver thread couldn't sleep", e);
                }
            }

			/*
             * Pop a packet from the packet queue
			 */
            packet = packetQueue.remove();

            if (packet.getType().equals(Packet.TYPE.HELLO)) { // this is a special type that needs to go through the connection mechanism for any node receiving this
                for (Peer c : MeshNetworkRouter.routingTable.values()) {
                    if (c.getMacAddress().equals(MeshNetworkRouter.getSelf().getMacAddress()) || c.getMacAddress().equals(packet.getSenderMac())) {
                        continue;
                    }
                    Packet update = new Packet(
                            Packet.TYPE.UPDATE,
                            Packet.getMacAsBytes(packet.getSenderMac()),
                            c.getMacAddress(),
                            MeshNetworkRouter.getSelf().getMacAddress()
                    );
                    Sender.queuePacket(update); // enqueue it to be sent
                }
                MeshNetworkRouter.routingTable.put(packet.getSenderMac(), new Peer(
                                packet.getSenderMac(),
                                packet.getSenderIP(),
                                packet.getSenderMac(),
                                MeshNetworkRouter.getSelf().getMacAddress()
                        )
                ); // put it in the routing table

                // Send routing table back as HELLO_ACK
                byte[] routingTable = MeshNetworkRouter.serializeRoutingTable();

                Packet ack = new Packet(
                        Packet.TYPE.HELLO_ACK,
                        routingTable, packet.getSenderMac(),
                        MeshNetworkRouter.getSelf().getMacAddress()
                );
                Sender.queuePacket(ack);
                notifyPeerJoined(packet.getSenderMac());
                updatePeerList();
            } else {
                if (packet.getMacAddress().equals(MeshNetworkRouter.getSelf().getMacAddress())) { // self is the intended target for a non hello message
                    if (packet.getType().equals(Packet.TYPE.HELLO_ACK)) { // if we get a hello ack populate the table
                        MeshNetworkRouter.deserializeRoutingTableAndAdd(packet.getData());
                        MeshNetworkRouter.getSelf().setGroupOwnerMacAddress(packet.getSenderMac());
                        notifyPeerJoined(packet.getSenderMac());
                        updatePeerList();
                    } else if (packet.getType().equals(Packet.TYPE.UPDATE)) { // an update, add to the table
                        String mac = Packet.getMacBytesAsString(packet.getData(), 0);
                        MeshNetworkRouter.routingTable.put(mac, new Peer(
                                        mac,
                                        packet.getSenderIP(),
                                        packet.getMacAddress(),
                                        MeshNetworkRouter.getSelf().getMacAddress()
                                )
                        );

                        final String message = mac + " joined the conversation";
                        final String name = packet.getSenderMac();
                        wiFiDirectActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (wiFiDirectActivity.isVisible) {
                                    Toast.makeText(wiFiDirectActivity, message, Toast.LENGTH_LONG).show();
                                } else {
                                    MessageActivity.addMessage(name, message);
                                }
                            }
                        });
                        updatePeerList();

                    } else if (packet.getType().equals(Packet.TYPE.MESSAGE)) {
                        final String message = packet.getSenderMac() + " says:\n" + new String(packet.getData());
                        final String msg = new String(packet.getData());
                        final String name = packet.getSenderMac();

                        //noinspection SuspiciousMethodCalls
                        if (!MeshNetworkRouter.routingTable.contains(packet.getSenderMac())) {
                            MeshNetworkRouter.routingTable.put(packet.getSenderMac(), new Peer(
                                            packet.getSenderMac(),
                                            packet.getSenderIP(),
                                            packet.getSenderMac(),
                                            MeshNetworkRouter.getSelf().getGroupOwnerMacAddress()
                                    )
                            ); // update the routing table if for some reason this guy isn't in it
                        }

                        wiFiDirectActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (wiFiDirectActivity.isVisible) {
                                    Toast.makeText(wiFiDirectActivity, message, Toast.LENGTH_LONG).show();
                                } else {
                                    MessageActivity.addMessage(name, msg);
                                }
                            }
                        });
                        updatePeerList();
                    }
                } else {
                    int TTL = packet.getTTL(); // forward it if you're not the recipient
                    TTL--; // decrease the TTL to prevent packets sticking around forever
                    if (TTL > 0) {
                        Sender.queuePacket(packet);
                        packet.setTTL(TTL);
                    }
                }
            }
        } while (true);
    }
}