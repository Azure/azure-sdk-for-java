// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.ClientCertificateBinding;
import com.microsoft.aad.msal4j.IAccount;
import com.microsoft.aad.msal4j.IAuthenticationResult;

import java.time.Instant;
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
        super(msalResult.accessToken(), getExpiresAt(msalResult), getRefreshAt(msalResult));
        authenticationResult = msalResult;
    }

    public MsalToken(IAuthenticationResult msalResult, String tokenType) {
        super(msalResult.accessToken(), getExpiresAt(msalResult), getRefreshAt(msalResult), tokenType);
        authenticationResult = msalResult;
    }

    /**
     * Creates an access token instance bound to a TLS client certificate, for mutual TLS (mTLS) proof-of-possession.
     *
     * @param msalResult the raw authentication result returned by MSAL
     * @param tokenType the type of the token, such as {@code mtls_pop}
     * @param clientCertificateBinding the client certificate the token is bound to
     */
    public MsalToken(IAuthenticationResult msalResult, String tokenType,
        ClientCertificateBinding clientCertificateBinding) {
        super(msalResult.accessToken(), getExpiresAt(msalResult), getRefreshAt(msalResult), tokenType,
            clientCertificateBinding);
        authenticationResult = msalResult;
    }

    private static OffsetDateTime getExpiresAt(IAuthenticationResult msalResult) {
        return OffsetDateTime.ofInstant(msalResult.expiresOnDate().toInstant(), ZoneOffset.UTC);
    }

    private static OffsetDateTime getRefreshAt(IAuthenticationResult msalResult) {
        return msalResult.metadata() == null || msalResult.metadata().refreshOn() == null
            ? null
            : OffsetDateTime.ofInstant(Instant.ofEpochSecond(msalResult.metadata().refreshOn()), ZoneOffset.UTC);
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
