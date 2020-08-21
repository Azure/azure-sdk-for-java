// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.batch;

import com.azure.ai.textanalytics.TextAnalyticsAsyncClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.RecognizePiiEntitiesResult;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.util.RecognizePiiEntitiesResultCollection;
import com.azure.core.credential.AzureKeyCredential;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Sample demonstrates how to recognize the Personally Identifiable Information(PII) entities of documents.
 */
public class RecognizePiiEntitiesBatchDocumentsAsync {
    /**
     * Main method to invoke this demo about how to recognize the Personally Identifiable Information entities of
     * documents.
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
        List<TextDocumentInput> documents = Arrays.asList(
            new TextDocumentInput("1", "My SSN is 859-98-0987").setLanguage("en"),
            new TextDocumentInput("2", "Visa card 4111 1111 1111 1111").setLanguage("en")
        );

        // Request options: show statistics and model version
        TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions().setIncludeStatistics(true).setModelVersion("latest");

        // Recognizing Personally Identifiable Information entities for each document in a batch of documents
        client.recognizePiiEntitiesBatchWithResponse(documents, requestOptions).subscribe(
            entitiesBatchResultResponse -> {
                // Response's status code
                System.out.printf("Status code of request response: %d%n", entitiesBatchResultResponse.getStatusCode());
                RecognizePiiEntitiesResultCollection recognizePiiEntitiesResultCollection = entitiesBatchResultResponse.getValue();

                // Model version
                System.out.printf("Results of Azure Text Analytics \"Personally Identifiable Information Entities Recognition\" Model, version: %s%n", recognizePiiEntitiesResultCollection.getModelVersion());

                // Batch statistics
                TextDocumentBatchStatistics batchStatistics = recognizePiiEntitiesResultCollection.getStatistics();
                System.out.printf("Documents statistics: document count = %s, erroneous document count = %s, transaction count = %s, valid document count = %s.%n",
                    batchStatistics.getDocumentCount(), batchStatistics.getInvalidDocumentCount(), batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

                // Recognized Personally Identifiable Information entities for each of documents from a batch of documents
                AtomicInteger counter = new AtomicInteger();
                for (RecognizePiiEntitiesResult entitiesResult : recognizePiiEntitiesResultCollection) {
                    System.out.printf("%n%s%n", documents.get(counter.getAndIncrement()));
                    if (entitiesResult.isError()) {
                        // Erroneous document
                        System.out.printf("Cannot recognize Personally Identifiable Information entities. Error: %s%n", entitiesResult.getError().getMessage());
                    } else {
                        // Valid document
                        entitiesResult.getEntities().forEach(entity -> System.out.printf(
                            "Recognized Personally Identifiable Information entity: %s, entity category: %s, entity subcategory: %s, offset: %s, length: %s, confidence score: %f.%n",
                            entity.getText(), entity.getCategory(), entity.getSubcategory(), entity.getOffset(), entity.getLength(), entity.getConfidenceScore()));
                    }
                }
            },
            error -> System.err.println("There was an error recognizing Personally Identifiable Information entities of the documents." + error),
            () -> System.out.println("Batch of Personally Identifiable Information entities recognized."));

        // The .subscribe() creation and assignment is not a blocking call. For the purpose of this example, we sleep
        // the thread so the program does not end before the send operation is complete. Using .block() instead of
        // .subscribe() will turn this into a synchronous call.
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException ignored) {
        }
    }
}
