// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.impl;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.MyFunctionCallArguments;
import com.azure.ai.openai.models.AudioTranscription;
import com.azure.ai.openai.models.AudioTranscriptionFormat;
import com.azure.ai.openai.models.AudioTranscriptionOptions;
import com.azure.ai.openai.models.AudioTranslation;
import com.azure.ai.openai.models.AudioTranslationFormat;
import com.azure.ai.openai.models.AudioTranslationOptions;
import com.azure.ai.openai.models.ChatChoice;
import com.azure.ai.openai.models.ChatCompletions;
import com.azure.ai.openai.models.ChatCompletionsFunctionToolCall;
import com.azure.ai.openai.models.ChatCompletionsFunctionToolDefinition;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatCompletionsToolDefinition;
import com.azure.ai.openai.models.ChatMessageImageContentItem;
import com.azure.ai.openai.models.ChatMessageImageUrl;
import com.azure.ai.openai.models.ChatMessageTextContentItem;
import com.azure.ai.openai.models.ChatRequestAssistantMessage;
import com.azure.ai.openai.models.ChatRequestMessage;
import com.azure.ai.openai.models.ChatRequestSystemMessage;
import com.azure.ai.openai.models.ChatRequestToolMessage;
import com.azure.ai.openai.models.ChatRequestUserMessage;
import com.azure.ai.openai.models.ChatResponseMessage;
import com.azure.ai.openai.models.Choice;
import com.azure.ai.openai.models.Completions;
import com.azure.ai.openai.models.CompletionsFinishReason;
import com.azure.ai.openai.models.CompletionsOptions;
import com.azure.ai.openai.models.EmbeddingItem;
import com.azure.ai.openai.models.Embeddings;
import com.azure.ai.openai.models.EmbeddingsOptions;
import com.azure.ai.openai.models.FunctionDefinition;
import com.azure.ai.openai.models.ImageGenerationData;
import com.azure.ai.openai.models.ImageGenerationOptions;
import com.azure.ai.openai.models.ImageGenerations;
import com.azure.ai.openai.models.SpeechGenerationOptions;
import com.azure.ai.openai.models.SpeechVoice;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.KeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.ProxyOptions;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.util.BinaryData;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.HttpClientOptions;
import com.azure.core.util.IterableStream;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
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

        Completions completions = client.getCompletions("{deploymentOrModelName}", new CompletionsOptions(prompt));

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
            .getCompletionsStream("{deploymentOrModelName}", new CompletionsOptions(prompt));

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
        List<ChatRequestMessage> chatMessages = new ArrayList<>();
        chatMessages.add(new ChatRequestSystemMessage("You are a helpful assistant. You will talk like a pirate."));
        chatMessages.add(new ChatRequestUserMessage("Can you help me?"));
        chatMessages.add(new ChatRequestAssistantMessage("Of course, me hearty! What can I do for ye?"));
        chatMessages.add(new ChatRequestUserMessage("What's the best way to train a parrot?"));

        ChatCompletions chatCompletions = client.getChatCompletions("{deploymentOrModelName}",
            new ChatCompletionsOptions(chatMessages));

        System.out.printf("Model ID=%s is created at %s.%n", chatCompletions.getId(), chatCompletions.getCreatedAt());
        for (ChatChoice choice : chatCompletions.getChoices()) {
            ChatResponseMessage message = choice.getMessage();
            System.out.printf("Index: %d, Chat Role: %s.%n", choice.getIndex(), message.getRole());
            System.out.println("Message:");
            System.out.println(message.getContent());
        }
        // END: readme-sample-getChatCompletions
    }

    public void getChatCompletionsStream() {
        // BEGIN: readme-sample-getChatCompletionsStream
        List<ChatRequestMessage> chatMessages = new ArrayList<>();
        chatMessages.add(new ChatRequestSystemMessage("You are a helpful assistant. You will talk like a pirate."));
        chatMessages.add(new ChatRequestUserMessage("Can you help me?"));
        chatMessages.add(new ChatRequestAssistantMessage("Of course, me hearty! What can I do for ye?"));
        chatMessages.add(new ChatRequestUserMessage("What's the best way to train a parrot?"));

        client.getChatCompletionsStream("{deploymentOrModelName}", new ChatCompletionsOptions(chatMessages))
                .forEach(chatCompletions -> {
                    if (CoreUtils.isNullOrEmpty(chatCompletions.getChoices())) {
                        return;
                    }
                    ChatResponseMessage delta = chatCompletions.getChoices().get(0).getDelta();
                    if (delta.getRole() != null) {
                        System.out.println("Role = " + delta.getRole());
                    }
                    if (delta.getContent() != null) {
                        String content = delta.getContent();
                        System.out.print(content);
                    }
                });
        // END: readme-sample-getChatCompletionsStream
    }

    public void getEmbedding() {
        // BEGIN: readme-sample-getEmbedding
        EmbeddingsOptions embeddingsOptions = new EmbeddingsOptions(
            Arrays.asList("Your text string goes here"));

        Embeddings embeddings = client.getEmbeddings("{deploymentOrModelName}", embeddingsOptions);

        for (EmbeddingItem item : embeddings.getData()) {
            System.out.printf("Index: %d.%n", item.getPromptIndex());
            for (Float embedding : item.getEmbedding()) {
                System.out.printf("%f;", embedding);
            }
        }
        // END: readme-sample-getEmbedding
    }

    public void imageGeneration() {
        // BEGIN: readme-sample-imageGeneration
        ImageGenerationOptions imageGenerationOptions = new ImageGenerationOptions(
            "A drawing of the Seattle skyline in the style of Van Gogh");
        ImageGenerations images = client.getImageGenerations("{deploymentOrModelName}", imageGenerationOptions);

        for (ImageGenerationData imageGenerationData : images.getData()) {
            System.out.printf(
                "Image location URL that provides temporary access to download the generated image is %s.%n",
                imageGenerationData.getUrl());
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

        AudioTranscription transcription = client.getAudioTranscription("{deploymentOrModelName}", fileName, transcriptionOptions);

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

        AudioTranslation translation = client.getAudioTranslation("{deploymentOrModelName}", fileName, translationOptions);

        System.out.println("Translation: " + translation.getText());
        // END: readme-sample-audioTranslation
    }

    public void chatWithImages() {
        // BEGIN: readme-sample-chatWithImages
        List<ChatRequestMessage> chatMessages = new ArrayList<>();
        chatMessages.add(new ChatRequestSystemMessage("You are a helpful assistant that describes images"));
        chatMessages.add(new ChatRequestUserMessage(Arrays.asList(
                new ChatMessageTextContentItem("Please describe this image"),
                new ChatMessageImageContentItem(
                        new ChatMessageImageUrl("https://upload.wikimedia.org/wikipedia/commons/thumb/4/44/Microsoft_logo.svg/512px-Microsoft_logo.svg.png"))
        )));

        ChatCompletionsOptions chatCompletionsOptions = new ChatCompletionsOptions(chatMessages);
        ChatCompletions chatCompletions = client.getChatCompletions("{deploymentOrModelName}", chatCompletionsOptions);

        System.out.println("Chat completion: " + chatCompletions.getChoices().get(0).getMessage().getContent());
        // END: readme-sample-chatWithImages
    }

    public void toolCalls() {
        // BEGIN: readme-sample-toolCalls
        List<ChatRequestMessage> chatMessages = Arrays.asList(
                new ChatRequestSystemMessage("You are a helpful assistant."),
                new ChatRequestUserMessage("What sort of clothing should I wear today in Berlin?")
        );
        ChatCompletionsToolDefinition toolDefinition = new ChatCompletionsFunctionToolDefinition(
                new FunctionDefinition("MyFunctionName"));

        ChatCompletionsOptions chatCompletionsOptions = new ChatCompletionsOptions(chatMessages);
        chatCompletionsOptions.setTools(Arrays.asList(toolDefinition));

        ChatCompletions chatCompletions = client.getChatCompletions("{deploymentOrModelName}", chatCompletionsOptions);

        ChatChoice choice = chatCompletions.getChoices().get(0);
        // The LLM is requesting the calling of the function we defined in the original request
        if (choice.getFinishReason() == CompletionsFinishReason.TOOL_CALLS) {
            ChatCompletionsFunctionToolCall toolCall = (ChatCompletionsFunctionToolCall) choice.getMessage().getToolCalls().get(0);
            String functionArguments = toolCall.getFunction().getArguments();

            // As an additional step, you may want to deserialize the parameters, so you can call your function
            MyFunctionCallArguments parameters = BinaryData.fromString(functionArguments).toObject(MyFunctionCallArguments.class);

            String functionCallResult = "{the-result-of-my-function}"; // myFunction(parameters...);

            ChatRequestAssistantMessage assistantMessage = new ChatRequestAssistantMessage("");
            assistantMessage.setToolCalls(choice.getMessage().getToolCalls());

            // We include:
            // - The past 2 messages from the original request
            // - A new ChatRequestAssistantMessage with the tool calls from the original request
            // - A new ChatRequestToolMessage with the result of our function call
            List<ChatRequestMessage> followUpMessages = Arrays.asList(
                    chatMessages.get(0),
                    chatMessages.get(1),
                    assistantMessage,
                    new ChatRequestToolMessage(functionCallResult, toolCall.getId())
            );

            ChatCompletionsOptions followUpChatCompletionsOptions = new ChatCompletionsOptions(followUpMessages);

            ChatCompletions followUpChatCompletions = client.getChatCompletions("{deploymentOrModelName}", followUpChatCompletionsOptions);

            // This time the finish reason is STOPPED
            ChatChoice followUpChoice = followUpChatCompletions.getChoices().get(0);
            if (followUpChoice.getFinishReason() == CompletionsFinishReason.STOPPED) {
                System.out.println("Chat Completions Result: " + followUpChoice.getMessage().getContent());
            }
        }
        // END: readme-sample-toolCalls
    }

    public void textToSpeech() throws IOException {
        // BEGIN: readme-sample-textToSpeech
        String deploymentOrModelId = "{azure-open-ai-deployment-model-id}";
        SpeechGenerationOptions options = new SpeechGenerationOptions(
                "Today is a wonderful day to build something people love!",
                SpeechVoice.ALLOY);
        BinaryData speech = client.generateSpeechFromText(deploymentOrModelId, options);
        // Checkout your generated speech in the file system.
        Path path = Paths.get("{your-local-file-path}/speech.wav");
        Files.write(path, speech.toBytes());
        // END: readme-sample-textToSpeech
    }

    public void enableHttpLogging() {
        // BEGIN: readme-sample-enablehttplogging
        OpenAIClient openAIClient = new OpenAIClientBuilder()
                .endpoint("{endpoint}")
                .credential(new AzureKeyCredential("{key}"))
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
                .buildClient();
        // or
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
        OpenAIClient configurationClientAad = new OpenAIClientBuilder()
                .credential(credential)
                .endpoint("{endpoint}")
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
                .buildClient();
        // END: readme-sample-enablehttplogging
    }

    public void troubleshootingExceptions() {
        OpenAIClient client = new OpenAIClientBuilder()
                .buildClient();
        // BEGIN: readme-sample-troubleshootingExceptions
        List<ChatRequestMessage> chatMessages = new ArrayList<>();
        chatMessages.add(new ChatRequestSystemMessage("You are a helpful assistant. You will talk like a pirate."));
        chatMessages.add(new ChatRequestUserMessage("Can you help me?"));
        chatMessages.add(new ChatRequestAssistantMessage("Of course, me hearty! What can I do for ye?"));
        chatMessages.add(new ChatRequestUserMessage("What's the best way to train a parrot?"));

        try {
            ChatCompletions chatCompletions = client.getChatCompletions("{deploymentOrModelName}",
                    new ChatCompletionsOptions(chatMessages));
        } catch (HttpResponseException e) {
            System.out.println(e.getMessage());
            // Do something with the exception
        }
        // END: readme-sample-troubleshootingExceptions

        OpenAIAsyncClient asyncClient = new OpenAIClientBuilder()
                .buildAsyncClient();
        // BEGIN: readme-sample-troubleshootingExceptions-async
        asyncClient.getChatCompletions("{deploymentOrModelName}", new ChatCompletionsOptions(chatMessages))
                .doOnSuccess(ignored -> System.out.println("Success!"))
                .doOnError(
                        error -> error instanceof ResourceNotFoundException,
                        error -> System.out.println("Exception: 'getChatCompletions' could not be performed."));
        // END: readme-sample-troubleshootingExceptions-async
    }

}
