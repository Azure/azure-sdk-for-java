// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.netty4.implementation;

import io.clientcore.core.http.client.HttpProtocolVersion;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.shared.LocalTestServer;
import io.clientcore.core.utils.CoreUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class represents a mock proxy server for testing.
 */
public final class MockProxyServer implements Closeable {
    private final boolean requiresAuthentication;
    private final String expectedAuthenticationValue;
    private final LocalTestServer proxyServer;

    private final Set<SocketAddress> isAuthenticated = Collections.newSetFromMap(new ConcurrentHashMap<>());

    /**
     * Constructs a proxy server that requires Basic authentication using the passed username and password.
     * <p>
     * If {@code username} or {@code password} is null or empty this is effectively the same as authentication not being
     * required.
     *
     * @param username Basic authentication username.
     * @param password Basic authentication password.
     */
    public MockProxyServer(String username, String password) {
        this.requiresAuthentication = !CoreUtils.isNullOrEmpty(username);

        this.expectedAuthenticationValue
            = this.requiresAuthentication ? basicAuthenticationValue(username, password) : null;

        this.proxyServer = new LocalTestServer(HttpProtocolVersion.HTTP_1_1, false, (req, resp, requestBody) -> {
            if ("CONNECT".equalsIgnoreCase(req.getMethod())) {
                if (requiresAuthentication) {
                    if (hasRequiredAuthentication(req)) {
                        isAuthenticated.add(req.getRemoteInetSocketAddress());
                        resp.setStatus(200);
                    } else {
                        resp.setStatus(407);
                        resp.setHeader(HttpHeaderName.PROXY_AUTHENTICATE.getCaseSensitiveName(), "basic");
                    }
                } else {
                    resp.setStatus(200);
                }
            } else if (requiresAuthentication) {
                if (isAuthenticated.contains(req.getRemoteInetSocketAddress())) {
                    handleMockProxyForwardingResponse(resp);
                } else {
                    resp.setStatus(407);
                    resp.setHeader(HttpHeaderName.PROXY_AUTHENTICATE.getCaseSensitiveName(), "basic");
                }
            } else {
                handleMockProxyForwardingResponse(resp);
            }
        });
        this.proxyServer.start();
    }

    private static String basicAuthenticationValue(String username, String password) {
        String token = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Gets the address of the server.
     *
     * @return Address of the server.
     */
    public InetSocketAddress socketAddress() {
        return new InetSocketAddress("localhost", proxyServer.getPort());
    }

    @Override
    public void close() {
        proxyServer.stop();
    }

    private boolean hasRequiredAuthentication(Request request) {
        String proxyAuthenticationHeader
            = request.getHeader(HttpHeaderName.PROXY_AUTHORIZATION.getCaseInsensitiveName());
        if (CoreUtils.isNullOrEmpty(proxyAuthenticationHeader)) {
            return false;
        }

        return expectedAuthenticationValue.equals(proxyAuthenticationHeader);
    }

    private void handleMockProxyForwardingResponse(Response resp) throws IOException {
        resp.setStatus(418);
        resp.setHeader("Content-Type", "application/text");
        resp.setHeader("Content-Length", "12");
        resp.getHttpOutput().write("I'm a teapot".getBytes(StandardCharsets.UTF_8));
        resp.getHttpOutput().flush();
        resp.getHttpOutput().complete(Callback.NOOP);
    }
}
