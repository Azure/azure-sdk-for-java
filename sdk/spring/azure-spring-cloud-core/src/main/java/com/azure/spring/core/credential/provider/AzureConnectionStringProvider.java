package com.azure.spring.core.credential.provider;

import com.azure.spring.core.credential.AzureCredentialType;

import static com.azure.spring.core.credential.AzureCredentialType.CONNECTION_STRING_CREDENTIAL;

/**
 * Provide the azure key credential.
 */
public class AzureConnectionStringProvider implements AzureCredentialProvider<String> {

    private final String connectionString;

    public AzureConnectionStringProvider(String connectionString) {
        this.connectionString = connectionString;
    }

    @Override
    public AzureCredentialType getType() {
        return CONNECTION_STRING_CREDENTIAL;
    }

    @Override
    public String getCredential() {
        return this.connectionString;
    }
}
