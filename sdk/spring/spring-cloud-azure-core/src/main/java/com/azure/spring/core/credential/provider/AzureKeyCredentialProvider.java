// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.credential.provider;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.spring.core.credential.AzureCredentialType;

/**
 * Provide the azure key credential.
 */
public final class AzureKeyCredentialProvider implements AzureCredentialProvider<AzureKeyCredential> {

    private final String key;

    /**
     * Create a {@link AzureNamedKeyCredentialProvider} instance with the Key Credential of value {@code key}.
     * @param key The key Credential.
     */
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
