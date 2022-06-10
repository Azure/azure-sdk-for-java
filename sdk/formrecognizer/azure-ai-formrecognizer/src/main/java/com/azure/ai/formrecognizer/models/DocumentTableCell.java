// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.ai.formrecognizer.implementation.util.DocumentTableCellHelper;

import java.util.List;

/**
 * An object representing the location and content of a table cell.
 */
public final class DocumentTableCell {
    /*
     * Table cell kind.
     */
    private DocumentTableCellKind kind;

    /*
     * Row index of the cell.
     */
    private int rowIndex;

    /*
     * Column index of the cell.
     */
    private int columnIndex;

    /*
     * Number of rows spanned by this cell.
     */
    private Integer rowSpan;

    /*
     * Number of columns spanned by this cell.
     */
    private Integer columnSpan;

    /*
     * Concatenated content of the table cell in reading order.
     */
    private String content;

    /*
     * Bounding regions covering the table cell.
     */
    private List<BoundingRegion> boundingRegions;

    /*
     * Location of the table cell in the reading order concatenated content.
     */
    private List<DocumentSpan> spans;

    /**
     * Get the kind property: Table cell kind.
     *
     * @return the kind value.
     */
    public DocumentTableCellKind getKind() {
        return this.kind;
    }

    /**
     * Set the kind property: Table cell kind.
     *
     * @param kind the kind value to set.
     * @return the DocumentTableCell object itself.
     */
    void setKind(DocumentTableCellKind kind) {
        this.kind = kind;
    }

    /**
     * Get the rowIndex property: Row index of the cell.
     *
     * @return the rowIndex value.
     */
    public int getRowIndex() {
        return this.rowIndex;
    }

    /**
     * Set the rowIndex property: Row index of the cell.
     *
     * @param rowIndex the rowIndex value to set.
     * @return the DocumentTableCell object itself.
     */
    void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    /**
     * Get the columnIndex property: Column index of the cell.
     *
     * @return the columnIndex value.
     */
    public int getColumnIndex() {
        return this.columnIndex;
    }

    /**
     * Set the columnIndex property: Column index of the cell.
     *
     * @param columnIndex the columnIndex value to set.
     * @return the DocumentTableCell object itself.
     */
    void setColumnIndex(int columnIndex) {
        this.columnIndex = columnIndex;
    }

    /**
     * Get the rowSpan property: Number of rows spanned by this cell.
     *
     * @return the rowSpan value.
     */
    public Integer getRowSpan() {
        return this.rowSpan;
    }

    /**
     * Set the rowSpan property: Number of rows spanned by this cell.
     *
     * @param rowSpan the rowSpan value to set.
     * @return the DocumentTableCell object itself.
     */
    void setRowSpan(Integer rowSpan) {
        this.rowSpan = rowSpan;
    }

    /**
     * Get the columnSpan property: Number of columns spanned by this cell.
     *
     * @return the columnSpan value.
     */
    public Integer getColumnSpan() {
        return this.columnSpan;
    }

    /**
     * Set the columnSpan property: Number of columns spanned by this cell.
     *
     * @param columnSpan the columnSpan value to set.
     * @return the DocumentTableCell object itself.
     */
    void setColumnSpan(Integer columnSpan) {
        this.columnSpan = columnSpan;
    }

    /**
     * Get the content property: Concatenated content of the table cell in reading order.
     *
     * @return the content value.
     */
    public String getContent() {
        return this.content;
    }

    /**
     * Set the content property: Concatenated content of the table cell in reading order.
     *
     * @param content the content value to set.
     * @return the DocumentTableCell object itself.
     */
    void setContent(String content) {
        this.content = content;
    }

    /**
     * Get the boundingRegions property: Bounding regions covering the table cell.
     *
     * @return the boundingRegions value.
     */
    public List<BoundingRegion> getBoundingRegions() {
        return this.boundingRegions;
    }

    /**
     * Set the boundingRegions property: Bounding regions covering the table cell.
     *
     * @param boundingRegions the boundingRegions value to set.
     * @return the DocumentTableCell object itself.
     */
    void setBoundingRegions(List<BoundingRegion> boundingRegions) {
        this.boundingRegions = boundingRegions;
    }

    /**
     * Get the spans property: Location of the table cell in the reading order concatenated content.
     *
     * @return the spans value.
     */
    public List<DocumentSpan> getSpans() {
        return this.spans;
    }

    /**
     * Set the spans property: Location of the table cell in the reading order concatenated content.
     *
     * @param spans the spans value to set.
     * @return the DocumentTableCell object itself.
     */
    void setSpans(List<DocumentSpan> spans) {
        this.spans = spans;
    }

    static {
        DocumentTableCellHelper.setAccessor(new DocumentTableCellHelper.DocumentTableCellAccessor() {
            @Override
            public void setSpans(DocumentTableCell documentTableCell, List<DocumentSpan> spans) {
                documentTableCell.setSpans(spans);
            }

            @Override
            public void setBoundingRegions(DocumentTableCell documentTableCell, List<BoundingRegion> boundingRegions) {
                documentTableCell.setBoundingRegions(boundingRegions);
            }

            @Override
            public void setContent(DocumentTableCell documentTableCell, String content) {
                documentTableCell.setContent(content);
            }

            @Override
            public void setColumnSpan(DocumentTableCell documentTableCell, Integer columnSpan) {
                documentTableCell.setColumnSpan(columnSpan);
            }

            @Override
            public void setRowSpan(DocumentTableCell documentTableCell, Integer rowSpan) {
                documentTableCell.setRowSpan(rowSpan);
            }

            @Override
            public void setColumnIndex(DocumentTableCell documentTableCell, int columnIndex) {
                documentTableCell.setColumnIndex(columnIndex);
            }

            @Override
            public void setRowIndex(DocumentTableCell documentTableCell, int rowIndex) {
                documentTableCell.setRowIndex(rowIndex);
            }

            @Override
            public void setKind(DocumentTableCell documentTableCell, DocumentTableCellKind kind) {
                documentTableCell.setKind(kind);
            }
        });
    }
}
