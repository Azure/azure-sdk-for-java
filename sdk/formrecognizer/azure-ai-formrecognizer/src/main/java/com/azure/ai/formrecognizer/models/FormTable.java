// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;

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
    private final IterableStream<FormTableCell> cells;

    /**
     * Constructs a FormTable object.
     *
     * @param rowCount Number of rows.
     * @param columnCount Number of columns.
     * @param cells ist of cells contained in the table.
     */
    public FormTable(final int rowCount, final int columnCount, final IterableStream<FormTableCell> cells) {
        this.rowCount = rowCount;
        this.columnCount = columnCount;
        this.cells = cells;
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
    public IterableStream<FormTableCell> getCells() {
        return this.cells;
    }
}
