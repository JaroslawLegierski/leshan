/*******************************************************************************
 * Copyright (c) 2022 Sierra Wireless and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
 *
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 *******************************************************************************/
package org.eclipse.leshan.transport.californium.identity;

import java.net.InetSocketAddress;
import java.security.Principal;

import org.eclipse.californium.core.coap.Message;
import org.eclipse.californium.elements.AddressEndpointContext;
import org.eclipse.californium.elements.EndpointContext;
import org.eclipse.leshan.core.peer.IpPeer;
import org.eclipse.leshan.core.peer.LwM2mPeer;

public class DefaultCoapIdentityHandler implements IdentityHandler {

    @Override
    public LwM2mPeer getIdentity(Message receivedMessage) {
        EndpointContext context = receivedMessage.getSourceContext();
        InetSocketAddress peerAddress = context.getPeerAddress();
        Principal senderIdentity = context.getPeerIdentity();
        if (senderIdentity == null) {
            return new IpPeer(peerAddress);
        }
        return null;
    }

    @Override
    public EndpointContext createEndpointContext(LwM2mPeer client, boolean allowConnectionInitiation) {
        if (client instanceof IpPeer) {
            return new AddressEndpointContext(((IpPeer) client).getSocketAddress());
        } else {
            throw new IllegalStateException(String.format("Unsupported Peer : %s", client));
        }
    }
}
