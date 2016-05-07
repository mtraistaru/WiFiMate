package com.ancestor.wifimate.network;

import android.util.Log;

import com.ancestor.wifimate.utils.Utils;
import com.ancestor.wifimate.peer.Packet;
import com.ancestor.wifimate.peer.PacketType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.inject.Inject;

/**
 * Created by Mihai.Traistaru on 23.10.2015
 */
public class StreamReceiverTCP implements Runnable {

    private static final String TAG = StreamReceiverTCP.class.getName();

    private ServerSocket serverSocket;
    private ConcurrentLinkedQueue<Packet> packetConcurrentLinkedQueue;

    @Inject
    Utils utils;

    public StreamReceiverTCP(int port, ConcurrentLinkedQueue<Packet> queue) {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            Log.e(TAG, "Server socket on port " + port + " could not be created. ", e);
        }
        packetConcurrentLinkedQueue = queue;
    }

    @Override
    public void run() {
        Socket socket;
        while (!Thread.currentThread().isInterrupted()) {
            try {
                socket = serverSocket.accept();
                InputStream in = socket.getInputStream();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                byte[] buf = new byte[1024];
                while (true) {
                    int n = in.read(buf);
                    if (n < 0)
                        break;
                    byteArrayOutputStream.write(buf, 0, n);
                }
                byte trimmedBytes[] = byteArrayOutputStream.toByteArray();
                Packet p = deserializePacket(trimmedBytes);
                p.setSenderIP(socket.getInetAddress().getHostAddress());
                packetConcurrentLinkedQueue.add(p);
                socket.close();
            } catch (IOException e) {
                Log.e(TAG, "error while running StreamReceiverTCP thread", e);
            }
        }
    }

    private Packet deserializePacket(byte[] inputData) {
        PacketType packetType = PacketType.values()[(int) inputData[0]];
        byte[] data = new byte[inputData.length - 14];
        int timeToLive = (int) inputData[1];
        String mac = utils.getMacBytesAsString(inputData, 2);
        String receiverMacAddress = utils.getMacBytesAsString(inputData, 8);
        System.arraycopy(inputData, 14, data, 0, inputData.length - 14);
        return new Packet(packetType, data, mac, receiverMacAddress, timeToLive);
    }
}