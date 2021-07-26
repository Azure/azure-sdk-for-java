// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.implementation.util.IdentityConstants;

import java.time.OffsetDateTime;
import java.util.function.Consumer;

/**
 * Fluent credential builder for instantiating a {@link SimpleTokenCredential}.
 *
 * @see SimpleTokenCredential
 */
public class SimpleTokenCredentialBuilder {
    private String accessToken;
    private OffsetDateTime tokenExpiryTime;

    /**
     * Sets the consumer to meet the device code challenge. If not specified a default consumer is used which prints
     * the device code info message to stdout.
     *
     * @param accessToken the user specified access token.
     * @return the SimpleTokenCredentialBuilder itself
     */
    public SimpleTokenCredentialBuilder accessToken(String accessToken) {
        this.accessToken = accessToken;
        return this;
    }

    /**
     * Sets the custom token expiry time for the specified access token.
     *
     * @param tokenExpiryTime the expiry time of specified access token..
     * @return the SimpleTokenCredentialBuilder itself
     */
    public SimpleTokenCredentialBuilder tokenExpiryTime(OffsetDateTime tokenExpiryTime) {
        this.tokenExpiryTime = tokenExpiryTime;
        return this;
    }


    /**
     * Creates a new {@link SimpleTokenCredential} with the current configurations.
     *
     * @return a {@link SimpleTokenCredential} with the current configurations.
     */
    public SimpleTokenCredential build() {
        return new SimpleTokenCredential(accessToken, tokenExpiryTime);
    }
}
