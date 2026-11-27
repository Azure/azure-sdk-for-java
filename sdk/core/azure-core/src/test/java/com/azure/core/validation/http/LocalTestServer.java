// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.validation.http;

import com.azure.core.http.HttpProtocolVersion;
import org.conscrypt.OpenSSLProvider;
import org.eclipse.jetty.alpn.server.ALPNServerConnectionFactory;
import org.eclipse.jetty.http2.server.HTTP2ServerConnectionFactory;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.ExecutorThreadPool;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Locale;
import java.util.Objects;

/**
 * Local server that will reply to requests based on the configured {@link HttpServlet}.
 */
public class LocalTestServer {
    private final Server server;
    private final ServerConnector httpConnector;
    private final ServerConnector httpsConnector;

    /**
     * Creates a new instance of {@link LocalTestServer} that will reply to requests based on the passed
     * RequestHandler.
     *
     * @param requestHandler The request handler that will be used to process requests.
     * @throws RuntimeException If the server cannot configure SSL.
     */
    public LocalTestServer(RequestHandler requestHandler) {
        this(requestHandler, 10);
    }

    /**
     * Creates a new instance of {@link LocalTestServer} that will reply to requests based on the passed
     * RequestHandler.
     *
     * @param requestHandler The request handler that will be used to process requests.
     * @param maxThreads The maximum number of threads that the server will use to process requests.
     * @throws RuntimeException If the server cannot configure SSL.
     */
    public LocalTestServer(RequestHandler requestHandler, int maxThreads) {
        this(requestHandler, maxThreads, HttpProtocolVersion.HTTP_1_1, createSslContextFactory("/keystore.jks"));
    }

    /**
     * Creates a local server with an HTTP/1.1 connector and a configurable HTTPS connector.
     *
     * @param requestHandler The request handler that will process requests.
     * @param maxThreads The maximum number of server threads.
     * @param maximumHttpVersion The maximum HTTPS protocol version. HTTP/2 also permits HTTP/1.1.
     * @throws RuntimeException If the server cannot configure SSL.
     */
    public LocalTestServer(RequestHandler requestHandler, int maxThreads, HttpProtocolVersion maximumHttpVersion) {
        this(requestHandler, maxThreads, maximumHttpVersion, createSslContextFactory("/http-protocol-keystore.jks"));
    }

    /**
     * Creates a local server for mutual TLS (mTLS) tests. Its HTTPS connector accepts TLS 1.2 and TLS 1.3 and only
     * HTTP/1.1 and, when asked to, requests a certificate from clients during the TLS handshake, accepting any
     * certificate a client presents. Clients can still connect without presenting one.
     *
     * @param requestHandler The request handler that will process requests. It can get the certificate a client
     * presented with {@link #getClientCertificate(HttpServletRequest)}.
     * @param requestClientCertificate Whether the HTTPS connector requests a certificate from clients.
     * @return The server, which must be started before it processes requests.
     * @throws RuntimeException If the server cannot configure SSL.
     */
    public static LocalTestServer createMutualTlsServer(RequestHandler requestHandler,
        boolean requestClientCertificate) {
        return createMutualTlsServer(requestHandler, requestClientCertificate, HttpProtocolVersion.HTTP_1_1);
    }

    /**
     * Creates a local server for mutual TLS (mTLS) tests. Its HTTPS connector accepts TLS 1.2 and TLS 1.3 and, when
     * asked to, requests a certificate from clients during the TLS handshake, accepting any certificate a client
     * presents. Clients can still connect without presenting one.
     *
     * @param requestHandler The request handler that will process requests. It can get the certificate a client
     * presented with {@link #getClientCertificate(HttpServletRequest)}.
     * @param requestClientCertificate Whether the HTTPS connector requests a certificate from clients.
     * @param maximumHttpVersion The maximum HTTPS protocol version. HTTP/2 also permits HTTP/1.1.
     * @return The server, which must be started before it processes requests.
     * @throws RuntimeException If the server cannot configure SSL.
     */
    public static LocalTestServer createMutualTlsServer(RequestHandler requestHandler, boolean requestClientCertificate,
        HttpProtocolVersion maximumHttpVersion) {
        return createMutualTlsServer(requestHandler, requestClientCertificate, maximumHttpVersion, "TLSv1.3",
            "TLSv1.2");
    }

