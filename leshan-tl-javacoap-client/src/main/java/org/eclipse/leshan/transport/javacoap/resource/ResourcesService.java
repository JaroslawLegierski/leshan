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
package org.eclipse.leshan.transport.javacoap.resource;

import static java.util.concurrent.CompletableFuture.completedFuture;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.mbed.coap.packet.CoapRequest;
import com.mbed.coap.packet.CoapResponse;
import com.mbed.coap.utils.Service;

public class ResourcesService implements Service<CoapRequest, CoapResponse> {

    private final Map<RequestMatcher, Service<CoapRequest, CoapResponse>> handlers;

    public static ResourcesBuilder builder() {
        return new ResourcesBuilder();
    }

    public final static Service<CoapRequest, CoapResponse> NOT_FOUND_SERVICE = request -> completedFuture(
            CoapResponse.notFound());

    ResourcesService(Map<RequestMatcher, Service<CoapRequest, CoapResponse>> handlers) {

        this.handlers = Collections
                .unmodifiableMap(handlers.entrySet().stream()
                        .collect(Collectors.toMap(Entry::getKey, Entry::getValue)));

    }

    @Override
    public CompletableFuture<CoapResponse> apply(CoapRequest request) {
        RequestMatcher requestMatcher = new RequestMatcher(request.options().getUriPath());

        return handlers.getOrDefault(requestMatcher, NOT_FOUND_SERVICE).apply(request);

    }

      public static class ResourcesBuilder {
        private final Map<RequestMatcher, Service<CoapRequest, CoapResponse>> handlers = new HashMap<>();

        public ResourcesBuilder add(String uriPath, Service<CoapRequest, CoapResponse> service) {
            handlers.put(new RequestMatcher(uriPath), service);
            return this;
        }

        public ResourcesService build() {
            return new ResourcesService(handlers);
        }
    }

    static final class RequestMatcher {
        private final String uriPath;

        RequestMatcher(String uriPath) {
         this.uriPath = uriPath;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            RequestMatcher that = (RequestMatcher) o;
            return Objects.equals(uriPath, that.uriPath);
        }

        @Override
        public int hashCode() {
            return Objects.hash(uriPath);
        }
    }
}
