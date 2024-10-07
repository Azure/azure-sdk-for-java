// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.util.auth;

import io.clientcore.core.util.auth.ChallengeHandler;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static io.clientcore.core.util.auth.AuthScheme.BASIC;

/**
 * Handles basic authentication challenges.
 */
public class BasicHandler extends ChallengeHandler {
    private final String username;
    private final String password;

    /**
     * Constructs a BasicHandler with the provided username and password.
     *
     * @param username The username for authentication.
     * @param password The password for authentication.
     */
    public BasicHandler(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Handles the basic authentication challenge by encoding the username and password.
     *
     * @return The authorization header for basic authentication.
     */
    @Override
    public String handle() {
        String token = username + ":" + password;
        return BASIC.getScheme() + Base64.getEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8));
    }
}
