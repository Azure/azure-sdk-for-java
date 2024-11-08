// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.quickpulse.model;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

public class QuickPulseMetrics implements JsonSerializable<QuickPulseMetrics> {
    private final String name;
    private final double value;
    private final int weight;

    public QuickPulseMetrics(String name, double value, int weight) {
        this.name = name;
        this.value = value;
        this.weight = weight;
    }

    public String getName() {
        return name;
    }

    public double getValue() {
        return value;
    }

    public int getWeight() {
        return weight;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("Name", name)
            .writeDoubleField("Value", value)
            .writeIntField("Weight", weight)
            .writeEndObject();
    }

    public static QuickPulseMetrics fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String name = null;
            double value = 0;
            int weight = 0;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("Name".equals(fieldName)) {
                    name = reader.getString();
                } else if ("Value".equals(fieldName)) {
                    value = reader.getDouble();
                } else if ("Weight".equals(fieldName)) {
                    weight = reader.getInt();
                } else {
                    reader.skipChildren();
                }
            }

            return new QuickPulseMetrics(name, value, weight);
        });
    }
}
