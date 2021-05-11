// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.BinaryData;

import java.time.OffsetDateTime;

/**
 *
 */
@Immutable
public final class LogsTableCell {
    private final String columnName;
    private final ColumnDataType columnType;
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
    public LogsTableCell(String columnName, ColumnDataType columnType, int columnIndex, int rowIndex, String rowValue) {
        this.columnName = columnName;
        this.columnType = columnType;
        this.columnIndex = columnIndex;
        this.rowIndex = rowIndex;
        this.rowValue = rowValue;
    }

    /**
     * @return
     */
    public ColumnDataType getColumnType() {
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
    public String getValueAsString() {
        return rowValue == null ? null : rowValue.toString();
    }

    /**
     * @return
     */
    public Boolean getValueAsBoolean() {
        return rowValue == null ? null : Boolean.valueOf(rowValue.toString());
    }

    /**
     * @return
     */
    public Integer getValueAsInteger() {
        return rowValue == null ? null : Integer.parseInt(rowValue);
    }

    /**
     * @return
     */
    public Double getValueAsDouble() {
        return rowValue == null ? null : Double.parseDouble(rowValue);
    }

    /**
     * @return
     */
    public Long getValueAsLong() {
        return rowValue == null ? null : Long.parseLong(rowValue);
    }

    /**
     * @return
     */
    public BinaryData getValueAsDynamic() {
        return rowValue == null ? null : BinaryData.fromString(rowValue);
    }

    /**
     * @return
     */
    public OffsetDateTime getValueAsDateTime() {
        return rowValue == null ? null : OffsetDateTime.parse(rowValue);
    }

}
