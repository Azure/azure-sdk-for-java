// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.geojson;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Serializes a {@link GeoObject} into JSON.
 */
final class GeoJsonSerializer extends JsonSerializer<GeoObject> {
    private static final ClientLogger LOGGER = new ClientLogger(GeoJsonSerializer.class);

    static final SimpleModule MODULE;

    static {
        MODULE = new SimpleModule();
        MODULE.addSerializer(GeoObject.class, new GeoJsonSerializer());
    }

    @Override
    public void serialize(GeoObject value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        write(value, gen);
    }

    private static void write(GeoObject value, JsonGenerator gen) throws IOException {
        gen.writeStartObject();

        if (value instanceof GeoPoint) {
            writeType(GeoObjectType.POINT.getJsonType(), gen);
            gen.writeFieldName(GeoJsonDeserializer.COORDINATES_PROPERTY);
            writePosition(((GeoPoint) value).getCoordinates(), gen);
        } else if (value instanceof GeoLineString) {
            writeType(GeoObjectType.LINE_STRING.getJsonType(), gen);
            gen.writeFieldName(GeoJsonDeserializer.COORDINATES_PROPERTY);
            writePositions(((GeoLineString) value).getCoordinates(), gen);
        } else if (value instanceof GeoPolygon) {
            writeType(GeoObjectType.POLYGON.getJsonType(), gen);
            gen.writeArrayFieldStart(GeoJsonDeserializer.COORDINATES_PROPERTY);
            for (GeoLinearRing ring : ((GeoPolygon) value).getRings()) {
                writePositions(ring.getCoordinates(), gen);
            }
            gen.writeEndArray();
        } else if (value instanceof GeoPointCollection) {
            writeType(GeoObjectType.MULTI_POINT.getJsonType(), gen);
            gen.writeArrayFieldStart(GeoJsonDeserializer.COORDINATES_PROPERTY);
            for (GeoPoint point : ((GeoPointCollection) value).getPoints()) {
                writePosition(point.getCoordinates(), gen);
            }
            gen.writeEndArray();
        } else if (value instanceof GeoLineStringCollection) {
            writeType(GeoObjectType.MULTI_LINE_STRING.getJsonType(), gen);
            gen.writeArrayFieldStart(GeoJsonDeserializer.COORDINATES_PROPERTY);
            for (GeoLineString line : ((GeoLineStringCollection) value).getLines()) {
                writePositions(line.getCoordinates(), gen);
            }
            gen.writeEndArray();
        } else if (value instanceof GeoPolygonCollection) {
            writeType(GeoObjectType.MULTI_POLYGON.getJsonType(), gen);
            gen.writeArrayFieldStart(GeoJsonDeserializer.COORDINATES_PROPERTY);
            for (GeoPolygon polygon : ((GeoPolygonCollection) value).getPolygons()) {
                gen.writeStartArray();
                for (GeoLinearRing ring : polygon.getRings()) {
                    writePositions(ring.getCoordinates(), gen);
                }
                gen.writeEndArray();
            }
            gen.writeEndArray();
        } else if (value instanceof GeoCollection) {
            writeType(GeoObjectType.GEOMETRY_COLLECTION.getJsonType(), gen);
            gen.writeArrayFieldStart(GeoJsonDeserializer.GEOMETRIES_PROPERTY);
            for (GeoObject geoObject : ((GeoCollection) value).getGeometries()) {
                write(geoObject, gen);
            }
            gen.writeEndArray();
        } else {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                String.format("Geo type '%s' isn't supported.", value.getClass().getName())));
        }

        writeBoundingBox(value.getBoundingBox(), gen);
        writeAdditionalProperties(value.getCustomProperties(), gen);

        gen.writeEndObject();
    }

    private static void writePositions(List<GeoPosition> positions, JsonGenerator gen) throws IOException {
        gen.writeStartArray();

        for (GeoPosition position : positions) {
            writePosition(position, gen);
        }

        gen.writeEndArray();
    }

    private static void writePosition(GeoPosition position, JsonGenerator gen) throws IOException {
        gen.writeStartArray();

        gen.writeNumber(position.getLongitude());
        gen.writeNumber(position.getLatitude());

        Double altitude = position.getAltitude();
        if (altitude != null) {
            gen.writeNumber(altitude);
        }

        gen.writeEndArray();
    }

    private static void writeType(String type, JsonGenerator gen) throws IOException {
        gen.writeStringField(GeoJsonDeserializer.TYPE_PROPERTY, type);
    }

    private static void writeBoundingBox(GeoBoundingBox boundingBox, JsonGenerator gen)
        throws IOException {
        if (boundingBox == null) {
            return;
        }

        gen.writeArrayFieldStart(GeoJsonDeserializer.BOUNDING_BOX_PROPERTY);
        gen.writeNumber(boundingBox.getWest());
        gen.writeNumber(boundingBox.getSouth());

        Double minAltitude = boundingBox.getMinAltitude();
        if (minAltitude != null) {
            gen.writeNumber(minAltitude);
        }

        gen.writeNumber(boundingBox.getEast());
        gen.writeNumber(boundingBox.getNorth());

        Double maxAltitude = boundingBox.getMaxAltitude();
        if (maxAltitude != null) {
            gen.writeNumber(maxAltitude);
        }

        gen.writeEndArray();
    }

    private static void writeAdditionalProperties(Map<String, Object> properties, JsonGenerator gen)
        throws IOException {
        if (CoreUtils.isNullOrEmpty(properties)) {
            return;
        }

        for (Map.Entry<String, Object> property : properties.entrySet()) {
            gen.writeFieldName(property.getKey());
            gen.writeObject(property.getValue());
        }
    }
}
