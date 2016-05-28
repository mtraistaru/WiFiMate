package com.ancestor.wifimate.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.ancestor.wifimate.Configuration;
import com.ancestor.wifimate.R;
import com.ancestor.wifimate.WiFiMateApp;
import com.ancestor.wifimate.fragment.PeerDetailsFragment;
import com.ancestor.wifimate.fragment.PeerListFragment;
import com.ancestor.wifimate.network.Router;
import com.ancestor.wifimate.receiver.WiFiBroadcastReceiver;
import com.ancestor.wifimate.receiver.WiFiDirectBroadcastReceiver;

import javax.inject.Inject;

import static com.ancestor.wifimate.R.id.ssid;

/**
 * Main activity of the app. Handles connection with peers.
 * Created by Mihai.Traistaru on 23.10.2015
 */
public class MainActivity extends AppCompatActivity implements ChannelListener, PeerListFragment.DeviceActionListener {

    public boolean isVisible = true;

    private static final String TAG = MainActivity.class.getName();
    private IntentFilter filter;
    private WifiManager wifiManager;
    private WiFiBroadcastReceiver receiverWifi;
    private WifiP2pManager wifiP2pManager;
    private boolean wifiP2PEnabled = false;
    private boolean retryChannel = false;
    private Channel channel;
    private BroadcastReceiver receiver = null;

    public void setWifiP2PEnabled(boolean wifiP2PEnabled) {
        this.wifiP2PEnabled = wifiP2PEnabled;
    }

