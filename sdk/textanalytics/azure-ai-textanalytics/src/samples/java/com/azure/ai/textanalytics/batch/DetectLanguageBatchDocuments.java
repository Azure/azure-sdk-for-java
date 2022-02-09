// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.batch;

import com.azure.ai.textanalytics.TextAnalyticsClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.DetectLanguageInput;
import com.azure.ai.textanalytics.util.DetectLanguageResultCollection;
import com.azure.ai.textanalytics.models.DetectedLanguage;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Sample demonstrates how to detect the languages of {@link DetectLanguageInput} documents.
 */
public class DetectLanguageBatchDocuments {
    /**
     * Main method to invoke this demo about how to detect the languages of {@link DetectLanguageInput} documents.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .buildClient();

        // The texts that need be analyzed.
        List<DetectLanguageInput> documents = Arrays.asList(
            new DetectLanguageInput("A", "This is written in English.", "US"),
            new DetectLanguageInput("B", "Este es un documento  escrito en Español.", "ES")
        );

        TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions().setIncludeStatistics(true).setModelVersion("latest");

        // Detecting language for each document in a batch of documents
        Response<DetectLanguageResultCollection> detectedLanguageResultResponse = client.detectLanguageBatchWithResponse(documents, requestOptions, Context.NONE);

        // Response's status code
        System.out.printf("Status code of request response: %d%n", detectedLanguageResultResponse.getStatusCode());
        DetectLanguageResultCollection detectedLanguageResultCollection = detectedLanguageResultResponse.getValue();

        // Model version
        System.out.printf("Results of Azure Text Analytics \"Language Detection\" Model, version: %s%n", detectedLanguageResultCollection.getModelVersion());

        // Batch statistics
        TextDocumentBatchStatistics batchStatistics = detectedLanguageResultCollection.getStatistics();
        System.out.printf("Documents statistics: document count = %s, erroneous document count = %s, transaction count = %s, valid document count = %s.%n",
            batchStatistics.getDocumentCount(), batchStatistics.getInvalidDocumentCount(), batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

        // Detected language for each document in a batch of documents
        AtomicInteger counter = new AtomicInteger();
        detectedLanguageResultCollection.forEach(detectLanguageResult -> {
            System.out.printf("%n%s%n", documents.get(counter.getAndIncrement()));
            if (detectLanguageResult.isError()) {
                // Erroneous document
                System.out.printf("Cannot detect language. Error: %s%n", detectLanguageResult.getError().getMessage());
            } else {
                // Valid document
                DetectedLanguage language = detectLanguageResult.getPrimaryLanguage();
                System.out.printf("Detected primary language: %s, ISO 6391 name: %s, confidence score: %f.%n",
                    language.getName(), language.getIso6391Name(), language.getConfidenceScore());
            }
        });
    }
}
