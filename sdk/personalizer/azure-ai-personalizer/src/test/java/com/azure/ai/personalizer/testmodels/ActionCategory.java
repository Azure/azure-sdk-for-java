// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer.testmodels;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;

import java.io.IOException;

import static com.azure.ai.personalizer.TestUtils.deserializationHelper;

public class ActionCategory implements JsonSerializable<ActionCategory> {
    String mostWatchedByAge;

    public String getMostWatchedByAge() {
        return mostWatchedByAge;
    }

    public ActionCategory setMostWatchedByAge(String mostWatchedByAge) {
        this.mostWatchedByAge = mostWatchedByAge;
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject().writeStringField("mostWatchedByAge", mostWatchedByAge).writeEndObject();
    }

    public static ActionCategory fromJson(JsonReader jsonReader) throws IOException {
        return deserializationHelper(jsonReader, ActionCategory::new, (reader, fieldName, actionCategory) -> {
            if ("mostWatchedByAge".equals(fieldName)) {
                actionCategory.mostWatchedByAge = reader.getString();
            } else {
                reader.skipChildren();
            }
        });
    }
}
