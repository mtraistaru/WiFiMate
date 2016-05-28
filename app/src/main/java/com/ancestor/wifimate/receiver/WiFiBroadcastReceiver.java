package com.ancestor.wifimate.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import com.ancestor.wifimate.Configuration;
import com.ancestor.wifimate.activity.MainActivity;
import com.ancestor.wifimate.network.Router;
import com.ancestor.wifimate.peer.CustomWiFiP2PDevice;
import com.ancestor.wifimate.peer.Receiver;
import com.ancestor.wifimate.peer.Sender;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.List;

/**
 * Created by Mihai.Traistaru on 23.10.2015
 */
public class WiFiBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = WiFiBroadcastReceiver.class.getName();
    private WifiManager wifiManager;
    private MainActivity activity;
    private boolean wifiConnected;
    private Router router;

    public WiFiBroadcastReceiver(WifiManager wifiManager, MainActivity activity, boolean wifiConnected, Router router) {
        super();
        this.wifiManager = wifiManager;
        this.activity = activity;
        this.wifiConnected = wifiConnected;
        this.router = router;
    }

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
            String wifiDirectSSID = null;
            StringBuilder sb = new StringBuilder();
            List<ScanResult> wifiList = this.wifiManager.getScanResults();
            sb.append("\nConnections: ").append(wifiList.size()).append("\n\n");
            for (int i = 0; i < wifiList.size(); i++) {
                if (wifiList.get(i).SSID.contains("DIRECT")) {
                    wifiDirectSSID = wifiList.get(i).SSID;
                }
                sb.append(Integer.valueOf(i + 1)).append(". ");
                sb.append((wifiList.get(i)).toString());
                sb.append("\n\n");
            }
            if (wifiDirectSSID == null || this.wifiConnected) {
                Toast.makeText(activity, "No networks to connect to", Toast.LENGTH_LONG).show();
            }
        } else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
            Log.d(TAG, "WIFI_STATE_CHANGED_ACTION");
            int iTemp = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
            checkState(iTemp);
        } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
            Log.d(TAG, "NETWORK_STATE_CHANGED_ACTION");
            NetworkInfo netInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            DetailedState state = netInfo.getDetailedState();
            Log.d(TAG, "State: " + state.name());
            changeState(state);
        }
    }

    private void changeState(DetailedState detailedState) {
        if (detailedState == DetailedState.SCANNING) {
            Log.d(TAG, "scanning...");
        } else if (detailedState == DetailedState.CONNECTING) {
            Log.d(TAG, "connecting...");
        } else if (detailedState == DetailedState.OBTAINING_IPADDR) {
            Log.d(TAG, "getting ip address...");
        } else if (detailedState == DetailedState.CONNECTED) {
            Log.d(TAG, "Connected.");
            Log.d(TAG, "BSSID: " + wifiManager.getConnectionInfo().getBSSID());
            Log.d(TAG, "IP Address: " + this.parseIpAddress(wifiManager.getConnectionInfo().getIpAddress()));
            Log.d(TAG, "SSID: " + wifiManager.getConnectionInfo().getSSID());
            Log.d(TAG, "DHCP Gateway: " + this.parseIpAddress(wifiManager.getDhcpInfo().gateway));
            Log.d(TAG, "MAC Address: " + wifiManager.getConnectionInfo().getMacAddress());
            Log.d(TAG, "DHCP Server: " + this.parseIpAddress(wifiManager.getDhcpInfo().serverAddress));
            Log.d(TAG, "DHCP Netmask: " + this.parseIpAddress(wifiManager.getDhcpInfo().netmask));
            router.setCustomWiFiP2PDevice(new CustomWiFiP2PDevice(
                            wifiManager.getConnectionInfo().getMacAddress(),
                            Configuration.GROUP_OWNER_IP_ADDRESS,
                            wifiManager.getConnectionInfo().getMacAddress(),
                            wifiManager.getConnectionInfo().getMacAddress()
                    )
            );
            if (!Receiver.running) {
                Receiver r = new Receiver(activity);
                new Thread(r).start();
                Sender s = new Sender(activity);
                new Thread(s).start();
            }
        } else if (detailedState == DetailedState.DISCONNECTING) {
            Log.d(TAG, "disconnecting...");
        } else if (detailedState == DetailedState.DISCONNECTED) {
            Log.d(TAG, "Disconnected.");
        } else if (detailedState == DetailedState.FAILED) {
            // TODO
        }
    }

    private void checkState(int state) {
        if (state == WifiManager.WIFI_STATE_ENABLING) {
            Log.d(TAG, "WIFI_STATE_ENABLING");
        } else if (state == WifiManager.WIFI_STATE_ENABLED) {
            Log.d(TAG, "WIFI_STATE_ENABLED");
        } else if (state == WifiManager.WIFI_STATE_DISABLING) {
            Log.d(TAG, "WIFI_STATE_DISABLING");
        } else if (state == WifiManager.WIFI_STATE_DISABLED) {
            Log.d(TAG, "WIFI_STATE_DISABLED");
        }
    }

    private String parseIpAddress(int ipAddress) {
        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ipAddress = Integer.reverseBytes(ipAddress);
        }
        byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();
        String ipAddressString;
        try {
            ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();
        } catch (UnknownHostException ex) {
            Log.e(TAG, "Can't get host ip.");
            ipAddressString = null;
        }
        return ipAddressString;
    }
}
