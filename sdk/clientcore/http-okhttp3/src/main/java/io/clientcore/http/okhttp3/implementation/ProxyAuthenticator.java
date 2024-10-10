// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.okhttp3.implementation;

import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.util.ClientLogger;
import io.clientcore.core.util.auth.AuthUtils;
import io.clientcore.core.util.auth.AuthorizationChallengeHandler;
import io.clientcore.core.util.binarydata.BinaryData;
import okhttp3.Authenticator;
import okhttp3.Challenge;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static io.clientcore.core.util.auth.AuthUtils.PROXY_AUTHENTICATION_INFO;
import static io.clientcore.core.util.auth.AuthUtils.PROXY_AUTHORIZATION;
import static io.clientcore.core.util.auth.AuthUtils.isNullOrEmpty;

/**
 * This class handles authorizing requests being sent through a proxy which require authentication.
 */
public final class ProxyAuthenticator implements Authenticator {
    private static final String VALIDATION_ERROR_TEMPLATE = "The '%s' returned in the 'Proxy-Authentication-Info' "
        + "header doesn't match the value sent in the 'Proxy-Authorization' header. Sent: %s, received: %s.";

    private static final String BASIC = "basic";
    private static final String DIGEST = "digest";
    private static final String PREEMPTIVE_AUTHENTICATE = "Preemptive Authenticate";

    /*
     * Proxies use 'CONNECT' as the HTTP method.
     */
    private static final String PROXY_METHOD = HttpMethod.CONNECT.name();

    /*
     * Proxies are always the root path.
     */
    private static final String PROXY_URI_PATH = "/";

    /*
     * Digest authentication to a proxy uses the 'CONNECT' method, these can't have a request body.
     */
    private static final Supplier<BinaryData> NO_BODY = BinaryData::empty;

    private static final String CNONCE = "cnonce";
    private static final String NC = "nc";

    private static final ClientLogger LOGGER = new ClientLogger(ProxyAuthenticator.class);

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
     *
     * @return The initial request with an authorization header applied.
     */
    @Override
    public Request authenticate(Route route, Response response) {
        String authorizationHeader =
            challengeHandler.attemptToPipelineAuthorization();

        // Pipelining was successful, use the generated authorization header.
        if (!isNullOrEmpty(authorizationHeader)) {
            return response.request().newBuilder()
                .header(PROXY_AUTHORIZATION, authorizationHeader)
                .build();
        }

        // If this is a pre-emptive challenge quit now if pipelining doesn't produce anything.
        if (PREEMPTIVE_AUTHENTICATE.equalsIgnoreCase(response.message())) {
            return response.request();
        }

        boolean hasBasicChallenge = false;
        List<Map<String, String>> digestChallenges = new ArrayList<>();

        for (Challenge challenge : response.challenges()) {
            if (BASIC.equalsIgnoreCase(challenge.scheme())) {
                hasBasicChallenge = true;
            } else if (DIGEST.equalsIgnoreCase(challenge.scheme())) {
                digestChallenges.add(challenge.authParams());
            }
        }

        // Prefer digest challenges over basic.
        if (digestChallenges.size() > 0) {
            authorizationHeader =
                challengeHandler.handleDigest(PROXY_METHOD, PROXY_URI_PATH, digestChallenges, NO_BODY, null);
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
         *
         * @return Response returned from the server.
         *
         * @throws IOException If an I/O error occurs.
         */
        @Override
        public Response intercept(Chain chain) throws IOException {
            Response response = chain.proceed(chain.request());

            String proxyAuthenticationInfoHeader = response.header(PROXY_AUTHENTICATION_INFO);

            if (!isNullOrEmpty(proxyAuthenticationInfoHeader)) {
                Map<String, String> authenticationInfoPieces = AuthUtils
                    .parseAuthenticationOrAuthorizationHeader(proxyAuthenticationInfoHeader);
                Map<String, String> authorizationPieces = AuthUtils
                    .parseAuthenticationOrAuthorizationHeader(chain.request().header(PROXY_AUTHORIZATION));

                /*
                 * If the authentication info response contains a cnonce or nc value it MUST match the value sent in the
                 * authorization header. This is the server performing validation to the client that it received the
                 * information.
                 */
                validateProxyAuthenticationInfoValue(CNONCE, authenticationInfoPieces, authorizationPieces);
                validateProxyAuthenticationInfoValue(NC, authenticationInfoPieces, authorizationPieces);

                challengeHandler.consumeAuthenticationInfoHeader(authenticationInfoPieces);
            }

            return response;
        }
    }

    /*
     * Validates that the value received in the 'Proxy-Authentication-Info' matches the value sent in the
     * 'Proxy-Authorization' header. If the values don't match an 'IllegalStateException' will be thrown with a message
     * outlining that the values didn't match.
     */
    private static void validateProxyAuthenticationInfoValue(String name, Map<String, String> authenticationInfoPieces,
                                                             Map<String, String> authorizationPieces) {
        if (authenticationInfoPieces.containsKey(name)) {
            String sentValue = authorizationPieces.get(name);
            String receivedValue = authenticationInfoPieces.get(name);

            if (!receivedValue.equalsIgnoreCase(sentValue)) {
                throw LOGGER.logThrowableAsError(
                    new IllegalStateException(
                        String.format(VALIDATION_ERROR_TEMPLATE, name, sentValue, receivedValue)));
            }
        }
    }
}
