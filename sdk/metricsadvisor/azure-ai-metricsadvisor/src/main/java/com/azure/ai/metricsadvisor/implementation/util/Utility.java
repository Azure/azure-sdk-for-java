// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.implementation.util;

import com.azure.ai.metricsadvisor.administration.models.DataFeedIngestionProgress;
import com.azure.ai.metricsadvisor.administration.models.DataFeedIngestionStatus;
import com.azure.ai.metricsadvisor.administration.models.IngestionStatusType;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Helper utility class to manage common methods.
 */
public final class Utility {
    private static final ClientLogger LOGGER = new ClientLogger(Utility.class);
    private static final String HTTP_REST_PROXY_SYNC_PROXY_ENABLE = "com.azure.core.http.restproxy.syncproxy.enable";
    private static final Context CONTEXT_WITH_SYNC = new Context(HTTP_REST_PROXY_SYNC_PROXY_ENABLE, true);

    /**
     * Extracts the result ID from the location URL.
     *
     * @param operationLocation The URL specified in the 'Location' response header containing the
     * resultId used to track the progress and obtain the result of the operation.
     *
     * @return The resultId used to track the progress.
     */
    public static String parseOperationId(String operationLocation) {
        if (!CoreUtils.isNullOrEmpty(operationLocation)) {
            int lastIndex = operationLocation.lastIndexOf('/');
            if (lastIndex != -1) {
                return operationLocation.substring(lastIndex + 1);
            }
        }
        throw LOGGER.logExceptionAsError(
            new RuntimeException("Failed to parse operation header for result Id from: " + operationLocation));
    }

    /**
     * Returns toString or null if object passed is null.
     * @param obj the object
     * @return Returns toString or null if object passed is null.
     */
    public static String toStringOrNull(Object obj) {
        return obj != null ? obj.toString() : null;
    }

    public static Context enableSync(Context context) {
        if (context == null || context == Context.NONE) {
            return CONTEXT_WITH_SYNC;
        }

        return context.addData(HTTP_REST_PROXY_SYNC_PROXY_ENABLE, true);
    }

    public static List<DataFeedIngestionStatus> toDataFeedIngestionStatus(List<com.azure.ai.metricsadvisor.implementation.models.DataFeedIngestionStatus> ingestionStatusList) {
        return ingestionStatusList
            .stream()
            .map(ingestionStatus -> {
                DataFeedIngestionStatus dataFeedIngestionStatus = new DataFeedIngestionStatus();
                DataFeedIngestionStatusHelper.setMessage(dataFeedIngestionStatus, ingestionStatus.getMessage());
                DataFeedIngestionStatusHelper.setIngestionStatusType(dataFeedIngestionStatus, IngestionStatusType.fromString(toStringOrNull(ingestionStatus.getStatus())));
                DataFeedIngestionStatusHelper.setTimestamp(dataFeedIngestionStatus, ingestionStatus.getTimestamp());
                return dataFeedIngestionStatus;
            })
            .collect(Collectors.toList());
    }

    public static DataFeedIngestionProgress toDataFeedIngestionProgress(
        com.azure.ai.metricsadvisor.implementation.models.DataFeedIngestionProgress dataFeedIngestionProgressResponse) {
        DataFeedIngestionProgress dataFeedIngestionProgress = new DataFeedIngestionProgress();
        DataFeedIngestionProgressHelper.setLatestActiveTimestamp(dataFeedIngestionProgress, dataFeedIngestionProgressResponse.getLatestActiveTimestamp());
        DataFeedIngestionProgressHelper.setLatestSuccessTimestamp(dataFeedIngestionProgress, dataFeedIngestionProgressResponse.getLatestSuccessTimestamp());
        return dataFeedIngestionProgress;
    }
}
