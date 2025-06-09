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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.leshan.bsserver.BootstrapConfig;
import org.eclipse.leshan.bsserver.BootstrapUtil;
import org.eclipse.leshan.core.LwM2mId;
import org.eclipse.leshan.core.node.LwM2mMultipleResource;
import org.eclipse.leshan.core.node.LwM2mNode;
import org.eclipse.leshan.core.node.LwM2mObjectInstance;
import org.eclipse.leshan.core.node.LwM2mPath;
import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.node.LwM2mSingleResource;
import org.eclipse.leshan.core.node.ObjectLink;
import org.eclipse.leshan.core.request.BindingMode;
import org.eclipse.leshan.core.request.BootstrapDeleteRequest;
import org.eclipse.leshan.core.request.BootstrapWriteRequest;
import org.eclipse.leshan.core.request.ContentFormat;
import org.eclipse.leshan.core.request.DownlinkBootstrapRequest;
import org.eclipse.leshan.core.response.LwM2mResponse;
import org.eclipse.leshan.core.util.datatype.ULong;

public class CustomBootstrapUtil {
    public static LwM2mObjectInstance toSecurityInstance(int instanceId,
            BootstrapConfig.ServerSecurity securityConfig) {
        Collection<LwM2mResource> resources = new ArrayList<>();

        // resources since v1.0
        if (securityConfig.uri != null)
            resources.add(LwM2mSingleResource.newStringResource(0, securityConfig.uri));
        resources.add(LwM2mSingleResource.newBooleanResource(1, securityConfig.bootstrapServer));
        if (securityConfig.securityMode != null)
            resources.add(LwM2mSingleResource.newIntegerResource(2, securityConfig.securityMode.code));
        if (securityConfig.publicKeyOrId != null)
            resources.add(LwM2mSingleResource.newBinaryResource(3, securityConfig.publicKeyOrId));
        if (securityConfig.serverPublicKey != null)
            resources.add(LwM2mSingleResource.newBinaryResource(4, securityConfig.serverPublicKey));
        if (securityConfig.secretKey != null)
            resources.add(LwM2mSingleResource.newBinaryResource(5, securityConfig.secretKey));
        if (securityConfig.smsSecurityMode != null)
            resources.add(LwM2mSingleResource.newIntegerResource(6, securityConfig.smsSecurityMode.code));
        if (securityConfig.smsBindingKeyParam != null)
            resources.add(LwM2mSingleResource.newBinaryResource(7, securityConfig.smsBindingKeyParam));
        if (securityConfig.smsBindingKeySecret != null)
            resources.add(LwM2mSingleResource.newBinaryResource(8, securityConfig.smsBindingKeySecret));
        if (securityConfig.serverSmsNumber != null)
            resources.add(LwM2mSingleResource.newStringResource(9, securityConfig.serverSmsNumber));
        if (securityConfig.serverId != null)
            resources.add(LwM2mSingleResource.newIntegerResource(10, securityConfig.serverId));
        if (securityConfig.clientOldOffTime != null)
            resources.add(LwM2mSingleResource.newIntegerResource(11, securityConfig.clientOldOffTime));
        if (securityConfig.bootstrapServerAccountTimeout != null)
            resources.add(LwM2mSingleResource.newIntegerResource(12, securityConfig.bootstrapServerAccountTimeout));

        // resources since v1.1
        if (securityConfig.matchingType != null)
            resources.add(LwM2mSingleResource.newUnsignedIntegerResource(13, securityConfig.matchingType.code));
        if (securityConfig.sni != null)
            resources.add(LwM2mSingleResource.newStringResource(14, securityConfig.sni));
        if (securityConfig.certificateUsage != null)
            resources.add(LwM2mSingleResource.newUnsignedIntegerResource(15, securityConfig.certificateUsage.code));
        if (securityConfig.cipherSuite != null) {
            Map<Integer, ULong> ciperSuiteULong = new HashMap<>();
            int i = 0;
            for (BootstrapConfig.CipherSuiteId cipherSuiteId : securityConfig.cipherSuite) {
                ciperSuiteULong.put(i++, cipherSuiteId.getValueForSecurityObject());
            }
            resources.add(LwM2mMultipleResource.newUnsignedIntegerResource(16, ciperSuiteULong));
        }
        if (securityConfig.oscoreSecurityMode != null) {
            resources.add(LwM2mSingleResource.newObjectLinkResource(17,
                    new ObjectLink(21, securityConfig.oscoreSecurityMode)));
        }

        if (securityConfig.oscoreSecurityMode != null) {
            // integer value needs to be made into an object link
            ObjectLink oscoreSecurityModeLink = new ObjectLink(LwM2mId.OSCORE, securityConfig.oscoreSecurityMode);
            resources.add(LwM2mSingleResource.newObjectLinkResource(17, oscoreSecurityModeLink));
        }
        return new LwM2mObjectInstance(instanceId, resources);
    }

