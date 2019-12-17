// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

/**
 * Code snippet for {@link TextAnalyticsClient}
 */
public class TextAnalyticsClientJavaDocCodeSnippets {
    private static final String SUBSCRIPTION_KEY = null;
    private static final String ENDPOINT = null;

    /**
     * Code snippet for creating a {@link TextAnalyticsClient}
     *
     * @return The TextAnalyticsClient object.
     */
    public TextAnalyticsClient createTextAnalyticsClient() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.instantiation
        TextAnalyticsClient textAnalyticsClient = new TextAnalyticsClientBuilder()
            .subscriptionKey(SUBSCRIPTION_KEY)
            .endpoint(ENDPOINT)
            .buildClient();
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.instantiation
        return textAnalyticsClient;
    }
}
