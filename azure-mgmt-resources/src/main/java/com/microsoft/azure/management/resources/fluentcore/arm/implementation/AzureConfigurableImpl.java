/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.arm.implementation;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.management.resources.fluentcore.arm.AzureConfigurable;
import com.microsoft.azure.RestClient;
import com.microsoft.rest.credentials.ServiceClientCredentials;
import okhttp3.Interceptor;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * The implementation for {@link AzureConfigurable<T>} and the base class for
 * configurable implementations.
 *
 * @param <T> the type of the configurable interface
 */
public class AzureConfigurableImpl<T extends AzureConfigurable<T>>
        implements AzureConfigurable<T> {
    private RestClient.Builder.Buildable restClientBuilder;

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

    protected RestClient buildRestClient(ServiceClientCredentials credentials) {
        return restClientBuilder.withCredentials(credentials).build();
    }
}
