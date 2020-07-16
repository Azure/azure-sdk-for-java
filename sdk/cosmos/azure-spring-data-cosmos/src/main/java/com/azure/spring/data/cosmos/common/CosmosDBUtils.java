// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.common;

import com.azure.data.cosmos.CosmosResponse;
import com.azure.data.cosmos.CosmosResponseDiagnostics;
import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.FeedResponseDiagnostics;
import com.azure.data.cosmos.Resource;
import com.azure.spring.data.cosmos.core.convert.ObjectMapperFactory;
import com.azure.spring.data.cosmos.exception.ConfigurationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.azure.spring.data.cosmos.core.ResponseDiagnostics;
import com.azure.spring.data.cosmos.core.ResponseDiagnosticsProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

import java.io.IOException;

/**
 * Util class to fill and process response diagnostics
 */
public class CosmosdbUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(CosmosdbUtils.class);

    /**
     * Get a copy of an existing instance
     * @param instance the known instance
     * @param <T> type of instance
     * @return copy instance
     * @throws ConfigurationException if the class type is invalid
     */
    @SuppressWarnings("unchecked")
    public static <T> T getCopyFrom(@NonNull T instance) {
        final ObjectMapper mapper = ObjectMapperFactory.getObjectMapper();

        try {
            final String s = mapper.writeValueAsString(instance);
            return (T) mapper.readValue(s, instance.getClass());
        } catch (IOException e) {
            throw new ConfigurationException("failed to get copy from "
                    + instance.getClass().getName(), e);
        }
    }

    /**
     * Generate ResponseDiagnostics with cosmos and feed response diagnostics
     *
     * @param <T> type of cosmosResponse
     * @param responseDiagnosticsProcessor collect Response Diagnostics from API responses and
     * then set in {@link ResponseDiagnostics} object.
     * @param cosmosResponse response from cosmos
     * @param feedResponse response from feed
     */
    public static <T extends Resource> void fillAndProcessResponseDiagnostics(
        ResponseDiagnosticsProcessor responseDiagnosticsProcessor,
        CosmosResponse<T> cosmosResponse, FeedResponse<T> feedResponse) {
        if (responseDiagnosticsProcessor == null) {
            return;
        }
        CosmosResponseDiagnostics cosmosResponseDiagnostics = null;
        if (cosmosResponse != null) {
            cosmosResponseDiagnostics = cosmosResponse.cosmosResponseDiagnosticsString();
        }
        FeedResponseDiagnostics feedResponseDiagnostics = null;
        ResponseDiagnostics.CosmosResponseStatistics cosmosResponseStatistics = null;
        if (feedResponse != null) {
            feedResponseDiagnostics = feedResponse.feedResponseDiagnostics();
            cosmosResponseStatistics = new ResponseDiagnostics.CosmosResponseStatistics(feedResponse);
        }
        if (cosmosResponseDiagnostics == null
            && (feedResponseDiagnostics == null || feedResponseDiagnostics.toString().isEmpty())
            && cosmosResponseStatistics == null) {
            LOGGER.debug("Empty response diagnostics");
            return;
        }
        final ResponseDiagnostics responseDiagnostics =
            new ResponseDiagnostics(cosmosResponseDiagnostics, feedResponseDiagnostics, cosmosResponseStatistics);

        //  Process response diagnostics
        responseDiagnosticsProcessor.processResponseDiagnostics(responseDiagnostics);
    }
}
