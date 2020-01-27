// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.DetectedLanguage;
import com.azure.ai.textanalytics.models.TextAnalyticsSubscriptionKeyCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.Configuration;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static com.azure.ai.textanalytics.TextAnalyticsClientTestBase.validateDetectedLanguages;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Client builder tests for the synchronous client
 */
public class TextAnalyticsClientBuilderSyncClientTest extends TestUtil {

    @Test
    public void validKey() {
        // Arrange
        final TextAnalyticsClient client = createClientBuilder(getEndpoint(),
            new TextAnalyticsSubscriptionKeyCredential(getSubscriptionKey())).buildClient();

        // Action and Assert
        validateDetectedLanguages(Arrays.asList(new DetectedLanguage("English", "en", 1.0)),
            client.detectLanguage("This is a test English Text").getDetectedLanguages());
    }

    @Test
    public void invalidKey() {
        // Arrange
        final TextAnalyticsClient client = createClientBuilder(getEndpoint(),
            new TextAnalyticsSubscriptionKeyCredential(INVALID_KEY)).buildClient();

        // Action and Assert
        assertThrows(HttpResponseException.class, () -> client.detectLanguage("This is a test English Text"));
    }

    @Test
    public void updateToInvalidKey() {
        // Arrange
        final TextAnalyticsSubscriptionKeyCredential credential =
            new TextAnalyticsSubscriptionKeyCredential(getSubscriptionKey());

        final TextAnalyticsClient client = createClientBuilder(getEndpoint(), credential).buildClient();

        // Update to invalid key
        credential.updateCredential(INVALID_KEY);

        // Action and Assert
        assertThrows(HttpResponseException.class, () -> client.detectLanguage("This is a test English Text"));
    }

    @Test
    public void updateToValidKey() {
        // Arrange
        final TextAnalyticsSubscriptionKeyCredential credential =
            new TextAnalyticsSubscriptionKeyCredential(INVALID_KEY);

        final TextAnalyticsClient client = createClientBuilder(getEndpoint(), credential).buildClient();

        // Update to valid key
        credential.updateCredential(getSubscriptionKey());

        // Action and Assert
        validateDetectedLanguages(Arrays.asList(new DetectedLanguage("English", "en", 1.0)),
            client.detectLanguage("This is a test English Text").getDetectedLanguages());
    }

    @Test
    public void missingEndpoint() {
        assertThrows(NullPointerException.class, () -> {
            final TextAnalyticsClientBuilder builder = new TextAnalyticsClientBuilder();
            builder.buildClient();
        });
    }

    @Test
    public void nullSubscriptionKey() {
        assertThrows(NullPointerException.class, () -> {
            final TextAnalyticsClientBuilder builder = new TextAnalyticsClientBuilder();
            builder.endpoint(getEndpoint()).subscriptionKey(null).buildClient();
        });
    }

    @Test
    public void nullAADCredential() {
        assertThrows(NullPointerException.class, () -> {
            final TextAnalyticsClientBuilder builder = new TextAnalyticsClientBuilder();
            builder.endpoint(getEndpoint()).credential(null).buildClient();
        });
    }

    @Test
    public void nullServiceVersion() {
        // Arrange
        final TextAnalyticsClientBuilder clientBuilder = new TextAnalyticsClientBuilder()
            .endpoint(getEndpoint())
            .subscriptionKey(new TextAnalyticsSubscriptionKeyCredential(getSubscriptionKey()))
            .retryPolicy(new RetryPolicy())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .serviceVersion(null);

        if (interceptorManager.isPlaybackMode()) {
            clientBuilder.httpClient(interceptorManager.getPlaybackClient());
        } else {
            clientBuilder.httpClient(new NettyAsyncHttpClientBuilder().wiretap(true).build())
                .addPolicy(interceptorManager.getRecordPolicy());
        }

        // Action and Assert
        validateDetectedLanguages(Arrays.asList(new DetectedLanguage("English", "en", 1.0)),
            clientBuilder.buildClient().detectLanguage("This is a test English Text").getDetectedLanguages());
    }

    @Test
    public void defaultPipeline() {
        // Arrange
        final TextAnalyticsClientBuilder clientBuilder = new TextAnalyticsClientBuilder()
            .endpoint(getEndpoint())
            .subscriptionKey(new TextAnalyticsSubscriptionKeyCredential(getSubscriptionKey()))
            .configuration(Configuration.getGlobalConfiguration())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));

        if (interceptorManager.isPlaybackMode()) {
            clientBuilder.httpClient(interceptorManager.getPlaybackClient());
        } else {
            clientBuilder.httpClient(new NettyAsyncHttpClientBuilder().wiretap(true).build())
                .addPolicy(interceptorManager.getRecordPolicy());
        }

        // Action and Assert
        validateDetectedLanguages(Arrays.asList(new DetectedLanguage("English", "en", 1.0)),
            clientBuilder.buildClient().detectLanguage("This is a test English Text").getDetectedLanguages());
    }
}
