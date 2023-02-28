// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.batch;

import com.azure.ai.textanalytics.TextAnalyticsClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.ClassificationCategory;
import com.azure.ai.textanalytics.models.ClassificationType;
import com.azure.ai.textanalytics.models.DynamicClassifyOptions;
import com.azure.ai.textanalytics.util.DynamicClassifyDocumentResultCollection;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * Sample demonstrates how to analyze dynamic classification of {@link TextDocumentInput} documents.
 */
public class DynamicClassifyBatchDocuments {
    /**
     * Main method to invoke this demo about how to analyze dynamic classification of {@link TextDocumentInput} documents.
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
        List<TextDocumentInput> documents = asList(
            new TextDocumentInput("A", "The WHO is issuing a warning about Monkey Pox."),
            new TextDocumentInput("B", "Mo Salah plays in Liverpool FC in England.")
        );

        DynamicClassifyOptions requestOptions = new DynamicClassifyOptions()
            .setClassificationType(ClassificationType.MULTI)
            .setIncludeStatistics(true)
            .setModelVersion("latest");

        // Dynamic classification for each document in a batch of documents
        final Response<DynamicClassifyDocumentResultCollection> dynamicClassifyResponse =
            client.dynamicClassifyBatchWithResponse(documents, Arrays.asList("Health", "Politics", "Music", "Sport"),
                requestOptions, Context.NONE);

        // Response's status code
        System.out.printf("Status code of request response: %d%n", dynamicClassifyResponse.getStatusCode());
        DynamicClassifyDocumentResultCollection resultCollection = dynamicClassifyResponse.getValue();

        // Model version
        System.out.printf("Results of \"Dynamic Classification\" Model, version: %s%n", resultCollection.getModelVersion());

        // Batch statistics
        TextDocumentBatchStatistics batchStatistics = resultCollection.getStatistics();
        System.out.printf("Documents statistics: document count = %d, erroneous document count = %d, transaction count = %d, valid document count = %d.%n",
            batchStatistics.getDocumentCount(), batchStatistics.getInvalidDocumentCount(), batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

        // Dynamic classification for each document in a batch of documents
        resultCollection.forEach(documentResult -> {
            System.out.println("Document ID: " + documentResult.getId());
            if (!documentResult.isError()) {
                for (ClassificationCategory classification : documentResult.getClassifications()) {
                    System.out.printf("\tCategory: %s, confidence score: %f.%n",
                        classification.getCategory(), classification.getConfidenceScore());
                }
            } else {
                System.out.printf("\tCannot classify category of document. Error: %s%n",
                    documentResult.getError().getMessage());
            }
        });
    }
}
