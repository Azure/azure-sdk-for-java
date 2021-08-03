// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.http.HttpMethod;
import com.azure.core.models.GeoBoundingBox;
import com.azure.core.models.GeoLineString;
import com.azure.core.models.GeoLinearRing;
import com.azure.core.models.GeoPoint;
import com.azure.core.models.GeoPolygon;
import com.azure.core.models.GeoPosition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests {@link SearchFilter}.
 */
public class SearchFilterTests {
    @Test
    public void noArguments() {
        assertEquals("Foo eq 2", SearchFilter.create("Foo eq 2"));
    }

    @Test
    public void oneArgument() {
        String actual = SearchFilter.create("Foo eq %d", 2);
        assertEquals("Foo eq 2", actual);
    }

    @ParameterizedTest
    @MethodSource("manyArgumentsSupplier")
    public void manyArguments(String expected, String formattableString, Object[] args) {
        assertEquals(expected, SearchFilter.create(formattableString, args));
    }

    private static Stream<Arguments> manyArgumentsSupplier() {
        return Stream.of(
            Arguments.of("Foo eq 2 and Bar eq 3", "Foo eq %d and Bar eq %d", new Object[]{2, 3}),
            Arguments.of("Foo eq 2 and Bar eq 3 and Baz eq 4", "Foo eq %d and Bar eq %d and Baz eq %d",
                new Object[]{2, 3, 4}),
            Arguments.of("Foo eq 2 and Bar eq 3 and Baz eq 4 and Qux eq 5",
                "Foo eq %d and Bar eq %d and Baz eq %d and Qux eq %d", new Object[]{2, 3, 4, 5}),
            Arguments.of("Foo eq 2 and Bar eq 3 and Baz eq 4 and Qux eq 5 and Quux eq 6",
                "Foo eq %d and Bar eq %d and Baz eq %d and Qux eq %d and Quux eq %d", new Object[]{2, 3, 4, 5, 6})
        );
    }

    @Test
    public void nullArgument() {
        assertEquals("Foo eq null", SearchFilter.create("Foo eq %s", new Object[]{null}));
    }

    @Test
    public void booleanArgument() {
        assertEquals("Foo eq true", SearchFilter.create("Foo eq %b", true));
        assertEquals("Foo eq false", SearchFilter.create("Foo eq %b", false));
        assertEquals("Foo eq false", SearchFilter.create("Foo eq %b", (Boolean) null));
    }

    @ParameterizedTest
    @MethodSource("numberArgumentSupplier")
    public void numberArgument(String expected, String formattableString, Object arg) {
        assertEquals(expected, SearchFilter.create(formattableString, arg));
    }

    private static Stream<Arguments> numberArgumentSupplier() {
        return Stream.of(
            Arguments.of("Foo eq 0", "Foo eq %d", (byte) 0),
            Arguments.of("Foo eq -2", "Foo eq %d", (byte) -2),
            Arguments.of("Foo eq 2", "Foo eq %d", (byte) 2),

            Arguments.of("Foo eq 0", "Foo eq %d", Byte.valueOf("0")),
            Arguments.of("Foo eq -2", "Foo eq %d", Byte.valueOf("-2")),
            Arguments.of("Foo eq 2", "Foo eq %d", Byte.valueOf("2")),

            Arguments.of("Foo eq 0", "Foo eq %d", (short) 0),
            Arguments.of("Foo eq -2", "Foo eq %d", (short) -2),
            Arguments.of("Foo eq 2", "Foo eq %d", (short) 2),

            Arguments.of("Foo eq 0", "Foo eq %d", Short.valueOf("0")),
            Arguments.of("Foo eq -2", "Foo eq %d", Short.valueOf("-2")),
            Arguments.of("Foo eq 2", "Foo eq %d", Short.valueOf("2")),

            Arguments.of("Foo eq 0", "Foo eq %d", 0),
            Arguments.of("Foo eq -2", "Foo eq %d", -2),
            Arguments.of("Foo eq 2", "Foo eq %d", 2),

            Arguments.of("Foo eq 0", "Foo eq %d", Integer.valueOf("0")),
            Arguments.of("Foo eq -2", "Foo eq %d", Integer.valueOf("-2")),
            Arguments.of("Foo eq 2", "Foo eq %d", Integer.valueOf("2")),

            Arguments.of("Foo eq 0", "Foo eq %d", 0L),
            Arguments.of("Foo eq -2", "Foo eq %d", -2L),
            Arguments.of("Foo eq 2", "Foo eq %d", 2L),

            Arguments.of("Foo eq 0", "Foo eq %d", Long.valueOf("0")),
            Arguments.of("Foo eq -2", "Foo eq %d", Long.valueOf("-2")),
            Arguments.of("Foo eq 2", "Foo eq %d", Long.valueOf("2")),

            Arguments.of("Foo eq 0", "Foo eq %.0f", 0F),
            Arguments.of("Foo eq -2", "Foo eq %.0f", -2F),
            Arguments.of("Foo eq 2", "Foo eq %.0f", 2F),

            Arguments.of("Foo eq 0", "Foo eq %.0f", Float.valueOf("0")),
            Arguments.of("Foo eq -2", "Foo eq %.0f", Float.valueOf("-2")),
            Arguments.of("Foo eq 2", "Foo eq %.0f", Float.valueOf("2")),

            Arguments.of("Foo eq 0", "Foo eq %.0f", 0D),
            Arguments.of("Foo eq -2", "Foo eq %.0f", -2D),
            Arguments.of("Foo eq 2", "Foo eq %.0f", 2D),

            Arguments.of("Foo eq 0", "Foo eq %.0f", Double.valueOf("0")),
            Arguments.of("Foo eq -2", "Foo eq %.0f", Double.valueOf("-2")),
            Arguments.of("Foo eq 2", "Foo eq %.0f", Double.valueOf("2"))
        );
    }

