// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.models;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests {@link GeoArray}.
 */
public class GeoArrayTests {
    @Test
    public void pointCoordinates() {
        GeoPoint point = new GeoPoint(1, 2);

        assertEquals(2, point.getCoordinates().count());
        assertEquals(1, point.getCoordinates().get(0));
        assertEquals(2, point.getCoordinates().get(1));
        assertThrows(IndexOutOfBoundsException.class, () -> point.getCoordinates().get(2));
    }

    @Test
    public void pointCoordinatesWithAltitude() {
        GeoPoint point = new GeoPoint(1, 2, 3D);

        assertEquals(3, point.getCoordinates().count());
        assertEquals(1, point.getCoordinates().get(0));
        assertEquals(2, point.getCoordinates().get(1));
        assertEquals(3, point.getCoordinates().get(2));
    }

    @Test
    public void pointCollectionCoordinates() {
        GeoPointCollection pointCollection = new GeoPointCollection(Arrays.asList(
            new GeoPoint(1, 2), new GeoPoint(3, 4)
        ));

        assertEquals(2, pointCollection.getCoordinates().size());

        assertEquals(1, pointCollection.getCoordinates().get(0).get(0));
        assertEquals(2, pointCollection.getCoordinates().get(0).get(1));

        assertEquals(3, pointCollection.getCoordinates().get(1).get(0));
        assertEquals(4, pointCollection.getCoordinates().get(1).get(1));
    }

    @Test
    public void lineCoordinates() {
        GeoLineString line = new GeoLineString(Arrays.asList(
            new GeoPosition(1, 2), new GeoPosition(3, 4), new GeoPosition(5, 6)
        ));

        assertEquals(3, line.getCoordinates().size());

        assertEquals(1, line.getCoordinates().get(0).get(0));
        assertEquals(2, line.getCoordinates().get(0).get(1));

        assertEquals(3, line.getCoordinates().get(1).get(0));
        assertEquals(4, line.getCoordinates().get(1).get(1));

        assertEquals(5, line.getCoordinates().get(2).get(0));
        assertEquals(6, line.getCoordinates().get(2).get(1));
    }

    @Test
    public void lineCollectionCoordinates() {
        GeoLineStringCollection lineCollection = new GeoLineStringCollection(Arrays.asList(
            new GeoLineString(Arrays.asList(new GeoPosition(1, 2), new GeoPosition(3, 4))),
            new GeoLineString(Arrays.asList(new GeoPosition(5, 6), new GeoPosition(7, 8)))
        ));

        assertEquals(2, lineCollection.getCoordinates().size());

        assertEquals(1, lineCollection.getCoordinates().get(0).get(0).get(0));
        assertEquals(2, lineCollection.getCoordinates().get(0).get(0).get(1));

        assertEquals(3, lineCollection.getCoordinates().get(0).get(1).get(0));
        assertEquals(4, lineCollection.getCoordinates().get(0).get(1).get(1));

        assertEquals(5, lineCollection.getCoordinates().get(1).get(0).get(0));
        assertEquals(6, lineCollection.getCoordinates().get(1).get(0).get(1));

        assertEquals(7, lineCollection.getCoordinates().get(1).get(1).get(0));
        assertEquals(8, lineCollection.getCoordinates().get(1).get(1).get(1));
    }

    @Test
    public void polygonCoordinates() {
        GeoPolygon polygon = new GeoPolygon(Arrays.asList(
            new GeoLinearRing(Arrays.asList(
                new GeoPosition(1, 1), new GeoPosition(1, 2), new GeoPosition(2, 2), new GeoPosition(1, 1)
            )),
            new GeoLinearRing(Arrays.asList(
                new GeoPosition(5, 5), new GeoPosition(5, 6), new GeoPosition(6, 6), new GeoPosition(5, 5)
            ))
        ));

        assertEquals(2, polygon.getCoordinates().size());

        assertEquals(1, polygon.getCoordinates().get(0).get(0).get(0));
        assertEquals(1, polygon.getCoordinates().get(0).get(0).get(1));

        assertEquals(1, polygon.getCoordinates().get(0).get(1).get(0));
        assertEquals(2, polygon.getCoordinates().get(0).get(1).get(1));

        assertEquals(2, polygon.getCoordinates().get(0).get(2).get(0));
        assertEquals(2, polygon.getCoordinates().get(0).get(2).get(1));

        assertEquals(1, polygon.getCoordinates().get(0).get(3).get(0));
        assertEquals(1, polygon.getCoordinates().get(0).get(3).get(1));

        assertEquals(5, polygon.getCoordinates().get(1).get(0).get(0));
        assertEquals(5, polygon.getCoordinates().get(1).get(0).get(1));

        assertEquals(5, polygon.getCoordinates().get(1).get(1).get(0));
        assertEquals(6, polygon.getCoordinates().get(1).get(1).get(1));

        assertEquals(6, polygon.getCoordinates().get(1).get(2).get(0));
        assertEquals(6, polygon.getCoordinates().get(1).get(2).get(1));

        assertEquals(5, polygon.getCoordinates().get(1).get(3).get(0));
        assertEquals(5, polygon.getCoordinates().get(1).get(3).get(1));
    }

