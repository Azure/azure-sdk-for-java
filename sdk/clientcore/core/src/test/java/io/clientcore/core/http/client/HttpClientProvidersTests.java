// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class HttpClientProvidersTests {
    @Test
    public void testNoProvider() {
        HttpClient httpClient = HttpClient.getSharedInstance();

        assertInstanceOf(DefaultHttpClient.class, httpClient);
    }
}
