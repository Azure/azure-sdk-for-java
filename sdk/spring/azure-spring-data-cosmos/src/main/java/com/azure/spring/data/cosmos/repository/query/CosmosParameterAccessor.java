// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.query;

import org.springframework.data.repository.query.ParameterAccessor;

/**
 * Interface to access method parameters. Allows dedicated access to parameters of special types and expose api to read
 * values.
 */
public interface CosmosParameterAccessor extends ParameterAccessor {

    /**
     * Get values of method parameters
     * @return Object[]
     */
    Object[] getValues();
}
