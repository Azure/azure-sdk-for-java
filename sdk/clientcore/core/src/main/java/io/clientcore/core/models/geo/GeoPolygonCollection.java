// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.models.geo;

import io.clientcore.core.annotations.Metadata;
import io.clientcore.core.annotations.MetadataProperties;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.serialization.json.JsonReader;
import io.clientcore.core.serialization.json.JsonToken;
import io.clientcore.core.serialization.json.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <p>Represents a collection of {@link GeoPolygon GeoPolygons} in GeoJSON format.</p>
 *
 * <p>This class encapsulates a list of {@link GeoPolygon} instances that form a collection of polygons. Each polygon
 * is defined by a list of {@link GeoLinearRing} instances that form the boundary of the polygon.</p>
 *
 * <p>This class is useful when you want to work with a collection of polygons in a geographic context. For example,
 * you can use it to represent a complex geographic area on a map that is composed of multiple polygons.</p>
 *
 * <p>Note: A polygon collection requires at least one ring for each polygon, and each ring requires at least
 * 4 coordinates (with the first and last coordinates being the same to form a closed loop).</p>
 *
 * @see GeoPolygon
 * @see GeoLinearRing
 * @see GeoPosition
 * @see GeoObject
 */
@Metadata(properties = MetadataProperties.IMMUTABLE)
public final class GeoPolygonCollection extends GeoObject {
    private static final ClientLogger LOGGER = new ClientLogger(GeoPolygonCollection.class);
    private final List<GeoPolygon> polygons;

    /**
     * Constructs a {@link GeoPolygonCollection}.
     *
     * @param polygons The polygons that define the multi-polygon.
     * @throws NullPointerException If {@code polygons} is {@code null}.
     */
    public GeoPolygonCollection(List<GeoPolygon> polygons) {
        this(polygons, null, null);
    }

    /**
     * Constructs a {@link GeoPolygonCollection}.
     *
     * @param polygons The polygons that define the multi-polygon.
     * @param boundingBox Bounding box for the multi-polygon.
     * @param customProperties Additional properties of the multi-polygon.
     * @throws NullPointerException If {@code polygons} is {@code null}.
     */
    public GeoPolygonCollection(List<GeoPolygon> polygons, GeoBoundingBox boundingBox,
        Map<String, Object> customProperties) {
        super(boundingBox, customProperties);

        Objects.requireNonNull(polygons, "'polygons' cannot be null.");
        this.polygons = Collections.unmodifiableList(new ArrayList<>(polygons));
    }

    /**
     * Unmodifiable representation of the {@link GeoPolygon geometric polygons} representing this multi-polygon.
     *
     * @return An unmodifiable representation of the {@link GeoPolygon geometric polygons} representing this
     * multi-polygon.
     */
    public List<GeoPolygon> getPolygons() {
        return polygons;
    }

    /**
     * Unmodifiable representation of the {@link GeoPosition geometric positions} representing this multi-polygon.
     *
     * @return An unmodifiable representation of the {@link GeoPosition geometric positions} representing this
     * multi-polygon.
     */
    GeoArray<GeoArray<GeoArray<GeoPosition>>> getCoordinates() {
        return new GeoArray<>(this);
    }

    @Override
    public GeoObjectType getType() {
        return GeoObjectType.MULTI_POLYGON;
    }

    @Override
    public int hashCode() {
        return Objects.hash(polygons, super.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GeoPolygonCollection)) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        GeoPolygonCollection other = (GeoPolygonCollection) obj;

        return super.equals(obj) && Objects.equals(polygons, other.polygons);
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject()
            .writeStringField("type", GeoObjectType.MULTI_POLYGON.toString())
            .writeArrayField("coordinates", polygons,
                (writer, geoPolygon) -> writer.writeArray(geoPolygon.getRings(), JsonWriter::writeJson))
            .writeJsonField("bbox", getBoundingBox());

        return writeCustomProperties(jsonWriter).writeEndObject();
    }

    /**
     * Reads a JSON stream into a {@link GeoPolygonCollection}.
     *
     * @param jsonReader The {@link JsonReader} being read.
     * @return The {@link GeoPolygonCollection} that the JSON stream represented, or null if it pointed to JSON null.
     * @throws IllegalStateException If the {@code type} node exists and isn't equal to {@code MultiPolygon}.
     * @throws IOException If a {@link GeoPolygonCollection} fails to be read from the {@code jsonReader}.
     */
    public static GeoPolygonCollection fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            List<GeoPolygon> polygons = null;
            GeoBoundingBox boundingBox = null;
            Map<String, Object> customProperties = null;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("type".equals(fieldName)) {
                    String type = reader.getString();
                    if (!GeoObjectType.MULTI_POLYGON.toString().equals(type)) {
                        throw LOGGER.throwableAtError()
                            .addKeyValue("expectedType", "MultiPolygon")
                            .addKeyValue("actualType", type)
                            .log("Deserialization failed.", IllegalStateException::new);
                    }
                } else if ("coordinates".equals(fieldName)) {
                    List<List<GeoLinearRing>> polygonRings
                        = reader.readArray(polygon -> polygon.readArray(GeoLinearRing::fromJson));
                    polygons = new ArrayList<>(polygonRings.size());
                    for (List<GeoLinearRing> rings : polygonRings) {
                        polygons.add(new GeoPolygon(rings));
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

            return new GeoPolygonCollection(polygons, boundingBox, customProperties);
        });
    }
}
