/*******************************************************************************
 * Copyright (c) 2024 Sierra Wireless and others.
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
package org.eclipse.leshan.core.endpoint;

import java.net.InetSocketAddress;
import java.net.URI;

/**
 * Class responsible to handle {@link EndpointUri}
 */
public interface EndPointUriHandler {

    EndPointUriParser getParser();

    EndpointUri createUri(String scheme, InetSocketAddress addr);

    EndpointUri createUri(String uri);

    EndpointUri createUri(URI uri);

    EndpointUri replaceAddress(EndpointUri originalUri, InetSocketAddress newAddress);

    InetSocketAddress getSocketAddr(EndpointUri uri);

    void validateURI(EndpointUri uri) throws InvalidEndpointUriException;
}