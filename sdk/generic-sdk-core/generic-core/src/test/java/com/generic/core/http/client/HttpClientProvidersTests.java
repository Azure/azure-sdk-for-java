// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class HttpClientProvidersTests {
    @Test
    public void testNoProvider() {
        HttpClient httpClient = HttpClient.createDefault();

        assertInstanceOf(DefaultHttpClient.class, httpClient);
    }

    @Test
    public void testIncorrectExplicitProvider() {
        DefaultHttpClientBuilder builder = new DefaultHttpClientBuilder();

        builder.setHttpClientProvider(new AnotherHttpClientProvider());

        assertThrows(IllegalStateException.class, builder::build);
    }

    class AnotherHttpClientProvider implements HttpClientProvider {
        @Override
        public HttpClient createInstance() {
            throw new IllegalStateException("Should never be called.");
        }
    }
}
