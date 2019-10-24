// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;


/**
 * The type Cosmos sync stored procedure response.
 */
public class CosmosStoredProcedureResponse extends CosmosResponse<CosmosStoredProcedureProperties> {
    private final CosmosStoredProcedure cosmosStoredProcedure;
    private final CosmosAsyncStoredProcedureResponse asyncResponse;

    /**
     * Instantiates a new Cosmos sync stored procedure response.
     *
     * @param resourceResponse the resource response
     * @param storedProcedure the stored procedure
     */
    CosmosStoredProcedureResponse(CosmosAsyncStoredProcedureResponse resourceResponse,
                                  CosmosStoredProcedure storedProcedure) {
        super(resourceResponse.getProperties());
        this.asyncResponse = resourceResponse;
        this.cosmosStoredProcedure = storedProcedure;
    }

    /**
     * Gets cosmos stored procedure properties.
     *
     * @return the cosmos stored procedure properties
     */
    public CosmosStoredProcedureProperties getProperties() {
        return asyncResponse.getProperties();
    }

    /**
     * Gets cosmos sync stored procedure.
     *
     * @return the cosmos sync stored procedure
     */
    public CosmosStoredProcedure getStoredProcedure() {
        return cosmosStoredProcedure;
    }

    @Override
    public String getActivityId() {
        return asyncResponse.getActivityId();
    }

    @Override
    public String getSessionToken() {
        return asyncResponse.getSessionToken();
    }

    @Override
    public int getStatusCode() {
        return asyncResponse.getStatusCode();
    }

    @Override
    public double getRequestCharge() {
        return asyncResponse.getRequestCharge();
    }

    /**
     * Response as string string.
     *
     * @return the string
     */
    public String responseAsString() {
        return asyncResponse.getResponseAsString();
    }

    /**
     * Script log string.
     *
     * @return the string
     */
    public String scriptLog() {
        return asyncResponse.getScriptLog();
    }


}
