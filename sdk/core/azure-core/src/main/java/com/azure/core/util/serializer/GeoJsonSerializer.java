// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.serializer;

import com.azure.core.implementation.GeoObjectHelper;
import com.azure.core.models.GeoBoundingBox;
import com.azure.core.models.GeoCollection;
import com.azure.core.models.GeoLineString;
import com.azure.core.models.GeoLineStringCollection;
import com.azure.core.models.GeoLinearRing;
import com.azure.core.models.GeoObject;
import com.azure.core.models.GeoObjectType;
import com.azure.core.models.GeoPoint;
import com.azure.core.models.GeoPointCollection;
import com.azure.core.models.GeoPolygon;
import com.azure.core.models.GeoPolygonCollection;
import com.azure.core.models.GeoPosition;
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

    private static final SimpleModule MODULE = new SimpleModule().addSerializer(GeoObject.class, new GeoJsonSerializer());


    /**
     * Gets a module wrapping this serializer as an adapter for the Jackson ObjectMapper.
     *
     * @return A {@link SimpleModule} to be plugged onto Jackson ObjectMapper.
     */
    public static SimpleModule getModule() {
        return MODULE;
    }

    @Override
    public void serialize(GeoObject value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        write(value, gen);
    }

    private static void write(GeoObject value, JsonGenerator gen) throws IOException {
        gen.writeStartObject();

        if (value instanceof GeoPoint) {
            writeType(GeoObjectType.POINT, gen);
            gen.writeFieldName(GeoJsonDeserializer.COORDINATES_PROPERTY);
            writePosition(((GeoPoint) value).getCoordinates(), gen);
        } else if (value instanceof GeoLineString) {
            writeType(GeoObjectType.LINE_STRING, gen);
            gen.writeFieldName(GeoJsonDeserializer.COORDINATES_PROPERTY);
            writePositions(((GeoLineString) value).getCoordinates(), gen);
        } else if (value instanceof GeoPolygon) {
            writeType(GeoObjectType.POLYGON, gen);
            gen.writeArrayFieldStart(GeoJsonDeserializer.COORDINATES_PROPERTY);
            for (GeoLinearRing ring : ((GeoPolygon) value).getRings()) {
                writePositions(ring.getCoordinates(), gen);
            }
            gen.writeEndArray();
        } else if (value instanceof GeoPointCollection) {
            writeType(GeoObjectType.MULTI_POINT, gen);
            gen.writeArrayFieldStart(GeoJsonDeserializer.COORDINATES_PROPERTY);
            for (GeoPoint point : ((GeoPointCollection) value).getPoints()) {
                writePosition(point.getCoordinates(), gen);
            }
            gen.writeEndArray();
        } else if (value instanceof GeoLineStringCollection) {
            writeType(GeoObjectType.MULTI_LINE_STRING, gen);
            gen.writeArrayFieldStart(GeoJsonDeserializer.COORDINATES_PROPERTY);
            for (GeoLineString line : ((GeoLineStringCollection) value).getLines()) {
                writePositions(line.getCoordinates(), gen);
            }
            gen.writeEndArray();
        } else if (value instanceof GeoPolygonCollection) {
            writeType(GeoObjectType.MULTI_POLYGON, gen);
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
            writeType(GeoObjectType.GEOMETRY_COLLECTION, gen);
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
        writeAdditionalProperties(GeoObjectHelper.getCustomProperties(value), gen);

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

    private static void writeType(GeoObjectType type, JsonGenerator gen) throws IOException {
        gen.writeStringField(GeoJsonDeserializer.TYPE_PROPERTY, type.toString());
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
