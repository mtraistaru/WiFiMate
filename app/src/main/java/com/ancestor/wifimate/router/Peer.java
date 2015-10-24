package com.ancestor.wifimate.router;

/**
 * This is an alternative representation the Android P2P library's WiFiP2PDevice class.
 * It contains information about any peer connected to the mesh and is stored in the routing table.
 * Created by Mihai.Traistaru on 23.10.2015
 */
public class Peer {

    /**
     * The peer's macAddress address.
     */
    private String macAddress;

    /**
     * The peer's name.
     */
    private String name;

    /**
     * The peer's Group-Owner MAC address, for routing.
     */
    private String groupOwnerMacAddress;

    /**
     * The peer's IP address.
     */
    private String ipAddress;

    /**
     * Whether it is a direct link or not, this could help with making routing more efficient.
     */
    private boolean directLink;

    /**
     * Constructor method for the p2p peer.
     *
     * @param macAddress the macAddress address of the peer.
     * @param ipAddress  the IP address of the peer.
     * @param name       the name of the peer.
     * @param groupOwner the group owner of the p2p network.
     */
    public Peer(String macAddress, String ipAddress, String name, String groupOwner) {
        this.setIpAddress(ipAddress);
        this.setMacAddress(macAddress);
        this.setName(name);
        this.setGroupOwnerMacAddress(groupOwner);
        this.directLink = true;
    }

    /**
     * Generate a peer object from a serialized string.
     *
     * @param serialized the string serialized representation of the p2p peer.
     * @return the p2p peer object.
     */
    public static Peer fromString(String serialized) {
        return new Peer(serialized.split(",")[0], serialized.split(",")[1], serialized.split(",")[2], serialized.split(",")[3]);
    }

    /**
     * Getter for the directLink boolean.
     *
     * @return true if we have a direct link to this peer from the current running peer.
     */
    public boolean isDirectLink() {
        return directLink;
    }

    /**
     * Setter for the directLink boolean.
     *
     * @param directLink true if we have a direct link to this peer from the current running peer.
     */
    public void setDirectLink(boolean directLink) {
        this.directLink = directLink;
    }

    /**
     * Getter for the Group-Owner macAddress address.
     *
     * @return the macAddress address of the group owner.
     */
    public String getGroupOwnerMacAddress() {
        return groupOwnerMacAddress;
    }

    /**
     * Setter for the Group-Owner macAddress address.
     *
     * @param groupOwnerMacAddress string representing the Group-Owner macAddress address.
     */
    public void setGroupOwnerMacAddress(String groupOwnerMacAddress) {
        this.groupOwnerMacAddress = groupOwnerMacAddress;
    }

    /**
     * Getter for the peer's name.
     *
     * @return string representing the peer's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Setter for the peer's name (like a nickname or something for future use).
     *
     * @param name string representing the peer's name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter for the peer's macAddress address.
     *
     * @return string representing the peer's macAddress address.
     */
    public String getMacAddress() {
        return macAddress;
    }

    /**
     * Setter for the peer's macAddress address.
     *
     * @param macAddress string representing the peer's macAddress address.
     */
    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    /**
     * Getter for the peer's IP address.
     *
     * @return string representing the peer's IP address.
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * Setter for the peer's IP address.
     *
     * @param ipAddress string representing the peer's IP address.
     */
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     * Serialize this peer's information into a comma delimited form
     */
    @Override
    public String toString() {
        return getIpAddress() + "," + getMacAddress() + "," + getName() + "," + getGroupOwnerMacAddress();
    }
}
