// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models.events;

import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/** The CallConnectedEvent model. */
@Immutable
public final class CallConnectedEvent extends CallAutomationEventBase {
    private CallConnectedEvent() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("callConnectionId", super.getCallConnectionId());
        jsonWriter.writeStringField("serverCallId", super.getServerCallId());
        jsonWriter.writeStringField("correlationId", super.getCorrelationId());
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of CallConnectedEvent from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of CallConnectedEvent if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the CallConnectedEvent.
     */
    public static CallConnectedEvent fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            final CallConnectedEvent event = new CallConnectedEvent();
            while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("callConnectionId".equals(fieldName)) {
                    event.setCallConnectionId(reader.getString());
                } else if ("serverCallId".equals(fieldName)) {
                    event.setServerCallId(reader.getString());
                } else if ("correlationId".equals(fieldName)) {
                    event.setCorrelationId(reader.getString());
                } else {
                    reader.skipChildren();
                }
            }
            return event;
        });
    }
}
