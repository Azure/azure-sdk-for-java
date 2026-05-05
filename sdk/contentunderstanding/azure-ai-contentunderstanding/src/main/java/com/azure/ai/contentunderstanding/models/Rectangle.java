// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.models;

import com.azure.core.annotation.Immutable;
import java.util.Objects;

/**
 * Represents an axis-aligned rectangle with integer coordinates.
 * Used by {@link AudioVisualSource} as the bounding box for spatial information (e.g., face detection).
 */
@Immutable
public final class Rectangle {
    private final int x;
    private final int y;
    private final int width;
    private final int height;

    /**
     * Creates a new {@link Rectangle}.
     *
     * @param x The x-coordinate of the top-left corner.
     * @param y The y-coordinate of the top-left corner.
     * @param width The width of the rectangle.
     * @param height The height of the rectangle.
     */
    public Rectangle(int x, int y, int width, int height) {
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
    public int getX() {
        return x;
    }

    /**
     * Gets the y-coordinate of the top-left corner.
     *
     * @return The y-coordinate.
     */
    public int getY() {
        return y;
    }

    /**
     * Gets the width of the rectangle.
     *
     * @return The width.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Gets the height of the rectangle.
     *
     * @return The height.
     */
    public int getHeight() {
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
        if (!(obj instanceof Rectangle)) {
            return false;
        }
        Rectangle other = (Rectangle) obj;
        return x == other.x && y == other.y && width == other.width && height == other.height;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, width, height);
    }
}
