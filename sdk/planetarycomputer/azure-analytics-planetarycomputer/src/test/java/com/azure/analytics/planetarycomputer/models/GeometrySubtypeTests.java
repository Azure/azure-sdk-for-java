// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.planetarycomputer.models;

import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

/**
 * Unit tests for Geometry subtype models: {@link Point}, {@link LineString}, {@link Polygon},
 * {@link MultiPoint}, {@link MultiLineString}, {@link MultiPolygon}.
 */
public final class GeometrySubtypeTests {

    @Test
    public void testPointDeserialize() throws Exception {
        Point model = BinaryData
            .fromString("{\"type\":\"Point\",\"coordinates\":[-73.99,40.71],\"bbox\":[-73.99,40.71,-73.99,40.71]}")
            .toObject(Point.class);
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
        Point model = new Point().setCoordinates(Arrays.asList(-73.99, 40.71))
            .setBoundingBox(Arrays.asList(-73.99, 40.71, -73.99, 40.71));
        model = BinaryData.fromObject(model).toObject(Point.class);
        Assertions.assertEquals(GeometryType.POINT, model.getType());
        Assertions.assertEquals(2, model.getCoordinates().size());
        Assertions.assertEquals(-73.99, model.getCoordinates().get(0));
    }

    @Test
    public void testLineStringDeserialize() throws Exception {
        LineString model
            = BinaryData.fromString("{\"type\":\"LineString\",\"coordinates\":[[-73.99,40.71],[-73.98,40.72]]}")
                .toObject(LineString.class);
        Assertions.assertEquals(GeometryType.LINE_STRING, model.getType());
        Assertions.assertNotNull(model.getCoordinates());
        Assertions.assertEquals(2, model.getCoordinates().size());
        Assertions.assertEquals(2, model.getCoordinates().get(0).size());
    }

    @Test
    public void testLineStringRoundTrip() throws Exception {
        LineString model
            = new LineString().setCoordinates(Arrays.asList(Arrays.asList(-73.99, 40.71), Arrays.asList(-73.98, 40.72)))
                .setBoundingBox(Arrays.asList(-73.99, 40.71, -73.98, 40.72));
        model = BinaryData.fromObject(model).toObject(LineString.class);
        Assertions.assertEquals(GeometryType.LINE_STRING, model.getType());
        Assertions.assertEquals(2, model.getCoordinates().size());
    }

    @Test
    public void testPolygonDeserialize() throws Exception {
        Polygon model = BinaryData.fromString("{\"type\":\"Polygon\",\"coordinates\":[[[-73.99,40.71],[-73.98,40.71],"
            + "[-73.98,40.72],[-73.99,40.72],[-73.99,40.71]]]}").toObject(Polygon.class);
        Assertions.assertEquals(GeometryType.POLYGON, model.getType());
        Assertions.assertNotNull(model.getCoordinates());
        Assertions.assertEquals(1, model.getCoordinates().size());
        Assertions.assertEquals(5, model.getCoordinates().get(0).size());
    }

    @Test
    public void testPolygonRoundTrip() throws Exception {
        Polygon model = new Polygon()
            .setCoordinates(Arrays.asList(Arrays.asList(Arrays.asList(-73.99, 40.71), Arrays.asList(-73.98, 40.71),
                Arrays.asList(-73.98, 40.72), Arrays.asList(-73.99, 40.72), Arrays.asList(-73.99, 40.71))))
            .setBoundingBox(Arrays.asList(-73.99, 40.71, -73.98, 40.72));
        model = BinaryData.fromObject(model).toObject(Polygon.class);
        Assertions.assertEquals(GeometryType.POLYGON, model.getType());
        Assertions.assertEquals(1, model.getCoordinates().size());
    }

    @Test
    public void testMultiPointDeserialize() throws Exception {
        MultiPoint model
            = BinaryData.fromString("{\"type\":\"MultiPoint\",\"coordinates\":[[-73.99,40.71],[-73.98,40.72]]}")
                .toObject(MultiPoint.class);
        Assertions.assertEquals(GeometryType.MULTI_POINT, model.getType());
        Assertions.assertNotNull(model.getCoordinates());
        Assertions.assertEquals(2, model.getCoordinates().size());
    }

