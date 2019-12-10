// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;


import com.azure.ai.textanalytics.models.DetectedLanguage;

public class HelloWorldAsync {

    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.
        TextAnalyticsAsyncClient client = new TextAnalyticsClientBuilder()
            .subscriptionKey("subscription-key")
            .endpoint("https://servicename.cognitiveservices.azure.com/")
            .buildAsyncClient();

        // The text that need be analysed.
        String text = "hello world";

        client.detectLanguage(text).subscribe(
            result -> {
                final DetectedLanguage primaryLanguage = result.getPrimaryLanguage();
                System.out.printf("Detected Language: %s, ISO 6391 Name: %s, Score: %s",
                    primaryLanguage.getName(), primaryLanguage.getIso6391Name(), primaryLanguage.getScore());
            },
            error -> System.err.println("There was an error detecting language of the text" + error),
            () -> System.out.println("Language detected."));
    }
}
