package com.azure.identity.extensions.implementation.cache;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.extensions.implementation.credential.TokenCredentialProviderOptions;

import java.util.Arrays;
import java.util.stream.Collectors;

public interface TokenCredentialCache {

    void put(String key, TokenCredential value);

    TokenCredential get(String key);

    void remove(String key);

    default String getKey(TokenCredentialProviderOptions options) {
        return joinOptions(options.getTenantId(), options.getClientId(), options.getClientCertificatePath(),
            options.getUsername(), String.valueOf(options.isManagedIdentityEnabled()),
            options.getTokenCredentialProviderClassName(), options.getTokenCredentialBeanName(),
            options.getTokenCredentialCacheClassName());
    }

    static String joinOptions(String... options) {
        return Arrays.stream(options).map(TokenCredentialCache::nonNullOption).collect(Collectors.joining(","));
    }

    static String nonNullOption(String option) {
        return option == null ? "" : option;
    }
}
