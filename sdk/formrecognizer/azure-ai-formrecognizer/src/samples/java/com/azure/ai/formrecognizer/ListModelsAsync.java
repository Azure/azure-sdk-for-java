// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.core.credential.AzureKeyCredential;

import java.util.concurrent.TimeUnit;

/**
 * Sample for listing all available models in an asynchronous way.
 */
public class ListModelsAsync {
    /**
     * Sample for extracting receipt information using file source URL.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) {
        FormRecognizerAsyncClient client = new FormRecognizerClientBuilder()
            .apiKey(new AzureKeyCredential("48c9ec5b1c444c899770946defc486c4"))
            .endpoint("https://javaformrecognizertestresource.cognitiveservices.azure.com/")
            .buildAsyncClient();

        client.listModels().subscribe(result -> {
                System.out.printf("Model ID = %s, model status = %s, created on = %s, last updated on = %s.%n",
                    result.getModelId(),
                    result.getStatus(),
                    result.getCreatedOn(),
                    result.getLastUpdatedOn());
            },
            error -> System.err.printf(String.format("There was an error list the models, %s.", error)),
            () -> System.out.println("A list of models ."));

        // The .subscribe() creation and assignment is not a blocking call. For the purpose of this example, we sleep
        // the thread so the program does not end before the send operation is complete. Using .block() instead of
        // .subscribe() will turn this into a synchronous call.
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException ignored) {
        }
    }
}
