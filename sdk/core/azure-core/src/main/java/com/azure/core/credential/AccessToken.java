// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.credential;

import java.time.OffsetDateTime;

/**
 * Represents an immutable access token with a token string and an expiration time.
 */
public class AccessToken {
    private final String token;
    private final OffsetDateTime expiresOn;

    /**
     * Creates an access token instance.
     * @param token the token string.
     * @param expiresOn the expiration time.
     */
    public AccessToken(String token, OffsetDateTime expiresOn) {
        this.token = token;
        this.expiresOn = expiresOn.minusMinutes(2); // 2 minutes before token expires
    }

    /**
     * @return the token string.
     */
    public String getToken() {
        return token;
    }

    /**
     * @return the time when the token expires, in UTC.
     */
    public OffsetDateTime getExpiresOn() {
        return expiresOn;
    }

    /**
     * @return if the token has expired.
     */
    public boolean isExpired() {
        return OffsetDateTime.now().isAfter(expiresOn);
    }
}
