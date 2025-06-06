// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.stainless;

import static com.openai.core.ObjectMappers.jsonMapper;

import com.azure.identity.AuthenticationUtil;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.core.JsonObject;
import com.openai.core.JsonValue;
import com.openai.credential.BearerTokenCredential;
import com.openai.models.ChatModel;
import com.openai.models.FunctionDefinition;
import com.openai.models.FunctionParameters;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.ChatCompletionMessageToolCall;
import com.openai.models.chat.completions.ChatCompletionTool;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

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
                .addTool(ChatCompletionTool.builder()
                        .function(FunctionDefinition.builder()
                                .name("get-sdk-quality")
                                .description("Gets the quality of the given SDK.")
                                .parameters(FunctionParameters.builder()
                                        .putAdditionalProperty("type", JsonValue.from("object"))
                                        .putAdditionalProperty("properties", JsonValue.from(
                                            Collections.singletonMap("name", Collections.singletonMap("type", "string"))))
                                        .putAdditionalProperty("required", JsonValue.from(Collections.singletonList("name")))
                                        .putAdditionalProperty("additionalProperties", JsonValue.from(false))
                                        .build())
                                .build())
                        .build())
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
                .forEach(toolCall -> System.out.println(callFunction(toolCall.function())));
    }

    private static String callFunction(ChatCompletionMessageToolCall.Function function) {
        if (!function.name().equals("get-sdk-quality")) {
            throw new IllegalArgumentException("Unknown function: " + function.name());
        }

        JsonValue arguments;
        try {
            arguments = JsonValue.from(jsonMapper().readTree(function.arguments()));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Bad function arguments", e);
        }

        String sdkName = ((JsonObject) arguments).values().get("name").asStringOrThrow();
        if (sdkName.contains("OpenAI")) {
            return sdkName + ": It's robust and polished!";
        }

        return sdkName + ": *shrug*";
    }
}
