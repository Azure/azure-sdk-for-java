// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.responses.models;

import com.azure.ai.openai.responses.implementation.accesshelpers.CreateResponsesRequestAccessHelper;
import com.azure.core.annotation.Fluent;
import com.azure.core.annotation.Generated;
import com.azure.core.util.BinaryData;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * The CreateResponsesRequest model.
 */
@Fluent
public final class CreateResponsesRequest implements JsonSerializable<CreateResponsesRequest> {

    /*
     * Model ID used to generate the response, like `gpt-4o` or `o1`.
     * Refer to the [model guide](/docs/models) for more information and supported features for each model.
     */
    @Generated
    private final CreateResponsesRequestModel model;

    /*
     * Set of 16 key-value pairs that can be attached to an object. This can be
     * useful for storing additional information about the object in a structured
     * format, and querying for objects via API or the dashboard.
     * 
     * Keys are strings with a maximum length of 64 characters. Values are strings
     * with a maximum length of 512 characters.
     */
    @Generated
    private Map<String, String> metadata;

    /*
     * What sampling temperature to use, between 0 and 2. Higher values like 0.8 will make the output more random, while
     * lower values like 0.2 will make it more focused and deterministic.
     * 
     * We generally recommend altering this or `top_p` but not both.
     */
    @Generated
    private Double temperature;

    /*
     * An alternative to sampling with temperature, called nucleus sampling, where the model considers the results of
     * the tokens with top_p probability mass. So 0.1 means only the tokens comprising the top 10% probability mass are
     * considered.
     * 
     * We generally recommend altering this or `temperature` but not both.
     */
    @Generated
    private Double topP;

    /*
     * The unique ID of the previous response to the model. Use this to create multi-turn conversations.
     */
    @Generated
    private String previousResponseId;

    /*
     * The reasoning property.
     */
    @Generated
    private ResponsesReasoningConfiguration reasoning;

    /*
     * An upper bound for the number of tokens that can be generated for a response, including visible output tokens and
     * [reasoning tokens](/docs/guides/reasoning).
     */
    @Generated
    private Integer maxOutputTokens;

    /*
     * The instructions property.
     */
    @Generated
    private String instructions;

    /*
     * The text property.
     */
    @Generated
    private ResponseTextOptions text;

    /*
     * The tools to use to generate a response.
     */
    @Generated
    private List<ResponsesTool> tools;

    /*
     * How the model should select which tool (or tools) to use when generating a response.
     */
    @Generated
    private BinaryData toolChoice;

    /*
     * The truncation strategy to use for the model response.
     * - `auto`: If the context of this response and previous ones exceeds the model's context window size, the model
     * will truncate the response to fit the context window by dropping input items in the middle of the conversation.
     * - `disabled`: If a model response will exceed the context window size for a model, the request will fail with a
     * 400 error.
     */
    @Generated
    private ResponseTruncation truncation;

    /*
     * A unique identifier representing your end-user, which can help OpenAI to monitor and detect abuse. [Learn
     * more](/docs/guides/safety-best-practices#end-user-ids).
     */
    @Generated
    private String user;

    /*
     * Text, image, or audio inputs to the model, used to generate a response.
     * Can also contain previous assistant responses and tool call outputs.
     * 
     * Learn more about prompting a model with the [Responses API](/docs/guides/responses).
     */
    @Generated
    private final BinaryData input;

    /*
     * Specifies additional output data to include in the model response.
     */
    @Generated
    private List<CreateResponsesRequestIncludable> include;

    /*
     * Specifies whether parallel tool calling should be enabled for this response.
     */
    @Generated
    private Boolean parallelToolCalls;

    /*
     * The store property.
     */
    @Generated
    private Boolean store;

    /*
     * If set to true, the model response data will be streamed to the client as it is generated using [server-sent
     * events](https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events/Using_server-sent_events#
     * Event_stream_format).
     * 
     * See the "Streaming" section below for more information.
     */
    @Generated
    private Boolean stream;

    /**
     * Creates an instance of CreateResponsesRequest class.
     *
     * @param model the model value to set.
     * @param input the input value to set.
     */
    private CreateResponsesRequest(CreateResponsesRequestModel model, BinaryData input) {
        this.model = model;
        this.input = input;
    }

    /**
     * Creates an instance of CreateResponsesRequest class.
     *
     * @param model the model value to set.
     * @param input the input value to set.
     */
    public CreateResponsesRequest(CreateResponsesRequestModel model, String input) {
        this.model = model;
        this.input = BinaryData.fromString(input);
    }

