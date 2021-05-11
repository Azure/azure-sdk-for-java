// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.models;

import com.azure.core.annotation.Immutable;

import java.util.List;

/**
 *
 */
@Immutable
public final class LogsTable {
    private final List<LogsTableCell> allTableCells;
    private final List<LogsTableRow> tableRows;
    private final List<LogsTableColumn> tableColumns;

    /**
     * @param allTableCells
     * @param tableRows
     */
    public LogsTable(List<LogsTableCell> allTableCells, List<LogsTableRow> tableRows,
                     List<LogsTableColumn> tableColumns) {
        this.tableColumns = tableColumns;
        this.allTableCells = allTableCells;
        this.tableRows = tableRows;
    }

    /**
     * @return
     */
    public List<LogsTableCell> getAllTableCells() {
        return allTableCells;
    }

    /**
     * @return
     */
    public List<LogsTableRow> getTableRows() {
        return tableRows;
    }

    /**
     * @return
     */
    public List<LogsTableColumn> getTableColumns() {
        return tableColumns;
    }
}
