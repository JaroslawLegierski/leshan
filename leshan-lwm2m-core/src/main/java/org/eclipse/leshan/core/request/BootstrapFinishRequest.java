/*******************************************************************************
 * Copyright (c) 2015 Sierra Wireless and others.
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
package org.eclipse.leshan.core.request;

import org.eclipse.leshan.core.response.BootstrapFinishResponse;

/**
 * Request sent when bootstrap session is finished
 */
public class BootstrapFinishRequest extends AbstractLwM2mRequest<BootstrapFinishResponse>
        implements DownlinkBootstrapRequest<BootstrapFinishResponse> {

    public BootstrapFinishRequest() {
        this(null);
    }

    public BootstrapFinishRequest(Object coapRequest) {
        super(coapRequest);
    }

    @Override
    public void accept(DownlinkRequestVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void accept(DownlinkBootstrapRequestVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return String.format("BootstrapFinishRequest");
    }
}
