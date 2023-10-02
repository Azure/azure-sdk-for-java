// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.test.http;

import org.apache.commons.compress.utils.IOUtils;
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
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.ExecutorThreadPool;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

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
        this.server = new Server(new ExecutorThreadPool(maxThreads));

        HttpConnectionFactory httpConnectionFactory = new HttpConnectionFactory();
        this.httpConnector = new ServerConnector(server, httpConnectionFactory);
        this.httpConnector.setHost("localhost");

        server.addConnector(this.httpConnector);

        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
        String mockKeyStore = LocalTestServer.class.getResource("/keystore.jks").toString();
        sslContextFactory.setKeyStorePath(mockKeyStore);
        sslContextFactory.setKeyStorePassword("password");
        sslContextFactory.setKeyManagerPassword("password");
        sslContextFactory.setKeyStorePath(mockKeyStore);
        sslContextFactory.setTrustStorePassword("password");
        sslContextFactory.setTrustAll(true);
        SslConnectionFactory sslConnectionFactory = new SslConnectionFactory(sslContextFactory,
            httpConnectionFactory.getProtocol());

        HttpConfiguration httpConfiguration = new HttpConfiguration();
        httpConfiguration.addCustomizer(new SecureRequestCustomizer());

        this.httpsConnector = new ServerConnector(server, sslConnectionFactory,
            new HttpConnectionFactory(httpConfiguration));
        this.httpsConnector.setHost("localhost");

        server.addConnector(this.httpsConnector);

        ServletContextHandler servletContextHandler = new ServletContextHandler();
        servletContextHandler.setContextPath("/");
        server.setHandler(servletContextHandler);

        ServletHolder servletHolder = new ServletHolder(new HttpServlet() {
            @Override
            protected void service(HttpServletRequest req, HttpServletResponse resp)
                throws ServletException, IOException {
                byte[] requestBody = fullyReadRequest(req.getInputStream());
                requestHandler.handle((Request) req, (Response) resp, requestBody);
            }
        });
        servletContextHandler.addServlet(servletHolder, "/");
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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
        IOUtils.copy(requestBody, outputStream);
        return outputStream.toByteArray();
    }
}
