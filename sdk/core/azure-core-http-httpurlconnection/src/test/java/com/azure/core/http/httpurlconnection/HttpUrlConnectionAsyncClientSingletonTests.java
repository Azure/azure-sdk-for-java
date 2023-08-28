// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.httpurlconnection;

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
public class HttpUrlConnectionAsyncClientSingletonTests {
    // ** To be implemented once configurations such as client sharing are implemented ** //
//    @Test
//    public void testSingletonClientInstanceCreation() {
//        HttpUrlConnectionClient client1 = (HttpUrlConnectionClient) new HttpUrlConnectionClientProvider().createInstance();
//        HttpUrlConnectionClient client2 = (HttpUrlConnectionClient) new HttpUrlConnectionClientProvider().createInstance();
//        assertEquals(client1, client2);
//    }
//
//    @Test
//    public void testNullHttpClientOptionsInstanceCreation() {
//        HttpUrlConnectionClient client1 = (HttpUrlConnectionClient) new HttpUrlConnectionClientProvider().createInstance(null);
//        HttpUrlConnectionClient client2 = (HttpUrlConnectionClient) new HttpUrlConnectionClientProvider().createInstance(null);
//        assertEquals(client1, client2);
//    }
}
