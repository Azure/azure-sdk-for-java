package com.azure.identity.extensions.implementation.utils;

import com.azure.identity.extensions.implementation.credential.TokenCredentialProviderOptions;
import com.azure.identity.extensions.implementation.token.AccessTokenResolverOptions;

import java.util.Arrays;
import java.util.stream.Collectors;

public class StringUtils {

    private StringUtils() {

    }

    public static String getTokenCredentialCacheKey(TokenCredentialProviderOptions options) {
        return joinOptions(options.getTenantId(), options.getClientId(), options.getClientCertificatePath(),
            options.getUsername(), String.valueOf(options.isManagedIdentityEnabled()),
            options.getTokenCredentialProviderClassName(), options.getTokenCredentialBeanName(),
            options.getTokenCredentialCacheClassName());
    }

    public static String getAccessTokenCacheKey(AccessTokenResolverOptions options) {
        return joinOptions(options.getTenantId(), options.getClaims(), String.join("-", options.getScopes()));
    }

    private static String joinOptions(String... options) {
        return Arrays.stream(options).map(StringUtils::nonNullOption).collect(Collectors.joining(","));
    }

    private static String nonNullOption(String option) {
        return option == null ? "" : option;
    }
}
