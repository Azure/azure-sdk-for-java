// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.DetectedLanguage;
import com.azure.ai.textanalytics.models.LinkedEntity;
import com.azure.ai.textanalytics.models.NamedEntity;
import com.azure.ai.textanalytics.models.TextSentiment;

/**
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS ARE USED TO EXTRACT
 * APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING LINE NUMBERS OF EXISTING CODE
 * SAMPLES.
 *
 * Class containing code snippets that will be injected to README.md.
 */
public class ReadmeSamples {
    private static final String SUBSCRIPTION_KEY = null;
    private static final String ENDPOINT = null;

    /**
     * Code snippet for  getting sync client using subscription key authentication.
     *
     */
    public void useSubscriptionKeySyncClient() {
        TextAnalyticsClient textAnalyticsClient = new TextAnalyticsClientBuilder()
            .subscriptionKey(SUBSCRIPTION_KEY)
            .endpoint(ENDPOINT)
            .buildClient();
    }

    /**
     * Code snippet for getting async client using subscription key authentication.
     *
     */
    public void useSubscriptionKeyAsyncClient() {
        TextAnalyticsAsyncClient textAnalyticsClient = new TextAnalyticsClientBuilder()
            .subscriptionKey(SUBSCRIPTION_KEY)
            .endpoint(ENDPOINT)
            .buildAsyncClient();
    }

    /**
     * Code snippet for getting async client using AAD authentication.
     *
     */
    public void useAadAsyncClient() {
        TextAnalyticsAsyncClient textAnalyticsClient = new TextAnalyticsClientBuilder()
            .subscriptionKey(SUBSCRIPTION_KEY)
            .endpoint(ENDPOINT)
            .buildAsyncClient();
    }

    /**
     * Code snippet for detecting language in a text.
     *
     */
    public void detectLanguages() {
        TextAnalyticsClient textAnalyticsClient = new TextAnalyticsClientBuilder()
            .subscriptionKey(SUBSCRIPTION_KEY)
            .endpoint(ENDPOINT)
            .buildClient();

        String inputText = "Bonjour tout le monde";

        for (DetectedLanguage detectedLanguage : textAnalyticsClient.detectLanguage(inputText).getDetectedLanguages()) {
            System.out.printf("Detected languages name: %s, ISO 6391 Name: %s, Score: %s.%n",
                detectedLanguage.getName(),
                detectedLanguage.getIso6391Name(),
                detectedLanguage.getScore());
        }
    }

    /**
     * Code snippet for recognizing named entity in a text.
     *
     */
    public void recognizeNamedEntity() {
        TextAnalyticsClient textAnalyticsClient = new TextAnalyticsClientBuilder()
            .subscriptionKey(SUBSCRIPTION_KEY)
            .endpoint(ENDPOINT)
            .buildClient();

        String text = "Satya Nadella is the CEO of Microsoft";

        for (NamedEntity entity : textAnalyticsClient.recognizeEntities(text).getNamedEntities()) {
            System.out.printf(
                "Recognized Named Entity: %s, Type: %s, Subtype: %s, Score: %s.%n",
                entity.getText(),
                entity.getType(),
                entity.getSubtype(),
                entity.getScore());
        }
    }

    /**
     * Code snippet for recognizing pii entity in a text.
     *
     */
    public void recognizePiiEntity() {
        TextAnalyticsClient textAnalyticsClient = new TextAnalyticsClientBuilder()
            .subscriptionKey(SUBSCRIPTION_KEY)
            .endpoint(ENDPOINT)
            .buildClient();

        // The text that need be analysed.
        String text = "My SSN is 555-55-5555";

        for (NamedEntity entity : textAnalyticsClient.recognizePiiEntities(text).getNamedEntities()) {
            System.out.printf(
                "Recognized PII Entity: %s, Type: %s, Subtype: %s, Score: %s.%n",
                entity.getText(),
                entity.getType(),
                entity.getSubtype(),
                entity.getScore());
        }
    }

    /**
     * Code snippet for recognizing linked entity in a text.
     *
     */
    public void recognizeLinkedEntity() {
        TextAnalyticsClient textAnalyticsClient = new TextAnalyticsClientBuilder()
            .subscriptionKey(SUBSCRIPTION_KEY)
            .endpoint(ENDPOINT)
            .buildClient();

        // The text that need be analysed.
        String text = "Old Faithful is a geyser at Yellowstone Park.";

        for (LinkedEntity linkedEntity : textAnalyticsClient.recognizeLinkedEntities(text).getLinkedEntities()) {
            System.out.printf("Recognized Linked Entity: %s, Url: %s, Data Source: %s.%n",
                linkedEntity.getName(),
                linkedEntity.getUrl(),
                linkedEntity.getDataSource());
        }
    }

    /**
     * Code snippet for analyzing sentiment of a text.
     *
     */
    public void analyzeSentiment() {
        TextAnalyticsClient textAnalyticsClient = new TextAnalyticsClientBuilder()
            .subscriptionKey(SUBSCRIPTION_KEY)
            .endpoint(ENDPOINT)
            .buildClient();

        String text = "The hotel was dark and unclean.";

        for (TextSentiment textSentiment : textAnalyticsClient.analyzeSentiment(text).getSentenceSentiments()) {
            System.out.printf(
                "Analyzed Sentence Sentiment class: %s.%n",
                textSentiment.getTextSentimentClass());
        }
    }
}
