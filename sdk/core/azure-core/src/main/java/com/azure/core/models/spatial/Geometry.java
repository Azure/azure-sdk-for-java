// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.models.spatial;

/**
 * An abstract representation of a geometry.
 */
public abstract class Geometry {
    private final GeometryProperties properties;

    Geometry(GeometryProperties properties) {
        this.properties = properties;
    }

    /**
     * Additional properties about this geometry.
     *
     * @return The additional properties associated with this geometry.
     */
    public GeometryProperties getProperties() {
        return properties;
    }
}
