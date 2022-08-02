package com.azure.spring.cloud.service.implementation.identity.api.credential;

import com.azure.core.credential.TokenCredential;
import com.azure.spring.cloud.service.implementation.identity.impl.credential.provider.TokenCredentialProviders;

import java.util.function.Supplier;

@FunctionalInterface
public interface TokenCredentialProvider extends Supplier<TokenCredential> {

    default TokenCredential get(TokenCredentialProviderOptions options) {
        return get();
    }

    static TokenCredentialProvider createDefault(TokenCredentialProviderOptions options) {
        return TokenCredentialProviders.createInstance(options);
    }
}
