// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes;

import com.azure.core.models.GeoLineString;
import com.azure.core.models.GeoLinearRing;
import com.azure.core.models.GeoPolygon;
import com.azure.core.models.GeoPosition;
import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.implementation.util.SpatialFormatter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static com.azure.search.documents.TestHelpers.createGeographyPolygon;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests {@link SpatialFormatter}.
 */
public class SpatialFormatterTests {
    private final ClientLogger logger = new ClientLogger(SpatialFormatterTests.class);

    @ParameterizedTest
    @MethodSource("encodePointSupplier")
    public void encodePoint(double longitude, double latitude, String expected) {
        assertEquals(expected, SpatialFormatter.encodePoint(longitude, latitude));
    }

    private static Stream<Arguments> encodePointSupplier() {
        final String pointFormat = "geography'POINT(%s %s)'";

        return Stream.of(
            Arguments.of(0D, 0D, String.format(pointFormat, "0", "0")),
            Arguments.of(0.0D, 0.0D, String.format(pointFormat, "0", "0")),
            Arguments.of(0.000000000000D, 0.000000000000D, String.format(pointFormat, "0", "0")),
            Arguments.of(0.01D, 0.01D, String.format(pointFormat, "0.01", "0.01")),
            Arguments.of(0.010000000000D, 0.010000000000D, String.format(pointFormat, "0.01", "0.01")),
            Arguments.of(-0D, -0D, String.format(pointFormat, "-0", "-0")),
            Arguments.of(-0.0D, -0.0D, String.format(pointFormat, "-0", "-0")),
            Arguments.of(-0.01D, -0.01D, String.format(pointFormat, "-0.01", "-0.01")),
            Arguments.of(-0.000000000000D, -0.000000000000D, String.format(pointFormat, "-0", "-0")),
            Arguments.of(-0.010000000000D, -0.010000000000D, String.format(pointFormat, "-0.01", "-0.01"))
        );
    }

    @Test
    public void geoLineStringWithLessThanFourPointsThrows() {
        GeoLineString lineString = new GeoLineString(Collections.singletonList(new GeoPosition(0, 0)));

        assertThrows(IllegalArgumentException.class, () -> SpatialFormatter.encodePolygon(lineString, logger));
    }

    @Test
    public void nonClosingGeoLineStringThrows() {
        GeoLineString lineString = new GeoLineString(Arrays.asList(new GeoPosition(0, 0), new GeoPosition(0, 1),
            new GeoPosition(0, 2), new GeoPosition(0, 3)));

        assertThrows(IllegalArgumentException.class, () -> SpatialFormatter.encodePolygon(lineString, logger));
    }

    @ParameterizedTest
    @MethodSource("encodeGeoLineStringPolygonSupplier")
    public void encodeGeoLineStringPolygon(GeoLineString lineString, String expected) {
        assertEquals(expected, SpatialFormatter.encodePolygon(lineString, logger));
    }

    private static Stream<Arguments> encodeGeoLineStringPolygonSupplier() {
        return getGeoPositionsAndStringValues()
            .stream()
            .map(positionsExpected -> {
                GeoLineString lineString = new GeoLineString(positionsExpected.getT1());

                return Arguments.of(lineString, positionsExpected.getT2());
            });
    }

    @Test
    public void multiRingPolygonThrows() {
        GeoLinearRing ring = new GeoLinearRing(Arrays.asList(new GeoPosition(0, 0), new GeoPosition(0, 1),
            new GeoPosition(1, 1), new GeoPosition(0, 0)));
        GeoPolygon multiRingPolygon = new GeoPolygon(Arrays.asList(ring, ring));

        assertThrows(IllegalArgumentException.class, () -> SpatialFormatter.encodePolygon(multiRingPolygon, logger));
    }

    @ParameterizedTest
    @MethodSource("encodeGeoPolygonPolygonSupplier")
    public void encodeGeoPolygonPolygon(GeoPolygon polygon, String expected) {
        assertEquals(expected, SpatialFormatter.encodePolygon(polygon, logger));
    }

    private static Stream<Arguments> encodeGeoPolygonPolygonSupplier() {
        return getGeoPositionsAndStringValues()
            .stream()
            .map(positionsExpected -> {
                GeoPolygon polygon = new GeoPolygon(new GeoLinearRing(positionsExpected.getT1()));

                return Arguments.of(polygon, positionsExpected.getT2());
            });
    }

