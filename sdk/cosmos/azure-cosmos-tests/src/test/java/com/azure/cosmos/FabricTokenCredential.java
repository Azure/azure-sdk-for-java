package com.azure.cosmos;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.InteractiveBrowserCredentialBuilder;
import reactor.core.publisher.Mono;

public class FabricTokenCredential implements TokenCredential {

    private TokenCredential tokenCredential;

    public FabricTokenCredential() {
        tokenCredential = new InteractiveBrowserCredentialBuilder()
            .authorityHost("")
            .build();
    }

    @Override
    public Mono<AccessToken> getToken(TokenRequestContext tokenRequestContext) {
        return tokenCredential.getToken(new TokenRequestContext().addScopes("https://cosmos.azure.com/.default"));
    }

    @Override
    public AccessToken getTokenSync(TokenRequestContext request) {
        return tokenCredential.getTokenSync(new TokenRequestContext().addScopes("https://cosmos.azure.com/.default"));
    }
}
