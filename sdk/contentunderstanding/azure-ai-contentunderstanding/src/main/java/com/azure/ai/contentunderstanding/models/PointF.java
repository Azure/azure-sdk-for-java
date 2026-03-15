// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.models;

import com.azure.core.annotation.Immutable;
import java.util.Objects;

/**
 * Represents a point with float-precision x and y coordinates.
 * Used by {@link DocumentSource} to define polygon vertices in document coordinate space.
 */
@Immutable
public final class PointF {
    private final float x;
    private final float y;

    /**
     * Creates a new {@link PointF}.
     *
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     */
    public PointF(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Gets the x-coordinate.
     *
     * @return The x-coordinate.
     */
    public float getX() {
        return x;
    }

    /**
     * Gets the y-coordinate.
     *
     * @return The y-coordinate.
     */
    public float getY() {
        return y;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PointF)) {
            return false;
        }
        PointF other = (PointF) obj;
        return Float.compare(x, other.x) == 0 && Float.compare(y, other.y) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
