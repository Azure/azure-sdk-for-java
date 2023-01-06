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
 * AzureRedisCredentialSupplier that provide a String as the password to connect Azure Redis.
 *
 * @since 4.6.0
 */
public class AzureRedisCredentialSupplier implements Supplier<String> {

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
     * @param tokenCredentialProvider Supplier that provide a {@link TokenCredential}, must not be {@literal null}.
     * @param accessTokenResolver An {@link AccessTokenResolver} instance, which will take a TokenCredential as input
     *                            and outputs a publisher that emits a single access token, must not be {@literal null}.
     */
    public AzureRedisCredentialSupplier(TokenCredentialProvider tokenCredentialProvider, AccessTokenResolver accessTokenResolver) {
        this(null, tokenCredentialProvider, accessTokenResolver);
    }

    @Override
    public String get() {
        return azureAuthenticationTemplate.getTokenAsPassword();
    }

    AzureRedisCredentialSupplier(AzureAuthenticationTemplate azureAuthenticationTemplate) {
        this.azureAuthenticationTemplate = azureAuthenticationTemplate;
    }
}
