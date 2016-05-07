package com.ancestor.wifimate.fragment;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ancestor.wifimate.R;
import com.ancestor.wifimate.WiFiMateApp;
import com.ancestor.wifimate.network.Router;
import com.ancestor.wifimate.peer.CustomWiFiP2PDevice;
import com.ancestor.wifimate.peer.Packet;
import com.ancestor.wifimate.peer.PacketType;
import com.ancestor.wifimate.peer.Sender;
import com.ancestor.wifimate.receiver.WiFiDirectBroadcastReceiver;

import javax.inject.Inject;

/**
 * Created by Mihai.Traistaru on 23.10.2015
 */
public class DeviceDetailFragment extends Fragment implements ConnectionInfoListener {

    private static final String TAG = DeviceDetailFragment.class.getName();

    private View contentView = null;
    private ProgressDialog progressDialog = null;
    private WifiP2pDevice device;

    @Inject
    Router router;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.device_detail, null);
        contentView.findViewById(R.id.btn_connect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;
                config.wps.setup = WpsInfo.PBC;
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                progressDialog = ProgressDialog.show(getActivity(), "Press back to cancel", "Connecting to :" + device.deviceAddress, true, true);
                ((DeviceListFragment.DeviceActionListener) getActivity()).connect(config);
            }
        });

        contentView.findViewById(R.id.btn_disconnect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((DeviceListFragment.DeviceActionListener) getActivity()).disconnect();
            }
        });

        return contentView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        Uri uri = data.getData();
        TextView statusText = (TextView) contentView.findViewById(R.id.status_text);
        statusText.setText("Sending: " + uri);
        Log.d(TAG, "Intent----------- " + uri);
    }

    @Override
    public void onConnectionInfoAvailable(final WifiP2pInfo info) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        if (this.getView() != null) {
            this.getView().setVisibility(View.VISIBLE);
        }
        if (!info.isGroupOwner) {
            Sender.queuePacket(new Packet(PacketType.HELLO, new byte[0], null, WiFiDirectBroadcastReceiver.macAddress));
        }
        contentView.findViewById(R.id.btn_connect).setVisibility(View.GONE);
    }

    public void showDetails(WifiP2pDevice device) {
        this.device = device;
        if (this.getView() != null) {
            this.getView().setVisibility(View.VISIBLE);
        }
        TextView view = (TextView) contentView.findViewById(R.id.device_address);
        String s = "Currently in the network chatting: \n";
        for (CustomWiFiP2PDevice c : router.getRoutingTable().values()) {
            s += c.getMacAddress() + "\n";
        }
        view.setText(s);
    }

    public void resetViews() {
        contentView.findViewById(R.id.btn_connect).setVisibility(View.VISIBLE);
        TextView view = (TextView) contentView.findViewById(R.id.device_address);
        view.setText(R.string.empty);
        view = (TextView) contentView.findViewById(R.id.device_info);
        view.setText(R.string.empty);
        view = (TextView) contentView.findViewById(R.id.group_owner);
        view.setText(R.string.empty);
        view = (TextView) contentView.findViewById(R.id.status_text);
        view.setText(R.string.empty);
        if (this.getView() != null) {
            this.getView().setVisibility(View.GONE);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        WiFiMateApp.getApp(context).getWiFiMateComponent().inject(this);
    }
}
