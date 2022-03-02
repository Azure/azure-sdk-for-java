// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty;

import com.azure.core.http.HttpClient;
import com.azure.core.util.Configuration;
import com.azure.core.util.HttpClientOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@Execution(ExecutionMode.SAME_THREAD) // because singleton http client, it can avoid race condition.
public class NettyAsyncHttpClientSingletonTests {
    @Test
    public void testSingletonClientInstanceCreation() {
        HttpClient client1 = new NettyAsyncHttpClientProvider().createInstance();
        HttpClient client2 = new NettyAsyncHttpClientProvider().createInstance();
        assertEquals(client1, client2);
    }

    @Test
    public void testNonDefaultClientInstanceCreation() {
        Configuration.getGlobalConfiguration().put("AZURE_DISABLE_DEFAULT_SHARING_HTTP_CLIENT", "true");
        HttpClient client1 = new NettyAsyncHttpClientProvider().createInstance();
        HttpClient client2 = new NettyAsyncHttpClientProvider().createInstance();
        assertNotEquals(client1, client2);
        Configuration.getGlobalConfiguration().put("AZURE_DISABLE_DEFAULT_SHARING_HTTP_CLIENT", "false");
    }

    @Test
    public void testCustomizedClientInstanceCreationNotShared() {
        HttpClientOptions clientOptions = new HttpClientOptions().setMaximumConnectionPoolSize(500);
        HttpClient client1 = new NettyAsyncHttpClientProvider().createInstance(clientOptions);
        HttpClient client2 = new NettyAsyncHttpClientProvider().createInstance(clientOptions);
        assertNotEquals(client1, client2);
    }

    @Test
    public void testNullHttpClientOptionsInstanceCreation() {
        HttpClient client1 = new NettyAsyncHttpClientProvider().createInstance(null);
        HttpClient client2 = new NettyAsyncHttpClientProvider().createInstance(null);
        assertEquals(client1, client2);
    }
}
