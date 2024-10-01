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

/**
 * Handles authorization challenges.
 */
public class AuthorizationChallengeHandler {

    private ChallengeHandler handler;
    private final String username;
    private final String password;

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
     * @return The authorization header for digest authentication.
     */
    public String handleDigest(String method, String uri, List<Map<String, String>> challenges, Supplier<BinaryData> entityBodySupplier) {
        handler = new DigestHandler(username, password, method, uri, challenges, entityBodySupplier);
        return handler.handle();
    }

    /**
     * Attempts to pipeline authorization based on the last challenge type.
     *
     * @return The authorization header, or null if no handler is available.
     */
    public String attemptToPipelineAuthorization() {
        if (handler instanceof DigestHandler) {
            return handler.handle();
        } else if (handler instanceof BasicHandler) {
            return handler.handle();
        }
        return null;
    }
}
