// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.usage;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.SpeechGenerationOptions;
import com.azure.ai.openai.models.SpeechVoice;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/**
 * An asynchronous sample demonstrates how to generate speech from a given text, text-to-speech.
 */
public class TextToSpeechAsyncSample {
    /**
     * Runs the sample algorithm and demonstrates how to generate speech from a given text, text-to-speech.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) throws InterruptedException {
        String azureOpenaiKey =  Configuration.getGlobalConfiguration().get("AZURE_OPENAI_KEY");
        String endpoint = Configuration.getGlobalConfiguration().get("AZURE_OPENAI_ENDPOINT");
        String deploymentOrModelId = "tts";

        OpenAIAsyncClient client = new OpenAIClientBuilder()
                .endpoint(endpoint)
                .credential(new AzureKeyCredential(azureOpenaiKey))
                .buildAsyncClient();

        SpeechGenerationOptions options = new SpeechGenerationOptions(
                "Today is a wonderful day to build something people love!",
                SpeechVoice.ALLOY);

        client.generateSpeechFromText(deploymentOrModelId, options)
                .subscribe(speech -> {
                    // Checkout your generated speech in the file system.
                    Path path = Paths.get("./azure-ai-openai/src/samples/java/com/azure/ai/openai/resources/speech.wav");
                    try {
                        Files.write(path, speech.toBytes());
                    } catch (IOException ignored) {
                    }
                });

        // The .subscribe() creation and assignment is not a blocking call. For the purpose of this example, we sleep
        // the thread so the program does not end before the send operation is complete. Using .block() instead of
        // .subscribe() will turn this into a synchronous call.
        TimeUnit.SECONDS.sleep(10);
    }
}
