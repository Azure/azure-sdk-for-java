// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.data.cosmosdb.core;

import com.azure.data.cosmos.CosmosResponseDiagnostics;
import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.FeedResponseDiagnostics;
import com.azure.data.cosmos.Resource;

/**
 * Diagnostics class of cosmos and feed response
 */
public class ResponseDiagnostics {

    private CosmosResponseDiagnostics cosmosResponseDiagnostics;
    private FeedResponseDiagnostics feedResponseDiagnostics;
    private CosmosResponseStatistics cosmosResponseStatistics;

    /**
     * Initialization
     *
     * @param cosmosResponseDiagnostics cannot be null
     * @param feedResponseDiagnostics cannot be null
     */
    public ResponseDiagnostics(CosmosResponseDiagnostics cosmosResponseDiagnostics,
                               FeedResponseDiagnostics feedResponseDiagnostics) {
        this.cosmosResponseDiagnostics = cosmosResponseDiagnostics;
        this.feedResponseDiagnostics = feedResponseDiagnostics;
    }

    /**
     * Initialization
     *
     * @param cosmosResponseDiagnostics cannot be null
     * @param feedResponseDiagnostics cannot be null
     * @param cosmosResponseStatistics cannot be null
     */
    public ResponseDiagnostics(CosmosResponseDiagnostics cosmosResponseDiagnostics,
                               FeedResponseDiagnostics feedResponseDiagnostics,
                               CosmosResponseStatistics cosmosResponseStatistics) {
        this.cosmosResponseDiagnostics = cosmosResponseDiagnostics;
        this.feedResponseDiagnostics = feedResponseDiagnostics;
        this.cosmosResponseStatistics = cosmosResponseStatistics;
    }

    /**
     * To get diagnostics of cosmos response
     * @return CosmosResponseDiagnostics
     */
    public CosmosResponseDiagnostics getCosmosResponseDiagnostics() {
        return cosmosResponseDiagnostics;
    }

    /**
     * To set diagnostics of cosmos response
     * @param cosmosResponseDiagnostics cannot be null
     */
    public void setCosmosResponseDiagnostics(CosmosResponseDiagnostics cosmosResponseDiagnostics) {
        this.cosmosResponseDiagnostics = cosmosResponseDiagnostics;
    }

    /**
     * To get diagnostics of feed response
     * @return FeedResponseDiagnostics
     */
    public FeedResponseDiagnostics getFeedResponseDiagnostics() {
        return feedResponseDiagnostics;
    }

    /**
     * To set diagnostics of feed response
     * @param feedResponseDiagnostics cannot be null
     */
    public void setFeedResponseDiagnostics(FeedResponseDiagnostics feedResponseDiagnostics) {
        this.feedResponseDiagnostics = feedResponseDiagnostics;
    }

    /**
     * To get the statistics value of cosmos response
     * @return CosmosResponseStatistics
     */
    public CosmosResponseStatistics getCosmosResponseStatistics() {
        return cosmosResponseStatistics;
    }

    /**
     * To set statistics of cosmos response
     * @param cosmosResponseStatistics cannot be null
     */
    public void setCosmosResponseStatistics(CosmosResponseStatistics cosmosResponseStatistics) {
        this.cosmosResponseStatistics = cosmosResponseStatistics;
    }

    @Override
    public String toString() {
        final StringBuilder diagnostics = new StringBuilder();
        if (cosmosResponseDiagnostics != null) {
            diagnostics.append("cosmosResponseDiagnostics={")
                       .append(cosmosResponseDiagnostics)
                       .append("}");
        }
        if (feedResponseDiagnostics != null) {
            if (diagnostics.length() != 0) {
                diagnostics.append(", ");
            }
            diagnostics.append("feedResponseDiagnostics={")
                       .append(feedResponseDiagnostics)
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
        public <T extends Resource> CosmosResponseStatistics(FeedResponse<T> feedResponse) {
            this.requestCharge = feedResponse.requestCharge();
            this.activityId = feedResponse.activityId();
        }

        /**
         * To get the charge value of request
         * @return double
         */
        public double getRequestCharge() {
            return requestCharge;
        }

        /**
         * To get the activity id
         * @return String
         */
        public String getActivityId() {
            return activityId;
        }

        @Override
        public String toString() {
            return "CosmosResponseStatistics{"
                + "requestCharge="
                + requestCharge
                + ", activityId='"
                + activityId
                + '\''
                + '}';
        }
    }
}
