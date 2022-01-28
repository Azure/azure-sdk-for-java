// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.implementation.keyvault.secrets;

import com.azure.core.util.ClientOptions;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.spring.core.factory.AbstractAzureHttpClientBuilderFactory;
import com.azure.spring.core.properties.AzureProperties;
import com.azure.spring.core.properties.PropertyMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiConsumer;

/**
 * Azure Key Vault certificate client builder factory, it builds the {@link SecretClientBuilder}.
 */
public class SecretClientBuilderFactory extends AbstractAzureHttpClientBuilderFactory<SecretClientBuilder> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecretClientBuilderFactory.class);

    private final SecretClientProperties secretClientProperties;

    /**
     * Create a {@link SecretClientBuilderFactory} with the {@link SecretClientProperties}.
     * @param secretClientProperties the properties of the secret client.
     */
    public SecretClientBuilderFactory(SecretClientProperties secretClientProperties) {
        this.secretClientProperties = secretClientProperties;
    }

    @Override
    protected BiConsumer<SecretClientBuilder, ClientOptions> consumeClientOptions() {
        return SecretClientBuilder::clientOptions;
    }

    @Override
    protected SecretClientBuilder createBuilderInstance() {
        return new SecretClientBuilder();
    }

    @Override
    protected AzureProperties getAzureProperties() {
        return this.secretClientProperties;
    }

    @Override
    protected void configureService(SecretClientBuilder builder) {
        PropertyMapper map = new PropertyMapper();
        map.from(secretClientProperties.getEndpoint()).to(builder::vaultUrl);
        map.from(secretClientProperties.getServiceVersion()).to(builder::serviceVersion);
    }
}
