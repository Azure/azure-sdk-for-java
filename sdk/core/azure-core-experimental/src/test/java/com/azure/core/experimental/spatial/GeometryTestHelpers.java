// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.spatial;

import java.util.Arrays;
import java.util.Collections;
import java.util.function.Supplier;

/**
 * Helper methods when running geometry tests.
 */
public class GeometryTestHelpers {
    public static final Supplier<GeometryPosition> PIKES_PLACE_POSITION = () ->
        new GeometryPosition(-122.342148, 47.609657);

    public static final Supplier<GeometryBoundingBox> PIKES_PLACE_BOUNDING_BOX = () ->
        new GeometryBoundingBox(-122.349408, 47.604448, -122.334368, 47.610870);

    public static final Supplier<GeometryPosition> MT_RAINIER_POSITION = () ->
        new GeometryPosition(-121.726906, 46.879967, 4392D);

    public static final Supplier<GeometryBoundingBox> MT_RAINIER_BOUNDING_BOX = () ->
        new GeometryBoundingBox(-121.993999, 46.713820, -121.528334, 47.035690, 0D, 4500D);

    public static final Supplier<LineGeometry> TRIANGLE_LINE = () ->
        new LineGeometry(Arrays.asList(
            new GeometryPosition(0, 0), new GeometryPosition(1, 1),
            new GeometryPosition(0, 1), new GeometryPosition(0, 0)
        ));

    public static final Supplier<PolygonGeometry> TRIANGLE_POLYGON = () ->
        new PolygonGeometry(Collections.singletonList(TRIANGLE_LINE.get()));

    public static final Supplier<LineGeometry> SQUARE_LINE = () ->
        new LineGeometry(Arrays.asList(
            new GeometryPosition(0, 0), new GeometryPosition(0, 1), new GeometryPosition(1, 1),
            new GeometryPosition(1, 0), new GeometryPosition(0, 0)
        ));

    public static final Supplier<PolygonGeometry> SQUARE_POLYGON = () ->
        new PolygonGeometry(Collections.singletonList(SQUARE_LINE.get()));

    public static final Supplier<LineGeometry> RECTANGLE_LINE = () ->
        new LineGeometry(Arrays.asList(
            new GeometryPosition(0, 0), new GeometryPosition(0, 2), new GeometryPosition(1, 2),
            new GeometryPosition(1, 0), new GeometryPosition(0, 0)
        ));

    public static final Supplier<PolygonGeometry> RECTANGLE_POLYGON = () ->
        new PolygonGeometry(Collections.singletonList(RECTANGLE_LINE.get()));
}
