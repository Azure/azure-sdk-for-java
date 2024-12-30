// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.extensions.implementation.cache;

import com.azure.identity.extensions.implementation.utils.ClassUtil;

import static com.azure.identity.extensions.implementation.utils.ClassUtil.instantiateClass;

public final class TokenCredentialCacheHelper {

    private static Class<? extends TokenCredentialCache> defaultCacheClass = InMemoryTokenCredentialCache.class;

    private TokenCredentialCacheHelper() {

    }

    public static TokenCredentialCache createInstance() {
        return createInstance(null);
    }

    public static TokenCredentialCache createInstance(String cacheClassName) {
        if (cacheClassName == null || cacheClassName.isEmpty()) {
            return new InMemoryTokenCredentialCache();
        }

        Class<? extends TokenCredentialCache> clazz
            = ClassUtil.getClass(cacheClassName, TokenCredentialCache.class);
        if (clazz == null) {
            clazz = defaultCacheClass;
        }

        return instantiateClass(clazz);
    }

    public static void setDefaultCacheClass(Class<? extends TokenCredentialCache> clazz) {
        defaultCacheClass = clazz;
    }
}
