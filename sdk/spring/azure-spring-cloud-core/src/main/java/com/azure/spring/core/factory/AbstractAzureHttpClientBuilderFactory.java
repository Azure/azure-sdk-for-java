// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.factory;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpClientProvider;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.Header;
import com.azure.core.util.HttpClientOptions;
import com.azure.spring.core.converter.AzureHttpProxyOptionsConverter;
import com.azure.spring.core.http.DefaultHttpProvider;
import com.azure.spring.core.properties.client.ClientProperties;
import com.azure.spring.core.properties.client.HttpClientProperties;
import com.azure.spring.core.properties.proxy.ProxyProperties;

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

    private final HttpClientOptions httpClientOptions = new HttpClientOptions();
    private HttpClientProvider httpClientProvider = new DefaultHttpProvider();
    private final List<HttpPipelinePolicy> httpPipelinePolicies = new ArrayList<>();
    private final AzureHttpProxyOptionsConverter proxyOptionsConverter = new AzureHttpProxyOptionsConverter();

    protected abstract BiConsumer<T, HttpClient> consumeHttpClient();

    protected abstract BiConsumer<T, HttpPipelinePolicy> consumeHttpPipelinePolicy();

    @Override
    protected void configureCore(T builder) {
        configureAzureEnvironment(builder);
        configureRetry(builder);
        configureCredential(builder);
        configureHttpClient(builder);
    }

    protected void configureHttpClient(T builder) {
        configureApplicationId(builder);
        configureProxy(builder);
        configureHttpHeaders(builder);
        configureHttpTransportProperties(builder);
        configureHttpPipelinePolicies(builder);
        final HttpClient httpClient = getHttpClientProvider().createInstance(this.httpClientOptions);
        consumeHttpClient().accept(builder, httpClient);
    }

    @Override
    protected void configureProxy(T builder) {
        final ProxyProperties proxy = getAzureProperties().getProxy();
        if (proxy != null) {
            this.httpClientOptions.setProxyOptions(proxyOptionsConverter.convert(proxy));
        }
    }

    @Override
    protected void configureApplicationId(T builder) {
        this.httpClientOptions.setApplicationId(getApplicationId());
    }

    protected void configureHttpHeaders(T builder) {
        this.httpClientOptions.setHeaders(getHeaders());
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

    protected HttpClientProvider getHttpClientProvider() {
        return this.httpClientProvider;
    }

}
