// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.vertx;

import io.vertx.core.http.impl.HttpClientBase;
import io.vertx.core.http.impl.HttpClientImpl;
import io.vertx.core.net.SocketAddress;

import java.lang.reflect.Field;
import java.util.function.Predicate;

/**
 * Utility class to reflectively retrieve configuration settings from the Vert.x HTTP Client that are
 * not exposed by default.
 * <p>
 * Avoids having to implement workarounds in the client code to make them available just for testing purposes.
 */
final class VertxClientTestHelper {

    private VertxClientTestHelper() {
        // Utility class
    }

    @SuppressWarnings({ "unchecked", "removal" })
    static Predicate<SocketAddress> getVertxInternalProxyFilter(HttpClientImpl client) {
        try {
            return (Predicate<SocketAddress>) java.security.AccessController
                .doPrivileged((java.security.PrivilegedExceptionAction<Object>) () -> {
                    Field field = HttpClientBase.class.getDeclaredField("proxyFilter");
                    field.setAccessible(true);
                    return field.get(client);
                });
        } catch (java.security.PrivilegedActionException e) {
            throw new RuntimeException(e);
        }
    }
}
