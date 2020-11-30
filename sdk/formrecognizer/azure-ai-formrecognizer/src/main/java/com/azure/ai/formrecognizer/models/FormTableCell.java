// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Immutable;

import java.util.Collections;
import java.util.List;

/**
 * The FormTableCell model.
 */
@Immutable
public final class FormTableCell extends FormElement {

    /*
     * Row index of the cell.
     */
    private final int rowIndex;

    /*
     * Column index of the cell.
     */
    private final int columnIndex;

    /*
     * Number of rows spanned by this cell.
     */
    private final int rowSpan;

    /*
     * Number of columns spanned by this cell.
     */
    private final int columnSpan;

    /*
     * Confidence value.
     */
    private final float confidence;

    /*
     * When includeFieldElements is set to true, a list of references to the
     * elements constituting this table cell.
     */
    private final List<FormElement> fieldElements;

    /*
     * Is the current cell a header cell?
     */
    private final boolean isHeader;

    /*
     * Is the current cell a footer cell?
     */
    private final boolean isFooter;

    /**
     * Constructs a FormTableCell object.
     *
     * @param rowIndex the row index of the cell.
     * @param columnIndex the column index of the cell.
     * @param rowSpan the number of rows spanned by this cell.
     * @param columnSpan the number of columns spanned by this cell.
     * @param text the recognized text value.
     * @param boundingBox the bounding box properties of the cell.
     * @param confidence the confidence value of the recognized text.
     * @param isHeader the boolean indicating if the current cell a header cell?
     * @param isFooter the boolean indicating if the current cell a footer cell?
     * @param pageNumber the 1 based page number of the cell
     * @param fieldElements a list of references to the elements constituting this table cell.
     */
    public FormTableCell(final int rowIndex, final int columnIndex, final int rowSpan,
        final int columnSpan, final String text, final FieldBoundingBox boundingBox,
        final float confidence, final boolean isHeader, final boolean isFooter, final int pageNumber,
        final List<FormElement> fieldElements) {
        super(text, boundingBox, pageNumber);
        this.rowIndex = rowIndex;
        this.columnIndex = columnIndex;
        this.rowSpan = rowSpan;
        this.columnSpan = columnSpan;
        this.confidence = confidence;
        this.isHeader = isHeader;
        this.isFooter = isFooter;
        this.fieldElements = fieldElements == null ? null : Collections.unmodifiableList(fieldElements);
    }

    /**
     * Get the confidence value of the recognized text of the cell.
     *
     * @return the confidence value of the recognized text of the cell.
     */
    public float getConfidence() {
        return this.confidence;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPageNumber() {
        return super.getPageNumber();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FieldBoundingBox getBoundingBox() {
        return super.getBoundingBox();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getText() {
        return super.getText();
    }

    /**
     * Get the row index of the cell.
     *
     * @return the row index of the cell.
     */
    public int getRowIndex() {
        return this.rowIndex;
    }

    /**
     * Get the column index of the cell.
     *
     * @return the column index of the cell.
     */
    public int getColumnIndex() {
        return this.columnIndex;
    }

    /**
     * Get the number of rows spanned by this cell.
     *
     * @return the number of rows spanned by this cell.
     */
    public int getRowSpan() {
        return this.rowSpan;
    }


    /**
     * Get the number of columns spanned by this cell.
     *
     * @return the number of columns spanned by this cell.
     */
    public int getColumnSpan() {
        return this.columnSpan;
    }

    /**
     * When includeFieldElements is set to true, gets the list of references to the elements
     * constituting this table cell.
     *
     * @return the unmodifiable list of list of references to the text elements constituting this table cell.
     */
    public List<FormElement> getFieldElements() {
        return this.fieldElements;
    }

    /**
     * Get the boolean if the current cell a header cell.
     *
     * @return the boolean indicating if the current cell a header cell.
     */
    public boolean isHeader() {
        return this.isHeader;
    }

    /**
     * Get the boolean if the current cell a footer cell.
     *
     * @return the boolean indicating if the current cell is a footer cell.
     */
    public boolean isFooter() {
        return this.isFooter;
    }
}
