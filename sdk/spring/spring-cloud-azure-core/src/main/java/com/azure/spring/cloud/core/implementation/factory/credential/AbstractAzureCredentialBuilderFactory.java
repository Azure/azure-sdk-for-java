// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.factory.credential;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.identity.CredentialBuilderBase;
import com.azure.spring.cloud.core.implementation.credential.descriptor.AuthenticationDescriptor;
import com.azure.spring.cloud.core.implementation.factory.AbstractAzureHttpClientBuilderFactory;
import com.azure.spring.cloud.core.properties.AzureProperties;
import com.azure.spring.cloud.core.provider.RetryOptionsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

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
    protected AbstractAzureCredentialBuilderFactory(AzureProperties azureProperties) {
        this.azureProperties = azureProperties;
    }

    @Override
    protected BiConsumer<T, HttpClient> consumeHttpClient() {
        return T::httpClient;
    }

    @Override
    protected BiConsumer<T, HttpPipeline> consumeHttpPipeline() {
        return T::httpPipeline;
    }

    @Override
    protected AzureProperties getAzureProperties() {
        return this.azureProperties;
    }

    @Override
    protected BiConsumer<T, Configuration> consumeConfiguration() {
        return T::configuration;
    }

    @Override
    protected void configureRetry(T builder) {
        RetryOptionsProvider.RetryOptions retry = null;
        if (azureProperties instanceof RetryOptionsProvider) {
            retry = ((RetryOptionsProvider) azureProperties).getRetry();
        }

        if (retry == null) {
            return;
        }

        if (RetryOptionsProvider.RetryMode.EXPONENTIAL == retry.getMode()) {
            if (retry.getExponential() != null && retry.getExponential().getMaxRetries() != null) {
                builder.maxRetry(retry.getExponential().getMaxRetries());
            }
        } else if (RetryOptionsProvider.RetryMode.FIXED == retry.getMode()
            && retry.getFixed() != null
            && retry.getFixed().getMaxRetries() != null) {
            builder.maxRetry(retry.getFixed().getMaxRetries());
        }

    }

    @Override
    protected List<AuthenticationDescriptor<?>> getAuthenticationDescriptors(T builder) {
        return Collections.emptyList();
    }

    @Override
    protected BiConsumer<T, ClientOptions> consumeClientOptions() {
        return (a, b) -> { };
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
    protected BiConsumer<T, HttpLogOptions> consumeHttpLogOptions() {
        return (a, b) -> { };
    }

    @Override
    protected BiConsumer<T, HttpPipelinePolicy> consumeHttpPipelinePolicy() {
        return (a, b) -> { };
    }

    @Override
    protected BiConsumer<T, RetryPolicy> consumeRetryPolicy() {
        LOGGER.debug("No need to specify retry policy.");
        return (a, b) -> { };
    }

    @Override
    protected void configureService(T builder) {

    }
}
