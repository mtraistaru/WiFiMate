package com.ancestor.wifimate.network;

import android.app.Activity;
import android.util.Log;
import android.widget.TextView;

import com.ancestor.wifimate.Configuration;
import com.ancestor.wifimate.R;
import com.ancestor.wifimate.peer.CustomWiFiP2PDevice;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Mihai.Traistaru on 23.10.2015
 */
public class Router {

    private static final String TAG = Router.class.getName();

    private ConcurrentHashMap<String, CustomWiFiP2PDevice> routingTable = new ConcurrentHashMap<>();

    private CustomWiFiP2PDevice customWiFiP2PDevice;

    public ConcurrentHashMap<String, CustomWiFiP2PDevice> getRoutingTable() {
        return routingTable;
    }

    public CustomWiFiP2PDevice getCustomWiFiP2PDevice() {
        return customWiFiP2PDevice;
    }

    public void setCustomWiFiP2PDevice(CustomWiFiP2PDevice customWiFiP2PDevice) {
        this.customWiFiP2PDevice = customWiFiP2PDevice;
        routingTable.put(customWiFiP2PDevice.getMacAddress(), customWiFiP2PDevice);
    }

    public String getClientIpAddress(CustomWiFiP2PDevice customWiFiP2PDevice) {
        if (customWiFiP2PDevice.getGroupOwnerMacAddress().equals(customWiFiP2PDevice.getGroupOwnerMacAddress())) {
            Log.d(TAG, "Have the same group owner, sending to :" + customWiFiP2PDevice.getIpAddress());
            return customWiFiP2PDevice.getIpAddress(); // shares the same Group-Owner, so it's okay to use its IP
        }
        CustomWiFiP2PDevice groupOwner = routingTable.get(customWiFiP2PDevice.getGroupOwnerMacAddress());
        if (customWiFiP2PDevice.getGroupOwnerMacAddress().equals(customWiFiP2PDevice.getMacAddress())) { // this is the group owner so can propagate
            if (!customWiFiP2PDevice.getGroupOwnerMacAddress().equals(customWiFiP2PDevice.getGroupOwnerMacAddress()) && groupOwner.isDirectLink()) {
                return customWiFiP2PDevice.getIpAddress(); // not the same group owner, but we have the group owner as a direct link
            } else if (groupOwner != null && !customWiFiP2PDevice.getGroupOwnerMacAddress().equals(customWiFiP2PDevice.getGroupOwnerMacAddress()) && !groupOwner.isDirectLink()) {
                for (CustomWiFiP2PDevice p : routingTable.values()) {
                    if (p.getGroupOwnerMacAddress().equals(p.getMacAddress())) {
                        return p.getIpAddress(); //try sending it to a random group owner; can also expand this to all group owners
                    }
                }
                return "0.0.0.0"; // no other group owners, don't know who to send it to
            }
        } else if (groupOwner != null) { // not the group owner - need to sent it to my group owner
            return Configuration.GROUP_OWNER_IP_ADDRESS;
        }
        return "0.0.0.0"; // drop the packet
    }

    public String getClientIpAddress(String macAddress) {
        CustomWiFiP2PDevice c = routingTable.get(macAddress);
        if (c == null) {
            Log.d(TAG, "NULL ENTRY in ROUTING TABLE FOR MAC");
            return Configuration.GROUP_OWNER_IP_ADDRESS;
        }
        return getClientIpAddress(c);
    }

    public byte[] serializeRoutingTable() {
        StringBuilder serialized = new StringBuilder();
        for (CustomWiFiP2PDevice v : routingTable.values()) {
            serialized.append(v.toString());
            serialized.append("\n");
        }
        return serialized.toString().getBytes();
    }

    public void deserializeRoutingTableAndAdd(byte[] serializedRoutingTable) {
        String[] div = new String(serializedRoutingTable).split("\n");
        for (String s : div) {
            CustomWiFiP2PDevice customWiFiP2PDevice = new CustomWiFiP2PDevice(s.split(",")[0], s.split(",")[1], s.split(",")[2], s.split(",")[3]);
            routingTable.put(customWiFiP2PDevice.getMacAddress(), customWiFiP2PDevice);
        }
    }

    public void updatePeerList(final Activity activity) {
        if (activity == null)
            return;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView view = (TextView) activity.findViewById(R.id.device_address);
                if (view != null) {
                    String s = "Currently in the network chatting: \n";
                    for (CustomWiFiP2PDevice c : routingTable.values()) {
                        s += c.getMacAddress() + "\n";
                    }
                    view.setText(s);
                }
            }
        });
    }
}
