// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.okhttp;

import com.azure.core.http.HttpClient;
import com.azure.core.util.Configuration;
import com.azure.core.util.HttpClientOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@Execution(ExecutionMode.SAME_THREAD) // because singleton http client, it can avoid race condition.
public class OkHttpAsyncHttpClientSingletonTests {
    @Test
    public void testSingletonClientInstanceCreation() {
        Configuration configuration = new Configuration().put("AZURE_ENABLE_HTTP_CLIENT_SHARING", "true");
        HttpClient client1 = new OkHttpAsyncClientProvider(configuration).createInstance();
        HttpClient client2 = new OkHttpAsyncClientProvider(configuration).createInstance();
        assertEquals(client1, client2);
    }

    @Test
    public void testNonDefaultClientInstanceCreation() {
        Configuration configuration = new Configuration().put("AZURE_ENABLE_HTTP_CLIENT_SHARING", "false");
        HttpClient client1 = new OkHttpAsyncClientProvider(configuration).createInstance();
        HttpClient client2 = new OkHttpAsyncClientProvider(configuration).createInstance();
        assertNotEquals(client1, client2);
    }

    @Test
    public void testCustomizedClientInstanceCreationNotShared() {
        Configuration configuration = new Configuration().put("AZURE_ENABLE_HTTP_CLIENT_SHARING", "false");
        HttpClientOptions clientOptions = new HttpClientOptions().setMaximumConnectionPoolSize(500);
        HttpClient client1 = new OkHttpAsyncClientProvider(configuration).createInstance(clientOptions);
        HttpClient client2 = new OkHttpAsyncClientProvider(configuration).createInstance(clientOptions);
        assertNotEquals(client1, client2);
    }

    @Test
    public void testNullHttpClientOptionsInstanceCreation() {
        Configuration configuration = new Configuration().put("AZURE_ENABLE_HTTP_CLIENT_SHARING", "true");
        HttpClient client1 = new OkHttpAsyncClientProvider(configuration).createInstance(null);
        HttpClient client2 = new OkHttpAsyncClientProvider(configuration).createInstance(null);
        assertEquals(client1, client2);
    }
}
