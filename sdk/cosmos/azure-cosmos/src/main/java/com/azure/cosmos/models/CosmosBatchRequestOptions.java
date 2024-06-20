// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosDiagnosticsThresholds;
import com.azure.cosmos.CosmosItemSerializer;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.RequestOptions;
import com.azure.cosmos.implementation.apachecommons.collections.list.UnmodifiableList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Encapsulates options that can be specified for a {@link CosmosBatch}.
 */
public final class CosmosBatchRequestOptions {
    private ConsistencyLevel consistencyLevel;
    private String sessionToken;
    private Map<String, String> customOptions;
    private CosmosDiagnosticsThresholds thresholds = new CosmosDiagnosticsThresholds();
    private List<String> excludeRegions;

    private CosmosItemSerializer customSerializer;

    /**
     * Creates an instance of the CosmosBatchRequestOptions class
     */
    public CosmosBatchRequestOptions() {
    }


    CosmosBatchRequestOptions(CosmosBatchRequestOptions toBeCloned) {
        this.consistencyLevel = toBeCloned.consistencyLevel;
        this.sessionToken = toBeCloned.sessionToken;
        this.customOptions = toBeCloned.customOptions;
        this.thresholds = toBeCloned.thresholds;
        this.customSerializer = toBeCloned.customSerializer;

        if (toBeCloned.excludeRegions != null) {
            this.excludeRegions = new ArrayList<>(toBeCloned.excludeRegions);
        }
    }

    /**
     * Gets the consistency level required for the request.
     *
     * @return the consistency level.
     */
    ConsistencyLevel getConsistencyLevel() {
        return consistencyLevel;
    }

    /**
     * Sets the consistency level required for the request.
     *
     * @param consistencyLevel the consistency level.
     * @return the TransactionalBatchRequestOptions.
     */
    CosmosBatchRequestOptions setConsistencyLevel(ConsistencyLevel consistencyLevel) {
        this.consistencyLevel = consistencyLevel;
        return this;
    }

    /**
     * Gets the token for use with session consistency.
     *
     * @return the session token.
     */
    public String getSessionToken() {
        return sessionToken;
    }

    /**
     * Sets the token for use with session consistency.
     *
     * @param sessionToken the session token.
     * @return the TransactionalBatchRequestOptions.
     */
    public CosmosBatchRequestOptions setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
        return this;
    }

    /**
     * Allows overriding the diagnostic thresholds for a specific operation.
     * @param operationSpecificThresholds the diagnostic threshold override for this operation
     * @return the TransactionalBatchRequestOptions.
     */
    public CosmosBatchRequestOptions setDiagnosticsThresholds(
        CosmosDiagnosticsThresholds operationSpecificThresholds) {

        this.thresholds = operationSpecificThresholds;
        return this;
    }

    /**
     * Gets the diagnostic thresholds used as an override for a specific operation. If no operation specific
     * diagnostic threshold has been specified, this method will return null, although at runtime the default
     * thresholds specified at the client-level will be used.
     * @return the diagnostic thresholds used as an override for a specific operation.
     */
    public CosmosDiagnosticsThresholds getDiagnosticsThresholds() {
        return this.thresholds;
    }

    RequestOptions toRequestOptions() {
        final RequestOptions requestOptions = new RequestOptions();
        requestOptions.setConsistencyLevel(getConsistencyLevel());
        requestOptions.setSessionToken(sessionToken);
        requestOptions.setDiagnosticsThresholds(thresholds);
        requestOptions.setEffectiveItemSerializer(this.customSerializer);

        if(this.customOptions != null) {
            for(Map.Entry<String, String> entry : this.customOptions.entrySet()) {
                requestOptions.setHeader(entry.getKey(), entry.getValue());
            }
        }
        requestOptions.setExcludedRegions(excludeRegions);

        return requestOptions;
    }

    /**
     * Sets the custom batch request option value by key
     *
     * @param name  a string representing the custom option's name
     * @param value a string representing the custom option's value
     *
     * @return the CosmosBatchRequestOptions.
     */
    CosmosBatchRequestOptions setHeader(String name, String value) {
        if (this.customOptions == null) {
            this.customOptions = new HashMap<>();
        }
        this.customOptions.put(name, value);
        return this;
    }

    /**
     * List of regions to exclude for the request/retries. Example "East US" or "East US, West US"
     * These regions will be excluded from the preferred regions list
     *
     * @param excludeRegions list of regions
     * @return the {@link CosmosBatchRequestOptions}
     */
    public CosmosBatchRequestOptions setExcludedRegions(List<String> excludeRegions) {
        this.excludeRegions = excludeRegions;
        return this;
    }

    /**
     * Gets the list of regions to be excluded for the request/retries. These regions are excluded
     * from the preferred region list.
     *
     * @return a list of excluded regions
     * */
    public List<String> getExcludedRegions() {
        if (this.excludeRegions == null) {
            return null;
        }
        return UnmodifiableList.unmodifiableList(this.excludeRegions);
    }

    /**
     * Gets the custom item serializer defined for this instance of request options
     * @return the custom item serializer
     */
    public CosmosItemSerializer getCustomItemSerializer() {
        return this.customSerializer;
    }

    /**
     * Allows specifying a custom item serializer to be used for this operation. If the serializer
     * on the request options is null, the serializer on CosmosClientBuilder is used. If both serializers
     * are null (the default), an internal Jackson ObjectMapper is ued for serialization/deserialization.
     * @param customItemSerializer the custom item serializer for this operation
     * @return  the CosmosItemRequestOptions.
     */
    public CosmosBatchRequestOptions setCustomItemSerializer(CosmosItemSerializer customItemSerializer) {
        this.customSerializer = customItemSerializer;

        return this;
    }

    /**
     * Gets the custom batch request options
     *
     * @return Map of custom request options
     */
    Map<String, String> getHeaders() {
        return this.customOptions;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // the following helper/accessor only helps to access this class outside of this package.//
    ///////////////////////////////////////////////////////////////////////////////////////////
    static void initialize() {
        ImplementationBridgeHelpers.CosmosBatchRequestOptionsHelper.setCosmosBatchRequestOptionsAccessor(
            new ImplementationBridgeHelpers.CosmosBatchRequestOptionsHelper.CosmosBatchRequestOptionsAccessor() {
                @Override
                public ConsistencyLevel getConsistencyLevel(CosmosBatchRequestOptions cosmosBatchRequestOptions) {
                    return cosmosBatchRequestOptions.getConsistencyLevel();
                }

                @Override
                public CosmosBatchRequestOptions setConsistencyLevel(CosmosBatchRequestOptions cosmosBatchRequestOptions, ConsistencyLevel consistencyLevel) {
                    return cosmosBatchRequestOptions.setConsistencyLevel(consistencyLevel);
                }

                @Override
                public CosmosBatchRequestOptions setHeader(CosmosBatchRequestOptions cosmosItemRequestOptions,
                                                           String name, String value) {
                    return cosmosItemRequestOptions.setHeader(name, value);
                }

                @Override
                public Map<String, String> getHeader(CosmosBatchRequestOptions cosmosItemRequestOptions) {
                    return cosmosItemRequestOptions.getHeaders();
                }

                @Override
                public CosmosBatchRequestOptions clone(CosmosBatchRequestOptions toBeCloned) {
                    return new CosmosBatchRequestOptions(toBeCloned);
                }

            }
        );
    }

    static { initialize(); }
}
