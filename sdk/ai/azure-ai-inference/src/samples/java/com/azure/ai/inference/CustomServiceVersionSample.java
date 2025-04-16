// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.inference;

import com.azure.ai.inference.models.ChatCompletions;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.util.Configuration;

/**
 * Sample for {@link ChatCompletionsClient} to demonstrate how to set a custom service version.
 */
public class CustomServiceVersionSample {

    /**
     * Main method to invoke this sample
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        String key = Configuration.getGlobalConfiguration().get("AZURE_API_KEY");
        String endpoint = Configuration.getGlobalConfiguration().get("MODEL_ENDPOINT");

        ChatCompletionsClient client = new ChatCompletionsClientBuilder()
            .credential(new AzureKeyCredential(key))
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .addPolicy(new ServiceVersionPolicy("2025-04-16-preview")) // set to the desired service version
            .endpoint(endpoint)
            .buildClient();

        String prompt = "Tell me 3 jokes about trains";

        ChatCompletions completions = client.complete(prompt);

        System.out.printf("%s.%n", completions.getChoice().getMessage().getContent());
    }
}
