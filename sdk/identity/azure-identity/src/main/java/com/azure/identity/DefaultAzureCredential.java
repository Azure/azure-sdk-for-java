// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.annotation.Immutable;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRefreshOptions;
import com.azure.identity.implementation.IdentityClientOptions;

import java.util.List;

/**
 * Creates a credential using environment variables or the shared token cache. It tries to create a valid credential in
 * the following order:
 *
 * <ol>
 * <li>{@link EnvironmentCredential}</li>
 * <li>{@link ManagedIdentityCredential}</li>
 * <li>{@link SharedTokenCacheCredential}</li>
 * <li>{@link AzureCliCredential}</li>
 * <li>Fails if none of the credentials above could be created.</li>
 * </ol>
 */
@Immutable
public final class DefaultAzureCredential extends ChainedTokenCredential {
    private final IdentityClientOptions identityClientOptions;

    /**
     * Creates default DefaultAzureCredential instance to use. This will use AZURE_CLIENT_ID,
     * AZURE_CLIENT_SECRET, and AZURE_TENANT_ID environment variables to create a
     * ClientSecretCredential.
     *
     * If these environment variables are not available, then this will use the Shared MSAL
     * token cache.
     *
     * @param tokenCredentials the list of credentials to execute for authentication.
     * @param identityClientOptions the options for configuring the identity client.
     */
    DefaultAzureCredential(List<TokenCredential> tokenCredentials, IdentityClientOptions identityClientOptions) {
        super(tokenCredentials);
        this.identityClientOptions = identityClientOptions;
    }

    @Override
    public TokenRefreshOptions getTokenRefreshOptions() {
        return identityClientOptions.getTokenRefreshOptions();
    }


    /**
     * {@inheritDoc}
     * The credentials in the returned list and their order may change in future versions of Identity.
     * This API is not intended to be used in production ready code and should only be used for development purposes.
     *
     * @return The list of {@link TokenCredential}.
     */
    public List<TokenCredential> getCredentials() {
        return super.getCredentials();
    }
}
