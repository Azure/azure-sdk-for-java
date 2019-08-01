// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.implementation.http.UrlBuilder;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

/**
 * A local HTTP server that listens to the authorization code response from Azure Active Directory.
 */
public final class AuthorizationCodeListener {
    private DisposableServer server;
    private MonoProcessor<String> authorizationCodeEmitter;

    private AuthorizationCodeListener(DisposableServer server, MonoProcessor<String> authorizationCodeEmitter) {
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
        return HttpServer.create()
            .port(port)
            .handle((inbound, outbound) -> {
                monoProcessor.onNext(getCodeFromUri(inbound.uri()));
                return inbound.receive().then();
            })
            .bind()
            .map(server -> new AuthorizationCodeListener(server, monoProcessor));
    }

    /**
     * Dispose the server
     * @return a Publisher signaling the completion
     */
    public Mono<Void> dispose() {
        return Mono.fromRunnable(() -> server.disposeNow());
    }

    /**
     * Listen for the next authorization code
     * @return a Publisher emitting an authorization code
     */
    public Mono<String> listen() {
        return authorizationCodeEmitter;
    }

    private static String getCodeFromUri(String uri) {
        UrlBuilder urlBuilder = UrlBuilder.parse(uri);
        for (String query : urlBuilder.query().keySet()) {
            if ("code".equalsIgnoreCase(query)) {
                return urlBuilder.query().get(query);
            }
        }
        return null;
    }
}
