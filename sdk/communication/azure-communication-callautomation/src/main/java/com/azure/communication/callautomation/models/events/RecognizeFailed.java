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
    static RecognizeFailed fromJsonImpl(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            final RecognizeFailed event = new RecognizeFailed();
            while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("resultInformation".equals(fieldName)) {
                    event.resultInformation = ResultInformation.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }
            return event;
        });
    }
}
