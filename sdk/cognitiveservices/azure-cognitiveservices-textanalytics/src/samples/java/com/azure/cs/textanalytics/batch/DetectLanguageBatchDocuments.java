// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics.batch;

import com.azure.cs.textanalytics.TextAnalyticsClient;
import com.azure.cs.textanalytics.TextAnalyticsClientBuilder;
import textanalytics.models.DetectedLanguage;
import textanalytics.models.DocumentLanguage;
import textanalytics.models.LanguageBatchInput;
import textanalytics.models.LanguageInput;
import textanalytics.models.LanguageResult;

import java.util.ArrayList;
import java.util.List;

public class DetectLanguageBatchDocuments {

    public static void main(String[] args) {
        // The connection string value can be obtained by going to your Text Analytics instance in the Azure portal
        // and navigating to "Access Keys" page under the "Settings" section.
        String connectionString = "endpoint={endpoint_value};id={id_value};name={secret_value}";

        // Instantiate a client that will be used to call the service.
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
            .connectionString(connectionString)
            .buildClient();

        // The texts that need be analysed.
        List<LanguageInput> documents = new ArrayList<>();
        LanguageInput input = new LanguageInput();
        input.setId("1").setText("This is written in English").setCountryHint("US");
        LanguageInput input2 = new LanguageInput();
        input2.setId("2").setText("Este es un document escrito en Español.").setCountryHint("es");
        documents.add(input);
        documents.add(input2);
        LanguageBatchInput batchInput = new LanguageBatchInput();
        batchInput.setDocuments(documents);


        // Detecting language from a batch of documents
        LanguageResult detectedResult = client.detectLanguageBatch(batchInput, false);
        List<DocumentLanguage> documentLanguages = detectedResult.getDocuments();
        for (DocumentLanguage documentLanguage : documentLanguages) {
            List<DetectedLanguage> detectedLanguages = documentLanguage.getDetectedLanguage();
            for (DetectedLanguage detectedLanguage : detectedLanguages) {
                System.out.println(String.format("Detected Language: %s, ISO 6391 Name: %s, Score: %s",
                    detectedLanguage.getName(), detectedLanguage.getIso6391Name(), detectedLanguage.getScore()));
            }
        }
    }

}
