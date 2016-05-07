package com.ancestor.wifimate.activity;

import android.app.Activity;
import android.os.Bundle;
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

import javax.inject.Inject;

/**
 * Created by Mihai.Traistaru on 23.10.2015
 */
public class MessageActivity extends Activity {

    private static final String TAG = MessageActivity.class.getName();

    private static TextView messageView;

    @Inject
    Router router;

    public static void addMessage(String from, String text) {
        messageView.append(from + " says " + text + "\n");
        final int scrollAmount = messageView.getLayout().getLineTop(messageView.getLineCount()) - messageView.getHeight();
        if (scrollAmount > 0)
            messageView.scrollTo(0, scrollAmount);
        else
            messageView.scrollTo(0, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message);
        WiFiMateApp.getApp(this).getWiFiMateComponent().inject(this);
        messageView = (TextView) findViewById(R.id.message_view);
        final Button button = (Button) findViewById(R.id.btn_send);
        final EditText message = (EditText) findViewById(R.id.edit_message);
        this.setTitle("Group Chat");
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String msgStr = message.getText().toString();
                addMessage("This phone", msgStr);
                message.setText("");
                for (CustomWiFiP2PDevice c : router.getRoutingTable().values()) {
                    if (c.getMacAddress().equals(router.getCustomWiFiP2PDevice().getMacAddress())) {
                        continue;
                    }
                    Sender.queuePacket(new Packet(
                            PacketType.MESSAGE,
                            msgStr.getBytes(),
                            c.getMacAddress(),
                            WiFiDirectBroadcastReceiver.macAddress));
                }
            }
        });
    }
}
