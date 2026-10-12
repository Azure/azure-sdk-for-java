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
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;

import javax.net.ssl.ExtendedSSLSession;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
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
import java.nio.ByteBuffer;
import java.security.AlgorithmConstraints;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchProviderException;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class KeyVaultTrustManagerTest {
    private static final String MATCHING_HOST = "service.example";
    private static final String DIFFERENT_HOST = "other.example";
    private static X509Certificate rootCertificate;
    private static X509Certificate unrelatedCertificate;
    private static X509Certificate leafCertificate;
    private static X509Certificate commonNameCertificate;
    private static SSLContext serverContext;
    private static KeyManager[] keyManagers;

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
        leafCertificate = certificate(leaf, "CN=" + DIFFERENT_HOST, root, "CN=Test CA", false, 3);
        commonNameCertificate = certificate(leaf, "CN=" + MATCHING_HOST, root, "CN=Test CA", false, 4, false);

        KeyStore keys = emptyKeyStore();
        char[] password = "test-password".toCharArray();
        keys.setKeyEntry("server", leaf.getPrivate(), password, new Certificate[] { leafCertificate, rootCertificate });
        KeyManagerFactory factory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        factory.init(keys, password);
        keyManagers = factory.getKeyManagers();
        serverContext = SSLContext.getInstance("TLS");
        serverContext.init(keyManagers, null, null);
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
    void preservesSupportedPeerIdentities(TrustSource source, boolean client, boolean engine) throws Exception {
        X509ExtendedTrustManager manager = trustManager(source);
        for (String hostname : new String[] {
            MATCHING_HOST,
            "SERVICE.EXAMPLE",
            "service.example.",
            "api.cluster.example",
            "127.0.0.1",
            "::1",
            "[::1]",
            "xn--bcher-kva.example" }) {
            if (source == TrustSource.UNTRUSTED) {
                assertThrows(CertificateException.class, () -> check(manager, client, engine, hostname, "HTTPS"));
            } else {
                assertDoesNotThrow(() -> check(manager, client, engine, hostname, "HTTPS"));
            }
        }
        assertThrows(CertificateException.class,
            () -> check(manager, client, engine, "deep.api.cluster.example", "HTTPS"));
    }

    @ParameterizedTest
    @MethodSource("contextualChecks")
    void preservesCommonNameMatchingWhenDnsSansAreAbsent(TrustSource source, boolean client, boolean engine)
        throws Exception {
        X509ExtendedTrustManager manager = trustManager(source, commonNameCertificate);
        X509Certificate[] chain = { commonNameCertificate, rootCertificate };
        if (source == TrustSource.UNTRUSTED) {
            assertThrows(CertificateException.class,
                () -> check(manager, client, engine, MATCHING_HOST, "HTTPS", null, chain));
        } else {
            assertDoesNotThrow(() -> check(manager, client, engine, MATCHING_HOST, "HTTPS", null, chain));
        }
        assertThrows(CertificateException.class,
            () -> check(manager, client, engine, DIFFERENT_HOST, "HTTPS", null, chain));
    }

    @ParameterizedTest
    @MethodSource("contextualChecks")
    void preservesSupportedIdentificationAlgorithms(TrustSource source, boolean client, boolean engine)
        throws Exception {
        X509ExtendedTrustManager manager = trustManager(source);
        for (String algorithm : new String[] { null, "", "HTTPS", "https", "LDAP", "LDAPS" }) {
            if (source == TrustSource.UNTRUSTED) {
                assertThrows(CertificateException.class,
                    () -> check(manager, client, engine, MATCHING_HOST, algorithm));
            } else {
                assertDoesNotThrow(() -> check(manager, client, engine, MATCHING_HOST, algorithm));
                if (algorithm == null || algorithm.isEmpty()) {
                    assertDoesNotThrow(() -> check(manager, client, engine, DIFFERENT_HOST, algorithm));
                } else {
                    assertThrows(CertificateException.class,
                        () -> check(manager, client, engine, DIFFERENT_HOST, algorithm));
                }
            }
        }
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
    @EnumSource(TrustSource.class)
    void rejectsMissingHandshakeSession(TrustSource source) throws Exception {
        X509ExtendedTrustManager manager = trustManager(source);
        X509Certificate[] chain = { leafCertificate, rootCertificate };
        SSLEngine engine = mock(SSLEngine.class);
        SSLSocket socket = mock(SSLSocket.class);
        when(socket.isConnected()).thenReturn(true);
        assertThrows(CertificateException.class, () -> manager.checkServerTrusted(chain, "RSA", engine));
        assertThrows(CertificateException.class, () -> manager.checkClientTrusted(chain, "RSA", engine));
        assertThrows(CertificateException.class, () -> manager.checkServerTrusted(chain, "RSA", socket));
        assertThrows(CertificateException.class, () -> manager.checkClientTrusted(chain, "RSA", socket));
    }

    @ParameterizedTest
    @EnumSource(TrustSource.class)
    void preservesNonTlsAndDisconnectedSocketChecks(TrustSource source) throws Exception {
        X509ExtendedTrustManager manager = trustManager(source);
        X509Certificate[] chain = { leafCertificate, rootCertificate };
        try (Socket plain = new Socket();
            SSLSocket disconnected = (SSLSocket) SSLContext.getDefault().getSocketFactory().createSocket()) {
            SSLParameters parameters = disconnected.getSSLParameters();
            parameters.setEndpointIdentificationAlgorithm("HTTPS");
            disconnected.setSSLParameters(parameters);
            for (Socket socket : new Socket[] { plain, disconnected }) {
                if (source == TrustSource.UNTRUSTED) {
                    assertThrows(CertificateException.class, () -> manager.checkClientTrusted(chain, "RSA", socket));
                    assertThrows(CertificateException.class, () -> manager.checkServerTrusted(chain, "RSA", socket));
                } else {
                    assertDoesNotThrow(() -> manager.checkClientTrusted(chain, "RSA", socket));
                    assertDoesNotThrow(() -> manager.checkServerTrusted(chain, "RSA", socket));
                }
            }
        }
    }

    static Stream<Arguments> invalidInputs() {
        return Stream.of(Arguments.of(null, "RSA"), Arguments.of(new X509Certificate[0], "RSA"),
            Arguments.of(new X509Certificate[] { leafCertificate }, null),
            Arguments.of(new X509Certificate[] { leafCertificate }, ""));
    }

    @ParameterizedTest
    @MethodSource("invalidInputs")
    void preservesInvalidInputContract(X509Certificate[] chain, String authType) throws Exception {
        X509ExtendedTrustManager manager = trustManager(TrustSource.REFRESHED_LEAF);
        SSLEngine engine = SSLContext.getDefault().createSSLEngine();
        try (Socket socket = new Socket()) {
            Stream
                .<Executable>of(() -> manager.checkClientTrusted(chain, authType),
                    () -> manager.checkServerTrusted(chain, authType),
                    () -> manager.checkClientTrusted(chain, authType, engine),
                    () -> manager.checkServerTrusted(chain, authType, engine),
                    () -> manager.checkClientTrusted(chain, authType, socket),
                    () -> manager.checkServerTrusted(chain, authType, socket))
                .forEach(check -> assertThrows(IllegalArgumentException.class, check));
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

    @Test
    void preservesValidationFailureWhenFallbackInitializationFails() throws Exception {
        KeyStore trustStore = emptyKeyStore();
        trustStore.setCertificateEntry("trusted", leafCertificate);
        TrustManagerFactory factory = mock(TrustManagerFactory.class);
        X509ExtendedTrustManager delegate = mock(X509ExtendedTrustManager.class);
        when(factory.getTrustManagers()).thenReturn(new TrustManager[] { delegate });
        X509Certificate[] chain = { leafCertificate, rootCertificate };
        CertificateException original = new CertificateException("Initial validation failed");
        doThrow(original).when(delegate).checkServerTrusted(chain, "RSA", (SSLEngine) null);
        try (MockedStatic<TrustManagerFactory> factories = mockStatic(TrustManagerFactory.class)) {
            factories.when(() -> TrustManagerFactory.getInstance("PKIX", "SunJSSE"))
                .thenReturn(factory)
                .thenThrow(new NoSuchProviderException("SunJSSE unavailable"));
            factories.when(() -> TrustManagerFactory.getInstance("PKIX", "IbmJSSE"))
                .thenThrow(new NoSuchProviderException("IbmJSSE unavailable"));
            KeyVaultTrustManager manager = new KeyVaultTrustManager(trustStore);
            CertificateException failure = assertThrows(CertificateException.class,
                () -> manager.checkServerTrusted(chain, "RSA", (SSLEngine) null));
            assertTrue(Arrays.asList(failure.getSuppressed()).contains(original));
        }
    }

    @Test
    void preservesOriginalFailureWhenFallbackCertificateIsAbsent() throws Exception {
        KeyStore trustStore = emptyKeyStore();
        TrustManagerFactory factory = mock(TrustManagerFactory.class);
        X509ExtendedTrustManager delegate = mock(X509ExtendedTrustManager.class);
        when(factory.getTrustManagers()).thenReturn(new TrustManager[] { delegate });
        X509Certificate[] chain = { leafCertificate, rootCertificate };
        CertificateException original = new CertificateException("Initial validation failed");
        doThrow(original).when(delegate).checkServerTrusted(chain, "RSA", (SSLEngine) null);
        try (MockedStatic<TrustManagerFactory> factories = mockStatic(TrustManagerFactory.class)) {
            factories.when(() -> TrustManagerFactory.getInstance("PKIX", "SunJSSE")).thenReturn(factory);
            KeyVaultTrustManager manager = new KeyVaultTrustManager(trustStore);
            assertSame(original, assertThrows(CertificateException.class,
                () -> manager.checkServerTrusted(chain, "RSA", (SSLEngine) null)));
        }
    }

    @Test
    void preservesIbmProviderFallback() throws Exception {
        KeyStore trustStore = emptyKeyStore();
        TrustManagerFactory factory = mock(TrustManagerFactory.class);
        X509ExtendedTrustManager delegate = mock(X509ExtendedTrustManager.class);
        when(factory.getTrustManagers()).thenReturn(new TrustManager[] { mock(X509TrustManager.class), delegate });
        try (MockedStatic<TrustManagerFactory> factories = mockStatic(TrustManagerFactory.class)) {
            factories.when(() -> TrustManagerFactory.getInstance("PKIX", "SunJSSE"))
                .thenThrow(new NoSuchProviderException("SunJSSE unavailable"));
            factories.when(() -> TrustManagerFactory.getInstance("PKIX", "IbmJSSE")).thenReturn(factory);
            KeyVaultTrustManager manager = new KeyVaultTrustManager(trustStore);
            X509Certificate[] chain = { leafCertificate, rootCertificate };
            manager.checkClientTrusted(chain, "RSA", (SSLEngine) null);
            verify(factory).init(trustStore);
            verify(delegate).checkClientTrusted(chain, "RSA", (SSLEngine) null);
        }
    }

    @Test
    void preservesSystemTrustForNullKeyStore() throws Exception {
        KeyStore fallbackStore = emptyKeyStore();
        TrustManagerFactory factory = mock(TrustManagerFactory.class);
        when(factory.getTrustManagers()).thenReturn(new TrustManager[] { mock(X509ExtendedTrustManager.class) });
        try (MockedStatic<KeyStore> stores = mockStatic(KeyStore.class);
            MockedStatic<TrustManagerFactory> factories = mockStatic(TrustManagerFactory.class)) {
            stores.when(() -> KeyStore.getInstance(KeyVaultKeyStore.KEY_STORE_TYPE)).thenReturn(fallbackStore);
            factories.when(() -> TrustManagerFactory.getInstance("PKIX", "SunJSSE")).thenReturn(factory);
            new KeyVaultTrustManager();
            verify(factory).init((KeyStore) null);
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

    @ParameterizedTest
    @MethodSource("handshakeChecks")
    @Timeout(30)
    void enforcesPeerNameDuringEngineHandshake(TrustSource source, String protocol) throws Exception {
        assumeTrue(Arrays.asList(serverContext.getSupportedSSLParameters().getProtocols()).contains(protocol));
        X509ExtendedTrustManager manager = trustManager(source);
        if (source == TrustSource.UNTRUSTED) {
            SSLHandshakeException failure
                = assertThrows(SSLHandshakeException.class, () -> engineHandshake(manager, MATCHING_HOST, protocol));
            assertTrue(hasCertificateCause(failure));
        } else {
            engineHandshake(manager, MATCHING_HOST, protocol);
        }
        SSLHandshakeException failure
            = assertThrows(SSLHandshakeException.class, () -> engineHandshake(manager, DIFFERENT_HOST, protocol));
        assertTrue(hasCertificateCause(failure));
    }

    @ParameterizedTest
    @MethodSource("handshakeChecks")
    @Timeout(30)
    void validatesClientCertificateDuringMutualTls(TrustSource source, String protocol) throws Exception {
        assumeTrue(Arrays.asList(serverContext.getSupportedSSLParameters().getProtocols()).contains(protocol));
        SSLContext server = SSLContext.getInstance("TLS");
        server.init(keyManagers, new TrustManager[] { trustManager(source) }, null);
        SSLContext client = SSLContext.getInstance("TLS");
        client.init(keyManagers, new TrustManager[] { trustManager(TrustSource.CA) }, null);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try (SSLServerSocket listener = (SSLServerSocket) server.getServerSocketFactory()
            .createServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            listener.setSoTimeout(5000);
            listener.setEnabledProtocols(new String[] { protocol });
            listener.setNeedClientAuth(true);
            Future<IOException> serverResult = executor.submit(() -> {
                try (SSLSocket connection = (SSLSocket) listener.accept()) {
                    connection.setSoTimeout(5000);
                    try {
                        connection.startHandshake();
                        connection.getOutputStream().write(connection.getInputStream().read());
                        return null;
                    } catch (IOException ex) {
                        return ex;
                    }
                }
            });
            try (Socket transport = new Socket(InetAddress.getLoopbackAddress(), listener.getLocalPort());
                SSLSocket connection = (SSLSocket) client.getSocketFactory()
                    .createSocket(transport, MATCHING_HOST, listener.getLocalPort(), true)) {
                connection.setSoTimeout(5000);
                SSLParameters parameters = connection.getSSLParameters();
                parameters.setEndpointIdentificationAlgorithm("HTTPS");
                connection.setSSLParameters(parameters);
                Executable exchange = () -> {
                    connection.startHandshake();
                    connection.getOutputStream().write(42);
                    assertEquals(42, connection.getInputStream().read());
                };
                if (source == TrustSource.UNTRUSTED) {
                    assertThrows(IOException.class, exchange);
                } else {
                    assertDoesNotThrow(exchange);
                }
            }
            IOException failure = serverResult.get(10, TimeUnit.SECONDS);
            if (source == TrustSource.UNTRUSTED) {
                assertTrue(hasCertificateCause(failure),
                    () -> "Expected client certificate rejection, got: " + failure);
            } else {
                assertNull(failure);
            }
        } finally {
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        }
    }

    private static void engineHandshake(X509ExtendedTrustManager manager, String hostname, String protocol)
        throws Exception {
        SSLContext clientContext = SSLContext.getInstance("TLS");
        clientContext.init(null, new TrustManager[] { manager }, null);
        SSLEngine client = clientContext.createSSLEngine(hostname, 443);
        client.setUseClientMode(true);
        client.setEnabledProtocols(new String[] { protocol });
        SSLParameters parameters = client.getSSLParameters();
        parameters.setEndpointIdentificationAlgorithm("HTTPS");
        client.setSSLParameters(parameters);
        SSLEngine server = serverContext.createSSLEngine();
        server.setUseClientMode(false);
        server.setEnabledProtocols(new String[] { protocol });
        ByteBuffer clientToServer = ByteBuffer.allocate(client.getSession().getPacketBufferSize() * 2);
        ByteBuffer serverToClient = ByteBuffer.allocate(server.getSession().getPacketBufferSize() * 2);
        ByteBuffer application = ByteBuffer.allocate(client.getSession().getApplicationBufferSize() * 2);
        client.beginHandshake();
        server.beginHandshake();
        for (int step = 0; step < 1000; step++) {
            advanceHandshake(client, serverToClient, clientToServer, application);
            advanceHandshake(server, clientToServer, serverToClient, application);
            if (client.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING
                && server.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
                return;
            }
        }
        throw new AssertionError("TLS engine handshake made no progress: " + client.getHandshakeStatus() + "/"
            + server.getHandshakeStatus());
    }

    private static void advanceHandshake(SSLEngine engine, ByteBuffer inbound, ByteBuffer outbound,
        ByteBuffer application) throws IOException {
        Runnable task;
        while ((task = engine.getDelegatedTask()) != null) {
            task.run();
        }
        switch (engine.getHandshakeStatus()) {
            case NEED_WRAP:
                assertEquals(SSLEngineResult.Status.OK, engine.wrap(ByteBuffer.allocate(0), outbound).getStatus());
                break;

            case NEED_UNWRAP:
                if (inbound.position() > 0) {
                    inbound.flip();
                    SSLEngineResult.Status status = engine.unwrap(inbound, application).getStatus();
                    inbound.compact();
                    assertTrue(
                        status == SSLEngineResult.Status.OK || status == SSLEngineResult.Status.BUFFER_UNDERFLOW);
                }
                break;

            default:
                break;
        }
    }

    private static boolean hasCertificateCause(Throwable failure) {
        for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
            if (cause instanceof CertificateException) {
                return true;
            }
        }
        return false;
    }

    private static X509ExtendedTrustManager trustManager(TrustSource source) throws Exception {
        return trustManager(source, leafCertificate);
    }

    private static X509ExtendedTrustManager trustManager(TrustSource source, X509Certificate certificate)
        throws Exception {
        KeyStore trustStore = emptyKeyStore();
        trustStore.setCertificateEntry("trusted",
            source == TrustSource.CA
                ? rootCertificate
                : source == TrustSource.LEAF ? certificate : unrelatedCertificate);
        TrustManagerFactory factory
            = TrustManagerFactory.getInstance("PKIX", new KeyVaultTrustManagerFactoryProvider());
        factory.init(trustStore);
        if (source == TrustSource.REFRESHED_LEAF) {
            // PKIX has already captured its trust anchors; the new leaf is available only through the fallback.
            trustStore.setCertificateEntry("new-leaf", certificate);
        }
        return (X509ExtendedTrustManager) factory.getTrustManagers()[0];
    }

    private static void check(X509ExtendedTrustManager manager, boolean client, boolean engine, String hostname,
        String algorithm) throws CertificateException {
        check(manager, client, engine, hostname, algorithm, null);
    }

    private static void check(X509ExtendedTrustManager manager, boolean client, boolean engine, String hostname,
        String algorithm, AlgorithmConstraints constraints) throws CertificateException {
        check(manager, client, engine, hostname, algorithm, constraints,
            new X509Certificate[] { leafCertificate, rootCertificate });
    }

    private static void check(X509ExtendedTrustManager manager, boolean client, boolean engine, String hostname,
        String algorithm, AlgorithmConstraints constraints, X509Certificate[] chain) throws CertificateException {
        ExtendedSSLSession session = mock(ExtendedSSLSession.class);
        when(session.getPeerHost()).thenReturn(hostname);
        when(session.getProtocol()).thenReturn("TLSv1.2");
        when(session.getLocalSupportedSignatureAlgorithms()).thenReturn(new String[] { "SHA256withRSA" });
        when(session.getRequestedServerNames()).thenReturn(Collections.emptyList());
        SSLParameters parameters = new SSLParameters();
        parameters.setEndpointIdentificationAlgorithm(algorithm);
        parameters.setAlgorithmConstraints(constraints);
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
                    SSLHandshakeException failure
                        = assertThrows(SSLHandshakeException.class, connection::startHandshake);
                    assertTrue(hasCertificateCause(failure));
                }
            }
            IOException serverFailure = serverResult.get(10, TimeUnit.SECONDS);
            if (accepted) {
                assertNull(serverFailure);
            } else {
                // Java 8 can wrap the peer's rejection or disconnect in a generic SSLException.
                assertTrue(serverFailure instanceof SSLException || serverFailure instanceof SocketException,
                    () -> "Expected a TLS failure or connection reset, got: " + serverFailure);
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
        return certificate(subject, subjectName, issuer, issuerName, ca, serial, true);
    }

    private static X509Certificate certificate(KeyPair subject, String subjectName, KeyPair issuer, String issuerName,
        boolean ca, int serial, boolean includeSubjectAlternativeNames) throws Exception {
        Instant now = Instant.now();
        JcaX509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(new X500Name(issuerName),
            BigInteger.valueOf(serial), Date.from(now.minusSeconds(3600)), Date.from(now.plusSeconds(86400)),
            new X500Name(subjectName), subject.getPublic());
        builder.addExtension(Extension.basicConstraints, true, new BasicConstraints(ca));
        builder.addExtension(Extension.keyUsage, true,
            new KeyUsage(ca ? KeyUsage.keyCertSign : KeyUsage.digitalSignature | KeyUsage.keyEncipherment));
        if (!ca) {
            if (includeSubjectAlternativeNames) {
                builder.addExtension(Extension.subjectAlternativeName, false,
                    new GeneralNames(new GeneralName[] {
                        new GeneralName(GeneralName.dNSName, MATCHING_HOST),
                        new GeneralName(GeneralName.dNSName, "*.cluster.example"),
                        new GeneralName(GeneralName.iPAddress, "127.0.0.1"),
                        new GeneralName(GeneralName.iPAddress, "::1"),
                        new GeneralName(GeneralName.dNSName, "xn--bcher-kva.example") }));
            }
            builder.addExtension(Extension.extendedKeyUsage, false, new ExtendedKeyUsage(
                new KeyPurposeId[] { KeyPurposeId.id_kp_serverAuth, KeyPurposeId.id_kp_clientAuth }));
        }
        return new JcaX509CertificateConverter()
            .getCertificate(builder.build(new JcaContentSignerBuilder("SHA256withRSA").build(issuer.getPrivate())));
    }
}
