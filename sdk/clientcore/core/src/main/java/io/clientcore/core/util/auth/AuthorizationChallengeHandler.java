// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.util.auth;

import io.clientcore.core.implementation.util.auth.BasicHandler;
import io.clientcore.core.implementation.util.auth.DigestHandler;
import io.clientcore.core.util.binarydata.BinaryData;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import static io.clientcore.core.util.auth.AuthUtils.isNullOrEmpty;

/**
 * Handles authorization challenges.
 */
public class AuthorizationChallengeHandler {

    private ChallengeHandler handler;
    private final String username;
    private final String password;
    private static final String NEXT_NONCE = "nextnonce";
    private static final String NONCE = "nonce";

    /**
     * Creates an {@link AuthorizationChallengeHandler} using the {@code username} and {@code password} to respond to
     * authentication challenges.
     *
     * @param username Username used to response to authorization challenges.
     * @param password Password used to respond to authorization challenges.
     * @throws NullPointerException If {@code username} or {@code password} are {@code null}.
     */
    public AuthorizationChallengeHandler(String username, String password) {
        this.username = Objects.requireNonNull(username, "'username' cannot be null.");
        this.password = Objects.requireNonNull(password, "'password' cannot be null.");
    }

    /**
     * Initializes the AuthorizationChallengeHandler with the provided credentials and
     * handles a basic authorization challenge.
     *
     * @return The authorization header for basic authentication.
     */
    public String handleBasic() {
        handler = new BasicHandler(username, password);
        return handler.handle();
    }

    /**
     * Initializes the AuthorizationChallengeHandler with the provided credentials and
     * handles a digest authorization challenge.
     *
     * @param method The HTTP method (e.g., GET, POST).
     * @param uri The request URI.
     * @param challenges The list of challenges from the server.
     * @param entityBodySupplier A supplier for the request entity body.
     * @param nonce The nonce value used in the digest authentication challenge.
     * @return The authorization header for digest authentication.
     */
    public String handleDigest(String method, String uri, List<Map<String, String>> challenges, Supplier<BinaryData> entityBodySupplier, String nonce) {
        handler = new DigestHandler(username, password, method, uri, challenges, entityBodySupplier, nonce);
        return handler.handle();
    }

    /**
     * Attempts to pipeline authorization, if supported by the handler.
     *
     * @return The authorization header, or null if pipelining isn't supported.
     */
    public String attemptToPipelineAuthorization() {
        if (handler instanceof DigestHandler) {
            ((DigestHandler) handler).setPipelineMode(true);
            String result = handler.handle();
            ((DigestHandler) handler).setPipelineMode(false);
            return result;
        } else if (handler instanceof BasicHandler) {
            return handler.handle();
        }
        return null;
    }

    /**
     * Consumes the authentication info header and updates the internal state, if applicable.
     *
     * @param authenticationInfoMap The parsed pieces of the authentication info header.
     */
    public void consumeAuthenticationInfoHeader(Map<String, String> authenticationInfoMap) {
        if (!(handler instanceof DigestHandler)) {
            return;
        }
        if (isNullOrEmpty(authenticationInfoMap)) {
            return;
        }

        /*
         * If the authentication info header has a nextnonce value set update the last challenge nonce value to it.
         * The nextnonce value indicates to the client which nonce value it should use to generate its response value.
         */
        if (authenticationInfoMap.containsKey(NEXT_NONCE)) {
            ((DigestHandler) (handler)).getLastChallenge().get().put(NONCE, authenticationInfoMap.get(NEXT_NONCE));
        }
    }
}