    public static BootstrapWriteRequest toWriteRequest(int instanceId, BootstrapConfig.ServerSecurity securityConfig,
            ContentFormat contentFormat) {
        LwM2mPath path = new LwM2mPath(LwM2mId.SECURITY, instanceId);
        final LwM2mNode securityInstance = BootstrapUtil.toSecurityInstance(instanceId, securityConfig);
        return new BootstrapWriteRequest(path, securityInstance, contentFormat);
    }

    public static LwM2mObjectInstance toServerInstance(int instanceId, BootstrapConfig.ServerConfig serverConfig) {
        Collection<LwM2mResource> resources = new ArrayList<>();

        // resources since v1.0
        resources.add(LwM2mSingleResource.newIntegerResource(0, serverConfig.shortId));
        resources.add(LwM2mSingleResource.newIntegerResource(1, serverConfig.lifetime));
        if (serverConfig.defaultMinPeriod != null)
            resources.add(LwM2mSingleResource.newIntegerResource(2, serverConfig.defaultMinPeriod));
        if (serverConfig.defaultMaxPeriod != null)
            resources.add(LwM2mSingleResource.newIntegerResource(3, serverConfig.defaultMaxPeriod));
        if (serverConfig.disableTimeout != null)
            resources.add(LwM2mSingleResource.newIntegerResource(5, serverConfig.disableTimeout));
        resources.add(LwM2mSingleResource.newBooleanResource(6, serverConfig.notifIfDisabled));
        if (serverConfig.binding != null)
            resources.add(LwM2mSingleResource.newStringResource(7, BindingMode.toString(serverConfig.binding)));

        // resources since v1.1
        if (serverConfig.apnLink != null)
            resources.add(LwM2mSingleResource.newObjectLinkResource(10, new ObjectLink(11, serverConfig.apnLink)));

        if (serverConfig.registrationPriority != null)
            resources.add(LwM2mSingleResource.newUnsignedIntegerResource(13, serverConfig.registrationPriority));
        if (serverConfig.initialDelay != null)
            resources.add(LwM2mSingleResource.newUnsignedIntegerResource(14, serverConfig.initialDelay));
        if (serverConfig.registrationFailure != null)
            resources.add(LwM2mSingleResource.newBooleanResource(15, serverConfig.registrationFailure));
        if (serverConfig.bootstrapOnRegistrationFailure != null)
            resources.add(LwM2mSingleResource.newBooleanResource(16, serverConfig.bootstrapOnRegistrationFailure));
        if (serverConfig.communicationRetryCount != null)
            resources.add(LwM2mSingleResource.newUnsignedIntegerResource(17, serverConfig.communicationRetryCount));
        if (serverConfig.CommunicationRetryTimer != null)
            resources.add(LwM2mSingleResource.newUnsignedIntegerResource(18, serverConfig.CommunicationRetryTimer));
        if (serverConfig.SequenceDelayTimer != null)
            resources.add(LwM2mSingleResource.newUnsignedIntegerResource(19, serverConfig.SequenceDelayTimer));
        if (serverConfig.SequenceRetryCount != null)
            resources.add(LwM2mSingleResource.newUnsignedIntegerResource(20, serverConfig.SequenceRetryCount));

        if (serverConfig.trigger != null)
            resources.add(LwM2mSingleResource.newBooleanResource(21, serverConfig.trigger));
        if (serverConfig.preferredTransport != null)
            resources.add(LwM2mSingleResource.newStringResource(22, serverConfig.preferredTransport.toString()));
        if (serverConfig.muteSend != null)
            resources.add(LwM2mSingleResource.newBooleanResource(23, serverConfig.muteSend));

        return new LwM2mObjectInstance(instanceId, resources);
    }

