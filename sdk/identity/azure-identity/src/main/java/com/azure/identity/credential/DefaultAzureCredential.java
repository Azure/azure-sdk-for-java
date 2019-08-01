// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.credential;

import com.azure.identity.IdentityClientOptions;

/**
 * The base class for a token credential to be used in an Azure client library.
 */
public final class DefaultAzureCredential extends ChainedTokenCredential {

    /**
     * Creates default DefaultAzureCredential instance to use. This will use AZURE_CLIENT_ID,
     * AZURE_CLIENT_SECRET, and AZURE_TENANT_ID environment variables to create a
     * ClientSecretCredential.
     */
    public DefaultAzureCredential() {
        this(new IdentityClientOptions());
    }

    /**
     * Creates default DefaultAzureCredential instance to use. This will use AZURE_CLIENT_ID,
     * AZURE_CLIENT_SECRET, and AZURE_TENANT_ID environment variables to create a
     * ClientSecretCredential.
     *
     * @param identityClientOptions the options to configure the IdentityClient
     */
    public DefaultAzureCredential(IdentityClientOptions identityClientOptions) {
        super();
        addLast(new EnvironmentCredential(identityClientOptions));
        addLast(new ManagedIdentityCredential(identityClientOptions));
    }
}
