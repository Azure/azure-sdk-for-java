// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics.batch;

import com.azure.cs.textanalytics.TextAnalyticsClient;
import com.azure.cs.textanalytics.TextAnalyticsClientBuilder;
import com.azure.cs.textanalytics.models.DocumentBatchStatistics;
import com.azure.cs.textanalytics.models.DocumentInput;
import com.azure.cs.textanalytics.models.DocumentResult;
import com.azure.cs.textanalytics.models.DocumentResultCollection;
import com.azure.cs.textanalytics.models.DocumentStatistics;
import com.azure.cs.textanalytics.models.TextAnalyticsRequestOptions;

import java.util.ArrayList;
import java.util.List;

public class RecognizeKeyPhrasesBatchDocuments {

    public static void main(String[] args) {

        // TODO: user AAD token to do the authentication
        // Instantiate a client that will be used to call the service.
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
            .buildClient();

        // The texts that need be analysed.
        List<DocumentInput> document = new ArrayList<>();
        DocumentInput input = new DocumentInput();
        input.setId("1").setText("My cat might need to see a veterinarian").setLanguage("US");
        DocumentInput input2 = new DocumentInput();
        input2.setId("2").setText("The pitot tube is used to measure airspeed.").setLanguage("US");
        document.add(input);
        document.add(input2);

        TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions().setShowStats(true).setModelVersion("1.0");
        DocumentResultCollection<String> detectedResult = client.extractKeyPhrases(document, requestOptions);
        // Document batch level statistics
        final String modelVersion = detectedResult.getModelVersion();
        System.out.println(String.format("Model version: %s", modelVersion));

        final DocumentBatchStatistics documentBatchStatistics = detectedResult.getStatistics();
        System.out.println(String.format("A batch of document statistics, document count: %s, erroneous document count: %s, transaction count: %s, valid document count: %s",
            documentBatchStatistics.getDocumentsCount(),
            documentBatchStatistics.getErroneousDocumentsCount(),
            documentBatchStatistics.getTransactionsCount(),
            documentBatchStatistics.getValidDocumentsCount()));

        // Detecting key phrase for each of document from a batch of documents
        for (DocumentResult<String> keyPhraseList : detectedResult) {
            final DocumentStatistics documentStatistics = keyPhraseList.getDocumentStatistics();
            System.out.println(String.format("One key phrase document statistics, character count: %s, transaction count: %s.",
                documentStatistics.getCharactersCount(), documentStatistics.getTransactionsCount()));
            final List<String> keyPhrases = keyPhraseList.getItems();
            for (String phrase : keyPhrases) {
                System.out.println(String.format("Recognized Phrases: %s", phrase));
            }
        }
    }
}
