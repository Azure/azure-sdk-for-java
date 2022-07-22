package com.azure.spring.cloud.autoconfigure.jdbc.provider;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ChainedTokenCredentialBuilder;
import com.azure.spring.cloud.autoconfigure.jdbc.CachedTokenCredential;
import com.azure.spring.cloud.autoconfigure.jdbc.resolver.NativeJdbcTokenCredentialResolver;

import java.util.Map;

public class CachedTokenCredentialProvider implements TokenCredentialProvider{

    private ChainedTokenCredentialBuilder chainedTokenCredentialBuilder;

    public CachedTokenCredentialProvider(Map<String, String> map) {
        TokenCredential tokenCredential = new NativeJdbcTokenCredentialResolver().resolve(map);
        if (tokenCredential != null) {
               // TODO TokenCredentialOptions
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

    public CachedTokenCredentialProvider addTokenCredentialFirst(TokenCredential tokenCredential) {
        getChainedTokenCredentialBuilder().addFirst(tokenCredential);
        return this;
    }

    public CachedTokenCredentialProvider addTokenCredentialLast(TokenCredential tokenCredential) {
        getChainedTokenCredentialBuilder().addLast(tokenCredential);
        return this;
    }

    private ChainedTokenCredentialBuilder getChainedTokenCredentialBuilder() {
        if (this.chainedTokenCredentialBuilder == null) {
            chainedTokenCredentialBuilder = new ChainedTokenCredentialBuilder();
        }
        return chainedTokenCredentialBuilder;
    }
    // customize chain
    // ChainedTokenCredential -> chained
    // TokenCredential specific

}
