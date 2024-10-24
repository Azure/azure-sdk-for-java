// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.util.auth;

import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.util.ClientLogger;
import io.clientcore.core.util.auth.ChallengeHandler;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static io.clientcore.core.util.auth.AuthUtils.BASIC;

/**
 * Handles basic authentication challenges.
 * This class implements the `ChallengeHandler` interface and provides
 * functionality to handle HTTP Basic Authentication challenges.
 */
public class BasicChallengeHandler implements ChallengeHandler {
    private static final ClientLogger LOGGER = new ClientLogger(BasicChallengeHandler.class);

    private final String username;
    private final String password;

    /**
     * Constructs a `BasicHandler` with the specified username and password.
     *
     * @param username The username for basic authentication.
     * @param password The password for basic authentication.
     */
    public BasicChallengeHandler(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public void handleChallenge(HttpRequest request, Response<?> response) {
        if (canHandle(response)) {
            // Check if the Authorization header is already present
            String existingAuthHeader = request.getHeaders().getValue(HttpHeaderName.AUTHORIZATION);
            if (existingAuthHeader == null) {
                String authHeader = generateBasicAuthHeader(username, password);
                request.getHeaders().add(HttpHeaderName.AUTHORIZATION, authHeader);
            } else if (existingAuthHeader.startsWith(BASIC)) {
                // Optionally, log a warning if the existing header is also Basic but will not overwrite it
                LOGGER.atWarning().log("Authorization header already exists: " + existingAuthHeader);
            }
        }
    }

    @Override
    public boolean canHandle(Response<?> response) {
        String authHeader;
        if (response.getHeaders() != null && response.getHeaders().get(HttpHeaderName.WWW_AUTHENTICATE) != null) {
            authHeader = response.getHeaders().get(HttpHeaderName.WWW_AUTHENTICATE).getValue();
            return authHeader != null && authHeader.startsWith(BASIC);
        }

        if (response.getHeaders() != null && response.getHeaders().get(HttpHeaderName.PROXY_AUTHENTICATE) != null) {
            authHeader = response.getHeaders().get(HttpHeaderName.PROXY_AUTHENTICATE).getValue();
            return authHeader != null && authHeader.contains(BASIC.trim());
        }
        return false;
    }

    private String generateBasicAuthHeader(String username, String password) {
        String token = username + ":" + password;
        return BASIC + Base64.getEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8));
    }
}
