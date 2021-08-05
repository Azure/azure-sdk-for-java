// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.AnalyzeActionsOptions;
import com.azure.ai.textanalytics.models.AnalyzeHealthcareEntitiesOperationDetail;
import com.azure.ai.textanalytics.models.AnalyzeHealthcareEntitiesOptions;
import com.azure.ai.textanalytics.models.AnalyzeSentimentOptions;
import com.azure.ai.textanalytics.models.AssessmentSentiment;
import com.azure.ai.textanalytics.models.DetectLanguageInput;
import com.azure.ai.textanalytics.models.DetectLanguageResult;
import com.azure.ai.textanalytics.models.DetectedLanguage;
import com.azure.ai.textanalytics.models.DocumentSentiment;
import com.azure.ai.textanalytics.models.EntityDataSource;
import com.azure.ai.textanalytics.models.ExtractKeyPhraseResult;
import com.azure.ai.textanalytics.models.ExtractKeyPhrasesAction;
import com.azure.ai.textanalytics.models.HealthcareEntity;
import com.azure.ai.textanalytics.models.PiiEntityCollection;
import com.azure.ai.textanalytics.models.PiiEntityDomain;
import com.azure.ai.textanalytics.models.RecognizeEntitiesAction;
import com.azure.ai.textanalytics.models.RecognizePiiEntitiesOptions;
import com.azure.ai.textanalytics.models.SentenceSentiment;
import com.azure.ai.textanalytics.models.TargetSentiment;
import com.azure.ai.textanalytics.models.TextAnalyticsActions;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.util.AnalyzeSentimentResultCollection;
import com.azure.ai.textanalytics.util.DetectLanguageResultCollection;
import com.azure.ai.textanalytics.util.ExtractKeyPhrasesResultCollection;
import com.azure.ai.textanalytics.util.RecognizeEntitiesResultCollection;
import com.azure.ai.textanalytics.util.RecognizeLinkedEntitiesResultCollection;
import com.azure.ai.textanalytics.util.RecognizePiiEntitiesResultCollection;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.IterableStream;
import com.azure.core.util.polling.AsyncPollResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Code snippet for {@link TextAnalyticsAsyncClient}
 */
public class TextAnalyticsAsyncClientJavaDocCodeSnippets {
    TextAnalyticsAsyncClient textAnalyticsAsyncClient = createTextAnalyticsAsyncClient();

