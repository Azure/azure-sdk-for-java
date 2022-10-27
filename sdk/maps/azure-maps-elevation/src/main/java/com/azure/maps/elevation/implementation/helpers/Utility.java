// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.elevation.implementation.helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.azure.core.models.GeoBoundingBox;
import com.azure.core.models.GeoPosition;
import com.azure.maps.elevation.implementation.models.LatLongPairAbbreviated;

public class Utility {
    
    public static List<LatLongPairAbbreviated> toLatLongPairAbbreviated(List<GeoPosition> points) {
        List<LatLongPairAbbreviated> latLongPairList = new ArrayList<>((points.size()));
        for (GeoPosition point : points) {
            latLongPairList.add(new LatLongPairAbbreviated().setLat(point.getLatitude()).setLon(point.getLongitude()));
        }
        return latLongPairList;
    }

    public static List<String> geoPositionToString(List<GeoPosition> points) {
        List<String> stringPointsList = new ArrayList<>();
        for (GeoPosition point : points) {
            stringPointsList.add(point.getLongitude() + "," + point.getLatitude());
        }
        return stringPointsList;
    }

    public static List<Double> geoBoundingBoxAsList(GeoBoundingBox boundingBox) {
        return Arrays.asList(boundingBox.getWest(), boundingBox.getSouth(), boundingBox.getEast(), boundingBox.getNorth());
    }
}
