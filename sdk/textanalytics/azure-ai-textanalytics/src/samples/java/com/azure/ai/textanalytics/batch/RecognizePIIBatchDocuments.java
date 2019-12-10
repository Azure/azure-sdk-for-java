// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.batch;

import com.azure.ai.textanalytics.TextAnalyticsClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.DocumentResultCollection;
import com.azure.ai.textanalytics.models.NamedEntityResult;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextBatchStatistics;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.core.util.Context;

import java.util.Arrays;
import java.util.List;

public class RecognizePIIBatchDocuments {

    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
            .subscriptionKey("subscription-key")
            .endpoint("https://servicename.cognitiveservices.azure.com/")
            .buildClient();

        // The texts that need be analysed.
        List<TextDocumentInput> inputs = Arrays.asList(
            new TextDocumentInput("1", "My SSN is 555-55-5555", "US"),
            new TextDocumentInput("2", "Visa card 4147999933330000", "US")
        );

        final TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions().setShowStatistics(true).setModelVersion("1.0");
        final DocumentResultCollection<NamedEntityResult> detectedBatchResult = client.recognizeBatchPiiEntitiesWithResponse(inputs, requestOptions, Context.NONE).getValue();
        System.out.printf("Model version: %s", detectedBatchResult.getModelVersion());

        final TextBatchStatistics batchStatistics = detectedBatchResult.getStatistics();
        System.out.printf("A batch of document statistics, document count: %s, erroneous document count: %s, transaction count: %s, valid document count: %s",
            batchStatistics.getDocumentCount(),
            batchStatistics.getErroneousDocumentCount(),
            batchStatistics.getTransactionCount(),
            batchStatistics.getValidDocumentCount());


        // Detecting pii entities from a batch of documents
        detectedBatchResult.stream().forEach(piiEntityDocumentResult ->
            piiEntityDocumentResult.getNamedEntities().forEach(entity ->
                System.out.printf("Recognized Personal Identifiable Info NamedEntity: %s, NamedEntity Type: %s, NamedEntity Subtype: %s, Offset: %s, Length: %s, Score: %s",
                    entity.getText(),
                    entity.getType(),
                    entity.getSubtype(),
                    entity.getOffset(),
                    entity.getLength(),
                    entity.getScore())));
    }

}
