// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.vertx;

import com.azure.core.http.HttpClient;
import io.vertx.core.http.impl.HttpClientImpl;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.web.client.impl.WebClientBase;

import java.lang.reflect.Field;
import java.util.function.Predicate;

/**
 * Utility class to reflectively retrieve configuration settings from the Vert.x HTTP Client that are
 * not exposed by default.
 *
 * Avoids having to implement workarounds in the client code to make them available just for testing purposes.
 */
final class VertxAsyncClientTestHelper {

    private VertxAsyncClientTestHelper() {
        // Utility class
    }

    static HttpClientImpl getVertxInternalHttpClient(HttpClient client) {
        try {
            VertxAsyncHttpClient vertxAsyncHttpClient = (VertxAsyncHttpClient) client;
            Field field = WebClientBase.class.getDeclaredField("client");
            field.setAccessible(true);
            return  (HttpClientImpl) field.get(vertxAsyncHttpClient.client);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    static Predicate<SocketAddress> getVertxInternalProxyFilter(HttpClientImpl client) {
        try {
            Field field = HttpClientImpl.class.getDeclaredField("proxyFilter");
            field.setAccessible(true);
            return (Predicate<SocketAddress>) field.get(client);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
