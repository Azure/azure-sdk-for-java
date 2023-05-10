// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai;

import com.azure.ai.openai.models.Choice;
import com.azure.ai.openai.models.Completions;
import com.azure.ai.openai.models.CompletionsOptions;
import com.azure.ai.openai.models.CompletionsUsage;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.IterableStream;

import java.util.ArrayList;
import java.util.List;

public class GetCompletionsNonAzure {
    /**
     * Runs the sample algorithm and demonstrates how to get completions for the provided input prompts.
     * Completions support a wide variety of tasks and generate text that continues from or "completes" provided
     * prompt data.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) {
        String openAIkey = Configuration.getGlobalConfiguration().get("NON_AZURE_OPEN_AI_KEY");
        String deploymentOrModelId = "text-davinci-003";

        OpenAIClient client = new OpenAIClientBuilder()
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .credential(new OpenAIApiKeyCredential(openAIkey))
            .buildClient();

//        List<String> prompt = new ArrayList<>();
//        prompt.add("Why did the eagles not carry Frodo Baggins to Mordor?");
//
//        Response<BinaryData> completionsWithResponse = client.getCompletionsWithResponse(deploymentOrModelId, BinaryData.fromObject(new CompletionsOptions(prompt)), null);
//
//        Completions completions = completionsWithResponse.getValue().toObject(Completions.class);
//
//        System.out.printf("Model ID=%s is created at %d.%n", completions.getId(), completions.getCreated());
//        for (Choice choice : completions.getChoices()) {
//            System.out.printf("Index: %d, Text: %s.%n", choice.getIndex(), choice.getText());
//        }
//
//        CompletionsUsage usage = completions.getUsage();
//        System.out.printf("Usage: number of prompt token is %d, "
//                + "number of completion token is %d, and number of total tokens in request and response is %d.%n",
//            usage.getPromptTokens(), usage.getCompletionTokens(), usage.getTotalTokens());

        List<String> prompt = new ArrayList<>();
        prompt.add("Why did the eagles not carry Frodo Baggins to Mordor?");
        IterableStream<Completions> completionsStream = client.getCompletionsStream(deploymentOrModelId,
            new CompletionsOptions(prompt).setMaxTokens(1000).setStream(true));

        completionsStream.forEach(completions -> {
            System.out.printf("Model ID=%s is created at %d.%n", completions.getId(), completions.getCreated());
            for (Choice choice : completions.getChoices()) {
                System.out.printf("Index: %d, Text: %s.%n", choice.getIndex(), choice.getText());
            }

            CompletionsUsage usage = completions.getUsage();
            if (usage != null) {
                System.out.printf("Usage: number of prompt token is %d, "
                        + "number of completion token is %d, and number of total tokens in request and response is %d.%n",
                    usage.getPromptTokens(), usage.getCompletionTokens(), usage.getTotalTokens());
            }
        });

        // Chat Completions
//        List<ChatMessage> chatMessages = new ArrayList<>();
//        chatMessages.add(new ChatMessage(ChatRole.SYSTEM).setContent("You are a helpful assistant. You will talk like a pirate."));
//        chatMessages.add(new ChatMessage(ChatRole.USER).setContent("Can you help me?"));
//        chatMessages.add(new ChatMessage(ChatRole.ASSISTANT).setContent("Of course, me hearty! What can I do for ye?"));
//        chatMessages.add(new ChatMessage(ChatRole.USER).setContent("What's the best way to train a parrot?"));
        deploymentOrModelId = "gpt-3.5-turbo";
//        ChatCompletions chatCompletions = client.getChatCompletions(deploymentOrModelId, new ChatCompletionsOptions(chatMessages));
//
//        System.out.printf("Model ID=%s is created at %d.%n", chatCompletions.getId(), chatCompletions.getCreated());
//        for (ChatChoice choice : chatCompletions.getChoices()) {
//            ChatMessage message = choice.getMessage();
//            System.out.printf("Index: %d, Chat Role: %s.%n", choice.getIndex(), message.getRole());
//            System.out.println("Message:");
//            System.out.println(message.getContent());
//        }

//
//        List<ChatMessage> chatMessages = new ArrayList<>();
//        chatMessages.add(new ChatMessage(ChatRole.SYSTEM).setContent("You are a helpful assistant. You will talk like a pirate."));
//        chatMessages.add(new ChatMessage(ChatRole.USER).setContent("Can you help me?"));
//        chatMessages.add(new ChatMessage(ChatRole.ASSISTANT).setContent("Of course, me hearty! What can I do for ye?"));
//        chatMessages.add(new ChatMessage(ChatRole.USER).setContent("What's the best way to train a parrot?"));
//
//        IterableStream<ChatCompletions> chatCompletionsStream = client.getChatCompletionsStream(deploymentOrModelId, new ChatCompletionsOptions(chatMessages));
//
//        chatCompletionsStream.forEach(chatCompletions -> {
//            System.out.printf("Model ID=%s is created at %d.%n", chatCompletions.getId(), chatCompletions.getCreated());
//            for (ChatChoice choice : chatCompletions.getChoices()) {
//                ChatMessageDelta message = choice.getDelta();
//                if (message != null) {
//                    System.out.printf("Index: %d, Chat Role: %s.%n", choice.getIndex(), message.getRole());
//                    System.out.println("Message:");
//                    System.out.println(message.getContent());
//                }
//            }
//
//            CompletionsUsage usage = chatCompletions.getUsage();
//            if (usage != null) {
//                System.out.printf("Usage: number of prompt token is %d, "
//                        + "number of completion token is %d, and number of total tokens in request and response is %d.%n",
//                    usage.getPromptTokens(), usage.getCompletionTokens(), usage.getTotalTokens());
//            }
//        });




//        // Embedding options
//        EmbeddingsOptions embeddingsOptions = new EmbeddingsOptions(Arrays.asList("Your text string goes here"));
//        deploymentOrModelId = "text-embedding-ada-002";
//        Embeddings embeddings = client.getEmbeddings(deploymentOrModelId, embeddingsOptions);
//
//        for (EmbeddingItem item : embeddings.getData()) {
//            System.out.printf("Index: %d.%n", item.getIndex());
//            for (Double embedding : item.getEmbedding()) {
//                System.out.printf("%f;", embedding);
//            }
//        }
//


        //
    }

}
