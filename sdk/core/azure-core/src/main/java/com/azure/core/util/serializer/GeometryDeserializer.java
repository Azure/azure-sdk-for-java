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
import com.azure.core.util.logging.ClientLogger;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.UntypedObjectDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.TreeTraversingParser;

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
        MODULE = new SimpleModule()
            .addDeserializer(Geometry.class, new GeometryDeserializer())
            .addDeserializer(PointGeometry.class, geometrySubclassDeserializer(PointGeometry.class))
            .addDeserializer(LineGeometry.class, geometrySubclassDeserializer(LineGeometry.class))
            .addDeserializer(PolygonGeometry.class, geometrySubclassDeserializer(PolygonGeometry.class))
            .addDeserializer(MultiPointGeometry.class, geometrySubclassDeserializer(MultiPointGeometry.class))
            .addDeserializer(MultiLineGeometry.class, geometrySubclassDeserializer(MultiLineGeometry.class))
            .addDeserializer(MultiPolygonGeometry.class, geometrySubclassDeserializer(MultiPolygonGeometry.class))
            .addDeserializer(CollectionGeometry.class, geometrySubclassDeserializer(CollectionGeometry.class));
    }

    @Override
    public Geometry deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return read(ctxt.readTree(p), ctxt);
    }

    private static Geometry read(JsonNode node, DeserializationContext ctxt) throws IOException {
        String type = getRequiredProperty(node, TYPE_PROPERTY).asText();

        if (GEOMETRY_COLLECTION_TYPE.equalsIgnoreCase(type)) {
            List<Geometry> geometries = new ArrayList<>();
            for (JsonNode geometryNode : getRequiredProperty(node, GEOMETRIES_PROPERTY)) {
                geometries.add(read(geometryNode, ctxt));
            }

            return new CollectionGeometry(geometries, readBoundingBox(node),
                readProperties(node, GEOMETRIES_PROPERTY, ctxt));
        }

        JsonNode coordinates = getRequiredProperty(node, COORDINATES_PROPERTY);

        GeometryBoundingBox boundingBox = readBoundingBox(node);
        Map<String, Object> properties = readProperties(node, ctxt);

        switch (type) {
            case POINT_TYPE:
                return new PointGeometry(readCoordinate(coordinates), boundingBox, properties);
            case LINE_STRING_TYPE:
                return new LineGeometry(readCoordinates(coordinates), boundingBox, properties);
            case POLYGON_TYPE:
                List<LineGeometry> rings = new ArrayList<>();
                coordinates.forEach(ring -> rings.add(new LineGeometry(readCoordinates(ring))));

                return new PolygonGeometry(rings, boundingBox, properties);
            case MULTI_POINT_TYPE:
                List<PointGeometry> points = new ArrayList<>();
                readCoordinates(coordinates).forEach(position -> points.add(new PointGeometry(position)));

                return new MultiPointGeometry(points, boundingBox, properties);
            case MULTI_LINE_STRING_TYPE:
                List<LineGeometry> lines = new ArrayList<>();
                coordinates.forEach(line -> lines.add(new LineGeometry(readCoordinates(line))));

                return new MultiLineGeometry(lines, boundingBox, properties);
            case MULTI_POLYGON_TYPE:
                return readMultiPolygon(coordinates, boundingBox, properties);
            default:
                throw LOGGER.logExceptionAsError(new IllegalStateException(
                    String.format("Unsupported geometry type %s.", type)));
        }
    }

    private static MultiPolygonGeometry readMultiPolygon(JsonNode node, GeometryBoundingBox boundingBox,
        Map<String, Object> properties) {
        List<PolygonGeometry> polygons = new ArrayList<>();
        for (JsonNode polygon : node) {
            List<LineGeometry> rings = new ArrayList<>();
            polygon.forEach(ring -> rings.add(new LineGeometry(readCoordinates(ring))));

            polygons.add(new PolygonGeometry(rings));
        }

        return new MultiPolygonGeometry(polygons, boundingBox, properties);
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

    private static GeometryBoundingBox readBoundingBox(JsonNode node) {
        JsonNode boundingBoxNode = node.get(BOUNDING_BOX_PROPERTY);
        if (boundingBoxNode != null) {
            switch (boundingBoxNode.size()) {
                case 4:
                    return new GeometryBoundingBox(boundingBoxNode.get(0).asDouble(),
                        boundingBoxNode.get(1).asDouble(), boundingBoxNode.get(2).asDouble(),
                        boundingBoxNode.get(3).asDouble());
                case 6:
                    return new GeometryBoundingBox(boundingBoxNode.get(0).asDouble(),
                        boundingBoxNode.get(1).asDouble(), boundingBoxNode.get(3).asDouble(),
                        boundingBoxNode.get(4).asDouble(), boundingBoxNode.get(2).asDouble(),
                        boundingBoxNode.get(5).asDouble());
                default:
                    throw LOGGER.logExceptionAsError(
                        new IllegalStateException("Only 2 or 3 dimension bounding boxes are supported."));
            }
        }

        return null;
    }

    private static Map<String, Object> readProperties(JsonNode node, DeserializationContext ctxt) throws IOException {
        return readProperties(node, COORDINATES_PROPERTY, ctxt);
    }

    private static Map<String, Object> readProperties(JsonNode node, String knownProperty,
        DeserializationContext ctxt) throws IOException {
        Map<String, Object> additionalProperties = null;
        Iterator<Map.Entry<String, JsonNode>> fieldsIterator = node.fields();
        while (fieldsIterator.hasNext()) {
            Map.Entry<String, JsonNode> field = fieldsIterator.next();
            String propertyName = field.getKey();
            if (propertyName.equalsIgnoreCase(TYPE_PROPERTY)
                || propertyName.equalsIgnoreCase(BOUNDING_BOX_PROPERTY)
                || propertyName.equalsIgnoreCase(knownProperty)) {
                continue;
            }

            if (additionalProperties == null) {
                additionalProperties = new HashMap<>();
            }

            additionalProperties.put(propertyName, readAdditionalPropertyValue(field.getValue(), ctxt));
        }

        return additionalProperties;
    }

    private static Object readAdditionalPropertyValue(JsonNode node, DeserializationContext ctxt) throws IOException {
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
            case ARRAY:
                JsonParser parser = new TreeTraversingParser(node);
                return UntypedObjectDeserializer.Vanilla.std.deserialize(parser, ctxt);
            default:
                throw LOGGER.logExceptionAsError(new IllegalStateException(
                    String.format("Unsupported additional property type %s.", node.getNodeType())));
        }
    }

    private static List<GeometryPosition> readCoordinates(JsonNode coordinates) {
        List<GeometryPosition> positions = new ArrayList<>();

        coordinates.forEach(coordinate -> positions.add(readCoordinate(coordinate)));

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

    private static <T extends Geometry> JsonDeserializer<T> geometrySubclassDeserializer(Class<T> subclass) {
        return new JsonDeserializer<T>() {
            @Override
            public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                return subclass.cast(read(ctxt.readTree(p), ctxt));
            }
        };
    }
}
