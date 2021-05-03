// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.models;

import com.azure.core.annotation.Immutable;

import java.util.List;

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
}