    public static BootstrapWriteRequest toWriteRequest(int instanceId, BootstrapConfig.ServerConfig serverConfig,
            ContentFormat contentFormat) {
        LwM2mPath path = new LwM2mPath(LwM2mId.SERVER, instanceId);
        final LwM2mNode securityInstance = BootstrapUtil.toServerInstance(instanceId, serverConfig);
        return new BootstrapWriteRequest(path, securityInstance, contentFormat);
    }

    public static LwM2mObjectInstance toAclInstance(int instanceId, BootstrapConfig.ACLConfig aclConfig) {
        Collection<LwM2mResource> resources = new ArrayList<>();

        resources.add(LwM2mSingleResource.newIntegerResource(0, aclConfig.objectId));
        resources.add(LwM2mSingleResource.newIntegerResource(1, aclConfig.objectInstanceId));
        if (aclConfig.acls != null)
            resources.add(LwM2mMultipleResource.newIntegerResource(2, aclConfig.acls));
        if (aclConfig.AccessControlOwner != null)
            resources.add(LwM2mSingleResource.newIntegerResource(3, aclConfig.AccessControlOwner));

        return new LwM2mObjectInstance(instanceId, resources);
    }

    public static BootstrapWriteRequest toWriteRequest(int instanceId, BootstrapConfig.ACLConfig aclConfig,
            ContentFormat contentFormat) {
        LwM2mPath path = new LwM2mPath(LwM2mId.ACCESS_CONTROL, instanceId);
        final LwM2mNode securityInstance = BootstrapUtil.toAclInstance(instanceId, aclConfig);
        return new BootstrapWriteRequest(path, securityInstance, contentFormat);
    }

    public static LwM2mObjectInstance toOscoreInstance(int instanceId, BootstrapConfig.OscoreObject oscoreConfig) {
        Collection<LwM2mResource> resources = new ArrayList<>();

        if (oscoreConfig.oscoreMasterSecret != null)
            resources.add(LwM2mSingleResource.newBinaryResource(0, oscoreConfig.oscoreMasterSecret));
        if (oscoreConfig.oscoreSenderId != null)
            resources.add(LwM2mSingleResource.newBinaryResource(1, oscoreConfig.oscoreSenderId));
        if (oscoreConfig.oscoreRecipientId != null)
            resources.add(LwM2mSingleResource.newBinaryResource(2, oscoreConfig.oscoreRecipientId));
        if (oscoreConfig.oscoreAeadAlgorithm != null)
            resources.add(LwM2mSingleResource.newIntegerResource(3, oscoreConfig.oscoreAeadAlgorithm));
        if (oscoreConfig.oscoreHmacAlgorithm != null)
            resources.add(LwM2mSingleResource.newIntegerResource(4, oscoreConfig.oscoreHmacAlgorithm));
        if (oscoreConfig.oscoreMasterSalt != null)
            resources.add(LwM2mSingleResource.newBinaryResource(5, oscoreConfig.oscoreMasterSalt));

        return new LwM2mObjectInstance(instanceId, resources);
    }

    public static BootstrapWriteRequest toWriteRequest(int instanceId, BootstrapConfig.OscoreObject oscoreConfig,
            ContentFormat contentFormat) {
        LwM2mPath path = new LwM2mPath(LwM2mId.OSCORE, instanceId);
        final LwM2mNode securityInstance = BootstrapUtil.toOscoreInstance(instanceId, oscoreConfig);
        return new BootstrapWriteRequest(path, securityInstance, contentFormat);
    }

