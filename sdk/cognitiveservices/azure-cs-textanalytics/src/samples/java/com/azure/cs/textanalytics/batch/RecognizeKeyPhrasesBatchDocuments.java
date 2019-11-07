// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics.batch;

import com.azure.core.util.Context;
import com.azure.cs.textanalytics.TextAnalyticsClient;
import com.azure.cs.textanalytics.TextAnalyticsClientBuilder;
import textanalytics.models.DocumentKeyPhrases;
import textanalytics.models.KeyPhraseResult;
import textanalytics.models.MultiLanguageBatchInput;
import textanalytics.models.MultiLanguageInput;

import java.util.ArrayList;
import java.util.List;

public class RecognizeKeyPhrasesBatchDocuments {

    public static void main(String[] args) {
        // The connection string value can be obtained by going to your Text Analytics instance in the Azure portal
        // and navigating to "Access Keys" page under the "Settings" section.
        String connectionString = "endpoint={endpoint_value};id={id_value};name={secret_value}";

        // Instantiate a client that will be used to call the service.
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
            .connectionString(connectionString)
            .buildClient();

        // The texts that need be analysed.
        List<MultiLanguageInput> documents = new ArrayList<>();
        MultiLanguageInput input = new MultiLanguageInput();
        input.setId("1").setText("My cat might need to see a veterinarian").setLanguage("US");
        MultiLanguageInput input2 = new MultiLanguageInput();
        input2.setId("2").setText("The pitot tube is used to measure airspeed.").setLanguage("US");
        documents.add(input);
        documents.add(input2);
        MultiLanguageBatchInput batchInput = new MultiLanguageBatchInput();
        batchInput.setDocuments(documents);


        // Detecting language from a batch of documents
        KeyPhraseResult detectedResult = client.detectKeyPhrasesBatchWithResponse(batchInput, false, Context.NONE).getValue();
        List<DocumentKeyPhrases> documentKeyPhrasesList = detectedResult.getDocuments();
        for (DocumentKeyPhrases documentKeyPhrases : documentKeyPhrasesList) {
            List<String> phrases = documentKeyPhrases.getKeyPhrases();
            for (String phrase : phrases) {
                System.out.println(String.format("Recognized Phrases: %s", phrase));
            }
        }
    }
}
