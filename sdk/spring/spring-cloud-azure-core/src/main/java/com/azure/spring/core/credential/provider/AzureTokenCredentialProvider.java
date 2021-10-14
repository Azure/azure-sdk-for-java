// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.credential.provider;

import com.azure.core.credential.TokenCredential;
import com.azure.spring.core.credential.AzureCredentialType;

/**
 * Provide the azure token credential.
 */
public class AzureTokenCredentialProvider implements AzureCredentialProvider<TokenCredential> {

    private final TokenCredential tokenCredential;

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
