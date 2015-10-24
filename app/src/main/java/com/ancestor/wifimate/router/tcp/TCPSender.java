package com.ancestor.wifimate.router.tcp;

import android.util.Log;

import com.ancestor.wifimate.router.MeshNetworkRouter;
import com.ancestor.wifimate.router.Packet;
import com.ancestor.wifimate.router.Receiver;

import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Runner to dequeue packets to send and issues the TCP connection to send them.
 * Created by Mihai.Traistaru on 23.10.2015
 */
public class TCPSender {

    private static final String TAG = TCPSender.class.getName();

    public boolean sendPacket(String ip, int port, Packet data) {
        // Try to connect, otherwise remove from table
        Socket tcpSocket;
        try {
            System.out.println("IP: " + ip);
            InetAddress serverAddr = InetAddress.getByName(ip);
            tcpSocket = new Socket();
            tcpSocket.bind(null);
            tcpSocket.connect(new InetSocketAddress(serverAddr, port), 5000);
        } catch (Exception e) {
            MeshNetworkRouter.routingTable.remove(data.getMacAddress());
            Receiver.notifyPeerLeft(data.getMacAddress());
            Receiver.updatePeerList();
            Log.e(TAG, "cannot connect", e);
            return false;
        }

        // Try to send, otherwise remove from table
        OutputStream os;
        try {
            os = tcpSocket.getOutputStream();
            os.write(data.serialize());
            os.close();
            tcpSocket.close();
        } catch (Exception e) {
            MeshNetworkRouter.routingTable.remove(data.getMacAddress());
            Receiver.notifyPeerLeft(data.getMacAddress());
            Receiver.updatePeerList();
            Log.e(TAG, "cannot send", e);
        }
        return true;
    }
}
