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

import java.util.Random;

import org.eclipse.leshan.client.resource.BaseInstanceEnabler;
import org.eclipse.leshan.client.servers.LwM2mServer;
import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.core.response.WriteResponse;
import org.eclipse.leshan.core.util.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionIdentity extends BaseInstanceEnabler {

    private static final Logger LOG = LoggerFactory.getLogger(ConnectionIdentity.class);

    private String ID = "ID0";
    private String PSKIdentity = "PSKIdentity";
    private byte[] PSKSecretKey = Hex.decodeHex("1234567890".toCharArray());

    private final Random random = new Random();

    @Override
    public synchronized ReadResponse read(LwM2mServer server, int resourceId) {
        switch (resourceId) {
        case 0:
            return ReadResponse.success(resourceId, ID);
        case 1:
            return ReadResponse.success(resourceId, PSKIdentity);
        case 2:
            return ReadResponse.success(resourceId, PSKSecretKey);
        default:
            return ReadResponse.notFound();
        }
    }

    public synchronized WriteResponse write(LwM2mServer server, boolean replace, int resourceId, LwM2mResource value) {
        LOG.info("Write on Device resource /{}/{}/{}", getModel().id, getId(), resourceId);
        switch (resourceId) {
        case 0:
            ID = (String) value.getValue();
            break;
        case 1:
            PSKIdentity = (String) value.getValue();
            break;
        case 2:
            PSKSecretKey = (byte[]) value.getValue();
            return WriteResponse.success();
        default:
            return WriteResponse.notFound();
        }
        return WriteResponse.success();
    }
}
