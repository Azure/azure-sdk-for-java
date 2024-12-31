// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.extensions.implementation.cache;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.identity.extensions.implementation.utils.ClassUtil;

import static com.azure.identity.extensions.implementation.utils.ClassUtil.instantiateClass;

public final class IdentityCacheHelper {

    private static Class<? extends IdentityCache<String, TokenCredential>> DEFAULT_TOKEN_CREDENTIAL_CACHE_CLASS = InMemoryTokenCredentialCache.class;
    private static Class<? extends IdentityCache<String, AccessToken>> DEFAULT_ACCESS_TOKEN_CACHE_CLASS = InMemoryAccessTokenCache.class;

    private IdentityCacheHelper() {

    }

    public static IdentityCache<String, TokenCredential> createTokenCredentialCacheInstance() {
        return createTokenCredentialCacheInstance(null);
    }

    public static IdentityCache<String, TokenCredential> createTokenCredentialCacheInstance(String cacheClassName) {
        Class<? extends IdentityCache<String, TokenCredential>> clazz = ClassUtil.getClass(cacheClassName, IdentityCache.class);
        if (clazz == null) {
            clazz = DEFAULT_TOKEN_CREDENTIAL_CACHE_CLASS;
        }

        return instantiateClass(clazz);
    }

    public static IdentityCache<String, AccessToken> createAccessTokenCacheInstance() {
        return createAccessTokenCacheInstance(null);
    }

    public static IdentityCache<String, AccessToken> createAccessTokenCacheInstance(String cacheClassName) {
        Class<? extends IdentityCache<String, AccessToken>> clazz = ClassUtil.getClass(cacheClassName, IdentityCache.class);
        if (clazz == null) {
            clazz = DEFAULT_ACCESS_TOKEN_CACHE_CLASS;
        }

        return instantiateClass(clazz);
    }

    public static void setDefaultTokenCredentialCacheClass(Class<? extends IdentityCache<String, TokenCredential>> clazz) {
        DEFAULT_TOKEN_CREDENTIAL_CACHE_CLASS = clazz;
    }

    public static void setDefaultAccessTokenCacheClass(Class<? extends IdentityCache<String, AccessToken>> clazz) {
        DEFAULT_ACCESS_TOKEN_CACHE_CLASS = clazz;
    }
}
