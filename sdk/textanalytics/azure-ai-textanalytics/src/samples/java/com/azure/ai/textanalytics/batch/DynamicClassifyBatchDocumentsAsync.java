// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.batch;

import com.azure.ai.textanalytics.TextAnalyticsAsyncClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.ClassificationCategory;
import com.azure.ai.textanalytics.models.ClassificationType;
import com.azure.ai.textanalytics.models.DynamicClassifyOptions;
import com.azure.ai.textanalytics.util.DynamicClassifyDocumentResultCollection;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.core.credential.AzureKeyCredential;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.Arrays.asList;

/**
 * Sample demonstrates how to asynchronously analyze dynamic classification of {@link TextDocumentInput} documents.
 */
public class DynamicClassifyBatchDocumentsAsync {
    /**
     * Main method to invoke this demo about how to analyze dynamic classification of {@link TextDocumentInput} documents.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.
        TextAnalyticsAsyncClient client = new TextAnalyticsClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .buildAsyncClient();

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
        client.dynamicClassifyBatchWithResponse(documents, Arrays.asList("Health", "Politics", "Music", "Sport"),
            requestOptions).subscribe(
                response -> {
                    // Response's status code
                    System.out.printf("Status code of request response: %d%n", response.getStatusCode());
                    DynamicClassifyDocumentResultCollection resultCollection = response.getValue();

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
                },
                error -> System.err.println("There was an error analyzing dynamic classification of the documents." + error),
                () -> System.out.println("End of analyzing dynamic classification."));

        // The .subscribe() creation and assignment is not a blocking call. For the purpose of this example, we sleep
        // the thread so the program does not end before the send operation is complete. Using .block() instead of
        // .subscribe() will turn this into a synchronous call.
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException ignored) {
        }
    }
}