    @Test
    public void polygonCollectionCoordinates() {
        GeoPolygonCollection polygonCollection = new GeoPolygonCollection(Arrays.asList(
            new GeoPolygon(Arrays.asList(
                new GeoLinearRing(Arrays.asList(
                    new GeoPosition(1, 1), new GeoPosition(1, 2), new GeoPosition(2, 2), new GeoPosition(1, 1)
                )),
                new GeoLinearRing(Arrays.asList(
                    new GeoPosition(5, 5), new GeoPosition(5, 6), new GeoPosition(6, 6), new GeoPosition(5, 5)
                ))
            )),
            new GeoPolygon(Arrays.asList(
                new GeoLinearRing(Arrays.asList(
                    new GeoPosition(9, 9), new GeoPosition(9, 10), new GeoPosition(10, 10), new GeoPosition(9, 9)
                )),
                new GeoLinearRing(Arrays.asList(
                    new GeoPosition(13, 13), new GeoPosition(13, 14), new GeoPosition(14, 14), new GeoPosition(13, 13)
                ))
            ))
        ));

        assertEquals(2, polygonCollection.getCoordinates().size());

        GeoArray<GeoArray<GeoPosition>> polygonCoordinates = polygonCollection.getCoordinates().get(0);

        assertEquals(1, polygonCoordinates.get(0).get(0).get(0));
        assertEquals(1, polygonCoordinates.get(0).get(0).get(0));

        assertEquals(1, polygonCoordinates.get(0).get(1).get(0));
        assertEquals(2, polygonCoordinates.get(0).get(1).get(1));

        assertEquals(2, polygonCoordinates.get(0).get(2).get(0));
        assertEquals(2, polygonCoordinates.get(0).get(2).get(1));

        assertEquals(1, polygonCoordinates.get(0).get(3).get(0));
        assertEquals(1, polygonCoordinates.get(0).get(3).get(1));

        assertEquals(5, polygonCoordinates.get(1).get(0).get(0));
        assertEquals(5, polygonCoordinates.get(1).get(0).get(1));

        assertEquals(5, polygonCoordinates.get(1).get(1).get(0));
        assertEquals(6, polygonCoordinates.get(1).get(1).get(1));

        assertEquals(6, polygonCoordinates.get(1).get(2).get(0));
        assertEquals(6, polygonCoordinates.get(1).get(2).get(1));

        assertEquals(5, polygonCoordinates.get(1).get(3).get(0));
        assertEquals(5, polygonCoordinates.get(1).get(3).get(1));

        polygonCoordinates = polygonCollection.getCoordinates().get(1);

        assertEquals(9, polygonCoordinates.get(0).get(0).get(0));
        assertEquals(9, polygonCoordinates.get(0).get(0).get(1));

        assertEquals(9, polygonCoordinates.get(0).get(1).get(0));
        assertEquals(10, polygonCoordinates.get(0).get(1).get(1));

        assertEquals(10, polygonCoordinates.get(0).get(2).get(0));
        assertEquals(10, polygonCoordinates.get(0).get(2).get(1));

        assertEquals(9, polygonCoordinates.get(0).get(3).get(0));
        assertEquals(9, polygonCoordinates.get(0).get(3).get(1));

        assertEquals(13, polygonCoordinates.get(1).get(0).get(0));
        assertEquals(13, polygonCoordinates.get(1).get(0).get(1));

        assertEquals(13, polygonCoordinates.get(1).get(1).get(0));
        assertEquals(14, polygonCoordinates.get(1).get(1).get(1));

        assertEquals(14, polygonCoordinates.get(1).get(2).get(0));
        assertEquals(14, polygonCoordinates.get(1).get(2).get(1));

        assertEquals(13, polygonCoordinates.get(1).get(3).get(0));
        assertEquals(13, polygonCoordinates.get(1).get(3).get(1));
    }
}
