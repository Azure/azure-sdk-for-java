// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

/**
 * Code snippet for {@link TextAnalyticsAsyncClient}
 */
public class TextAnalyticsAsyncClientJavaDocCodeSnippets {
    private static final String SUBSCRIPTION_KEY = null;
    private static final String ENDPOINT = null;

    /**
     * Code snippet for creating a {@link TextAnalyticsAsyncClient}
     *
     * @return The TextAnalyticsAsyncClient object
     */
    public TextAnalyticsAsyncClient createTextAnalyticsAsyncClient() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.instantiation
        TextAnalyticsAsyncClient textAnalyticsAsyncClient = new TextAnalyticsClientBuilder()
            .subscriptionKey(SUBSCRIPTION_KEY)
            .endpoint(ENDPOINT)
            .buildAsyncClient();
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.instantiation
        return textAnalyticsAsyncClient;
    }
}
