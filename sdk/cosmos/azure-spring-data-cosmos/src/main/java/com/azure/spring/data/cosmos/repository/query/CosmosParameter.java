// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.query;

import org.springframework.core.MethodParameter;
import org.springframework.data.repository.query.Parameter;

/**
 * A single cosmos parameter of a query method.
 */
public class CosmosParameter extends Parameter {

    /**
     * Creates a new {@link CosmosParameter} for the given {@link MethodParameter}.
     *
     * @param parameter must not be {@literal null}.
     */
    public CosmosParameter(MethodParameter parameter) {
        super(parameter);
    }

    @Override
    public boolean isSpecialParameter() {
        return super.isSpecialParameter();
    }
}
