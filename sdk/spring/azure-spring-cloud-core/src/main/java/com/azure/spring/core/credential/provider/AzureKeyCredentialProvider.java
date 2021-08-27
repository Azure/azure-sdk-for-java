package com.azure.spring.core.credential.provider;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.spring.core.credential.AzureCredentialType;

/**
 * Provide the azure key credential.
 */
public class AzureKeyCredentialProvider implements AzureCredentialProvider<AzureKeyCredential> {

    private final String key;

    public AzureKeyCredentialProvider(String key) {
        this.key = key;
    }

    @Override
    public AzureCredentialType getType() {
        return AzureCredentialType.KEY_CREDENTIAL;
    }

    @Override
    public AzureKeyCredential getCredential() {
        return new AzureKeyCredential(key);
    }
}
