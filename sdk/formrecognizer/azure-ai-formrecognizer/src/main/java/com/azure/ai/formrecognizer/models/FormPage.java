// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Immutable;

import java.util.Collections;
import java.util.List;

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
     * When includeFieldElements is set to true, a list of recognized text lines.
     */
    private final List<FormLine> lines;

    /*
     * List of data tables extracted from the page.
     */
    private final List<FormTable> tables;

    /*
     * The general orientation of the text in clockwise direction, measured in
     * degrees between (-180, 180].
     */
    private final float textAngle;

    /*
     * The unit used by the width, height and boundingBox properties. For
     * images, the unit is "pixel". For PDF, the unit is "inch".
     */
    private final LengthUnit unit;

    /*
     * The width of the image/PDF in pixels/inches, respectively.
     */
    private final float width;

    /*
     * The 1 based page number.
     */
    private final Integer pageNumber;

    /**
     * Constructs a FormPage object.
     *
     * @param height The height of the image/PDF in pixels/inches, respectively.
     * @param textAngle The general orientation of the text in clockwise direction.
     * @param unit The unit used by the width, height and boundingBox properties.
     * @param width The width of the image/PDF in pixels/inches, respectively.
     * @param lines When includeFieldElements is set to true, a list of recognized text lines.
     * @param tables List of data tables extracted from the page.
     * @param pageNumber the 1-based page number in the input document.
     */
    public FormPage(final float height, final float textAngle, final LengthUnit unit,
        final float width, final List<FormLine> lines, final List<FormTable> tables, final int pageNumber) {
        this.height = height;
        this.textAngle = textAngle > 180 ? textAngle - 360 : textAngle;
        this.unit = unit;
        this.width = width;
        this.lines = lines == null ? null : Collections.unmodifiableList(lines);
        this.tables = tables == null ? null : Collections.unmodifiableList(tables);
        this.pageNumber = pageNumber;
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
     * Get the lines property: When includeFieldElements is set to true, a list
     * of recognized text lines.
     *
     * @return the unmodifiable list of recognized lines.
     */
    public List<FormLine> getLines() {
        return this.lines;
    }

    /**
     * Get the tables property: List of data tables extracted from the page.
     *
     * @return the unmodifiable list of recognized tables.
     */
    public List<FormTable> getTables() {
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
    public LengthUnit getUnit() {
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

    /**
     * Get the 1-based page number in the input document.
     *
     * @return the page number value.
     */
    public Integer getPageNumber() {
        return this.pageNumber;
    }
}

