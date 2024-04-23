// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.usage;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.AudioTranscriptionFormat;
import com.azure.ai.openai.models.AudioTranscriptionOptions;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/**
 * An asynchronous sample demonstrates how to transcript a given audio file.
 */
public class AudioTranscriptionAsyncSample {
    /**
     * Runs the sample algorithm and demonstrates how to transcript a given audio file.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) throws InterruptedException {
        String azureOpenaiKey = Configuration.getGlobalConfiguration().get("AZURE_OPENAI_KEY");
        String endpoint = Configuration.getGlobalConfiguration().get("AZURE_OPENAI_ENDPOINT");
        String deploymentOrModelId = "{azure-open-ai-deployment-model-id}";
        String fileName = "batman.wav";
        Path filePath = Paths.get("src/samples/java/com/azure/ai/openai/resources/" + fileName);

        OpenAIAsyncClient client = new OpenAIClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(azureOpenaiKey))
            .buildAsyncClient();

        byte[] file = BinaryData.fromFile(filePath).toBytes();

        // To request timestamps for 'segments' and/or 'words', specific the desired response format as
        // 'AudioTranscriptionFormat.VERBOSE_JSON' and provide the desired combination of enum flags for the available
        // timestamp granularities. If not otherwise specified, 'segments' will be provided. Note that 'words', unlike 'segments',
        // will introduce additional processing latency to compute.
        AudioTranscriptionOptions transcriptionOptions = new AudioTranscriptionOptions(file)
            .setResponseFormat(AudioTranscriptionFormat.JSON);

        client.getAudioTranscription(deploymentOrModelId, fileName, transcriptionOptions)
            .subscribe(transcription -> {
                System.out.println("Transcription: " + transcription.getText());
            });

        // The .subscribe() creation and assignment is not a blocking call. For the purpose of this example, we sleep
        // the thread so the program does not end before the send operation is complete. Using .block() instead of
        // .subscribe() will turn this into a synchronous call.
        TimeUnit.SECONDS.sleep(10);
    }
}
