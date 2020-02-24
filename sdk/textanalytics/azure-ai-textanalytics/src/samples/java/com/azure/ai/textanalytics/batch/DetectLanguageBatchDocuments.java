// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.batch;

import com.azure.ai.textanalytics.TextAnalyticsClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.DetectLanguageInput;
import com.azure.ai.textanalytics.models.DetectedLanguage;
import com.azure.ai.textanalytics.models.TextAnalyticsApiKeyCredential;
import com.azure.core.util.Context;

import java.util.Arrays;
import java.util.List;

/**
 * Sample demonstrates how to detect the languages of a batch input text.
 */
public class DetectLanguageBatchDocuments {
    /**
     * Main method to invoke this demo about how to detect the languages of a batch input text.
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
        List<DetectLanguageInput> inputs = Arrays.asList(
            new DetectLanguageInput("1", "This is written in English.", "US"),
            new DetectLanguageInput("2", "Este es un document escrito en Español.", "ES")
        );

        // Detecting batch languages
        client.detectLanguageBatch(inputs, null, Context.NONE).forEach(detectLanguageResult -> {
            // Detected languages for a document from a batch of documents
            System.out.printf("%nDocument ID: %s%n", detectLanguageResult.getId());

            if (detectLanguageResult.isError()) {
                // Erroneous document
                System.out.printf("Cannot detect language. Error: %s%n", detectLanguageResult.getError().getMessage());
            } else {
                // Valid document
                final DetectedLanguage detectedPrimaryLanguage = detectLanguageResult.getPrimaryLanguage();
                System.out.printf("Detected primary language: %s, ISO 6391 name: %s, score: %.2f.%n",
                    detectedPrimaryLanguage.getName(),
                    detectedPrimaryLanguage.getIso6391Name(),
                    detectedPrimaryLanguage.getScore());
            }
        });
    }
}
