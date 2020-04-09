// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.core.credential.AzureKeyCredential;
import org.junit.jupiter.api.Test;

import static com.azure.ai.textanalytics.TestUtils.VALID_HTTPS_LOCALHOST;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for Text Analytics client builder
 */
public class TextAnalyticsClientBuilderUnitTest {

    /**
     * Test for missing endpoint when building an asynchronous client
     */
    @Test
    public void missingEndpointAsyncClient() {
        assertThrows(NullPointerException.class, () -> {
            final TextAnalyticsClientBuilder builder = new TextAnalyticsClientBuilder();
            builder.buildAsyncClient();
        });
    }

    /**
     * Test for missing endpoint when building a synchronous client
     */
    @Test
    public void missingEndpoint() {
        assertThrows(NullPointerException.class, () -> {
            final TextAnalyticsClientBuilder builder = new TextAnalyticsClientBuilder();
            builder.buildClient();
        });
    }

    /**
     * Test for invalid endpoint
     */
    @Test
    public void invalidProtocol() {
        assertThrows(IllegalArgumentException.class, () -> {
            final TextAnalyticsClientBuilder builder = new TextAnalyticsClientBuilder();
            builder.endpoint(TestUtils.INVALID_URL);
        });
    }

    /**
     * Test for null API key
     */
    @Test
    public void nullApiKey() {
        assertThrows(NullPointerException.class, () -> {
            final TextAnalyticsClientBuilder builder = new TextAnalyticsClientBuilder();
            builder.endpoint(VALID_HTTPS_LOCALHOST).apiKey(null);
        });
    }

    /**
     * Test for empty Api Key without any other authentication
     */
    @Test
    public void emptyApiKey() {
        assertThrows(IllegalArgumentException.class, () -> new AzureKeyCredential(""));
    }

    /**
     * Test for null AAD credential
     */
    @Test
    public void nullAADCredential() {
        assertThrows(NullPointerException.class, () -> {
            final TextAnalyticsClientBuilder builder = new TextAnalyticsClientBuilder();
            builder.endpoint(VALID_HTTPS_LOCALHOST).credential(null);
        });
    }
}
