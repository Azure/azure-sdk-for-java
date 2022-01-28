// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.factory.credential;

import com.azure.core.util.ClientOptions;
import com.azure.identity.CredentialBuilderBase;
import com.azure.spring.core.aware.RetryAware;
import com.azure.spring.core.aware.authentication.TokenCredentialAware;
import com.azure.spring.core.factory.AbstractAzureHttpClientBuilderFactory;
import com.azure.spring.core.properties.AzureProperties;
import com.azure.spring.core.properties.PropertyMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.azure.core.util.Configuration.PROPERTY_AZURE_CLIENT_CERTIFICATE_PATH;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_CLIENT_ID;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_CLIENT_SECRET;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_PASSWORD;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_TENANT_ID;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_USERNAME;

/**
 *
 */
public abstract class AbstractAzureCredentialBuilderFactory<T extends CredentialBuilderBase<T>> extends AbstractAzureHttpClientBuilderFactory<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAzureCredentialBuilderFactory.class);

    private final AzureProperties azureProperties;

    /**
     * To create a {@link AbstractAzureCredentialBuilderFactory} instance with {@link AzureProperties}.
     * @param azureProperties The Azure properties.
     */
    public AbstractAzureCredentialBuilderFactory(AzureProperties azureProperties) {
        this.azureProperties = azureProperties;
    }

    @Override
    protected AzureProperties getAzureProperties() {
        return this.azureProperties;
    }

    @Override
    protected void configureRetry(T builder) {
        RetryAware.Retry retry = getAzureProperties().getRetry();
        if (retry == null) {
            return;
        }

        if (retry.getMaxAttempts() != null) {
            builder.maxRetry(retry.getMaxAttempts());
        }
        Function<Duration, Duration> retryTimeout = retryTimeout();
        if (retryTimeout != null) {
            builder.retryTimeout(retryTimeout);
        }
    }

    /**
     * Default timeout implementation
     * @return Timeout function
     */
    protected Function<Duration, Duration> retryTimeout() {
        RetryAware.Retry retry = getAzureProperties().getRetry();
        if (retry == null || retry.getTimeout() == null) {
            return null;
        }
        return timeout -> retry.getTimeout();
    }

    @Override
    protected void configureConfiguration(T builder) {
        PropertyMapper mapper = new PropertyMapper();
        TokenCredentialAware.TokenCredential credential = this.azureProperties.getCredential();
        mapper.from(credential.getClientId()).to(v -> configuration.put(PROPERTY_AZURE_CLIENT_ID, v));
        mapper.from(credential.getClientSecret()).to(v -> configuration.put(PROPERTY_AZURE_CLIENT_SECRET, v));
        mapper.from(credential.getUsername()).to(v -> configuration.put(PROPERTY_AZURE_USERNAME, v));
        mapper.from(credential.getPassword()).to(v -> configuration.put(PROPERTY_AZURE_PASSWORD, v));
        mapper.from(credential.getManagedIdentityClientId()).to(v -> configuration.put(PROPERTY_AZURE_CLIENT_ID, v));
        mapper.from(credential.getClientCertificatePath()).to(v -> configuration.put(PROPERTY_AZURE_CLIENT_CERTIFICATE_PATH, v));
        mapper.from(azureProperties.getProfile().getTenantId()).to(v -> configuration.put(PROPERTY_AZURE_TENANT_ID, v));

        super.configureConfiguration(builder);
    }

    @Override
    protected BiConsumer<T, ClientOptions> consumeClientOptions() {
        return (a, b) -> { };
    }

    @Override
    protected void configureService(T builder) {

    }
}
