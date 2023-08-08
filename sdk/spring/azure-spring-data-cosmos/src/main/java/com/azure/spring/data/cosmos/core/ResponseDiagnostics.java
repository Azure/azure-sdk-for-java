// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.core;

import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.models.FeedResponse;

/**
 * Diagnostics class of cosmos and feed response
 * <p>
 * NOTE: activityId will be null in case of cross partition queries
 */
public class ResponseDiagnostics {

    private final CosmosDiagnostics cosmosDiagnostics;
    private final CosmosResponseStatistics cosmosResponseStatistics;

    /**
     * Initialization
     *
     * @param cosmosDiagnostics cannot be null
     * @param cosmosResponseStatistics cannot be null
     */
    public ResponseDiagnostics(CosmosDiagnostics cosmosDiagnostics,
                               CosmosResponseStatistics cosmosResponseStatistics) {
        this.cosmosDiagnostics = cosmosDiagnostics;
        this.cosmosResponseStatistics = cosmosResponseStatistics;
    }

    /**
     * To get diagnostics of cosmos response
     *
     * @return CosmosResponseDiagnostics
     */
    public CosmosDiagnostics getCosmosDiagnostics() {
        return cosmosDiagnostics;
    }

    /**
     * To get the statistics value of cosmos response
     *
     * @return CosmosResponseStatistics
     */
    public CosmosResponseStatistics getCosmosResponseStatistics() {
        return cosmosResponseStatistics;
    }

    @Override
    public String toString() {
        final StringBuilder diagnostics = new StringBuilder();
        if (cosmosDiagnostics != null) {
            diagnostics.append("cosmosResponseDiagnostics={")
                       .append(cosmosDiagnostics)
                       .append("}");
        }
        if (cosmosResponseStatistics != null) {
            if (diagnostics.length() != 0) {
                diagnostics.append(", ");
            }
            diagnostics.append("cosmosResponseStatistics={")
                       .append(cosmosResponseStatistics)
                       .append("}");
        }
        return diagnostics.toString();
    }

    /**
     * Generates statistics from cosmos response
     * <p>
     * NOTE: activityId will be null in case of cross partition queries
     */
    public static class CosmosResponseStatistics {

        private final double requestCharge;
        private final String activityId;

        /**
         * Initialization
         *
         * @param feedResponse response from feed
         * @param <T> type of cosmosResponse
         */
        public <T> CosmosResponseStatistics(FeedResponse<T> feedResponse) {
            this.requestCharge = feedResponse.getRequestCharge();
            this.activityId = feedResponse.getActivityId();
        }

        /**
         * To get the charge value of request
         *
         * @return double
         */
        public double getRequestCharge() {
            return requestCharge;
        }

        /**
         * To get the activity id
         *
         * @return String
         */
        public String getActivityId() {
            return activityId;
        }

        @Override
        public String toString() {
            return "CosmosResponseStatistics{"
                + "requestCharge=" + requestCharge + ","
                + "activityId='" + activityId + '\''
                + '}';
        }
    }
}
