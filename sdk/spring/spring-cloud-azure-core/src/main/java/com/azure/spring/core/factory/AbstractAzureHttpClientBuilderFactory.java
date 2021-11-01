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
import com.azure.core.util.Header;
import com.azure.core.util.HttpClientOptions;
import com.azure.spring.core.converter.AzureHttpLogOptionsConverter;
import com.azure.spring.core.converter.AzureHttpProxyOptionsConverter;
import com.azure.spring.core.converter.AzureRetryPolicyConverter;
import com.azure.spring.core.http.DefaultHttpProvider;
import com.azure.spring.core.properties.client.ClientProperties;
import com.azure.spring.core.properties.client.HttpClientProperties;
import com.azure.spring.core.properties.proxy.HttpProxyProperties;
import com.azure.spring.core.properties.proxy.ProxyProperties;
import com.azure.spring.core.properties.retry.HttpRetryProperties;
import com.azure.spring.core.properties.retry.RetryProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

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
    private final AzureHttpProxyOptionsConverter proxyOptionsConverter = new AzureHttpProxyOptionsConverter();
    private final AzureHttpLogOptionsConverter logOptionsConverter = new AzureHttpLogOptionsConverter();
    private final AzureRetryPolicyConverter httpRetryOptionsConverter = new AzureRetryPolicyConverter();
    protected abstract BiConsumer<T, HttpClient> consumeHttpClient();

    protected abstract BiConsumer<T, HttpPipelinePolicy> consumeHttpPipelinePolicy();

    protected abstract BiConsumer<T, HttpPipeline> consumeHttpPipeline();

    protected abstract BiConsumer<T, HttpLogOptions> consumeHttpLogOptions();

    protected BiConsumer<T, RetryPolicy> consumeRetryPolicy() {
        return (a, b) -> { };
    }

    @Override
    protected void configureCore(T builder) {
        super.configureCore(builder);
        configureHttpClient(builder);
        configureHttpLogOptions(builder);
    }

    protected void configureHttpClient(T builder) {
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
        final ProxyProperties proxyProperties = getAzureProperties().getProxy();
        if (proxyProperties == null) {
            return;
        }

        if (proxyProperties instanceof HttpProxyProperties) {
            ProxyOptions proxyOptions = proxyOptionsConverter.convert((HttpProxyProperties) proxyProperties);
            if (proxyOptions != null) {
                this.httpClientOptions.setProxyOptions(proxyOptions);
            } else {
                LOGGER.debug("No HTTP proxy properties available.");
            }
        } else {
            LOGGER.warn("Non-http proxy configuration will not be applied.");
        }
    }

    @Override
    protected void configureApplicationId(T builder) {
        this.httpClientOptions.setApplicationId(getApplicationId());
    }

    protected void configureHttpHeaders(T builder) {
        this.httpClientOptions.setHeaders(getHeaders());
    }

    protected void configureHttpLogOptions(T builder) {
        ClientProperties client = getAzureProperties().getClient();
        HttpLogOptions logOptions = this.logOptionsConverter.convert(client.getLogging());
        consumeHttpLogOptions().accept(builder, logOptions);
    }

    protected void configureHttpTransportProperties(T builder) {
        final ClientProperties clientProperties = getAzureProperties().getClient();
        if (clientProperties == null) {
            return;
        }
        final HttpClientProperties properties;
        if (clientProperties instanceof HttpClientProperties) {
            properties = (HttpClientProperties) clientProperties;
            httpClientOptions.setWriteTimeout(properties.getWriteTimeout());
            httpClientOptions.responseTimeout(properties.getResponseTimeout());
            httpClientOptions.readTimeout(properties.getReadTimeout());
        }
    }

    @Override
    protected void configureRetry(T builder) {
        RetryProperties retryProperties = getAzureProperties().getRetry();
        if (retryProperties == null) {
            return;
        }

        if (retryProperties instanceof HttpRetryProperties) {
            RetryPolicy retryPolicy = httpRetryOptionsConverter.convert((HttpRetryProperties) retryProperties);
            consumeRetryPolicy().accept(builder, retryPolicy);
        }
    }

    protected void configureHttpPipelinePolicies(T builder) {
        for (HttpPipelinePolicy policy : this.httpPipelinePolicies) {
            consumeHttpPipelinePolicy().accept(builder, policy);
        }
    }

    protected List<Header> getHeaders() {
        final ClientProperties client = getAzureProperties().getClient();
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
