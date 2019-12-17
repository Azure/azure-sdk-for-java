// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

/**
 * Code snippet for {@link TextAnalyticsClient}
 */
public class TextAnalyticsClientJavaDocCodeSnippets {
    private static final String subscriptionKey = null;
    private static final String endpoint = null;

    /**
     * Code snippet for creating a {@link TextAnalyticsClient}
     */
    public TextAnalyticsClient createTextAnalyticsClient() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.instantiation
        TextAnalyticsClient textAnalyticsClient = new TextAnalyticsClientBuilder()
            .subscriptionKey(subscriptionKey)
            .endpoint(endpoint)
            .buildClient();
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.instantiation
        return textAnalyticsClient;
    }
}
