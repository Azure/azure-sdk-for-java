// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import com.azure.core.v2.implementation.GeoObjectHelper;
import com.azure.core.v2.models.GeoBoundingBox;
import com.azure.core.v2.models.GeoCollection;
import com.azure.core.v2.models.GeoLineString;
import com.azure.core.v2.models.GeoLineStringCollection;
import com.azure.core.v2.models.GeoLinearRing;
import com.azure.core.v2.models.GeoObject;
import com.azure.core.v2.models.GeoObjectType;
import com.azure.core.v2.models.GeoPoint;
import com.azure.core.v2.models.GeoPointCollection;
import com.azure.core.v2.models.GeoPolygon;
import com.azure.core.v2.models.GeoPolygonCollection;
import com.azure.core.v2.models.GeoPosition;
import com.azure.core.v2.util.CoreUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Test helpers for {@link GeoJsonDeserializerTests} and {@link GeoJsonSerializerTests}.
 */
public class GeoSerializationTestHelpers {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    static String geoToJson(GeoObject geoObject) {
        if (geoObject instanceof GeoPoint) {
            return pointToJson((GeoPoint) geoObject);
        } else if (geoObject instanceof GeoLineString) {
            return lineToJson((GeoLineString) geoObject);
        } else if (geoObject instanceof GeoPolygon) {
            return polygonToJson((GeoPolygon) geoObject);
        } else if (geoObject instanceof GeoPointCollection) {
            return multiPointToJson((GeoPointCollection) geoObject);
        } else if (geoObject instanceof GeoLineStringCollection) {
            return multiLineToJson((GeoLineStringCollection) geoObject);
        } else if (geoObject instanceof GeoPolygonCollection) {
            return multiPolygonToJson((GeoPolygonCollection) geoObject);
        } else if (geoObject instanceof GeoCollection) {
            return collectionToJson((GeoCollection) geoObject);
        } else {
            throw new IllegalStateException("Unknown geo type.");
        }
    }

    private static String pointToJson(GeoPoint point) {
        StringBuilder builder = new StringBuilder("{");
        addType(GeoObjectType.POINT, builder);

        builder.append(",\"coordinates\":");
        addPosition(point.getCoordinates(), builder);

        addAdditionalProperties(point, builder);

        builder.append("}");

        return builder.toString();
    }

    private static String lineToJson(GeoLineString line) {
        StringBuilder builder = new StringBuilder("{");
        addType(GeoObjectType.LINE_STRING, builder);

        builder.append(",\"coordinates\":");
        addLine(line.getCoordinates(), builder);

        addAdditionalProperties(line, builder);

        builder.append("}");

        return builder.toString();
    }

    private static String polygonToJson(GeoPolygon polygon) {
        StringBuilder builder = new StringBuilder("{");
        addType(GeoObjectType.POLYGON, builder);

        builder.append(",\"coordinates\":");
        addPolygon(polygon.getRings(), builder);

        addAdditionalProperties(polygon, builder);

        builder.append("}");

        return builder.toString();
    }

    private static String multiPointToJson(GeoPointCollection multiPoint) {
        StringBuilder builder = new StringBuilder("{");
        addType(GeoObjectType.MULTI_POINT, builder);

        builder.append(",\"coordinates\":");
        addLine(multiPoint.getPoints().stream().map(GeoPoint::getCoordinates).collect(Collectors.toList()), builder);

        addAdditionalProperties(multiPoint, builder);

        builder.append("}");

        return builder.toString();

    }

    private static String multiLineToJson(GeoLineStringCollection multiLine) {
        StringBuilder builder = new StringBuilder("{");
        addType(GeoObjectType.MULTI_LINE_STRING, builder);

        builder.append(",\"coordinates\":[");
        for (int i = 0; i < multiLine.getLines().size(); i++) {
            if (i > 0) {
                builder.append(",");
            }

            addLine(multiLine.getLines().get(i).getCoordinates(), builder);
        }

        builder.append("]");

        addAdditionalProperties(multiLine, builder);

        builder.append("}");

        return builder.toString();
    }

    private static String multiPolygonToJson(GeoPolygonCollection multiPolygon) {
        StringBuilder builder = new StringBuilder("{");
        addType(GeoObjectType.MULTI_POLYGON, builder);

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

    private static String collectionToJson(GeoCollection collection) {
        StringBuilder builder = new StringBuilder("{");
        addType(GeoObjectType.GEOMETRY_COLLECTION, builder);

        builder.append(",\"geometries\":[");

        for (int i = 0; i < collection.getGeometries().size(); i++) {
            if (i > 0) {
                builder.append(",");
            }

            builder.append(geoToJson(collection.getGeometries().get(i)));
        }

        builder.append("]");

        addAdditionalProperties(collection, builder);

        builder.append("}");

        return builder.toString();
    }

    private static void addType(GeoObjectType type, StringBuilder builder) {
        builder.append("\"type\":\"").append(type.toString()).append("\"");
    }

    private static void addPosition(GeoPosition position, StringBuilder builder) {
        builder.append("[").append(position.getLongitude()).append(",").append(position.getLatitude());

        Double altitude = position.getAltitude();
        if (altitude != null) {
            builder.append(",").append(altitude);
        }

        builder.append("]");
    }

    private static void addLine(List<GeoPosition> positions, StringBuilder builder) {
        builder.append("[");

        for (int i = 0; i < positions.size(); i++) {
            if (i > 0) {
                builder.append(",");
            }

            addPosition(positions.get(i), builder);
        }

        builder.append("]");
    }

    private static void addPolygon(List<GeoLinearRing> rings, StringBuilder builder) {
        builder.append("[");

        for (int i = 0; i < rings.size(); i++) {
            if (i > 0) {
                builder.append(",");
            }

            addLine(rings.get(i).getCoordinates(), builder);
        }

        builder.append("]");
    }

    private static void addAdditionalProperties(GeoObject geoObject, StringBuilder builder) {
        addBoundingBox(geoObject.getBoundingBox(), builder);
        addProperties(GeoObjectHelper.getCustomProperties(geoObject), builder);
    }

    private static void addBoundingBox(GeoBoundingBox boundingBox, StringBuilder builder) {
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

        builder.append(boundingBox.getEast()).append(",").append(boundingBox.getNorth());

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
