// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

public class PageInfo {
    private final TextLanguage language;
    private final double pageHeight;
    private final int pageNumber;
    private final double pageWidth;
    private final double textAngle;
    private final DimensionUnit unit;

    public PageInfo(final TextLanguage language, final double pageHeight, final int pageNumber, final double pageWidth, final double textAngle,
        final DimensionUnit unit) {
        this.language = language;
        this.pageHeight = pageHeight;
        this.pageNumber = pageNumber;
        this.pageWidth = pageWidth;
        this.textAngle = textAngle;
        this.unit = unit;
    }

    public TextLanguage getLanguage() {
        return language;
    }

    public double getPageHeight() {
        return pageHeight;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public double getPageWidth() {
        return pageWidth;
    }

    public double getTextAngle() {
        return textAngle;
    }

    public DimensionUnit getUnit() {
        return unit;
    }
}
