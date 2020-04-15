// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;

/**
 * The FormPage model.
 */
@Immutable
public final class FormPage {

    /*
     * The height of the image/PDF in pixels/inches, respectively.
     */
    private final float height;

    /*
     * When includeTextDetails is set to true, a list of recognized text lines.
     */
    private final IterableStream<FormLine> lines;

    /*
     * List of data tables extracted from the page.
     */
    private final IterableStream<FormTable> tables;

    /*
     * The general orientation of the text in clockwise direction, measured in
     * degrees between (-180, 180].
     */
    private final float textAngle;

    /*
     * The unit used by the width, height and boundingBox properties. For
     * images, the unit is "pixel". For PDF, the unit is "inch".
     */
    private final DimensionUnit unit;

    /*
     * The width of the image/PDF in pixels/inches, respectively.
     */
    private final float width;

    /**
     * Constructs a FormPage object.
     *
     * @param height The height of the image/PDF in pixels/inches, respectively.
     * @param textAngle The general orientation of the text in clockwise direction.
     * @param unit The unit used by the width, height and boundingBox properties.
     * @param width The width of the image/PDF in pixels/inches, respectively.
     * @param lines When includeTextDetails is set to true, a list of recognized text lines.
     * @param tables List of data tables extracted from the page.
     */
    public FormPage(final float height, final float textAngle, final DimensionUnit unit,
        final float width, final IterableStream<FormLine> lines, final IterableStream<FormTable> tables) {
        this.height = height;
        this.textAngle = textAngle;
        this.unit = unit;
        this.width = width;
        this.lines = IterableStream.of(lines);
        this.tables = IterableStream.of(tables);
    }

    /**
     * Get the height property: The height of the image/PDF in pixels/inches,
     * respectively.
     *
     * @return the height value.
     */
    public float getHeight() {
        return this.height;
    }

    /**
     * Get the lines property: When includeTextDetails is set to true, a list
     * of recognized text lines.
     *
     * @return the lines value.
     */
    public IterableStream<FormLine> getLines() {
        return this.lines;
    }

    /**
     * Get the tables property: List of data tables extracted from the page.
     *
     * @return the tables value.
     */
    public IterableStream<FormTable> getTables() {
        return this.tables;
    }

    /**
     * Get the text angle property.
     *
     * @return the text angle value.
     */
    public float getTextAngle() {
        return this.textAngle;
    }

    /**
     * Get the unit property: The unit used by the width, height and
     * boundingBox properties. For images, the unit is "pixel". For PDF, the
     * unit is "inch".
     *
     * @return the unit value.
     */
    public DimensionUnit getUnit() {
        return this.unit;
    }

    /**
     * Get the width property: The width of the image/PDF in pixels/inches,
     * respectively.
     *
     * @return the width value.
     */
    public float getWidth() {
        return this.width;
    }
}

