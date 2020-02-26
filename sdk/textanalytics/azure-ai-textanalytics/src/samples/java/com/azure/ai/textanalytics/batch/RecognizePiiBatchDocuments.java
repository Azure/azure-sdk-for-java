// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.batch;

import com.azure.ai.textanalytics.TextAnalyticsClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.DocumentResultCollection;
import com.azure.ai.textanalytics.models.PiiEntity;
import com.azure.ai.textanalytics.models.RecognizePiiEntitiesResult;
import com.azure.ai.textanalytics.models.TextAnalyticsApiKeyCredential;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.core.util.Context;

import java.util.Arrays;
import java.util.List;

/**
 * Sample demonstrates how to recognize the PII(Personally Identifiable Information) entities of a batch input text.
 */
public class RecognizePiiBatchDocuments {
    /**
     * Main method to invoke this demo about how to recognize the Personally Identifiable Information entities of a
     * batch input text.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
            .apiKey(new TextAnalyticsApiKeyCredential("{api_key}"))
            .endpoint("{endpoint}")
            .buildClient();

        // The texts that need be analyzed.
        List<TextDocumentInput> inputs = Arrays.asList(
            new TextDocumentInput("1", "My SSN is 555-55-5555", "en"),
            new TextDocumentInput("2", "Visa card 4111 1111 1111 1111", "en")
        );

        // Request options: show statistics and model version
        final TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions().setShowStatistics(true);

        // Recognizing batch entities
        final DocumentResultCollection<RecognizePiiEntitiesResult> recognizedBatchResult =
            client.recognizePiiEntitiesBatchWithResponse(inputs, requestOptions, Context.NONE).getValue();
        System.out.printf("Model version: %s%n", recognizedBatchResult.getModelVersion());

        // Batch statistics
        final TextDocumentBatchStatistics batchStatistics = recognizedBatchResult.getStatistics();
        System.out.printf("A batch of document statistics, document count: %s, erroneous document count: %s, transaction count: %s, valid document count: %s.%n",
            batchStatistics.getDocumentCount(),
            batchStatistics.getInvalidDocumentCount(),
            batchStatistics.getTransactionCount(),
            batchStatistics.getValidDocumentCount());

        // Recognized Personally Identifiable Information entities for each of document from a batch of documents
        for (RecognizePiiEntitiesResult piiEntityDocumentResult : recognizedBatchResult) {
            System.out.printf("Document ID: %s%n", piiEntityDocumentResult.getId());
            // Erroneous document
            if (piiEntityDocumentResult.isError()) {
                System.out.printf("Cannot recognize Personally Identifiable Information entities. Error: %s%n",
                    piiEntityDocumentResult.getError().getMessage());
                continue;
            }
            // Valid document
            for (PiiEntity entity : piiEntityDocumentResult.getEntities()) {
                System.out.printf("Recognized personal identifiable information entity: %s, entity category: %s, entity sub-category: %s, offset: %s, length: %s, score: %.2f.%n",
                    entity.getText(),
                    entity.getCategory(),
                    entity.getSubCategory() == null || entity.getSubCategory().isEmpty() ? "N/A" : entity.getSubCategory(),
                    entity.getOffset(),
                    entity.getLength(),
                    entity.getScore());
            }
        }
    }
}
