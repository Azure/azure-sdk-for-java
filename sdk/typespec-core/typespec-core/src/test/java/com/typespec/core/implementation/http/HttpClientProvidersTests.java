// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.implementation.http;

import com.typespec.core.http.HttpClient;
import com.typespec.core.http.HttpClientProvider;
import com.typespec.core.util.HttpClientOptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class HttpClientProvidersTests {
    @Test
    public void testNoProviders() {
        assertThrows(IllegalStateException.class, () -> HttpClient.createDefault());
    }

    @Test
    public void testIncorrectExplicitProvider() {
        HttpClientOptions options = new HttpClientOptions();
        options.setHttpClientProvider(AnotherHttpClientProvider.class);
        assertThrows(IllegalStateException.class, () -> HttpClient.createDefault(options));
    }

    class AnotherHttpClientProvider implements HttpClientProvider {
        @Override
        public HttpClient createInstance() {
            throw new IllegalStateException("should never be called");
        }
    }
}
