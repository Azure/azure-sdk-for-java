// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.AnalyzeSentimentResult;
import com.azure.ai.textanalytics.models.CategorizedEntity;
import com.azure.ai.textanalytics.models.DetectLanguageInput;
import com.azure.ai.textanalytics.models.DetectLanguageResult;
import com.azure.ai.textanalytics.models.DetectedLanguage;
import com.azure.ai.textanalytics.models.DocumentResultCollection;
import com.azure.ai.textanalytics.models.DocumentSentiment;
import com.azure.ai.textanalytics.models.ExtractKeyPhraseResult;
import com.azure.ai.textanalytics.models.LinkedEntity;
import com.azure.ai.textanalytics.models.LinkedEntityMatch;
import com.azure.ai.textanalytics.models.PiiEntity;
import com.azure.ai.textanalytics.models.RecognizeEntitiesResult;
import com.azure.ai.textanalytics.models.RecognizeLinkedEntitiesResult;
import com.azure.ai.textanalytics.models.RecognizePiiEntitiesResult;
import com.azure.ai.textanalytics.models.SentenceSentiment;
import com.azure.ai.textanalytics.models.TextAnalyticsApiKeyCredential;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.Context;

import java.util.Arrays;
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
            .apiKey(new TextAnalyticsApiKeyCredential("{api_key}"))
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
            .apiKey(new TextAnalyticsApiKeyCredential("{api_key}"))
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
        final DetectedLanguage detectedLanguage = textAnalyticsClient.detectLanguage("Bonjour tout le monde");
        System.out.printf("Detected language name: %s, ISO 6391 name: %s, score: %.2f.%n",
            detectedLanguage.getName(), detectedLanguage.getIso6391Name(), detectedLanguage.getScore());
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.detectLanguage#String
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#detectLanguageWithResponse(String, String, Context)}
     */
    public void detectLanguageWithResponse() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.detectLanguageWithResponse#String-String-Context
        final DetectedLanguage detectedLanguage = textAnalyticsClient.detectLanguageWithResponse(
            "This text is in English", "US", Context.NONE).getValue();
        System.out.printf("Detected language name: %s, ISO 6391 name: %s, score: %.2f.%n",
            detectedLanguage.getName(), detectedLanguage.getIso6391Name(), detectedLanguage.getScore());
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.detectLanguageWithResponse#String-String-Context
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#detectLanguageBatch(List)}
     */
    public void detectLanguageStringList() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.detectLanguageBatch#List
        final List<String> textInputs = Arrays.asList(
            "This is written in English",
            "Este es un document escrito en Español.");
        final DocumentResultCollection<DetectLanguageResult> detectLanguageResults =
            textAnalyticsClient.detectLanguageBatch(textInputs);

        // Batch statistics
        final TextDocumentBatchStatistics batchStatistics = detectLanguageResults.getStatistics();
        System.out.printf("A batch of document statistics, transaction count: %s, valid document count: %s.%n",
            batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

        for (DetectLanguageResult detectLanguageResult : detectLanguageResults) {
            DetectedLanguage detectedLanguage = detectLanguageResult.getPrimaryLanguage();
            System.out.printf("Primary language name: %s, ISO 6391 name: %s, score: %.2f.%n",
                detectedLanguage.getName(), detectedLanguage.getIso6391Name(), detectedLanguage.getScore());
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.detectLanguageBatch#List
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#detectLanguageBatchWithResponse(List, TextAnalyticsRequestOptions,
     * Context)}
     */
    public void detectLanguageStringListWithResponse() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.detectLanguageBatchWithResponse#List-String-TextAnalyticsRequestOptions-Context
        List<String> textInputs = Arrays.asList(
            "This is written in English",
            "Este es un document escrito en Español."
        );
        DocumentResultCollection<DetectLanguageResult> detectLanguageResults =
            textAnalyticsClient.detectLanguageBatchWithResponse(textInputs, "US", null, Context.NONE).getValue();

        // Batch statistics
        TextDocumentBatchStatistics batchStatistics = detectLanguageResults.getStatistics();
        System.out.printf(
            "A batch of document statistics, transaction count: %s, valid document count: %s.%n",
            batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

        for (DetectLanguageResult detectLanguageResult : detectLanguageResults) {
            System.out.printf("Document ID: %s%n", detectLanguageResult.getId());
            DetectedLanguage detectedLanguage = detectLanguageResult.getPrimaryLanguage();
            System.out.printf("Primary language name: %s, ISO 6391 name: %s, score: %.2f.%n",
                detectedLanguage.getName(), detectedLanguage.getIso6391Name(), detectedLanguage.getScore());
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.detectLanguageBatchWithResponse#List-String-TextAnalyticsRequestOptions-Context
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#detectLanguageBatchWithResponse(List, TextAnalyticsRequestOptions,
     * Context)}
     */
    public void detectBatchLanguagesWithResponse() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.detectLanguageBatchWithResponse#List-TextAnalyticsRequestOptions-Context
        List<DetectLanguageInput> detectLanguageInputs = Arrays.asList(
            new DetectLanguageInput("1", "This is written in English.", "US"),
            new DetectLanguageInput("2", "Este es un document escrito en Español.", "es")
        );

        DocumentResultCollection<DetectLanguageResult> detectLanguageResults =
            textAnalyticsClient.detectLanguageBatchWithResponse(detectLanguageInputs,
                new TextAnalyticsRequestOptions().setShowStatistics(true), Context.NONE).getValue();

        // Batch statistics
        TextDocumentBatchStatistics batchStatistics = detectLanguageResults.getStatistics();
        System.out.printf(
            "A batch of document statistics, transaction count: %s, valid document count: %s.%n",
            batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

        for (DetectLanguageResult detectLanguageResult : detectLanguageResults) {
            System.out.printf("Document ID: %s%n", detectLanguageResult.getId());
            DetectedLanguage detectedLanguage = detectLanguageResult.getPrimaryLanguage();
            System.out.printf("Primary language name: %s, ISO 6391 name: %s, score: %.2f.%n",
                detectedLanguage.getName(), detectedLanguage.getIso6391Name(), detectedLanguage.getScore());
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.detectLanguageBatchWithResponse#List-TextAnalyticsRequestOptions-Context
    }

    // Categorized Entity

    /**
     * Code snippet for {@link TextAnalyticsClient#recognizeEntities(String)}
     */
    public void recognizeEntities() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeEntities#String
        final PagedIterable<CategorizedEntity> recognizeEntitiesResult =
            textAnalyticsClient.recognizeEntities("Satya Nadella is the CEO of Microsoft");
        for (CategorizedEntity entity : recognizeEntitiesResult) {
            System.out.printf("Recognized entity: %s, entity category: %s, score: %.2f.%n",
                entity.getText(), entity.getCategory(), entity.getScore());
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeEntities#String
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#recognizeEntities(String, String, Context)}
     */
    public void recognizeEntitiesWithResponse() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeEntities#String-String-Context
        final PagedIterable<CategorizedEntity> recognizeEntitiesResult = textAnalyticsClient.recognizeEntities(
            "Satya Nadella is the CEO of Microsoft", "en", Context.NONE);

        for (CategorizedEntity entity : recognizeEntitiesResult) {
            System.out.printf("Recognized entity: %s, entity category: %s, score: %.2f.%n",
                entity.getText(), entity.getCategory(), entity.getScore());
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeEntities#String-String-Context
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#recognizeEntitiesBatch(List)}
     */
    public void recognizeEntitiesStringList() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeEntitiesBatch#List
        final List<String> textInputs = Arrays.asList(
            "I had a wonderful trip to Seattle last week.",
            "I work at Microsoft.");

        final DocumentResultCollection<RecognizeEntitiesResult> recognizeEntitiesResults =
            textAnalyticsClient.recognizeEntitiesBatch(textInputs);

        // Batch statistics
        final TextDocumentBatchStatistics batchStatistics = recognizeEntitiesResults.getStatistics();
        System.out.printf("A batch of document statistics, transaction count: %s, valid document count: %s.%n",
            batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

        for (RecognizeEntitiesResult recognizeEntitiesResult : recognizeEntitiesResults) {
            for (CategorizedEntity entity : recognizeEntitiesResult.getEntities()) {
                System.out.printf("Recognized entity: %s, entity category: %s, score: %.2f.%n",
                    entity.getText(), entity.getCategory(), entity.getScore());
            }
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeEntitiesBatch#List
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#recognizeEntitiesBatchWithResponse(List, String,
     * TextAnalyticsRequestOptions, Context)}
     */
    public void recognizeEntitiesStringListWithResponse() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeEntitiesBatchWithResponse#List-String-TextAnalyticsRequestOptions-Context
        List<String> textInputs = Arrays.asList(
            "I had a wonderful trip to Seattle last week.",
            "I work at Microsoft.");

        DocumentResultCollection<RecognizeEntitiesResult> recognizeEntitiesResults =
            textAnalyticsClient.recognizeEntitiesBatchWithResponse(textInputs, "en", null, Context.NONE).getValue();

        // Batch statistics
        TextDocumentBatchStatistics batchStatistics = recognizeEntitiesResults.getStatistics();
        System.out.printf(
            "A batch of document statistics, transaction count: %s, valid document count: %s.%n",
            batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

        for (RecognizeEntitiesResult recognizeEntitiesResult : recognizeEntitiesResults) {
            for (CategorizedEntity entity : recognizeEntitiesResult.getEntities()) {
                System.out.printf("Recognized entity: %s, entity category: %s, score: %.2f.%n",
                    entity.getText(), entity.getCategory(), entity.getScore());
            }
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeEntitiesBatchWithResponse#List-String-TextAnalyticsRequestOptions-Context
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#recognizeEntitiesBatchWithResponse(List, TextAnalyticsRequestOptions,
     * Context)}
     */
    public void recognizeBatchEntitiesWithResponse() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeEntitiesBatchWithResponse#List-TextAnalyticsRequestOptions-Context
        List<TextDocumentInput> textDocumentInputs = Arrays.asList(
            new TextDocumentInput("0", "I had a wonderful trip to Seattle last week."),
            new TextDocumentInput("1", "I work at Microsoft.")
        );

        DocumentResultCollection<RecognizeEntitiesResult> recognizeEntitiesResults =
            textAnalyticsClient.recognizeEntitiesBatchWithResponse(textDocumentInputs,
                new TextAnalyticsRequestOptions().setShowStatistics(true), Context.NONE).getValue();

        // Batch statistics
        TextDocumentBatchStatistics batchStatistics = recognizeEntitiesResults.getStatistics();
        System.out.printf(
            "A batch of document statistics, transaction count: %s, valid document count: %s.%n",
            batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

        for (RecognizeEntitiesResult recognizeEntitiesResult : recognizeEntitiesResults) {
            for (CategorizedEntity entity : recognizeEntitiesResult.getEntities()) {
                System.out.printf("Recognized entity: %s, entity category: %s, score: %.2f.%n",
                    entity.getText(), entity.getCategory(), entity.getScore());
            }
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeEntitiesBatchWithResponse#List-TextAnalyticsRequestOptions-Context
    }

    // PII Entity

    /**
     * Code snippet for {@link TextAnalyticsClient#recognizePiiEntities(String)}
     */
    public void recognizePiiEntities() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.recognizePiiEntities#String
        for (PiiEntity entity : textAnalyticsClient.recognizePiiEntities("My SSN is 555-55-5555")) {
            System.out.printf("Recognized PII entity: %s, entity category: %s, score: %.2f.%n",
                entity.getText(), entity.getCategory(), entity.getScore());
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.recognizePiiEntities#String
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#recognizePiiEntities(String, String, Context)}
     */
    public void recognizePiiEntitiesWithResponse() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.recognizePiiEntities#String-String-Context
        for (PiiEntity entity : textAnalyticsClient.recognizePiiEntities("My SSN is 555-55-5555", "en",
            Context.NONE)) {
            System.out.printf("Recognized PII entity: %s, entity category: %s, score: %.2f.%n",
                entity.getText(), entity.getCategory(), entity.getScore());
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.recognizePiiEntities#String-String-Context
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#recognizePiiEntitiesBatch(List)}
     */
    public void recognizePiiEntitiesStringList() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.recognizePiiEntitiesBatch#List
        final List<String> textInputs = Arrays.asList("My SSN is 555-55-5555", "Visa card 4111 1111 1111 1111");

        final DocumentResultCollection<RecognizePiiEntitiesResult> recognizePiiEntitiesResults =
            textAnalyticsClient.recognizePiiEntitiesBatch(textInputs);

        // Batch statistics
        final TextDocumentBatchStatistics batchStatistics = recognizePiiEntitiesResults.getStatistics();
        System.out.printf("A batch of document statistics, transaction count: %s, valid document count: %s.%n",
            batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

        for (RecognizePiiEntitiesResult recognizePiiEntitiesResult : recognizePiiEntitiesResults) {
            for (PiiEntity entity : recognizePiiEntitiesResult.getEntities()) {
                System.out.printf("Recognized PII entity: %s, entity category: %s, score: %.2f.%n",
                    entity.getText(), entity.getCategory(), entity.getScore());
            }
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.recognizePiiEntitiesBatch#List
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#recognizePiiEntitiesBatchWithResponse(List, String,
     * TextAnalyticsRequestOptions, Context)}
     */
    public void recognizePiiEntitiesStringListWithResponse() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.recognizePiiEntitiesBatchWithResponse#List-String-TextAnalyticsRequestOptions-Context
        List<String> textInputs = Arrays.asList(
            "My SSN is 555-55-5555",
            "Visa card 4111 1111 1111 1111"
        );

        DocumentResultCollection<RecognizePiiEntitiesResult> recognizePiiEntitiesResults =
            textAnalyticsClient.recognizePiiEntitiesBatchWithResponse(textInputs, "en", null, Context.NONE).getValue();

        // Batch statistics
        TextDocumentBatchStatistics batchStatistics = recognizePiiEntitiesResults.getStatistics();
        System.out.printf("A batch of document statistics, transaction count: %s, valid document count: %s.%n",
            batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

        for (RecognizePiiEntitiesResult recognizePiiEntitiesResult : recognizePiiEntitiesResults) {
            for (PiiEntity entity : recognizePiiEntitiesResult.getEntities()) {
                System.out.printf("Recognized PII entity: %s, entity category: %s, score: %.2f.%n",
                    entity.getText(), entity.getCategory(), entity.getScore());
            }
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.recognizePiiEntitiesBatchWithResponse#List-String-TextAnalyticsRequestOptions-Context
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#recognizePiiEntitiesBatchWithResponse(List,
     * TextAnalyticsRequestOptions, Context)}
     */
    public void recognizeBatchPiiEntitiesWithResponse() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.recognizePiiEntitiesBatchWithResponse#List-TextAnalyticsRequestOptions-Context
        List<TextDocumentInput> textDocumentInputs = Arrays.asList(
            new TextDocumentInput("0", "My SSN is 555-55-5555"),
            new TextDocumentInput("1", "Visa card 4111 1111 1111 1111")
        );

        DocumentResultCollection<RecognizePiiEntitiesResult> recognizePiiEntitiesResults =
            textAnalyticsClient.recognizePiiEntitiesBatchWithResponse(textDocumentInputs,
                new TextAnalyticsRequestOptions().setShowStatistics(true), Context.NONE).getValue();

        // Batch statistics
        TextDocumentBatchStatistics batchStatistics = recognizePiiEntitiesResults.getStatistics();
        System.out.printf(
            "A batch of document statistics, transaction count: %s, valid document count: %s.%n",
            batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

        for (RecognizePiiEntitiesResult recognizePiiEntitiesResult : recognizePiiEntitiesResults) {
            for (PiiEntity entity : recognizePiiEntitiesResult.getEntities()) {
                System.out.printf("Recognized PII entity: %s, entity category: %s, score: %.2f.%n",
                    entity.getText(), entity.getCategory(), entity.getScore());
            }
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.recognizePiiEntitiesBatchWithResponse#List-TextAnalyticsRequestOptions-Context
    }

    // Linked Entity

    /**
     * Code snippet for {@link TextAnalyticsClient#recognizeLinkedEntities(String)}
     */
    public void recognizeLinkedEntities() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeLinkedEntities#String
        final String inputText = "Old Faithful is a geyser at Yellowstone Park.";
        System.out.println("Linked Entities:");
        for (LinkedEntity linkedEntity : textAnalyticsClient.recognizeLinkedEntities(inputText)) {
            System.out.printf("Name: %s, ID: %s, URL: %s, data source: %s.%n",
                linkedEntity.getName(),
                linkedEntity.getId(),
                linkedEntity.getUrl(),
                linkedEntity.getDataSource());
            for (LinkedEntityMatch linkedEntityMatch : linkedEntity.getLinkedEntityMatches()) {
                System.out.printf("Text: %s, offset: %s, length: %s, score: %.2f.%n",
                    linkedEntityMatch.getText(),
                    linkedEntityMatch.getOffset(),
                    linkedEntityMatch.getLength(),
                    linkedEntityMatch.getScore());
            }
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeLinkedEntities#String
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#recognizeLinkedEntities(String, String, Context)}
     */
    public void recognizeLinkedEntitiesWithResponse() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeLinkedEntities#String-String-Context
        String inputText = "Old Faithful is a geyser at Yellowstone Park.";
        for (LinkedEntity linkedEntity : textAnalyticsClient.recognizeLinkedEntities(inputText, "en", Context.NONE)) {
            System.out.println("Linked Entities:");
            System.out.printf("Name: %s, ID: %s, URL: %s, data source: %s.%n",
                linkedEntity.getName(),
                linkedEntity.getId(),
                linkedEntity.getUrl(),
                linkedEntity.getDataSource());
            for (LinkedEntityMatch linkedEntityMatch : linkedEntity.getLinkedEntityMatches()) {
                System.out.printf("Text: %s, offset: %s, length: %s, score: %.2f.%n",
                    linkedEntityMatch.getText(),
                    linkedEntityMatch.getOffset(),
                    linkedEntityMatch.getLength(),
                    linkedEntityMatch.getScore());
            }
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeLinkedEntities#String-String-Context
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#recognizeLinkedEntitiesBatch(List)}
     */
    public void recognizeLinkedEntitiesStringList() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeLinkedEntitiesBatch#List
        final List<String> textInputs = Arrays.asList(
            "Old Faithful is a geyser at Yellowstone Park.",
            "Mount Shasta has lenticular clouds.");

        final DocumentResultCollection<RecognizeLinkedEntitiesResult> recognizeLinkedEntitiesResults =
            textAnalyticsClient.recognizeLinkedEntitiesBatch(textInputs);

        // Batch statistics
        final TextDocumentBatchStatistics batchStatistics = recognizeLinkedEntitiesResults.getStatistics();
        System.out.printf("A batch of document statistics, transaction count: %s, valid document count: %s.%n",
            batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

        for (RecognizeLinkedEntitiesResult recognizeLinkedEntitiesResult : recognizeLinkedEntitiesResults) {
            for (LinkedEntity linkedEntity : recognizeLinkedEntitiesResult.getEntities()) {
                System.out.println("Linked Entities:");
                System.out.printf("Name: %s, ID: %s, URL: %s, data source: %s.%n",
                    linkedEntity.getName(),
                    linkedEntity.getId(),
                    linkedEntity.getUrl(),
                    linkedEntity.getDataSource());
                for (LinkedEntityMatch linkedEntityMatch : linkedEntity.getLinkedEntityMatches()) {
                    System.out.printf("Text: %s, offset: %s, length: %s, score: %.2f.%n",
                        linkedEntityMatch.getText(),
                        linkedEntityMatch.getOffset(),
                        linkedEntityMatch.getLength(),
                        linkedEntityMatch.getScore());
                }
            }
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeLinkedEntitiesBatch#List
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#recognizeLinkedEntitiesBatchWithResponse(List,
     * TextAnalyticsRequestOptions, Context)}
     */
    public void recognizeLinkedEntitiesStringListWithResponse() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeLinkedEntitiesBatchWithResponse#List-String-TextAnalyticsRequestOptions-Context
        List<String> textInputs = Arrays.asList(
            "Old Faithful is a geyser at Yellowstone Park.",
            "Mount Shasta has lenticular clouds."
        );

        DocumentResultCollection<RecognizeLinkedEntitiesResult> recognizeLinkedEntitiesResults =
            textAnalyticsClient.recognizeLinkedEntitiesBatchWithResponse(textInputs, "en", null,
                Context.NONE).getValue();

        // Batch statistics
        TextDocumentBatchStatistics batchStatistics = recognizeLinkedEntitiesResults.getStatistics();
        System.out.printf("A batch of document statistics, transaction count: %s, valid document count: %s.%n",
            batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

        for (RecognizeLinkedEntitiesResult recognizeLinkedEntitiesResult : recognizeLinkedEntitiesResults) {
            for (LinkedEntity linkedEntity : recognizeLinkedEntitiesResult.getEntities()) {
                System.out.println("Linked Entities:");
                System.out.printf("Name: %s, ID: %s, URL: %s, data source: %s.%n",
                    linkedEntity.getName(),
                    linkedEntity.getId(),
                    linkedEntity.getUrl(),
                    linkedEntity.getDataSource());
                for (LinkedEntityMatch linkedEntityMatch : linkedEntity.getLinkedEntityMatches()) {
                    System.out.printf("Text: %s, offset: %s, length: %s, score: %.2f.%n",
                        linkedEntityMatch.getText(),
                        linkedEntityMatch.getOffset(),
                        linkedEntityMatch.getLength(),
                        linkedEntityMatch.getScore());
                }
            }
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeLinkedEntitiesBatchWithResponse#List-String-TextAnalyticsRequestOptions-Context
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#recognizeLinkedEntitiesBatchWithResponse(List,
     * TextAnalyticsRequestOptions, Context)}
     */
    public void recognizeLinkedEntitiesBatch() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeLinkedEntitiesBatchWithResponse#List-TextAnalyticsRequestOptions-Context
        List<TextDocumentInput> textDocumentInputs = Arrays.asList(
            new TextDocumentInput("1", "Old Faithful is a geyser at Yellowstone Park.", "en"),
            new TextDocumentInput("2", "Mount Shasta has lenticular clouds.", "en")
        );

        DocumentResultCollection<RecognizeLinkedEntitiesResult> recognizeLinkedEntitiesResults =
            textAnalyticsClient.recognizeLinkedEntitiesBatchWithResponse(textDocumentInputs,
                new TextAnalyticsRequestOptions().setShowStatistics(true), Context.NONE).getValue();

        // Batch statistics
        TextDocumentBatchStatistics batchStatistics = recognizeLinkedEntitiesResults.getStatistics();
        System.out.printf(
            "A batch of document statistics, transaction count: %s, valid document count: %s.%n",
            batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

        for (RecognizeLinkedEntitiesResult recognizeLinkedEntitiesResult : recognizeLinkedEntitiesResults) {
            for (LinkedEntity linkedEntity : recognizeLinkedEntitiesResult.getEntities()) {
                System.out.println("Linked Entities:");
                System.out.printf("Name: %s, ID: %s, URL: %s, data source: %s.%n",
                    linkedEntity.getName(),
                    linkedEntity.getId(),
                    linkedEntity.getUrl(),
                    linkedEntity.getDataSource());
                for (LinkedEntityMatch linkedEntityMatch : linkedEntity.getLinkedEntityMatches()) {
                    System.out.printf("Text: %s, offset: %s, length: %s, score: %.2f.%n",
                        linkedEntityMatch.getText(),
                        linkedEntityMatch.getOffset(),
                        linkedEntityMatch.getLength(),
                        linkedEntityMatch.getScore());
                }
            }
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeLinkedEntitiesBatchWithResponse#List-TextAnalyticsRequestOptions-Context
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
     * Code snippet for {@link TextAnalyticsClient#extractKeyPhrases(String, String, Context)}
     */
    public void extractKeyPhrasesWithResponse() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.extractKeyPhrases#String-String-Context
        System.out.println("Extracted phrases:");
        for (String keyPhrase : textAnalyticsClient.extractKeyPhrases(
            "My cat might need to see a veterinarian.", "en", Context.NONE)) {
            System.out.printf("%s.%n", keyPhrase);
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.extractKeyPhrases#String-String-Context
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#extractKeyPhrasesBatch(List)}
     */
    public void extractKeyPhrasesStringList() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.extractKeyPhrasesBatch#List
        final List<String> textInputs = Arrays.asList(
            "My cat might need to see a veterinarian.",
            "The pitot tube is used to measure airspeed."
        );

        // Extracting batch key phrases
        final DocumentResultCollection<ExtractKeyPhraseResult> extractKeyPhraseResults =
            textAnalyticsClient.extractKeyPhrasesBatch(textInputs);

        // Batch statistics
        final TextDocumentBatchStatistics batchStatistics = extractKeyPhraseResults.getStatistics();
        System.out.printf("A batch of document statistics, transaction count: %s, valid document count: %s.%n",
            batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

        // Extracted key phrase for each of document from a batch of documents
        for (ExtractKeyPhraseResult extractKeyPhraseResult : extractKeyPhraseResults) {
            System.out.printf("Document ID: %s%n", extractKeyPhraseResult.getId());
            // Valid document
            System.out.println("Extracted phrases:");
            for (String keyPhrase : extractKeyPhraseResult.getKeyPhrases()) {
                System.out.printf("%s.%n", keyPhrase);
            }
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.extractKeyPhrasesBatch#List
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#extractKeyPhrasesBatchWithResponse(List, TextAnalyticsRequestOptions,
     * Context)}
     */
    public void extractKeyPhrasesStringListWithResponse() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.extractKeyPhrasesBatchWithResponse#List-String-TextAnalyticsRequestOptions-Context
        List<String> textInputs = Arrays.asList(
            "My cat might need to see a veterinarian.",
            "The pitot tube is used to measure airspeed."
        );

        // Extracting batch key phrases
        DocumentResultCollection<ExtractKeyPhraseResult> extractKeyPhraseResults =
            textAnalyticsClient.extractKeyPhrasesBatchWithResponse(textInputs, "en", null, Context.NONE).getValue();

        // Batch statistics
        TextDocumentBatchStatistics batchStatistics = extractKeyPhraseResults.getStatistics();
        System.out.printf(
            "A batch of document statistics, transaction count: %s, valid document count: %s.%n",
            batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

        // Extracted key phrase for each of document from a batch of documents
        for (ExtractKeyPhraseResult extractKeyPhraseResult : extractKeyPhraseResults) {
            System.out.printf("Document ID: %s%n", extractKeyPhraseResult.getId());
            // Valid document
            System.out.println("Extracted phrases:");
            for (String keyPhrase : extractKeyPhraseResult.getKeyPhrases()) {
                System.out.printf("%s.%n", keyPhrase);
            }
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.extractKeyPhrasesBatchWithResponse#List-String-TextAnalyticsRequestOptions-Context
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#extractKeyPhrasesBatchWithResponse(List, TextAnalyticsRequestOptions,
     * Context)}
     */
    public void extractBatchKeyPhrasesWithResponse() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.extractKeyPhrasesBatchWithResponse#List-TextAnalyticsRequestOptions-Context
        List<TextDocumentInput> textDocumentInputs = Arrays.asList(
            new TextDocumentInput("1", "My cat might need to see a veterinarian.", "en"),
            new TextDocumentInput("2", "The pitot tube is used to measure airspeed.", "en")
        );

        // Extracting batch key phrases
        DocumentResultCollection<ExtractKeyPhraseResult> extractKeyPhraseResults =
            textAnalyticsClient.extractKeyPhrasesBatchWithResponse(textDocumentInputs,
                new TextAnalyticsRequestOptions().setShowStatistics(true), Context.NONE).getValue();

        // Batch statistics
        TextDocumentBatchStatistics batchStatistics = extractKeyPhraseResults.getStatistics();
        System.out.printf(
            "A batch of document statistics, transaction count: %s, valid document count: %s.%n",
            batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

        // Extracted key phrase for each of document from a batch of documents
        for (ExtractKeyPhraseResult extractKeyPhraseResult : extractKeyPhraseResults) {
            System.out.printf("Document ID: %s%n", extractKeyPhraseResult.getId());
            // Valid document
            System.out.println("Extracted phrases:");
            for (String keyPhrase : extractKeyPhraseResult.getKeyPhrases()) {
                System.out.printf("%s.%n", keyPhrase);
            }
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.extractKeyPhrasesBatchWithResponse#List-TextAnalyticsRequestOptions-Context
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
            documentSentiment.getSentimentScores().getPositive(),
            documentSentiment.getSentimentScores().getNeutral(),
            documentSentiment.getSentimentScores().getNegative());

        for (SentenceSentiment sentenceSentiment : documentSentiment.getSentences()) {
            System.out.printf(
                "Recognized sentence sentiment: %s, positive score: %.2f, neutral score: %.2f, negative score: %.2f.%n",
                sentenceSentiment.getSentiment(),
                sentenceSentiment.getSentimentScores().getPositive(),
                sentenceSentiment.getSentimentScores().getNeutral(),
                sentenceSentiment.getSentimentScores().getNegative());
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.analyzeSentiment#String
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#analyzeSentimentWithResponse(String, String, Context)}
     */
    public void analyzeSentimentWithResponse() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.analyzeSentimentWithResponse#String-String-Context
        final DocumentSentiment documentSentiment = textAnalyticsClient.analyzeSentimentWithResponse(
            "The hotel was dark and unclean.", "en", Context.NONE).getValue();

        System.out.printf("Recognized sentiment: %s, positive score: %.2f, neutral score: %.2f, negative score: %.2f.%n",
            documentSentiment.getSentiment(),
            documentSentiment.getSentimentScores().getPositive(),
            documentSentiment.getSentimentScores().getNeutral(),
            documentSentiment.getSentimentScores().getNegative());

        for (SentenceSentiment sentenceSentiment : documentSentiment.getSentences()) {
            System.out.printf(
                "Recognized sentence sentiment: %s, positive score: %.2f, neutral score: %.2f, negative score: %.2f.%n",
                sentenceSentiment.getSentiment(),
                sentenceSentiment.getSentimentScores().getPositive(),
                sentenceSentiment.getSentimentScores().getNeutral(),
                sentenceSentiment.getSentimentScores().getNegative());
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.analyzeSentimentWithResponse#String-String-Context
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#analyzeSentimentBatch(List)}
     */
    public void analyzeSentimentStringList() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.analyzeSentimentBatch#List
        final List<String> textInputs = Arrays.asList(
            "The hotel was dark and unclean. The restaurant had amazing gnocchi.",
            "The restaurant had amazing gnocchi. The hotel was dark and unclean."
        );

        // Analyzing batch sentiments
        DocumentResultCollection<AnalyzeSentimentResult> sentimentBatchResult =
            textAnalyticsClient.analyzeSentimentBatch(textInputs);

        // Batch statistics
        final TextDocumentBatchStatistics batchStatistics = sentimentBatchResult.getStatistics();
        System.out.printf(
            "A batch of document statistics, transaction count: %s, valid document count: %s.%n",
            batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

        // Analyzed sentiment for each of document from a batch of documents
        for (AnalyzeSentimentResult analyzeSentimentResult : sentimentBatchResult) {
            System.out.printf("Document ID: %s%n", analyzeSentimentResult.getId());
            // Valid document
            final DocumentSentiment documentSentiment = analyzeSentimentResult.getDocumentSentiment();
            System.out.printf(
                "Recognized document sentiment: %s, positive score: %.2f, neutral score: %.2f, negative score: %.2f.%n",
                documentSentiment.getSentiment(),
                documentSentiment.getSentimentScores().getPositive(),
                documentSentiment.getSentimentScores().getNeutral(),
                documentSentiment.getSentimentScores().getNegative());
            for (SentenceSentiment sentenceSentiment : documentSentiment.getSentences()) {
                System.out.printf(
                    "Recognized sentence sentiment: %s, positive score: %.2f, neutral score: %.2f, negative score: %.2f.%n",
                    sentenceSentiment.getSentiment(),
                    sentenceSentiment.getSentimentScores().getPositive(),
                    sentenceSentiment.getSentimentScores().getNeutral(),
                    sentenceSentiment.getSentimentScores().getNegative());
            }
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.analyzeSentimentBatch#List
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#analyzeSentimentBatchWithResponse(List, TextAnalyticsRequestOptions,
     * Context)}
     */
    public void analyzeSentimentWithResponseStringList() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.analyzeSentimentBatchWithResponse#List-String-TextAnalyticsRequestOptions-Context
        List<String> textInputs = Arrays.asList(
            "The hotel was dark and unclean. The restaurant had amazing gnocchi.",
            "The restaurant had amazing gnocchi. The hotel was dark and unclean."
        );

        // Analyzing batch sentiments
        DocumentResultCollection<AnalyzeSentimentResult> sentimentBatchResult =
            textAnalyticsClient.analyzeSentimentBatchWithResponse(textInputs, "en",
                null, Context.NONE).getValue();

        // Batch statistics

        TextDocumentBatchStatistics batchStatistics = sentimentBatchResult.getStatistics();
        System.out.printf("A batch of document statistics, transaction count: %s, valid document count: %s.%n",
            batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

        // Analyzed sentiment for each of document from a batch of documents
        for (AnalyzeSentimentResult analyzeSentimentResult : sentimentBatchResult) {
            System.out.printf("Document ID: %s%n", analyzeSentimentResult.getId());
            // Valid document
            DocumentSentiment documentSentiment = analyzeSentimentResult.getDocumentSentiment();
            System.out.printf(
                "Recognized document sentiment: %s, positive score: %.2f, neutral score: %.2f, negative score: %.2f.%n",
                documentSentiment.getSentiment(),
                documentSentiment.getSentimentScores().getPositive(),
                documentSentiment.getSentimentScores().getNeutral(),
                documentSentiment.getSentimentScores().getNegative());
            for (SentenceSentiment sentenceSentiment : documentSentiment.getSentences()) {
                System.out.printf(
                    "Recognized sentence sentiment: %s, positive score: %.2f, neutral score: %.2f, negative score: %.2f.%n",
                    sentenceSentiment.getSentiment(),
                    sentenceSentiment.getSentimentScores().getPositive(),
                    sentenceSentiment.getSentimentScores().getNeutral(),
                    sentenceSentiment.getSentimentScores().getNegative());
            }
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.analyzeSentimentBatchWithResponse#List-String-TextAnalyticsRequestOptions-Context
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#analyzeSentimentBatchWithResponse(List, TextAnalyticsRequestOptions,
     * Context)}
     */
    public void analyzeBatchSentimentWithResponse() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.analyzeSentimentBatchWithResponse#List-TextAnalyticsRequestOptions-Context
        List<TextDocumentInput> textDocumentInputs = Arrays.asList(
            new TextDocumentInput("1", "The hotel was dark and unclean. The restaurant had amazing gnocchi.", "en"),
            new TextDocumentInput("2", "The restaurant had amazing gnocchi. The hotel was dark and unclean.", "en")
        );

        // Analyzing batch sentiments
        DocumentResultCollection<AnalyzeSentimentResult> sentimentBatchResult =
            textAnalyticsClient.analyzeSentimentBatchWithResponse(textDocumentInputs,
                new TextAnalyticsRequestOptions().setShowStatistics(true), Context.NONE).getValue();

        // Batch statistics
        TextDocumentBatchStatistics batchStatistics = sentimentBatchResult.getStatistics();
        System.out.printf("A batch of document statistics, transaction count: %s, valid document count: %s.%n",
            batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

        // Analyzed sentiment for each of document from a batch of documents
        for (AnalyzeSentimentResult analyzeSentimentResult : sentimentBatchResult) {
            System.out.printf("Document ID: %s%n", analyzeSentimentResult.getId());
            // Valid document
            DocumentSentiment documentSentiment = analyzeSentimentResult.getDocumentSentiment();
            System.out.printf(
                "Recognized document sentiment: %s, positive score: %.2f, neutral score: %.2f, negative score: %.2f.%n",
                documentSentiment.getSentiment(),
                documentSentiment.getSentimentScores().getPositive(),
                documentSentiment.getSentimentScores().getNeutral(),
                documentSentiment.getSentimentScores().getNegative());
            for (SentenceSentiment sentenceSentiment : documentSentiment.getSentences()) {
                System.out.printf(
                    "Recognized sentence sentiment: %s, positive score: %.2f, neutral score: %.2f, negative score: %.2f.%n",
                    sentenceSentiment.getSentiment(),
                    sentenceSentiment.getSentimentScores().getPositive(),
                    sentenceSentiment.getSentimentScores().getNeutral(),
                    sentenceSentiment.getSentimentScores().getNegative());
            }
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.analyzeSentimentBatchWithResponse#List-TextAnalyticsRequestOptions-Context
    }
}
