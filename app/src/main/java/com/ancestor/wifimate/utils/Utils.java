package com.ancestor.wifimate.utils;

/**
 * Created by Mihai.Traistaru on 5/7/2016.
 */
public class Utils {

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
}
