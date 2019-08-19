package com.azure.search.service.customization;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class SearchInterceptor implements Interceptor {

    private SearchCredentials credentials;

    public SearchInterceptor(SearchCredentials credentials) {
        this.credentials = credentials;
    }

    public Response intercept(Chain chain) throws IOException {
        Request newRequest = chain.request().newBuilder().header("api-key", credentials.getApiKey()).build();
        return chain.proceed(newRequest);
    }
}
