// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.implementation;

import com.azure.search.models.GeoPoint;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.UntypedObjectDeserializer;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Custom deserializer to detect GeoJSON structures in dynamic results and deserialize as instances of {@link GeoPoint}
 */
final class GeoPointDeserializer extends UntypedObjectDeserializer {

    private final UntypedObjectDeserializer defaultDeserializer;

    /**
     * Constructor
     * @param defaultDeserializer the deserializer to use when a GeoJSON match is not found
     */
    GeoPointDeserializer(UntypedObjectDeserializer defaultDeserializer) {
        super(null, null);
        this.defaultDeserializer = defaultDeserializer;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        if (jp.currentTokenId() == JsonTokenId.ID_START_OBJECT) {
            Object obj = defaultDeserializer.deserialize(jp, ctxt);
            return parseGeoPoint(obj);
        } else if (jp.currentTokenId() == JsonTokenId.ID_START_ARRAY) {
            List<?> list = (List) defaultDeserializer.deserialize(jp, ctxt);
            return list.stream()
                .map(this::parseGeoPoint)
                .collect(Collectors.toList());
        } else {
            return defaultDeserializer.deserialize(jp, ctxt);
        }
    }

    /**
     * Converts an object to a GeoPoint if it is valid GeoJSON, otherwise returns the original object.
     * @param obj the object to parse
     * @return an instance of {@link GeoPoint} if valid GeoJSON, otherwise obj.
     */
    @SuppressWarnings("unchecked")
    private Object parseGeoPoint(Object obj) {
        if (isGeoJsonPoint(obj)) {
            Map<String, Object> map = (Map<String, Object>) obj;
            List<?> coordinates = (List) map.get("coordinates");

            Double latitude = coordinates.get(1).getClass() == Double.class
                ? (Double) coordinates.get(1)
                : new Double((Integer) coordinates.get(1));
            Double longitude = coordinates.get(0).getClass() == Double.class
                ? (Double) coordinates.get(0)
                : new Double((Integer) coordinates.get(0));

            return GeoPoint.create(latitude, longitude);
        } else {
            return obj;
        }
    }

    /**
     * Determines whether an object is valid GeoJSON object.
     * @param obj the object to test
     * @return true if the object is valid GeoJSON, false otherwise.
     */
    @SuppressWarnings("unchecked")
    private boolean isGeoJsonPoint(Object obj) {
        try {
            Map<String, Object> map = (Map<String, Object>) obj;

            return isValidPoint(map)
                && isValidCoordinates(map)
                && isValidCrs(map);
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean isValidPoint(Map<String, Object> map) {
        return map.get("type").equals("Point");
    }

    private boolean isValidCrs(Map<String, Object> map) {
        // crs is not required to deserialize, but must be valid if present
        boolean isValidCrs;
        if (map.containsKey("crs")) {
            Map<String, Object> crs = (Map<String, Object>) map.get("crs");
            boolean isValidType = crs.get("type").equals("name");

            Map<String, Object> properties = (Map<String, Object>) crs.get("properties");
            boolean isValidProperties = properties.get("name").equals("EPSG:4326");

            isValidCrs = isValidType && isValidProperties;
        } else {
            isValidCrs = true;
        }
        return isValidCrs;
    }

    private boolean isValidCoordinates(Map<String, Object> map) {
        boolean isValidCoordinates = false;
        List<?> coordinates = (List) map.get("coordinates");
        if (coordinates.size() == 2) {
            Class<?> longitudeClass = coordinates.get(0).getClass();
            boolean isValidLongitude = longitudeClass == Integer.class
                || longitudeClass == Double.class;

            Class<?> latitudeClass = coordinates.get(1).getClass();
            boolean isValidLatitude = latitudeClass == Integer.class
                || latitudeClass == Double.class;

            isValidCoordinates = isValidLongitude && isValidLatitude;
        }
        return isValidCoordinates;
    }
}
