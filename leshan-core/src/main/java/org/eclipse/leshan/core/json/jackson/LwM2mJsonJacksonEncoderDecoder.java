/*******************************************************************************
 * Copyright (c) 2020 Sierra Wireless and others.
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
 *     Gemalto M2M GmbH
 *******************************************************************************/

package org.eclipse.leshan.core.json.jackson;

import java.io.IOException;

import org.eclipse.leshan.core.json.JsonRootObject;
import org.eclipse.leshan.core.json.LwM2mJsonDecoder;
import org.eclipse.leshan.core.json.LwM2mJsonEncoder;
import org.eclipse.leshan.core.json.LwM2mJsonException;
import org.eclipse.leshan.core.util.json.JsonException;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.JsonNodeFeature;

/**
 * Helper for encoding/decoding LWM2M JSON format using jackson
 */
public class LwM2mJsonJacksonEncoderDecoder implements LwM2mJsonDecoder, LwM2mJsonEncoder {

    private final JsonRootObjectSerDes serDes;
    private final ObjectMapper mapper;

    public LwM2mJsonJacksonEncoderDecoder() {
        this(new JsonRootObjectSerDes(), null);
    }

    public LwM2mJsonJacksonEncoderDecoder(JsonRootObjectSerDes serDes, ObjectMapper objectMapper) {
        this.serDes = serDes;
        this.mapper = objectMapper == null ? createDefaultObjectMapper() : objectMapper;
    }

    protected ObjectMapper createDefaultObjectMapper() {
        return new ObjectMapper().configure(JsonNodeFeature.STRIP_TRAILING_BIGDECIMAL_ZEROES, false)
                .configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true);
    }

    @Override
    public String toJsonLwM2m(JsonRootObject jro) throws LwM2mJsonException {
        try {
            return serDes.sSerialize(jro);
        } catch (JsonException e) {
            throw new LwM2mJsonException("Unable to serialize LWM2M JSON.", e);
        }
    }

    @Override
    public JsonRootObject fromJsonLwM2m(String jsonString) throws LwM2mJsonException {
        try {
            return serDes.deserialize(mapper.readTree(jsonString));
        } catch (JsonException | IOException e) {
            throw new LwM2mJsonException("Unable to parse LWM2M JSON.", e);
        }
    }
}
