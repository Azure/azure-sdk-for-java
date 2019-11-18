// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics.batch;

import com.azure.cs.textanalytics.TextAnalyticsClient;
import com.azure.cs.textanalytics.TextAnalyticsClientBuilder;
import com.azure.cs.textanalytics.models.DetectedLanguage;
import com.azure.cs.textanalytics.models.DocumentBatchStatistics;
import com.azure.cs.textanalytics.models.DocumentResultCollection;
import com.azure.cs.textanalytics.models.LanguageInput;
import com.azure.cs.textanalytics.models.TextAnalyticsRequestOptions;

import java.util.ArrayList;
import java.util.List;

public class DetectLanguageBatchDocuments {

    public static void main(String[] args) {
        // TODO: user AAD token to do the authentication
        // Instantiate a client that will be used to call the service.
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
            .buildClient();

        // The texts that need be analysed.
        List<LanguageInput> document = new ArrayList<>();
        LanguageInput input = new LanguageInput();
        input.setId("1").setText("This is written in English").setCountryHint("US");
        LanguageInput input2 = new LanguageInput();
        input2.setId("2").setText("Este es un document escrito en Español.").setCountryHint("es");
        document.add(input);
        document.add(input2);

        TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions().setShowStats(true).setModelVersion("1.0");
        DocumentResultCollection<DetectedLanguage> detectedResult = client.detectLanguages(document, requestOptions);
        // Document level statistics
        final String modelVersion = detectedResult.getModelVersion();
        System.out.println(String.format("Model version: %s", modelVersion));

        final DocumentBatchStatistics documentBatchStatistics = detectedResult.getStatistics();
        System.out.println(String.format("A batch of document statistics, document count: %s, erroneous document count: %s, transaction count: %s, valid document count: %s",
            documentBatchStatistics.getDocumentsCount(),
            documentBatchStatistics.getErroneousDocumentsCount(),
            documentBatchStatistics.getTransactionsCount(),
            documentBatchStatistics.getValidDocumentsCount()));

        // Detecting language from a batch of documents
        List<DetectedLanguage> documentLanguages = detectedResult.getItems();
        for (DetectedLanguage detectedLanguage : documentLanguages) {
                System.out.println(String.format("Detected Language: %s, ISO 6391 Name: %s, Score: %s",
                    detectedLanguage.getName(), detectedLanguage.getIso6391Name(), detectedLanguage.getScore()));
        }
    }

}
