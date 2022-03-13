// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.ai.formrecognizer.implementation.util.DocumentPageHelper;

import java.util.List;

/**
 * Content and layout elements extracted from a page from the input.
 */
public final class DocumentPage {
    /*
     * 1-based page number in the input document.
     */
    private int pageNumber;

    /*
     * The general orientation of the content in clockwise direction, measured
     * in degrees between (-180, 180].
     */
    private float angle;

    /*
     * The width of the image/PDF in pixels/inches, respectively.
     */
    private float width;

    /*
     * The height of the image/PDF in pixels/inches, respectively.
     */
    private float height;

    /*
     * The unit used by the width, height, and boundingBox properties. For
     * images, the unit is "pixel". For PDF, the unit is "inch".
     */
    private LengthUnit unit;

    /*
     * Location of the page in the reading order concatenated content.
     */
    private List<DocumentSpan> spans;

    /*
     * Extracted words from the page.
     */
    private List<DocumentWord> words;

    /*
     * Extracted selection marks from the page.
     */
    private List<DocumentSelectionMark> selectionMarks;

    /*
     * Extracted lines from the page, potentially containing both textual and
     * visual elements.
     */
    private List<DocumentLine> lines;

    /**
     * Get the pageNumber property: 1-based page number in the input document.
     *
     * @return the pageNumber value.
     */
    public int getPageNumber() {
        return this.pageNumber;
    }

    /**
     * Set the pageNumber property: 1-based page number in the input document.
     *
     * @param pageNumber the pageNumber value to set.
     * @return the DocumentPage object itself.
     */
    void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    /**
     * Get the angle property: The general orientation of the content in clockwise direction, measured in degrees
     * between (-180, 180].
     *
     * @return the angle value.
     */
    public float getAngle() {
        return this.angle;
    }

    /**
     * Set the angle property: The general orientation of the content in clockwise direction, measured in degrees
     * between (-180, 180].
     *
     * @param angle the angle value to set.
     * @return the DocumentPage object itself.
     */
    void setAngle(float angle) {
        this.angle = angle;
    }

    /**
     * Get the width property: The width of the image/PDF in pixels/inches, respectively.
     *
     * @return the width value.
     */
    public float getWidth() {
        return this.width;
    }

    /**
     * Set the width property: The width of the image/PDF in pixels/inches, respectively.
     *
     * @param width the width value to set.
     * @return the DocumentPage object itself.
     */
    void setWidth(float width) {
        this.width = width;
    }

    /**
     * Get the height property: The height of the image/PDF in pixels/inches, respectively.
     *
     * @return the height value.
     */
    public float getHeight() {
        return this.height;
    }

    /**
     * Set the height property: The height of the image/PDF in pixels/inches, respectively.
     *
     * @param height the height value to set.
     * @return the DocumentPage object itself.
     */
    void setHeight(float height) {
        this.height = height;
    }

    /**
     * Get the unit property: The unit used by the width, height, and boundingBox properties. For images, the unit is
     * "pixel". For PDF, the unit is "inch".
     *
     * @return the unit value.
     */
    public LengthUnit getUnit() {
        return this.unit;
    }

    /**
     * Set the unit property: The unit used by the width, height, and boundingBox properties. For images, the unit is
     * "pixel". For PDF, the unit is "inch".
     *
     * @param unit the unit value to set.
     * @return the DocumentPage object itself.
     */
    void setUnit(LengthUnit unit) {
        this.unit = unit;
    }

    /**
     * Get the spans property: Location of the page in the reading order concatenated content.
     *
     * @return the spans value.
     */
    public List<DocumentSpan> getSpans() {
        return this.spans;
    }

    /**
     * Set the spans property: Location of the page in the reading order concatenated content.
     *
     * @param spans the spans value to set.
     * @return the DocumentPage object itself.
     */
    void setSpans(List<DocumentSpan> spans) {
        this.spans = spans;
    }

    /**
     * Get the words property: Extracted words from the page.
     *
     * @return the words value.
     */
    public List<DocumentWord> getWords() {
        return this.words;
    }

    /**
     * Set the words property: Extracted words from the page.
     *
     * @param words the words value to set.
     * @return the DocumentPage object itself.
     */
    void setWords(List<DocumentWord> words) {
        this.words = words;
    }

    /**
     * Get the selectionMarks property: Extracted selection marks from the page.
     *
     * @return the selectionMarks value.
     */
    public List<DocumentSelectionMark> getSelectionMarks() {
        return this.selectionMarks;
    }

    /**
     * Set the selectionMarks property: Extracted selection marks from the page.
     *
     * @param selectionMarks the selectionMarks value to set.
     * @return the DocumentPage object itself.
     */
    void setSelectionMarks(List<DocumentSelectionMark> selectionMarks) {
        this.selectionMarks = selectionMarks;
    }

    /**
     * Get the lines property: Extracted lines from the page, potentially containing both textual and visual elements.
     *
     * @return the lines value.
     */
    public List<DocumentLine> getLines() {
        return this.lines;
    }

    /**
     * Set the lines property: Extracted lines from the page, potentially containing both textual and visual elements.
     *
     * @param lines the lines value to set.
     * @return the DocumentPage object itself.
     */
    void setLines(List<DocumentLine> lines) {
        this.lines = lines;
    }

    static {
        DocumentPageHelper.setAccessor(new DocumentPageHelper.DocumentPageAccessor() {
            @Override
            public void setPageNumber(DocumentPage documentPage, int pageNumber) {
                documentPage.setPageNumber(pageNumber);
            }

            @Override
            public void setAngle(DocumentPage documentPage, float angle) {
                documentPage.setAngle(angle);
            }

            @Override
            public void setWidth(DocumentPage documentPage, float width) {
                documentPage.setWidth(width);
            }

            @Override
            public void setHeight(DocumentPage documentPage, float height) {
                documentPage.setHeight(height);
            }

            @Override
            public void setUnit(DocumentPage documentPage, LengthUnit unit) {
                documentPage.setUnit(unit);
            }

            @Override
            public void setSpans(DocumentPage documentPage, List<DocumentSpan> spans) {
                documentPage.setSpans(spans);
            }

            @Override
            public void setWords(DocumentPage documentPage, List<DocumentWord> words) {
                documentPage.setWords(words);
            }

            @Override
            public void setSelectionMarks(DocumentPage documentPage, List<DocumentSelectionMark> selectionMarks) {
                documentPage.setSelectionMarks(selectionMarks);
            }

            @Override
            public void setLines(DocumentPage documentPage, List<DocumentLine> lines) {
                documentPage.setLines(lines);
            }
        });
    }
}
