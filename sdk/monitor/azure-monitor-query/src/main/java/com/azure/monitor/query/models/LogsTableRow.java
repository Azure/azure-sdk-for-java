// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.TypeReference;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Represents a row in a {@link LogsTable} of a logs query.
 */
@Immutable
public final class LogsTableRow {
    private final int rowIndex;
    private final List<LogsTableCell> tableRow;
    private final ClientLogger logger = new ClientLogger(LogsTableRow.class);

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
    public List<LogsTableCell> getRow() {
        return tableRow;
    }

    /**
     * Returns the value associated with the given column name. If the column name is not found
     * {@link Optional#isPresent()} evaluates to {@code false}.
     * @param columnName the column name for which the value is returned.
     * @return The value associated with the given column name.
     */
    public Optional<LogsTableCell> getColumnValue(String columnName) {
        return tableRow.stream()
                .filter(cell -> cell.getColumnName().equals(columnName))
                .findFirst();
    }

    /**
     * Returns the table row as an object of the given {@code type}. The field names on the type should be
     * reflectively accessible and the names should match the column names. If the table row contains columns that
     * are not available on the object, they are ignored and similarly if the fields in the object are not found in
     * the table columns, they will be null.
     * @param type The type of the object to be returned
     * @param <T> The class type.
     * @return The object that this table row is mapped to.
     * @throws IllegalArgumentException if an instance of the object cannot be created.
     */
    public <T> T getRowAsObject(Class<T> type) {
        try {
            T t = type.newInstance();

            Map<String, Field> declaredFieldMapping = Arrays.stream(type.getDeclaredFields())
                    .collect(Collectors.toMap(field -> field.getName().toLowerCase(Locale.ROOT), field -> field));

            tableRow.stream()
                    .forEach(tableCell -> {
                        String columnName = tableCell.getColumnName();
                        try {
                            Field field = declaredFieldMapping.get(columnName.toLowerCase(Locale.ROOT));
                            if (field == null) {
                                return;
                            }
                            field.setAccessible(true);
                            if (tableCell.getColumnType() == LogsColumnType.BOOL) {
                                field.set(t, tableCell.getValueAsBoolean());
                            } else if (tableCell.getColumnType() == LogsColumnType.DATETIME) {
                                field.set(t, tableCell.getValueAsDateTime());
                            } else if (tableCell.getColumnType() == LogsColumnType.DYNAMIC) {
                                if (tableCell.getValueAsDynamic() != null) {
                                    field.set(t,
                                            tableCell.getValueAsDynamic()
                                                    .toObject(TypeReference.createInstance(field.getType())));
                                }
                            } else if (tableCell.getColumnType() == LogsColumnType.INT) {
                                field.set(t, tableCell.getValueAsInteger());
                            } else if (tableCell.getColumnType() == LogsColumnType.LONG) {
                                field.set(t, tableCell.getValueAsLong());
                            } else if (tableCell.getColumnType() == LogsColumnType.REAL
                                || tableCell.getColumnType() == LogsColumnType.DECIMAL) {
                                field.set(t, tableCell.getValueAsDouble());
                            } else if (tableCell.getColumnType() == LogsColumnType.STRING
                                || tableCell.getColumnType() == LogsColumnType.GUID
                                || tableCell.getColumnType() == LogsColumnType.TIMESPAN) {
                                field.set(t, tableCell.getValueAsString());
                            }
                            field.setAccessible(false);
                        } catch (IllegalAccessException ex) {
                            throw logger.logExceptionAsError(
                                    new IllegalArgumentException("Failed to set column value for " + columnName, ex));
                        }
                    });
            return t;
        } catch (InstantiationException | IllegalAccessException ex) {
            throw logger.logExceptionAsError(
                    new IllegalArgumentException("Cannot create an instance of class " + type.getName(), ex));
        }
    }
}
