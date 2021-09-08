// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.implementation.logs.models;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.TypeReference;
import com.azure.monitor.query.LogsQueryAsyncClient;
import com.azure.monitor.query.models.LogsBatchQuery;
import com.azure.monitor.query.models.LogsColumnType;
import com.azure.monitor.query.models.LogsQueryOptions;
import com.azure.monitor.query.models.LogsTable;
import com.azure.monitor.query.models.LogsTableRow;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Helper to access package-private method of {@link LogsBatchQuery} from {@link LogsQueryAsyncClient}.
 */
public final class LogsQueryHelper {
    private static final ClientLogger LOGGER = new ClientLogger(LogsQueryHelper.class);
    private static BatchQueryAccessor accessor;

    public static Duration getMaxServerTimeout(LogsBatchQuery query) {
        return accessor.getMaxServerTimeout(query);
    }

    /**
     * Accessor interface
     */
    public interface BatchQueryAccessor {
        List<BatchQueryRequest> getBatchQueries(LogsBatchQuery query);

        Duration getMaxServerTimeout(LogsBatchQuery query);
    }

    /**
     * Sets the accessor instance.
     * @param batchQueryAccessor the accessor instance
     */
    public static void setAccessor(final BatchQueryAccessor batchQueryAccessor) {
        accessor = batchQueryAccessor;
    }

    /**
     * Returns the list of batch queries.
     * @param query the {@link LogsBatchQuery} to access {@link @BatchQueryRequest} from.
     * @return the list of batch queries.
     */
    public static List<BatchQueryRequest> getBatchQueries(LogsBatchQuery query) {
        return accessor.getBatchQueries(query);
    }

    public static String buildPreferHeaderString(LogsQueryOptions options) {
        if (options == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        if (options.isIncludeVisualization()) {
            sb.append("include-render=true");
        }

        if (options.isIncludeStatistics()) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append("include-statistics=true");
        }

        if (options.getServerTimeout() != null) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append("wait=");
            sb.append(options.getServerTimeout().getSeconds());
        }

        return sb.toString().isEmpty() ? null : sb.toString();
    }

    /**
     * Returns the table as a list of objects of the given {@code type}. The field names on the type should be
     * reflectively accessible and the names should match the column names. If the table row contains columns that
     * are not available on the object, they are ignored and similarly if the fields in the object are not found in
     * the table columns, they will be null.
     * @param table The table that contains the query result.
     * @param type The type of the object to be returned
     * @param <T> The class type.
     * @return A list of objects that table is mapped to.
     * @throws IllegalArgumentException if an instance of the object cannot be created.
     */
    public static <T> List<T> toObject(LogsTable table, Class<T> type) {
        List<T> result = new ArrayList<>();
        for (LogsTableRow tableRow : table.getRows()) {
            try {
                T t = type.newInstance();

                Map<String, Field> declaredFieldMapping = Arrays.stream(type.getDeclaredFields())
                        .collect(Collectors.toMap(field -> field.getName().toLowerCase(Locale.ROOT), field -> field));

                tableRow.getRow().stream()
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
                                throw LOGGER.logExceptionAsError(
                                        new IllegalArgumentException("Failed to set column value for " + columnName, ex));
                            }
                        });
                result.add(t);
            } catch (InstantiationException | IllegalAccessException ex) {
                throw LOGGER.logExceptionAsError(
                        new IllegalArgumentException("Cannot create an instance of class " + type.getName(), ex));
            }
        }
        return result;
    }
}
