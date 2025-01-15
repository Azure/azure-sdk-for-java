// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.okhttp3;

import io.clientcore.core.http.client.HttpClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class OkHttpHttpClientSingletonTests {
    @Test
    public void testGetSharedInstance() {
        HttpClient client1 = new OkHttpHttpClientProvider().getSharedInstance();
        HttpClient client2 = new OkHttpHttpClientProvider().getSharedInstance();

        assertEquals(client1, client2);
    }

    @Test
    public void testGetNewInstance() {
        HttpClient client1 = new OkHttpHttpClientProvider().getNewInstance();
        HttpClient client2 = new OkHttpHttpClientProvider().getNewInstance();

        assertNotEquals(client1, client2);
    }
}
