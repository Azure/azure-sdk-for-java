// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.utils;

import io.clientcore.core.serialization.json.JsonReader;
import io.clientcore.core.serialization.json.JsonSerializable;
import io.clientcore.core.serialization.json.JsonToken;
import io.clientcore.core.serialization.json.JsonWriter;

import java.io.IOException;

public class BarModel implements JsonSerializable<BarModel> {

    private String barId;
    private String barName;

    public String getBarId() {
        return barId;
    }

    public BarModel setBarId(String barId) {
        this.barId = barId;
        return this;
    }

    public String getBarName() {
        return barName;
    }

    public BarModel setBarName(String barName) {
        this.barName = barName;
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("barId", barId);
        jsonWriter.writeStringField("barName", barName);
        jsonWriter.writeEndObject();
        return jsonWriter;
    }

    public static BarModel fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            BarModel barModel = new BarModel();
            boolean isEmpty = true;
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                if ("barId".equals(fieldName)) {
                    barModel.setBarId(reader.getString());
                    isEmpty = false;
                } else if ("barName".equals(fieldName)) {
                    barModel.setBarName(reader.getString());
                    isEmpty = false;
                } else {
                    reader.skipChildren();
                }
            }
            if (isEmpty) {
                throw new IOException("Not a valid BarModel json");
            }
            return barModel;
        });
    }
}
