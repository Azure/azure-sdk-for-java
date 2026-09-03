// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.chat.models;

import java.time.Duration;

/** Options for creating a Web PubSub client access token for Chat. */
public final class GetClientAccessTokenOptions {
    private Duration expiresAfter = Duration.ofHours(1);
    private String userId;

    /** Creates an instance of {@link GetClientAccessTokenOptions}. */
    public GetClientAccessTokenOptions() {
    }

    /**
     * Sets the duration after which the client access token expires.
     *
     * @param expiresAfter The token lifetime.
     * @return The updated options.
     */
    public GetClientAccessTokenOptions setExpiresAfter(Duration expiresAfter) {
        this.expiresAfter = expiresAfter;
        return this;
    }

    /**
     * Gets the duration after which the client access token expires.
     *
     * @return The token lifetime.
     */
    public Duration getExpiresAfter() {
        return expiresAfter;
    }

    /**
     * Sets the user ID included in the client access token.
     *
     * @param userId The user ID.
     * @return The updated options.
     */
    public GetClientAccessTokenOptions setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    /**
     * Gets the user ID included in the client access token.
     *
     * @return The user ID.
     */
    public String getUserId() {
        return userId;
    }
}
