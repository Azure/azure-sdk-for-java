// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.redis.passwordless.jedis;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.extensions.implementation.credential.provider.TokenCredentialProvider;
import com.azure.identity.extensions.implementation.template.AzureAuthenticationTemplate;
import com.azure.identity.extensions.implementation.token.AccessTokenResolver;

import java.util.Properties;
import java.util.function.Supplier;

/**
 * AzureRedisCredentialSupplier that provide a char array as the password to connect Azure Redis.
 *
 * @since 4.6.0
 */
public class AzureRedisCredentialSupplier implements Supplier<char[]> {

    private final AzureAuthenticationTemplate azureAuthenticationTemplate;

    /**
     * Create {@link AzureRedisCredentialSupplier} instance.
     * @param properties properties to initialize AzureRedisCredentialSupplier.
     */
    public AzureRedisCredentialSupplier(Properties properties) {
        this(properties, null, null);
    }

    /**
     * Create {@link AzureRedisCredentialSupplier} instance.
     * @param properties properties to initialize AzureRedisCredentialSupplier.
     * @param tokenCredentialProvider Supplier that provide a {@link TokenCredential}.
     * @param accessTokenResolver An {@link AccessTokenResolver} instance, which will take a TokenCredential as input
     *                            and outputs a publisher that emits a single access token.
     */
    public AzureRedisCredentialSupplier(Properties properties, TokenCredentialProvider tokenCredentialProvider, AccessTokenResolver accessTokenResolver) {
        azureAuthenticationTemplate = new AzureAuthenticationTemplate(tokenCredentialProvider, accessTokenResolver);
        azureAuthenticationTemplate.init(properties);
    }

    /**
     * Create {@link AzureRedisCredentialSupplier} instance.
     * @param tokenCredentialProvider Supplier that provide a {@link TokenCredential}.
     * @param accessTokenResolver An {@link AccessTokenResolver} instance, which will take a TokenCredential as input
     *                            and outputs a publisher that emits a single access token.
     */
    public AzureRedisCredentialSupplier(TokenCredentialProvider tokenCredentialProvider, AccessTokenResolver accessTokenResolver) {
        azureAuthenticationTemplate = new AzureAuthenticationTemplate(tokenCredentialProvider, accessTokenResolver);
    }

    @Override
    public char[] get() {
        return azureAuthenticationTemplate.getTokenAsPassword().toCharArray();
    }

}
