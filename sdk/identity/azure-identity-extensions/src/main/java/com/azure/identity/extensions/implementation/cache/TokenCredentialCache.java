package com.azure.identity.extensions.implementation.cache;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.extensions.implementation.credential.TokenCredentialProviderOptions;

public interface TokenCredentialCache {

    void put(TokenCredentialProviderOptions options, TokenCredential value);

    TokenCredential get(TokenCredentialProviderOptions options);

    void remove(TokenCredentialProviderOptions options);

    default String getKey(TokenCredentialProviderOptions options) {
        // todo implement the key generation
        return "";
    }
}
