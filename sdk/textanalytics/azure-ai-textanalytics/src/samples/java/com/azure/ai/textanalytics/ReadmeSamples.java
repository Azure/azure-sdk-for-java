// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.CategorizedEntity;
import com.azure.ai.textanalytics.models.DetectLanguageInput;
import com.azure.ai.textanalytics.models.DetectedLanguage;
import com.azure.ai.textanalytics.models.DocumentSentiment;
import com.azure.ai.textanalytics.models.LinkedEntity;
import com.azure.ai.textanalytics.models.LinkedEntityMatch;
import com.azure.ai.textanalytics.models.PiiEntity;
import com.azure.ai.textanalytics.models.SentenceSentiment;
import com.azure.ai.textanalytics.models.TextAnalyticsApiKeyCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.util.Context;
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
     * Code snippet for getting sync client using the API key authentication.
     */
    public void useApiKeySyncClient() {
        TextAnalyticsClient textAnalyticsClient = new TextAnalyticsClientBuilder()
            .apiKey(new TextAnalyticsApiKeyCredential("{api_key}"))
            .endpoint("{endpoint}")
            .buildClient();
    }

    /**
     * Code snippet for getting async client using API key authentication.
     */
    public void useApiKeyAsyncClient() {
        TextAnalyticsAsyncClient textAnalyticsClient = new TextAnalyticsClientBuilder()
            .apiKey(new TextAnalyticsApiKeyCredential("{api_key}"))
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
        DetectedLanguage detectedLanguage = textAnalyticsClient.detectLanguage(inputText);
        System.out.printf("Detected language name: %s, ISO 6391 name: %s, score: %.2f.%n",
            detectedLanguage.getName(), detectedLanguage.getIso6391Name(), detectedLanguage.getScore());
    }

    /**
     * Code snippet for recognizing category entity in a text.
     */
    public void recognizeCategorizedEntity() {
        String text = "Satya Nadella is the CEO of Microsoft";
        for (CategorizedEntity entity : textAnalyticsClient.recognizeEntities(text)) {
            System.out.printf("Recognized categorized entity: %s, category: %s, subCategory: %s, score: %.2f.%n",
                entity.getText(), entity.getCategory(), entity.getSubCategory(), entity.getScore());
        }
    }

    /**
     * Code snippet for recognizing Personally Identifiable Information entity in a text.
     */
    public void recognizePiiEntity() {
        String text = "My SSN is 555-55-5555";
        for (PiiEntity entity : textAnalyticsClient.recognizePiiEntities(text)) {
            System.out.printf("Recognized Personally Identifiable Information entity: %s, category: %s, subCategory: %s, score: %.2f.%n",
                entity.getText(), entity.getCategory(), entity.getSubCategory(), entity.getScore());
        }
    }

    /**
     * Code snippet for recognizing linked entity in a text.
     */
    public void recognizeLinkedEntity() {
        String text = "Old Faithful is a geyser at Yellowstone Park.";
        for (LinkedEntity linkedEntity : textAnalyticsClient.recognizeLinkedEntities(text)) {
            System.out.println("Linked Entities:");
            System.out.printf("Name: %s, entity ID in data source: %s, URL: %s, data source: %s.%n",
                linkedEntity.getName(), linkedEntity.getDataSourceEntityId(), linkedEntity.getUrl(), linkedEntity.getDataSource());
            for (LinkedEntityMatch linkedEntityMatch : linkedEntity.getLinkedEntityMatches()) {
                System.out.printf("Text: %s, offset: %s, length: %s, score: %.2f.%n", linkedEntityMatch.getText(),
                    linkedEntityMatch.getOffset(), linkedEntityMatch.getLength(), linkedEntityMatch.getScore());
            }
        }
    }

    /**
     * Code snippet for extracting key phrases in a text.
     */
    public void extractKeyPhrases() {
        String text = "My cat might need to see a veterinarian.";
        System.out.println("Extracted phrases:");
        for (String keyPhrase : textAnalyticsClient.extractKeyPhrases(text)) {
            System.out.printf("%s.%n", keyPhrase);
        }
    }

    /**
     * Code snippet for analyzing sentiment of a text.
     */
    public void analyzeSentiment() {
        String text = "The hotel was dark and unclean. I like microsoft.";
        DocumentSentiment documentSentiment = textAnalyticsClient.analyzeSentiment(text);
        System.out.printf("Analyzed document sentiment: %s.%n", documentSentiment.getSentiment());
        for (SentenceSentiment sentenceSentiment : documentSentiment.getSentences()) {
            System.out.printf("Analyzed sentence sentiment: %s.%n", sentenceSentiment.getSentiment());
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
            textAnalyticsClient.detectLanguageBatchWithResponse(inputs, null, Context.NONE);
        } catch (HttpResponseException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Code snippet for rotating API key of the client
     */
    public void rotatingApiKey() {
        TextAnalyticsApiKeyCredential credential = new TextAnalyticsApiKeyCredential("{api_key}");
        TextAnalyticsClient textAnalyticsClient = new TextAnalyticsClientBuilder()
            .apiKey(credential)
            .endpoint("{endpoint}")
            .buildClient();

        credential.updateCredential("{new_api_key}");
    }
}
