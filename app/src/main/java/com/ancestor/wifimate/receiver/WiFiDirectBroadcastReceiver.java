/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ancestor.wifimate.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.util.Log;

import com.ancestor.wifimate.Configuration;
import com.ancestor.wifimate.R;
import com.ancestor.wifimate.activity.MainActivity;
import com.ancestor.wifimate.fragment.PeerDetailsFragment;
import com.ancestor.wifimate.fragment.PeerListFragment;
import com.ancestor.wifimate.network.Router;
import com.ancestor.wifimate.peer.CustomWiFiP2PDevice;
import com.ancestor.wifimate.peer.Receiver;
import com.ancestor.wifimate.peer.Sender;

/**
 * Created by Mihai.Traistaru on 23.10.2015
 */
public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = WiFiDirectBroadcastReceiver.class.getName();

    public static String macAddress;
    private WifiP2pManager manager;
    private Channel channel;
    private MainActivity activity;
    private Router router;

    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, Channel channel, MainActivity activity, Router router) {
        super();
        this.manager = manager;
        this.channel = channel;
        this.activity = activity;
        this.router = router;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                activity.setWifiP2PEnabled(true);
                manager.createGroup(channel, new ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "group created");
                    }

                    @Override
                    public void onFailure(int reason) {
                        Log.d(TAG, "group failed");
                    }
                });
            } else {
                activity.setWifiP2PEnabled(false);
                activity.resetData();
            }
            Log.d(TAG, "Action taken: WIFI_P2P_STATE_CHANGED_ACTION state: " + state);
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            if (manager != null) {
                manager.requestPeers(channel, (PeerListListener) activity.getFragmentManager().findFragmentById(R.id.fragment_peer_list));
            }
            Log.d(TAG, "Action taken: WIFI_P2P_PEERS_CHANGED_ACTION");
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            if (manager == null) {
                return;
            }
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if (networkInfo.isConnected()) {
                PeerDetailsFragment fragment = (PeerDetailsFragment) activity.getFragmentManager().findFragmentById(R.id.fragment_peer_details);
                manager.requestConnectionInfo(channel, fragment);
            } else { // It's a disconnect
                Log.d(TAG, "Action taken: WIFI_P2P_CONNECTION_CHANGED_ACTION - Disconnected.");
                activity.resetData();
            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            PeerListFragment fragment = (PeerListFragment) activity.getFragmentManager().findFragmentById(R.id.fragment_peer_list);
            fragment.updateThisDevice((WifiP2pDevice) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
            macAddress = ((WifiP2pDevice) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE)).deviceAddress;
            router.setCustomWiFiP2PDevice(new CustomWiFiP2PDevice(
                    ((WifiP2pDevice) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE)).deviceAddress, Configuration.GROUP_OWNER_IP_ADDRESS,
                    ((WifiP2pDevice) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE)).deviceName,
                    ((WifiP2pDevice) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE)).deviceAddress)
            );
            if (!Receiver.running) {
                Receiver r = new Receiver(activity);
                new Thread(r).start();
                Sender s = new Sender(activity);
                new Thread(s).start();
            }
            manager.requestGroupInfo(channel, new WifiP2pManager.GroupInfoListener() {
                        @Override
                        public void onGroupInfoAvailable(WifiP2pGroup group) {
                            if (group != null) {
                                String ssid = group.getNetworkName();
                                String password = group.getPassphrase();
                                Log.d(TAG, "Group details");
                                Log.d(TAG, "ssid: " + ssid + "\npassword: " + password);
                            }
                        }
                    }
            );
        }
    }
}
