// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

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
 * Deserializes a JSON object into a {@link GeoObject}.
 */
final class GeoJsonDeserializer extends JsonDeserializer<GeoObject> {
    private static final ClientLogger LOGGER = new ClientLogger(GeoJsonDeserializer.class);

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
            .addDeserializer(GeoObject.class, new GeoJsonDeserializer())
            .addDeserializer(GeoPoint.class, geoSubclassDeserializer(GeoPoint.class))
            .addDeserializer(GeoLineString.class, geoSubclassDeserializer(GeoLineString.class))
            .addDeserializer(GeoPolygon.class, geoSubclassDeserializer(GeoPolygon.class))
            .addDeserializer(GeoPointCollection.class, geoSubclassDeserializer(GeoPointCollection.class))
            .addDeserializer(GeoLineStringCollection.class, geoSubclassDeserializer(GeoLineStringCollection.class))
            .addDeserializer(GeoPolygonCollection.class, geoSubclassDeserializer(GeoPolygonCollection.class))
            .addDeserializer(GeoCollection.class, geoSubclassDeserializer(GeoCollection.class));
    }

    /**
     * Gets a module wrapping this deserializer as an adapter for the Jackson ObjectMapper.
     *
     * @return A {@link SimpleModule} to be plugged onto Jackson ObjectMapper.
     */
    public static SimpleModule getModule() {
        return MODULE;
    }

    @Override
    public GeoObject deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return read(ctxt.readTree(p));
    }

    private static GeoObject read(JsonNode node) {
        String type = getRequiredProperty(node, TYPE_PROPERTY).asText();

        if (isGeoObjectType(type, GeoObjectType.GEOMETRY_COLLECTION)) {
            List<GeoObject> geometries = new ArrayList<>();
            for (JsonNode geoNode : getRequiredProperty(node, GEOMETRIES_PROPERTY)) {
                geometries.add(read(geoNode));
            }

            return new GeoCollection(geometries, readBoundingBox(node),
                readProperties(node, GEOMETRIES_PROPERTY));
        }

        JsonNode coordinates = getRequiredProperty(node, COORDINATES_PROPERTY);

        GeoBoundingBox boundingBox = readBoundingBox(node);
        Map<String, Object> properties = readProperties(node);

        if (isGeoObjectType(type, GeoObjectType.POINT)) {
            return new GeoPoint(readCoordinate(coordinates), boundingBox, properties);
        } else if (isGeoObjectType(type, GeoObjectType.LINE_STRING)) {
            return new GeoLineString(readCoordinates(coordinates), boundingBox, properties);
        } else if (isGeoObjectType(type, GeoObjectType.POLYGON)) {
            List<GeoLinearRing> rings = new ArrayList<>();
            coordinates.forEach(ring -> rings.add(new GeoLinearRing(readCoordinates(ring))));

            return new GeoPolygon(rings, boundingBox, properties);
        } else if (isGeoObjectType(type, GeoObjectType.MULTI_POINT)) {
            List<GeoPoint> points = new ArrayList<>();
            readCoordinates(coordinates).forEach(position -> points.add(new GeoPoint(position)));

            return new GeoPointCollection(points, boundingBox, properties);
        } else if (isGeoObjectType(type, GeoObjectType.MULTI_LINE_STRING)) {
            List<GeoLineString> lines = new ArrayList<>();
            coordinates.forEach(line -> lines.add(new GeoLineString(readCoordinates(line))));

            return new GeoLineStringCollection(lines, boundingBox, properties);
        } else if (isGeoObjectType(type, GeoObjectType.MULTI_POLYGON)) {
            return readMultiPolygon(coordinates, boundingBox, properties);
        }

        throw LOGGER.logExceptionAsError(new IllegalStateException(String.format("Unsupported geo type %s.", type)));
    }

    private static boolean isGeoObjectType(String jsonType, GeoObjectType type) {
        return type.toString().equalsIgnoreCase(jsonType);
    }

    private static GeoPolygonCollection readMultiPolygon(JsonNode node, GeoBoundingBox boundingBox,
        Map<String, Object> properties) {
        List<GeoPolygon> polygons = new ArrayList<>();
        for (JsonNode polygon : node) {
            List<GeoLinearRing> rings = new ArrayList<>();
            polygon.forEach(ring -> rings.add(new GeoLinearRing(readCoordinates(ring))));

            polygons.add(new GeoPolygon(rings));
        }

        return new GeoPolygonCollection(polygons, boundingBox, properties);
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

    private static GeoBoundingBox readBoundingBox(JsonNode node) {
        JsonNode boundingBoxNode = node.get(BOUNDING_BOX_PROPERTY);
        if (boundingBoxNode != null) {
            switch (boundingBoxNode.size()) {
                case 4:
                    return new GeoBoundingBox(boundingBoxNode.get(0).asDouble(),
                        boundingBoxNode.get(1).asDouble(), boundingBoxNode.get(2).asDouble(),
                        boundingBoxNode.get(3).asDouble());
                case 6:
                    return new GeoBoundingBox(boundingBoxNode.get(0).asDouble(),
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

    private static Map<String, Object> readProperties(JsonNode node) {
        return readProperties(node, COORDINATES_PROPERTY);
    }

    private static Map<String, Object> readProperties(JsonNode node, String knownProperty) {
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

            additionalProperties.put(propertyName, readAdditionalPropertyValue(field.getValue()));
        }

        return additionalProperties;
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
                Map<String, Object> object = new HashMap<>();
                node.fields().forEachRemaining(field ->
                    object.put(field.getKey(), readAdditionalPropertyValue(field.getValue())));

                return object;
            case ARRAY:
                List<Object> array = new ArrayList<>();
                node.forEach(element -> array.add(readAdditionalPropertyValue(element)));

                return array;
            default:
                throw LOGGER.logExceptionAsError(new IllegalStateException(
                    String.format("Unsupported additional property type %s.", node.getNodeType())));
        }
    }

    private static List<GeoPosition> readCoordinates(JsonNode coordinates) {
        List<GeoPosition> positions = new ArrayList<>();

        coordinates.forEach(coordinate -> positions.add(readCoordinate(coordinate)));

        return positions;
    }

    private static GeoPosition readCoordinate(JsonNode coordinate) {
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

        return new GeoPosition(longitude, latitude, altitude);
    }

    private static <T extends GeoObject> JsonDeserializer<T> geoSubclassDeserializer(Class<T> subclass) {
        return new JsonDeserializer<T>() {
            @Override
            public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                return subclass.cast(read(ctxt.readTree(p)));
            }
        };
    }
}
