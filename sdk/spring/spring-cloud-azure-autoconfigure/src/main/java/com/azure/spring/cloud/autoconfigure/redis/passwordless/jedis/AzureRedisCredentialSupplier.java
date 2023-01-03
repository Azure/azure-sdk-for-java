// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.redis.passwordless.jedis;

import com.azure.identity.extensions.implementation.credential.provider.TokenCredentialProvider;
import com.azure.identity.extensions.implementation.template.AzureAuthenticationTemplate;
import com.azure.identity.extensions.implementation.token.AccessTokenResolver;

import java.util.Properties;
import java.util.function.Supplier;

public class AzureRedisCredentialSupplier implements Supplier<char[]> {

    private final AzureAuthenticationTemplate azureAuthenticationTemplate;

    public AzureRedisCredentialSupplier(Properties properties) {
        this(properties, null, null);
    }

    public AzureRedisCredentialSupplier(Properties properties, TokenCredentialProvider tokenCredentialProvider, AccessTokenResolver accessTokenResolver) {
        azureAuthenticationTemplate = new AzureAuthenticationTemplate(tokenCredentialProvider, accessTokenResolver);
        azureAuthenticationTemplate.init(properties);
    }

    public AzureRedisCredentialSupplier(TokenCredentialProvider tokenCredentialProvider, AccessTokenResolver accessTokenResolver) {
        azureAuthenticationTemplate = new AzureAuthenticationTemplate(tokenCredentialProvider, accessTokenResolver);
    }

    @Override
    public char[] get() {
        return azureAuthenticationTemplate.getTokenAsPassword().toCharArray();
    }

}
