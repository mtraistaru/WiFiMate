package com.ancestor.wifimate.peer;

/**
 * Created by Mihai.Traistaru on 23.10.2015
 */
public class CustomWiFiP2PDevice {

    private String macAddress;
    private String name;
    private String groupOwnerMacAddress;
    private String ipAddress;
    private boolean directLink;

    public CustomWiFiP2PDevice(String macAddress, String ipAddress, String name, String groupOwnerMacAddress) {
        this.ipAddress = ipAddress;
        this.macAddress = macAddress;
        this.name = name;
        this.groupOwnerMacAddress = groupOwnerMacAddress;
        this.directLink = true;
    }

    public boolean isDirectLink() {
        return directLink;
    }

    public String getGroupOwnerMacAddress() {
        return groupOwnerMacAddress;
    }

    public void setGroupOwnerMacAddress(String groupOwnerMacAddress) {
        this.groupOwnerMacAddress = groupOwnerMacAddress;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    @Override
    public String toString() {
        return getIpAddress() + "," + getMacAddress() + "," + getName() + "," + getGroupOwnerMacAddress();
    }
}
