// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.util.AuthorizationChallengeHandler;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.UrlBuilder;
import io.netty.handler.codec.http.HttpHeaders;
import reactor.core.publisher.Mono;
import reactor.netty.ConnectionObserver;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

import java.io.Closeable;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.SocketAddress;
import java.net.URL;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.azure.core.util.AuthorizationChallengeHandler.PROXY_AUTHENTICATE;

/**
 * This class represents a mock proxy server for testing.
 */
public final class MockProxyServer implements Closeable {
    private static final HttpClient FORWARDING_CLIENT = new NettyAsyncHttpClientBuilder().build();

    private final boolean requiresAuthentication;
    private final String expectedAuthenticationValue;
    private final DisposableServer disposableServer;

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

        this.expectedAuthenticationValue = this.requiresAuthentication
            ? new AuthorizationChallengeHandler(username, password).handleBasic()
            : null;

        this.disposableServer = HttpServer.create()
            .host("localhost")
            .observe((connection, newState) -> {
                if (newState == ConnectionObserver.State.RELEASED) {
                    isAuthenticated.remove(connection.channel().remoteAddress());
                }
            })
            .handle((request, response) -> {
                if (requiresAuthentication) {
                    if (isAuthenticated.contains(request.remoteAddress())) {
                        return forwardProxiedRequest(request, response);
                    } else if (hasRequiredAuthentication(request.requestHeaders())) {
                        isAuthenticated.add(request.remoteAddress());
                        return response.status(200).send();
                    } else {
                        return response.status(407)
                            .header(PROXY_AUTHENTICATE, "basic")
                            .send();
                    }
                } else {
                    return response.status(200).send();
                }
            })
            .bindNow();
    }

    /**
     * Gets the address of the server.
     *
     * @return Address of the server.
     */
    public InetSocketAddress socketAddress() {
        return (InetSocketAddress) disposableServer.address();
    }

    @Override
    public void close() {
        disposableServer.disposeNow();
    }

    private boolean hasRequiredAuthentication(HttpHeaders requestHeaders) {
        String proxyAuthenticationHeader = requestHeaders.get(AuthorizationChallengeHandler.PROXY_AUTHORIZATION);
        if (CoreUtils.isNullOrEmpty(proxyAuthenticationHeader)) {
            return false;
        }

        return expectedAuthenticationValue.equals(proxyAuthenticationHeader);
    }

    private Mono<Void> forwardProxiedRequest(HttpServerRequest request, HttpServerResponse response) {
        HttpMethod httpMethod = HttpMethod.valueOf(request.method().name());
        URL url;
        try {
            url = new UrlBuilder().setScheme("http")
                .setHost(request.requestHeaders().get("host"))
                .setPath(request.fullPath())
                .toUrl();
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }
        com.azure.core.http.HttpHeaders headers = new NettyToAzureCoreHttpHeadersWrapper(request.requestHeaders());

        return FORWARDING_CLIENT.send(new HttpRequest(httpMethod, url, headers, request.receive().asByteBuffer()))
            .flatMap(res -> response.status(res.getStatusCode())
                .sendByteArray(res.getBodyAsByteArray())
                .then());
    }
}
