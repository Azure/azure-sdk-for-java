// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.AnalyzeSentimentResult;
import com.azure.ai.textanalytics.models.CategorizedEntity;
import com.azure.ai.textanalytics.models.DetectLanguageInput;
import com.azure.ai.textanalytics.models.DetectLanguageResult;
import com.azure.ai.textanalytics.models.DetectedLanguage;
import com.azure.ai.textanalytics.models.DocumentResultCollection;
import com.azure.ai.textanalytics.models.ExtractKeyPhraseResult;
import com.azure.ai.textanalytics.models.LinkedEntity;
import com.azure.ai.textanalytics.models.PiiEntity;
import com.azure.ai.textanalytics.models.RecognizeEntitiesResult;
import com.azure.ai.textanalytics.models.RecognizeLinkedEntitiesResult;
import com.azure.ai.textanalytics.models.RecognizePiiEntitiesResult;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextAnalyticsApiKeyCredential;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.models.TextSentiment;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.util.Context;

import java.util.Arrays;
import java.util.List;

/**
 * Code snippets for {@link TextAnalyticsClient} and {@link TextAnalyticsClientBuilder}
 */
public class TextAnalyticsClientJavaDocCodeSnippets {
    private final TextAnalyticsClient textAnalyticsClient = new TextAnalyticsClientBuilder().buildClient();

    /**
     * Code snippet for creating a {@link TextAnalyticsClient} with pipeline
     */
    public void createTextAnalyticsClientWithPipeline() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.pipeline.instantiation
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(/* add policies */)
            .build();

