// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.netty4;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Tests {@link NettyHttpClientProvider}.
 */
@Timeout(value = 3, unit = TimeUnit.MINUTES)
public class NettyHttpClientProviderTests {
    @Test
    public void testGetSharedClient() {
        NettyHttpClient httpClient = (NettyHttpClient) new NettyHttpClientProvider().getSharedInstance();

        assertSame(httpClient, new NettyHttpClientProvider().getSharedInstance());
    }

    @Test
    public void testGetNewClient() {
        NettyHttpClient httpClient = (NettyHttpClient) new NettyHttpClientProvider().getNewInstance();

        assertNotSame(httpClient, new NettyHttpClientProvider().getSharedInstance());
        assertNotSame(httpClient, new NettyHttpClientProvider().getNewInstance());
    }
}
