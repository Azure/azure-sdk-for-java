// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.implementation.ResourceResponse;
import com.azure.cosmos.implementation.StoredProcedure;
import com.azure.cosmos.implementation.StoredProcedureResponse;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;

import java.time.Duration;
import java.util.Map;

/**
 * The type Cosmos stored procedure response.
 */
public class CosmosStoredProcedureResponse extends CosmosResponse<CosmosStoredProcedureProperties> {

    private final StoredProcedureResponse storedProcedureResponse;

    CosmosStoredProcedureResponse(ResourceResponse<StoredProcedure> response) {
        super(response);
        String bodyAsString = response.getBodyAsString();
        if (!StringUtils.isEmpty(bodyAsString)) {
            super.setProperties(new CosmosStoredProcedureProperties(bodyAsString));
        }
        storedProcedureResponse = null;
    }

    CosmosStoredProcedureResponse(StoredProcedureResponse response) {
        super(null, null);
        this.storedProcedureResponse = response;

    }

    /**
     * Gets the stored procedure properties
     *
     * @return the stored procedure properties or null
     */
    public CosmosStoredProcedureProperties getProperties() {
        return super.getProperties();
    }

    /**
     * Gets the Activity ID for the request.
     *
     * @return the activity id.
     */
    @Override
    public String getActivityId() {
        if (storedProcedureResponse != null) {
            return this.storedProcedureResponse.getActivityId();
        }
        return super.getActivityId();
    }

    /**
     * Gets the token used for managing client's consistency requirements.
     *
     * @return the session token.
     */
    @Override
    public String getSessionToken() {
        if (storedProcedureResponse != null) {
            return this.storedProcedureResponse.getSessionToken();
        }
        return super.getSessionToken();
    }

    /**
     * Gets the HTTP status code associated with the response.
     *
     * @return the status code.
     */
    @Override
    public int getStatusCode() {
        if (storedProcedureResponse != null) {
            return this.storedProcedureResponse.getStatusCode();
        }
        return super.getStatusCode();
    }

    /**
     * Gets the number of normalized requests charged.
     *
     * @return the request charge.
     */
    @Override
    public double getRequestCharge() {
        if (storedProcedureResponse != null) {
            return storedProcedureResponse.getRequestCharge();
        }
        return super.getRequestCharge();
    }

    @Override
    public String getMaxResourceQuota() {
        if (storedProcedureResponse != null) {
            return storedProcedureResponse.getMaxResourceQuota();
        }
        return super.getMaxResourceQuota();
    }

    @Override
    public String getCurrentResourceQuotaUsage() {
        if (storedProcedureResponse != null) {
            return storedProcedureResponse.getCurrentResourceQuotaUsage();
        }
        return super.getCurrentResourceQuotaUsage();
    }

    @Override
    public Map<String, String> getResponseHeaders() {
        if (storedProcedureResponse != null) {
            return storedProcedureResponse.getResponseHeaders();
        }
        return super.getResponseHeaders();
    }

    @Override
    public CosmosDiagnostics getDiagnostics() {
        if (storedProcedureResponse != null) {
            return storedProcedureResponse.getCosmosDiagnostics();
        }
        return super.getDiagnostics();
    }

    @Override
    public Duration getDuration() {
        if (storedProcedureResponse != null) {
            return storedProcedureResponse.getCosmosDiagnostics().getDuration();
        }
        return super.getDuration();
    }

    /**
     * Gets the response of the stored procedure as a string.
     *
     * @return the response as a string.
     */
    public String getResponseAsString() {
        if (storedProcedureResponse != null) {
            return storedProcedureResponse.getResponseAsString();
        }
        return null;
    }

    /**
     * Gets the output from stored procedure console.log() statements.
     *
     * @return the output string from the stored procedure console.log() statements.
     */
    public String getScriptLog() {
        if (storedProcedureResponse != null) {
            return this.storedProcedureResponse.getScriptLog();
        }
        return null;
    }
}
