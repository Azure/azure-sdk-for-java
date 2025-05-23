// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.utils;

import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.models.binarydata.BinaryData;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;

final class CompositeChallengeHandler implements ChallengeHandler {
    private static final ClientLogger LOGGER = new ClientLogger(CompositeChallengeHandler.class);

    private final List<ChallengeHandler> challengeHandlers;

    CompositeChallengeHandler(List<ChallengeHandler> challengeHandlers) {
        this.challengeHandlers = challengeHandlers;
    }

    @Override
    public boolean canHandle(Response<BinaryData> response, boolean isProxy) {
        for (ChallengeHandler handler : challengeHandlers) {
            if (handler.canHandle(response, isProxy)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void handleChallenge(HttpRequest request, Response<BinaryData> response, boolean isProxy) {
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
        LOGGER.atError().log("None of the challenge handlers could handle the challenge.");
    }

    @Override
    public Map.Entry<String, AuthenticateChallenge> handleChallenge(String method, URI uri,
        List<AuthenticateChallenge> challenges) {
        Objects.requireNonNull(challenges, "Cannot use a null 'challenges' to handle challenges.");
        for (ChallengeHandler handler : challengeHandlers) {
            Map.Entry<String, AuthenticateChallenge> result = handler.handleChallenge(method, uri, challenges);
            if (result != null) {
                return result;
            }
        }

        return null;
    }

    @Override
    public boolean canHandle(List<AuthenticateChallenge> challenges) {
        Objects.requireNonNull(challenges, "Cannot use a null 'challenges' to determine if it can be handled.");
        for (ChallengeHandler handler : challengeHandlers) {
            if (handler.canHandle(challenges)) {
                return true;
            }
        }

        return false;
    }
}
