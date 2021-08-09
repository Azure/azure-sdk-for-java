// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.BinaryData;

import java.time.OffsetDateTime;

/**
 * Represents a single value of a specific row and column in {@link LogsTable}.
 */
@Immutable
public final class LogsTableCell {
    private final String columnName;
    private final LogsColumnType columnType;
    private final int columnIndex;
    private final int rowIndex;
    private final String rowValue;

    /**
     * Creates an instance of {@link LogsTableCell}.
     * @param columnName The name of the column this cell is associated with.
     * @param columnType The data type of the value this cell contains.
     * @param columnIndex The column index of the column this cell is associated with.
     * @param rowIndex The row index of the row this cell is associated with.
     * @param rowValue The value of the cell.
     */
    public LogsTableCell(String columnName, LogsColumnType columnType, int columnIndex, int rowIndex, Object rowValue) {
        this.columnName = columnName;
        this.columnType = columnType;
        this.columnIndex = columnIndex;
        this.rowIndex = rowIndex;
        this.rowValue = rowValue == null ? null : rowValue.toString();
    }

    /**
     * Returns the data type of the value this cell contains.
     * @return the data type of the value this cell contains.
     */
    public LogsColumnType getColumnType() {
        return columnType;
    }

    /**
     * Returns the name of the column this cell is associated with.
     * @return the name of the column this cell is associated with.
     */
    public String getColumnName() {
        return columnName;
    }

    /**
     * Returns the column index of the column this cell is associated with.
     * @return the column index of the column this cell is associated with.
     */
    public int getColumnIndex() {
        return columnIndex;
    }

    /**
     * Returns the row index of the row this cell is associated with.
     * @return the row index of the row this cell is associated with.
     */
    public int getRowIndex() {
        return rowIndex;
    }

    /**
     * Returns the value as a string.
     * @return the value as a string.
     */
    public String getValueAsString() {
        return rowValue == null ? null : rowValue;
    }

    /**
     * Returns the value as a boolean.
     * @return the value as a boolean.
     */
    public Boolean getValueAsBoolean() {
        return rowValue == null ? null : Boolean.valueOf(rowValue);
    }

    /**
     * Returns the value as an integer.
     * @return the value as an integer.
     */
    public Integer getValueAsInteger() {
        return rowValue == null ? null : Integer.parseInt(rowValue);
    }

    /**
     * Returns the value as a double.
     * @return the value as a double.
     */
    public Double getValueAsDouble() {
        return rowValue == null ? null : Double.parseDouble(rowValue);
    }

    /**
     * Returns the value as a long.
     * @return the value as a long.
     */
    public Long getValueAsLong() {
        return rowValue == null ? null : Long.parseLong(rowValue);
    }

    /**
     * Returns the value as a dynamic type which can be deserialized into a model type from {@link BinaryData}.
     * @return the value as a dynamic type which can be deserialized into a model type from {@link BinaryData}.
     */
    public BinaryData getValueAsDynamic() {
        return rowValue == null ? null : BinaryData.fromString(rowValue);
    }

    /**
     * Returns the value as an {@link OffsetDateTime}.
     * @return the value as an {@link OffsetDateTime}.
     */
    public OffsetDateTime getValueAsDateTime() {
        return rowValue == null ? null : OffsetDateTime.parse(rowValue);
    }

}
