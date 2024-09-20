// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer.testmodels;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

public class ActionCategory implements JsonSerializable<ActionCategory> {
    public String getMostWatchedByAge() {
        return mostWatchedByAge;
    }

    public ActionCategory setMostWatchedByAge(String mostWatchedByAge) {
        this.mostWatchedByAge = mostWatchedByAge;
        return this;
    }

    String mostWatchedByAge;

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("mostWatchedByAge", mostWatchedByAge)
            .writeEndObject();
    }

    public static ActionCategory fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            ActionCategory actionCategory = new ActionCategory();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("mostWatchedByAge".equals(fieldName)) {
                    actionCategory.mostWatchedByAge = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return actionCategory;
        });
    }
}
