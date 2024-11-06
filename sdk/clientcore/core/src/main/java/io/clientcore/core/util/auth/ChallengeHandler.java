// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.util.auth;

import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
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
     *
     * @param request The HTTP request to be updated with authentication info.
     * @param response The HTTP response containing the authentication challenge.
     * @param isProxy Indicates if the challenge is for a proxy.
     */
    void handleChallenge(HttpRequest request, Response<?> response, boolean isProxy);

    /**
     * Validate if this ChallengeHandler can handle the provided challenge
     * by inspecting the 'Proxy-Authenticate' or 'WWW-Authenticate' headers.
     *
     * @param response The HTTP response containing the authentication challenge.
     * @param isProxy boolean indicating if it is a proxy challenge handler.
     * @return boolean indicating if the challenge can be handled.
     */
    boolean canHandle(Response<?> response, boolean isProxy);

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

        CompositeChallengeHandler(List<ChallengeHandler> challengeHandlers) {
            this.challengeHandlers = challengeHandlers;
        }

        @Override
        public boolean canHandle(Response<?> response, boolean isProxy) {
            for (ChallengeHandler handler : challengeHandlers) {
                if (handler.canHandle(response, isProxy)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void handleChallenge(HttpRequest request, Response<?> response, boolean isProxy) {
            // First, try to handle with DigestChallengeHandler, giving it priority
            for (ChallengeHandler handler : challengeHandlers) {
                if (handler.canHandle(response, isProxy) && handler instanceof DigestChallengeHandler) {
                    handler.handleChallenge(request, response, isProxy);
                    return;
                }
            }

            // If no DigestChallengeHandler was able to handle, try other handlers (e.g., Basic)
            for (ChallengeHandler handler : challengeHandlers) {
                if (handler.canHandle(response, isProxy)) {
                    handler.handleChallenge(request, response, isProxy);
                    return;
                }
            }

            // Log an error if no handler could handle the challenge
            LOGGER.logThrowableAsError(
                new UnsupportedOperationException("None of the challenge handlers could handle the challenge."));
        }
    }
}