    @Test
    public void decimalArgument() {
        assertEquals("Foo eq 2.5", SearchFilter.create("Foo eq %.1f", 2.5F));
        assertEquals("Foo eq 2.5", SearchFilter.create("Foo eq %.1f", 2.5D));
    }

    @Test
    public void exponentArgument() {
        assertEquals("Foo eq 2.5e+10", SearchFilter.create("Foo eq %.1e", 2.5e10F));
        assertEquals("Foo eq 2.5e+10", SearchFilter.create("Foo eq %.1e", 2.5e10D));
    }

    @ParameterizedTest
    @MethodSource("limitArgumentSupplier")
    public void limitArgument(String expected, String formattableString, Object arg) {
        assertEquals(expected, SearchFilter.create(formattableString, arg));
    }

    private static Stream<Arguments> limitArgumentSupplier() {
        return Stream.of(
            Arguments.of("Foo eq NaN", "Foo eq %s", Float.NaN),
            Arguments.of("Foo eq INF", "Foo eq %s", Float.POSITIVE_INFINITY),
            Arguments.of("Foo eq -INF", "Foo eq %s", Float.NEGATIVE_INFINITY),

            Arguments.of("Foo eq NaN", "Foo eq %s", Double.NaN),
            Arguments.of("Foo eq INF", "Foo eq %s", Double.POSITIVE_INFINITY),
            Arguments.of("Foo eq -INF", "Foo eq %s", Double.NEGATIVE_INFINITY)
        );
    }

    @Test
    public void dateArgument() {
        assertEquals("Foo eq 1912-06-23T11:59:59Z", SearchFilter.create("Foo eq %s",
            Date.from(OffsetDateTime.of(1912, 6, 23, 11, 59, 59, 0, ZoneOffset.UTC).toInstant())));
        assertEquals("Foo eq 1912-06-23T11:59:59Z",
            SearchFilter.create("Foo eq %s", OffsetDateTime.of(1912, 6, 23, 11, 59, 59, 0, ZoneOffset.UTC)));
    }

    @ParameterizedTest
    @MethodSource("textArgumentSupplier")
    public void textArgument(String expected, String formattableString, Object arg) {
        assertEquals(expected, SearchFilter.create(formattableString, arg));
    }

    @SuppressWarnings("UnnecessaryBoxing")
    private static Stream<Arguments> textArgumentSupplier() {
        return Stream.of(
            Arguments.of("Foo eq 'x'", "Foo eq %s", 'x'),
            Arguments.of("Foo eq ''''", "Foo eq %s", '\''),
            Arguments.of("Foo eq '\"'", "Foo eq %s", '"'),

            Arguments.of("Foo eq 'x'", "Foo eq %s", Character.valueOf('x')),
            Arguments.of("Foo eq ''''", "Foo eq %s", Character.valueOf('\'')),
            Arguments.of("Foo eq '\"'", "Foo eq %s", Character.valueOf('\"')),

            Arguments.of("Foo eq 'bar'", "Foo eq %s", "bar"),
            Arguments.of("Foo eq 'bar''s'", "Foo eq %s", "bar's"),
            Arguments.of("Foo eq '\"bar\"'", "Foo eq %s", "\"bar\""),

            Arguments.of("Foo eq 'bar'", "Foo eq %s", new StringBuilder("bar")),
            Arguments.of("Foo eq 'bar''s'", "Foo eq %s", new StringBuilder("bar's")),
            Arguments.of("Foo eq '\"bar\"'", "Foo eq %s", new StringBuilder("\"bar\""))
        );
    }

