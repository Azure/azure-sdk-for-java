// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.CategorizedEntity;
import com.azure.ai.textanalytics.models.DetectLanguageInput;
import com.azure.ai.textanalytics.models.DetectedLanguage;
import com.azure.ai.textanalytics.models.DocumentSentiment;
import com.azure.ai.textanalytics.models.LinkedEntity;
import com.azure.ai.textanalytics.models.SentenceSentiment;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.util.TextAnalyticsPagedIterable;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.util.Context;

import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

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
        System.out.printf("Detected language name: %s, ISO 6391 name: %s, score: %f.%n",
            detectedLanguage.getName(), detectedLanguage.getIso6391Name(), detectedLanguage.getScore());
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.detectLanguage#String
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#detectLanguage(String, String)}
     */
    public void detectLanguageWithCountryHint() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.detectLanguage#String-String
        DetectedLanguage detectedLanguage = textAnalyticsClient.detectLanguage(
            "This text is in English", "US");
        System.out.printf("Detected language name: %s, ISO 6391 name: %s, score: %f.%n",
            detectedLanguage.getName(), detectedLanguage.getIso6391Name(), detectedLanguage.getScore());
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.detectLanguage#String-String
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#detectLanguageBatch(Iterable)}
     */
    public void detectLanguageStringList() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.detectLanguageBatch#Iterable
        List<String> documents = Arrays.asList(
            "This is written in English",
            "Este es un documento  escrito en Español.");

        textAnalyticsClient.detectLanguageBatch(documents).iterableByPage().forEach(response -> {
            // Batch statistics
            TextDocumentBatchStatistics batchStatistics = response.getStatistics();
            System.out.printf("A batch of documents statistics, transaction count: %s, valid document count: %s.%n",
                batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

            response.getElements().forEach(detectLanguageResult -> {
                DetectedLanguage detectedLanguage = detectLanguageResult.getPrimaryLanguage();
                System.out.printf("Primary language name: %s, ISO 6391 name: %s, score: %f.%n",
                    detectedLanguage.getName(), detectedLanguage.getIso6391Name(), detectedLanguage.getScore());
            });
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.detectLanguageBatch#Iterable
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#detectLanguageBatch(Iterable, String)}
     */
    public void detectLanguageStringListWithCountryHint() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.detectLanguageBatch#Iterable-String
        List<String> documents = Arrays.asList(
            "This is written in English",
            "Este es un documento  escrito en Español."
        );

        textAnalyticsClient.detectLanguageBatch(documents, "US").iterableByPage().forEach(
            response -> {
                // Batch statistics
                TextDocumentBatchStatistics batchStatistics = response.getStatistics();
                System.out.printf("A batch of documents statistics, transaction count: %s, valid document count: %s.%n",
                    batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());
                // Batch result of languages
                response.getElements().forEach(detectLanguageResult -> {
                    System.out.printf("Document ID: %s%n", detectLanguageResult.getId());
                    DetectedLanguage detectedLanguage = detectLanguageResult.getPrimaryLanguage();
                    System.out.printf("Primary language name: %s, ISO 6391 name: %s, score: %f.%n",
                        detectedLanguage.getName(), detectedLanguage.getIso6391Name(), detectedLanguage.getScore());
                });
            });
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.detectLanguageBatch#Iterable-String
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

        textAnalyticsClient.detectLanguageBatch(documents, "US", null).iterableByPage().forEach(
            response -> {
                // Batch statistics
                TextDocumentBatchStatistics batchStatistics = response.getStatistics();
                System.out.printf("A batch of documents statistics, transaction count: %s, valid document count: %s.%n",
                    batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());
                // Batch result of languages
                response.getElements().forEach(detectLanguageResult -> {
                    System.out.printf("Document ID: %s%n", detectLanguageResult.getId());
                    DetectedLanguage detectedLanguage = detectLanguageResult.getPrimaryLanguage();
                    System.out.printf("Primary language name: %s, ISO 6391 name: %s, score: %f.%n",
                        detectedLanguage.getName(), detectedLanguage.getIso6391Name(), detectedLanguage.getScore());
                });
            });
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.detectLanguageBatch#Iterable-String-TextAnalyticsRequestOptions
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#detectLanguageBatch(Iterable, TextAnalyticsRequestOptions,
     * Context)}
     */
    public void detectBatchLanguagesMaxOverload() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.detectLanguageBatch#Iterable-TextAnalyticsRequestOptions-Context
        List<DetectLanguageInput> detectLanguageInputs = Arrays.asList(
            new DetectLanguageInput("1", "This is written in English.", "US"),
            new DetectLanguageInput("2", "Este es un documento  escrito en Español.", "es")
        );

        textAnalyticsClient.detectLanguageBatch(detectLanguageInputs,
            new TextAnalyticsRequestOptions().setIncludeStatistics(true), Context.NONE).iterableByPage().forEach(
                response -> {
                    // Batch statistics
                    TextDocumentBatchStatistics batchStatistics = response.getStatistics();
                    System.out.printf(
                        "A batch of documents statistics, transaction count: %s, valid document count: %s.%n",
                        batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());
                    // Batch result of languages
                    response.getElements().forEach(detectLanguageResult -> {
                        System.out.printf("Document ID: %s%n", detectLanguageResult.getId());
                        DetectedLanguage detectedLanguage = detectLanguageResult.getPrimaryLanguage();
                        System.out.printf("Primary language name: %s, ISO 6391 name: %s, score: %f.%n",
                            detectedLanguage.getName(), detectedLanguage.getIso6391Name(), detectedLanguage.getScore());
                    });
                });
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.detectLanguageBatch#Iterable-TextAnalyticsRequestOptions-Context
    }

    // Categorized Entity

    /**
     * Code snippet for {@link TextAnalyticsClient#recognizeEntities(String)}
     */
    public void recognizeEntities() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeCategorizedEntities#String
        final TextAnalyticsPagedIterable<CategorizedEntity> recognizeEntitiesResult =
            textAnalyticsClient.recognizeEntities("Satya Nadella is the CEO of Microsoft");
        for (CategorizedEntity entity : recognizeEntitiesResult) {
            System.out.printf("Recognized entity: %s, entity category: %s, score: %f.%n",
                entity.getText(), entity.getCategory(), entity.getConfidenceScore());
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeCategorizedEntities#String
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#recognizeEntities(String, String)}
     */
    public void recognizeEntitiesWithLanguage() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeCategorizedEntities#String-String
        final TextAnalyticsPagedIterable<CategorizedEntity> recognizeEntitiesResult =
            textAnalyticsClient.recognizeEntities("Satya Nadella is the CEO of Microsoft", "en");

        for (CategorizedEntity entity : recognizeEntitiesResult) {
            System.out.printf("Recognized entity: %s, entity category: %s, score: %f.%n",
                entity.getText(), entity.getCategory(), entity.getConfidenceScore());
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeCategorizedEntities#String-String
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#recognizeEntitiesBatch(Iterable)}
     */
    public void recognizeEntitiesStringList() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeCategorizedEntitiesBatch#Iterable
        final List<String> documents = Arrays.asList(
            "I had a wonderful trip to Seattle last week.",
            "I work at Microsoft.");

        textAnalyticsClient.recognizeEntitiesBatch(documents).iterableByPage().forEach(response -> {
            // Batch statistics
            final TextDocumentBatchStatistics batchStatistics = response.getStatistics();
            System.out.printf("A batch of documents statistics, transaction count: %s, valid document count: %s.%n",
                batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

            response.getElements().forEach(recognizeEntitiesResult ->
                recognizeEntitiesResult.getEntities().forEach(entity ->
                    System.out.printf("Recognized entity: %s, entity category: %s, score: %f.%n",
                        entity.getText(), entity.getCategory(), entity.getConfidenceScore())));
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeCategorizedEntitiesBatch#Iterable
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#recognizeEntitiesBatch(Iterable, String)}
     */
    public void recognizeEntitiesStringListWithLanguageCode() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeCategorizedEntitiesBatch#Iterable-String
        List<String> documents = Arrays.asList(
            "I had a wonderful trip to Seattle last week.",
            "I work at Microsoft.");

        textAnalyticsClient.recognizeEntitiesBatch(documents, "en").iterableByPage()
            .forEach(response -> {
                // Batch statistics
                TextDocumentBatchStatistics batchStatistics = response.getStatistics();
                System.out.printf(
                    "A batch of documents statistics, transaction count: %s, valid document count: %s.%n",
                    batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

                response.getElements().forEach(recognizeEntitiesResult ->
                    recognizeEntitiesResult.getEntities().forEach(entity -> {
                        System.out.printf("Recognized entity: %s, entity category: %s, score: %f.%n",
                            entity.getText(), entity.getCategory(), entity.getConfidenceScore());
                    }));
            });
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeCategorizedEntitiesBatch#Iterable-String
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#recognizeEntitiesBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    public void recognizeEntitiesStringListWithOptions() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeCategorizedEntitiesBatch#Iterable-String-TextAnalyticsRequestOptions
        List<String> documents = Arrays.asList(
            "I had a wonderful trip to Seattle last week.",
            "I work at Microsoft.");

        textAnalyticsClient.recognizeEntitiesBatch(documents, "en", null).iterableByPage()
            .forEach(response -> {
                // Batch statistics
                TextDocumentBatchStatistics batchStatistics = response.getStatistics();
                System.out.printf(
                    "A batch of documents statistics, transaction count: %s, valid document count: %s.%n",
                    batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

                response.getElements().forEach(recognizeEntitiesResult ->
                    recognizeEntitiesResult.getEntities().forEach(entity -> {
                        System.out.printf("Recognized entity: %s, entity category: %s, score: %f.%n",
                            entity.getText(), entity.getCategory(), entity.getConfidenceScore());
                    }));
            });
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeCategorizedEntitiesBatch#Iterable-String-TextAnalyticsRequestOptions
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#recognizeEntitiesBatch(Iterable, TextAnalyticsRequestOptions,
     * Context)}
     */
    public void recognizeBatchEntitiesMaxOverload() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeEntitiesBatch#Iterable-TextAnalyticsRequestOptions-Context
        List<TextDocumentInput> textDocumentInputs = Arrays.asList(
            new TextDocumentInput("0", "I had a wonderful trip to Seattle last week."),
            new TextDocumentInput("1", "I work at Microsoft.")
        );

        textAnalyticsClient.recognizeEntitiesBatch(textDocumentInputs,
                new TextAnalyticsRequestOptions().setIncludeStatistics(true), Context.NONE).iterableByPage().forEach(
                    response -> {
                        // Batch statistics
                        TextDocumentBatchStatistics batchStatistics = response.getStatistics();
                        System.out.printf(
                            "A batch of documents statistics, transaction count: %s, valid document count: %s.%n",
                            batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

                        response.getElements().forEach(recognizeEntitiesResult ->
                            recognizeEntitiesResult.getEntities().forEach(entity -> {
                                System.out.printf("Recognized entity: %s, entity category: %s, score: %f.%n",
                                    entity.getText(), entity.getCategory(), entity.getConfidenceScore());
                            }));
                    });
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeEntitiesBatch#Iterable-TextAnalyticsRequestOptions-Context
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
            linkedEntity.getLinkedEntityMatches().forEach(entityMatch -> System.out.printf(
                "Matched entity: %s, score: %f.%n", entityMatch.getText(), entityMatch.getConfidenceScore()));
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
            linkedEntity.getLinkedEntityMatches().forEach(entityMatch -> System.out.printf(
                "Matched entity: %s, score: %f.%n", entityMatch.getText(), entityMatch.getConfidenceScore()));
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeLinkedEntities#String-String
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#recognizeLinkedEntitiesBatch(Iterable)}
     */
    public void recognizeLinkedEntitiesStringList() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeLinkedEntitiesBatch#Iterable
        final List<String> documents = Arrays.asList(
            "Old Faithful is a geyser at Yellowstone Park.",
            "Mount Shasta has lenticular clouds.");

        textAnalyticsClient.recognizeLinkedEntitiesBatch(documents).iterableByPage().forEach(response -> {
            // Batch statistics
            TextDocumentBatchStatistics batchStatistics = response.getStatistics();
            System.out.printf("A batch of documents statistics, transaction count: %s, valid document count: %s.%n",
                batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

            response.getElements().forEach(recognizeLinkedEntitiesResult ->
                recognizeLinkedEntitiesResult.getEntities().forEach(linkedEntity -> {
                    System.out.println("Linked Entities:");
                    System.out.printf("Name: %s, entity ID in data source: %s, URL: %s, data source: %s.%n",
                        linkedEntity.getName(), linkedEntity.getDataSourceEntityId(), linkedEntity.getUrl(),
                        linkedEntity.getDataSource());
                    linkedEntity.getLinkedEntityMatches().forEach(entityMatch -> System.out.printf(
                        "Matched entity: %s, score: %f.%n", entityMatch.getText(), entityMatch.getConfidenceScore()));
                }));
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeLinkedEntitiesBatch#Iterable
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#recognizeLinkedEntitiesBatch(Iterable, String)}
     */
    public void recognizeLinkedEntitiesStringListWithLanguageCode() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeLinkedEntitiesBatch#Iterable-String
        List<String> documents = Arrays.asList(
            "Old Faithful is a geyser at Yellowstone Park.",
            "Mount Shasta has lenticular clouds."
        );

        textAnalyticsClient.recognizeLinkedEntitiesBatch(documents, "en").iterableByPage()
            .forEach(response -> {
                // Batch statistics
                TextDocumentBatchStatistics batchStatistics = response.getStatistics();
                System.out.printf("A batch of documents statistics, transaction count: %s, valid document count: %s.%n",
                    batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

                response.getElements().forEach(recognizeLinkedEntitiesResult ->
                    recognizeLinkedEntitiesResult.getEntities().forEach(linkedEntity -> {
                        System.out.println("Linked Entities:");
                        System.out.printf("Name: %s, entity ID in data source: %s, URL: %s, data source: %s.%n",
                            linkedEntity.getName(), linkedEntity.getDataSourceEntityId(), linkedEntity.getUrl(),
                            linkedEntity.getDataSource());
                        linkedEntity.getLinkedEntityMatches().forEach(entityMatch -> System.out.printf(
                            "Matched entity: %s, score: %f.%n", entityMatch.getText(), entityMatch.getConfidenceScore()));
                    }));
            });
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeLinkedEntitiesBatch#Iterable-String
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#recognizeLinkedEntitiesBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    public void recognizeLinkedEntitiesStringListWithOptions() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeLinkedEntitiesBatch#Iterable-String-TextAnalyticsRequestOptions
        List<String> documents = Arrays.asList(
            "Old Faithful is a geyser at Yellowstone Park.",
            "Mount Shasta has lenticular clouds."
        );

        textAnalyticsClient.recognizeLinkedEntitiesBatch(documents, "en", null).iterableByPage()
            .forEach(response -> {
                // Batch statistics
                TextDocumentBatchStatistics batchStatistics = response.getStatistics();
                System.out.printf("A batch of documents statistics, transaction count: %s, valid document count: %s.%n",
                    batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

                response.getElements().forEach(recognizeLinkedEntitiesResult ->
                    recognizeLinkedEntitiesResult.getEntities().forEach(linkedEntity -> {
                        System.out.println("Linked Entities:");
                        System.out.printf("Name: %s, entity ID in data source: %s, URL: %s, data source: %s.%n",
                            linkedEntity.getName(), linkedEntity.getDataSourceEntityId(), linkedEntity.getUrl(),
                            linkedEntity.getDataSource());
                        linkedEntity.getLinkedEntityMatches().forEach(entityMatch -> System.out.printf(
                            "Matched entity: %s, score: %f.%n", entityMatch.getText(), entityMatch.getConfidenceScore()));
                    }));
            });
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeLinkedEntitiesBatch#Iterable-String-TextAnalyticsRequestOptions
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#recognizeLinkedEntitiesBatch(Iterable, TextAnalyticsRequestOptions, Context)}
     */
    public void recognizeLinkedEntitiesBatchMaxOverload() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeLinkedEntitiesBatch#Iterable-TextAnalyticsRequestOptions-Context
        List<TextDocumentInput> textDocumentInputs = Arrays.asList(
            new TextDocumentInput("1", "Old Faithful is a geyser at Yellowstone Park.", "en"),
            new TextDocumentInput("2", "Mount Shasta has lenticular clouds.", "en")
        );

        textAnalyticsClient.recognizeLinkedEntitiesBatch(textDocumentInputs,
            new TextAnalyticsRequestOptions().setIncludeStatistics(true), Context.NONE).iterableByPage().forEach(
                response -> {
                    // Batch statistics
                    TextDocumentBatchStatistics batchStatistics = response.getStatistics();
                    System.out.printf(
                        "A batch of documents statistics, transaction count: %s, valid document count: %s.%n",
                        batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

                    response.getElements().forEach(recognizeLinkedEntitiesResult ->
                        recognizeLinkedEntitiesResult.getEntities().forEach(linkedEntity -> {
                            System.out.println("Linked Entities:");
                            System.out.printf("Name: %s, entity ID in data source: %s, URL: %s, data source: %s.%n",
                                linkedEntity.getName(), linkedEntity.getDataSourceEntityId(), linkedEntity.getUrl(),
                                linkedEntity.getDataSource());
                            linkedEntity.getLinkedEntityMatches().forEach(entityMatch -> System.out.printf(
                                "Matched entity: %s, score: %.2f.%n", entityMatch.getText(), entityMatch.getConfidenceScore()));
                        }));
                });
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
     * Code snippet for {@link TextAnalyticsClient#extractKeyPhrasesBatch(Iterable)}
     */
    public void extractKeyPhrasesStringList() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.extractKeyPhrasesBatch#Iterable
        final List<String> documents = Arrays.asList(
            "My cat might need to see a veterinarian.",
            "The pitot tube is used to measure airspeed."
        );

        // Extracting batch key phrases
        textAnalyticsClient.extractKeyPhrasesBatch(documents).iterableByPage().forEach(response -> {
            // Batch statistics
            final TextDocumentBatchStatistics batchStatistics = response.getStatistics();
            System.out.printf("A batch of documents statistics, transaction count: %s, valid document count: %s.%n",
                batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

            // Extracted key phrase for each of documents from a batch of documents
            response.getElements().forEach(extractKeyPhraseResult -> {
                System.out.printf("Document ID: %s%n", extractKeyPhraseResult.getId());
                // Valid document
                System.out.println("Extracted phrases:");
                extractKeyPhraseResult.getKeyPhrases().forEach(keyPhrase -> System.out.printf("%s.%n", keyPhrase));
            });
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.extractKeyPhrasesBatch#Iterable
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#extractKeyPhrasesBatch(Iterable, String)}
     */
    public void extractKeyPhrasesStringListWithLanguageCode() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.extractKeyPhrasesBatch#Iterable-String
        List<String> documents = Arrays.asList(
            "My cat might need to see a veterinarian.",
            "The pitot tube is used to measure airspeed."
        );

        // Extracting batch key phrases
        textAnalyticsClient.extractKeyPhrasesBatch(documents, "en").iterableByPage().forEach(
            response -> {
                // Batch statistics
                TextDocumentBatchStatistics batchStatistics = response.getStatistics();
                System.out.printf(
                    "A batch of documents statistics, transaction count: %s, valid document count: %s.%n",
                    batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

                // Extracted key phrase for each of documents from a batch of documents
                response.getElements().forEach(extractKeyPhraseResult -> {
                    System.out.printf("Document ID: %s%n", extractKeyPhraseResult.getId());
                    // Valid document
                    System.out.println("Extracted phrases:");
                    extractKeyPhraseResult.getKeyPhrases().forEach(keyPhrase -> System.out.printf("%s.%n", keyPhrase));
                });
            });
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.extractKeyPhrasesBatch#Iterable-String
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#extractKeyPhrasesBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    public void extractKeyPhrasesStringListWithOptions() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.extractKeyPhrasesBatch#Iterable-String-TextAnalyticsRequestOptions
        List<String> documents = Arrays.asList(
            "My cat might need to see a veterinarian.",
            "The pitot tube is used to measure airspeed."
        );

        // Extracting batch key phrases
        textAnalyticsClient.extractKeyPhrasesBatch(documents, "en", null).iterableByPage().forEach(
            response -> {
                // Batch statistics
                TextDocumentBatchStatistics batchStatistics = response.getStatistics();
                System.out.printf(
                    "A batch of documents statistics, transaction count: %s, valid document count: %s.%n",
                    batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

                // Extracted key phrase for each of documents from a batch of documents
                response.getElements().forEach(extractKeyPhraseResult -> {
                    System.out.printf("Document ID: %s%n", extractKeyPhraseResult.getId());
                    // Valid document
                    System.out.println("Extracted phrases:");
                    extractKeyPhraseResult.getKeyPhrases().forEach(keyPhrase -> System.out.printf("%s.%n", keyPhrase));
                });
            });
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.extractKeyPhrasesBatch#Iterable-String-TextAnalyticsRequestOptions
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#extractKeyPhrasesBatch(Iterable, TextAnalyticsRequestOptions,
     * Context)}
     */
    public void extractBatchKeyPhrasesMaxOverload() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.extractKeyPhrasesBatch#Iterable-TextAnalyticsRequestOptions-Context
        List<TextDocumentInput> textDocumentInputs = Arrays.asList(
            new TextDocumentInput("1", "My cat might need to see a veterinarian.", "en"),
            new TextDocumentInput("2", "The pitot tube is used to measure airspeed.", "en")
        );

        // Extracting batch key phrases
        textAnalyticsClient.extractKeyPhrasesBatch(textDocumentInputs,
            new TextAnalyticsRequestOptions().setIncludeStatistics(true), Context.NONE).iterableByPage().forEach(
                response -> {
                    // Batch statistics
                    TextDocumentBatchStatistics batchStatistics = response.getStatistics();
                    System.out.printf(
                        "A batch of documents statistics, transaction count: %s, valid document count: %s.%n",
                        batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

                    // Extracted key phrase for each of documents from a batch of documents
                    response.getElements().forEach(extractKeyPhraseResult -> {
                        System.out.printf("Document ID: %s%n", extractKeyPhraseResult.getId());
                        // Valid document
                        System.out.println("Extracted phrases:");
                        extractKeyPhraseResult.getKeyPhrases().forEach(keyPhrase ->
                            System.out.printf("%s.%n", keyPhrase));
                    });
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

        System.out.printf("Recognized sentiment: %s, positive score: %.2f, neutral score: %.2f, negative score: %.2f.%n",
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
     * Code snippet for {@link TextAnalyticsClient#analyzeSentimentBatch(Iterable)}
     */
    public void analyzeSentimentStringList() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.analyzeSentimentBatch#Iterable
        final List<String> documents = Arrays.asList(
            "The hotel was dark and unclean. The restaurant had amazing gnocchi.",
            "The restaurant had amazing gnocchi. The hotel was dark and unclean."
        );

        // Analyzing batch sentiments
        textAnalyticsClient.analyzeSentimentBatch(documents).iterableByPage().forEach(response -> {
            // Batch statistics
            final TextDocumentBatchStatistics batchStatistics = response.getStatistics();
            System.out.printf(
                "A batch of documents statistics, transaction count: %s, valid document count: %s.%n",
                batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

            // Analyzed sentiment for each of documents from a batch of documents
            response.getElements().forEach(analyzeSentimentResult -> {
                System.out.printf("Document ID: %s%n", analyzeSentimentResult.getId());
                // Valid document
                final DocumentSentiment documentSentiment = analyzeSentimentResult.getDocumentSentiment();
                System.out.printf(
                    "Recognized document sentiment: %s, positive score: %.2f, neutral score: %.2f, "
                        + "negative score: %.2f.%n",
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
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.analyzeSentimentBatch#Iterable
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#analyzeSentimentBatch(Iterable, String)}
     */
    public void analyzeSentimentStringListWithLanguageCode() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.analyzeSentimentBatch#Iterable-String
        List<String> documents = Arrays.asList(
            "The hotel was dark and unclean. The restaurant had amazing gnocchi.",
            "The restaurant had amazing gnocchi. The hotel was dark and unclean."
        );

        // Analyzing batch sentiments
        textAnalyticsClient.analyzeSentimentBatch(documents, "en").iterableByPage()
            .forEach(response -> {
                // Batch statistics
                TextDocumentBatchStatistics batchStatistics = response.getStatistics();
                System.out.printf("A batch of documents statistics, transaction count: %s, valid document count: %s.%n",
                    batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

                // Analyzed sentiment for each of documents from a batch of documents
                response.getElements().forEach(analyzeSentimentResult -> {
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
            });
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.analyzeSentimentBatch#Iterable-String
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#analyzeSentimentBatch(Iterable, String, TextAnalyticsRequestOptions)}
     */
    public void analyzeSentimentStringListWithOptions() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.analyzeSentimentBatch#Iterable-String-TextAnalyticsRequestOptions
        List<String> documents = Arrays.asList(
            "The hotel was dark and unclean. The restaurant had amazing gnocchi.",
            "The restaurant had amazing gnocchi. The hotel was dark and unclean."
        );

        // Analyzing batch sentiments
        textAnalyticsClient.analyzeSentimentBatch(documents, "en", null).iterableByPage()
            .forEach(response -> {
                // Batch statistics
                TextDocumentBatchStatistics batchStatistics = response.getStatistics();
                System.out.printf("A batch of documents statistics, transaction count: %s, valid document count: %s.%n",
                    batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

                // Analyzed sentiment for each of documents from a batch of documents
                response.getElements().forEach(analyzeSentimentResult -> {
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
            });
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.analyzeSentimentBatch#Iterable-String-TextAnalyticsRequestOptions
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#analyzeSentimentBatch(Iterable, TextAnalyticsRequestOptions, Context)}
     */
    public void analyzeBatchSentimentMaxOverload() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.analyzeSentimentBatch#Iterable-TextAnalyticsRequestOptions-Context
        List<TextDocumentInput> textDocumentInputs = Arrays.asList(
            new TextDocumentInput("1", "The hotel was dark and unclean. The restaurant had amazing gnocchi.", "en"),
            new TextDocumentInput("2", "The restaurant had amazing gnocchi. The hotel was dark and unclean.", "en")
        );

        // Analyzing batch sentiments
        textAnalyticsClient.analyzeSentimentBatch(textDocumentInputs,
                new TextAnalyticsRequestOptions().setIncludeStatistics(true), Context.NONE).iterableByPage()
            .forEach(response -> {
                // Batch statistics
                TextDocumentBatchStatistics batchStatistics = response.getStatistics();
                System.out.printf("A batch of documents statistics, transaction count: %s, valid document count: %s.%n",
                    batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

                // Analyzed sentiment for each of documents from a batch of documents
                response.getElements().forEach(analyzeSentimentResult -> {
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
            });
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.analyzeSentimentBatch#Iterable-TextAnalyticsRequestOptions-Context
    }

    /**
     * Code snippet for {@Link TextAnalyticsPagedIterable}
     */
    public void textAnalyticsPagedIterableSample() {
        TextAnalyticsPagedIterable<LinkedEntity> pagedIterable = textAnalyticsClient.recognizeLinkedEntities("");
        // BEGIN: com.azure.ai.textanalytics.util.TextAnalyticsPagedIterable.stream
        pagedIterable.stream().forEach(item -> System.out.println("Processing item" + item));
        // END: com.azure.ai.textanalytics.util.TextAnalyticsPagedIterable.stream

        // BEGIN: com.azure.ai.textanalytics.util.TextAnalyticsPagedIterable.iterator
        Iterator<LinkedEntity> iterator = pagedIterable.iterator();
        while (iterator.hasNext()) {
            System.out.println("Processing item" + iterator.next());
        }
        // END: com.azure.ai.textanalytics.util.TextAnalyticsPagedIterable.iterator

        // BEGIN: com.azure.ai.textanalytics.util.TextAnalyticsPagedIterable.streamByPage
        pagedIterable.streamByPage().forEach(resp -> {
            if (resp.getStatusCode() == HttpURLConnection.HTTP_OK) {
                System.out.printf("Response headers are %s. Url %s%n", resp.getDeserializedHeaders(),
                    resp.getRequest().getUrl());
                resp.getElements().forEach(value -> System.out.printf("Response value is %s%n", value));
            }
        });
        // END: com.azure.ai.textanalytics.util.TextAnalyticsPagedIterable.streamByPage

        // BEGIN: com.azure.ai.textanalytics.util.TextAnalyticsPagedIterable.iterableByPage
        pagedIterable.iterableByPage().forEach(resp -> {
            if (resp.getStatusCode() == HttpURLConnection.HTTP_OK) {
                System.out.printf("Response headers are %s. Url %s%n", resp.getDeserializedHeaders(),
                    resp.getRequest().getUrl());
                resp.getElements().forEach(value -> System.out.printf("Response value is %s%n", value));
            }
        });
        // END: com.azure.ai.textanalytics.util.TextAnalyticsPagedIterable.iterableByPage
    }
}
