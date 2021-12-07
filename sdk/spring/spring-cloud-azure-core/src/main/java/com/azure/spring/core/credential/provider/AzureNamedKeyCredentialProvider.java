// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.credential.provider;

import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.spring.core.credential.AzureCredentialType;

/**
 * Provide the azure named key credential.
 */
public final class AzureNamedKeyCredentialProvider implements AzureCredentialProvider<AzureNamedKeyCredential> {

    private final String name;
    private final String key;

    /**
     * Create a {@link AzureNamedKeyCredentialProvider} instance with the Named Key Credential of {@code name} and
     * {@code key}.
     * @param name The name of the Named Key Credential.
     * @param key The key of the Named Key Credential.
     */
    public AzureNamedKeyCredentialProvider(String name, String key) {
        this.name = name;
        this.key = key;
    }

    @Override
    public AzureCredentialType getType() {
        return AzureCredentialType.NAMED_KEY_CREDENTIAL;
    }

    @Override
    public AzureNamedKeyCredential getCredential() {
        return new AzureNamedKeyCredential(name, key);
    }
}
