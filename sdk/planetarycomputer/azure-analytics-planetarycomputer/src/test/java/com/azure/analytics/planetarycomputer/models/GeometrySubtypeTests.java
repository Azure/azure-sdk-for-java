// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.planetarycomputer.models;

import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

/**
 * Unit tests for Geometry subtype models: {@link GeoJsonPoint}, {@link GeoJsonLineString}, {@link GeoJsonPolygon},
 * {@link GeoJsonMultiPoint}, {@link GeoJsonMultiLineString}, {@link GeoJsonMultiPolygon}.
 */
public final class GeometrySubtypeTests {

    @Test
    public void testPointDeserialize() throws Exception {
        GeoJsonPoint model = BinaryData
            .fromString("{\"type\":\"Point\",\"coordinates\":[-73.99,40.71],\"bbox\":[-73.99,40.71,-73.99,40.71]}")
            .toObject(GeoJsonPoint.class);
        Assertions.assertEquals(GeometryType.POINT, model.getType());
        Assertions.assertNotNull(model.getCoordinates());
        Assertions.assertEquals(2, model.getCoordinates().size());
        Assertions.assertEquals(-73.99, model.getCoordinates().get(0));
        Assertions.assertEquals(40.71, model.getCoordinates().get(1));
        Assertions.assertNotNull(model.getBoundingBox());
        Assertions.assertEquals(4, model.getBoundingBox().size());
    }

    @Test
    public void testPointRoundTrip() throws Exception {
        GeoJsonPoint model = new GeoJsonPoint().setCoordinates(Arrays.asList(-73.99, 40.71))
            .setBoundingBox(Arrays.asList(-73.99, 40.71, -73.99, 40.71));
        model = BinaryData.fromObject(model).toObject(GeoJsonPoint.class);
        Assertions.assertEquals(GeometryType.POINT, model.getType());
        Assertions.assertEquals(2, model.getCoordinates().size());
        Assertions.assertEquals(-73.99, model.getCoordinates().get(0));
    }

    @Test
    public void testLineStringDeserialize() throws Exception {
        GeoJsonLineString model
            = BinaryData.fromString("{\"type\":\"LineString\",\"coordinates\":[[-73.99,40.71],[-73.98,40.72]]}")
                .toObject(GeoJsonLineString.class);
        Assertions.assertEquals(GeometryType.LINE_STRING, model.getType());
        Assertions.assertNotNull(model.getCoordinates());
        Assertions.assertEquals(2, model.getCoordinates().size());
        Assertions.assertEquals(2, model.getCoordinates().get(0).size());
    }

    @Test
    public void testLineStringRoundTrip() throws Exception {
        GeoJsonLineString model = new GeoJsonLineString()
            .setCoordinates(Arrays.asList(Arrays.asList(-73.99, 40.71), Arrays.asList(-73.98, 40.72)))
            .setBoundingBox(Arrays.asList(-73.99, 40.71, -73.98, 40.72));
        model = BinaryData.fromObject(model).toObject(GeoJsonLineString.class);
        Assertions.assertEquals(GeometryType.LINE_STRING, model.getType());
        Assertions.assertEquals(2, model.getCoordinates().size());
    }

    @Test
    public void testPolygonDeserialize() throws Exception {
        GeoJsonPolygon model
            = BinaryData.fromString("{\"type\":\"Polygon\",\"coordinates\":[[[-73.99,40.71],[-73.98,40.71],"
                + "[-73.98,40.72],[-73.99,40.72],[-73.99,40.71]]]}").toObject(GeoJsonPolygon.class);
        Assertions.assertEquals(GeometryType.POLYGON, model.getType());
        Assertions.assertNotNull(model.getCoordinates());
        Assertions.assertEquals(1, model.getCoordinates().size());
        Assertions.assertEquals(5, model.getCoordinates().get(0).size());
    }

    @Test
    public void testPolygonRoundTrip() throws Exception {
        GeoJsonPolygon model = new GeoJsonPolygon()
            .setCoordinates(Arrays.asList(Arrays.asList(Arrays.asList(-73.99, 40.71), Arrays.asList(-73.98, 40.71),
                Arrays.asList(-73.98, 40.72), Arrays.asList(-73.99, 40.72), Arrays.asList(-73.99, 40.71))))
            .setBoundingBox(Arrays.asList(-73.99, 40.71, -73.98, 40.72));
        model = BinaryData.fromObject(model).toObject(GeoJsonPolygon.class);
        Assertions.assertEquals(GeometryType.POLYGON, model.getType());
        Assertions.assertEquals(1, model.getCoordinates().size());
    }

    @Test
    public void testMultiPointDeserialize() throws Exception {
        GeoJsonMultiPoint model
            = BinaryData.fromString("{\"type\":\"MultiPoint\",\"coordinates\":[[-73.99,40.71],[-73.98,40.72]]}")
                .toObject(GeoJsonMultiPoint.class);
        Assertions.assertEquals(GeometryType.MULTI_POINT, model.getType());
        Assertions.assertNotNull(model.getCoordinates());
        Assertions.assertEquals(2, model.getCoordinates().size());
    }

