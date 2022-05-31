// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License. See License.txt in the project root for
// license information.

package com.azure.maps.search.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.models.GeoObject;
import com.azure.maps.search.implementation.helpers.PolygonPropertiesHelper;
import com.azure.maps.search.implementation.helpers.Utility;
import com.azure.maps.search.implementation.models.GeoJsonObject;

/** The PolygonPrivate model. */
@Immutable
public final class Polygon {
    private String providerID;
    private GeoObject geometryData;

    static {
        PolygonPropertiesHelper.setAccessor(new PolygonPropertiesHelper
            .PolygonAccessor() {
            @Override
            public void setGeometry(Polygon result, GeoJsonObject geometry) {
                result.setGeometry(geometry);            }

            @Override
            public void setProviderID(Polygon result, String providerId) {
                result.setProviderID(providerId);
            }
        });
    }

    /**
     * Get the providerID property: ID of the returned entity.
     *
     * @return the providerID value.
     */
    public String getProviderID() {
        return this.providerID;
    }

    /**
     * Get the geometryData property: Geometry data in GeoJSON format. Please refer to [RFC
     * 7946](https://tools.ietf.org/html/rfc7946) for details. Present only if "error" is not present.
     *
     * @return the geometryData value.
     */
    public GeoObject getGeometry() {
        return this.geometryData;
    }

    //
    private void setProviderID(String providerId) {
        this.providerID = providerId;
    }

    private void setGeometry(GeoJsonObject geometry) {
        this.geometryData = Utility.toGeoObject(geometry);
    }
}
