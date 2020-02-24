// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.ResourceResponse;
import com.azure.cosmos.implementation.UserDefinedFunction;
import org.apache.commons.lang3.StringUtils;

public class CosmosAsyncUserDefinedFunctionResponse extends CosmosResponse<CosmosUserDefinedFunctionProperties> {

    private final CosmosUserDefinedFunctionProperties cosmosUserDefinedFunctionProperties;
    private final CosmosAsyncUserDefinedFunction cosmosUserDefinedFunction;

    CosmosAsyncUserDefinedFunctionResponse(ResourceResponse<UserDefinedFunction> response,
                                           CosmosAsyncContainer container) {
        super(response);
        String bodyAsString = response.getBodyAsString();
        if (StringUtils.isEmpty(bodyAsString)) {
            cosmosUserDefinedFunctionProperties = null;
            cosmosUserDefinedFunction = null;
        } else {
            cosmosUserDefinedFunctionProperties = new CosmosUserDefinedFunctionProperties(bodyAsString);
            super.setProperties(cosmosUserDefinedFunctionProperties);
            cosmosUserDefinedFunction = new CosmosAsyncUserDefinedFunction(cosmosUserDefinedFunctionProperties.getId(), container);
        }
    }

    /**
     * Gets the cosmos getUser defined function getProperties
     *
     * @return the cosmos getUser defined function getProperties
     */
    public CosmosUserDefinedFunctionProperties getProperties() {
        return cosmosUserDefinedFunctionProperties;
    }

    /**
     * Gets the cosmos user defined function object
     *
     * @return the cosmos user defined function object
     */
    public CosmosAsyncUserDefinedFunction getUserDefinedFunction() {
        return cosmosUserDefinedFunction;
    }
}
