// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;


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
        super(resourceResponse.properties());
        this.asyncResponse = resourceResponse;
        this.cosmosStoredProcedure = storedProcedure;
    }

    /**
     * Gets cosmos stored procedure properties.
     *
     * @return the cosmos stored procedure properties
     */
    public CosmosStoredProcedureProperties properties() {
        return asyncResponse.properties();
    }

    /**
     * Gets cosmos sync stored procedure.
     *
     * @return the cosmos sync stored procedure
     */
    public CosmosStoredProcedure storedProcedure() {
        return cosmosStoredProcedure;
    }

    @Override
    public String activityId() {
        return asyncResponse.activityId();
    }

    @Override
    public String sessionToken() {
        return asyncResponse.sessionToken();
    }

    @Override
    public int statusCode() {
        return asyncResponse.statusCode();
    }

    @Override
    public double requestCharge() {
        return asyncResponse.requestCharge();
    }

    /**
     * Response as string string.
     *
     * @return the string
     */
    public String responseAsString() {
        return asyncResponse.responseAsString();
    }

    /**
     * Script log string.
     *
     * @return the string
     */
    public String scriptLog() {
        return asyncResponse.scriptLog();
    }


}
