package com.ancestor.wifimate.utils;

import android.content.Context;
import android.widget.TextView;

import com.ancestor.wifimate.R;

/**
 * Created by Mihai.Traistaru on 5/7/2016.
 */
public class Utils {

    private TextView chatMessageView;

    public TextView getChatMessageView() {
        return chatMessageView;
    }

    public void setChatMessageView(TextView textView) {
        chatMessageView = textView;
    }

    public byte[] getMacAsBytes(String macAddress) {
        byte[] mac = new byte[6]; // macAddress.split(":").length == 6 bytes
        for (int i = 0; i < macAddress.split(":").length; i++) {
            mac[i] = Integer.decode("0x" + macAddress.split(":")[i]).byteValue();
        }
        return mac;
    }

    public String getMacBytesAsString(byte[] data, int startOffset) {
        StringBuilder sb = new StringBuilder(18);
        for (int i = startOffset; i < startOffset + 6; i++) {
            byte b = data[i];
            if (sb.length() > 0)
                sb.append(':');
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public void insertChatMessage(Context context, String from, String text) {
        chatMessageView.append(from + " " + context.getResources().getString(R.string.Message_Says) + " " + text + " " + "\n");
        final int scrollAmount = chatMessageView.getLayout().getLineTop(chatMessageView.getLineCount()) - chatMessageView.getHeight();
        if (scrollAmount > 0) {
            chatMessageView.scrollTo(0, scrollAmount);
        } else {
            chatMessageView.scrollTo(0, 0);
        }
    }
}
