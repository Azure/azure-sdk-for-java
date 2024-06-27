// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;

import java.io.IOException;

/** The RecognizeFailed model. */
@Fluent
public final class RecognizeFailed extends CallAutomationEventBaseWithReasonCode {
    /**
     * Reads an instance of RecognizeFailed from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of RecognizeFailed if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the RecognizeFailed.
     */
    public static RecognizeFailed fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            final RecognizeFailed event = new RecognizeFailed();
            while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("resultInformation".equals(fieldName)) {
                    event.resultInformation = ResultInformation.fromJson(reader);
                } else {
                    if (!event.handleField(fieldName, reader)) {
                        reader.skipChildren();
                    }
                }
            }
            return event;
        });
    }
}
