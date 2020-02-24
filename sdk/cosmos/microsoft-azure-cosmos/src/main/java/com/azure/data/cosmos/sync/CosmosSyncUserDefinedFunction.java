// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.sync;

import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.CosmosUserDefinedFunction;
import com.azure.data.cosmos.CosmosUserDefinedFunctionProperties;

/**
 * The type Cosmos sync user defined function.
 */
public class CosmosSyncUserDefinedFunction {
    private final String id;
    private final CosmosSyncContainer container;
    private final CosmosUserDefinedFunction userDefinedFunction;

    /**
     * Instantiates a new Cosmos sync user defined function.
     *
     * @param id the id
     * @param container the container
     * @param userDefinedFunction the user defined function
     */
    CosmosSyncUserDefinedFunction(String id, CosmosSyncContainer container, CosmosUserDefinedFunction userDefinedFunction) {

        this.id = id;
        this.container = container;
        this.userDefinedFunction = userDefinedFunction;
    }

    /**
     * Id string.
     *
     * @return the string
     */
    public String id() {
        return id;
    }

    /**
     * Read cosmos user defined function.
     *
     * @return the cosmos sync user defined function response
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosSyncUserDefinedFunctionResponse read() throws CosmosClientException {
        return container.getScripts().mapUDFResponseAndBlock(userDefinedFunction.read());
    }

    /**
     * Replace cosmos user defined function.
     *
     * @param udfSettings the udf settings
     * @return the cosmos sync user defined function response
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosSyncUserDefinedFunctionResponse replace(CosmosUserDefinedFunctionProperties udfSettings)
            throws CosmosClientException {
        return container.getScripts().mapUDFResponseAndBlock(userDefinedFunction.replace(udfSettings));
    }

    /**
     * Delete cosmos user defined function.
     *
     * @return the cosmos sync response
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosSyncResponse delete() throws CosmosClientException {
        return container.getScripts().mapDeleteResponseAndBlock(userDefinedFunction.delete());
    }
}