        TextAnalyticsClient textAnalyticsClient = new TextAnalyticsClientBuilder()
            .subscriptionKey(new TextAnalyticsApiKeyCredential("{subscription_key}"))
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
            .subscriptionKey(new TextAnalyticsApiKeyCredential("{subscription_key}"))
            .endpoint("{endpoint}")
            .buildClient();
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.instantiation
    }

    // Languages
    /**
     * Code snippet for {@link TextAnalyticsClient#detectLanguage(String)}
     */
    public void detectLanguageSingleText() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.detectLanguage#String
        final DetectLanguageResult detectLanguageResult = textAnalyticsClient.detectLanguage("Bonjour tout le monde");
        for (DetectedLanguage detectedLanguage : detectLanguageResult.getDetectedLanguages()) {
            System.out.printf("Detected languages name: %s, ISO 6391 name: %s, score: %s.%n",
                detectedLanguage.getName(), detectedLanguage.getIso6391Name(), detectedLanguage.getScore());
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.detectLanguage#String
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#detectLanguageWithResponse(String, String, Context)}
     */
    public void detectLanguageForSingleInputTextAndCountryHintWithResponse() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.detectLanguageWithResponse#String-String-Context
        final DetectLanguageResult detectLanguageResult = textAnalyticsClient.detectLanguageWithResponse(
            "This text is in English", "US", Context.NONE).getValue();
        for (DetectedLanguage detectedLanguage : detectLanguageResult.getDetectedLanguages()) {
            System.out.printf("Detected languages name: %s, ISO 6391 name: %s, score: %s.%n",
                detectedLanguage.getName(), detectedLanguage.getIso6391Name(), detectedLanguage.getScore());
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.detectLanguageWithResponse#String-String-Context
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#detectLanguages(List)}
     */
    public void detectLanguageForListInputTexts() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.detectLanguages#List
        final List<String> textInputs = Arrays.asList(
            "This is written in English",
            "Este es un document escrito en Español.");
        final DocumentResultCollection<DetectLanguageResult> detectLanguageResults =
            textAnalyticsClient.detectLanguages(textInputs);

        // Batch statistics
        final TextDocumentBatchStatistics batchStatistics = detectLanguageResults.getStatistics();
        System.out.printf(
            "A batch of document statistics, transaction count: %s, valid document count: %s.%n",
            batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

        for (DetectLanguageResult detectLanguageResult : detectLanguageResults) {
            System.out.printf("Document ID: %s%n", detectLanguageResult.getId());
            for (DetectedLanguage detectedLanguage : detectLanguageResult.getDetectedLanguages()) {
                System.out.printf("Detected language: %s, ISO 6391 name: %s, score: %s.%n",
                    detectedLanguage.getName(), detectedLanguage.getIso6391Name(), detectedLanguage.getScore());
            }
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.detectLanguages#List
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#detectLanguagesWithResponse(List, String, Context)}
     */
    public void detectLanguageForListInputTextsWithResponse() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.detectLanguagesWithResponse#List-String-Context
        final List<String> textInputs = Arrays.asList(
            "This is written in English",
            "Este es un document escrito en Español.");
        final DocumentResultCollection<DetectLanguageResult> detectLanguageResults =
            textAnalyticsClient.detectLanguagesWithResponse(textInputs, "US", Context.NONE).getValue();

        // Batch statistics
        final TextDocumentBatchStatistics batchStatistics = detectLanguageResults.getStatistics();
        System.out.printf(
            "A batch of document statistics, transaction count: %s, valid document count: %s.%n",
            batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

        for (DetectLanguageResult detectLanguageResult : detectLanguageResults) {
            System.out.printf("Document ID: %s%n", detectLanguageResult.getId());
            for (DetectedLanguage detectedLanguage : detectLanguageResult.getDetectedLanguages()) {
                System.out.printf("Detected language: %s, ISO 6391 name: %s, score: %s.%n",
                    detectedLanguage.getName(), detectedLanguage.getIso6391Name(), detectedLanguage.getScore());
            }
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.detectLanguagesWithResponse#List-String-Context
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#detectBatchLanguages(List)}
     */
    public void detectLanguageForListDetectedLanguageInput() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.detectBatchLanguages#List
        final List<DetectLanguageInput> detectLanguageInputs = Arrays.asList(
            new DetectLanguageInput("1", "This is written in English.", "US"),
            new DetectLanguageInput("2", "Este es un document escrito en Español.", "es")
        );

        final DocumentResultCollection<DetectLanguageResult> detectLanguageResults =
            textAnalyticsClient.detectBatchLanguages(detectLanguageInputs);

        // Batch statistics
        final TextDocumentBatchStatistics batchStatistics = detectLanguageResults.getStatistics();
        System.out.printf(
            "A batch of document statistics, transaction count: %s, valid document count: %s.%n",
            batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

        for (DetectLanguageResult detectLanguageResult : detectLanguageResults) {
            System.out.printf("Document ID: %s%n", detectLanguageResult.getId());
            for (DetectedLanguage detectedLanguage : detectLanguageResult.getDetectedLanguages()) {
                System.out.printf("Detected language: %s, ISO 6391 name: %s, score: %s.%n",
                    detectedLanguage.getName(), detectedLanguage.getIso6391Name(), detectedLanguage.getScore());
            }
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.detectBatchLanguages#List
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#detectBatchLanguagesWithResponse(List, TextAnalyticsRequestOptions, Context)}
     */
    public void detectLanguageForListDetectedLanguageInputWithResponse() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.detectBatchLanguagesWithResponse#List-TextAnalyticsRequestOptions-Context
        final List<DetectLanguageInput> detectLanguageInputs = Arrays.asList(
            new DetectLanguageInput("1", "This is written in English.", "US"),
            new DetectLanguageInput("2", "Este es un document escrito en Español.", "es")
        );

        final DocumentResultCollection<DetectLanguageResult> detectLanguageResults =
            textAnalyticsClient.detectBatchLanguagesWithResponse(detectLanguageInputs,
                new TextAnalyticsRequestOptions().setShowStatistics(true), Context.NONE).getValue();

        // Batch statistics
        final TextDocumentBatchStatistics batchStatistics = detectLanguageResults.getStatistics();
        System.out.printf(
            "A batch of document statistics, transaction count: %s, valid document count: %s.%n",
            batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

        for (DetectLanguageResult detectLanguageResult : detectLanguageResults) {
            System.out.printf("Document ID: %s%n", detectLanguageResult.getId());
            for (DetectedLanguage detectedLanguage : detectLanguageResult.getDetectedLanguages()) {
                System.out.printf("Detected language: %s, ISO 6391 name: %s, score: %s.%n",
                    detectedLanguage.getName(), detectedLanguage.getIso6391Name(), detectedLanguage.getScore());
            }
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.detectBatchLanguagesWithResponse#List-TextAnalyticsRequestOptions-Context
    }

    // Entity
    /**
     * Code snippet for {@link TextAnalyticsClient#recognizeEntities(String)}
     */
    public void recognizeEntitiesSingleText() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeEntities#String
        final RecognizeEntitiesResult recognizeEntitiesResult =
            textAnalyticsClient.recognizeEntities("Satya Nadella is the CEO of Microsoft");
        for (CategorizedEntity entity : recognizeEntitiesResult.getEntities()) {
            System.out.printf("Recognized entity: %s, entity Category: %s, score: %s.%n",
                entity.getText(), entity.getCategory(), entity.getScore());
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeEntities#String
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#recognizeEntitiesWithResponse(String, String, Context)}
     */
    public void recognizeEntitiesSingleTextWithResponse() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeEntitiesWithResponse#String-String-Context
        final RecognizeEntitiesResult recognizeEntitiesResult = textAnalyticsClient.recognizeEntitiesWithResponse(
            "Satya Nadella is the CEO of Microsoft", "en", Context.NONE).getValue();

        for (CategorizedEntity entity : recognizeEntitiesResult.getEntities()) {
            System.out.printf("Recognized entity: %s, entity Category: %s, score: %s.%n",
                entity.getText(), entity.getCategory(), entity.getScore());
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeEntitiesWithResponse#String-String-Context
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#recognizeEntities(List)}
     */
    public void recognizeEntitiesListText() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeEntities#List
        final List<String> textInputs = Arrays.asList(
            "I had a wonderful trip to Seattle last week.",
            "I work at Microsoft.");

        final DocumentResultCollection<RecognizeEntitiesResult> recognizeEntitiesResults =
            textAnalyticsClient.recognizeEntities(textInputs);

        // Batch statistics
        final TextDocumentBatchStatistics batchStatistics = recognizeEntitiesResults.getStatistics();
        System.out.printf(
            "A batch of document statistics, transaction count: %s, valid document count: %s.%n",
            batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

        for (RecognizeEntitiesResult recognizeEntitiesResult : recognizeEntitiesResults) {
            for (CategorizedEntity entity : recognizeEntitiesResult.getEntities()) {
                System.out.printf("Recognized entity: %s, entity Category: %s, score: %s.%n",
                    entity.getText(), entity.getCategory(), entity.getScore());
            }
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeEntities#List
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#recognizeEntitiesWithResponse(List, String, Context)}
     */
    public void recognizeEntitiesListTextWithResponse() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeEntitiesWithResponse#List-String-Context
        final List<String> textInputs = Arrays.asList(
            "I had a wonderful trip to Seattle last week.",
            "I work at Microsoft.");

        final DocumentResultCollection<RecognizeEntitiesResult> recognizeEntitiesResults =
            textAnalyticsClient.recognizeEntitiesWithResponse(textInputs, "en", Context.NONE).getValue();

        // Batch statistics
        final TextDocumentBatchStatistics batchStatistics = recognizeEntitiesResults.getStatistics();
        System.out.printf(
            "A batch of document statistics, transaction count: %s, valid document count: %s.%n",
            batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

        for (RecognizeEntitiesResult recognizeEntitiesResult : recognizeEntitiesResults) {
            for (CategorizedEntity entity : recognizeEntitiesResult.getEntities()) {
                System.out.printf("Recognized entity: %s, entity Category: %s, score: %s.%n",
                    entity.getText(), entity.getCategory(), entity.getScore());
            }
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeEntitiesWithResponse#List-String-Context
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#recognizeBatchEntities(List)}
     */
    public void recognizeBatchEntitiesListText() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeBatchEntities#List
        final List<TextDocumentInput> textDocumentInputs = Arrays.asList(
            new TextDocumentInput("0", "I had a wonderful trip to Seattle last week."),
            new TextDocumentInput("1", "I work at Microsoft."));

        final DocumentResultCollection<RecognizeEntitiesResult> recognizeEntitiesResults =
            textAnalyticsClient.recognizeBatchEntities(textDocumentInputs);

        // Batch statistics
        final TextDocumentBatchStatistics batchStatistics = recognizeEntitiesResults.getStatistics();
        System.out.printf(
            "A batch of document statistics, transaction count: %s, valid document count: %s.%n",
            batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

        for (RecognizeEntitiesResult recognizeEntitiesResult : recognizeEntitiesResults) {
            for (CategorizedEntity entity : recognizeEntitiesResult.getEntities()) {
                System.out.printf("Recognized entity: %s, entity Category: %s, score: %s.%n",
                    entity.getText(), entity.getCategory(), entity.getScore());
            }
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeBatchEntities#List
    }

    /**
     * Code snippet for
     * {@link TextAnalyticsClient#recognizeBatchEntitiesWithResponse(List, TextAnalyticsRequestOptions, Context)}
     */
    public void recognizeBatchEntitiesListTextWithResponse() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeBatchEntitiesWithResponse#List-TextAnalyticsRequestOptions-Context
        final List<TextDocumentInput> textDocumentInputs = Arrays.asList(
            new TextDocumentInput("0", "I had a wonderful trip to Seattle last week."),
            new TextDocumentInput("1", "I work at Microsoft."));

        final DocumentResultCollection<RecognizeEntitiesResult> recognizeEntitiesResults =
            textAnalyticsClient.recognizeBatchEntitiesWithResponse(textDocumentInputs,
                new TextAnalyticsRequestOptions().setShowStatistics(true), Context.NONE).getValue();

        // Batch statistics
        final TextDocumentBatchStatistics batchStatistics = recognizeEntitiesResults.getStatistics();
        System.out.printf(
            "A batch of document statistics, transaction count: %s, valid document count: %s.%n",
            batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

        for (RecognizeEntitiesResult recognizeEntitiesResult : recognizeEntitiesResults) {
            for (CategorizedEntity entity : recognizeEntitiesResult.getEntities()) {
                System.out.printf("Recognized entity: %s, entity Category: %s, score: %s.%n",
                    entity.getText(), entity.getCategory(), entity.getScore());
            }
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeBatchEntitiesWithResponse#List-TextAnalyticsRequestOptions-Context
    }

    // PII Entity
    /**
     * Code snippet for {@link TextAnalyticsClient#recognizePiiEntities(String)}
     */
    public void recognizePiiEntitiesSingleText() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.recognizePiiEntities#String
        final RecognizePiiEntitiesResult recognizePiiEntitiesResult =
            textAnalyticsClient.recognizePiiEntities("My SSN is 555-55-5555");
        for (PiiEntity entity : recognizePiiEntitiesResult.getEntities()) {
            System.out.printf("Recognized PII entity: %s, entity Category: %s, score: %s.%n",
                entity.getText(), entity.getCategory(), entity.getScore());
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.recognizePiiEntities#String
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#recognizePiiEntitiesWithResponse(String, String, Context)}
     */
    public void recognizePiiEntitiesSingleTextWithResponse() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.recognizePiiEntitiesWithResponse#String-String-Context
        final RecognizePiiEntitiesResult recognizePiiEntitiesResult =
            textAnalyticsClient.recognizePiiEntitiesWithResponse("My SSN is 555-55-5555", "en", Context.NONE)
                .getValue();

        for (PiiEntity entity : recognizePiiEntitiesResult.getEntities()) {
            System.out.printf("Recognized PII entity: %s, entity Category: %s, score: %s.%n",
                entity.getText(), entity.getCategory(), entity.getScore());
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.recognizePiiEntitiesWithResponse#String-String-Context
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#recognizePiiEntities(List)}
     */
    public void recognizePiiEntitiesListText() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.recognizePiiEntities#List
        final List<String> textInputs = Arrays.asList("My SSN is 555-55-5555", "Visa card 4111 1111 1111 1111");

        final DocumentResultCollection<RecognizePiiEntitiesResult> recognizePiiEntitiesResults =
            textAnalyticsClient.recognizePiiEntities(textInputs);

        // Batch statistics
        final TextDocumentBatchStatistics batchStatistics = recognizePiiEntitiesResults.getStatistics();
        System.out.printf(
            "A batch of document statistics, transaction count: %s, valid document count: %s.%n",
            batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

        for (RecognizePiiEntitiesResult recognizePiiEntitiesResult : recognizePiiEntitiesResults) {
            for (PiiEntity entity : recognizePiiEntitiesResult.getEntities()) {
                System.out.printf("Recognized PII entity: %s, entity Category: %s, score: %s.%n",
                    entity.getText(), entity.getCategory(), entity.getScore());
            }
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.recognizePiiEntities#List
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#recognizePiiEntitiesWithResponse(List, String, Context)}
     */
    public void recognizePiiEntitiesListTextWithResponse() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.recognizePiiEntitiesWithResponse#List-String-Context
        final List<String> textInputs = Arrays.asList("My SSN is 555-55-5555", "Visa card 4111 1111 1111 1111");

        final DocumentResultCollection<RecognizePiiEntitiesResult> recognizePiiEntitiesResults =
            textAnalyticsClient.recognizePiiEntitiesWithResponse(textInputs, "en", Context.NONE).getValue();

        // Batch statistics
        final TextDocumentBatchStatistics batchStatistics = recognizePiiEntitiesResults.getStatistics();
        System.out.printf(
            "A batch of document statistics, transaction count: %s, valid document count: %s.%n",
            batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

        for (RecognizePiiEntitiesResult recognizePiiEntitiesResult : recognizePiiEntitiesResults) {
            for (PiiEntity entity : recognizePiiEntitiesResult.getEntities()) {
                System.out.printf("Recognized PII entity: %s, entity Category: %s, score: %s.%n",
                    entity.getText(), entity.getCategory(), entity.getScore());
            }
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.recognizePiiEntitiesWithResponse#List-String-Context
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#recognizeBatchPiiEntities(List)}
     */
    public void recognizeBatchPiiEntitiesListText() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeBatchPiiEntities#List
        final List<TextDocumentInput> textDocumentInputs = Arrays.asList(
            new TextDocumentInput("0", "My SSN is 555-55-5555"),
            new TextDocumentInput("1", "Visa card 4111 1111 1111 1111"));

        final DocumentResultCollection<RecognizePiiEntitiesResult> recognizePiiEntitiesResults =
            textAnalyticsClient.recognizeBatchPiiEntities(textDocumentInputs);

        // Batch statistics
        final TextDocumentBatchStatistics batchStatistics = recognizePiiEntitiesResults.getStatistics();
        System.out.printf(
            "A batch of document statistics, transaction count: %s, valid document count: %s.%n",
            batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

        for (RecognizePiiEntitiesResult recognizePiiEntitiesResult : recognizePiiEntitiesResults) {
            for (PiiEntity entity : recognizePiiEntitiesResult.getEntities()) {
                System.out.printf("Recognized PII entity: %s, entity Category: %s, score: %s.%n",
                    entity.getText(), entity.getCategory(), entity.getScore());
            }
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeBatchPiiEntities#List
    }

    /**
     * Code snippet for
     * {@link TextAnalyticsClient#recognizeBatchPiiEntitiesWithResponse(List, TextAnalyticsRequestOptions, Context)}
     */
    public void recognizeBatchPiiEntitiesListTextWithResponse() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeBatchPiiEntitiesWithResponse#List-TextAnalyticsRequestOptions-Context
        final List<TextDocumentInput> textDocumentInputs = Arrays.asList(
            new TextDocumentInput("0", "My SSN is 555-55-5555"),
            new TextDocumentInput("1", "Visa card 4111 1111 1111 1111"));

        final DocumentResultCollection<RecognizePiiEntitiesResult> recognizePiiEntitiesResults =
            textAnalyticsClient.recognizeBatchPiiEntitiesWithResponse(textDocumentInputs,
                new TextAnalyticsRequestOptions().setShowStatistics(true), Context.NONE).getValue();

        // Batch statistics
        final TextDocumentBatchStatistics batchStatistics = recognizePiiEntitiesResults.getStatistics();
        System.out.printf(
            "A batch of document statistics, transaction count: %s, valid document count: %s.%n",
            batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

        for (RecognizePiiEntitiesResult recognizePiiEntitiesResult : recognizePiiEntitiesResults) {
            for (PiiEntity entity : recognizePiiEntitiesResult.getEntities()) {
                System.out.printf("Recognized PII entity: %s, entity Category: %s, score: %s.%n",
                    entity.getText(), entity.getCategory(), entity.getScore());
            }
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeBatchPiiEntitiesWithResponse#List-TextAnalyticsRequestOptions-Context
    }

    // Linked Entity
    /**
     * Code snippet for {@link TextAnalyticsClient#recognizeLinkedEntities(String)}
     */
    public void recognizeLinkedEntitiesSingleText() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeLinkedEntities#String
        for (LinkedEntity linkedEntity : textAnalyticsClient.recognizeLinkedEntities(
            "Old Faithful is a geyser at Yellowstone Park.").getLinkedEntities()) {
            System.out.printf("Recognized linked entity: %s, URL: %s, data source: %s.%n",
                linkedEntity.getName(), linkedEntity.getUrl(), linkedEntity.getDataSource());
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeLinkedEntities#String
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#recognizeLinkedEntitiesWithResponse(String, String, Context)}
     */
    public void recognizeLinkedEntitiesSingleTextWithResponse() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeLinkedEntitiesWithResponse#String-String-Context
        final RecognizeLinkedEntitiesResult recognizeLinkedEntitiesResult =
            textAnalyticsClient.recognizeLinkedEntitiesWithResponse(
                "Old Faithful is a geyser at Yellowstone Park.", "en", Context.NONE).getValue();

        for (LinkedEntity linkedEntity : recognizeLinkedEntitiesResult.getLinkedEntities()) {
            System.out.printf("Recognized linked entity: %s, URL: %s, data source: %s.%n",
                linkedEntity.getName(), linkedEntity.getUrl(), linkedEntity.getDataSource());
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeLinkedEntitiesWithResponse#String-String-Context
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#recognizeLinkedEntities(List)}
     */
    public void recognizeLinkedEntitiesListText() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeLinkedEntities#List
        final List<String> textInputs = Arrays.asList(
            "Old Faithful is a geyser at Yellowstone Park.",
            "Mount Shasta has lenticular clouds.");

        final DocumentResultCollection<RecognizeLinkedEntitiesResult> recognizeLinkedEntitiesResults =
            textAnalyticsClient.recognizeLinkedEntities(textInputs);

        // Batch statistics
        final TextDocumentBatchStatistics batchStatistics = recognizeLinkedEntitiesResults.getStatistics();
        System.out.printf(
            "A batch of document statistics, transaction count: %s, valid document count: %s.%n",
            batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

        for (RecognizeLinkedEntitiesResult recognizeLinkedEntitiesResult : recognizeLinkedEntitiesResults) {
            for (LinkedEntity linkedEntity : recognizeLinkedEntitiesResult.getLinkedEntities()) {
                System.out.printf("Recognized linked entity: %s, URL: %s, data source: %s.%n",
                    linkedEntity.getName(), linkedEntity.getUrl(), linkedEntity.getDataSource());
            }
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeLinkedEntities#List
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#recognizeLinkedEntitiesWithResponse(List, String, Context)}
     */
    public void recognizeLinkedEntitiesListTextWithResponse() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeLinkedEntitiesWithResponse#List-String-Context
        final List<String> textInputs = Arrays.asList(
            "Old Faithful is a geyser at Yellowstone Park.",
            "Mount Shasta has lenticular clouds.");

        final DocumentResultCollection<RecognizeLinkedEntitiesResult> recognizeLinkedEntitiesResults =
            textAnalyticsClient.recognizeLinkedEntitiesWithResponse(textInputs, "en", Context.NONE).getValue();

        // Batch statistics
        final TextDocumentBatchStatistics batchStatistics = recognizeLinkedEntitiesResults.getStatistics();
        System.out.printf(
            "A batch of document statistics, transaction count: %s, valid document count: %s.%n",
            batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

        for (RecognizeLinkedEntitiesResult recognizeLinkedEntitiesResult : recognizeLinkedEntitiesResults) {
            for (LinkedEntity linkedEntity : recognizeLinkedEntitiesResult.getLinkedEntities()) {
                System.out.printf("Recognized linked entity: %s, URL: %s, data source: %s.%n",
                    linkedEntity.getName(), linkedEntity.getUrl(), linkedEntity.getDataSource());
            }
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeLinkedEntitiesWithResponse#List-String-Context
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#recognizeBatchLinkedEntities(List)}
     */
    public void recognizeBatchLinkedEntitiesListText() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeBatchLinkedEntities#List
        final List<TextDocumentInput> textDocumentInputs = Arrays.asList(
            new TextDocumentInput("1", "Old Faithful is a geyser at Yellowstone Park.", "en"),
            new TextDocumentInput("2", "Mount Shasta has lenticular clouds.", "en")
        );

        final DocumentResultCollection<RecognizeLinkedEntitiesResult> recognizeLinkedEntitiesResults =
            textAnalyticsClient.recognizeBatchLinkedEntities(textDocumentInputs);

        // Batch statistics
        final TextDocumentBatchStatistics batchStatistics = recognizeLinkedEntitiesResults.getStatistics();
        System.out.printf(
            "A batch of document statistics, transaction count: %s, valid document count: %s.%n",
            batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

        for (RecognizeLinkedEntitiesResult recognizeLinkedEntitiesResult : recognizeLinkedEntitiesResults) {
            for (LinkedEntity linkedEntity : recognizeLinkedEntitiesResult.getLinkedEntities()) {
                System.out.printf("Recognized linked entity: %s, URL: %s, data source: %s.%n",
                    linkedEntity.getName(), linkedEntity.getUrl(), linkedEntity.getDataSource());
            }
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeBatchLinkedEntities#List
    }

    /**
     * Code snippet for
     * {@link TextAnalyticsClient#recognizeBatchLinkedEntitiesWithResponse(List, TextAnalyticsRequestOptions, Context)}
     */
    public void recognizeBatchLinkedEntitiesListTextWithResponse() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeBatchLinkedEntitiesWithResponse#List-TextAnalyticsRequestOptions-Context
        final List<TextDocumentInput> textDocumentInputs = Arrays.asList(
            new TextDocumentInput("1", "Old Faithful is a geyser at Yellowstone Park.", "en"),
            new TextDocumentInput("2", "Mount Shasta has lenticular clouds.", "en")
        );

        final DocumentResultCollection<RecognizeLinkedEntitiesResult> recognizeLinkedEntitiesResults =
            textAnalyticsClient.recognizeBatchLinkedEntitiesWithResponse(textDocumentInputs,
                new TextAnalyticsRequestOptions().setShowStatistics(true), Context.NONE).getValue();

        // Batch statistics
        final TextDocumentBatchStatistics batchStatistics = recognizeLinkedEntitiesResults.getStatistics();
        System.out.printf(
            "A batch of document statistics, transaction count: %s, valid document count: %s.%n",
            batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

        for (RecognizeLinkedEntitiesResult recognizeLinkedEntitiesResult : recognizeLinkedEntitiesResults) {
            for (LinkedEntity linkedEntity : recognizeLinkedEntitiesResult.getLinkedEntities()) {
                System.out.printf("Recognized linked entity: %s, URL: %s, data source: %s.%n",
                    linkedEntity.getName(), linkedEntity.getUrl(), linkedEntity.getDataSource());
            }
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.recognizeBatchLinkedEntitiesWithResponse#List-TextAnalyticsRequestOptions-Context
    }

    // Key Phrases
    /**
     * Code snippet for {@link TextAnalyticsClient#extractKeyPhrases(String)}
     */
    public void extractKeyPhrasesSingleText() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.extractKeyPhrases#String
        final List<String> keyPhrases =
            textAnalyticsClient.extractKeyPhrases("My cat might need to see a veterinarian.").getKeyPhrases();

        for (String keyPhrase : keyPhrases) {
            System.out.printf("Recognized phrases: %s.%n", keyPhrase);
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.extractKeyPhrases#String
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#extractKeyPhrasesWithResponse(String, String, Context)}
     */
    public void extractKeyPhrasesSingleTextWithResponse() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.extractKeyPhrasesWithResponse#String-String-Context
        final ExtractKeyPhraseResult extractKeyPhrases = textAnalyticsClient.extractKeyPhrasesWithResponse(
            "My cat might need to see a veterinarian.", "en", Context.NONE).getValue();
        for (String keyPhrases : extractKeyPhrases.getKeyPhrases()) {
            System.out.printf("Recognized phrases: %s.%n", keyPhrases);
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.extractKeyPhrasesWithResponse#String-String-Context
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#extractKeyPhrases(List)}
     */
    public void extractKeyPhrasesListText() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.extractKeyPhrases#List
        final List<String> textInputs = Arrays.asList(
            "My cat might need to see a veterinarian.",
            "The pitot tube is used to measure airspeed."
        );

        // Extracting batch key phrases
        final DocumentResultCollection<ExtractKeyPhraseResult> extractKeyPhraseResults =
            textAnalyticsClient.extractKeyPhrases(textInputs);

        // Batch statistics
        final TextDocumentBatchStatistics batchStatistics = extractKeyPhraseResults.getStatistics();
        System.out.printf(
            "A batch of document statistics, transaction count: %s, valid document count: %s.%n",
            batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

        // Extracted key phrase for each of document from a batch of documents
        for (ExtractKeyPhraseResult extractKeyPhraseResult : extractKeyPhraseResults) {
            System.out.printf("Document ID: %s%n", extractKeyPhraseResult.getId());
            // Valid document
            for (String keyPhrases : extractKeyPhraseResult.getKeyPhrases()) {
                System.out.printf("Extracted phrases: %s.%n", keyPhrases);
            }
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.extractKeyPhrases#List
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#extractKeyPhrasesWithResponse(List, String, Context)}
     */
    public void extractKeyPhrasesListTextWithResponse() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.extractKeyPhrasesWithResponse#List-String-Context
        final List<String> textInputs = Arrays.asList(
            "My cat might need to see a veterinarian.",
            "The pitot tube is used to measure airspeed."
        );

        // Extracting batch key phrases
        final DocumentResultCollection<ExtractKeyPhraseResult> extractKeyPhraseResults =
            textAnalyticsClient.extractKeyPhrasesWithResponse(textInputs, "en", Context.NONE).getValue();

        // Batch statistics
        final TextDocumentBatchStatistics batchStatistics = extractKeyPhraseResults.getStatistics();
        System.out.printf(
            "A batch of document statistics, transaction count: %s, valid document count: %s.%n",
            batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

        // Extracted key phrase for each of document from a batch of documents
        for (ExtractKeyPhraseResult extractKeyPhraseResult : extractKeyPhraseResults) {
            System.out.printf("Document ID: %s%n", extractKeyPhraseResult.getId());
            // Valid document
            for (String keyPhrases : extractKeyPhraseResult.getKeyPhrases()) {
                System.out.printf("Extracted phrases: %s.%n", keyPhrases);
            }
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.extractKeyPhrasesWithResponse#List-String-Context
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#extractBatchKeyPhrases(List)}
     */
    public void extractBatchKeyPhrasesListText() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.extractBatchKeyPhrases#List
        final List<TextDocumentInput> textDocumentInputs = Arrays.asList(
            new TextDocumentInput("1", "My cat might need to see a veterinarian.", "en"),
            new TextDocumentInput("2", "The pitot tube is used to measure airspeed.", "en")
        );

        // Extracting batch key phrases
        final DocumentResultCollection<ExtractKeyPhraseResult> extractKeyPhraseResults =
            textAnalyticsClient.extractBatchKeyPhrases(textDocumentInputs);

        // Batch statistics
        final TextDocumentBatchStatistics batchStatistics = extractKeyPhraseResults.getStatistics();
        System.out.printf(
            "A batch of document statistics, transaction count: %s, valid document count: %s.%n",
            batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

        // Extracted key phrase for each of document from a batch of documents
        for (ExtractKeyPhraseResult extractKeyPhraseResult : extractKeyPhraseResults) {
            System.out.printf("Document ID: %s%n", extractKeyPhraseResult.getId());
            // Valid document
            for (String keyPhrases : extractKeyPhraseResult.getKeyPhrases()) {
                System.out.printf("Extracted phrases: %s.%n", keyPhrases);
            }
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.extractBatchKeyPhrases#List
    }

    /**
     * Code snippet for
     * {@link TextAnalyticsClient#extractBatchKeyPhrasesWithResponse(List, TextAnalyticsRequestOptions, Context)}
     */
    public void extractBatchKeyPhrasesListTextWithResponse() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.extractBatchKeyPhrasesWithResponse#List-TextAnalyticsRequestOptions-Context
        final List<TextDocumentInput> textDocumentInputs = Arrays.asList(
            new TextDocumentInput("1", "My cat might need to see a veterinarian.", "en"),
            new TextDocumentInput("2", "The pitot tube is used to measure airspeed.", "en")
        );

        // Extracting batch key phrases
        final DocumentResultCollection<ExtractKeyPhraseResult> extractKeyPhraseResults =
            textAnalyticsClient.extractBatchKeyPhrasesWithResponse(textDocumentInputs,
                new TextAnalyticsRequestOptions().setShowStatistics(true), Context.NONE).getValue();

        // Batch statistics
        final TextDocumentBatchStatistics batchStatistics = extractKeyPhraseResults.getStatistics();
        System.out.printf(
            "A batch of document statistics, transaction count: %s, valid document count: %s.%n",
            batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

        // Extracted key phrase for each of document from a batch of documents
        for (ExtractKeyPhraseResult extractKeyPhraseResult : extractKeyPhraseResults) {
            System.out.printf("Document ID: %s%n", extractKeyPhraseResult.getId());
            // Valid document
            for (String keyPhrases : extractKeyPhraseResult.getKeyPhrases()) {
                System.out.printf("Extracted phrases: %s.%n", keyPhrases);
            }
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.extractBatchKeyPhrasesWithResponse#List-TextAnalyticsRequestOptions-Context
    }

    // Sentiment
    /**
     * Code snippet for {@link TextAnalyticsClient#analyzeSentiment(String)}
     */
    public void analyzeSentimentSingleText() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.analyzeSentiment#String
        final AnalyzeSentimentResult sentimentResult =
            textAnalyticsClient.analyzeSentiment("The hotel was dark and unclean.");

        final TextSentiment documentSentiment = sentimentResult.getDocumentSentiment();
        System.out.printf(
            "Recognized sentiment: %s, positive score: %s, neutral score: %s, negative score: %s.%n",
            documentSentiment.getTextSentimentClass(),
            documentSentiment.getPositiveScore(),
            documentSentiment.getNeutralScore(),
            documentSentiment.getNegativeScore());

        for (TextSentiment textSentiment : sentimentResult.getSentenceSentiments()) {
            System.out.printf(
                "Recognized sentence sentiment: %s, positive score: %s, neutral score: %s, negative score: %s.%n",
                textSentiment.getTextSentimentClass(),
                textSentiment.getPositiveScore(),
                textSentiment.getNeutralScore(),
                textSentiment.getNegativeScore());
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.analyzeSentiment#String
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#analyzeSentimentWithResponse(String, String, Context)}
     */
    public void analyzeSentimentSingleTextWithResponse() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.analyzeSentimentWithResponse#String-String-Context
        final AnalyzeSentimentResult sentimentResult = textAnalyticsClient.analyzeSentimentWithResponse(
            "The hotel was dark and unclean.", "en", Context.NONE).getValue();

        final TextSentiment documentSentiment = sentimentResult.getDocumentSentiment();
        System.out.printf(
            "Recognized sentiment: %s, positive score: %s, neutral score: %s, negative score: %s.%n",
            documentSentiment.getTextSentimentClass(),
            documentSentiment.getPositiveScore(),
            documentSentiment.getNeutralScore(),
            documentSentiment.getNegativeScore());

        for (TextSentiment textSentiment : sentimentResult.getSentenceSentiments()) {
            System.out.printf(
                "Recognized sentence sentiment: %s, positive score: %s, neutral score: %s, negative score: %s.%n",
                textSentiment.getTextSentimentClass(),
                textSentiment.getPositiveScore(),
                textSentiment.getNeutralScore(),
                textSentiment.getNegativeScore());
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.analyzeSentimentWithResponse#String-String-Context
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#analyzeSentiment(List)}
     */
    public void analyzeSentimentListText() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.analyzeSentiment#List
        final List<String> textInputs = Arrays.asList(
            "The hotel was dark and unclean. The restaurant had amazing gnocchi.",
            "The restaurant had amazing gnocchi. The hotel was dark and unclean."
        );

        // Analyzing batch sentiments
        DocumentResultCollection<AnalyzeSentimentResult> analyzedBatchResult =
            textAnalyticsClient.analyzeSentiment(textInputs);

        // Batch statistics
        final TextDocumentBatchStatistics batchStatistics = analyzedBatchResult.getStatistics();
        System.out.printf(
            "A batch of document statistics, transaction count: %s, valid document count: %s.%n",
            batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

        // Analyzed sentiment for each of document from a batch of documents
        for (AnalyzeSentimentResult analyzeSentimentResult : analyzedBatchResult) {
            System.out.printf("Document ID: %s%n", analyzeSentimentResult.getId());
            // Valid document
            final TextSentiment documentSentiment = analyzeSentimentResult.getDocumentSentiment();
            System.out.printf(
                "Recognized document sentiment: %s, positive score: %s, neutral score: %s, negative score: %s.%n",
                documentSentiment.getTextSentimentClass(),
                documentSentiment.getPositiveScore(),
                documentSentiment.getNeutralScore(),
                documentSentiment.getNegativeScore());
            for (TextSentiment sentenceSentiment : analyzeSentimentResult.getSentenceSentiments()) {
                System.out.printf(
                    "Recognized sentence sentiment: %s, positive score: %s, neutral score: %s, negative score: %s.%n",
                    sentenceSentiment.getTextSentimentClass(),
                    sentenceSentiment.getPositiveScore(),
                    sentenceSentiment.getNeutralScore(),
                    sentenceSentiment.getNegativeScore());
            }
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.analyzeSentiment#List
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#analyzeSentimentWithResponse(List, String, Context)}
     */
    public void analyzeSentimentListTextWithResponse() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.analyzeSentimentWithResponse#List-String-Context
        final List<String> textInputs = Arrays.asList(
            "The hotel was dark and unclean. The restaurant had amazing gnocchi.",
            "The restaurant had amazing gnocchi. The hotel was dark and unclean."
        );

        // Analyzing batch sentiments
        DocumentResultCollection<AnalyzeSentimentResult> analyzedBatchResult =
            textAnalyticsClient.analyzeSentimentWithResponse(textInputs, "en", Context.NONE).getValue();

        // Batch statistics
        final TextDocumentBatchStatistics batchStatistics = analyzedBatchResult.getStatistics();
        System.out.printf(
            "A batch of document statistics, transaction count: %s, valid document count: %s.%n",
            batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

        // Analyzed sentiment for each of document from a batch of documents
        for (AnalyzeSentimentResult analyzeSentimentResult : analyzedBatchResult) {
            System.out.printf("Document ID: %s%n", analyzeSentimentResult.getId());
            // Valid document
            final TextSentiment documentSentiment = analyzeSentimentResult.getDocumentSentiment();
            System.out.printf(
                "Recognized document sentiment: %s, positive score: %s, neutral score: %s, negative score: %s.%n",
                documentSentiment.getTextSentimentClass(),
                documentSentiment.getPositiveScore(),
                documentSentiment.getNeutralScore(),
                documentSentiment.getNegativeScore());
            for (TextSentiment sentenceSentiment : analyzeSentimentResult.getSentenceSentiments()) {
                System.out.printf(
                    "Recognized sentence sentiment: %s, positive score: %s, neutral score: %s, negative score: %s.%n",
                    sentenceSentiment.getTextSentimentClass(),
                    sentenceSentiment.getPositiveScore(),
                    sentenceSentiment.getNeutralScore(),
                    sentenceSentiment.getNegativeScore());
            }
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.analyzeSentimentWithResponse#List-String-Context
    }

    /**
     * Code snippet for {@link TextAnalyticsClient#analyzeBatchSentiment(List)}
     */
    public void analyzeBatchSentimentListText() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.analyzeBatchSentiment#List
        final List<TextDocumentInput> textDocumentInputs = Arrays.asList(
            new TextDocumentInput("1", "The hotel was dark and unclean. The restaurant had amazing gnocchi.", "en"),
            new TextDocumentInput("2", "The restaurant had amazing gnocchi. The hotel was dark and unclean.", "en")
        );

        // Analyzing batch sentiments
        final DocumentResultCollection<AnalyzeSentimentResult> analyzedBatchResult =
            textAnalyticsClient.analyzeBatchSentiment(textDocumentInputs);

        // Batch statistics
        final TextDocumentBatchStatistics batchStatistics = analyzedBatchResult.getStatistics();
        System.out.printf(
            "A batch of document statistics, transaction count: %s, valid document count: %s.%n",
            batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

        // Analyzed sentiment for each of document from a batch of documents
        for (AnalyzeSentimentResult analyzeSentimentResult : analyzedBatchResult) {
            System.out.printf("Document ID: %s%n", analyzeSentimentResult.getId());
            // Valid document
            final TextSentiment documentSentiment = analyzeSentimentResult.getDocumentSentiment();
            System.out.printf(
                "Recognized document sentiment: %s, positive score: %s, neutral score: %s, negative score: %s.%n",
                documentSentiment.getTextSentimentClass(),
                documentSentiment.getPositiveScore(),
                documentSentiment.getNeutralScore(),
                documentSentiment.getNegativeScore());
            for (TextSentiment sentenceSentiment : analyzeSentimentResult.getSentenceSentiments()) {
                System.out.printf(
                    "Recognized sentence sentiment: %s, positive score: %s, neutral score: %s, negative score: %s.%n",
                    sentenceSentiment.getTextSentimentClass(),
                    sentenceSentiment.getPositiveScore(),
                    sentenceSentiment.getNeutralScore(),
                    sentenceSentiment.getNegativeScore());
            }
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.analyzeBatchSentiment#List
    }

    /**
     * Code snippet for
     * {@link TextAnalyticsClient#analyzeBatchSentimentWithResponse(List, TextAnalyticsRequestOptions, Context)}
     */
    public void analyzeBatchSentimentListTextWithResponse() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsClient.analyzeBatchSentimentWithResponse#List-TextAnalyticsRequestOptions-Context
        List<TextDocumentInput> textDocumentInputs = Arrays.asList(
            new TextDocumentInput("1", "The hotel was dark and unclean. The restaurant had amazing gnocchi.", "en"),
            new TextDocumentInput("2", "The restaurant had amazing gnocchi. The hotel was dark and unclean.", "en")
        );

        // Analyzing batch sentiments
        final DocumentResultCollection<AnalyzeSentimentResult> analyzedBatchResult =
            textAnalyticsClient.analyzeBatchSentimentWithResponse(textDocumentInputs,
                new TextAnalyticsRequestOptions().setShowStatistics(true), Context.NONE).getValue();

        // Batch statistics
        final TextDocumentBatchStatistics batchStatistics = analyzedBatchResult.getStatistics();
        System.out.printf(
            "A batch of document statistics, transaction count: %s, valid document count: %s.%n",
            batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

        // Analyzed sentiment for each of document from a batch of documents
        for (AnalyzeSentimentResult analyzeSentimentResult : analyzedBatchResult) {
            System.out.printf("Document ID: %s%n", analyzeSentimentResult.getId());
            // Valid document
            final TextSentiment documentSentiment = analyzeSentimentResult.getDocumentSentiment();
            System.out.printf(
                "Recognized document sentiment: %s, positive score: %s, neutral score: %s, negative score: %s.%n",
                documentSentiment.getTextSentimentClass(),
                documentSentiment.getPositiveScore(),
                documentSentiment.getNeutralScore(),
                documentSentiment.getNegativeScore());
            for (TextSentiment sentenceSentiment : analyzeSentimentResult.getSentenceSentiments()) {
                System.out.printf(
                    "Recognized sentence sentiment: %s, positive score: %s, neutral score: %s, negative score: %s.%n",
                    sentenceSentiment.getTextSentimentClass(),
                    sentenceSentiment.getPositiveScore(),
                    sentenceSentiment.getNeutralScore(),
                    sentenceSentiment.getNegativeScore());
            }
        }
        // END: com.azure.ai.textanalytics.TextAnalyticsClient.analyzeBatchSentimentWithResponse#List-TextAnalyticsRequestOptions-Context
    }
}
