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
 * <p>Represents a geometric polygon in GeoJSON format.</p>
 *
 * <p>This class encapsulates a polygon defined by a list of {@link GeoLinearRing} instances. Each ring represents a
 * closed loop of coordinates forming the boundary of the polygon.</p>
 *
 * <p>This class is useful when you want to work with a polygon in a geographic context. For example, you can use it
 * to represent a geographic area on a map.</p>
 *
 * <p>Note: A polygon requires at least one ring, and each ring requires at least 4 coordinates
 * (with the first and last coordinates being the same to form a closed loop).</p>
 *
 * @see GeoLinearRing
 * @see GeoPosition
 * @see GeoObject
 */
@Metadata(properties = MetadataProperties.IMMUTABLE)
public final class GeoPolygon extends GeoObject {
    private static final ClientLogger LOGGER = new ClientLogger(GeoPolygon.class);
    private final List<GeoLinearRing> rings;

    /**
     * Constructs a geometric polygon.
     *
     * @param ring The {@link GeoLinearRing ring} that defines the polygon.
     * @throws NullPointerException If {@code ring} is {@code null}.
     */
    public GeoPolygon(GeoLinearRing ring) {
        this(ring, null, null);
    }

    /**
     * Constructs a geometric polygon.
     *
     * @param ring The {@link GeoLinearRing ring} that defines the polygon.
     * @param boundingBox Bounding box for the polygon.
     * @param customProperties Additional properties of the polygon.
     * @throws NullPointerException If {@code ring} is {@code null}.
     */
    public GeoPolygon(GeoLinearRing ring, GeoBoundingBox boundingBox, Map<String, Object> customProperties) {
        this(Collections.singletonList(Objects.requireNonNull(ring, "'ring' cannot be null.")), boundingBox,
            customProperties);
    }

    /**
     * Constructs a geometric polygon.
     *
     * @param rings The {@link GeoLinearRing rings} that define the polygon.
     * @throws NullPointerException If {@code rings} is {@code null}.
     */
    public GeoPolygon(List<GeoLinearRing> rings) {
        this(rings, null, null);
    }

    /**
     * Constructs a geometric polygon.
     *
     * @param rings The {@link GeoLinearRing rings} that define the polygon.
     * @param boundingBox Bounding box for the polygon.
     * @param customProperties Additional properties of the polygon.
     * @throws NullPointerException If {@code rings} is {@code null}.
     */
    public GeoPolygon(List<GeoLinearRing> rings, GeoBoundingBox boundingBox, Map<String, Object> customProperties) {
        super(boundingBox, customProperties);

        Objects.requireNonNull(rings, "'rings' cannot be null.");
        this.rings = Collections.unmodifiableList(new ArrayList<>(rings));
    }

    /**
     * Unmodifiable representation of the {@link GeoLinearRing geometric rings} representing this polygon.
     *
     * @return An unmodifiable representation of the {@link GeoLinearRing geometric rings} representing this polygon.
     */
    public List<GeoLinearRing> getRings() {
        return rings;
    }

    /**
     * Gets the outer ring of the polygon.
     *
     * @return Outer ring of the polygon.
     */
    public GeoLinearRing getOuterRing() {
        return rings.get(0);
    }

    /**
     * Unmodifiable representation of the {@link GeoPosition geometric positions} representing this polygon.
     *
     * @return Unmodifiable representation of the {@link GeoPosition geometric positions} representing this polygon.
     */
    GeoArray<GeoArray<GeoPosition>> getCoordinates() {
        return new GeoArray<>(this);
    }

    @Override
    public GeoObjectType getType() {
        return GeoObjectType.POLYGON;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rings, super.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GeoPolygon)) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        GeoPolygon other = (GeoPolygon) obj;

        return super.equals(obj) && Objects.equals(rings, other.rings);
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject()
            .writeStringField("type", GeoObjectType.POLYGON.toString())
            .writeArrayField("coordinates", getRings(), JsonWriter::writeJson)
            .writeJsonField("bbox", getBoundingBox());

        return writeCustomProperties(jsonWriter).writeEndObject();
    }

    /**
     * Reads a JSON stream into a {@link GeoPolygon}.
     *
     * @param jsonReader The {@link JsonReader} being read.
     * @return The {@link GeoPolygon} that the JSON stream represented, or null if it pointed to JSON null.
     * @throws IllegalStateException If the {@code type} node exists and isn't equal to {@code Polygon}.
     * @throws IOException If a {@link GeoPolygon} fails to be read from the {@code jsonReader}.
     */
    public static GeoPolygon fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            List<GeoLinearRing> rings = null;
            GeoBoundingBox boundingBox = null;
            Map<String, Object> customProperties = null;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("type".equals(fieldName)) {
                    String type = reader.getString();
                    if (!GeoObjectType.POLYGON.toString().equals(type)) {
                        throw LOGGER.throwableAtError()
                            .addKeyValue("expectedType", "Polygon")
                            .addKeyValue("actualType", type)
                            .log("Deserialization failed.", IllegalStateException::new);
                    }
                } else if ("coordinates".equals(fieldName)) {
                    rings = reader.readArray(GeoLinearRing::fromJson);
                } else if ("bbox".equals(fieldName)) {
                    boundingBox = GeoBoundingBox.fromJson(reader);
                } else {
                    if (customProperties == null) {
                        customProperties = new LinkedHashMap<>();
                    }

                    customProperties.put(fieldName, reader.readUntyped());
                }
            }

            return new GeoPolygon(rings, boundingBox, customProperties);
        });
    }
}
