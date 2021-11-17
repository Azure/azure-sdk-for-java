// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.factory;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.Configuration;
import com.azure.identity.CredentialBuilderBase;
import com.azure.spring.core.aware.RetryAware;
import com.azure.spring.core.aware.authentication.TokenCredentialAware;
import com.azure.spring.core.credential.descriptor.AuthenticationDescriptor;
import com.azure.spring.core.properties.AzureProperties;
import com.azure.spring.core.properties.PropertyMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import static com.azure.core.util.Configuration.PROPERTY_AZURE_CLIENT_CERTIFICATE_PATH;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_CLIENT_ID;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_CLIENT_SECRET;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_PASSWORD;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_TENANT_ID;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_USERNAME;

/**
 *
 */
public class AzureCredentialBuilderFactory<T extends CredentialBuilderBase<T>> extends AbstractAzureHttpClientBuilderFactory<T> {

    private final AzureProperties azureProperties;
    private final T builder;

    public AzureCredentialBuilderFactory(AzureProperties azureProperties, T builder) {
        this.azureProperties = azureProperties;
        this.builder = builder;
    }

    @Override
    protected BiConsumer<T, HttpClient> consumeHttpClient() {
        return T::httpClient;
    }

    @Override
    protected BiConsumer<T, HttpPipelinePolicy> consumeHttpPipelinePolicy() {
        return (a, b) -> { };
    }

    @Override
    protected BiConsumer<T, HttpPipeline> consumeHttpPipeline() {
        return T::httpPipeline;
    }

    @Override
    protected BiConsumer<T, HttpLogOptions> consumeHttpLogOptions() {
        return (a, b) -> { };
    }

    @Override
    protected T createBuilderInstance() {
        return builder;
    }

    @Override
    protected AzureProperties getAzureProperties() {
        return this.azureProperties;
    }

    @Override
    protected List<AuthenticationDescriptor<?>> getAuthenticationDescriptors(T builder) {
        return new ArrayList<>();
    }

    @Override
    protected void configureService(T builder) {

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
    protected BiConsumer<T, Configuration> consumeConfiguration() {
        return (builder, configuration) -> builder.configuration(configuration);
    }

    @Override
    protected BiConsumer<T, TokenCredential> consumeDefaultTokenCredential() {
        return (a, b) -> { };
    }

    @Override
    protected BiConsumer<T, String> consumeConnectionString() {
        return (a, b) -> { };
    }

    @Override
    protected void configureRetry(T builder) {
        RetryAware.Retry retry = getAzureProperties().getRetry();
        if (retry != null && retry.getMaxAttempts() != null) {
            builder.maxRetry(retry.getMaxAttempts());
            // TODO (xiada): don't know how to specify the retryTimeout
//            builder.retryTimeout()
        }
    }

    @Override
    protected BiConsumer<T, RetryPolicy> consumeRetryPolicy() {
        return null;
    }
}
