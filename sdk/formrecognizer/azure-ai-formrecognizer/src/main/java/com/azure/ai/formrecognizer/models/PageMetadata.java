// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Immutable;

/**
 * The PageMetadata model.
 */
@Immutable
public final class PageMetadata {
    /*
     * The detected language on the page overall.
     */
    private final TextLanguage language;

    /*
     * The height of the image/PDF in pixels/inches, respectively.
     */
    private final double pageHeight;

    /*
     * The 1-based page number in the input document.
     */
    private final int pageNumber;

    /*
     * The width of the image/PDF in pixels/inches, respectively.
     */
    private final double pageWidth;

    /*
     * The orientation of the text in clockwise direction, measured in
     * degrees between (-180, 180].
     */
    private final double textAngle;

    /*
     * The unit used by the width, height and boundingBox properties. For
     * images, the unit is "pixel". For PDF, the unit is "inch".
     */
    private final DimensionUnit unit;

    /**
     * Constructs a PageMetadata model.
     *
     * @param language The detected language on the page overall.
     * @param pageHeight The height of the image/PDF in pixels/inches, respectively.
     * @param pageNumber The 1-based page number in the input document.
     * @param pageWidth The width of the image/PDF in pixels/inches, respectively.
     * @param textAngle The orientation of the text in clockwise direction, measured in degrees between (-180, 180].
     * @param unit The unit used by the width, height and boundingBox properties.
     */
    public PageMetadata(final TextLanguage language, final double pageHeight, final int pageNumber,
                        final double pageWidth, final double textAngle,
                        final DimensionUnit unit) {
        this.language = language;
        this.pageHeight = pageHeight;
        this.pageNumber = pageNumber;
        this.pageWidth = pageWidth;
        this.textAngle = textAngle;
        this.unit = unit;
    }

    /**
     * Get the language property: The detected language on the page overall.
     *
     * @return the language value.
     */
    public TextLanguage getLanguage() {
        return language;
    }

    /**
     * Get the height property: The height of the image/PDF in pixels/inches,
     * respectively.
     *
     * @return the height value.
     */
    public double getPageHeight() {
        return pageHeight;
    }

    /**
     * Get the 1-based page number in the input document.
     *
     * @return the page value.
     */
    public int getPageNumber() {
        return pageNumber;
    }

    /**
     * Get the width property: The width of the image/PDF in pixels/inches,
     * respectively.
     *
     * @return the width value.
     */
    public double getPageWidth() {
        return pageWidth;
    }

    /**
     * Get the angle property: The general orientation of the text in clockwise
     * direction, measured in degrees between (-180, 180].
     *
     * @return the angle value.
     */
    public double getTextAngle() {
        return textAngle;
    }

    /**
     * Get the unit property: The unit used by the width, height and
     * boundingBox properties. For images, the unit is "pixel". For PDF, the
     * unit is "inch".
     *
     * @return the unit value.
     */
    public DimensionUnit getUnit() {
        return unit;
    }
}
