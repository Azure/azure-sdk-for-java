// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.models;

import com.azure.core.annotation.Immutable;
import java.util.Objects;

/**
 * Represents an axis-aligned rectangle with float-precision coordinates.
 * Used by {@link DocumentSource} as the bounding box computed from polygon coordinates.
 */
@Immutable
public final class RectangleF {
    private final float x;
    private final float y;
    private final float width;
    private final float height;

    /**
     * Creates a new {@link RectangleF}.
     *
     * @param x The x-coordinate of the top-left corner.
     * @param y The y-coordinate of the top-left corner.
     * @param width The width of the rectangle.
     * @param height The height of the rectangle.
     */
    public RectangleF(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Gets the x-coordinate of the top-left corner.
     *
     * @return The x-coordinate.
     */
    public float getX() {
        return x;
    }

    /**
     * Gets the y-coordinate of the top-left corner.
     *
     * @return The y-coordinate.
     */
    public float getY() {
        return y;
    }

    /**
     * Gets the width of the rectangle.
     *
     * @return The width.
     */
    public float getWidth() {
        return width;
    }

    /**
     * Gets the height of the rectangle.
     *
     * @return The height.
     */
    public float getHeight() {
        return height;
    }

    @Override
    public String toString() {
        return "[x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof RectangleF)) {
            return false;
        }
        RectangleF other = (RectangleF) obj;
        return Float.compare(x, other.x) == 0
            && Float.compare(y, other.y) == 0
            && Float.compare(width, other.width) == 0
            && Float.compare(height, other.height) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, width, height);
    }
}