    /**
     * Creates an instance of CreateResponsesRequest class.
     *
     * @param model the model value to set.
     * @param input the input value to set.
     */
    public CreateResponsesRequest(CreateResponsesRequestModel model, List<ResponsesMessage> input) {
        this.model = model;
        this.input = BinaryData.fromObject(input);
    }

    /**
     * Get the model property: Model ID used to generate the response, like `gpt-4o` or `o1`.
     * Refer to the [model guide](/docs/models) for more information and supported features for each model.
     *
     * @return the model value.
     */
    @Generated
    public CreateResponsesRequestModel getModel() {
        return this.model;
    }

    /**
     * Get the metadata property: Set of 16 key-value pairs that can be attached to an object. This can be
     * useful for storing additional information about the object in a structured
     * format, and querying for objects via API or the dashboard.
     *
     * Keys are strings with a maximum length of 64 characters. Values are strings
     * with a maximum length of 512 characters.
     *
     * @return the metadata value.
     */
    @Generated
    public Map<String, String> getMetadata() {
        return this.metadata;
    }

    /**
     * Set the metadata property: Set of 16 key-value pairs that can be attached to an object. This can be
     * useful for storing additional information about the object in a structured
     * format, and querying for objects via API or the dashboard.
     *
     * Keys are strings with a maximum length of 64 characters. Values are strings
     * with a maximum length of 512 characters.
     *
     * @param metadata the metadata value to set.
     * @return the CreateResponsesRequest object itself.
     */
    @Generated
    public CreateResponsesRequest setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * Get the temperature property: What sampling temperature to use, between 0 and 2. Higher values like 0.8 will make
     * the output more random, while lower values like 0.2 will make it more focused and deterministic.
     *
     * We generally recommend altering this or `top_p` but not both.
     *
     * @return the temperature value.
     */
    @Generated
    public Double getTemperature() {
        return this.temperature;
    }

    /**
     * Set the temperature property: What sampling temperature to use, between 0 and 2. Higher values like 0.8 will make
     * the output more random, while lower values like 0.2 will make it more focused and deterministic.
     *
     * We generally recommend altering this or `top_p` but not both.
     *
     * @param temperature the temperature value to set.
     * @return the CreateResponsesRequest object itself.
     */
    @Generated
    public CreateResponsesRequest setTemperature(Double temperature) {
        this.temperature = temperature;
        return this;
    }

    /**
     * Get the topP property: An alternative to sampling with temperature, called nucleus sampling, where the model
     * considers the results of the tokens with top_p probability mass. So 0.1 means only the tokens comprising the top
     * 10% probability mass are considered.
     *
     * We generally recommend altering this or `temperature` but not both.
     *
     * @return the topP value.
     */
    @Generated
    public Double getTopP() {
        return this.topP;
    }

    /**
     * Set the topP property: An alternative to sampling with temperature, called nucleus sampling, where the model
     * considers the results of the tokens with top_p probability mass. So 0.1 means only the tokens comprising the top
     * 10% probability mass are considered.
     *
     * We generally recommend altering this or `temperature` but not both.
     *
     * @param topP the topP value to set.
     * @return the CreateResponsesRequest object itself.
     */
    @Generated
    public CreateResponsesRequest setTopP(Double topP) {
        this.topP = topP;
        return this;
    }

    /**
     * Get the previousResponseId property: The unique ID of the previous response to the model. Use this to create
     * multi-turn conversations.
     *
     * @return the previousResponseId value.
     */
    @Generated
    public String getPreviousResponseId() {
        return this.previousResponseId;
    }

    /**
     * Set the previousResponseId property: The unique ID of the previous response to the model. Use this to create
     * multi-turn conversations.
     *
     * @param previousResponseId the previousResponseId value to set.
     * @return the CreateResponsesRequest object itself.
     */
    @Generated
    public CreateResponsesRequest setPreviousResponseId(String previousResponseId) {
        this.previousResponseId = previousResponseId;
        return this;
    }

    /**
     * Get the reasoning property: The reasoning property.
     *
     * @return the reasoning value.
     */
    @Generated
    public ResponsesReasoningConfiguration getReasoning() {
        return this.reasoning;
    }

    /**
     * Set the reasoning property: The reasoning property.
     *
     * @param reasoning the reasoning value to set.
     * @return the CreateResponsesRequest object itself.
     */
    @Generated
    public CreateResponsesRequest setReasoning(ResponsesReasoningConfiguration reasoning) {
        this.reasoning = reasoning;
        return this;
    }

