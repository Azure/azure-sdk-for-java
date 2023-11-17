// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.usage;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.AudioTranscription;
import com.azure.ai.openai.models.AudioTranscriptionFormat;
import com.azure.ai.openai.models.AudioTranscriptionOptions;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A sample demonstrates how to transcript a given audio file.
 */
public class AudioTranscriptionSample {
    /**
     * Runs the sample algorithm and demonstrates how to get the images for a given prompt.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) {
        String azureOpenaiKey = "{azure-open-ai-key}";
        String endpoint = "{azure-open-ai-endpoint}";
        String deploymentOrModelId = "{azure-open-ai-deployment-model-id}";
        String fileName = "batman.wav";
        Path filePath = Paths.get("src/samples/java/com/azure/ai/openai/resources/" + fileName);

        OpenAIClient client = new OpenAIClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(azureOpenaiKey))
            .buildClient();

        byte[] file = BinaryData.fromFile(filePath).toBytes();
        AudioTranscriptionOptions transcriptionOptions = new AudioTranscriptionOptions(file)
            .setResponseFormat(AudioTranscriptionFormat.JSON);

        AudioTranscription transcription = client.getAudioTranscription(deploymentOrModelId, fileName, transcriptionOptions);

        System.out.println("Transcription: " + transcription.getText());
    }
}
