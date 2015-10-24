package com.ancestor.wifimate.router.tcp;

import android.util.Log;

import com.ancestor.wifimate.router.Packet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Receives packets on a server socket thread and enqueues them to a receiver runner.
 * Created by Mihai.Traistaru on 23.10.2015
 */
public class TCPReceiver implements Runnable {

    private static final String TAG = TCPReceiver.class.getName();

    private ServerSocket serverSocket;
    private ConcurrentLinkedQueue<Packet> packetQueue;

    /**
     * Constructor for the TCPReceiver.
     * @param port the port of the server
     * @param queue the queue of the receiver
     */
    public TCPReceiver(int port, ConcurrentLinkedQueue<Packet> queue) {
        try {
            this.serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            Log.e(TAG, "Server socket on port " + port + " could not be created. ", e);
        }
        this.packetQueue = queue;
    }

    @Override
    public void run() {
        Socket socket;
        while (!Thread.currentThread().isInterrupted()) {
            try {
                socket = this.serverSocket.accept();
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
                Packet p = Packet.deserialize(trimmedBytes);
                p.setSenderIP(socket.getInetAddress().getHostAddress());
                this.packetQueue.add(p);
                socket.close();
            } catch (IOException e) {
                Log.e(TAG, "error while running TCPReceiver thread", e);
            }
        }
    }
}