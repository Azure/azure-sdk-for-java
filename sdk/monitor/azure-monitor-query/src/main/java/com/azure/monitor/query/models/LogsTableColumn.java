// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.models;

/**
 * Represents a column in {@link LogsTable}.
 */
public final class LogsTableColumn {
    private final String columnName;
    private final LogsColumnType columnType;

    /**
     * Creates a column in {@link LogsTable}.
     * @param columnName The name of the column.
     * @param columnType The data type of the value in this column.
     */
    public LogsTableColumn(String columnName, LogsColumnType columnType) {
        this.columnName = columnName;
        this.columnType = columnType;
    }

    /**
     * Returns the name of the column.
     * @return the name of the column.
     */
    public String getColumnName() {
        return columnName;
    }

    /**
     * Returns the data type of the value in this column.
     * @return the data type of the value in this column.
     */
    public LogsColumnType getColumnType() {
        return columnType;
    }
}