    private static List<Tuple2<List<GeoPosition>, String>> getGeoPositionsAndStringValues() {
        List<GeoPosition> noDecimalCoordinates = Arrays.asList(new GeoPosition(0, 0), new GeoPosition(0, 1),
            new GeoPosition(1, 1), new GeoPosition(0, 0));
        String noDecimalCoordinatesString = createGeographyPolygon("0", "0", "0", "1", "1", "1", "0", "0");

        List<GeoPosition> negativeNoDecimalCoordinates = Arrays.asList(new GeoPosition(-0D, -0D),
            new GeoPosition(-0D, -1), new GeoPosition(-1, -1), new GeoPosition(-0D, -0D));
        String negativeNoDecimalCoordinatesString = createGeographyPolygon("-0", "-0", "-0", "-1", "-1", "-1", "-0",
            "-0");

        List<GeoPosition> simpleTrailingZerosCoordinates = Arrays.asList(new GeoPosition(0.0, 0.0),
            new GeoPosition(0.0, 1.0), new GeoPosition(1.0, 1.0), new GeoPosition(0.0, 0.0));
        String simpleTrailingZerosCoordinatesString = createGeographyPolygon("0", "0", "0", "1", "1", "1", "0", "0");

        List<GeoPosition> negativeSimpleTrailingZerosCoordinates = Arrays.asList(new GeoPosition(-0.0, -0.0),
            new GeoPosition(-0.0, -1.0), new GeoPosition(-1.0, -1.0), new GeoPosition(-0.0, -0.0));
        String negativeSimpleTrailingZerosCoordinatesString = createGeographyPolygon("-0", "-0", "-0", "-1", "-1", "-1",
            "-0", "-0");

        List<GeoPosition> simpleNoTrailingZerosCoordinates = Arrays.asList(new GeoPosition(0.01, 0.01),
            new GeoPosition(0.01, 1.01), new GeoPosition(1.01, 1.01), new GeoPosition(0.01, 0.01));
        String simpleNoTrailingZerosCoordinatesString = createGeographyPolygon("0.01", "0.01", "0.01", "1.01", "1.01",
            "1.01", "0.01", "0.01");

        List<GeoPosition> negativeSimpleNoTrailingZerosCoordinates = Arrays.asList(new GeoPosition(-0.01, -0.01),
            new GeoPosition(-0.01, -1.01), new GeoPosition(-1.01, -1.01), new GeoPosition(-0.01, -0.01));
        String negativeSimpleNoTrailingZerosCoordinatesString = createGeographyPolygon("-0.01", "-0.01", "-0.01",
            "-1.01", "-1.01", "-1.01", "-0.01", "-0.01");

        List<GeoPosition> manyTrailingZerosCoordinates = Arrays.asList(new GeoPosition(0.000000000000, 0.000000000000),
            new GeoPosition(0.000000000000, 1.000000000000), new GeoPosition(1.000000000000, 1.000000000000),
            new GeoPosition(0.000000000000, 0.000000000000));
        String manyTrailingZerosCoordinatesString = createGeographyPolygon("0", "0", "0", "1", "1", "1", "0", "0");

        List<GeoPosition> negativeManyTrailingZerosCoordinates = Arrays.asList(
            new GeoPosition(-0.000000000000, -0.000000000000), new GeoPosition(-0.000000000000, -1.000000000000),
            new GeoPosition(-1.000000000000, -1.000000000000), new GeoPosition(-0.000000000000, -0.000000000000));
        String negativeManyTrailingZerosCoordinatesString = createGeographyPolygon("-0", "-0", "-0", "-1", "-1", "-1",
            "-0", "-0");

        List<GeoPosition> complexTrailingZerosCoordinates = Arrays.asList(
            new GeoPosition(0.010000000000, 0.010000000000), new GeoPosition(0.010000000000, 1.010000000000),
            new GeoPosition(1.010000000000, 1.010000000000), new GeoPosition(0.010000000000, 0.010000000000));
        String complexTrailingZerosCoordinatesString = createGeographyPolygon("0.01", "0.01", "0.01", "1.01", "1.01",
            "1.01", "0.01", "0.01");

        List<GeoPosition> negativeComplexTrailingZerosCoordinates = Arrays.asList(
            new GeoPosition(-0.010000000000, -0.010000000000), new GeoPosition(-0.010000000000, -1.010000000000),
            new GeoPosition(-1.010000000000, -1.010000000000), new GeoPosition(-0.010000000000, -0.010000000000));
        String negativeComplexTrailingZerosCoordinatesString = createGeographyPolygon("-0.01", "-0.01", "-0.01",
            "-1.01", "-1.01", "-1.01", "-0.01", "-0.01");

        return Arrays.asList(
            Tuples.of(noDecimalCoordinates, noDecimalCoordinatesString),
            Tuples.of(negativeNoDecimalCoordinates, negativeNoDecimalCoordinatesString),
            Tuples.of(simpleTrailingZerosCoordinates, simpleTrailingZerosCoordinatesString),
            Tuples.of(negativeSimpleTrailingZerosCoordinates, negativeSimpleTrailingZerosCoordinatesString),
            Tuples.of(simpleNoTrailingZerosCoordinates, simpleNoTrailingZerosCoordinatesString),
            Tuples.of(negativeSimpleNoTrailingZerosCoordinates, negativeSimpleNoTrailingZerosCoordinatesString),
            Tuples.of(manyTrailingZerosCoordinates, manyTrailingZerosCoordinatesString),
            Tuples.of(negativeManyTrailingZerosCoordinates, negativeManyTrailingZerosCoordinatesString),
            Tuples.of(complexTrailingZerosCoordinates, complexTrailingZerosCoordinatesString),
            Tuples.of(negativeComplexTrailingZerosCoordinates, negativeComplexTrailingZerosCoordinatesString)
        );
    }
}
