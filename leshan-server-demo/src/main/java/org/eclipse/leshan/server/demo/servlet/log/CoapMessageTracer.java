/*******************************************************************************
 * Copyright (c) 2015 Sierra Wireless and others.
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
package org.eclipse.leshan.server.demo.servlet.log;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.californium.core.coap.EmptyMessage;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.network.interceptors.MessageInterceptor;
import org.eclipse.leshan.server.registration.Registration;
import org.eclipse.leshan.server.registration.RegistrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoapMessageTracer implements MessageInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(CoapMessageTracer.class);

    private final Map<String, CoapMessageListener> listeners = new ConcurrentHashMap<>();

    private final RegistrationService registry;

    public void addListener(String endpoint, CoapMessageListener listener) {
        Registration registration = registry.getByEndpoint(endpoint);
        if (registration != null) {
            listeners.put(toStringAddress(registration.getIdentity().getPeerAddress()), listener);
        }
    }

    public void removeListener(String endpoint) {
        Registration registration = registry.getByEndpoint(endpoint);
        if (registration != null) {
            listeners.remove(toStringAddress(registration.getIdentity().getPeerAddress()));
        }
    }

    private String toStringAddress(InetSocketAddress clientAddress) {
        return clientAddress.getAddress() + ":" + clientAddress.getPort();
    }

    public CoapMessageTracer(RegistrationService registry) {
        this.registry = registry;
    }

    @Override
    public void sendRequest(Request request) {
        CoapMessageListener listener = listeners.get(toStringAddress(request.getDestinationContext().getPeerAddress()));
        if (listener != null) {
            listener.trace(new CoapMessage(request, false));
        }
        long servertimestamp = System.currentTimeMillis();
        LOG.info("\n" + "UDP QOS ReceiveRequest: \n" + "MID: " + request.getMID() + "\n" + "endpoint: "
                + request.getURI() + "\n" + "msg: " + request.getType() + "\n" + "rejected: " + request.isRejected()
                + "\n" + "acknowledge: " + request.acknowledge() + "\n" + "timeout: " + request.isTimedOut() + "\n"
                + "duplicated: " + request.isDuplicate() + "\n" + "token: " + request.getTokenString() + "\n"
                + "timestamp: " + request.getNanoTimestamp() + "\n" + "servertimestamp: " + servertimestamp + "\n"
                + "payload: " + request.getPayloadString() + "\n" + "options: " + request.getOptions() + "\n"

        );
        if (request.getSourceContext() != null) {
            for (int i = 0; i < request.getSourceContext().entries().values().size(); i++) {
                if (request.getSourceContext().entries().values().toArray()[i] != null)
                    LOG.info("QoS DTLS:" + request.getSourceContext().entries().keySet().toArray()[i] + ": "
                            + request.getSourceContext().entries().values().toArray()[i] + "\n");
                if (request.getSourceContext().entries().keySet().toArray()[i].toString()
                        .contains("DTLS:DTLS_HANDSHAKE_TIMESTAMP")) {
                    LOG.info("QoS DTLS deltatimeouts:"
                            + (Long.parseLong((String) request.getSourceContext().entries().values().toArray()[i])
                                    - servertimestamp));
                }

            }
        }
    }

    @Override
    public void sendResponse(Response response) {
        CoapMessageListener listener = listeners
                .get(toStringAddress(response.getDestinationContext().getPeerAddress()));
        if (listener != null) {
            listener.trace(new CoapMessage(response, false));
        }
        LOG.info("\n" + "UDP QOS SendResponse: \n" + "MID: " + response.getMID() + "\n" + "msg: " + response.getType()
                + "\n" + "rejected: " + response.isRejected() + "\n" + "acknowledge: " + response.acknowledge() + "\n"
                + "timeout: " + response.isTimedOut() + "\n" + "duplicated: " + response.isDuplicate() + "\n"
                + "token: " + response.getTokenString() + "\n" + "timestamp: " + response.getNanoTimestamp() + "\n"
                + "payload: " + response.getPayloadString() + "\n" + "options: " + response.getOptions() + "\n");
        if (response.getSourceContext() != null) {
            for (int i = 0; i < response.getSourceContext().entries().values().size(); i++) {
                if (response.getSourceContext().entries().values().toArray()[i] != null)
                    LOG.info("QoS DTLS:" + response.getSourceContext().entries().keySet().toArray()[i] + ": "
                            + response.getSourceContext().entries().values().toArray()[i] + "\n");

            }
        }
    }

    @Override
    public void sendEmptyMessage(EmptyMessage message) {
        CoapMessageListener listener = listeners.get(toStringAddress(message.getDestinationContext().getPeerAddress()));
        if (listener != null) {
            listener.trace(new CoapMessage(message, false));
        }
    }

    @Override
    public void receiveRequest(Request request) {
        CoapMessageListener listener = listeners.get(toStringAddress(request.getSourceContext().getPeerAddress()));
        if (listener != null) {
            listener.trace(new CoapMessage(request, true));
        }
        long servertimestamp = System.currentTimeMillis();
        LOG.info("\n" + "UDP QOS ReceiveRequest: \n" + "MID: " + request.getMID() + "\n" + "endpoint: "
                + request.getURI() + "\n" + "msg: " + request.getType() + "\n" + "rejected: " + request.isRejected()
                + "\n" + "acknowledge: " + request.acknowledge() + "\n" + "timeout: " + request.isTimedOut() + "\n"
                + "duplicated: " + request.isDuplicate() + "\n" + "token: " + request.getTokenString() + "\n"
                + "timestamp: " + request.getNanoTimestamp() + "\n" + "servertimestamp: " + servertimestamp + "\n"
                + "payload: " + request.getPayloadString() + "\n" + "options: " + request.getOptions() + "\n"

        );
        if (request.getSourceContext() != null) {
            for (int i = 0; i < request.getSourceContext().entries().values().size(); i++) {
                if (request.getSourceContext().entries().values().toArray()[i] != null)
                    LOG.info("QoS DTLS:" + request.getSourceContext().entries().keySet().toArray()[i] + ": "
                            + request.getSourceContext().entries().values().toArray()[i] + "\n");
                if (request.getSourceContext().entries().keySet().toArray()[i].toString()
                        .equals("DTLS_HANDSHAKE_TIMESTAMP")) {
                    LOG.info("QoS DTLS deltatimeouts:"
                            + (Long.parseLong(request.getSourceContext().entries().values().toArray()[i].toString())
                                    - servertimestamp));
                }

            }
        }
    }

    @Override
    public void receiveResponse(Response response) {
        CoapMessageListener listener = listeners.get(toStringAddress(response.getSourceContext().getPeerAddress()));
        if (listener != null) {
            listener.trace(new CoapMessage(response, true));
        }
        LOG.info("\n" + "UDP QOS ReceiveResponse: \n" + "MID: " + response.getMID() + "\n" + "msg: "
                + response.getType() + "\n" + "rejected: " + response.isRejected() + "\n" + "acknowledge: "
                + response.acknowledge() + "\n" + "timeout: " + response.isTimedOut() + "\n" + "duplicated: "
                + response.isDuplicate() + "\n" + "token: " + response.getTokenString() + "\n" + "timestamp: "
                + response.getNanoTimestamp() + "\n" + "servertimestamp: " + System.currentTimeMillis() + "\n"
                + "payload: " + response.getPayloadString() + "\n" + "options: " + response.getOptions() + "\n");
        if (response.getSourceContext() != null) {
            for (int i = 0; i < response.getSourceContext().entries().values().size(); i++) {
                if (response.getSourceContext().entries().values().toArray()[i] != null)
                    LOG.info("QoS DTLS:" + response.getSourceContext().entries().keySet().toArray()[i] + ": "
                            + response.getSourceContext().entries().values().toArray()[i] + "\n");

            }
        }
    }

    @Override
    public void receiveEmptyMessage(EmptyMessage message) {
        CoapMessageListener listener = listeners.get(toStringAddress(message.getSourceContext().getPeerAddress()));
        if (listener != null) {
            listener.trace(new CoapMessage(message, true));
        }

    }

}
