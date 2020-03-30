// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.batch;

import com.azure.ai.textanalytics.TextAnalyticsClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.core.credential.AzureKeyCredential;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Sample demonstrates how to extract the key phrases of {@code String} documents.
 */
public class ExtractKeyPhrasesBatchStringDocuments {
    /**
     * Main method to invoke this demo about how to extract the key phrases of {@code String} documents.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
            .apiKey(new AzureKeyCredential("{api_key}"))
            .endpoint("{endpoint}")
            .buildClient();

        // The texts that need be analyzed.
        List<String> inputs = Arrays.asList(
            "The food was delicious and there were wonderful staff.",
            "The pitot tube is used to measure airspeed."
        );

        // Extracting batch key phrases
        AtomicInteger counter = new AtomicInteger();
        client.extractKeyPhrasesBatch(inputs).forEach(extractKeyPhraseResult -> {
            // Extracted key phrase for each of documents from a batch of documents
            System.out.printf("%nDocument: %s%n", inputs.get(counter.getAndIncrement()));

            if (extractKeyPhraseResult.isError()) {
                // Erroneous document
                System.out.printf("Cannot extract key phrases. Error: %s%n", extractKeyPhraseResult.getError().getMessage());
                return;
            }
            // Valid document
            System.out.println("Extracted phrases:");
            extractKeyPhraseResult.getKeyPhrases().forEach(keyPhrases -> System.out.printf("%s.%n", keyPhrases));
        });
    }
}
