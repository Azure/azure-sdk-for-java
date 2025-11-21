// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.core.query;

import com.azure.spring.data.cosmos.core.generator.FindQuerySpecGenerator;
import com.azure.spring.data.cosmos.exception.IllegalQueryException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.data.repository.query.parser.Part;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.azure.spring.data.cosmos.common.TestConstants.CRITERIA_KEY;
import static com.azure.spring.data.cosmos.common.TestConstants.CRITERIA_OBJECT;
import static org.junit.jupiter.api.Assertions.*;

public class CriteriaUnitTest {

    @Test
    public void testUnaryCriteria() {
        final List<Object> values = Arrays.asList(CRITERIA_OBJECT);
        final Criteria criteria = Criteria.getInstance(CriteriaType.IS_EQUAL,
            CRITERIA_KEY, values, Part.IgnoreCaseType.NEVER);

        Assertions.assertTrue(criteria.getSubCriteria().isEmpty());
        assertEquals(values, criteria.getSubjectValues());
        assertEquals(CriteriaType.IS_EQUAL, criteria.getType());
        assertEquals(CRITERIA_KEY, criteria.getSubject());
        Assertions.assertTrue(CriteriaType.isBinary(criteria.getType()));
        assertEquals(Part.IgnoreCaseType.NEVER, criteria.getIgnoreCase());
    }

    @Test
    public void testBinaryCriteria() {
        final List<Object> values = Arrays.asList(CRITERIA_OBJECT);
        final Criteria leftCriteria = Criteria.getInstance(CriteriaType.IS_EQUAL,
            CRITERIA_KEY, values, Part.IgnoreCaseType.NEVER);
        final Criteria rightCriteria = Criteria.getInstance(CriteriaType.IS_EQUAL,
            CRITERIA_OBJECT, values, Part.IgnoreCaseType.NEVER);
        final Criteria criteria = Criteria.getInstance(CriteriaType.AND, leftCriteria, rightCriteria);

        Assertions.assertNotNull(criteria.getSubCriteria());
        Assertions.assertNull(criteria.getSubjectValues());
        Assertions.assertNull(criteria.getSubject());
        assertEquals(criteria.getType(), CriteriaType.AND);
        Assertions.assertTrue(CriteriaType.isClosed(criteria.getType()));

        assertEquals(2, criteria.getSubCriteria().size());
        assertEquals(leftCriteria, criteria.getSubCriteria().get(0));
        assertEquals(rightCriteria, criteria.getSubCriteria().get(1));

        assertEquals(Part.IgnoreCaseType.NEVER, criteria.getSubCriteria().get(0).getIgnoreCase());
        assertEquals(Part.IgnoreCaseType.NEVER, criteria.getSubCriteria().get(1).getIgnoreCase());
    }

    @Test
    public void testInvalidInKeywordParameter() {
        final List<Object> values = Collections.singletonList(CRITERIA_OBJECT);
        final Criteria criteria = Criteria.getInstance(CriteriaType.IN,
            CRITERIA_KEY, values, Part.IgnoreCaseType.NEVER);
        final CosmosQuery query = new CosmosQuery(criteria);

        new FindQuerySpecGenerator().generateCosmos(query);
    }

    @Test
    public void testInvalidInKeywordType() {
        final List<Object> values = Collections.singletonList(new IllegalQueryException(""));
        final Criteria criteria = Criteria.getInstance(CriteriaType.IN,
            CRITERIA_KEY, values, Part.IgnoreCaseType.NEVER);
        final CosmosQuery query = new CosmosQuery(criteria);

        new FindQuerySpecGenerator().generateCosmos(query);
    }
}
