// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.util.UrlBuilder;
import com.azure.core.util.logging.ClientLogger;
import fi.iki.elonen.NanoHTTPD;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;

import java.io.IOException;

/**
 * A local HTTP server that listens to the authorization code response from Azure Active Directory.
 */
public final class AuthorizationCodeListener {
    private final ClientLogger logger = new ClientLogger(AuthorizationCodeListener.class);

    private NanoHTTPD httpServer;
    private MonoProcessor<String> authorizationCodeEmitter;

    private AuthorizationCodeListener(NanoHTTPD httpServer, MonoProcessor<String> authorizationCodeEmitter) {
        this.httpServer = httpServer;
        this.authorizationCodeEmitter = authorizationCodeEmitter;

        try {
            this.httpServer.start();
        } catch (IOException e) {
            logger.error(
                "Unable to start identity authorization code listener on port " + httpServer.getListeningPort(), e);
        }
    }

    /**
     * Starts the server asynchronously on a given port. "http://locahost:{port}" must be white-listed as a reply URL.
     * @param port the port to listen on
     * @return a Publisher emitting the listener instance
     */
    public static Mono<AuthorizationCodeListener> create(int port) {
        MonoProcessor<String> monoProcessor = MonoProcessor.create();

        return Mono.just(new NanoHTTPD(port) {
            @Override
            public Response serve(final IHTTPSession session) {
                String uriWithQueryParams = session.getUri() + "?" + session.getQueryParameterString();
                monoProcessor.onNext(getCodeFromUri(uriWithQueryParams));
                return newFixedLengthResponse("");
            }
        }).map(server -> new AuthorizationCodeListener(server, monoProcessor));
    }

    /**
     * Dispose the server
     * @return a Publisher signaling the completion
     */
    public Mono<Void> dispose() {
        return Mono.fromRunnable(() -> {
            httpServer.closeAllConnections();
            httpServer.stop();
        });
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
        for (String query : urlBuilder.getQuery().keySet()) {
            if ("code".equalsIgnoreCase(query)) {
                return urlBuilder.getQuery().get(query);
            }
        }
        return null;
    }
}
