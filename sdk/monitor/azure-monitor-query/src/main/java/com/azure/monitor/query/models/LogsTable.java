// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.models;

import com.azure.core.annotation.Immutable;

import java.util.List;

/**
 * Represents a table of results for a logs query.
 */
@Immutable
public final class LogsTable {
    private final List<LogsTableCell> allTableCells;
    private final List<LogsTableRow> tableRows;
    private final List<LogsTableColumn> tableColumns;

    /**
     * Creates an instance of table that contains results of a logs query.
     * @param allTableCells All the cells that make up this table.
     * @param tableRows All the rows in this table.
     * @param tableColumns All the columns in this table.
     */
    public LogsTable(List<LogsTableCell> allTableCells, List<LogsTableRow> tableRows,
                     List<LogsTableColumn> tableColumns) {
        this.tableColumns = tableColumns;
        this.allTableCells = allTableCells;
        this.tableRows = tableRows;
    }

    /**
     * Returns all the cells in this table.
     * @return all the cells in this table.
     */
    public List<LogsTableCell> getAllTableCells() {
        return allTableCells;
    }

    /**
     * Returns all the rows in this table.
     * @return all the rows in this table.
     */
    public List<LogsTableRow> getTableRows() {
        return tableRows;
    }

    /**
     * Returns all the columns in this table.
     * @return all the columns in this table.
     */
    public List<LogsTableColumn> getTableColumns() {
        return tableColumns;
    }
}
