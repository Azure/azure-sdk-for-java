// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.query.criteria;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.azure.spring.data.gremlin.query.criteria.CriteriaType.IS_EQUAL;
import static com.azure.spring.data.gremlin.query.criteria.CriteriaType.OR;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CriteriaUnitTest {

    @Test
    public void testGetUnaryInstanceException() {
        final List<Object> values = new ArrayList<>();

        assertThrows(IllegalArgumentException.class, () -> Criteria.getUnaryInstance(OR, "fake-name", values));
    }

    @Test
    public void testGetBinaryInstanceException() {
        final List<Object> values = new ArrayList<>();
        final Criteria left = Criteria.getUnaryInstance(IS_EQUAL, "fake-name", values);
        final Criteria right = Criteria.getUnaryInstance(IS_EQUAL, "fake-name", values);

        assertThrows(IllegalArgumentException.class, () -> Criteria.getBinaryInstance(IS_EQUAL, left, right));
    }

    @Test
    public void testCriteriaTypeToGremlinException() {
        assertThrows(UnsupportedOperationException.class, () -> CriteriaType.criteriaTypeToGremlin(IS_EQUAL));
    }
}
