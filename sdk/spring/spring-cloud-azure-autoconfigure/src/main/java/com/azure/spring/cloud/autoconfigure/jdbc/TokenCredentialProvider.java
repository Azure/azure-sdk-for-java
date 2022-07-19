package com.azure.spring.cloud.autoconfigure.jdbc;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ChainedTokenCredentialBuilder;
import com.azure.spring.cloud.autoconfigure.implementation.jdbc.AzureJDBCPropertiesUtils;
import java.util.Map;


public class TokenCredentialProvider {

    private ChainedTokenCredentialBuilder chainedTokenCredentialBuilder;

    public TokenCredentialProvider() {
        this.chainedTokenCredentialBuilder = new ChainedTokenCredentialBuilder();
    }

    public TokenCredentialProvider(Map<String, String> map, boolean cached) {
        this.chainedTokenCredentialBuilder = new ChainedTokenCredentialBuilder();
        TokenCredential tokenCredential = AzureJDBCPropertiesUtils.resolveTokenCredential(map);

        if (tokenCredential != null) {
            if (cached) {
                chainedTokenCredentialBuilder.addFirst(new CachedTokenCredential(tokenCredential));
            } else {
                chainedTokenCredentialBuilder.addFirst(tokenCredential);
            }
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
