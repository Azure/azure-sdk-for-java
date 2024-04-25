// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.implementation;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;

import java.io.IOException;

public final class Properties implements JsonSerializable<Properties> {
    private Unit unit = new Unit();

    private Location location = new Location();

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeJsonField("unit", this.unit);
        jsonWriter.writeJsonField("location", this.location);
        return jsonWriter.writeEndObject();
    }

    public static Properties fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {

            Unit unit = null;
            Location location = null;

            while (reader.nextToken() != null) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("unit".equals(fieldName)) {
                    unit = Unit.fromJson(reader);
                } else if ("location".equals(fieldName)) {
                    location = Location.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }

            Properties model = new Properties();
            model.setUnit(unit);
            model.setLocation(location);
            return model;
        });
    }
}
