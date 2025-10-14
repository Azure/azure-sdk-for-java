// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.stainless;

import com.azure.core.test.annotation.LiveOnly;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.openai.azure.credential.AzureApiKeyCredential;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.core.JsonValue;
import com.openai.core.http.StreamResponse;
import com.openai.credential.BearerTokenCredential;
import com.openai.errors.BadRequestException;
import com.openai.errors.NotFoundException;
import com.openai.models.ResponseFormatJsonObject;
import com.openai.models.ResponseFormatJsonSchema;
import com.openai.models.audio.transcriptions.Transcription;
import com.openai.models.audio.transcriptions.TranscriptionCreateParams;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionChunk;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.ChatCompletionMessage;
import com.openai.models.chat.completions.ChatCompletionMessageParam;
import com.openai.models.chat.completions.ChatCompletionMessageToolCall;
import com.openai.models.completions.CompletionUsage;
import com.openai.models.embeddings.CreateEmbeddingResponse;
import com.openai.models.embeddings.EmbeddingCreateParams;
import com.openai.models.images.Image;
import com.openai.models.images.ImageGenerateParams;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseStreamEvent;
import com.openai.models.responses.ResponseRetrieveParams;
import com.openai.models.responses.ResponseDeleteParams;
import com.openai.models.responses.ResponseInputItem;
import com.openai.models.responses.ResponseInputImage;
import com.openai.models.responses.EasyInputMessage;
import com.openai.models.responses.ResponseOutputMessage;
import com.openai.models.ResponseFormatJsonSchema.JsonSchema;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.Base64;
import java.util.Collections;
import java.util.ArrayList;

import static com.azure.ai.openai.stainless.TestUtils.AZURE_OPEN_AI;
import static com.azure.ai.openai.stainless.TestUtils.GA;
import static com.azure.ai.openai.stainless.TestUtils.OPEN_AI;
import static com.azure.ai.openai.stainless.TestUtils.PREVIEW;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

@LiveOnly
public class OpenAIOkHttpClientTest extends OpenAIOkHttpClientTestBase {
    private OpenAIClient client;

