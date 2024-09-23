// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.FixedDelayOptions;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.test.http.AssertingHttpClientBuilder;
import com.azure.core.util.Configuration;

import java.time.Duration;

/**
 * Common properties used in testing.
 */
final class TestUtils {
    static final String HUB_NAME = "Hub";

    static String getEndpoint() {
        return Configuration.getGlobalConfiguration()
            .get("WEB_PUB_SUB_ENDPOINT", "http://testendpoint.webpubsubdev.azure.com");
    }

    static String getConnectionString() {
        return Configuration.getGlobalConfiguration()
            .get("WEB_PUB_SUB_CONNECTION_STRING", "Endpoint=https://testendpoint.webpubsubdev.azure.com;AccessKey=LoremIpsumDolorSitAmetConsectetur;Version=1.0;");
    }

    static RetryOptions getRetryOptions() {
        return new RetryOptions(new FixedDelayOptions(3, Duration.ofSeconds(20)));
    }

    static HttpClient buildAsyncAssertingClient(HttpClient httpClient) {
        return new AssertingHttpClientBuilder(httpClient)
            .assertAsync()
            .skipRequest((httpRequest, context) -> false)
            .build();
    }

    private TestUtils() {
    }
}
