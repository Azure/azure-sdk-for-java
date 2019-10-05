// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;

/**
 * The type Cosmos sync user defined function.
 */
public class CosmosUserDefinedFunction {
    private final String id;
    private final CosmosContainer container;
    private final CosmosAsyncUserDefinedFunction userDefinedFunction;

    /**
     * Instantiates a new Cosmos sync user defined function.
     *
     * @param id the id
     * @param container the container
     * @param userDefinedFunction the user defined function
     */
    CosmosUserDefinedFunction(String id, CosmosContainer container, CosmosAsyncUserDefinedFunction userDefinedFunction) {

        this.id = id;
        this.container = container;
        this.userDefinedFunction = userDefinedFunction;
    }

    /**
     * Id string.
     *
     * @return the string
     */
    public String getId() {
        return id;
    }

    /**
     * Read cosmos user defined function.
     *
     * @return the cosmos sync user defined function response
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosUserDefinedFunctionResponse read() throws CosmosClientException {
        return container.getScripts().mapUDFResponseAndBlock(userDefinedFunction.read());
    }

    /**
     * Replace cosmos user defined function.
     *
     * @param udfSettings the udf settings
     * @return the cosmos sync user defined function response
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosUserDefinedFunctionResponse replace(CosmosUserDefinedFunctionProperties udfSettings)
            throws CosmosClientException {
        return container.getScripts().mapUDFResponseAndBlock(userDefinedFunction.replace(udfSettings));
    }

    /**
     * Delete cosmos user defined function.
     *
     * @return the cosmos sync response
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosUserDefinedFunctionResponse delete() throws CosmosClientException {
        return container.getScripts().mapUDFResponseAndBlock(userDefinedFunction.delete());
    }
}
