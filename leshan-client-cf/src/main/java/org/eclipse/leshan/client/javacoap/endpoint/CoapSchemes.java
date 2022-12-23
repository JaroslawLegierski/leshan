package org.eclipse.leshan.client.javacoap.endpoint;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.leshan.client.javacoap.endpoint.providers.JdkProvider;
import org.eclipse.leshan.client.javacoap.endpoint.providers.MbedtlsProvider;
import org.eclipse.leshan.client.javacoap.endpoint.providers.OpensslProvider;
import org.eclipse.leshan.client.javacoap.endpoint.providers.Pair;
import org.eclipse.leshan.client.javacoap.endpoint.providers.PlainTextProvider;
import org.eclipse.leshan.client.javacoap.endpoint.providers.StandardIoProvider;

import com.mbed.coap.packet.Opaque;
import com.mbed.coap.server.CoapServerBuilder;
import com.mbed.coap.server.CoapServerBuilderForTcp;
import com.mbed.coap.transport.javassl.CoapSerializer;

public class CoapSchemes {
    public static char[] secret() {
        return "".toCharArray();
    }

    public final CoapServerBuilder create(TransportProvider transportProvider, String keystoreFile,
            Pair<String, Opaque> psk, URI uri) {

        try {
            return create(transportProvider, loadKeystore(keystoreFile), psk, uri);
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String supportedSchemes() {
        return "coap, coap+tcp, coaps, coaps+tcp, coaps+tcp-d2 (draft 2)";
    }

    protected CoapServerBuilder create(TransportProvider transportProvider, KeyStore ks, Pair<String, Opaque> psk,
            URI uri) throws GeneralSecurityException, IOException {
        InetSocketAddress destAdr = addressFromUri(uri);

        switch (uri.getScheme()) {
        case "coap":
            return CoapServerBuilder.newBuilder()
                    .transport(new PlainTextProvider().createUDP(CoapSerializer.UDP, destAdr, ks, psk));

        case "coap+tcp":
            return CoapServerBuilderForTcp.newBuilderForTcp()
                    .transport(new PlainTextProvider().createTCP(CoapSerializer.TCP, destAdr, ks));

        case "coaps":
            return CoapServerBuilder.newBuilder()
                    .transport(transportProvider.createUDP(CoapSerializer.UDP, destAdr, ks, psk));

        case "coaps+tcp":
            return CoapServerBuilderForTcp.newBuilderForTcp()
                    .transport(transportProvider.createTCP(CoapSerializer.TCP, destAdr, ks));

        case "coaps+tcp-d2":
            return CoapServerBuilder.newBuilder()
                    .transport(transportProvider.createTCP(CoapSerializer.UDP, destAdr, ks));

        default:
            throw new IllegalArgumentException("Scheme not supported: " + uri.getScheme());
        }
    }

    protected static InetSocketAddress addressFromUri(URI uri) {
        return new InetSocketAddress(uri.getHost(), uri.getPort());
    }

    private static KeyStore loadKeystore(String keystoreFile) {
        KeyStore ks = null;
        if (keystoreFile != null) {
            try (FileInputStream f = new FileInputStream(keystoreFile)) {
                ks = KeyStore.getInstance("JKS");
                ks.load(f, secret());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return ks;
    }

    public TransportProvider transportProviderFor(String transport) {
        switch (transport.toLowerCase()) {
        case "jdk":
            return new JdkProvider();
        case "openssl":
            return new OpensslProvider();
        case "stdio":
            return new StandardIoProvider();
        case "mbedtls":
            return new MbedtlsProvider();
        default:
            throw new IllegalArgumentException("Not supported transport: " + transport);
        }
    }

    public TransportProvider defaultProvider() {
        return new JdkProvider();
    }

    public static String findKeyAlias(KeyStore ks) throws KeyStoreException {
        ArrayList<String> aliases = Collections.list(ks.aliases());

        for (String alias : aliases) {
            if (ks.isKeyEntry(alias) && !"ca".equals(alias)) {
                return alias;
            }
        }
        return null;
    }

    public static List<X509Certificate> readCAs(KeyStore ks) throws KeyStoreException {
        List<X509Certificate> certs = new LinkedList<>();
        Enumeration<String> aliases = ks.aliases();
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            if (!ks.isKeyEntry(alias)) {
                certs.add((X509Certificate) ks.getCertificate(alias));
            }
        }

        return certs;
    }

}
