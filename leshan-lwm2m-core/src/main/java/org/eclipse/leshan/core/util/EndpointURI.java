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
 *     Jaroslaw Legierski Orange Polska S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.leshan.core.util;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EndpointURI {

    private final String scheme;
    private final String host;
    private final Integer port;

    private final String rfcpattern = "^(([^:/?#]+):)?(//([^/?#]*))?:(\\d+)";
    // based on https://www.ietf.org/rfc/rfc2396.txt annex B pattern + port
    // "^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?";

    public EndpointURI(String scheme, String host, Integer port) {
        this.scheme = scheme;
        this.host = host;
        this.port = port;
    }

    public EndpointURI(String s) {
        Pattern pattern = Pattern.compile(rfcpattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(s);
        matcher.find();
        this.scheme = matcher.group(2);
        this.host = matcher.group(4);
        this.port = Integer.parseInt(matcher.group(5));
    }

    public String getScheme() {
        return scheme;
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        EndpointURI that = (EndpointURI) o;
        return Objects.equals(scheme, that.scheme) && Objects.equals(host, that.host)
                && Objects.equals(port, that.port);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scheme, host, port);
    }

    @Override
    public String toString() {
        return (scheme + "://" + host + ":" + port.toString());
    }

}
