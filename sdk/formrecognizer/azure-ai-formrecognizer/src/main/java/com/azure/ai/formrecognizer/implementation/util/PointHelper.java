// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation.util;

import com.azure.ai.formrecognizer.models.BoundingRegion;
import com.azure.ai.formrecognizer.models.Point;

/**
 * The helper class to set the non-public properties of an {@link Point} instance.
 */
public final class PointHelper {
    private static PointAccessor accessor;

    private PointHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link BoundingRegion} instance.
     */
    public interface PointAccessor {
        void setX(Point point, float xCoordinate);
        void setY(Point point, float yCoordinate);
    }

    /**
     * The method called from {@link Point} to set it's accessor.
     *
     * @param pointAccessor The accessor.
     */
    public static void setAccessor(final PointHelper.PointAccessor pointAccessor) {
        accessor = pointAccessor;
    }

    static void setX(Point point, float xCoordinate) {
        accessor.setX(point, xCoordinate);
    }

    static void setY(Point point, float yCoordinate) {
        accessor.setY(point, yCoordinate);
    }
}
