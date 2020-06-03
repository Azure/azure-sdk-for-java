// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.credential.AccessToken;

import java.time.OffsetDateTime;

/**
 * Type representing authentication result from the azure-identity client.
 */
public class IdentityToken extends AccessToken {
    /**
     * Creates an identity token instance.
     *
     * @param token the token string.
     * @param expiresAt the expiration time.
     */
    public IdentityToken(String token, OffsetDateTime expiresAt) {
        super(token, expiresAt.plusMinutes(2));
    }
}
