// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.batch;

import com.azure.ai.textanalytics.TextAnalyticsClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.CategorizedEntity;
import com.azure.ai.textanalytics.models.EntitiesResult;
import com.azure.ai.textanalytics.models.TextAnalyticsApiKeyCredential;
import com.azure.ai.textanalytics.models.TextAnalyticsPagedResponse;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.core.util.Context;

import java.util.Arrays;
import java.util.List;

/**
 * Sample demonstrates how to recognize the entities of a batch input text.
 */
public class RecognizeCategorizedEntitiesBatchDocuments {
    /**
     * Main method to invoke this demo about how to recognize the entities of a batch input text.
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
            new TextDocumentInput("1", "Satya Nadella is the CEO of Microsoft.", "en"),
            new TextDocumentInput("2", "Elon Musk is the CEO of SpaceX and Tesla.", "en")
        );

        // Request options: show statistics and model version
        final TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions().setShowStatistics(true);

        // Recognizing batch entities
        final Iterable<TextAnalyticsPagedResponse<EntitiesResult<CategorizedEntity>>> recognizedBatchResult =
            client.recognizeCategorizedEntitiesBatch(inputs, requestOptions, Context.NONE).iterableByPage();

        recognizedBatchResult.forEach(pagedResponse -> {
            System.out.printf("Model version: %s%n", pagedResponse.getModelVersion());

            // Batch statistics
            final TextDocumentBatchStatistics batchStatistics = pagedResponse.getStatistics();
            System.out.printf("A batch of document statistics, document count: %s, erroneous document count: %s, transaction count: %s, valid document count: %s.%n",
                batchStatistics.getDocumentCount(),
                batchStatistics.getInvalidDocumentCount(),
                batchStatistics.getTransactionCount(),
                batchStatistics.getValidDocumentCount());

            // Recognized entities for each of document from a batch of documents
            pagedResponse.getElements().forEach(entitiesResult -> {
                System.out.printf("%nDocument ID: %s, input text: %s%n", entitiesResult.getId(), entitiesResult.getInputText());
                if (entitiesResult.isError()) {
                    // Erroneous document
                    System.out.printf("Cannot recognize entities. Error: %s%n", entitiesResult.getError().getMessage());
                } else {
                    // Valid document
                    entitiesResult.getEntities().forEach(entity ->
                        System.out.printf("Recognized categorized entity: %s, entity category: %s, entity sub-category: %s, offset: %s, length: %s, score: %.2f.%n",
                            entity.getText(),
                            entity.getCategory(),
                            entity.getSubCategory() == null || entity.getSubCategory().isEmpty() ? "N/A" : entity.getSubCategory(),
                            entity.getOffset(),
                            entity.getLength(),
                            entity.getScore())
                    );
                }
            });
        });
    }
}
