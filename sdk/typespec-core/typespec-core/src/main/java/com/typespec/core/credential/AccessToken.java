// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.credential;

import java.time.OffsetDateTime;

/**
 * Represents an immutable access token with a token string and an expiration time.
 */
public class AccessToken {
    private final String token;
    private final OffsetDateTime expiresAt;

    /**
     * Creates an access token instance.
     *
     * @param token the token string.
     * @param expiresAt the expiration time.
     */
    public AccessToken(String token, OffsetDateTime expiresAt) {
        this.token = token;
        this.expiresAt = expiresAt;
    }

    /**
     * Gets the token.
     *
     * @return The token.
     */
    public String getToken() {
        return token;
    }

    /**
     * Gets the time when the token expires, in UTC.
     *
     * @return The time when the token expires, in UTC.
     */
    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    /**
     * Whether the token has expired.
     *
     * @return Whether the token has expired.
     */
    public boolean isExpired() {
        return OffsetDateTime.now().isAfter(expiresAt);
    }
}
