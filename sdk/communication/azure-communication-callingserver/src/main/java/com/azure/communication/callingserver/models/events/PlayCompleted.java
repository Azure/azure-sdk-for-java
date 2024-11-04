// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models.events;

import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/** The PlayCompleted model. */
@Immutable
public final class PlayCompleted extends CallAutomationEventBase {
    /*
     * Operation context
     */
    private String operationContext;

    /*
     * The resultInfo property.
     */
    private ResultInfo resultInfo;

    private PlayCompleted() {
    }

    /**
     * Get the operationContext property: Operation context.
     *
     * @return the operationContext value.
     */
    public String getOperationContext() {
        return this.operationContext;
    }

    /**
     * Get the resultInfo property: The resultInfo property.
     *
     * @return the resultInfo value.
     */
    public ResultInfo getResultInfo() {
        return this.resultInfo;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return toJsonShared(jsonWriter.writeStartObject()).writeStringField("operationContext", operationContext)
            .writeJsonField("resultInfo", resultInfo)
            .writeEndObject();
    }

    /**
     * Reads an instance of {@link PlayCompleted} from the {@link JsonReader}.
     *
     * @param jsonReader The {@link JsonReader} to read from.
     * @return An instance of {@link PlayCompleted}, or null if the {@link JsonReader} was pointing to
     * {@link JsonToken#NULL}.
     * @throws IOException If an error occurs while reading the {@link JsonReader}.
     */
    public static PlayCompleted fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            PlayCompleted event = new PlayCompleted();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if (fromJsonShared(event, fieldName, reader)) {
                    continue;
                }

                if ("operationContext".equals(fieldName)) {
                    event.operationContext = reader.getString();
                } else if ("resultInfo".equals(fieldName)) {
                    event.resultInfo = ResultInfo.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }

            return event;
        });
    }
}
