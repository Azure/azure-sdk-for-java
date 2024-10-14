// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.util.auth;

import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.HttpResponse;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Class representing a challenge handler for authentication.
 */
public interface ChallengeHandler {

    /**
     * Handles the authentication challenge based on the HTTP request and response.
     *
     * @param request The HTTP request to be updated with authentication info.
     * @param response The HTTP response containing the authentication challenge.
     * @param cnonce The client-generated nonce for the authentication.
     * @param nonceCount The count of nonce usage for digest authentication.
     * @param lastChallenge A reference to the last challenge map, which stores the state of the previous challenge.
     */
    void handleChallenge(HttpRequest request, HttpResponse<?> response, String cnonce, int nonceCount, AtomicReference<ConcurrentHashMap<String, String>> lastChallenge);

    /**
     * Factory method for creating composite handlers.
     *
     * @param handlers The array of ChallengeHandler instances to be combined.
     * @return A CompositeChallengeHandler that combines the provided handlers.
     */
    static ChallengeHandler of(ChallengeHandler... handlers) {
        return new CompositeChallengeHandler(Arrays.asList(handlers));
    }

    /**
     * static class to handle multiple challenge handlers in a composite way.
     */
    class CompositeChallengeHandler implements ChallengeHandler {
        private final List<ChallengeHandler> challengeHandlers;

        private CompositeChallengeHandler(List<ChallengeHandler> challengeHandlers) {
            this.challengeHandlers = challengeHandlers;
        }


        @Override
        public void handleChallenge(HttpRequest request, HttpResponse<?> response, String cnonce, int nonceCount, AtomicReference<ConcurrentHashMap<String, String>> lastChallenge) {
            for (ChallengeHandler handler : challengeHandlers) {
                handler.handleChallenge(request, response, cnonce, nonceCount, lastChallenge);
                return;
            }
            throw new UnsupportedOperationException("None of the challenge handlers could handle the challenge.");
        }
    }
}


