// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.identity.impl.credential.provider;

import com.azure.spring.cloud.service.implementation.identity.api.credential.TokenCredentialProvider;
import com.azure.spring.cloud.service.implementation.identity.api.credential.TokenCredentialProviderOptions;
import com.azure.spring.cloud.service.implementation.identity.impl.utils.ClassUtil;

import static com.azure.spring.cloud.service.implementation.identity.impl.utils.ClassUtil.instantiateClass;

public class TokenCredentialProviders {

    private static Class<? extends TokenCredentialProvider> defaultProviderClass = DefaultTokenCredentialProvider.class;

    private TokenCredentialProviders() {

    }

    public static TokenCredentialProvider createInstance() {
        return createInstance(null);
    }

    /**
     * 1. get provider impl class name from options
     * 2. if not, use the default one
     * 3. create instance of the class
     * 4. config the instance with options
     * 5. return the instance
     */
    public static TokenCredentialProvider createInstance(TokenCredentialProviderOptions options) {
        if (options == null) {
            options = new TokenCredentialProviderOptions();
        }

        Class<? extends TokenCredentialProvider> clazz = ClassUtil.getClass(options.getTokenCredentialProviderClassName(), TokenCredentialProvider.class);
        if (clazz == null) {
            clazz = defaultProviderClass;
        }

        return instantiateClass(clazz, options);
    }

    public static void setDefaultProviderClass(Class<? extends TokenCredentialProvider> clazz) {
        defaultProviderClass = clazz;
    }

}
