package com.azure.identity.credential;

import com.azure.identity.IdentityClientOptions;

/**
 * The base class for a token credential to be used in an Azure client library.
 */
public final class AzureCredential extends ChainedCredential {

    /**
     * Creates default AzureCredential instance to use. This will use AZURE_CLIENT_ID,
     * AZURE_CLIENT_SECRET, and AZURE_TENANT_ID environment variables to create a
     * ClientSecretCredential.
     */
    public AzureCredential() {
        this(new IdentityClientOptions());
    }

    /**
     * Creates default AzureCredential instance to use. This will use AZURE_CLIENT_ID,
     * AZURE_CLIENT_SECRET, and AZURE_TENANT_ID environment variables to create a
     * ClientSecretCredential.
     *
     * @param identityClientOptions the options to configure the IdentityClient
     */
    public AzureCredential(IdentityClientOptions identityClientOptions) {
        super();
        addLast(new EnvironmentCredential(identityClientOptions));
    }
}
