// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.openai.tests;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.AuthenticationUtil;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.azure.AzureOpenAIServiceVersion;
import com.openai.core.JsonValue;
import com.openai.errors.BadRequestException;
import com.openai.models.ChatCompletion;
import com.openai.models.ChatCompletionAssistantMessageParam;
import com.openai.models.ChatCompletionContentPart;
import com.openai.models.ChatCompletionContentPartImage;
import com.openai.models.ChatCompletionContentPartText;
import com.openai.models.ChatCompletionCreateParams;
import com.openai.models.ChatCompletionFunctionCallOption;
import com.openai.models.ChatCompletionFunctionMessageParam;
import com.openai.models.ChatCompletionMessage;
import com.openai.models.ChatCompletionMessageParam;
import com.openai.models.ChatCompletionMessageToolCall;
import com.openai.models.ChatCompletionSystemMessageParam;
import com.openai.models.ChatCompletionTool;
import com.openai.models.ChatCompletionToolMessageParam;
import com.openai.models.ChatCompletionUserMessageParam;
import com.openai.models.CompletionUsage;
import com.openai.models.FunctionDefinition;
import com.openai.models.FunctionParameters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("ALL")
public class OpenAIOkHttpClientTestBase {
    static final String ASSISTANT_CONTENT =
            "Don't make assumptions about what values to plug into functions. Ask for clarification if a user request is ambiguous.";
    static final AzureOpenAIServiceVersion AZURE_OPENAI_SERVICE_VERSION_GA = AzureOpenAIServiceVersion.getV2024_06_01();
    static final AzureOpenAIServiceVersion AZURE_OPENAI_SERVICE_VERSION_PREVIEW = AzureOpenAIServiceVersion.getV2024_08_01_PREVIEW();
    static final String USER_CONTENT = "Who won the world series in 2020?";

    String getEndpoint() {
        String azureOpenaiEndpoint = System.getenv("AZURE_OPENAI_ENDPOINT");
        if (azureOpenaiEndpoint.endsWith("/")) {
            azureOpenaiEndpoint = azureOpenaiEndpoint.substring(0, azureOpenaiEndpoint.length() - 1);
        }
        return azureOpenaiEndpoint;
    }

    // This method will be removed when Azure Identity library supports Azure TokenCredential as a Supplier of String
    static Supplier<String> getBearerTokenCredentialProvider(TokenCredential azureTokenCredential) {
        return AuthenticationUtil.getBearerTokenSupplier(
            new DefaultAzureCredentialBuilder().build(), "https://cognitiveservices.azure.com/.default");
    }

    // Request: Helper methods to prepare request params
    ChatCompletionMessageParam createSystemMessageParam() {
        return ChatCompletionMessageParam.ofSystem(ChatCompletionSystemMessageParam.builder()
                .role(JsonValue.from("system"))
                .content(ChatCompletionSystemMessageParam.Content.ofText(ASSISTANT_CONTENT))
                .build());
    }

    ChatCompletionMessageParam createUserMessageParam(String content) {
        return ChatCompletionMessageParam.ofUser(ChatCompletionUserMessageParam.builder()
                .role(JsonValue.from("user"))
                .content(ChatCompletionUserMessageParam.Content.ofText(content))
                .build());
    }

    ChatCompletionCreateParams.Builder createParamsBuilder(String model) {
        ChatCompletionCreateParams.Builder paramsBuilder = ChatCompletionCreateParams.builder()
                .messages(asList(createSystemMessageParam(), createUserMessageParam(USER_CONTENT)))
                .model(model);
        return paramsBuilder;
    }

    ChatCompletionCreateParams.Builder createParamsBuilder(String model, String userMessage) {
        ChatCompletionCreateParams.Builder paramsBuilder = ChatCompletionCreateParams.builder()
                .messages(asList(createSystemMessageParam(), createUserMessageParam(userMessage)))
                .model(model);
        return paramsBuilder;
    }

    ChatCompletionCreateParams createChatCompletionParams(String testModel, String userMessage) {
        return ChatCompletionCreateParams.builder()
                .messages(asList(createSystemMessageParam(), createUserMessageParam(userMessage)))
                .model(testModel)
                .build();
    }

