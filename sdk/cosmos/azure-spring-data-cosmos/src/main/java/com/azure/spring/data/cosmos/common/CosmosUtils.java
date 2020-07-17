// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.common;

import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.models.FeedResponse;
import com.azure.spring.data.cosmos.core.ResponseDiagnostics;
import com.azure.spring.data.cosmos.core.ResponseDiagnosticsProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Util class to fill and process response diagnostics
 */
public class CosmosUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(CosmosUtils.class);

    /**
     * Generate ResponseDiagnostics with cosmos and feed response diagnostics
     *
     * @param <T> type of cosmosResponse
     * @param responseDiagnosticsProcessor collect Response Diagnostics from API responses and then set in {@link
     * ResponseDiagnostics} object.
     * @param cosmosDiagnostics response from cosmos
     * @param feedResponse response from feed
     */
    public static <T> void fillAndProcessResponseDiagnostics(
        ResponseDiagnosticsProcessor responseDiagnosticsProcessor,
        CosmosDiagnostics cosmosDiagnostics, FeedResponse<T> feedResponse) {
        if (responseDiagnosticsProcessor == null) {
            return;
        }
        ResponseDiagnostics.CosmosResponseStatistics cosmosResponseStatistics = null;
        if (feedResponse != null) {
            cosmosResponseStatistics = new ResponseDiagnostics.CosmosResponseStatistics(feedResponse);
        }
        if (cosmosDiagnostics == null && cosmosResponseStatistics == null) {
            LOGGER.debug("Empty response diagnostics");
            return;
        }
        final ResponseDiagnostics responseDiagnostics =
            new ResponseDiagnostics(cosmosDiagnostics, cosmosResponseStatistics);

        //  Process response diagnostics
        responseDiagnosticsProcessor.processResponseDiagnostics(responseDiagnostics);
    }
}
