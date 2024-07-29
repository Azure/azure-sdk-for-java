// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.jdk.httpclient;

import io.clientcore.core.http.client.HttpClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class JdkHttpClientSingletonTests {
    @Test
    public void testGetSharedInstance() {
        HttpClient client1 = new JdkHttpClientProvider().getSharedInstance();
        HttpClient client2 = new JdkHttpClientProvider().getSharedInstance();

        assertEquals(client1, client2);
    }

    @Test
    public void testGetNewInstance() {
        HttpClient client1 = new JdkHttpClientProvider().getNewInstance();
        HttpClient client2 = new JdkHttpClientProvider().getNewInstance();

        assertNotEquals(client1, client2);
    }
}
