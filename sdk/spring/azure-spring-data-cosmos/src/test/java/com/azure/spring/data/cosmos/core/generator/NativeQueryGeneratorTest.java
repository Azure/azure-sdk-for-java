// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.core.generator;

import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.spring.data.cosmos.core.query.CosmosQuery;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.data.domain.Sort.Direction.ASC;

public class NativeQueryGeneratorTest {

    @Mock
    private EmptyQueryGenerator queryGenerator;

    @Before
    public void setUp() {
        this.queryGenerator = new EmptyQueryGenerator();
    }

    @Test
    public void generateSortedQueryTest() {
        List<SqlParameter> sqlParameters = new ArrayList<>();
        sqlParameters.add(new SqlParameter("@firstName", "TREVOR"));

        SqlQuerySpec querySpec = new SqlQuerySpec("select * from a where a.firstName = @firstName", sqlParameters);
        final SqlQuerySpec sortedQuerySpec = NativeQueryGenerator.getInstance().generateSortedQuery(querySpec, Sort.by(ASC, "id"));

        Assert.assertEquals(sortedQuerySpec.getQueryText(), "select * from a where a.firstName = @firstName ORDER BY a.id ASC");
    }

    private static class EmptyQueryGenerator extends NativeQueryGenerator implements QuerySpecGenerator {

        @Override
        public SqlQuerySpec generateCosmos(CosmosQuery query) {
            return this.generateCosmos(query);
        }
    }
}
