// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.traffic.implementation.helpers;

import java.util.Arrays;
import java.util.List;

import com.azure.core.models.GeoBoundingBox;
import com.azure.core.models.GeoPosition;
import com.azure.maps.traffic.implementation.models.LatLongPair;



public class Utility {
    public static List<Double> toBoundingBox(GeoBoundingBox boundingBox) {
        return Arrays.asList(boundingBox.getSouth(), boundingBox.getWest(), boundingBox.getNorth(), boundingBox.getEast());
    }
    public static List<Double> toCoordinates(GeoPosition geoPosition) {
        return Arrays.asList(geoPosition.getLatitude(), geoPosition.getLongitude());
    }

    public static GeoPosition toGeoPosition(LatLongPair latLongPair) {
        return new GeoPosition(latLongPair.getLongitude(), latLongPair.getLatitude());
    }
}
