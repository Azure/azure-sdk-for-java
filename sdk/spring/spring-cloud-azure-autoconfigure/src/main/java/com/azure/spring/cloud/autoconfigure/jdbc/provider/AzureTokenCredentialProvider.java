package com.azure.spring.cloud.autoconfigure.jdbc.provider;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ChainedTokenCredentialBuilder;
import com.azure.spring.cloud.autoconfigure.jdbc.AzureJdbcProperties;
import com.azure.spring.cloud.autoconfigure.jdbc.AzureJdbcPropertiesUtils;
import com.azure.spring.cloud.autoconfigure.jdbc.nativejdbc.CachedTokenCredential;
import com.azure.spring.cloud.autoconfigure.jdbc.nativejdbc.provider.TokenCredentialProvider;
import com.azure.spring.cloud.core.implementation.credential.resolver.AzureTokenCredentialResolver;

import java.util.Map;

public class AzureTokenCredentialProvider implements TokenCredentialProvider {

    private ChainedTokenCredentialBuilder chainedTokenCredentialBuilder;

    public AzureTokenCredentialProvider(Map<String, String> map) {
        AzureJdbcProperties azureJdbcProperties = new AzureJdbcProperties();
        AzureJdbcPropertiesUtils.convertConfigMapToAzureProperties(map, azureJdbcProperties);
        TokenCredential tokenCredential = new AzureTokenCredentialResolver().resolve(azureJdbcProperties);
        if (tokenCredential != null) {
              getChainedTokenCredentialBuilder().addFirst(new CachedTokenCredential(tokenCredential));
        }
    }

    @Override
    public TokenCredential getTokenCredential() {
        if (chainedTokenCredentialBuilder == null) {
            return null;
        }
        return chainedTokenCredentialBuilder.build();
    }

    public TokenCredentialProvider addTokenCredentialFirst(TokenCredential tokenCredential) {
        getChainedTokenCredentialBuilder().addFirst(tokenCredential);
        return this;
    }

    public TokenCredentialProvider addTokenCredentialLast(TokenCredential tokenCredential) {
        getChainedTokenCredentialBuilder().addLast(tokenCredential);
        return this;
    }

    private ChainedTokenCredentialBuilder getChainedTokenCredentialBuilder() {
        if (this.chainedTokenCredentialBuilder == null) {
            chainedTokenCredentialBuilder = new ChainedTokenCredentialBuilder();
        }
        return chainedTokenCredentialBuilder;
    }
}
