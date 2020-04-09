// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.CustomFormModelInfo;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Context;

/**
 * Sample for listing all available models in a synchronous way.
 */
public class ListModels {
    /**
     * Sample for extracting receipt information using file source URL.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) {
        FormRecognizerClient client = new FormRecognizerClientBuilder()
            .apiKey(new AzureKeyCredential("48c9ec5b1c444c899770946defc486c4"))
            .endpoint("https://javaformrecognizertestresource.cognitiveservices.azure.com/")
            .buildClient();

        for (CustomFormModelInfo modelInfo : client.listModels(Context.NONE)) {
            System.out.printf("Model ID = %s, model status = %s, created on = %s, last updated on = %s.%n",
                modelInfo.getModelId(),
                modelInfo.getStatus(),
                modelInfo.getCreatedOn(),
                modelInfo.getLastUpdatedOn());
        }
    }
}
