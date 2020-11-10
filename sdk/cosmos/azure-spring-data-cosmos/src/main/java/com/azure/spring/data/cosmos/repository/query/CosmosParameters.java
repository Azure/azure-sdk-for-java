// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.query;

import org.springframework.core.MethodParameter;
import org.springframework.data.repository.query.Parameters;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Method parameters that have to be bound to query parameters or applied to the query independently.
 */
public class CosmosParameters extends Parameters<CosmosParameters, CosmosParameter> {

    /**
     * Creates a new instance of {@link CosmosParameters}.
     *
     * @param method must not be {@literal null}.
     */
    public CosmosParameters(Method method) {
        super(method);
    }

    private CosmosParameters(List<CosmosParameter> parameters) {
        super(parameters);
    }

    @Override
    protected CosmosParameters createFrom(List<CosmosParameter> parameters) {
        return new CosmosParameters(parameters);
    }

    @Override
    protected CosmosParameter createParameter(MethodParameter parameter) {
        return new CosmosParameter(parameter);
    }
}
