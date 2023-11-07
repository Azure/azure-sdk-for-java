// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.client;

import com.generic.core.http.client.httpurlconnection.HttpUrlConnectionClient;
import com.generic.core.http.client.httpurlconnection.HttpUrlConnectionClientProvider;
import com.generic.core.http.models.HttpClientOptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class HttpClientProvidersTests {
    @Test
    public void testHttpUrlConnectionAsDefaultProvider() {
        HttpClientOptions options = new HttpClientOptions();
        options.setHttpClientProvider(HttpUrlConnectionClientProvider.class);
        // sanity check
        HttpClient httpClient = HttpClient.createDefault(options);
        assertInstanceOf(HttpUrlConnectionClient.class, httpClient);
    }
}
