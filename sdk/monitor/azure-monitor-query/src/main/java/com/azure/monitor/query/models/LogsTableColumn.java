// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.models;

/**
 *
 */
public final class LogsTableColumn {
    private final String columnName;
    private final ColumnDataType columnType;

    /**
     * @param columnName
     * @param columnType
     */
    public LogsTableColumn(String columnName, ColumnDataType columnType) {
        this.columnName = columnName;
        this.columnType = columnType;
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
    public ColumnDataType getColumnType() {
        return columnType;
    }
}
