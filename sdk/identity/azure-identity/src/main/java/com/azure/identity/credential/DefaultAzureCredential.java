// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.credential;

import com.azure.core.implementation.annotation.Immutable;
import com.azure.identity.implementation.IdentityClientOptions;

import java.util.ArrayDeque;
import java.util.Arrays;

/**
 * The base class for a token credential to be used in an Azure client library.
 */
@Immutable
public final class DefaultAzureCredential extends ChainedTokenCredential {

    /**
     * Creates default DefaultAzureCredential instance to use. This will use AZURE_CLIENT_ID,
     * AZURE_CLIENT_SECRET, and AZURE_TENANT_ID environment variables to create a
     * ClientSecretCredential.
     *
     * @param identityClientOptions the options to configure the IdentityClient
     */
    DefaultAzureCredential(IdentityClientOptions identityClientOptions) {
        super(new ArrayDeque<>(Arrays.asList(new EnvironmentCredential(identityClientOptions), new ManagedIdentityCredential(null, identityClientOptions))));
    }
}
