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
import com.azure.core.util.logging.ClientLogger;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Deserializes a JSON object into a {@link Geometry}.
 */
final class GeometryDeserializer extends JsonDeserializer<Geometry> {
    private static final ClientLogger LOGGER = new ClientLogger(GeometryDeserializer.class);

    /*
     * GeoJSON geometry types.
     */
    static final String POINT_TYPE = "Point";
    static final String LINE_STRING_TYPE = "LineString";
    static final String MULTI_POINT_TYPE = "MultiPoint";
    static final String POLYGON_TYPE = "Polygon";
    static final String MULTI_LINE_STRING_TYPE = "MultiLineString";
    static final String MULTI_POLYGON_TYPE = "MultiPolygon";
    static final String GEOMETRY_COLLECTION_TYPE = "GeometryCollection";

    /*
     * Required GeoJSON properties.
     */
    static final String TYPE_PROPERTY = "type";
    static final String GEOMETRIES_PROPERTY = "geometries";
    static final String COORDINATES_PROPERTY = "coordinates";

    /*
     * Optional GeoJSON properties.
     */
    static final String BOUNDING_BOX_PROPERTY = "bbox";

    static final SimpleModule MODULE;

    static {
        MODULE = new SimpleModule();
        MODULE.addDeserializer(Geometry.class, new GeometryDeserializer());
    }

