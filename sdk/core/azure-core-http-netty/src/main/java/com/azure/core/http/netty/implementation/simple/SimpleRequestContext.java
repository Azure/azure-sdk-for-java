// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation.simple;

import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import io.netty.channel.pool.ChannelPool;

import java.util.concurrent.CompletableFuture;

public class SimpleRequestContext {

    private final ChannelPool channelPool;
    private final HttpRequest request;
    private final CompletableFuture<HttpResponse> responseFuture;
    private final SimpleBodyCollector bodyCollector;

    private volatile int statusCode;
    private volatile com.azure.core.http.HttpHeaders httpHeaders;

    public SimpleRequestContext(ChannelPool channelPool, HttpRequest request,
                                CompletableFuture<HttpResponse> responseFuture,
                                SimpleBodyCollector bodyCollector) {
        this.channelPool = channelPool;
        this.request = request;
        this.responseFuture = responseFuture;
        this.bodyCollector = bodyCollector;
    }

    public HttpRequest getRequest() {
        return request;
    }

    public CompletableFuture<HttpResponse> getResponseFuture() {
        return responseFuture;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public com.azure.core.http.HttpHeaders getHttpHeaders() {
        return httpHeaders;
    }

    public void setHttpHeaders(com.azure.core.http.HttpHeaders httpHeaders) {
        this.httpHeaders = httpHeaders;
    }

    public SimpleBodyCollector getBodyCollector() {
        return bodyCollector;
    }

    public ChannelPool getChannelPool() {
        return channelPool;
    }
}
