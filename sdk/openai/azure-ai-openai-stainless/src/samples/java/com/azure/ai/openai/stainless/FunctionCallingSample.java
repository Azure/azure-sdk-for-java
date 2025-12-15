// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.stainless;

import com.azure.identity.AuthenticationUtil;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.credential.BearerTokenCredential;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.ChatCompletionMessageFunctionToolCall;
import com.openai.models.chat.completions.ChatCompletionMessageToolCall;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

// Tool class for SDK quality evaluation
@JsonClassDescription("Gets the quality of the given SDK.")
class GetSdkQuality {
    @JsonPropertyDescription("The name of the SDK.")
    public String name;

    public String execute() {
        return name.contains("OpenAI") ? "Excellent quality and robust implementation!" : "Unknown quality";
    }
}

public final class FunctionCallingSample {
    private FunctionCallingSample() {}

    public static void main(String[] args) {
        // Configures using one of:
        // - The `OPENAI_API_KEY` environment variable
        // - The `AZURE_OPENAI_ENDPOINT` and `AZURE_OPENAI_KEY` environment variables
        OpenAIOkHttpClient.Builder clientBuilder = OpenAIOkHttpClient.builder();

        /* Azure-specific code starts here */
        // You can either set 'endpoint' or 'apiKey' directly in the builder.
        // or set same two env vars and use fromEnv() method instead
        clientBuilder
            .baseUrl(System.getenv("AZURE_OPENAI_ENDPOINT"))
            .credential(BearerTokenCredential.create(AuthenticationUtil.getBearerTokenSupplier(
                new DefaultAzureCredentialBuilder().build(), "https://cognitiveservices.azure.com/.default")));
        /* Azure-specific code ends here */

        // All code from this line down is general-purpose OpenAI code
        OpenAIClient client = clientBuilder.build();

        ChatCompletionCreateParams createParams = ChatCompletionCreateParams.builder()
                .model(ChatModel.GPT_4O)
                .maxCompletionTokens(2048)
                .addTool(GetSdkQuality.class)
                .addUserMessage("How good are the following SDKs: OpenAI Java SDK, Unknown Company SDK")
                .build();

        client.chat().completions().create(createParams).choices().stream()
                .map(ChatCompletion.Choice::message)
                .flatMap(message -> {
                    message.content().ifPresent(System.out::println);
                    Optional<List<ChatCompletionMessageToolCall>> toolCalls = message.toolCalls();
                    if (toolCalls.isPresent()) {
                        return toolCalls.get().stream();
                    } else {
                        return Stream.empty();
                    }
                })
                .forEach(toolCall -> {
                    Object result = callFunction(toolCall.asFunction().function());
                    System.out.println(result);
                });
    }

    private static Object callFunction(ChatCompletionMessageFunctionToolCall.Function function) {
        switch (function.name()) {
            case "GetSdkQuality":
                return function.arguments(GetSdkQuality.class).execute();
            default:
                throw new IllegalArgumentException("Unknown function: " + function.name());
        }
    }
}