    public static List<DownlinkBootstrapRequest<? extends LwM2mResponse>> toRequests(
            CustomBootstrapConfig bootstrapConfig) {
        return toRequests(bootstrapConfig, ContentFormat.TLV);
    }

    public static List<DownlinkBootstrapRequest<? extends LwM2mResponse>> toRequests(
            CustomBootstrapConfig bootstrapConfig, ContentFormat contentFormat) {
        List<DownlinkBootstrapRequest<? extends LwM2mResponse>> requests = new ArrayList<>();
        // handle delete
        for (String path : bootstrapConfig.toDelete) {
            requests.add(new BootstrapDeleteRequest(path));
        }
        // handle security
        for (Map.Entry<Integer, BootstrapConfig.ServerSecurity> security : bootstrapConfig.security.entrySet()) {
            requests.add(toWriteRequest(security.getKey(), security.getValue(), contentFormat));
        }
        // handle server
        for (Map.Entry<Integer, BootstrapConfig.ServerConfig> server : bootstrapConfig.servers.entrySet()) {
            requests.add(toWriteRequest(server.getKey(), server.getValue(), contentFormat));
        }
        // handle acl
        for (Map.Entry<Integer, BootstrapConfig.ACLConfig> acl : bootstrapConfig.acls.entrySet()) {
            requests.add(toWriteRequest(acl.getKey(), acl.getValue(), contentFormat));
        }
        // handle oscore
        for (Map.Entry<Integer, BootstrapConfig.OscoreObject> oscore : bootstrapConfig.oscore.entrySet()) {
            requests.add(toWriteRequest(oscore.getKey(), oscore.getValue(), contentFormat));
        }
        // handle ConnectionIdentity
        for (Map.Entry<Integer, CustomBootstrapConfig.ConnectionIdentity> connectionIdentity : bootstrapConfig.connectionidentity
                .entrySet()) {
            requests.add(toWriteRequest(connectionIdentity.getKey(), connectionIdentity.getValue(), contentFormat));
        }
        // handle ConnectionIdentity
        for (Map.Entry<Integer, CustomBootstrapConfig.ConnectionServiceEndpoint> connectionServiceEndpoint : bootstrapConfig.connectionserviceendpoint
                .entrySet()) {
            requests.add(toWriteRequest(connectionServiceEndpoint.getKey(), connectionServiceEndpoint.getValue(),
                    contentFormat));
        }
        return (requests);
    }

    public static List<DownlinkBootstrapRequest<? extends LwM2mResponse>> toRequests(
            CustomBootstrapConfig bootstrapConfig, ContentFormat contentFormat, int bootstrapServerID) {
        List<DownlinkBootstrapRequest<? extends LwM2mResponse>> requests = new ArrayList<>();
        // handle delete
        for (String path : bootstrapConfig.toDelete) {
            requests.add(new BootstrapDeleteRequest(path));
        }
        // handle security
        int id = 0;
        for (BootstrapConfig.ServerSecurity security : new TreeMap<>(bootstrapConfig.security).values()) {
            if (security.bootstrapServer) {
                requests.add(toWriteRequest(bootstrapServerID, security, contentFormat));
            } else {
                if (id == bootstrapServerID)
                    id++;
                requests.add(toWriteRequest(id, security, contentFormat));
                id++;
            }
        }
        // handle server
        for (Map.Entry<Integer, BootstrapConfig.ServerConfig> server : bootstrapConfig.servers.entrySet()) {
            requests.add(toWriteRequest(server.getKey(), server.getValue(), contentFormat));
        }
        // handle acl
        for (Map.Entry<Integer, BootstrapConfig.ACLConfig> acl : bootstrapConfig.acls.entrySet()) {
            requests.add(toWriteRequest(acl.getKey(), acl.getValue(), contentFormat));
        }
        // handle oscore
        for (Map.Entry<Integer, BootstrapConfig.OscoreObject> oscore : bootstrapConfig.oscore.entrySet()) {
            requests.add(toWriteRequest(oscore.getKey(), oscore.getValue(), contentFormat));
        }
        // handle ConnectionIdentity
        for (Map.Entry<Integer, CustomBootstrapConfig.ConnectionIdentity> connectionIdentity : bootstrapConfig.connectionidentity
                .entrySet()) {
            requests.add(toWriteRequest(connectionIdentity.getKey(), connectionIdentity.getValue(), contentFormat));
        }
        // handle ConnectionServiceEndpoint
        for (Map.Entry<Integer, CustomBootstrapConfig.ConnectionServiceEndpoint> connectionServiceEndpoint : bootstrapConfig.connectionserviceendpoint
                .entrySet()) {
            requests.add(toWriteRequest(connectionServiceEndpoint.getKey(), connectionServiceEndpoint.getValue(),
                    contentFormat));
        }
        return (requests);
    }

