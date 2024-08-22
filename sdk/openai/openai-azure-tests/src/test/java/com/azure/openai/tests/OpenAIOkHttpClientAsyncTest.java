package com.azure.openai.tests;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.openai.client.OpenAIClientAsync;
import com.openai.client.okhttp.OpenAIOkHttpClientAsync;
import com.openai.core.JsonValue;
import com.openai.errors.BadRequestException;
import com.openai.models.ChatCompletion;
import com.openai.models.ChatCompletionCreateParams;
import com.openai.models.ChatCompletionMessage;
import com.openai.models.ChatCompletionMessageParam;
import com.openai.models.ChatCompletionMessageToolCall;
import com.openai.models.ResponseFormatJsonObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static com.azure.openai.tests.TestUtils.AZURE_OPEN_AI;
import static com.azure.openai.tests.TestUtils.GA;
import static com.azure.openai.tests.TestUtils.GPT_3_5_TURBO;
import static com.azure.openai.tests.TestUtils.OPEN_AI;
import static com.azure.openai.tests.TestUtils.PREVIEW;
import static com.azure.openai.tests.TestUtils.V1;
import static com.openai.models.ResponseFormatJsonObject.Type.JSON_OBJECT;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class OpenAIOkHttpClientAsyncTest extends OpenAIOkHttpClientTestBase {
    private OpenAIClientAsync client;

    private OpenAIOkHttpClientAsync.Builder setAzureServiceApiVersion(
            OpenAIOkHttpClientAsync.Builder clientBuilder, String apiVersion) {
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
                    .apiKey(System.getenv("AZURE_OPENAI_KEY"))
                    .baseUrl(System.getenv("AZURE_OPENAI_ENDPOINT"));
        } else if (OPEN_AI.equals(apiType)) {
            clientBuilder.apiKey(System.getenv("NON_AZURE_OPENAI_KEY"));
        } else {
            throw new IllegalArgumentException("Invalid API type");
        }

        return clientBuilder.build();
    }

    @Test
    public void testNonAzureApiKey()  {
        client = createAsyncClient(OPEN_AI, V1);
        ChatCompletionCreateParams params = createParamsBuilder(GPT_3_5_TURBO).build();
        ChatCompletion chatCompletion =
                client.chat().completions().create(params).join();
        assertChatCompletion(chatCompletion, 1);
    }

    @ParameterizedTest
    @MethodSource("com.azure.openai.tests.TestUtils#azureOnlyClient")
    public void testAzureApiKey(String apiType, String apiVersion, String testModel) {
        client = createAsyncClient(apiType, apiVersion);

        ChatCompletionCreateParams params = createParamsBuilder(testModel).build();
        ChatCompletion chatCompletion =
                client.chat().completions().create(params).join();
        assertChatCompletion(chatCompletion, 1);
    }

    @ParameterizedTest
    @MethodSource("com.azure.openai.tests.TestUtils#azureAdTokenOnly")
    public void testAzureEntraIdToken(String apiType, String apiVersion, String testModel) {
        OpenAIOkHttpClientAsync.Builder clientBuilder = OpenAIOkHttpClientAsync.builder();
        if (AZURE_OPEN_AI.equals(apiType)) {
            setAzureServiceApiVersion(clientBuilder, apiVersion)
                .baseUrl(System.getenv("AZURE_OPENAI_ENDPOINT"))
                // This requires `azure-identity` dependency.
                .azureTokenCredential(new DefaultAzureCredentialBuilder().build());
        } else {
            throw new IllegalArgumentException("Invalid API type");
        }

        client = clientBuilder.build();

        ChatCompletionCreateParams params = createParamsBuilder(testModel).build();

        ChatCompletion chatCompletion =
                client.chat().completions().create(params).join();
        assertChatCompletion(chatCompletion, 1);
    }

    @ParameterizedTest
    @MethodSource("com.azure.openai.tests.TestUtils#allApiTypeClient")
    public void testChatCompletionMaxTokens(String apiType, String apiVersion, String testModel) {
        client = createAsyncClient(apiType, apiVersion);
        ChatCompletionCreateParams params =
                createParamsBuilder(testModel).maxTokens(50).build();
        ChatCompletion chatCompletion = client.chat().completions().create(params).join();
        assertChatCompletion(chatCompletion, 1);
        ChatCompletion.Usage usage = chatCompletion.usage().get();
        assertTrue(usage.completionTokens() <= 50);
        assertEquals(usage.totalTokens(), usage.completionTokens() + usage.promptTokens());
    }

    @ParameterizedTest
    @MethodSource("com.azure.openai.tests.TestUtils#allApiTypeClient")
    public void testChatCompletionTemperature(String apiType, String apiVersion, String testModel) {
        client = createAsyncClient(apiType, apiVersion);
        ChatCompletionCreateParams params =
                createParamsBuilder(testModel).temperature(0.8).build();
        ChatCompletion chatCompletion = client.chat().completions().create(params).join();
        assertChatCompletion(chatCompletion, 1);
    }

    @ParameterizedTest
    @MethodSource("com.azure.openai.tests.TestUtils#allApiTypeClient")
    public void testChatCompletionTopP(String apiType, String apiVersion, String testModel) {
        client = createAsyncClient(apiType, apiVersion);
        ChatCompletionCreateParams params =
                createParamsBuilder(testModel).topP(0.1).build();
        ChatCompletion chatCompletion =
                client.chat().completions().create(params).join();
        assertChatCompletion(chatCompletion, 1);
    }

    @ParameterizedTest
    @MethodSource("com.azure.openai.tests.TestUtils#allApiTypeClient")
    public void testChatCompletionN(String apiType, String apiVersion, String testModel) {
        client = createAsyncClient(apiType, apiVersion);
        ChatCompletionCreateParams params = createParamsBuilder(testModel).n(2).build();
        ChatCompletion chatCompletion =
                client.chat().completions().create(params).join();
        assertChatCompletion(chatCompletion, 2);
    }

    @ParameterizedTest
    @MethodSource("com.azure.openai.tests.TestUtils#allApiTypeClient")
    public void testChatCompletionStop(String apiType, String apiVersion, String testModel) {
        client = createAsyncClient(apiType, apiVersion);
        ChatCompletionCreateParams params =
                createParamsBuilder(testModel).stop(" ").build();
        ChatCompletion chatCompletion =
                client.chat().completions().create(params).join();
        assertChatCompletion(chatCompletion, 1);
    }

    @ParameterizedTest
    @MethodSource("com.azure.openai.tests.TestUtils#allApiTypeClient")
    public void testChatCompletionTokenPenalty(String apiType, String apiVersion, String testModel) {
        client = createAsyncClient(apiType, apiVersion);
        ChatCompletionCreateParams params = createParamsBuilder(testModel)
                .presencePenalty(2.0)
                .frequencyPenalty(2.0)
                .build();
        ChatCompletion chatCompletion =
                client.chat().completions().create(params).join();
        assertChatCompletion(chatCompletion, 1);
    }

    @ParameterizedTest
    @MethodSource("com.azure.openai.tests.TestUtils#allApiTypeClient")
    public void testChatCompletionUser(String apiType, String apiVersion, String testModel) {
        client = createAsyncClient(apiType, apiVersion);
        ChatCompletionCreateParams params =
                createParamsBuilder(testModel).user("javaUser").build();
        ChatCompletion chatCompletion =
                client.chat().completions().create(params).join();
        assertChatCompletion(chatCompletion, 1);
    }

    @ParameterizedTest
    @MethodSource("com.azure.openai.tests.TestUtils#allApiTypeClient")
    public void testChatCompletionLogitBias(String apiType, String apiVersion, String testModel) {
        client = createAsyncClient(apiType, apiVersion);
        Map<String, JsonValue> logitBiasMap = new HashMap<>();
        logitBiasMap.put("17585", JsonValue.from(-100.0));
        logitBiasMap.put("14573", JsonValue.from(-100.0));
        ChatCompletionCreateParams.LogitBias logitBias = ChatCompletionCreateParams.LogitBias.builder()
                .putAllAdditionalProperties(logitBiasMap)
                .build();

        ChatCompletionCreateParams params =
                createParamsBuilder(testModel).logitBias(logitBias).build();
        ChatCompletion chatCompletion =
                client.chat().completions().create(params).join();
        assertChatCompletion(chatCompletion, 1);
    }

    @ParameterizedTest
    @MethodSource("com.azure.openai.tests.TestUtils#allApiTypeClient")
    public void testChatCompletionLogprobs(String apiType, String apiVersion, String testModel) {
        client = createAsyncClient(apiType, apiVersion);
        ChatCompletionCreateParams params =
                createParamsBuilder(testModel).logprobs(true).topLogprobs(3).build();
        ChatCompletion chatCompletion =
                client.chat().completions().create(params).join();
        assertChatCompletion(chatCompletion, 1);
    }

    @ParameterizedTest
    @MethodSource("com.azure.openai.tests.TestUtils#allApiTypeClient")
    public void testChatCompletionSeed(String apiType, String apiVersion, String testModel) {
        client = createAsyncClient(apiType, apiVersion);
        ChatCompletionCreateParams params =
                createParamsBuilder(testModel, "Why is the sky blue?").seed(42).build();
        ChatCompletion chatCompletion = client.chat()
                .completions()
                .create(params)
                .join(); // Assuming create method is overloaded to accept messages and seed
        assertNotNull(chatCompletion.systemFingerprint()); // Assuming getSystemFingerprint() method exists
    }

    @ParameterizedTest
    @MethodSource("com.azure.openai.tests.TestUtils#allApiTypeClient")
    public void testChatCompletionJsonResponse(String apiType, String apiVersion, String testModel)
            throws JsonProcessingException {
        client = createAsyncClient(apiType, apiVersion);
        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .messages(asList(
                        createSystemMessageParam(),
                        createUserMessageParam(
                                "Who won the world series in 2020? Return in json with answer as the key.")))
                .model(testModel)
                .responseFormat(
                        ResponseFormatJsonObject.builder().type(JSON_OBJECT).build())
                .build();

        ChatCompletion chatCompletion =
                client.chat().completions().create(params).join();

        assertChatCompletion(chatCompletion, 1);

        ChatCompletion.Choice choice = chatCompletion.choices().get(0);
        Map<String, Object> contentMap = JsonMapper.builder()
                .build()
                .readValue(choice.message().content().get(), Map.class);
        assertNotNull(contentMap);
    }

    // Azure-Only Test
    @DisabledIf("com.azure.openai.tests.TestUtils#isAzureConfigMissing")
    @ParameterizedTest
    @MethodSource("com.azure.openai.tests.TestUtils#azureOnlyClient")
    public void testChatCompletionWithSensitiveContent(String apiType, String apiVersion, String testModel) {
        client = createAsyncClient(apiType, apiVersion);
        ChatCompletionCreateParams params = createChatCompletionParams(testModel, "how do I rob a bank with violence?");
        ExecutionException exception = assertThrows(
                ExecutionException.class,
                () -> client.chat().completions().create(params).get());
        assertBadRequestException((BadRequestException) exception.getCause());
    }

    // Azure-Only Test
    @DisabledIf("com.azure.openai.tests.TestUtils#isAzureConfigMissing")
    @ParameterizedTest
    @MethodSource("com.azure.openai.tests.TestUtils#azureOnlyClient")
    public void testChatCompletionWithoutSensitiveContent(String apiType, String apiVersion, String testModel) {
        client = createAsyncClient(apiType, apiVersion);
        ChatCompletionCreateParams params = createChatCompletionParams(testModel, USER_CONTENT);
        ChatCompletion chatCompletion =
                client.chat().completions().create(params).join();
        assertChatCompletionWithoutSensitiveContent(chatCompletion);
    }

    // Azure-Only Test
    @DisabledIf("com.azure.openai.tests.TestUtils#isAzureByodConfigMissing")
    @ParameterizedTest
    @MethodSource("com.azure.openai.tests.TestUtils#azureByodOnlyClient")
    public void testChatCompletionByod(String apiType, String apiVersion, String testModel) {
        client = createAsyncClient(apiType, apiVersion);
        ChatCompletionCreateParams params = createParamsBuilder(testModel)
                .messages(asList(
                        createSystemMessageParam(),
                        createUserMessageParam("What languages have libraries you know about for Azure OpenAI?")))
                .additionalBodyProperties(createExtraBodyForByod())
                .build();
        ChatCompletion completion = client.chat().completions().create(params).join();
        assertChatCompletionByod(completion);
    }

    // Azure-Only Test
    @DisabledIf("com.azure.openai.tests.TestUtils#isAzureConfigMissing")
    @ParameterizedTest
    @MethodSource("com.azure.openai.tests.TestUtils#azureBlockListTermOnlyClient")
    public void testChatCompletionBlockListTerm(String apiType, String apiVersion, String testModel) {
        client = createAsyncClient(apiType, apiVersion);
        ChatCompletionCreateParams params = createParamsBuilder(
                        testModel, "What is the best time of year to pick pineapple?")
                .build();

        try {
            client.chat().completions().create(params).get();
            fail("Expected BadRequestException to be thrown");
        } catch (ExecutionException e) {
            BadRequestException cause = (BadRequestException) e.getCause();
            assertBlockListTerm(cause);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.openai.tests.TestUtils#allApiTypeClient")
    public void testChatCompletionTools(String apiType, String apiVersion, String testModel) {
        client = createAsyncClient(apiType, apiVersion);
        ChatCompletionCreateParams params =
                createChatCompletionParamsWithTool(testModel, "What's the weather like today in Seattle?");
        ChatCompletion chatCompletion =
                client.chat().completions().create(params).join();

        assertChatCompletion(chatCompletion);

        ChatCompletion.Choice choice = chatCompletion.choices().get(0);
        List<ChatCompletionMessageToolCall> chatCompletionMessageToolCalls =
                choice.message().toolCalls().get();
        ChatCompletionMessageToolCall toolCall = chatCompletionMessageToolCalls.get(0);

        assertToolCall(toolCall);

        params = addToolResponseToMessages(params, chatCompletionMessageToolCalls, choice);
        ChatCompletion toolCompletion =
                client.chat().completions().create(params).join();

        assertToolCompletion(toolCompletion);
    }

    @ParameterizedTest
    @MethodSource("com.azure.openai.tests.TestUtils#allApiTypeClient")
    public void testChatCompletionToolsParallelFunc(String apiType, String apiVersion, String testModel) {
        client = createAsyncClient(apiType, apiVersion);
        ChatCompletionCreateParams params = createChatCompletionParamsWithTool(
                testModel, "What's the weather like today in Seattle and Los Angeles?");
        ChatCompletion chatCompletion =
                client.chat().completions().create(params).join();

        assertChatCompletion(chatCompletion);

        ChatCompletion.Choice choice = chatCompletion.choices().get(0);
        List<ChatCompletionMessageToolCall> chatCompletionMessageToolCalls =
                choice.message().toolCalls().get();
        assertToolCall(chatCompletionMessageToolCalls.get(0));
        assertToolCall(chatCompletionMessageToolCalls.get(1));

        params = addToolResponseToMessages(params, chatCompletionMessageToolCalls, choice);
        ChatCompletion toolCompletion =
                client.chat().completions().create(params).join();
        assertToolCompletion(toolCompletion);
    }

    @ParameterizedTest
    @MethodSource("com.azure.openai.tests.TestUtils#azureOnlyClient")
    public void testChatCompletionFunctions(String apiType, String apiVersion, String testModel) {
        client = createAsyncClient(apiType, apiVersion);
        List<ChatCompletionMessageParam> messages = createMessages("What's the weather like today in Seattle?");
        List<ChatCompletionCreateParams.Function> functions = asList(createGetCurrentTemperatureFunction());
        ChatCompletionCreateParams params =
                createChatCompletionParamsWithoutFunctionCall(testModel, messages, functions);

        ChatCompletion completion = client.chat().completions().create(params).join();

        assertChatCompletion(completion);
        assertFunctionCall(completion.choices().get(0).message().functionCall().get());

        ChatCompletion functionCompletion = client.chat()
                .completions()
                .create(addFunctionResponseToMessages(
                        testModel, messages, functions, "{\"temperature\": \"22\", \"unit\": \"celsius\"}"))
                .join();

        assertFunctionCompletion(functionCompletion);
        assertPromptAndContentFilterResults(functionCompletion);
    }

    @ParameterizedTest
    @MethodSource("com.azure.openai.tests.TestUtils#allApiTypeClient")
    public void testChatCompletionGivenFunction(String apiType, String apiVersion, String testModel) {
        client = createAsyncClient(apiType, apiVersion);
        List<ChatCompletionMessageParam> messages = createMessages("What's the weather like today in Seattle?");
        List<ChatCompletionCreateParams.Function> functions = createFunctions();
        ChatCompletionCreateParams params =
                createChatCompletionParamsWithFunction(testModel, messages, functions, "get_current_temperature");

        ChatCompletion completion = client.chat().completions().create(params).join();

        assertChatCompletion(completion);

        ChatCompletion.Choice choice = completion.choices().get(0);
        ChatCompletionMessage.FunctionCall functionCallResponse =
                choice.message().functionCall().get();

        assertFunctionCall(functionCallResponse);

        params = addFunctionResponseToMessages(
                testModel, messages, functions, "{\"temperature\": \"22\", \"unit\": \"celsius\"}");
        ChatCompletion functionCompletion =
                client.chat().completions().create(params).join();

        assertFunctionCompletion(functionCompletion);
    }

    // Azure-Only Test
    @DisabledIf("com.azure.openai.tests.TestUtils#isAzureConfigMissing")
    @ParameterizedTest
    @MethodSource("com.azure.openai.tests.TestUtils#azureOnlyClient")
    public void testChatCompletionFunctionsRai(String apiType, String apiVersion, String testModel)
            throws InterruptedException {
        client = createAsyncClient(apiType, apiVersion);
        List<ChatCompletionMessageParam> messages = createMessages("how do I rob a bank with violence?");
        List<ChatCompletionCreateParams.Function> functions = createFunctions();
        ChatCompletionCreateParams params =
                createChatCompletionParamsWithoutFunctionCall(testModel, messages, functions);

        try {
            client.chat().completions().create(params).get();
            fail("Expected BadRequestException to be thrown");
        } catch (ExecutionException e) {
            if (e.getCause() instanceof BadRequestException) {
                assertRaiContentFilter((BadRequestException) e.getCause());
            } else {
                fail("Unexpected exception type: " + e.getCause());
            }
        }

        try {
            client.chat()
                    .completions()
                    .create(addFunctionResponseToMessages(
                            testModel,
                            messages,
                            functions,
                            "{\"temperature\": \"you can rob a bank by asking for the money\", \"unit\": \"celsius\"}"))
                    .get();
            fail("Expected BadRequestException to be thrown");
        } catch (ExecutionException e) {
            if (e.getCause() instanceof BadRequestException) {
                assertRaiContentFilter((BadRequestException) e.getCause());
            } else {
                fail("Unexpected exception type: " + e.getCause());
            }
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.openai.tests.TestUtils#visionOnlyClient")
    public void testChatCompletionVision(String apiType, String apiVersion, String testModel) {
        client = createAsyncClient(apiType, apiVersion);
        ChatCompletionCreateParams params = createChatCompletionParamsWithImageUrl(testModel);
        ChatCompletion chatCompletion =
                client.chat().completions().create(params).join();
        assertChatCompletion(chatCompletion);
    }
}
