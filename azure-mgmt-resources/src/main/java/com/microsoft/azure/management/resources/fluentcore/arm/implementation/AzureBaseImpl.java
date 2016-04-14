package com.microsoft.azure.management.resources.fluentcore.arm.implementation;

import com.microsoft.azure.management.resources.fluentcore.arm.AzureBase;
import com.microsoft.rest.UserAgentInterceptor;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;

public class AzureBaseImpl<T extends AzureBase<T>>
        implements AzureBase<T> {
    private OkHttpClient.Builder httpClientBuilder;
    private Retrofit.Builder retrofitBuilder;

    protected OkHttpClient httpClient;
    protected Retrofit retrofit;

    protected AzureBaseImpl() {
        this.httpClientBuilder = new OkHttpClient.Builder();
        this.retrofitBuilder = new Retrofit.Builder();
    }

    @Override
    public T withLogLevel(HttpLoggingInterceptor.Level level) {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(level);
        httpClientBuilder.addInterceptor(loggingInterceptor);
        return (T) this;
    }

    @Override
    public T withInterceptor(Interceptor interceptor) {
        httpClientBuilder.addInterceptor(interceptor);
        return (T) this;
    }

    @Override
    public T withUserAgent(String userAgent) {
        UserAgentInterceptor userAgentInterceptor = new UserAgentInterceptor(userAgent);
        httpClientBuilder.addInterceptor(userAgentInterceptor);
        return (T) this;
    }
}
