// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.impl;

import com.azure.ai.openai.OpenAIAsyncClient;
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
import com.azure.ai.openai.models.EmbeddingItem;
import com.azure.ai.openai.models.Embeddings;
import com.azure.ai.openai.models.EmbeddingsOptions;
import com.azure.ai.openai.models.ImageGenerationOptions;
import com.azure.ai.openai.models.ImageLocation;
import com.azure.ai.openai.models.ImageResponse;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.KeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.ProxyOptions;
import com.azure.core.models.ResponseError;
import com.azure.core.util.BinaryData;
import com.azure.core.util.HttpClientOptions;
import com.azure.core.util.IterableStream;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS ARE USED TO EXTRACT
 * APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING LINE NUMBERS OF EXISTING CODE
 * SAMPLES.
 *
 * Class containing code snippets that will be injected to README.md.
 */
public final class ReadmeSamples {
    private OpenAIClient client = new OpenAIClientBuilder().buildClient();
    public void createSyncClientKeyCredential() {
        // BEGIN: readme-sample-createSyncClientKeyCredential
        OpenAIClient client = new OpenAIClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .buildClient();
        // END: readme-sample-createSyncClientKeyCredential
    }

    public void createAsyncClientKeyCredential() {
        // BEGIN: readme-sample-createAsyncClientKeyCredential
        OpenAIAsyncClient client = new OpenAIClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .buildAsyncClient();
        // END: readme-sample-createAsyncClientKeyCredential
    }

    public void createNonAzureSyncClientWithApiKey() {
        // BEGIN: readme-sample-createNonAzureOpenAISyncClientApiKey
        OpenAIClient client = new OpenAIClientBuilder()
            .credential(new KeyCredential("{openai-secret-key}"))
            .buildClient();
        // END: readme-sample-createNonAzureOpenAISyncClientApiKey
    }

    public void createNonAzureAsyncClientWithApiKey() {
        // BEGIN: readme-sample-createNonAzureOpenAIAsyncClientApiKey
        OpenAIAsyncClient client = new OpenAIClientBuilder()
            .credential(new KeyCredential("{openai-secret-key}"))
            .buildAsyncClient();
        // END: readme-sample-createNonAzureOpenAIAsyncClientApiKey
    }

    public void createOpenAIClientWithAAD() {
        // BEGIN: readme-sample-createOpenAIClientWithAAD
        TokenCredential defaultCredential = new DefaultAzureCredentialBuilder().build();
        OpenAIClient client = new OpenAIClientBuilder()
            .credential(defaultCredential)
            .endpoint("{endpoint}")
            .buildClient();
        // END: readme-sample-createOpenAIClientWithAAD
    }

    public void createOpenAIClientWithProxyOption() {
        // BEGIN: readme-sample-createOpenAIClientWithProxyOption
        // Proxy options
        final String hostname = "{your-host-name}";
        final int port = 447; // your port number

        ProxyOptions proxyOptions = new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress(hostname, port))
            .setCredentials("{username}", "{password}");

