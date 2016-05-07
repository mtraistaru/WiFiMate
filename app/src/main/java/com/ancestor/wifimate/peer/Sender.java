package com.ancestor.wifimate.peer;

import android.app.Activity;
import android.util.Log;

import com.ancestor.wifimate.Configuration;
import com.ancestor.wifimate.WiFiMateApp;
import com.ancestor.wifimate.network.Router;
import com.ancestor.wifimate.network.StreamSenderTCP;

import java.util.concurrent.ConcurrentLinkedQueue;

import javax.inject.Inject;

/**
 * Created by Mihai.Traistaru on 23.10.2015
 */
public class Sender implements Runnable {

    private static final String TAG = Sender.class.getName();

    private Activity activity;

    @Inject
    Router router;

    private static ConcurrentLinkedQueue<Packet> packetConcurrentLinkedQueue;

    public Sender(Activity activity) {
        if (packetConcurrentLinkedQueue == null) {
            packetConcurrentLinkedQueue = new ConcurrentLinkedQueue<>();
        }
        this.activity = activity;
        WiFiMateApp.getApp(activity).getWiFiMateComponent().inject(this);
    }

    public static boolean queuePacket(Packet packet) {
        if (packetConcurrentLinkedQueue == null) {
            packetConcurrentLinkedQueue = new ConcurrentLinkedQueue<>();
        }
        return packetConcurrentLinkedQueue.add(packet);
    }

    @Override
    public void run() {
        StreamSenderTCP packetSender = new StreamSenderTCP(activity);
        do {
            while (packetConcurrentLinkedQueue.isEmpty()) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Log.e(TAG, "couldn't sleep StreamSenderTCP thread", e);
                }
            }
            Packet p = packetConcurrentLinkedQueue.remove();
            String ip = router.getClientIpAddress(p.getMacAddress());
            packetSender.sendPacket(ip, Configuration.RECEIVE_PORT, p);

        } while (true);
    }
}
