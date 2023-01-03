// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.redis.passwordless.data.jedis;

import com.azure.identity.extensions.implementation.template.AzureAuthenticationTemplate;

import java.util.Properties;
import java.util.function.Supplier;

/**
 * AzureRedisCredentialSupplier that provide a String as the password to connect Azure Redis.
 *
 */
public class AzureRedisCredentialSupplier implements Supplier<String> {

    private final AzureAuthenticationTemplate azureAuthenticationTemplate;

    /**
     * Create {@link AzureRedisCredentialSupplier} instance.
     * @param properties properties to initialize AzureRedisCredentialSupplier.
     */
    public AzureRedisCredentialSupplier(Properties properties) {
        azureAuthenticationTemplate = new AzureAuthenticationTemplate();
        azureAuthenticationTemplate.init(properties);
    }

    @Override
    public String get() {
        return azureAuthenticationTemplate.getTokenAsPassword();
    }

    AzureRedisCredentialSupplier(AzureAuthenticationTemplate azureAuthenticationTemplate) {
        this.azureAuthenticationTemplate = azureAuthenticationTemplate;
    }
}
