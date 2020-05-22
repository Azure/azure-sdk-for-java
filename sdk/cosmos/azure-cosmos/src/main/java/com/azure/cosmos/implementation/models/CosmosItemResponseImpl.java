// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.models;

import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.implementation.CosmosItemProperties;
import com.azure.cosmos.models.CosmosAsyncItemResponse;
import com.azure.cosmos.models.CosmosItemResponse;

import java.time.Duration;
import java.util.Map;

/**
 * Internal implementation of {@link com.azure.cosmos.models.CosmosItemResponse}.
 *
 * @param <T> The type parameter.
 */
public class CosmosItemResponseImpl<T> implements CosmosItemResponse<T> {
    private final CosmosAsyncItemResponse<T> responseWrapper;

    private CosmosItemResponseImpl(CosmosAsyncItemResponse<T> response) {
        this.responseWrapper = response;
    }

    public static <T> CosmosItemResponse<T> fromAsyncResponse(CosmosAsyncItemResponse<T> asyncResponse) {
        return new CosmosItemResponseImpl<>(asyncResponse);
    }

    public static CosmosItemProperties getProperties(CosmosItemResponse<?> itemResponse) {
        return ((CosmosItemResponseImpl<?>) itemResponse).getProperties();
    }

    @Override
    public T getItem() {
        return responseWrapper.getItem();
    }

    /**
     * Gets the itemSettings
     *
     * @return the itemSettings
     */
    public CosmosItemProperties getProperties() {
        return ((CosmosAsyncItemResponseImpl<?>) responseWrapper).getProperties();
    }

    @Override
    public String getMaxResourceQuota() {
        return responseWrapper.getMaxResourceQuota();
    }

    @Override
    public String getCurrentResourceQuotaUsage() {
        return responseWrapper.getCurrentResourceQuotaUsage();
    }

    @Override
    public String getActivityId() {
        return responseWrapper.getActivityId();
    }

    @Override
    public double getRequestCharge() {
        return responseWrapper.getRequestCharge();
    }

    @Override
    public int getStatusCode() {
        return responseWrapper.getStatusCode();
    }

    @Override
    public String getSessionToken() {
        return responseWrapper.getSessionToken();
    }

    @Override
    public Map<String, String> getResponseHeaders() {
        return responseWrapper.getResponseHeaders();
    }

    @Override
    public CosmosDiagnostics getDiagnostics() {
        return responseWrapper.getDiagnostics();
    }

    @Override
    public Duration getDuration() {
        return responseWrapper.getDuration();
    }

    @Override
    public String getETag() {
        return responseWrapper.getETag();
    }
}
