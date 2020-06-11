// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Immutable;

import java.util.List;

/**
 * The FormTableCell model.
 */
@Immutable
public final class FormTableCell extends FormContent {

    /*
     * Row index of the cell.
     */
    private final Integer rowIndex;

    /*
     * Column index of the cell.
     */
    private final Integer columnIndex;

    /*
     * Number of rows spanned by this cell.
     */
    private final Integer rowSpan;

    /*
     * Number of columns spanned by this cell.
     */
    private final Integer columnSpan;

    /*
     * Confidence value.
     */
    private final float confidence;

    /*
     * When includeTextDetails is set to true, a list of references to the text
     * elements constituting this table cell.
     */
    private final List<FormContent> elements;

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
     * @param rowIndex Row index of the cell.
     * @param columnIndex Column index of the cell.
     * @param rowSpan Number of rows spanned by this cell.
     * @param columnSpan Number of columns spanned by this cell.
     * @param text The recognized text value.
     * @param boundingBox The bounding box properties of the cell.
     * @param confidence Confidence value of the recognized text.
     * @param isHeader Is the current cell a header cell?
     * @param isFooter Is the current cell a footer cell?
     * @param pageNumber The 1 based page number of the cell
     * @param textContent a list of references to the text elements constituting this table cell.
     */
    public FormTableCell(final int rowIndex, final int columnIndex, final Integer rowSpan,
        final Integer columnSpan, final String text, final BoundingBox boundingBox,
        final float confidence, final boolean isHeader, final boolean isFooter, final int pageNumber,
        final List<FormContent> textContent) {
        super(text, boundingBox, pageNumber, null);
        this.rowIndex = rowIndex;
        this.columnIndex = columnIndex;
        this.rowSpan = rowSpan;
        this.columnSpan = columnSpan;
        this.confidence = confidence;
        this.isHeader = isHeader;
        this.isFooter = isFooter;
        this.elements = textContent;
    }

    /**
     * Get the confidence of the text of the cell.
     *
     * @return the confidence value.
     */
    public float getConfidence() {
        return this.confidence;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getPageNumber() {
        return super.getPageNumber();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BoundingBox getBoundingBox() {
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
     * @return the rowIndex value.
     */
    public Integer getRowIndex() {
        return this.rowIndex;
    }

    /**
     * Get the column index of the cell.
     *
     * @return the columnIndex value.
     */
    public Integer getColumnIndex() {
        return this.columnIndex;
    }

    /**
     * Get the number of rows spanned by this cell.
     *
     * @return the rowSpan value.
     */
    public Integer getRowSpan() {
        return this.rowSpan;
    }


    /**
     * Get the number of columns spanned by this cell.
     *
     * @return the columnSpan value.
     */
    public Integer getColumnSpan() {
        return this.columnSpan;
    }

    /**
     * Get the list of references to the text elements constituting this table cell
     * When includeTextDetails is set to true.
     *
     * @return the elements value.
     */
    public List<FormContent> getElements() {
        return this.elements;
    }

    /**
     * Get the boolean if the current cell a header cell.
     *
     * @return the isHeader value.
     */
    public boolean isHeader() {
        return this.isHeader;
    }

    /**
     * Get the boolean if the current cell a footer cell.
     *
     * @return the isFooter value.
     */
    public boolean isFooter() {
        return this.isFooter;
    }
}
