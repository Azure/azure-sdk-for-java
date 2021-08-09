// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.AnalyzeActionsOperationDetail;
import com.azure.ai.textanalytics.models.AnalyzeActionsOptions;
import com.azure.ai.textanalytics.models.AnalyzeHealthcareEntitiesOperationDetail;
import com.azure.ai.textanalytics.models.AnalyzeHealthcareEntitiesOptions;
import com.azure.ai.textanalytics.models.AnalyzeSentimentOptions;
import com.azure.ai.textanalytics.models.AssessmentSentiment;
import com.azure.ai.textanalytics.models.CategorizedEntity;
import com.azure.ai.textanalytics.models.CategorizedEntityCollection;
import com.azure.ai.textanalytics.models.DetectLanguageInput;
import com.azure.ai.textanalytics.models.DetectedLanguage;
import com.azure.ai.textanalytics.models.DocumentSentiment;
import com.azure.ai.textanalytics.models.EntityDataSource;
import com.azure.ai.textanalytics.models.ExtractKeyPhrasesAction;
import com.azure.ai.textanalytics.models.HealthcareEntity;
import com.azure.ai.textanalytics.models.PiiEntity;
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
import com.azure.ai.textanalytics.util.AnalyzeActionsResultPagedIterable;
import com.azure.ai.textanalytics.util.AnalyzeSentimentResultCollection;
import com.azure.ai.textanalytics.util.DetectLanguageResultCollection;
import com.azure.ai.textanalytics.util.ExtractKeyPhrasesResultCollection;
import com.azure.ai.textanalytics.util.AnalyzeHealthcareEntitiesPagedIterable;
import com.azure.ai.textanalytics.util.RecognizeEntitiesResultCollection;
import com.azure.ai.textanalytics.util.RecognizeLinkedEntitiesResultCollection;
import com.azure.ai.textanalytics.util.RecognizePiiEntitiesResultCollection;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.IterableStream;
import com.azure.core.util.polling.SyncPoller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Code snippets for {@link TextAnalyticsClient} and {@link TextAnalyticsClientBuilder}
 */
public class TextAnalyticsClientJavaDocCodeSnippets {
    private TextAnalyticsClient textAnalyticsClient = new TextAnalyticsClientBuilder().buildClient();

    /**
     * Code snippet for creating a {@link TextAnalyticsClient} with pipeline
     */
    public void createTextAnalyticsClientWithPipeline() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.pipeline.instantiation
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(/* add policies */)
            .build();

