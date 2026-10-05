// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;

import javax.net.ssl.ExtendedSSLSession;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.security.AlgorithmConstraints;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class KeyVaultTrustManagerTest {
    private static final String MATCHING_HOST = "service.example";
    private static final String DIFFERENT_HOST = "other.example";
    private static X509Certificate rootCertificate;
    private static X509Certificate unrelatedCertificate;
    private static X509Certificate leafCertificate;
    private static SSLContext serverContext;

    enum TrustSource {
        CA, LEAF, REFRESHED_LEAF, UNTRUSTED
    }

    @BeforeAll
    static void createCertificates() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair root = generator.generateKeyPair();
        KeyPair unrelated = generator.generateKeyPair();
        KeyPair leaf = generator.generateKeyPair();
        rootCertificate = certificate(root, "CN=Test CA", root, "CN=Test CA", true, 1);
        unrelatedCertificate = certificate(unrelated, "CN=Other CA", unrelated, "CN=Other CA", true, 2);
        leafCertificate = certificate(leaf, "CN=" + MATCHING_HOST, root, "CN=Test CA", false, 3);

        KeyStore keys = emptyKeyStore();
        char[] password = "test-password".toCharArray();
        keys.setKeyEntry("server", leaf.getPrivate(), password, new Certificate[] { leafCertificate, rootCertificate });
        KeyManagerFactory factory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        factory.init(keys, password);
        serverContext = SSLContext.getInstance("TLS");
        serverContext.init(factory.getKeyManagers(), null, null);
    }

    static Stream<Arguments> contextualChecks() {
        return Arrays.stream(TrustSource.values())
            .flatMap(source -> Stream.of(false, true)
                .flatMap(client -> Stream.of(false, true).map(engine -> Arguments.of(source, client, engine))));
    }

    @ParameterizedTest
    @MethodSource("contextualChecks")
    void checksPeerName(TrustSource source, boolean client, boolean engine) throws Exception {
        X509ExtendedTrustManager manager = trustManager(source);
        if (source == TrustSource.UNTRUSTED) {
            assertThrows(CertificateException.class, () -> check(manager, client, engine, MATCHING_HOST, "HTTPS"));
        } else {
            assertDoesNotThrow(() -> check(manager, client, engine, MATCHING_HOST, "HTTPS"));
        }
        assertThrows(CertificateException.class, () -> check(manager, client, engine, DIFFERENT_HOST, "HTTPS"));
    }

    @ParameterizedTest
    @MethodSource("contextualChecks")
    void preservesDisabledEndpointIdentification(TrustSource source, boolean client, boolean engine) throws Exception {
        X509ExtendedTrustManager manager = trustManager(source);
        if (source == TrustSource.UNTRUSTED) {
            assertThrows(CertificateException.class, () -> check(manager, client, engine, DIFFERENT_HOST, ""));
        } else {
            assertDoesNotThrow(() -> check(manager, client, engine, DIFFERENT_HOST, ""));
        }
    }

    @ParameterizedTest
    @EnumSource(TrustSource.class)
    void preservesTwoArgumentChecks(TrustSource source) throws Exception {
        X509ExtendedTrustManager manager = trustManager(source);
        X509Certificate[] chain = { leafCertificate, rootCertificate };
        if (source == TrustSource.UNTRUSTED) {
            assertThrows(CertificateException.class, () -> manager.checkServerTrusted(chain, "RSA"));
            assertThrows(CertificateException.class, () -> manager.checkClientTrusted(chain, "RSA"));
        } else {
            assertDoesNotThrow(() -> manager.checkServerTrusted(chain, "RSA"));
            assertDoesNotThrow(() -> manager.checkClientTrusted(chain, "RSA"));
        }
    }

    @ParameterizedTest
    @EnumSource(TrustSource.class)
    void preservesNullContextChecks(TrustSource source) throws Exception {
        X509ExtendedTrustManager manager = trustManager(source);
        X509Certificate[] chain = { leafCertificate, rootCertificate };
        if (source == TrustSource.UNTRUSTED) {
            assertThrows(CertificateException.class, () -> manager.checkServerTrusted(chain, "RSA", (Socket) null));
            assertThrows(CertificateException.class, () -> manager.checkServerTrusted(chain, "RSA", (SSLEngine) null));
            assertThrows(CertificateException.class, () -> manager.checkClientTrusted(chain, "RSA", (Socket) null));
            assertThrows(CertificateException.class, () -> manager.checkClientTrusted(chain, "RSA", (SSLEngine) null));
        } else {
            assertDoesNotThrow(() -> manager.checkServerTrusted(chain, "RSA", (Socket) null));
            assertDoesNotThrow(() -> manager.checkServerTrusted(chain, "RSA", (SSLEngine) null));
            assertDoesNotThrow(() -> manager.checkClientTrusted(chain, "RSA", (Socket) null));
            assertDoesNotThrow(() -> manager.checkClientTrusted(chain, "RSA", (SSLEngine) null));
        }
    }

    @ParameterizedTest
    @MethodSource("contextualChecks")
    void rejectsUnsupportedEndpointIdentification(TrustSource source, boolean client, boolean engine) throws Exception {
        X509ExtendedTrustManager manager = trustManager(source);
        assertThrows(CertificateException.class, () -> check(manager, client, engine, MATCHING_HOST, "unsupported"));
    }

    @ParameterizedTest
    @MethodSource("contextualChecks")
    void preservesAlgorithmConstraints(TrustSource source, boolean client, boolean engine) throws Exception {
        X509ExtendedTrustManager manager = trustManager(source);
        AlgorithmConstraints rejectAllAlgorithms = mock(AlgorithmConstraints.class);
        KeyStore trustStore = emptyKeyStore();
        trustStore.setCertificateEntry("trusted",
            source == TrustSource.CA
                ? rootCertificate
                : source == TrustSource.UNTRUSTED ? unrelatedCertificate : leafCertificate);
        TrustManagerFactory factory = TrustManagerFactory.getInstance("PKIX", "SunJSSE");
        factory.init(trustStore);
        X509ExtendedTrustManager stock = (X509ExtendedTrustManager) factory.getTrustManagers()[0];
        if (source == TrustSource.LEAF || source == TrustSource.REFRESHED_LEAF) {
            // PKIX exempts the trust anchor itself from certificate-chain algorithm constraints.
            assertDoesNotThrow(() -> check(stock, client, engine, MATCHING_HOST, "HTTPS", rejectAllAlgorithms));
            assertDoesNotThrow(() -> check(manager, client, engine, MATCHING_HOST, "HTTPS", rejectAllAlgorithms));
        } else {
            assertThrows(CertificateException.class,
                () -> check(stock, client, engine, MATCHING_HOST, "HTTPS", rejectAllAlgorithms));
            assertThrows(CertificateException.class,
                () -> check(manager, client, engine, MATCHING_HOST, "HTTPS", rejectAllAlgorithms));
        }
    }

    @Test
    void rejectsProviderWithoutExtendedTrustManager() throws Exception {
        KeyStore trustStore = emptyKeyStore();
        TrustManagerFactory factory = mock(TrustManagerFactory.class);
        when(factory.getTrustManagers()).thenReturn(new TrustManager[] { mock(X509TrustManager.class) });
        try (MockedStatic<TrustManagerFactory> factories = mockStatic(TrustManagerFactory.class)) {
            factories.when(() -> TrustManagerFactory.getInstance("PKIX", "SunJSSE")).thenReturn(factory);
            factories.when(() -> TrustManagerFactory.getInstance("PKIX", "IbmJSSE")).thenReturn(factory);
            IllegalStateException failure
                = assertThrows(IllegalStateException.class, () -> new KeyVaultTrustManager(trustStore));
            assertTrue(failure.getCause() instanceof CertificateException);
        }
    }

    static Stream<Arguments> handshakeChecks() {
        return Arrays.stream(TrustSource.values())
            .flatMap(source -> Stream.of("TLSv1.2", "TLSv1.3").map(protocol -> Arguments.of(source, protocol)));
    }

    @ParameterizedTest
    @MethodSource("handshakeChecks")
    @Timeout(30)
    void enforcesPeerNameDuringHandshake(TrustSource source, String protocol) throws Exception {
        assumeTrue(Arrays.asList(serverContext.getSupportedSSLParameters().getProtocols()).contains(protocol));
        handshake(trustManager(source), MATCHING_HOST, protocol, source != TrustSource.UNTRUSTED);
        handshake(trustManager(source), DIFFERENT_HOST, protocol, false);
    }

    private static X509ExtendedTrustManager trustManager(TrustSource source) throws Exception {
        KeyStore trustStore = emptyKeyStore();
        trustStore.setCertificateEntry("trusted",
            source == TrustSource.CA
                ? rootCertificate
                : source == TrustSource.LEAF ? leafCertificate : unrelatedCertificate);
        TrustManagerFactory factory
            = TrustManagerFactory.getInstance("PKIX", new KeyVaultTrustManagerFactoryProvider());
        factory.init(trustStore);
        if (source == TrustSource.REFRESHED_LEAF) {
            // PKIX has already captured its trust anchors; the new leaf is available only through the fallback.
            trustStore.setCertificateEntry("new-leaf", leafCertificate);
        }
        return (X509ExtendedTrustManager) factory.getTrustManagers()[0];
    }

    private static void check(X509ExtendedTrustManager manager, boolean client, boolean engine, String hostname,
        String algorithm) throws CertificateException {
        check(manager, client, engine, hostname, algorithm, null);
    }

    private static void check(X509ExtendedTrustManager manager, boolean client, boolean engine, String hostname,
        String algorithm, AlgorithmConstraints constraints) throws CertificateException {
        ExtendedSSLSession session = mock(ExtendedSSLSession.class);
        when(session.getPeerHost()).thenReturn(hostname);
        when(session.getProtocol()).thenReturn("TLSv1.2");
        when(session.getLocalSupportedSignatureAlgorithms()).thenReturn(new String[] { "SHA256withRSA" });
        when(session.getRequestedServerNames()).thenReturn(Collections.emptyList());
        SSLParameters parameters = new SSLParameters();
        parameters.setEndpointIdentificationAlgorithm(algorithm);
        parameters.setAlgorithmConstraints(constraints);
        X509Certificate[] chain = { leafCertificate, rootCertificate };
        if (engine) {
            SSLEngine connection = mock(SSLEngine.class);
            when(connection.getHandshakeSession()).thenReturn(session);
            when(connection.getSSLParameters()).thenReturn(parameters);
            if (client) {
                manager.checkClientTrusted(chain, "RSA", connection);
            } else {
                manager.checkServerTrusted(chain, "RSA", connection);
            }
        } else {
            SSLSocket connection = mock(SSLSocket.class);
            when(connection.isConnected()).thenReturn(true);
            when(connection.getHandshakeSession()).thenReturn(session);
            when(connection.getSSLParameters()).thenReturn(parameters);
            if (client) {
                manager.checkClientTrusted(chain, "RSA", connection);
            } else {
                manager.checkServerTrusted(chain, "RSA", connection);
            }
        }
    }

    private static void handshake(X509ExtendedTrustManager manager, String hostname, String protocol, boolean accepted)
        throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try (SSLServerSocket server = (SSLServerSocket) serverContext.getServerSocketFactory()
            .createServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            server.setSoTimeout(5000);
            server.setEnabledProtocols(new String[] { protocol });
            Future<IOException> serverResult = executor.submit(() -> {
                try (SSLSocket connection = (SSLSocket) server.accept()) {
                    connection.setSoTimeout(5000);
                    try {
                        connection.startHandshake();
                        return null;
                    } catch (IOException ex) {
                        return ex;
                    }
                }
            });
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new TrustManager[] { manager }, null);
            try (Socket transport = new Socket(InetAddress.getLoopbackAddress(), server.getLocalPort());
                SSLSocket connection = (SSLSocket) context.getSocketFactory()
                    .createSocket(transport, hostname, server.getLocalPort(), true)) {
                connection.setSoTimeout(5000);
                SSLParameters parameters = connection.getSSLParameters();
                parameters.setEndpointIdentificationAlgorithm("HTTPS");
                connection.setSSLParameters(parameters);
                if (accepted) {
                    connection.startHandshake();
                } else {
                    assertThrows(SSLHandshakeException.class, connection::startHandshake);
                }
            }
            IOException serverFailure = serverResult.get(10, TimeUnit.SECONDS);
            if (accepted) {
                assertNull(serverFailure);
            } else {
                assertTrue(serverFailure instanceof SSLHandshakeException || serverFailure instanceof SocketException,
                    () -> "Expected a TLS alert or connection reset, got: " + serverFailure);
            }
        } finally {
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        }
    }

    private static KeyStore emptyKeyStore() throws Exception {
        KeyStore store = KeyStore.getInstance("JKS");
        store.load(null, null);
        return store;
    }

    private static X509Certificate certificate(KeyPair subject, String subjectName, KeyPair issuer, String issuerName,
        boolean ca, int serial) throws Exception {
        Instant now = Instant.now();
        JcaX509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(new X500Name(issuerName),
            BigInteger.valueOf(serial), Date.from(now.minusSeconds(3600)), Date.from(now.plusSeconds(86400)),
            new X500Name(subjectName), subject.getPublic());
        builder.addExtension(Extension.basicConstraints, true, new BasicConstraints(ca));
        builder.addExtension(Extension.keyUsage, true,
            new KeyUsage(ca ? KeyUsage.keyCertSign : KeyUsage.digitalSignature | KeyUsage.keyEncipherment));
        if (!ca) {
            builder.addExtension(Extension.subjectAlternativeName, false,
                new GeneralNames(new GeneralName(GeneralName.dNSName, MATCHING_HOST)));
            builder.addExtension(Extension.extendedKeyUsage, false, new ExtendedKeyUsage(
                new KeyPurposeId[] { KeyPurposeId.id_kp_serverAuth, KeyPurposeId.id_kp_clientAuth }));
        }
        return new JcaX509CertificateConverter()
            .getCertificate(builder.build(new JcaContentSignerBuilder("SHA256withRSA").build(issuer.getPrivate())));
    }
}