    /**
     * Creates a local server for mutual TLS (mTLS) tests whose HTTPS connector only accepts the given TLS protocols, for
     * example to test TLS 1.2 and TLS 1.3 separately. When asked to, the HTTPS connector requests a certificate from
     * clients during the TLS handshake, accepting any certificate a client presents. Clients can still connect without
     * presenting one.
     *
     * @param requestHandler The request handler that will process requests. It can get the certificate a client
     * presented with {@link #getClientCertificate(HttpServletRequest)}, and the TLS protocol of the connection with
     * {@link #getTlsProtocol(HttpServletRequest)}.
     * @param requestClientCertificate Whether the HTTPS connector requests a certificate from clients.
     * @param maximumHttpVersion The maximum HTTPS protocol version. HTTP/2 also permits HTTP/1.1.
     * @param tlsProtocols The TLS protocols the HTTPS connector accepts, such as {@code "TLSv1.2"} or
     * {@code "TLSv1.3"}.
     * @return The server, which must be started before it processes requests.
     * @throws IllegalArgumentException If no TLS protocol is given.
     * @throws RuntimeException If the server cannot configure SSL.
     */
    public static LocalTestServer createMutualTlsServer(RequestHandler requestHandler, boolean requestClientCertificate,
        HttpProtocolVersion maximumHttpVersion, String... tlsProtocols) {
        if (tlsProtocols == null || tlsProtocols.length == 0) {
            throw new IllegalArgumentException("At least one TLS protocol is required.");
        }

        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setSslContext(maximumHttpVersion == HttpProtocolVersion.HTTP_2
            ? Http2ConnectorFactory.createMutualTlsSslContext()
            : createMutualTlsSslContext(null));
        sslContextFactory.setIncludeProtocols(tlsProtocols);
        sslContextFactory.setWantClientAuth(requestClientCertificate);

        return new LocalTestServer(requestHandler, 10, maximumHttpVersion, sslContextFactory);
    }

    /**
     * Gets the certificate the client presented on the TLS connection a request came on. When the connection resumed
     * a TLS session, the client doesn't present its certificate again, and this is the certificate it presented when
     * the session was created.
     *
     * @param request The request.
     * @return The certificate the client presented, or null if it didn't present one.
     */
    public static X509Certificate getClientCertificate(HttpServletRequest request) {
        X509Certificate[] certificates
            = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
        return certificates == null || certificates.length == 0 ? null : certificates[0];
    }

    /**
     * Gets the TLS session of the connection a request came on.
     *
     * @param request The request.
     * @return The TLS session, or null if the request didn't come over TLS.
     */
    public static SSLSession getTlsSession(HttpServletRequest request) {
        Object session = request.getAttribute("org.eclipse.jetty.servlet.request.ssl_session");
        return session instanceof SSLSession ? (SSLSession) session : null;
    }

    /**
     * Gets the TLS protocol of the connection a request came on.
     *
     * @param request The request.
     * @return The TLS protocol, such as {@code "TLSv1.3"}, or null if the request didn't come over TLS.
     */
    public static String getTlsProtocol(HttpServletRequest request) {
        SSLSession session = getTlsSession(request);
        return session == null ? null : session.getProtocol();
    }

    private LocalTestServer(RequestHandler requestHandler, int maxThreads, HttpProtocolVersion maximumHttpVersion,
        SslContextFactory.Server sslContextFactory) {
        this.server = new Server(new ExecutorThreadPool(maxThreads));

        HttpConfiguration httpConfiguration = new HttpConfiguration();
        httpConfiguration.setResponseHeaderSize(16 * 1024);
        HttpConnectionFactory httpConnectionFactory = new HttpConnectionFactory(httpConfiguration);
        this.httpConnector = new ServerConnector(server, httpConnectionFactory);
        this.httpConnector.setHost("localhost");

        server.addConnector(this.httpConnector);

        HttpConfiguration httpsConfiguration = new HttpConfiguration(httpConfiguration);
        httpsConfiguration.addCustomizer(new SecureRequestCustomizer());

        if (maximumHttpVersion == HttpProtocolVersion.HTTP_2) {
            this.httpsConnector = Http2ConnectorFactory.create(server, sslContextFactory, httpsConfiguration);
        } else {
            SslConnectionFactory sslConnectionFactory
                = new SslConnectionFactory(sslContextFactory, httpConnectionFactory.getProtocol());
            this.httpsConnector
                = new ServerConnector(server, sslConnectionFactory, new HttpConnectionFactory(httpsConfiguration));
        }
        this.httpsConnector.setHost("localhost");

        server.addConnector(this.httpsConnector);

        ServletContextHandler servletContextHandler = new ServletContextHandler();
        servletContextHandler.setContextPath("/");
        server.setHandler(servletContextHandler);

        ServletHolder servletHolder = new ServletHolder(new AzureTestHttpServlet(requestHandler));
        servletContextHandler.addServlet(servletHolder, "/");
    }

    private static SslContextFactory.Server createSslContextFactory(String keystoreResource) {
        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
        String mockKeyStore = Objects.toString(LocalTestServer.class.getResource(keystoreResource), null);
        sslContextFactory.setKeyStorePath(mockKeyStore);
        sslContextFactory.setKeyStorePassword("password");
        sslContextFactory.setKeyManagerPassword("password");
        sslContextFactory.setKeyStorePath(mockKeyStore);
        sslContextFactory.setTrustStorePassword("password");
        sslContextFactory.setTrustAll(true);
        return sslContextFactory;
    }

