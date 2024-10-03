// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.client;

import io.clientcore.core.shared.LocalTestServer;

import javax.servlet.ServletException;
import java.util.Base64;
import java.util.Objects;

/**
 * A simple HTTP proxy server that enforce basic proxy authentication, once authenticated any request matching
 * {@code serviceEndpoints} will be responded with an empty HTTP 200.
 */
public final class SimpleBasicAuthHttpProxyServer {
    private final String userName;
    private final String password;
    private final String serviceEndpoint;
    private LocalTestServer proxyServer;

    /**
     * Creates SimpleBasicAuthHttpProxyServer.
     *
     * @param userName the proxy username for basic authentication
     * @param password the proxy password for basic authentication
     * @param serviceEndpoint the mock endpoint targeting the service behind proxy
     */
    SimpleBasicAuthHttpProxyServer(String userName, String password, String serviceEndpoint) {
        this.userName = userName;
        this.password = password;
        this.serviceEndpoint = serviceEndpoint;
    }

    public ProxyEndpoint start() {
        this.proxyServer = new LocalTestServer((req, resp, requestBody) -> {
            String requestUri = req.getServletPath();

            if (!Objects.equals(requestUri, serviceEndpoint)) {
                throw new ServletException("Unexpected request to proxy server");
            }

            String proxyAuthorization = req.getHeader("Proxy-Authorization");

            if (proxyAuthorization == null) {
                resp.setStatus(407);
                resp.setHeader("Proxy-Authenticate", "Basic");

                return;
            }

            if (!proxyAuthorization.startsWith("Basic")) {
                resp.setStatus(401);

                return;
            }

            String encodedCred = proxyAuthorization.substring("Basic".length());
            encodedCred = encodedCred.trim();
            final Base64.Decoder decoder = Base64.getDecoder();
            final byte[] decodedCred = decoder.decode(encodedCred);

            if (!new String(decodedCred).equals(userName + ":" + password)) {
                resp.setStatus(401);
            }
        });

        this.proxyServer.start();

        return new ProxyEndpoint("localhost", this.proxyServer.getHttpPort());
    }

    public void shutdown() {
        if (this.proxyServer != null) {
            this.proxyServer.stop();
        }
    }

    public static final class ProxyEndpoint {
        private final String host;
        private final int port;

        ProxyEndpoint(String host, int port) {
            this.host = host;
            this.port = port;
        }

        String getHost() {
            return this.host;
        }

        int getPort() {
            return this.port;
        }
    }
}
