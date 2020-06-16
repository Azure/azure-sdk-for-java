// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.models.spatial;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * An abstract representation of a geometry.
 */
public abstract class Geometry {
    private final GeometryBoundingBox boundingBox;
    private final Map<String, Object> properties;

    Geometry(GeometryBoundingBox boundingBox, Map<String, Object> properties) {
        this.boundingBox = boundingBox;

        if (properties == null) {
            this.properties = Collections.emptyMap();
        } else {
            this.properties = properties.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
    }

    /**
     * Bounding box for this geometry.
     *
     * @return The bounding box for this geometry.
     */
    public GeometryBoundingBox getBoundingBox() {
        return boundingBox;
    }

    /**
     * Additional properties about this geometry.
     *
     * @return An unmodifiable representation of the additional properties associated with this geometry.
     */
    public Map<String, Object> getProperties() {
        return properties;
    }
}
