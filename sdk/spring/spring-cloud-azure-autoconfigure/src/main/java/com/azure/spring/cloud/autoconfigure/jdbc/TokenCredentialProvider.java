package com.azure.spring.cloud.autoconfigure.jdbc;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ChainedTokenCredentialBuilder;
import com.azure.spring.cloud.autoconfigure.implementation.jdbc.AzureJDBCPropertiesUtils;
import java.util.Map;


public class TokenCredentialProvider {

    private ChainedTokenCredentialBuilder chainedTokenCredentialBuilder;
    private TokenCredential tokenCredential;

    public TokenCredentialProvider(Map<String, String> map, boolean cached) {
        TokenCredential resolvedTokenCredential = AzureJDBCPropertiesUtils.resolveTokenCredential(map);

        if (resolvedTokenCredential != null) {
            if (cached) {
                tokenCredential = new CachedTokenCredential(resolvedTokenCredential);
            } else {
                tokenCredential = resolvedTokenCredential;
            }
        }
    }

    public TokenCredential getTokenCredential() {
        if (chainedTokenCredentialBuilder == null) {
            return tokenCredential;
        } else {
            return chainedTokenCredentialBuilder.build();
        }
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
        if (chainedTokenCredentialBuilder == null) {
            chainedTokenCredentialBuilder = new ChainedTokenCredentialBuilder();
            if (tokenCredential != null) {
                chainedTokenCredentialBuilder.addFirst(tokenCredential);
            }
        }
        return chainedTokenCredentialBuilder;
    }
    // customize chain
    // ChainedTokenCredential -> chained
    // TokenCredential specific

}
