// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.identity.implementation.models;

import com.microsoft.aad.msal4j.IAccount;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import io.clientcore.core.credentials.oauth.AccessToken;
import io.clientcore.core.credentials.oauth.AccessTokenType;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Type representing authentication result from the MSAL (Microsoft Authentication Library).
 */
public final class MsalToken extends AccessToken {

    private final IAuthenticationResult authenticationResult;

    /**
     * Creates an access token instance.
     *
     * @param authenticationResult the raw authentication result returned by MSAL
     */
    public MsalToken(IAuthenticationResult authenticationResult) {
        super(authenticationResult.accessToken(),
            OffsetDateTime.ofInstant(authenticationResult.expiresOnDate().toInstant(), ZoneOffset.UTC),
            authenticationResult.metadata() != null
                ? authenticationResult.metadata().refreshOn() == null
                    ? null
                    : OffsetDateTime.ofInstant(Instant.ofEpochSecond(authenticationResult.metadata().refreshOn()),
                        ZoneOffset.UTC)
                : null);
        this.authenticationResult = authenticationResult;
    }

    /**
     * Creates an instance of Msal Token.
     *
     * @param msalResult the authentication result
     * @param tokenType the type of token
     */
    public MsalToken(IAuthenticationResult msalResult, String tokenType) {
        super(msalResult.accessToken(),
            OffsetDateTime.ofInstant(msalResult.expiresOnDate().toInstant(), ZoneOffset.UTC),
            msalResult.metadata() != null
                ? msalResult.metadata().refreshOn() == null
                    ? null
                    : OffsetDateTime.ofInstant(Instant.ofEpochSecond(msalResult.metadata().refreshOn()), ZoneOffset.UTC)
                : null,
            AccessTokenType.fromString(tokenType));
        authenticationResult = msalResult;
    }

    /**
     * @return the signed in account
     */
    public IAccount getAccount() {
        return authenticationResult.account();
    }

    /**
     * Get the MSAL Authentication result.
     *
     * @return the authentication result.
     */
    public IAuthenticationResult getAuthenticationResult() {
        return authenticationResult;
    }
}
