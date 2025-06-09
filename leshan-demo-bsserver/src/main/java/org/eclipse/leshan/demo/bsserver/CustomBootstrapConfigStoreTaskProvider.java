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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.leshan.bsserver.BootstrapConfigStore;
import org.eclipse.leshan.bsserver.BootstrapConfigStoreTaskProvider;
import org.eclipse.leshan.bsserver.BootstrapSession;
import org.eclipse.leshan.core.request.BootstrapDiscoverRequest;
import org.eclipse.leshan.core.response.BootstrapDiscoverResponse;
import org.eclipse.leshan.core.response.LwM2mResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomBootstrapConfigStoreTaskProvider extends BootstrapConfigStoreTaskProvider {

    private static final Logger LOG = LoggerFactory.getLogger(CustomBootstrapConfigStoreTaskProvider.class);

    private final BootstrapConfigStore store;

    public CustomBootstrapConfigStoreTaskProvider(BootstrapConfigStore store) {
        super(store);
        this.store = store;
    }

    @Override
    public Tasks getTasks(BootstrapSession session, List<LwM2mResponse> previousResponse) {

        CustomBootstrapConfig config = (CustomBootstrapConfig) store.get(session);
        if (config == null)
            return null;

        if (previousResponse == null && shouldStartWithDiscover(config)) {
            Tasks tasks = new Tasks();
            tasks.requestsToSend = new ArrayList<>(1);
            tasks.requestsToSend.add(new BootstrapDiscoverRequest());
            tasks.last = false;
            return tasks;
        } else {
            Tasks tasks = new Tasks();

            // handle bootstrap discover response
            if (previousResponse != null) {
                BootstrapDiscoverResponse response = (BootstrapDiscoverResponse) previousResponse.get(0);
                if (!response.isSuccess()) {
                    LOG.warn(
                            "Bootstrap Discover return error {} : unable to continue bootstrap session with autoIdForSecurityObject mode. {}",
                            response, session);
                    return null;
                }

                Integer bootstrapServerInstanceId = findBootstrapServerInstanceId(response.getObjectLinks());
                if (bootstrapServerInstanceId == null) {
                    LOG.warn(
                            "Unable to find bootstrap server instance in Security Object (0) in response {}: unable to continue bootstrap session with autoIdForSecurityObject mode. {}",
                            response, session);
                    return null;
                }

                // create requests from config
                tasks.requestsToSend = CustomBootstrapUtil.toRequests(config,
                        config.contentFormat != null ? config.contentFormat : session.getContentFormat(),
                        bootstrapServerInstanceId);
            } else {
                // create requests from config
                tasks.requestsToSend = CustomBootstrapUtil.toRequests(config,
                        config.contentFormat != null ? config.contentFormat : session.getContentFormat());
            }

            // We add model for Security(0), Server(0) and ACL(2) which are the only one supported by BootstrapConfig
            tasks.supportedObjects = new HashMap<>();
            tasks.supportedObjects.put(0, "1.1");
            tasks.supportedObjects.put(1, "1.1");
            tasks.supportedObjects.put(2, "1.0");
            // We add model for specific objects 36050 and 36051
            tasks.supportedObjects.put(36050, "1.0");
            tasks.supportedObjects.put(36051, "1.0");

            return tasks;
        }
    }

}
