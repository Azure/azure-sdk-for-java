// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.factory;

import com.azure.core.client.traits.HttpTrait;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpClientProvider;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.ProxyOptions;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.Header;
import com.azure.core.util.HttpClientOptions;
import com.azure.spring.core.aware.ClientAware;
import com.azure.spring.core.aware.ProxyAware;
import com.azure.spring.core.aware.RetryAware;
import com.azure.spring.core.implementation.http.DefaultHttpProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static com.azure.spring.core.implementation.converter.AzureHttpLogOptionsConverter.HTTP_LOG_OPTIONS_CONVERTER;
import static com.azure.spring.core.implementation.converter.AzureHttpProxyOptionsConverter.HTTP_PROXY_CONVERTER;
import static com.azure.spring.core.implementation.converter.AzureHttpRetryPolicyConverter.HTTP_RETRY_CONVERTER;

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

    @Override
    protected void configureCore(T builder, Configuration configuration) {
        super.configureCore(builder, configuration);
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
            if (builder instanceof HttpTrait) {
                ((HttpTrait<?>) builder).pipeline(this.httpPipeline);
            } else {
                //throw new IllegalArgumentException("builder isn't http");
            }
        } else {
            configureHttpHeaders(builder);
            configureHttpTransportProperties(builder);
            configureHttpPipelinePolicies(builder);
            final HttpClient httpClient = getHttpClientProvider().createInstance(this.httpClientOptions);
            if (builder instanceof HttpTrait) {
                ((HttpTrait<?>) builder).httpClient(httpClient);
            } else {
                //throw new IllegalArgumentException("builder isn't http");
            }
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
        ClientAware.Client client = getAzureProperties().getClient();

        if (client instanceof ClientAware.HttpClient && builder instanceof HttpTrait) {
            HttpLogOptions logOptions =
                HTTP_LOG_OPTIONS_CONVERTER.convert(((ClientAware.HttpClient) client).getLogging());
            ((HttpTrait<?>) builder).httpLogOptions(logOptions);
        } else {
            LOGGER.warn("The client properties of an http-based client is of type {}", client.getClass().getName());
        }

    }

    /**
     * Configure the HTTP transport properties to the builder.
     *
     * @param builder The builder of the HTTP-based service client.
     */
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
            RetryOptions retryOptions = HTTP_RETRY_CONVERTER.convert((RetryAware.HttpRetry) retry);
            if (retryOptions != null) {
                if (builder instanceof HttpTrait) {
                    ((HttpTrait<?>) builder).retryOptions(retryOptions);
                } else {
                    throw new IllegalArgumentException("not an http builder");
                }
            }
        } else {
            LOGGER.warn("Retry properties of type {} in an http client builder factory.", retry.getClass().getName());
        }
    }

    /**
     * Configure the set of {@link HttpPipelinePolicy} added via this factory to the builder.
     *
     * @param builder The builder of the HTTP-based service client.
     */
    protected void configureHttpPipelinePolicies(T builder) {
        if (builder instanceof HttpTrait) {
            for (HttpPipelinePolicy policy : this.httpPipelinePolicies) {
                ((HttpTrait<?>) builder).addPolicy(policy);
            }
        } else {
            //throw new IllegalArgumentException("builder isn't http");
        }
    }

    /**
     * Extract the HTTP headers from the {@link com.azure.spring.core.properties.AzureProperties}.
     * @return The list of HTTP headers will be sent with the HTTP requests made of the HTTP-based sdk client.
     */
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