    /**
     * Get the maxOutputTokens property: An upper bound for the number of tokens that can be generated for a response,
     * including visible output tokens and [reasoning tokens](/docs/guides/reasoning).
     *
     * @return the maxOutputTokens value.
     */
    @Generated
    public Integer getMaxOutputTokens() {
        return this.maxOutputTokens;
    }

    /**
     * Set the maxOutputTokens property: An upper bound for the number of tokens that can be generated for a response,
     * including visible output tokens and [reasoning tokens](/docs/guides/reasoning).
     *
     * @param maxOutputTokens the maxOutputTokens value to set.
     * @return the CreateResponsesRequest object itself.
     */
    @Generated
    public CreateResponsesRequest setMaxOutputTokens(Integer maxOutputTokens) {
        this.maxOutputTokens = maxOutputTokens;
        return this;
    }

    /**
     * Get the instructions property: The instructions property.
     *
     * @return the instructions value.
     */
    @Generated
    public String getInstructions() {
        return this.instructions;
    }

    /**
     * Set the instructions property: The instructions property.
     *
     * @param instructions the instructions value to set.
     * @return the CreateResponsesRequest object itself.
     */
    @Generated
    public CreateResponsesRequest setInstructions(String instructions) {
        this.instructions = instructions;
        return this;
    }

    /**
     * Get the text property: The text property.
     *
     * @return the text value.
     */
    @Generated
    public ResponseTextOptions getText() {
        return this.text;
    }

    /**
     * Set the text property: The text property.
     *
     * @param text the text value to set.
     * @return the CreateResponsesRequest object itself.
     */
    @Generated
    public CreateResponsesRequest setText(ResponseTextOptions text) {
        this.text = text;
        return this;
    }

    /**
     * Get the tools property: The tools to use to generate a response.
     *
     * @return the tools value.
     */
    @Generated
    public List<ResponsesTool> getTools() {
        return this.tools;
    }

    /**
     * Set the tools property: The tools to use to generate a response.
     *
     * @param tools the tools value to set.
     * @return the CreateResponsesRequest object itself.
     */
    @Generated
    public CreateResponsesRequest setTools(List<ResponsesTool> tools) {
        this.tools = tools;
        return this;
    }

    /**
     * Get the toolChoice property: How the model should select which tool (or tools) to use when generating a response.
     *
     * @return the toolChoice value.
     */
    @Generated
    public BinaryData getToolChoice() {
        return this.toolChoice;
    }

    /**
     * Set the toolChoice property: How the model should select which tool (or tools) to use when generating a response.
     *
     * @param toolChoice the toolChoice value to set.
     * @return the CreateResponsesRequest object itself.
     */
    @Generated
    public CreateResponsesRequest setToolChoice(BinaryData toolChoice) {
        this.toolChoice = toolChoice;
        return this;
    }

    /**
     * Get the truncation property: The truncation strategy to use for the model response.
     * - `auto`: If the context of this response and previous ones exceeds the model's context window size, the model
     * will truncate the response to fit the context window by dropping input items in the middle of the conversation.
     * - `disabled`: If a model response will exceed the context window size for a model, the request will fail with a
     * 400 error.
     *
     * @return the truncation value.
     */
    @Generated
    public ResponseTruncation getTruncation() {
        return this.truncation;
    }

    /**
     * Set the truncation property: The truncation strategy to use for the model response.
     * - `auto`: If the context of this response and previous ones exceeds the model's context window size, the model
     * will truncate the response to fit the context window by dropping input items in the middle of the conversation.
     * - `disabled`: If a model response will exceed the context window size for a model, the request will fail with a
     * 400 error.
     *
     * @param truncation the truncation value to set.
     * @return the CreateResponsesRequest object itself.
     */
    @Generated
    public CreateResponsesRequest setTruncation(ResponseTruncation truncation) {
        this.truncation = truncation;
        return this;
    }

    /**
     * Get the user property: A unique identifier representing your end-user, which can help OpenAI to monitor and
     * detect abuse. [Learn more](/docs/guides/safety-best-practices#end-user-ids).
     *
     * @return the user value.
     */
    @Generated
    public String getUser() {
        return this.user;
    }

    /**
     * Set the user property: A unique identifier representing your end-user, which can help OpenAI to monitor and
     * detect abuse. [Learn more](/docs/guides/safety-best-practices#end-user-ids).
     *
     * @param user the user value to set.
     * @return the CreateResponsesRequest object itself.
     */
    @Generated
    public CreateResponsesRequest setUser(String user) {
        this.user = user;
        return this;
    }

