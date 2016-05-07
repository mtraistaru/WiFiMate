package com.ancestor.wifimate.peer;

import com.ancestor.wifimate.utils.Utils;

import javax.inject.Inject;

/**
 * Created by Mihai.Traistaru on 23.10.2015
 */
public class Packet {

    @Inject
    Utils utils;

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

    public byte[] getData() {
        return data;
    }

    public PacketType getPacketType() {
        return packetType;
    }

    public byte[] serialize() {
        // 6 bytes for mac
        byte[] serialized = new byte[1 + data.length + 13];
        serialized[0] = (byte) packetType.ordinal();
        serialized[1] = (byte) timeToLive;
        byte[] mac = utils.getMacAsBytes(this.receiverMacAddress);
        System.arraycopy(mac, 0, serialized, 2, 6);
        mac = utils.getMacAsBytes(this.senderMac);
        System.arraycopy(mac, 0, serialized, 8, 6);
        System.arraycopy(data, 0, serialized, 14, serialized.length - 14);
        return serialized;
    }

    public String getMacAddress() {
        return receiverMacAddress;
    }

    public String getSenderMac() {
        return senderMac;
    }

    public String getSenderIP() {
        return senderIP;
    }

    public void setSenderIP(String senderIP) {
        this.senderIP = senderIP;
    }

    @Override
    public String toString() {
        return "Type" + getPacketType().toString() + "receiver:" + getMacAddress() + "sender:" + getSenderMac();
    }

    public int getTimeToLive() {
        return timeToLive;
    }

    public void setTimeToLive(int timeToLive) {
        this.timeToLive = timeToLive;
    }
}
