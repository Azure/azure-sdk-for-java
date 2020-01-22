// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.DetectLanguageResult;
import com.azure.ai.textanalytics.models.DetectedLanguage;
import com.azure.ai.textanalytics.models.TextAnalyticsSubscriptionKeyCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.test.TestBase;
import com.azure.core.util.Configuration;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.azure.ai.textanalytics.TextAnalyticsClientTestBase.validateDetectedLanguages;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TextAnalyticsClientBuilderTest extends TestBase {
    static final String AZURE_TEXT_ANALYTICS_SUBSCRIPTION_KEY = "AZURE_TEXT_ANALYTICS_SUBSCRIPTION_KEY";

    @Test
    public void rotateSharedKeyCredentialAsyncClient() {
        final String endpoint = getEndpoint();
        Objects.requireNonNull(endpoint, "`endpoint` expected to be set.");

        final String subscriptionKey = Configuration.getGlobalConfiguration()
            .get(AZURE_TEXT_ANALYTICS_SUBSCRIPTION_KEY);
        Objects.requireNonNull(subscriptionKey, "`AZURE_TEXT_ANALYTICS_SUBSCRIPTION_KEY` expected to be set.");

        final TextAnalyticsSubscriptionKeyCredential credential =
            new TextAnalyticsSubscriptionKeyCredential(subscriptionKey);

        final TextAnalyticsClientBuilder clientBuilder = new TextAnalyticsClientBuilder()
            .subscriptionKey(credential)
            .endpoint(endpoint);

        if (interceptorManager.isPlaybackMode()) {
            clientBuilder.httpClient(interceptorManager.getPlaybackClient());
        } else {
            clientBuilder.httpClient(new NettyAsyncHttpClientBuilder().wiretap(true).build())
                .addPolicy(interceptorManager.getRecordPolicy());
        }

        final TextAnalyticsAsyncClient client = clientBuilder.buildAsyncClient();

        DetectedLanguage primaryLanguage = new DetectedLanguage("English", "en", 1.0);
        List<DetectedLanguage> expectedLanguageList = Arrays.asList(primaryLanguage);
        StepVerifier.create(client.detectLanguage("This is a test English Text"))
            .assertNext(response -> validateDetectedLanguages(expectedLanguageList, response.getDetectedLanguages()))
            .verifyComplete();

        credential.updateCredential("invalid key");

        StepVerifier.create(client.detectLanguage("This is a test English Text"))
            .verifyError(HttpResponseException.class);
    }

    @Test
    public void rotateSharedKeyCredentialSyncClient() {
        final String endpoint = getEndpoint();
        Objects.requireNonNull(endpoint, "`endpoint` expected to be set.");

        final String subscriptionKey = Configuration.getGlobalConfiguration()
            .get(AZURE_TEXT_ANALYTICS_SUBSCRIPTION_KEY);
        Objects.requireNonNull(subscriptionKey, "`AZURE_TEXT_ANALYTICS_SUBSCRIPTION_KEY` expected to be set.");
        final TextAnalyticsSubscriptionKeyCredential credential =
            new TextAnalyticsSubscriptionKeyCredential(subscriptionKey);

        final TextAnalyticsClientBuilder clientBuilder = new TextAnalyticsClientBuilder()
            .subscriptionKey(credential)
            .endpoint(endpoint);

        if (interceptorManager.isPlaybackMode()) {
            clientBuilder.httpClient(interceptorManager.getPlaybackClient());
        } else {
            clientBuilder.httpClient(new NettyAsyncHttpClientBuilder().wiretap(true).build())
                .addPolicy(interceptorManager.getRecordPolicy());
        }

        final TextAnalyticsClient client = clientBuilder.buildClient();

        DetectedLanguage primaryLanguage = new DetectedLanguage("English", "en", 1.0);
        List<DetectedLanguage> expectedLanguageList = Arrays.asList(primaryLanguage);

        DetectLanguageResult detectLanguageResult = client.detectLanguage("This is a test English Text");
        validateDetectedLanguages(expectedLanguageList, detectLanguageResult.getDetectedLanguages());

        credential.updateCredential("invalid key");

        assertThrows(HttpResponseException.class, () -> client.detectLanguage("This is a test English Text"));
    }

    String getEndpoint() {
        return interceptorManager.isPlaybackMode()
            ? "http://localhost:8080"
            : Configuration.getGlobalConfiguration().get("AZURE_TEXT_ANALYTICS_ENDPOINT");
    }
}
