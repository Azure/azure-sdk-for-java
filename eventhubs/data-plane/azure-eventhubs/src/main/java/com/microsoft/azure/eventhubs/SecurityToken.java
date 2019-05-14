// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs;

import java.util.Date;

public final class SecurityToken {
    private final String tokenType;
    private final String token;
    private final Date validTo;

    public SecurityToken(final String tokenType, final String token, final Date validTo) {
        this.tokenType = tokenType;
        this.token = token;
        this.validTo = validTo;
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
}
