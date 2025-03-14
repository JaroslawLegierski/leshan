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
package org.eclipse.leshan.transport.californium.client.endpoint.coaps;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.Principal;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.CoapEndpoint.Builder;
import org.eclipse.californium.elements.Connector;
import org.eclipse.californium.elements.EndpointContextMatcher;
import org.eclipse.californium.elements.PrincipalEndpointContextMatcher;
import org.eclipse.californium.elements.auth.RawPublicKeyIdentity;
import org.eclipse.californium.elements.config.Configuration;
import org.eclipse.californium.elements.util.CertPathUtil;
import org.eclipse.californium.scandium.DTLSConnector;
import org.eclipse.californium.scandium.config.DtlsConfig;
import org.eclipse.californium.scandium.config.DtlsConfig.DtlsRole;
import org.eclipse.californium.scandium.config.DtlsConnectorConfig;
import org.eclipse.californium.scandium.dtls.cipher.CipherSuite;
import org.eclipse.californium.scandium.dtls.pskstore.SinglePskStore;
import org.eclipse.californium.scandium.dtls.x509.CertificateVerifier;
import org.eclipse.californium.scandium.dtls.x509.SingleCertificateProvider;
import org.eclipse.californium.scandium.dtls.x509.StaticCertificateVerifier;
import org.eclipse.leshan.client.endpoint.ClientEndpointToolbox;
import org.eclipse.leshan.client.security.CertificateVerifierFactory;
import org.eclipse.leshan.client.servers.ServerInfo;
import org.eclipse.leshan.core.SecurityMode;
import org.eclipse.leshan.core.endpoint.EndpointUri;
import org.eclipse.leshan.core.endpoint.Protocol;
import org.eclipse.leshan.core.security.certificate.verifier.X509CertificateVerifier;
import org.eclipse.leshan.transport.californium.DefaultCoapsExceptionTranslator;
import org.eclipse.leshan.transport.californium.ExceptionTranslator;
import org.eclipse.leshan.transport.californium.Lwm2mEndpointContextMatcher;
import org.eclipse.leshan.transport.californium.client.CaliforniumConnectionController;
import org.eclipse.leshan.transport.californium.client.endpoint.coap.CoapClientEndpointFactory;
import org.eclipse.leshan.transport.californium.identity.DefaultCoapsIdentityHandler;
import org.eclipse.leshan.transport.californium.identity.IdentityHandler;
import org.eclipse.leshan.transport.californium.security.LwM2mCertificateVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoapsClientEndpointFactory extends CoapClientEndpointFactory {

    private static final Logger LOG = LoggerFactory.getLogger(CoapsClientEndpointFactory.class);

    protected final CertificateVerifierFactory certificateVerifierFactory = new CertificateVerifierFactory();

    public CoapsClientEndpointFactory() {
        this("LWM2M Client");
    }

    @Override
    public String getEndpointDescription() {
        return "CoAP over DTLS endpoint based on Californium/Scandium library";
    }

    public CoapsClientEndpointFactory(String loggingTagPrefix) {
        super(loggingTagPrefix);
    }

    @Override
    public Protocol getProtocol() {
        return Protocol.COAPS;
    }

    @Override
    protected String getLoggingTag(EndpointUri uri) {
        if (loggingTagPrefix != null) {
            return String.format("[%s-%s]", loggingTagPrefix, uri);
        } else {
            return String.format("[%s]", uri);
        }
    }

    @Override
    public CoapEndpoint createCoapEndpoint(InetAddress clientAddress, Configuration defaultConfiguration,
            ServerInfo serverInfo, boolean clientInitiatedOnly, List<Certificate> trustStore,
            ClientEndpointToolbox toolbox) {

        // we can not create CoAPs endpoint if server is not secure data.
        if (!serverInfo.isSecure()) {
            return null;
        }

        // create DTLS connector Config
        DtlsConnectorConfig.Builder rootConfigBuilder = createRootDtlsConnectorConfigBuilder(defaultConfiguration);
        DtlsConnectorConfig.Builder effectiveConfigBuilder = createEffectiveDtlsConnectorConfigBuilder(
                new InetSocketAddress(clientAddress, 0), serverInfo, rootConfigBuilder, defaultConfiguration,
                clientInitiatedOnly, trustStore);
        DtlsConnectorConfig dtlsConfig;
        try {
            dtlsConfig = effectiveConfigBuilder.build();
        } catch (IllegalStateException e) {
            LOG.warn("Unable to create DTLS config to create endpont to connect to {}.", serverInfo.getFullUri(), e);
            return null;
        }

        // create CoAP endpoint
        return createEndpointBuilder(dtlsConfig, defaultConfiguration, toolbox).build();
    }

    protected DtlsConnectorConfig.Builder createRootDtlsConnectorConfigBuilder(Configuration configuration) {
        return new DtlsConnectorConfig.Builder(configuration);
    }

    protected DtlsConnectorConfig.Builder createEffectiveDtlsConnectorConfigBuilder(InetSocketAddress addr,
            ServerInfo serverInfo, DtlsConnectorConfig.Builder rootDtlsConfigBuilder, Configuration coapConfig,
            boolean clientInitiatedOnly, List<Certificate> trustStore) {

        if (serverInfo.isSecure()) {
            DtlsConnectorConfig incompleteConfig = rootDtlsConfigBuilder.getIncompleteConfig();
            DtlsConnectorConfig.Builder effectiveBuilder = DtlsConnectorConfig.builder(incompleteConfig);
            effectiveBuilder.setAddress(addr);

            // Support PSK
            if (serverInfo.secureMode == SecurityMode.PSK) {
                SinglePskStore staticPskStore = new SinglePskStore(serverInfo.pskId, serverInfo.pskKey);
                effectiveBuilder.setPskStore(staticPskStore);
                filterCipherSuites(effectiveBuilder, incompleteConfig.getSupportedCipherSuites(), true, false);
            } else if (serverInfo.secureMode == SecurityMode.RPK) {
                // set identity
                SingleCertificateProvider singleCertificateProvider = new SingleCertificateProvider(
                        serverInfo.privateKey, serverInfo.publicKey);
                // we don't want to check Key Pair here, if we do it this should be done in BootstrapConsistencyChecker
                singleCertificateProvider.setVerifyKeyPair(false);
                effectiveBuilder.setCertificateIdentityProvider(singleCertificateProvider);
                // set RPK truststore
                final PublicKey expectedKey = serverInfo.serverPublicKey;
                CertificateVerifier rpkVerifier = new StaticCertificateVerifier.Builder()
                        .setTrustedRPKs(new RawPublicKeyIdentity(expectedKey)).build();
                effectiveBuilder.setCertificateVerifier(rpkVerifier);
                filterCipherSuites(effectiveBuilder, incompleteConfig.getSupportedCipherSuites(), false, true);
            } else if (serverInfo.secureMode == SecurityMode.X509) {
                // set identity
                SingleCertificateProvider singleCertificateProvider = new SingleCertificateProvider(
                        serverInfo.privateKey, new Certificate[] { serverInfo.clientCertificate });
                // we don't want to check Key Pair here, if we do it this should be done in BootstrapConsistencyChecker
                singleCertificateProvider.setVerifyKeyPair(false);
                effectiveBuilder.setCertificateIdentityProvider(singleCertificateProvider);

                // set certificate verifier
                X509CertificateVerifier certificateVerifier = certificateVerifierFactory.create(serverInfo, trustStore);
                effectiveBuilder.setCertificateVerifier(new LwM2mCertificateVerifier(certificateVerifier));

                // TODO We set CN with '*' as we are not able to know the CN for some certificate usage and so this is
                // not used anymore to identify a server with x509.
                // See : https://github.com/eclipse/leshan/issues/992
                filterCipherSuites(effectiveBuilder, incompleteConfig.getSupportedCipherSuites(), false, true);
            } else {
                throw new RuntimeException("Unable to create connector : unsupported security mode");
            }

            // activate SNI if needed
            if (serverInfo.sni != null) {
                effectiveBuilder.set(DtlsConfig.DTLS_USE_SERVER_NAME_INDICATION, true);
            }

            // Handle DTLS mode
            DtlsRole dtlsRole = incompleteConfig.getConfiguration().get(DtlsConfig.DTLS_ROLE);
            if (dtlsRole == null) {
                if (serverInfo.bootstrap) {
                    // For bootstrap no need to have DTLS role exchange
                    // and so we can set DTLS Connection as client only by default.
                    effectiveBuilder.set(DtlsConfig.DTLS_ROLE, DtlsRole.CLIENT_ONLY);
                } else if (clientInitiatedOnly) {
                    // if client initiated only we don't allow connector to work as server role.
                    effectiveBuilder.set(DtlsConfig.DTLS_ROLE, DtlsRole.CLIENT_ONLY);
                } else {
                    effectiveBuilder.set(DtlsConfig.DTLS_ROLE, DtlsRole.BOTH);
                }
            }

            if (incompleteConfig.getConfiguration().get(DtlsConfig.DTLS_ROLE) == DtlsRole.BOTH) {
                // Ensure that BOTH mode can be used or fallback to CLIENT_ONLY
                if (serverInfo.secureMode == SecurityMode.X509) {
                    X509Certificate certificate = (X509Certificate) serverInfo.clientCertificate;
                    if (CertPathUtil.canBeUsedForAuthentication(certificate, true)) {
                        if (!CertPathUtil.canBeUsedForAuthentication(certificate, false)) {
                            effectiveBuilder.set(DtlsConfig.DTLS_ROLE, DtlsRole.CLIENT_ONLY);
                            LOG.warn("Client certificate does not allow Server Authentication usage."
                                    + "\nThis will prevent a LWM2M server to initiate DTLS connection to this client."
                                    + "\nSee : https://github.com/eclipse/leshan/wiki/Server-Failover#about-connections");
                        }
                    }
                }
            }
            return effectiveBuilder;
        }
        return null;
    }

    private void filterCipherSuites(DtlsConnectorConfig.Builder dtlsConfigurationBuilder, List<CipherSuite> ciphers,
            boolean psk, boolean requireServerCertificateMessage) {
        if (ciphers == null)
            return;

        List<CipherSuite> filteredCiphers = new ArrayList<>();
        for (CipherSuite cipher : ciphers) {
            if (psk && cipher.isPskBased()) {
                filteredCiphers.add(cipher);
            } else if (requireServerCertificateMessage && cipher.requiresServerCertificateMessage()) {
                filteredCiphers.add(cipher);
            }
        }
        dtlsConfigurationBuilder.set(DtlsConfig.DTLS_CIPHER_SUITES, filteredCiphers);
    }

    /**
     * This method is intended to be overridden.
     *
     * @param dtlsConfig the DTLS config used to create this endpoint.
     * @param coapConfig the CoAP config used to create this endpoint.
     * @return the {@link Builder} used for secured communication.
     */
    protected CoapEndpoint.Builder createEndpointBuilder(DtlsConnectorConfig dtlsConfig, Configuration coapConfig,
            ClientEndpointToolbox toolbox) {

        CoapEndpoint.Builder builder = new CoapEndpoint.Builder();
        builder.setConnector(createSecuredConnector(dtlsConfig));
        builder.setConfiguration(coapConfig);
        builder.setLoggingTag(getLoggingTag(
                toolbox.getUriHandler().createUri(getProtocol().getUriScheme(), dtlsConfig.getAddress())));

        EndpointContextMatcher securedContextMatcher = createSecuredContextMatcher();
        builder.setEndpointContextMatcher(securedContextMatcher);

        return builder;
    }

    /**
     * For server {@link Lwm2mEndpointContextMatcher} is created. <br>
     * For client {@link PrincipalEndpointContextMatcher} is created.
     * <p>
     * This method is intended to be overridden.
     *
     * @return the {@link EndpointContextMatcher} used for secured communication
     */
    protected EndpointContextMatcher createSecuredContextMatcher() {
        return new PrincipalEndpointContextMatcher() {
            @Override
            protected boolean matchPrincipals(Principal requestedPrincipal, Principal availablePrincipal) {
                // As we are using 1 connector/endpoint by server at client side,
                // and connector strongly limit connection from/to the expected foreign peer,
                // we don't need to re-check principal at EndpointContextMatcher level.
                return true;
            }
        };
    }

    /**
     * By default create a {@link DTLSConnector}.
     * <p>
     * This method is intended to be overridden.
     *
     * @param dtlsConfig the DTLS config used to create the Secured Connector.
     * @return the {@link Connector} used for unsecured {@link CoapEndpoint}
     */
    protected Connector createSecuredConnector(DtlsConnectorConfig dtlsConfig) {
        return new DTLSConnector(dtlsConfig);
    }

    @Override
    public IdentityHandler createIdentityHandler() {
        return new DefaultCoapsIdentityHandler(true);

    }

    @Override
    public CaliforniumConnectionController createConnectionController() {
        return (endpoint, server, resume) -> {
            Connector connector = ((CoapEndpoint) endpoint).getConnector();
            if (connector instanceof DTLSConnector) {
                if (resume) {
                    LOG.info("Clear DTLS session for resumption for server {}", server.getUri());
                    ((DTLSConnector) connector).forceResumeAllSessions();
                } else {
                    LOG.info("Clear DTLS session for server {}", server.getUri());
                    ((DTLSConnector) connector).clearConnectionState();
                }
            }
        };
    }

    @Override
    public ExceptionTranslator createExceptionTranslator() {
        return new DefaultCoapsExceptionTranslator();
    }
}
