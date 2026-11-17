// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * A Microsoft Entra ID access token returned by a {@link CosmosAccessTokenProvider}.
 */
public final class CosmosAccessToken {
    private final String token;
    private final OffsetDateTime expiresAt;

    /**
     * Creates an access token.
     *
     * @param token the raw access token. Must not be null or empty.
     * @param expiresAt the expiry time of the token. Must not be null.
     * @throws IllegalArgumentException if {@code token} is null or empty.
     * @throws NullPointerException if {@code expiresAt} is null.
     */
    public CosmosAccessToken(String token, OffsetDateTime expiresAt) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Argument 'token' must not be null or empty.");
        }

        this.token = token;
        this.expiresAt = Objects.requireNonNull(expiresAt, "Argument 'expiresAt' must not be null.");
    }

    /**
     * Gets the raw access token.
     *
     * @return the raw access token.
     */
    public String getToken() {
        return token;
    }

    /**
     * Gets the expiry time of the token.
     *
     * @return the expiry time of the token.
     */
    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    @Override
    public String toString() {
        // Never expose the token value in logs.
        return "CosmosAccessToken{expiresAt=" + expiresAt + "}";
    }
}
