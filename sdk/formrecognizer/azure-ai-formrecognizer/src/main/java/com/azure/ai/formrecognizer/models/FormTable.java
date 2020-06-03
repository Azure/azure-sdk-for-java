// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Immutable;

import java.util.List;

/**
 * The FormTable model.
 */
@Immutable
public final class FormTable {

    /*
     * Number of rows.
     */
    private final int rowCount;

    /*
     * Number of columns.
     */
    private final int columnCount;

    /*
     * List of cells contained in the table.
     */
    private final List<FormTableCell> cells;

    /*
     * The 1 based page number.
     */
    private final Integer pageNumber;

    /**
     * Constructs a FormTable object.
     *
     * @param rowCount Number of rows.
     * @param columnCount Number of columns.
     * @param cells ist of cells contained in the table.
     * @param pageNumber the 1-based page number in the input document.
     */
    public FormTable(final int rowCount, final int columnCount, final List<FormTableCell> cells,
        final Integer pageNumber) {
        this.rowCount = rowCount;
        this.columnCount = columnCount;
        this.cells = cells;
        this.pageNumber = pageNumber;
    }

    /**
     * Get the rows property: Number of rows.
     *
     * @return the rows value.
     */
    public int getRowCount() {
        return this.rowCount;
    }

    /**
     * Get the columns property: Number of columns.
     *
     * @return the columns value.
     */
    public int getColumnCount() {
        return this.columnCount;
    }

    /**
     * Get the cells property: List of cells contained in the table.
     *
     * @return the cells value.
     */
    public List<FormTableCell> getCells() {
        return this.cells;
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
