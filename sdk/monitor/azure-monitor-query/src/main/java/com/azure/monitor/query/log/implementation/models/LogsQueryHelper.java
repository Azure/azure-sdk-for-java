package com.azure.monitor.query.log.implementation.models;

import com.azure.monitor.query.LogsQueryAsyncClient;
import com.azure.monitor.query.models.LogsBatchQuery;
import com.azure.monitor.query.models.LogsQueryOptions;

import java.time.Duration;
import java.util.List;

/**
 * Helper to access package-private method of {@link LogsBatchQuery} from {@link LogsQueryAsyncClient}.
 */
public final class LogsQueryHelper {
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
}
