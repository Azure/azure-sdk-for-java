// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.implementation.ResourceResponse;
import com.azure.cosmos.implementation.UserDefinedFunction;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;

/**
 * The type Cosmos user defined function response.
 */
public class CosmosUserDefinedFunctionResponse extends CosmosResponse<CosmosUserDefinedFunctionProperties> {

    private final CosmosUserDefinedFunctionProperties cosmosUserDefinedFunctionProperties;

    CosmosUserDefinedFunctionResponse(ResourceResponse<UserDefinedFunction> response) {
        super(response);
        String bodyAsString = response.getBodyAsString();
        if (StringUtils.isEmpty(bodyAsString)) {
            cosmosUserDefinedFunctionProperties = null;
        } else {
            cosmosUserDefinedFunctionProperties = new CosmosUserDefinedFunctionProperties(bodyAsString);
            super.setProperties(cosmosUserDefinedFunctionProperties);
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
}