    /**
     * Get the input property: Text, image, or audio inputs to the model, used to generate a response.
     * Can also contain previous assistant responses and tool call outputs.
     *
     * Learn more about prompting a model with the [Responses API](/docs/guides/responses).
     *
     * @return the input value.
     */
    @Generated
    public BinaryData getInput() {
        return this.input;
    }

    /**
     * Get the include property: Specifies additional output data to include in the model response.
     *
     * @return the include value.
     */
    @Generated
    public List<CreateResponsesRequestIncludable> getInclude() {
        return this.include;
    }

    /**
     * Set the include property: Specifies additional output data to include in the model response.
     *
     * @param include the include value to set.
     * @return the CreateResponsesRequest object itself.
     */
    @Generated
    public CreateResponsesRequest setInclude(List<CreateResponsesRequestIncludable> include) {
        this.include = include;
        return this;
    }

    /**
     * Get the parallelToolCalls property: Specifies whether parallel tool calling should be enabled for this response.
     *
     * @return the parallelToolCalls value.
     */
    @Generated
    public Boolean isParallelToolCalls() {
        return this.parallelToolCalls;
    }

    /**
     * Set the parallelToolCalls property: Specifies whether parallel tool calling should be enabled for this response.
     *
     * @param parallelToolCalls the parallelToolCalls value to set.
     * @return the CreateResponsesRequest object itself.
     */
    @Generated
    public CreateResponsesRequest setParallelToolCalls(Boolean parallelToolCalls) {
        this.parallelToolCalls = parallelToolCalls;
        return this;
    }

    /**
     * Get the store property: The store property.
     *
     * @return the store value.
     */
    @Generated
    public Boolean isStore() {
        return this.store;
    }

    /**
     * Set the store property: The store property.
     *
     * @param store the store value to set.
     * @return the CreateResponsesRequest object itself.
     */
    @Generated
    public CreateResponsesRequest setStore(Boolean store) {
        this.store = store;
        return this;
    }

    /**
     * Get the stream property: If set to true, the model response data will be streamed to the client as it is
     * generated using [server-sent
     * events](https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events/Using_server-sent_events#Event_stream_format).
     *
     * See the "Streaming" section below for more information.
     *
     * @return the stream value.
     */
    @Generated
    public Boolean isStream() {
        return this.stream;
    }

