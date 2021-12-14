// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.factory;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpClientProvider;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.ProxyOptions;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Header;
import com.azure.core.util.HttpClientOptions;
import com.azure.spring.core.aware.ClientAware;
import com.azure.spring.core.aware.ProxyAware;
import com.azure.spring.core.aware.RetryAware;
import com.azure.spring.core.http.DefaultHttpProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static com.azure.spring.core.converter.AzureHttpLogOptionsConverter.HTTP_LOG_OPTIONS_CONVERTER;
import static com.azure.spring.core.converter.AzureHttpProxyOptionsConverter.HTTP_PROXY_CONVERTER;
import static com.azure.spring.core.converter.AzureHttpRetryPolicyConverter.HTTP_RETRY_CONVERTER;

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

    protected abstract BiConsumer<T, ClientOptions> consumeClientOptions();

    protected abstract BiConsumer<T, HttpClient> consumeHttpClient();

    protected abstract BiConsumer<T, HttpPipelinePolicy> consumeHttpPipelinePolicy();

    protected abstract BiConsumer<T, HttpPipeline> consumeHttpPipeline();

    protected abstract BiConsumer<T, HttpLogOptions> consumeHttpLogOptions();

    protected abstract BiConsumer<T, RetryPolicy> consumeRetryPolicy();

    @Override
    protected void configureCore(T builder) {
        super.configureCore(builder);
        configureHttpClient(builder);
        configureHttpLogOptions(builder);
    }

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
        final ProxyAware.Proxy proxy = getAzureProperties().getProxy();
        if (proxy == null) {
            return;
        }

        ProxyOptions proxyOptions = HTTP_PROXY_CONVERTER.convert(proxy);
        if (proxyOptions != null) {
            this.httpClientOptions.setProxyOptions(proxyOptions);
        } else {
            LOGGER.debug("No HTTP proxy properties available.");
        }
    }

    @Override
    protected BiConsumer<T, String> consumeApplicationId() {
        return (builder, id) -> this.httpClientOptions.setApplicationId(id);
    }

    protected void configureHttpHeaders(T builder) {
        this.httpClientOptions.setHeaders(getHeaders());
    }

    protected void configureHttpLogOptions(T builder) {
        ClientAware.Client client = getAzureProperties().getClient();

        if (client instanceof ClientAware.HttpClient) {
            HttpLogOptions logOptions =
                HTTP_LOG_OPTIONS_CONVERTER.convert(((ClientAware.HttpClient) client).getLogging());
            consumeHttpLogOptions().accept(builder, logOptions);
        } else {
            LOGGER.warn("The client properties of an http-based client is of type {}", client.getClass().getName());
        }

    }

    protected void configureHttpTransportProperties(T builder) {
        final ClientAware.Client client = getAzureProperties().getClient();
        if (client == null) {
            return;
        }
        final ClientAware.HttpClient properties;
        if (client instanceof ClientAware.HttpClient) {
            properties = (ClientAware.HttpClient) client;
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
        RetryAware.Retry retry = getAzureProperties().getRetry();
        if (retry == null) {
            return;
        }

        if (retry instanceof RetryAware.HttpRetry) {
            RetryPolicy retryPolicy = HTTP_RETRY_CONVERTER.convert((RetryAware.HttpRetry) retry);
            consumeRetryPolicy().accept(builder, retryPolicy);
        } else {
            LOGGER.warn("Retry properties of type {} in an http client builder factory.", retry.getClass().getName());
        }
    }

    protected void configureHttpPipelinePolicies(T builder) {
        for (HttpPipelinePolicy policy : this.httpPipelinePolicies) {
            consumeHttpPipelinePolicy().accept(builder, policy);
        }
    }

    protected List<Header> getHeaders() {
        final ClientAware.Client client = getAzureProperties().getClient();
        if (client == null || client.getHeaders() == null) {
            return null;
        }
        return client.getHeaders()
                     .stream()
                     .map(h -> new Header(h.getName(), h.getValues()))
                     .collect(Collectors.toList());
    }

    public void setHttpClientProvider(HttpClientProvider httpClientProvider) {
        if (httpClientProvider != null) {
            this.httpClientProvider = httpClientProvider;
        }
    }

    protected List<HttpPipelinePolicy> getHttpPipelinePolicies() {
        return Collections.unmodifiableList(this.httpPipelinePolicies);
    }

    public void addHttpPipelinePolicy(HttpPipelinePolicy policy) {
        this.httpPipelinePolicies.add(policy);
    }

    public void setHttpPipeline(HttpPipeline httpPipeline) {
        this.httpPipeline = httpPipeline;
    }

    protected HttpClientProvider getHttpClientProvider() {
        return this.httpClientProvider;
    }

}