    @Inject
    Router router;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        WiFiMateApp.getApp(this).getWiFiMateComponent().inject(this);
        filter = new IntentFilter();
        addFilterActions(filter, WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION, WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION,
                WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION, WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = wifiP2pManager.initialize(this, getMainLooper(), null);
        if (Configuration.isDeviceBridgingEnabled) {
            wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            if (!wifiManager.isWifiEnabled()) {
                Snackbar.make(findViewById(R.id.fragment_peer_details), R.string.Message_EnablingWiFi, Snackbar.LENGTH_LONG).show();
                wifiManager.setWifiEnabled(true);
            }
            boolean wifiConnected = false;
            receiverWifi = new WiFiBroadcastReceiver(wifiManager, this, wifiConnected, router);
            registerReceiver(receiverWifi, addFilterActions(new IntentFilter(), WifiManager.SCAN_RESULTS_AVAILABLE_ACTION,
                    WifiManager.NETWORK_STATE_CHANGED_ACTION, WifiManager.WIFI_STATE_CHANGED_ACTION));
            establishAccessPointCommunication(Configuration.ACCESS_POINT, Configuration.ACCESS_POINT_PASSWORD);
        }
        final Button buttonSwitch = (Button) findViewById(R.id.buttonSwitch);
        if (buttonSwitch != null) {
            buttonSwitch.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                    startActivity(intent);
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        receiver = new WiFiDirectBroadcastReceiver(wifiP2pManager, channel, this, router);
        registerReceiver(receiver, filter);
        isVisible = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
        isVisible = false;
    }

    public void resetData() {
        PeerListFragment fragmentPeerList = (PeerListFragment) getFragmentManager().findFragmentById(R.id.fragment_peer_list);
        PeerDetailsFragment fragmentPeerDetails = (PeerDetailsFragment) getFragmentManager().findFragmentById(R.id.fragment_peer_details);
        if (fragmentPeerList != null) {
            fragmentPeerList.clearPeers();
        }
        if (fragmentPeerDetails != null) {
            fragmentPeerDetails.resetViews();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.enableWiFiDirect:
                if (wifiP2pManager != null && channel != null) {
                    startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                }
                return true;
            case R.id.discoverPeers:
                if (!wifiP2PEnabled) {
                    wifiManager.startScan();
                    View view = findViewById(R.id.fragment_peer_details);
                    if (view != null) {
                        Snackbar.make(view, R.string.Message_Warning_WiFiDirectOff, Snackbar.LENGTH_SHORT).show();
                    }
                    return true;
                }
                final PeerListFragment fragment = (PeerListFragment) getFragmentManager().findFragmentById(R.id.fragment_peer_list);
                fragment.discoverPeers();
                wifiP2pManager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        View view = findViewById(R.id.fragment_peer_details);
                        if (view != null) {
                            Snackbar.make(view, R.string.Message_DiscoverPeersStarted, Snackbar.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(int reasonCode) {
                        View view = findViewById(R.id.fragment_peer_details);
                        if (view != null) {
                            Snackbar.make(view, R.string.Message_DiscoverPeersError + " " + reasonCode, Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void showDetails(WifiP2pDevice device) {
        PeerDetailsFragment fragment = (PeerDetailsFragment) getFragmentManager().findFragmentById(R.id.fragment_peer_details);
        fragment.updateView(device, router);
    }

    @Override
    public void connect(WifiP2pConfig config) {
        wifiP2pManager.connect(channel, config, new ActionListener() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure(int reason) {
                View view = findViewById(R.id.fragment_peer_details);
                if (view != null) {
                    Snackbar.make(view, R.string.Message_ConnectionError, Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void disconnect() {
        final PeerDetailsFragment fragment = (PeerDetailsFragment) getFragmentManager().findFragmentById(R.id.fragment_peer_details);
        fragment.resetViews();
        wifiP2pManager.removeGroup(channel, new ActionListener() {
            @Override
            public void onFailure(int reasonCode) {
                Log.d(TAG, getResources().getString(R.string.Message_DisconnectionError) + " " + reasonCode);
            }

            @Override
            public void onSuccess() {
                if (fragment.getView() != null) {
                    fragment.getView().setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onChannelDisconnected() {
        if (wifiP2pManager != null && !retryChannel) {
            View view = findViewById(R.id.fragment_peer_details);
            if (view != null) {
                Snackbar.make(view, R.string.Message_ChannelDisconnected, Snackbar.LENGTH_LONG).show();
            }
            resetData();
            retryChannel = true;
            wifiP2pManager.initialize(this, getMainLooper(), this);
        } else {
            View view = findViewById(R.id.fragment_peer_details);
            if (view != null) {
                Snackbar.make(view, R.string.Message_CriticalNetworkError, Snackbar.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void cancelDisconnect() {
        if (wifiP2pManager != null) {
            final PeerListFragment fragment = (PeerListFragment) getFragmentManager().findFragmentById(R.id.fragment_peer_list);
            if (fragment.getDevice() == null || fragment.getDevice().status == WifiP2pDevice.CONNECTED) {
                disconnect();
            } else if (fragment.getDevice().status == WifiP2pDevice.AVAILABLE || fragment.getDevice().status == WifiP2pDevice.INVITED) {
                wifiP2pManager.cancelConnect(channel, new ActionListener() {
                    @Override
                    public void onSuccess() {
                        View view = findViewById(R.id.fragment_peer_details);
                        if (view != null) {
                            Snackbar.make(view, R.string.Message_CancellingConnection, Snackbar.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(int reasonCode) {
                        View view = findViewById(R.id.fragment_peer_details);
                        if (view != null) {
                            Snackbar.make(view, R.string.Message_CancelError + " " + reasonCode, Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
    }

    private void establishAccessPointCommunication(String ssid, String password) {
        Log.d(TAG, "Connecting to access point: " + ssid + " using password: " + password);
        wifiManager.enableNetwork(generateWiFiConfiguration(password), true);
        wifiManager.setWifiEnabled(true);
        Log.d(TAG, "The IP: " + wifiManager.getConnectionInfo().getIpAddress());
        Log.d(TAG, "The BSSID: " + wifiManager.getConnectionInfo().getBSSID());
        Log.d(TAG, "The SSID: " + wifiManager.getConnectionInfo().getSSID());
        if (wifiManager.getConnectionInfo().getIpAddress() != 0) {
            View view = findViewById(R.id.fragment_peer_details);
            if (view != null) {
                Snackbar.make(view, R.string.Message_Connected + " " + wifiManager.getConnectionInfo().getIpAddress(), Snackbar.LENGTH_LONG).show();
            }
        } else {
            View view = findViewById(R.id.fragment_peer_details);
            if (view != null) {
                Snackbar.make(view, R.string.Message_NotConnected + " " + wifiManager.getConnectionInfo().getIpAddress() + "(" + ssid + "," + password + ")", Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private int generateWiFiConfiguration(String password) {
        WifiConfiguration wc = new WifiConfiguration();
        wc.SSID = "\"" + ssid + "\"";
        wc.preSharedKey = "\"" + password + "\"";
        wc.status = WifiConfiguration.Status.ENABLED;
        wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        wc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        return wifiManager.addNetwork(wc);
    }

    private IntentFilter addFilterActions(IntentFilter intentFilter, String... filters) {
        for (String filter : filters) {
            intentFilter.addAction(filter);
        }
        return intentFilter;
    }
}