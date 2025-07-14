// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.shared;

import io.clientcore.core.http.client.HttpProtocolVersion;
import org.conscrypt.OpenSSLProvider;
import org.eclipse.jetty.alpn.server.ALPNServerConnectionFactory;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.http2.server.HTTP2ServerConnectionFactory;
import org.eclipse.jetty.server.ConnectionFactory;
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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Local server that will reply to requests based on the configured {@link HttpServlet}.
 */
public class LocalTestServer {
    private final Server server;
    private final ServerConnector connector;

    /**
     * Creates a new instance of {@link LocalTestServer} that will reply to requests based on the passed
     * RequestHandler.
     *
     * @param supportedProtocol The protocol supported by this server. If null, {@link HttpProtocolVersion#HTTP_1_1}
     * will be the supported protocol.
     * @param includeTls Flag indicating if TLS will be included.
     * @param requestHandler The request handler that will be used to process requests.
     * @throws RuntimeException If the server cannot configure SSL.
     */
    public LocalTestServer(HttpProtocolVersion supportedProtocol, boolean includeTls, RequestHandler requestHandler) {
        this.server = new Server(new ExecutorThreadPool());

        HttpConfiguration httpConfig = new HttpConfiguration();
        httpConfig.setResponseHeaderSize(16 * 1024);
        if (includeTls) {
            httpConfig.addCustomizer(new SecureRequestCustomizer());
        }

        List<ConnectionFactory> connectionFactories = new ArrayList<>();

        // SSL/TLS connection factory
        if (includeTls) {
            String nextProtocol = supportedProtocol == null
                ? HttpVersion.HTTP_1_1.asString()
                : (supportedProtocol == HttpProtocolVersion.HTTP_1_1) ? HttpVersion.HTTP_1_1.asString() : "alpn";

            Security.addProvider(new OpenSSLProvider());
            SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
            sslContextFactory.setProvider("Conscrypt");
            String mockKeyStore = Objects.toString(LocalTestServer.class.getResource("/keystore.jks"), null);
            sslContextFactory.setKeyStorePath(mockKeyStore);
            sslContextFactory.setKeyStorePassword("password");
            sslContextFactory.setKeyManagerPassword("password");
            sslContextFactory.setKeyStorePath(mockKeyStore);
            sslContextFactory.setTrustStorePassword("password");
            sslContextFactory.setTrustAll(true);

            connectionFactories.add(new SslConnectionFactory(sslContextFactory, nextProtocol));
        }

        if (supportedProtocol == HttpProtocolVersion.HTTP_2) {
            // ALPN connection factory
            // HTTP/2 connection factory
            ALPNServerConnectionFactory alpn = new ALPNServerConnectionFactory();
            alpn.setDefaultProtocol("h2");
            connectionFactories.add(alpn);
            connectionFactories.add(new HTTP2ServerConnectionFactory(httpConfig));
        }

        if (supportedProtocol == null || supportedProtocol == HttpProtocolVersion.HTTP_1_1) {
            // HTTP/1.1 connection factory
            connectionFactories.add(new HttpConnectionFactory(httpConfig));
        }

        connector = new ServerConnector(server, connectionFactories.toArray(new ConnectionFactory[0]));
        connector.setHost("localhost");
        server.addConnector(connector);

        ServletContextHandler servletContextHandler = new ServletContextHandler();
        servletContextHandler.setContextPath("/");
        server.setHandler(servletContextHandler);

        ServletHolder servletHolder = new ServletHolder(new CoreTestHttpServlet(requestHandler));
        servletContextHandler.addServlet(servletHolder, "/");
    }

    private static final class CoreTestHttpServlet extends HttpServlet {
        private final RequestHandler requestHandler;

        private CoreTestHttpServlet(RequestHandler requestHandler) {
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
     * Gets the port that the local server is listening on.
     *
     * @return The port that the local server is listening on.
     */
    public int getPort() {
        return connector.getLocalPort();
    }

    /**
     * Gets the HTTP URI that the local server is listening on.
     *
     * @return The HTTP URI that the local server is listening on.
     */
    public String getUri() {
        return "http://localhost:" + getPort();
    }

    /**
     * Gets the HTTPS URI that the local server is listening on.
     *
     * @return The HTTPS URI that the local server is listening on.
     */
    public String getHttpsUri() {
        return "https://localhost:" + getPort();
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
        HttpClientTests.inputStreamToOutputStream(requestBody, outputStream);
        return outputStream.toByteArray();
    }
}
