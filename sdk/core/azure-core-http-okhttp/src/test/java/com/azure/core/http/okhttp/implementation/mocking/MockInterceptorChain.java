// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.http.okhttp.implementation.mocking;

import okhttp3.Call;
import okhttp3.Connection;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class MockInterceptorChain implements Interceptor.Chain {
    private final Response response;
    private final Request request;

    public MockInterceptorChain(Response response, Request request) {
        this.response = response;
        this.request = request;
    }

    @NotNull
    @Override
    public Call call() {
        return null;
    }

    @Override
    public int connectTimeoutMillis() {
        return 0;
    }

    @Nullable
    @Override
    public Connection connection() {
        return null;
    }

    @NotNull
    @Override
    public Response proceed(@NotNull Request request) throws IOException {
        return response;
    }

    @Override
    public int readTimeoutMillis() {
        return 0;
    }

    @NotNull
    @Override
    public Request request() {
        return request;
    }

    @NotNull
    @Override
    public Interceptor.Chain withConnectTimeout(int i, @NotNull TimeUnit timeUnit) {
        return null;
    }

    @NotNull
    @Override
    public Interceptor.Chain withReadTimeout(int i, @NotNull TimeUnit timeUnit) {
        return null;
    }

    @NotNull
    @Override
    public Interceptor.Chain withWriteTimeout(int i, @NotNull TimeUnit timeUnit) {
        return null;
    }

    @Override
    public int writeTimeoutMillis() {
        return 0;
    }
}
