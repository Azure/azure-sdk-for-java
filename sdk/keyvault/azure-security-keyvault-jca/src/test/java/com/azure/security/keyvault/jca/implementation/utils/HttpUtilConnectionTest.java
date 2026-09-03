// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.utils;

import com.azure.security.keyvault.jca.implementation.JreKeyStoreFactory;
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
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.parallel.Isolated;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;

import static com.azure.security.keyvault.jca.implementation.utils.HttpUtil.ACCEPT_ENCODING_KEY;
import static com.azure.security.keyvault.jca.implementation.utils.HttpUtil.ACCEPT_ENCODING_VALUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Isolated("Mutates the JRE trust store and HTTPS proxy properties used by HttpUtil")
public class HttpUtilConnectionTest {
    private static final char[] KEY_PASSWORD = "changeit".toCharArray();

    @Test
    @Timeout(20)
    void realHttpsGetUsesJreTrustStoreAndHostnameVerification() throws Exception {
        KeyPair keyPair = generateKeyPair();
        X509Certificate certificate = createServerCertificate(keyPair, "localhost");
        KeyStore trustStore = JreKeyStoreFactory.getDefaultKeyStore();
        assertNotNull(trustStore);

        String trustAlias = "http-util-connection-test-" + UUID.randomUUID();
        trustStore.setCertificateEntry(trustAlias, certificate);

        try (LocalHttpsServer server = new LocalHttpsServer(keyPair.getPrivate(), certificate, "response")) {
            server.start();

            String result = HttpUtil.get("https://localhost:" + server.getPort() + "/test", null);

            server.awaitCompletion();
            assertEquals("response", result);
            assertEquals("GET /test HTTP/1.1", server.getRequestLine());
            assertEquals(ACCEPT_ENCODING_VALUE, server.getRequestHeaders().get(ACCEPT_ENCODING_KEY));
        } finally {
            trustStore.deleteEntry(trustAlias);
        }
    }

    @Test
    @Timeout(20)
    void realHttpsGetRejectsTrustedCertificateForDifferentHostname() throws Exception {
        KeyPair keyPair = generateKeyPair();
        X509Certificate certificate = createServerCertificate(keyPair, "different.example.test");
        KeyStore trustStore = JreKeyStoreFactory.getDefaultKeyStore();
        assertNotNull(trustStore);

        String trustAlias = "http-util-connection-test-" + UUID.randomUUID();
        trustStore.setCertificateEntry(trustAlias, certificate);

        try (LocalHttpsServer server = new LocalHttpsServer(keyPair.getPrivate(), certificate, "response")) {
            server.start();
            HttpURLConnection connection = HttpUtil.openConnection("https://localhost:" + server.getPort() + "/test");

            try {
                assertThrows(IOException.class, connection::getResponseCode);
            } finally {
                connection.disconnect();
            }

            assertNotNull(server.awaitFailure());
        } finally {
            trustStore.deleteEntry(trustAlias);
        }
    }

    @Test
    @Timeout(20)
    void realHttpsGetDecodesGzipResponse() throws Exception {
        KeyPair keyPair = generateKeyPair();
        X509Certificate certificate = createServerCertificate(keyPair, "localhost");
        KeyStore trustStore = JreKeyStoreFactory.getDefaultKeyStore();
        assertNotNull(trustStore);

        String trustAlias = "http-util-connection-test-" + UUID.randomUUID();
        trustStore.setCertificateEntry(trustAlias, certificate);
        Map<String, String> responseHeaders = new LinkedHashMap<>();
        responseHeaders.put("Content-Encoding", "gzip");

        try (LocalHttpsServer server
            = new LocalHttpsServer(keyPair.getPrivate(), certificate, gzip("response"), responseHeaders)) {
            server.start();

            String result = HttpUtil.get("https://localhost:" + server.getPort() + "/gzip", null);

            server.awaitCompletion();
            assertEquals("response", result);
            assertEquals(ACCEPT_ENCODING_VALUE, server.getRequestHeaders().get(ACCEPT_ENCODING_KEY));
        } finally {
            trustStore.deleteEntry(trustAlias);
        }
    }

