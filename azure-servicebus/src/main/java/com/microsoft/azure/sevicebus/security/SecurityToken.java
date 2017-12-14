package com.microsoft.azure.sevicebus.security;

import java.time.Instant;

public class SecurityToken {
    private SecurityTokenType tokenType;
    private String tokenAudience;
    private String tokenValue;
    private Instant validFrom;
    private Instant validUntil;
    
    public SecurityToken(SecurityTokenType tokenType, String tokenAudience, String tokenValue, Instant validFrom, Instant validUntil)
    {
        this.tokenType = tokenType;
        this.tokenAudience = tokenAudience;
        this.tokenValue = tokenValue;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
    }

    public SecurityTokenType getTokenType() {
        return tokenType;
    }
    
    public String getTokenAudience() {
        return this.tokenAudience;
    }

    public String getTokenValue() {
        return this.tokenValue;
    }

    public Instant getValidFrom() {
        return this.validFrom;
    }

    public Instant getValidUntil() {
        return this.validUntil;
    }
}
