// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.responses.models;

import com.azure.core.annotation.Generated;
import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * The ResponsesStreamEvent model.
 */
@Immutable
public class ResponsesStreamEvent implements JsonSerializable<ResponsesStreamEvent> {

    /*
     * The type property.
     */
    @Generated
    private ResponsesStreamEventType type;

    /**
     * Creates an instance of ResponsesStreamEvent class.
     */
    @Generated
    protected ResponsesStreamEvent() {
    }

    /**
     * Get the type property: The type property.
     *
     * @return the type value.
     */
    @Generated
    public ResponsesStreamEventType getType() {
        return this.type;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("type", this.type == null ? null : this.type.toString());
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of ResponsesStreamEvent from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of ResponsesStreamEvent if the JsonReader was pointing to an instance of it, or null if it
     * was pointing to JSON null.
     * @throws IOException If an error occurs while reading the ResponsesStreamEvent.
     */
    @Generated
    public static ResponsesStreamEvent fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String discriminatorValue = null;
            try (JsonReader readerToUse = reader.bufferObject()) {
                // Prepare for reading
                readerToUse.nextToken();
                while (readerToUse.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = readerToUse.getFieldName();
                    readerToUse.nextToken();
                    if ("type".equals(fieldName)) {
                        discriminatorValue = readerToUse.getString();
                        break;
                    } else {
                        readerToUse.skipChildren();
                    }
                }
                // Use the discriminator value to determine which subtype should be deserialized.
                if ("error".equals(discriminatorValue)) {
                    return ResponsesStreamEventError.fromJson(readerToUse.reset());
                } else if ("response.completed".equals(discriminatorValue)) {
                    return ResponsesStreamEventCompleted.fromJson(readerToUse.reset());
                } else if ("response.content_part.added".equals(discriminatorValue)) {
                    return ResponsesStreamEventContentPartAdded.fromJson(readerToUse.reset());
                } else if ("response.content_part.done".equals(discriminatorValue)) {
                    return ResponsesStreamEventContentPartDone.fromJson(readerToUse.reset());
                } else if ("response.created".equals(discriminatorValue)) {
                    return ResponsesStreamEventCreated.fromJson(readerToUse.reset());
                } else if ("response.failed".equals(discriminatorValue)) {
                    return ResponsesStreamEventFailed.fromJson(readerToUse.reset());
                } else if ("response.file_search_call.completed".equals(discriminatorValue)) {
                    return ResponsesStreamEventFileSearchCallCompleted.fromJson(readerToUse.reset());
                } else if ("response.file_search_call.in.progress".equals(discriminatorValue)) {
                    return ResponsesStreamEventFileSearchCallInProgress.fromJson(readerToUse.reset());
                } else if ("response.file_search_call.searching".equals(discriminatorValue)) {
                    return ResponsesStreamEventFileSearchCallSearching.fromJson(readerToUse.reset());
                } else if ("response.function_call_arguments.delta".equals(discriminatorValue)) {
                    return ResponsesStreamEventFunctionCallArgumentsDelta.fromJson(readerToUse.reset());
                } else if ("response.function_call_arguments.done".equals(discriminatorValue)) {
                    return ResponsesStreamEventFunctionCallArgumentsDone.fromJson(readerToUse.reset());
                } else if ("response.incomplete".equals(discriminatorValue)) {
                    return ResponsesStreamEventIncomplete.fromJson(readerToUse.reset());
                } else if ("response.in_progress".equals(discriminatorValue)) {
                    return ResponsesStreamEventInProgress.fromJson(readerToUse.reset());
                } else if ("response.output_item.added".equals(discriminatorValue)) {
                    return ResponsesStreamEventOutputItemAdded.fromJson(readerToUse.reset());
                } else if ("response.output_item.done".equals(discriminatorValue)) {
                    return ResponsesStreamEventOutputItemDone.fromJson(readerToUse.reset());
                } else if ("response.output_text.annotation.added".equals(discriminatorValue)) {
                    return ResponsesStreamEventOutputTextAnnotationAdded.fromJson(readerToUse.reset());
                } else if ("response.output_text.delta".equals(discriminatorValue)) {
                    return ResponsesStreamEventOutputTextDelta.fromJson(readerToUse.reset());
                } else if ("response.output_text.done".equals(discriminatorValue)) {
                    return ResponsesStreamEventOutputTextDone.fromJson(readerToUse.reset());
                } else if ("response.refusal.delta".equals(discriminatorValue)) {
                    return ResponsesStreamEventRefusalDelta.fromJson(readerToUse.reset());
                } else if ("response.refusal.done".equals(discriminatorValue)) {
                    return ResponsesStreamEventRefusalDone.fromJson(readerToUse.reset());
                } else if ("response.web_search_call.completed".equals(discriminatorValue)) {
                    return ResponsesResponseStreamEventResponseWebSearchCallCompleted.fromJson(readerToUse.reset());
                } else if ("response.web_search_call.in_progress".equals(discriminatorValue)) {
                    return ResponsesResponseStreamEventResponseWebSearchCallInProgress.fromJson(readerToUse.reset());
                } else if ("response.web_search_call.searching".equals(discriminatorValue)) {
                    return ResponsesResponseStreamEventResponseWebSearchCallSearching.fromJson(readerToUse.reset());
                } else {
                    return fromJsonKnownDiscriminator(readerToUse.reset());
                }
            }
        });
    }

    @Generated
    static ResponsesStreamEvent fromJsonKnownDiscriminator(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            ResponsesStreamEvent deserializedResponsesStreamEvent = new ResponsesStreamEvent();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("type".equals(fieldName)) {
                    deserializedResponsesStreamEvent.type = ResponsesStreamEventType.fromString(reader.getString());
                } else {
                    reader.skipChildren();
                }
            }
            return deserializedResponsesStreamEvent;
        });
    }
}
