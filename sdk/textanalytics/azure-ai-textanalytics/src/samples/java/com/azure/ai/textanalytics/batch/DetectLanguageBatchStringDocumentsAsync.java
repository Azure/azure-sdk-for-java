// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.batch;

import com.azure.ai.textanalytics.TextAnalyticsAsyncClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.DetectedLanguage;
import com.azure.core.credential.AzureKeyCredential;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Sample demonstrates how to asynchronously detect the languages of {@code String} documents.
 */
public class DetectLanguageBatchStringDocumentsAsync {
    /**
     * Main method to invoke this demo about how to detect the languages of {@code String} documents.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.
        TextAnalyticsAsyncClient client = new TextAnalyticsClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .buildAsyncClient();

        // The texts that need be analyzed.
        List<String> documents = Arrays.asList(
            "This is written in English.",
            "Este es un documento  escrito en Español."
        );

        // Detecting language for each document in a batch of documents
        AtomicInteger counter = new AtomicInteger();
        client.detectLanguageBatch(documents, "US").subscribe(
            detectLanguageResult -> {
                // Detected language for each document
                System.out.printf("%nText = %s%n", documents.get(counter.getAndIncrement()));
                if (detectLanguageResult.isError()) {
                    // Erroneous document
                    System.out.printf("Cannot detect language. Error: %s%n", detectLanguageResult.getError().getMessage());
                } else {
                    // Valid document
                    DetectedLanguage language = detectLanguageResult.getPrimaryLanguage();
                    System.out.printf("Detected primary language: %s, ISO 6391 name: %s, score: %f.%n",
                        language.getName(), language.getIso6391Name(), language.getScore());
                }
            },
            error -> System.err.println("There was an error detecting language of the documents." + error),
            () -> System.out.println("Batch of language detected."));

        // The .subscribe() creation and assignment is not a blocking call. For the purpose of this example, we sleep
        // the thread so the program does not end before the send operation is complete. Using .block() instead of
        // .subscribe() will turn this into a synchronous call.
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException ignored) {
        }
    }
}
