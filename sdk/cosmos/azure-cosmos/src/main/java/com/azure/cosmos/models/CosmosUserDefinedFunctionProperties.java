// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.implementation.Resource;
import com.azure.cosmos.implementation.UserDefinedFunction;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The type Cosmos user defined function properties.
 */
public final class CosmosUserDefinedFunctionProperties {

    private UserDefinedFunction userDefinedFunction;

    /**
     * Constructor.
     */
    CosmosUserDefinedFunctionProperties() {
        this.userDefinedFunction = new UserDefinedFunction();
    }

    /**
     * Constructor.
     *
     * @param id the id of the Cosmos user defined function.
     * @param body the body of the Cosmos user defined function.
     */
    public CosmosUserDefinedFunctionProperties(String id, String body) {
        this.userDefinedFunction = new UserDefinedFunction();
        userDefinedFunction.setId(id);
        userDefinedFunction.setBody(body);
    }

    /**
     * Constructor.
     *
     * @param jsonNode the JSON node that represents the cosmos user defined function properties.
     */
    CosmosUserDefinedFunctionProperties(ObjectNode jsonNode) {
        this.userDefinedFunction = new UserDefinedFunction(jsonNode);
    }

    /**
     * Get the body of the user defined function.
     *
     * @return the body.
     */
    public String getBody() {
        return this.userDefinedFunction.getBody();
    }

    /**
     * Set the body of the user defined function.
     *
     * @param body the body.
     * @return the CosmosUserDefinedFunctionProperties.
     */
    public CosmosUserDefinedFunctionProperties setBody(String body) {
        this.userDefinedFunction.setBody(body);
        return this;
    }

    Resource getResource() {
        return this.userDefinedFunction;
    }

    /**
     * Gets the name of the resource.
     *
     * @return the name of the resource.
     */
    public String getId() {
        return this.userDefinedFunction.getId();
    }

    /**
     * Sets the id
     *
     * @param id the name of the resource.
     * @return the current cosmos trigger properties instance
     */
    public CosmosUserDefinedFunctionProperties setId(String id) {
        this.userDefinedFunction.setId(id);
        return this;
    }

    /**
     * Gets the ID associated with the resource.
     *
     * @return the ID associated with the resource.
     */
    String getResourceId() {
        return this.userDefinedFunction.getResourceId();
    }

    /**
     * Get the last modified timestamp associated with the resource.
     * This is only relevant when getting response from the server.
     *
     * @return the timestamp.
     */
    public Instant getTimestamp() {
        return this.userDefinedFunction.getTimestamp();
    }

    /**
     * Get the entity tag associated with the resource.
     * This is only relevant when getting response from the server.
     *
     * @return the e tag.
     */
    public String getETag() {
        return this.userDefinedFunction.getETag();
    }

    static List<CosmosUserDefinedFunctionProperties> getFromV2Results(List<UserDefinedFunction> results) {
        return results.stream().map(udf -> new CosmosUserDefinedFunctionProperties(udf.getPropertyBag()))
                   .collect(Collectors.toList());
    }
}
