// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.util.auth;

import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;

import java.util.Arrays;

/**
 * Class representing a challenge handler for authentication.
 */
public interface ChallengeHandler {
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
}