    @Override
    public Geometry deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return read(ctxt.readTree(p));
    }

    private static Geometry read(JsonNode node) {
        String type = getRequiredProperty(node, TYPE_PROPERTY).asText();

        if (GEOMETRY_COLLECTION_TYPE.equalsIgnoreCase(type)) {
            List<Geometry> geometries = new ArrayList<>();
            getRequiredProperty(node, GEOMETRIES_PROPERTY)
                .iterator()
                .forEachRemaining(geometryNode -> geometries.add(read(geometryNode)));

            return new CollectionGeometry(geometries, readProperties(node, GEOMETRIES_PROPERTY));
        }

        JsonNode coordinates = getRequiredProperty(node, COORDINATES_PROPERTY);
        GeometryProperties properties = readProperties(node);

        switch (type) {
            case POINT_TYPE:
                return new PointGeometry(readCoordinate(coordinates), properties);
            case LINE_STRING_TYPE:
                return new LineGeometry(readCoordinates(coordinates), properties);
            case MULTI_POINT_TYPE:
                List<PointGeometry> points = new ArrayList<>();
                readCoordinates(coordinates).forEach(position -> points.add(new PointGeometry(position)));

                return new MultiPointGeometry(points, properties);
            case POLYGON_TYPE:
                List<LineGeometry> rings = new ArrayList<>();
                node.iterator().forEachRemaining(ring -> rings.add(new LineGeometry(readCoordinates(ring))));

                return new PolygonGeometry(rings, properties);
            case MULTI_LINE_STRING_TYPE:
                List<LineGeometry> lines = new ArrayList<>();
                node.iterator().forEachRemaining(line -> lines.add(new LineGeometry(readCoordinates(line))));

                return new MultiLineGeometry(lines, properties);
            case MULTI_POLYGON_TYPE:
                return readMultiPolygon(node, properties);
            default:
                throw LOGGER.logExceptionAsError(new IllegalStateException(
                    String.format("Unsupported geometry type %s.", type)));
        }
    }

    private static MultiPolygonGeometry readMultiPolygon(JsonNode node, GeometryProperties properties) {
        List<PolygonGeometry> polygons = new ArrayList<>();
        for (JsonNode polygon : node) {
            List<LineGeometry> rings = new ArrayList<>();
            polygon.iterator().forEachRemaining(ring -> rings.add(new LineGeometry(readCoordinates(ring))));

            polygons.add(new PolygonGeometry(rings));
        }

        return new MultiPolygonGeometry(polygons, properties);
    }

    /*
     * Attempts to retrieve a required property node value.
     *
     * @param node Parent JsonNode.
     * @param name Property being retrieved.
     * @return The JsonNode of the required property.
     */
    private static JsonNode getRequiredProperty(JsonNode node, String name) {
        JsonNode requiredNode = node.get(name);

        if (requiredNode == null) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(
                String.format("GeoJSON object expected to have '%s' property.", name)));
        }

        return requiredNode;
    }

    private static GeometryProperties readProperties(JsonNode node) {
        return readProperties(node, COORDINATES_PROPERTY);
    }

    private static GeometryProperties readProperties(JsonNode node, String knownProperty) {
        GeometryBoundingBox boundingBox = null;

        JsonNode boundingBoxNode = node.get(BOUNDING_BOX_PROPERTY);
        if (boundingBoxNode != null) {
            switch (boundingBoxNode.size()) {
                case 4:
                    boundingBox = new GeometryBoundingBox(boundingBoxNode.get(0).asDouble(),
                        boundingBoxNode.get(1).asDouble(), boundingBoxNode.get(2).asDouble(),
                        boundingBoxNode.get(3).asDouble());
                    break;
                case 6:
                    boundingBox = new GeometryBoundingBox(boundingBoxNode.get(0).asDouble(),
                        boundingBoxNode.get(1).asDouble(), boundingBoxNode.get(3).asDouble(),
                        boundingBoxNode.get(4).asDouble(), boundingBoxNode.get(2).asDouble(),
                        boundingBoxNode.get(5).asDouble());
                    break;
                default:
                    throw LOGGER.logExceptionAsError(
                        new IllegalStateException("Only 2 or 3 dimension bounding boxes are supported."));
            }
        }

        Map<String, Object> additionalProperties = null;
        for (Iterator<Map.Entry<String, JsonNode>> it = node.fields(); it.hasNext(); ) {
            Map.Entry<String, JsonNode> field = it.next();
            String propertyName = field.getKey();
            if (propertyName.equalsIgnoreCase(TYPE_PROPERTY)
                || propertyName.equalsIgnoreCase(BOUNDING_BOX_PROPERTY)
                || propertyName.equalsIgnoreCase(knownProperty)) {
                continue;
            }

            if (additionalProperties == null) {
                additionalProperties = new HashMap<>();
            }

            additionalProperties.put(propertyName, readAdditionalPropertyValue(field.getValue()));
        }

        if (boundingBox != null || additionalProperties != null) {
            return new GeometryProperties(boundingBox, additionalProperties);
        }

        return GeometryProperties.DEFAULT_PROPERTIES;
    }

    private static Object readAdditionalPropertyValue(JsonNode node) {
        switch (node.getNodeType()) {
            case STRING:
                return node.asText();
            case NUMBER:
                if (node.isInt()) {
                    return node.asInt();
                } else if (node.isLong()) {
                    return node.asLong();
                } else if (node.isFloat()) {
                    return node.floatValue();
                } else {
                    return node.asDouble();
                }
            case BOOLEAN:
                return node.asBoolean();
            case NULL:
            case MISSING:
                return null;
            case OBJECT:
                Map<String, Object> map = new HashMap<>();
                node.fields()
                    .forEachRemaining(field -> map.put(field.getKey(), readAdditionalPropertyValue(field.getValue())));
                return map;
            case ARRAY:
                List<Object> list = new ArrayList<>(node.size());
                node.iterator().forEachRemaining(element -> list.add(readAdditionalPropertyValue(element)));
                return list;
            default:
                throw LOGGER.logExceptionAsError(new IllegalStateException(
                    String.format("Unsupported additional property type %s.", node.getNodeType())));
        }
    }

    private static List<GeometryPosition> readCoordinates(JsonNode coordinates) {
        List<GeometryPosition> positions = new ArrayList<>();

        coordinates.iterator().forEachRemaining(coordinate -> positions.add(readCoordinate(coordinate)));

        return positions;
    }

    private static GeometryPosition readCoordinate(JsonNode coordinate) {
        int coordinateCount = coordinate.size();

        if (coordinateCount < 2 || coordinateCount > 3) {
            throw LOGGER.logExceptionAsError(new IllegalStateException("Only 2 or 3 element coordinates supported."));
        }

        double longitude = coordinate.get(0).asDouble();
        double latitude = coordinate.get(1).asDouble();
        Double altitude = null;

        if (coordinateCount > 2) {
            altitude = coordinate.get(2).asDouble();
        }

        return new GeometryPosition(longitude, latitude, altitude);
    }
}
