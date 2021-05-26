// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.models;

import com.azure.core.annotation.Immutable;

import java.util.List;
import java.util.Optional;

/**
 * Represents a row in a {@link LogsTable} of a logs query.
 */
@Immutable
public final class LogsTableRow {
    private final int rowIndex;
    private final List<LogsTableCell> tableRow;

    /**
     * Creates a row in a {@link LogsTable} of a logs query.
     * @param rowIndex The row index.
     * @param tableRow The collection of values in the row.
     */
    public LogsTableRow(int rowIndex, List<LogsTableCell> tableRow) {
        this.rowIndex = rowIndex;
        this.tableRow = tableRow;
    }

    /**
     * Returns the row index of this row.
     * @return the row index of this row.
     */
    public int getRowIndex() {
        return rowIndex;
    }

    /**
     * Returns the collection of values in this row.
     * @return the collection of values in this row.
     */
    public List<LogsTableCell> getTableRow() {
        return tableRow;
    }

    /**
     * Returns the value associated with the given column name. If the column name is not found
     * {@link Optional#isPresent()} evaluates to {@code false}.
     * @param columnName the column name for which the value is returned.
     *
     * @return The value associated with the given column name.
     */
    public Optional<LogsTableCell> getColumnValue(String columnName) {
        return tableRow.stream()
            .filter(cell -> cell.getColumnName().equals(columnName))
            .findFirst();
    }
}
