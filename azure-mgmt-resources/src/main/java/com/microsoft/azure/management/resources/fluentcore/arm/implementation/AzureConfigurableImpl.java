/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.arm.implementation;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.RestClient;
import com.microsoft.azure.credentials.AzureTokenCredentials;
import com.microsoft.azure.management.resources.fluentcore.arm.AzureConfigurable;
import okhttp3.Interceptor;
import okhttp3.logging.HttpLoggingInterceptor;

import java.lang.reflect.Field;
import java.net.Proxy;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * The implementation for {@link AzureConfigurable<T>} and the base class for
 * configurable implementations.
 *
 * @param <T> the type of the configurable interface
 */
public class AzureConfigurableImpl<T extends AzureConfigurable<T>>
        implements AzureConfigurable<T> {
    protected RestClient.Builder.Buildable restClientBuilder;

    protected AzureConfigurableImpl() {
        this.restClientBuilder = AzureEnvironment.AZURE.newRestClientBuilder(); // default to public cloud
    }

    @SuppressWarnings("unchecked")
    @Override
    public T withLogLevel(HttpLoggingInterceptor.Level level) {
        this.restClientBuilder = this.restClientBuilder.withLogLevel(level);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T withInterceptor(Interceptor interceptor) {
        this.restClientBuilder = this.restClientBuilder.withInterceptor(interceptor);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T withUserAgent(String userAgent) {
        this.restClientBuilder = this.restClientBuilder.withUserAgent(userAgent);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T withReadTimeout(long timeout, TimeUnit unit) {
        this.restClientBuilder = restClientBuilder.withReadTimeout(timeout, unit);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T withConnectionTimeout(long timeout, TimeUnit unit) {
        this.restClientBuilder = restClientBuilder.withConnectionTimeout(timeout, unit);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T withMaxIdleConnections(int maxIdleConnections) {
        this.restClientBuilder = restClientBuilder.withMaxIdleConnections(maxIdleConnections);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T withCallbackExecutor(Executor executor) {
        this.restClientBuilder = restClientBuilder.withCallbackExecutor(executor);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T withProxy(Proxy proxy) {
        this.restClientBuilder = restClientBuilder.withProxy(proxy);
        return (T) this;
    }

    protected RestClient buildRestClient(AzureTokenCredentials credentials) {
        restClientBuilder = modifyBaseUrl(restClientBuilder, credentials.getEnvironment().getBaseUrl());
        return restClientBuilder.withCredentials(credentials).build();
    }

    protected RestClient buildRestClientForGraph(AzureTokenCredentials credentials) {
        restClientBuilder = modifyBaseUrl(restClientBuilder, credentials.getEnvironment().getGraphEndpoint());
        return restClientBuilder.withCredentials(credentials).build();
    }

    private RestClient.Builder.Buildable modifyBaseUrl(RestClient.Builder.Buildable builder, String baseUrl) {
        try {
            // This reflection will be removed in next version of client runtime
            Field enclosed = builder.getClass().getDeclaredField("this$0");
            enclosed.setAccessible(true);
            Object enclosedObj = enclosed.get(builder);
            Field url = enclosedObj.getClass().getDeclaredField("baseUrl");
            url.setAccessible(true);
            url.set(enclosedObj, baseUrl);
        } catch (IllegalAccessException | NoSuchFieldException ex) {
            // swallow it to use default base url
        }
        return builder;
    }
}
