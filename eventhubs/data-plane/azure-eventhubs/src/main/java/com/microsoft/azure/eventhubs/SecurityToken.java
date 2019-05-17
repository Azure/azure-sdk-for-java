// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs;

import java.util.Date;

public class SecurityToken {
    private final String tokenType;
    private final String token;
    private final Date validTo;
    private final String audience;

    public SecurityToken(final String token, final Date validTo, final String audience, final String tokenType) {
        this.tokenType = tokenType;
        this.token = token;
        this.validTo = validTo;
        this.audience = audience;
    }

    public String getTokenType() {
        return this.tokenType;
    }

    public String getToken() {
        return this.token;
    }

    public Date validTo() {
        return this.validTo;
    }
    
    public String getAudience() {
    	return this.audience;
    }
}
