// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.inference;

import com.azure.ai.inference.models.ChatCompletions;
import com.azure.ai.inference.models.ChatCompletionsFunctionToolCall;
import com.azure.ai.inference.models.ChatCompletionsFunctionToolDefinition;
import com.azure.ai.inference.models.ChatCompletionsOptions;
import com.azure.ai.inference.models.ChatMessageContentItem;
import com.azure.ai.inference.models.ChatMessageImageContentItem;
import com.azure.ai.inference.models.ChatMessageImageUrl;
import com.azure.ai.inference.models.ChatMessageTextContentItem;
import com.azure.ai.inference.models.ChatRequestMessage;
import com.azure.ai.inference.models.ChatRequestAssistantMessage;
import com.azure.ai.inference.models.ChatRequestUserMessage;
import com.azure.ai.inference.models.ChatRequestSystemMessage;
import com.azure.ai.inference.models.ChatRequestToolMessage;
import com.azure.ai.inference.models.CompletionsFinishReason;
import com.azure.ai.inference.models.CompletionsUsage;
import com.azure.ai.inference.models.FunctionCall;
import com.azure.ai.inference.models.FunctionDefinition;
import com.azure.ai.inference.models.StreamingChatChoiceUpdate;
import com.azure.ai.inference.models.StreamingChatCompletionsUpdate;
import com.azure.ai.inference.models.StreamingChatResponseToolCallUpdate;
import com.azure.core.http.HttpClient;

