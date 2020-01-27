package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.TextAnalyticsSubscriptionKeyCredential;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.test.TestBase;
import com.azure.core.util.Configuration;

/**
 * The test util class that could share test function for all other tests for synchronous or asynchronous client.
 */
public class TestUtil extends TestBase {
    private static final String AZURE_TEXT_ANALYTICS_SUBSCRIPTION_KEY = "AZURE_TEXT_ANALYTICS_SUBSCRIPTION_KEY";
    static final String INVALID_KEY = "invalid key";

    /**
     * Create a client builder with endpoint and subscription key credential.
     *
     * @param endpoint the given endpoint
     * @param credential the given {@link TextAnalyticsSubscriptionKeyCredential} credential
     * @return {@link TextAnalyticsClientBuilder}
     */
    TextAnalyticsClientBuilder createClientBuilder(String endpoint, TextAnalyticsSubscriptionKeyCredential credential) {
        final TextAnalyticsClientBuilder clientBuilder = new TextAnalyticsClientBuilder()
            .subscriptionKey(credential)
            .endpoint(endpoint);

        if (interceptorManager.isPlaybackMode()) {
            clientBuilder.httpClient(interceptorManager.getPlaybackClient());
        } else {
            clientBuilder.httpClient(new NettyAsyncHttpClientBuilder().wiretap(true).build())
                .addPolicy(interceptorManager.getRecordPolicy());
        }

        return clientBuilder;
    }

    /**
     * Get the endpoint based on what running mode is on.
     *
     * @return endpoint string
     */
    String getEndpoint() {
        return interceptorManager.isPlaybackMode()
            ? "http://localhost:8080"
            : Configuration.getGlobalConfiguration().get("AZURE_TEXT_ANALYTICS_ENDPOINT");
    }

    /**
     * Get the string of subscription key value based on what running mode is on.
     *
     * @return the subscription key string
     */
    String getSubscriptionKey() {
        return interceptorManager.isPlaybackMode() ? "subscriptionKeyInPlayback"
            : Configuration.getGlobalConfiguration().get(AZURE_TEXT_ANALYTICS_SUBSCRIPTION_KEY);
    }
}
