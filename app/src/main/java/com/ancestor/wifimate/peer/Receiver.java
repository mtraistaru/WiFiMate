package com.ancestor.wifimate.peer;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.ancestor.wifimate.Configuration;
import com.ancestor.wifimate.utils.Utils;
import com.ancestor.wifimate.WiFiMateApp;
import com.ancestor.wifimate.activity.MessageActivity;
import com.ancestor.wifimate.activity.WiFiDirectActivity;
import com.ancestor.wifimate.network.Router;
import com.ancestor.wifimate.network.StreamReceiverTCP;

import java.util.concurrent.ConcurrentLinkedQueue;

import javax.inject.Inject;

/**
 * Created by Mihai.Traistaru on 23.10.2015
 */
public class Receiver implements Runnable {

    private static final String TAG = Receiver.class.getName();

    public static boolean running = false;

    private WiFiDirectActivity wiFiDirectActivity;

    @Inject
    Router router;

    @Inject
    Utils utils;

    public Receiver(WiFiDirectActivity wiFiDirectActivity) {
        this.wiFiDirectActivity = wiFiDirectActivity;
        WiFiMateApp.getApp(wiFiDirectActivity).getWiFiMateComponent().inject(this);
        running = true;
    }

    public void run() {
        ConcurrentLinkedQueue<Packet> packetQueue = new ConcurrentLinkedQueue<>();
        new Thread(new StreamReceiverTCP(Configuration.RECEIVE_PORT, packetQueue)).start();
        Packet packet;
        do {
            while (packetQueue.isEmpty()) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Log.e(TAG, "StreamReceiverTCP thread couldn't sleep", e);
                }
            }
            packet = packetQueue.remove();
            if (packet.getPacketType().equals(PacketType.HELLO)) {
                for (CustomWiFiP2PDevice c : router.getRoutingTable().values()) {
                    if (c.getMacAddress().equals(router.getCustomWiFiP2PDevice().getMacAddress()) || c.getMacAddress().equals(packet.getSenderMac())) {
                        continue;
                    }
                    Packet update = new Packet(
                            PacketType.UPDATE,
                            utils.getMacAsBytes(packet.getSenderMac()),
                            c.getMacAddress(),
                            router.getCustomWiFiP2PDevice().getMacAddress()
                    );
                    Sender.queuePacket(update);
                }
                router.getRoutingTable().put(packet.getSenderMac(), new CustomWiFiP2PDevice(
                                packet.getSenderMac(),
                                packet.getSenderIP(),
                                packet.getSenderMac(),
                                router.getCustomWiFiP2PDevice().getMacAddress()
                        )
                );
                byte[] routingTable = router.serializeRoutingTable();
                Packet ack = new Packet(
                        PacketType.HELLO_ACKNOWLEDGED,
                        routingTable, packet.getSenderMac(),
                        router.getCustomWiFiP2PDevice().getMacAddress()
                );
                Sender.queuePacket(ack);
                notifyPeerJoined(packet.getSenderMac(), wiFiDirectActivity);
                router.updatePeerList(wiFiDirectActivity);
            } else {
                if (packet.getMacAddress().equals(router.getCustomWiFiP2PDevice().getMacAddress())) {
                    if (packet.getPacketType().equals(PacketType.HELLO_ACKNOWLEDGED)) {
                        router.deserializeRoutingTableAndAdd(packet.getData());
                        router.getCustomWiFiP2PDevice().setGroupOwnerMacAddress(packet.getSenderMac());
                        notifyPeerJoined(packet.getSenderMac(), wiFiDirectActivity);
                        router.updatePeerList(wiFiDirectActivity);
                    } else if (packet.getPacketType().equals(PacketType.UPDATE)) {
                        String mac = utils.getMacBytesAsString(packet.getData(), 0);
                        router.getRoutingTable().put(mac, new CustomWiFiP2PDevice(
                                        mac,
                                        packet.getSenderIP(),
                                        packet.getMacAddress(),
                                        router.getCustomWiFiP2PDevice().getMacAddress()
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
                        router.updatePeerList(wiFiDirectActivity);
                    } else if (packet.getPacketType().equals(PacketType.MESSAGE)) {
                        final String message = packet.getSenderMac() + " says:\n" + new String(packet.getData());
                        final String msg = new String(packet.getData());
                        final String name = packet.getSenderMac();
                        if (!router.getRoutingTable().contains(packet.getSenderMac())) {
                            router.getRoutingTable().put(packet.getSenderMac(), new CustomWiFiP2PDevice(
                                            packet.getSenderMac(),
                                            packet.getSenderIP(),
                                            packet.getSenderMac(),
                                            router.getCustomWiFiP2PDevice().getGroupOwnerMacAddress()
                                    )
                            );
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
                        router.updatePeerList(wiFiDirectActivity);
                    }
                } else {
                    int TTL = packet.getTimeToLive();
                    TTL--;
                    if (TTL > 0) {
                        Sender.queuePacket(packet);
                        packet.setTimeToLive(TTL);
                    }
                }
            }
        } while (true);
    }

    private void notifyPeerJoined(String MAC, final Activity activity) {
        final String message;
        final String msg;
        message = msg = MAC + " has joined.";
        final String name = MAC;
        activity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
                MessageActivity.addMessage(name, msg);
            }
        });
    }
}