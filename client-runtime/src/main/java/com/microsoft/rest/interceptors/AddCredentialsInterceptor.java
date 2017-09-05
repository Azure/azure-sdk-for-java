package com.microsoft.rest.interceptors;

import com.microsoft.rest.credentials.ServiceClientCredentials;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

/**
 * Stopgap for transitioning away from OkHttp interceptors while refactoring credentials.
 */
public class AddCredentialsInterceptor implements Interceptor {
    private final ServiceClientCredentials credentials;

    public AddCredentialsInterceptor(ServiceClientCredentials credentials) {
        this.credentials = credentials;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        String credential = credentials.headerValue(chain.request().url().toString());
        Request newRequest = chain.request().newBuilder()
                .header("Authorization", credential)
                .build();
        return chain.proceed(newRequest);
    }
}