    @Test
    public void testMultiPointRoundTrip() throws Exception {
        MultiPoint model = new MultiPoint()
            .setCoordinates(Arrays.asList(Arrays.asList(-73.99, 40.71), Arrays.asList(-73.98, 40.72)));
        model = BinaryData.fromObject(model).toObject(MultiPoint.class);
        Assertions.assertEquals(GeometryType.MULTI_POINT, model.getType());
        Assertions.assertEquals(2, model.getCoordinates().size());
    }

    @Test
    public void testMultiLineStringDeserialize() throws Exception {
        MultiLineString model
            = BinaryData.fromString("{\"type\":\"MultiLineString\",\"coordinates\":[[[-73.99,40.71],[-73.98,40.72]],"
                + "[[-73.97,40.73],[-73.96,40.74]]]}").toObject(MultiLineString.class);
        Assertions.assertEquals(GeometryType.MULTI_LINE_STRING, model.getType());
        Assertions.assertNotNull(model.getCoordinates());
        Assertions.assertEquals(2, model.getCoordinates().size());
    }

    @Test
    public void testMultiLineStringRoundTrip() throws Exception {
        MultiLineString model = new MultiLineString()
            .setCoordinates(Arrays.asList(Arrays.asList(Arrays.asList(-73.99, 40.71), Arrays.asList(-73.98, 40.72)),
                Arrays.asList(Arrays.asList(-73.97, 40.73), Arrays.asList(-73.96, 40.74))))
            .setBoundingBox(Arrays.asList(-73.99, 40.71, -73.96, 40.74));
        model = BinaryData.fromObject(model).toObject(MultiLineString.class);
        Assertions.assertEquals(GeometryType.MULTI_LINE_STRING, model.getType());
        Assertions.assertEquals(2, model.getCoordinates().size());
    }

    @Test
    public void testMultiPolygonDeserialize() throws Exception {
        MultiPolygon model
            = BinaryData
                .fromString(
                    "{\"type\":\"MultiPolygon\"," + "\"coordinates\":[[[[-73.99,40.71],[-73.98,40.71],[-73.98,40.72],"
                        + "[-73.99,40.72],[-73.99,40.71]]]]}")
                .toObject(MultiPolygon.class);
        Assertions.assertEquals(GeometryType.MULTI_POLYGON, model.getType());
        Assertions.assertNotNull(model.getCoordinates());
        Assertions.assertEquals(1, model.getCoordinates().size());
    }

    @Test
    public void testMultiPolygonRoundTrip() throws Exception {
        MultiPolygon model = new MultiPolygon()
            .setCoordinates(
                Arrays.asList(Arrays.asList(Arrays.asList(Arrays.asList(-73.99, 40.71), Arrays.asList(-73.98, 40.71),
                    Arrays.asList(-73.98, 40.72), Arrays.asList(-73.99, 40.72), Arrays.asList(-73.99, 40.71)))))
            .setBoundingBox(Arrays.asList(-73.99, 40.71, -73.98, 40.72));
        model = BinaryData.fromObject(model).toObject(MultiPolygon.class);
        Assertions.assertEquals(GeometryType.MULTI_POLYGON, model.getType());
        Assertions.assertEquals(1, model.getCoordinates().size());
    }

    @Test
    public void testGeometryDiscriminatorDispatch() throws Exception {
        // Test that Geometry.fromJson dispatches to the correct subtype
        Geometry point
            = BinaryData.fromString("{\"type\":\"Point\",\"coordinates\":[-73.99,40.71]}").toObject(Geometry.class);
        Assertions.assertEquals(GeometryType.POINT, point.getType());
        Assertions.assertTrue(point instanceof Point);

        Geometry polygon
            = BinaryData.fromString("{\"type\":\"Polygon\",\"coordinates\":[[[-73.99,40.71],[-73.98,40.71],"
                + "[-73.98,40.72],[-73.99,40.71]]]}").toObject(Geometry.class);
        Assertions.assertEquals(GeometryType.POLYGON, polygon.getType());
        Assertions.assertTrue(polygon instanceof Polygon);

        Geometry multiPoint = BinaryData.fromString("{\"type\":\"MultiPoint\",\"coordinates\":[[-73.99,40.71]]}")
            .toObject(Geometry.class);
        Assertions.assertEquals(GeometryType.MULTI_POINT, multiPoint.getType());
        Assertions.assertTrue(multiPoint instanceof MultiPoint);
    }
}
