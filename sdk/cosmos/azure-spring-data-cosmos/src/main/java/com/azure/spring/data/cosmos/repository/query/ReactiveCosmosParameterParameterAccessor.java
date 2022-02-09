// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.query;

import org.springframework.data.repository.query.ParametersParameterAccessor;

import java.util.Arrays;
import java.util.List;

/**
 * {@link ReactiveCosmosParameterParameterAccessor} implementation using a {@link ParametersParameterAccessor} instance
 * to find special parameters.
 */
public class ReactiveCosmosParameterParameterAccessor extends ParametersParameterAccessor
        implements ReactiveCosmosParameterAccessor {

    private final List<Object> values;

    /**
     * Creates a new {@link ReactiveCosmosParameterParameterAccessor}.
     *
     * @param method must not be {@literal null}.
     * @param values must not be {@literal null}.
     */
    public ReactiveCosmosParameterParameterAccessor(ReactiveCosmosQueryMethod method, Object[] values) {
        super(method.getParameters(), values);

        this.values = Arrays.asList(values);
    }

    @Override
    public Object[] getValues() {
        return values.toArray();
    }
}
