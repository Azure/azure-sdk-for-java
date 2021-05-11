// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.conversion.script;

import com.azure.spring.data.gremlin.common.GremlinEntityType;
import com.azure.spring.data.gremlin.exception.GremlinInvalidEntityIdFieldException;
import org.junit.Test;

public class AbstractGremlinScriptLiteralUnitTest {

    @Test(expected = GremlinInvalidEntityIdFieldException.class)
    public void testEntityInvalidIdType() {
        final Double id = 12.342;
        GremlinScriptLiteralHelper.generateEntityWithRequiredId(id, GremlinEntityType.EDGE);
    }

    @Test(expected = GremlinInvalidEntityIdFieldException.class)
    public void testPropertyInvalidIdType() {
        final Double id = 12.342;
        GremlinScriptLiteralHelper.generatePropertyWithRequiredId(id);
    }
}
