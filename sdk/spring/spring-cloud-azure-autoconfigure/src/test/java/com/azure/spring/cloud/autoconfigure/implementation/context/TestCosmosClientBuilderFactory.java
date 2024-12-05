// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.context;

import com.azure.core.credential.TokenCredential;
import com.azure.spring.cloud.core.credential.AzureCredentialResolver;
import com.azure.spring.cloud.service.implementation.cosmos.CosmosClientBuilderFactory;
import com.azure.spring.cloud.service.implementation.cosmos.CosmosClientProperties;

class TestCosmosClientBuilderFactory extends CosmosClientBuilderFactory {

    private TokenCredential defaultTokenCredential;
    private AzureCredentialResolver<TokenCredential> azureTokenCredentialResolver;

    TestCosmosClientBuilderFactory(CosmosClientProperties cosmosClientProperties) {
        super(cosmosClientProperties);
    }

    @Override
    public void setDefaultTokenCredential(TokenCredential defaultTokenCredential) {
        this.defaultTokenCredential = defaultTokenCredential;
    }

    @Override
    public void setTokenCredentialResolver(AzureCredentialResolver<TokenCredential> tokenCredentialResolver) {
        this.azureTokenCredentialResolver = tokenCredentialResolver;
    }

    TokenCredential getDefaultTokenCredential() {
        return defaultTokenCredential;
    }

    AzureCredentialResolver<TokenCredential> getAzureTokenCredentialResolver() {
        return azureTokenCredentialResolver;
    }
}
