// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.CustomFormModelInfo;
import com.azure.core.credential.AzureKeyCredential;

/**
 * Sample for listing all available models in a synchronous way.
 */
public class ListModels {
    /**
     * Sample for listing information for all models using file source URL.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) {
        FormRecognizerClient client = new FormRecognizerClientBuilder()
            .apiKey(new AzureKeyCredential("{api_key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildClient();

        for (CustomFormModelInfo modelInfo : client.listModels()) {
            System.out.printf("Model ID = %s, model status = %s, created on = %s, last updated on = %s.%n",
                modelInfo.getModelId(),
                modelInfo.getStatus(),
                modelInfo.getCreatedOn(),
                modelInfo.getLastUpdatedOn());
        }
    }
}
