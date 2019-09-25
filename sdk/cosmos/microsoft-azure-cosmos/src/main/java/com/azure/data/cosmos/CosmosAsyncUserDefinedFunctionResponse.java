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
            super.properties(new CosmosUserDefinedFunctionProperties(response));
            cosmosUserDefinedFunctionProperties = new CosmosUserDefinedFunctionProperties(response);
            cosmosUserDefinedFunction = new CosmosAsyncUserDefinedFunction(cosmosUserDefinedFunctionProperties.id(), container);
        }
    }

    /**
     * Gets the cosmos user defined function properties
     * @return the cosmos user defined function properties
     */
    public CosmosUserDefinedFunctionProperties properties() {
        return cosmosUserDefinedFunctionProperties;
    }

    /**
     * Gets the cosmos user defined function object
     * @return the cosmos user defined function object
     */
    public CosmosAsyncUserDefinedFunction userDefinedFunction() {
        return cosmosUserDefinedFunction;
    }
}
