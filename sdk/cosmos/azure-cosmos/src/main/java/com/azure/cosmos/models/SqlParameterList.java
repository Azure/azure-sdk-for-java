// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Represents a collection of SQL parameters to for a SQL query  in the Azure Cosmos DB database service.
 */
public final class SqlParameterList {

    private final List<SqlParameter> parameters;

    /**
     * Initializes a new instance of the SqlParameterList class.
     */
    public SqlParameterList() {
        this.parameters = new ArrayList<>();
    }

    /**
     * Initializes a new instance of the SqlParameterList class from an array of parameters.
     *
     * @param parameters the array of parameters.
     * @throws IllegalArgumentException thrown if an error occurs
     */
    public SqlParameterList(SqlParameter... parameters) {
        if (parameters == null) {
            throw new IllegalArgumentException("parameters");
        }

        this.parameters = Arrays.asList(parameters);
    }

    /**
     * Initializes a new instance of the SqlParameterList class from a collection of parameters.
     *
     * @param parameters the collection of parameters.
     * @throws IllegalArgumentException thrown if an error occurs
     */
    public SqlParameterList(Collection<SqlParameter> parameters) {
        if (parameters == null) {
            throw new IllegalArgumentException("parameters");
        }

        this.parameters = new ArrayList<>(parameters);
    }

    public boolean add(SqlParameter parameter) {
        return this.parameters.add(parameter);
    }

    public boolean addAll(Collection<? extends SqlParameter> parameters) {
        return this.parameters.addAll(parameters);
    }

    List<SqlParameter> getParameters() {
        return parameters;
    }
}
