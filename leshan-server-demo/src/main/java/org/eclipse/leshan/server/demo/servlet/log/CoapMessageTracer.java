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
import org.eclipse.leshan.server.demo.servlet.statistics.ConnectionStatistics;
import org.eclipse.leshan.server.registration.Registration;
import org.eclipse.leshan.server.registration.RegistrationService;

public class CoapMessageTracer implements MessageInterceptor {

    private final Map<String, CoapMessageListener> listeners = new ConcurrentHashMap<>();

    private final RegistrationService registry;

    private final ConnectionStatistics connectionStatistics;

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

    public CoapMessageTracer(RegistrationService registry, ConnectionStatistics connectionStatistics) {
        this.registry = registry;
        this.connectionStatistics = connectionStatistics;
    }

    @Override
    public void sendRequest(Request request) {
        CoapMessageListener listener = listeners.get(toStringAddress(request.getDestinationContext().getPeerAddress()));
        if (listener != null) {
            listener.trace(new CoapMessage(request, false));
        }
        connectionStatistics.reportSendRequest(request);
    }

    @Override
    public void sendResponse(Response response) {
        CoapMessageListener listener = listeners
                .get(toStringAddress(response.getDestinationContext().getPeerAddress()));
        if (listener != null) {
            listener.trace(new CoapMessage(response, false));
        }
        connectionStatistics.reportSendResponse(response);
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
        connectionStatistics.reportReceiveRequest(request);
    }

    @Override
    public void receiveResponse(Response response) {
        CoapMessageListener listener = listeners.get(toStringAddress(response.getSourceContext().getPeerAddress()));
        if (listener != null) {
            listener.trace(new CoapMessage(response, true));
        }
        connectionStatistics.reportReceiveResponse(response);
    }

    @Override
    public void receiveEmptyMessage(EmptyMessage message) {
        CoapMessageListener listener = listeners.get(toStringAddress(message.getSourceContext().getPeerAddress()));
        if (listener != null) {
            listener.trace(new CoapMessage(message, true));
        }

    }

}
