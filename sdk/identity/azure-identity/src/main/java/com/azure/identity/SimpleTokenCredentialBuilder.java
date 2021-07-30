// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.implementation.util.IdentityConstants;
import com.azure.identity.implementation.util.ValidationUtil;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.function.Consumer;

/**
 * Fluent credential builder for instantiating a {@link SimpleTokenCredential}.
 *
 * @see SimpleTokenCredential
 */
public class SimpleTokenCredentialBuilder {
    private String accessTokenString;
    private AccessToken accessToken;

    /**
     * Sets the specified access token with default expiry time of 1 hour.
     *
     * @param accessToken the user specified access token.
     * @return the SimpleTokenCredentialBuilder itself
     */
    public SimpleTokenCredentialBuilder accessToken(String accessToken) {
        this.accessTokenString = accessToken;
        return this;
    }

    /**
     * Sets the specified access token.
     *
     * @param accessToken the user specified access token.
     * @return the SimpleTokenCredentialBuilder itself
     */
    public SimpleTokenCredentialBuilder accessToken(AccessToken accessToken) {
        this.accessToken = accessToken;
        return this;
    }


    /**
     * Creates a new {@link SimpleTokenCredential} with the current configurations.
     *
     * @return a {@link SimpleTokenCredential} with the current configurations.
     */
    public SimpleTokenCredential build() {
        ValidationUtil.validateAllEmpty(getClass().getSimpleName(), new HashMap<String, Object>() {{
            put("accessToken", accessToken);
            put("accessTokenString", accessTokenString);
        }});
        return new SimpleTokenCredential(accessTokenString, accessToken);
    }
}
