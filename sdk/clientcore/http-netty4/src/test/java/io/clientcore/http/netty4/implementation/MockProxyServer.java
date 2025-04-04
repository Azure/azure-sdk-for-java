// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.netty4.implementation;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.shared.LocalTestServer;
import io.clientcore.core.utils.CoreUtils;
import io.clientcore.core.utils.UriBuilder;
import io.clientcore.http.netty4.NettyHttpClientBuilder;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;

import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class represents a mock proxy server for testing.
 */
public final class MockProxyServer implements Closeable {
    private static final HttpClient FORWARDING_CLIENT = new NettyHttpClientBuilder().build();

    private final boolean requiresAuthentication;
    private final String expectedAuthenticationValue;
    private final LocalTestServer proxyServer;

    private final Set<SocketAddress> isAuthenticated = Collections.newSetFromMap(new ConcurrentHashMap<>());

    /**
     * Constructs a simple pass-through proxy server.
     */
    public MockProxyServer() {
        this(null, null);
    }

    /**
     * Constructs a proxy server that requires Basic authentication using the passed username and password.
     * <p>
     * If {@code username} or {@code password} is null or empty this is effectively the same as {@link
     * #MockProxyServer()}, where authentication isn't required.
     *
     * @param username Basic authentication username.
     * @param password Basic authentication password.
     */
    public MockProxyServer(String username, String password) {
        this.requiresAuthentication = !CoreUtils.isNullOrEmpty(username);

        this.expectedAuthenticationValue
            = this.requiresAuthentication ? basicAuthenticationValue(username, password) : null;

        this.proxyServer = new LocalTestServer((req, resp, requestBody) -> {
            if (requiresAuthentication) {
                if (isAuthenticated.contains(req.getRemoteInetSocketAddress())) {
                    forwardProxiedRequest(req, resp);
                } else if (hasRequiredAuthentication(req)) {
                    isAuthenticated.add(req.getRemoteInetSocketAddress());
                    resp.setStatus(200);
                    resp.flushBuffer();
                } else {
                    resp.setStatus(407);
                    resp.setHeader(HttpHeaderName.PROXY_AUTHENTICATE.getCaseSensitiveName(), "basic");
                    resp.flushBuffer();
                }
            } else {
                resp.setStatus(200);
                resp.flushBuffer();
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
        return new InetSocketAddress("localhost", proxyServer.getHttpPort());
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

    private void forwardProxiedRequest(Request req, Response resp) {
        HttpMethod httpMethod = HttpMethod.valueOf(req.getMethod());
        URI uri;
        try {
            uri = new UriBuilder().setScheme("http").setHost(req.getHeader("host")).setPath(req.getPathInfo()).toUri();
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }

        try (io.clientcore.core.http.models.Response<BinaryData> response2
            = FORWARDING_CLIENT.send(new HttpRequest().setMethod(httpMethod)
                .setUri(uri)
                .setHeaders(convertFromRequest(req))
                .setBody(BinaryData.fromStream(req.getInputStream())))) {
            resp.setStatus(response2.getStatusCode());
            byte[] body = response2.getValue().toBytes();
            resp.setContentLength(body.length);
            resp.setContentType("application/octet-stream");
            resp.getHttpOutput().write(body);
            resp.flushBuffer();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private static HttpHeaders convertFromRequest(Request req) {
        HttpHeaders headers = new HttpHeaders();

        Enumeration<String> headerNames = req.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            HttpHeaderName httpHeaderName = HttpHeaderName.fromString(headerName);

            Enumeration<String> headerValues = req.getHeaders(headerName);
            while (headerValues.hasMoreElements()) {
                headers.add(httpHeaderName, headerValues.nextElement());
            }
        }

        return headers;
    }
}
