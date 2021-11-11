// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.factory;

import com.azure.core.http.ProxyOptions;
import com.azure.spring.core.aware.ProxyAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.Configuration;
import com.azure.identity.CredentialBuilderBase;
import com.azure.spring.core.aware.RetryAware;
import com.azure.spring.core.credential.descriptor.AuthenticationDescriptor;
import com.azure.spring.core.properties.AzureProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.azure.spring.core.converter.AzureHttpProxyOptionsConverter.HTTP_PROXY_CONVERTER;

/**
 *
 */
public class AzureCredentialBuilderFactory<T extends CredentialBuilderBase<T>> extends AbstractAzureHttpClientBuilderFactory<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureCredentialBuilderFactory.class);

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
        final ProxyAware.Proxy proxy = getAzureProperties().getProxy();
        if (proxy == null) {
            return;
        }

        ProxyOptions proxyOptions = HTTP_PROXY_CONVERTER.convert(proxy);
        if (proxyOptions != null) {
            builder.proxyOptions(proxyOptions);
        }
    }

    @Override
    protected BiConsumer<T, Configuration> consumeConfiguration() {
        return T::configuration;
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
    protected BiConsumer<T, RetryPolicy> consumeRetryPolicy() {
        LOGGER.debug("No need to specify retry policy.");
        return null;
    }
}
