package com.ancestor.wifimate.peer;

import com.ancestor.wifimate.utils.Utils;

/**
 * Created by Mihai.Traistaru on 23.10.2015
 */
public class Packet {

    private byte[] data;
    private PacketType packetType;
    private String receiverMacAddress;
    private String senderMac;
    private String senderIP;
    private int timeToLive;

    public Packet(PacketType packetType, byte[] data, String receiverMacAddress, String senderMac) {
        this.data = data;
        this.packetType = packetType;
        this.receiverMacAddress = receiverMacAddress;
        this.timeToLive = 3;
        if (receiverMacAddress == null) {
            this.receiverMacAddress = "00:00:00:00:00:00";
        }
        this.senderMac = senderMac;
    }

    public Packet(PacketType packetType, byte[] data, String receiverMacAddress, String senderMac, int timeToLive) {
        this.data = data;
        this.packetType = packetType;
        this.receiverMacAddress = receiverMacAddress;
        if (receiverMacAddress == null) {
            this.receiverMacAddress = "00:00:00:00:00:00";
        }
        this.senderMac = senderMac;
        this.timeToLive = timeToLive;
    }

    byte[] getData() {
        return data;
    }

    PacketType getPacketType() {
        return packetType;
    }

    public byte[] serialize(Utils utils) {
        // 6 bytes for mac
        byte[] serialized = new byte[1 + data.length + 13];
        serialized[0] = (byte) packetType.ordinal();
        serialized[1] = (byte) timeToLive;
        byte[] mac = utils.getMacAsBytes(this.receiverMacAddress);
        for (int i = 2; i <= 7; i++) {
            serialized[i] = mac[i - 2];
        }
        mac = utils.getMacAsBytes(this.senderMac);
        for (int i = 8; i <= 13; i++) {
            serialized[i] = mac[i - 8];
        }
        for (int i = 14; i < serialized.length; i++) {
            serialized[i] = data[i - 14];
        }
        return serialized;
    }

    public String getMacAddress() {
        return receiverMacAddress;
    }

    String getSenderMac() {
        return senderMac;
    }

    String getSenderIP() {
        return senderIP;
    }

    public void setSenderIP(String senderIP) {
        this.senderIP = senderIP;
    }

    @Override
    public String toString() {
        return "Type" + getPacketType().toString() + "receiver:" + getMacAddress() + "sender:" + getSenderMac();
    }

    int getTimeToLive() {
        return timeToLive;
    }

    void setTimeToLive(int timeToLive) {
        this.timeToLive = timeToLive;
    }
}
