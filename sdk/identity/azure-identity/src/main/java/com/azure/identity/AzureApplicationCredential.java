// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.annotation.Immutable;
import com.azure.core.credential.TokenCredential;

import java.util.List;

/**
 * Creates a credential using environment variables for Azure hosted Environments. It tries to create a valid credential
 * in the following order:
 *
 * <ol>
 * <li>{@link EnvironmentCredential}</li>
 * <li>{@link ManagedIdentityCredential}</li>
 * <li>Fails if none of the credentials above could be created.</li>
 * </ol>
 */
@Immutable
public final class AzureApplicationCredential extends ChainedTokenCredential {
    /**
     * Creates default AzureApplicationCredential instance to use. This will use environment variables to create
     * {@link EnvironmentCredential}
     *
     * If these environment variables are not available, then this will attempt to use Managed Identity Authentication
     * via {@link ManagedIdentityCredential}.
     *
     * @param tokenCredentials the list of credentials to execute for authentication.
     */
    AzureApplicationCredential(List<TokenCredential> tokenCredentials) {
        super(tokenCredentials);
    }
}
