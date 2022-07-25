package com.azure.spring.cloud.autoconfigure.jdbc.nativejdbc.provider;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ChainedTokenCredentialBuilder;
import com.azure.spring.cloud.autoconfigure.jdbc.nativejdbc.CachedTokenCredential;
import com.azure.spring.cloud.autoconfigure.jdbc.nativejdbc.resolver.NativeJdbcTokenCredentialResolver;

import java.util.Map;

public class JdbcTokenCredentialProvider implements TokenCredentialProvider{

    private ChainedTokenCredentialBuilder chainedTokenCredentialBuilder;

    public JdbcTokenCredentialProvider(Map<String, String> map) {
        TokenCredential tokenCredential = new NativeJdbcTokenCredentialResolver().resolve(map);
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

    public JdbcTokenCredentialProvider addTokenCredentialFirst(TokenCredential tokenCredential) {
        getChainedTokenCredentialBuilder().addFirst(tokenCredential);
        return this;
    }

    public JdbcTokenCredentialProvider addTokenCredentialLast(TokenCredential tokenCredential) {
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
