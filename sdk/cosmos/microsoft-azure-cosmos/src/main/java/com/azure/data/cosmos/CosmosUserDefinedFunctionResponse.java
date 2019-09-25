// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;

/**
 * The type Cosmos sync user defined function response.
 */
public class CosmosUserDefinedFunctionResponse extends CosmosResponse<CosmosUserDefinedFunctionProperties> {

    private final CosmosUserDefinedFunction userDefinedFunction;
    private CosmosAsyncUserDefinedFunctionResponse asyncResponse;

    /**
     * Instantiates a new Cosmos sync user defined function response.
     *
     * @param resourceResponse the resource response
     * @param userDefinedFunction the user defined function
     */
    CosmosUserDefinedFunctionResponse(CosmosAsyncUserDefinedFunctionResponse resourceResponse,
                                      CosmosUserDefinedFunction userDefinedFunction) {
        super(resourceResponse.properties());
        this.asyncResponse = resourceResponse;
        this.userDefinedFunction = userDefinedFunction;
    }

    /**
     * Gets cosmos user defined function properties.
     *
     * @return the cosmos user defined function properties
     */
    public CosmosUserDefinedFunctionProperties properties() {
        return asyncResponse.properties();
    }

    /**
     * Gets cosmos sync user defined function.
     *
     * @return the cosmos sync user defined function
     */
    public CosmosUserDefinedFunction userDefinedFunction() {
        return userDefinedFunction;
    }

}