    private OpenAIOkHttpClient.Builder setAzureServiceApiVersion(OpenAIOkHttpClient.Builder clientBuilder,
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

    private OpenAIClient createClient(String apiType, String apiVersion) {
        OpenAIOkHttpClient.Builder clientBuilder = OpenAIOkHttpClient.builder();
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
    public void testNonAzureApiKey(String apiType, String apiVersion, String testModel) {
        client = createClient(apiType, apiVersion);
        ChatCompletionCreateParams params = createParamsBuilder(testModel).build();
        ChatCompletion chatCompletion = client.chat().completions().create(params);
        assertChatCompletion(chatCompletion, 1);
    }

    @ParameterizedTest
    @MethodSource("com.azure.ai.openai.stainless.TestUtils#azureOnlyClient")
    public void testAzureApiKey(String apiType, String apiVersion, String testModel) {
        client = createClient(apiType, apiVersion);

        ChatCompletionCreateParams params = createParamsBuilder(testModel).build();
        ChatCompletion chatCompletion = client.chat().completions().create(params);
        assertChatCompletion(chatCompletion, 1);
    }

    @ParameterizedTest
    @MethodSource("com.azure.ai.openai.stainless.TestUtils#azureAdTokenOnly")
    public void testAzureEntraIdToken(String apiType, String apiVersion, String testModel) {
        OpenAIOkHttpClient.Builder clientBuilder = OpenAIOkHttpClient.builder();
        if (AZURE_OPEN_AI.equals(apiType)) {
            setAzureServiceApiVersion(clientBuilder, apiVersion).baseUrl(getEndpoint())
                // This requires `azure-identity` dependency.
                .credential(BearerTokenCredential.create(getBearerTokenCredentialProvider()));
        } else {
            throw new IllegalArgumentException("Invalid API type");
        }

        client = clientBuilder.build();

        ChatCompletionCreateParams params = createParamsBuilder(testModel).build();

        ChatCompletion chatCompletion = client.chat().completions().create(params);
        assertChatCompletion(chatCompletion, 1);
    }

    @ParameterizedTest
    @MethodSource("com.azure.ai.openai.stainless.TestUtils#allApiTypeClient")
    public void testChatCompletionMaxTokens(String apiType, String apiVersion, String testModel) {
        client = createClient(apiType, apiVersion);

        ChatCompletionCreateParams params = createParamsBuilder(testModel).maxTokens(50).build();
        ChatCompletion chatCompletion = client.chat().completions().create(params);
        assertChatCompletion(chatCompletion, 1);
        CompletionUsage usage = chatCompletion.usage().get();
        assertTrue(usage.completionTokens() <= 50);
        assertEquals(usage.totalTokens(), usage.completionTokens() + usage.promptTokens());
    }

    @ParameterizedTest
    @MethodSource("com.azure.ai.openai.stainless.TestUtils#allApiTypeClient")
    public void testChatCompletionTemperature(String apiType, String apiVersion, String testModel) {
        client = createClient(apiType, apiVersion);

        ChatCompletionCreateParams params = createParamsBuilder(testModel).temperature(0.8).build();
        ChatCompletion chatCompletion = client.chat().completions().create(params);
        assertChatCompletion(chatCompletion, 1);
    }

    @ParameterizedTest
    @MethodSource("com.azure.ai.openai.stainless.TestUtils#allApiTypeClient")
    public void testChatCompletionTopP(String apiType, String apiVersion, String testModel) {
        client = createClient(apiType, apiVersion);

        ChatCompletionCreateParams params = createParamsBuilder(testModel).topP(0.1).build();
        ChatCompletion chatCompletion = client.chat().completions().create(params);
        assertChatCompletion(chatCompletion, 1);
    }

    @ParameterizedTest
    @MethodSource("com.azure.ai.openai.stainless.TestUtils#allApiTypeClient")
    public void testChatCompletionN(String apiType, String apiVersion, String testModel) {
        client = createClient(apiType, apiVersion);

        ChatCompletionCreateParams params = createParamsBuilder(testModel).n(2).build();
        ChatCompletion chatCompletion = client.chat().completions().create(params);
        assertChatCompletion(chatCompletion, 2);
    }

    @ParameterizedTest
    @MethodSource("com.azure.ai.openai.stainless.TestUtils#allApiTypeClient")
    public void testChatCompletionStop(String apiType, String apiVersion, String testModel) {
        client = createClient(apiType, apiVersion);

        ChatCompletionCreateParams params = createParamsBuilder(testModel).stop(" ").build();
        ChatCompletion chatCompletion = client.chat().completions().create(params);
        assertChatCompletion(chatCompletion, 1);
    }

    @ParameterizedTest
    @MethodSource("com.azure.ai.openai.stainless.TestUtils#allApiTypeClient")
    public void testChatCompletionTokenPenalty(String apiType, String apiVersion, String testModel) {
        client = createClient(apiType, apiVersion);

        ChatCompletionCreateParams params
            = createParamsBuilder(testModel).presencePenalty(2.0).frequencyPenalty(2.0).build();
        ChatCompletion chatCompletion = client.chat().completions().create(params);
        assertChatCompletion(chatCompletion, 1);
    }

    @ParameterizedTest
    @MethodSource("com.azure.ai.openai.stainless.TestUtils#allApiTypeClient")
    public void testChatCompletionUser(String apiType, String apiVersion, String testModel) {
        client = createClient(apiType, apiVersion);

        ChatCompletionCreateParams params = createParamsBuilder(testModel).user("javaUser").build();
        ChatCompletion chatCompletion = client.chat().completions().create(params);
        assertChatCompletion(chatCompletion, 1);
    }

    @ParameterizedTest
    @MethodSource("com.azure.ai.openai.stainless.TestUtils#allApiTypeClient")
    public void testChatCompletionLogitBias(String apiType, String apiVersion, String testModel) {
        client = createClient(apiType, apiVersion);

        Map<String, JsonValue> logitBiasMap = new HashMap<>();
        logitBiasMap.put("17585", JsonValue.from(-100.0));
        logitBiasMap.put("14573", JsonValue.from(-100.0));
        ChatCompletionCreateParams.LogitBias logitBias
            = ChatCompletionCreateParams.LogitBias.builder().putAllAdditionalProperties(logitBiasMap).build();

        ChatCompletionCreateParams params = createParamsBuilder(testModel).logitBias(logitBias).build();
        ChatCompletion chatCompletion = client.chat().completions().create(params);
        assertChatCompletion(chatCompletion, 1);
    }

    @ParameterizedTest
    @MethodSource("com.azure.ai.openai.stainless.TestUtils#allApiTypeClient")
    public void testChatCompletionLogprobs(String apiType, String apiVersion, String testModel) {
        client = createClient(apiType, apiVersion);

        ChatCompletionCreateParams params = createParamsBuilder(testModel).logprobs(true).topLogprobs(3).build();
        ChatCompletion chatCompletion = client.chat().completions().create(params);
        assertChatCompletion(chatCompletion, 1);
    }

    @ParameterizedTest
    @MethodSource("com.azure.ai.openai.stainless.TestUtils#allApiTypeClient")
    public void testChatCompletionSeed(String apiType, String apiVersion, String testModel) {
        client = createClient(apiType, apiVersion);

        ChatCompletionCreateParams params = createParamsBuilder(testModel, "Why is the sky blue?").seed(42).build();
        ChatCompletion chatCompletion = client.chat().completions().create(params); // Assuming create method is overloaded to accept messages and seed
        assertNotNull(chatCompletion.systemFingerprint()); // Assuming getSystemFingerprint() method exists
    }

    @ParameterizedTest
    @MethodSource("com.azure.ai.openai.stainless.TestUtils#allApiTypeClient")
    public void testChatCompletionJsonResponse(String apiType, String apiVersion, String testModel)
        throws JsonProcessingException {
        client = createClient(apiType, apiVersion);

        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
            .messages(asList(createSystemMessageParam(),
                createUserMessageParam("Who won the world series in 2020? Return in json with answer as the key.")))
            .model(testModel)
            .responseFormat(ResponseFormatJsonObject.builder().type(JsonValue.from("json_object")).build())
            .build();

        ChatCompletion chatCompletion = client.chat().completions().create(params);

        assertChatCompletion(chatCompletion, 1);

        ChatCompletion.Choice choice = chatCompletion.choices().get(0);
        assertNotNull(JsonMapper.builder().build().readValue(choice.message().content().get(), Map.class));
    }

    @ParameterizedTest
    @MethodSource("com.azure.ai.openai.stainless.TestUtils#azureOnlyClient")
    public void testChatCompletionWithSensitiveContent(String apiType, String apiVersion, String testModel) {
        client = createClient(apiType, apiVersion);

        ChatCompletionCreateParams params = createChatCompletionParams(testModel, "how do I rob a bank with violence?");
        BadRequestException thrownException
            = assertThrows(BadRequestException.class, () -> client.chat().completions().create(params));
        assertRaiContentFilter(thrownException);
    }

    @ParameterizedTest
    @MethodSource("com.azure.ai.openai.stainless.TestUtils#azureOnlyClient")
    public void testChatCompletionWithoutSensitiveContent(String apiType, String apiVersion, String testModel) {
        client = createClient(apiType, apiVersion);

        ChatCompletionCreateParams params = createChatCompletionParams(testModel, USER_CONTENT);
        ChatCompletion chatCompletion = client.chat().completions().create(params);
        assertChatCompletionWithoutSensitiveContent(chatCompletion);
    }

    @Disabled

    @ParameterizedTest
    @MethodSource("com.azure.ai.openai.stainless.TestUtils#azureOnlyClient")
    public void testChatCompletionByod(String apiType, String apiVersion, String testModel) {
        client = createClient(apiType, apiVersion);
        ChatCompletionCreateParams params = createParamsBuilder(testModel)
            .messages(asList(createSystemMessageParam(),
                createUserMessageParam("What do most contributions require you to do?")))
            .additionalBodyProperties(createExtraBodyForByod())
            .build();
        ChatCompletion completion = client.chat().completions().create(params);
        assertChatCompletionByod(completion);
    }

    @ParameterizedTest
    @MethodSource("com.azure.ai.openai.stainless.TestUtils#allApiTypeClient")
    public void testChatCompletionTools(String apiType, String apiVersion, String testModel) {
        client = createClient(apiType, apiVersion);

        ChatCompletionCreateParams params
            = createChatCompletionParamsWithTool(testModel, "What's the weather like today in Seattle?");
        ChatCompletion chatCompletion = client.chat().completions().create(params);

        assertChatCompletion(chatCompletion);

        ChatCompletion.Choice choice = chatCompletion.choices().get(0);
        List<ChatCompletionMessageToolCall> chatCompletionMessageToolCalls = choice.message().toolCalls().get();
        ChatCompletionMessageToolCall toolCall = chatCompletionMessageToolCalls.get(0);

        assertToolCall(toolCall);

        params = addToolResponseToMessages(params, chatCompletionMessageToolCalls, choice);
        ChatCompletion toolCompletion = client.chat().completions().create(params);

        assertToolCompletion(toolCompletion);
    }

    @ParameterizedTest
    @MethodSource("com.azure.ai.openai.stainless.TestUtils#allApiTypeClient")
    public void testChatCompletionToolsParallelFunc(String apiType, String apiVersion, String testModel) {
        client = createClient(apiType, apiVersion);

        ChatCompletionCreateParams params = createChatCompletionParamsWithTool(testModel,
            "What's the weather like today in Seattle and Los Angeles?");
        ChatCompletion chatCompletion = client.chat().completions().create(params);

        assertChatCompletion(chatCompletion);

        ChatCompletion.Choice choice = chatCompletion.choices().get(0);
        List<ChatCompletionMessageToolCall> chatCompletionMessageToolCalls = choice.message().toolCalls().get();
        assertToolCall(chatCompletionMessageToolCalls.get(0));
        assertToolCall(chatCompletionMessageToolCalls.get(1));

        params = addToolResponseToMessages(params, chatCompletionMessageToolCalls, choice);
        ChatCompletion toolCompletion = client.chat().completions().create(params);
        assertToolCompletion(toolCompletion);
    }

    @ParameterizedTest
    @MethodSource("com.azure.ai.openai.stainless.TestUtils#azureOnlyClient")
    public void testChatCompletionFunctions(String apiType, String apiVersion, String testModel) {
        client = createClient(apiType, apiVersion);

        List<ChatCompletionMessageParam> messages = createMessages("What's the weather like today in Seattle?");
        List<ChatCompletionCreateParams.Function> functions = asList(createGetCurrentTemperatureFunction());
        ChatCompletionCreateParams params
            = createChatCompletionParamsWithoutFunctionCall(testModel, messages, functions);

        ChatCompletion completion = client.chat().completions().create(params);

        assertChatCompletion(completion);
        assertEquals(ChatCompletion.Choice.FinishReason.FUNCTION_CALL, completion.choices().get(0).finishReason());
        assertFunctionCall(completion.choices().get(0).message().functionCall().get());

        ChatCompletion functionCompletion = client.chat()
            .completions()
            .create(addFunctionResponseToMessages(testModel, messages, functions,
                "{\"temperature\": \"22\", \"unit\": \"celsius\"}"));

        assertFunctionCompletion(functionCompletion);
        assertPromptAndContentFilterResults(functionCompletion);
    }

    @Disabled("Deprecated feature not working.")

    @ParameterizedTest
    @MethodSource("com.azure.ai.openai.stainless.TestUtils#allApiTypeClient")
    public void testChatCompletionGivenFunction(String apiType, String apiVersion, String testModel) {
        client = createClient(apiType, apiVersion);

        List<ChatCompletionMessageParam> messages = createMessages("What's the weather like today in Seattle?");
        List<ChatCompletionCreateParams.Function> functions = createFunctions();
        ChatCompletionCreateParams params
            = createChatCompletionParamsWithFunction(testModel, messages, functions, "get_current_temperature");

        ChatCompletion completion = client.chat().completions().create(params);

        assertChatCompletion(completion);

        ChatCompletion.Choice choice = completion.choices().get(0);
        ChatCompletionMessage.FunctionCall functionCallResponse = choice.message().functionCall().get();

        assertFunctionCall(functionCallResponse);

        ChatCompletion functionCompletion = client.chat()
            .completions()
            .create(addFunctionResponseToMessages(testModel, messages, functions,
                "{\"temperature\": \"22\", \"unit\": \"celsius\"}"));

        assertFunctionCompletion(functionCompletion);
    }

    @ParameterizedTest
    @MethodSource("com.azure.ai.openai.stainless.TestUtils#azureOnlyClient")
    public void testChatCompletionFunctionsRai(String apiType, String apiVersion, String testModel) {
        client = createClient(apiType, apiVersion);

        List<ChatCompletionMessageParam> messages = createMessages("how do I rob a bank with violence?");
        List<ChatCompletionCreateParams.Function> functions = createFunctions();
        ChatCompletionCreateParams params
            = createChatCompletionParamsWithoutFunctionCall(testModel, messages, functions);
        BadRequestException thrownException
            = assertThrows(BadRequestException.class, () -> client.chat().completions().create(params));
        assertRaiContentFilter(thrownException);
    }

    @ParameterizedTest
    @MethodSource("com.azure.ai.openai.stainless.TestUtils#visionOnlyClient")
    public void testChatCompletionVision(String apiType, String apiVersion, String testModel) {
        client = createClient(apiType, apiVersion);

        ChatCompletionCreateParams params = createChatCompletionParamsWithImageUrl(testModel);
        ChatCompletion chatCompletion = client.chat().completions().create(params);
        assertChatCompletion(chatCompletion);
    }

    @ParameterizedTest
    @MethodSource("com.azure.ai.openai.stainless.TestUtils#azureOnlyClient")
    public void chatCompletionSecurityContext(String apiType, String apiVersion, String testModel) {
        client = createClient(apiType, apiVersion);

        HashMap<String, JsonValue> userSecurityContextFields = new HashMap<>();
        userSecurityContextFields.put("end_user_id", JsonValue.from(UUID.randomUUID().toString()));
        userSecurityContextFields.put("source_ip", JsonValue.from("123.456.78.9"));

        HashMap<String, JsonValue> userSecurityContext = new HashMap<>();
        userSecurityContext.put("user_security_context", JsonValue.from(userSecurityContextFields));

        ChatCompletionCreateParams params
            = createChatCompletionParamsBuilder(testModel, "Hello, world").additionalBodyProperties(userSecurityContext)
                .build();

        ChatCompletion chatCompletion = client.chat().completions().create(params);
        assertChatCompletion(chatCompletion);
    }

    @ParameterizedTest
    @MethodSource("com.azure.ai.openai.stainless.TestUtils#allApiTypeClient")
    public void chatCompletionStream(String apiType, String apiVersion, String testModel) {
        client = createClient(apiType, apiVersion);

        StringBuilder responseBuilder = new StringBuilder();
        try (StreamResponse<ChatCompletionChunk> streamChunks = client.chat()
            .completions()
            .createStreaming(
                createChatCompletionParams(testModel, "Tell a story about a cat and a dog who are best friends. "
                    + "It should be at least 100 words long, but at most 120. Be strict about these limits."))) {
            streamChunks.stream()
                .map(it -> (!it.choices().isEmpty()) ? it.choices().get(0).delta().content().orElse("") : "")
                .forEach(responseBuilder::append);
        }
        int roughWordCount = responseBuilder.toString().split(" ").length;

        // making the boundaries less strict to prevent live test flakiness
        assertTrue(roughWordCount > 50 && roughWordCount <= 200, "Response length: " + roughWordCount);
    }

    @ParameterizedTest
    @MethodSource("com.azure.ai.openai.stainless.TestUtils#azureOnlyClient")
    public void chatCompletionsStreamRaiTrigger(String apiType, String apiVersion, String testModel) {
        client = createClient(apiType, apiVersion);

        BadRequestException thrownException = assertThrows(BadRequestException.class,
            () -> client.chat()
                .completions()
                .createStreaming(createChatCompletionParams(testModel,
                    "Give me a detailed tutorial on how to rob a bank with violence.")));
        assertRaiContentFilter(thrownException);
    }

    @ParameterizedTest
    @MethodSource("com.azure.ai.openai.stainless.TestUtils#azureOnlyClient")
    public void responsesReturnTextSuccessfully(String apiType, String apiVersion, String testModel) {
        client = createClient(apiType, apiVersion);

        ResponseCreateParams createParams = ResponseCreateParams.builder()
            .input("Tell me a story about building the best SDK!")
            .model(testModel)
            .build();

        Response response = client.responses().create(createParams);

        assertResponsesReturnTextSuccessfully(response);
    }

    @ParameterizedTest
    @MethodSource("com.azure.ai.openai.stainless.TestUtils#azureOnlyClient")
    public void responsesConversationTest(String apiType, String apiVersion, String testModel) {
        client = createClient(apiType, apiVersion);

        List<ResponseInputItem> inputItems = new ArrayList<>();
        inputItems.add(ResponseInputItem.ofEasyInputMessage(EasyInputMessage.builder()
            .role(EasyInputMessage.Role.USER)
            .content("Tell me a story about building the best SDK!")
            .build()));

        ResponseCreateParams createParams
            = ResponseCreateParams.builder().inputOfResponse(inputItems).model(testModel).build();

        for (int i = 0; i < 2; i++) {
            Response response = client.responses().create(createParams);

            List<ResponseOutputMessage> messages = assertResponsesConversationTest(response);
            messages.forEach(msg -> inputItems.add(ResponseInputItem.ofResponseOutputMessage(msg)));

            inputItems.add(ResponseInputItem.ofEasyInputMessage(EasyInputMessage.builder()
                .role(EasyInputMessage.Role.USER)
                .content("But why?" + new String(new char[i]).replace("\0", "?"))
                .build()));

            createParams = createParams.toBuilder().inputOfResponse(inputItems).build();
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.ai.openai.stainless.TestUtils#audioOnlyClient")
    public void testAudioTranscription(String apiType, String apiVersion, String testModel) {
        client = createClient(apiType, apiVersion);
        TranscriptionCreateParams params = createTranscriptionCreateParams(testModel);
        Transcription transcription = client.audio().transcriptions().create(params).asTranscription();
        assertAudioTranscription(transcription);
    }

    @ParameterizedTest
    @MethodSource("com.azure.ai.openai.stainless.TestUtils#azureOnlyClient")
    public void responsesGetPreviousResponseSuccessfully(String apiType, String apiVersion, String testModel) {
        client = createClient(apiType, apiVersion);

        ResponseCreateParams createParams = ResponseCreateParams.builder()
            .input("Tell me a story about building the best SDK!")
            .model(testModel)
            .build();

        Response response = client.responses().create(createParams);
        String text = extractOutputText(response);

        ResponseRetrieveParams retrieveParams = ResponseRetrieveParams.builder().responseId(response.id()).build();

        Response responseRetrieved = client.responses().retrieve(retrieveParams);

        assertNotNull(responseRetrieved, "Response should not be null");
        assertFalse(responseRetrieved.output().isEmpty(), "Response output should not be empty");
        assertEquals(response.id(), responseRetrieved.id());

        String textRetrieved = extractOutputText(responseRetrieved);

        assertNotNull(textRetrieved, "Text should not be null");
        assertFalse(textRetrieved.trim().isEmpty(), "Text should not be empty");
        assertEquals(text, textRetrieved);
    }

    @ParameterizedTest
    @MethodSource("com.azure.ai.openai.stainless.TestUtils#azureOnlyClient")
    public void responsesDeleteResponseSuccessfully(String apiType, String apiVersion, String testModel) {
        client = createClient(apiType, apiVersion);

        ResponseCreateParams createParams = ResponseCreateParams.builder()
            .input("Tell me a story about building the best SDK!")
            .model(testModel)
            .build();

        Response response = client.responses().create(createParams);

        ResponseDeleteParams deleteResponse = ResponseDeleteParams.builder().responseId(response.id()).build();

        client.responses().delete(deleteResponse);

        String exMessage = null;
        ResponseRetrieveParams retrieveParams = ResponseRetrieveParams.builder().responseId(response.id()).build();

        try {
            client.responses().retrieve(retrieveParams);
        } catch (NotFoundException ex) {
            exMessage = ex.getMessage();
        }

        assertTrue(exMessage.contains("404"));
    }

    @ParameterizedTest
    @MethodSource("com.azure.ai.openai.stainless.TestUtils#azureOnlyClient")
    public void responsesImagesBase64Successfully(String apiType, String apiVersion, String testModel) {
        client = createClient(apiType, apiVersion);

        String base64Image = null;
        try {
            String fileName = "logo.png";
            byte[] imageBytes
                = Files.readAllBytes(Paths.get("src/samples/java/com/azure/ai/openai/stainless/resources/" + fileName));
            base64Image = Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException e) {
            fail("Failed to read the image file: " + e.getMessage(), e);
        }

        String logoBase64Url = "data:image/jpeg;base64," + base64Image;

        ResponseInputImage logoInputImage
            = ResponseInputImage.builder().detail(ResponseInputImage.Detail.AUTO).imageUrl(logoBase64Url).build();
        ResponseInputItem messageInputItem = ResponseInputItem.ofMessage(ResponseInputItem.Message.builder()
            .role(ResponseInputItem.Message.Role.USER)
            .addInputTextContent("Describe this image.")
            .addContent(logoInputImage)
            .build());
        ResponseCreateParams createParams = ResponseCreateParams.builder()
            .inputOfResponse(Collections.singletonList(messageInputItem))
            .model(testModel)
            .build();

        Response response = client.responses().create(createParams);

        assertNotNull(response, "Response should not be null");
        assertFalse(response.output().isEmpty(), "Response output should not be empty");

        String text = extractOutputText(response);

        assertNotNull(text, "Text should not be null");
        assertFalse(text.trim().isEmpty(), "Text should not be empty");
        assertTrue(
            text.contains("orange") && text.contains("green") && text.contains("blue") && text.contains("yellow"));
    }

    @ParameterizedTest
    @MethodSource("com.azure.ai.openai.stainless.TestUtils#azureOnlyClient")
    public void responsesImagesUrlSuccessfully(String apiType, String apiVersion, String testModel) {
        client = createClient(apiType, apiVersion);

        String logoUrl
            = "https://th.bing.com/th/id/R.565799473e5e4ffb1d36bb3b5fc0400c?rik=HWolfL7xZmcc3g&pid=ImgRaw&r=0";

        ResponseInputImage logoInputImage
            = ResponseInputImage.builder().detail(ResponseInputImage.Detail.AUTO).imageUrl(logoUrl).build();
        ResponseInputItem messageInputItem = ResponseInputItem.ofMessage(ResponseInputItem.Message.builder()
            .role(ResponseInputItem.Message.Role.USER)
            .addInputTextContent("Describe this image.")
            .addContent(logoInputImage)
            .build());
        ResponseCreateParams createParams = ResponseCreateParams.builder()
            .inputOfResponse(Collections.singletonList(messageInputItem))
            .model(testModel)
            .build();

        Response response = client.responses().create(createParams);

        assertNotNull(response, "Response should not be null");
        assertFalse(response.output().isEmpty(), "Response output should not be empty");

        String text = extractOutputText(response);

        assertNotNull(text, "Text should not be null");
        assertFalse(text.trim().isEmpty(), "Text should not be empty");
        assertTrue(text.contains("blue") && text.contains("logo"));
    }

    @ParameterizedTest
    @MethodSource("com.azure.ai.openai.stainless.TestUtils#azureOnlyClientWithEmbedding")
    public void embeddingsReturnSuccessfully(String apiType, String apiVersion, String testModel) {
        OpenAIClient client = createClient(apiType, apiVersion);

        EmbeddingCreateParams createParams = EmbeddingCreateParams.builder()
            .input("The quick brown fox jumped over the lazy dog")
            .model(testModel)
            .build();

        CreateEmbeddingResponse response = client.embeddings().create(createParams);
        assertEmbeddingsReturnSuccessfully(response);
    }

    @ParameterizedTest
    @MethodSource("com.azure.ai.openai.stainless.TestUtils#allApiImageClient")
    public void testImageGeneration(String apiType, String apiVersion, String testModel) {
        client = createClient(apiType, apiVersion);
        String prompt = "Golden Retriever dog smiling when running on flower field";
        ImageGenerateParams params = createImageGenerateParams(testModel, prompt);
        Optional<List<Image>> images = client.images().generate(params).data();
        assertImageGeneration(images);
    }

    @ParameterizedTest
    @MethodSource("com.azure.ai.openai.stainless.TestUtils#azureOnlyClient")
    public void streamingReturnSuccessfully(String apiType, String apiVersion, String testModel) {
        client = createClient(apiType, apiVersion);

        ResponseCreateParams createParams = ResponseCreateParams.builder()
            .input("Tell me a short story about building the best SDK! in 300 words or less.")
            .model(testModel)
            .build();

        try (StreamResponse<ResponseStreamEvent> streamResponse = client.responses().createStreaming(createParams)) {
            streamResponse.stream().forEach(event -> event.outputTextDelta().ifPresent(textEvent -> {
                assertNotNull(textEvent.delta(), "Text delta should not be null");
                assertFalse(textEvent.delta().trim().isEmpty(), "Text delta should not be empty");
            }));
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.ai.openai.stainless.TestUtils#azureOnlyClient")
    public void testStructuredOutputsReturnSuccessfully(String apiType, String apiVersion, String testModel) {
        client = createClient(apiType, apiVersion);

        JsonSchema.Schema schema = createSchema();

        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
            .model(testModel)
            .temperature(0.0)
            .maxCompletionTokens(512)
            .responseFormat(ResponseFormatJsonSchema.builder()
                .jsonSchema(ResponseFormatJsonSchema.JsonSchema.builder().name("employee-list").schema(schema).build())
                .build())
            .addUserMessage("Who works at OpenAI?")
            .build();

        ChatCompletion result = client.chat().completions().create(params);
        assertChatCompletionDetailedResponse(result, "employees");
    }
}
