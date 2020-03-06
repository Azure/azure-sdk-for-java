// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.batch;

import com.azure.ai.textanalytics.TextAnalyticsClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.TextAnalyticsApiKeyCredential;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.core.util.Context;

import java.util.Arrays;
import java.util.List;

/**
 * Sample demonstrates how to extract the key phrases of documents.
 */
public class ExtractKeyPhrasesBatchDocuments {
    /**
     * Main method to invoke this demo about how to extract the key phrases of documents.
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
        List<TextDocumentInput> inputs = Arrays.asList(
            new TextDocumentInput("1", "The food was delicious and there were wonderful staff.", "en"),
            new TextDocumentInput("2", "The pitot tube is used to measure airspeed.", "en")
        );

        // Extracting batch key phrases
        client.extractKeyPhrasesBatch(inputs, null, Context.NONE).forEach(extractKeyPhraseResult -> {
            // Extracted key phrase for each of documents
            System.out.printf("%nDocument ID: %s%n", extractKeyPhraseResult.getId());
            if (extractKeyPhraseResult.isError()) {
                // Erroneous document
                System.out.printf("Cannot extract key phrases. Error: %s%n", extractKeyPhraseResult.getError().getMessage());
            } else {
                // Valid document
                System.out.println("Extracted phrases:");
                extractKeyPhraseResult.getKeyPhrases().forEach(keyPhrases -> System.out.printf("%s.%n", keyPhrases));
            }
        });
    }
}
