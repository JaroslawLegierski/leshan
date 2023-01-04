/*******************************************************************************
 * Copyright (c) 2013-2015 Sierra Wireless and others.
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
 *     Zebra Technologies - initial API and implementation
 *     Michał Wadowski (Orange) - Improved compliance with rfc6690
 *******************************************************************************/
package org.eclipse.leshan.transport.javacoap.request;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map.Entry;


import org.eclipse.leshan.core.link.Link;
import org.eclipse.leshan.core.link.LinkSerializer;
import org.eclipse.leshan.core.model.LwM2mModel;
import org.eclipse.leshan.core.node.codec.LwM2mEncoder;
import org.eclipse.leshan.core.request.BindingMode;
import org.eclipse.leshan.core.request.BootstrapRequest;
import org.eclipse.leshan.core.request.ContentFormat;
import org.eclipse.leshan.core.request.DeregisterRequest;
import org.eclipse.leshan.core.request.Identity;
import org.eclipse.leshan.core.request.RegisterRequest;
import org.eclipse.leshan.core.request.SendRequest;
import org.eclipse.leshan.core.request.UpdateRequest;
import org.eclipse.leshan.core.request.UplinkRequest;
import org.eclipse.leshan.core.request.UplinkRequestVisitor;

import com.mbed.coap.packet.CoapRequest;
import com.mbed.coap.packet.Opaque;

/**
 * This class is able to create CoAP request from LWM2M {@link UplinkRequest}.
 * <p>
 * Call <code>CoapRequestBuilder#visit(lwm2mRequest)</code>, then get the result using {@link #getRequest()}
 */
public class JavaCoapRequestBuilder implements UplinkRequestVisitor {

    // protected Request coapRequest;
    protected CoapRequest coapRequest;
    protected final Identity server;
    protected final LwM2mEncoder encoder;
    protected final LwM2mModel model;
    protected final LinkSerializer linkSerializer;


    public JavaCoapRequestBuilder(Identity server, LwM2mEncoder encoder, LwM2mModel model,
            LinkSerializer linkSerializer) {
        this.server = server;
        this.encoder = encoder;
        this.model = model;
        this.linkSerializer = linkSerializer;

    }

    @Override public void visit(BootstrapRequest request) {

        coapRequest = CoapRequest.post("");

        coapRequest.options().setUriPath("bs");

        String uriquery = "";
        //        // @since 1.1
                HashMap<String, String> attributes = new HashMap<>();
                attributes.putAll(request.getAdditionalAttributes());
                attributes.put("ep", request.getEndpointName());
                if (request.getPreferredContentFormat() != null) {
                    attributes.put("pct", Integer.toString(request.getPreferredContentFormat().getCode()));
                }
                for (Entry<String, String> attr : attributes.entrySet()) {

                    if (uriquery.isEmpty()) {

                        uriquery = attr.getKey() + "=" + attr.getValue();
                    } else {
                        uriquery = uriquery + "&" + attr.getKey() + "=" + attr.getValue();

                    }
                }
                coapRequest.options().setUriQuery(uriquery);
    }

    // todo !!!!
    @Override public void visit(RegisterRequest request) {

        coapRequest = CoapRequest.post("");

        String uri = "/rd";
        String uriquery = "";

        HashMap<String, String> attributes = new HashMap<>();
        attributes.putAll(request.getAdditionalAttributes());

        attributes.put("ep", request.getEndpointName());

        Long lifetime = request.getLifetime();
        if (lifetime != null)
            attributes.put("lt", lifetime.toString());

        String smsNumber = request.getSmsNumber();
        if (smsNumber != null)
            attributes.put("sms", smsNumber);

        String lwVersion = request.getLwVersion();
        if (lwVersion != null)
            attributes.put("lwm2m", lwVersion);

        EnumSet<BindingMode> bindingMode = request.getBindingMode();
        if (bindingMode != null)
            attributes.put("b", BindingMode.toString(bindingMode));

        Boolean queueMode = request.getQueueMode();
        if (queueMode != null && queueMode)
            attributes.put("Q", null);

        coapRequest.options().setUriPath(uri);

        for (Entry<String, String> attr : attributes.entrySet()) {
            if (attr.getValue() != null) {
                if (uriquery.isEmpty()) {

                    uriquery = attr.getKey() + "=" + attr.getValue();
                } else {
                    uriquery = uriquery + "&" + attr.getKey() + "=" + attr.getValue();

                }

            } else {
                coapRequest.options().setUriQuery(attr.getKey());

            }
            coapRequest.options().setUriQuery(uriquery);

        }

        Link[] objectLinks = request.getObjectLinks();
        if (objectLinks != null) {
            String payload = linkSerializer.serializeCoreLinkFormat(objectLinks);

            coapRequest = coapRequest.payload(Opaque.of(payload), (short) ContentFormat.LINK.getCode());
            // todo there is no token generator in java-coap
           coapRequest=coapRequest.token(123456);
        }
    }
    @Override public void visit(UpdateRequest request) {

        coapRequest = CoapRequest.post("");


                    coapRequest.options().setUriPath(request.getRegistrationId());

                Long lifetime = request.getLifeTimeInSec();
                if (lifetime != null)

                      coapRequest.options().setUriQuery("lt=" + lifetime);

                String smsNumber = request.getSmsNumber();
                if (smsNumber != null)

                    coapRequest.options().setUriQuery("sms=" + smsNumber);

                EnumSet<BindingMode> bindingMode = request.getBindingMode();
                if (bindingMode != null)

                    coapRequest.options().setUriQuery("b=" + BindingMode.toString(bindingMode));

                Link[] linkObjects = request.getObjectLinks();
                if (linkObjects != null) {

                    coapRequest = coapRequest.payload(Opaque.of(linkSerializer.serializeCoreLinkFormat(linkObjects)),
                            (short) ContentFormat.LINK.getCode());

                }
    }

    @Override public void visit(DeregisterRequest request) {

        coapRequest = CoapRequest.delete("");

        coapRequest.options().setUriPath(request.getRegistrationId());
    }

    @Override public void visit(SendRequest request) {

        coapRequest= CoapRequest.post("");

        coapRequest.options().setUriPath("/dp");
                ContentFormat format = request.getFormat();
                Opaque payload =  Opaque.of(encoder.encodeTimestampedNodes(request.getTimestampedNodes(),format, model ));

          coapRequest = coapRequest.payload(payload, (short) format.getCode());
        coapRequest=coapRequest.token(123457);
    }

    public CoapRequest getRequest() {
        return coapRequest;
    }

}
