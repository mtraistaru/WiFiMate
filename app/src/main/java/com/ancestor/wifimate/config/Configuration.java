package com.ancestor.wifimate.config;

/**
 * Contains the configuration settings related to the Wi-Fi Direct implementation
 * Created by Mihai.Traistaru on 23.10.2015
 */
public interface Configuration {

    /**
     * The default ports that all clients receive at
     */
    int RECEIVE_PORT = 9999;

    /**
     * The default Group Owner IP address for the initial connections
     */
    String GROUP_OWNER_IP_ADDRESS = "192.168.50.1";

    /**
     * This only works on certain devices where multiple simultaneous connections are available (infrastructure & ad-hoc)
     */
    boolean isDeviceBridgingEnabled = false;
}
