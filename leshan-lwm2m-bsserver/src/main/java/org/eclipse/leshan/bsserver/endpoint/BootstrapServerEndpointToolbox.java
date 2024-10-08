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
package org.eclipse.leshan.bsserver.endpoint;

import org.eclipse.leshan.core.endpoint.EndPointUriHandler;
import org.eclipse.leshan.core.link.lwm2m.LwM2mLinkParser;
import org.eclipse.leshan.core.node.codec.LwM2mDecoder;
import org.eclipse.leshan.core.node.codec.LwM2mEncoder;

public class BootstrapServerEndpointToolbox {

    private final LwM2mDecoder decoder;
    private final LwM2mEncoder encoder;
    private final LwM2mLinkParser linkParser;
    private final EndPointUriHandler uriHandler;

    public BootstrapServerEndpointToolbox(LwM2mDecoder decoder, LwM2mEncoder encoder, LwM2mLinkParser linkParser,
            EndPointUriHandler uriHandler) {
        this.decoder = decoder;
        this.encoder = encoder;
        this.linkParser = linkParser;
        this.uriHandler = uriHandler;
    }

    public LwM2mDecoder getDecoder() {
        return decoder;
    }

    public LwM2mEncoder getEncoder() {
        return encoder;
    }

    public LwM2mLinkParser getLinkParser() {
        return linkParser;
    }

    public EndPointUriHandler getUriHandler() {
        return uriHandler;
    }
}