import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.IterableStream;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.azure.ai.inference.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ChatCompletionsSyncClientTest extends ChatCompletionsClientTestBase {
    private ChatCompletionsClient client;
    private static final String FUNCTION_NAME = "FutureTemperature";
    private static final String FUNCTION_RETURN = "-7";
    private static final String TEST_URL =
        "https://upload.wikimedia.org/wikipedia/commons/thumb/d/dd/Gfp-wisconsin-madison-the-nature-boardwalk.jpg/2560px-Gfp-wisconsin-madison-the-nature-boardwalk.jpg";
    private static final String TEST_IMAGE_PATH = "./src/samples/resources/sample-images/sample.png";
    private static final String TEST_IMAGE_FORMAT = "png";

    private ChatCompletionsClient getChatCompletionsClient(HttpClient httpClient) {
        return getChatCompletionsClientBuilder(
            interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient)
            .buildClient();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.inference.TestUtils#getTestParameters")
    public void testGetChatCompletions(HttpClient httpClient) {
        client = getChatCompletionsClient(httpClient);
        getChatCompletionsRunner((prompt) -> {
            ChatCompletions resultCompletions = client.complete(prompt);
            assertCompletions(1, resultCompletions);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.inference.TestUtils#getTestParameters")
    public void testGetCompletionsStream(HttpClient httpClient) {
        client = getChatCompletionsClient(httpClient);
        getChatCompletionsRunner((prompt) -> {
            List<ChatRequestMessage> chatMessages = new ArrayList<>();
            chatMessages.add(new ChatRequestUserMessage(prompt));
            IterableStream<StreamingChatCompletionsUpdate> resultCompletions = client.completeStream(new ChatCompletionsOptions(chatMessages));
            assertTrue(resultCompletions.stream().toArray().length > 1);
            resultCompletions.forEach(ChatCompletionsClientTestBase::assertCompletionsStream);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.inference.TestUtils#getTestParameters")
    public void testGetCompletionsFromOptions(HttpClient httpClient) {
        client = getChatCompletionsClient(httpClient);
        getChatCompletionsFromOptionsRunner((options) -> {
            ChatCompletions completions = client.complete(options);
            assertCompletions(1, completions);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.inference.TestUtils#getTestParameters")
    public void testGetCompletionsWithResponse(HttpClient httpClient) {
        client = getChatCompletionsClient(httpClient);
        getChatCompletionsFromOptionsRunner((options) -> {
            Response<BinaryData> binaryDataResponse = client.completeWithResponse(
                BinaryData.fromObject(options), new RequestOptions());
            ChatCompletions response = binaryDataResponse.getValue().toObject(ChatCompletions.class);
            assertCompletions(1, response);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.inference.TestUtils#getTestParameters")
    public void testGetCompletionsUsageField(HttpClient httpClient) {
        client = getChatCompletionsClient(httpClient);
        getChatCompletionsFromOptionsRunner((options) -> {
            options.setMaxTokens(1024);

            ChatCompletions resultCompletions = client.complete(options);

            CompletionsUsage usage = resultCompletions.getUsage();
            assertCompletions(1, resultCompletions);
            assertNotNull(usage);
            assertTrue(usage.getTotalTokens() > 0);
            assertEquals(usage.getCompletionTokens() + usage.getPromptTokens(), usage.getTotalTokens());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.inference.TestUtils#getTestParameters")
    public void testGetCompletionsTokenCutoff(HttpClient httpClient) {
        client = getChatCompletionsClient(httpClient);
        getChatCompletionsFromOptionsRunner((options) -> {
            options.setMaxTokens(3);
            ChatCompletions resultCompletions = client.complete(options);
            assertCompletions(1, resultCompletions);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.inference.TestUtils#getTestParameters")
    public void testGetCompletionsWithImageFile(HttpClient httpClient) {
        client = getChatCompletionsClient(httpClient);
        Path testFilePath = Paths.get(TEST_IMAGE_PATH);
        List<ChatMessageContentItem> contentItems = new ArrayList<>();
        contentItems.add(new ChatMessageTextContentItem("Describe the image."));
        contentItems.add(new ChatMessageImageContentItem(testFilePath, TEST_IMAGE_FORMAT));

        List<ChatRequestMessage> chatMessages = new ArrayList<>();
        chatMessages.add(new ChatRequestSystemMessage("You are a helpful assistant."));
        chatMessages.add(ChatRequestUserMessage.fromContentItems(contentItems));

        ChatCompletions completions = client.complete(new ChatCompletionsOptions(chatMessages));
        assertCompletions(1, completions);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.inference.TestUtils#getTestParameters")
    public void testGetCompletionsWithImageUrl(HttpClient httpClient) {
        client = getChatCompletionsClient(httpClient);
        List<ChatMessageContentItem> contentItems = new ArrayList<>();
        contentItems.add(new ChatMessageTextContentItem("Describe the image."));
        contentItems.add(new ChatMessageImageContentItem(
            new ChatMessageImageUrl(TEST_URL)));

        List<ChatRequestMessage> chatMessages = new ArrayList<>();
        chatMessages.add(new ChatRequestSystemMessage("You are a helpful assistant."));
        chatMessages.add(ChatRequestUserMessage.fromContentItems(contentItems));

        ChatCompletions completions = client.complete(new ChatCompletionsOptions(chatMessages));

        assertCompletions(1, completions);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.inference.TestUtils#getTestParameters")
    public void testGetCompletionsStreamWithFunctionCalls(HttpClient httpClient) {
        client = getChatCompletionsClient(httpClient);
        String location = "Berlin";
        List<ChatRequestMessage> chatMessages = Arrays.asList(
            new ChatRequestSystemMessage("You are a helpful assistant."),
            new ChatRequestUserMessage(String.format("What sort of clothing should I wear today in %s?", location))
        );

        ChatCompletionsFunctionToolDefinition toolDefinition = new ChatCompletionsFunctionToolDefinition(
            getFutureTemperatureFunctionDefinition());

        ChatCompletionsOptions chatCompletionsOptions = new ChatCompletionsOptions(chatMessages);
        chatCompletionsOptions.setTools(Arrays.asList(toolDefinition));

        IterableStream<StreamingChatCompletionsUpdate> chatCompletionsStream = client.completeStream(chatCompletionsOptions);

        String toolCallId = null;
        String functionName = null;
        StringBuilder functionArguments = new StringBuilder();
        CompletionsFinishReason finishReason = null;
        for (StreamingChatCompletionsUpdate chatCompletions : chatCompletionsStream) {
            // In the case of Azure, the 1st message will contain filter information but no choices sometimes
            if (chatCompletions.getChoices().isEmpty()) {
                continue;
            }
            StreamingChatChoiceUpdate choice = chatCompletions.getChoice();
            if (choice.getFinishReason() != null) {
                finishReason = choice.getFinishReason();
                assertSame(finishReason, CompletionsFinishReason.TOOL_CALLS);
            }
            List<StreamingChatResponseToolCallUpdate> toolCalls = choice.getDelta().getToolCalls();
            // We take the functionName when it's available, and we aggregate the arguments.
            // We also monitor FinishReason for TOOL_CALL. That's the LLM signaling we should
            // call our function
            if (toolCalls != null) {
                StreamingChatResponseToolCallUpdate toolCall = toolCalls.get(0);
                if (toolCall != null) {
                    functionArguments.append(toolCall.getFunction().getArguments());
                    if (toolCall.getId() != null) {
                        toolCallId = toolCall.getId();
                    }

                    if (toolCall.getFunction().getName() != null) {
                        functionName = toolCall.getFunction().getName();
                    }
                }
            }
        }

        assertNotNull(functionName);
        assertTrue(functionName.contentEquals(functionName));
        assertNotNull(toolCallId);

        // We verify that the LLM wants us to call the function we advertised in the original request
        // Preparation for follow-up with the service we add:
        // - All the messages we sent
        // - The ChatCompletionsFunctionToolCall from the service as part of a ChatRequestAssistantMessage
        // - The result of function tool as part of a ChatRequestToolMessage
        if (finishReason == CompletionsFinishReason.TOOL_CALLS) {
            // Here the "content" can be null if used in non-Azure OpenAI
            // We prepare the assistant message reminding the LLM of the context of this request. We provide:
            // - The tool call id
            // - The function description
            FunctionCall functionCall = new FunctionCall(functionName, functionArguments.toString());
            ChatCompletionsFunctionToolCall functionToolCall = new ChatCompletionsFunctionToolCall(toolCallId, functionCall);
            ChatRequestAssistantMessage assistantRequestMessage = new ChatRequestAssistantMessage("");
            assistantRequestMessage.setToolCalls(Arrays.asList(functionToolCall));

            // As an additional step, you may want to deserialize the parameters, so you can call your function
            FunctionArguments parameters = BinaryData.fromString(functionArguments.toString()).toObject(FunctionArguments.class);
            assertTrue(parameters.locationName.contentEquals(location));
            String functionCallResult = futureTemperature(parameters.locationName, parameters.date);

            // This message contains the information that will allow the LLM to resume the text generation
            ChatRequestToolMessage toolRequestMessage = new ChatRequestToolMessage(functionCallResult, toolCallId);
            List<ChatRequestMessage> followUpMessages = Arrays.asList(
                // We add the original messages from the request
                chatMessages.get(0),
                chatMessages.get(1),
                assistantRequestMessage,
                toolRequestMessage
            );

            IterableStream<StreamingChatCompletionsUpdate> followUpChatCompletionsStream = client.completeStream(
                new ChatCompletionsOptions(followUpMessages));

            StringBuilder finalResult = new StringBuilder();
            CompletionsFinishReason finalFinishReason = null;
            for (StreamingChatCompletionsUpdate chatCompletions : followUpChatCompletionsStream) {
                if (chatCompletions.getChoices().isEmpty()) {
                    continue;
                }
                StreamingChatChoiceUpdate choice = chatCompletions.getChoice();
                if (choice.getFinishReason() != null) {
                    finalFinishReason = choice.getFinishReason();
                    assertSame(finalFinishReason, CompletionsFinishReason.STOPPED);
                }
                if (choice.getDelta().getContent() != null) {
                    finalResult.append(choice.getDelta().getContent());
                }
            }
            assertTrue(finalResult.toString().contains(FUNCTION_RETURN));
        }
    }

    private static String futureTemperature(String locationName, String data) {
        return String.format("%s C", FUNCTION_RETURN);
    }

    private static FunctionDefinition getFutureTemperatureFunctionDefinition() {
        FunctionDefinition functionDefinition = new FunctionDefinition(FUNCTION_NAME);
        functionDefinition.setDescription("Get the future temperature for a given location and date.");
        FutureTemperatureParameters parameters = new FutureTemperatureParameters();
        functionDefinition.setParameters(BinaryData.fromObject(parameters));
        return functionDefinition;
    }

    public static final class FunctionArguments implements JsonSerializable<FunctionArguments> {
        @JsonProperty(value = "location_name")
        private String locationName;

        @JsonProperty(value = "date")
        private String date;

        private FunctionArguments(String location,  String date) {
            this.locationName = location;
            this.date = date;
        }

        @Override
        public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
            jsonWriter.writeStartObject();
            jsonWriter.writeStringField("location_name", this.locationName);
            jsonWriter.writeStringField("date", this.date);
            return jsonWriter.writeEndObject();
        }

        public static FunctionArguments fromJson(JsonReader jsonReader) throws IOException {
            return jsonReader.readObject(reader -> {
                String location = null;
                String date = null;
                while (reader.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = reader.getFieldName();
                    reader.nextToken();
                    if ("location_name".equals(fieldName)) {
                        location = reader.getString();
                    } else if ("date".equals(fieldName)) {
                        date = reader.getString();
                    } else {
                        reader.skipChildren();
                    }
                }
                return new FunctionArguments(location, date);
            });
        }
    }

    private static final class FutureTemperatureParameters implements JsonSerializable<FutureTemperatureParameters> {
        @JsonProperty(value = "type")
        private String type = "object";

        @JsonProperty(value = "properties")
        private FutureTemperatureProperties properties;

        private FutureTemperatureParameters() {
            this.type = "object";
            this.properties = new FutureTemperatureProperties();
        }

        private FutureTemperatureParameters(String type, FutureTemperatureProperties properties) {
            this.type = type;
            this.properties = properties;
        }

        @Override
        public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
            jsonWriter.writeStartObject();
            jsonWriter.writeStringField("type", this.type == null ? null : this.type);
            jsonWriter.writeJsonField("properties", this.properties);
            return jsonWriter.writeEndObject();
        }

        public static FutureTemperatureParameters fromJson(JsonReader jsonReader) throws IOException {
            return jsonReader.readObject(reader -> {
                String type = null;
                FutureTemperatureProperties properties = null;
                while (reader.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = reader.getFieldName();
                    reader.nextToken();
                    if ("type".equals(fieldName)) {
                        type = reader.getString();
                    } else if ("properties".equals(fieldName)) {
                        properties = FutureTemperatureProperties.fromJson(reader);
                    } else {
                        reader.skipChildren();
                    }
                }
                return new FutureTemperatureParameters(type, properties);
            });
        }
    }

    private static final class FutureTemperatureProperties implements JsonSerializable<FutureTemperatureProperties> {
        private String unitString = "Temperature unit. Can be either Celsius or Fahrenheit. Defaults to Celsius.";
        @JsonProperty(value = "unit")
        StringField unit = new StringField(unitString);

        private String locationString = "The name of the location to get the future temperature for.";
        @JsonProperty(value = "location_name")
        StringField locationName = new StringField(locationString);

        private String dateString = "The date to get the future temperature for. The format is YYYY-MM-DD.";
        @JsonProperty(value = "date")
        StringField date = new StringField(dateString);

        private FutureTemperatureProperties() {
            this.unit = new StringField(unitString);
            this.locationName = new StringField(locationString);
            this.date = new StringField(dateString);
        }

        private FutureTemperatureProperties(StringField unit, StringField locationName, StringField date) {
            this.unit = unit;
            this.locationName = locationName;
            this.date = date;
        }

        @Override
        public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
            jsonWriter.writeStartObject();
            jsonWriter.writeJsonField("unit", this.unit);
            jsonWriter.writeJsonField("location_name", this.locationName);
            jsonWriter.writeJsonField("date", this.date);
            return jsonWriter.writeEndObject();
        }

        public static FutureTemperatureProperties fromJson(JsonReader jsonReader) throws IOException {
            return jsonReader.readObject(reader -> {
                StringField unit = null;
                StringField location = null;
                StringField date = null;
                while (reader.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = reader.getFieldName();
                    reader.nextToken();
                    if ("unit".equals(fieldName)) {
                        unit = StringField.fromJson(reader);
                    } else if ("date".equals(fieldName)) {
                        date = StringField.fromJson(reader);
                    } else if ("location_name".equals(fieldName)) {
                        location = StringField.fromJson(reader);
                    } else {
                        reader.skipChildren();
                    }
                }
                return new FutureTemperatureProperties(unit, location, date);
            });
        }
    }

    private static class StringField implements JsonSerializable<StringField> {
        @JsonProperty(value = "type")
        private final String type = "string";

        @JsonProperty(value = "description")
        private String description;

        @JsonCreator
        StringField(@JsonProperty(value = "description") String description) {
            this.description = description;
        }

        @Override
        public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
            jsonWriter.writeStartObject();
            jsonWriter.writeStringField("type", this.type);
            jsonWriter.writeStringField("description", this.description);
            return jsonWriter.writeEndObject();
        }

        public static StringField fromJson(JsonReader jsonReader) throws IOException {
            return jsonReader.readObject(reader -> {
                String description = null;
                while (reader.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = reader.getFieldName();
                    reader.nextToken();
                    if ("description".equals(fieldName)) {
                        description = reader.getString();
                    } else {
                        reader.skipChildren();
                    }
                }
                return new StringField(description);
            });
        }

    }
}

