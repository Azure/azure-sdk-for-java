// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.credential.provider;

import com.azure.core.credential.TokenCredential;
import com.azure.spring.core.credential.AzureCredentialType;

/**
 * Provide the azure token credential.
 */
public final class AzureTokenCredentialProvider implements AzureCredentialProvider<TokenCredential> {

    private final TokenCredential tokenCredential;

    /**
     * Create a {@link AzureTokenCredentialProvider} of with the token credential.
     * @param tokenCredential The token credential.
     */
    public AzureTokenCredentialProvider(TokenCredential tokenCredential) {
        this.tokenCredential = tokenCredential;
    }

    @Override
    public AzureCredentialType getType() {
        return AzureCredentialType.TOKEN_CREDENTIAL;
    }

    @Override
    public TokenCredential getCredential() {
        return this.tokenCredential;
    }
}
