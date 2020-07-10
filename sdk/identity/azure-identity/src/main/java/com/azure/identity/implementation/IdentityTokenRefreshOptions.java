// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.credential.TokenRefreshOptions;

import java.time.Duration;

/**
 * The options to configure the token refresh behavior for azure-identity credentials.
 */
public class IdentityTokenRefreshOptions extends TokenRefreshOptions {
    private Duration offset = Duration.ofMinutes(2);

    @Override
    public Duration getOffset() {
        return offset;
    }

    /**
     * Sets the duration value representing the amount of time to subtract from the token expiry time.
     * @param offset the duration value representing the amount of time to subtract from the token expiry time
     */
    public void setOffset(Duration offset) {
        this.offset = offset;
    }
}
