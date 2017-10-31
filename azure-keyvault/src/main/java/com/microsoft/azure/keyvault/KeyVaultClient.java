package com.microsoft.azure.keyvault;

import com.microsoft.azure.keyvault.implementation.KeyVaultClientCustomImpl;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.credentials.ServiceClientCredentials;

public final class KeyVaultClient extends KeyVaultClientCustomImpl implements KeyVaultClientCustom {

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