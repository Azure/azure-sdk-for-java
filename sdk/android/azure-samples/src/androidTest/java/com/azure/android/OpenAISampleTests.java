package com.azure.android;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.AudioTranscription;
import com.azure.ai.openai.models.AudioTranscriptionFormat;
import com.azure.ai.openai.models.AudioTranscriptionOptions;
import com.azure.ai.openai.models.AudioTranslation;
import com.azure.ai.openai.models.AudioTranslationFormat;
import com.azure.ai.openai.models.AudioTranslationOptions;
import com.azure.ai.openai.models.ChatChoice;
import com.azure.ai.openai.models.ChatCompletions;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatMessage;
import com.azure.ai.openai.models.ChatRole;
import com.azure.ai.openai.models.Choice;
import com.azure.ai.openai.models.Completions;
import com.azure.ai.openai.models.CompletionsOptions;
import com.azure.ai.openai.models.CompletionsUsage;
import com.azure.ai.openai.models.EmbeddingItem;
import com.azure.ai.openai.models.Embeddings;
import com.azure.ai.openai.models.EmbeddingsOptions;
import com.azure.ai.openai.models.EmbeddingsUsage;
import com.azure.ai.openai.models.ImageGenerationOptions;
import com.azure.ai.openai.models.ImageLocation;
import com.azure.ai.openai.models.ImageResponse;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.models.ResponseError;
import com.azure.core.util.BinaryData;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class OpenAISampleTests {
    /**
     * The following tests are Open AI samples ported directly, with assertions added to ensure
     * that they will fail instrumented tests rather than simply logging errors and passing tests.
     * Currently it still contains log statements in order to help verify that the samples are working, and the
     * transcription and translation samples at the bottom will probably need to have their file paths changed.
     */
    private final String azureOpenaiKey = "{azure-open-ai-key}";
    private final String endpoint = "{azure-open-ai-endpoint}";
    private final String deploymentOrModelId = "{azure-open-ai-deployment-model-id}";
    OpenAIClient client = new OpenAIClientBuilder()
        .endpoint(endpoint)
        .credential(new AzureKeyCredential(azureOpenaiKey))
        .buildClient();

    @Test
    public void GetImagesSampleTest() {
        ImageGenerationOptions imageGenerationOptions = new ImageGenerationOptions(
            "A drawing of the Seattle skyline in the style of Van Gogh");
        ImageResponse images = client.getImages(imageGenerationOptions);

        for (ImageLocation imageLocation : images.getData()) {
            ResponseError error = imageLocation.getError();
            if (error != null) {
                fail(String.format("Image generation operation failed. Error code: %s, error message: %s.%n",
                    error.getCode(), error.getMessage()));
            } else {
                assertNotNull(imageLocation.getUrl());
            }
        }
    }

    @Test
    public void GetEmbeddingsSampleTest() {
        String TAG = "GetEmbeddingsSample";
        EmbeddingsOptions embeddingsOptions = new EmbeddingsOptions(Arrays.asList("Your text string goes here"));

        Embeddings embeddings = client.getEmbeddings(deploymentOrModelId, embeddingsOptions);

        for (EmbeddingItem item : embeddings.getData()) {
            Log.i(TAG, String.format("Index: %d.%n", item.getPromptIndex()));
            for (Double embedding : item.getEmbedding()) {
                assertNotNull(embedding);
            }
        }

        EmbeddingsUsage usage = embeddings.getUsage();
        assertTrue(usage.getTotalTokens() > 0);
        Log.i(TAG, String.format(
            "Usage: number of prompt token is %d and number of total tokens in request and response is %d.%n",
            usage.getPromptTokens(), usage.getTotalTokens()));
    }

    @Test
    public void GetCompletionsSampleTest() {
        String TAG = "GetCompletionsSample";
        List<String> prompt = new ArrayList<>();
        prompt.add("Why did the eagles not carry Frodo Baggins to Mordor?");

        Completions completions = client.getCompletions(deploymentOrModelId, new CompletionsOptions(prompt));

        assertNotNull(completions.getId());
        assertNotNull(completions.getCreatedAt());
        Log.i(TAG, String.format("Model ID=%s is created at %s.%n", completions.getId(), completions.getCreatedAt()));
        for (Choice choice : completions.getChoices()) {
            assertNotNull(choice.getText());
            Log.i(TAG, String.format("Index: %d, Text: %s.%n", choice.getIndex(), choice.getText()));
        }

        CompletionsUsage usage = completions.getUsage();
        assertTrue(usage.getTotalTokens() > 0);
        assertEquals(usage.getCompletionTokens() + usage.getPromptTokens(), usage.getTotalTokens());
        Log.i(TAG, String.format("Usage: number of prompt token is %d, "
                + "number of completion token is %d, and number of total tokens in request and response is %d.%n",
            usage.getPromptTokens(), usage.getCompletionTokens(), usage.getTotalTokens()));
    }

    @Test
    public void GetChatCompletionsSampleTest() {
        String TAG = "GetChatCompletionsSample";
        List<ChatMessage> chatMessages = new ArrayList<>();
        chatMessages.add(new ChatMessage(ChatRole.SYSTEM, "You are a helpful assistant. You will talk like a pirate."));
        chatMessages.add(new ChatMessage(ChatRole.USER, "Can you help me?"));
        chatMessages.add(new ChatMessage(ChatRole.ASSISTANT, "Of course, me hearty! What can I do for ye?"));
        chatMessages.add(new ChatMessage(ChatRole.USER, "What's the best way to train a parrot?"));

        ChatCompletions chatCompletions = client.getChatCompletions(deploymentOrModelId, new ChatCompletionsOptions(chatMessages));

        Log.i(TAG, String.format("Model ID=%s is created at %s.%n", chatCompletions.getId(), chatCompletions.getCreatedAt()));
        assertNotNull(chatCompletions.getId());
        for (ChatChoice choice : chatCompletions.getChoices()) {
            ChatMessage message = choice.getMessage();
            assertNotNull(message);
            Log.i(TAG, String.format("Index: %d, Chat Role: %s.%n", choice.getIndex(), message.getRole()));
            Log.i(TAG, String.format("Message: %s", message.getContent()));
        }

        CompletionsUsage usage = chatCompletions.getUsage();
        assertEquals(usage.getCompletionTokens() + usage.getPromptTokens(), usage.getTotalTokens());
        Log.i(TAG, String.format("Usage: number of prompt token is %d, "
                + "number of completion token is %d, and number of total tokens in request and response is %d.%n",
            usage.getPromptTokens(), usage.getCompletionTokens(), usage.getTotalTokens()));
    }

    @Test
    public void AudioTranscriptionSampleTest() {
        String TAG = "AudioTranscriptionSample";
        String fileName = "batman.wav";
        Path filePath = Paths.get("src/samples/java/com/azure/ai/openai/resources/" + fileName);

        byte[] file = BinaryData.fromFile(filePath).toBytes();
        AudioTranscriptionOptions transcriptionOptions = new AudioTranscriptionOptions(file)
            .setResponseFormat(AudioTranscriptionFormat.JSON);

        AudioTranscription transcription = client.getAudioTranscription(deploymentOrModelId, fileName, transcriptionOptions);
        assertNotNull(transcription);
        Log.i(TAG, "Transcription: " + transcription.getText());
    }

    @Test
    public void AudioTranslationSampleTest() {
        String TAG = "AudioTranslationSample";
        String fileName = "JP_it_is_rainy_today.wav";
        Path filePath = Paths.get("src/samples/java/com/azure/ai/openai/resources/" + fileName);

        byte[] file = BinaryData.fromFile(filePath).toBytes();
        AudioTranslationOptions translationOptions = new AudioTranslationOptions(file)
            .setResponseFormat(AudioTranslationFormat.JSON);

        AudioTranslation translation = client.getAudioTranslation(deploymentOrModelId, fileName, translationOptions);
        assertNotNull(translation);
        Log.i(TAG, "Translation: " + translation.getText());
    }
}
