// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.realtime.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

// Manually added model
/**
 * The RealtimeTurnDetectionDisabled model. Supported only in Azure.
 */
@Fluent
public final class RealtimeTurnDetectionDisabled extends RealtimeTurnDetection {

    /*
     * The type property.
     */
    private RealtimeTurnDetectionType type = RealtimeTurnDetectionType.fromString("none");

    /**
     * Creates an instance of RealtimeTurnDetectionDisabled. Only accepted in Azure OpenAI services. Non-Azure OpenAI
     * will fail the request if this is used. In that case, don't set the turn detection in your session configuration.
     */
    public RealtimeTurnDetectionDisabled() {
    }

    /**
     * Get the type property: The type property.
     * 
     * @return the type value.
     */
    @Override
    public RealtimeTurnDetectionType getType() {
        return this.type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        // Azure supports, `none`, but non-Azure only accepts `server_vad`
        jsonWriter.writeStringField("type", this.type == null ? null : this.type.toString());
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of RealtimeServerVadTurnDetection from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of RealtimeServerVadTurnDetection if the JsonReader was pointing to an instance of it, or
     * null if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the RealtimeServerVadTurnDetection.
     */
    public static RealtimeTurnDetectionDisabled fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            RealtimeTurnDetectionDisabled deserializedRealtimeServerVadTurnDetection
                = new RealtimeTurnDetectionDisabled();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("type".equals(fieldName)) {
                    deserializedRealtimeServerVadTurnDetection.type
                        = RealtimeTurnDetectionType.fromString(reader.getString());
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedRealtimeServerVadTurnDetection;
        });
    }
}