    @Test
    public void unknownTypeThrows() {
        assertThrows(IllegalArgumentException.class, () -> SearchFilter.create("Foo eq %s", HttpMethod.GET));
    }

    @ParameterizedTest
    @MethodSource("geographyArgumentSupplier")
    public void geographyArgument(Object geography, String formattableString, String expected) {
        assertEquals(expected, SearchFilter.create(formattableString, geography));
    }

    private static Stream<Arguments> geographyArgumentSupplier() {
        final String formattableString = "Foo eq %s";

        final String expectedPointFilter = "Foo eq geography'POINT(0 0)'";
        final String expectedPolygonFilter = "Foo eq geography'POLYGON((0 0,0 1,1 1,0 0))'";

        final List<GeoPosition> polygonCoordinates = Arrays.asList(new GeoPosition(0D, 0D), new GeoPosition(0D, 1D),
            new GeoPosition(1D, 1D), new GeoPosition(0D, 0D));

        final List<GeoPosition> polygonCoordinatesWithAltitude = Arrays.asList(new GeoPosition(0D, 0D, 1D),
            new GeoPosition(0D, 1D, 1D), new GeoPosition(1D, 1D, 1D), new GeoPosition(0D, 0D, 1D));

        final GeoBoundingBox boundingBox = new GeoBoundingBox(-1D, -1D, 1D, 1D);

        return Stream.of(
            // GeoPosition
            Arguments.of(new GeoPosition(0D, 0D), formattableString, expectedPointFilter),
            Arguments.of(new GeoPosition(0D, 0D, 1D), formattableString, expectedPointFilter),

            // GeoPoint
            Arguments.of(new GeoPoint(0D, 0D), formattableString, expectedPointFilter),
            Arguments.of(new GeoPoint(0D, 0D, 1D), formattableString, expectedPointFilter),
            Arguments.of(new GeoPoint(new GeoPosition(0D, 0D)), formattableString, expectedPointFilter),
            Arguments.of(new GeoPoint(new GeoPosition(0D, 0D, 1D)), formattableString, expectedPointFilter),
            Arguments.of(new GeoPoint(new GeoPosition(0D, 0D), boundingBox, Collections.emptyMap()), formattableString,
                expectedPointFilter),

            // GeoLineString
            Arguments.of(new GeoLineString(polygonCoordinates), formattableString, expectedPolygonFilter),
            Arguments.of(new GeoLineString(polygonCoordinates, boundingBox, Collections.emptyMap()), formattableString,
                expectedPolygonFilter),
            Arguments.of(new GeoLineString(polygonCoordinatesWithAltitude), formattableString, expectedPolygonFilter),
            Arguments.of(new GeoLineString(polygonCoordinatesWithAltitude, boundingBox, Collections.emptyMap()),
                formattableString, expectedPolygonFilter),

            // GeoPolygon
            Arguments.of(new GeoPolygon(new GeoLinearRing(polygonCoordinates)), formattableString,
                expectedPolygonFilter),
            Arguments.of(new GeoPolygon(new GeoLinearRing(polygonCoordinates), boundingBox, Collections.emptyMap()),
                formattableString, expectedPolygonFilter),
            Arguments.of(new GeoPolygon(new GeoLinearRing(polygonCoordinatesWithAltitude)), formattableString,
                expectedPolygonFilter),
            Arguments.of(new GeoPolygon(new GeoLinearRing(polygonCoordinatesWithAltitude), boundingBox,
                Collections.emptyMap()), formattableString, expectedPolygonFilter),

            Arguments.of(new GeoPolygon(Collections.singletonList(new GeoLinearRing(polygonCoordinates))),
                formattableString, expectedPolygonFilter),
            Arguments.of(new GeoPolygon(Collections.singletonList(new GeoLinearRing(polygonCoordinates)), boundingBox,
                Collections.emptyMap()), formattableString, expectedPolygonFilter),
            Arguments.of(new GeoPolygon(Collections.singletonList(new GeoLinearRing(polygonCoordinatesWithAltitude))),
                formattableString, expectedPolygonFilter),
            Arguments.of(new GeoPolygon(Collections.singletonList(new GeoLinearRing(polygonCoordinatesWithAltitude)),
                boundingBox, Collections.emptyMap()), formattableString, expectedPolygonFilter)

        );
    }
}
