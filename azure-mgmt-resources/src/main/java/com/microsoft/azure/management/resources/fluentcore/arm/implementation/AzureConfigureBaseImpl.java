package com.microsoft.azure.management.resources.fluentcore.arm.implementation;

import com.microsoft.azure.management.resources.fluentcore.arm.AzureConfigureBase;
import com.microsoft.rest.RestClient;
import okhttp3.Interceptor;
import okhttp3.logging.HttpLoggingInterceptor;

public class AzureConfigureBaseImpl<T extends AzureConfigureBase<T>>
        implements AzureConfigureBase<T> {
    protected RestClient.Builder restClientBuilder;
    protected RestClient restClient;

    protected AzureConfigureBaseImpl() {
        this.restClientBuilder = new RestClient.Builder("https://management.azure.com");
    }

    @Override
    public T withLogLevel(HttpLoggingInterceptor.Level level) {
        this.restClientBuilder = this.restClientBuilder.withLogLevel(level);
        return (T) this;
    }

    @Override
    public T withInterceptor(Interceptor interceptor) {
        this.restClientBuilder = this.restClientBuilder.withInterceptor(interceptor);
        return (T) this;
    }

    @Override
    public T withUserAgent(String userAgent) {
        this.restClientBuilder = this.restClientBuilder.withUserAgent(userAgent);
        return (T) this;
    }
}
