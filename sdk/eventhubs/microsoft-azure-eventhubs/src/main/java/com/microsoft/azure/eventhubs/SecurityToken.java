// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs;

import java.util.Date;

/**
 * A generic representation of a security token.
 *
 */
public class SecurityToken {
    private final String tokenType;
    private final String token;
    private final Date validTo;
    private final String audience;

    /**
     * Constructor.
     * @param token      the actual token data
     * @param validTo    expiration date/time of the token
     * @param audience   audience of the token
     * @param tokenType  type of the token
     */
    public SecurityToken(final String token, final Date validTo, final String audience, final String tokenType) {
        this.tokenType = tokenType;
        this.token = token;
        this.validTo = validTo;
        this.audience = audience;
    }

    /**
     * Get the type of the token.
     * @return token type
     */
    public String getTokenType() {
        return this.tokenType;
    }

    /**
     * Get the token itself.
     * @return token
     */
    public String getToken() {
        return this.token;
    }

    /**
     * Get the expiration date/time of the token.
     * @return expiration
     */
    public Date validTo() {
        return this.validTo;
    }
    
    /**
     * Get the audience of the token.
     * @return audience
     */
    public String getAudience() {
        return this.audience;
    }
}
