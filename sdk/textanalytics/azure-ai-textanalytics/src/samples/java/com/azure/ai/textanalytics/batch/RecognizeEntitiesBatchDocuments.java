// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.batch;

import com.azure.ai.textanalytics.TextAnalyticsClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.DocumentResultCollection;
import com.azure.ai.textanalytics.models.NamedEntity;
import com.azure.ai.textanalytics.models.RecognizeEntitiesResult;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.core.util.Context;

import java.util.Arrays;
import java.util.List;

/**
 * Sample demonstrate how to recognize entities of a batch of text inputs.
 */
public class RecognizeEntitiesBatchDocuments {
    /**
     * Main method to invoke this demo about how to recognize entities of a batch of text inputs.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
            .subscriptionKey("subscription-key")
            .endpoint("https://servicename.cognitiveservices.azure.com/")
            .buildClient();

        // The texts that need be analysed.
        List<TextDocumentInput> inputs = Arrays.asList(
            new TextDocumentInput("1", "Satya Nadella is the CEO of Microsoft.", "en"),
            new TextDocumentInput("2", "Elon Musk is the CEO of SpaceX and Tesla.", "en")
        );

        final TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions().setShowStatistics(true);
        final DocumentResultCollection<RecognizeEntitiesResult> detectedBatchResult =
            client.recognizeBatchEntitiesWithResponse(inputs, requestOptions, Context.NONE).getValue();
        System.out.printf("Model version: %s%n", detectedBatchResult.getModelVersion());

        final TextDocumentBatchStatistics batchStatistics = detectedBatchResult.getStatistics();
        System.out.printf("A batch of document statistics, document count: %s, erroneous document count: %s, transaction count: %s, valid document count: %s.%n",
            batchStatistics.getDocumentCount(),
            batchStatistics.getErroneousDocumentCount(),
            batchStatistics.getTransactionCount(),
            batchStatistics.getValidDocumentCount());

        for (RecognizeEntitiesResult recognizeEntitiesResult : detectedBatchResult) {
            for (NamedEntity entity : recognizeEntitiesResult.getNamedEntities()) {
                System.out.printf("Recognized NamedEntity: %s, NamedEntity Type: %s, NamedEntity Subtype: %s, Offset: %s, Length: %s, Score: %s.%n",
                    entity.getText(),
                    entity.getType(),
                    entity.getSubtype(),
                    entity.getOffset(),
                    entity.getLength(),
                    entity.getScore());
            }
        }
    }
}
