package com.ancestor.wifimate.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
 * Chat activity. Handles exchanged messages by peers.
 * Created by Mihai.Traistaru on 23.10.2015
 */
public class ChatActivity extends AppCompatActivity {

    private static final String TAG = ChatActivity.class.getName();

    @Inject
    Router router;

    @Inject
    Utils utils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        WiFiMateApp.getApp(this).getWiFiMateComponent().inject(this);
        TextView chatMessage = (TextView) findViewById(R.id.chatMessage);
        utils.setChatMessageView(chatMessage);
        final Button sendButton = (Button) findViewById(R.id.sendButton);
        final EditText inputMessage = (EditText) findViewById(R.id.inputEditText);
        setTitle(getResources().getString(R.string.Title_Chat));
        if (sendButton != null) {
            sendButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    String messageString = null;
                    if (inputMessage != null) {
                        messageString = inputMessage.getText().toString();
                    }
                    utils.insertChatMessage(ChatActivity.this, getResources().getString(R.string.Message_ThisDevice), messageString);
                    if (inputMessage != null) {
                        inputMessage.setText("");
                    }
                    for (CustomWiFiP2PDevice c : router.getRoutingTable().values()) {
                        if (c.getMacAddress().equals(router.getCustomWiFiP2PDevice().getMacAddress())) {
                            continue;
                        }
                        if (messageString != null) {
                            Sender.queuePacket(new Packet(
                                    PacketType.MESSAGE,
                                    messageString.getBytes(),
                                    c.getMacAddress(),
                                    WiFiDirectBroadcastReceiver.macAddress));
                        }
                    }
                }
            });
        }
    }
}