    ChatCompletionCreateParams createChatCompletionParamsWithFunction(
            String testModel,
            List<ChatCompletionMessageParam> messages,
            List<ChatCompletionCreateParams.Function> functions,
            String functionName) {
        ChatCompletionCreateParams.FunctionCall functionCall =
                ChatCompletionCreateParams.FunctionCall.ofFunctionCallOption(ChatCompletionFunctionCallOption.builder()
                        .name(functionName)
                        .build());
        return ChatCompletionCreateParams.builder()
                .messages(messages)
                .functions(functions)
                .functionCall(functionCall)
                .model(testModel)
                .build();
    }

    ChatCompletionCreateParams createChatCompletionParamsWithoutFunctionCall(
            String testModel,
            List<ChatCompletionMessageParam> messages,
            List<ChatCompletionCreateParams.Function> functions) {
        return ChatCompletionCreateParams.builder()
                .messages(messages)
                .functions(functions)
                .model(testModel)
                .build();
    }

    ChatCompletionCreateParams createChatCompletionParamsWithTool(String testModel, String userMessage) {
        ChatCompletionTool chatCompletionTool = ChatCompletionTool.builder()
            .type(JsonValue.from("function"))
            .function(FunctionDefinition.builder()
                .name("get_current_weather")
                .description("Get the current weather in a given location")
                .parameters(FunctionParameters.builder()
                    .putAdditionalProperty("type", JsonValue.from("object"))
                    .putAdditionalProperty(
                        "properties",
                        JsonValue.from(FunctionParameters.builder()
                            .putAdditionalProperty(
                                "location",
                                JsonValue.from(FunctionParameters.builder()
                                    .putAdditionalProperty(
                                        "type", JsonValue.from("string"))
                                    .putAdditionalProperty(
                                        "description",
                                        JsonValue.from(
                                            "The city and state, e.g. San Francisco, CA"))
                                    .build()))
                            .putAdditionalProperty(
                                "unit",
                                JsonValue.from(FunctionParameters.builder()
                                    .putAdditionalProperty(
                                        "type", JsonValue.from("string"))
                                    .putAdditionalProperty(
                                        "enum",
                                        JsonValue.from(
                                            asList(
                                                "celsius",
                                                "fahrenheit")))
                                    .build()))
                            .build()))
                    .putAdditionalProperty(
                        "required", JsonValue.from(Collections.singletonList("location")))
                    .build())
                .build())
            .build();

        return ChatCompletionCreateParams.builder()
                .messages(asList(createSystemMessageParam(), createUserMessageParam(userMessage)))
                .model(testModel)
                .tools(asList(chatCompletionTool))
//                .toolChoice(AUTO)
                .build();
    }

    ChatCompletionCreateParams createChatCompletionParamsWithImageUrl(String testModel) {
        ChatCompletionMessageParam userMessageParam =
                ChatCompletionMessageParam.ofUser(ChatCompletionUserMessageParam.builder()
                        .role(JsonValue.from("user"))
                        .content(ChatCompletionUserMessageParam.Content.ofArrayOfContentParts(asList(
                                ChatCompletionContentPart.ofText(
                                        ChatCompletionContentPartText.builder()
                                                .text("What's in this image?")
                                                .build()),
                                ChatCompletionContentPart.ofImageUrl(
                                        ChatCompletionContentPartImage.builder()
                                                .imageUrl(ChatCompletionContentPartImage.ImageUrl.builder()
                                                        .url(
                                                                "https://learn.microsoft.com/en-us/azure/ai-services/computer-vision/images/handwritten-note.jpg")
                                                        .build())
                                                .build()))))
                        .build());
        return ChatCompletionCreateParams.builder()
                .messages(asList(createSystemMessageParam(), userMessageParam))
                .model(testModel)
                .build();
    }

    List<ChatCompletionMessageParam> createMessages(String userMessage) {
        List<ChatCompletionMessageParam> messages = new ArrayList<>();
        messages.add(createSystemMessageParam());
        messages.add(createUserMessageParam(userMessage));
        return messages;
    }

    ChatCompletionCreateParams.Function createGetCurrentWeatherFunction() {
        return ChatCompletionCreateParams.Function.builder()
                .name("get_current_weather")
                .description("Get the current weather")
                .parameters(FunctionParameters.builder()
                        .putAdditionalProperty("type", JsonValue.from("object"))
                        .putAdditionalProperty(
                                "properties",
                                JsonValue.from(FunctionParameters.builder()
                                        .putAdditionalProperty(
                                                "location",
                                                JsonValue.from(FunctionParameters.builder()
                                                        .putAdditionalProperty("type", JsonValue.from("string"))
                                                        .putAdditionalProperty(
                                                                "description",
                                                                JsonValue.from(
                                                                        "The city and state, e.g. San Francisco, CA"))
                                                        .build()))
                                        .putAdditionalProperty(
                                                "unit",
                                                JsonValue.from(FunctionParameters.builder()
                                                        .putAdditionalProperty("type", JsonValue.from("string"))
                                                        .putAdditionalProperty(
                                                                "enum", JsonValue.from(asList("celsius", "fahrenheit")))
                                                        .putAdditionalProperty(
                                                                "description",
                                                                JsonValue.from(
                                                                        "The temperature unit to use. Infer this from the users location."))
                                                        .build()))
                                        .build()))
                        .build())
                .build();
    }

