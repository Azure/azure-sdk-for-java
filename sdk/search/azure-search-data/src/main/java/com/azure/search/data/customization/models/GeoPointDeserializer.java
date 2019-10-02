// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.data.customization.models;

import com.azure.search.data.common.jsonwrapper.api.Deserializer;
import com.azure.search.data.common.jsonwrapper.api.Node;

import java.util.List;

// Custom deserializer for GeoPoint type
public class GeoPointDeserializer extends Deserializer {

    public GeoPointDeserializer() {
        super(GeoPoint.class);
    }

    /**
     * Constructor
     *
     * @param rawType class type
     */
    protected GeoPointDeserializer(Class rawType) {
        super(rawType);
    }

    @Override
    public GeoPoint deserialize(Node node) {
        String type = node.get("type").asString();
        if (type.equalsIgnoreCase("Point")) {
            Node coordinates = node.get("coordinates");
            if (coordinates != null && coordinates.isJsonArray()) {
                List<Node> elements = coordinates.getElements();
                if (elements.size() == 2) {
                    double longitude = elements.get(0).asDouble();
                    double latitude = elements.get(1).asDouble();
                    return GeoPoint.create(latitude, longitude);
                }
            }
        }

        return null;
    }
}
