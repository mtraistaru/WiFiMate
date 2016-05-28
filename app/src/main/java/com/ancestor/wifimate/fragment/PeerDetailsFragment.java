package com.ancestor.wifimate.fragment;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.ancestor.wifimate.R;
import com.ancestor.wifimate.WiFiMateApp;
import com.ancestor.wifimate.network.Router;
import com.ancestor.wifimate.peer.CustomWiFiP2PDevice;
import com.ancestor.wifimate.peer.Packet;
import com.ancestor.wifimate.peer.PacketType;
import com.ancestor.wifimate.peer.Sender;
import com.ancestor.wifimate.receiver.WiFiDirectBroadcastReceiver;
import com.ancestor.wifimate.utils.Utils;

import javax.inject.Inject;

/**
 * Created by Mihai.Traistaru on 23.10.2015
 */
public class PeerDetailsFragment extends Fragment implements ConnectionInfoListener {

    private static final String TAG = PeerDetailsFragment.class.getName();

    private ProgressDialog progressDialog;
    private WifiP2pDevice device;

    @Inject
    Utils utils;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.peer_details, null);
        Button connectButton = (Button) view.findViewById(R.id.connectButton);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WifiP2pConfig wifiP2pConfig = generateNewP2PConfiguration();
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                progressDialog = ProgressDialog.show(getActivity(), getResources().getString(R.string.Message_CancelHint),
                        getResources().getString(R.string.Message_ConnectingTo) + " " + device.deviceAddress, true, true);
                ((PeerListFragment.DeviceActionListener) getActivity()).connect(wifiP2pConfig);
            }
        });
        view.findViewById(R.id.disconnectButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((PeerListFragment.DeviceActionListener) getActivity()).disconnect();
            }
        });
        return view;
    }

    public void resetViews() {
        if (getView() != null) {
            getView().findViewById(R.id.connectButton).setVisibility(View.VISIBLE);
            ((TextView) getView().findViewById(R.id.peerAddress)).setText(R.string.EmptyString);
            ((TextView) getView().findViewById(R.id.peerDetails)).setText(R.string.EmptyString);
            ((TextView) getView().findViewById(R.id.go)).setText(R.string.EmptyString);
            ((TextView) getView().findViewById(R.id.status_text)).setText(R.string.EmptyString);
            getView().setVisibility(View.GONE);
        }
    }

    private WifiP2pConfig generateNewP2PConfiguration() {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        return config;
    }

    public void updateView(WifiP2pDevice device, Router router) {
        this.device = device;
        if (this.getView() != null) {
            this.getView().setVisibility(View.VISIBLE);
        }
        TextView view = (TextView) getView().findViewById(R.id.peerAddress);
        String s = getResources().getString(R.string.Message_Online) + " ";
        for (CustomWiFiP2PDevice c : router.getRoutingTable().values()) {
            s += c.getMacAddress() + "\n";
        }
        view.setText(s);
    }

    @Override
    public void onConnectionInfoAvailable(final WifiP2pInfo info) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        if (getView() != null) {
            getView().setVisibility(View.VISIBLE);
        }
        if (!info.isGroupOwner) {
            Sender.queuePacket(new Packet(PacketType.HELLO, new byte[0], null, WiFiDirectBroadcastReceiver.macAddress));
        }
        getView().findViewById(R.id.connectButton).setVisibility(View.GONE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (getView() != null) {
            TextView statusTextView = (TextView) getView().findViewById(R.id.status_text);
            String statusText = getResources().getString(R.string.Message_Sending) + " " + data.getData();
            statusTextView.setText(statusText);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        WiFiMateApp.getApp(context).getWiFiMateComponent().inject(this);
    }
}
