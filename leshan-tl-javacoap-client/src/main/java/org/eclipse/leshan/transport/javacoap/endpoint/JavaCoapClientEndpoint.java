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
package org.eclipse.leshan.transport.javacoap.endpoint;

import java.net.URI;

import org.eclipse.leshan.client.endpoint.ClientEndpointToolbox;
import org.eclipse.leshan.client.endpoint.LwM2mClientEndpoint;
import org.eclipse.leshan.client.servers.ServerIdentity;
import org.eclipse.leshan.core.endpoint.Protocol;
import org.eclipse.leshan.core.model.LwM2mModel;
import org.eclipse.leshan.core.request.UplinkRequest;
import org.eclipse.leshan.core.response.ErrorCallback;
import org.eclipse.leshan.core.response.LwM2mResponse;
import org.eclipse.leshan.core.response.ResponseCallback;
import org.eclipse.leshan.transport.javacoap.request.JavaCoapClientCoapMessageTranslator;

import com.mbed.coap.client.CoapClient;
import com.mbed.coap.exception.CoapException;
import com.mbed.coap.packet.CoapRequest;
import com.mbed.coap.packet.CoapResponse;
import com.mbed.coap.server.CoapServer;

public class JavaCoapClientEndpoint implements LwM2mClientEndpoint {

    private final URI endpointUri;
    private final CoapClient client;
    private final JavaCoapClientCoapMessageTranslator translator;
    private final ClientEndpointToolbox toolbox;
    private final LwM2mModel model;



    public JavaCoapClientEndpoint(URI endpointUri, CoapServer coapServer, CoapClient client,
            JavaCoapClientCoapMessageTranslator translator, ClientEndpointToolbox toolbox, LwM2mModel model)
    {
        this.endpointUri = endpointUri;
        this.client = client;
        this.translator = translator;
        this.toolbox = toolbox;
        this.model = model;

    }

    @Override public Protocol getProtocol() {
        return Protocol.COAP;
    }

    @Override public URI getURI() {
        return endpointUri;
    }

    @Override public void forceReconnection(ServerIdentity server, boolean resume) {

    }

    @Override public long getMaxCommunicationPeriodFor(long lifetimeInMs){
        // See https://github.com/OpenMobileAlliance/OMA_LwM2M_for_Developers/issues/283 to better understand.
        // TODO For DTLS, worst Handshake scenario should be taking into account too.

        int floor = 30000; // value from which we stop to adjust communication period using COAP EXCHANGE LIFETIME.

        // To be sure registration doesn't expired, update request should be send considering all CoAP retransmissions
        // and registration lifetime.
        // See https://tools.ietf.org/html/rfc7252#section-4.8.2
        //long exchange_lifetime = lwm2mendpoint.getConfig().get(CoapConfig.EXCHANGE_LIFETIME, TimeUnit.MILLISECONDS);

        long exchange_lifetime = 247000;
        if (lifetimeInMs - exchange_lifetime >= floor) {
            return lifetimeInMs - exchange_lifetime;
        } else {
         //   LOG.warn("Too small lifetime : we advice to not use a lifetime < (COAP EXCHANGE LIFETIME + 30s)");
            // lifetime value is too short, so we do a compromise and we don't remove COAP EXCHANGE LIFETIME completely
            // We distribute the remaining lifetime range [0, exchange_lifetime + floor] on the remaining range
            // [1,floor]s.
            return lifetimeInMs * (floor - 1000) / (exchange_lifetime + floor) + 1000;
        }
    }

    @Override public <T extends LwM2mResponse> T send(ServerIdentity server, UplinkRequest<T> request, long timeoutInMs)
            throws InterruptedException {

        final CoapRequest coapRequest = translator.createCoapRequest(server, request, toolbox, model);

        CoapResponse coapResponse = null;

        // Send CoAP request synchronously
        try {
            coapResponse = client.sendSync(coapRequest);
        } catch (CoapException e) {
            e.printStackTrace();
        }


        return translator.createLwM2mResponse(server, request, coapRequest, coapResponse, toolbox);

    }

    @Override
    public <T extends LwM2mResponse> void send(ServerIdentity server, UplinkRequest<T> request,
            ResponseCallback<T> responseCallback, ErrorCallback errorCallback, long timeoutInMs) {



    }
}
