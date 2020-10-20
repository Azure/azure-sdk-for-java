// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.query.parameter;

import com.azure.spring.data.gremlin.query.paramerter.GremlinParameter;
import com.azure.spring.data.gremlin.query.paramerter.GremlinParameters;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull;

import java.lang.reflect.Method;

public class GremlinParameterUnitTest {

    private Method method;
    private MethodParameter methodParameter;

    public String handle(@NonNull String name) {
        return "handle: " + name;
    }

    @Before
    public void setup() throws NoSuchMethodException {
        method = this.getClass().getMethod("handle", String.class);
        methodParameter = new MethodParameter(this.getClass().getMethod("handle", String.class), 0);
    }

    @Test
    public void testGremlinParameter() {
        final GremlinParameter parameter = new GremlinParameter(this.methodParameter);

        Assert.assertNotNull(parameter);
        Assert.assertEquals(parameter.getType(), String.class);
        Assert.assertEquals(parameter.getIndex(), 0);
    }

    @Test
    public void testGremlinParameters() {
        final GremlinParameters gremlinParameters = new GremlinParameters(this.method);

        Assert.assertNotNull(gremlinParameters);
        Assert.assertEquals(gremlinParameters.getNumberOfParameters(), 1);
        Assert.assertNotNull(gremlinParameters.getParameter(0));
    }
}

