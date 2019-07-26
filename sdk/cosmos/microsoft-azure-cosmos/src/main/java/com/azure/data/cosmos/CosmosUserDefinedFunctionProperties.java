// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.Constants;
import com.azure.data.cosmos.internal.ResourceResponse;
import com.azure.data.cosmos.internal.UserDefinedFunction;

import java.util.List;
import java.util.stream.Collectors;

public class CosmosUserDefinedFunctionProperties extends Resource {

    /**
     * Constructor
     */
    public CosmosUserDefinedFunctionProperties(){
        super();
    }

    CosmosUserDefinedFunctionProperties(ResourceResponse<UserDefinedFunction> response) {
        super(response.getResource().toJson());
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
     * @param id the name of the resource.
     * @return the current instance of cosmos user defined function properties
     */
    public CosmosUserDefinedFunctionProperties id(String id) {
        super.id(id);
        return this;
    }

    /**
     * Get the body of the user defined function.
     *
     * @return the body.
     */
    public String body() {
        return super.getString(Constants.Properties.BODY);
    }

    /**
     * Set the body of the user defined function.
     *
     * @param body the body.
     * @return the CosmosUserDefinedFunctionProperties.
     */
    public CosmosUserDefinedFunctionProperties body(String body) {
        super.set(Constants.Properties.BODY, body);
        return this;
    }

    static List<CosmosUserDefinedFunctionProperties> getFromV2Results(List<UserDefinedFunction> results) {
        return results.stream().map(udf -> new CosmosUserDefinedFunctionProperties(udf.toJson())).collect(Collectors.toList());
    }
}
