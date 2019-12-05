// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics;


import com.azure.cs.textanalytics.models.DetectedLanguage;

public class HelloWorldAsync {

    public static void main(String[] args) {
        // TODO: user AAD token to do the authentication
        // Instantiate a client that will be used to call the service.
        TextAnalyticsAsyncClient client = new TextAnalyticsClientBuilder()
            .buildAsyncClient();

        // The text that need be analysed.
        String text = "hello world";

        client.detectLanguage(text, "US").subscribe(
            result -> {
                final DetectedLanguage primaryLanguage = result.getPrimaryLanguage();
                System.out.printf("Detected Language: %s, ISO 6391 Name: %s, Score: %s",
                    primaryLanguage.getName(), primaryLanguage.getIso6391Name(), primaryLanguage.getScore());
            },
            error -> System.err.println("There was an error detecting language of the text" + error),
            () -> System.out.println("Language detected."));
    }
}
