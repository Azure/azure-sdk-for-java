// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration.models;

import com.azure.core.annotation.Immutable;

/**
 * The AzureAppInsightsDataFeedSource model.
 */
@Immutable
public final class AzureAppInsightsDataFeedSource extends DataFeedSource {

    /*
     * Azure cloud environment
     */
    private final String azureCloud;

    /*
     * Azure Application Insights ID
     */
    private final String applicationId;

    /*
     * API Key
     */
    private final String apiKey;

    /*
     * Query
     */
    private final String query;

    /**
     * Create a AzureAppInsightsDataFeedSource instance.
     *
     * @param applicationId the Azure Application Insights ID
     * @param apiKey the Azure Application Insights API key
     * @param azureCloud the Azure cloud environment
     * @param query the query
     */
    public AzureAppInsightsDataFeedSource(final String applicationId, final String apiKey,
        String azureCloud, final String query) {
        this.applicationId = applicationId;
        this.apiKey = apiKey;
        this.query = query;
        this.azureCloud = azureCloud;
    }

    /**
     * Get the Azure cloud environment.
     *
     * @return the azureCloud value.
     */
    public String getAzureCloud() {
        return this.azureCloud;
    }

    /**
     * Get the Azure Application Insights ID.
     *
     * @return the applicationId value.
     */
    public String getApplicationId() {
        return applicationId;
    }

    /**
     * Get the apiKey property: API Key.
     *
     * @return the apiKey value.
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * Get the query property.
     *
     * @return the query value.
     */
    public String getQuery() {
        return query;
    }
}
