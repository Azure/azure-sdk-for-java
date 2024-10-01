//  Copyright (c) Microsoft Corporation. All rights reserved.
//  Licensed under the MIT License.

package io.clientcore.core.util.auth;

import java.util.List;

public class CompositeChallengeHandler extends ChallengeHandler {
    private final List<ChallengeHandler> handlers;

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

