/*******************************************************************************
 * Copyright (c) 2017 Bosch Software Innovations GmbH and others.
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
 *    Achim Kraus (Bosch Software Innovations GmbH) - initial implementation.
 *    Orange - keep one JSON dependency
 ******************************************************************************/
package org.eclipse.leshan.server.redis.serialization;

import java.security.PublicKey;

import org.eclipse.leshan.core.request.Identity;
import org.eclipse.leshan.core.util.Hex;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class CustomIdentitySer {

    private static final String KEY_ADDRESS = "address";
    private static final String KEY_PORT = "port";
    private static final String KEY_ID = "id";
    private static final String KEY_CN = "cn";
    private static final String KEY_RPK = "rpk";

    public static JsonNode serialize(Identity identity) {
        ObjectNode o = JsonNodeFactory.instance.objectNode();

        if (identity.isPSK()) {
            o.put(KEY_ID, identity.getPskIdentity());
        } else if (identity.isRPK()) {
            PublicKey publicKey = identity.getRawPublicKey();
            o.put(KEY_RPK, Hex.encodeHexString(publicKey.getEncoded()));
        } else if (identity.isX509()) {
            o.put(KEY_CN, identity.getX509CommonName());
        } else if (!identity.isSecure()) {
            o.put(KEY_ADDRESS, identity.getPeerAddress().getHostString());
            o.put(KEY_PORT, identity.getPeerAddress().getPort());
        }
        return o;
    }

}
