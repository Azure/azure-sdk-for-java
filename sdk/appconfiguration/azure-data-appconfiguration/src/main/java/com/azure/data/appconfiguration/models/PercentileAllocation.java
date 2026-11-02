// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.appconfiguration.models;

import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * Allocates a variant to a percentile range.
 */
@Immutable
public final class PercentileAllocation implements JsonSerializable<PercentileAllocation> {
    private final String variant;
    private final double from;
    private final double to;

    /**
     * Creates a percentile allocation.
     * @param variant The variant name.
     * @param from The starting percentile.
     * @param to The ending percentile.
     */
    public PercentileAllocation(String variant, double from, double to) {
        this.variant = variant;
        this.from = from;
        this.to = to;
    }

    /**
     * Gets the variant name.
     * @return The variant name.
     */
    public String getVariant() {
        return variant;
    }

    /**
     * Gets the starting percentile.
     * @return The starting percentile.
     */
    public double getFrom() {
        return from;
    }

    /**
     * Gets the ending percentile.
     * @return The ending percentile.
     */
    public double getTo() {
        return to;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("variant", variant)
            .writeDoubleField("from", from)
            .writeDoubleField("to", to)
            .writeEndObject();
    }

    /**
     * Reads a percentile allocation.
     * @param jsonReader The JSON reader.
     * @return The allocation, or null if the JSON value is null.
     * @throws IOException If reading fails.
     */
    public static PercentileAllocation fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String variant = null;
            double from = 0;
            double to = 0;
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("variant".equals(fieldName)) {
                    variant = reader.getString();
                } else if ("from".equals(fieldName)) {
                    from = reader.getDouble();
                } else if ("to".equals(fieldName)) {
                    to = reader.getDouble();
                } else {
                    reader.skipChildren();
                }
            }
            return new PercentileAllocation(variant, from, to);
        });
    }
}
