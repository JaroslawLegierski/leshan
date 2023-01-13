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
package org.eclipse.leshan.transport.javacoap.endpoint;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.security.cert.Certificate;
import java.util.Collection;
import java.util.List;


import org.eclipse.leshan.client.endpoint.ClientEndpointToolbox;
import org.eclipse.leshan.client.endpoint.LwM2mClientEndpoint;
import org.eclipse.leshan.client.endpoint.LwM2mClientEndpointsProvider;
import org.eclipse.leshan.client.request.DownlinkRequestReceiver;
import org.eclipse.leshan.client.resource.LwM2mObjectEnabler;
import org.eclipse.leshan.client.resource.LwM2mObjectTree;
import org.eclipse.leshan.client.servers.ServerIdentity;
import org.eclipse.leshan.client.servers.ServerInfo;
import org.eclipse.leshan.core.SecurityMode;
import org.eclipse.leshan.core.oscore.OscoreIdentity;
import org.eclipse.leshan.core.request.Identity;
import org.eclipse.leshan.transport.javacoap.request.JavaCoapClientCoapMessageTranslator;
import org.eclipse.leshan.transport.javacoap.resource.ObjectResource;
import org.eclipse.leshan.transport.javacoap.resource.ResourcesService;

import com.mbed.coap.client.CoapClient;
import com.mbed.coap.client.CoapClientBuilder;
import com.mbed.coap.server.CoapServer;

public class JavaCoapClientEndpointsProvider implements LwM2mClientEndpointsProvider {

    private String url;
    private CoapServer coapServer;

    private CoapClient coapClient;
    private final JavaCoapClientCoapMessageTranslator messagetranslator = new JavaCoapClientCoapMessageTranslator();
    private JavaCoapClientEndpoint lwm2mEndpoint;
    private ServerIdentity currentServer;
    private Identity identity;
    private int shortserverid;

    public JavaCoapClientEndpointsProvider(String url, int shortserverid) {

        this.url=url;
        this.shortserverid = shortserverid;
    }



   @Override
    public void init(LwM2mObjectTree objectTree, DownlinkRequestReceiver requestReceiver,
            ClientEndpointToolbox toolbox) {


        URI endpointURI = URI.create(url);

        // create Resources / Routes
        ResourcesService.ResourcesBuilder resorcesbuilder = new ResourcesService.ResourcesBuilder();

       for (LwM2mObjectEnabler enabler : objectTree.getObjectEnablers().values()) {

           String objectpath="/";
           String instancepath="";
           String resourcepath="";
           String finalpath="";
           objectpath=objectpath+enabler.getId();

           for ( int i=0 ;i<enabler.getAvailableInstanceIds().size(); i++ ) {

               instancepath="/"+enabler.getAvailableInstanceIds().get(i);
              List<Integer> availableResources1 = enabler.getAvailableResourceIds(i);

               for (Integer availableResource : enabler.getAvailableResourceIds(i)) {
                   resourcepath="/"+availableResource;

                   finalpath=objectpath+instancepath+resourcepath;
                    resorcesbuilder.add(finalpath, new ObjectResource(requestReceiver, finalpath,toolbox,url, shortserverid));
               }

           }
       }


            ResourcesService resources = resorcesbuilder.build();


        coapServer = CoapServer.builder().transport(0)
                .route(resources)
                .build();


         InetSocketAddress destination = new InetSocketAddress(endpointURI.getHost(), endpointURI.getPort());

          coapClient = CoapClientBuilder.clientFor(destination, coapServer);

        lwm2mEndpoint = new JavaCoapClientEndpoint(endpointURI,  coapClient,
                messagetranslator, toolbox, objectTree.getModel());
    }

    @Override
    public ServerIdentity createEndpoint(ServerInfo serverInfo, boolean clientInitiatedOnly,
            List<Certificate> trustStore, ClientEndpointToolbox toolbox) {

        // TODO we should get endpoint used URI dynamically in Resources

        ServerIdentity currentServer = extractIdentity(serverInfo);

        return currentServer;


    }

    @Override
    public Collection<ServerIdentity> createEndpoints(Collection<? extends ServerInfo> serverInfo,
            boolean clientInitiatedOnly, List<Certificate> trustStore, ClientEndpointToolbox toolbox) {


        // TODO TL : need to be implemented or removed ?
        return null;

    }


    @Override public void destroyEndpoints() {


    }

    @Override public void start() {

        try {
            coapServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override public List<LwM2mClientEndpoint> getEndpoints() {
        return null;
    }

    @Override public LwM2mClientEndpoint getEndpoint(ServerIdentity server) {

            return lwm2mEndpoint;

    }

    @Override public void stop() {

        coapServer.stop();
    }

    @Override public void destroy() {

        if (coapServer.isRunning()) {
            coapServer.stop();
        }
    }

    private ServerIdentity extractIdentity(ServerInfo serverInfo) {
        Identity serverIdentity;
        if (serverInfo.isSecure()) {
            // Support PSK
            if (serverInfo.secureMode == SecurityMode.PSK) {
                serverIdentity = Identity.psk(serverInfo.getAddress(), serverInfo.pskId);
            } else if (serverInfo.secureMode == SecurityMode.RPK) {
                serverIdentity = Identity.rpk(serverInfo.getAddress(), serverInfo.serverPublicKey);
            } else if (serverInfo.secureMode == SecurityMode.X509) {
                // TODO We set CN with '*' as we are not able to know the CN for some certificate usage and so this is
                // not used anymore to identify a server with x509.
                // See : https://github.com/eclipse/leshan/issues/992
                serverIdentity = Identity.x509(serverInfo.getAddress(), "*");
            } else {
                throw new RuntimeException("Unable to create connector : unsupported security mode");
            }
        } else if (serverInfo.useOscore) {
            // Build server identity for OSCORE
            serverIdentity = Identity.oscoreOnly(serverInfo.getAddress(),
                    new OscoreIdentity(serverInfo.oscoreSetting.getRecipientId()));
        } else {
            serverIdentity = Identity.unsecure(serverInfo.getAddress());
        }

        if (serverInfo.bootstrap) {
            return new ServerIdentity(serverIdentity, serverInfo.serverId, ServerIdentity.Role.LWM2M_BOOTSTRAP_SERVER,
                    serverInfo.serverUri);
        } else {
            return new ServerIdentity(serverIdentity, serverInfo.serverId, serverInfo.serverUri);
        }
    }


}
