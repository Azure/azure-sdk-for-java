// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.batch;

import com.azure.ai.textanalytics.TextAnalyticsClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.DocumentResultCollection;
import com.azure.ai.textanalytics.models.NamedEntity;
import com.azure.ai.textanalytics.models.RecognizePiiEntitiesResult;
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
     * Main method to invoke this demo about how to recognize the PII entities of a batch input text.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
            .subscriptionKey("{subscription_key}")
            .endpoint("https://{servicename}.cognitiveservices.azure.com/")
            .buildClient();

        // The texts that need be analysed.
        List<TextDocumentInput> inputs = Arrays.asList(
            new TextDocumentInput("1", "My SSN is 555-55-5555", "en"),
            new TextDocumentInput("2", "Visa card 4111 1111 1111 1111", "en")
        );

        // Request options: show statistics and model version
        final TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions().setShowStatistics(true);

        // Recognizing batch entities
        final DocumentResultCollection<RecognizePiiEntitiesResult> recognizedBatchResult =
            client.recognizeBatchPiiEntitiesWithResponse(inputs, requestOptions, Context.NONE).getValue();
        System.out.printf("Model version: %s%n", recognizedBatchResult.getModelVersion());

        // Batch statistics
        final TextDocumentBatchStatistics batchStatistics = recognizedBatchResult.getStatistics();
        System.out.printf("A batch of document statistics, document count: %s, erroneous document count: %s, transaction count: %s, valid document count: %s.%n",
            batchStatistics.getDocumentCount(),
            batchStatistics.getErroneousDocumentCount(),
            batchStatistics.getTransactionCount(),
            batchStatistics.getValidDocumentCount());

        // Recognized PII entities for each of document from a batch of documents
        for (RecognizePiiEntitiesResult piiEntityDocumentResult : recognizedBatchResult) {
            System.out.printf("Document ID: %s%n", piiEntityDocumentResult.getId());
            // Erroneous document
            if (piiEntityDocumentResult.isError()) {
                System.out.printf("Cannot recognize PII entities. Error: %s%n", piiEntityDocumentResult.getError().getMessage());
                continue;
            }
            // Valid document
            for (NamedEntity entity : piiEntityDocumentResult.getNamedEntities()) {
                System.out.printf("Recognized personal identifiable information entity: %s, entity type: %s, entity subtype: %s, offset: %s, length: %s, score: %s.%n",
                    entity.getText(),
                    entity.getType(),
                    entity.getSubtype() == null || entity.getSubtype().isEmpty() ? "N/A" : entity.getSubtype(),
                    entity.getOffset(),
                    entity.getLength(),
                    entity.getScore());
            }
        }
    }
}
