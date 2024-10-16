// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.util.auth;

import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.HttpResponse;
import io.clientcore.core.util.auth.ChallengeHandler;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Handles basic authentication challenges.
 */
public class BasicHandler implements ChallengeHandler {

    private final String username;
    private final String password;
    private static final String BASIC = "Basic ";
    private boolean hasHandledChallenge = false; // Tracks if the handler has handled a challenge

    public BasicHandler(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public void handleChallenge(HttpRequest request, HttpResponse<?> response, String cnonce, int nonceCount, AtomicReference<ConcurrentHashMap<String, String>> lastChallenge) {
        synchronized (request.getHeaders()) {
            // Clear the previous Authorization header
            request.getHeaders().set(HttpHeaderName.AUTHORIZATION, (String) null);

            // Set the Authorization header only if this handler has processed a challenge before
            if (!hasHandledChallenge) {
                String token = username + ":" + password;
                String authorizationHeader = BASIC + Base64.getEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8));
                request.getHeaders().add(HttpHeaderName.AUTHORIZATION, authorizationHeader);

                // Mark that this handler has handled a challenge
                hasHandledChallenge = true;
            }
        }
    }
}
