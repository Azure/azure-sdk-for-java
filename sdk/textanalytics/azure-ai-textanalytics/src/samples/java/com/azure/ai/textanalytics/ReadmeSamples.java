// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.DetectLanguageInput;
import com.azure.ai.textanalytics.models.DetectedLanguage;
import com.azure.ai.textanalytics.models.DocumentSentiment;
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

    /**
     * Code snippet for handling exception
     */
    public void handlingException() {
        List<DetectLanguageInput> documents = Arrays.asList(
            new DetectLanguageInput("1", "This is written in English.", "us"),
            new DetectLanguageInput("1", "Este es un documento  escrito en Español.", "es")
        );

        try {
            textAnalyticsClient.detectLanguageBatch(documents, null, Context.NONE);
        } catch (HttpResponseException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Code snippet for analyzing sentiment of a document.
     */
    public void analyzeSentiment() {
        String document = "The hotel was dark and unclean. I like microsoft.";
        DocumentSentiment documentSentiment = textAnalyticsClient.analyzeSentiment(document);
        System.out.printf("Analyzed document sentiment: %s.%n", documentSentiment.getSentiment());
        documentSentiment.getSentences().forEach(sentenceSentiment ->
            System.out.printf("Analyzed sentence sentiment: %s.%n", sentenceSentiment.getSentiment()));
    }

    /**
     * Code snippet for detecting language in a document.
     */
    public void detectLanguages() {
        String document = "Bonjour tout le monde";
        DetectedLanguage detectedLanguage = textAnalyticsClient.detectLanguage(document);
        System.out.printf("Detected language name: %s, ISO 6391 name: %s, score: %f.%n",
            detectedLanguage.getName(), detectedLanguage.getIso6391Name(), detectedLanguage.getScore());
    }

    /**
     * Code snippet for recognizing category entity in a document.
     */
    public void recognizeEntity() {
        String document = "Satya Nadella is the CEO of Microsoft";
        textAnalyticsClient.recognizeEntities(document).forEach(entity ->
            System.out.printf("Recognized entity: %s, category: %s, subCategory: %s, score: %f.%n",
                entity.getText(), entity.getCategory(), entity.getSubCategory(), entity.getConfidenceScore()));
    }

    /**
     * Code snippet for recognizing Personally Identifiable Information entity in a document.
     */
    public void recognizePiiEntity() {
        String document = "My SSN is 555-55-5555";
        textAnalyticsClient.recognizePiiEntities(document).forEach(piiEntity ->
            System.out.printf("Recognized Personally Identifiable Information entity: %s, category: %s, subCategory: %s, score: %f.%n",
                piiEntity.getText(), piiEntity.getCategory(), piiEntity.getSubCategory(), piiEntity.getConfidenceScore()));
    }

    /**
     * Code snippet for recognizing linked entity in a document.
     */
    public void recognizeLinkedEntity() {
        String document = "Old Faithful is a geyser at Yellowstone Park.";
        textAnalyticsClient.recognizeLinkedEntities(document).forEach(linkedEntity -> {
            System.out.println("Linked Entities:");
            System.out.printf("Name: %s, entity ID in data source: %s, URL: %s, data source: %s.%n",
                linkedEntity.getName(), linkedEntity.getDataSourceEntityId(), linkedEntity.getUrl(), linkedEntity.getDataSource());
            linkedEntity.getLinkedEntityMatches().forEach(linkedEntityMatch ->
                System.out.printf("Text: %s, score: %f.%n", linkedEntityMatch.getText(), linkedEntityMatch.getConfidenceScore()));
        });
    }

    /**
     * Code snippet for extracting key phrases in a document.
     */
    public void extractKeyPhrases() {
        String document = "My cat might need to see a veterinarian.";
        System.out.println("Extracted phrases:");
        textAnalyticsClient.extractKeyPhrases(document).forEach(keyPhrase -> System.out.printf("%s.%n", keyPhrase));
    }
}
