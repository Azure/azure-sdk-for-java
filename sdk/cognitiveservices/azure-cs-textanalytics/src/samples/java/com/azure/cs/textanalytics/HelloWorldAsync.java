// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics;


public class HelloWorldAsync {

    public static void main(String[] args) {
        // TODO: user AAD token to do the authentication
        // Instantiate a client that will be used to call the service.
        TextAnalyticsAsyncClient client = new TextAnalyticsClientBuilder()
            .buildAsyncClient();

        // The text that need be analysed.
        String text = "hello world";

        client.detectLanguages(text, "US", false).subscribe(
            result -> System.out.println(String.format("Detected Language: %s, ISO 6391 Name: %s, Score: %s",
                result.getName(), result.getIso6391Name(), result.getScore())),
            error -> System.err.println("There was an error detecting language of the text" + error.toString()),
            () -> System.out.println("Language detected."));
    }
}
