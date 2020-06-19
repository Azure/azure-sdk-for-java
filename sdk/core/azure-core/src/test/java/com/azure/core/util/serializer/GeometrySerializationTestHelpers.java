// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.serializer;

import com.azure.core.models.spatial.CollectionGeometry;
import com.azure.core.models.spatial.Geometry;
import com.azure.core.models.spatial.GeometryBoundingBox;
import com.azure.core.models.spatial.GeometryPosition;
import com.azure.core.models.spatial.LineGeometry;
import com.azure.core.models.spatial.MultiLineGeometry;
import com.azure.core.models.spatial.MultiPointGeometry;
import com.azure.core.models.spatial.MultiPolygonGeometry;
import com.azure.core.models.spatial.PointGeometry;
import com.azure.core.models.spatial.PolygonGeometry;
import com.azure.core.util.CoreUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Test helpers for {@link GeometryDeserializerTests} and {@link GeometrySerializerTests}.
 */
public class GeometrySerializationTestHelpers {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    static String pointToJson(PointGeometry point) {
        StringBuilder builder = new StringBuilder("{");
        addType(GeometryDeserializer.POINT_TYPE, builder);

        builder.append(",\"coordinates\":");
        addPosition(point.getPosition(), builder);

        addAdditionalProperties(point, builder);

        builder.append("}");

        return builder.toString();
    }

    static String lineToJson(LineGeometry line) {
        StringBuilder builder = new StringBuilder("{");
        addType(GeometryDeserializer.LINE_STRING_TYPE, builder);

        builder.append(",\"coordinates\":");
        addLine(line.getPositions(), builder);

        addAdditionalProperties(line, builder);

        builder.append("}");

        return builder.toString();
    }

    static String polygonToJson(PolygonGeometry polygon) {
        StringBuilder builder = new StringBuilder("{");
        addType(GeometryDeserializer.POLYGON_TYPE, builder);

        builder.append(",\"coordinates\":");
        addPolygon(polygon.getRings(), builder);

        addAdditionalProperties(polygon, builder);

        builder.append("}");

        return builder.toString();
    }

    static String multiPointToJson(MultiPointGeometry multiPoint) {
        StringBuilder builder = new StringBuilder("{");
        addType(GeometryDeserializer.MULTI_POINT_TYPE, builder);

        builder.append(",\"coordinates\":");
        addLine(multiPoint.getPoints().stream().map(PointGeometry::getPosition).collect(Collectors.toList()), builder);

        addAdditionalProperties(multiPoint, builder);

        builder.append("}");

        return builder.toString();

    }

    static String multiLineToJson(MultiLineGeometry multiLine) {
        StringBuilder builder = new StringBuilder("{");
        addType(GeometryDeserializer.MULTI_LINE_STRING_TYPE, builder);

        builder.append(",\"coordinates\":");
        addPolygon(multiLine.getLines(), builder);

        addAdditionalProperties(multiLine, builder);

        builder.append("}");

        return builder.toString();
    }

    static String multiPolygonToJson(MultiPolygonGeometry multiPolygon) {
        StringBuilder builder = new StringBuilder("{");
        addType(GeometryDeserializer.MULTI_POLYGON_TYPE, builder);

        builder.append(",\"coordinates\":[");

        for (int i = 0; i < multiPolygon.getPolygons().size(); i++) {
            if (i > 0) {
                builder.append(",");
            }

            addPolygon(multiPolygon.getPolygons().get(i).getRings(), builder);
        }

        builder.append("]");

        addAdditionalProperties(multiPolygon, builder);

        builder.append("}");

        return builder.toString();
    }

    static String collectionToJson(CollectionGeometry collection) {
        StringBuilder builder = new StringBuilder("{");
        addType(GeometryDeserializer.GEOMETRY_COLLECTION_TYPE, builder);

        builder.append(",\"geometries\":[");

        for (int i = 0; i < collection.getGeometries().size(); i++) {
            if (i > 0) {
                builder.append(",");
            }

            Geometry geometry = collection.getGeometries().get(i);
            if (geometry instanceof PointGeometry) {
                builder.append(pointToJson((PointGeometry) geometry));
            } else if (geometry instanceof LineGeometry) {
                builder.append(lineToJson((LineGeometry) geometry));
            } else if (geometry instanceof PolygonGeometry) {
                builder.append(polygonToJson((PolygonGeometry) geometry));
            } else if (geometry instanceof MultiPointGeometry) {
                builder.append(multiPointToJson((MultiPointGeometry) geometry));
            } else if (geometry instanceof MultiLineGeometry) {
                builder.append(multiLineToJson((MultiLineGeometry) geometry));
            } else if (geometry instanceof MultiPolygonGeometry) {
                builder.append(multiPolygonToJson((MultiPolygonGeometry) geometry));
            } else if (geometry instanceof CollectionGeometry) {
                builder.append(collectionToJson((CollectionGeometry) geometry));
            } else {
                throw new IllegalStateException("Unknown geometry type.");
            }
        }

        builder.append("]");

        addAdditionalProperties(collection, builder);

        builder.append("}");

        return builder.toString();
    }

    private static void addType(String type, StringBuilder builder) {
        builder.append("\"type\":\"").append(type).append("\"");
    }

    private static void addPosition(GeometryPosition position, StringBuilder builder) {
        builder.append("[")
            .append(position.getLongitude())
            .append(",")
            .append(position.getLatitude());

        Double altitude = position.getAltitude();
        if (altitude != null) {
            builder.append(",").append(altitude);
        }

        builder.append("]");
    }

    private static void addLine(List<GeometryPosition> positions, StringBuilder builder) {
        builder.append("[");

        for (int i = 0; i < positions.size(); i++) {
            if (i > 0) {
                builder.append(",");
            }

            addPosition(positions.get(i), builder);
        }

        builder.append("]");
    }

    private static void addPolygon(List<LineGeometry> lines, StringBuilder builder) {
        builder.append("[");

        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) {
                builder.append(",");
            }

            addLine(lines.get(i).getPositions(), builder);
        }

        builder.append("]");
    }

    private static void addAdditionalProperties(Geometry geometry, StringBuilder builder) {
        addBoundingBox(geometry.getBoundingBox(), builder);
        addProperties(geometry.getProperties(), builder);
    }

    private static void addBoundingBox(GeometryBoundingBox boundingBox, StringBuilder builder) {
        if (boundingBox == null) {
            return;
        }

        builder.append(",\"bbox\":[")
            .append(boundingBox.getWest())
            .append(",")
            .append(boundingBox.getSouth())
            .append(",");

        Double minAltitude = boundingBox.getMinAltitude();
        if (minAltitude != null) {
            builder.append(minAltitude).append(",");
        }

        builder.append(boundingBox.getEast())
            .append(",")
            .append(boundingBox.getNorth());

        Double maxAltitude = boundingBox.getMaxAltitude();
        if (maxAltitude != null) {
            builder.append(",").append(maxAltitude);
        }

        builder.append("]");
    }

    private static void addProperties(Map<String, Object> properties, StringBuilder builder) {
        if (CoreUtils.isNullOrEmpty(properties)) {
            return;
        }

        try {
            for (Map.Entry<String, Object> property : properties.entrySet()) {
                if (builder.length() > 0) {
                    builder.append(",");
                }

                builder.append("\"")
                    .append(property.getKey())
                    .append("\":")
                    .append(MAPPER.writeValueAsString(property.getValue()));
            }
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
    }
}
