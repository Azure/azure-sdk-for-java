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

import java.util.Collections;
import java.util.List;
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

    private static class EmptyQueryGenerator extends AbstractQueryGenerator implements QuerySpecGenerator {

        @Override
        public SqlQuerySpec generateCosmos(CosmosQuery query) {
            return this.generateCosmosQuery(query, "");
        }
    }
}
