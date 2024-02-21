// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.models;

import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a geometric line.
 */
@Immutable
public final class GeoLineString extends GeoObject {
    private final GeoArray<GeoPosition> coordinates;

    /**
     * Constructs a geometric line.
     *
     * @param positions Geometric positions that define the line.
     * @throws NullPointerException If {@code positions} is {@code null}.
     */
    public GeoLineString(List<GeoPosition> positions) {
        this(positions, null, null);
    }

    /**
     * Constructs a geometric line.
     *
     * @param positions Geometric positions that define the line.
     * @param boundingBox Bounding box for the line.
     * @param customProperties Additional properties of the geometric line.
     * @throws NullPointerException If {@code positions} is {@code null}.
     */
    public GeoLineString(List<GeoPosition> positions, GeoBoundingBox boundingBox,
        Map<String, Object> customProperties) {
        super(boundingBox, customProperties);

        Objects.requireNonNull(positions, "'positions' cannot be null.");
        this.coordinates = new GeoArray<>(new ArrayList<>(positions));
    }

    /**
     * Unmodifiable representation of the {@link GeoPosition geometric positions} representing this line.
     *
     * @return An unmodifiable representation of the {@link GeoPosition geometric positions} representing this line.
     */
    public List<GeoPosition> getCoordinates() {
        return coordinates;
    }

    @Override
    public GeoObjectType getType() {
        return GeoObjectType.LINE_STRING;
    }

    @Override
    public int hashCode() {
        return Objects.hash(coordinates, super.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GeoLineString)) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        GeoLineString other = (GeoLineString) obj;
        return super.equals(other) && Objects.equals(coordinates, other.coordinates);
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject()
            .writeStringField("type", GeoObjectType.LINE_STRING.toString())
            .writeArrayField("coordinates", getCoordinates(), JsonWriter::writeJson)
            .writeJsonField("bbox", getBoundingBox());

        return writeCustomProperties(jsonWriter).writeEndObject();
    }

    /**
     * Reads a JSON stream into a {@link GeoLineString}.
     *
     * @param jsonReader The {@link JsonReader} being read.
     * @return The {@link GeoLineString} that the JSON stream represented, or null if it pointed to JSON null.
     * @throws IllegalStateException If the {@code type} node exists and isn't equal to {@code LineString}.
     * @throws IOException If a {@link GeoLineString} fails to be read from the {@code jsonReader}.
     */
    public static GeoLineString fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            List<GeoPosition> coordinates = null;
            GeoBoundingBox boundingBox = null;
            Map<String, Object> customProperties = null;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("type".equals(fieldName)) {
                    String type = reader.getString();
                    if (!GeoObjectType.LINE_STRING.toString().equals(type)) {
                        throw new IllegalStateException("'type' was expected to be non-null and equal to 'LineString'. "
                            + "The found 'type' was '" + type + "'.");
                    }
                } else if ("coordinates".equals(fieldName)) {
                    coordinates = reader.readArray(GeoPosition::fromJson);
                } else if ("bbox".equals(fieldName)) {
                    boundingBox = GeoBoundingBox.fromJson(reader);
                } else {
                    if (customProperties == null) {
                        customProperties = new LinkedHashMap<>();
                    }

                    customProperties.put(fieldName, reader.readUntyped());
                }
            }

            return new GeoLineString(coordinates, boundingBox, customProperties);
        });
    }
}
