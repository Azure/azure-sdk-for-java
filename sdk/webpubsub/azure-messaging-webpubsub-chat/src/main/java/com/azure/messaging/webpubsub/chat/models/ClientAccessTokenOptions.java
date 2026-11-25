// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.chat.models;

import java.time.Duration;

/**
 * Options for the {@code getClientAccessToken} operation.
 */
public final class ClientAccessTokenOptions {
    private Duration expiresAfter = Duration.ofHours(1);
    private String userId;

    /** Creates an instance of {@link ClientAccessTokenOptions}. */
    public ClientAccessTokenOptions() {
    }

    /**
     * Sets the duration after which the client access token expires.
     *
     * @param expiresAfter The token lifetime. Must be at least one minute and at most
     * {@link Integer#MAX_VALUE} minutes. Fractional minutes are truncated when requesting a token from the service.
     * @return The updated options.
     */
    public ClientAccessTokenOptions setExpiresAfter(Duration expiresAfter) {
        this.expiresAfter = expiresAfter;
        return this;
    }

    /**
     * Gets the duration after which the client access token expires.
     *
     * @return The token lifetime, which defaults to one hour.
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
    public ClientAccessTokenOptions setUserId(String userId) {
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
