// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.query.paramerter;

import org.springframework.core.MethodParameter;
import org.springframework.data.repository.query.Parameters;

import java.lang.reflect.Method;
import java.util.List;

public class GremlinParameters extends Parameters<GremlinParameters, GremlinParameter> {

    public GremlinParameters(Method method) {
        super(method);
    }

    private GremlinParameters(List<GremlinParameter> parameters) {
        super(parameters);
    }

    @Override
    protected GremlinParameters createFrom(List<GremlinParameter> parameters) {
        return new GremlinParameters(parameters);
    }

    @Override
    protected GremlinParameter createParameter(MethodParameter parameter) {
        return new GremlinParameter(parameter);
    }
}