    ChatCompletionCreateParams.Function createGetCurrentTemperatureFunction() {
        return ChatCompletionCreateParams.Function.builder()
                .name("get_current_temperature")
                .description("Get the current temperature")
                .parameters(FunctionParameters.builder()
                        .putAdditionalProperty("type", JsonValue.from("object"))
                        .putAdditionalProperty(
                                "properties",
                                JsonValue.from(FunctionParameters.builder()
                                        .putAdditionalProperty(
                                                "location",
                                                JsonValue.from(FunctionParameters.builder()
                                                        .putAdditionalProperty("type", JsonValue.from("string"))
                                                        .putAdditionalProperty(
                                                                "description",
                                                                JsonValue.from(
                                                                        "The city and state, e.g. San Francisco, CA"))
                                                        .build()))
                                        .putAdditionalProperty(
                                                "unit",
                                                JsonValue.from(FunctionParameters.builder()
                                                        .putAdditionalProperty("type", JsonValue.from("string"))
                                                        .putAdditionalProperty(
                                                                "enum", JsonValue.from(asList("celsius", "fahrenheit")))
                                                        .putAdditionalProperty(
                                                                "description",
                                                                JsonValue.from("The temperature unit to use."))
                                                        .build()))
                                        .build()))
                        .build())
                .build();
    }

    List<ChatCompletionCreateParams.Function> createFunctions() {
        return asList(createGetCurrentWeatherFunction(), createGetCurrentTemperatureFunction());
    }

    ChatCompletionCreateParams addFunctionResponseToMessages(
            String testModel,
            List<ChatCompletionMessageParam> messages,
            List<ChatCompletionCreateParams.Function> functions,
            String content) {
        messages.add(ChatCompletionMessageParam.ofFunction(
                ChatCompletionFunctionMessageParam.builder()
                        .name("get_current_temperature")
                        .role(JsonValue.from("function"))
                        .content(content)
                        .build()));
        return ChatCompletionCreateParams.builder()
                .messages(messages)
                .functions(functions)
                .model(testModel)
                .build();
    }

    ChatCompletionCreateParams addToolResponseToMessages(
            ChatCompletionCreateParams params,
            List<ChatCompletionMessageToolCall> chatCompletionMessageToolCalls,
            ChatCompletion.Choice choice) {
        // Create a new builder from the existing params
        ChatCompletionCreateParams.Builder paramsBuilder = params.toBuilder();

        // Add tool response to messages: Assistant
        paramsBuilder.addMessage(ChatCompletionMessageParam.ofAssistant(
                ChatCompletionAssistantMessageParam.builder()
                        .role(JsonValue.from("assistant"))
                        .toolCalls(chatCompletionMessageToolCalls)
                        .build()));

        // Add tool response to messages: Tool
        ChatCompletionMessageParam toolMessageParam =
                ChatCompletionMessageParam.ofTool(ChatCompletionToolMessageParam.builder()
                        .role(JsonValue.from("tool"))
                        .toolCallId(chatCompletionMessageToolCalls.get(0).id())
                        .content(ChatCompletionToolMessageParam.Content.ofText(
                                "{\"temperature\": \"22\", \"unit\": \"celsius\", \"description\": \"Sunny\"}"))
                        .build());
        // Add the tool message to the params
        paramsBuilder.addMessage(toolMessageParam);

        if (chatCompletionMessageToolCalls.size() > 1) {
            ChatCompletionMessageParam toolMessageParam2 =
                    ChatCompletionMessageParam.ofTool(ChatCompletionToolMessageParam.builder()
                            .role(JsonValue.from("tool"))
                            .toolCallId(chatCompletionMessageToolCalls.get(1).id())
                            .content(ChatCompletionToolMessageParam.Content.ofText(
                                    "{\"temperature\": \"80\", \"unit\": \"fahrenheit\", \"description\": \"Sunny\"}"))
                            .build());
            paramsBuilder.addMessage(toolMessageParam2);
        }

        // Return the updated params
        return paramsBuilder.build();
    }

