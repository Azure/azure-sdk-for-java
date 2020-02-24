// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.batch;

import com.azure.ai.textanalytics.TextAnalyticsClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.LinkedEntity;
import com.azure.ai.textanalytics.models.LinkedEntityMatch;
import com.azure.ai.textanalytics.models.RecognizeLinkedEntitiesResult;
import com.azure.ai.textanalytics.models.TextAnalyticsPagedResponse;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextAnalyticsApiKeyCredential;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.core.util.Context;

import java.util.Arrays;
import java.util.List;

/**
 * Sample demonstrates how to recognize the linked entities of a batch input text.
 */
public class RecognizeLinkedEntitiesBatchDocuments {
    /**
     * Main method to invoke this demo about how to recognize the linked entities of a batch input text.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
            .apiKey(new TextAnalyticsApiKeyCredential("b2f8b7b697c348dcb0e30055d49f3d0f"))
            .endpoint("https://javatextanalyticstestresources.cognitiveservices.azure.com/")
            .buildClient();

        // The texts that need be analyzed.
        List<TextDocumentInput> inputs = Arrays.asList(
            new TextDocumentInput("1", "Old Faithful is a geyser at Yellowstone Park.", "en"),
            new TextDocumentInput("2", "Mount Shasta has lenticular clouds.", "en")
        );

        // Request options: show statistics and model version
        final TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions().setShowStatistics(true);

        // Recognizing batch entities
        final Iterable<TextAnalyticsPagedResponse<RecognizeLinkedEntitiesResult>> recognizedBatchResult =
            client.recognizeLinkedEntitiesBatch(inputs, requestOptions, Context.NONE).iterableByPage();


        for (TextAnalyticsPagedResponse<RecognizeLinkedEntitiesResult> textAnalyticsPagedResponse : recognizedBatchResult) {

            System.out.printf("Model version: %s%n", textAnalyticsPagedResponse.getModelVersion());

            // Batch statistics
            final TextDocumentBatchStatistics batchStatistics = textAnalyticsPagedResponse.getStatistics();
            System.out.printf("A batch of document statistics, document count: %s, erroneous document count: %s, transaction count: %s, valid document count: %s.%n",
                batchStatistics.getDocumentCount(),
                batchStatistics.getInvalidDocumentCount(),
                batchStatistics.getTransactionCount(),
                batchStatistics.getValidDocumentCount());

            // Recognized linked entities from a batch of documents
            for (RecognizeLinkedEntitiesResult linkedEntityDocumentResult : textAnalyticsPagedResponse.getElements()) {
                System.out.printf("%nDocument ID: %s%n", linkedEntityDocumentResult.getId());
                System.out.printf("Input text: %s%n", linkedEntityDocumentResult.getInputText());
                // Erroneous document
                if (linkedEntityDocumentResult.isError()) {
                    System.out.printf("Cannot recognize linked entities. Error: %s%n", linkedEntityDocumentResult.getError().getMessage());
                    continue;
                }
                // Valid document
                for (LinkedEntity linkedEntity : linkedEntityDocumentResult.getEntities()) {
                    System.out.println("Linked Entities:");
                    System.out.printf("Name: %s, entity ID in data source: %s, URL: %s, data source: %s.%n",
                        linkedEntity.getName(),
                        linkedEntity.getDataSourceEntityId(),
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
        }
    }
}
