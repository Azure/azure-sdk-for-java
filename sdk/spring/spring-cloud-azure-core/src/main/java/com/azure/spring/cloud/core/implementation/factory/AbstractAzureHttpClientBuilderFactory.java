// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.factory;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpClientProvider;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.ProxyOptions;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Header;
import com.azure.core.util.HttpClientOptions;
import com.azure.spring.cloud.core.implementation.http.DefaultHttpProvider;
import com.azure.spring.cloud.core.properties.AzureProperties;
import com.azure.spring.cloud.core.provider.ClientOptionsProvider;
import com.azure.spring.cloud.core.provider.ProxyOptionsProvider;
import com.azure.spring.cloud.core.provider.RetryOptionsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static com.azure.spring.cloud.core.implementation.converter.AzureHttpLogOptionsConverter.HTTP_LOG_OPTIONS_CONVERTER;
import static com.azure.spring.cloud.core.implementation.converter.AzureHttpProxyOptionsConverter.HTTP_PROXY_CONVERTER;
import static com.azure.spring.cloud.core.implementation.converter.AzureHttpRetryOptionsConverter.HTTP_RETRY_CONVERTER;

/**
 * Abstract factory of the http client builder.
 *
 * @param <T> The type of the http client builder.
 */
public abstract class AbstractAzureHttpClientBuilderFactory<T> extends AbstractAzureServiceClientBuilderFactory<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAzureHttpClientBuilderFactory.class);

    private final HttpClientOptions httpClientOptions = new HttpClientOptions();
    private HttpClientProvider httpClientProvider = new DefaultHttpProvider();
    private final List<HttpPipelinePolicy> httpPipelinePolicies = new ArrayList<>();
    private HttpPipeline httpPipeline;

    /**
     * Return a {@link BiConsumer} of how the {@link T} builder consume a {@link ClientOptions}.
     * @return The consumer of how the {@link T} builder consume a {@link ClientOptions}.
     */
    protected abstract BiConsumer<T, ClientOptions> consumeClientOptions();

    /**
     * Return a {@link BiConsumer} of how the {@link T} builder consume a {@link HttpClient}.
     * @return The consumer of how the {@link T} builder consume a {@link HttpClient}.
     */
    protected abstract BiConsumer<T, HttpClient> consumeHttpClient();

    /**
     * Return a {@link BiConsumer} of how the {@link T} builder consume a {@link HttpPipelinePolicy}.
     * @return The consumer of how the {@link T} builder consume a {@link HttpPipelinePolicy}.
     */
    protected abstract BiConsumer<T, HttpPipelinePolicy> consumeHttpPipelinePolicy();

    /**
     * Return a {@link BiConsumer} of how the {@link T} builder consume a {@link HttpPipeline}.
     * @return The consumer of how the {@link T} builder consume a {@link HttpPipeline}.
     */
    protected abstract BiConsumer<T, HttpPipeline> consumeHttpPipeline();

    /**
     * Return a {@link BiConsumer} of how the {@link T} builder consume a {@link HttpLogOptions}.
     * @return The consumer of how the {@link T} builder consume a {@link HttpLogOptions}.
     */
    protected abstract BiConsumer<T, HttpLogOptions> consumeHttpLogOptions();

    /**
     * Return a {@link BiConsumer} of how the {@link T} builder consume a {@link RetryPolicy}.
     * @return The consumer of how the {@link T} builder consume a {@link RetryPolicy}.
     */
    protected abstract BiConsumer<T, RetryPolicy> consumeRetryPolicy();

    @Override
    protected void configureCore(T builder) {
        super.configureCore(builder);
        configureHttpClient(builder);
        configureHttpLogOptions(builder);
    }

    /**
     * Configure the {@link HttpClient} to the {@link T} builder. If a {@link HttpPipeline} is provided to the factory,
     * the pipeline will be set to the builder. Otherwise, a {@link HttpClient} will be created and together with the
     * {@link HttpPipelinePolicy} set to the factory will be configured to the builder.
     *
     * @param builder The builder of the HTTP-based service client.
     */
    protected void configureHttpClient(T builder) {
        consumeClientOptions().accept(builder, httpClientOptions);
        if (this.httpPipeline != null) {
            consumeHttpPipeline().accept(builder, this.httpPipeline);
        } else {
            configureHttpHeaders(builder);
            configureHttpTransportProperties(builder);
            configureHttpPipelinePolicies(builder);
            final HttpClient httpClient = getHttpClientProvider().createInstance(this.httpClientOptions);
            consumeHttpClient().accept(builder, httpClient);
        }
    }

    @Override
    protected void configureProxy(T builder) {
        final ProxyOptionsProvider.ProxyOptions proxy = getAzureProperties().getProxy();
        if (proxy == null) {
            return;
        }

        if (proxy instanceof ProxyOptionsProvider.HttpProxyOptions) {
            ProxyOptions proxyOptions = HTTP_PROXY_CONVERTER.convert((ProxyOptionsProvider.HttpProxyOptions) proxy);
            if (proxyOptions != null) {
                this.httpClientOptions.setProxyOptions(proxyOptions);
            } else {
                LOGGER.debug("No HTTP proxy properties available.");
            }
        } else {
            LOGGER.debug("The provided proxy options is not a ProxyOptionsProvider.HttpProxyOptions type.");
        }
    }

    @Override
    protected BiConsumer<T, String> consumeApplicationId() {
        return (builder, id) -> this.httpClientOptions.setApplicationId(id);
    }

    /**
     * Configure the {@link Header} that will be sent with the HTTP requests made of the HTTP-based sdk client.
     *
     * @param builder The builder of the HTTP-based service client.
     */
    protected void configureHttpHeaders(T builder) {
        this.httpClientOptions.setHeaders(getHeaders());
    }

    /**
     * Configure the {@link HttpLogOptions} to the builder.
     *
     * @param builder The builder of the HTTP-based service client.
     */
    protected void configureHttpLogOptions(T builder) {
        ClientOptionsProvider.ClientOptions client = getAzureProperties().getClient();

        if (client instanceof ClientOptionsProvider.HttpClientOptions) {
            HttpLogOptions logOptions =
                HTTP_LOG_OPTIONS_CONVERTER.convert(((ClientOptionsProvider.HttpClientOptions) client).getLogging());
            consumeHttpLogOptions().accept(builder, logOptions);
        } else if (client != null) {
            LOGGER.warn("The client properties of an http-based client is of type {}", client.getClass().getName());
        }

    }

    /**
     * Configure the HTTP transport properties to the builder.
     *
     * @param builder The builder of the HTTP-based service client.
     */
    protected void configureHttpTransportProperties(T builder) {
        final ClientOptionsProvider.ClientOptions client = getAzureProperties().getClient();
        if (client == null) {
            return;
        }
        final ClientOptionsProvider.HttpClientOptions properties;
        if (client instanceof ClientOptionsProvider.HttpClientOptions) {
            properties = (ClientOptionsProvider.HttpClientOptions) client;
            httpClientOptions.setWriteTimeout(properties.getWriteTimeout());
            httpClientOptions.responseTimeout(properties.getResponseTimeout());
            httpClientOptions.readTimeout(properties.getReadTimeout());
            httpClientOptions.setConnectTimeout(properties.getConnectTimeout());
            httpClientOptions.setConnectionIdleTimeout(properties.getConnectionIdleTimeout());
            httpClientOptions.setMaximumConnectionPoolSize(properties.getMaximumConnectionPoolSize());
        }
    }

    @Override
    protected void configureRetry(T builder) {
        AzureProperties azureProperties = getAzureProperties();
        RetryOptionsProvider.RetryOptions retry = null;
        if (azureProperties instanceof RetryOptionsProvider) {
            retry = ((RetryOptionsProvider) azureProperties).getRetry();
        }

        if (retry == null) {
            return;
        }
        RetryOptions retryOptions = HTTP_RETRY_CONVERTER.convert(retry);

        if (retryOptions == null) {
            LOGGER.debug("No HTTP retry properties available.");
            return;
        }
        consumeRetryPolicy().accept(builder, new RetryPolicy(retryOptions));
    }

    /**
     * Configure the set of {@link HttpPipelinePolicy} added via this factory to the builder.
     *
     * @param builder The builder of the HTTP-based service client.
     */
    protected void configureHttpPipelinePolicies(T builder) {
        for (HttpPipelinePolicy policy : this.httpPipelinePolicies) {
            consumeHttpPipelinePolicy().accept(builder, policy);
        }
    }

    /**
     * Extract the HTTP headers from the {@link AzureProperties}.
     * @return The list of HTTP headers will be sent with the HTTP requests made of the HTTP-based sdk client.
     */
    protected List<Header> getHeaders() {
        final ClientOptionsProvider.ClientOptions client = getAzureProperties().getClient();
        if (client == null) {
            return null;
        }
        if (!(client instanceof ClientOptionsProvider.HttpClientOptions)) {
            LOGGER.debug("The clientOptions passed in is not of ClientOptionsProvider.HttpClientOptions.");
            return null;
        }
        ClientOptionsProvider.HttpClientOptions clientOptions = (ClientOptionsProvider.HttpClientOptions) client;
        if (clientOptions.getHeaders() == null) {
            return null;
        }
        return clientOptions.getHeaders()
                            .stream()
                            .map(h -> new Header(h.getName(), h.getValues()))
                            .collect(Collectors.toList());
    }

    /**
     * Get a set of {@link HttpPipelinePolicy} configured via this factory.
     *
     * @return The list of the http pipeline policy.
     */
    protected List<HttpPipelinePolicy> getHttpPipelinePolicies() {
        return Collections.unmodifiableList(this.httpPipelinePolicies);
    }

    /**
     * Adds a {@link HttpPipelinePolicy} to the set of existing policies.
     *
     *  @param policy The {@link HttpPipelinePolicy policy} to be added.
     */
    public void addHttpPipelinePolicy(HttpPipelinePolicy policy) {
        this.httpPipelinePolicies.add(policy);
    }

    /**
     * Set the {@link HttpPipeline}.
     *
     * @param httpPipeline The http pipeline.
     */
    public void setHttpPipeline(HttpPipeline httpPipeline) {
        this.httpPipeline = httpPipeline;
    }

    /**
     * Get the {@link HttpClientProvider}.
     *
     * @return The http client provider.
     */
    protected HttpClientProvider getHttpClientProvider() {
        return this.httpClientProvider;
    }

    /**
     * Get the {@link HttpClientOptions}.
     *
     * @return The http client options.
     */
    protected HttpClientOptions getHttpClientOptions() {
        return this.httpClientOptions;
    }

    /**
     * Set the {@link HttpClientProvider}.
     *
     * @param httpClientProvider The http client provider.
     */
    public void setHttpClientProvider(HttpClientProvider httpClientProvider) {
        if (httpClientProvider != null) {
            this.httpClientProvider = httpClientProvider;
        }
    }
}
