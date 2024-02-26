// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.usage;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.SpeechGenerationOptions;
import com.azure.ai.openai.models.SpeechVoice;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A sample demonstrates how to generate speech from a given text, text-to-speech.
 */
public class TextToSpeechSample {
    /**
     * Runs the sample algorithm and demonstrates how to generate speech from a given text, text-to-speech.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) throws IOException {
        String azureOpenaiKey =  Configuration.getGlobalConfiguration().get("AZURE_OPENAI_KEY");
        String endpoint = Configuration.getGlobalConfiguration().get("AZURE_OPENAI_ENDPOINT");
        String deploymentOrModelId = "tts";

        OpenAIClient client = new OpenAIClientBuilder()
                .endpoint(endpoint)
                .credential(new AzureKeyCredential(azureOpenaiKey))
                .buildClient();

        SpeechGenerationOptions options = new SpeechGenerationOptions(
                "Today is a wonderful day to build something people love!",
                SpeechVoice.ALLOY);

        BinaryData speech = client.generateSpeechFromText(deploymentOrModelId, options);
        // Checkout your generated speech in the file system.
        Path path = Paths.get("./azure-ai-openai/src/samples/java/com/azure/ai/openai/resources/speech.wav");
        Files.write(path, speech.toBytes());
    }
}
