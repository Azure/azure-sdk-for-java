// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.models;

import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a geometric point.
 */
@Immutable
public final class GeoPoint extends GeoObject {
    private final GeoPosition coordinates;

    /**
     * Constructs a {@link GeoPoint}.
     *
     * @param longitude The longitudinal position of the point.
     * @param latitude The latitudinal position of the point.
     */
    public GeoPoint(double longitude, double latitude) {
        this(longitude, latitude, null);
    }

    /**
     * Constructs a {@link GeoPoint}.
     *
     * @param longitude The longitudinal position of the point.
     * @param latitude The latitudinal position of the point.
     * @param altitude The altitude of the point.
     */
    public GeoPoint(double longitude, double latitude, Double altitude) {
        this(new GeoPosition(longitude, latitude, altitude));
    }

    /**
     * Constructs a geometric point.
     *
     * @param position The {@link GeoPosition geometric position} of the point.
     * @throws NullPointerException If {@code position} is {@code null}.
     */
    public GeoPoint(GeoPosition position) {
        this(position, null, null);
    }

    /**
     * Constructs a geometric point.
     *
     * @param position The {@link GeoPosition geometric position} of the point.
     * @param boundingBox Bounding box for the point.
     * @param customProperties Additional properties of the geometric point.
     * @throws NullPointerException If {@code position} is {@code null}.
     */
    public GeoPoint(GeoPosition position, GeoBoundingBox boundingBox, Map<String, Object> customProperties) {
        super(boundingBox, customProperties);

        this.coordinates = Objects.requireNonNull(position, "'position' cannot be null.");
    }

    /**
     * The {@link GeoPosition geometric position} of the point.
     *
     * @return The {@link GeoPosition geometric position} of the point.
     */
    public GeoPosition getCoordinates() {
        return coordinates;
    }

    @Override
    public GeoObjectType getType() {
        return GeoObjectType.POINT;
    }

    @Override
    public int hashCode() {
        return Objects.hash(coordinates, super.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GeoPoint)) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        GeoPoint other = (GeoPoint) obj;

        return super.equals(obj) && Objects.equals(coordinates, other.coordinates);
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject()
            .writeStringField("type", GeoObjectType.POINT.toString())
            .writeJsonField("coordinates", coordinates)
            .writeJsonField("bbox", getBoundingBox());

        return writeCustomProperties(jsonWriter).writeEndObject();
    }

    /**
     * Reads a JSON stream into a {@link GeoPoint}.
     *
     * @param jsonReader The {@link JsonReader} being read.
     * @return The {@link GeoPoint} that the JSON stream represented, or null if it pointed to JSON null.
     * @throws IllegalStateException If the {@code type} node exists and isn't equal to {@code Point}.
     * @throws IOException If a {@link GeoPoint} fails to be read from the {@code jsonReader}.
     */
    public static GeoPoint fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            GeoPosition position = null;
            GeoBoundingBox boundingBox = null;
            Map<String, Object> customProperties = null;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("type".equals(fieldName)) {
                    String type = reader.getString();
                    if (!GeoObjectType.POINT.toString().equals(type)) {
                        throw new IllegalStateException("'type' was expected to be non-null and equal to 'Point'. The "
                            + "found 'type' was '" + type + "'.");
                    }
                } else if ("coordinates".equals(fieldName)) {
                    position = GeoPosition.fromJson(reader);
                } else if ("bbox".equals(fieldName)) {
                    boundingBox = GeoBoundingBox.fromJson(reader);
                } else {
                    if (customProperties == null) {
                        customProperties = new LinkedHashMap<>();
                    }

                    customProperties.put(fieldName, reader.readUntyped());
                }
            }

            return new GeoPoint(position, boundingBox, customProperties);
        });
    }
}
