/*******************************************************************************
 * Copyright (c) 2022    Sierra Wireless and others.
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
 *     Orange Polska S.A. -  optional objects support added
 *******************************************************************************/
package org.eclipse.leshan.demo.client;

import org.eclipse.leshan.client.resource.BaseInstanceEnabler;
import org.eclipse.leshan.client.servers.LwM2mServer;
import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.core.response.WriteResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionServiceEndpoint extends BaseInstanceEnabler {

    private static final Logger LOG = LoggerFactory.getLogger(ConnectionServiceEndpoint.class);

    private String ServiceName = "ServiceName";
    private String Payload = "Payload";
    private String ServiceURI = "ServiceURI";
    private String TopicRoot = "Topic Root";
    private byte[] ServerPublicKey = {}; // Server Public Key Base64 encoded

    @Override
    public synchronized ReadResponse read(LwM2mServer server, int resourceId) {
        LOG.info("Read on Device resource /{}/{}/{}", getModel().id, getId(), resourceId);
        switch (resourceId) {
        case 0:
            return ReadResponse.success(resourceId, ServiceName);
        case 1:
            return ReadResponse.success(resourceId, Payload);
        case 2:
            return ReadResponse.success(resourceId, ServiceURI);
        case 3:
            return ReadResponse.success(resourceId, TopicRoot);
        case 4:
            return ReadResponse.success(resourceId, ServerPublicKey);
        default:
            return ReadResponse.notFound();
        }
    }

    public synchronized WriteResponse write(LwM2mServer server, boolean replace, int resourceId, LwM2mResource value) {
        LOG.info("Write on Device resource /{}/{}/{}", getModel().id, getId(), resourceId);
        switch (resourceId) {
        case 0:
            ServiceName = (String) value.getValue();
            break;
        case 1:
            Payload = (String) value.getValue();
            break;
        case 2:
            ServiceURI = (String) value.getValue();
            break;
        case 3:
            TopicRoot = (String) value.getValue();
            break;
        case 4:
            ServerPublicKey = (byte[]) value.getValue();
            break;
        default:
            return WriteResponse.notFound();
        }
        return WriteResponse.success();
    }
}
