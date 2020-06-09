// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.data.cosmosdb.common;

import com.azure.data.cosmos.CosmosResponse;
import com.azure.data.cosmos.CosmosResponseDiagnostics;
import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.FeedResponseDiagnostics;
import com.azure.data.cosmos.Resource;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.spring.data.cosmosdb.core.ResponseDiagnostics;
import com.microsoft.azure.spring.data.cosmosdb.core.ResponseDiagnosticsProcessor;
import com.microsoft.azure.spring.data.cosmosdb.core.convert.ObjectMapperFactory;
import com.microsoft.azure.spring.data.cosmosdb.exception.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

import java.io.IOException;

public class CosmosdbUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(CosmosdbUtils.class);

    @SuppressWarnings("unchecked")
    public static <T> T getCopyFrom(@NonNull T instance) {
        final ObjectMapper mapper = ObjectMapperFactory.getObjectMapper();

        try {
            final String s = mapper.writeValueAsString(instance);
            return (T) mapper.readValue(s, instance.getClass());
        } catch (IOException e) {
            throw new ConfigurationException("failed to get copy from " + instance.getClass().getName(), e);
        }
    }

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
        if (cosmosResponseDiagnostics == null &&
            (feedResponseDiagnostics == null || feedResponseDiagnostics.toString().isEmpty()) &&
            cosmosResponseStatistics == null) {
            LOGGER.debug("Empty response diagnostics");
            return;
        }
        final ResponseDiagnostics responseDiagnostics =
            new ResponseDiagnostics(cosmosResponseDiagnostics, feedResponseDiagnostics, cosmosResponseStatistics);

        //  Process response diagnostics
        responseDiagnosticsProcessor.processResponseDiagnostics(responseDiagnostics);
    }
}