    Map<String, JsonValue> createExtraBodyForByod() {
        Map<String, JsonValue> authentication = new HashMap<>();
        authentication.put("type", JsonValue.from("api_key"));
        authentication.put("key", JsonValue.from(System.getenv("AZURE_SEARCH_API_KEY")));

        Map<String, JsonValue> parameters = new HashMap<>();
        parameters.put("endpoint", JsonValue.from(System.getenv("AZURE_SEARCH_ENDPOINT")));
        parameters.put("index_name", JsonValue.from(System.getenv("AZURE_OPENAI_SEARCH_INDEX")));
        parameters.put("authentication", JsonValue.from(authentication));
        parameters.put("fields_mapping", JsonValue.from(Collections.singletonMap("title_field", "title")));
        parameters.put("query_type", JsonValue.from("simple"));

        Map<String, JsonValue> dataSource = new HashMap<>();
        dataSource.put("type", JsonValue.from("azure_search"));
        dataSource.put("parameters", JsonValue.from(parameters));

        Map<String, JsonValue> extraBody = new HashMap<>();
        extraBody.put("data_sources", JsonValue.from(asList(dataSource)));
        return extraBody;
    }

    // Response: Helper methods to assert response
    void assertChatCompletion(ChatCompletion chatCompletion, int expectedChoicesSize) {
        assertNotNull(chatCompletion._id());
        assertEquals("chat.completion", chatCompletion._object_().toString());
        assertNotNull(chatCompletion.model());
        assertNotNull(chatCompletion.created());

        CompletionUsage usage = chatCompletion.usage().get();
        assertTrue(usage.completionTokens() > 0);
        assertTrue(usage.promptTokens() > 0);
        assertTrue(usage.totalTokens() > 0);

        List<ChatCompletion.Choice> choices = chatCompletion.choices();
        assertEquals(expectedChoicesSize, choices.size());
        for (int i = 0; i < choices.size(); i++) {
            ChatCompletion.Choice choice = choices.get(i);
            assertEquals(i, choice.index());
            assertNotNull(choice.finishReason());
            assertTrue(choice.message().content().isPresent());
            assertNotNull(choice.message()._role());
        }
    }

    void assertFunctionCall(ChatCompletionMessage.FunctionCall functionCallResponse) {
        assertEquals("get_current_temperature", functionCallResponse.name());
        assertTrue(functionCallResponse.arguments().contains("Seattle"));
    }

    void assertFunctionCompletion(ChatCompletion functionCompletion) {
        assertNotNull(functionCompletion);
        assertTrue(functionCompletion
                .choices()
                .get(0)
                .message()
                .content()
                .get()
                .toLowerCase()
                .contains("22"));
        assertEquals(
                "assistant",
                functionCompletion.choices().get(0).message()._role().toString());
    }

    void assertChatCompletion(ChatCompletion chatCompletion) {
        assertNotNull(chatCompletion._id());
        assertEquals("chat.completion", chatCompletion._object_().toString());
        assertNotNull(chatCompletion.model());
        assertNotNull(chatCompletion.created());

        CompletionUsage usage = chatCompletion.usage().get();
        assertTrue(usage.completionTokens() > 0);
        assertTrue(usage.promptTokens() > 0);
        assertEquals(usage.totalTokens(), usage.completionTokens() + usage.promptTokens());

        assertEquals(1, chatCompletion.choices().size());
        ChatCompletion.Choice choice = chatCompletion.choices().get(0);

        assertNotNull(choice.finishReason());
        assertEquals(0, choice.index());
    }

    void assertToolCall(ChatCompletionMessageToolCall toolCall) {
        assertNotNull(toolCall.id());
        assertNotNull(toolCall.function());
        assertEquals("get_current_weather", toolCall.function().name());
        assertTrue(toolCall.function().arguments().contains("location"));
    }

    void assertToolCompletion(ChatCompletion toolCompletion) {
        assertNotNull(toolCompletion);

        List<ChatCompletion.Choice> choices = toolCompletion.choices();
        String content = choices.get(0).message().content().get().toLowerCase();
        assertTrue(content.contains("sunny"));
        assertTrue(content.contains("22"));
        assertEquals(
                "assistant", toolCompletion.choices().get(0).message()._role().toString());

        if (choices.size() > 1) {
            String content2 = choices.get(1).message().content().get().toLowerCase();
            assertTrue(content2.contains("sunny"));
            assertTrue(content2.contains("80"));
            assertEquals(
                    "assistant",
                    toolCompletion.choices().get(1).message()._role().toString());
        }
    }

