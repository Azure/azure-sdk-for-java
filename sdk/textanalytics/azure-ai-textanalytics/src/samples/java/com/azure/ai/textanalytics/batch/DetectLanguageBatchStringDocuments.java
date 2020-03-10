// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.batch;

import com.azure.ai.textanalytics.TextAnalyticsClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.DetectedLanguage;
import com.azure.ai.textanalytics.models.TextAnalyticsApiKeyCredential;

import java.util.Arrays;
import java.util.List;

/**
 * Sample demonstrates how to detect the languages of documents.
 */
public class DetectLanguageBatchStringDocuments {
    /**
     * Main method to invoke this demo about how to detect the languages of documents.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
            .apiKey(new TextAnalyticsApiKeyCredential("{api_key}"))
            .endpoint("{endpoint}")
            .buildClient();

        // The texts that need be analyzed.
        List<String> inputs = Arrays.asList(
            "This is written in English.",
            "Este es un documento  escrito en Español."
        );

        // Detecting batch languages
        client.detectLanguageBatch(inputs).forEach(detectLanguageResult -> {
            // Detected languages for a document from a batch of documents
            System.out.printf("%nDocument ID: %s%n", detectLanguageResult.getId());
            System.out.printf("Document: %s%n", inputs.get(Integer.parseInt(detectLanguageResult.getId())));
            if (detectLanguageResult.isError()) {
                // Erroneous document
                System.out.printf("Cannot detect language. Error: %s%n", detectLanguageResult.getError().getMessage());
                return;
            }
            // Valid document
            final DetectedLanguage language = detectLanguageResult.getPrimaryLanguage();
            System.out.printf("Detected primary language: %s, ISO 6391 name: %s, score: %f.%n",
                language.getName(), language.getIso6391Name(), language.getScore());
        });
    }
}
