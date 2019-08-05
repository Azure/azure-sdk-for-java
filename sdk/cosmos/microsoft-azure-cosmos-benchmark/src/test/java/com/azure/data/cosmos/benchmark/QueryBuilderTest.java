// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.benchmark;

import com.azure.data.cosmos.SqlParameter;
import com.google.common.collect.ImmutableList;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class QueryBuilderTest {

    @Test(groups = {"unit"})
    public void basic() {
        ReadMyWriteWorkflow.QueryBuilder queryBuilder = new ReadMyWriteWorkflow.QueryBuilder();
        assertThat(queryBuilder.toSqlQuerySpec().queryText())
                .isEqualTo("SELECT * FROM root");
    }

    @Test(groups = {"unit"})
    public void top() {
        ReadMyWriteWorkflow.QueryBuilder queryBuilder = new ReadMyWriteWorkflow.QueryBuilder();
        queryBuilder.top(50);
        assertThat(queryBuilder.toSqlQuerySpec().queryText())
                .isEqualTo("SELECT TOP 50 * FROM root");
    }

    @Test(groups = {"unit"})
    public void orderBy() {
        ReadMyWriteWorkflow.QueryBuilder queryBuilder = new ReadMyWriteWorkflow.QueryBuilder();
        queryBuilder.orderBy("prop");
        assertThat(queryBuilder.toSqlQuerySpec().queryText())
                .isEqualTo("SELECT * FROM root ORDER BY root.prop");
    }

    @Test(groups = {"unit"})
    public void whereInClause() {
        ReadMyWriteWorkflow.QueryBuilder queryBuilder = new ReadMyWriteWorkflow.QueryBuilder();

        ImmutableList<SqlParameter> parameters = ImmutableList.of(new SqlParameter("@param1", 1),
                                                                  new SqlParameter("@param2", 2));
        queryBuilder.whereClause(new ReadMyWriteWorkflow.QueryBuilder.WhereClause.InWhereClause("colName",
                                                               parameters));
        assertThat(queryBuilder.toSqlQuerySpec().queryText())
                .isEqualTo("SELECT * FROM root WHERE root.colName IN (@param1, @param2)");
        assertThat(queryBuilder.toSqlQuerySpec().parameters()).containsExactlyElementsOf(parameters);
    }

    @Test(groups = {"unit"})
    public void topOrderByWhereClause() {
        ReadMyWriteWorkflow.QueryBuilder queryBuilder = new ReadMyWriteWorkflow.QueryBuilder();
        queryBuilder.orderBy("prop");
        queryBuilder.top(5);

        ImmutableList<SqlParameter> parameters = ImmutableList.of(new SqlParameter("@param1", 1),
                                                                  new SqlParameter("@param2", 2));
        queryBuilder.whereClause(new ReadMyWriteWorkflow.QueryBuilder.WhereClause.InWhereClause("colName",
                                                               parameters));
        assertThat(queryBuilder.toSqlQuerySpec().queryText())
                .isEqualTo("SELECT TOP 5 * FROM root WHERE root.colName IN (@param1, @param2) ORDER BY root.prop");
        assertThat(queryBuilder.toSqlQuerySpec().parameters()).containsExactlyElementsOf(parameters);
    }
}
