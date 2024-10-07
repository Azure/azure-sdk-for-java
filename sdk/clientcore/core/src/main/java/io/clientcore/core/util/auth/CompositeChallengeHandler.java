// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.util.auth;

import java.util.List;

/**
 * Composite challenge handler that iterates through a list of handlers to find a valid authorization header.
 */
public class CompositeChallengeHandler extends ChallengeHandler {
    private final List<ChallengeHandler> handlers;

    /**
     * Constructs a `CompositeChallengeHandler` with a list of handlers.
     *
     * @param handlers The list of `ChallengeHandler` instances to be combined.
     */
    public CompositeChallengeHandler(List<ChallengeHandler> handlers) {
        this.handlers = handlers;
    }

    @Override
    public String handle() {
        for (ChallengeHandler handler : handlers) {
            String authorizationHeader = handler.handle();
            if (authorizationHeader != null) {
                return authorizationHeader;
            }
        }
        return null;
    }
}

