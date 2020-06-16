// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.serializer;

import com.azure.core.models.spatial.CollectionGeometry;
import com.azure.core.models.spatial.Geometry;
import com.azure.core.models.spatial.GeometryBoundingBox;
import com.azure.core.models.spatial.GeometryPosition;
import com.azure.core.models.spatial.GeometryProperties;
import com.azure.core.models.spatial.LineGeometry;
import com.azure.core.models.spatial.MultiLineGeometry;
import com.azure.core.models.spatial.MultiPointGeometry;
import com.azure.core.models.spatial.MultiPolygonGeometry;
import com.azure.core.models.spatial.PointGeometry;
import com.azure.core.models.spatial.PolygonGeometry;
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
 * Serializes a {@link Geometry} into JSON.
 */
final class GeometrySerializer extends JsonSerializer<Geometry> {
    private static final ClientLogger LOGGER = new ClientLogger(GeometrySerializer.class);

    static final SimpleModule MODULE;

    static {
        MODULE = new SimpleModule();
        MODULE.addSerializer(Geometry.class, new GeometrySerializer());
    }

    @Override
    public void serialize(Geometry value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        write(value, gen);
    }

    private static void write(Geometry value, JsonGenerator gen) throws IOException {
        gen.writeStartObject();

        if (value instanceof PointGeometry) {
            writeType(GeometryDeserializer.POINT_TYPE, gen);
            gen.writeFieldName(GeometryDeserializer.COORDINATES_PROPERTY);
            writePosition(((PointGeometry) value).getPosition(), gen);
        } else if (value instanceof LineGeometry) {
            writeType(GeometryDeserializer.LINE_STRING_TYPE, gen);
            gen.writeFieldName(GeometryDeserializer.COORDINATES_PROPERTY);
            writePositions(((LineGeometry) value).getPositions(), gen);
        } else if (value instanceof PolygonGeometry) {
            writeType(GeometryDeserializer.POLYGON_TYPE, gen);
            gen.writeArrayFieldStart(GeometryDeserializer.COORDINATES_PROPERTY);
            for (LineGeometry ring : ((PolygonGeometry) value).getRings()) {
                writePositions(ring.getPositions(), gen);
            }
            gen.writeEndArray();
        } else if (value instanceof MultiPointGeometry) {
            writeType(GeometryDeserializer.MULTI_POINT_TYPE, gen);
            gen.writeArrayFieldStart(GeometryDeserializer.COORDINATES_PROPERTY);
            for (PointGeometry point : ((MultiPointGeometry) value).getPoints()) {
                writePosition(point.getPosition(), gen);
            }
            gen.writeEndArray();
        } else if (value instanceof MultiLineGeometry) {
            writeType(GeometryDeserializer.MULTI_LINE_STRING_TYPE, gen);
            gen.writeArrayFieldStart(GeometryDeserializer.COORDINATES_PROPERTY);
            for (LineGeometry line : ((MultiLineGeometry) value).getLines()) {
                writePositions(line.getPositions(), gen);
            }
            gen.writeEndArray();
        } else if (value instanceof MultiPolygonGeometry) {
            writeType(GeometryDeserializer.MULTI_POLYGON_TYPE, gen);
            gen.writeArrayFieldStart(GeometryDeserializer.COORDINATES_PROPERTY);
            for (PolygonGeometry polygon : ((MultiPolygonGeometry) value).getPolygons()) {
                gen.writeStartArray();
                for (LineGeometry ring : polygon.getRings()) {
                    writePositions(ring.getPositions(), gen);
                }
                gen.writeEndArray();
            }
            gen.writeEndArray();
        } else if (value instanceof CollectionGeometry) {
            writeType(GeometryDeserializer.GEOMETRY_COLLECTION_TYPE, gen);
            gen.writeArrayFieldStart(GeometryDeserializer.GEOMETRIES_PROPERTY);
            for (Geometry geometry : ((CollectionGeometry) value).getGeometries()) {
                write(geometry, gen);
            }
            gen.writeEndArray();
        } else {
            throw LOGGER.logExceptionAsError(new IllegalStateException(
                String.format("Geometry type '%s' isn't supported.", value.getClass().getName())));
        }

        GeometryProperties geometryProperties = value.getProperties();
        writeBoundingBox(geometryProperties.getBoundingBox(), gen);
        writeAdditionalProperties(geometryProperties.getAdditionalProperties(), gen);

        gen.writeEndObject();
    }

    private static void writePositions(List<GeometryPosition> positions, JsonGenerator gen) throws IOException {
        gen.writeStartArray();

        for (GeometryPosition position : positions) {
            writePosition(position, gen);
        }

        gen.writeEndArray();
    }

    private static void writePosition(GeometryPosition position, JsonGenerator gen) throws IOException {
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
        gen.writeStringField(GeometryDeserializer.TYPE_PROPERTY, type);
    }

    private static void writeBoundingBox(GeometryBoundingBox boundingBox, JsonGenerator gen)
        throws IOException {
        if (boundingBox == null) {
            return;
        }

        gen.writeArrayFieldStart(GeometryDeserializer.BOUNDING_BOX_PROPERTY);
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
