package com.ancestor.wifimate.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.ancestor.wifimate.R;
import com.ancestor.wifimate.router.MeshNetworkRouter;
import com.ancestor.wifimate.router.Packet;
import com.ancestor.wifimate.router.Peer;
import com.ancestor.wifimate.router.Sender;
import com.ancestor.wifimate.wifi.WiFiDirectBroadcastReceiver;

/**
 * Activity for the group chat view
 * Created by Mihai.Traistaru on 23.10.2015
 */
public class MessageActivity extends Activity {

    private static final String TAG = MessageActivity.class.getName();

    private static TextView messageView;

    /**
     * Add a message to the view
     * @param from the sender of the message
     * @param text the text sent
     */
    public static void addMessage(String from, String text) {

        messageView.append(from + " says " + text + "\n");
        final int scrollAmount = messageView.getLayout().getLineTop(messageView.getLineCount()) - messageView.getHeight();
        // if there is no need to scroll, scrollAmount will be <=0
        if (scrollAmount > 0)
            messageView.scrollTo(0, scrollAmount);
        else
            messageView.scrollTo(0, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message);

        messageView = (TextView) findViewById(R.id.message_view);

        final Button button = (Button) findViewById(R.id.btn_send);
        final EditText message = (EditText) findViewById(R.id.edit_message);

        this.setTitle("Group Chat");

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String msgStr = message.getText().toString();
                addMessage("This phone", msgStr);
                message.setText("");

                // Send to other clients as a group chat message
                for (Peer c : MeshNetworkRouter.routingTable.values()) {
                    if (c.getMacAddress().equals(MeshNetworkRouter.getSelf().getMacAddress())) {
                        continue;
                    }
                    Sender.queuePacket(new Packet(
                            Packet.TYPE.MESSAGE,
                            msgStr.getBytes(),
                            c.getMacAddress(),
                            WiFiDirectBroadcastReceiver.MAC));
                }
            }
        });
    }
}
