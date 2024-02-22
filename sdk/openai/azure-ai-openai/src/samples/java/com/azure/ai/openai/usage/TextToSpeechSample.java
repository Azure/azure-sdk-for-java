// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.usage;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.SpeechGenerationOptions;
import com.azure.ai.openai.models.SpeechVoice;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;

/**
 * A sample demonstrates how to generate speech from a given text, text-to-speech.
 */
public class TextToSpeechSample {
    /**
     * Runs the sample algorithm and demonstrates how to generate speech from a given text, text-to-speech.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) {
        String azureOpenaiKey = "{azure-open-ai-key}";
        String endpoint = "{azure-open-ai-endpoint}";
        String deploymentOrModelId = "{azure-open-ai-deployment-model-id}";

        OpenAIClient client = new OpenAIClientBuilder()
                .endpoint(endpoint)
                .credential(new AzureKeyCredential(azureOpenaiKey))
                .buildClient();

        SpeechGenerationOptions options = new SpeechGenerationOptions(
                "Today is a wonderful day to build something people love!",
                SpeechVoice.ALLOY);

        BinaryData speech = client.generateSpeechFromText(deploymentOrModelId, options);

        System.out.println("Speech: " + speech);
    }
}
