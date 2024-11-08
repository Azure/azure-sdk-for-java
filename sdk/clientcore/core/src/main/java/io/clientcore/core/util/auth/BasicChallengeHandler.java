// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.util.auth;

import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static io.clientcore.core.util.auth.AuthUtils.BASIC;

/**
 * Handles basic authentication challenges.
 * This class implements the `ChallengeHandler` interface and provides
 * functionality to handle HTTP Basic Authentication challenges.
 */
public class BasicChallengeHandler implements ChallengeHandler {

    private final String authHeader;

    /**
     * Constructs a `BasicChallengeHandler` with the specified username and password.
     *
     * @param username The username for basic authentication.
     * @param password The password for basic authentication.
     */
    public BasicChallengeHandler(String username, String password) {
        String token = username + ":" + password;
        this.authHeader = "Basic " + Base64.getEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void handleChallenge(HttpRequest request, Response<?> response, boolean isProxy) {
        if (canHandle(response, isProxy)) {
            synchronized (request.getHeaders()) {
                HttpHeaderName headerName = isProxy ? HttpHeaderName.PROXY_AUTHORIZATION : HttpHeaderName.AUTHORIZATION;
                // Check if the appropriate Authorization header is already present
                if (request.getHeaders().getValue(headerName) == null) {
                    request.getHeaders().add(headerName, authHeader);
                }
            }
        }
    }

    @Override
    public boolean canHandle(Response<?> response, boolean isProxy) {
        String authHeader;
        if (response.getHeaders() != null) {
            HttpHeaderName authHeaderName
                = isProxy ? HttpHeaderName.PROXY_AUTHENTICATE : HttpHeaderName.WWW_AUTHENTICATE;
            authHeader = response.getHeaders().getValue(authHeaderName);

            if (authHeader != null) {
                // Split by commas to handle multiple authentication methods in the header.
                String[] challenges = authHeader.split(",");
                for (String challenge : challenges) {
                    if (challenge.trim().regionMatches(true, 0, BASIC, 0, BASIC.length())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
