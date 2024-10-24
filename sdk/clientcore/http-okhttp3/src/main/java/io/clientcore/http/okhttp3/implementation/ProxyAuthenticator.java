// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.okhttp3.implementation;

import io.clientcore.core.http.models.HttpHeader;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.HttpResponse;
import io.clientcore.core.util.ClientLogger;
import io.clientcore.core.util.auth.AuthUtils;
import io.clientcore.core.util.auth.ChallengeHandler;
import io.clientcore.core.util.binarydata.BinaryData;
import okhttp3.Authenticator;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * This class handles authorizing requests being sent through a proxy which require authentication.
 */
public final class ProxyAuthenticator implements Authenticator {
    private static final String VALIDATION_ERROR_TEMPLATE = "The '%s' returned in the 'Proxy-Authentication-Info' "
        + "header doesn't match the value sent in the 'Proxy-Authorization' header. Sent: %s, received: %s.";

    private static final String PREEMPTIVE_AUTHENTICATE = "Preemptive Authenticate";
    /**
     * Header representing the authorization the client is presenting to a proxy server.
     */
    public static final String PROXY_AUTHORIZATION = "Proxy-Authorization";

    /**
     * Header representing additional information a proxy server is expecting during future authentication requests.
     */
    public static final String PROXY_AUTHENTICATION_INFO = "Proxy-Authentication-Info";

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
    private static final String NEXT_NONCE = "nextnonce";
    private static final String NONCE = "nonce";

    private static final ClientLogger LOGGER = new ClientLogger(ProxyAuthenticator.class);

    private final ChallengeHandler compositeChallengeHandler;
    private final ProxyAuthenticationInfoInterceptor proxyInterceptor;
    private String lastAuthorizationHeader;

    /**
     * Constructs a {@link ProxyAuthenticator} which handles authenticating against proxy servers.
     *
     * @param compositeChallengeHandler List of challenge handlers that will process the challenges.
     */
    public ProxyAuthenticator(ChallengeHandler compositeChallengeHandler) {
        this.compositeChallengeHandler = compositeChallengeHandler;
        this.proxyInterceptor = new ProxyAuthenticationInfoInterceptor();
    }

    /**
     * Creates an {@link Interceptor} which will attempt to capture authentication info response headers to update the
     * {@link ProxyAuthenticator} in preparation for future authentication challenges.
     *
     * @return An {@link Interceptor} that attempts to read headers from the response.
     */
    public Interceptor getProxyAuthenticationInfoInterceptor() {
        return this.proxyInterceptor;
    }

    /**
     * @param route Route being used to reach the server.
     * @param response Response from the server requesting authentication.
     *
     * @return The initial request with an authorization header applied.
     */
    @Override
    public Request authenticate(Route route, Response response) {
        // Handle preemptive authentication
        if (PREEMPTIVE_AUTHENTICATE.equalsIgnoreCase(response.message())) {
            // If we have already authenticated, apply the stored Proxy-Authorization header.
            if (lastAuthorizationHeader != null) {
                return response.request().newBuilder()
                    .header(PROXY_AUTHORIZATION, lastAuthorizationHeader)
                    .build();
            }
            // If no previous authorization, return the request unchanged.
            return response.request();
        }

        Request.Builder requestBuilder = response.request().newBuilder();
        HttpRequest httpRequest = new HttpRequest(HttpMethod.valueOf(PROXY_METHOD), PROXY_URI_PATH);
        HttpResponse<?> httpResponse = new HttpResponse<>(httpRequest, response.code(), OkHttpResponse.fromOkHttpHeaders(response.headers()), NO_BODY);
        String authorizationHeader;
        // Replace nonce value in the PROXY_AUTHENTICATE header with the updated nonce
        ConcurrentHashMap<String, String> lastChallengeMap = proxyInterceptor.getLastChallenge();
        String updatedNonce = lastChallengeMap.get(NONCE);

        if (updatedNonce != null) {
            String proxyAuthenticateHeader = httpResponse.getHeaders().get(HttpHeaderName.PROXY_AUTHENTICATE).getValue();

            if (proxyAuthenticateHeader != null) {
                // Replace the old nonce with the updated nonce in the header
                String updatedHeader = replaceNonceInHeader(proxyAuthenticateHeader, updatedNonce);
                httpResponse.getHeaders().set(HttpHeaderName.PROXY_AUTHENTICATE, updatedHeader);
            }
        }

        compositeChallengeHandler.handleChallenge(httpRequest, httpResponse);
        authorizationHeader = httpRequest.getHeaders().getValue(HttpHeaderName.AUTHORIZATION);

        if (authorizationHeader != null) {
            // Store the Proxy-Authorization header for future preemptive requests.
            lastAuthorizationHeader = authorizationHeader;
            requestBuilder.header(PROXY_AUTHORIZATION, authorizationHeader);
        }

        return requestBuilder.build();
    }

    /**
     * Replaces the nonce value in the Proxy-Authenticate header with the new nonce.
     *
     * @param header The original Proxy-Authenticate header.
     * @param newNonce The new nonce to replace the existing one.
     * @return The updated Proxy-Authenticate header.
     */
    private String replaceNonceInHeader(String header, String newNonce) {
        // Split the header into parts
        String[] parts = header.split(","); // Assuming multiple values are comma-separated
        StringBuilder updatedHeader = new StringBuilder();

        for (String part : parts) {
            String trimmedPart = part.trim();
            // If the part contains "nonce=", replace it with the new nonce
            if (trimmedPart.startsWith("nonce=")) {
                updatedHeader.append("nonce=\"").append(newNonce).append("\"");
            } else {
                updatedHeader.append(trimmedPart); // Keep other parts unchanged
            }
            updatedHeader.append(", "); // Add a separator
        }

        // Remove the trailing ", " if present
        if (updatedHeader.length() > 2) {
            updatedHeader.setLength(updatedHeader.length() - 2);
        }

        return updatedHeader.toString();
    }

    /**
     * This class handles intercepting the response returned from the server when proxying.
     */
    private static class ProxyAuthenticationInfoInterceptor implements Interceptor {
        private static final String NONCE = "nonce";
        private final ConcurrentHashMap<String, String> lastChallenge = new ConcurrentHashMap<>();  // Manages the state of nonce.

        /**
         * Attempts to intercept the 'Proxy-Authentication-Info' response header sent from the server. If the header is
         * set it will be used to validate the request and response and update the pipelined challenge in the passed
         * {@link ChallengeHandler}.
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

            if (!AuthUtils.isNullOrEmpty(proxyAuthenticationInfoHeader)) {
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

                // Let the challenge handler process the info and return any data like next nonce.
                String nextNonce = processAuthenticationInfoHeader(authenticationInfoPieces);

                if (nextNonce != null) {
                    lastChallenge.put(NONCE, nextNonce);
                }
            }

            return response;
        }

        private ConcurrentHashMap<String, String> getLastChallenge() {
            return this.lastChallenge;
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

    /**
     * Processes the authentication info header and extracts the 'nextnonce' value if present.
     */
    private static String processAuthenticationInfoHeader(Map<String, String> authenticationInfoMap) {
        if (authenticationInfoMap == null || authenticationInfoMap.isEmpty()) {
            return null;
        }

        /*
         * Extracts the 'nextnonce' value from the authentication info header, if present.
         * This value is used to replace the current nonce for future digest authentications.
         */
        if (authenticationInfoMap.containsKey(NEXT_NONCE)) {
            return authenticationInfoMap.get(NEXT_NONCE);
        }

        // If no nextnonce is present, return null.
        return null;
    }
}
