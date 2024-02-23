// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.models;

import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a collection of {@link GeoLineString GeoLines}.
 */
@Immutable
public final class GeoLineStringCollection extends GeoObject {
    private final List<GeoLineString> lines;

    /**
     * Constructs a {@link GeoLineStringCollection}.
     *
     * @param lines The geometric lines that define the multi-line.
     * @throws NullPointerException If {@code lines} is {@code null}.
     */
    public GeoLineStringCollection(List<GeoLineString> lines) {
        this(lines, null, null);
    }

    /**
     * Constructs a {@link GeoLineStringCollection}.
     *
     * @param lines The geometric lines that define the multi-line.
     * @param boundingBox Bounding box for the multi-line.
     * @param customProperties Additional properties of the multi-line.
     * @throws NullPointerException If {@code lines} is {@code null}.
     */
    public GeoLineStringCollection(List<GeoLineString> lines, GeoBoundingBox boundingBox,
        Map<String, Object> customProperties) {
        super(boundingBox, customProperties);

        Objects.requireNonNull(lines, "'lines' cannot be null.");

        this.lines = Collections.unmodifiableList(new ArrayList<>(lines));
    }

    /**
     * Unmodifiable representation of the {@link GeoLineString geometric lines} representing this multi-line.
     *
     * @return An unmodifiable representation of the {@link GeoLineString geometric lines} representing this multi-line.
     */
    public List<GeoLineString> getLines() {
        return lines;
    }

    /**
     * Unmodifiable representation of the {@link GeoPosition geometric positions} representing this multi-line.
     *
     * @return An unmodifiable representation of the {@link GeoPosition geometric positions} representing this
     * multi-line.
     */
    GeoArray<GeoArray<GeoPosition>> getCoordinates() {
        return new GeoArray<>(this);
    }

    @Override
    public GeoObjectType getType() {
        return GeoObjectType.MULTI_LINE_STRING;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lines, super.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GeoLineStringCollection)) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        GeoLineStringCollection other = (GeoLineStringCollection) obj;

        return super.equals(obj) && Objects.equals(lines, other.lines);
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject()
            .writeStringField("type", GeoObjectType.MULTI_LINE_STRING.toString())
            .writeArrayField("coordinates", lines,
                (writer, geoLineString) -> writer.writeArray(geoLineString.getCoordinates(), JsonWriter::writeJson))
            .writeJsonField("bbox", getBoundingBox());

        return writeCustomProperties(jsonWriter).writeEndObject();
    }

    /**
     * Reads a JSON stream into a {@link GeoLineStringCollection}.
     *
     * @param jsonReader The {@link JsonReader} being read.
     * @return The {@link GeoLineStringCollection} that the JSON stream represented, or null if it pointed to JSON null.
     * @throws IllegalStateException If the {@code type} node exists and isn't equal to {@code MultiLineString}.
     * @throws IOException If a {@link GeoLineStringCollection} fails to be read from the {@code jsonReader}.
     */
    public static GeoLineStringCollection fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            List<GeoLineString> lines = null;
            GeoBoundingBox boundingBox = null;
            Map<String, Object> customProperties = null;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("type".equals(fieldName)) {
                    String type = reader.getString();
                    if (!GeoObjectType.MULTI_LINE_STRING.toString().equals(type)) {
                        throw new IllegalStateException("'type' was expected to be non-null and equal to "
                            + "'MultiLineString'. The found 'type' was '" + type + "'.");
                    }
                } else if ("coordinates".equals(fieldName)) {
                    List<List<GeoPosition>> positionList
                        = reader.readArray(reader2 -> reader2.readArray(GeoPosition::fromJson));
                    lines = new ArrayList<>(positionList.size());
                    for (List<GeoPosition> positions : positionList) {
                        lines.add(new GeoLineString(positions));
                    }
                } else if ("bbox".equals(fieldName)) {
                    boundingBox = GeoBoundingBox.fromJson(reader);
                } else {
                    if (customProperties == null) {
                        customProperties = new LinkedHashMap<>();
                    }

                    customProperties.put(fieldName, reader.readUntyped());
                }
            }

            return new GeoLineStringCollection(lines, boundingBox, customProperties);
        });
    }
}
