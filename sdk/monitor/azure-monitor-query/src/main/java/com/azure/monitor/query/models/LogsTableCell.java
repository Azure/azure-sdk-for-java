// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.models;

import com.azure.core.annotation.Immutable;

/**
 *
 */
@Immutable
public final class LogsTableCell {
    private final String columnName;
    private final String columnType;
    private final int columnIndex;
    private final int rowIndex;
    private final String rowValue;

    /**
     * @param columnName
     * @param columnType
     * @param columnIndex
     * @param rowIndex
     * @param rowValue
     */
    public LogsTableCell(String columnName, String columnType, int columnIndex, int rowIndex, String rowValue) {
        this.columnName = columnName;
        this.columnType = columnType;
        this.columnIndex = columnIndex;
        this.rowIndex = rowIndex;
        this.rowValue = rowValue;
    }

    /**
     * @return
     */
    public String getColumnType() {
        return columnType;
    }

    /**
     * @return
     */
    public String getColumnName() {
        return columnName;
    }

    /**
     * @return
     */
    public int getColumnIndex() {
        return columnIndex;
    }

    /**
     * @return
     */
    public int getRowIndex() {
        return rowIndex;
    }

    /**
     * @return
     */
    public String getRowValue() {
        return rowValue;
    }
}
