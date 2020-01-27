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
import reactor.test.StepVerifier;

import java.util.Arrays;

import static com.azure.ai.textanalytics.TextAnalyticsClientTestBase.validateDetectedLanguages;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Client builder tests for the asynchronous client
 */
public class TextAnalyticsClientBuilderAsyncClientTest extends TestUtil {

    @Test
    public void validKey() {
        // Arrange
        final TextAnalyticsAsyncClient client = createClientBuilder(getEndpoint(),
            new TextAnalyticsSubscriptionKeyCredential(getSubscriptionKey())).buildAsyncClient();

        // Action and Assert
        StepVerifier.create(client.detectLanguage("This is a test English Text"))
            .assertNext(response -> validateDetectedLanguages(
                Arrays.asList(new DetectedLanguage("English", "en", 1.0)),
                response.getDetectedLanguages()))
            .verifyComplete();
    }

    @Test
    public void invalidKey() {
        // Arrange
        final TextAnalyticsAsyncClient client = createClientBuilder(getEndpoint(),
            new TextAnalyticsSubscriptionKeyCredential(INVALID_KEY)).buildAsyncClient();

        // Action and Assert
        StepVerifier.create(client.detectLanguage("This is a test English Text"))
            .verifyError(HttpResponseException.class);
    }

    @Test
    public void updateToInvalidKey() {
        // Arrange
        final TextAnalyticsSubscriptionKeyCredential credential =
            new TextAnalyticsSubscriptionKeyCredential(getSubscriptionKey());

        final TextAnalyticsAsyncClient client = createClientBuilder(getEndpoint(), credential).buildAsyncClient();

        // Update to invalid key
        credential.updateCredential(INVALID_KEY);

        // Action and Assert
        StepVerifier.create(client.detectLanguage("This is a test English Text"))
            .verifyError(HttpResponseException.class);
    }

    @Test
    public void updateToValidKey() {
        // Arrange
        final TextAnalyticsSubscriptionKeyCredential credential =
            new TextAnalyticsSubscriptionKeyCredential(INVALID_KEY);

        final TextAnalyticsAsyncClient client = createClientBuilder(getEndpoint(), credential).buildAsyncClient();

        // Update to valid key
        credential.updateCredential(getSubscriptionKey());

        // Action and Assert
        StepVerifier.create(client.detectLanguage("This is a test English Text"))
            .assertNext(response -> validateDetectedLanguages(
                Arrays.asList(new DetectedLanguage("English", "en", 1.0)),
                response.getDetectedLanguages()))
            .verifyComplete();
    }

    @Test
    public void missingEndpoint() {
        assertThrows(NullPointerException.class, () -> {
            final TextAnalyticsClientBuilder builder = new TextAnalyticsClientBuilder();
            builder.buildAsyncClient();
        });
    }

    @Test
    public void nullSubscriptionKey() {
        assertThrows(NullPointerException.class, () -> {
            final TextAnalyticsClientBuilder builder = new TextAnalyticsClientBuilder();
            builder.endpoint(getEndpoint()).subscriptionKey(null).buildAsyncClient();
        });
    }

    @Test
    public void nullAADCredential() {
        assertThrows(NullPointerException.class, () -> {
            final TextAnalyticsClientBuilder builder = new TextAnalyticsClientBuilder();
            builder.endpoint(getEndpoint()).credential(null).buildAsyncClient();
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
        StepVerifier.create(clientBuilder.buildAsyncClient().detectLanguage("This is a test English Text"))
            .assertNext(response -> validateDetectedLanguages(
                Arrays.asList(new DetectedLanguage("English", "en", 1.0)),
                response.getDetectedLanguages()))
            .verifyComplete();
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
        StepVerifier.create(clientBuilder.buildAsyncClient().detectLanguage("This is a test English Text"))
            .assertNext(response -> validateDetectedLanguages(
                Arrays.asList(new DetectedLanguage("English", "en", 1.0)),
                response.getDetectedLanguages()))
            .verifyComplete();
    }
}
