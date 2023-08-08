// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.credential.AccessToken;
import com.microsoft.aad.msal4j.IAccount;
import com.microsoft.aad.msal4j.IAuthenticationResult;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Type representing authentication result from the MSAL (Microsoft Authentication Library).
 */
public final class MsalToken extends AccessToken {

    private IAuthenticationResult authenticationResult;

    /**
     * Creates an access token instance.
     *
     * @param msalResult the raw authentication result returned by MSAL
     */
    public MsalToken(IAuthenticationResult msalResult) {
        super(msalResult.accessToken(),
                OffsetDateTime.ofInstant(msalResult.expiresOnDate().toInstant(), ZoneOffset.UTC));
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
