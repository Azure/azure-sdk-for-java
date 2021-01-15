// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.query.paramerter;

import com.azure.spring.data.gremlin.query.query.GremlinQueryMethod;
import org.springframework.data.repository.query.ParametersParameterAccessor;

public class GremlinParametersParameterAccessor extends ParametersParameterAccessor
        implements GremlinParameterAccessor {

    public GremlinParametersParameterAccessor(GremlinQueryMethod method, Object[] values) {
        super(method.getParameters(), values);
    }
}
