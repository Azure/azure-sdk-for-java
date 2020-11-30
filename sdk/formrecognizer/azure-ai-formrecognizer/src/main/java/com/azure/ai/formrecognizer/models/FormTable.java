// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.ai.formrecognizer.implementation.FormTableHelper;

import java.util.Collections;
import java.util.List;

/**
 * The FormTable model.
 */
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
    private final int pageNumber;

    private FieldBoundingBox fieldBoundingBox;

    static {
        FormTableHelper.setAccessor(new FormTableHelper.FormTableAccessor() {
            @Override
            public void setBoundingBox(FormTable formTable, FieldBoundingBox fieldBoundingBox) {
                formTable.setFieldBoundingBox(fieldBoundingBox);
            }
        });
    }

    /**
     * Constructs a FormTable object.
     *
     * @param rowCount the number of rows in the table.
     * @param columnCount the number of columns in the table.
     * @param cells the list of cells contained in the table.
     * @param pageNumber the 1-based page number in the input document.
     */
    public FormTable(final int rowCount, final int columnCount, final List<FormTableCell> cells,
        final int pageNumber) {
        this.rowCount = rowCount;
        this.columnCount = columnCount;
        this.cells = cells == null ? null : Collections.unmodifiableList(cells);
        this.pageNumber = pageNumber;
    }

    /**
     * Get the number of rows in the table.
     *
     * @return the number of rows in the table.
     */
    public int getRowCount() {
        return this.rowCount;
    }

    /**
     * Get the number of columns in the table.
     *
     * @return the number of columns in the table.
     */
    public int getColumnCount() {
        return this.columnCount;
    }

    /**
     * Get the list of cells contained in the table.
     *
     * @return the unmodifiable list of cells in the table.
     */
    public List<FormTableCell> getCells() {
        return this.cells;
    }

    /**
     * Get the 1-based page number in the input document.
     *
     * @return the 1-based page number in the input document.
     */
    public int getPageNumber() {
        return this.pageNumber;
    }

    /**
     * The private setter to set the appearance property
     * via {@link FormTableHelper.FormTableAccessor}.
     *
     * @param fieldBoundingBox the bounding box of the form table.
     * @return the updated FormTable object.
     */
    private FormTable setFieldBoundingBox(FieldBoundingBox fieldBoundingBox) {
        this.fieldBoundingBox = fieldBoundingBox;
        return this;
    }

    /**
     * Get the bounding box information for the the form table.
     * @return the bounding box information for the the form table.
     */
    public FieldBoundingBox getFieldBoundingBox() {
        return fieldBoundingBox;
    }
}
