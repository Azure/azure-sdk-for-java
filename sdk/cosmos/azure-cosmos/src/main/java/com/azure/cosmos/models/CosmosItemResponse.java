// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.CosmosDiagnostics;

import java.time.Duration;
import java.util.Map;

/**
 * The type Cosmos item response. This contains the item and response methods
 *
 * @param <T> the type parameter
 */
public interface CosmosItemResponse<T> {
    /**
     * Gets the resource.
     *
     * @return the resource
     */
    T getItem();

    /**
     * Gets the maximum size limit for this entity (in megabytes (MB) for server resources and in count for master
     * resources).
     *
     * @return the max resource quota.
     */
    String getMaxResourceQuota();

    /**
     * Gets the current size of this entity (in megabytes (MB) for server resources and in count for master resources)
     *
     * @return the current resource quota usage.
     */
    String getCurrentResourceQuotaUsage();

    /**
     * Gets the Activity ID for the request.
     *
     * @return the activity getId.
     */
    String getActivityId();

    /**
     * Gets the request charge as request units (RU) consumed by the operation.
     * <p>
     * For more information about the RU and factors that can impact the effective charges please visit
     * <a href="https://docs.microsoft.com/en-us/azure/cosmos-db/request-units">Request Units in Azure Cosmos DB</a>
     *
     * @return the request charge.
     */
    double getRequestCharge();

    /**
     * Gets the HTTP status code associated with the response.
     *
     * @return the status code.
     */
    int getStatusCode();

    /**
     * Gets the token used for managing client's consistency requirements.
     *
     * @return the session token.
     */
    String getSessionToken();

    /**
     * Gets the headers associated with the response.
     *
     * @return the response headers.
     */
    Map<String, String> getResponseHeaders();

    /**
     * Gets the diagnostics information for the current request to Azure Cosmos DB service.
     *
     * @return diagnostics information for the current request to Azure Cosmos DB service.
     */
    CosmosDiagnostics getDiagnostics();

    /**
     * Gets the end-to-end request latency for the current request to Azure Cosmos DB service.
     *
     * @return end-to-end request latency for the current request to Azure Cosmos DB service.
     */
    Duration getDuration();

    /**
     * Gets the ETag from the response headers.
     * This is only relevant when getting response from the server.
     *
     * Null in case of delete operation.
     *
     * @return ETag
     */
    String getETag();
}
