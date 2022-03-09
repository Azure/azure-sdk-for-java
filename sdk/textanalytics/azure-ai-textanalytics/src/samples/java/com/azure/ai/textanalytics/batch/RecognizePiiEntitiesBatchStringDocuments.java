// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.batch;

import com.azure.ai.textanalytics.TextAnalyticsClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.PiiEntityCategory;
import com.azure.ai.textanalytics.models.PiiEntityCollection;
import com.azure.ai.textanalytics.models.RecognizePiiEntitiesResult;
import com.azure.ai.textanalytics.models.RecognizePiiEntitiesOptions;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.util.RecognizePiiEntitiesResultCollection;
import com.azure.core.credential.AzureKeyCredential;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Sample demonstrates how to recognize the Personally Identifiable Information(PII) entities of {@code String} documents.
 */
public class RecognizePiiEntitiesBatchStringDocuments {
    /**
     * Main method to invoke this demo about how to recognize the Personally Identifiable Information entities of
     * documents.
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
        List<String> documents = Arrays.asList(
            "My name is Joe and SSN is 859-98-0987",
            "Visa card 4111 1111 1111 1111"
        );

        // Show statistics, model version, and PII entities that only related to the given Pii entity categories.
        RecognizePiiEntitiesOptions options = new RecognizePiiEntitiesOptions()
                                                  .setCategoriesFilter(
                                                      PiiEntityCategory.US_SOCIAL_SECURITY_NUMBER,
                                                      PiiEntityCategory.CREDIT_CARD_NUMBER)
                                                  .setIncludeStatistics(true)
                                                  .setModelVersion("latest");

        // Recognizing Personally Identifiable Information entities for each document in a batch of documents
        RecognizePiiEntitiesResultCollection recognizePiiEntitiesResultCollection = client.recognizePiiEntitiesBatch(documents, "en", options);

        // Model version
        System.out.printf("Results of Azure Text Analytics \"Personally Identifiable Information Entities Recognition\" Model, version: %s%n", recognizePiiEntitiesResultCollection.getModelVersion());

        // Batch statistics
        TextDocumentBatchStatistics batchStatistics = recognizePiiEntitiesResultCollection.getStatistics();
        System.out.printf("Documents statistics: document count = %s, erroneous document count = %s, transaction count = %s, valid document count = %s.%n",
            batchStatistics.getDocumentCount(), batchStatistics.getInvalidDocumentCount(), batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

        // Recognized Personally Identifiable Information entities for each document in a batch of documents
        AtomicInteger counter = new AtomicInteger();
        for (RecognizePiiEntitiesResult entitiesResult : recognizePiiEntitiesResultCollection) {
            // Recognized entities for each of documents from a batch of documents
            System.out.printf("%nText = %s%n", documents.get(counter.getAndIncrement()));
            if (entitiesResult.isError()) {
                // Erroneous document
                System.out.printf("Cannot recognize Personally Identifiable Information entities. Error: %s%n", entitiesResult.getError().getMessage());
            } else {
                // Valid document
                PiiEntityCollection piiEntityCollection = entitiesResult.getEntities();
                System.out.printf("Redacted Text: %s%n", piiEntityCollection.getRedactedText());
                piiEntityCollection.forEach(entity -> System.out.printf(
                    "Recognized Personally Identifiable Information entity: %s, entity category: %s, entity subcategory: %s, offset: %s, confidence score: %f.%n",
                    entity.getText(), entity.getCategory(), entity.getSubcategory(), entity.getOffset(), entity.getConfidenceScore()));
            }
        }
    }
}
