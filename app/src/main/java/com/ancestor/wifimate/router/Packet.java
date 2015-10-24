package com.ancestor.wifimate.router;

/**
 * The packet structure.
 *
 * Created by Mihai.Traistaru on 23.10.2015
 */
public class Packet {

    private byte[] data;

    private Packet.TYPE type;
    private String receiverMacAddress;
    private String senderMac;
    private String senderIP;
    private int TTL;

    /**
     * Constructor for a Packet object with a TTL of 3.
     *
     * @param type the packet type.
     * @param extraData the packet extra data byte array.
     * @param receiverMacAddress the receiver MAC address.
     * @param senderMac the MAC address of the sender.
     */
    public Packet(TYPE type, byte[] extraData, String receiverMacAddress, String senderMac) {
        this.setData(extraData);
        this.setType(type);
        this.receiverMacAddress = receiverMacAddress;
        this.setTTL(3);
        if (receiverMacAddress == null) {
            this.receiverMacAddress = "00:00:00:00:00:00";
        }
        this.senderMac = senderMac;
    }

    /**
     * Constructor for a Packet object with a custom TTL.
     *
     * @param type the packet type.
     * @param extraData the packet extra data byte array.
     * @param receiverMacAddress the receiver MAC address.
     * @param senderMac the MAC address of the sender.
     * @param TTL the time to live.
     */
    public Packet(TYPE type, byte[] extraData, String receiverMacAddress, String senderMac, int TTL) {
        this.setData(extraData);
        this.setType(type);
        this.receiverMacAddress = receiverMacAddress;
        if (receiverMacAddress == null) {
            this.receiverMacAddress = "00:00:00:00:00:00";
        }
        this.senderMac = senderMac;
        this.TTL = TTL;
    }

    /**
     * Helper function to get a MAC address string as byte array.
     *
     * @param macAddress string representing the MAC address.
     * @return the byte array representing the MAC address.
     */
    public static byte[] getMacAsBytes(String macAddress) {
        byte[] mac = new byte[6]; // macAddress.split(":").length == 6 bytes
        for (int i = 0; i < macAddress.split(":").length; i++) {
            mac[i] = Integer.decode("0x" + macAddress.split(":")[i]).byteValue();
        }
        return mac;
    }

    /**
     * Helper function to get a byte array of data with an offset and use the next six bytes to make a MAC address string.
     *
     * @param data the byte array.
     * @param startOffset the start offset.
     * @return string representing the MAC address.
     */
    public static String getMacBytesAsString(byte[] data, int startOffset) {
        StringBuilder sb = new StringBuilder(18);
        for (int i = startOffset; i < startOffset + 6; i++) {
            byte b = data[i];
            if (sb.length() > 0)
                sb.append(':');
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * Deserialize a packet according to a predefined structure.
     *
     * @param inputData the byte array data representing a serialized Packet object.
     * @return the Packet object constructed.
     */
    public static Packet deserialize(byte[] inputData) {
        Packet.TYPE type = TYPE.values()[(int) inputData[0]];
        byte[] data = new byte[inputData.length - 14];
        int timeToLive = (int) inputData[1];
        String mac = getMacBytesAsString(inputData, 2);
        String receiverMacAddress = getMacBytesAsString(inputData, 8);
        System.arraycopy(inputData, 14, data, 0, inputData.length - 14);
        return new Packet(type, data, mac, receiverMacAddress, timeToLive);
    }

    /**
     * Getter for the data (message body).
     *
     * @return the byte array representing the data.
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Setter for the data (message body).
     *
     * @param data the byte array representing the data.
     */
    public void setData(byte[] data) {
        this.data = data;
    }

    /**
     * Getter for the type of packet.
     *
     * @return the type of the packet.
     */
    public TYPE getType() {
        return type;
    }

    /**
     * Setter for the type of packet.
     *
     * @param type the type of the packet.
     */
    public void setType(TYPE type) {
        this.type = type;
    }

    /**
     * Serialize a packet according to the predefined structure.
     *
     * @return the byte array data representing a serialized Packet object.
     */
    public byte[] serialize() {

        // 6 bytes for mac
        byte[] serialized = new byte[1 + data.length + 13];
        serialized[0] = (byte) type.ordinal();
        serialized[1] = (byte) TTL;
        byte[] mac = getMacAsBytes(this.receiverMacAddress);
        System.arraycopy(mac, 0, serialized, 2, 6);
        mac = getMacAsBytes(this.senderMac);
        System.arraycopy(mac, 0, serialized, 8, 6);
        System.arraycopy(data, 0, serialized, 14, serialized.length - 14);
        return serialized;
    }

    /**
     * Getter for the receiver's MAC address.
     *
     * @return string representing the receiver's MAC address.
     */
    public String getMacAddress() {
        return receiverMacAddress;
    }

    /**
     * Setter for the receiver's MAC address.
     *
     * @param macAddress string representing the receiver's MAC address.
     */
    public void setMac(String macAddress) {
        this.receiverMacAddress = macAddress;
    }

    /**
     * Getter for the sender's MAC address.
     *
     * @return string representing the sender's MAC address.
     */
    public String getSenderMac() {
        return senderMac;
    }

    /**
     * Getter for the sender's IP address.
     *
     * @return string representing the sender's IP address.
     */
    public String getSenderIP() {
        return senderIP;
    }

    /**
     * Setter for the sender's IP address.
     *
     * @param senderIP string representing the sender's IP address.
     */
    public void setSenderIP(String senderIP) {
        this.senderIP = senderIP;
    }

    /**
     * Serializes a Packet object into a string.
     *
     * @return the string representation of the Packet.
     */
    @Override
    public String toString() {
        return "Type" + getType().toString() + "receiver:" + getMacAddress() + "sender:" + getSenderMac();
    }

    /**
     * Getter for the TTL
     *
     * @return the TTL
     */
    public int getTTL() {
        return TTL;
    }

    /**
     * Setter for the TTL.
     *
     * @param TTL the TTL to set.
     */
    public void setTTL(int TTL) {
        this.TTL = TTL;
    }

    /**
     * Types of echo packets
     *
     * Created by Mihai.Traistaru on 23.10.2015
     */
    public enum TYPE {
        HELLO,
        HELLO_ACK,
        BYE,
        MESSAGE,
        UPDATE
    }
}
