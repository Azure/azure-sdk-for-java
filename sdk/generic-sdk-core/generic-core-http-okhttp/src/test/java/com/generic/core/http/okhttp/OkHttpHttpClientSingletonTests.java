// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.okhttp;

import com.generic.core.http.client.HttpClient;
import com.generic.core.implementation.test.TestConfigurationSource;
import com.generic.core.util.configuration.Configuration;
import com.generic.core.util.configuration.ConfigurationBuilder;
import com.generic.core.util.configuration.ConfigurationSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@Execution(ExecutionMode.SAME_THREAD) // because singleton http client, it can avoid race condition.
public class OkHttpHttpClientSingletonTests {
    private static final ConfigurationSource EMPTY_SOURCE = new TestConfigurationSource();

    @Test
    public void testSingletonClientInstanceCreation() {
        Configuration configuration = getConfiguration(true);
        HttpClient client1 = new OkHttpHttpClientProvider(configuration).createInstance();
        HttpClient client2 = new OkHttpHttpClientProvider(configuration).createInstance();

        assertEquals(client1, client2);
    }

    @Test
    public void testNonDefaultClientInstanceCreation() {
        Configuration configuration = getConfiguration(false);
        HttpClient client1 = new OkHttpHttpClientProvider(configuration).createInstance();
        HttpClient client2 = new OkHttpHttpClientProvider(configuration).createInstance();

        assertNotEquals(client1, client2);
    }

    private static Configuration getConfiguration(boolean enableSharing) {
        return new ConfigurationBuilder(EMPTY_SOURCE, EMPTY_SOURCE, new TestConfigurationSource()
            .put("ENABLE_HTTP_CLIENT_SHARING", Boolean.toString(enableSharing)))
            .build();
    }
}
