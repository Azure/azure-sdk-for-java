// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.sync;

import com.azure.data.cosmos.CosmosUserDefinedFunctionProperties;
import com.azure.data.cosmos.CosmosUserDefinedFunctionResponse;

/**
 * The type Cosmos sync user defined function response.
 */
public class CosmosSyncUserDefinedFunctionResponse extends CosmosSyncResponse {

    private final CosmosSyncUserDefinedFunction userDefinedFunction;
    private CosmosUserDefinedFunctionResponse asyncResponse;

    /**
     * Instantiates a new Cosmos sync user defined function response.
     *
     * @param resourceResponse the resource response
     * @param userDefinedFunction the user defined function
     */
    CosmosSyncUserDefinedFunctionResponse(CosmosUserDefinedFunctionResponse resourceResponse,
                                          CosmosSyncUserDefinedFunction userDefinedFunction) {
        super(resourceResponse);
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
    public CosmosSyncUserDefinedFunction userDefinedFunction() {
        return userDefinedFunction;
    }

}
