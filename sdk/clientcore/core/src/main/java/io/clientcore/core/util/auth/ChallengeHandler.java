// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.util.auth;

import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.implementation.util.auth.DigestChallengeHandler;
import io.clientcore.core.implementation.util.auth.DigestProxyChallengeHandler;
import io.clientcore.core.util.ClientLogger;

import java.util.Arrays;
import java.util.List;

/**
 * Class representing a challenge handler for authentication.
 */
public interface ChallengeHandler {
    ClientLogger LOGGER = new ClientLogger(ChallengeHandler.class);

    /**
     * Handles the authentication challenge based on the HTTP request and response.
     * @param request The HTTP request to be updated with authentication info.
     * @param response The HTTP response containing the authentication challenge.
     */
    void handleChallenge(HttpRequest request, Response<?> response);

    /**
     * Validate if this ChallengeHandler can handle the provided challenge
     * by inspecting the 'Proxy-Authenticate' or 'WWW-Authenticate' headers.
     * @param response The HTTP response containing the authentication challenge.
     * @return boolean indicating if the challenge can be handled.
     */
    boolean canHandle(Response<?> response);

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
     * A private static class to handle multiple challenge handlers in a composite way.
     */
    class CompositeChallengeHandler implements ChallengeHandler {
        private final List<ChallengeHandler> challengeHandlers;

        private CompositeChallengeHandler(List<ChallengeHandler> challengeHandlers) {
            this.challengeHandlers = challengeHandlers;
        }

        @Override
        public boolean canHandle(Response<?> response) {
            for (ChallengeHandler handler : challengeHandlers) {
                if (handler.canHandle(response)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void handleChallenge(HttpRequest request, Response<?> response) {
            // Check for DigestChallengeHandler or DigestProxyChallengeHandler first
            for (ChallengeHandler handler : challengeHandlers) {
                if (handler.canHandle(response)
                    && (handler instanceof DigestChallengeHandler || handler instanceof DigestProxyChallengeHandler)) {
                    handler.handleChallenge(request, response);
                    return;
                }
            }

            // If no digest handler was able to handle, check for other handlers (e.g., Basic)
            for (ChallengeHandler handler : challengeHandlers) {
                if (handler.canHandle(response)) {
                    handler.handleChallenge(request, response);
                    return;
                }
            }

            LOGGER.logThrowableAsError(new UnsupportedOperationException("None of the challenge handlers could handle the challenge."));
        }
    }
}