    /**
     * Set the stream property: If set to true, the model response data will be streamed to the client as it is
     * generated using [server-sent
     * events](https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events/Using_server-sent_events#Event_stream_format).
     *
     * See the "Streaming" section below for more information.
     *
     * @param stream the stream value to set.
     * @return the CreateResponsesRequest object itself.
     */
    private CreateResponsesRequest setStream(Boolean stream) {
        this.stream = stream;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("model", this.model == null ? null : this.model.toString());
        jsonWriter.writeFieldName("input");
        this.input.writeTo(jsonWriter);
        jsonWriter.writeMapField("metadata", this.metadata, (writer, element) -> writer.writeString(element));
        jsonWriter.writeNumberField("temperature", this.temperature);
        jsonWriter.writeNumberField("top_p", this.topP);
        jsonWriter.writeStringField("previous_response_id", this.previousResponseId);
        jsonWriter.writeJsonField("reasoning", this.reasoning);
        jsonWriter.writeNumberField("max_output_tokens", this.maxOutputTokens);
        jsonWriter.writeStringField("instructions", this.instructions);
        jsonWriter.writeJsonField("text", this.text);
        jsonWriter.writeArrayField("tools", this.tools, (writer, element) -> writer.writeJson(element));
        if (this.toolChoice != null) {
            jsonWriter.writeFieldName("tool_choice");
            this.toolChoice.writeTo(jsonWriter);
        }
        jsonWriter.writeStringField("truncation", this.truncation == null ? null : this.truncation.toString());
        jsonWriter.writeStringField("user", this.user);
        jsonWriter.writeArrayField("include", this.include,
            (writer, element) -> writer.writeString(element == null ? null : element.toString()));
        jsonWriter.writeBooleanField("parallel_tool_calls", this.parallelToolCalls);
        jsonWriter.writeBooleanField("store", this.store);
        jsonWriter.writeBooleanField("stream", this.stream);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of CreateResponsesRequest from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of CreateResponsesRequest if the JsonReader was pointing to an instance of it, or null if it
     * was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the CreateResponsesRequest.
     */
    @Generated
    public static CreateResponsesRequest fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            CreateResponsesRequestModel model = null;
            BinaryData input = null;
            Map<String, String> metadata = null;
            Double temperature = null;
            Double topP = null;
            String previousResponseId = null;
            ResponsesReasoningConfiguration reasoning = null;
            Integer maxOutputTokens = null;
            String instructions = null;
            ResponseTextOptions text = null;
            List<ResponsesTool> tools = null;
            BinaryData toolChoice = null;
            ResponseTruncation truncation = null;
            String user = null;
            List<CreateResponsesRequestIncludable> include = null;
            Boolean parallelToolCalls = null;
            Boolean store = null;
            Boolean stream = null;
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("model".equals(fieldName)) {
                    model = CreateResponsesRequestModel.fromString(reader.getString());
                } else if ("input".equals(fieldName)) {
                    input = reader.getNullable(nonNullReader -> BinaryData.fromObject(nonNullReader.readUntyped()));
                } else if ("metadata".equals(fieldName)) {
                    metadata = reader.readMap(reader1 -> reader1.getString());
                } else if ("temperature".equals(fieldName)) {
                    temperature = reader.getNullable(JsonReader::getDouble);
                } else if ("top_p".equals(fieldName)) {
                    topP = reader.getNullable(JsonReader::getDouble);
                } else if ("previous_response_id".equals(fieldName)) {
                    previousResponseId = reader.getString();
                } else if ("reasoning".equals(fieldName)) {
                    reasoning = ResponsesReasoningConfiguration.fromJson(reader);
                } else if ("max_output_tokens".equals(fieldName)) {
                    maxOutputTokens = reader.getNullable(JsonReader::getInt);
                } else if ("instructions".equals(fieldName)) {
                    instructions = reader.getString();
                } else if ("text".equals(fieldName)) {
                    text = ResponseTextOptions.fromJson(reader);
                } else if ("tools".equals(fieldName)) {
                    tools = reader.readArray(reader1 -> ResponsesTool.fromJson(reader1));
                } else if ("tool_choice".equals(fieldName)) {
                    toolChoice
                        = reader.getNullable(nonNullReader -> BinaryData.fromObject(nonNullReader.readUntyped()));
                } else if ("truncation".equals(fieldName)) {
                    truncation = ResponseTruncation.fromString(reader.getString());
                } else if ("user".equals(fieldName)) {
                    user = reader.getString();
                } else if ("include".equals(fieldName)) {
                    include
                        = reader.readArray(reader1 -> CreateResponsesRequestIncludable.fromString(reader1.getString()));
                } else if ("parallel_tool_calls".equals(fieldName)) {
                    parallelToolCalls = reader.getNullable(JsonReader::getBoolean);
                } else if ("store".equals(fieldName)) {
                    store = reader.getNullable(JsonReader::getBoolean);
                } else if ("stream".equals(fieldName)) {
                    stream = reader.getNullable(JsonReader::getBoolean);
                } else {
                    reader.skipChildren();
                }
            }
            CreateResponsesRequest deserializedCreateResponsesRequest = new CreateResponsesRequest(model, input);
            deserializedCreateResponsesRequest.metadata = metadata;
            deserializedCreateResponsesRequest.temperature = temperature;
            deserializedCreateResponsesRequest.topP = topP;
            deserializedCreateResponsesRequest.previousResponseId = previousResponseId;
            deserializedCreateResponsesRequest.reasoning = reasoning;
            deserializedCreateResponsesRequest.maxOutputTokens = maxOutputTokens;
            deserializedCreateResponsesRequest.instructions = instructions;
            deserializedCreateResponsesRequest.text = text;
            deserializedCreateResponsesRequest.tools = tools;
            deserializedCreateResponsesRequest.toolChoice = toolChoice;
            deserializedCreateResponsesRequest.truncation = truncation;
            deserializedCreateResponsesRequest.user = user;
            deserializedCreateResponsesRequest.include = include;
            deserializedCreateResponsesRequest.parallelToolCalls = parallelToolCalls;
            deserializedCreateResponsesRequest.store = store;
            deserializedCreateResponsesRequest.stream = stream;
            return deserializedCreateResponsesRequest;
        });
    }

    static {
        CreateResponsesRequestAccessHelper
            .setAccessor(new CreateResponsesRequestAccessHelper.CreateResponsesRequestAccessor() {
                @Override
                public void setStream(CreateResponsesRequest options, boolean stream) {
                    options.setStream(stream);
                }
            });
    }
}
