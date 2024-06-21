// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/** AnswerFailed event. */
@Immutable
public final class AnswerFailed extends CallAutomationEventBase {

    /*
     * Contains the resulting SIP code, sub-code and message.
     */
    private ResultInformation resultInformation;

    private AnswerFailed() {
        this.resultInformation = null;
    }

    /**
     * Get the resultInformation property: Contains the resulting SIP code, sub-code and message.
     *
     * @return the resultInformation value.
     */
    public ResultInformation getResultInformation() {
        return this.resultInformation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeJsonField("resultInformation", resultInformation);
        super.writeFields(jsonWriter);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of AnswerFailed from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of AnswerFailed if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the AnswerFailed.
     */
    public static AnswerFailed fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            final AnswerFailed event = new AnswerFailed();
            while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("resultInformation".equals(fieldName)) {
                    event.resultInformation = ResultInformation.fromJson(reader);
                } else {
                    if (!event.readField(fieldName, reader)) {
                        reader.skipChildren();
                    }
                }
            }
            return event;
        });
    }
}
