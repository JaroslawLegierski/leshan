/*
 * Copyright (C) 2022 java-coap contributors (https://github.com/open-coap/java-coap)
 * Copyright (C) 2011-2021 ARM Limited. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.leshan.client.javacoap.endpoint.providers;

import java.net.InetSocketAddress;
import java.security.KeyStore;

import org.eclipse.leshan.client.javacoap.endpoint.TransportProvider;
import org.eclipse.leshan.client.javacoap.endpoint.stdio.StreamBlockingTransport;

import com.mbed.coap.packet.Opaque;
import com.mbed.coap.transport.CoapTransport;
import com.mbed.coap.transport.javassl.CoapSerializer;

public class StandardIoProvider extends TransportProvider {

    @Override public CoapTransport createTCP(CoapSerializer coapSerializer, InetSocketAddress destAdr, KeyStore ks) {
        return create(coapSerializer, destAdr);
    }

    @Override
    public CoapTransport createUDP(CoapSerializer coapSerializer, InetSocketAddress destAdr, KeyStore ks,
            Pair<String, Opaque> psk) {
        return create(coapSerializer, destAdr);
    }

    private CoapTransport create(CoapSerializer coapSerializer, InetSocketAddress destAdr) {
        return StreamBlockingTransport.forStandardIO(destAdr, coapSerializer);
    }

}
