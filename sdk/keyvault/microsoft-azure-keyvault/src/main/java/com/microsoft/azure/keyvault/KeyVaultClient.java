// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.keyvault;

import com.microsoft.azure.keyvault.implementation.KeyVaultClientCustomImpl;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.credentials.ServiceClientCredentials;

/**
 * Class for Key Vault Client.
 * 
 */
public final class KeyVaultClient extends KeyVaultClientCustomImpl {

    /**
     * Initializes an instance of KeyVaultClient client.
     *
     * @param credentials the management credentials for Azure
     */
    public KeyVaultClient(ServiceClientCredentials credentials) {
        super(credentials);
        initializeService();
    }
    /**
     * Initializes an instance of KeyVaultClient client.
     *
     * @param restClient the REST client to connect to Azure.
     */
    public KeyVaultClient(RestClient restClient) {
        super(restClient);
        initializeService();
    }
}
