/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.arm.implementation;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.RestClient;
import com.microsoft.azure.management.resources.fluentcore.arm.AzureConfigurable;
import com.microsoft.rest.credentials.ServiceClientCredentials;
import okhttp3.Interceptor;
import okhttp3.logging.HttpLoggingInterceptor;

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
        this.restClientBuilder = AzureEnvironment.AZURE.newRestClientBuilder();
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
        this.restClientBuilder = restClientBuilder.withMaxIdleConnections(5);
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

    protected RestClient buildRestClient(ServiceClientCredentials credentials) {
        return restClientBuilder.withCredentials(credentials).build();
    }
}