    void assertContentFilterResult(Map<String, JsonValue> contentFilterResult) {
        assertFilterResult(contentFilterResult, "hate", false, "safe");
        assertFilterResult(contentFilterResult, "self_harm", false, "safe");
        assertFilterResult(contentFilterResult, "sexual", false, "safe");
        assertFilterResult(contentFilterResult, "violence", true, "medium");
    }

    void assertContentFilterResultAllSafe(Map<String, JsonValue> contentFilterResult) {
        assertFilterResult(contentFilterResult, "hate", false, "safe");
        assertFilterResult(contentFilterResult, "self_harm", false, "safe");
        assertFilterResult(contentFilterResult, "sexual", false, "safe");
        assertFilterResult(contentFilterResult, "violence", false, "safe");
    }

    void assertFilterResult(
            Map<String, JsonValue> contentFilterResult,
            String filterName,
            boolean expectedFiltered,
            String expectedSeverity) {
//        Map<String, JsonValue> filterMap = (Map<String, JsonValue>)
//                contentFilterResult.get(filterName).asObject().get();
//        assertEquals(expectedFiltered, (Boolean)
//                filterMap.get("filtered").asBoolean().get());
//        assertEquals(expectedSeverity, filterMap.get("severity").asStringOrThrow());
    }

    void assertChatCompletionWithoutSensitiveContent(ChatCompletion chatCompletion) {
        assertNotNull(chatCompletion._id());
        assertEquals("chat.completion", chatCompletion._object_().toString());
        assertNotNull(chatCompletion.model());
        assertNotNull(chatCompletion.created());

        assertEquals(1, chatCompletion.choices().size());
        ChatCompletion.Choice choice = chatCompletion.choices().get(0);

        assertNotNull(choice.finishReason());
        assertEquals(0, choice.index());
        assertTrue(choice.message().content().isPresent());
        assertNotNull(choice.message()._role());

        assertPromptAndContentFilterResults(chatCompletion);
    }

    void assertCannotAssistantMessage(ChatCompletion chatCompletion) {
        assertChatCompletion(chatCompletion, 1);
        String response = chatCompletion.choices().get(0).message().content().get();
        assertTrue(response.contains("I can't help with that.") || response.contains("I cannot help with that.")
            || response.contains("I can't assist with that.") || response.contains("I cannot assist with that."));
    }

    void assertPromptAndContentFilterResults(ChatCompletion chatCompletion) {
//        ChatCompletion.Choice choice = chatCompletion.choices().get(0);
//        JsonValue promptFilterResults = chatCompletion._additionalProperties().get("prompt_filter_results");
//        assertNotNull(promptFilterResults);
//        assertFalse(promptFilterResults.isMissing());
//
//        List<JsonValue> promptFilterResultsList =
//                (List<JsonValue>) promptFilterResults.asArray().get();
//        assertEquals(1, promptFilterResultsList.size());
//        JsonValue promptFilterResultJsonValue = promptFilterResultsList.get(0);
//        Map<String, JsonValue> promptFilterResultsMap =
//                (Map<String, JsonValue>) promptFilterResultJsonValue.asObject().get();
//        assertEquals(
//                0,
//                ((Number) promptFilterResultsMap.get("prompt_index").asNumber().get()).intValue());
//
//        Map<String, JsonValue> contentFilterResultsMap = (Map<String, JsonValue>)
//                promptFilterResultsMap.get("content_filter_results").asObject().get();
//        assertContentFilterResultAllSafe(contentFilterResultsMap);
//
//        JsonValue contentFilterResultsMapJsonValue =
//                choice._additionalProperties().get("content_filter_results");
//        assertNotNull(contentFilterResultsMapJsonValue);
//        assertFalse(contentFilterResultsMapJsonValue.isMissing());
//        Map<String, JsonValue> contentFilterResultsMapInChoice = (Map<String, JsonValue>)
//                contentFilterResultsMapJsonValue.asObject().get();
//        assertContentFilterResultAllSafe(contentFilterResultsMapInChoice);
    }

