// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.query.paramerter;

import org.springframework.core.MethodParameter;
import org.springframework.data.repository.query.Parameter;

public class GremlinParameter extends Parameter {

    public GremlinParameter(MethodParameter parameter) {
        super(parameter);
    }
}
