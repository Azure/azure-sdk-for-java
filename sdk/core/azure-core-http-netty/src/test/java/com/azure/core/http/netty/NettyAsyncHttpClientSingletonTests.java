// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty;

import com.azure.core.http.HttpClient;
import com.azure.core.test.utils.TestConfigurationSource;
import com.azure.core.util.Configuration;
import com.azure.core.util.ConfigurationBuilder;
import com.azure.core.util.ConfigurationSource;
import com.azure.core.util.HttpClientOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@Execution(ExecutionMode.SAME_THREAD) // because singleton http client, it can avoid race condition.
public class NettyAsyncHttpClientSingletonTests {
    private static final ConfigurationSource EMPTY_SOURCE = new TestConfigurationSource();

    @Test
    public void testSingletonClientInstanceCreation() {
        Configuration configuration = getConfiguration(true);

        HttpClient client1 = new NettyAsyncHttpClientProvider(configuration).createInstance();
        HttpClient client2 = new NettyAsyncHttpClientProvider(configuration).createInstance();
        assertEquals(client1, client2);
    }

    @Test
    public void testNonDefaultClientInstanceCreation() {
        Configuration configuration = getConfiguration(false);

        HttpClient client1 = new NettyAsyncHttpClientProvider(configuration).createInstance();
        HttpClient client2 = new NettyAsyncHttpClientProvider(configuration).createInstance();
        assertNotEquals(client1, client2);
    }

    @Test
    public void testCustomizedClientInstanceCreationNotShared() {
        Configuration configuration = getConfiguration(false);

        HttpClientOptions clientOptions = new HttpClientOptions().setMaximumConnectionPoolSize(500);
        HttpClient client1 = new NettyAsyncHttpClientProvider(configuration).createInstance(clientOptions);
        HttpClient client2 = new NettyAsyncHttpClientProvider(configuration).createInstance(clientOptions);
        assertNotEquals(client1, client2);
    }

    @Test
    public void testNullHttpClientOptionsInstanceCreation() {
        Configuration configuration = getConfiguration(true);
        HttpClient client1 = new NettyAsyncHttpClientProvider(configuration).createInstance(null);
        HttpClient client2 = new NettyAsyncHttpClientProvider(configuration).createInstance(null);
        assertEquals(client1, client2);
    }

    private static Configuration getConfiguration(boolean enableSharing) {
        return new ConfigurationBuilder(EMPTY_SOURCE, EMPTY_SOURCE, new TestConfigurationSource()
            .put("AZURE_ENABLE_HTTP_CLIENT_SHARING", Boolean.toString(enableSharing)))
            .build();
    }
}
