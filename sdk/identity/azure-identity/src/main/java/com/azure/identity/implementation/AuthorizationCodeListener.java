package com.azure.identity.implementation;

import com.azure.core.implementation.http.UrlBuilder;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

public class AuthorizationCodeListener {
    private DisposableServer server;
    private MonoProcessor<String> authorizationCodeEmitter;

    private AuthorizationCodeListener(DisposableServer server, MonoProcessor<String> authorizationCodeEmitter) {
        this.server = server;
        this.authorizationCodeEmitter = authorizationCodeEmitter;
    }

    public String getHost() {
        return server.host();
    }

    public int getPort() {
        return server.port();
    }

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

    public Mono<Void> dispose() {
        return server.onDispose();
    }

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
