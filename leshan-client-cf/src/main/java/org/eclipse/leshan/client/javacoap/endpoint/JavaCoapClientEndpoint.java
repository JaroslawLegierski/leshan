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
package org.eclipse.leshan.client.javacoap.endpoint;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.californium.core.config.CoapConfig;
import org.eclipse.californium.core.network.Endpoint;
import org.eclipse.leshan.client.endpoint.ClientEndpointToolbox;
import org.eclipse.leshan.client.endpoint.LwM2mClientEndpoint;
import org.eclipse.leshan.client.javacoap.CaliforniumConnectionController;
import org.eclipse.leshan.client.servers.ServerIdentity;
import org.eclipse.leshan.core.californium.ExceptionTranslator;
import org.eclipse.leshan.core.californium.identity.IdentityHandler;
import org.eclipse.leshan.core.endpoint.Protocol;
import org.eclipse.leshan.core.model.LwM2mModel;
import org.eclipse.leshan.core.request.UplinkRequest;
import org.eclipse.leshan.core.response.ErrorCallback;
import org.eclipse.leshan.core.response.LwM2mResponse;
import org.eclipse.leshan.core.response.ResponseCallback;
import org.eclipse.leshan.core.util.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mbed.coap.client.CoapClient;
import com.mbed.coap.exception.CoapException;
import com.mbed.coap.packet.CoapRequest;
import com.mbed.coap.packet.CoapResponse;

public class JavaCoapClientEndpoint implements LwM2mClientEndpoint {

    private final Logger LOG = LoggerFactory.getLogger(JavaCoapClientEndpoint.class);

    private final Protocol protocol;
    private final ScheduledExecutorService executor;
    private final Endpoint endpoint;

    private final ClientEndpointToolbox toolbox;
    private final JavaCoapClientCoapMessageTranslator translator;
    private final IdentityHandler identityHandler;
    private final CaliforniumConnectionController connectionController;
    private final LwM2mModel model;
    private final ExceptionTranslator exceptionTranslator;
    private CoapClient client;

    public JavaCoapClientEndpoint(Protocol protocol, Endpoint endpoint, JavaCoapClientCoapMessageTranslator translator,
            ClientEndpointToolbox toolbox, IdentityHandler identityHandler,
            CaliforniumConnectionController connectionController, LwM2mModel model,
            ExceptionTranslator exceptionTranslator, ScheduledExecutorService executor,CoapClient client) {
        this.protocol = protocol;
        this.translator = translator;
        this.toolbox = toolbox;
        this.endpoint = endpoint;
        this.identityHandler = identityHandler;
        this.connectionController = connectionController;
        this.model = model;
        this.exceptionTranslator = exceptionTranslator;
        this.executor = executor;
        this.client=client;

    }

    @Override public Protocol getProtocol() {
        return protocol;
    }

    @Override public URI getURI() {
        try {
            return new URI(protocol.getUriScheme(), null, getCoapEndpoint().getAddress().getHostString(),
                    getCoapEndpoint().getAddress().getPort(), null, null, null);
        } catch (URISyntaxException e) {
            // TODO TL : handle this properly
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }

    @Override public long getMaxCommunicationPeriodFor(long lifetimeInMs) {
        // See https://github.com/OpenMobileAlliance/OMA_LwM2M_for_Developers/issues/283 to better understand.
        // TODO For DTLS, worst Handshake scenario should be taking into account too.

        int floor = 30000; // value from which we stop to adjust communication period using COAP EXCHANGE LIFETIME.

        // To be sure registration doesn't expired, update request should be send considering all CoAP retransmissions
        // and registration lifetime.
        // See https://tools.ietf.org/html/rfc7252#section-4.8.2
        long exchange_lifetime = endpoint.getConfig().get(CoapConfig.EXCHANGE_LIFETIME, TimeUnit.MILLISECONDS);
        if (lifetimeInMs - exchange_lifetime >= floor) {
            return lifetimeInMs - exchange_lifetime;
        } else {
            LOG.warn("Too small lifetime : we advice to not use a lifetime < (COAP EXCHANGE LIFETIME + 30s)");
            // lifetime value is too short, so we do a compromise and we don't remove COAP EXCHANGE LIFETIME completely
            // We distribute the remaining lifetime range [0, exchange_lifetime + floor] on the remaining range
            // [1,floor]s.
            return lifetimeInMs * (floor - 1000) / (exchange_lifetime + floor) + 1000;
        }
    }

    @Override
    public <T extends LwM2mResponse> T send(ServerIdentity server, UplinkRequest<T> lwm2mRequest, long timeoutInMs)
            throws InterruptedException {
        // Create the CoAP request from LwM2m request


        final CoapRequest coapRequest = translator.createCoapRequest(server, lwm2mRequest, toolbox, model,
                identityHandler);

        CoapResponse coapResponse = null;

        // Send CoAP request synchronously
        try {
            coapResponse = client.sendSync(coapRequest);
        } catch (CoapException e) {
            e.printStackTrace();
        }


        return translator.createLwM2mResponse(server, lwm2mRequest, coapRequest, coapResponse, toolbox);


    }

    @Override
    public <T extends LwM2mResponse> void send(ServerIdentity server, UplinkRequest<T> lwm2mRequest,
            ResponseCallback<T> responseCallback, ErrorCallback errorCallback, long timeoutInMs) {
        Validate.notNull(responseCallback);
        Validate.notNull(errorCallback);

        final CoapRequest coapRequest = translator.createCoapRequest(server, lwm2mRequest, toolbox, model,
                identityHandler);



        CompletableFuture<CoapResponse> cfcoapResponse = null;
        CoapResponse coapResponse = null;

        cfcoapResponse = client.send(coapRequest);
        try {
            coapResponse = cfcoapResponse.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }



        // Send CoAP request synchronously


        T lwM2mResponse = translator.createLwM2mResponse(server, lwm2mRequest, coapRequest, coapResponse, toolbox);


    }

    @Override public void forceReconnection(ServerIdentity server, boolean resume) {
        connectionController.forceReconnection(endpoint, server, resume);
    }

    public Endpoint getCoapEndpoint() {
        return endpoint;
    }
}
