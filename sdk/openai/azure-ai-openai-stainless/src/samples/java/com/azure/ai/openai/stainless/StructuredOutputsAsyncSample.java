// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.stainless;

import com.azure.identity.AuthenticationUtil;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.client.OpenAIClientAsync;
import com.openai.client.okhttp.OpenAIOkHttpClientAsync;
import com.openai.core.JsonValue;
import com.openai.credential.BearerTokenCredential;
import com.openai.models.ChatModel;
import com.openai.models.ResponseFormatJsonSchema;
import com.openai.models.ResponseFormatJsonSchema.JsonSchema;
import com.openai.models.chat.completions.ChatCompletionCreateParams;

import java.util.Collections;

public final class StructuredOutputsAsyncSample {
    private StructuredOutputsAsyncSample() {}

    public static void main(String[] args) {
        // Configures using one of:
        // - The `OPENAI_API_KEY` environment variable
        // - The `AZURE_OPENAI_ENDPOINT` and `AZURE_OPENAI_KEY` environment variables
        OpenAIOkHttpClientAsync.Builder clientBuilder = OpenAIOkHttpClientAsync.builder();

        /* Azure-specific code starts here */
        // You can either set 'endpoint' or 'apiKey' directly in the builder.
        // or set same two env vars and use fromEnv() method instead
        clientBuilder
            .baseUrl(System.getenv("AZURE_OPENAI_ENDPOINT"))
            .credential(BearerTokenCredential.create(AuthenticationUtil.getBearerTokenSupplier(
                new DefaultAzureCredentialBuilder().build(), "https://cognitiveservices.azure.com/.default")));
        /* Azure-specific code ends here */

        // All code from this line down is general-purpose OpenAI code
        OpenAIClientAsync client = clientBuilder.build();

        // TODO: Update this once we support extracting JSON schemas from Java classes
        JsonSchema.Schema schema = JsonSchema.Schema.builder()
                .putAdditionalProperty("type", JsonValue.from("object"))
                .putAdditionalProperty(
                        "properties", JsonValue.from(Collections.singletonMap("employees", Collections.singletonMap("items", Collections.singletonMap("type", "string")))))
                .build();
        ChatCompletionCreateParams createParams = ChatCompletionCreateParams.builder()
                .model(ChatModel.GPT_4O)
                .maxCompletionTokens(2048)
                .responseFormat(ResponseFormatJsonSchema.builder()
                        .jsonSchema(JsonSchema.builder()
                                .name("employee-list")
                                .schema(schema)
                                .build())
                        .build())
                .addUserMessage("Who works at OpenAI?")
                .build();

        client.chat()
                .completions()
                .create(createParams)
                .thenAccept(completion -> completion.choices().forEach(
                    choice -> choice.message().content().ifPresent(System.out::println)))
                .join();
    }
}
