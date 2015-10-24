package com.ancestor.wifimate.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import com.ancestor.wifimate.activity.WiFiDirectActivity;
import com.ancestor.wifimate.config.Configuration;
import com.ancestor.wifimate.router.MeshNetworkRouter;
import com.ancestor.wifimate.router.Peer;
import com.ancestor.wifimate.router.Receiver;
import com.ancestor.wifimate.router.Sender;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.List;

/**
 * Used for bridging or legacy wifi connections.
 * <p>
 * Created by Mihai.Traistaru on 23.10.2015
 */
public class WiFiBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = WiFiBroadcastReceiver.class.getName();
    private WifiManager wifiManager;
    private WiFiDirectActivity activity;
    private boolean wifiConnected;

    public WiFiBroadcastReceiver(WifiManager wifiManager, WiFiDirectActivity activity, boolean wifiConnected) {
        super();
        this.wifiManager = wifiManager;
        this.activity = activity;
        this.wifiConnected = wifiConnected;
    }

    /**
     * Called when the number of wifi connections has changed.
     */
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {

            String wifiDirectSSID = null;

            StringBuilder sb = new StringBuilder();
            List<ScanResult> wifiList = this.wifiManager.getScanResults();
            sb.append("\n        Number Of Wifi connections :").append(wifiList.size()).append("\n\n");

            for (int i = 0; i < wifiList.size(); i++) {
                if (wifiList.get(i).SSID.contains("DIRECT")) {
                    wifiDirectSSID = wifiList.get(i).SSID;
                }
                sb.append(Integer.valueOf(i + 1)).append(". ");
                sb.append((wifiList.get(i)).toString());
                sb.append("\n\n");
            }

            if (wifiDirectSSID == null || this.wifiConnected) {
                Toast.makeText(activity, "Found no WiFi direct network to connect to", Toast.LENGTH_LONG).show();
            }
        } else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
            Log.d(TAG, "WIFI_STATE_CHANGED_ACTION");
            int iTemp = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
            checkState(iTemp);
        } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
            Log.d(TAG, "NETWORK_STATE_CHANGED_ACTION");
            NetworkInfo netInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            DetailedState state = netInfo.getDetailedState();
            Log.d(TAG, "	state = " + state.name());
            changeState(state);
        }
    }

    private void changeState(DetailedState detailedState) {
        if (detailedState == DetailedState.SCANNING) {
            Log.d(TAG, "SCANNING");
        } else if (detailedState == DetailedState.CONNECTING) {
            Log.d(TAG, "CONNECTING");
        } else if (detailedState == DetailedState.OBTAINING_IPADDR) {
            Log.d(TAG, "OBTAINING IP ADDRESS");
        } else if (detailedState == DetailedState.CONNECTED) {
            Log.d(TAG, "CONNECTED");
            Log.d(TAG, "	basic ssid =" + wifiManager.getConnectionInfo().getBSSID());
            Log.d(TAG, "	ip address =" + this.parseIpAddress(wifiManager.getConnectionInfo().getIpAddress()));
            Log.d(TAG, "	ssid =" + wifiManager.getConnectionInfo().getSSID());
            Log.d(TAG, "	dhcp gateway =" + this.parseIpAddress(wifiManager.getDhcpInfo().gateway));
            Log.d(TAG, "	MAC address =" + wifiManager.getConnectionInfo().getMacAddress());
            Log.d(TAG, "	dhcp server=" + this.parseIpAddress(wifiManager.getDhcpInfo().serverAddress));
            Log.d(TAG, "	dhcp netmask =" + this.parseIpAddress(wifiManager.getDhcpInfo().netmask));

            MeshNetworkRouter.setSelf(new Peer(
                            wifiManager.getConnectionInfo().getMacAddress(),
                            Configuration.GROUP_OWNER_IP_ADDRESS,
                            wifiManager.getConnectionInfo().getMacAddress(),
                            wifiManager.getConnectionInfo().getMacAddress()
                    )
            );

            if (!Receiver.running) {
                Receiver r = new Receiver(activity);
                new Thread(r).start();
                Sender s = new Sender();
                new Thread(s).start();
            }
        } else if (detailedState == DetailedState.DISCONNECTING) {
            Log.d(TAG, "DISCONNECTING");
        } else if (detailedState == DetailedState.DISCONNECTED) {
            Log.d(TAG, "DISCONNECTED");
        } else if (detailedState == DetailedState.FAILED) {
            // TODO
        }
    }

    public void checkState(int state) {
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

        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) { // convert little-endian to big-endian if needed
            ipAddress = Integer.reverseBytes(ipAddress);
        }

        byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();

        String ipAddressString;
        try {
            ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();
        } catch (UnknownHostException ex) {
            Log.e(TAG, "Unable to get the host address.");
            ipAddressString = null;
        }
        return ipAddressString;
    }
}
