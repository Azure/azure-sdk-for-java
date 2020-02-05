// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.DetectLanguageInput;
import com.azure.ai.textanalytics.models.DetectedLanguage;
import com.azure.ai.textanalytics.models.LinkedEntity;
import com.azure.ai.textanalytics.models.CategorizedEntity;
import com.azure.ai.textanalytics.models.PiiEntity;
import com.azure.ai.textanalytics.models.TextAnalyticsApiKeyCredential;
import com.azure.ai.textanalytics.models.TextSentiment;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.Arrays;
import java.util.List;

/**
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS ARE USED TO EXTRACT
 * APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING LINE NUMBERS OF EXISTING CODE
 * SAMPLES.
 *
 * Class containing code snippets that will be injected to README.md.
 */
public class ReadmeSamples {
    private TextAnalyticsClient textAnalyticsClient = new TextAnalyticsClientBuilder().buildClient();

    /**
     * Code snippet for configuring http client.
     */
    public void configureHttpClient() {
        HttpClient client = new NettyAsyncHttpClientBuilder()
            .port(8080)
            .wiretap(true)
            .build();
    }

    /**
     * Code snippet for  getting sync client using subscription key authentication.
     */
    public void useSubscriptionKeySyncClient() {
        TextAnalyticsClient textAnalyticsClient = new TextAnalyticsClientBuilder()
            .subscriptionKey(new TextAnalyticsApiKeyCredential("{subscription_key}"))
            .endpoint("{endpoint}")
            .buildClient();
    }

    /**
     * Code snippet for getting async client using subscription key authentication.
     */
    public void useSubscriptionKeyAsyncClient() {
        TextAnalyticsAsyncClient textAnalyticsClient = new TextAnalyticsClientBuilder()
            .subscriptionKey(new TextAnalyticsApiKeyCredential("{subscription_key}"))
            .endpoint("{endpoint}")
            .buildAsyncClient();
    }

    /**
     * Code snippet for getting async client using AAD authentication.
     */
    public void useAadAsyncClient() {
        TextAnalyticsAsyncClient textAnalyticsClient = new TextAnalyticsClientBuilder()
            .endpoint("{endpoint}")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();
    }

    /**
     * Code snippet for detecting language in a text.
     */
    public void detectLanguages() {
        String inputText = "Bonjour tout le monde";

        for (DetectedLanguage detectedLanguage : textAnalyticsClient.detectLanguage(inputText).getDetectedLanguages()) {
            System.out.printf("Detected languages name: %s, ISO 6391 Name: %s, Score: %s.%n",
                detectedLanguage.getName(),
                detectedLanguage.getIso6391Name(),
                detectedLanguage.getScore());
        }
    }

    /**
     * Code snippet for recognizing Category entity in a text.
     */
    public void recognizeCategorizedEntity() {
        String text = "Satya Nadella is the CEO of Microsoft";

        for (CategorizedEntity entity : textAnalyticsClient.recognizeEntities(text).getEntities()) {
            System.out.printf(
                "Recognized Categorized Entity: %s, Category: %s, SubCategory: %s, Score: %s.%n",
                entity.getText(),
                entity.getCategory(),
                entity.getSubCategory(),
                entity.getScore());
        }
    }

    /**
     * Code snippet for recognizing PII entity in a text.
     */
    public void recognizePiiEntity() {
        String text = "My SSN is 555-55-5555";

        for (PiiEntity entity : textAnalyticsClient.recognizePiiEntities(text).getEntities()) {
            System.out.printf(
                "Recognized PII Entity: %s, Category: %s, SubCategory: %s, Score: %s.%n",
                entity.getText(),
                entity.getCategory(),
                entity.getSubCategory(),
                entity.getScore());
        }
    }

    /**
     * Code snippet for recognizing linked entity in a text.
     */
    public void recognizeLinkedEntity() {
        String text = "Old Faithful is a geyser at Yellowstone Park.";

        for (LinkedEntity linkedEntity : textAnalyticsClient.recognizeLinkedEntities(text).getLinkedEntities()) {
            System.out.printf("Recognized Linked Entity: %s, Url: %s, Data Source: %s.%n",
                linkedEntity.getName(),
                linkedEntity.getUrl(),
                linkedEntity.getDataSource());
        }
    }

    /**
     * Code snippet for extracting key phrases in a text.
     */
    public void extractKeyPhrases() {
        String text = "My cat might need to see a veterinarian.";

        for (String keyPhrase : textAnalyticsClient.extractKeyPhrases(text).getKeyPhrases()) {
            System.out.printf("Recognized phrases: %s.%n", keyPhrase);
        }
    }

    /**
     * Code snippet for analyzing sentiment of a text.
     */
    public void analyzeSentiment() {
        String text = "The hotel was dark and unclean.";

        for (TextSentiment textSentiment : textAnalyticsClient.analyzeSentiment(text).getSentenceSentiments()) {
            System.out.printf(
                "Analyzed Sentence Sentiment class: %s.%n",
                textSentiment.getTextSentimentClass());
        }
    }

    /**
     * Code snippet for handling exception
     */
    public void handlingException() {
        List<DetectLanguageInput> inputs = Arrays.asList(
            new DetectLanguageInput("1", "This is written in English.", "us"),
            new DetectLanguageInput("1", "Este es un document escrito en Español.", "es")
        );

        try {
            textAnalyticsClient.detectBatchLanguages(inputs);
        } catch (HttpResponseException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Code snippet for rotating subscription key of the client
     */
    public void rotatingSubscriptionKey() {
        TextAnalyticsApiKeyCredential credential = new TextAnalyticsApiKeyCredential("{expired_subscription_key}");
        TextAnalyticsClient textAnalyticsClient = new TextAnalyticsClientBuilder()
            .subscriptionKey(credential)
            .endpoint("{endpoint}")
            .buildClient();

        credential.updateCredential("{new_subscription_key}");
    }
}
