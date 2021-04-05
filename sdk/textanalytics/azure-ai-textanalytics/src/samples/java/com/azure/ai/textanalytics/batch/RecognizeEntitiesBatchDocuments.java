// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.batch;

import com.azure.ai.textanalytics.TextAnalyticsClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.RecognizeEntitiesOptions;
import com.azure.ai.textanalytics.models.RecognizeEntitiesResult;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.util.RecognizeEntitiesResultCollection;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Sample demonstrates how to recognize the entities of {@link TextDocumentInput} documents.
 */
public class RecognizeEntitiesBatchDocuments {
    /**
     * Main method to invoke this demo about how to recognize the entities of {@link TextDocumentInput} documents.
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
        List<TextDocumentInput> documents = Arrays.asList(
            new TextDocumentInput("A", "Satya Nadella is the CEO of Microsoft.").setLanguage("en"),
            new TextDocumentInput("B", "Elon Musk is the CEO of SpaceX and Tesla.").setLanguage("en")
        );

        RecognizeEntitiesOptions requestOptions = new RecognizeEntitiesOptions().setIncludeStatistics(true).setModelVersion("latest");

        // Recognizing entities for each document in a batch of documents
        Response<RecognizeEntitiesResultCollection> entitiesBatchResultResponse =
            client.recognizeEntitiesBatchWithResponse(documents, requestOptions, Context.NONE);

        // Response's status code
        System.out.printf("Status code of request response: %d%n", entitiesBatchResultResponse.getStatusCode());
        RecognizeEntitiesResultCollection recognizeEntitiesResultCollection = entitiesBatchResultResponse.getValue();

        // Model version
        System.out.printf("Results of Azure Text Analytics \"Entities Recognition\" Model, version: %s%n", recognizeEntitiesResultCollection.getModelVersion());

        // Batch statistics
        TextDocumentBatchStatistics batchStatistics = recognizeEntitiesResultCollection.getStatistics();
        System.out.printf("Documents statistics: document count = %s, erroneous document count = %s, transaction count = %s, valid document count = %s.%n",
            batchStatistics.getDocumentCount(), batchStatistics.getInvalidDocumentCount(), batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

        // Recognized entities for each document in a batch of documents
        AtomicInteger counter = new AtomicInteger();
        for (RecognizeEntitiesResult entitiesResult : recognizeEntitiesResultCollection) {
            // Recognized entities for each document in a batch of documents
            System.out.printf("%n%s%n", documents.get(counter.getAndIncrement()));
            if (entitiesResult.isError()) {
                // Erroneous document
                System.out.printf("Cannot recognize entities. Error: %s%n", entitiesResult.getError().getMessage());
            } else {
                // Valid document
                entitiesResult.getEntities().forEach(entity -> System.out.printf(
                    "Recognized entity: %s, entity category: %s, entity subcategory: %s, confidence score: %f.%n",
                    entity.getText(), entity.getCategory(), entity.getSubcategory(), entity.getConfidenceScore())
                );
            }
        }
    }
}
