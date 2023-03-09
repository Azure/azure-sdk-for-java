// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.texttranslator;

import com.azure.ai.texttranslator.authentication;
import com.azure.core.credential.AzureKeyCredential;

public final class ReadmeSamples {
    /**
     * Sample for creating text translator client without any authentication.
     * Note: this will only work with GET languages operation.
     */
    public void createClientAnonymous() {
        TranslatorClient client = new TranslatorClientBuilder()
            .endpoint("https://api.cognitive.microsofttranslator.com")
            .buildClient();
    }

    /**
     * Sample for creating text translator client using Text Translator API Key and Region.
     */
    public void createClient() {
        String apiKey = "<text-translator-api-key>";
        String region = "<text-translator-region>";
        AzureRegionalKeyCredential regionalCredential = new AzureRegionalKeyCredential(new AzureKeyCredential(apiKey), region);

        TranslatorClient client = new TranslatorClientBuilder()
                .credential(regionalCredential)
                .endpoint("https://api.cognitive.microsofttranslator.com")
                .buildClient();
    }

    /**
     * Sample for creating text translator client using Custom endpoint and Text Translator API Key.
     */
    public void createClientUsingCustomEndpoint() {
        String apiKey = "<text-translator-api-key>";
        String endpoint = String.format("https://%s.cognitiveservices.azure.com/", "<text-translator-resource-name>");

        CustomEndpoint customEndpoint = new CustomEndpoint(endpoint);
        AzureKeyCredential credential = new AzureKeyCredential(apiKey);

        TranslatorClient client = new TranslatorClientBuilder()
                .credential(credential)
                .endpoint(customEndpoint)
                .buildClient();
    }
}
