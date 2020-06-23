// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.batch;

import com.azure.ai.textanalytics.TextAnalyticsClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.ExtractKeyPhraseResult;
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
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .buildClient();

        // The texts that need be analyzed.
        List<String> documents = Arrays.asList(
            "The food was delicious and there were wonderful staff.",
            "The pitot tube is used to measure airspeed."
        );

        // Extracting key phrases for each document in a batch of documents
        AtomicInteger counter = new AtomicInteger();
        for (ExtractKeyPhraseResult extractKeyPhraseResult : client.extractKeyPhrasesBatch(documents, "en")) {
            // Extracted key phrase for each document in a batch of documents
            System.out.printf("%nText = %s%n", documents.get(counter.getAndIncrement()));
            if (extractKeyPhraseResult.isError()) {
                // Erroneous document
                System.out.printf("Cannot extract key phrases. Error: %s%n", extractKeyPhraseResult.getError().getMessage());
            } else {
                // Valid document
                System.out.println("Extracted phrases:");
                extractKeyPhraseResult.getKeyPhrases().forEach(keyPhrases -> System.out.printf("\t%s.%n", keyPhrases));
            }
        }
    }
}
