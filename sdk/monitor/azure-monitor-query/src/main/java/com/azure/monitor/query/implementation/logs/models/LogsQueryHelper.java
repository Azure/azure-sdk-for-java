// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.implementation.logs.models;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.models.ResponseError;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.TypeReference;
import com.azure.monitor.query.LogsQueryAsyncClient;
import com.azure.monitor.query.models.LogsBatchQuery;
import com.azure.monitor.query.models.LogsBatchQueryResult;
import com.azure.monitor.query.models.LogsBatchQueryResultCollection;
import com.azure.monitor.query.models.LogsColumnType;
import com.azure.monitor.query.models.LogsQueryOptions;
import com.azure.monitor.query.models.LogsQueryResult;
import com.azure.monitor.query.models.LogsTable;
import com.azure.monitor.query.models.LogsTableCell;
import com.azure.monitor.query.models.LogsTableColumn;
import com.azure.monitor.query.models.LogsTableRow;
import com.azure.monitor.query.models.QueryTimeInterval;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Helper to access package-private method of {@link LogsBatchQuery} from {@link LogsQueryAsyncClient}.
 */
public final class LogsQueryHelper {
    public static final String AZURE_RESPONSE_TIMEOUT = "azure-response-timeout";
    public static final int CLIENT_TIMEOUT_BUFFER = 5;

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

    /**
     * Returns this {@link QueryTimeInterval} in ISO 8601 string format.
     *
     * @return ISO 8601 formatted string representation of this {@link QueryTimeInterval} instance.
     */
    public static String toIso8601Format(QueryTimeInterval timeInterval) {
        if (timeInterval.getStartTime() != null && timeInterval.getEndTime() != null) {
            return timeInterval.getStartTime() + "/" + timeInterval.getEndTime();
        }

        if (timeInterval.getStartTime() != null && timeInterval.getDuration() != null) {
            return timeInterval.getStartTime() + "/" + timeInterval.getDuration();
        }

        if (timeInterval.getDuration()!= null && timeInterval.getEndTime() != null) {
            return timeInterval.getDuration() + "/" + timeInterval.getEndTime();
        }

        return timeInterval.getDuration() == null ? null : timeInterval.getDuration().toString();
    }


    public static Context updateContext(Duration serverTimeout, Context context) {
        if (serverTimeout != null) {
            return context.addData(AZURE_RESPONSE_TIMEOUT, serverTimeout.plusSeconds(CLIENT_TIMEOUT_BUFFER));
        }
        return context;
    }

    public static List<String> getAllWorkspaces(LogsQueryOptions options) {
        if (options != null) {
            return options.getAdditionalWorkspaces();
        }
        return null;
    }


    public static Response<LogsQueryResult> convertToLogQueryResult(Response<QueryResults> response) {
        QueryResults queryResults = response.getValue();
        LogsQueryResult logsQueryResult = getLogsQueryResult(queryResults.getTables(), queryResults.getStatistics(),
            queryResults.getRender(), queryResults.getError());
        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
            response.getHeaders(), logsQueryResult);
    }

    public static LogsQueryResult getLogsQueryResult(List<Table> innerTables, Object innerStats,
                                               Object innerVisualization, ErrorInfo innerError) {
        List<LogsTable> tables = null;

        if (innerTables != null) {
            tables = new ArrayList<>();
            for (Table table : innerTables) {
                List<LogsTableCell> tableCells = new ArrayList<>();
                List<LogsTableRow> tableRows = new ArrayList<>();
                List<LogsTableColumn> tableColumns = new ArrayList<>();
                LogsTable logsTable = new LogsTable(tableCells, tableRows, tableColumns);
                tables.add(logsTable);
                List<List<Object>> rows = table.getRows();

                for (int i = 0; i < rows.size(); i++) {
                    List<Object> row = rows.get(i);
                    LogsTableRow tableRow = new LogsTableRow(i, new ArrayList<>());
                    tableRows.add(tableRow);
                    for (int j = 0; j < row.size(); j++) {
                        LogsColumnType columnType = table.getColumns().get(j).getType() == null
                            ? null
                            : LogsColumnType.fromString(table.getColumns().get(j).getType().toString());
                        LogsTableCell cell = new LogsTableCell(table.getColumns().get(j).getName(),
                            columnType, j, i,
                            row.get(j));
                        tableCells.add(cell);
                        tableRow.getRow().add(cell);
                    }
                }
            }
        }

        BinaryData queryStatistics = null;

        if (innerStats != null) {
            queryStatistics = BinaryData.fromObject(innerStats);
        }

        BinaryData queryVisualization = null;
        if (innerVisualization != null) {
            queryVisualization = BinaryData.fromObject(innerVisualization);
        }

        LogsQueryResult logsQueryResult = new LogsQueryResult(tables, queryStatistics, queryVisualization,
            mapLogsQueryError(innerError));
        return logsQueryResult;
    }

    public static ResponseError mapLogsQueryError(ErrorInfo errors) {
        if (errors != null) {
            ErrorInfo innerError = errors.getInnererror();
            ErrorInfo currentError = errors.getInnererror();
            while (currentError != null) {
                innerError = currentError.getInnererror();
                currentError = currentError.getInnererror();
            }
            String code = errors.getCode();
            if (errors.getCode() != null && innerError != null && errors.getCode().equals(innerError.getCode())) {
                code = innerError.getCode();
            }
            return new ResponseError(code, errors.getMessage());
        }

        return null;
    }

    public static Response<LogsBatchQueryResultCollection> convertToLogQueryBatchResult(Response<BatchResponse> response) {
        List<LogsBatchQueryResult> batchResults = new ArrayList<>();
        LogsBatchQueryResultCollection logsBatchQueryResultCollection = new LogsBatchQueryResultCollection(batchResults);

        BatchResponse batchResponse = response.getValue();

        for (BatchQueryResponse singleQueryResponse : batchResponse.getResponses()) {

            BatchQueryResults queryResults = singleQueryResponse.getBody();
            LogsQueryResult logsQueryResult = getLogsQueryResult(queryResults.getTables(),
                queryResults.getStatistics(), queryResults.getRender(), queryResults.getError());
            LogsBatchQueryResult logsBatchQueryResult = new LogsBatchQueryResult(singleQueryResponse.getId(),
                singleQueryResponse.getStatus(), logsQueryResult.getAllTables(), logsQueryResult.getStatistics(),
                logsQueryResult.getVisualization(), logsQueryResult.getError());
            batchResults.add(logsBatchQueryResult);
        }
        batchResults.sort(Comparator.comparingInt(o -> Integer.parseInt(o.getId())));
        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), logsBatchQueryResultCollection);
    }
}
