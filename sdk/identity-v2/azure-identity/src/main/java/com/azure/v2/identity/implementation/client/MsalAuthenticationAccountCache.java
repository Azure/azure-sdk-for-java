// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.identity.implementation.client;

import com.azure.v2.core.credentials.TokenRequestContext;
import com.azure.v2.identity.implementation.models.MsalAuthenticationAccount;
import com.azure.v2.identity.implementation.models.MsalToken;
import com.azure.v2.identity.implementation.models.PublicClientOptions;
import com.azure.v2.identity.models.AuthenticationRecord;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import io.clientcore.core.credentials.oauth.AccessToken;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Msal Authentication Account Cache offers APIs to manage and read cache for Public client brokered auth flows.
 */
public class MsalAuthenticationAccountCache {
    private final AtomicReference<MsalAuthenticationAccount> cachedToken;
    private boolean isCaeEnabledRequestCached;
    private boolean isCaeDisabledRequestCached;

    /**
     * Creates an instance of Msal Authentication Account Cache.
     */
    public MsalAuthenticationAccountCache() {
        cachedToken = new AtomicReference<>();
    }

    /**
     * Updates the cache using the provided msal token.
     *
     * @param msalToken the msal token
     * @param publicClientOptions the public client options
     * @param tokenRequestContext the token request context
     * @return the access token.
     */
    public AccessToken updateCache(MsalToken msalToken, PublicClientOptions publicClientOptions,
        TokenRequestContext tokenRequestContext) {
        IAuthenticationResult authenticationResult = msalToken.getAuthenticationResult();
        cachedToken.set(new MsalAuthenticationAccount(
            new AuthenticationRecord(authenticationResult.account().environment(),
                authenticationResult.account().homeAccountId(), authenticationResult.account().username(),
                publicClientOptions.getTenantId(), publicClientOptions.getClientId()),
            msalToken.getAccount().getTenantProfiles()));
        if (tokenRequestContext.isCaeEnabled()) {
            isCaeEnabledRequestCached = true;
        } else {
            isCaeDisabledRequestCached = true;
        }
        return msalToken;
    }

    /**
     * Checks if the cache is populated to serve the given token request context.
     *
     * @param request the token request context
     * @return the boolean flag indicating if the cache is populated or not.
     */
    public boolean isCachePopulated(TokenRequestContext request) {
        return (cachedToken.get() != null)
            && ((request.isCaeEnabled() && isCaeEnabledRequestCached)
                || (!request.isCaeEnabled() && isCaeDisabledRequestCached));
    }

    /**
     * Gets the cached msal account.
     *
     * @return the msal auth account
     */
    public MsalAuthenticationAccount getCachedAccount() {
        return cachedToken.get();
    }

    /**
     * Sets the cache to the given msal account.
     *
     * @param account the msal account
     * @return the updated cache.
     */
    public MsalAuthenticationAccountCache setCachedAccount(MsalAuthenticationAccount account) {
        cachedToken.set(account);
        return this;
    }
}
