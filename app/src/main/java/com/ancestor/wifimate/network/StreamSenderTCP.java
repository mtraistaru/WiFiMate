package com.ancestor.wifimate.network;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.ancestor.wifimate.WiFiMateApp;
import com.ancestor.wifimate.peer.Packet;
import com.ancestor.wifimate.utils.Utils;

import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import javax.inject.Inject;

/**
 * Runner to dequeue packets to send and issues the TCP connection to send them.
 * Created by Mihai.Traistaru on 23.10.2015
 */
public class StreamSenderTCP {

    private static final String TAG = StreamSenderTCP.class.getName();

    private Activity activity;

    @Inject
    Router router;

    @Inject
    Utils utils;

    public StreamSenderTCP(Activity activity) {
        this.activity = activity;
        WiFiMateApp.getApp(activity).getWiFiMateComponent().inject(this);
    }

    public boolean sendPacket(String ip, int port, Packet data) {
        // Try to connect, otherwise remove from table
        Socket tcpSocket;
        try {
            Log.d(TAG, "IP Address: " + ip);
            InetAddress serverAddress = InetAddress.getByName(ip);
            tcpSocket = new Socket();
            tcpSocket.bind(null);
            tcpSocket.connect(new InetSocketAddress(serverAddress, port), 5000);
        } catch (Exception e) {
            router.getRoutingTable().remove(data.getMacAddress());
            notifyPeerLeft(data.getMacAddress(), activity);
            router.updatePeerList(activity);
            Log.e(TAG, "connection error", e);
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
            router.getRoutingTable().remove(data.getMacAddress());
            notifyPeerLeft(data.getMacAddress(), activity);
            router.updatePeerList(activity);
            Log.e(TAG, "send error", e);
        }
        return true;
    }

    private void notifyPeerLeft(String MAC, final Activity activity) {
        final String message;
        final String msg;
        message = msg = MAC + " went offline.";
        final String name = MAC;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
                utils.insertChatMessage(activity, name, msg);
            }
        });
    }
}
