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
            .apiKey(new TextAnalyticsApiKeyCredential("{api_key}"))
            .endpoint("{endpoint}")
            .buildAsyncClient();
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.instantiation
        return textAnalyticsAsyncClient;
    }

    /**
     * Code snippet for updating the existing API key.
     */
    public void rotateApiKey() {
        // BEGIN: com.azure.ai.textanalytics.models.TextAnalyticsApiKeyCredential
        TextAnalyticsApiKeyCredential credential =
            new TextAnalyticsApiKeyCredential("{api_key}");

        TextAnalyticsAsyncClient textAnalyticsAsyncClient = new TextAnalyticsClientBuilder()
            .apiKey(credential)
            .endpoint("{endpoint}")
            .buildAsyncClient();

        credential.updateCredential("{new_api_key}");
        // END: com.azure.ai.textanalytics.models.TextAnalyticsApiKeyCredential
    }

    // Languages

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#detectLanguage(String)}
     */
    public void detectLanguage() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.detectLanguage#string
        String inputText = "Bonjour tout le monde";
        textAnalyticsAsyncClient.detectLanguage(inputText).subscribe(detectedLanguage ->
            System.out.printf("Detected language name: %s, ISO 6391 Name: %s, score: %.2f.%n",
                detectedLanguage.getName(),
                detectedLanguage.getIso6391Name(),
                detectedLanguage.getScore()));
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.detectLanguage#string
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#detectLanguageWithResponse(String, String)}
     */
    public void detectLanguageWithResponse() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.detectLanguageWithResponse#string-string
        String input = "This text is in English";
        String countryHint = "US";
        textAnalyticsAsyncClient.detectLanguageWithResponse(input, countryHint).subscribe(detectedLanguageResponse -> {
            final DetectedLanguage detectedLanguage = detectedLanguageResponse.getValue();
            System.out.printf("Detected language name: %s, ISO 6391 Name: %s, score: %.2f.%n",
                detectedLanguage.getName(),
                detectedLanguage.getIso6391Name(),
                detectedLanguage.getScore());
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.detectLanguageWithResponse#string-string
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#detectLanguageBatch(List)}
     */
    public void detectLanguageStringList() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.detectLanguageBatch#List
        final List<String> textInputs = Arrays.asList(
            "This is written in English", "Este es un document escrito en Español.");
        textAnalyticsAsyncClient.detectLanguageBatch(textInputs).subscribe(detectedBatchResult -> {
            // Batch statistics
            final TextDocumentBatchStatistics batchStatistics = detectedBatchResult.getStatistics();
            System.out.printf("Batch statistics, transaction count: %s, valid document count: %s.%n",
                batchStatistics.getTransactionCount(),
                batchStatistics.getValidDocumentCount());

            for (DetectLanguageResult detectLanguageResult : detectedBatchResult) {
                DetectedLanguage detectedLanguage = detectLanguageResult.getPrimaryLanguage();
                System.out.printf("Detected language name: %s, ISO 6391 Name: %s, score: %.2f.%n",
                    detectedLanguage.getName(),
                    detectedLanguage.getIso6391Name(),
                    detectedLanguage.getScore());
            }
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.detectLanguageBatch#List
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#detectLanguageBatchWithResponse(List, String,
     * TextAnalyticsRequestOptions)}
     */
    public void detectLanguageStringListWithResponse() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.detectLanguageBatchWithResponse#List-String-TextAnalyticsRequestOptions
        List<String> textInputs1 = Arrays.asList(
            "This is written in English",
            "Este es un document escrito en Español."
        );
        textAnalyticsAsyncClient.detectLanguageBatchWithResponse(textInputs1, "US", null).subscribe(response -> {
            DocumentResultCollection<DetectLanguageResult> detectedBatchResult = response.getValue();

            // Batch statistics
            TextDocumentBatchStatistics batchStatistics = detectedBatchResult.getStatistics();
            System.out.printf("Batch statistics, transaction count: %s, valid document count: %s.%n",
                batchStatistics.getTransactionCount(),
                batchStatistics.getValidDocumentCount());

            for (DetectLanguageResult detectLanguageResult : detectedBatchResult) {
                DetectedLanguage detectedLanguage = detectLanguageResult.getPrimaryLanguage();
                System.out.printf("Detected language name: %s, ISO 6391 Name: %s, score: %.2f.%n",
                    detectedLanguage.getName(),
                    detectedLanguage.getIso6391Name(),
                    detectedLanguage.getScore());
            }
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.detectLanguageBatchWithResponse#List-String-TextAnalyticsRequestOptions
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#detectLanguageBatchWithResponse(List,
     * TextAnalyticsRequestOptions)}
     */
    public void detectBatchLanguagesWithResponse() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.detectLanguageBatchWithResponse#List-TextAnalyticsRequestOptions
        List<DetectLanguageInput> detectLanguageInputs1 = Arrays.asList(
            new DetectLanguageInput("1", "This is written in English.", "US"),
            new DetectLanguageInput("2", "Este es un document escrito en Español.", "es")
        );

        // Request options: show statistics and model version
        TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions().setShowStatistics(true);

        textAnalyticsAsyncClient.detectLanguageBatchWithResponse(detectLanguageInputs1, requestOptions)
            .subscribe(response -> {
                DocumentResultCollection<DetectLanguageResult> detectedBatchResult = response.getValue();

                // Batch statistics
                TextDocumentBatchStatistics batchStatistics = detectedBatchResult.getStatistics();
                System.out.printf("Batch statistics, transaction count: %s, valid document count: %s.%n",
                    batchStatistics.getTransactionCount(),
                    batchStatistics.getValidDocumentCount());

                for (DetectLanguageResult detectLanguageResult : detectedBatchResult) {
                    DetectedLanguage detectedLanguage = detectLanguageResult.getPrimaryLanguage();
                    System.out.printf("Detected language name: %s, ISO 6391 Name: %s, score: %.2f.%n",
                        detectedLanguage.getName(),
                        detectedLanguage.getIso6391Name(),
                        detectedLanguage.getScore());
                }
            });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.detectLanguageBatchWithResponse#List-TextAnalyticsRequestOptions
    }

    // Entity

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#recognizeEntities(String)}
     */
    public void recognizeEntities() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeEntities#string
        String inputText = "Satya Nadella is the CEO of Microsoft";
        textAnalyticsAsyncClient.recognizeEntities(inputText)
            .subscribe(entity -> System.out.printf("Recognized categorized entity: %s, category: %s, score: %.2f.%n",
                entity.getText(),
                entity.getCategory(),
                entity.getScore()));
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeEntities#string
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#recognizeEntities(String, String)}
     */
    public void recognizeEntitiesWithResponse() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeEntities#string-string
        String inputText1 = "Satya Nadella is the CEO of Microsoft";
        textAnalyticsAsyncClient.recognizeEntities(inputText1, "en")
            .subscribe(entity -> System.out.printf("Recognized categorized entity: %s, category: %s, score: %.2f.%n",
                entity.getText(),
                entity.getCategory(),
                entity.getScore()));
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeEntities#string-string
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#recognizeEntitiesBatch(List)}
     */
    public void recognizeEntitiesStringList() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeEntitiesBatch#List
        List<String> textInputs1 = Arrays.asList(
            "I had a wonderful trip to Seattle last week.",
            "I work at Microsoft."
        );

        textAnalyticsAsyncClient.recognizeEntitiesBatch(textInputs1).subscribe(entityBatchResult -> {
            // Batch statistics
            TextDocumentBatchStatistics batchStatistics = entityBatchResult.getStatistics();
            System.out.printf("Batch statistics, transaction count: %s, valid document count: %s.%n",
                batchStatistics.getTransactionCount(),
                batchStatistics.getValidDocumentCount());

            for (RecognizeEntitiesResult recognizeEntitiesResult : entityBatchResult) {
                for (CategorizedEntity entity : recognizeEntitiesResult.getEntities()) {
                    System.out.printf("Recognized categorized entity: %s, category: %s, score: %.2f.%n",
                        entity.getText(),
                        entity.getCategory(),
                        entity.getScore());
                }
            }
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeEntitiesBatch#List
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#recognizeEntitiesBatchWithResponse(List, String,
     * TextAnalyticsRequestOptions)}
     */
    public void recognizeEntitiesStringListWithResponse() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeEntitiesBatchWithResponse#List-String-TextAnalyticsRequestOptions
        List<String> textInputs1 = Arrays.asList(
            "I had a wonderful trip to Seattle last week.", "I work at Microsoft.");

        textAnalyticsAsyncClient.recognizeEntitiesBatchWithResponse(textInputs1, "en", null)
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
                            "Recognized categorized entity: %s, category: %s, score: %.2f.%n",
                            entity.getText(),
                            entity.getCategory(),
                            entity.getScore());
                    }
                }
            });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeEntitiesBatchWithResponse#List-String-TextAnalyticsRequestOptions
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#recognizeEntitiesBatchWithResponse(List,
     * TextAnalyticsRequestOptions)}
     */
    public void recognizeBatchEntitiesWithResponse() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeEntitiesBatchWithResponse#List-TextAnalyticsRequestOptions
        List<TextDocumentInput> textDocumentInputs1 = Arrays.asList(
            new TextDocumentInput("0", "I had a wonderful trip to Seattle last week."),
            new TextDocumentInput("1", "I work at Microsoft."));

        // Request options: show statistics and model version
        TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions().setShowStatistics(true);

        textAnalyticsAsyncClient.recognizeEntitiesBatchWithResponse(textDocumentInputs1, requestOptions)
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
                            "Recognized categorized entity: %s, category: %s, score: %.2f.%n",
                            entity.getText(),
                            entity.getCategory(),
                            entity.getScore());
                    }
                }
            });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeEntitiesBatchWithResponse#List-TextAnalyticsRequestOptions
    }

    // Pii Entity

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#recognizePiiEntities(String)}
     */
    public void recognizePiiEntities() {

        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizePiiEntities#string
        String inputText = "My SSN is 555-55-5555";
        textAnalyticsAsyncClient.recognizePiiEntities(inputText).subscribe(piiEntity -> System.out.printf(
            "Recognized categorized entity: %s, category: %s, score: %.2f.%n",
            piiEntity.getText(),
            piiEntity.getCategory(),
            piiEntity.getScore()));
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizePiiEntities#string
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#recognizePiiEntities(String, String)}
     */
    public void recognizePiiEntitiesWithResponse() {

        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizePiiEntities#string-string
        String inputText1 = "My SSN is 555-55-5555";
        textAnalyticsAsyncClient.recognizePiiEntities(inputText1, "en")
            .subscribe(entity -> System.out.printf("Recognized PII entity: %s, category: %s, score: %.2f.%n",
                entity.getText(),
                entity.getCategory(),
                entity.getScore()));
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizePiiEntities#string-string
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#recognizePiiEntitiesBatch(List)}
     */
    public void recognizePiiEntitiesStringList() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizePiiEntitiesBatch#list
        List<String> textInputs = Arrays.asList(
            "My SSN is 555-55-5555.", "Visa card 0111 1111 1111 1111.");

        textAnalyticsAsyncClient.recognizePiiEntitiesBatch(textInputs).subscribe(recognizeEntitiesResults -> {

            // Batch statistics
            TextDocumentBatchStatistics batchStatistics = recognizeEntitiesResults.getStatistics();
            System.out.printf("Batch statistics, transaction count: %s, valid document count: %s.%n",
                batchStatistics.getTransactionCount(),
                batchStatistics.getValidDocumentCount());

            for (RecognizePiiEntitiesResult recognizeEntitiesResult : recognizeEntitiesResults) {
                for (PiiEntity entity : recognizeEntitiesResult.getEntities()) {
                    System.out.printf("Recognized PII entity: %s, category: %s, score: %.2f.%n",
                        entity.getText(),
                        entity.getCategory(),
                        entity.getScore());
                }
            }
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizePiiEntitiesBatch#list
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#recognizePiiEntitiesBatchWithResponse(List, String,
     * TextAnalyticsRequestOptions)}
     */
    public void recognizePiiEntitiesStringListWithResponse() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizePiiEntitiesBatchWithResponse#list-String-TextAnalyticsRequestOptions
        List<String> textInputs = Arrays.asList(
            "My SSN is 555-55-5555.",
            "Visa card 0111 1111 1111 1111."
        );

        textAnalyticsAsyncClient.recognizePiiEntitiesBatchWithResponse(textInputs, "US", null).subscribe(response -> {
            DocumentResultCollection<RecognizePiiEntitiesResult> recognizeEntitiesResults = response.getValue();

            // Batch statistics
            TextDocumentBatchStatistics batchStatistics = recognizeEntitiesResults.getStatistics();
            System.out.printf("Batch statistics, transaction count: %s, valid document count: %s.%n",
                batchStatistics.getTransactionCount(),
                batchStatistics.getValidDocumentCount());

            for (RecognizePiiEntitiesResult recognizeEntitiesResult : recognizeEntitiesResults) {
                for (PiiEntity entity : recognizeEntitiesResult.getEntities()) {
                    System.out.printf("Recognized PII entity: %s, category: %s, score: %.2f.%n",
                        entity.getText(),
                        entity.getCategory(),
                        entity.getScore());
                }
            }
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizePiiEntitiesBatchWithResponse#list-String-TextAnalyticsRequestOptions
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#recognizePiiEntitiesBatchWithResponse(List,
     * TextAnalyticsRequestOptions)}
     */
    public void recognizeBatchPiiEntitiesWithResponse() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizePiiEntitiesBatchWithResponse#List-TextAnalyticsRequestOptions
        List<TextDocumentInput> textDocumentInputs1 = Arrays.asList(
            new TextDocumentInput("0", "My SSN is 555-55-5555."),
            new TextDocumentInput("1", "Visa card 0111 1111 1111 1111."));

        // Request options: show statistics and model version
        TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions().setShowStatistics(true);

        textAnalyticsAsyncClient.recognizePiiEntitiesBatchWithResponse(textDocumentInputs1, requestOptions)
            .subscribe(response -> {
                DocumentResultCollection<RecognizePiiEntitiesResult> recognizeEntitiesResults = response.getValue();

                // Batch statistics
                TextDocumentBatchStatistics batchStatistics = recognizeEntitiesResults.getStatistics();
                System.out.printf("Batch statistics, transaction count: %s, valid document count: %s.%n",
                    batchStatistics.getTransactionCount(),
                    batchStatistics.getValidDocumentCount());

                for (RecognizePiiEntitiesResult recognizeEntitiesResult : recognizeEntitiesResults) {
                    for (PiiEntity entity : recognizeEntitiesResult.getEntities()) {
                        System.out.printf("Recognized PII entity: %s, category: %s, score: %.2f.%n",
                            entity.getText(),
                            entity.getCategory(),
                            entity.getScore());
                    }
                }
            });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizePiiEntitiesBatchWithResponse#List-TextAnalyticsRequestOptions
    }

    // Linked Entity

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#recognizeLinkedEntities(String)}
     */
    public void recognizeLinkedEntities() {

        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeLinkedEntities#string
        String inputText = "Old Faithful is a geyser at Yellowstone Park.";
        textAnalyticsAsyncClient.recognizeLinkedEntities(inputText).subscribe(linkedEntity -> {
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
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeLinkedEntities#string
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#recognizeLinkedEntities(String, String)}
     */
    public void recognizeLinkedEntitiesWithResponse() {

        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeLinkedEntities#string-string
        String inputText1 = "Old Faithful is a geyser at Yellowstone Park.";
        textAnalyticsAsyncClient.recognizeLinkedEntities(inputText1, "en")
            .subscribe(linkedEntity -> {
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
            });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeLinkedEntities#string-string
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#recognizeLinkedEntitiesBatch(List)}
     */
    public void recognizeLinkedEntitiesStringList() {

        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeLinkedEntitiesBatch#List
        List<String> textInputs1 = Arrays.asList(
            "Old Faithful is a geyser at Yellowstone Park.",
            "Mount Shasta has lenticular clouds."
        );

        textAnalyticsAsyncClient.recognizeLinkedEntitiesBatch(textInputs1).subscribe(recognizeLinkedEntitiesResults -> {

            // Batch statistics
            TextDocumentBatchStatistics batchStatistics = recognizeLinkedEntitiesResults.getStatistics();
            System.out.printf("Batch statistics, transaction count: %s, valid document count: %s.%n",
                batchStatistics.getTransactionCount(),
                batchStatistics.getValidDocumentCount());

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
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeLinkedEntitiesBatch#List

    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#recognizeLinkedEntitiesBatchWithResponse(List,
     * TextAnalyticsRequestOptions)}
     */
    public void recognizeLinkedEntitiesStringListWithResponse() {

        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeLinkedEntitiesBatchWithResponse#List-String-TextAnalyticsRequestOptions
        List<String> textInputs1 = Arrays.asList(
            "Old Faithful is a geyser at Yellowstone Park.",
            "Mount Shasta has lenticular clouds."
        );

        textAnalyticsAsyncClient.recognizeLinkedEntitiesBatchWithResponse(textInputs1, "en", null)
            .subscribe(response -> {
                DocumentResultCollection<RecognizeLinkedEntitiesResult> recognizeLinkedEntitiesResults = response.getValue();

                // Batch statistics
                TextDocumentBatchStatistics batchStatistics = recognizeLinkedEntitiesResults.getStatistics();
                System.out.printf("Batch statistics, transaction count: %s, valid document count: %s.%n",
                    batchStatistics.getTransactionCount(),
                    batchStatistics.getValidDocumentCount());

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
            });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeLinkedEntitiesBatchWithResponse#List-String-TextAnalyticsRequestOptions
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#recognizeLinkedEntitiesBatchWithResponse(List,
     * TextAnalyticsRequestOptions)}
     */
    public void recognizeBatchLinkedEntitiesWithResponse() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeLinkedEntitiesBatchWithResponse#List-TextAnalyticsRequestOptions
        List<TextDocumentInput> textDocumentInputs1 = Arrays.asList(
            new TextDocumentInput("0", "Old Faithful is a geyser at Yellowstone Park."),
            new TextDocumentInput("1", "Mount Shasta has lenticular clouds."));

        // Request options: show statistics and model version
        TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions().setShowStatistics(true);

        textAnalyticsAsyncClient.recognizeLinkedEntitiesBatchWithResponse(textDocumentInputs1, requestOptions)
            .subscribe(response -> {
                DocumentResultCollection<RecognizeLinkedEntitiesResult> recognizeLinkedEntitiesResults =
                    response.getValue();

                // Batch statistics
                TextDocumentBatchStatistics batchStatistics = recognizeLinkedEntitiesResults.getStatistics();
                System.out.printf("Batch statistics, transaction count: %s, valid document count: %s.%n",
                    batchStatistics.getTransactionCount(),
                    batchStatistics.getValidDocumentCount());

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
            });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeLinkedEntitiesBatchWithResponse#List-TextAnalyticsRequestOptions
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
    public void extractKeyPhrasesWithResponse() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.extractKeyPhrases#string-string
        System.out.println("Extracted phrases:");
        textAnalyticsAsyncClient.extractKeyPhrases("Bonjour tout le monde", "fr")
            .subscribe(keyPhrase -> System.out.printf("%s.%n", keyPhrase));
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.extractKeyPhrases#string-string
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#extractKeyPhrasesBatch(List)}
     */
    public void extractKeyPhrasesStringList() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.extractKeyPhrasesBatch#list
        List<String> textInputs = Arrays.asList(
            "Hello world. This is some input text that I love.",
            "Bonjour tout le monde");

        textAnalyticsAsyncClient.extractKeyPhrasesBatch(textInputs).subscribe(extractKeyPhraseResults -> {

            // Batch statistics
            TextDocumentBatchStatistics batchStatistics = extractKeyPhraseResults.getStatistics();
            System.out.printf("Batch statistics, transaction count: %s, valid document count: %s.%n",
                batchStatistics.getTransactionCount(),
                batchStatistics.getValidDocumentCount());

            for (ExtractKeyPhraseResult extractKeyPhraseResult : extractKeyPhraseResults) {
                System.out.println("Extracted phrases:");
                for (String keyPhrase : extractKeyPhraseResult.getKeyPhrases()) {
                    System.out.printf("%s.%n", keyPhrase);
                }
            }
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.extractKeyPhrasesBatch#list
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#extractKeyPhrasesBatchWithResponse(List, String,
     * TextAnalyticsRequestOptions)}
     */
    public void extractKeyPhrasesStringListWithResponse() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.extractKeyPhrasesBatchWithResponse#List-String-TextAnalyticsRequestOptions
        List<String> textInputs1 = Arrays.asList(
            "Hello world. This is some input text that I love.",
            "Bonjour tout le monde");

        textAnalyticsAsyncClient.extractKeyPhrasesBatchWithResponse(textInputs1, "en", null).subscribe(response -> {
            DocumentResultCollection<ExtractKeyPhraseResult> extractKeyPhraseResults = response.getValue();

            // Batch statistics
            TextDocumentBatchStatistics batchStatistics = extractKeyPhraseResults.getStatistics();
            System.out.printf("Batch statistics, transaction count: %s, valid document count: %s.%n",
                batchStatistics.getTransactionCount(),
                batchStatistics.getValidDocumentCount());

            for (ExtractKeyPhraseResult extractKeyPhraseResult : extractKeyPhraseResults) {
                System.out.println("Extracted phrases:");
                for (String keyPhrase : extractKeyPhraseResult.getKeyPhrases()) {
                    System.out.printf("%s.%n", keyPhrase);
                }
            }
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.extractKeyPhrasesBatchWithResponse#List-String-TextAnalyticsRequestOptions
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#extractKeyPhrasesBatchWithResponse(List,
     * TextAnalyticsRequestOptions)}
     */
    public void extractBatchKeyPhrasesWithResponse() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.extractKeyPhrasesBatchWithResponse#List-TextAnalyticsRequestOptions
        List<TextDocumentInput> textDocumentInputs1 = Arrays.asList(
            new TextDocumentInput("0", "I had a wonderful trip to Seattle last week."),
            new TextDocumentInput("1", "I work at Microsoft."));

        // Request options: show statistics and model version
        TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions().setShowStatistics(true);

        textAnalyticsAsyncClient.extractKeyPhrasesBatchWithResponse(textDocumentInputs1, requestOptions)
            .subscribe(response -> {
                DocumentResultCollection<ExtractKeyPhraseResult> extractKeyPhraseResults = response.getValue();

                // Batch statistics
                TextDocumentBatchStatistics batchStatistics = extractKeyPhraseResults.getStatistics();
                System.out.printf("Batch statistics, transaction count: %s, valid document count: %s.%n",
                    batchStatistics.getTransactionCount(),
                    batchStatistics.getValidDocumentCount());

                for (ExtractKeyPhraseResult extractKeyPhraseResult : extractKeyPhraseResults) {
                    System.out.println("Extracted phrases:");
                    for (String keyPhrase : extractKeyPhraseResult.getKeyPhrases()) {
                        System.out.printf("%s.%n", keyPhrase);
                    }
                }
            });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.extractKeyPhrasesBatchWithResponse#List-TextAnalyticsRequestOptions
    }

    // Sentiment

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#analyzeSentiment(String)}
     */
    public void analyzeSentiment() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeSentiment#string
        String inputText = "The hotel was dark and unclean.";
        textAnalyticsAsyncClient.analyzeSentiment(inputText).subscribe(documentSentiment -> {
            System.out.printf("Recognized document sentiment: %s.%n", documentSentiment.getSentiment());

            for (SentenceSentiment sentenceSentiment : documentSentiment.getSentences()) {
                System.out.printf(
                    "Recognized sentence sentiment: %s, positive score: %.2f, neutral score: %.2f, negative score: %.2f.%n",
                    sentenceSentiment.getSentiment(),
                    sentenceSentiment.getSentimentScores().getPositive(),
                    sentenceSentiment.getSentimentScores().getNeutral(),
                    sentenceSentiment.getSentimentScores().getNegative());
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
            .subscribe(response -> {
                DocumentSentiment documentSentiment = response.getValue();
                System.out.printf("Recognized sentiment class: %s.%n", documentSentiment.getSentiment());

                for (SentenceSentiment sentenceSentiment : documentSentiment.getSentences()) {
                    System.out.printf("Recognized sentence sentiment: %s, positive score: %.2f, neutral score: %.2f, "
                            + "negative score: %.2f.%n",
                        sentenceSentiment.getSentiment(),
                        sentenceSentiment.getSentimentScores().getPositive(),
                        sentenceSentiment.getSentimentScores().getNeutral(),
                        sentenceSentiment.getSentimentScores().getNegative());
                }
            });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeSentimentWithResponse#string-string
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#analyzeSentimentBatch(List)}
     */
    public void analyzeSentimentStringList() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeSentimentBatch#List
        List<String> textInputs = Arrays.asList(
            "The hotel was dark and unclean.", "The restaurant had amazing gnocchi.");

        textAnalyticsAsyncClient.analyzeSentimentBatch(textInputs).subscribe(analyzeSentimentResults -> {

            // Batch statistics
            TextDocumentBatchStatistics batchStatistics = analyzeSentimentResults.getStatistics();
            System.out.printf("Batch statistics, transaction count: %s, valid document count: %s.%n",
                batchStatistics.getTransactionCount(),
                batchStatistics.getValidDocumentCount());

            for (AnalyzeSentimentResult analyzeSentimentResult : analyzeSentimentResults) {
                System.out.printf("Document ID: %s%n", analyzeSentimentResult.getId());
                DocumentSentiment documentSentiment = analyzeSentimentResult.getDocumentSentiment();
                System.out.printf("Recognized document sentiment: %s.%n",
                    documentSentiment.getSentiment());
                for (SentenceSentiment sentenceSentiment : documentSentiment.getSentences()) {
                    System.out.printf("Recognized sentence sentiment: %s, positive score: %.2f, neutral score: %.2f, "
                            + "negative score: %.2f.%n",
                        sentenceSentiment.getSentiment(),
                        sentenceSentiment.getSentimentScores().getPositive(),
                        sentenceSentiment.getSentimentScores().getNeutral(),
                        sentenceSentiment.getSentimentScores().getNegative());
                }
            }
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeSentimentBatch#List
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#analyzeSentimentBatchWithResponse(List, String,
     * TextAnalyticsRequestOptions)}
     */
    public void analyzeSentimentWithResponseStringList() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeSentimentBatchWithResponse#List-String-TextAnalyticsRequestOptions
        List<String> textInputs1 = Arrays.asList(
            "The hotel was dark and unclean.",
            "The restaurant had amazing gnocchi."
        );

        textAnalyticsAsyncClient.analyzeSentimentBatchWithResponse(textInputs1, "en", null).subscribe(response -> {
            DocumentResultCollection<AnalyzeSentimentResult> analyzeSentimentResults = response.getValue();

            // Batch statistics
            TextDocumentBatchStatistics batchStatistics = analyzeSentimentResults.getStatistics();
            System.out.printf("Batch statistics, transaction count: %s, valid document count: %s.%n",
                batchStatistics.getTransactionCount(),
                batchStatistics.getValidDocumentCount());

            for (AnalyzeSentimentResult analyzeSentimentResult : analyzeSentimentResults) {
                System.out.printf("Document ID: %s%n", analyzeSentimentResult.getId());
                DocumentSentiment documentSentiment = analyzeSentimentResult.getDocumentSentiment();
                System.out.printf("Recognized document sentiment: %s.%n", documentSentiment.getSentiment());
                for (SentenceSentiment sentenceSentiment : documentSentiment.getSentences()) {
                    System.out.printf("Recognized sentence sentiment: %s, positive score: %.2f, neutral score: %.2f, "
                            + "negative score: %.2f.%n",
                        sentenceSentiment.getSentiment(),
                        sentenceSentiment.getSentimentScores().getPositive(),
                        sentenceSentiment.getSentimentScores().getNeutral(),
                        sentenceSentiment.getSentimentScores().getNegative());
                }
            }
        });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeSentimentBatchWithResponse#List-String-TextAnalyticsRequestOptions
    }

    /**
     * Code snippet for {@link TextAnalyticsAsyncClient#analyzeSentimentBatchWithResponse(List,
     * TextAnalyticsRequestOptions)}
     */
    public void analyzeBatchSentimentWithResponse() {
        // BEGIN: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeSentimentBatchWithResponse#List-TextAnalyticsRequestOptions
        List<TextDocumentInput> textDocumentInputs1 = Arrays.asList(
            new TextDocumentInput("0", "The hotel was dark and unclean."),
            new TextDocumentInput("1", "The restaurant had amazing gnocchi."));

        // Request options: show statistics and model version
        TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions().setShowStatistics(true);

        textAnalyticsAsyncClient.analyzeSentimentBatchWithResponse(textDocumentInputs1, requestOptions)
            .subscribe(response -> {
                DocumentResultCollection<AnalyzeSentimentResult> analyzeSentimentResults = response.getValue();

                // Batch statistics
                TextDocumentBatchStatistics batchStatistics = analyzeSentimentResults.getStatistics();
                System.out.printf("Batch statistics, transaction count: %s, valid document count: %s.%n",
                    batchStatistics.getTransactionCount(),
                    batchStatistics.getValidDocumentCount());

                for (AnalyzeSentimentResult analyzeSentimentResult : analyzeSentimentResults) {
                    System.out.printf("Document ID: %s%n", analyzeSentimentResult.getId());
                    DocumentSentiment documentSentiment = analyzeSentimentResult.getDocumentSentiment();
                    System.out.printf("Recognized document sentiment: %s.%n", documentSentiment.getSentiment());
                    for (SentenceSentiment sentenceSentiment : documentSentiment.getSentences()) {
                        System.out.printf("Recognized sentence sentiment: %s, positive score: %.2f, neutral score: %.2f, "
                                + "negative score: %.2f.%n",
                            sentenceSentiment.getSentiment(),
                            sentenceSentiment.getSentimentScores().getPositive(),
                            sentenceSentiment.getSentimentScores().getNeutral(),
                            sentenceSentiment.getSentimentScores().getNegative());
                    }
                }
            });
        // END: com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeSentimentBatchWithResponse#List-TextAnalyticsRequestOptions
    }
}
