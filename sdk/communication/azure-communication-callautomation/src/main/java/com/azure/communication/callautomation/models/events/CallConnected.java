// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;

import java.io.IOException;

/** The CallConnected model. */
@Immutable
public final class CallConnected extends CallAutomationEventBase {
    private CallConnected() {
    }

    /**
     * Reads an instance of CallConnected from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of CallConnected if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the CallConnected.
     */
    public static CallConnected fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            final CallConnected event = new CallConnected();
            while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if (!event.handleField(fieldName, reader)) {
                    reader.skipChildren();
                }
            }
            return event;
        });
    }
}