    public static LwM2mObjectInstance toConnectionIdentity(int instanceId,
            CustomBootstrapConfig.ConnectionIdentity connectionIdentity) {
        Collection<LwM2mResource> resources = new ArrayList<>();

        if (connectionIdentity.ID != null)
            resources.add(LwM2mSingleResource.newStringResource(0, connectionIdentity.ID));
        if (connectionIdentity.PSKIdentity != null)
            resources.add(LwM2mSingleResource.newStringResource(1, connectionIdentity.PSKIdentity));
        if (connectionIdentity.PSKSecretKey != null)
            resources.add(LwM2mSingleResource.newBinaryResource(2, connectionIdentity.PSKSecretKey));

        return new LwM2mObjectInstance(instanceId, resources);
    }

    public static LwM2mObjectInstance toConnectionServiceEndpoint(int instanceId,
            CustomBootstrapConfig.ConnectionServiceEndpoint connectionServiceEndpoint) {
        Collection<LwM2mResource> resources = new ArrayList<>();

        if (connectionServiceEndpoint.ServiceName != null)
            resources.add(LwM2mSingleResource.newStringResource(0, connectionServiceEndpoint.ServiceName));
        if (connectionServiceEndpoint.Payload != null)
            resources.add(LwM2mSingleResource.newStringResource(1, connectionServiceEndpoint.Payload));
        if (connectionServiceEndpoint.ServiceURI != null)
            resources.add(LwM2mSingleResource.newStringResource(2, connectionServiceEndpoint.ServiceURI));
        if (connectionServiceEndpoint.TopicRoot != null)
            resources.add(LwM2mSingleResource.newStringResource(3, connectionServiceEndpoint.TopicRoot));
        if (connectionServiceEndpoint.ServerPublicKey != null)
            resources.add(LwM2mSingleResource.newBinaryResource(4, connectionServiceEndpoint.ServerPublicKey));

        return new LwM2mObjectInstance(instanceId, resources);
    }

    public static BootstrapWriteRequest toWriteRequest(int instanceId,
            CustomBootstrapConfig.ConnectionIdentity connectionIdentity, ContentFormat contentFormat) {
        LwM2mPath path = new LwM2mPath(36050, instanceId);
        final LwM2mNode connectionIdentityInstance = CustomBootstrapUtil.toConnectionIdentity(instanceId,
                connectionIdentity);
        return new BootstrapWriteRequest(path, connectionIdentityInstance, contentFormat);
    }

    public static BootstrapWriteRequest toWriteRequest(int instanceId,
            CustomBootstrapConfig.ConnectionServiceEndpoint connectionServiceEndpoint, ContentFormat contentFormat) {
        LwM2mPath path = new LwM2mPath(36051, instanceId);
        final LwM2mNode connectionServiceEndpointInstance = CustomBootstrapUtil.toConnectionServiceEndpoint(instanceId,
                connectionServiceEndpoint);
        return new BootstrapWriteRequest(path, connectionServiceEndpointInstance, contentFormat);
    }
}
