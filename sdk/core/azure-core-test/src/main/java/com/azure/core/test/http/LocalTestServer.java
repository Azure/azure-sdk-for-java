// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.http;

import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import javax.servlet.http.HttpServlet;

/**
 * Local server that will reply to requests based on the configured {@link HttpServlet}.
 */
public class LocalTestServer {
    private final Server server;
    private final ServerConnector httpConnector;
    private final ServerConnector httpsConnector;

    /**
     * Creates a new instance of {@link LocalTestServer} that will reply to requests based on the passed HTTP servlet.
     *
     * @param httpServlet The HTTP servlet that will reply to requests.
     * @throws RuntimeException If the server cannot configure SSL.
     */
    public LocalTestServer(HttpServlet httpServlet) {
        this.server = new Server(new QueuedThreadPool(50));

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

        ServletHolder servletHolder = new ServletHolder(httpServlet);
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
            server.stop();
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
}
