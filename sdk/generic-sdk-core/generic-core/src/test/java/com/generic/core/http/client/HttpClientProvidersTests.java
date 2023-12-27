// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class HttpClientProvidersTests {
    @Test
    public void testNoProvider() {
        HttpClient httpClient = HttpClient.createDefault();

        assertInstanceOf(DefaultHttpClient.class, httpClient);
    }
}
