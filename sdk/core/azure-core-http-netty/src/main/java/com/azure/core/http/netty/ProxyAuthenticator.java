// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty;

import com.azure.core.http.AuthorizationChallengeHandler;
import com.azure.core.util.CoreUtils;
import io.netty.handler.proxy.HttpProxyHandler;
import reactor.netty.Connection;
import reactor.netty.http.client.HttpClientRequest;
import reactor.netty.http.client.HttpClientResponse;

import javax.net.ssl.SSLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.azure.core.http.AuthorizationChallengeHandler.PROXY_AUTHENTICATE;
import static com.azure.core.http.AuthorizationChallengeHandler.PROXY_AUTHENTICATION_INFO;
import static com.azure.core.http.AuthorizationChallengeHandler.PROXY_AUTHORIZATION;

final class ProxyAuthenticator {
    private static final String AUTH_BASIC = "Basic";
    private static final String AUTH_DIGEST = "Digest";

    private final AuthorizationChallengeHandler challengeHandler;

    ProxyAuthenticator(String username, String password) {
        this.challengeHandler = new AuthorizationChallengeHandler(username, password);
    }

    void doOnRequestHandler(HttpClientRequest request, Connection connection) {
        String method = request.method().name();
        String uri = request.path();

        // TODO: Need to get the HTTP entity.
        String proxyAuthenticationHeader = challengeHandler.attemptToPipelineAuthorization(method, uri, null);
        if (proxyAuthenticationHeader != null) {
            request.requestHeaders().add(PROXY_AUTHORIZATION, proxyAuthenticationHeader);
        }
    }

    void doOnRequestErrorHandler(HttpClientRequest request, Throwable throwable) {
        if (!(throwable instanceof SSLException)) {
            return;
        }

        Throwable cause = throwable.getCause();
        if (!(cause instanceof HttpProxyHandler.HttpProxyConnectException)) {
            return;
        }

        HttpProxyHandler.HttpProxyConnectException proxyConnectException =
            (HttpProxyHandler.HttpProxyConnectException) cause;

        boolean hasBasicChallenge = false;
        List<Map<String, String>> digestChallenges = new ArrayList<>();
        for (String authenticationChallenge : proxyConnectException.headers().getAll(PROXY_AUTHENTICATE)) {
            String[] typeValuePair = authenticationChallenge.split(" ", 2);
            if (AUTH_BASIC.equalsIgnoreCase(typeValuePair[0])) {
                hasBasicChallenge = true;
            } else if (AUTH_DIGEST.equalsIgnoreCase(typeValuePair[0])) {
                digestChallenges.add(parseDigestChallenge(typeValuePair[1]));
            }
        }

        String proxyAuthorizationHeader = null;
        // Prefer digest challenges over basic.
        if (digestChallenges.size() > 0) {
            proxyAuthorizationHeader = challengeHandler.handleDigest(request.method().name(), request.path(),
                digestChallenges, null);
        }

        if (proxyAuthorizationHeader == null && hasBasicChallenge) {
            proxyAuthorizationHeader = challengeHandler.handleBasic();
        }

        if (!CoreUtils.isNullOrEmpty(proxyAuthorizationHeader)) {
            request.addHeader(PROXY_AUTHORIZATION, proxyAuthorizationHeader);
        }
    }

    void doOnResponseHandler(HttpClientResponse response, Connection connection) {
        String proxyAuthenticationInfoHeader = response.responseHeaders().get(PROXY_AUTHENTICATION_INFO);
        if (!CoreUtils.isNullOrEmpty(proxyAuthenticationInfoHeader)) {
            Map<String, String> headerPieces = AuthorizationChallengeHandler
                .parseChallengeHeader(proxyAuthenticationInfoHeader);

            challengeHandler.consumeAuthenticationInfoHeader(headerPieces);
        }
    }

    private static Map<String, String> parseDigestChallenge(String digestChallenge) {
        Map<String, String> challenge = new HashMap<>();
        for (String challengePiece : digestChallenge.split(",")) {
            String[] kvp = challengePiece.split("=", 2);
            if (kvp.length != 2) {
                continue;
            }

            challenge.put(kvp[0].trim(), kvp[1].trim().replace("\"", ""));
        }

        return challenge;
    }
}
