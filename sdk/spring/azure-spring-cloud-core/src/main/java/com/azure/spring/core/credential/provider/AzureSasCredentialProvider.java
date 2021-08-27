package com.azure.spring.core.credential.provider;

import com.azure.core.credential.AzureSasCredential;
import com.azure.spring.core.credential.AzureCredentialType;

/**
 * Provide the azure sas credential.
 */
public class AzureSasCredentialProvider implements AzureCredentialProvider<AzureSasCredential> {

    private final String sasToken;

    public AzureSasCredentialProvider(String sasToken) {
        this.sasToken = sasToken;
    }

    @Override
    public AzureCredentialType getType() {
        return AzureCredentialType.SAS_CREDENTIAL;
    }

    @Override
    public AzureSasCredential getCredential() {
        return new AzureSasCredential(sasToken);
    }
}
