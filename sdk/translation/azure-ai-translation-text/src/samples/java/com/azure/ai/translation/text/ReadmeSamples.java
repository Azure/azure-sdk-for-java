// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.text;

import com.azure.core.credential.AzureKeyCredential;

public final class ReadmeSamples {
    /**
     * Sample for creating text translator client without any authentication.
     * Note: this will only work with GET languages operation.
     */
    public void createClientAnonymous() {
        TextTranslationClient client = new TextTranslationClientBuilder()
            .endpoint("https://api.cognitive.microsofttranslator.com")
            .buildClient();
    }

    /**
     * Sample for creating text translator client using Text Translator API Key and Region.
     */
    public void createClient() {
        String apiKey = System.getenv("TEXT_TRANSLATOR_API_KEY");
        String region = System.getenv("TEXT_TRANSLATOR_API_REGION");
        AzureKeyCredential credential = new AzureKeyCredential(apiKey);

        TextTranslationClient client = new TextTranslationClientBuilder()
                .credential(credential)
                .region(region)
                .endpoint("https://api.cognitive.microsofttranslator.com")
                .buildClient();
    }

    /**
     * Sample for creating text translator client using Custom endpoint and Text Translator API Key.
     */
    public void createClientUsingCustomEndpoint() {
        String apiKey = "<text-translator-api-key>";
        String endpoint = String.format("https://%s.cognitiveservices.azure.com/", "<text-translator-resource-name>");

        AzureKeyCredential credential = new AzureKeyCredential(apiKey);

        TextTranslationClient client = new TextTranslationClientBuilder()
                .credential(credential)
                .endpoint(endpoint)
                .buildClient();
    }
}
