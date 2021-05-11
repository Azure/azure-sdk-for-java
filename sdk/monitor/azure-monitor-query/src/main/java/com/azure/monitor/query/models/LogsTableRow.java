// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.models;

import com.azure.core.annotation.Immutable;

import java.util.List;
import java.util.Optional;

/**
 *
 */
@Immutable
public final class LogsTableRow {
    private final int rowIndex;
    private final List<LogsTableCell> tableRow;

    /**
     * @param rowIndex
     * @param tableRow
     */
    public LogsTableRow(int rowIndex, List<LogsTableCell> tableRow) {
        this.rowIndex = rowIndex;
        this.tableRow = tableRow;
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
    public List<LogsTableCell> getTableRow() {
        return tableRow;
    }

    /**
     * @param columnName
     *
     * @return
     */
    public Optional<LogsTableCell> getColumnValue(String columnName) {
        return tableRow.stream()
            .filter(cell -> cell.getColumnName().equals(columnName))
            .findFirst();
    }
}
