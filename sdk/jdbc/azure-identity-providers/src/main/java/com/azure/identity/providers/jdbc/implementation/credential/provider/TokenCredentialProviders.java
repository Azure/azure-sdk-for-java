// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.providers.jdbc.implementation.credential.provider;

import com.azure.identity.providers.jdbc.implementation.credential.TokenCredentialProviderOptions;
import com.azure.identity.providers.jdbc.implementation.utils.ClassUtil;
import static com.azure.identity.providers.jdbc.implementation.utils.ClassUtil.instantiateClass;

/**
 * Util class to provide TokenCredentialProvider.
 */
public final class TokenCredentialProviders {

    private static Class<? extends TokenCredentialProvider> defaultProviderClass = DefaultTokenCredentialProvider.class;

    private TokenCredentialProviders() {

    }

    public static TokenCredentialProvider createInstance() {
        return createInstance(null);
    }

    /**
     * Get TokenCredentialProvider instance from options.
     *
     * @param options Options to create TokenCredentialProvider instance.
     * @return TokenCredentialProvider instance.
     */
    public static TokenCredentialProvider createInstance(TokenCredentialProviderOptions options) {
        if (options == null) {
            options = new TokenCredentialProviderOptions();
        }

        Class<? extends TokenCredentialProvider> clazz
            = ClassUtil.getClass(options.getTokenCredentialProviderClassName(), TokenCredentialProvider.class);
        if (clazz == null) {
            clazz = defaultProviderClass;
        }

        return instantiateClass(clazz, options);
    }

    public static void setDefaultProviderClass(Class<? extends TokenCredentialProvider> clazz) {
        defaultProviderClass = clazz;
    }

}
