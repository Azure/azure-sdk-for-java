// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.http.okhttp3.mocking;

import okhttp3.Call;
import okhttp3.Connection;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Mock implementation of {@link Interceptor.Chain}.
 */
public class MockInterceptorChain implements Interceptor.Chain {
    private final Response proceedResponse;
    private final Request request;

    /**
     * Creates an instance of {@link MockInterceptorChain}.
     *
     * @param proceedResponse The {@link Response} to return when {@link #proceed(Request)} is called with any
     * {@link Request}.
     * @param request The {@link Request} to return when {@link Request} is called.
     */
    public MockInterceptorChain(Response proceedResponse, Request request) {
        this.proceedResponse = proceedResponse;
        this.request = request;
    }

    @NotNull
    @Override
    public Call call() {
        return null;
    }

    @NotNull
    @Override
    public Request request() {
        return request;
    }

    @NotNull
    @Override
    public Response proceed(@NotNull Request request) throws IOException {
        return proceedResponse;
    }

    @Nullable
    @Override
    public Connection connection() {
        return null;
    }

    @Override
    public int connectTimeoutMillis() {
        return 0;
    }

    @NotNull
    @Override
    public Interceptor.Chain withConnectTimeout(int i, @NotNull TimeUnit timeUnit) {
        return this;
    }

    @Override
    public int readTimeoutMillis() {
        return 0;
    }

    @NotNull
    @Override
    public Interceptor.Chain withReadTimeout(int i, @NotNull TimeUnit timeUnit) {
        return this;
    }

    @Override
    public int writeTimeoutMillis() {
        return 0;
    }

    @NotNull
    @Override
    public Interceptor.Chain withWriteTimeout(int i, @NotNull TimeUnit timeUnit) {
        return this;
    }
}
