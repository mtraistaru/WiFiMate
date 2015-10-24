package com.ancestor.wifimate.router;

import android.util.Log;

import com.ancestor.wifimate.config.Configuration;

import java.util.concurrent.ConcurrentHashMap;

/**
 * A manager for keeping track of a mesh and handling routing.
 * Created by Mihai.Traistaru on 23.10.2015
 */
public class MeshNetworkRouter {

    private static final String TAG = MeshNetworkRouter.class.getName();

    /**
     * The routing table.
     */
    public static ConcurrentHashMap<String, Peer> routingTable = new ConcurrentHashMap<>();

    /**
     * Self image of the peer.
     */
    private static Peer self;

    /**
     * Introduce a new peer into the routing table.
     *
     * @param peer the peer to be added
     */
    public static void newPeer(Peer peer) {
        routingTable.put(peer.getMacAddress(), peer);
    }

    /**
     * A peer has left the routing table.
     *
     * @param peer the peer to be removed.
     */
    public static void peerGone(Peer peer) {
        routingTable.remove(peer.getMacAddress());
    }

    /**
     * Getter for the self image of the peer.
     *
     * @return the peer
     */
    public static Peer getSelf() {
        return self;
    }

    /**
     * Setter for the self image of the peer.
     *
     * @param self the peer image to set.
     */
    public static void setSelf(Peer self) {
        MeshNetworkRouter.self = self;
        newPeer(self);
    }

    /**
     * Either returns the IP in the current network if on the same one
     * or sends to the relevant Group Owner
     * or sends to all group owners if group owner not in the mesh
     *
     * @param peer the peer for which to get the IP address
     */
    public static String getPeerIPAddress(Peer peer) {

        if (self.getGroupOwnerMacAddress().equals(peer.getGroupOwnerMacAddress())) {
            Log.d(TAG, "Have the same group owner, sending to :" + peer.getIpAddress());
            return peer.getIpAddress(); // shares the same Group-Owner, so it's okay to use its IP
        }

        Peer groupOwner = routingTable.get(peer.getGroupOwnerMacAddress());

        if (self.getGroupOwnerMacAddress().equals(self.getMacAddress())) { // this is the group owner so can propagate
            if (!self.getGroupOwnerMacAddress().equals(peer.getGroupOwnerMacAddress()) && groupOwner.isDirectLink()) {
                return peer.getIpAddress(); // not the same group owner, but we have the group owner as a direct link
            } else if (groupOwner != null && !self.getGroupOwnerMacAddress().equals(peer.getGroupOwnerMacAddress()) && !groupOwner.isDirectLink()) {
                for (Peer p : routingTable.values()) {
                    if (p.getGroupOwnerMacAddress().equals(p.getMacAddress())) {
                        return p.getIpAddress(); //try sending it to a random group owner; can also expand this to all group owners
                    }
                }
                return "0.0.0.0"; // no other group owners, don't know who to send it to
            }
        } else if (groupOwner != null) { // not the group owner - need to sent it to my group owner
            return Configuration.GROUP_OWNER_IP_ADDRESS;
        }

        return "0.0.0.0"; // drop the packet
    }

    /**
     * Serialize the routing table by one serialized Peer per line.
     *
     * @return a byte vector holding the routing table serialized.
     */
    public static byte[] serializeRoutingTable() {
        StringBuilder serialized = new StringBuilder();
        for (Peer v : routingTable.values()) {
            serialized.append(v.toString());
            serialized.append("\n");
        }
        return serialized.toString().getBytes();
    }

    /**
     * Deserialize a routing table and populate the existing one with the data.
     *
     * @param routingTable the routing table to deserialize.
     */
    public static void deserializeRoutingTableAndAdd(byte[] routingTable) {
        String[] div = new String(routingTable).split("\n");
        for (String s : div) {
            Peer peer = Peer.fromString(s);
            MeshNetworkRouter.routingTable.put(peer.getMacAddress(), peer);
        }
    }

    /**
     * Either returns the IP in the current network if on the same one
     * or sends to the relevant Group Owner
     * or sends to all group owners if group owner not in mesh.
     *
     * @param macAddress the ma address of the peer in the routing table.
     */
    public static String getPeerIPAddress(String macAddress) {
        Peer c = routingTable.get(macAddress);
        if (c == null) {
            Log.d(TAG, "NULL ENTRY in ROUTING TABLE FOR MAC");
            return Configuration.GROUP_OWNER_IP_ADDRESS;
        }
        return getPeerIPAddress(c);
    }
}
