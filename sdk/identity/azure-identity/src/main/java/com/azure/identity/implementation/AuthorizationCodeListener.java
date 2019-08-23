// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.implementation.http.UrlBuilder;
import com.sun.net.httpserver.HttpServer;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

/**
 * A local HTTP server that listens to the authorization code response from Azure Active Directory.
 */
public final class AuthorizationCodeListener {
    private HttpServer server;
    private MonoProcessor<String> authorizationCodeEmitter;

    private AuthorizationCodeListener(HttpServer server, MonoProcessor<String> authorizationCodeEmitter) {
        this.server = server;
        this.authorizationCodeEmitter = authorizationCodeEmitter;
    }

    /**
     * Starts the server asynchronously on a given port. "http://locahost:{port}" must be white-listed as a reply URL.
     * @param port the port to listen on
     * @return a Publisher emitting the listener instance
     */
    public static Mono<AuthorizationCodeListener> create(int port) {
        MonoProcessor<String> monoProcessor = MonoProcessor.create();
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/", httpExchange -> {
                String url = httpExchange.getRequestURI().toString();
                System.out.println(url);
                monoProcessor.onNext(getCodeFromUri(url));

                String response = "";
                httpExchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = httpExchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            });
            server.setExecutor(null);
            return Mono.just(new AuthorizationCodeListener(server, monoProcessor));
        } catch (IOException e) {
            return Mono.error(e);
        }
    }

    /**
     * Dispose the server
     * @return a Publisher signaling the completion
     */
    public Mono<Void> dispose() {
        return Mono.fromRunnable(() -> server.stop(0));
    }

    /**
     * Listen for the next authorization code
     * @return a Publisher emitting an authorization code
     */
    public Mono<String> listen() {
        server.start();
        return authorizationCodeEmitter;
    }

    private static String getCodeFromUri(String url) {
        UrlBuilder urlBuilder = UrlBuilder.parse(url);
        for (String query : urlBuilder.query().keySet()) {
            if ("code".equalsIgnoreCase(query)) {
                return urlBuilder.query().get(query);
            }
        }
        return null;
    }
}