    @Test
    @Timeout(20)
    void realHttpsGetUsesSystemProxyConnectTunnel() throws Exception {
        String originalProxyHost = System.getProperty("https.proxyHost");
        String originalProxyPort = System.getProperty("https.proxyPort");
        String originalNonProxyHosts = System.getProperty("http.nonProxyHosts");

        try (LocalConnectProxy proxy = new LocalConnectProxy()) {
            System.setProperty("https.proxyHost", InetAddress.getLoopbackAddress().getHostAddress());
            System.setProperty("https.proxyPort", String.valueOf(proxy.getPort()));
            System.setProperty("http.nonProxyHosts", "localhost|127.*|[::1]");
            proxy.start();

            String result = HttpUtil.get("https://proxy-target.example.test/resource", null);

            proxy.awaitCompletion();
            assertNull(result);
            assertEquals("CONNECT proxy-target.example.test:443 HTTP/1.1", proxy.getRequestLine());
        } finally {
            restoreSystemProperty("https.proxyHost", originalProxyHost);
            restoreSystemProperty("https.proxyPort", originalProxyPort);
            restoreSystemProperty("http.nonProxyHosts", originalNonProxyHosts);
        }
    }

    private static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        return keyPairGenerator.generateKeyPair();
    }

    private static X509Certificate createServerCertificate(KeyPair keyPair, String hostname) throws Exception {
        Date notBefore = new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1));
        Date notAfter = new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1));
        X500Name name = new X500Name("CN=" + hostname);
        JcaX509v3CertificateBuilder builder
            = new JcaX509v3CertificateBuilder(name, BigInteger.ONE, notBefore, notAfter, name, keyPair.getPublic());

        builder.addExtension(Extension.basicConstraints, true, new BasicConstraints(false));
        builder.addExtension(Extension.keyUsage, true,
            new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyEncipherment));
        builder.addExtension(Extension.extendedKeyUsage, false, new ExtendedKeyUsage(KeyPurposeId.id_kp_serverAuth));
        builder.addExtension(Extension.subjectAlternativeName, false,
            new GeneralNames(new GeneralName(GeneralName.dNSName, hostname)));

        ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA").build(keyPair.getPrivate());
        X509Certificate certificate = new JcaX509CertificateConverter().getCertificate(builder.build(signer));
        certificate.verify(keyPair.getPublic());
        return certificate;
    }

    private static byte[] gzip(String value) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipStream = new GZIPOutputStream(outputStream)) {
            gzipStream.write(value.getBytes(StandardCharsets.UTF_8));
        }
        return outputStream.toByteArray();
    }

    private static void restoreSystemProperty(String name, String value) {
        if (value == null) {
            System.clearProperty(name);
        } else {
            System.setProperty(name, value);
        }
    }

    private static final class LocalConnectProxy implements AutoCloseable {
        private final ServerSocket serverSocket;
        private final CountDownLatch completed = new CountDownLatch(1);
        private final Thread proxyThread;
        private volatile String requestLine;
        private volatile Throwable failure;

        private LocalConnectProxy() throws IOException {
            serverSocket = new ServerSocket(0, 1, InetAddress.getLoopbackAddress());
            proxyThread = new Thread(this::serve, "http-util-local-connect-proxy");
            proxyThread.setDaemon(true);
        }

        private void start() {
            proxyThread.start();
        }

        private int getPort() {
            return serverSocket.getLocalPort();
        }

        private String getRequestLine() {
            return requestLine;
        }

        private void serve() {
            try (Socket socket = serverSocket.accept()) {
                socket.setSoTimeout(10_000);
                BufferedReader reader
                    = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.ISO_8859_1));
                requestLine = reader.readLine();
                int headerCount = 0;
                String line;
                while ((line = reader.readLine()) != null && !line.isEmpty()) {
                    headerCount++;
                }
                if (headerCount == 0) {
                    throw new IOException("The CONNECT request did not contain any headers.");
                }

                OutputStream outputStream = socket.getOutputStream();
                outputStream.write("HTTP/1.1 502 Bad Gateway\r\nContent-Length: 0\r\nConnection: close\r\n\r\n"
                    .getBytes(StandardCharsets.ISO_8859_1));
                outputStream.flush();
            } catch (Throwable throwable) {
                failure = throwable;
            } finally {
                completed.countDown();
            }
        }

        private void awaitCompletion() throws Exception {
            assertTrue(completed.await(10, TimeUnit.SECONDS), "The local CONNECT proxy did not finish in time.");
            if (failure != null) {
                throw new AssertionError("The local CONNECT proxy failed.", failure);
            }
        }

        @Override
        public void close() throws IOException {
            serverSocket.close();
            try {
                proxyThread.join(10_000);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new IOException("Interrupted while stopping the local CONNECT proxy.", exception);
            }
        }
    }

    private static final class LocalHttpsServer implements AutoCloseable {
        private final SSLServerSocket serverSocket;
        private final byte[] responseBody;
        private final Map<String, String> responseHeaders;
        private final CountDownLatch completed = new CountDownLatch(1);
        private final Map<String, String> requestHeaders = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        private final Thread serverThread;
        private volatile String requestLine;
        private volatile Throwable failure;

        private LocalHttpsServer(PrivateKey privateKey, X509Certificate certificate, String responseBody)
            throws Exception {
            this(privateKey, certificate, responseBody.getBytes(StandardCharsets.UTF_8), new LinkedHashMap<>());
        }

        private LocalHttpsServer(PrivateKey privateKey, X509Certificate certificate, byte[] responseBody,
            Map<String, String> responseHeaders) throws Exception {
            KeyStore serverKeyStore = KeyStore.getInstance("PKCS12");
            serverKeyStore.load(null, null);
            serverKeyStore.setKeyEntry("server", privateKey, KEY_PASSWORD, new Certificate[] { certificate });

            KeyManagerFactory keyManagerFactory
                = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(serverKeyStore, KEY_PASSWORD);

            SSLContext serverContext = SSLContext.getInstance("TLS");
            serverContext.init(keyManagerFactory.getKeyManagers(), null, null);
            serverSocket = (SSLServerSocket) serverContext.getServerSocketFactory()
                .createServerSocket(0, 1, InetAddress.getLoopbackAddress());
            this.responseBody = responseBody;
            this.responseHeaders = responseHeaders;
            serverThread = new Thread(this::serve, "http-util-local-https-server");
            serverThread.setDaemon(true);
        }

        private void start() {
            serverThread.start();
        }

        private int getPort() {
            return serverSocket.getLocalPort();
        }

        private String getRequestLine() {
            return requestLine;
        }

        private Map<String, String> getRequestHeaders() {
            return requestHeaders;
        }

        private void serve() {
            try (SSLSocket socket = (SSLSocket) serverSocket.accept()) {
                socket.setSoTimeout(10_000);
                readRequest(socket);
                writeResponse(socket.getOutputStream());
            } catch (Throwable throwable) {
                failure = throwable;
            } finally {
                completed.countDown();
            }
        }

        private void readRequest(SSLSocket socket) throws IOException {
            BufferedReader reader
                = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.ISO_8859_1));
            requestLine = reader.readLine();
            if (requestLine == null) {
                throw new IOException("The TLS connection closed before an HTTP request was received.");
            }
            String line;
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                int separator = line.indexOf(':');
                if (separator > 0) {
                    requestHeaders.put(line.substring(0, separator), line.substring(separator + 1).trim());
                }
            }
        }

        private void writeResponse(OutputStream outputStream) throws IOException {
            ByteArrayOutputStream response = new ByteArrayOutputStream();
            response.write(
                "HTTP/1.1 200 OK\r\nContent-Type: text/plain; charset=UTF-8\r\n".getBytes(StandardCharsets.ISO_8859_1));
            for (Map.Entry<String, String> header : responseHeaders.entrySet()) {
                response
                    .write((header.getKey() + ": " + header.getValue() + "\r\n").getBytes(StandardCharsets.ISO_8859_1));
            }
            response.write(("Content-Length: " + responseBody.length + "\r\nConnection: close\r\n\r\n")
                .getBytes(StandardCharsets.ISO_8859_1));
            response.write(responseBody);
            outputStream.write(response.toByteArray());
            outputStream.flush();
        }

        private void awaitCompletion() throws Exception {
            assertTrue(completed.await(10, TimeUnit.SECONDS), "The local HTTPS server did not finish in time.");
            if (failure != null) {
                throw new AssertionError("The local HTTPS server failed.", failure);
            }
        }

        private Throwable awaitFailure() throws InterruptedException {
            assertTrue(completed.await(10, TimeUnit.SECONDS), "The local HTTPS server did not finish in time.");
            assertNotNull(failure);
            return failure;
        }

        @Override
        public void close() throws IOException {
            serverSocket.close();
            try {
                serverThread.join(10_000);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new IOException("Interrupted while stopping the local HTTPS server.", exception);
            }
        }
    }
}