    /**
     * Code snippet for creating a {@link TextAnalyticsAsyncClient}
     *
     * @return The TextAnalyticsAsyncClient object
     */
    public TextAnalyticsAsyncClient createTextAnalyticsAsyncClient() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.instantiation
        TextAnalyticsAsyncClient textAnalyticsAsyncClient = new TextAnalyticsClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .buildAsyncClient();
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.instantiation
        return textAnalyticsAsyncClient;
    }

    /**
     * Code snippet for updating the existing API key.
     */
    public void rotateAzureKeyCredential() {
        // BEGIN: com.azure.ai.textanalytics.models.AzureKeyCredential
        AzureKeyCredential credential = new AzureKeyCredential("{key}");

        TextAnalyticsAsyncClient textAnalyticsAsyncClient = new TextAnalyticsClientBuilder()
            .credential(credential)
            .endpoint("{endpoint}")
            .buildAsyncClient();

        credential.update("{new_api_key}");
        // END: com.azure.ai.textanalytics.models.AzureKeyCredential
    }

    // Languages

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#detectLanguage(String)}
     */
    public void detectLanguage() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.detectLanguage#string
        String document = "Bonjour tout le monde";
        textAnalyticsAsyncClient.detectLanguage(document).subscribe(detectedLanguage ->
            System.out.printf("Detected language name: %s, ISO 6391 Name: %s, confidence score: %f.%n",
                detectedLanguage.getName(), detectedLanguage.getIso6391Name(), detectedLanguage.getConfidenceScore()));
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.detectLanguage#string
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#detectLanguage(String, String)}
     */
    public void detectLanguageWithCountryHint() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.detectLanguage#string-string
        String document = "This text is in English";
        String countryHint = "US";
        textAnalyticsAsyncClient.detectLanguage(document, countryHint).subscribe(detectedLanguage ->
            System.out.printf("Detected language name: %s, ISO 6391 Name: %s, confidence score: %f.%n",
                detectedLanguage.getName(), detectedLanguage.getIso6391Name(), detectedLanguage.getConfidenceScore()));
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.detectLanguage#string-string
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#detectLanguageBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    public void detectLanguageStringListWithOptions() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.detectLanguageBatch#Iterable-String-TextAnalyticsRequestOptions
        List<String> documents = Arrays.asList(
            "This is written in English",
            "Este es un documento  escrito en Español."
        );
        textAnalyticsAsyncClient.detectLanguageBatch(documents, "US", null).subscribe(
            batchResult -> {
                // Batch statistics
                TextDocumentBatchStatistics batchStatistics = batchResult.getStatistics();
                System.out.printf("Batch statistics, transaction count: %s, valid document count: %s.%n",
                    batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());
                // Batch result of languages
                for (DetectLanguageResult detectLanguageResult : batchResult) {
                    DetectedLanguage detectedLanguage = detectLanguageResult.getPrimaryLanguage();
                    System.out.printf("Detected language name: %s, ISO 6391 Name: %s, confidence score: %f.%n",
                        detectedLanguage.getName(), detectedLanguage.getIso6391Name(),
                        detectedLanguage.getConfidenceScore());
                }
            });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.detectLanguageBatch#Iterable-String-TextAnalyticsRequestOptions
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#detectLanguageBatchWithResponse(Iterable, TextAnalyticsRequestOptions)}
     */
    public void detectBatchLanguagesMaxOverload() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.detectLanguageBatch#Iterable-TextAnalyticsRequestOptions
        List<DetectLanguageInput> detectLanguageInputs1 = Arrays.asList(
            new DetectLanguageInput("1", "This is written in English.", "US"),
            new DetectLanguageInput("2", "Este es un documento  escrito en Español.", "ES")
        );

        TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions().setIncludeStatistics(true);

        textAnalyticsAsyncClient.detectLanguageBatchWithResponse(detectLanguageInputs1, requestOptions)
            .subscribe(response -> {
                // Response's status code
                System.out.printf("Status code of request response: %d%n", response.getStatusCode());

                DetectLanguageResultCollection resultCollection = response.getValue();
                // Batch statistics
                TextDocumentBatchStatistics batchStatistics = resultCollection.getStatistics();
                System.out.printf("Batch statistics, transaction count: %s, valid document count: %s.%n",
                    batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());
                // Batch result of languages
                for (DetectLanguageResult detectLanguageResult : resultCollection) {
                    DetectedLanguage detectedLanguage = detectLanguageResult.getPrimaryLanguage();
                    System.out.printf("Detected language name: %s, ISO 6391 Name: %s, confidence score: %f.%n",
                        detectedLanguage.getName(), detectedLanguage.getIso6391Name(),
                        detectedLanguage.getConfidenceScore());
                }
            });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.detectLanguageBatch#Iterable-TextAnalyticsRequestOptions
    }

    // Entity

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#recognizeEntities(String)}
     */
    public void recognizeEntities() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeEntities#string
        String document = "Satya Nadella is the CEO of Microsoft";
        textAnalyticsAsyncClient.recognizeEntities(document)
            .subscribe(entityCollection -> entityCollection.forEach(entity ->
                System.out.printf("Recognized categorized entity: %s, category: %s, confidence score: %f.%n",
                entity.getText(),
                entity.getCategory(),
                entity.getConfidenceScore())));
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeEntities#string
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#recognizeEntities(String, String)}
     */
    public void recognizeEntitiesWithLanguage() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeEntities#string-string
        String document = "Satya Nadella is the CEO of Microsoft";
        textAnalyticsAsyncClient.recognizeEntities(document, "en")
            .subscribe(entityCollection -> entityCollection.forEach(entity ->
                System.out.printf("Recognized categorized entity: %s, category: %s, confidence score: %f.%n",
                entity.getText(),
                entity.getCategory(),
                entity.getConfidenceScore())));
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeEntities#string-string
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#recognizeEntitiesBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    public void recognizeEntitiesStringListWithOptions() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeCategorizedEntitiesBatch#Iterable-String-TextAnalyticsRequestOptions
        List<String> documents = Arrays.asList(
            "I had a wonderful trip to Seattle last week.", "I work at Microsoft.");

        textAnalyticsAsyncClient.recognizeEntitiesBatch(documents, "en", null)
            .subscribe(batchResult -> {
                // Batch statistics
                TextDocumentBatchStatistics batchStatistics = batchResult.getStatistics();
                System.out.printf("Batch statistics, transaction count: %s, valid document count: %s.%n",
                    batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());
                // Batch Result of entities
                batchResult.forEach(recognizeEntitiesResult ->
                    recognizeEntitiesResult.getEntities().forEach(entity -> System.out.printf(
                        "Recognized categorized entity: %s, category: %s, confidence score: %f.%n",
                            entity.getText(), entity.getCategory(), entity.getConfidenceScore())));
            });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeCategorizedEntitiesBatch#Iterable-String-TextAnalyticsRequestOptions
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#recognizeEntitiesBatchWithResponse(Iterable, TextAnalyticsRequestOptions)}
     */
    public void recognizeBatchEntitiesMaxOverload() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeCategorizedEntitiesBatch#Iterable-TextAnalyticsRequestOptions
        List<TextDocumentInput> textDocumentInputs1 = Arrays.asList(
            new TextDocumentInput("0", "I had a wonderful trip to Seattle last week.").setLanguage("en"),
            new TextDocumentInput("1", "I work at Microsoft.").setLanguage("en"));

        TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions().setIncludeStatistics(true);

        textAnalyticsAsyncClient.recognizeEntitiesBatchWithResponse(textDocumentInputs1, requestOptions)
            .subscribe(response -> {
                // Response's status code
                System.out.printf("Status code of request response: %d%n", response.getStatusCode());
                RecognizeEntitiesResultCollection resultCollection = response.getValue();

                // Batch statistics
                TextDocumentBatchStatistics batchStatistics = resultCollection.getStatistics();
                System.out.printf("Batch statistics, transaction count: %s, valid document count: %s.%n",
                    batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

                resultCollection.forEach(recognizeEntitiesResult ->
                    recognizeEntitiesResult.getEntities().forEach(entity -> System.out.printf(
                        "Recognized categorized entity: %s, category: %s, confidence score: %f.%n",
                        entity.getText(),
                        entity.getCategory(),
                        entity.getConfidenceScore())));
            });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeCategorizedEntitiesBatch#Iterable-TextAnalyticsRequestOptions
    }

    // Personally Identifiable Information Entity

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#recognizePiiEntities(String)}
     */
    public void recognizePiiEntities() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizePiiEntities#string
        String document = "My SSN is 859-98-0987";
        textAnalyticsAsyncClient.recognizePiiEntities(document).subscribe(piiEntityCollection -> {
            System.out.printf("Redacted Text: %s%n", piiEntityCollection.getRedactedText());
            piiEntityCollection.forEach(entity -> System.out.printf(
                "Recognized Personally Identifiable Information entity: %s, entity category: %s,"
                    + " entity subcategory: %s, confidence score: %f.%n",
                entity.getText(), entity.getCategory(), entity.getSubcategory(), entity.getConfidenceScore()));
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizePiiEntities#string
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#recognizePiiEntities(String, String)}
     */
    public void recognizePiiEntitiesWithLanguage() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizePiiEntities#string-string
        String document = "My SSN is 859-98-0987";
        textAnalyticsAsyncClient.recognizePiiEntities(document, "en")
            .subscribe(piiEntityCollection -> {
                System.out.printf("Redacted Text: %s%n", piiEntityCollection.getRedactedText());
                piiEntityCollection.forEach(entity -> System.out.printf(
                    "Recognized Personally Identifiable Information entity: %s, entity category: %s,"
                        + " entity subcategory: %s, confidence score: %f.%n",
                    entity.getText(), entity.getCategory(), entity.getSubcategory(), entity.getConfidenceScore()));
            });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizePiiEntities#string-string
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#recognizePiiEntities(String, String, RecognizePiiEntitiesOptions)}
     */
    public void recognizePiiEntitiesWithRecognizePiiEntitiesOptions() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizePiiEntities#string-string-RecognizePiiEntitiesOptions
        String document = "My SSN is 859-98-0987";
        textAnalyticsAsyncClient.recognizePiiEntities(document, "en",
            new RecognizePiiEntitiesOptions().setDomainFilter(PiiEntityDomain.PROTECTED_HEALTH_INFORMATION))
            .subscribe(piiEntityCollection -> {
                System.out.printf("Redacted Text: %s%n", piiEntityCollection.getRedactedText());
                piiEntityCollection.forEach(entity -> System.out.printf(
                    "Recognized Personally Identifiable Information entity: %s, entity category: %s,"
                        + " entity subcategory: %s, confidence score: %f.%n",
                    entity.getText(), entity.getCategory(), entity.getSubcategory(), entity.getConfidenceScore()));
            });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizePiiEntities#string-string-RecognizePiiEntitiesOptions
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#recognizePiiEntitiesBatch(Iterable, String, RecognizePiiEntitiesOptions)}
     */
    public void recognizePiiEntitiesStringListWithOptions() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizePiiEntitiesBatch#Iterable-String-RecognizePiiEntitiesOptions
        List<String> documents = Arrays.asList(
            "My SSN is 859-98-0987.",
            "Visa card 0111 1111 1111 1111."
        );

        // Show statistics and model version
        RecognizePiiEntitiesOptions requestOptions = new RecognizePiiEntitiesOptions().setIncludeStatistics(true)
            .setModelVersion("latest");

        textAnalyticsAsyncClient.recognizePiiEntitiesBatch(documents, "en", requestOptions)
            .subscribe(piiEntitiesResults -> {
                // Batch statistics
                TextDocumentBatchStatistics batchStatistics = piiEntitiesResults.getStatistics();
                System.out.printf("Batch statistics, transaction count: %s, valid document count: %s.%n",
                    batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

                piiEntitiesResults.forEach(recognizePiiEntitiesResult -> {
                    PiiEntityCollection piiEntityCollection = recognizePiiEntitiesResult.getEntities();
                    System.out.printf("Redacted Text: %s%n", piiEntityCollection.getRedactedText());
                    piiEntityCollection.forEach(entity -> System.out.printf(
                        "Recognized Personally Identifiable Information entity: %s, entity category: %s,"
                            + " entity subcategory: %s, confidence score: %f.%n",
                        entity.getText(), entity.getCategory(), entity.getSubcategory(), entity.getConfidenceScore()));
                });
            });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizePiiEntitiesBatch#Iterable-String-RecognizePiiEntitiesOptions
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#recognizePiiEntitiesBatchWithResponse(Iterable,
     * RecognizePiiEntitiesOptions)}
     */
    public void recognizeBatchPiiEntitiesMaxOverload() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizePiiEntitiesBatch#Iterable-RecognizePiiEntitiesOptions
        List<TextDocumentInput> textDocumentInputs1 = Arrays.asList(
            new TextDocumentInput("0", "My SSN is 859-98-0987."),
            new TextDocumentInput("1", "Visa card 0111 1111 1111 1111."));

        // Show statistics and model version
        RecognizePiiEntitiesOptions requestOptions = new RecognizePiiEntitiesOptions().setIncludeStatistics(true)
            .setModelVersion("latest");

        textAnalyticsAsyncClient.recognizePiiEntitiesBatchWithResponse(textDocumentInputs1, requestOptions)
            .subscribe(response -> {
                RecognizePiiEntitiesResultCollection piiEntitiesResults = response.getValue();
                // Batch statistics
                TextDocumentBatchStatistics batchStatistics = piiEntitiesResults.getStatistics();
                System.out.printf("Batch statistics, transaction count: %s, valid document count: %s.%n",
                    batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

                piiEntitiesResults.forEach(recognizePiiEntitiesResult -> {
                    PiiEntityCollection piiEntityCollection = recognizePiiEntitiesResult.getEntities();
                    System.out.printf("Redacted Text: %s%n", piiEntityCollection.getRedactedText());
                    piiEntityCollection.forEach(entity -> System.out.printf(
                        "Recognized Personally Identifiable Information entity: %s, entity category: %s,"
                            + " entity subcategory: %s, confidence score: %f.%n",
                        entity.getText(), entity.getCategory(), entity.getSubcategory(), entity.getConfidenceScore()));
                });
            });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizePiiEntitiesBatch#Iterable-RecognizePiiEntitiesOptions
    }

    // Linked Entity

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#recognizeLinkedEntities(String)}
     */
    public void recognizeLinkedEntities() {

        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeLinkedEntities#string
        String document = "Old Faithful is a geyser at Yellowstone Park.";
        textAnalyticsAsyncClient.recognizeLinkedEntities(document).subscribe(
            linkedEntityCollection -> linkedEntityCollection.forEach(linkedEntity -> {
                System.out.println("Linked Entities:");
                System.out.printf("Name: %s, entity ID in data source: %s, URL: %s, data source: %s.%n",
                    linkedEntity.getName(), linkedEntity.getDataSourceEntityId(), linkedEntity.getUrl(),
                    linkedEntity.getDataSource());
                linkedEntity.getMatches().forEach(entityMatch -> System.out.printf(
                    "Matched entity: %s, confidence score: %f.%n",
                    entityMatch.getText(), entityMatch.getConfidenceScore()));
            }));
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeLinkedEntities#string
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#recognizeLinkedEntities(String, String)}
     */
    public void recognizeLinkedEntitiesWithLanguage() {

        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeLinkedEntities#string-string
        String document = "Old Faithful is a geyser at Yellowstone Park.";
        textAnalyticsAsyncClient.recognizeLinkedEntities(document, "en").subscribe(
            linkedEntityCollection -> linkedEntityCollection.forEach(linkedEntity -> {
                System.out.println("Linked Entities:");
                System.out.printf("Name: %s, entity ID in data source: %s, URL: %s, data source: %s.%n",
                    linkedEntity.getName(), linkedEntity.getDataSourceEntityId(), linkedEntity.getUrl(),
                    linkedEntity.getDataSource());
                linkedEntity.getMatches().forEach(entityMatch -> System.out.printf(
                    "Matched entity: %s, confidence score: %f.%n",
                    entityMatch.getText(), entityMatch.getConfidenceScore()));
            }));
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeLinkedEntities#string-string
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#recognizeLinkedEntitiesBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    public void recognizeLinkedEntitiesStringListWithOptions() {

        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeLinkedEntitiesBatch#Iterable-String-TextAnalyticsRequestOptions
        List<String> documents = Arrays.asList(
            "Old Faithful is a geyser at Yellowstone Park.",
            "Mount Shasta has lenticular clouds."
        );

        textAnalyticsAsyncClient.recognizeLinkedEntitiesBatch(documents, "en", null)
            .subscribe(batchResult -> {
                // Batch statistics
                TextDocumentBatchStatistics batchStatistics = batchResult.getStatistics();
                System.out.printf("Batch statistics, transaction count: %s, valid document count: %s.%n",
                    batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

                batchResult.forEach(recognizeLinkedEntitiesResult ->
                    recognizeLinkedEntitiesResult.getEntities().forEach(linkedEntity -> {
                        System.out.println("Linked Entities:");
                        System.out.printf("Name: %s, entity ID in data source: %s, URL: %s, data source: %s.%n",
                            linkedEntity.getName(), linkedEntity.getDataSourceEntityId(), linkedEntity.getUrl(),
                            linkedEntity.getDataSource());
                        linkedEntity.getMatches().forEach(entityMatch -> System.out.printf(
                            "Matched entity: %s, confidence score: %f.%n",
                            entityMatch.getText(), entityMatch.getConfidenceScore()));
                    }));
            });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeLinkedEntitiesBatch#Iterable-String-TextAnalyticsRequestOptions
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#recognizeLinkedEntitiesBatchWithResponse(Iterable, TextAnalyticsRequestOptions)}
     */
    public void recognizeBatchLinkedEntitiesMaxOverload() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeLinkedEntitiesBatch#Iterable-TextAnalyticsRequestOptions
        List<TextDocumentInput> textDocumentInputs1 = Arrays.asList(
            new TextDocumentInput("0", "Old Faithful is a geyser at Yellowstone Park.").setLanguage("en"),
            new TextDocumentInput("1", "Mount Shasta has lenticular clouds.").setLanguage("en"));

        TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions().setIncludeStatistics(true);

        textAnalyticsAsyncClient.recognizeLinkedEntitiesBatchWithResponse(textDocumentInputs1, requestOptions)
            .subscribe(response -> {
                // Response's status code
                System.out.printf("Status code of request response: %d%n", response.getStatusCode());
                RecognizeLinkedEntitiesResultCollection resultCollection = response.getValue();

                // Batch statistics
                TextDocumentBatchStatistics batchStatistics = resultCollection.getStatistics();
                System.out.printf("Batch statistics, transaction count: %s, valid document count: %s.%n",
                    batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

                resultCollection.forEach(recognizeLinkedEntitiesResult ->
                    recognizeLinkedEntitiesResult.getEntities().forEach(linkedEntity -> {
                        System.out.println("Linked Entities:");
                        System.out.printf("Name: %s, entity ID in data source: %s, URL: %s, data source: %s.%n",
                            linkedEntity.getName(), linkedEntity.getDataSourceEntityId(), linkedEntity.getUrl(),
                            linkedEntity.getDataSource());
                        linkedEntity.getMatches().forEach(entityMatch -> System.out.printf(
                            "Matched entity: %s, confidence score: %.2f.%n",
                            entityMatch.getText(), entityMatch.getConfidenceScore()));
                    }));
            });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeLinkedEntitiesBatch#Iterable-TextAnalyticsRequestOptions
    }

    // Key Phrases

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#extractKeyPhrases(String)}
     */
    public void extractKeyPhrases() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.extractKeyPhrases#string
        System.out.println("Extracted phrases:");
        textAnalyticsAsyncClient.extractKeyPhrases("Bonjour tout le monde").subscribe(keyPhrase ->
            System.out.printf("%s.%n", keyPhrase));
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.extractKeyPhrases#string
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#extractKeyPhrases(String, String)}
     */
    public void extractKeyPhrasesWithLanguage() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.extractKeyPhrases#string-string
        System.out.println("Extracted phrases:");
        textAnalyticsAsyncClient.extractKeyPhrases("Bonjour tout le monde", "fr")
            .subscribe(keyPhrase -> System.out.printf("%s.%n", keyPhrase));
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.extractKeyPhrases#string-string
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#extractKeyPhrasesBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    public void extractKeyPhrasesStringListWithOptions() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.extractKeyPhrasesBatch#Iterable-String-TextAnalyticsRequestOptions
        List<String> documents = Arrays.asList(
            "Hello world. This is some input text that I love.",
            "Bonjour tout le monde");

        textAnalyticsAsyncClient.extractKeyPhrasesBatch(documents, "en", null).subscribe(
            extractKeyPhraseResults -> {
                // Batch statistics
                TextDocumentBatchStatistics batchStatistics = extractKeyPhraseResults.getStatistics();
                System.out.printf("Batch statistics, transaction count: %s, valid document count: %s.%n",
                    batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

                extractKeyPhraseResults.forEach(extractKeyPhraseResult -> {
                    System.out.println("Extracted phrases:");
                    extractKeyPhraseResult.getKeyPhrases().forEach(keyPhrase -> System.out.printf("%s.%n", keyPhrase));
                });
            });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.extractKeyPhrasesBatch#Iterable-String-TextAnalyticsRequestOptions
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#extractKeyPhrasesBatchWithResponse(Iterable, TextAnalyticsRequestOptions)}
     */
    public void extractBatchKeyPhrasesMaxOverload() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.extractKeyPhrasesBatch#Iterable-TextAnalyticsRequestOptions
        List<TextDocumentInput> textDocumentInputs1 = Arrays.asList(
            new TextDocumentInput("0", "I had a wonderful trip to Seattle last week.").setLanguage("en"),
            new TextDocumentInput("1", "I work at Microsoft.").setLanguage("en"));

        TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions().setIncludeStatistics(true);

        textAnalyticsAsyncClient.extractKeyPhrasesBatchWithResponse(textDocumentInputs1, requestOptions)
            .subscribe(response -> {
                // Response's status code
                System.out.printf("Status code of request response: %d%n", response.getStatusCode());
                ExtractKeyPhrasesResultCollection resultCollection = response.getValue();

                // Batch statistics
                TextDocumentBatchStatistics batchStatistics = resultCollection.getStatistics();
                System.out.printf("Batch statistics, transaction count: %s, valid document count: %s.%n",
                    batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

                for (ExtractKeyPhraseResult extractKeyPhraseResult : resultCollection) {
                    System.out.println("Extracted phrases:");
                    for (String keyPhrase : extractKeyPhraseResult.getKeyPhrases()) {
                        System.out.printf("%s.%n", keyPhrase);
                    }
                }
            });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.extractKeyPhrasesBatch#Iterable-TextAnalyticsRequestOptions
    }

    // Sentiment

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#analyzeSentiment(String)}
     */
    public void analyzeSentiment() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeSentiment#string
        String document = "The hotel was dark and unclean.";
        textAnalyticsAsyncClient.analyzeSentiment(document).subscribe(documentSentiment -> {
            System.out.printf("Recognized document sentiment: %s.%n", documentSentiment.getSentiment());

            for (SentenceSentiment sentenceSentiment : documentSentiment.getSentences()) {
                System.out.printf(
                    "Recognized sentence sentiment: %s, positive score: %.2f, neutral score: %.2f, "
                        + "negative score: %.2f.%n",
                    sentenceSentiment.getSentiment(),
                    sentenceSentiment.getConfidenceScores().getPositive(),
                    sentenceSentiment.getConfidenceScores().getNeutral(),
                    sentenceSentiment.getConfidenceScores().getNegative());
            }
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeSentiment#string
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#analyzeSentiment(String, String)}
     */
    public void analyzeSentimentWithLanguage() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeSentiment#String-String
        String document = "The hotel was dark and unclean.";
        textAnalyticsAsyncClient.analyzeSentiment(document, "en")
            .subscribe(documentSentiment -> {
                System.out.printf("Recognized sentiment label: %s.%n", documentSentiment.getSentiment());
                for (SentenceSentiment sentenceSentiment : documentSentiment.getSentences()) {
                    System.out.printf("Recognized sentence sentiment: %s, positive score: %.2f, neutral score: %.2f, "
                            + "negative score: %.2f.%n",
                        sentenceSentiment.getSentiment(),
                        sentenceSentiment.getConfidenceScores().getPositive(),
                        sentenceSentiment.getConfidenceScores().getNeutral(),
                        sentenceSentiment.getConfidenceScores().getNegative());
                }
            });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeSentiment#String-String
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#analyzeSentiment(String, String, AnalyzeSentimentOptions)}
     */
    public void analyzeSentimentWithLanguageWithOpinionMining() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeSentiment#String-String-AnalyzeSentimentOptions
        textAnalyticsAsyncClient.analyzeSentiment("The hotel was dark and unclean.", "en",
            new AnalyzeSentimentOptions().setIncludeOpinionMining(true))
            .subscribe(documentSentiment -> {
                for (SentenceSentiment sentenceSentiment : documentSentiment.getSentences()) {
                    System.out.printf("\tSentence sentiment: %s%n", sentenceSentiment.getSentiment());
                    sentenceSentiment.getOpinions().forEach(opinion -> {
                        TargetSentiment targetSentiment = opinion.getTarget();
                        System.out.printf("\tTarget sentiment: %s, target text: %s%n",
                            targetSentiment.getSentiment(), targetSentiment.getText());
                        for (AssessmentSentiment assessmentSentiment : opinion.getAssessments()) {
                            System.out.printf("\t\t'%s' sentiment because of \"%s\". Is the assessment negated: %s.%n",
                                assessmentSentiment.getSentiment(), assessmentSentiment.getText(),
                                assessmentSentiment.isNegated());
                        }
                    });
                }
            });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeSentiment#String-String-AnalyzeSentimentOptions
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#analyzeSentimentBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    public void analyzeSentimentStringListWithOptions() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeSentimentBatch#Iterable-String-TextAnalyticsRequestOptions
        List<String> documents = Arrays.asList(
            "The hotel was dark and unclean.",
            "The restaurant had amazing gnocchi."
        );

        textAnalyticsAsyncClient.analyzeSentimentBatch(documents, "en",
            new TextAnalyticsRequestOptions().setIncludeStatistics(true)).subscribe(
                response -> {
                    // Batch statistics
                    TextDocumentBatchStatistics batchStatistics = response.getStatistics();
                    System.out.printf("Batch statistics, transaction count: %s, valid document count: %s.%n",
                        batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

                    response.forEach(analyzeSentimentResult -> {
                        System.out.printf("Document ID: %s%n", analyzeSentimentResult.getId());
                        DocumentSentiment documentSentiment = analyzeSentimentResult.getDocumentSentiment();
                        System.out.printf("Recognized document sentiment: %s.%n", documentSentiment.getSentiment());
                        documentSentiment.getSentences().forEach(sentenceSentiment ->
                            System.out.printf("Recognized sentence sentiment: %s, positive score: %.2f, "
                                    + "neutral score: %.2f, negative score: %.2f.%n",
                                sentenceSentiment.getSentiment(),
                                sentenceSentiment.getConfidenceScores().getPositive(),
                                sentenceSentiment.getConfidenceScores().getNeutral(),
                                sentenceSentiment.getConfidenceScores().getNegative()));
                    });
                });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeSentimentBatch#Iterable-String-TextAnalyticsRequestOptions
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#analyzeSentimentBatch(Iterable, String, AnalyzeSentimentOptions)}
     */
    public void analyzeSentimentStringListWithOptionsAndOpinionMining() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeSentimentBatch#Iterable-String-AnalyzeSentimentOptions
        List<String> documents = Arrays.asList(
            "The hotel was dark and unclean.",
            "The restaurant had amazing gnocchi."
        );

        AnalyzeSentimentOptions options = new AnalyzeSentimentOptions().setIncludeOpinionMining(true);

        textAnalyticsAsyncClient.analyzeSentimentBatch(documents, "en", options).subscribe(
            response -> {
                // Batch statistics
                TextDocumentBatchStatistics batchStatistics = response.getStatistics();
                System.out.printf("Batch statistics, transaction count: %s, valid document count: %s.%n",
                    batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

                response.forEach(analyzeSentimentResult -> {
                    System.out.printf("Document ID: %s%n", analyzeSentimentResult.getId());
                    DocumentSentiment documentSentiment = analyzeSentimentResult.getDocumentSentiment();
                    documentSentiment.getSentences().forEach(sentenceSentiment -> {
                        System.out.printf("\tSentence sentiment: %s%n", sentenceSentiment.getSentiment());
                        sentenceSentiment.getOpinions().forEach(opinion -> {
                            TargetSentiment targetSentiment = opinion.getTarget();
                            System.out.printf("\t\tTarget sentiment: %s, target text: %s%n",
                                targetSentiment.getSentiment(), targetSentiment.getText());
                            for (AssessmentSentiment assessmentSentiment : opinion.getAssessments()) {
                                System.out.printf(
                                    "\t\t\t'%s' assessment sentiment because of \"%s\". Is the assessment negated: %s.%n",
                                    assessmentSentiment.getSentiment(), assessmentSentiment.getText(),
                                    assessmentSentiment.isNegated());
                            }
                        });
                    });
                });
            });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeSentimentBatch#Iterable-String-AnalyzeSentimentOptions
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#analyzeSentimentBatchWithResponse(Iterable, TextAnalyticsRequestOptions)}
     */
    public void analyzeBatchSentimentMaxOverload() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeSentimentBatch#Iterable-TextAnalyticsRequestOptions
        List<TextDocumentInput> textDocumentInputs1 = Arrays.asList(
            new TextDocumentInput("0", "The hotel was dark and unclean.").setLanguage("en"),
            new TextDocumentInput("1", "The restaurant had amazing gnocchi.").setLanguage("en"));

        TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions().setIncludeStatistics(true);

        textAnalyticsAsyncClient.analyzeSentimentBatchWithResponse(textDocumentInputs1, requestOptions)
            .subscribe(response -> {
                // Response's status code
                System.out.printf("Status code of request response: %d%n", response.getStatusCode());
                AnalyzeSentimentResultCollection resultCollection = response.getValue();

                // Batch statistics
                TextDocumentBatchStatistics batchStatistics = resultCollection.getStatistics();
                System.out.printf("Batch statistics, transaction count: %s, valid document count: %s.%n",
                    batchStatistics.getTransactionCount(),
                    batchStatistics.getValidDocumentCount());

                resultCollection.forEach(analyzeSentimentResult -> {
                    System.out.printf("Document ID: %s%n", analyzeSentimentResult.getId());
                    DocumentSentiment documentSentiment = analyzeSentimentResult.getDocumentSentiment();
                    System.out.printf("Recognized document sentiment: %s.%n", documentSentiment.getSentiment());
                    documentSentiment.getSentences().forEach(sentenceSentiment ->
                        System.out.printf("Recognized sentence sentiment: %s, positive score: %.2f, "
                                + "neutral score: %.2f, negative score: %.2f.%n",
                            sentenceSentiment.getSentiment(),
                            sentenceSentiment.getConfidenceScores().getPositive(),
                            sentenceSentiment.getConfidenceScores().getNeutral(),
                            sentenceSentiment.getConfidenceScores().getNegative()));
                });
            });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeSentimentBatch#Iterable-TextAnalyticsRequestOptions
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#analyzeSentimentBatchWithResponse(Iterable, AnalyzeSentimentOptions)}
     */
    public void analyzeBatchSentimentMaxOverloadWithOpinionMining() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeSentimentBatch#Iterable-AnalyzeSentimentOptions
        List<TextDocumentInput> textDocumentInputs1 = Arrays.asList(
            new TextDocumentInput("0", "The hotel was dark and unclean.").setLanguage("en"),
            new TextDocumentInput("1", "The restaurant had amazing gnocchi.").setLanguage("en"));

        AnalyzeSentimentOptions options = new AnalyzeSentimentOptions()
            .setIncludeOpinionMining(true).setIncludeStatistics(true);
        textAnalyticsAsyncClient.analyzeSentimentBatchWithResponse(textDocumentInputs1, options)
            .subscribe(response -> {
                // Response's status code
                System.out.printf("Status code of request response: %d%n", response.getStatusCode());
                AnalyzeSentimentResultCollection resultCollection = response.getValue();

                // Batch statistics
                TextDocumentBatchStatistics batchStatistics = resultCollection.getStatistics();
                System.out.printf("Batch statistics, transaction count: %s, valid document count: %s.%n",
                    batchStatistics.getTransactionCount(),
                    batchStatistics.getValidDocumentCount());

                resultCollection.forEach(analyzeSentimentResult -> {
                    System.out.printf("Document ID: %s%n", analyzeSentimentResult.getId());
                    DocumentSentiment documentSentiment = analyzeSentimentResult.getDocumentSentiment();
                    documentSentiment.getSentences().forEach(sentenceSentiment -> {
                        System.out.printf("\tSentence sentiment: %s%n", sentenceSentiment.getSentiment());
                        sentenceSentiment.getOpinions().forEach(opinion -> {
                            TargetSentiment targetSentiment = opinion.getTarget();
                            System.out.printf("\t\tTarget sentiment: %s, target text: %s%n",
                                targetSentiment.getSentiment(), targetSentiment.getText());
                            for (AssessmentSentiment assessmentSentiment : opinion.getAssessments()) {
                                System.out.printf(
                                    "\t\t\t'%s' assessment sentiment because of \"%s\". Is the assessment negated: %s.%n",
                                    assessmentSentiment.getSentiment(), assessmentSentiment.getText(),
                                    assessmentSentiment.isNegated());
                            }
                        });
                    });
                });
            });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeSentimentBatch#Iterable-AnalyzeSentimentOptions
    }

    // Healthcare
    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#beginAnalyzeHealthcareEntities(Iterable, AnalyzeHealthcareEntitiesOptions)}
     */
    public void analyzeHealthcareMaxOverload() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.beginAnalyzeHealthcareEntities#Iterable-AnalyzeHealthcareEntitiesOptions
        List<TextDocumentInput> documents = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            documents.add(new TextDocumentInput(Integer.toString(i),
                "The patient is a 54-year-old gentleman with a history of progressive angina "
                    + "over the past several months."));
        }

        AnalyzeHealthcareEntitiesOptions options = new AnalyzeHealthcareEntitiesOptions()
            .setIncludeStatistics(true);

        textAnalyticsAsyncClient.beginAnalyzeHealthcareEntities(documents, options)
            .flatMap(pollResult -> {
                AnalyzeHealthcareEntitiesOperationDetail operationResult = pollResult.getValue();
                System.out.printf("Operation created time: %s, expiration time: %s.%n",
                    operationResult.getCreatedAt(), operationResult.getExpiresAt());
                return pollResult.getFinalResult();
            })
            .flatMap(analyzeActionsResultPagedFlux -> analyzeActionsResultPagedFlux.byPage())
            .subscribe(
                pagedResponse -> pagedResponse.getElements().forEach(
                    analyzeHealthcareEntitiesResultCollection -> {
                        // Model version
                        System.out.printf("Results of Azure Text Analytics \"Analyze Healthcare\" Model, version: %s%n",
                            analyzeHealthcareEntitiesResultCollection.getModelVersion());

                        TextDocumentBatchStatistics healthcareTaskStatistics =
                            analyzeHealthcareEntitiesResultCollection.getStatistics();
                        // Batch statistics
                        System.out.printf("Documents statistics: document count = %s, erroneous document count = %s,"
                                              + " transaction count = %s, valid document count = %s.%n",
                            healthcareTaskStatistics.getDocumentCount(),
                            healthcareTaskStatistics.getInvalidDocumentCount(),
                            healthcareTaskStatistics.getTransactionCount(),
                            healthcareTaskStatistics.getValidDocumentCount());

                        analyzeHealthcareEntitiesResultCollection.forEach(healthcareEntitiesResult -> {
                            System.out.println("document id = " + healthcareEntitiesResult.getId());
                            System.out.println("Document entities: ");
                            AtomicInteger ct = new AtomicInteger();
                            healthcareEntitiesResult.getEntities().forEach(healthcareEntity -> {
                                System.out.printf(
                                    "\ti = %d, Text: %s, category: %s, confidence score: %f.%n",
                                    ct.getAndIncrement(), healthcareEntity.getText(), healthcareEntity.getCategory(),
                                    healthcareEntity.getConfidenceScore());

                                IterableStream<EntityDataSource> healthcareEntityDataSources =
                                    healthcareEntity.getDataSources();
                                if (healthcareEntityDataSources != null) {
                                    healthcareEntityDataSources.forEach(healthcareEntityLink -> System.out.printf(
                                        "\t\tEntity ID in data source: %s, data source: %s.%n",
                                        healthcareEntityLink.getEntityId(), healthcareEntityLink.getName()));
                                }
                            });
                            // Healthcare entity relation groups
                            healthcareEntitiesResult.getEntityRelations().forEach(entityRelation -> {
                                System.out.printf("\tRelation type: %s.%n", entityRelation.getRelationType());
                                entityRelation.getRoles().forEach(role -> {
                                    final HealthcareEntity entity = role.getEntity();
                                    System.out.printf("\t\tEntity text: %s, category: %s, role: %s.%n",
                                        entity.getText(), entity.getCategory(), role.getName());
                                });
                            });
                        });
                    }));
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.beginAnalyzeHealthcareEntities#Iterable-AnalyzeHealthcareEntitiesOptions
    }

    // Analyze actions
    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#beginAnalyzeActions(Iterable, TextAnalyticsActions, String, AnalyzeActionsOptions)}
     */
    public void analyzeActionsWithLanguage() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.beginAnalyzeActions#Iterable-TextAnalyticsActions-String-AnalyzeActionsOptions
        List<String> documents = Arrays.asList(
            "Elon Musk is the CEO of SpaceX and Tesla.",
            "1", "My SSN is 859-98-0987"
        );
        textAnalyticsAsyncClient.beginAnalyzeActions(documents,
            new TextAnalyticsActions().setDisplayName("{tasks_display_name}")
                .setRecognizeEntitiesActions(new RecognizeEntitiesAction())
                .setExtractKeyPhrasesActions(new ExtractKeyPhrasesAction()),
            "en",
            new AnalyzeActionsOptions().setIncludeStatistics(false))
            .flatMap(AsyncPollResponse::getFinalResult)
            .flatMap(analyzeActionsResultPagedFlux -> analyzeActionsResultPagedFlux.byPage())
            .subscribe(
                pagedResponse -> pagedResponse.getElements().forEach(
                    analyzeActionsResult -> {
                        analyzeActionsResult.getRecognizeEntitiesResults().forEach(
                            actionResult -> {
                                if (!actionResult.isError()) {
                                    actionResult.getDocumentsResults().forEach(
                                        entitiesResult -> entitiesResult.getEntities().forEach(
                                            entity -> System.out.printf(
                                                "Recognized entity: %s, entity category: %s, entity subcategory: %s,"
                                                    + " confidence score: %f.%n",
                                                entity.getText(), entity.getCategory(), entity.getSubcategory(),
                                                entity.getConfidenceScore())));
                                }
                            });
                        analyzeActionsResult.getExtractKeyPhrasesResults().forEach(
                            actionResult -> {
                                if (!actionResult.isError()) {
                                    actionResult.getDocumentsResults().forEach(extractKeyPhraseResult -> {
                                        System.out.println("Extracted phrases:");
                                        extractKeyPhraseResult.getKeyPhrases()
                                            .forEach(keyPhrases -> System.out.printf("\t%s.%n", keyPhrases));
                                    });
                                }
                            });
                    }));
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.beginAnalyzeActions#Iterable-TextAnalyticsActions-String-AnalyzeActionsOptions
    }
    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#beginAnalyzeActions(Iterable, TextAnalyticsActions, AnalyzeActionsOptions)}
     */
    public void analyzeActionsMaxOverload() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.beginAnalyzeActions#Iterable-TextAnalyticsActions-AnalyzeActionsOptions
        List<TextDocumentInput> documents = Arrays.asList(
            new TextDocumentInput("0", "Elon Musk is the CEO of SpaceX and Tesla.").setLanguage("en"),
            new TextDocumentInput("1", "My SSN is 859-98-0987").setLanguage("en")
        );
        textAnalyticsAsyncClient.beginAnalyzeActions(documents,
            new TextAnalyticsActions().setDisplayName("{tasks_display_name}")
                .setRecognizeEntitiesActions(new RecognizeEntitiesAction())
                .setExtractKeyPhrasesActions(new ExtractKeyPhrasesAction()),
            new AnalyzeActionsOptions().setIncludeStatistics(false))
            .flatMap(AsyncPollResponse::getFinalResult)
            .flatMap(analyzeActionsResultPagedFlux -> analyzeActionsResultPagedFlux.byPage())
            .subscribe(
                pagedResponse -> pagedResponse.getElements().forEach(
                    analyzeActionsResult -> {
                        System.out.println("Entities recognition action results:");
                        analyzeActionsResult.getRecognizeEntitiesResults().forEach(
                            actionResult -> {
                                if (!actionResult.isError()) {
                                    actionResult.getDocumentsResults().forEach(
                                        entitiesResult -> entitiesResult.getEntities().forEach(
                                            entity -> System.out.printf(
                                                "Recognized entity: %s, entity category: %s, entity subcategory: %s,"
                                                    + " confidence score: %f.%n",
                                                entity.getText(), entity.getCategory(), entity.getSubcategory(),
                                                entity.getConfidenceScore())));
                                }
                            });
                        System.out.println("Key phrases extraction action results:");
                        analyzeActionsResult.getExtractKeyPhrasesResults().forEach(
                            actionResult -> {
                                if (!actionResult.isError()) {
                                    actionResult.getDocumentsResults().forEach(extractKeyPhraseResult -> {
                                        System.out.println("Extracted phrases:");
                                        extractKeyPhraseResult.getKeyPhrases()
                                            .forEach(keyPhrases -> System.out.printf("\t%s.%n", keyPhrases));
                                    });
                                }
                            });
                    }));
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.beginAnalyzeActions#Iterable-TextAnalyticsActions-AnalyzeActionsOptions
    }
}
