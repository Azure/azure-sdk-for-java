// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Immutable;

/**
 * Quadrangle bounding box, with coordinates specified relative to the top-left of the original image
 */
@Immutable
public final class BoundingBox {

    /**
     * The top-left coordinate of the Bounding box.
     */
    private final Point topLeft;

    /**
     * The top-right coordinate of the Bounding box.
     */
    private final Point topRight;

    /**
     * The bottom-right coordinate of the Bounding box.
     */
    private final Point bottomRight;

    /**
     * The bottom-left coordinate of the Bounding box.
     */
    private final Point bottomLeft;

    /**
     * Constructs a Bounding box object.
     *
     * @param topLeft The top-left coordinate of the Bounding box.
     * @param topRight The top-right coordinate of the Bounding box.
     * @param bottomRight The bottom right coordinate of the Bounding box.
     * @param bottomLeft The bottom-left coordinate of the Bounding box.
     */
    public BoundingBox(final Point topLeft, final Point topRight, final Point bottomRight, final Point bottomLeft) {
        this.topLeft = topLeft;
        this.topRight = topRight;
        this.bottomRight = bottomRight;
        this.bottomLeft = bottomLeft;
    }

    /**
     * Gets the top left coordinate of the Bounding box.
     *
     * @return The top left coordinate of the Bounding box.
     */
    public Point getTopLeft() {
        return this.topLeft;
    }

    /**
     * Gets the top right coordinate of the Bounding box.
     *
     * @return The top tight coordinate of the Bounding box.
     */
    public Point getTopRight() {
        return this.topRight;
    }

    /**
     * Gets the bottom right coordinate of the Bounding box.
     *
     * @return The bottom right coordinate of the Bounding box.
     */
    public Point getBottomRight() {
        return this.bottomRight;
    }

    /**
     * Gets the bottom left coordinate of the Bounding box.
     *
     * @return The bottom left coordinate of the Bounding box.
     */
    public Point getBottomLeft() {
        return this.bottomLeft;
    }
}
