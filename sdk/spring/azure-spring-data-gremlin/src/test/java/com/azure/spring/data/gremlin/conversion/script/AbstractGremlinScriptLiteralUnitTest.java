// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.conversion.script;

import com.azure.spring.data.gremlin.common.GremlinEntityType;
import com.azure.spring.data.gremlin.exception.GremlinInvalidEntityIdFieldException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class AbstractGremlinScriptLiteralUnitTest {

    @Test
    public void testEntityInvalidIdType() {
        final Double id = 12.342;
        assertThrows(GremlinInvalidEntityIdFieldException.class,
            () -> GremlinScriptLiteralHelper.generateEntityWithRequiredId(id, GremlinEntityType.EDGE));
    }

    @Test
    public void testPropertyInvalidIdType() {
        final Double id = 12.342;
        assertThrows(GremlinInvalidEntityIdFieldException.class,
            () -> GremlinScriptLiteralHelper.generatePropertyWithRequiredId(id));
    }
}
