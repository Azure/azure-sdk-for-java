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
 * Represents a heterogeneous collection of {@link GeoObject GeoObjects}.
 */
@Immutable
public final class GeoCollection extends GeoObject {
    private final List<GeoObject> geometries;

    /**
     * Constructs a {@link GeoCollection}.
     *
     * @param geometries The geometries in the collection.
     * @throws NullPointerException If {@code geometries} is {@code null}.
     */
    public GeoCollection(List<GeoObject> geometries) {
        this(geometries, null, null);
    }

    /**
     * Constructs a {@link GeoCollection}.
     *
     * @param geometries The geometries in the collection.
     * @param boundingBox Bounding box for the {@link GeoCollection}.
     * @param customProperties Additional properties of the {@link GeoCollection}.
     * @throws NullPointerException If {@code geometries} is {@code null}.
     */
    public GeoCollection(List<GeoObject> geometries, GeoBoundingBox boundingBox, Map<String, Object> customProperties) {
        super(boundingBox, customProperties);

        Objects.requireNonNull(geometries, "'geometries' cannot be null.");
        this.geometries = Collections.unmodifiableList(new ArrayList<>(geometries));
    }

    /**
     * Unmodifiable representation of the {@link GeoObject geometries} contained in this collection.
     *
     * @return An unmodifiable representation of the {@link GeoObject geometries} in this collection.
     */
    public List<GeoObject> getGeometries() {
        return geometries;
    }

    @Override
    public GeoObjectType getType() {
        return GeoObjectType.GEOMETRY_COLLECTION;
    }

    @Override
    public int hashCode() {
        return Objects.hash(geometries, super.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GeoCollection)) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        GeoCollection other = (GeoCollection) obj;
        return super.equals(other) && Objects.equals(geometries, other.geometries);
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject()
            .writeStringField("type", GeoObjectType.GEOMETRY_COLLECTION.toString())
            .writeArrayField("geometries", geometries, JsonWriter::writeJson)
            .writeJsonField("bbox", getBoundingBox());

        return writeCustomProperties(jsonWriter).writeEndObject();
    }

    /**
     * Reads a JSON stream into a {@link GeoCollection}.
     *
     * @param jsonReader The {@link JsonReader} being read.
     * @return The {@link GeoCollection} that the JSON stream represented, or null if it pointed to JSON null.
     * @throws IllegalStateException If the {@code type} node exists and isn't equal to {@code GeometryCollection}.
     * @throws IOException If a {@link GeoCollection} fails to be read from the {@code jsonReader}.
     */
    public static GeoCollection fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            List<GeoObject> geometries = null;
            GeoBoundingBox boundingBox = null;
            Map<String, Object> customProperties = null;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("type".equals(fieldName)) {
                    String type = reader.getString();
                    if (!GeoObjectType.GEOMETRY_COLLECTION.toString().equals(type)) {
                        throw new IllegalStateException("'type' was expected to be non-null and equal to "
                            + "'GeometryCollection'. The found 'type' was '" + type + "'.");
                    }
                } else if ("geometries".equals(fieldName)) {
                    geometries = reader.readArray(GeoObject::fromJson);
                } else if ("bbox".equals(fieldName)) {
                    boundingBox = GeoBoundingBox.fromJson(reader);
                } else {
                    if (customProperties == null) {
                        customProperties = new LinkedHashMap<>();
                    }

                    customProperties.put(fieldName, reader.readUntyped());
                }
            }

            return new GeoCollection(geometries, boundingBox, customProperties);
        });
    }
}
