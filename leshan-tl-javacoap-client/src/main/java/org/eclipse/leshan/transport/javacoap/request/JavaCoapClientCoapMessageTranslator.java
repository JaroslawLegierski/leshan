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
package org.eclipse.leshan.transport.javacoap.request;


import org.eclipse.leshan.client.endpoint.ClientEndpointToolbox;
import org.eclipse.leshan.client.servers.ServerIdentity;
import org.eclipse.leshan.core.model.LwM2mModel;

import org.eclipse.leshan.core.request.UplinkRequest;
import org.eclipse.leshan.core.response.LwM2mResponse;

import com.mbed.coap.packet.CoapRequest;
import com.mbed.coap.packet.CoapResponse;

public class JavaCoapClientCoapMessageTranslator {

    public CoapRequest createCoapRequest(ServerIdentity serverIdentity,
            UplinkRequest<? extends LwM2mResponse> lwm2mRequest, ClientEndpointToolbox toolbox, LwM2mModel model) {

        // create CoAP Request
        JavaCoapRequestBuilder builder = new JavaCoapRequestBuilder(serverIdentity.getIdentity(), toolbox.getEncoder(),
                model, toolbox.getLinkSerializer());
        lwm2mRequest.accept(builder);
        return builder.getRequest();
    }

    public <T extends LwM2mResponse> T createLwM2mResponse(ServerIdentity serverIdentity, UplinkRequest<T> lwm2mRequest,
            CoapRequest coapRequest, CoapResponse coapResponse, ClientEndpointToolbox toolbox) {

        // create LWM2M Response
        JavaCoapLwM2mResponseBuilder<T> builder = new JavaCoapLwM2mResponseBuilder<T>(coapResponse);
        lwm2mRequest.accept(builder);
        return builder.getResponse();
    }

/*    public Resource createRootResource(LeshanCoapServer coapServer, IdentityHandlerProvider identityHandlerProvider,
            ServerIdentityExtractor identityExtrator, DownlinkRequestReceiver requestReceiver,
            ClientEndpointToolbox toolbox, LwM2mObjectTree objectTree) {
        // Use to handle Delete on "/"
        final JavaCoapRootResource rootResource = new JavaCoapRootResource(identityHandlerProvider, identityExtrator,
                coapServer.coapServer, requestReceiver, toolbox);
        objectTree.addListener(new ObjectsListenerAdapter() {
            @Override public void resourceChanged(LwM2mPath... paths) {
                rootResource.changed(new ObserveCompositeRelationFilter(paths));
            }
        });
        return rootResource;
    }

    public List<Resource> createResources(LeshanCoapServer coapServer, IdentityHandlerProvider identityHandlerProvider,
            ServerIdentityExtractor identityExtrator, DownlinkRequestReceiver requestReceiver,
            ClientEndpointToolbox toolbox, LwM2mObjectTree objectTree) {
        ArrayList<Resource> resources = new ArrayList<>();

        // create bootstrap resource
        resources.add(new BootstrapResource(identityHandlerProvider, identityExtrator, requestReceiver));

        // create object resources
        for (LwM2mObjectEnabler enabler : objectTree.getObjectEnablers().values()) {
            resources.add(
                    createObjectResource(enabler, identityHandlerProvider, identityExtrator, requestReceiver, toolbox));
        }

        // link resource to object tree
        objectTree.addListener(new ObjectsListenerAdapter() {
            @Override public void objectAdded(LwM2mObjectEnabler object) {
                CoapResource clientObject = createObjectResource(object, identityHandlerProvider, identityExtrator,
                        requestReceiver, toolbox);
                // coapServer.add(clientObject);

            }

            @Override public void objectRemoved(LwM2mObjectEnabler object) {
                // Resource resource = coapServer.getRoot().getChild(Integer.toString(object.getId()));
                // if (resource instanceof ObjectListener) {
                // object.removeListener((ObjectListener) (resource));
                // }
                // coapServer.remove(resource);
            }
        });
        return resources;
    }

    public CoapResource createObjectResource(LwM2mObjectEnabler objectEnabler,
            IdentityHandlerProvider identityHandlerProvider, ServerIdentityExtractor identityExtractor,
            DownlinkRequestReceiver requestReceiver, ClientEndpointToolbox toolbox) {
        ObjectResource objectResource = new ObjectResource(objectEnabler.getId(), identityHandlerProvider,
                identityExtractor, requestReceiver, toolbox);
        objectEnabler.addListener(objectResource);
        return objectResource;
    }*/
}
