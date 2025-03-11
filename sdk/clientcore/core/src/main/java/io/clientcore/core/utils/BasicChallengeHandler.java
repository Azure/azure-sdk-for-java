// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.utils;

import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.models.binarydata.BinaryData;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import static io.clientcore.core.utils.AuthUtils.BASIC;

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
    public void handleChallenge(HttpRequest request, Response<BinaryData> response, boolean isProxy) {
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
    public boolean canHandle(Response<BinaryData> response, boolean isProxy) {
        if (response.getHeaders() != null) {
            HttpHeaderName authHeaderName
                = isProxy ? HttpHeaderName.PROXY_AUTHENTICATE : HttpHeaderName.WWW_AUTHENTICATE;
            String authHeader = response.getHeaders().getValue(authHeaderName);

            if (authHeader != null) {
                // Parse the authenticate header into AuthenticateChallenges, then check if any use scheme 'Basic'.
                List<AuthenticateChallenge> challenges = AuthUtils.parseAuthenticateHeader(authHeader);
                for (AuthenticateChallenge challenge : challenges) {
                    if (BASIC.equalsIgnoreCase(challenge.getScheme())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
