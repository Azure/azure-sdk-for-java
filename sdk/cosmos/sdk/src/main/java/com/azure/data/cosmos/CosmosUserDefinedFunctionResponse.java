// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.ResourceResponse;
import com.azure.data.cosmos.internal.UserDefinedFunction;

public class CosmosUserDefinedFunctionResponse extends CosmosResponse<CosmosUserDefinedFunctionProperties> {

    private CosmosUserDefinedFunctionProperties cosmosUserDefinedFunctionProperties;
    private CosmosUserDefinedFunction cosmosUserDefinedFunction;

    CosmosUserDefinedFunctionResponse(ResourceResponse<UserDefinedFunction> response, CosmosContainer container) {
        super(response);
        if(response.getResource() != null) {
            super.resourceSettings(new CosmosUserDefinedFunctionProperties(response));
            cosmosUserDefinedFunctionProperties = new CosmosUserDefinedFunctionProperties(response);
            cosmosUserDefinedFunction = new CosmosUserDefinedFunction(cosmosUserDefinedFunctionProperties.id(), container);
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
    public CosmosUserDefinedFunction userDefinedFunction() {
        return cosmosUserDefinedFunction;
    }
}
