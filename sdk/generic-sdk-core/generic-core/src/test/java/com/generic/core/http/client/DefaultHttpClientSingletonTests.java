// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.client;

import com.generic.core.implementation.http.client.DefaultHttpClientProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@Execution(ExecutionMode.SAME_THREAD) // Avoiding race conditions caused by singleton
public class DefaultHttpClientSingletonTests {
    @Test
    public void testSingletonClientInstanceCreation() {
        HttpClient client1 = new DefaultHttpClientProvider().getSharedInstance();
        HttpClient client2 = new DefaultHttpClientProvider().getSharedInstance();

        assertEquals(client1, client2);
    }

    @Test
    public void testNonDefaultClientInstanceCreation() {
        HttpClient client1 = new DefaultHttpClientProvider().getNewInstance();
        HttpClient client2 = new DefaultHttpClientProvider().getNewInstance();

        assertNotEquals(client1, client2);
    }
}
