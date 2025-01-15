// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer.testmodels;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;

import java.io.IOException;

import static com.azure.ai.personalizer.TestUtils.deserializationHelper;

public class SlotPositionFeatures implements JsonSerializable<SlotPositionFeatures> {
    String size;
    String position;

    public String getSize() {
        return size;
    }

    public SlotPositionFeatures setSize(String size) {
        this.size = size;
        return this;
    }

    public String getPosition() {
        return position;
    }

    public SlotPositionFeatures setPosition(String position) {
        this.position = position;
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("size", size)
            .writeStringField("position", position)
            .writeEndObject();
    }

    public static SlotPositionFeatures fromJson(JsonReader jsonReader) throws IOException {
        return deserializationHelper(jsonReader, SlotPositionFeatures::new, (reader, fieldName, features) -> {
            if ("size".equals(fieldName)) {
                features.size = reader.getString();
            } else if ("position".equals(fieldName)) {
                features.position = reader.getString();
            } else {
                reader.skipChildren();
            }
        });
    }
}
