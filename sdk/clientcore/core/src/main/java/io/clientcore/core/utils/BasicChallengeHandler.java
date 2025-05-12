// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.utils;

import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.models.binarydata.BinaryData;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
            HttpHeaderName headerName = isProxy ? HttpHeaderName.PROXY_AUTHORIZATION : HttpHeaderName.AUTHORIZATION;
            request.getHeaders().set(headerName, authHeader);
        }
    }

    @Override
    public boolean canHandle(Response<BinaryData> response, boolean isProxy) {
        HttpHeaders responseHeaders = response.getHeaders();
        if (responseHeaders == null) {
            return false;
        }

        HttpHeaderName authHeaderName = isProxy ? HttpHeaderName.PROXY_AUTHENTICATE : HttpHeaderName.WWW_AUTHENTICATE;
        List<String> authenticateHeaders = responseHeaders.getValues(authHeaderName);
        if (CoreUtils.isNullOrEmpty(authenticateHeaders)) {
            return false;
        }

        for (String authenticateHeader : authenticateHeaders) {
            for (AuthenticateChallenge challenge : AuthUtils.parseAuthenticateHeader(authenticateHeader)) {
                if (canHandle(challenge)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public Map.Entry<String, AuthenticateChallenge> handleChallenge(String method, URI uri,
        List<AuthenticateChallenge> challenges) {
        Objects.requireNonNull(challenges, "Cannot use a null 'challenges' to handle challenges.");
        for (AuthenticateChallenge challenge : challenges) {
            if (canHandle(challenge)) {
                return new AbstractMap.SimpleImmutableEntry<>(authHeader, challenge);
            }
        }

        return null;
    }

    @Override
    public boolean canHandle(List<AuthenticateChallenge> challenges) {
        Objects.requireNonNull(challenges, "Cannot use a null 'challenges' to determine if it can be handled.");
        for (AuthenticateChallenge challenge : challenges) {
            if (canHandle(challenge)) {
                return true;
            }
        }

        return false;
    }

    private static boolean canHandle(AuthenticateChallenge challenge) {
        return challenge != null && BASIC.equalsIgnoreCase(challenge.getScheme());
    }
}
