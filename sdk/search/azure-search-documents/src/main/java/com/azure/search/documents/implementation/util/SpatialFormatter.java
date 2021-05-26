// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.util;

import com.azure.core.models.GeoLineString;
import com.azure.core.models.GeoLinearRing;
import com.azure.core.models.GeoPoint;
import com.azure.core.models.GeoPolygon;
import com.azure.core.models.GeoPosition;
import com.azure.core.util.logging.ClientLogger;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Objects;

/**
 * Helper class containing methods which encode geographic types for use in OData filters.
 */
public final class SpatialFormatter {
    private static final DecimalFormat COORDINATE_FORMATTER = new DecimalFormat();

    /*
     * This is the maximum length of a longitude-latitude pair in a geography OData expression.
     *
     * Each double is allowed 17 characters, 15 digits of precision, 1 digit for a decimal, and 1 digit for a sign, and
     * 1 character for the space between the pair.
     */
    private static final int LONGITUDE_LATITUDE_MAX_LENGTH = 2 * 17 + 1;

    /*
     * The length of the point OData expression identifier.
     */
    private static final int POINT_EXPRESSION_IDENTIFIER_LENGTH = "geography'POINT()".length();

    private static final int POLYGON_EXPRESSION_IDENTIFIER_LENGTH = "geography'POLYGON(())".length();

    /**
     * Encodes a {@link GeoPoint} into an OData expression.
     *
     * @param longitude Longitude of the point.
     * @param latitude Latitude of the point.
     * @return An OData expression representing the {@link GeoPoint}.
     */
    public static String encodePoint(double longitude, double latitude) {
        StringBuilder builder = new StringBuilder(POINT_EXPRESSION_IDENTIFIER_LENGTH + LONGITUDE_LATITUDE_MAX_LENGTH);

        return addPoint(builder.append("geography'POINT("), longitude, latitude)
            .append(")'")
            .toString();
    }

    /**
     * Encodes a closed {@link GeoLineString} into an OData expression.
     * <p>
     * The {@link GeoLineString} is expected to contain at least four points and the first and last points have the same
     * longitudinal and latitudinal values.
     *
     * @param line The {@link GeoLineString}.
     * @param logger A logger that will log any exceptions thrown.
     * @return An OData expression representing the {@link GeoLineString}.
     * @throws NullPointerException If {@code line} is null.
     * @throws IllegalArgumentException If the {@link GeoLineString} contains less than four points and the first and
     * last points don't use the same longitudinal and latitudinal values.
     */
    public static String encodePolygon(GeoLineString line, ClientLogger logger) {
        Objects.requireNonNull(line, "'line' cannot be null.");

        List<GeoPosition> coordinates = line.getCoordinates();
        if (coordinates.size() < 4) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                "'line' must have at least four coordinates to form a searchable polygon."));
        }

        if (!Objects.equals(coordinates.get(0), coordinates.get(coordinates.size() - 1))) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                "'line' must have matching first and last coordinates to form a searchable polygon."));
        }

        return encodePolygon(coordinates);
    }

    /**
     * Encodes a {@link GeoPolygon} into an OData expression.
     * <p>
     * The {@link GeoPolygon} is expected to contain a single {@link GeoLinearRing} representing it.
     *
     * @param polygon The {@link GeoPolygon}.
     * @param logger A logger that will log any exceptions thrown.
     * @return An OData expression representing the {@link GeoPolygon}.
     * @throws NullPointerException If {@code polygon} is null.
     * @throws IllegalArgumentException If the {@link GeoPolygon} is represented by multiple {@link GeoLinearRing
     * GeoLinearRings}.
     */
    public static String encodePolygon(GeoPolygon polygon, ClientLogger logger) {
        Objects.requireNonNull(polygon, "'polygon' cannot be null.");

        if (polygon.getRings().size() != 1) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                "'polygon' must have exactly one ring to form a searchable polygon."));
        }

        return encodePolygon(polygon.getOuterRing().getCoordinates());
    }

    private static String encodePolygon(List<GeoPosition> ring) {
        int approximateODataExpressionSize = POLYGON_EXPRESSION_IDENTIFIER_LENGTH
            + ring.size() * LONGITUDE_LATITUDE_MAX_LENGTH
            + ring.size();

        StringBuilder builder = new StringBuilder(approximateODataExpressionSize)
            .append("geography'POLYGON((");

        boolean first = true;
        for (GeoPosition position : ring) {
            if (!first) {
                builder.append(",");
            } else {
                first = false;
            }

            addPoint(builder, position.getLongitude(), position.getLatitude());
        }

        return builder.append("))'")
            .toString();
    }

    /*
     * This method is synchronized as DecimalFormat is NOT thread-safe.
     */
    private static synchronized StringBuilder addPoint(StringBuilder builder, double longitude, double latitude) {
        return builder.append(COORDINATE_FORMATTER.format(longitude))
            .append(' ')
            .append(COORDINATE_FORMATTER.format(latitude));
    }
}
