// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

import com.azure.core.http.ProxyOptions;
import com.azure.core.util.AuthorizationChallengeHandler;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests {@link DeferredHttpProxyProvider}.
 */
public class DeferredHttpProxyProviderTests {
    @ParameterizedTest
    @MethodSource("testEqualsSupplier")
    public void testEquals(DeferredHttpProxyProvider provider, Object other, boolean expected) {
        assertEquals(expected, provider.equals(other));
    }

    private static Stream<Arguments> testEqualsSupplier() {
        AuthorizationChallengeHandler handler1 = new AuthorizationChallengeHandler("1", "1");
        AuthorizationChallengeHandler handler2 = new AuthorizationChallengeHandler("2", "2");

        ProxyOptions options1 = new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("localhost", 8888))
            .setCredentials("1", "1");
        ProxyOptions options2 = new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("localhost", 8080))
            .setCredentials("2", "2");

        AtomicReference<ChallengeHolder> challengeHolder = new AtomicReference<>();

        DeferredHttpProxyProvider provider1 = new DeferredHttpProxyProvider(handler1, challengeHolder, options1);
        DeferredHttpProxyProvider provider2 = new DeferredHttpProxyProvider(handler2, challengeHolder, options2);
        return Stream.of(
            // Other is itself.
            Arguments.of(provider1, provider1, true),
            Arguments.of(provider2, provider2, true),

            // Other is a different type.
            Arguments.of(provider1, 1, false),
            Arguments.of(provider2, 2, false),

            // Other has different values.
            Arguments.of(provider1, provider2, false),
            Arguments.of(provider2, provider1, false),

            // Other has same values.
            Arguments.of(provider1, new DeferredHttpProxyProvider(handler1, challengeHolder, options1), true),
            Arguments.of(provider2, new DeferredHttpProxyProvider(handler2, challengeHolder, options2), true)
        );
    }
}
