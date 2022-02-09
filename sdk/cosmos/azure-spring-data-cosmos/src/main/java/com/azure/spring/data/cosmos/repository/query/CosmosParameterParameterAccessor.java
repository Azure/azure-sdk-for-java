// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.query;

import org.springframework.data.repository.query.ParametersParameterAccessor;

import java.util.Arrays;
import java.util.List;

/**
 * {@link ParametersParameterAccessor} implementation and store all special parameters in a List.
 */
public class CosmosParameterParameterAccessor extends ParametersParameterAccessor
        implements CosmosParameterAccessor {

    private final List<Object> values;

    /**
     * Creates a new {@link CosmosParameterParameterAccessor}.
     *
     * @param method must not be {@literal null}.
     * @param values must not be {@literal null}.
     */
    public CosmosParameterParameterAccessor(CosmosQueryMethod method, Object[] values) {
        super(method.getParameters(), values);

        this.values = Arrays.asList(values);
    }

    @Override
    public Object[] getValues() {
        return values.toArray();
    }
}
