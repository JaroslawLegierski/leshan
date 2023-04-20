package org.eclipse.leshan.core.request;

import java.net.InetSocketAddress;

public class IpPeer implements Peer {

    private final InetSocketAddress peerAddress;
    private final LwM2MIdentity identity;


    public IpPeer(InetSocketAddress peerAddress) {
        this.peerAddress = peerAddress;
        this.identity = null;
    }

    public IpPeer(InetSocketAddress peerAddress, PskIdentity pskidentity) {
        this.peerAddress = peerAddress;
        this.identity = pskidentity;
    }

    public IpPeer(InetSocketAddress peerAddress, RpkIdentity identity) {
        this.peerAddress = peerAddress;
        this.identity = identity;
    }

    public IpPeer(InetSocketAddress peerAddress,  X509Identity identity) {
        this.peerAddress = peerAddress;
        this.identity = identity;
    }


    @Override
    public LwM2MIdentity getIdentity() {
        return identity;
    }

    public InetSocketAddress getSocketAddress() {
        return peerAddress;
    }
}
