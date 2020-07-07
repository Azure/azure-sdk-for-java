// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.core.query;

import com.azure.spring.data.cosmos.core.generator.FindQuerySpecGenerator;
import com.azure.spring.data.cosmos.exception.IllegalQueryException;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.azure.spring.data.cosmos.common.TestConstants.CRITERIA_KEY;
import static com.azure.spring.data.cosmos.common.TestConstants.CRITERIA_OBJECT;

public class CriteriaUnitTest {

    @Test
    public void testUnaryCriteria() {
        final List<Object> values = Arrays.asList(CRITERIA_OBJECT);
        final Criteria criteria = Criteria.getInstance(CriteriaType.IS_EQUAL, CRITERIA_KEY, values);

        Assert.assertTrue(criteria.getSubCriteria().isEmpty());
        Assert.assertEquals(values, criteria.getSubjectValues());
        Assert.assertEquals(CriteriaType.IS_EQUAL, criteria.getType());
        Assert.assertEquals(CRITERIA_KEY, criteria.getSubject());
        Assert.assertTrue(CriteriaType.isBinary(criteria.getType()));
    }

    @Test
    public void testBinaryCriteria() {
        final List<Object> values = Arrays.asList(CRITERIA_OBJECT);
        final Criteria leftCriteria = Criteria.getInstance(CriteriaType.IS_EQUAL, CRITERIA_KEY, values);
        final Criteria rightCriteria = Criteria.getInstance(CriteriaType.IS_EQUAL, CRITERIA_OBJECT, values);
        final Criteria criteria = Criteria.getInstance(CriteriaType.AND, leftCriteria, rightCriteria);

        Assert.assertNotNull(criteria.getSubCriteria());
        Assert.assertNull(criteria.getSubjectValues());
        Assert.assertNull(criteria.getSubject());
        Assert.assertEquals(criteria.getType(), CriteriaType.AND);
        Assert.assertTrue(CriteriaType.isClosed(criteria.getType()));

        Assert.assertEquals(2, criteria.getSubCriteria().size());
        Assert.assertEquals(leftCriteria, criteria.getSubCriteria().get(0));
        Assert.assertEquals(rightCriteria, criteria.getSubCriteria().get(1));
    }

    @Test(expected = IllegalQueryException.class)
    public void testInvalidInKeywordParameter() {
        final List<Object> values = Collections.singletonList(CRITERIA_OBJECT);
        final Criteria criteria = Criteria.getInstance(CriteriaType.IN, CRITERIA_KEY, values);
        final DocumentQuery query = new DocumentQuery(criteria);

        new FindQuerySpecGenerator().generateCosmos(query);
    }

    @Test(expected = IllegalQueryException.class)
    public void testInvalidInKeywordType() {
        final List<Object> values = Collections.singletonList(new IllegalQueryException(""));
        final Criteria criteria = Criteria.getInstance(CriteriaType.IN, CRITERIA_KEY, values);
        final DocumentQuery query = new DocumentQuery(criteria);

        new FindQuerySpecGenerator().generateCosmos(query);
    }
}