    @Test
    public void testMultiPointRoundTrip() throws Exception {
        GeoJsonMultiPoint model = new GeoJsonMultiPoint()
            .setCoordinates(Arrays.asList(Arrays.asList(-73.99, 40.71), Arrays.asList(-73.98, 40.72)));
        model = BinaryData.fromObject(model).toObject(GeoJsonMultiPoint.class);
        Assertions.assertEquals(GeometryType.MULTI_POINT, model.getType());
        Assertions.assertEquals(2, model.getCoordinates().size());
    }

    @Test
    public void testMultiLineStringDeserialize() throws Exception {
        GeoJsonMultiLineString model
            = BinaryData.fromString("{\"type\":\"MultiLineString\",\"coordinates\":[[[-73.99,40.71],[-73.98,40.72]],"
                + "[[-73.97,40.73],[-73.96,40.74]]]}").toObject(GeoJsonMultiLineString.class);
        Assertions.assertEquals(GeometryType.MULTI_LINE_STRING, model.getType());
        Assertions.assertNotNull(model.getCoordinates());
        Assertions.assertEquals(2, model.getCoordinates().size());
    }

    @Test
    public void testMultiLineStringRoundTrip() throws Exception {
        GeoJsonMultiLineString model = new GeoJsonMultiLineString()
            .setCoordinates(Arrays.asList(Arrays.asList(Arrays.asList(-73.99, 40.71), Arrays.asList(-73.98, 40.72)),
                Arrays.asList(Arrays.asList(-73.97, 40.73), Arrays.asList(-73.96, 40.74))))
            .setBoundingBox(Arrays.asList(-73.99, 40.71, -73.96, 40.74));
        model = BinaryData.fromObject(model).toObject(GeoJsonMultiLineString.class);
        Assertions.assertEquals(GeometryType.MULTI_LINE_STRING, model.getType());
        Assertions.assertEquals(2, model.getCoordinates().size());
    }

    @Test
    public void testMultiPolygonDeserialize() throws Exception {
        GeoJsonMultiPolygon model
            = BinaryData
                .fromString(
                    "{\"type\":\"MultiPolygon\"," + "\"coordinates\":[[[[-73.99,40.71],[-73.98,40.71],[-73.98,40.72],"
                        + "[-73.99,40.72],[-73.99,40.71]]]]}")
                .toObject(GeoJsonMultiPolygon.class);
        Assertions.assertEquals(GeometryType.MULTI_POLYGON, model.getType());
        Assertions.assertNotNull(model.getCoordinates());
        Assertions.assertEquals(1, model.getCoordinates().size());
    }

    @Test
    public void testMultiPolygonRoundTrip() throws Exception {
        GeoJsonMultiPolygon model = new GeoJsonMultiPolygon()
            .setCoordinates(
                Arrays.asList(Arrays.asList(Arrays.asList(Arrays.asList(-73.99, 40.71), Arrays.asList(-73.98, 40.71),
                    Arrays.asList(-73.98, 40.72), Arrays.asList(-73.99, 40.72), Arrays.asList(-73.99, 40.71)))))
            .setBoundingBox(Arrays.asList(-73.99, 40.71, -73.98, 40.72));
        model = BinaryData.fromObject(model).toObject(GeoJsonMultiPolygon.class);
        Assertions.assertEquals(GeometryType.MULTI_POLYGON, model.getType());
        Assertions.assertEquals(1, model.getCoordinates().size());
    }

    @Test
    public void testGeometryDiscriminatorDispatch() throws Exception {
        // Test that GeoJsonGeometry.fromJson dispatches to the correct subtype
        GeoJsonGeometry point = BinaryData.fromString("{\"type\":\"Point\",\"coordinates\":[-73.99,40.71]}")
            .toObject(GeoJsonGeometry.class);
        Assertions.assertEquals(GeometryType.POINT, point.getType());
        Assertions.assertTrue(point instanceof GeoJsonPoint);

        GeoJsonGeometry polygon
            = BinaryData.fromString("{\"type\":\"Polygon\",\"coordinates\":[[[-73.99,40.71],[-73.98,40.71],"
                + "[-73.98,40.72],[-73.99,40.71]]]}").toObject(GeoJsonGeometry.class);
        Assertions.assertEquals(GeometryType.POLYGON, polygon.getType());
        Assertions.assertTrue(polygon instanceof GeoJsonPolygon);

        GeoJsonGeometry multiPoint = BinaryData.fromString("{\"type\":\"MultiPoint\",\"coordinates\":[[-73.99,40.71]]}")
            .toObject(GeoJsonGeometry.class);
        Assertions.assertEquals(GeometryType.MULTI_POINT, multiPoint.getType());
        Assertions.assertTrue(multiPoint instanceof GeoJsonMultiPoint);
    }
}
