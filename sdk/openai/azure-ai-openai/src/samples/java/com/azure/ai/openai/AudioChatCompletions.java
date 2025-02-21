// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai;

import com.azure.ai.openai.models.AudioOutputParameters;
import com.azure.ai.openai.models.AudioResponseData;
import com.azure.ai.openai.models.ChatChoice;
import com.azure.ai.openai.models.ChatCompletionModality;
import com.azure.ai.openai.models.ChatCompletions;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatMessageAudioContentItem;
import com.azure.ai.openai.models.ChatRequestUserMessage;
import com.azure.ai.openai.models.ChatResponseMessage;
import com.azure.ai.openai.models.InputAudioContent;
import com.azure.ai.openai.models.InputAudioFormat;
import com.azure.ai.openai.models.OutputAudioFormat;
import com.azure.ai.openai.models.SpeechVoice;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;

import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Sample demonstrates how to use the Azure OpenAI Service to get chat completions with audio input and output.
 */
public class AudioChatCompletions {

    /**
     * Main method to run the sample.
     *
     * @param args Command line arguments. Unused.
     */
    public static void main(String[] args) {
        String azureOpenaiKey = Configuration.getGlobalConfiguration().get("AZURE_OPENAI_KEY");
        String endpoint = Configuration.getGlobalConfiguration().get("AZURE_OPENAI_ENDPOINT");
        String deploymentOrModelId = "{azure-open-ai-deployment-model-id}"; //gpt-4o-audio-preview

        OpenAIClient client = new OpenAIClientBuilder()
                .endpoint(endpoint)
                .credential(new AzureKeyCredential(azureOpenaiKey))
                .buildClient();

        byte[] file
                = BinaryData.fromFile(Paths.get("path/file/prompt.wav")).toBytes();

        ChatCompletionsOptions options = new ChatCompletionsOptions(Arrays.asList(
                new ChatRequestUserMessage(Arrays.asList(new ChatMessageAudioContentItem(
                        new InputAudioContent(file, InputAudioFormat.WAV))))));
        options.setModalities(Arrays.asList(ChatCompletionModality.TEXT, ChatCompletionModality.AUDIO));
        options.setStore(true);
        options.setAudio(new AudioOutputParameters(SpeechVoice.ALLOY, OutputAudioFormat.WAV));

        ChatCompletions chatCompletions = client.getChatCompletions(deploymentOrModelId, options);
        ChatChoice choice = chatCompletions.getChoices().get(0);
        ChatResponseMessage message = choice.getMessage();

        // Assert that the message has content
        AudioResponseData audioResponse = message.getAudio();

        String audioData = audioResponse.getData(); // Base64 encoded audio data
        String transcript = audioResponse.getTranscript(); // Transcription of the audio
    }
}
