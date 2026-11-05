// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import com.azure.ai.agents.implementation.utils.Beta;
import com.azure.core.annotation.Immutable;
import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.util.Objects;

/**
 * An input item for a voice-agent response. Use the factory methods to include messages, function calls,
 * function call outputs, and references in the same response input list.
 */
@Immutable
@Beta(warningText = "Preview API. VoiceAgents=V1Preview")
public final class RealtimeResponseInputItem implements JsonSerializable<RealtimeResponseInputItem> {
    private static final ClientLogger LOGGER = new ClientLogger(RealtimeResponseInputItem.class);

    private final JsonSerializable<?> value;

    private RealtimeResponseInputItem(JsonSerializable<?> value) {
        this.value = Objects.requireNonNull(value, "'value' cannot be null.");
    }

    /**
     * Creates an input item containing a system, developer, user, or assistant message.
     * @param message the message.
     * @return the input item.
     * @throws NullPointerException if {@code message} is null.
     */
    public static RealtimeResponseInputItem fromMessage(RealtimeResponseInputMessage message) {
        return new RealtimeResponseInputItem(message);
    }

    /**
     * Creates an input item containing a function call.
     * @param functionCall the function call.
     * @return the input item.
     * @throws NullPointerException if {@code functionCall} is null.
     */
    public static RealtimeResponseInputItem fromFunctionCall(RealtimeResponseInputFunctionCall functionCall) {
        return new RealtimeResponseInputItem(functionCall);
    }

    /**
     * Creates an input item containing a function call output.
     * @param functionCallOutput the function call output.
     * @return the input item.
     * @throws NullPointerException if {@code functionCallOutput} is null.
     */
    public static RealtimeResponseInputItem
        fromFunctionCallOutput(RealtimeResponseInputFunctionCallOutput functionCallOutput) {
        return new RealtimeResponseInputItem(functionCallOutput);
    }

    /**
     * Creates an input item referencing an existing item in the same Realtime session.
     * @param itemReference the item reference.
     * @return the input item.
     * @throws NullPointerException if {@code itemReference} is null.
     */
    public static RealtimeResponseInputItem fromItemReference(RealtimeResponseItemReference itemReference) {
        return new RealtimeResponseInputItem(itemReference);
    }

    /**
     * Gets the message variant.
     * @return the message, or null if this item contains another variant.
     */
    public RealtimeResponseInputMessage getMessage() {
        return value instanceof RealtimeResponseInputMessage ? (RealtimeResponseInputMessage) value : null;
    }

    /**
     * Gets the function call variant.
     * @return the function call, or null if this item contains another variant.
     */
    public RealtimeResponseInputFunctionCall getFunctionCall() {
        return value instanceof RealtimeResponseInputFunctionCall ? (RealtimeResponseInputFunctionCall) value : null;
    }

    /**
     * Gets the function call output variant.
     * @return the function call output, or null if this item contains another variant.
     */
    public RealtimeResponseInputFunctionCallOutput getFunctionCallOutput() {
        return value instanceof RealtimeResponseInputFunctionCallOutput
            ? (RealtimeResponseInputFunctionCallOutput) value
            : null;
    }

    /**
     * Gets the item reference variant.
     * @return the item reference, or null if this item contains another variant.
     */
    public RealtimeResponseItemReference getItemReference() {
        return value instanceof RealtimeResponseItemReference ? (RealtimeResponseItemReference) value : null;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return value.toJson(jsonWriter);
    }

    /**
     * Reads an input item, selecting its variant using the {@code type} discriminator.
     * @param jsonReader the JSON reader.
     * @return the input item, or null for JSON null.
     * @throws IOException if the JSON cannot be read.
     * @throws IllegalStateException if the item type is missing or unsupported.
     */
    public static RealtimeResponseInputItem fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            try (JsonReader bufferedReader = reader.bufferObject()) {
                String type = null;
                bufferedReader.nextToken();
                while (bufferedReader.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = bufferedReader.getFieldName();
                    bufferedReader.nextToken();
                    if ("type".equals(fieldName)) {
                        type = bufferedReader.getString();
                        break;
                    }
                    bufferedReader.skipChildren();
                }
                if ("message".equals(type)) {
                    return fromMessage(RealtimeResponseInputMessage.fromJson(bufferedReader.reset()));
                } else if ("function_call".equals(type)) {
                    return fromFunctionCall(RealtimeResponseInputFunctionCall.fromJson(bufferedReader.reset()));
                } else if ("function_call_output".equals(type)) {
                    return fromFunctionCallOutput(
                        RealtimeResponseInputFunctionCallOutput.fromJson(bufferedReader.reset()));
                } else if ("item_reference".equals(type)) {
                    return fromItemReference(RealtimeResponseItemReference.fromJson(bufferedReader.reset()));
                }
                throw LOGGER.logExceptionAsError(
                    new IllegalStateException("Unsupported realtime response input item type: " + type));
            }
        });
    }
}
