package org.eclipse.leshan.integration.tests;

import org.assertj.core.api.Assertions;
import org.eclipse.californium.util.nat.NioNatUtil;
import org.eclipse.leshan.core.endpoint.Protocol;
import org.eclipse.leshan.core.request.ReadRequest;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.integration.tests.util.LeshanTestClient;
import org.eclipse.leshan.integration.tests.util.LeshanTestClientBuilder;
import org.eclipse.leshan.integration.tests.util.LeshanTestServer;
import org.eclipse.leshan.integration.tests.util.LeshanTestServerBuilder;
import org.eclipse.leshan.server.registration.Registration;
import org.eclipse.leshan.server.security.InMemorySecurityStore;
import org.eclipse.leshan.server.security.NonUniqueSecurityInfoException;
import org.eclipse.leshan.server.security.SecurityInfo;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.cert.CertificateEncodingException;

import static org.eclipse.leshan.integration.tests.util.Credentials.*;
import static org.eclipse.leshan.integration.tests.util.Credentials.clientPublicKey;
import static org.eclipse.leshan.integration.tests.util.LeshanTestClientBuilder.givenClientUsing;
import static org.eclipse.leshan.integration.tests.util.assertion.Assertions.assertThat;

public class IpChangeTest {
    Protocol givenProtocol = Protocol.COAPS;

    //TODO fill those
    String givenClientEndpointProvider = "";
    String givenServerEndpointProvider = "";

    private InetSocketAddress first_ip = new InetSocketAddress("127.0.0.1", 0);
    private InetSocketAddress second_ip = new InetSocketAddress("127.0.0.2", 0);
    protected LeshanTestServerBuilder givenServerUsing(Protocol givenProtocol) {
        return new LeshanTestServerBuilder(givenProtocol).with(new InMemorySecurityStore());
    }

    private Thread create_redirect(InetSocketAddress bindAddress, InetSocketAddress destination){
        NioNatUtil natUtil;
        try {
            natUtil = new NioNatUtil(bindAddress, destination);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new Thread(natUtil);
    }

    public void unsecured(){
        //TODO
    }
    @Test
    public void registered_device_with_psk_to_server_with_psk()
            throws NonUniqueSecurityInfoException, InterruptedException {
        LeshanTestServerBuilder givenServer;
        LeshanTestServer server;
        LeshanTestClientBuilder givenClient;
        LeshanTestClient client;

        givenServer = givenServerUsing(givenProtocol).with(givenServerEndpointProvider);
        givenClient = givenClientUsing(givenProtocol).with(givenClientEndpointProvider);

        // Create PSK server & start it
        server = givenServer.build(); // default server support PSK
        server.start();

        // Create PSK Client
        client = givenClient.connectingTo(server).usingPsk(GOOD_PSK_ID, GOOD_PSK_KEY).build();

        // Add client credentials to the server
        server.getSecurityStore()
                .add(SecurityInfo.newPreSharedKeyInfo(client.getEndpointName(), GOOD_PSK_ID, GOOD_PSK_KEY));

        // Check client is not registered
        assertThat(client).isNotRegisteredAt(server);

        // Start it and wait for registration
        client.start();
        server.waitForNewRegistrationOf(client);

        // Check client is well registered
        assertThat(client).isRegisteredAt(server);
        Registration registration = server.getRegistrationFor(client);

        // check we can send request to client.
        ReadResponse response = server.send(registration, new ReadRequest(3, 0, 1), 500);
        Assertions.assertThat(response.isSuccess()).isTrue();

        Thread redirect = create_redirect(first_ip, second_ip);

        redirect.start();

        // check we can send request to client.
        response = server.send(registration, new ReadRequest(3, 0, 1), 500);
        Assertions.assertThat(response.isSuccess()).isTrue();

        redirect.stop();

        if (client != null)
            client.destroy(false);
        if (server != null)
            server.destroy();
    }

    @Test
    public void registered_device_with_rpk_to_server_with_rpk()
            throws NonUniqueSecurityInfoException, InterruptedException {
        LeshanTestServerBuilder givenServer;
        LeshanTestServer server;
        LeshanTestClientBuilder givenClient;
        LeshanTestClient client;

        givenServer = givenServerUsing(givenProtocol).with(givenServerEndpointProvider);
        givenClient = givenClientUsing(givenProtocol).with(givenClientEndpointProvider);

        // Create RPK server & start it
        server = givenServer.using(serverPublicKey, serverPrivateKey).build();
        server.start();

        // Create RPK Client
        client = givenClient.connectingTo(server) //
                .using(clientPublicKey, clientPrivateKey)//
                .trusting(serverPublicKey).build();

        // Add client credentials to the server
        server.getSecurityStore().add(SecurityInfo.newRawPublicKeyInfo(client.getEndpointName(), clientPublicKey));

        // Check client is not registered
        assertThat(client).isNotRegisteredAt(server);

        // Start it and wait for registration
        client.start();
        server.waitForNewRegistrationOf(client);

        // Check client is well registered
        assertThat(client).isRegisteredAt(server);
        Registration registration = server.getRegistrationFor(client);

        // check we can send request to client.
        ReadResponse response = server.send(registration, new ReadRequest(3, 0, 1), 500);
        assertThat(response.isSuccess()).isTrue();

        Thread redirect = create_redirect(first_ip, second_ip);

        redirect.start();

        // check we can send request to client.
        response = server.send(registration, new ReadRequest(3, 0, 1), 500);
        Assertions.assertThat(response.isSuccess()).isTrue();

        redirect.stop();

        if (client != null)
            client.destroy(false);
        if (server != null)
            server.destroy();
    }

    @Test
    public void registered_device_with_x509cert_to_server_with_x509cert()
            throws NonUniqueSecurityInfoException, CertificateEncodingException, InterruptedException {
        LeshanTestServerBuilder givenServer;
        LeshanTestServer server;
        LeshanTestClientBuilder givenClient;
        LeshanTestClient client;

        givenServer = givenServerUsing(givenProtocol).with(givenServerEndpointProvider);
        givenClient = givenClientUsing(givenProtocol).with(givenClientEndpointProvider);



        // Create X509 server & start it
        server = givenServer //
                .actingAsServerOnly()//
                .using(serverX509Cert, serverPrivateKeyFromCert)//
                .trusting(trustedCertificates).build();
        server.start();

        // Create X509 Client
        client = givenClient.connectingTo(server) //
                .using(clientX509Cert, clientPrivateKeyFromCert)//
                .trusting(serverX509Cert).build();

        // Add client credentials to the server
        server.getSecurityStore().add(SecurityInfo.newX509CertInfo(client.getEndpointName()));

        // Check client is not registered
        assertThat(client).isNotRegisteredAt(server);

        // Start it and wait for registration
        client.start();
        server.waitForNewRegistrationOf(client);

        // Check client is well registered
        assertThat(client).isRegisteredAt(server);
        Registration registration = server.getRegistrationFor(client);

        // check we can send request to client.
        ReadResponse response = server.send(registration, new ReadRequest(3, 0, 1), 500);
        assertThat(response.isSuccess()).isTrue();

        Thread redirect = create_redirect(first_ip, second_ip);

        redirect.start();

        // check we can send request to client.
        response = server.send(registration, new ReadRequest(3, 0, 1), 500);
        Assertions.assertThat(response.isSuccess()).isTrue();

        redirect.stop();

        if (client != null)
            client.destroy(false);
        if (server != null)
            server.destroy();
    }
}
