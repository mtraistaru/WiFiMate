package com.ancestor.wifimate.peer;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.ancestor.wifimate.Configuration;
import com.ancestor.wifimate.activity.MainActivity;
import com.ancestor.wifimate.utils.Utils;
import com.ancestor.wifimate.WiFiMateApp;
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

    private MainActivity mainActivity;

    @Inject
    Router router;

    @Inject
    Utils utils;

    public Receiver(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        WiFiMateApp.getApp(mainActivity).getWiFiMateComponent().inject(this);
        running = true;
    }

    public void run() {
        ConcurrentLinkedQueue<Packet> packetQueue = new ConcurrentLinkedQueue<>();
        new Thread(new StreamReceiverTCP(Configuration.RECEIVE_PORT, packetQueue, utils)).start();
        Packet packet;
        do {
            while (packetQueue.isEmpty()) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Log.e(TAG, "StreamReceiverTCP thread sleep error", e);
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
                notifyPeerJoined(packet.getSenderMac(), mainActivity);
                router.updatePeerList(mainActivity);
            } else {
                if (packet.getMacAddress().equals(router.getCustomWiFiP2PDevice().getMacAddress())) {
                    if (packet.getPacketType().equals(PacketType.HELLO_ACKNOWLEDGED)) {
                        router.deserializeRoutingTableAndAdd(packet.getData());
                        router.getCustomWiFiP2PDevice().setGroupOwnerMacAddress(packet.getSenderMac());
                        notifyPeerJoined(packet.getSenderMac(), mainActivity);
                        router.updatePeerList(mainActivity);
                    } else if (packet.getPacketType().equals(PacketType.UPDATE)) {
                        String mac = utils.getMacBytesAsString(packet.getData(), 0);
                        router.getRoutingTable().put(mac, new CustomWiFiP2PDevice(
                                        mac,
                                        packet.getSenderIP(),
                                        packet.getMacAddress(),
                                        router.getCustomWiFiP2PDevice().getMacAddress()
                                )
                        );
                        final String message = mac + " has come online";
                        final String name = packet.getSenderMac();
                        mainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mainActivity.isVisible) {
                                    Toast.makeText(mainActivity, message, Toast.LENGTH_LONG).show();
                                } else {
                                    utils.insertChatMessage(mainActivity, name, message);
                                }
                            }
                        });
                        router.updatePeerList(mainActivity);
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
                        mainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mainActivity.isVisible) {
                                    Toast.makeText(mainActivity, message, Toast.LENGTH_LONG).show();
                                } else {
                                    utils.insertChatMessage(mainActivity, name, msg);
                                }
                            }
                        });
                        router.updatePeerList(mainActivity);
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
        message = msg = MAC + " is online.";
        final String name = MAC;
        activity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
                if (utils.getChatMessageView() != null) {
                    utils.insertChatMessage(activity, name, msg);
                }
            }
        });
    }
}