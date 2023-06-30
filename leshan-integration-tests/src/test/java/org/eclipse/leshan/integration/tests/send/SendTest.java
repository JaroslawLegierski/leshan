/*******************************************************************************
 * Copyright (c) 2021 Sierra Wireless and others.
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
package org.eclipse.leshan.integration.tests.send;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.leshan.integration.tests.util.Credentials.GOOD_PSK_ID;
import static org.eclipse.leshan.integration.tests.util.Credentials.GOOD_PSK_KEY;
import static org.eclipse.leshan.integration.tests.util.LeshanTestClientBuilder.givenClientUsing;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import org.eclipse.leshan.client.servers.LwM2mServer;
import org.eclipse.leshan.core.endpoint.Protocol;
import org.eclipse.leshan.core.node.LwM2mNode;
import org.eclipse.leshan.core.node.LwM2mPath;
import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.node.TimestampedLwM2mNodes;
import org.eclipse.leshan.core.request.ContentFormat;
import org.eclipse.leshan.core.response.ErrorCallback;
import org.eclipse.leshan.core.response.ResponseCallback;
import org.eclipse.leshan.core.response.SendResponse;
import org.eclipse.leshan.integration.tests.util.LeshanTestClient;
import org.eclipse.leshan.integration.tests.util.LeshanTestServer;
import org.eclipse.leshan.integration.tests.util.LeshanTestServerBuilder;
import org.eclipse.leshan.integration.tests.util.SimpleNat;
import org.eclipse.leshan.integration.tests.util.assertion.Assertions;
import org.eclipse.leshan.integration.tests.util.junit5.extensions.ArgumentsUtil;
import org.eclipse.leshan.integration.tests.util.junit5.extensions.BeforeEachParameterizedResolver;
import org.eclipse.leshan.server.security.InMemorySecurityStore;
import org.eclipse.leshan.server.security.NonUniqueSecurityInfoException;
import org.eclipse.leshan.server.security.SecurityInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(BeforeEachParameterizedResolver.class)
public class SendTest {

    /*---------------------------------/
     *  Parameterized Tests
     * -------------------------------*/
    @ParameterizedTest(name = "{0} over {1} - Client using {2} - Server using {3}")
    @MethodSource("transports")
    @Retention(RetentionPolicy.RUNTIME)
    private @interface TestAllCases {
    }

    static Stream<Arguments> transports() {

        Object[][] transports = new Object[][] {
                // ProtocolUsed - Client Endpoint Provider - Server Endpoint Provider
                { Protocol.COAP, "Californium", "Californium" } };

        Object[] contentFormats = new Object[] { //
                ContentFormat.SENML_JSON, //
                ContentFormat.SENML_CBOR };

        // for each transport, create 1 test by format.
        return Stream.of(ArgumentsUtil.combine(contentFormats, transports));
    }

    /*---------------------------------/
     *  Set-up and Tear-down Tests
     * -------------------------------*/

    LeshanTestServer server;
    LeshanTestClient client;
    SimpleNat nat;


    protected LeshanTestServerBuilder givenServerUsing(Protocol givenProtocol) {
        return new LeshanTestServerBuilder(givenProtocol).with(new InMemorySecurityStore());
    }

    @AfterEach
    public void stop() throws InterruptedException {
        if (client != null)
            client.destroy(false);
        if (server != null)
            server.destroy();
    }

    /*---------------------------------/
     *  Tests
     * -------------------------------*/
    @TestAllCases
    public void can_send_resources_psk(ContentFormat contentformat, Protocol givenProtocol,
            String givenClientEndpointProvider, String givenServerEndpointProvider)
            throws InterruptedException, TimeoutException, NonUniqueSecurityInfoException {

        //server = givenServerUsing(givenProtocol).with(givenServerEndpointProvider).build();
        server =givenServerUsing(new Protocol("COAPS", "coaps")).with(givenServerEndpointProvider).build();
        server.start();

        URI uri = server.getEndpoint(new Protocol("COAPS", "coaps")).getURI();
        nat = new SimpleNat(new InetSocketAddress("localhost", 0), new InetSocketAddress(uri.getHost(), uri.getPort()));
        nat.start();

        client = givenClientUsing(new Protocol("COAPS", "coaps")).with(givenClientEndpointProvider)
                .connectingTo(server)
                .usingPsk(GOOD_PSK_ID, GOOD_PSK_KEY)
                .build();

        server.getSecurityStore()
                .add(SecurityInfo.newPreSharedKeyInfo(client.getEndpointName(), GOOD_PSK_ID, GOOD_PSK_KEY));



        client.start();
        server.waitForNewRegistrationOf(client);
        client.waitForRegistrationTo(server);
        nat.changeAddress();
        // Send Data
        LwM2mServer registeredServer = client.getRegisteredServers().values().iterator().next();
        SendResponse response = client.getSendService().sendData(registeredServer, contentformat,
                Arrays.asList("/3/0/1", "/3/0/2"), 1000);
        assertThat(response.isSuccess()).isTrue();

        // wait for data and check result
        TimestampedLwM2mNodes data = server.waitForData(client.getEndpointName(), 1, TimeUnit.SECONDS);
        Map<LwM2mPath, LwM2mNode> nodes = data.getNodes();
        LwM2mResource modelnumber = (LwM2mResource) nodes.get(new LwM2mPath("/3/0/1"));
        assertThat(modelnumber.getId()).isEqualTo(1);
        assertThat(modelnumber.getValue()).isEqualTo("IT-TEST-123");

        LwM2mResource serialnumber = (LwM2mResource) nodes.get(new LwM2mPath("/3/0/2"));
        assertThat(serialnumber.getId()).isEqualTo(2);
        assertThat(serialnumber.getValue()).isEqualTo("12345");
    }


    @TestAllCases
    public void can_send_resources(ContentFormat contentformat, Protocol givenProtocol,
            String givenClientEndpointProvider, String givenServerEndpointProvider)
            throws InterruptedException, TimeoutException {

        server = givenServerUsing(givenProtocol).with(givenServerEndpointProvider).build();
        server.start();
        client = givenClientUsing(givenProtocol).with(givenClientEndpointProvider).connectingTo(server).build();
        client.start();
        server.waitForNewRegistrationOf(client);
        client.waitForRegistrationTo(server);

        // Send Data
        LwM2mServer registeredServer = client.getRegisteredServers().values().iterator().next();
        SendResponse response = client.getSendService().sendData(registeredServer, contentformat,
                Arrays.asList("/3/0/1", "/3/0/2"), 1000);
        assertThat(response.isSuccess()).isTrue();

        // wait for data and check result
        TimestampedLwM2mNodes data = server.waitForData(client.getEndpointName(), 1, TimeUnit.SECONDS);
        Map<LwM2mPath, LwM2mNode> nodes = data.getNodes();
        LwM2mResource modelnumber = (LwM2mResource) nodes.get(new LwM2mPath("/3/0/1"));
        assertThat(modelnumber.getId()).isEqualTo(1);
        assertThat(modelnumber.getValue()).isEqualTo("IT-TEST-123");

        LwM2mResource serialnumber = (LwM2mResource) nodes.get(new LwM2mPath("/3/0/2"));
        assertThat(serialnumber.getId()).isEqualTo(2);
        assertThat(serialnumber.getValue()).isEqualTo("12345");


    }


}
