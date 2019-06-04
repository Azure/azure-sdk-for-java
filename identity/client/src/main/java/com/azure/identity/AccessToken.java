package com.azure.identity;

import java.time.OffsetDateTime;

public class AccessToken {
    private String token;
    private OffsetDateTime expiresOn;

    public String token() {
        return token;
    }

    public AccessToken token(String token) {
        this.token = token;
        return this;
    }

    public OffsetDateTime expiresOn() {
        return expiresOn;
    }

    public AccessToken expiresOn(OffsetDateTime expiresOn) {
        this.expiresOn = expiresOn;
        return this;
    }

    public boolean isExpired() {
        return OffsetDateTime.now().isAfter(expiresOn);
    }
}
