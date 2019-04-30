package com.microsoft.azure.servicebus.security;

import java.time.Instant;

/**
 * This class encapsulates the details of a security token.
 * 
 * @since 1.2.0
 *
 */
public class SecurityToken {
    private SecurityTokenType tokenType;
    private String tokenAudience;
    private String tokenValue;
    private Instant validFrom;
    private Instant validUntil;
    
    /**
     * Creates an instance of security token. 
     * @param tokenType {@link SecurityTokenType} 
     * @param tokenAudience path of the entity for which this security token is to be presented
     * @param tokenValue string representation of the token value
     * @param validFrom Instant from when this token is valid
     * @param validUntil Instant when this token expires
     */
    public SecurityToken(SecurityTokenType tokenType, String tokenAudience, String tokenValue, Instant validFrom, Instant validUntil)
    {
        this.tokenType = tokenType;
        this.tokenAudience = tokenAudience;
        this.tokenValue = tokenValue;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
    }

    /**
     * Gets the type of this security token.
     * @return security token type
     */
    public SecurityTokenType getTokenType() {
        return tokenType;
    }
    
    /**
     * Gets the path of the entity for which this token is to be presented.
     * @return path of the entity for which this token is created
     */
    public String getTokenAudience() {
        return this.tokenAudience;
    }

    /**
     * Gets the value of this token.
     * @return string representation of the token value
     */
    public String getTokenValue() {
        return this.tokenValue;
    }

    /**
     * Gets the start time of this token validity
     * @return Instant from when this token is valid
     */
    public Instant getValidFrom() {
        return this.validFrom;
    }

    /**
     * Gets the end time of this token validity.
     * @return Instant when this token expires
     */
    public Instant getValidUntil() {
        return this.validUntil;
    }
}
