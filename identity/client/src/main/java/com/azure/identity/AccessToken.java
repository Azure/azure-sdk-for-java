// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import java.time.OffsetDateTime;

/**
 * Represents an access token with a token string and an expiration time.
 */
public class AccessToken {
    private String token;
    private OffsetDateTime expiresOn;

    /**
     * @return the token string.
     */
    public String token() {
        return token;
    }

    /**
     * Specifies the token string.
     * @param token the token string.
     * @return AccessToken
     */
    AccessToken token(String token) {
        this.token = token;
        return this;
    }

    /**
     * @return the time when the token expires, in UTC.
     */
    public OffsetDateTime expiresOn() {
        return expiresOn;
    }

    /**
     * Specifies when the token expires, in UTC.
     * @param expiresOn the date-time of expiry, in UTC.
     * @return AccessToken
     */
    AccessToken expiresOn(OffsetDateTime expiresOn) {
        this.expiresOn = expiresOn;
        return this;
    }

    /**
     * @return if the token has expired.
     */
    public boolean isExpired() {
        return OffsetDateTime.now().isAfter(expiresOn);
    }
}
