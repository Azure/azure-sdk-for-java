// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.core.generator;

import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.spring.data.cosmos.core.query.CosmosQuery;
import com.azure.spring.data.cosmos.core.query.Criteria;
import com.azure.spring.data.cosmos.core.query.CriteriaType;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.data.util.*;
import org.springframework.lang.*;

import java.util.*;
import java.util.stream.Collectors;

public class AbstractQueryGeneratorTest {

    @Mock
    private EmptyQueryGenerator queryGenerator;

    @Before
    public void setUp() {
        this.queryGenerator = new EmptyQueryGenerator();
    }

    @Test
    public void binaryOperatorPriorityPreserved() {
        Criteria x = Criteria.getInstance(CriteriaType.IS_EQUAL, "x", Collections.singletonList("xVal"), Part.IgnoreCaseType.NEVER);
        Criteria left = Criteria.getInstance(CriteriaType.IS_EQUAL, "y", Collections.singletonList("yVal"), Part.IgnoreCaseType.NEVER);
        Criteria right = Criteria.getInstance(CriteriaType.IS_EQUAL, "z", Collections.singletonList("zVal"), Part.IgnoreCaseType.NEVER);

        Criteria or = Criteria.getInstance(CriteriaType.OR, left, right);
        Criteria and = Criteria.getInstance(CriteriaType.AND, x, or);

        final CosmosQuery query = new CosmosQuery(and);
        SqlQuerySpec querySpec = queryGenerator.generateCosmos(query);
        List<String> parameterNames = querySpec.getParameters().stream().map(SqlParameter::getName).collect(Collectors.toList());
        Assert.assertNotNull(querySpec.getQueryText());
        MatcherAssert.assertThat(querySpec.getQueryText(), Matchers.stringContainsInOrder(
            parameterNames.get(0), CriteriaType.AND.getSqlKeyword(),
            "(", parameterNames.get(1), CriteriaType.OR.getSqlKeyword(), parameterNames.get(2), ")"));
    }

    @Test
    public void generateBinaryQueryWithStartsWithDoesNotUseUpper() {
        Criteria nameStartsWith = Criteria.getInstance(CriteriaType.STARTS_WITH, "firstName",
            Collections.singletonList("TREVOR"),
            Part.IgnoreCaseType.ALWAYS);
        CosmosQuery query = new CosmosQuery(nameStartsWith);

        SqlQuerySpec result = queryGenerator.generateCosmos(query);

        Assert.assertEquals(result.getQueryText(), " WHERE STARTSWITH(r.firstName, @firstName0, true) ");
    }

    @Test
    public void generateBinaryQueryWithArrayContainsUsesUpper() {
        Criteria hasLastName = Criteria.getInstance(CriteriaType.ARRAY_CONTAINS, "lastName",
            Collections.singletonList("ANDERSON"),
            Part.IgnoreCaseType.ALWAYS);
        CosmosQuery query = new CosmosQuery(hasLastName);

        SqlQuerySpec result = queryGenerator.generateCosmos(query);

        Assert.assertEquals(result.getQueryText(), " WHERE ARRAY_CONTAINS(UPPER(r.lastName), UPPER(@lastName0)) ");
    }

    @Test
    public void generateBinaryQueryWithStringEquals() {
        for (Part.IgnoreCaseType ignoreCaseType : Part.IgnoreCaseType.values()) {
            Criteria nameStartsWith = Criteria.getInstance(CriteriaType.STRING_EQUALS, "firstName",
                    Collections.singletonList("TREVOR"),
                    ignoreCaseType);
            CosmosQuery query = new CosmosQuery(nameStartsWith);

            SqlQuerySpec result = queryGenerator.generateCosmos(query);

            if (ignoreCaseType == Part.IgnoreCaseType.NEVER) {
                Assert.assertEquals(result.getQueryText(), " WHERE STRINGEQUALS(r.firstName, @firstName0) ");
            } else {
                Assert.assertEquals(result.getQueryText(), " WHERE STRINGEQUALS(r.firstName, @firstName0, true) ");
            }
        }
    }

    @Test
    public void generateBinaryQueryWithIsEqualIntUsesUpper() {
        Criteria isEqualInt = Criteria.getInstance(CriteriaType.IS_EQUAL, "zipcode",
            Collections.singletonList(20180),
            Part.IgnoreCaseType.ALWAYS);
        CosmosQuery query = new CosmosQuery(isEqualInt);

        SqlQuerySpec result = queryGenerator.generateCosmos(query);

        Assert.assertEquals(result.getQueryText(), " WHERE UPPER(r.zipcode) = UPPER(@zipcode0) ");
    }

    @Test
    public void generateBinaryQueryWithIsEqualStringDoesNotUseUpper() {
        Criteria isEqualString = Criteria.getInstance(CriteriaType.IS_EQUAL, "firstName",
            Collections.singletonList("TREVOR"),
            Part.IgnoreCaseType.ALWAYS);
        CosmosQuery query = new CosmosQuery(isEqualString);

        SqlQuerySpec result = queryGenerator.generateCosmos(query);

        Assert.assertEquals(result.getQueryText(), " WHERE STRINGEQUALS(r.firstName, @firstName0, true) ");
    }

    private static class EmptyQueryGenerator extends AbstractQueryGenerator implements QuerySpecGenerator {

        @Override
        public SqlQuerySpec generateCosmos(CosmosQuery query) {
            return this.generateCosmosQuery(query, "");
        }
    }
}
