// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.data.cosmosdb.repository.query;

import org.springframework.data.repository.query.ParametersParameterAccessor;

import java.util.Arrays;
import java.util.List;

public class ReactiveCosmosParameterParameterAccessor extends ParametersParameterAccessor
        implements ReactiveCosmosParameterAccessor {

    private final List<Object> values;

    public ReactiveCosmosParameterParameterAccessor(ReactiveCosmosQueryMethod method, Object[] values) {
        super(method.getParameters(), values);

        this.values = Arrays.asList(values);
    }

    @Override
    public Object[] getValues() {
        return values.toArray();
    }
}
