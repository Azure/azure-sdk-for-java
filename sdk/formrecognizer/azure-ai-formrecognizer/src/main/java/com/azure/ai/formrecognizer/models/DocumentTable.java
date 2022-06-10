// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.ai.formrecognizer.implementation.util.DocumentTableHelper;

import java.util.List;

/**
 * A table object consisting table cells arranged in a rectangular layout.
 */
public final class DocumentTable {
    /*
     * Number of rows in the table.
     */
    private int rowCount;

    /*
     * Number of columns in the table.
     */
    private int columnCount;

    /*
     * Cells contained within the table.
     */
    private List<DocumentTableCell> cells;

    /*
     * Bounding regions covering the table.
     */
    private List<BoundingRegion> boundingRegions;

    /*
     * Location of the table in the reading order concatenated content.
     */
    private List<DocumentSpan> spans;

    /**
     * Get the rowCount property: Number of rows in the table.
     *
     * @return the rowCount value.
     */
    public int getRowCount() {
        return this.rowCount;
    }

    /**
     * Set the rowCount property: Number of rows in the table.
     *
     * @param rowCount the rowCount value to set.
     * @return the DocumentTable object itself.
     */
    void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    /**
     * Get the columnCount property: Number of columns in the table.
     *
     * @return the columnCount value.
     */
    public int getColumnCount() {
        return this.columnCount;
    }

    /**
     * Set the columnCount property: Number of columns in the table.
     *
     * @param columnCount the columnCount value to set.
     * @return the DocumentTable object itself.
     */
    void setColumnCount(int columnCount) {
        this.columnCount = columnCount;
    }

    /**
     * Get the cells property: Cells contained within the table.
     *
     * @return the cells value.
     */
    public List<DocumentTableCell> getCells() {
        return this.cells;
    }

    /**
     * Set the cells property: Cells contained within the table.
     *
     * @param cells the cells value to set.
     * @return the DocumentTable object itself.
     */
    void setCells(List<DocumentTableCell> cells) {
        this.cells = cells;
    }

    /**
     * Get the boundingRegions property: Bounding regions covering the table.
     *
     * @return the boundingRegions value.
     */
    public List<BoundingRegion> getBoundingRegions() {
        return this.boundingRegions;
    }

    /**
     * Set the boundingRegions property: Bounding regions covering the table.
     *
     * @param boundingRegions the boundingRegions value to set.
     * @return the DocumentTable object itself.
     */
    void setBoundingRegions(List<BoundingRegion> boundingRegions) {
        this.boundingRegions = boundingRegions;
    }

    /**
     * Get the spans property: Location of the table in the reading order concatenated content.
     *
     * @return the spans value.
     */
    public List<DocumentSpan> getSpans() {
        return this.spans;
    }

    /**
     * Set the spans property: Location of the table in the reading order concatenated content.
     *
     * @param spans the spans value to set.
     * @return the DocumentTable object itself.
     */
    void setSpans(List<DocumentSpan> spans) {
        this.spans = spans;
    }

    static {
        DocumentTableHelper.setAccessor(new DocumentTableHelper.DocumentTableAccessor() {
            @Override
            public void setRowCount(DocumentTable documentTable, int rowCount) {
                documentTable.setRowCount(rowCount);
            }

            @Override
            public void setColumnCount(DocumentTable documentTable, int columnCount) {
                documentTable.setColumnCount(columnCount);
            }

            @Override
            public void setCells(DocumentTable documentTable, List<DocumentTableCell> cells) {
                documentTable.setCells(cells);
            }

            @Override
            public void setBoundingRegions(DocumentTable documentTable, List<BoundingRegion> boundingRegions) {
                documentTable.setBoundingRegions(boundingRegions);
            }

            @Override
            public void setSpans(DocumentTable documentTable, List<DocumentSpan> spans) {
                documentTable.setSpans(spans);
            }
        });
    }
}
