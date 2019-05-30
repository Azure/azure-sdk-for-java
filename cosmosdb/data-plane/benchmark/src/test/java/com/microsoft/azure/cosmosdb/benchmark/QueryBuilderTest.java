/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.microsoft.azure.cosmosdb.benchmark;

import com.google.common.collect.ImmutableList;
import com.microsoft.azure.cosmosdb.SqlParameter;
import org.testng.annotations.Test;

import static com.microsoft.azure.cosmosdb.benchmark.ReadMyWriteWorkflow.QueryBuilder.WhereClause;
import static org.assertj.core.api.Assertions.assertThat;

public class QueryBuilderTest {

    @Test(groups = {"unit"})
    public void basic() {
        ReadMyWriteWorkflow.QueryBuilder queryBuilder = new ReadMyWriteWorkflow.QueryBuilder();
        assertThat(queryBuilder.toSqlQuerySpec().getQueryText())
                .isEqualTo("SELECT * FROM root");
    }

    @Test(groups = {"unit"})
    public void top() {
        ReadMyWriteWorkflow.QueryBuilder queryBuilder = new ReadMyWriteWorkflow.QueryBuilder();
        queryBuilder.top(50);
        assertThat(queryBuilder.toSqlQuerySpec().getQueryText())
                .isEqualTo("SELECT TOP 50 * FROM root");
    }

    @Test(groups = {"unit"})
    public void orderBy() {
        ReadMyWriteWorkflow.QueryBuilder queryBuilder = new ReadMyWriteWorkflow.QueryBuilder();
        queryBuilder.orderBy("prop");
        assertThat(queryBuilder.toSqlQuerySpec().getQueryText())
                .isEqualTo("SELECT * FROM root ORDER BY root.prop");
    }

    @Test(groups = {"unit"})
    public void whereInClause() {
        ReadMyWriteWorkflow.QueryBuilder queryBuilder = new ReadMyWriteWorkflow.QueryBuilder();

        ImmutableList<SqlParameter> parameters = ImmutableList.of(new SqlParameter("@param1", 1),
                                                                  new SqlParameter("@param2", 2));
        queryBuilder.whereClause(new WhereClause.InWhereClause("colName",
                                                               parameters));
        assertThat(queryBuilder.toSqlQuerySpec().getQueryText())
                .isEqualTo("SELECT * FROM root WHERE root.colName IN (@param1, @param2)");
        assertThat(queryBuilder.toSqlQuerySpec().getParameters()).containsExactlyElementsOf(parameters);
    }

    @Test(groups = {"unit"})
    public void topOrderByWhereClause() {
        ReadMyWriteWorkflow.QueryBuilder queryBuilder = new ReadMyWriteWorkflow.QueryBuilder();
        queryBuilder.orderBy("prop");
        queryBuilder.top(5);

        ImmutableList<SqlParameter> parameters = ImmutableList.of(new SqlParameter("@param1", 1),
                                                                  new SqlParameter("@param2", 2));
        queryBuilder.whereClause(new WhereClause.InWhereClause("colName",
                                                               parameters));
        assertThat(queryBuilder.toSqlQuerySpec().getQueryText())
                .isEqualTo("SELECT TOP 5 * FROM root WHERE root.colName IN (@param1, @param2) ORDER BY root.prop");
        assertThat(queryBuilder.toSqlQuerySpec().getParameters()).containsExactlyElementsOf(parameters);
    }
}
