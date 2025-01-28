// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.util.auth;

import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.instrumentation.logging.ClientLogger;

import java.util.List;

final class CompositeChallengeHandler implements ChallengeHandler {
    private static final ClientLogger LOGGER = new ClientLogger(CompositeChallengeHandler.class);

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
