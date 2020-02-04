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

import java.util.Arrays;
import java.util.List;

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
            .subscriptionKey(new TextAnalyticsApiKeyCredential("{subscription_key}"))
            .endpoint("{endpoint}")
            .buildAsyncClient();
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.instantiation
        return textAnalyticsAsyncClient;
    }

    /**
     * Code snippet for updating the existing subscription key.
     */
    public void rotateSubscriptionKey() {
        // BEGIN: com.azure.ai.textanalytics.models.TextAnalyticsApiKeyCredential
        TextAnalyticsApiKeyCredential credential =
            new TextAnalyticsApiKeyCredential("{subscription_key}");

        TextAnalyticsAsyncClient textAnalyticsAsyncClient = new TextAnalyticsClientBuilder()
            .subscriptionKey(credential)
            .endpoint("{endpoint}")
            .buildAsyncClient();

        credential.updateCredential("{new_subscription_key}");
        // END: com.azure.ai.textanalytics.models.TextAnalyticsApiKeyCredential
    }

    // Languages
    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#detectLanguage(String)}
     */
    public void detectLanguages() {
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
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#detectLanguageWithResponse(String, String)}
     */
    public void detectLanguageWithResponse() {
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
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#detectLanguage(List)}
     */
    public void detectLanguagesStringList() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.detectLanguages#List
        final List<String> textInputs = Arrays.asList(
            "This is written in English", "Este es un document escrito en Español.");
        textAnalyticsAsyncClient.detectLanguage(textInputs).subscribe(detectedBatchResult -> {
            // Batch statistics
            final TextDocumentBatchStatistics batchStatistics = detectedBatchResult.getStatistics();
            System.out.printf("Batch statistics, transaction count: %s, valid document count: %s.%n",
                batchStatistics.getTransactionCount(),
                batchStatistics.getValidDocumentCount());

            for (DetectLanguageResult detectLanguageResult : detectedBatchResult) {
                for (DetectedLanguage detectedLanguage : detectLanguageResult.getDetectedLanguages()) {
                    System.out.printf("Detected language: %s, ISO 6391 name: %s, score: %s.%n",
                        detectedLanguage.getName(),
                        detectedLanguage.getIso6391Name(),
                        detectedLanguage.getScore());
                }
            }
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.detectLanguages#List
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#detectLanguageWithResponse(List, String)}
     */
    public void detectLanguagesWithResponse() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.detectLanguagesWithResponse#List-String
        final List<String> textInputs1 = Arrays.asList(
            "This is written in English", "Este es un document escrito en Español.");
        textAnalyticsAsyncClient.detectLanguageWithResponse(textInputs1, "US").subscribe(response -> {
            DocumentResultCollection<DetectLanguageResult> detectedBatchResult = response.getValue();

            // Batch statistics
            final TextDocumentBatchStatistics batchStatistics = detectedBatchResult.getStatistics();
            System.out.printf("Batch statistics, transaction count: %s, valid document count: %s.%n",
                batchStatistics.getTransactionCount(),
                batchStatistics.getValidDocumentCount());

            for (DetectLanguageResult detectLanguageResult : detectedBatchResult) {
                for (DetectedLanguage detectedLanguage : detectLanguageResult.getDetectedLanguages()) {
                    System.out.printf("Detected language: %s, ISO 6391 name: %s, score: %s.%n",
                        detectedLanguage.getName(),
                        detectedLanguage.getIso6391Name(),
                        detectedLanguage.getScore());
                }
            }
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.detectLanguagesWithResponse#List-String
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#detectBatchLanguage(List)} )}
     */
    public void detectLanguagesBatch() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.detectBatchLanguages#List
        List<DetectLanguageInput> detectLanguageInputs = Arrays.asList(
            new DetectLanguageInput("1", "This is written in English.", "US"),
            new DetectLanguageInput("2", "Este es un document escrito en Español.", "ES")
        );
        textAnalyticsAsyncClient.detectBatchLanguage(detectLanguageInputs).subscribe(detectedBatchResult -> {

            // Batch statistics
            final TextDocumentBatchStatistics batchStatistics = detectedBatchResult.getStatistics();
            System.out.printf("Batch statistics, transaction count: %s, valid document count: %s.%n",
                batchStatistics.getTransactionCount(),
                batchStatistics.getValidDocumentCount());

            for (DetectLanguageResult detectLanguageResult : detectedBatchResult) {
                for (DetectedLanguage detectedLanguage : detectLanguageResult.getDetectedLanguages()) {
                    System.out.printf("Detected language: %s, ISO 6391 name: %s, score: %s.%n",
                        detectedLanguage.getName(),
                        detectedLanguage.getIso6391Name(),
                        detectedLanguage.getScore());
                }
            }
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.detectBatchLanguages#List
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#detectBatchLanguageWithResponse(List,
     * TextAnalyticsRequestOptions)}
     */
    public void detectBatchLanguagesWithResponse() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.detectBatchLanguagesWithResponse#List-TextAnalyticsRequestOptions
        List<DetectLanguageInput> detectLanguageInputs1 = Arrays.asList(
            new DetectLanguageInput("1", "This is written in English.", "US"),
            new DetectLanguageInput("2", "Este es un document escrito en Español.", "es")
        );

        // Request options: show statistics and model version
        final TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions().setShowStatistics(true);

        textAnalyticsAsyncClient.detectBatchLanguageWithResponse(detectLanguageInputs1, requestOptions)
            .subscribe(response -> {
                DocumentResultCollection<DetectLanguageResult> detectedBatchResult = response.getValue();

                // Batch statistics
                TextDocumentBatchStatistics batchStatistics = detectedBatchResult.getStatistics();
                System.out.printf("Batch statistics, transaction count: %s, valid document count: %s.%n",
                    batchStatistics.getTransactionCount(),
                    batchStatistics.getValidDocumentCount());

                for (DetectLanguageResult detectLanguageResult : detectedBatchResult) {
                    for (DetectedLanguage detectedLanguage : detectLanguageResult.getDetectedLanguages()) {
                        System.out.printf("Detected language: %s, ISO 6391 name: %s, score: %s.%n",
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
     * Code snippet for {@link TextAnalyticsAsyncClient#recognizeEntities(String)}
     */
    public void recognizeEntities() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeEntities#string
        String inputText = "Satya Nadella is the CEO of Microsoft";
        textAnalyticsAsyncClient.recognizeEntities(inputText).subscribe(recognizeEntitiesResult -> {
            for (CategorizedEntity entity : recognizeEntitiesResult.getEntities()) {
                System.out.printf(
                    "Recognized Categorized Entity: %s, Category: %s, Score: %s.%n",
                    entity.getText(),
                    entity.getCategory(),
                    entity.getScore());
            }
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeEntities#string
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#recognizeEntitiesWithResponse(String, String)}
     */
    public void recognizeEntitiesWithResponse() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeEntitiesWithResponse#string-string
        String inputText1 = "Satya Nadella is the CEO of Microsoft";
        textAnalyticsAsyncClient.recognizeEntitiesWithResponse(inputText1, "en")
            .subscribe(recognizeEntitiesResult -> {
                for (CategorizedEntity entity : recognizeEntitiesResult.getValue().getEntities()) {
                    System.out.printf(
                        "Recognized Categorized Entity: %s, Category: %s, Score: %s.%n",
                        entity.getText(),
                        entity.getCategory(),
                        entity.getScore());
                }
            });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeEntitiesWithResponse#string-string
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#recognizeEntities(List)}
     */
    public void recognizeEntitiesStringList() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeEntities#List
        List<String> textInputs = Arrays.asList(
            "I had a wonderful trip to Seattle last week.", "I work at Microsoft.");

        textAnalyticsAsyncClient.recognizeEntities(textInputs).subscribe(recognizeEntitiesResults -> {
            // Batch statistics
            TextDocumentBatchStatistics batchStatistics = recognizeEntitiesResults.getStatistics();
            System.out.printf("Batch statistics, transaction count: %s, valid document count: %s.%n",
                batchStatistics.getTransactionCount(),
                batchStatistics.getValidDocumentCount());

            for (RecognizeEntitiesResult recognizeEntitiesResult : recognizeEntitiesResults) {
                for (CategorizedEntity entity : recognizeEntitiesResult.getEntities()) {
                    System.out.printf(
                        "Recognized Categorized Entity: %s, Category: %s, Score: %s.%n",
                        entity.getText(),
                        entity.getCategory(),
                        entity.getScore());
                }
            }
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeEntities#List
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#recognizeEntitiesWithResponse(List, String)}
     */
    public void recognizeEntitiesWithResponseStringList() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeEntitiesWithResponse#List-String
        List<String> textInputs1 = Arrays.asList(
            "I had a wonderful trip to Seattle last week.", "I work at Microsoft.");

        textAnalyticsAsyncClient.recognizeEntitiesWithResponse(textInputs1, "en").subscribe(response -> {
            DocumentResultCollection<RecognizeEntitiesResult> recognizeEntitiesResults = response.getValue();

            // Batch statistics
            TextDocumentBatchStatistics batchStatistics = recognizeEntitiesResults.getStatistics();
            System.out.printf("Batch statistics, transaction count: %s, valid document count: %s.%n",
                batchStatistics.getTransactionCount(),
                batchStatistics.getValidDocumentCount());

            for (RecognizeEntitiesResult recognizeEntitiesResult : recognizeEntitiesResults) {
                for (CategorizedEntity entity : recognizeEntitiesResult.getEntities()) {
                    System.out.printf(
                        "Recognized Categorized Entity: %s, Category: %s, Score: %s.%n",
                        entity.getText(),
                        entity.getCategory(),
                        entity.getScore());
                }
            }
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeEntitiesWithResponse#List-String
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#recognizeBatchEntities(List)}
     */
    public void recognizeBatchEntities() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeBatchEntities#List
        List<TextDocumentInput> textDocumentInputs = Arrays.asList(
            new TextDocumentInput("0", "I had a wonderful trip to Seattle last week."),
            new TextDocumentInput("1", "I work at Microsoft."));

        textAnalyticsAsyncClient.recognizeBatchEntities(textDocumentInputs).subscribe(recognizeEntitiesResults -> {

            // Batch statistics
            TextDocumentBatchStatistics batchStatistics = recognizeEntitiesResults.getStatistics();
            System.out.printf("Batch statistics, transaction count: %s, valid document count: %s.%n",
                batchStatistics.getTransactionCount(),
                batchStatistics.getValidDocumentCount());

            for (RecognizeEntitiesResult recognizeEntitiesResult : recognizeEntitiesResults) {
                for (CategorizedEntity entity : recognizeEntitiesResult.getEntities()) {
                    System.out.printf(
                        "Recognized Categorized Entity: %s, Category: %s, Score: %s.%n",
                        entity.getText(),
                        entity.getCategory(),
                        entity.getScore());
                }
            }
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeBatchEntities#List
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#recognizeBatchEntitiesWithResponse(List,
     * TextAnalyticsRequestOptions)}
     */
    public void recognizeBatchEntitiesWithResponse() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeBatchEntitiesWithResponse#List-TextAnalyticsRequestOptions
        List<TextDocumentInput> textDocumentInputs1 = Arrays.asList(
            new TextDocumentInput("0", "I had a wonderful trip to Seattle last week."),
            new TextDocumentInput("1", "I work at Microsoft."));

        // Request options: show statistics and model version
        final TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions().setShowStatistics(true);

        textAnalyticsAsyncClient.recognizeBatchEntitiesWithResponse(textDocumentInputs1, requestOptions)
            .subscribe(response -> {
                DocumentResultCollection<RecognizeEntitiesResult> recognizeEntitiesResults = response.getValue();

                // Batch statistics
                TextDocumentBatchStatistics batchStatistics = recognizeEntitiesResults.getStatistics();
                System.out.printf("Batch statistics, transaction count: %s, valid document count: %s.%n",
                    batchStatistics.getTransactionCount(),
                    batchStatistics.getValidDocumentCount());

                for (RecognizeEntitiesResult recognizeEntitiesResult : recognizeEntitiesResults) {
                    for (CategorizedEntity entity : recognizeEntitiesResult.getEntities()) {
                        System.out.printf(
                            "Recognized Categorized Entity: %s, Category: %s, Score: %s.%n",
                            entity.getText(),
                            entity.getCategory(),
                            entity.getScore());
                    }
                }
            });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeBatchEntitiesWithResponse#List-TextAnalyticsRequestOptions
    }

    // Pii Entity
    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#recognizePiiEntities(String)}
     */
    public void recognizePiiEntities() {

        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizePiiEntities#string
        String inputText = "My SSN is 555-55-5555";
        textAnalyticsAsyncClient.recognizePiiEntities(inputText).subscribe(recognizeEntitiesResult -> {
            for (PiiEntity entity : recognizeEntitiesResult.getEntities()) {
                System.out.printf(
                    "Recognized Categorized Entity: %s, Category: %s, Score: %s.%n",
                    entity.getText(),
                    entity.getCategory(),
                    entity.getScore());
            }
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizePiiEntities#string
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#recognizePiiEntitiesWithResponse(String, String)}
     */
    public void recognizePiiEntitiesWithResponse() {

        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizePiiEntitiesWithResponse#string-string
        String inputText1 = "My SSN is 555-55-5555";
        textAnalyticsAsyncClient.recognizePiiEntitiesWithResponse(inputText1, "en")
            .subscribe(recognizeEntitiesResult -> {
                for (PiiEntity entity : recognizeEntitiesResult.getValue().getEntities()) {
                    System.out.printf(
                        "Recognized PII Entity: %s, Category: %s, Score: %s.%n",
                        entity.getText(),
                        entity.getCategory(),
                        entity.getScore());
                }
            });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizePiiEntitiesWithResponse#string-string
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#recognizePiiEntities(List)}
     */
    public void recognizePiiEntitiesStringList() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizePiiEntities#list-string
        List<String> textInputs = Arrays.asList(
            "My SSN is 555-55-5555.", "Visa card 0111 1111 1111 1111.");

        textAnalyticsAsyncClient.recognizePiiEntities(textInputs).subscribe(recognizeEntitiesResults -> {

            // Batch statistics
            TextDocumentBatchStatistics batchStatistics = recognizeEntitiesResults.getStatistics();
            System.out.printf("Batch statistics, transaction count: %s, valid document count: %s.%n",
                batchStatistics.getTransactionCount(),
                batchStatistics.getValidDocumentCount());

            for (RecognizePiiEntitiesResult recognizeEntitiesResult : recognizeEntitiesResults) {
                for (PiiEntity entity : recognizeEntitiesResult.getEntities()) {
                    System.out.printf(
                        "Recognized PII Entity: %s, Category: %s, Score: %s.%n",
                        entity.getText(),
                        entity.getCategory(),
                        entity.getScore());
                }
            }
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizePiiEntities#list-string
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#recognizePiiEntitiesWithResponse(List, String)}
     */
    public void recognizePiiEntitiesWithResponseStringList() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizePiiEntitiesWithResponse#List-String
        List<String> textInputs1 = Arrays.asList(
            "My SSN is 555-55-5555.", "Visa card 0111 1111 1111 1111.");

        textAnalyticsAsyncClient.recognizePiiEntitiesWithResponse(textInputs1, "en").subscribe(response -> {
            DocumentResultCollection<RecognizePiiEntitiesResult> recognizeEntitiesResults = response.getValue();

            // Batch statistics
            TextDocumentBatchStatistics batchStatistics = recognizeEntitiesResults.getStatistics();
            System.out.printf("Batch statistics, transaction count: %s, valid document count: %s.%n",
                batchStatistics.getTransactionCount(),
                batchStatistics.getValidDocumentCount());

            for (RecognizePiiEntitiesResult recognizeEntitiesResult : recognizeEntitiesResults) {
                for (PiiEntity entity : recognizeEntitiesResult.getEntities()) {
                    System.out.printf(
                        "Recognized PII Entity: %s, Category: %s, Score: %s.%n",
                        entity.getText(),
                        entity.getCategory(),
                        entity.getScore());
                }
            }
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizePiiEntitiesWithResponse#List-String
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#recognizeBatchPiiEntities(List)}
     */
    public void recognizeBatchPiiEntities() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeBatchPiiEntities#List
        List<TextDocumentInput> textDocumentInputs = Arrays.asList(
            new TextDocumentInput("0", "My SSN is 555-55-5555."),
            new TextDocumentInput("1", "Visa card 0111 1111 1111 1111."));

        textAnalyticsAsyncClient.recognizeBatchPiiEntities(textDocumentInputs).subscribe(recognizeEntitiesResults -> {

            // Batch statistics
            TextDocumentBatchStatistics batchStatistics = recognizeEntitiesResults.getStatistics();
            System.out.printf("Batch statistics, transaction count: %s, valid document count: %s.%n",
                batchStatistics.getTransactionCount(),
                batchStatistics.getValidDocumentCount());

            for (RecognizePiiEntitiesResult recognizeEntitiesResult : recognizeEntitiesResults) {
                for (PiiEntity entity : recognizeEntitiesResult.getEntities()) {
                    System.out.printf(
                        "Recognized PII Entity: %s, Category: %s, Score: %s.%n",
                        entity.getText(),
                        entity.getCategory(),
                        entity.getScore());
                }
            }
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeBatchPiiEntities#List
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#recognizeBatchPiiEntitiesWithResponse(List,
     * TextAnalyticsRequestOptions)}
     */
    public void recognizeBatchPiiEntitiesWithResponse() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeBatchPiiEntitiesWithResponse#List-TextAnalyticsRequestOptions
        List<TextDocumentInput> textDocumentInputs1 = Arrays.asList(
            new TextDocumentInput("0", "My SSN is 555-55-5555."),
            new TextDocumentInput("1", "Visa card 0111 1111 1111 1111."));

        // Request options: show statistics and model version
        final TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions().setShowStatistics(true);

        textAnalyticsAsyncClient.recognizeBatchPiiEntitiesWithResponse(textDocumentInputs1, requestOptions)
            .subscribe(response -> {
                DocumentResultCollection<RecognizePiiEntitiesResult> recognizeEntitiesResults = response.getValue();

                // Batch statistics
                TextDocumentBatchStatistics batchStatistics = recognizeEntitiesResults.getStatistics();
                System.out.printf("Batch statistics, transaction count: %s, valid document count: %s.%n",
                    batchStatistics.getTransactionCount(),
                    batchStatistics.getValidDocumentCount());

                for (RecognizePiiEntitiesResult recognizeEntitiesResult : recognizeEntitiesResults) {
                    for (PiiEntity entity : recognizeEntitiesResult.getEntities()) {
                        System.out.printf(
                            "Recognized PII Entity: %s, Category: %s, Score: %s.%n",
                            entity.getText(),
                            entity.getCategory(),
                            entity.getScore());
                    }
                }
            });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeBatchPiiEntitiesWithResponse#List-TextAnalyticsRequestOptions
    }

    // Linked Entity
    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#recognizeLinkedEntities(String)}
     */
    public void recognizeLinkedEntities() {

        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeLinkedEntities#string
        String inputText = "Old Faithful is a geyser at Yellowstone Park.";
        textAnalyticsAsyncClient.recognizeLinkedEntities(inputText).subscribe(recognizeEntitiesResult -> {
            for (LinkedEntity linkedEntity : recognizeEntitiesResult.getLinkedEntities()) {
                System.out.printf("Recognized Linked CategorizedEntity: %s, URL: %s, Data Source: %s.%n",
                    linkedEntity.getName(),
                    linkedEntity.getUrl(),
                    linkedEntity.getDataSource());
            }
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeLinkedEntities#string
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#recognizeLinkedEntitiesWithResponse(String, String)}
     */
    public void recognizeLinkedEntitiesWithResponse() {

        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeLinkedEntitiesWithResponse#string-string
        String inputText1 = "Old Faithful is a geyser at Yellowstone Park.";
        textAnalyticsAsyncClient.recognizeLinkedEntitiesWithResponse(inputText1, "en")
            .subscribe(linkedEntitiesResultResponse -> {
                for (LinkedEntity linkedEntity : linkedEntitiesResultResponse.getValue().getLinkedEntities()) {
                    System.out.printf("Recognized Linked CategorizedEntity: %s, URL: %s, Data Source: %s.%n",
                        linkedEntity.getName(),
                        linkedEntity.getUrl(),
                        linkedEntity.getDataSource());
                }
            });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeLinkedEntitiesWithResponse#string-string
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#recognizeLinkedEntities(List)}
     */
    public void recognizeLinkedEntitiesStringList() {

        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeLinkedEntities#list-string
        List<String> textInputs = Arrays.asList(
            "Old Faithful is a geyser at Yellowstone Park.", "Mount Shasta has lenticular clouds.");

        textAnalyticsAsyncClient.recognizeLinkedEntities(textInputs).subscribe(recognizeLinkedEntitiesResults -> {

            // Batch statistics
            TextDocumentBatchStatistics batchStatistics = recognizeLinkedEntitiesResults.getStatistics();
            System.out.printf("Batch statistics, transaction count: %s, valid document count: %s.%n",
                batchStatistics.getTransactionCount(),
                batchStatistics.getValidDocumentCount());

            for (RecognizeLinkedEntitiesResult recognizeLinkedEntitiesResult : recognizeLinkedEntitiesResults) {
                for (LinkedEntity linkedEntity : recognizeLinkedEntitiesResult.getLinkedEntities()) {
                    System.out.printf("Recognized Linked CategorizedEntity: %s, URL: %s, Data Source: %s.%n",
                        linkedEntity.getName(),
                        linkedEntity.getUrl(),
                        linkedEntity.getDataSource());
                }
            }
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeLinkedEntities#list-string
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#recognizeLinkedEntitiesWithResponse(List, String)}
     */
    public void recognizeLinkedEntitiesWithResponseStringList() {

        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeLinkedEntitiesWithResponse#List-String
        List<String> textInputs1 = Arrays.asList(
            "Old Faithful is a geyser at Yellowstone Park.", "Mount Shasta has lenticular clouds.");

        textAnalyticsAsyncClient.recognizeLinkedEntitiesWithResponse(textInputs1, "en").subscribe(response -> {
            DocumentResultCollection<RecognizeLinkedEntitiesResult> recognizeLinkedEntitiesResults = response.getValue();

            // Batch statistics
            TextDocumentBatchStatistics batchStatistics = recognizeLinkedEntitiesResults.getStatistics();
            System.out.printf("Batch statistics, transaction count: %s, valid document count: %s.%n",
                batchStatistics.getTransactionCount(),
                batchStatistics.getValidDocumentCount());

            for (RecognizeLinkedEntitiesResult recognizeLinkedEntitiesResult : recognizeLinkedEntitiesResults) {
                for (LinkedEntity linkedEntity : recognizeLinkedEntitiesResult.getLinkedEntities()) {
                    System.out.printf("Recognized Linked CategorizedEntity: %s, URL: %s, Data Source: %s.%n",
                        linkedEntity.getName(),
                        linkedEntity.getUrl(),
                        linkedEntity.getDataSource());
                }
            }
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeLinkedEntitiesWithResponse#List-String

    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#recognizeBatchLinkedEntities(List)}
     */
    public void recognizeBatchLinkedEntities() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeBatchLinkedEntities#List
        List<TextDocumentInput> textDocumentInputs = Arrays.asList(
            new TextDocumentInput("0", "Old Faithful is a geyser at Yellowstone Park."),
            new TextDocumentInput("1", "Mount Shasta has lenticular clouds."));

        textAnalyticsAsyncClient.recognizeBatchLinkedEntities(textDocumentInputs)
            .subscribe(recognizeLinkedEntitiesResults -> {

                // Batch statistics
                TextDocumentBatchStatistics batchStatistics = recognizeLinkedEntitiesResults.getStatistics();
                System.out.printf("Batch statistics, transaction count: %s, valid document count: %s.%n",
                    batchStatistics.getTransactionCount(),
                    batchStatistics.getValidDocumentCount());

                for (RecognizeLinkedEntitiesResult recognizeLinkedEntitiesResult : recognizeLinkedEntitiesResults) {
                    for (LinkedEntity linkedEntity : recognizeLinkedEntitiesResult.getLinkedEntities()) {
                        System.out.printf("Recognized Linked CategorizedEntity: %s, URL: %s, Data Source: %s.%n",
                            linkedEntity.getName(),
                            linkedEntity.getUrl(),
                            linkedEntity.getDataSource());
                    }
                }
            });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeBatchLinkedEntities#List
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#recognizeBatchLinkedEntitiesWithResponse(List,
     * TextAnalyticsRequestOptions)}
     */
    public void recognizeBatchLinkedEntitiesWithResponse() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeBatchLinkedEntitiesWithResponse#List-TextAnalyticsRequestOptions
        List<TextDocumentInput> textDocumentInputs1 = Arrays.asList(
            new TextDocumentInput("0", "Old Faithful is a geyser at Yellowstone Park."),
            new TextDocumentInput("1", "Mount Shasta has lenticular clouds."));

        // Request options: show statistics and model version
        final TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions().setShowStatistics(true);

        textAnalyticsAsyncClient.recognizeBatchLinkedEntitiesWithResponse(textDocumentInputs1, requestOptions)
            .subscribe(response -> {
                DocumentResultCollection<RecognizeLinkedEntitiesResult> recognizeLinkedEntitiesResults =
                    response.getValue();

                // Batch statistics
                TextDocumentBatchStatistics batchStatistics = recognizeLinkedEntitiesResults.getStatistics();
                System.out.printf("Batch statistics, transaction count: %s, valid document count: %s.%n",
                    batchStatistics.getTransactionCount(),
                    batchStatistics.getValidDocumentCount());

                for (RecognizeLinkedEntitiesResult recognizeLinkedEntitiesResult : recognizeLinkedEntitiesResults) {
                    for (LinkedEntity linkedEntity : recognizeLinkedEntitiesResult.getLinkedEntities()) {
                        System.out.printf("Recognized Linked CategorizedEntity: %s, URL: %s, Data Source: %s.%n",
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
     * Code snippet for {@link TextAnalyticsAsyncClient#extractKeyPhrases(String)}
     */
    public void extractKeyPhrases() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.extractKeyPhrases#string
        String inputText = "Bonjour tout le monde";
        textAnalyticsAsyncClient.extractKeyPhrases(inputText).subscribe(extractKeyPhraseResult -> {
            for (String keyPhrase : extractKeyPhraseResult.getKeyPhrases()) {
                System.out.printf("Recognized phrases: %s.%n", keyPhrase);
            }
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.extractKeyPhrases#string
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#extractKeyPhrasesWithResponse(String, String)}
     */
    public void extractKeyPhrasesWithResponse() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.extractKeyPhrasesWithResponse#string-string
        String inputText1 = "Bonjour tout le monde";
        textAnalyticsAsyncClient.extractKeyPhrasesWithResponse(inputText1, "fr")
            .subscribe(keyPhraseResultResponse -> {
                for (String keyPhrase : keyPhraseResultResponse.getValue().getKeyPhrases()) {
                    System.out.printf("Recognized phrases: %s.%n", keyPhrase);
                }
            });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.extractKeyPhrasesWithResponse#string-string
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#extractKeyPhrases(List)}
     */
    public void extractKeyPhrasesStringList() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.extractKeyPhrases#list-string
        List<String> textInputs = Arrays.asList(
            "Hello world. This is some input text that I love.",
            "Bonjour tout le monde");

        textAnalyticsAsyncClient.extractKeyPhrases(textInputs).subscribe(extractKeyPhraseResults -> {

            // Batch statistics
            TextDocumentBatchStatistics batchStatistics = extractKeyPhraseResults.getStatistics();
            System.out.printf("Batch statistics, transaction count: %s, valid document count: %s.%n",
                batchStatistics.getTransactionCount(),
                batchStatistics.getValidDocumentCount());

            for (ExtractKeyPhraseResult extractKeyPhraseResult : extractKeyPhraseResults) {
                for (String keyPhrase : extractKeyPhraseResult.getKeyPhrases()) {
                    System.out.printf("Recognized phrases: %s.%n", keyPhrase);
                }
            }
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.extractKeyPhrases#list-string
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#extractKeyPhrasesWithResponse(List, String)}
     */
    public void extractKeyPhrasesWithResponseStringList() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.extractKeyPhrasesWithResponse#List-String
        List<String> textInputs1 = Arrays.asList(
            "Hello world. This is some input text that I love.",
            "Bonjour tout le monde");

        textAnalyticsAsyncClient.extractKeyPhrasesWithResponse(textInputs1, "en").subscribe(response -> {
            DocumentResultCollection<ExtractKeyPhraseResult> extractKeyPhraseResults = response.getValue();

            // Batch statistics
            TextDocumentBatchStatistics batchStatistics = extractKeyPhraseResults.getStatistics();
            System.out.printf("Batch statistics, transaction count: %s, valid document count: %s.%n",
                batchStatistics.getTransactionCount(),
                batchStatistics.getValidDocumentCount());

            for (ExtractKeyPhraseResult extractKeyPhraseResult : extractKeyPhraseResults) {
                for (String keyPhrase : extractKeyPhraseResult.getKeyPhrases()) {
                    System.out.printf("Recognized phrases: %s.%n", keyPhrase);
                }
            }
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.extractKeyPhrasesWithResponse#List-String
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#extractBatchKeyPhrases(List)}
     */
    public void extractBatchKeyPhrases() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.extractBatchKeyPhrases#List
        List<TextDocumentInput> textDocumentInputs = Arrays.asList(
            new TextDocumentInput("0", "Hello world. This is some input text that I love."),
            new TextDocumentInput("1", "I work at Microsoft."));

        textAnalyticsAsyncClient.extractBatchKeyPhrases(textDocumentInputs).subscribe(extractKeyPhraseResults -> {

            // Batch statistics
            TextDocumentBatchStatistics batchStatistics = extractKeyPhraseResults.getStatistics();
            System.out.printf("Batch statistics, transaction count: %s, valid document count: %s.%n",
                batchStatistics.getTransactionCount(),
                batchStatistics.getValidDocumentCount());

            for (ExtractKeyPhraseResult extractKeyPhraseResult : extractKeyPhraseResults) {
                for (String keyPhrase : extractKeyPhraseResult.getKeyPhrases()) {
                    System.out.printf("Recognized phrases: %s.%n", keyPhrase);
                }
            }
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.extractBatchKeyPhrases#List
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#extractBatchKeyPhrasesWithResponse(List,
     * TextAnalyticsRequestOptions)}
     */
    public void extractBatchKeyPhrasesWithResponse() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.extractBatchKeyPhrasesWithResponse#List-TextAnalyticsRequestOptions
        List<TextDocumentInput> textDocumentInputs1 = Arrays.asList(
            new TextDocumentInput("0", "I had a wonderful trip to Seattle last week."),
            new TextDocumentInput("1", "I work at Microsoft."));

        // Request options: show statistics and model version
        final TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions().setShowStatistics(true);

        textAnalyticsAsyncClient.extractBatchKeyPhrasesWithResponse(textDocumentInputs1, requestOptions)
            .subscribe(response -> {
                final DocumentResultCollection<ExtractKeyPhraseResult> extractKeyPhraseResults = response.getValue();

                // Batch statistics
                TextDocumentBatchStatistics batchStatistics = extractKeyPhraseResults.getStatistics();
                System.out.printf("Batch statistics, transaction count: %s, valid document count: %s.%n",
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
     * Code snippet for {@link TextAnalyticsAsyncClient#analyzeSentiment(String)}
     */
    public void analyzeSentiment() {
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
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#analyzeSentimentWithResponse(String, String)}
     */
    public void analyzeSentimentWithResponse() {
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
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#analyzeSentiment(List)}
     */
    public void analyzeSentimentStringList() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeSentiment#list-string
        List<String> textInputs = Arrays.asList(
            "The hotel was dark and unclean.", "The restaurant had amazing gnocchi.");

        textAnalyticsAsyncClient.analyzeSentiment(textInputs).subscribe(analyzeSentimentResults -> {

            // Batch statistics
            TextDocumentBatchStatistics batchStatistics = analyzeSentimentResults.getStatistics();
            System.out.printf("Batch statistics, transaction count: %s, valid document count: %s.%n",
                batchStatistics.getTransactionCount(),
                batchStatistics.getValidDocumentCount());

            for (AnalyzeSentimentResult analyzeSentimentResult : analyzeSentimentResults) {
                System.out.printf("Document ID: %s%n", analyzeSentimentResult.getId());
                TextSentiment documentSentiment = analyzeSentimentResult.getDocumentSentiment();
                System.out.printf("Recognized document sentiment: %s.%n",
                    documentSentiment.getTextSentimentClass());
                for (TextSentiment sentenceSentiment : analyzeSentimentResult.getSentenceSentiments()) {
                    System.out.printf("Recognized sentence sentiment: %s, positive score: %s, neutral score: %s, "
                            + "negative score: %s.%n",
                        sentenceSentiment.getTextSentimentClass(),
                        sentenceSentiment.getPositiveScore(),
                        sentenceSentiment.getNeutralScore(),
                        sentenceSentiment.getNegativeScore());
                }
            }
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeSentiment#list-string
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#analyzeSentimentWithResponse(List, String)}
     */
    public void analyzeSentimentWithResponseStringList() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeSentimentWithResponse#List-String
        List<String> textInputs1 = Arrays.asList(
            "The hotel was dark and unclean.", "The restaurant had amazing gnocchi.");

        textAnalyticsAsyncClient.analyzeSentimentWithResponse(textInputs1, "en").subscribe(response -> {
            DocumentResultCollection<AnalyzeSentimentResult> analyzeSentimentResults = response.getValue();

            // Batch statistics
            TextDocumentBatchStatistics batchStatistics = analyzeSentimentResults.getStatistics();
            System.out.printf("Batch statistics, transaction count: %s, valid document count: %s.%n",
                batchStatistics.getTransactionCount(),
                batchStatistics.getValidDocumentCount());

            for (AnalyzeSentimentResult analyzeSentimentResult : analyzeSentimentResults) {
                System.out.printf("Document ID: %s%n", analyzeSentimentResult.getId());
                TextSentiment documentSentiment = analyzeSentimentResult.getDocumentSentiment();
                System.out.printf("Recognized document sentiment: %s.%n",
                    documentSentiment.getTextSentimentClass());
                for (TextSentiment sentenceSentiment : analyzeSentimentResult.getSentenceSentiments()) {
                    System.out.printf("Recognized sentence sentiment: %s, positive score: %s, neutral score: %s, "
                            + "negative score: %s.%n",
                        sentenceSentiment.getTextSentimentClass(),
                        sentenceSentiment.getPositiveScore(),
                        sentenceSentiment.getNeutralScore(),
                        sentenceSentiment.getNegativeScore());
                }
            }
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeSentimentWithResponse#List-String
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#analyzeBatchSentiment(List)}
     */
    public void analyzeBatchSentiment() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeBatchSentiment#List
        List<TextDocumentInput> textDocumentInputs = Arrays.asList(
            new TextDocumentInput("0", "The hotel was dark and unclean."),
            new TextDocumentInput("1", "The restaurant had amazing gnocchi."));

        textAnalyticsAsyncClient.analyzeBatchSentiment(textDocumentInputs).subscribe(analyzeSentimentResults -> {

            // Batch statistics
            TextDocumentBatchStatistics batchStatistics = analyzeSentimentResults.getStatistics();
            System.out.printf("Batch statistics, transaction count: %s, valid document count: %s.%n",
                batchStatistics.getTransactionCount(),
                batchStatistics.getValidDocumentCount());

            for (AnalyzeSentimentResult analyzeSentimentResult : analyzeSentimentResults) {
                System.out.printf("Document ID: %s%n", analyzeSentimentResult.getId());
                TextSentiment documentSentiment = analyzeSentimentResult.getDocumentSentiment();
                System.out.printf("Recognized document sentiment: %s.%n",
                    documentSentiment.getTextSentimentClass());
                for (TextSentiment sentenceSentiment : analyzeSentimentResult.getSentenceSentiments()) {
                    System.out.printf("Recognized sentence sentiment: %s, positive score: %s, neutral score: %s, "
                            + "negative score: %s.%n",
                        sentenceSentiment.getTextSentimentClass(),
                        sentenceSentiment.getPositiveScore(),
                        sentenceSentiment.getNeutralScore(),
                        sentenceSentiment.getNegativeScore());
                }
            }
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeBatchSentiment#List
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#analyzeBatchSentimentWithResponse(List,
     * TextAnalyticsRequestOptions)}
     */
    public void analyzeBatchSentimentWithResponse() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeBatchSentimentWithResponse#List-TextAnalyticsRequestOptions
        List<TextDocumentInput> textDocumentInputs1 = Arrays.asList(
            new TextDocumentInput("0", "The hotel was dark and unclean."),
            new TextDocumentInput("1", "The restaurant had amazing gnocchi."));

        // Request options: show statistics and model version
        final TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions().setShowStatistics(true);

        textAnalyticsAsyncClient.analyzeBatchSentimentWithResponse(textDocumentInputs1, requestOptions)
            .subscribe(response -> {
                DocumentResultCollection<AnalyzeSentimentResult> analyzeSentimentResults = response.getValue();

                // Batch statistics
                TextDocumentBatchStatistics batchStatistics = analyzeSentimentResults.getStatistics();
                System.out.printf("Batch statistics, transaction count: %s, valid document count: %s.%n",
                    batchStatistics.getTransactionCount(),
                    batchStatistics.getValidDocumentCount());

                for (AnalyzeSentimentResult analyzeSentimentResult : analyzeSentimentResults) {
                    System.out.printf("Document ID: %s%n", analyzeSentimentResult.getId());
                    TextSentiment documentSentiment = analyzeSentimentResult.getDocumentSentiment();
                    System.out.printf("Recognized document sentiment: %s.%n",
                        documentSentiment.getTextSentimentClass());
                    for (TextSentiment sentenceSentiment : analyzeSentimentResult.getSentenceSentiments()) {
                        System.out.printf("Recognized sentence sentiment: %s, positive score: %s, neutral score: %s, "
                                + "negative score: %s.%n",
                            sentenceSentiment.getTextSentimentClass(),
                            sentenceSentiment.getPositiveScore(),
                            sentenceSentiment.getNeutralScore(),
                            sentenceSentiment.getNegativeScore());
                    }
                }
            });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeBatchSentimentWithResponse#List-TextAnalyticsRequestOptions
    }
}