        TextAnalyticsClient textAnalyticsClient = new TextAnalyticsClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .pipeline(pipeline)
            .buildClient();
        // END:  com.azure.ai.textanalytics.TextAnalyticsClient.pipeline.instantiation
    }

    /**
     * Code snippet for creating a {@link TextAnalyticsClient}
     */
    public void createTextAnalyticsClient() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.instantiation
        TextAnalyticsClient textAnalyticsClient = new TextAnalyticsClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .buildClient();
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.instantiation
    }

    // Languages

    /**
     * Code snippet for {@link TextAnalyticsClient#detectLanguage(String)}
     */
    public void detectLanguage() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.detectLanguage#String
        DetectedLanguage detectedLanguage = textAnalyticsClient.detectLanguage("Bonjour tout le monde");
        System.out.printf("Detected language name: %s, ISO 6391 name: %s, confidence score: %f.%n",
            detectedLanguage.getName(), detectedLanguage.getIso6391Name(), detectedLanguage.getConfidenceScore());
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.detectLanguage#String
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#detectLanguage(String, String)}
     */
    public void detectLanguageWithCountryHint() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.detectLanguage#String-String
        DetectedLanguage detectedLanguage = textAnalyticsClient.detectLanguage(
            "This text is in English", "US");
        System.out.printf("Detected language name: %s, ISO 6391 name: %s, confidence score: %f.%n",
            detectedLanguage.getName(), detectedLanguage.getIso6391Name(), detectedLanguage.getConfidenceScore());
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.detectLanguage#String-String
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#detectLanguageBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    public void detectLanguageStringListWithOptions() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.detectLanguageBatch#Iterable-String-TextAnalyticsRequestOptions
        List<String> documents = Arrays.asList(
            "This is written in English",
            "Este es un documento  escrito en Español."
        );

        DetectLanguageResultCollection resultCollection =
            textAnalyticsClient.detectLanguageBatch(documents, "US", null);

        // Batch statistics
        TextDocumentBatchStatistics batchStatistics = resultCollection.getStatistics();
        System.out.printf("A batch of documents statistics, transaction count: %s, valid document count: %s.%n",
            batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

        // Batch result of languages
        resultCollection.forEach(detectLanguageResult -> {
            System.out.printf("Document ID: %s%n", detectLanguageResult.getId());
            DetectedLanguage detectedLanguage = detectLanguageResult.getPrimaryLanguage();
            System.out.printf("Primary language name: %s, ISO 6391 name: %s, confidence score: %f.%n",
                detectedLanguage.getName(), detectedLanguage.getIso6391Name(),
                detectedLanguage.getConfidenceScore());
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.detectLanguageBatch#Iterable-String-TextAnalyticsRequestOptions
    }

    /**
     * Code snippet for
     * {@link TextAnalyticsClient#detectLanguageBatchWithResponse(Iterable, TextAnalyticsRequestOptions, Context)}
     */
    public void detectBatchLanguagesMaxOverload() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.detectLanguageBatch#Iterable-TextAnalyticsRequestOptions-Context
        List<DetectLanguageInput> detectLanguageInputs = Arrays.asList(
            new DetectLanguageInput("1", "This is written in English.", "US"),
            new DetectLanguageInput("2", "Este es un documento  escrito en Español.", "es")
        );

        Response<DetectLanguageResultCollection> response =
            textAnalyticsClient.detectLanguageBatchWithResponse(detectLanguageInputs,
                new TextAnalyticsRequestOptions().setIncludeStatistics(true), Context.NONE);

        // Response's status code
        System.out.printf("Status code of request response: %d%n", response.getStatusCode());
        DetectLanguageResultCollection detectedLanguageResultCollection = response.getValue();

        // Batch statistics
        TextDocumentBatchStatistics batchStatistics = detectedLanguageResultCollection.getStatistics();
        System.out.printf(
            "Documents statistics: document count = %s, erroneous document count = %s, transaction count = %s,"
                + " valid document count = %s.%n",
            batchStatistics.getDocumentCount(), batchStatistics.getInvalidDocumentCount(),
            batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

        // Batch result of languages
        detectedLanguageResultCollection.forEach(detectLanguageResult -> {
            System.out.printf("Document ID: %s%n", detectLanguageResult.getId());
            DetectedLanguage detectedLanguage = detectLanguageResult.getPrimaryLanguage();
            System.out.printf("Primary language name: %s, ISO 6391 name: %s, confidence score: %f.%n",
                detectedLanguage.getName(), detectedLanguage.getIso6391Name(),
                detectedLanguage.getConfidenceScore());
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.detectLanguageBatch#Iterable-TextAnalyticsRequestOptions-Context
    }

    // Categorized Entity

    /**
     * Code snippet for {@link TextAnalyticsClient#recognizeEntities(String)}
     */
    public void recognizeEntities() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeCategorizedEntities#String
        final CategorizedEntityCollection recognizeEntitiesResult =
            textAnalyticsClient.recognizeEntities("Satya Nadella is the CEO of Microsoft");
        for (CategorizedEntity entity : recognizeEntitiesResult) {
            System.out.printf("Recognized entity: %s, entity category: %s, confidence score: %f.%n",
                entity.getText(), entity.getCategory(), entity.getConfidenceScore());
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeCategorizedEntities#String
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#recognizeEntities(String, String)}
     */
    public void recognizeEntitiesWithLanguage() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeCategorizedEntities#String-String
        final CategorizedEntityCollection recognizeEntitiesResult =
            textAnalyticsClient.recognizeEntities("Satya Nadella is the CEO of Microsoft", "en");

        for (CategorizedEntity entity : recognizeEntitiesResult) {
            System.out.printf("Recognized entity: %s, entity category: %s, confidence score: %f.%n",
                entity.getText(), entity.getCategory(), entity.getConfidenceScore());
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeCategorizedEntities#String-String
    }

    /**
     * Code snippet for
     * {@link TextAnalyticsClient#recognizeEntitiesBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    public void recognizeEntitiesStringListWithOptions() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeCategorizedEntitiesBatch#Iterable-String-TextAnalyticsRequestOptions
        List<String> documents = Arrays.asList(
            "I had a wonderful trip to Seattle last week.",
            "I work at Microsoft.");

        RecognizeEntitiesResultCollection resultCollection =
            textAnalyticsClient.recognizeEntitiesBatch(documents, "en", null);

        // Batch statistics
        TextDocumentBatchStatistics batchStatistics = resultCollection.getStatistics();
        System.out.printf(
            "A batch of documents statistics, transaction count: %s, valid document count: %s.%n",
            batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

        resultCollection.forEach(recognizeEntitiesResult ->
            recognizeEntitiesResult.getEntities().forEach(entity ->
                System.out.printf("Recognized entity: %s, entity category: %s, confidence score: %f.%n",
                    entity.getText(), entity.getCategory(), entity.getConfidenceScore())));
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeCategorizedEntitiesBatch#Iterable-String-TextAnalyticsRequestOptions
    }

    /**
     * Code snippet for
     * {@link TextAnalyticsClient#recognizeEntitiesBatchWithResponse(Iterable, TextAnalyticsRequestOptions, Context)}
     */
    public void recognizeBatchEntitiesMaxOverload() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeEntitiesBatch#Iterable-TextAnalyticsRequestOptions-Context
        List<TextDocumentInput> textDocumentInputs = Arrays.asList(
            new TextDocumentInput("0", "I had a wonderful trip to Seattle last week.").setLanguage("en"),
            new TextDocumentInput("1", "I work at Microsoft.").setLanguage("en")
        );

        Response<RecognizeEntitiesResultCollection> response =
            textAnalyticsClient.recognizeEntitiesBatchWithResponse(textDocumentInputs,
                new TextAnalyticsRequestOptions().setIncludeStatistics(true), Context.NONE);

        // Response's status code
        System.out.printf("Status code of request response: %d%n", response.getStatusCode());
        RecognizeEntitiesResultCollection recognizeEntitiesResultCollection = response.getValue();

        // Batch statistics
        TextDocumentBatchStatistics batchStatistics = recognizeEntitiesResultCollection.getStatistics();
        System.out.printf(
            "A batch of documents statistics, transaction count: %s, valid document count: %s.%n",
            batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

        recognizeEntitiesResultCollection.forEach(recognizeEntitiesResult ->
            recognizeEntitiesResult.getEntities().forEach(entity ->
                System.out.printf("Recognized entity: %s, entity category: %s, confidence score: %f.%n",
                    entity.getText(), entity.getCategory(), entity.getConfidenceScore())));
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeEntitiesBatch#Iterable-TextAnalyticsRequestOptions-Context
    }

    // Personally Identifiable Information Entity

    /**
     * Code snippet for {@link TextAnalyticsClient#recognizePiiEntities(String)}
     */
    public void recognizePiiEntities() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.recognizePiiEntities#String
        PiiEntityCollection piiEntityCollection = textAnalyticsClient.recognizePiiEntities("My SSN is 859-98-0987");
        System.out.printf("Redacted Text: %s%n", piiEntityCollection.getRedactedText());
        for (PiiEntity entity : piiEntityCollection) {
            System.out.printf(
                "Recognized Personally Identifiable Information entity: %s, entity category: %s,"
                    + " entity subcategory: %s, confidence score: %f.%n",
                entity.getText(), entity.getCategory(), entity.getSubcategory(), entity.getConfidenceScore());
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.recognizePiiEntities#String
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#recognizePiiEntities(String, String)}
     */
    public void recognizePiiEntitiesWithLanguage() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.recognizePiiEntities#String-String
        PiiEntityCollection piiEntityCollection = textAnalyticsClient.recognizePiiEntities(
            "My SSN is 859-98-0987", "en");
        System.out.printf("Redacted Text: %s%n", piiEntityCollection.getRedactedText());
        piiEntityCollection.forEach(entity -> System.out.printf(
                "Recognized Personally Identifiable Information entity: %s, entity category: %s,"
                    + " entity subcategory: %s, confidence score: %f.%n",
                entity.getText(), entity.getCategory(), entity.getSubcategory(), entity.getConfidenceScore()));
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.recognizePiiEntities#String-String
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#recognizePiiEntities(String, String, RecognizePiiEntitiesOptions)}
     */
    public void recognizePiiEntitiesWithRecognizePiiEntitiesOptions() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.recognizePiiEntities#String-String-RecognizePiiEntitiesOptions
        PiiEntityCollection piiEntityCollection = textAnalyticsClient.recognizePiiEntities(
            "My SSN is 859-98-0987", "en",
            new RecognizePiiEntitiesOptions().setDomainFilter(PiiEntityDomain.PROTECTED_HEALTH_INFORMATION));
        System.out.printf("Redacted Text: %s%n", piiEntityCollection.getRedactedText());
        piiEntityCollection.forEach(entity -> System.out.printf(
            "Recognized Personally Identifiable Information entity: %s, entity category: %s,"
                + " entity subcategory: %s, confidence score: %f.%n",
            entity.getText(), entity.getCategory(), entity.getSubcategory(), entity.getConfidenceScore()));
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.recognizePiiEntities#String-String-RecognizePiiEntitiesOptions
    }

    /**
     * Code snippet for
     * {@link TextAnalyticsClient#recognizePiiEntitiesBatch(Iterable, String, RecognizePiiEntitiesOptions)}
     */
    public void recognizePiiEntitiesStringListWithOptions() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.recognizePiiEntitiesBatch#Iterable-String-RecognizePiiEntitiesOptions
        List<String> documents = Arrays.asList(
            "My SSN is 859-98-0987",
            "Visa card 4111 1111 1111 1111"
        );

        RecognizePiiEntitiesResultCollection resultCollection = textAnalyticsClient.recognizePiiEntitiesBatch(
            documents, "en", new RecognizePiiEntitiesOptions().setIncludeStatistics(true));

        // Batch statistics
        TextDocumentBatchStatistics batchStatistics = resultCollection.getStatistics();
        System.out.printf("A batch of documents statistics, transaction count: %s, valid document count: %s.%n",
            batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

        resultCollection.forEach(recognizePiiEntitiesResult -> {
            PiiEntityCollection piiEntityCollection = recognizePiiEntitiesResult.getEntities();
            System.out.printf("Redacted Text: %s%n", piiEntityCollection.getRedactedText());
            piiEntityCollection.forEach(entity -> System.out.printf(
                "Recognized Personally Identifiable Information entity: %s, entity category: %s,"
                    + " entity subcategory: %s, confidence score: %f.%n",
                entity.getText(), entity.getCategory(), entity.getSubcategory(), entity.getConfidenceScore()));
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.recognizePiiEntitiesBatch#Iterable-String-RecognizePiiEntitiesOptions
    }

    /**
     * Code snippet for
     * {@link TextAnalyticsClient#recognizePiiEntitiesBatchWithResponse(Iterable, RecognizePiiEntitiesOptions, Context)}
     */
    public void recognizeBatchPiiEntitiesMaxOverload() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.recognizePiiEntitiesBatch#Iterable-RecognizePiiEntitiesOptions-Context
        List<TextDocumentInput> textDocumentInputs = Arrays.asList(
            new TextDocumentInput("0", "My SSN is 859-98-0987"),
            new TextDocumentInput("1", "Visa card 4111 1111 1111 1111")
        );

        Response<RecognizePiiEntitiesResultCollection> response =
            textAnalyticsClient.recognizePiiEntitiesBatchWithResponse(textDocumentInputs,
                new RecognizePiiEntitiesOptions().setIncludeStatistics(true), Context.NONE);

        RecognizePiiEntitiesResultCollection resultCollection = response.getValue();

        // Batch statistics
        TextDocumentBatchStatistics batchStatistics = resultCollection.getStatistics();
        System.out.printf("A batch of documents statistics, transaction count: %s, valid document count: %s.%n",
            batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

        resultCollection.forEach(recognizePiiEntitiesResult -> {
            PiiEntityCollection piiEntityCollection = recognizePiiEntitiesResult.getEntities();
            System.out.printf("Redacted Text: %s%n", piiEntityCollection.getRedactedText());
            piiEntityCollection.forEach(entity -> System.out.printf(
                "Recognized Personally Identifiable Information entity: %s, entity category: %s,"
                    + " entity subcategory: %s, confidence score: %f.%n",
                entity.getText(), entity.getCategory(), entity.getSubcategory(), entity.getConfidenceScore()));
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.recognizePiiEntitiesBatch#Iterable-RecognizePiiEntitiesOptions-Context
    }

    // Linked Entity

    /**
     * Code snippet for {@link TextAnalyticsClient#recognizeLinkedEntities(String)}
     */
    public void recognizeLinkedEntities() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeLinkedEntities#String
        final String document = "Old Faithful is a geyser at Yellowstone Park.";
        System.out.println("Linked Entities:");
        textAnalyticsClient.recognizeLinkedEntities(document).forEach(linkedEntity -> {
            System.out.printf("Name: %s, entity ID in data source: %s, URL: %s, data source: %s.%n",
                linkedEntity.getName(), linkedEntity.getDataSourceEntityId(), linkedEntity.getUrl(),
                linkedEntity.getDataSource());
            linkedEntity.getMatches().forEach(entityMatch -> System.out.printf(
                "Matched entity: %s, confidence score: %f.%n",
                entityMatch.getText(), entityMatch.getConfidenceScore()));
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeLinkedEntities#String
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#recognizeLinkedEntities(String, String)}
     */
    public void recognizeLinkedEntitiesWithLanguage() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeLinkedEntities#String-String
        String document = "Old Faithful is a geyser at Yellowstone Park.";
        textAnalyticsClient.recognizeLinkedEntities(document, "en").forEach(linkedEntity -> {
            System.out.printf("Name: %s, entity ID in data source: %s, URL: %s, data source: %s.%n",
                linkedEntity.getName(), linkedEntity.getDataSourceEntityId(), linkedEntity.getUrl(),
                linkedEntity.getDataSource());
            linkedEntity.getMatches().forEach(entityMatch -> System.out.printf(
                "Matched entity: %s, confidence score: %f.%n",
                entityMatch.getText(), entityMatch.getConfidenceScore()));
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeLinkedEntities#String-String
    }

    /**
     * Code snippet for
     * {@link TextAnalyticsClient#recognizeLinkedEntitiesBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    public void recognizeLinkedEntitiesStringListWithOptions() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeLinkedEntitiesBatch#Iterable-String-TextAnalyticsRequestOptions
        List<String> documents = Arrays.asList(
            "Old Faithful is a geyser at Yellowstone Park.",
            "Mount Shasta has lenticular clouds."
        );

        RecognizeLinkedEntitiesResultCollection resultCollection =
            textAnalyticsClient.recognizeLinkedEntitiesBatch(documents, "en", null);

        // Batch statistics
        TextDocumentBatchStatistics batchStatistics = resultCollection.getStatistics();
        System.out.printf("A batch of documents statistics, transaction count: %s, valid document count: %s.%n",
            batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

        resultCollection.forEach(recognizeLinkedEntitiesResult ->
            recognizeLinkedEntitiesResult.getEntities().forEach(linkedEntity -> {
                System.out.println("Linked Entities:");
                System.out.printf("Name: %s, entity ID in data source: %s, URL: %s, data source: %s.%n",
                    linkedEntity.getName(), linkedEntity.getDataSourceEntityId(), linkedEntity.getUrl(),
                    linkedEntity.getDataSource());
                linkedEntity.getMatches().forEach(entityMatch -> System.out.printf(
                    "Matched entity: %s, confidence score: %f.%n",
                    entityMatch.getText(), entityMatch.getConfidenceScore()));
            }));
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeLinkedEntitiesBatch#Iterable-String-TextAnalyticsRequestOptions
    }

    /**
     * Code snippet for
     * {@link TextAnalyticsClient#recognizeLinkedEntitiesBatchWithResponse(Iterable, TextAnalyticsRequestOptions, Context)}
     */
    public void recognizeLinkedEntitiesBatchMaxOverload() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeLinkedEntitiesBatch#Iterable-TextAnalyticsRequestOptions-Context
        List<TextDocumentInput> textDocumentInputs = Arrays.asList(
            new TextDocumentInput("1", "Old Faithful is a geyser at Yellowstone Park.").setLanguage("en"),
            new TextDocumentInput("2", "Mount Shasta has lenticular clouds.").setLanguage("en")
        );

        Response<RecognizeLinkedEntitiesResultCollection> response =
            textAnalyticsClient.recognizeLinkedEntitiesBatchWithResponse(textDocumentInputs,
                new TextAnalyticsRequestOptions().setIncludeStatistics(true), Context.NONE);

        // Response's status code
        System.out.printf("Status code of request response: %d%n", response.getStatusCode());
        RecognizeLinkedEntitiesResultCollection resultCollection = response.getValue();

        // Batch statistics
        TextDocumentBatchStatistics batchStatistics = resultCollection.getStatistics();
        System.out.printf(
            "A batch of documents statistics, transaction count: %s, valid document count: %s.%n",
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
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeLinkedEntitiesBatch#Iterable-TextAnalyticsRequestOptions-Context
    }

    // Key Phrases

    /**
     * Code snippet for {@link TextAnalyticsClient#extractKeyPhrases(String)}
     */
    public void extractKeyPhrases() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.extractKeyPhrases#String
        System.out.println("Extracted phrases:");
        for (String keyPhrase : textAnalyticsClient.extractKeyPhrases("My cat might need to see a veterinarian.")) {
            System.out.printf("%s.%n", keyPhrase);
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.extractKeyPhrases#String
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#extractKeyPhrases(String, String)}
     */
    public void extractKeyPhrasesWithLanguage() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.extractKeyPhrases#String-String-Context
        System.out.println("Extracted phrases:");
        textAnalyticsClient.extractKeyPhrases("My cat might need to see a veterinarian.", "en")
            .forEach(kegPhrase -> System.out.printf("%s.%n", kegPhrase));
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.extractKeyPhrases#String-String-Context
    }

    /**
     * Code snippet for
     * {@link TextAnalyticsClient#extractKeyPhrasesBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    public void extractKeyPhrasesStringListWithOptions() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.extractKeyPhrasesBatch#Iterable-String-TextAnalyticsRequestOptions
        List<String> documents = Arrays.asList(
            "My cat might need to see a veterinarian.",
            "The pitot tube is used to measure airspeed."
        );

        // Extracting batch key phrases
        ExtractKeyPhrasesResultCollection resultCollection =
            textAnalyticsClient.extractKeyPhrasesBatch(documents, "en", null);

        // Batch statistics
        TextDocumentBatchStatistics batchStatistics = resultCollection.getStatistics();
        System.out.printf(
            "A batch of documents statistics, transaction count: %s, valid document count: %s.%n",
            batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

        // Extracted key phrase for each of documents from a batch of documents
        resultCollection.forEach(extractKeyPhraseResult -> {
            System.out.printf("Document ID: %s%n", extractKeyPhraseResult.getId());
            // Valid document
            System.out.println("Extracted phrases:");
            extractKeyPhraseResult.getKeyPhrases().forEach(keyPhrase -> System.out.printf("%s.%n", keyPhrase));
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.extractKeyPhrasesBatch#Iterable-String-TextAnalyticsRequestOptions
    }

    /**
     * Code snippet for
     * {@link TextAnalyticsClient#extractKeyPhrasesBatchWithResponse(Iterable, TextAnalyticsRequestOptions, Context)}
     */
    public void extractBatchKeyPhrasesMaxOverload() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.extractKeyPhrasesBatch#Iterable-TextAnalyticsRequestOptions-Context
        List<TextDocumentInput> textDocumentInputs = Arrays.asList(
            new TextDocumentInput("1", "My cat might need to see a veterinarian.").setLanguage("en"),
            new TextDocumentInput("2", "The pitot tube is used to measure airspeed.").setLanguage("en")
        );

        // Extracting batch key phrases
        Response<ExtractKeyPhrasesResultCollection> response =
            textAnalyticsClient.extractKeyPhrasesBatchWithResponse(textDocumentInputs,
                new TextAnalyticsRequestOptions().setIncludeStatistics(true), Context.NONE);


        // Response's status code
        System.out.printf("Status code of request response: %d%n", response.getStatusCode());
        ExtractKeyPhrasesResultCollection resultCollection = response.getValue();

        // Batch statistics
        TextDocumentBatchStatistics batchStatistics = resultCollection.getStatistics();
        System.out.printf(
            "A batch of documents statistics, transaction count: %s, valid document count: %s.%n",
            batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

        // Extracted key phrase for each of documents from a batch of documents
        resultCollection.forEach(extractKeyPhraseResult -> {
            System.out.printf("Document ID: %s%n", extractKeyPhraseResult.getId());
            // Valid document
            System.out.println("Extracted phrases:");
            extractKeyPhraseResult.getKeyPhrases().forEach(keyPhrase ->
                System.out.printf("%s.%n", keyPhrase));
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.extractKeyPhrasesBatch#Iterable-TextAnalyticsRequestOptions-Context
    }

    // Sentiment

    /**
     * Code snippet for {@link TextAnalyticsClient#analyzeSentiment(String)}
     */
    public void analyzeSentiment() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.analyzeSentiment#String
        final DocumentSentiment documentSentiment =
            textAnalyticsClient.analyzeSentiment("The hotel was dark and unclean.");

        System.out.printf(
            "Recognized sentiment: %s, positive score: %.2f, neutral score: %.2f, negative score: %.2f.%n",
            documentSentiment.getSentiment(),
            documentSentiment.getConfidenceScores().getPositive(),
            documentSentiment.getConfidenceScores().getNeutral(),
            documentSentiment.getConfidenceScores().getNegative());

        for (SentenceSentiment sentenceSentiment : documentSentiment.getSentences()) {
            System.out.printf(
                "Recognized sentence sentiment: %s, positive score: %.2f, neutral score: %.2f, negative score: %.2f.%n",
                sentenceSentiment.getSentiment(),
                sentenceSentiment.getConfidenceScores().getPositive(),
                sentenceSentiment.getConfidenceScores().getNeutral(),
                sentenceSentiment.getConfidenceScores().getNegative());
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.analyzeSentiment#String
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#analyzeSentiment(String, String)}
     */
    public void analyzeSentimentWithLanguage() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.analyzeSentiment#String-String
        final DocumentSentiment documentSentiment = textAnalyticsClient.analyzeSentiment(
            "The hotel was dark and unclean.", "en");

        System.out.printf(
            "Recognized sentiment: %s, positive score: %.2f, neutral score: %.2f, negative score: %.2f.%n",
            documentSentiment.getSentiment(),
            documentSentiment.getConfidenceScores().getPositive(),
            documentSentiment.getConfidenceScores().getNeutral(),
            documentSentiment.getConfidenceScores().getNegative());

        for (SentenceSentiment sentenceSentiment : documentSentiment.getSentences()) {
            System.out.printf(
                "Recognized sentence sentiment: %s, positive score: %.2f, neutral score: %.2f, negative score: %.2f.%n",
                sentenceSentiment.getSentiment(),
                sentenceSentiment.getConfidenceScores().getPositive(),
                sentenceSentiment.getConfidenceScores().getNeutral(),
                sentenceSentiment.getConfidenceScores().getNegative());
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.analyzeSentiment#String-String
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#analyzeSentiment(String, String, AnalyzeSentimentOptions)}
     */
    public void analyzeSentimentWithLanguageWithOpinionMining() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.analyzeSentiment#String-String-AnalyzeSentimentOptions
        final DocumentSentiment documentSentiment = textAnalyticsClient.analyzeSentiment(
            "The hotel was dark and unclean.", "en",
            new AnalyzeSentimentOptions().setIncludeOpinionMining(true));
        for (SentenceSentiment sentenceSentiment : documentSentiment.getSentences()) {
            System.out.printf("\tSentence sentiment: %s%n", sentenceSentiment.getSentiment());
            sentenceSentiment.getOpinions().forEach(opinion -> {
                TargetSentiment targetSentiment = opinion.getTarget();
                System.out.printf("\tTarget sentiment: %s, target text: %s%n", targetSentiment.getSentiment(),
                    targetSentiment.getText());
                for (AssessmentSentiment assessmentSentiment : opinion.getAssessments()) {
                    System.out.printf("\t\t'%s' sentiment because of \"%s\". Is the assessment negated: %s.%n",
                        assessmentSentiment.getSentiment(), assessmentSentiment.getText(), assessmentSentiment.isNegated());
                }
            });
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.analyzeSentiment#String-String-AnalyzeSentimentOptions
    }

    /**
     * Code snippet for
     * {@link TextAnalyticsClient#analyzeSentimentBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    public void analyzeSentimentStringListWithOptions() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.analyzeSentimentBatch#Iterable-String-TextAnalyticsRequestOptions
        List<String> documents = Arrays.asList(
            "The hotel was dark and unclean. The restaurant had amazing gnocchi.",
            "The restaurant had amazing gnocchi. The hotel was dark and unclean."
        );

        // Analyzing batch sentiments
        AnalyzeSentimentResultCollection resultCollection = textAnalyticsClient.analyzeSentimentBatch(
            documents, "en", new TextAnalyticsRequestOptions().setIncludeStatistics(true));

        // Batch statistics
        TextDocumentBatchStatistics batchStatistics = resultCollection.getStatistics();
        System.out.printf("A batch of documents statistics, transaction count: %s, valid document count: %s.%n",
            batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

        // Analyzed sentiment for each of documents from a batch of documents
        resultCollection.forEach(analyzeSentimentResult -> {
            System.out.printf("Document ID: %s%n", analyzeSentimentResult.getId());
            // Valid document
            DocumentSentiment documentSentiment = analyzeSentimentResult.getDocumentSentiment();
            System.out.printf(
                "Recognized document sentiment: %s, positive score: %.2f, neutral score: %.2f,"
                    + " negative score: %.2f.%n",
                documentSentiment.getSentiment(),
                documentSentiment.getConfidenceScores().getPositive(),
                documentSentiment.getConfidenceScores().getNeutral(),
                documentSentiment.getConfidenceScores().getNegative());
            documentSentiment.getSentences().forEach(sentenceSentiment -> System.out.printf(
                "Recognized sentence sentiment: %s, positive score: %.2f, neutral score: %.2f,"
                    + " negative score: %.2f.%n",
                sentenceSentiment.getSentiment(),
                sentenceSentiment.getConfidenceScores().getPositive(),
                sentenceSentiment.getConfidenceScores().getNeutral(),
                sentenceSentiment.getConfidenceScores().getNegative()));
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.analyzeSentimentBatch#Iterable-String-TextAnalyticsRequestOptions
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#analyzeSentimentBatch(Iterable, String, AnalyzeSentimentOptions)}
     */
    public void analyzeSentimentStringListWithOptionsAndOpinionMining() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.analyzeSentimentBatch#Iterable-String-AnalyzeSentimentOptions
        List<String> documents = Arrays.asList(
            "The hotel was dark and unclean. The restaurant had amazing gnocchi.",
            "The restaurant had amazing gnocchi. The hotel was dark and unclean."
        );

        // Analyzing batch sentiments
        AnalyzeSentimentResultCollection resultCollection = textAnalyticsClient.analyzeSentimentBatch(
            documents, "en", new AnalyzeSentimentOptions().setIncludeOpinionMining(true));

        // Analyzed sentiment for each of documents from a batch of documents
        resultCollection.forEach(analyzeSentimentResult -> {
            System.out.printf("Document ID: %s%n", analyzeSentimentResult.getId());
            DocumentSentiment documentSentiment = analyzeSentimentResult.getDocumentSentiment();
            documentSentiment.getSentences().forEach(sentenceSentiment -> {
                System.out.printf("\tSentence sentiment: %s%n", sentenceSentiment.getSentiment());
                sentenceSentiment.getOpinions().forEach(opinion -> {
                    TargetSentiment targetSentiment = opinion.getTarget();
                    System.out.printf("\tTarget sentiment: %s, target text: %s%n", targetSentiment.getSentiment(),
                        targetSentiment.getText());
                    for (AssessmentSentiment assessmentSentiment : opinion.getAssessments()) {
                        System.out.printf("\t\t'%s' sentiment because of \"%s\". Is the assessment negated: %s.%n",
                            assessmentSentiment.getSentiment(), assessmentSentiment.getText(), assessmentSentiment.isNegated());
                    }
                });
            });
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.analyzeSentimentBatch#Iterable-String-AnalyzeSentimentOptions
    }

    /**
     * Code snippet for
     * {@link TextAnalyticsClient#analyzeSentimentBatchWithResponse(Iterable, TextAnalyticsRequestOptions, Context)}
     */
    public void analyzeBatchSentimentMaxOverload() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.analyzeSentimentBatch#Iterable-TextAnalyticsRequestOptions-Context
        List<TextDocumentInput> textDocumentInputs = Arrays.asList(
            new TextDocumentInput("1", "The hotel was dark and unclean. The restaurant had amazing gnocchi.")
                .setLanguage("en"),
            new TextDocumentInput("2", "The restaurant had amazing gnocchi. The hotel was dark and unclean.")
                .setLanguage("en")
        );

        // Analyzing batch sentiments
        Response<AnalyzeSentimentResultCollection> response =
            textAnalyticsClient.analyzeSentimentBatchWithResponse(textDocumentInputs,
                new TextAnalyticsRequestOptions().setIncludeStatistics(true), Context.NONE);

        // Response's status code
        System.out.printf("Status code of request response: %d%n", response.getStatusCode());
        AnalyzeSentimentResultCollection resultCollection = response.getValue();

        // Batch statistics
        TextDocumentBatchStatistics batchStatistics = resultCollection.getStatistics();
        System.out.printf("A batch of documents statistics, transaction count: %s, valid document count: %s.%n",
            batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

        // Analyzed sentiment for each of documents from a batch of documents
        resultCollection.forEach(analyzeSentimentResult -> {
            System.out.printf("Document ID: %s%n", analyzeSentimentResult.getId());
            // Valid document
            DocumentSentiment documentSentiment = analyzeSentimentResult.getDocumentSentiment();
            System.out.printf(
                "Recognized document sentiment: %s, positive score: %.2f, neutral score: %.2f, "
                    + "negative score: %.2f.%n",
                documentSentiment.getSentiment(),
                documentSentiment.getConfidenceScores().getPositive(),
                documentSentiment.getConfidenceScores().getNeutral(),
                documentSentiment.getConfidenceScores().getNegative());
            documentSentiment.getSentences().forEach(sentenceSentiment -> {
                System.out.printf(
                    "Recognized sentence sentiment: %s, positive score: %.2f, neutral score: %.2f,"
                        + " negative score: %.2f.%n",
                    sentenceSentiment.getSentiment(),
                    sentenceSentiment.getConfidenceScores().getPositive(),
                    sentenceSentiment.getConfidenceScores().getNeutral(),
                    sentenceSentiment.getConfidenceScores().getNegative());
            });
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.analyzeSentimentBatch#Iterable-TextAnalyticsRequestOptions-Context
    }

    /**
     * Code snippet for
     * {@link TextAnalyticsClient#analyzeSentimentBatchWithResponse(Iterable, AnalyzeSentimentOptions, Context)}
     */
    public void analyzeBatchSentimentMaxOverloadWithOpinionMining() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.analyzeSentimentBatch#Iterable-AnalyzeSentimentOptions-Context
        List<TextDocumentInput> textDocumentInputs = Arrays.asList(
            new TextDocumentInput("1", "The hotel was dark and unclean. The restaurant had amazing gnocchi.")
                .setLanguage("en"),
            new TextDocumentInput("2", "The restaurant had amazing gnocchi. The hotel was dark and unclean.")
                .setLanguage("en")
        );

        AnalyzeSentimentOptions options = new AnalyzeSentimentOptions().setIncludeOpinionMining(true)
            .setIncludeStatistics(true);

        // Analyzing batch sentiments
        Response<AnalyzeSentimentResultCollection> response =
            textAnalyticsClient.analyzeSentimentBatchWithResponse(textDocumentInputs, options, Context.NONE);

        // Response's status code
        System.out.printf("Status code of request response: %d%n", response.getStatusCode());
        AnalyzeSentimentResultCollection resultCollection = response.getValue();

        // Batch statistics
        TextDocumentBatchStatistics batchStatistics = resultCollection.getStatistics();
        System.out.printf("A batch of documents statistics, transaction count: %s, valid document count: %s.%n",
            batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

        // Analyzed sentiment for each of documents from a batch of documents
        resultCollection.forEach(analyzeSentimentResult -> {
            System.out.printf("Document ID: %s%n", analyzeSentimentResult.getId());
            DocumentSentiment documentSentiment = analyzeSentimentResult.getDocumentSentiment();
            documentSentiment.getSentences().forEach(sentenceSentiment -> {
                System.out.printf("\tSentence sentiment: %s%n", sentenceSentiment.getSentiment());
                sentenceSentiment.getOpinions().forEach(opinion -> {
                    TargetSentiment targetSentiment = opinion.getTarget();
                    System.out.printf("\tTarget sentiment: %s, target text: %s%n", targetSentiment.getSentiment(),
                        targetSentiment.getText());
                    for (AssessmentSentiment assessmentSentiment : opinion.getAssessments()) {
                        System.out.printf("\t\t'%s' sentiment because of \"%s\". Is the assessment negated: %s.%n",
                            assessmentSentiment.getSentiment(), assessmentSentiment.getText(),
                            assessmentSentiment.isNegated());
                    }
                });
            });
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.analyzeSentimentBatch#Iterable-AnalyzeSentimentOptions-Context
    }

    // Healthcare
    /**
     * Code snippet for
     * {@link TextAnalyticsClient#beginAnalyzeHealthcareEntities(Iterable, AnalyzeHealthcareEntitiesOptions, Context)}
     */
    public void analyzeHealthcareMaxOverload() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.beginAnalyzeHealthcareEntities#Iterable-AnalyzeHealthcareEntitiesOptions-Context
        List<TextDocumentInput> documents = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            documents.add(new TextDocumentInput(Integer.toString(i),
                "The patient is a 54-year-old gentleman with a history of progressive angina over "
                    + "the past several months."));
        }

        // Request options: show statistics and model version
        AnalyzeHealthcareEntitiesOptions options = new AnalyzeHealthcareEntitiesOptions()
            .setIncludeStatistics(true);

        SyncPoller<AnalyzeHealthcareEntitiesOperationDetail, AnalyzeHealthcareEntitiesPagedIterable>
            syncPoller = textAnalyticsClient.beginAnalyzeHealthcareEntities(documents, options, Context.NONE);

        syncPoller.waitForCompletion();
        AnalyzeHealthcareEntitiesPagedIterable result = syncPoller.getFinalResult();

        // Task operation statistics
        final AnalyzeHealthcareEntitiesOperationDetail operationResult = syncPoller.poll().getValue();
        System.out.printf("Operation created time: %s, expiration time: %s.%n",
            operationResult.getCreatedAt(), operationResult.getExpiresAt());

        result.forEach(analyzeHealthcareEntitiesResultCollection -> {
            // Model version
            System.out.printf("Results of Azure Text Analytics \"Analyze Healthcare\" Model, version: %s%n",
                analyzeHealthcareEntitiesResultCollection.getModelVersion());

            TextDocumentBatchStatistics healthcareTaskStatistics =
                analyzeHealthcareEntitiesResultCollection.getStatistics();
            // Batch statistics
            System.out.printf("Documents statistics: document count = %s, erroneous document count = %s,"
                    + " transaction count = %s, valid document count = %s.%n",
                healthcareTaskStatistics.getDocumentCount(), healthcareTaskStatistics.getInvalidDocumentCount(),
                healthcareTaskStatistics.getTransactionCount(), healthcareTaskStatistics.getValidDocumentCount());

            analyzeHealthcareEntitiesResultCollection.forEach(healthcareEntitiesResult -> {
                System.out.println("document id = " + healthcareEntitiesResult.getId());
                System.out.println("Document entities: ");
                AtomicInteger ct = new AtomicInteger();
                healthcareEntitiesResult.getEntities().forEach(healthcareEntity -> {
                    System.out.printf("\ti = %d, Text: %s, category: %s, confidence score: %f.%n",
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
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.beginAnalyzeHealthcareEntities#Iterable-AnalyzeHealthcareEntitiesOptions-Context
    }

    // Analyze actions
    /**
     * Code snippet for {@link TextAnalyticsClient#beginAnalyzeActions(Iterable, TextAnalyticsActions, String, AnalyzeActionsOptions)}
     */
    public void analyzeActionsWithLanguage() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.beginAnalyzeActions#Iterable-TextAnalyticsActions-String-AnalyzeActionsOptions
        List<String> documents = Arrays.asList(
            "Elon Musk is the CEO of SpaceX and Tesla.",
            "My SSN is 859-98-0987"
        );

        SyncPoller<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedIterable> syncPoller =
            textAnalyticsClient.beginAnalyzeActions(
                documents,
                new TextAnalyticsActions().setDisplayName("{tasks_display_name}")
                    .setRecognizeEntitiesActions(new RecognizeEntitiesAction())
                    .setExtractKeyPhrasesActions(new ExtractKeyPhrasesAction()),
                "en",
                new AnalyzeActionsOptions().setIncludeStatistics(false));
        syncPoller.waitForCompletion();
        AnalyzeActionsResultPagedIterable result = syncPoller.getFinalResult();
        result.forEach(analyzeActionsResult -> {
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
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.beginAnalyzeActions#Iterable-TextAnalyticsActions-String-AnalyzeActionsOptions
    }

    /**
     * Code snippet for
     * {@link TextAnalyticsClient#beginAnalyzeActions(Iterable, TextAnalyticsActions, AnalyzeActionsOptions, Context)}
     */
    public void analyzeActionsMaxOverload() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.beginAnalyzeActions#Iterable-TextAnalyticsActions-AnalyzeActionsOptions-Context
        List<TextDocumentInput> documents = Arrays.asList(
            new TextDocumentInput("0", "Elon Musk is the CEO of SpaceX and Tesla.").setLanguage("en"),
            new TextDocumentInput("1", "My SSN is 859-98-0987").setLanguage("en")
        );

        SyncPoller<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedIterable> syncPoller =
            textAnalyticsClient.beginAnalyzeActions(
                documents,
                new TextAnalyticsActions().setDisplayName("{tasks_display_name}")
                   .setRecognizeEntitiesActions(new RecognizeEntitiesAction())
                   .setExtractKeyPhrasesActions(new ExtractKeyPhrasesAction()),
                new AnalyzeActionsOptions().setIncludeStatistics(false),
                Context.NONE);
        syncPoller.waitForCompletion();
        AnalyzeActionsResultPagedIterable result = syncPoller.getFinalResult();
        result.forEach(analyzeActionsResult -> {
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
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.beginAnalyzeActions#Iterable-TextAnalyticsActions-AnalyzeActionsOptions-Context
    }
}
