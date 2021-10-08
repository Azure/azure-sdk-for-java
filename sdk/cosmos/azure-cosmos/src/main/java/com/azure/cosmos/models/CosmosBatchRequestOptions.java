// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.RequestOptions;
import com.azure.cosmos.util.Beta;

import java.util.HashMap;
import java.util.Map;

/**
 * Encapsulates options that can be specified for a {@link CosmosBatch}.
 */
@Beta(value = Beta.SinceVersion.V4_19_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public final class CosmosBatchRequestOptions {
    private ConsistencyLevel consistencyLevel;
    private String sessionToken;
    private Map<String, String> customOptions;

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
    @Beta(value = Beta.SinceVersion.V4_19_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public String getSessionToken() {
        return sessionToken;
    }

    /**
     * Sets the token for use with session consistency.
     *
     * @param sessionToken the session token.
     * @return the TransactionalBatchRequestOptions.
     */
    @Beta(value = Beta.SinceVersion.V4_19_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public CosmosBatchRequestOptions setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
        return this;
    }

    RequestOptions toRequestOptions() {
        final RequestOptions requestOptions = new RequestOptions();
        requestOptions.setConsistencyLevel(getConsistencyLevel());
        requestOptions.setSessionToken(sessionToken);
        if(this.customOptions != null) {
            for(Map.Entry<String, String> entry : this.customOptions.entrySet()) {
                requestOptions.setHeader(entry.getKey(), entry.getValue());
            }
        }

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

    static {
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
            }
        );
    }
}
