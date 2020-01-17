// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.okhttp.implementation;

import com.azure.core.http.AuthorizationChallengeHandler;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import okhttp3.Authenticator;
import okhttp3.Challenge;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;
import okio.Buffer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.azure.core.http.AuthorizationChallengeHandler.PROXY_AUTHENTICATION_INFO;
import static com.azure.core.http.AuthorizationChallengeHandler.PROXY_AUTHORIZATION;

/**
 * This class handles authorizing requests being sent through a proxy which require authentication.
 */
public final class ProxyAuthenticator implements Authenticator {
    private static final String CNONCE = "cnonce";
    private static final String NC = "nc";

    private final ClientLogger logger = new ClientLogger(ProxyAuthenticator.class);

    private final AuthorizationChallengeHandler challengeHandler;

    /**
     * Constructs a {@link ProxyAuthenticator} which handles authenticating against proxy servers.
     *
     * @param username Username used in authentication challenges.
     * @param password Password used in authentication challenges.
     */
    public ProxyAuthenticator(String username, String password) {
        this.challengeHandler = new AuthorizationChallengeHandler(username, password);
    }

    /**
     * Creates an {@link Interceptor} which will attempt to capture authentication info response headers to update the
     * {@link ProxyAuthenticator} in preparation for future authentication challenges.
     *
     * @return An {@link Interceptor} that attempts to read headers from the response.
     */
    public Interceptor getProxyAuthenticationInfoInterceptor() {
        return new ProxyAuthenticationInfoInterceptor(challengeHandler);
    }

    /**
     * @param route Route being used to reach the server.
     * @param response Response from the server requesting authentication.
     * @return The initial request with an authorization header applied.
     */
    @Override
    public Request authenticate(Route route, Response response) {
        String method = response.request().method();
        String uri = response.request().url().encodedPath();

        String authorizationHeader = challengeHandler.attemptToPipelineAuthorization(method, uri,
            () -> entitySupplier(response.request()));

        // Pipelining was successful, use the generated authorization header.
        if (!CoreUtils.isNullOrEmpty(authorizationHeader)) {
            return response.request().newBuilder()
                .header(PROXY_AUTHORIZATION, authorizationHeader)
                .build();
        }

        // If this is a pre-emptive challenge quit now if pipelining doesn't produce anything.
        if ("Preemptive Authenticate".equalsIgnoreCase(response.message())) {
            return response.request();
        }

        boolean hasBasicChallenge = false;
        List<Map<String, String>> digestChallenges = new ArrayList<>();

        for (Challenge challenge : response.challenges()) {
            if ("Basic".equalsIgnoreCase(challenge.scheme())) {
                hasBasicChallenge = true;
            } else if ("Digest".equalsIgnoreCase(challenge.scheme())) {
                digestChallenges.add(challenge.authParams());
            }
        }

        // Prefer digest challenges over basic.
        if (digestChallenges.size() > 0) {
            authorizationHeader = challengeHandler.handleDigest(method, uri, digestChallenges,
                () -> entitySupplier(response.request()));
        }

        /*
         * If Digest proxy was attempted but it wasn't able to be computed and the server sent a Basic
         * challenge as well apply the basic authorization header.
         */
        if (authorizationHeader == null && hasBasicChallenge) {
            authorizationHeader = challengeHandler.handleBasic();
        }

        Request.Builder requestBuilder = response.request().newBuilder();

        if (authorizationHeader != null) {
            requestBuilder.header(PROXY_AUTHORIZATION, authorizationHeader);
        }

        return requestBuilder.build();
    }

    /*
     * Retrieves the HTTP entity for the given request. A HTTP entity consists of the entity headers and the body, if
     * present.
     */
    private byte[] entitySupplier(Request request) {
        RequestBody requestBody = request.body();

        if (requestBody == null) {
            return new byte[0];
        }

        try {
            Buffer bodyBuffer = new Buffer();
            requestBody.writeTo(bodyBuffer);
            return bodyBuffer.readByteArray();
        } catch (IOException e) {
            throw logger.logExceptionAsWarning(new UncheckedIOException(e));
        }
    }

    /**
     * This class handles intercepting the response returned from the server when proxying.
     */
    private static class ProxyAuthenticationInfoInterceptor implements Interceptor {
        private final AuthorizationChallengeHandler challengeHandler;

        /**
         * Constructs an {@link Interceptor} which intercepts responses from the server when using proxy authentication
         * in an attempt to retrieve authentication info response headers.
         *
         * @param challengeHandler {@link AuthorizationChallengeHandler} that consumes authentication info response
         * headers.
         */
        ProxyAuthenticationInfoInterceptor(AuthorizationChallengeHandler challengeHandler) {
            this.challengeHandler = challengeHandler;
        }

        /**
         * Attempts to intercept the 'Proxy-Authentication-Info' response header sent from the server. If the header is
         * set it will be used to validate the request and response and update the pipelined challenge in the passed
         * {@link AuthorizationChallengeHandler}.
         *
         * @param chain Interceptor chain.
         * @return Response returned from the server.
         * @throws IOException If an I/O error occurs.
         */
        @Override
        public Response intercept(Chain chain) throws IOException {
            Response response = chain.proceed(chain.request());

            String proxyAuthenticationInfoHeader = response.header(PROXY_AUTHENTICATION_INFO);
            if (!CoreUtils.isNullOrEmpty(proxyAuthenticationInfoHeader)) {
                Map<String, String> headerPieces = AuthorizationChallengeHandler
                    .parseChallengeHeader(proxyAuthenticationInfoHeader);
                Map<String, String> authorizationPieces = AuthorizationChallengeHandler
                    .parseChallengeHeader(chain.request().header(PROXY_AUTHORIZATION));

                /*
                 * If the authentication info response contains a cnonce or nc value it MUST match the value sent in the
                 * authorization header. This is the server performing validation to the client that it received the
                 * information.
                 */
                assert !headerPieces.containsKey(CNONCE) ||
                    headerPieces.get(CNONCE).equals(authorizationPieces.get(CNONCE));
                assert !headerPieces.containsKey(NC) || headerPieces.get(NC).equals(authorizationPieces.get(NC));

                challengeHandler.consumeAuthenticationInfoHeader(headerPieces);
            }

            return response;
        }
    }
}
