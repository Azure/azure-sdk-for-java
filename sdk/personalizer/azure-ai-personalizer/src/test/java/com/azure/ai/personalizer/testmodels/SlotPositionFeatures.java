// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer.testmodels;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

public class SlotPositionFeatures implements JsonSerializable<SlotPositionFeatures> {
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

    String size;
    String position;

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("size", size)
            .writeStringField("position", position)
            .writeEndObject();
    }

    public static SlotPositionFeatures fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            SlotPositionFeatures slotPositionFeatures = new SlotPositionFeatures();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("size".equals(fieldName)) {
                    slotPositionFeatures.size = reader.getString();
                } else if ("position".equals(fieldName)) {
                    slotPositionFeatures.position = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return slotPositionFeatures;
        });
    }
}
