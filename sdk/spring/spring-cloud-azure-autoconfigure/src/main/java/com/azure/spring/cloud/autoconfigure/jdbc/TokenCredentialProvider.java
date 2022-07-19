package com.azure.spring.cloud.autoconfigure.jdbc;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ChainedTokenCredentialBuilder;
import com.azure.spring.cloud.core.implementation.credential.resolver.AzureTokenCredentialResolver;
import com.azure.spring.cloud.core.properties.AzureProperties;

public class TokenCredentialProvider {

    private ChainedTokenCredentialBuilder chainedTokenCredentialBuilder;


    public TokenCredentialProvider() {
        this.chainedTokenCredentialBuilder = new ChainedTokenCredentialBuilder();
    }

    public TokenCredentialProvider(AzureProperties azureProperties) {
        this.chainedTokenCredentialBuilder = new ChainedTokenCredentialBuilder();
        AzureTokenCredentialResolver tokenCredentialResolver = new AzureTokenCredentialResolver();
        TokenCredential tokenCredential = tokenCredentialResolver.resolve(azureProperties);

        if (tokenCredential != null) {
            chainedTokenCredentialBuilder.addFirst(tokenCredential);
        }
    }

    public TokenCredential getTokenCredential() {
        return chainedTokenCredentialBuilder.build();
    }

    public TokenCredentialProvider addTokenCredentialFirst(TokenCredential tokenCredential) {
        chainedTokenCredentialBuilder.addFirst(tokenCredential);
        return this;
    }


    public TokenCredentialProvider addTokenCredentialLast(TokenCredential tokenCredential) {
        chainedTokenCredentialBuilder.addLast(tokenCredential);
        return this;
    }
    // customize chain
    // ChainedTokenCredential -> chained
    // TokenCredential specific


}
