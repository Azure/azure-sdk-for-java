// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.AnalyzeSentimentResult;
import com.azure.ai.textanalytics.models.DetectLanguageInput;
import com.azure.ai.textanalytics.models.DetectLanguageResult;
import com.azure.ai.textanalytics.models.DetectedLanguage;
import com.azure.ai.textanalytics.models.DocumentResultCollection;
import com.azure.ai.textanalytics.models.ExtractKeyPhraseResult;
import com.azure.ai.textanalytics.models.LinkedEntity;
import com.azure.ai.textanalytics.models.NamedEntity;
import com.azure.ai.textanalytics.models.RecognizeEntitiesResult;
import com.azure.ai.textanalytics.models.RecognizeLinkedEntitiesResult;
import com.azure.ai.textanalytics.models.RecognizePiiEntitiesResult;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.models.TextSentiment;

import java.util.Arrays;
import java.util.List;

/**
 * Code snippet for {@link TextAnalyticsAsyncClient}
 */
public class TextAnalyticsAsyncClientJavaDocCodeSnippets {
    private static final String SUBSCRIPTION_KEY = null;
    private static final String ENDPOINT = null;

    /**
     * Code snippet for creating a {@link TextAnalyticsAsyncClient}
     *
     * @return The TextAnalyticsAsyncClient object
     */
    public TextAnalyticsAsyncClient createTextAnalyticsAsyncClient() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.instantiation
        TextAnalyticsAsyncClient textAnalyticsAsyncClient = new TextAnalyticsClientBuilder()
            .subscriptionKey(SUBSCRIPTION_KEY)
            .endpoint(ENDPOINT)
            .buildAsyncClient();
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.instantiation
        return textAnalyticsAsyncClient;
    }

    // Languages

    /**
     * Code snippet for detecting Language
     */
    public void detectLanguageCodeSnippets() {
        TextAnalyticsAsyncClient textAnalyticsAsyncClient = createTextAnalyticsAsyncClient();

        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.detectLanguage#string
        String inputText = "Bonjour tout le monde";
        textAnalyticsAsyncClient.detectLanguage(inputText).subscribe(detectLanguageResult -> {
            for (DetectedLanguage detectedLanguage : detectLanguageResult.getDetectedLanguages()) {
                System.out.printf("Detected languages name: %s, ISO 6391 Name: %s, Score: %s.%n",
                    detectedLanguage.getName(),
                    detectedLanguage.getIso6391Name(),
                    detectedLanguage.getScore());
            }
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.detectLanguage#string

        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.detectLanguageWithResponse#string-string
        String input = "This text is in English";
        String countryHint = "US";
        textAnalyticsAsyncClient.detectLanguageWithResponse(input, countryHint).subscribe(detectLanguageResult -> {
            for (DetectedLanguage detectedLanguage : detectLanguageResult.getValue().getDetectedLanguages()) {
                System.out.printf("Detected languages name: %s, ISO 6391 Name: %s, Score: %s.%n",
                    detectedLanguage.getName(),
                    detectedLanguage.getIso6391Name(),
                    detectedLanguage.getScore());
            }
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.detectLanguageWithResponse#string-string

        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.detectLanguages#List
        final List<String> inputs = Arrays.asList(
            "This is written in English", "Este es un document escrito en Español.");
        textAnalyticsAsyncClient.detectLanguages(inputs).subscribe(detectedBatchResult -> {
            System.out.printf("Model version: %s%n", detectedBatchResult.getModelVersion());

            // Batch statistics
            final TextDocumentBatchStatistics batchStatistics = detectedBatchResult.getStatistics();
            System.out.printf("Batch statistics, document count: %s, erroneous document count: %s,"
                    + " transaction count: %s, valid document count: %s.%n",
                batchStatistics.getDocumentCount(),
                batchStatistics.getTransactionCount(),
                batchStatistics.getValidDocumentCount());

            for (DetectLanguageResult detectLanguageResult : detectedBatchResult) {
                for (DetectedLanguage detectedLanguage : detectLanguageResult.getDetectedLanguages()) {
                    System.out.printf("Another detected language: %s, ISO 6391 name: %s, score: %s.%n",
                        detectedLanguage.getName(),
                        detectedLanguage.getIso6391Name(),
                        detectedLanguage.getScore());
                }
            }
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.detectLanguages#List

        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.detectLanguagesWithResponse#List-String
        final List<String> listInputs = Arrays.asList(
            "This is written in English", "Este es un document escrito en Español.");
        textAnalyticsAsyncClient.detectLanguagesWithResponse(listInputs, "US").subscribe(response -> {
            DocumentResultCollection<DetectLanguageResult> detectedBatchResult = response.getValue();
            System.out.printf("Model version: %s%n", detectedBatchResult.getModelVersion());

            // Batch statistics
            final TextDocumentBatchStatistics batchStatistics = detectedBatchResult.getStatistics();
            System.out.printf("Batch statistics, document count: %s, erroneous document count: %s, "
                    + "transaction count: %s, valid document count: %s.%n",
                batchStatistics.getDocumentCount(),
                batchStatistics.getErroneousDocumentCount(),
                batchStatistics.getTransactionCount(),
                batchStatistics.getValidDocumentCount());

            for (DetectLanguageResult detectLanguageResult : detectedBatchResult) {
                for (DetectedLanguage detectedLanguage : detectLanguageResult.getDetectedLanguages()) {
                    System.out.printf("Another detected language: %s, ISO 6391 name: %s, score: %s.%n",
                        detectedLanguage.getName(),
                        detectedLanguage.getIso6391Name(),
                        detectedLanguage.getScore());
                }
            }
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.detectLanguagesWithResponse#List-String

        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.detectBatchLanguages#List
        List<DetectLanguageInput> detectLanguageInputs = Arrays.asList(
            new DetectLanguageInput("1", "This is written in English.", "US"),
            new DetectLanguageInput("2", "Este es un document escrito en Español.", "es")
        );
        textAnalyticsAsyncClient.detectBatchLanguages(detectLanguageInputs).subscribe(detectedBatchResult -> {
            System.out.printf("Model version: %s%n", detectedBatchResult.getModelVersion());

            // Batch statistics
            final TextDocumentBatchStatistics batchStatistics = detectedBatchResult.getStatistics();
            System.out.printf("Batch statistics, document count: %s, erroneous document count: %s, "
                    + "transaction count: %s, valid document count: %s.%n",
                batchStatistics.getDocumentCount(),
                batchStatistics.getErroneousDocumentCount(),
                batchStatistics.getTransactionCount(),
                batchStatistics.getValidDocumentCount());

            for (DetectLanguageResult detectLanguageResult : detectedBatchResult) {
                for (DetectedLanguage detectedLanguage : detectLanguageResult.getDetectedLanguages()) {
                    System.out.printf("Another detected language: %s, ISO 6391 name: %s, score: %s.%n",
                        detectedLanguage.getName(),
                        detectedLanguage.getIso6391Name(),
                        detectedLanguage.getScore());
                }
            }
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.detectBatchLanguages#List

        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.detectBatchLanguagesWithResponse#List-TextAnalyticsRequestOptions
        List<DetectLanguageInput> detectLanguageInputs1 = Arrays.asList(
            new DetectLanguageInput("1", "This is written in English.", "US"),
            new DetectLanguageInput("2", "Este es un document escrito en Español.", "es")
        );

        // Request options: show statistics and model version
        final TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions().setShowStatistics(true);

        textAnalyticsAsyncClient.detectBatchLanguagesWithResponse(detectLanguageInputs1, requestOptions)
            .subscribe(response -> {
                final DocumentResultCollection<DetectLanguageResult> detectedBatchResult = response.getValue();
                System.out.printf("Model version: %s%n", detectedBatchResult.getModelVersion());

                // Batch statistics
                final TextDocumentBatchStatistics batchStatistics = detectedBatchResult.getStatistics();
                System.out.printf("Batch statistics, document count: %s, erroneous document count: %s,"
                        + " transaction count: %s, valid document count: %s.%n",
                    batchStatistics.getDocumentCount(),
                    batchStatistics.getErroneousDocumentCount(),
                    batchStatistics.getTransactionCount(),
                    batchStatistics.getValidDocumentCount());

                for (DetectLanguageResult detectLanguageResult : detectedBatchResult) {
                    for (DetectedLanguage detectedLanguage : detectLanguageResult.getDetectedLanguages()) {
                        System.out.printf("Other detected language: %s, ISO 6391 name: %s, score: %s.%n",
                            detectedLanguage.getName(),
                            detectedLanguage.getIso6391Name(),
                            detectedLanguage.getScore());
                    }
                }
            });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.detectBatchLanguagesWithResponse#List-TextAnalyticsRequestOptions
    }

    // Entity

    /**
     * Code snippet for recognizing entities
     */
    public void recognizeEntitiesCodeSnippets() {
        TextAnalyticsAsyncClient textAnalyticsAsyncClient = createTextAnalyticsAsyncClient();

        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeEntities#string
        String inputText = "Satya Nadella is the CEO of Microsoft";
        textAnalyticsAsyncClient.recognizeEntities(inputText).subscribe(recognizeEntitiesResult -> {
            for (NamedEntity entity : recognizeEntitiesResult.getNamedEntities()) {
                System.out.printf(
                    "Recognized Named Entity: %s, Type: %s, Subtype: %s, Score: %s.%n",
                    entity.getText(),
                    entity.getType(),
                    entity.getSubtype(),
                    entity.getScore());
            }
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeEntities#string

        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeEntitiesWithResponse#string-string
        String inputText1 = "Satya Nadella is the CEO of Microsoft";
        textAnalyticsAsyncClient.recognizeEntitiesWithResponse(inputText1, "en")
            .subscribe(recognizeEntitiesResult -> {
                for (NamedEntity entity : recognizeEntitiesResult.getValue().getNamedEntities()) {
                    System.out.printf(
                        "Recognized Named Entity: %s, Type: %s, Subtype: %s, Score: %s.%n",
                        entity.getText(),
                        entity.getType(),
                        entity.getSubtype(),
                        entity.getScore());
                }
            });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeEntitiesWithResponse#string-string


        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeEntities#List
        List<String> inputs = Arrays.asList(
            "I had a wonderful trip to Seattle last week.", "I work at Microsoft.");

        textAnalyticsAsyncClient.recognizeEntities(inputs).subscribe(recognizeEntitiesResults -> {
            System.out.printf("Model version: %s%n", recognizeEntitiesResults.getModelVersion());

            // Batch statistics
            final TextDocumentBatchStatistics batchStatistics = recognizeEntitiesResults.getStatistics();
            System.out.printf("Batch statistics, document count: %s, erroneous document count: %s, "
                    + "transaction count: %s, valid document count: %s.%n",
                batchStatistics.getDocumentCount(),
                batchStatistics.getTransactionCount(),
                batchStatistics.getValidDocumentCount());

            for (RecognizeEntitiesResult recognizeEntitiesResult : recognizeEntitiesResults) {
                for (NamedEntity entity : recognizeEntitiesResult.getNamedEntities()) {
                    System.out.printf(
                        "Recognized Named Entity: %s, Type: %s, Subtype: %s, Score: %s.%n",
                        entity.getText(),
                        entity.getType(),
                        entity.getSubtype(),
                        entity.getScore());
                }
            }
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeEntities#List

        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeEntitiesWithResponse#List-String
        List<String> inputs1 = Arrays.asList(
            "I had a wonderful trip to Seattle last week.", "I work at Microsoft.");

        textAnalyticsAsyncClient.recognizeEntitiesWithResponse(inputs1, "en").subscribe(response -> {
            DocumentResultCollection<RecognizeEntitiesResult> recognizeEntitiesResults = response.getValue();
            System.out.printf("Model version: %s%n", recognizeEntitiesResults.getModelVersion());

            // Batch statistics
            final TextDocumentBatchStatistics batchStatistics = recognizeEntitiesResults.getStatistics();
            System.out.printf("Batch statistics, document count: %s, erroneous document count: %s, "
                    + "transaction count: %s, valid document count: %s.%n",
                batchStatistics.getDocumentCount(),
                batchStatistics.getErroneousDocumentCount(),
                batchStatistics.getTransactionCount(),
                batchStatistics.getValidDocumentCount());

            for (RecognizeEntitiesResult recognizeEntitiesResult : recognizeEntitiesResults) {
                for (NamedEntity entity : recognizeEntitiesResult.getNamedEntities()) {
                    System.out.printf(
                        "Recognized Named Entity: %s, Type: %s, Subtype: %s, Score: %s.%n",
                        entity.getText(),
                        entity.getType(),
                        entity.getSubtype(),
                        entity.getScore());
                }
            }
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeEntitiesWithResponse#List-String

        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeBatchEntities#List
        List<TextDocumentInput> textDocumentInputs = Arrays.asList(
            new TextDocumentInput("0", "I had a wonderful trip to Seattle last week."),
            new TextDocumentInput("1", "I work at Microsoft."));

        textAnalyticsAsyncClient.recognizeBatchEntities(textDocumentInputs).subscribe(recognizeEntitiesResults -> {
            System.out.printf("Model version: %s%n", recognizeEntitiesResults.getModelVersion());

            // Batch statistics
            final TextDocumentBatchStatistics batchStatistics = recognizeEntitiesResults.getStatistics();
            System.out.printf("Batch statistics, document count: %s, erroneous document count: %s, "
                    + "transaction count: %s, valid document count: %s.%n",
                batchStatistics.getDocumentCount(),
                batchStatistics.getErroneousDocumentCount(),
                batchStatistics.getTransactionCount(),
                batchStatistics.getValidDocumentCount());

            for (RecognizeEntitiesResult recognizeEntitiesResult : recognizeEntitiesResults) {
                for (NamedEntity entity : recognizeEntitiesResult.getNamedEntities()) {
                    System.out.printf(
                        "Recognized Named Entity: %s, Type: %s, Subtype: %s, Score: %s.%n",
                        entity.getText(),
                        entity.getType(),
                        entity.getSubtype(),
                        entity.getScore());
                }
            }
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeBatchEntities#List

        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeBatchEntitiesWithResponse#List-TextAnalyticsRequestOptions
        List<TextDocumentInput> textDocumentInputs1 = Arrays.asList(
            new TextDocumentInput("0", "I had a wonderful trip to Seattle last week."),
            new TextDocumentInput("1", "I work at Microsoft."));

        // Request options: show statistics and model version
        final TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions().setShowStatistics(true);

        textAnalyticsAsyncClient.recognizeBatchEntitiesWithResponse(textDocumentInputs1, requestOptions)
            .subscribe(response -> {
                final DocumentResultCollection<RecognizeEntitiesResult> recognizeEntitiesResults = response.getValue();
                System.out.printf("Model version: %s%n", recognizeEntitiesResults.getModelVersion());

                // Batch statistics
                final TextDocumentBatchStatistics batchStatistics = recognizeEntitiesResults.getStatistics();
                System.out.printf("Batch statistics, document count: %s, erroneous document count: %s, "
                        + "transaction count: %s, valid document count: %s.%n",
                    batchStatistics.getDocumentCount(),
                    batchStatistics.getErroneousDocumentCount(),
                    batchStatistics.getTransactionCount(),
                    batchStatistics.getValidDocumentCount());

                for (RecognizeEntitiesResult recognizeEntitiesResult : recognizeEntitiesResults) {
                    for (NamedEntity entity : recognizeEntitiesResult.getNamedEntities()) {
                        System.out.printf(
                            "Recognized Named Entity: %s, Type: %s, Subtype: %s, Score: %s.%n",
                            entity.getText(),
                            entity.getType(),
                            entity.getSubtype(),
                            entity.getScore());
                    }
                }
            });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeBatchEntitiesWithResponse#List-TextAnalyticsRequestOptions
    }

    // Pii Entity

    /**
     * Code snippet for recognizing entities
     */
    public void recognizePiiEntitiesCodeSnippets() {
        TextAnalyticsAsyncClient textAnalyticsAsyncClient = createTextAnalyticsAsyncClient();

        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizePiiEntities#string
        String inputText = "My SSN is 555-55-5555";
        textAnalyticsAsyncClient.recognizePiiEntities(inputText).subscribe(recognizeEntitiesResult -> {
            for (NamedEntity entity : recognizeEntitiesResult.getNamedEntities()) {
                System.out.printf(
                    "Recognized Named Entity: %s, Type: %s, Subtype: %s, Score: %s.%n",
                    entity.getText(),
                    entity.getType(),
                    entity.getSubtype(),
                    entity.getScore());
            }
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizePiiEntities#string

        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizePiiEntitiesWithResponse#string-string
        String inputText1 = "My SSN is 555-55-5555";
        textAnalyticsAsyncClient.recognizePiiEntitiesWithResponse(inputText1, "en")
            .subscribe(recognizeEntitiesResult -> {
                for (NamedEntity entity : recognizeEntitiesResult.getValue().getNamedEntities()) {
                    System.out.printf(
                        "Recognized Named Entity: %s, Type: %s, Subtype: %s, Score: %s.%n",
                        entity.getText(),
                        entity.getType(),
                        entity.getSubtype(),
                        entity.getScore());
                }
            });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizePiiEntitiesWithResponse#string-string


        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizePiiEntities#list-string
        List<String> inputs = Arrays.asList(
            "I had a wonderful trip to Seattle last week.", "I work at Microsoft.");

        textAnalyticsAsyncClient.recognizePiiEntities(inputs).subscribe(recognizeEntitiesResults -> {
            System.out.printf("Model version: %s%n", recognizeEntitiesResults.getModelVersion());

            // Batch statistics
            final TextDocumentBatchStatistics batchStatistics = recognizeEntitiesResults.getStatistics();
            System.out.printf("Batch statistics, document count: %s, erroneous document count: %s, "
                    + "transaction count: %s, valid document count: %s.%n",
                batchStatistics.getDocumentCount(),
                batchStatistics.getTransactionCount(),
                batchStatistics.getValidDocumentCount());

            for (RecognizePiiEntitiesResult recognizeEntitiesResult : recognizeEntitiesResults) {
                for (NamedEntity entity : recognizeEntitiesResult.getNamedEntities()) {
                    System.out.printf(
                        "Recognized Named Entity: %s, Type: %s, Subtype: %s, Score: %s.%n",
                        entity.getText(),
                        entity.getType(),
                        entity.getSubtype(),
                        entity.getScore());
                }
            }
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizePiiEntities#list-string

        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizePiiEntitiesWithResponse#List-String
        List<String> inputs1 = Arrays.asList(
            "I had a wonderful trip to Seattle last week.", "I work at Microsoft.");

        textAnalyticsAsyncClient.recognizePiiEntitiesWithResponse(inputs1, "en").subscribe(response -> {
            DocumentResultCollection<RecognizePiiEntitiesResult> recognizeEntitiesResults = response.getValue();
            System.out.printf("Model version: %s%n", recognizeEntitiesResults.getModelVersion());

            // Batch statistics
            final TextDocumentBatchStatistics batchStatistics = recognizeEntitiesResults.getStatistics();
            System.out.printf("Batch statistics, document count: %s, erroneous document count: %s, "
                    + "transaction count: %s, valid document count: %s.%n",
                batchStatistics.getDocumentCount(),
                batchStatistics.getErroneousDocumentCount(),
                batchStatistics.getTransactionCount(),
                batchStatistics.getValidDocumentCount());

            for (RecognizePiiEntitiesResult recognizeEntitiesResult : recognizeEntitiesResults) {
                for (NamedEntity entity : recognizeEntitiesResult.getNamedEntities()) {
                    System.out.printf(
                        "Recognized Named Entity: %s, Type: %s, Subtype: %s, Score: %s.%n",
                        entity.getText(),
                        entity.getType(),
                        entity.getSubtype(),
                        entity.getScore());
                }
            }
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizePiiEntitiesWithResponse#List-String

        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeBatchPiiEntities#List
        List<TextDocumentInput> textDocumentInputs = Arrays.asList(
            new TextDocumentInput("0", "I had a wonderful trip to Seattle last week."),
            new TextDocumentInput("1", "I work at Microsoft."));

        textAnalyticsAsyncClient.recognizeBatchPiiEntities(textDocumentInputs).subscribe(recognizeEntitiesResults -> {
            System.out.printf("Model version: %s%n", recognizeEntitiesResults.getModelVersion());

            // Batch statistics
            final TextDocumentBatchStatistics batchStatistics = recognizeEntitiesResults.getStatistics();
            System.out.printf("Batch statistics, document count: %s, erroneous document count: %s, "
                    + "transaction count: %s, valid document count: %s.%n",
                batchStatistics.getDocumentCount(),
                batchStatistics.getErroneousDocumentCount(),
                batchStatistics.getTransactionCount(),
                batchStatistics.getValidDocumentCount());

            for (RecognizePiiEntitiesResult recognizeEntitiesResult : recognizeEntitiesResults) {
                for (NamedEntity entity : recognizeEntitiesResult.getNamedEntities()) {
                    System.out.printf(
                        "Recognized Named Entity: %s, Type: %s, Subtype: %s, Score: %s.%n",
                        entity.getText(),
                        entity.getType(),
                        entity.getSubtype(),
                        entity.getScore());
                }
            }
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeBatchPiiEntities#List

        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeBatchPiiEntitiesWithResponse#List-TextAnalyticsRequestOptions
        List<TextDocumentInput> textDocumentInputs1 = Arrays.asList(
            new TextDocumentInput("0", "I had a wonderful trip to Seattle last week."),
            new TextDocumentInput("1", "I work at Microsoft."));

        // Request options: show statistics and model version
        final TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions().setShowStatistics(true);

        textAnalyticsAsyncClient.recognizeBatchPiiEntitiesWithResponse(textDocumentInputs1, requestOptions)
            .subscribe(response -> {
                DocumentResultCollection<RecognizePiiEntitiesResult> recognizeEntitiesResults = response.getValue();
                System.out.printf("Model version: %s%n", recognizeEntitiesResults.getModelVersion());

                // Batch statistics
                final TextDocumentBatchStatistics batchStatistics = recognizeEntitiesResults.getStatistics();
                System.out.printf("Batch statistics, document count: %s, erroneous document count: %s, "
                        + "transaction count: %s, valid document count: %s.%n",
                    batchStatistics.getDocumentCount(),
                    batchStatistics.getErroneousDocumentCount(),
                    batchStatistics.getTransactionCount(),
                    batchStatistics.getValidDocumentCount());

                for (RecognizePiiEntitiesResult recognizeEntitiesResult : recognizeEntitiesResults) {
                    for (NamedEntity entity : recognizeEntitiesResult.getNamedEntities()) {
                        System.out.printf(
                            "Recognized Named Entity: %s, Type: %s, Subtype: %s, Score: %s.%n",
                            entity.getText(),
                            entity.getType(),
                            entity.getSubtype(),
                            entity.getScore());
                    }
                }
            });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeBatchPiiEntitiesWithResponse#List-TextAnalyticsRequestOptions
    }

    // Linked Entity

    /**
     * Code snippet for recognizing entities
     */
    public void recognizeLinkedEntitiesCodeSnippets() {
        TextAnalyticsAsyncClient textAnalyticsAsyncClient = createTextAnalyticsAsyncClient();

        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeLinkedEntities#string
        String inputText = "Old Faithful is a geyser at Yellowstone Park.";
        textAnalyticsAsyncClient.recognizeLinkedEntities(inputText).subscribe(recognizeEntitiesResult -> {
            for (LinkedEntity linkedEntity : recognizeEntitiesResult.getLinkedEntities()) {
                System.out.printf("Recognized Linked NamedEntity: %s, URL: %s, Data Source: %s.%n",
                    linkedEntity.getName(),
                    linkedEntity.getUrl(),
                    linkedEntity.getDataSource());
            }
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeLinkedEntities#string

        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeLinkedEntitiesWithResponse#string-string
        String inputText1 = "Old Faithful is a geyser at Yellowstone Park.";
        textAnalyticsAsyncClient.recognizeLinkedEntitiesWithResponse(inputText1, "en")
            .subscribe(linkedEntitiesResultResponse -> {
                for (LinkedEntity linkedEntity : linkedEntitiesResultResponse.getValue().getLinkedEntities()) {
                    System.out.printf("Recognized Linked NamedEntity: %s, URL: %s, Data Source: %s.%n",
                        linkedEntity.getName(),
                        linkedEntity.getUrl(),
                        linkedEntity.getDataSource());
                }
            });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeLinkedEntitiesWithResponse#string-string


        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeLinkedEntities#list-string
        List<String> inputs = Arrays.asList(
            "I had a wonderful trip to Seattle last week.", "I work at Microsoft.");

        textAnalyticsAsyncClient.recognizeLinkedEntities(inputs).subscribe(recognizeLinkedEntitiesResults -> {
            System.out.printf("Model version: %s%n", recognizeLinkedEntitiesResults.getModelVersion());

            // Batch statistics
            TextDocumentBatchStatistics batchStatistics = recognizeLinkedEntitiesResults.getStatistics();
            System.out.printf("Batch statistics, document count: %s, erroneous document count: %s,"
                    + " transaction count: %s, valid document count: %s.%n",
                batchStatistics.getDocumentCount(),
                batchStatistics.getTransactionCount(),
                batchStatistics.getValidDocumentCount());

            for (RecognizeLinkedEntitiesResult recognizeLinkedEntitiesResult : recognizeLinkedEntitiesResults) {
                for (LinkedEntity linkedEntity : recognizeLinkedEntitiesResult.getLinkedEntities()) {
                    System.out.printf("Recognized Linked NamedEntity: %s, URL: %s, Data Source: %s.%n",
                        linkedEntity.getName(),
                        linkedEntity.getUrl(),
                        linkedEntity.getDataSource());
                }
            }
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeLinkedEntities#list-string

        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeLinkedEntitiesWithResponse#List-String
        List<String> inputs1 = Arrays.asList(
            "I had a wonderful trip to Seattle last week.", "I work at Microsoft.");

        textAnalyticsAsyncClient.recognizeLinkedEntitiesWithResponse(inputs1, "en").subscribe(response -> {
            DocumentResultCollection<RecognizeLinkedEntitiesResult> recognizeLinkedEntitiesResults = response.getValue();
            System.out.printf("Model version: %s%n", recognizeLinkedEntitiesResults.getModelVersion());

            // Batch statistics
            TextDocumentBatchStatistics batchStatistics = recognizeLinkedEntitiesResults.getStatistics();
            System.out.printf("Batch statistics, document count: %s, erroneous document count: %s, "
                    + "transaction count: %s, valid document count: %s.%n",
                batchStatistics.getDocumentCount(),
                batchStatistics.getErroneousDocumentCount(),
                batchStatistics.getTransactionCount(),
                batchStatistics.getValidDocumentCount());

            for (RecognizeLinkedEntitiesResult recognizeLinkedEntitiesResult : recognizeLinkedEntitiesResults) {
                for (LinkedEntity linkedEntity : recognizeLinkedEntitiesResult.getLinkedEntities()) {
                    System.out.printf("Recognized Linked NamedEntity: %s, URL: %s, Data Source: %s.%n",
                        linkedEntity.getName(),
                        linkedEntity.getUrl(),
                        linkedEntity.getDataSource());
                }
            }
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeLinkedEntitiesWithResponse#List-String

        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeBatchLinkedEntities#List
        List<TextDocumentInput> textDocumentInputs = Arrays.asList(
            new TextDocumentInput("0", "I had a wonderful trip to Seattle last week."),
            new TextDocumentInput("1", "I work at Microsoft."));

        textAnalyticsAsyncClient.recognizeBatchLinkedEntities(textDocumentInputs)
            .subscribe(recognizeLinkedEntitiesResults -> {
                System.out.printf("Model version: %s%n", recognizeLinkedEntitiesResults.getModelVersion());

                // Batch statistics
                final TextDocumentBatchStatistics batchStatistics = recognizeLinkedEntitiesResults.getStatistics();
                System.out.printf("Batch statistics, document count: %s, erroneous document count: %s, "
                        + "transaction count: %s, valid document count: %s.%n",
                    batchStatistics.getDocumentCount(),
                    batchStatistics.getErroneousDocumentCount(),
                    batchStatistics.getTransactionCount(),
                    batchStatistics.getValidDocumentCount());

                for (RecognizeLinkedEntitiesResult recognizeLinkedEntitiesResult : recognizeLinkedEntitiesResults) {
                    for (LinkedEntity linkedEntity : recognizeLinkedEntitiesResult.getLinkedEntities()) {
                        System.out.printf("Recognized Linked NamedEntity: %s, URL: %s, Data Source: %s.%n",
                            linkedEntity.getName(),
                            linkedEntity.getUrl(),
                            linkedEntity.getDataSource());
                    }
                }
            });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeBatchLinkedEntities#List

        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeBatchLinkedEntitiesWithResponse#List-TextAnalyticsRequestOptions
        List<TextDocumentInput> textDocumentInputs1 = Arrays.asList(
            new TextDocumentInput("0", "I had a wonderful trip to Seattle last week."),
            new TextDocumentInput("1", "I work at Microsoft."));

        // Request options: show statistics and model version
        final TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions().setShowStatistics(true);

        textAnalyticsAsyncClient.recognizeBatchLinkedEntitiesWithResponse(textDocumentInputs1, requestOptions)
            .subscribe(response -> {
                DocumentResultCollection<RecognizeLinkedEntitiesResult> recognizeLinkedEntitiesResults =
                    response.getValue();
                System.out.printf("Model version: %s%n", recognizeLinkedEntitiesResults.getModelVersion());

                // Batch statistics
                final TextDocumentBatchStatistics batchStatistics = recognizeLinkedEntitiesResults.getStatistics();
                System.out.printf("Batch statistics, document count: %s, erroneous document count: %s, transaction"
                        + " count: %s, valid document count: %s.%n",
                    batchStatistics.getDocumentCount(),
                    batchStatistics.getErroneousDocumentCount(),
                    batchStatistics.getTransactionCount(),
                    batchStatistics.getValidDocumentCount());

                for (RecognizeLinkedEntitiesResult recognizeLinkedEntitiesResult : recognizeLinkedEntitiesResults) {
                    for (LinkedEntity linkedEntity : recognizeLinkedEntitiesResult.getLinkedEntities()) {
                        System.out.printf("Recognized Linked NamedEntity: %s, URL: %s, Data Source: %s.%n",
                            linkedEntity.getName(),
                            linkedEntity.getUrl(),
                            linkedEntity.getDataSource());
                    }
                }
            });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeBatchLinkedEntitiesWithResponse#List-TextAnalyticsRequestOptions
    }

    // Key Phrases

    /**
     * Code snippet for recognizing entities
     */
    public void extractKeyPhrasesCodeSnippets() {
        TextAnalyticsAsyncClient textAnalyticsAsyncClient = createTextAnalyticsAsyncClient();

        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.extractKeyPhrases#string
        String inputText = "Bonjour tout le monde";
        textAnalyticsAsyncClient.extractKeyPhrases(inputText).subscribe(extractKeyPhraseResult -> {
            for (String keyPhrase : extractKeyPhraseResult.getKeyPhrases()) {
                System.out.printf("Recognized phrases: %s.%n", keyPhrase);
            }
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.extractKeyPhrases#string

        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.extractKeyPhrasesWithResponse#string-string
        String inputText1 = "Bonjour tout le monde";
        textAnalyticsAsyncClient.extractKeyPhrasesWithResponse(inputText1, "fr")
            .subscribe(keyPhraseResultResponse -> {
                for (String keyPhrase : keyPhraseResultResponse.getValue().getKeyPhrases()) {
                    System.out.printf("Recognized phrases: %s.%n", keyPhrase);
                }
            });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.extractKeyPhrasesWithResponse#string-string


        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.extractKeyPhrases#list-string
        List<String> inputs = Arrays.asList(
            "Hello world. This is some input text that I love.",
            "Bonjour tout le monde");

        textAnalyticsAsyncClient.extractKeyPhrases(inputs).subscribe(extractKeyPhraseResults -> {
            System.out.printf("Model version: %s%n", extractKeyPhraseResults.getModelVersion());

            // Batch statistics
            final TextDocumentBatchStatistics batchStatistics = extractKeyPhraseResults.getStatistics();
            System.out.printf("Batch statistics, document count: %s, erroneous document count: %s,"
                    + " transaction count: %s, valid document count: %s.%n",
                batchStatistics.getDocumentCount(),
                batchStatistics.getTransactionCount(),
                batchStatistics.getValidDocumentCount());

            for (ExtractKeyPhraseResult extractKeyPhraseResult : extractKeyPhraseResults) {
                for (String keyPhrase : extractKeyPhraseResult.getKeyPhrases()) {
                    System.out.printf("Recognized phrases: %s.%n", keyPhrase);
                }
            }
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.extractKeyPhrases#list-string

        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.extractKeyPhrasesWithResponse#List-String
        List<String> inputs1 = Arrays.asList(
            "Hello world. This is some input text that I love.",
            "Bonjour tout le monde");

        textAnalyticsAsyncClient.extractKeyPhrasesWithResponse(inputs1, "en").subscribe(response -> {
            DocumentResultCollection<ExtractKeyPhraseResult> extractKeyPhraseResults = response.getValue();
            System.out.printf("Model version: %s%n", extractKeyPhraseResults.getModelVersion());

            // Batch statistics
            final TextDocumentBatchStatistics batchStatistics = extractKeyPhraseResults.getStatistics();
            System.out.printf("Batch statistics, document count: %s, erroneous document count: %s, "
                    + "transaction count: %s, valid document count: %s.%n",
                batchStatistics.getDocumentCount(),
                batchStatistics.getErroneousDocumentCount(),
                batchStatistics.getTransactionCount(),
                batchStatistics.getValidDocumentCount());

            for (ExtractKeyPhraseResult extractKeyPhraseResult : extractKeyPhraseResults) {
                for (String keyPhrase : extractKeyPhraseResult.getKeyPhrases()) {
                    System.out.printf("Recognized phrases: %s.%n", keyPhrase);
                }
            }
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.extractKeyPhrasesWithResponse#List-String

        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.extractBatchKeyPhrases#List
        List<TextDocumentInput> textDocumentInputs = Arrays.asList(
            new TextDocumentInput("0", "Hello world. This is some input text that I love."),
            new TextDocumentInput("1", "I work at Microsoft."));

        textAnalyticsAsyncClient.extractBatchKeyPhrases(textDocumentInputs).subscribe(extractKeyPhraseResults -> {
            System.out.printf("Model version: %s%n", extractKeyPhraseResults.getModelVersion());

            // Batch statistics
            final TextDocumentBatchStatistics batchStatistics = extractKeyPhraseResults.getStatistics();
            System.out.printf("Batch statistics, document count: %s, erroneous document count: %s, transaction count:"
                    + " %s, valid document count: %s.%n",
                batchStatistics.getDocumentCount(),
                batchStatistics.getErroneousDocumentCount(),
                batchStatistics.getTransactionCount(),
                batchStatistics.getValidDocumentCount());

            for (ExtractKeyPhraseResult extractKeyPhraseResult : extractKeyPhraseResults) {
                for (String keyPhrase : extractKeyPhraseResult.getKeyPhrases()) {
                    System.out.printf("Recognized phrases: %s.%n", keyPhrase);
                }
            }
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.extractBatchKeyPhrases#List

        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.extractBatchKeyPhrasesWithResponse#List-TextAnalyticsRequestOptions
        List<TextDocumentInput> textDocumentInputs1 = Arrays.asList(
            new TextDocumentInput("0", "I had a wonderful trip to Seattle last week."),
            new TextDocumentInput("1", "I work at Microsoft."));

        // Request options: show statistics and model version
        final TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions().setShowStatistics(true);

        textAnalyticsAsyncClient.extractBatchKeyPhrasesWithResponse(textDocumentInputs1, requestOptions)
            .subscribe(response -> {
                final DocumentResultCollection<ExtractKeyPhraseResult> extractKeyPhraseResults = response.getValue();
                System.out.printf("Model version: %s%n", extractKeyPhraseResults.getModelVersion());

                // Batch statistics
                final TextDocumentBatchStatistics batchStatistics = extractKeyPhraseResults.getStatistics();
                System.out.printf("Batch statistics, document count: %s, erroneous document count: %s, transaction"
                        + " count: %s, valid document count: %s.%n",
                    batchStatistics.getDocumentCount(),
                    batchStatistics.getErroneousDocumentCount(),
                    batchStatistics.getTransactionCount(),
                    batchStatistics.getValidDocumentCount());

                for (ExtractKeyPhraseResult extractKeyPhraseResult : extractKeyPhraseResults) {
                    for (String keyPhrase : extractKeyPhraseResult.getKeyPhrases()) {
                        System.out.printf("Recognized phrases: %s.%n", keyPhrase);
                    }
                }
            });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.extractBatchKeyPhrasesWithResponse#List-TextAnalyticsRequestOptions
    }

    // Sentiment

    /**
     * Code snippet for recognizing entities
     */
    public void analyzeSentimentCodeSnippets() {
        TextAnalyticsAsyncClient textAnalyticsAsyncClient = createTextAnalyticsAsyncClient();

        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeSentiment#string
        String inputText = "The hotel was dark and unclean.";
        textAnalyticsAsyncClient.analyzeSentiment(inputText).subscribe(analyzeSentimentResult -> {
            TextSentiment documentSentiment = analyzeSentimentResult.getDocumentSentiment();
            System.out.printf(
                "Recognized sentiment class: %s.%n",
                documentSentiment.getTextSentimentClass());

            for (TextSentiment textSentiment : analyzeSentimentResult.getSentenceSentiments()) {
                System.out.printf(
                    "Recognized sentence sentiment: %s, positive score: %s, neutral score: %s, negative score: %s.%n",
                    textSentiment.getTextSentimentClass(),
                    textSentiment.getPositiveScore(),
                    textSentiment.getNeutralScore(),
                    textSentiment.getNegativeScore());
            }
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeSentiment#string

        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeSentimentWithResponse#string-string
        String inputText1 = "The hotel was dark and unclean.";
        textAnalyticsAsyncClient.analyzeSentimentWithResponse(inputText1, "en")
            .subscribe(analyzeSentimentResult -> {
                AnalyzeSentimentResult sentimentResult = analyzeSentimentResult.getValue();
                TextSentiment documentSentiment = sentimentResult.getDocumentSentiment();
                System.out.printf(
                    "Recognized sentiment class: %s.%n",
                    documentSentiment.getTextSentimentClass());

                for (TextSentiment textSentiment : sentimentResult.getSentenceSentiments()) {
                    System.out.printf(
                        "Recognized sentence sentiment: %s, positive score: %s, neutral score: %s, "
                            + "negative score: %s.%n",
                        textSentiment.getTextSentimentClass(),
                        textSentiment.getPositiveScore(),
                        textSentiment.getNeutralScore(),
                        textSentiment.getNegativeScore());
                }
            });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeSentimentWithResponse#string-string


        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeSentiment#list-string
        List<String> inputs = Arrays.asList(
            "The hotel was dark and unclean.", "The restaurant had amazing gnocchi.");

        textAnalyticsAsyncClient.analyzeSentiment(inputs).subscribe(analyzeSentimentResults -> {
            System.out.printf("Model version: %s%n", analyzeSentimentResults.getModelVersion());

            // Batch statistics
            final TextDocumentBatchStatistics batchStatistics = analyzeSentimentResults.getStatistics();
            System.out.printf("Batch statistics, document count: %s, erroneous document count: %s,"
                    + " transaction count: %s, valid document count: %s.%n",
                batchStatistics.getDocumentCount(),
                batchStatistics.getTransactionCount(),
                batchStatistics.getValidDocumentCount());

            for (AnalyzeSentimentResult analyzeSentimentResult : analyzeSentimentResults) {
                System.out.printf("Document ID: %s%n", analyzeSentimentResult.getId());
                TextSentiment documentSentiment = analyzeSentimentResult.getDocumentSentiment();
                System.out.printf("Recognized document sentiment: %s.%n",
                    documentSentiment.getTextSentimentClass());
                for (TextSentiment sentenceSentiment : analyzeSentimentResult.getSentenceSentiments()) {
                    System.out.printf("Recognized sentence sentiment: %s, positive score: %s, neutral score: %s, "
                            + "negative score: %s, length of sentence: %s, offset of sentence: %s.%n",
                        sentenceSentiment.getTextSentimentClass(),
                        sentenceSentiment.getPositiveScore(),
                        sentenceSentiment.getNeutralScore(),
                        sentenceSentiment.getNegativeScore(),
                        sentenceSentiment.getLength(),
                        sentenceSentiment.getOffset());
                }
            }
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeSentiment#list-string

        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeSentimentWithResponse#List-String
        List<String> inputs1 = Arrays.asList(
            "The hotel was dark and unclean.", "The restaurant had amazing gnocchi.");

        textAnalyticsAsyncClient.analyzeSentimentWithResponse(inputs1, "en").subscribe(response -> {
            DocumentResultCollection<AnalyzeSentimentResult> analyzeSentimentResults = response.getValue();
            System.out.printf("Model version: %s%n", analyzeSentimentResults.getModelVersion());

            // Batch statistics
            final TextDocumentBatchStatistics batchStatistics = analyzeSentimentResults.getStatistics();
            System.out.printf("Batch statistics, document count: %s, erroneous document count: %s, "
                    + "transaction count: %s, valid document count: %s.%n",
                batchStatistics.getDocumentCount(),
                batchStatistics.getErroneousDocumentCount(),
                batchStatistics.getTransactionCount(),
                batchStatistics.getValidDocumentCount());

            for (AnalyzeSentimentResult analyzeSentimentResult : analyzeSentimentResults) {
                System.out.printf("Document ID: %s%n", analyzeSentimentResult.getId());
                TextSentiment documentSentiment = analyzeSentimentResult.getDocumentSentiment();
                System.out.printf("Recognized document sentiment: %s.%n",
                    documentSentiment.getTextSentimentClass());
                for (TextSentiment sentenceSentiment : analyzeSentimentResult.getSentenceSentiments()) {
                    System.out.printf("Recognized sentence sentiment: %s, positive score: %s, neutral score: %s, "
                            + "negative score: %s, length of sentence: %s, offset of sentence: %s.%n",
                        sentenceSentiment.getTextSentimentClass(),
                        sentenceSentiment.getPositiveScore(),
                        sentenceSentiment.getNeutralScore(),
                        sentenceSentiment.getNegativeScore(),
                        sentenceSentiment.getLength(),
                        sentenceSentiment.getOffset());
                }
            }
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeSentimentWithResponse#List-String

        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeBatchSentiment#List
        List<TextDocumentInput> textDocumentInputs = Arrays.asList(
            new TextDocumentInput("0", "The hotel was dark and unclean."),
            new TextDocumentInput("1", "The restaurant had amazing gnocchi."));

        textAnalyticsAsyncClient.analyzeBatchSentiment(textDocumentInputs).subscribe(analyzeSentimentResults -> {
            System.out.printf("Model version: %s%n", analyzeSentimentResults.getModelVersion());

            // Batch statistics
            final TextDocumentBatchStatistics batchStatistics = analyzeSentimentResults.getStatistics();
            System.out.printf("Batch statistics, document count: %s, erroneous document count: %s, transaction count:"
                    + " %s, valid document count: %s.%n",
                batchStatistics.getDocumentCount(),
                batchStatistics.getErroneousDocumentCount(),
                batchStatistics.getTransactionCount(),
                batchStatistics.getValidDocumentCount());

            for (AnalyzeSentimentResult analyzeSentimentResult : analyzeSentimentResults) {
                System.out.printf("Document ID: %s%n", analyzeSentimentResult.getId());
                TextSentiment documentSentiment = analyzeSentimentResult.getDocumentSentiment();
                System.out.printf("Recognized document sentiment: %s.%n",
                    documentSentiment.getTextSentimentClass());
                for (TextSentiment sentenceSentiment : analyzeSentimentResult.getSentenceSentiments()) {
                    System.out.printf("Recognized sentence sentiment: %s, positive score: %s, neutral score: %s, "
                            + "negative score: %s, length of sentence: %s, offset of sentence: %s.%n",
                        sentenceSentiment.getTextSentimentClass(),
                        sentenceSentiment.getPositiveScore(),
                        sentenceSentiment.getNeutralScore(),
                        sentenceSentiment.getNegativeScore(),
                        sentenceSentiment.getLength(),
                        sentenceSentiment.getOffset());
                }
            }
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeBatchSentiment#List

        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeBatchSentimentWithResponse#List-TextAnalyticsRequestOptions
        List<TextDocumentInput> textDocumentInputs1 = Arrays.asList(
            new TextDocumentInput("0", "The hotel was dark and unclean."),
            new TextDocumentInput("1", "The restaurant had amazing gnocchi."));

        // Request options: show statistics and model version
        final TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions().setShowStatistics(true);

        textAnalyticsAsyncClient.analyzeBatchSentimentWithResponse(textDocumentInputs1, requestOptions)
            .subscribe(response -> {
                DocumentResultCollection<AnalyzeSentimentResult> analyzeSentimentResults = response.getValue();
                System.out.printf("Model version: %s%n", analyzeSentimentResults.getModelVersion());

                // Batch statistics
                TextDocumentBatchStatistics batchStatistics = analyzeSentimentResults.getStatistics();
                System.out.printf("Batch statistics, document count: %s, erroneous document count: %s, transaction"
                        + " count: %s, valid document count: %s.%n",
                    batchStatistics.getDocumentCount(),
                    batchStatistics.getErroneousDocumentCount(),
                    batchStatistics.getTransactionCount(),
                    batchStatistics.getValidDocumentCount());

                for (AnalyzeSentimentResult analyzeSentimentResult : analyzeSentimentResults) {
                    System.out.printf("Document ID: %s%n", analyzeSentimentResult.getId());
                    TextSentiment documentSentiment = analyzeSentimentResult.getDocumentSentiment();
                    System.out.printf("Recognized document sentiment: %s.%n",
                        documentSentiment.getTextSentimentClass());
                    for (TextSentiment sentenceSentiment : analyzeSentimentResult.getSentenceSentiments()) {
                        System.out.printf("Recognized sentence sentiment: %s, positive score: %s, neutral score: %s, "
                                + "negative score: %s, length of sentence: %s, offset of sentence: %s.%n",
                            sentenceSentiment.getTextSentimentClass(),
                            sentenceSentiment.getPositiveScore(),
                            sentenceSentiment.getNeutralScore(),
                            sentenceSentiment.getNegativeScore(),
                            sentenceSentiment.getLength(),
                            sentenceSentiment.getOffset());
                    }
                }
            });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeBatchSentimentWithResponse#List-TextAnalyticsRequestOptions
    }
}
