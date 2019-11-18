// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics.batch;

import com.azure.core.util.Context;
import com.azure.cs.textanalytics.TextAnalyticsClient;
import com.azure.cs.textanalytics.TextAnalyticsClientBuilder;
import com.azure.cs.textanalytics.implementation.models.DocumentKeyPhrases;
import com.azure.cs.textanalytics.implementation.models.KeyPhraseResult;
import com.azure.cs.textanalytics.models.MultiLanguageBatchInput;
import com.azure.cs.textanalytics.models.DocumentInput;

import java.util.ArrayList;
import java.util.List;

public class RecognizeKeyPhrasesBatchDocuments {

    public static void main(String[] args) {

        // TODO: user AAD token to do the authentication
        // Instantiate a client that will be used to call the service.
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
            .buildClient();

        // The texts that need be analysed.
        List<DocumentInput> documents = new ArrayList<>();
        DocumentInput input = new DocumentInput();
        input.setId("1").setText("My cat might need to see a veterinarian").setLanguage("US");
        DocumentInput input2 = new DocumentInput();
        input2.setId("2").setText("The pitot tube is used to measure airspeed.").setLanguage("US");
        documents.add(input);
        documents.add(input2);
        MultiLanguageBatchInput batchInput = new MultiLanguageBatchInput();
        batchInput.setDocuments(documents);


        // Detecting language from a batch of documents
        KeyPhraseResult detectedResult = client.extractKeyPhrasesWithResponse(batchInput, false, Context.NONE).getValue();
        List<DocumentKeyPhrases> documentKeyPhrasesList = detectedResult.getDocuments();
        for (DocumentKeyPhrases documentKeyPhrases : documentKeyPhrasesList) {
            List<String> phrases = documentKeyPhrases.getKeyPhrases();
            for (String phrase : phrases) {
                System.out.println(String.format("Recognized Phrases: %s", phrase));
            }
        }
    }
}
