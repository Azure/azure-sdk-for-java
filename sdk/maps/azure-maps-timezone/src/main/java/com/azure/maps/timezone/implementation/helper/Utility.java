// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// See License.txt in the project root for license information.

package com.azure.maps.timezone.implementation.helper;

import java.util.Arrays;
import java.util.List;

import com.azure.core.models.GeoPosition;

public class Utility {
    public static List<Double> toCoordinateList(GeoPosition coordinates) {
        return Arrays.asList(coordinates.getLatitude(), coordinates.getLongitude());
    }
}
