package com.ancestor.wifimate.router;

import android.util.Log;

import com.ancestor.wifimate.config.Configuration;
import com.ancestor.wifimate.router.tcp.TCPSender;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * The main sender of the application for all the packets that appear in the queue.
 *
 * Created by Mihai.Traistaru on 23.10.2015
 */
public class Sender implements Runnable {

    private static final String TAG = Sender.class.getName();

    /**
     * Queue for the packets to be sent.
     */
    private static ConcurrentLinkedQueue<Packet> packetConcurrentLinkedQueue;

    /**
     * Constructor for the sender object.
     */
    public Sender() {
        if (packetConcurrentLinkedQueue == null) {
            packetConcurrentLinkedQueue = new ConcurrentLinkedQueue<>();
        }
    }

    /**
     * Enqueue a packet to be sent.
     *
     * @param packet the packet to be sent.
     * @return true if the packet has been successfully added to the queue.
     */
    public static boolean queuePacket(Packet packet) {
        if (packetConcurrentLinkedQueue == null) {
            packetConcurrentLinkedQueue = new ConcurrentLinkedQueue<>();
        }
        return packetConcurrentLinkedQueue.add(packet);
    }

    @Override
    public void run() {
        TCPSender packetSender = new TCPSender();
        do {
            while (packetConcurrentLinkedQueue.isEmpty()) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Log.e(TAG, "couldn't sleep TCPSender thread", e);
                }
            }
            Packet p = packetConcurrentLinkedQueue.remove();
            String ip = MeshNetworkRouter.getPeerIPAddress(p.getMacAddress());
            packetSender.sendPacket(ip, Configuration.RECEIVE_PORT, p);

        } while (true);
    }
}