    // Creates the server's SSL context for mTLS tests, from the given security provider or, if it's null, the default
    // one.
    private static SSLContext createMutualTlsSslContext(String provider) {
        try (InputStream stream = LocalTestServer.class.getResourceAsStream("/keystore.jks")) {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(stream, "password".toCharArray());
            KeyManagerFactory keyManagerFactory
                = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, "password".toCharArray());

            SSLContext sslContext
                = provider == null ? SSLContext.getInstance("TLS") : SSLContext.getInstance("TLS", provider);
            sslContext.init(keyManagerFactory.getKeyManagers(),
                new TrustManager[] { new AcceptAllClientCertificatesTrustManager() }, null);
            return sslContext;
        } catch (IOException | GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    // Accepts any certificate a client presents, so that tests can use self-signed client certificates.
    private static final class AcceptAllClientCertificatesTrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            throw new CertificateException("The test server doesn't trust server certificates.");
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    // Load HTTP/2-only dependencies only when the new server mode is requested by a test-JAR consumer.
    private static final class Http2ConnectorFactory {
        // Jetty negotiates HTTP/2 through Conscrypt, so the mTLS server's SSL context must come from it too.
        private static SSLContext createMutualTlsSslContext() {
            Security.addProvider(new OpenSSLProvider());
            return LocalTestServer.createMutualTlsSslContext("Conscrypt");
        }

        private static ServerConnector create(Server server, SslContextFactory.Server sslContextFactory,
            HttpConfiguration configuration) {
            Security.addProvider(new OpenSSLProvider());
            sslContextFactory.setProvider("Conscrypt");

            HttpConnectionFactory http1 = new HttpConnectionFactory(configuration);
            HTTP2ServerConnectionFactory http2 = new HTTP2ServerConnectionFactory(configuration);
            String http1AlpnProtocol = http1.getProtocol().toLowerCase(Locale.ROOT);
            ALPNServerConnectionFactory alpn = new ALPNServerConnectionFactory(http2.getProtocol(), http1AlpnProtocol);
            alpn.setDefaultProtocol(http1AlpnProtocol);

            return new ServerConnector(server, new SslConnectionFactory(sslContextFactory, alpn.getProtocol()), alpn,
                http2, http1);
        }
    }

    private static final class AzureTestHttpServlet extends HttpServlet {
        private final RequestHandler requestHandler;

        private AzureTestHttpServlet(RequestHandler requestHandler) {
            this.requestHandler = requestHandler;
        }

        @Override
        protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            byte[] requestBody = fullyReadRequest(req.getInputStream());
            requestHandler.handle((Request) req, (Response) resp, requestBody);
        }
    }

    /**
     * Starts the local server.
     * <p>
     * This must be called before any requests will be processed.
     *
     * @throws RuntimeException If the server fails to start.
     */
    public void start() {
        try {
            server.start();
            while (!hasServerStarted(server)) {
                Thread.sleep(1000); // Wait until the server has actually started.
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean hasServerStarted(Server server) {
        String serverState = server.getState();

        if (serverState.equals(AbstractLifeCycle.FAILED)
            || serverState.equals(AbstractLifeCycle.STOPPING)
            || serverState.equals(AbstractLifeCycle.STOPPED)) {
            throw new RuntimeException(
                "Server state has reached an unexpected state while waiting for it to start: " + serverState);
        }

        return serverState.equals(AbstractLifeCycle.STARTED) || serverState.equals(AbstractLifeCycle.RUNNING);
    }

    /**
     * Stops the local server.
     * <p>
     * This must be called to close any server resources.
     *
     * @throws RuntimeException If the server fails to stop.
     */
    public void stop() {
        try {
            if (server.isRunning()) {
                server.stop();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the HTTP port that the local server is listening on.
     *
     * @return The HTTP port that the local server is listening on.
     */
    public int getHttpPort() {
        return httpConnector.getLocalPort();
    }

    /**
     * Gets the HTTPS port that the local server is listening on.
     *
     * @return The HTTPS port that the local server is listening on.
     */
    public int getHttpsPort() {
        return httpsConnector.getLocalPort();
    }

    /**
     * Gets the HTTP URI that the local server is listening on.
     *
     * @return The HTTP URI that the local server is listening on.
     */
    public String getHttpUri() {
        return "http://localhost:" + getHttpPort();
    }

    /**
     * Gets the HTTPS URI that the local server is listening on.
     *
     * @return The HTTPS URI that the local server is listening on.
     */
    public String getHttpsUri() {
        server.getURI();
        return "https://localhost:" + getHttpsPort();
    }

    /**
     * Handler that will be used to process requests.
     */
    public interface RequestHandler {
        /**
         * Handles the request.
         *
         * @param req The request.
         * @param resp The response.
         * @param requestBody The request body.
         * @throws IOException If an IO error occurs.
         * @throws ServletException If a servlet error occurs.
         */
        void handle(Request req, Response resp, byte[] requestBody) throws IOException, ServletException;
    }

    private static byte[] fullyReadRequest(InputStream requestBody) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        HttpValidatonUtils.copy(requestBody, outputStream);
        return outputStream.toByteArray();
    }
}
