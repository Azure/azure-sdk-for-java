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
 * Represents a collection of {@link GeoPoint GeoPoints}.
 */
@Immutable
public final class GeoPointCollection extends GeoObject {
    private final List<GeoPoint> points;

    /**
     * Constructs a {@link GeoPointCollection}.
     *
     * @param points The points that define the multi-point.
     * @throws NullPointerException If {@code points} is {@code null}.
     */
    public GeoPointCollection(List<GeoPoint> points) {
        this(points, null, null);
    }

    /**
     * Constructs a {@link GeoPointCollection}.
     *
     * @param points The points that define the multi-point.
     * @param boundingBox Bounding box for the multi-point.
     * @param customProperties Additional properties of the multi-point.
     * @throws NullPointerException If {@code points} is {@code null}.
     */
    public GeoPointCollection(List<GeoPoint> points, GeoBoundingBox boundingBox, Map<String, Object> customProperties) {
        super(boundingBox, customProperties);

        Objects.requireNonNull(points, "'points' cannot be null.");
        this.points = Collections.unmodifiableList(new ArrayList<>(points));
    }

    /**
     * Unmodifiable representation of the {@link GeoPoint geometric points} representing this multi-point.
     *
     * @return An unmodifiable representation of the {@link GeoPoint geometric points} representing this multi-point.
     */
    public List<GeoPoint> getPoints() {
        return points;
    }

    /**
     * Unmodifiable representation of the {@link GeoPosition geometric positions} representing this multi-point.
     *
     * @return An unmodifiable representation of the {@link GeoPosition geometric positions} representing this
     * multi-point.
     */
    GeoArray<GeoPosition> getCoordinates() {
        return new GeoArray<>(this);
    }

    @Override
    public GeoObjectType getType() {
        return GeoObjectType.MULTI_POINT;
    }

    @Override
    public int hashCode() {
        return Objects.hash(points, super.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GeoPointCollection)) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        GeoPointCollection other = (GeoPointCollection) obj;

        return super.equals(obj) && Objects.equals(points, other.points);
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject()
            .writeStringField("type", GeoObjectType.MULTI_POINT.toString())
            .writeArrayField("coordinates", points, (writer, geoPoint) -> geoPoint.getCoordinates().toJson(writer))
            .writeJsonField("bbox", getBoundingBox());

        return writeCustomProperties(jsonWriter).writeEndObject();
    }

    /**
     * Reads a JSON stream into a {@link GeoPointCollection}.
     *
     * @param jsonReader The {@link JsonReader} being read.
     * @return The {@link GeoPointCollection} that the JSON stream represented, or null if it pointed to JSON null.
     * @throws IllegalStateException If the {@code type} node exists and isn't equal to {@code MultiPoint}.
     * @throws IOException If a {@link GeoPointCollection} fails to be read from the {@code jsonReader}.
     */
    public static GeoPointCollection fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            List<GeoPoint> points = null;
            GeoBoundingBox boundingBox = null;
            Map<String, Object> customProperties = null;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("type".equals(fieldName)) {
                    String type = reader.getString();
                    if (!GeoObjectType.MULTI_POINT.toString().equals(type)) {
                        throw new IllegalStateException("'type' was expected to be non-null and equal to 'MultiPoint'. "
                            + "The found 'type' was '" + type + "'.");
                    }
                } else if ("coordinates".equals(fieldName)) {
                    List<GeoPosition> positions = reader.readArray(GeoPosition::fromJson);
                    points = new ArrayList<>(positions.size());
                    for (GeoPosition position : positions) {
                        points.add(new GeoPoint(position));
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

            return new GeoPointCollection(points, boundingBox, customProperties);
        });
    }
}
