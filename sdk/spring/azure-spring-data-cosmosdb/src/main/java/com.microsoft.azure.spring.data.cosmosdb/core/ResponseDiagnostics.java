// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.data.cosmosdb.core;

import com.azure.data.cosmos.CosmosResponseDiagnostics;
import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.FeedResponseDiagnostics;
import com.azure.data.cosmos.Resource;

public class ResponseDiagnostics {

    private CosmosResponseDiagnostics cosmosResponseDiagnostics;
    private FeedResponseDiagnostics feedResponseDiagnostics;
    private CosmosResponseStatistics cosmosResponseStatistics;

    public ResponseDiagnostics(CosmosResponseDiagnostics cosmosResponseDiagnostics,
                               FeedResponseDiagnostics feedResponseDiagnostics) {
        this.cosmosResponseDiagnostics = cosmosResponseDiagnostics;
        this.feedResponseDiagnostics = feedResponseDiagnostics;
    }

    public ResponseDiagnostics(CosmosResponseDiagnostics cosmosResponseDiagnostics,
                               FeedResponseDiagnostics feedResponseDiagnostics,
                               CosmosResponseStatistics cosmosResponseStatistics) {
        this.cosmosResponseDiagnostics = cosmosResponseDiagnostics;
        this.feedResponseDiagnostics = feedResponseDiagnostics;
        this.cosmosResponseStatistics = cosmosResponseStatistics;
    }

    public CosmosResponseDiagnostics getCosmosResponseDiagnostics() {
        return cosmosResponseDiagnostics;
    }

    public void setCosmosResponseDiagnostics(CosmosResponseDiagnostics cosmosResponseDiagnostics) {
        this.cosmosResponseDiagnostics = cosmosResponseDiagnostics;
    }

    public FeedResponseDiagnostics getFeedResponseDiagnostics() {
        return feedResponseDiagnostics;
    }

    public void setFeedResponseDiagnostics(FeedResponseDiagnostics feedResponseDiagnostics) {
        this.feedResponseDiagnostics = feedResponseDiagnostics;
    }

    public CosmosResponseStatistics getCosmosResponseStatistics() {
        return cosmosResponseStatistics;
    }

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

    public static class CosmosResponseStatistics {

        private final double requestCharge;
        private final String activityId;

        public <T extends Resource> CosmosResponseStatistics(FeedResponse<T> feedResponse) {
            this.requestCharge = feedResponse.requestCharge();
            this.activityId = feedResponse.activityId();
        }

        public double getRequestCharge() {
            return requestCharge;
        }

        public String getActivityId() {
            return activityId;
        }

        @Override
        public String toString() {
            return "CosmosResponseStatistics{" +
                "requestCharge=" + requestCharge +
                ", activityId='" + activityId + '\'' +
                '}';
        }
    }
}
