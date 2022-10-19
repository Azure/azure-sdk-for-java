// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.ExponentialBackoffOptions;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.http.policy.RetryPolicy;
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
     * Test for empty Api Key without any other authentication
     */
    @Test
    public void emptyApiKey() {
        assertThrows(IllegalArgumentException.class, () -> new AzureKeyCredential(""));
    }

    /**
     * Test for null API key
     */
    @Test
    public void nullApiKey() {
        AzureKeyCredential azureKeyCredential = null;
        assertThrows(NullPointerException.class, () -> {
            final TextAnalyticsClientBuilder builder = new TextAnalyticsClientBuilder();
            builder.endpoint(VALID_HTTPS_LOCALHOST).credential(azureKeyCredential);
        });
    }

    /**
     * Test for null AAD credential
     */
    @Test
    public void nullAADCredential() {
        TokenCredential tokenCredential = null;
        assertThrows(NullPointerException.class, () -> {
            final TextAnalyticsClientBuilder builder = new TextAnalyticsClientBuilder();
            builder.endpoint(VALID_HTTPS_LOCALHOST).credential(tokenCredential);
        });
    }

    /**
     * Thest for the case where both retryOptions and retryPolicy are set.
     */
    @Test
    public void bothRetryOptionsAndRetryPolicySet() {
        assertThrows(IllegalStateException.class, () -> new TextAnalyticsClientBuilder()
            .endpoint(VALID_HTTPS_LOCALHOST)
            .credential(new AzureKeyCredential("foo"))
            .retryOptions(new RetryOptions(new ExponentialBackoffOptions()))
            .retryPolicy(new RetryPolicy())
            .buildClient());
    }
}
