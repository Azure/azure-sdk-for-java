// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License. See License.txt in the project root for
// license information.

package com.azure.maps.route.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.models.GeoPointCollection;

/** An object with a matrix of coordinates. */
@Fluent
public final class RouteMatrixQuery {
    /*
     * A valid `GeoJSON MultiPoint` geometry type. Please refer to [RFC
     * 7946](https://tools.ietf.org/html/rfc7946#section-3.1.3) for details.
     */
    private GeoPointCollection origins;

    /*
     * A valid `GeoJSON MultiPoint` geometry type. Please refer to [RFC
     * 7946](https://tools.ietf.org/html/rfc7946#section-3.1.3) for details.
     */
    private GeoPointCollection destinations;

    /**
     * Get the origins property: A valid `GeoJSON MultiPoint` geometry type. Please refer to [RFC
     * 7946](https://tools.ietf.org/html/rfc7946#section-3.1.3) for details.
     *
     * @return the origins value.
     */
    public GeoPointCollection getOrigins() {
        return this.origins;
    }

    /**
     * Set the origins property: A valid `GeoJSON MultiPoint` geometry type. Please refer to [RFC
     * 7946](https://tools.ietf.org/html/rfc7946#section-3.1.3) for details.
     *
     * @param origins the origins value to set.
     * @return the RouteMatrixQuery object itself.
     */
    public RouteMatrixQuery setOrigins(GeoPointCollection origins) {
        this.origins = origins;
        return this;
    }

    /**
     * Get the destinations property: A valid `GeoJSON MultiPoint` geometry type. Please refer to [RFC
     * 7946](https://tools.ietf.org/html/rfc7946#section-3.1.3) for details.
     *
     * @return the destinations value.
     */
    public GeoPointCollection getDestinations() {
        return this.destinations;
    }

    /**
     * Set the destinations property: A valid `GeoJSON MultiPoint` geometry type. Please refer to [RFC
     * 7946](https://tools.ietf.org/html/rfc7946#section-3.1.3) for details.
     *
     * @param destinations the destinations value to set.
     * @return the RouteMatrixQuery object itself.
     */
    public RouteMatrixQuery setDestinations(GeoPointCollection destinations) {
        this.destinations = destinations;
        return this;
    }
}
