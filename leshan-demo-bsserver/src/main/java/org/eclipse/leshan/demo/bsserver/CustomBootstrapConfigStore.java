/*******************************************************************************
 * Copyright (c) 2013-2015 Sierra Wireless and others.
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
 *    Orange Polska S.A. -  optional objects support added
 *******************************************************************************/
package org.eclipse.leshan.demo.bsserver;

import org.eclipse.leshan.bsserver.BootstrapConfig;
import org.eclipse.leshan.bsserver.BootstrapConfigStore;
import org.eclipse.leshan.bsserver.BootstrapSession;

/**
 * CustomBootstrapConfigStore class supporting non standard LwM2M Objects
 */
public class CustomBootstrapConfigStore implements BootstrapConfigStore {

    private final BootstrapConfigStore delegate;

    public CustomBootstrapConfigStore(BootstrapConfigStore delegate) {
        this.delegate = delegate;
    }

    @Override
    public BootstrapConfig get(BootstrapSession session) {
        BootstrapConfig config = delegate.get(session);
        return config == null ? null : new CustomBootstrapConfig(config);
    }

}
