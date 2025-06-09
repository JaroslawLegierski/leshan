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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.leshan.bsserver.BootstrapConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CustomBootstrapConfig extends BootstrapConfig {

    private static final Logger LOG = LoggerFactory.getLogger(CustomBootstrapConfig.class);

    private final BootstrapConfig originalConfig; // Store the original BootstrapConfig
    @JsonProperty("connectionidentity")
    public Map<Integer, ConnectionIdentity> connectionidentity = new HashMap<>();
    @JsonProperty("connectionserviceendpoint")
    public Map<Integer, ConnectionServiceEndpoint> connectionserviceendpoint = new HashMap<>();

    public void logConfig() {
        LOG.info("CustomBootstrapConfig Initialized");
        LOG.info("connectionidentity: " + connectionidentity);
        LOG.info("connectionserviceendpoint: " + connectionserviceendpoint);
    }

    public CustomBootstrapConfig() {
        super();
        originalConfig = null;
        logConfig();
    }

    public CustomBootstrapConfig(BootstrapConfig config) {
        this.originalConfig = config;
        logConfig();
    }

    public BootstrapConfig getOriginalConfig() {
        return originalConfig;
    }

    public static class ConnectionIdentity implements Serializable {
        private static final long serialVersionUID = 1L;
        public String ID = "ID0";
        public String PSKIdentity = "PSKIdentity";
        public byte[] PSKSecretKey = new byte[] {};
    }

    public static class ConnectionServiceEndpoint implements Serializable {
        private static final long serialVersionUID = 1L;
        public String ServiceName = "ServiceName";
        public String Payload = "Payload";
        public String ServiceURI = "ServiceURI";
        public String TopicRoot = "Topic Root";
        public byte[] ServerPublicKey = new byte[] {};
    }

    @Override
    public String toString() {
        return String.format("BootstrapConfig [ConnectionIdentity=%s, ConnectionServiceEndpoint=%s]",
                connectionidentity, connectionserviceendpoint);
    }
}
