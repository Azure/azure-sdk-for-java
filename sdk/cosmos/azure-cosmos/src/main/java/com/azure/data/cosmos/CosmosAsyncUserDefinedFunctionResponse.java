// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.ResourceResponse;
import com.azure.data.cosmos.internal.UserDefinedFunction;

public class CosmosAsyncUserDefinedFunctionResponse extends CosmosResponse<CosmosUserDefinedFunctionProperties> {

    private CosmosUserDefinedFunctionProperties cosmosUserDefinedFunctionProperties;
    private CosmosAsyncUserDefinedFunction cosmosUserDefinedFunction;

    CosmosAsyncUserDefinedFunctionResponse(ResourceResponse<UserDefinedFunction> response, CosmosAsyncContainer container) {
        super(response);
        if(response.getResource() != null) {
            super.setProperties(new CosmosUserDefinedFunctionProperties(response));
            cosmosUserDefinedFunctionProperties = new CosmosUserDefinedFunctionProperties(response);
            cosmosUserDefinedFunction = new CosmosAsyncUserDefinedFunction(cosmosUserDefinedFunctionProperties.getId(), container);
        }
    }

    /**
     * Gets the cosmos getUser defined function getProperties
     * @return the cosmos getUser defined function getProperties
     */
    public CosmosUserDefinedFunctionProperties getProperties() {
        return cosmosUserDefinedFunctionProperties;
    }

    /**
     * Gets the cosmos user defined function object
     * @return the cosmos user defined function object
     */
    public CosmosAsyncUserDefinedFunction getUserDefinedFunction() {
        return cosmosUserDefinedFunction;
    }
}
