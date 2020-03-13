// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.UserDefinedFunction;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The type Cosmos user defined function properties.
 */
public final class CosmosUserDefinedFunctionProperties extends Resource {

    /**
     * Constructor
     */
    public CosmosUserDefinedFunctionProperties() {
        super();
    }

    /**
     * Constructor.
     *
     * @param jsonString the json string that represents the cosmos user defined function properties.
     */
    CosmosUserDefinedFunctionProperties(String jsonString) {
        super(jsonString);
    }

    /**
     * Sets the id
     *
     * @param id the name of the resource.
     * @return the current instance of cosmos user defined function properties
     */
    public CosmosUserDefinedFunctionProperties setId(String id) {
        super.setId(id);
        return this;
    }

    /**
     * Get the body of the user defined function.
     *
     * @return the body.
     */
    public String getBody() {
        return super.getString(Constants.Properties.BODY);
    }

    /**
     * Set the body of the user defined function.
     *
     * @param body the body.
     * @return the CosmosUserDefinedFunctionProperties.
     */
    public CosmosUserDefinedFunctionProperties setBody(String body) {
        super.set(Constants.Properties.BODY, body);
        return this;
    }

    static List<CosmosUserDefinedFunctionProperties> getFromV2Results(List<UserDefinedFunction> results) {
        return results.stream().map(udf -> new CosmosUserDefinedFunctionProperties(udf.toJson()))
                   .collect(Collectors.toList());
    }
}