    void assertChatCompletionByod(ChatCompletion chatCompletion) {
//        assertNotNull(chatCompletion._id());
//        assertEquals("extensions.chat.completion", chatCompletion.object_().toString());
//        assertNotNull(chatCompletion.model());
//        assertNotNull(chatCompletion.created());
//
//        assertEquals(1, chatCompletion.choices().size());
//        ChatCompletion.Choice choice = chatCompletion.choices().get(0);
//
//        assertNotNull(choice.finishReason());
//        assertEquals(0, choice.index());
//        assertTrue(choice.message().content().isPresent());
//        assertNotNull(choice.message().role());
//        JsonValue context = choice.message()._additionalProperties().get("context");
//        assertNotNull(context);
//        Map<String, JsonValue> contextMap =
//                (Map<String, JsonValue>) context.asObject().get();
//        assertNotNull(contextMap.get("citations"));
//        assertNotNull(contextMap.get("intent"));
    }

    void assertRaiContentFilter(BadRequestException e) {
//        OpenAIError error = e.error();
//        Map<String, JsonValue> errorMap = error.additionalProperties();
//        assertNotNull(errorMap);
//        JsonValue errorDetails = errorMap.get("error");
//        assertNotNull(errorDetails);
//        Map<String, JsonValue> errorDetailsMap =
//            (Map<String, JsonValue>) errorDetails.asObject().get();
//        JsonValue code = errorDetailsMap.get("code");
//        assertNotNull(code);
//        assertEquals("content_filter", code.asString().get());
//        JsonValue message = errorDetailsMap.get("message");
//        assertNotNull(message);
//
//        Map<String, JsonValue> innererrorMap = (Map<String, JsonValue>)
//            errorDetailsMap.get("innererror").asObject().get();
//        assertNotNull(innererrorMap);
//        JsonValue contentFilterResult = innererrorMap.get("content_filter_result");
//        assertNotNull(contentFilterResult);
//        Map<String, JsonValue> contentFilterResultMap =
//            (Map<String, JsonValue>) contentFilterResult.asObject().get();
//
//        JsonValue hate = contentFilterResultMap.get("hate");
//        assertNotNull(hate);
//        Map<String, JsonValue> hateMap =
//            (Map<String, JsonValue>) hate.asObject().get();
//        JsonValue hateFiltered = hateMap.get("filtered");
//        assertNotNull(hateFiltered);
//        assertFalse((Boolean) hateFiltered.asBoolean().get());
//        JsonValue hateSeverity = hateMap.get("severity");
//        assertNotNull(hateSeverity);
//        assertEquals("safe", hateSeverity.asString().get());
//
//        JsonValue selfHarm = contentFilterResultMap.get("self_harm");
//        assertNotNull(selfHarm);
//        Map<String, JsonValue> selfHarmMap =
//            (Map<String, JsonValue>) selfHarm.asObject().get();
//        JsonValue selfHarmFiltered = selfHarmMap.get("filtered");
//        assertNotNull(selfHarmFiltered);
//        assertFalse((Boolean) selfHarmFiltered.asBoolean().get());
//        JsonValue selfHarmSeverity = selfHarmMap.get("severity");
//        assertNotNull(selfHarmSeverity);
//        assertEquals("safe", selfHarmSeverity.asString().get());
//
//        JsonValue sexual = contentFilterResultMap.get("sexual");
//        assertNotNull(sexual);
//        Map<String, JsonValue> sexualMap =
//            (Map<String, JsonValue>) sexual.asObject().get();
//        JsonValue sexualFiltered = sexualMap.get("filtered");
//        assertNotNull(sexualFiltered);
//        assertFalse((Boolean) sexualFiltered.asBoolean().get());
//        JsonValue sexualSeverity = sexualMap.get("severity");
//        assertNotNull(sexualSeverity);
//        assertEquals("safe", sexualSeverity.asString().get());
//
//        JsonValue violence = contentFilterResultMap.get("violence");
//        assertNotNull(violence);
////        Map<String, JsonValue> violenceMap =
////            (Map<String, JsonValue>) violence.asObject().get();
//
//        Map<String, JsonValue> violenceMap = (violence.asObject().isPresent())
//            ? (Map<String, JsonValue>) violence.asObject().get()
//            : new HashMap<>();
//
//        JsonValue violenceFiltered = violenceMap.get("filtered");
//        assertNotNull(violenceFiltered);
//        assertTrue((Boolean) violenceFiltered.asBoolean().get());
//        JsonValue violenceSeverity = violenceMap.get("severity");
//        assertNotNull(violenceSeverity);
//        assertEquals("medium", violenceSeverity.asString().get());
    }
}