        OpenAIClient client = new OpenAIClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .clientOptions(new HttpClientOptions().setProxyOptions(proxyOptions))
            .buildClient();
        // END: readme-sample-createOpenAIClientWithProxyOption
    }

    public void getCompletions() {
        // BEGIN: readme-sample-getCompletions
        List<String> prompt = new ArrayList<>();
        prompt.add("Say this is a test");

        Completions completions = client.getCompletions("{deploymentOrModelId}", new CompletionsOptions(prompt));

        System.out.printf("Model ID=%s is created at %s.%n", completions.getId(), completions.getCreatedAt());
        for (Choice choice : completions.getChoices()) {
            System.out.printf("Index: %d, Text: %s.%n", choice.getIndex(), choice.getText());
        }
        // END: readme-sample-getCompletions
    }

    public void getCompletionsStream() {
        // BEGIN: readme-sample-getCompletionsStream
        List<String> prompt = new ArrayList<>();
        prompt.add("How to bake a cake?");

        IterableStream<Completions> completionsStream = client
            .getCompletionsStream("{deploymentOrModelId}", new CompletionsOptions(prompt));

        completionsStream
            .stream()
            // Remove .skip(1) when using Non-Azure OpenAI API
            // Note: the first chat completions can be ignored when using Azure OpenAI service which is a known service bug.
            // TODO: remove .skip(1) when service fix the issue.
            .skip(1)
            .forEach(completions -> System.out.print(completions.getChoices().get(0).getText()));
        // END: readme-sample-getCompletionsStream
    }

    public void getChatCompletions() {
        // BEGIN: readme-sample-getChatCompletions
        List<ChatMessage> chatMessages = new ArrayList<>();
        chatMessages.add(new ChatMessage(ChatRole.SYSTEM, "You are a helpful assistant. You will talk like a pirate."));
        chatMessages.add(new ChatMessage(ChatRole.USER, "Can you help me?"));
        chatMessages.add(new ChatMessage(ChatRole.ASSISTANT, "Of course, me hearty! What can I do for ye?"));
        chatMessages.add(new ChatMessage(ChatRole.USER, "What's the best way to train a parrot?"));

        ChatCompletions chatCompletions = client.getChatCompletions("{deploymentOrModelId}",
            new ChatCompletionsOptions(chatMessages));

        System.out.printf("Model ID=%s is created at %s.%n", chatCompletions.getId(), chatCompletions.getCreatedAt());
        for (ChatChoice choice : chatCompletions.getChoices()) {
            ChatMessage message = choice.getMessage();
            System.out.printf("Index: %d, Chat Role: %s.%n", choice.getIndex(), message.getRole());
            System.out.println("Message:");
            System.out.println(message.getContent());
        }
        // END: readme-sample-getChatCompletions
    }

    public void getChatCompletionsStream() {
        // BEGIN: readme-sample-getChatCompletionsStream
        List<ChatMessage> chatMessages = new ArrayList<>();
        chatMessages.add(new ChatMessage(ChatRole.SYSTEM, "You are a helpful assistant. You will talk like a pirate."));
        chatMessages.add(new ChatMessage(ChatRole.USER, "Can you help me?"));
        chatMessages.add(new ChatMessage(ChatRole.ASSISTANT, "Of course, me hearty! What can I do for ye?"));
        chatMessages.add(new ChatMessage(ChatRole.USER, "What's the best way to train a parrot?"));

        IterableStream<ChatCompletions> chatCompletionsStream = client.getChatCompletionsStream("{deploymentOrModelId}",
            new ChatCompletionsOptions(chatMessages));

        chatCompletionsStream
            .stream()
            // Remove .skip(1) when using Non-Azure OpenAI API
            // Note: the first chat completions can be ignored when using Azure OpenAI service which is a known service bug.
            // TODO: remove .skip(1) when service fix the issue.
            .skip(1)
            .forEach(chatCompletions -> {
                ChatMessage delta = chatCompletions.getChoices().get(0).getDelta();
                if (delta.getRole() != null) {
                    System.out.println("Role = " + delta.getRole());
                }
                if (delta.getContent() != null) {
                    System.out.print(delta.getContent());
                }
            });
        // END: readme-sample-getChatCompletionsStream
    }

    public void getEmbedding() {
        // BEGIN: readme-sample-getEmbedding
        EmbeddingsOptions embeddingsOptions = new EmbeddingsOptions(
            Arrays.asList("Your text string goes here"));

        Embeddings embeddings = client.getEmbeddings("{deploymentOrModelId}", embeddingsOptions);

        for (EmbeddingItem item : embeddings.getData()) {
            System.out.printf("Index: %d.%n", item.getPromptIndex());
            for (Double embedding : item.getEmbedding()) {
                System.out.printf("%f;", embedding);
            }
        }
        // END: readme-sample-getEmbedding
    }

    public void imageGeneration() {
        // BEGIN: readme-sample-imageGeneration
        ImageGenerationOptions imageGenerationOptions = new ImageGenerationOptions(
            "A drawing of the Seattle skyline in the style of Van Gogh");
        ImageResponse images = client.getImages(imageGenerationOptions);

        for (ImageLocation imageLocation : images.getData()) {
            ResponseError error = imageLocation.getError();
            if (error != null) {
                System.out.printf("Image generation operation failed. Error code: %s, error message: %s.%n",
                    error.getCode(), error.getMessage());
            } else {
                System.out.printf(
                    "Image location URL that provides temporary access to download the generated image is %s.%n",
                    imageLocation.getUrl());
            }
        }
        // END: readme-sample-imageGeneration
    }

    public void audioTranscription() {
        // BEGIN: readme-sample-audioTranscription
        String fileName = "{your-file-name}";
        Path filePath = Paths.get("{your-file-path}" + fileName);

        byte[] file = BinaryData.fromFile(filePath).toBytes();
        AudioTranscriptionOptions transcriptionOptions = new AudioTranscriptionOptions(file)
            .setResponseFormat(AudioTranscriptionFormat.JSON);

        AudioTranscription transcription = client.getAudioTranscription("{deploymentOrModelId}", fileName, transcriptionOptions);

        System.out.println("Transcription: " + transcription.getText());
        // END: readme-sample-audioTranscription
    }

    public void audioTranslation() {
        // BEGIN: readme-sample-audioTranslation
        String fileName = "{your-file-name}";
        Path filePath = Paths.get("{your-file-path}" + fileName);

        byte[] file = BinaryData.fromFile(filePath).toBytes();
        AudioTranslationOptions translationOptions = new AudioTranslationOptions(file)
            .setResponseFormat(AudioTranslationFormat.JSON);

        AudioTranslation translation = client.getAudioTranslation("{deploymentOrModelId}", fileName, translationOptions);

        System.out.println("Translation: " + translation.getText());
        // END: readme-sample-audioTranslation
    }
}
