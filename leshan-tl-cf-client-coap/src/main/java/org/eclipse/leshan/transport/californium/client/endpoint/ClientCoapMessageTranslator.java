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
package org.eclipse.leshan.transport.californium.client.endpoint;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.server.resources.Resource;
import org.eclipse.leshan.client.endpoint.ClientEndpointToolbox;
import org.eclipse.leshan.client.notification.NotificationManager;
import org.eclipse.leshan.client.request.DownlinkRequestReceiver;
import org.eclipse.leshan.client.resource.LwM2mObjectEnabler;
import org.eclipse.leshan.client.resource.LwM2mObjectTree;
import org.eclipse.leshan.client.resource.listener.ObjectListener;
import org.eclipse.leshan.client.resource.listener.ObjectsListenerAdapter;
import org.eclipse.leshan.client.servers.LwM2mServer;
import org.eclipse.leshan.core.model.LwM2mModel;
import org.eclipse.leshan.core.node.LwM2mPath;
import org.eclipse.leshan.core.peer.IpPeer;
import org.eclipse.leshan.core.peer.LwM2mPeer;
import org.eclipse.leshan.core.request.UplinkRequest;
import org.eclipse.leshan.core.response.LwM2mResponse;
import org.eclipse.leshan.transport.californium.client.ObserveCompositeRelationFilter;
import org.eclipse.leshan.transport.californium.client.RootResource;
import org.eclipse.leshan.transport.californium.client.bootstrap.BootstrapResource;
import org.eclipse.leshan.transport.californium.client.object.ObjectResource;
import org.eclipse.leshan.transport.californium.client.request.CoapRequestBuilder;
import org.eclipse.leshan.transport.californium.client.request.LwM2mResponseBuilder;
import org.eclipse.leshan.transport.californium.identity.IdentityHandler;
import org.eclipse.leshan.transport.californium.identity.IdentityHandlerProvider;

public class ClientCoapMessageTranslator {

    public Request createCoapRequest(LwM2mServer server, UplinkRequest<? extends LwM2mResponse> lwm2mRequest,
            ClientEndpointToolbox toolbox, LwM2mModel model, IdentityHandler identityHandler) {

        // create CoAP Request
        LwM2mPeer lwm2mPeer = server.getTransportData();
        if (!(lwm2mPeer instanceof IpPeer)) {
            throw new IllegalStateException(
                    String.format("%s is not a LwM2mPeer supported by this class", server.getClass().getSimpleName()));
        }

        CoapRequestBuilder builder = new CoapRequestBuilder((IpPeer) lwm2mPeer, toolbox.getEncoder(), model,
                toolbox.getLinkSerializer(), identityHandler);
        lwm2mRequest.accept(builder);
        return builder.getRequest();
    }

    public <T extends LwM2mResponse> T createLwM2mResponse(LwM2mServer server, UplinkRequest<T> lwm2mRequest,
            Request coapRequest, Response coapResponse, ClientEndpointToolbox toolbox) {

        // create LWM2M Response
        LwM2mResponseBuilder<T> builder = new LwM2mResponseBuilder<T>(coapResponse);
        lwm2mRequest.accept(builder);
        return builder.getResponse();
    }

    public Resource createRootResource(CoapServer coapServer, IdentityHandlerProvider identityHandlerProvider,
            ServerIdentityExtractor identityExtrator, DownlinkRequestReceiver requestReceiver,
            ClientEndpointToolbox toolbox, LwM2mObjectTree objectTree) {
        // Use to handle Delete on "/"
        final RootResource rootResource = new RootResource(identityHandlerProvider, identityExtrator, coapServer,
                requestReceiver, toolbox);
        objectTree.addListener(new ObjectsListenerAdapter() {
            @Override
            public void resourceChanged(LwM2mPath... paths) {
                rootResource.changed(new ObserveCompositeRelationFilter(paths));
            }
        });
        return rootResource;
    }

    public List<Resource> createResources(CoapServer coapServer, IdentityHandlerProvider identityHandlerProvider,
            ServerIdentityExtractor identityExtrator, DownlinkRequestReceiver requestReceiver,
            NotificationManager notificationManager, ClientEndpointToolbox toolbox, LwM2mObjectTree objectTree) {
        ArrayList<Resource> resources = new ArrayList<>();

        // create bootstrap resource
        resources.add(new BootstrapResource(identityHandlerProvider, identityExtrator, requestReceiver));

        // create object resources
        for (LwM2mObjectEnabler enabler : objectTree.getObjectEnablers().values()) {
            resources.add(createObjectResource(enabler, identityHandlerProvider, identityExtrator, requestReceiver,
                    notificationManager, toolbox));
        }

        // link resource to object tree
        objectTree.addListener(new ObjectsListenerAdapter() {
            @Override
            public void objectAdded(LwM2mObjectEnabler object) {
                CoapResource clientObject = createObjectResource(object, identityHandlerProvider, identityExtrator,
                        requestReceiver, notificationManager, toolbox);
                coapServer.add(clientObject);
            }

            @Override
            public void objectRemoved(LwM2mObjectEnabler object) {
                Resource resource = coapServer.getRoot().getChild(Integer.toString(object.getId()));
                if (resource instanceof ObjectListener) {
                    object.removeListener((ObjectListener) (resource));
                }
                coapServer.remove(resource);
            }
        });
        return resources;
    }

    public CoapResource createObjectResource(LwM2mObjectEnabler objectEnabler,
            IdentityHandlerProvider identityHandlerProvider, ServerIdentityExtractor identityExtractor,
            DownlinkRequestReceiver requestReceiver, NotificationManager notificationManager,
            ClientEndpointToolbox toolbox) {
        ObjectResource objectResource = new ObjectResource(objectEnabler.getId(), identityHandlerProvider,
                identityExtractor, requestReceiver, notificationManager, toolbox);
        objectEnabler.addListener(objectResource);
        return objectResource;
    }
}
