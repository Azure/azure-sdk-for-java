// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.stainless;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.openai.azure.credential.AzureApiKeyCredential;
import com.openai.client.OpenAIClientAsync;
import com.openai.client.okhttp.OpenAIOkHttpClientAsync;
import com.openai.core.JsonValue;
import com.openai.credential.BearerTokenCredential;
import com.openai.errors.BadRequestException;
import com.openai.models.ResponseFormatJsonObject;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.ChatCompletionMessage;
import com.openai.models.chat.completions.ChatCompletionMessageParam;
import com.openai.models.chat.completions.ChatCompletionMessageToolCall;
import com.openai.models.completions.CompletionUsage;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static com.azure.ai.openai.stainless.TestUtils.AZURE_OPEN_AI;
import static com.azure.ai.openai.stainless.TestUtils.GA;
import static com.azure.ai.openai.stainless.TestUtils.GPT_3_5_TURBO;
import static com.azure.ai.openai.stainless.TestUtils.OPEN_AI;
import static com.azure.ai.openai.stainless.TestUtils.PREVIEW;
import static com.azure.ai.openai.stainless.TestUtils.V1;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OpenAIOkHttpClientAsyncTest extends OpenAIOkHttpClientTestBase {
    private OpenAIClientAsync client;

    private OpenAIOkHttpClientAsync.Builder setAzureServiceApiVersion(OpenAIOkHttpClientAsync.Builder clientBuilder,
        String apiVersion) {
        if (GA.equals(apiVersion)) {
            clientBuilder.azureServiceVersion(AZURE_OPENAI_SERVICE_VERSION_GA);
        } else if (PREVIEW.equals(apiVersion)) {
            clientBuilder.azureServiceVersion(AZURE_OPENAI_SERVICE_VERSION_PREVIEW);
        } else {
            throw new IllegalArgumentException("Invalid Azure API version");
        }
        return clientBuilder;
    }

    private OpenAIClientAsync createAsyncClient(String apiType, String apiVersion) {
        OpenAIOkHttpClientAsync.Builder clientBuilder = OpenAIOkHttpClientAsync.builder();
        if (AZURE_OPEN_AI.equals(apiType)) {
            setAzureServiceApiVersion(clientBuilder, apiVersion)
                .credential(AzureApiKeyCredential.create(System.getenv("AZURE_OPENAI_KEY")))
                .baseUrl(getEndpoint());
        } else if (OPEN_AI.equals(apiType)) {
            clientBuilder.credential(BearerTokenCredential.create(System.getenv("NON_AZURE_OPENAI_KEY")));
        } else {
            throw new IllegalArgumentException("Invalid API type");
        }

        return clientBuilder.build();
    }

    @ParameterizedTest
    @MethodSource("com.azure.ai.openai.stainless.TestUtils#openAiOnlyClient")
    public void testNonAzureApiKey() {
        client = createAsyncClient(OPEN_AI, V1);
        ChatCompletionCreateParams params = createParamsBuilder(GPT_3_5_TURBO).build();
        ChatCompletion chatCompletion = client.chat().completions().create(params).join();
        assertChatCompletion(chatCompletion, 1);
    }

    @ParameterizedTest
    @MethodSource("com.azure.ai.openai.stainless.TestUtils#azureOnlyClient")
    public void testAzureApiKey(String apiType, String apiVersion, String testModel) {
        client = createAsyncClient(apiType, apiVersion);

        ChatCompletionCreateParams params = createParamsBuilder(testModel).build();
        ChatCompletion chatCompletion = client.chat().completions().create(params).join();
        assertChatCompletion(chatCompletion, 1);
    }

    @ParameterizedTest
    @MethodSource("com.azure.ai.openai.stainless.TestUtils#azureAdTokenOnly")
    public void testAzureEntraIdToken(String apiType, String apiVersion, String testModel) {
        OpenAIOkHttpClientAsync.Builder clientBuilder = OpenAIOkHttpClientAsync.builder();
        if (AZURE_OPEN_AI.equals(apiType)) {
            setAzureServiceApiVersion(clientBuilder, apiVersion).baseUrl(getEndpoint())
                // This requires `azure-identity` dependency.
                .credential(BearerTokenCredential.create(getBearerTokenCredentialProvider()));
        } else {
            throw new IllegalArgumentException("Invalid API type");
        }

        client = clientBuilder.build();

        ChatCompletionCreateParams params = createParamsBuilder(testModel).build();

        ChatCompletion chatCompletion = client.chat().completions().create(params).join();
        assertChatCompletion(chatCompletion, 1);
    }

    @ParameterizedTest
    @MethodSource("com.azure.ai.openai.stainless.TestUtils#allApiTypeClient")
    public void testChatCompletionMaxTokens(String apiType, String apiVersion, String testModel) {
        client = createAsyncClient(apiType, apiVersion);
        ChatCompletionCreateParams params = createParamsBuilder(testModel).maxTokens(50).build();
        ChatCompletion chatCompletion = client.chat().completions().create(params).join();
        assertChatCompletion(chatCompletion, 1);
        CompletionUsage usage = chatCompletion.usage().get();
        assertTrue(usage.completionTokens() <= 50);
        assertEquals(usage.totalTokens(), usage.completionTokens() + usage.promptTokens());
    }

    @ParameterizedTest
    @MethodSource("com.azure.ai.openai.stainless.TestUtils#allApiTypeClient")
    public void testChatCompletionTemperature(String apiType, String apiVersion, String testModel) {
        client = createAsyncClient(apiType, apiVersion);
        ChatCompletionCreateParams params = createParamsBuilder(testModel).temperature(0.8).build();
        ChatCompletion chatCompletion = client.chat().completions().create(params).join();
        assertChatCompletion(chatCompletion, 1);
    }

    @ParameterizedTest
    @MethodSource("com.azure.ai.openai.stainless.TestUtils#allApiTypeClient")
    public void testChatCompletionTopP(String apiType, String apiVersion, String testModel) {
        client = createAsyncClient(apiType, apiVersion);
        ChatCompletionCreateParams params = createParamsBuilder(testModel).topP(0.1).build();
        ChatCompletion chatCompletion = client.chat().completions().create(params).join();
        assertChatCompletion(chatCompletion, 1);
    }

    @ParameterizedTest
    @MethodSource("com.azure.ai.openai.stainless.TestUtils#allApiTypeClient")
    public void testChatCompletionN(String apiType, String apiVersion, String testModel) {
        client = createAsyncClient(apiType, apiVersion);
        ChatCompletionCreateParams params = createParamsBuilder(testModel).n(2).build();
        ChatCompletion chatCompletion = client.chat().completions().create(params).join();
        assertChatCompletion(chatCompletion, 2);
    }

    @ParameterizedTest
    @MethodSource("com.azure.ai.openai.stainless.TestUtils#allApiTypeClient")
    public void testChatCompletionStop(String apiType, String apiVersion, String testModel) {
        client = createAsyncClient(apiType, apiVersion);
        ChatCompletionCreateParams params = createParamsBuilder(testModel).stop(" ").build();
        ChatCompletion chatCompletion = client.chat().completions().create(params).join();
        assertChatCompletion(chatCompletion, 1);
    }

    @ParameterizedTest
    @MethodSource("com.azure.ai.openai.stainless.TestUtils#allApiTypeClient")
    public void testChatCompletionTokenPenalty(String apiType, String apiVersion, String testModel) {
        client = createAsyncClient(apiType, apiVersion);
        ChatCompletionCreateParams params
            = createParamsBuilder(testModel).presencePenalty(2.0).frequencyPenalty(2.0).build();
        ChatCompletion chatCompletion = client.chat().completions().create(params).join();
        assertChatCompletion(chatCompletion, 1);
    }

    @ParameterizedTest
    @MethodSource("com.azure.ai.openai.stainless.TestUtils#allApiTypeClient")
    public void testChatCompletionUser(String apiType, String apiVersion, String testModel) {
        client = createAsyncClient(apiType, apiVersion);
        ChatCompletionCreateParams params = createParamsBuilder(testModel).user("javaUser").build();
        ChatCompletion chatCompletion = client.chat().completions().create(params).join();
        assertChatCompletion(chatCompletion, 1);
    }

    @ParameterizedTest
    @MethodSource("com.azure.ai.openai.stainless.TestUtils#allApiTypeClient")
    public void testChatCompletionLogitBias(String apiType, String apiVersion, String testModel) {
        client = createAsyncClient(apiType, apiVersion);
        Map<String, JsonValue> logitBiasMap = new HashMap<>();
        logitBiasMap.put("17585", JsonValue.from(-100.0));
        logitBiasMap.put("14573", JsonValue.from(-100.0));
        ChatCompletionCreateParams.LogitBias logitBias
            = ChatCompletionCreateParams.LogitBias.builder().putAllAdditionalProperties(logitBiasMap).build();

        ChatCompletionCreateParams params = createParamsBuilder(testModel).logitBias(logitBias).build();
        ChatCompletion chatCompletion = client.chat().completions().create(params).join();
        assertChatCompletion(chatCompletion, 1);
    }

    @ParameterizedTest
    @MethodSource("com.azure.ai.openai.stainless.TestUtils#allApiTypeClient")
    public void testChatCompletionLogprobs(String apiType, String apiVersion, String testModel) {
        client = createAsyncClient(apiType, apiVersion);
        ChatCompletionCreateParams params = createParamsBuilder(testModel).logprobs(true).topLogprobs(3).build();
        ChatCompletion chatCompletion = client.chat().completions().create(params).join();
        assertChatCompletion(chatCompletion, 1);
    }

    @ParameterizedTest
    @MethodSource("com.azure.ai.openai.stainless.TestUtils#allApiTypeClient")
    public void testChatCompletionSeed(String apiType, String apiVersion, String testModel) {
        client = createAsyncClient(apiType, apiVersion);
        ChatCompletionCreateParams params = createParamsBuilder(testModel, "Why is the sky blue?").seed(42).build();
        ChatCompletion chatCompletion = client.chat().completions().create(params).join(); // Assuming create method is overloaded to accept messages and seed
        assertNotNull(chatCompletion.systemFingerprint()); // Assuming getSystemFingerprint() method exists
    }

    @ParameterizedTest
    @MethodSource("com.azure.ai.openai.stainless.TestUtils#allApiTypeClient")
    public void testChatCompletionJsonResponse(String apiType, String apiVersion, String testModel)
        throws JsonProcessingException {
        client = createAsyncClient(apiType, apiVersion);
        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
            .messages(asList(createSystemMessageParam(),
                createUserMessageParam("Who won the world series in 2020? Return in json with answer as the key.")))
            .model(testModel)
            .responseFormat(ResponseFormatJsonObject.builder().type(JsonValue.from("json_object")).build())
            .build();

        ChatCompletion chatCompletion = client.chat().completions().create(params).join();

        assertChatCompletion(chatCompletion, 1);

        ChatCompletion.Choice choice = chatCompletion.choices().get(0);
        assertNotNull(JsonMapper.builder().build().readValue(choice.message().content().get(), Map.class));
    }

    @ParameterizedTest
    @MethodSource("com.azure.ai.openai.stainless.TestUtils#azureOnlyClient")
    public void testChatCompletionWithSensitiveContent(String apiType, String apiVersion, String testModel) {
        client = createAsyncClient(apiType, apiVersion);
        ChatCompletionCreateParams params = createChatCompletionParams(testModel, "how do I rob a bank with violence?");

        assertThrows(ExecutionException.class, () -> client.chat().completions().create(params).get());
    }

    @ParameterizedTest
    @MethodSource("com.azure.ai.openai.stainless.TestUtils#azureOnlyClient")
    public void testChatCompletionWithoutSensitiveContent(String apiType, String apiVersion, String testModel) {
        client = createAsyncClient(apiType, apiVersion);
        ChatCompletionCreateParams params = createChatCompletionParams(testModel, USER_CONTENT);
        ChatCompletion chatCompletion = client.chat().completions().create(params).join();
        assertChatCompletionWithoutSensitiveContent(chatCompletion);
    }

    @ParameterizedTest
    @MethodSource("com.azure.ai.openai.stainless.TestUtils#azureOnlyClient")
    public void testChatCompletionByod(String apiType, String apiVersion, String testModel) {
        client = createAsyncClient(apiType, apiVersion);
        ChatCompletionCreateParams params = createParamsBuilder(testModel)
            .messages(asList(createSystemMessageParam(),
                createUserMessageParam("What languages have libraries you know about for Azure OpenAI?")))
            .additionalBodyProperties(createExtraBodyForByod())
            .build();
        ChatCompletion completion = client.chat().completions().create(params).join();
        assertChatCompletionByod(completion);
    }

    @ParameterizedTest
    @MethodSource("com.azure.ai.openai.stainless.TestUtils#allApiTypeClient")
    public void testChatCompletionTools(String apiType, String apiVersion, String testModel) {
        client = createAsyncClient(apiType, apiVersion);
        ChatCompletionCreateParams params
            = createChatCompletionParamsWithTool(testModel, "What's the weather like today in Seattle?");
        ChatCompletion chatCompletion = client.chat().completions().create(params).join();

        assertChatCompletion(chatCompletion);

        ChatCompletion.Choice choice = chatCompletion.choices().get(0);
        List<ChatCompletionMessageToolCall> chatCompletionMessageToolCalls = choice.message().toolCalls().get();
        ChatCompletionMessageToolCall toolCall = chatCompletionMessageToolCalls.get(0);

        assertToolCall(toolCall);

        params = addToolResponseToMessages(params, chatCompletionMessageToolCalls, choice);
        ChatCompletion toolCompletion = client.chat().completions().create(params).join();

        assertToolCompletion(toolCompletion);
    }

    @ParameterizedTest
    @MethodSource("com.azure.ai.openai.stainless.TestUtils#allApiTypeClient")
    public void testChatCompletionToolsParallelFunc(String apiType, String apiVersion, String testModel) {
        client = createAsyncClient(apiType, apiVersion);
        ChatCompletionCreateParams params = createChatCompletionParamsWithTool(testModel,
            "What's the weather like today in Seattle and Los Angeles?");
        ChatCompletion chatCompletion = client.chat().completions().create(params).join();

        assertChatCompletion(chatCompletion);

        ChatCompletion.Choice choice = chatCompletion.choices().get(0);
        List<ChatCompletionMessageToolCall> chatCompletionMessageToolCalls = choice.message().toolCalls().get();
        assertToolCall(chatCompletionMessageToolCalls.get(0));
        assertToolCall(chatCompletionMessageToolCalls.get(1));

        params = addToolResponseToMessages(params, chatCompletionMessageToolCalls, choice);
        ChatCompletion toolCompletion = client.chat().completions().create(params).join();
        assertToolCompletion(toolCompletion);
    }

    @ParameterizedTest
    @MethodSource("com.azure.ai.openai.stainless.TestUtils#azureOnlyClient")
    public void testChatCompletionFunctions(String apiType, String apiVersion, String testModel) {
        client = createAsyncClient(apiType, apiVersion);
        List<ChatCompletionMessageParam> messages = createMessages("What's the weather like today in Seattle?");
        List<ChatCompletionCreateParams.Function> functions = asList(createGetCurrentTemperatureFunction());
        ChatCompletionCreateParams params
            = createChatCompletionParamsWithoutFunctionCall(testModel, messages, functions);

        ChatCompletion completion = client.chat().completions().create(params).join();

        assertChatCompletion(completion);
        assertEquals(ChatCompletion.Choice.FinishReason.FUNCTION_CALL, completion.choices().get(0).finishReason());
        assertFunctionCall(completion.choices().get(0).message().functionCall().get());

        ChatCompletion functionCompletion = client.chat()
            .completions()
            .create(addFunctionResponseToMessages(testModel, messages, functions,
                "{\"temperature\": \"22\", \"unit\": \"celsius\"}"))
            .join();

        assertFunctionCompletion(functionCompletion);
        assertPromptAndContentFilterResults(functionCompletion);
    }

    @Disabled("Deprecated feature not working.")
    @ParameterizedTest
    @MethodSource("com.azure.ai.openai.stainless.TestUtils#allApiTypeClient")
    public void testChatCompletionGivenFunction(String apiType, String apiVersion, String testModel) {
        client = createAsyncClient(apiType, apiVersion);
        List<ChatCompletionMessageParam> messages = createMessages("What's the weather like today in Seattle?");
        List<ChatCompletionCreateParams.Function> functions = createFunctions();
        ChatCompletionCreateParams params
            = createChatCompletionParamsWithoutFunctionCall(testModel, messages, functions);

        ChatCompletion completion = client.chat().completions().create(params).join();

        assertChatCompletion(completion);

        ChatCompletion.Choice choice = completion.choices().get(0);
        ChatCompletionMessage.FunctionCall functionCallResponse = choice.message().functionCall().get();

        assertFunctionCall(functionCallResponse);

        params = addFunctionResponseToMessages(testModel, messages, functions,
            "{\"temperature\": \"22\", \"unit\": \"celsius\"}");
        ChatCompletion functionCompletion = client.chat().completions().create(params).join();

        assertFunctionCompletion(functionCompletion);
    }

    @DisabledIf("com.azure.ai.openai.stainless.TestUtils#isAzureConfigMissing")
    @ParameterizedTest
    @MethodSource("com.azure.ai.openai.stainless.TestUtils#azureOnlyClient")
    public void testChatCompletionFunctionsRai(String apiType, String apiVersion, String testModel) {
        client = createAsyncClient(apiType, apiVersion);
        List<ChatCompletionMessageParam> messages = createMessages("how do I rob a bank with violence?");
        List<ChatCompletionCreateParams.Function> functions = createFunctions();
        ChatCompletionCreateParams params
            = createChatCompletionParamsWithoutFunctionCall(testModel, messages, functions);

        ExecutionException thrownException
            = assertThrows(ExecutionException.class, () -> client.chat().completions().create(params).get());
        BadRequestException causeException = assertInstanceOf(BadRequestException.class, thrownException.getCause());
        assertRaiContentFilter(causeException);
    }

    @ParameterizedTest
    @MethodSource("com.azure.ai.openai.stainless.TestUtils#visionOnlyClient")
    public void testChatCompletionVision(String apiType, String apiVersion, String testModel) {
        client = createAsyncClient(apiType, apiVersion);
        ChatCompletionCreateParams params = createChatCompletionParamsWithImageUrl(testModel);
        ChatCompletion chatCompletion = client.chat().completions().create(params).join();
        assertChatCompletion(chatCompletion);
    }

    @ParameterizedTest
    @MethodSource("com.azure.ai.openai.stainless.TestUtils#azureOnlyClient")
    public void chatCompletionSecurityContext(String apiType, String apiVersion, String testModel) {
        client = createAsyncClient(apiType, apiVersion);
        HashMap<String, JsonValue> userSecurityContextFields = new HashMap<>();
        userSecurityContextFields.put("end_user_id", JsonValue.from(UUID.randomUUID().toString()));
        userSecurityContextFields.put("source_ip", JsonValue.from("123.456.78.9"));

        HashMap<String, JsonValue> userSecurityContext = new HashMap<>();
        userSecurityContext.put("user_security_context", JsonValue.from(userSecurityContextFields));

        ChatCompletionCreateParams params
            = createChatCompletionParamsBuilder(testModel, "Hello, world").additionalBodyProperties(userSecurityContext)
                .build();

        ChatCompletion chatCompletion = client.chat().completions().create(params).join();
        assertChatCompletion(chatCompletion);
    }

    @ParameterizedTest
    @MethodSource("com.azure.ai.openai.stainless.TestUtils#allApiTypeClient")
    public void chatCompletionStream(String apiType, String apiVersion, String testModel) {
        client = createAsyncClient(apiType, apiVersion);

        StringBuilder responseBuilder = new StringBuilder();
        client.chat()
            .completions()
            .createStreaming(createChatCompletionParams(testModel,
                "Tell a story about a cat and a dog who are best friends. "
                    + "It should be at least 100 words long, but at most 120. Be strict about these limits."))
            .subscribe(it -> {
                String a = !it.choices().isEmpty() ? it.choices().get(0).delta().content().orElse("") : "";
                responseBuilder.append(a);
            })
            .onCompleteFuture()
            .join();

        int roughWordCount = responseBuilder.toString().split(" ").length;

        // making the boundaries less strict to prevent live test flakiness
        assertTrue(roughWordCount > 50 && roughWordCount <= 200, "Response length: " + roughWordCount);
    }

    @ParameterizedTest
    @MethodSource("com.azure.ai.openai.stainless.TestUtils#azureOnlyClient")
    public void chatCompletionsStreamRaiTrigger(String apiType, String apiVersion, String testModel) {
        client = createAsyncClient(apiType, apiVersion);

        Throwable throwable = client.chat()
            .completions()
            .createStreaming(createChatCompletionParams(testModel,
                "Give me a detailed tutorial on how to rob a bank with violence."))
            .subscribe(chatCompletionChunk -> {
            })
            .onCompleteFuture()
            .handle((unused, throwable1) -> throwable1)
            .join();
        BadRequestException thrownException = assertInstanceOf(BadRequestException.class, throwable.getCause());
        assertRaiContentFilter(thrownException);
    }
}
