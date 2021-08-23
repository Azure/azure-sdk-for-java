package com.azure.core.experimental.credential;

import com.azure.core.credential.AccessToken;

import java.time.OffsetDateTime;

public class AccessTokenExperimental extends AccessToken {
    private OffsetDateTime refreshOn;

    /**
     * Creates an access token instance.
     *
     * @param token     the token string.
     * @param expiresAt the expiration time.
     */
    public AccessTokenExperimental(String token, OffsetDateTime expiresAt) {
        super(token, expiresAt);
        if (expiresAt.compareTo(OffsetDateTime.MIN.plusMinutes(5)) > 0) {
            refreshOn = expiresAt.minusMinutes(5);
        } else {
            refreshOn = expiresAt;
        }
    }

    /**
     * Creates an access token instance.
     *
     * @param token     the token string.
     * @param expiresAt the expiration time.
     */
    public AccessTokenExperimental(String token, OffsetDateTime expiresAt, OffsetDateTime refreshOn) {
        super(token, expiresAt);
        this.refreshOn = refreshOn;
    }

    /**
     * @return the time when the token should be refreshed, in UTC.
     */
    public OffsetDateTime getRefreshOn() {
        return refreshOn;
    }
}
