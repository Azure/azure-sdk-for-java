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
package com.microsoft.azure.cosmosdb.rx;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.microsoft.azure.cosmosdb.Database;
import com.microsoft.azure.cosmosdb.FeedOptions;
import com.microsoft.azure.cosmosdb.FeedResponse;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient.Builder;

import rx.Observable;

public class DatabaseQueryTest extends TestSuiteBase {

    public final static String DATABASE_ID1 = getDatabaseId(DatabaseQueryTest.class) + "1";
    public final static String DATABASE_ID2 = getDatabaseId(DatabaseQueryTest.class) + "2";

    private List<Database> createdDatabases = new ArrayList<>();

    private Builder clientBuilder;
    private AsyncDocumentClient client;
    
    @Factory(dataProvider = "clientBuilders")
    public DatabaseQueryTest(AsyncDocumentClient.Builder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }
    
    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryDatabaseWithFilter() throws Exception {
        String query = String.format("SELECT * from c where c.id = '%s'", DATABASE_ID1);

        FeedOptions options = new FeedOptions();
        options.setMaxItemCount(2);
        Observable<FeedResponse<Database>> queryObservable = client.queryDatabases(query, options);

        List<Database> expectedDatabases = createdDatabases.stream()
                .filter(d -> StringUtils.equals(DATABASE_ID1, d.getId()) ).collect(Collectors.toList());

        assertThat(expectedDatabases).isNotEmpty();

        int expectedPageSize = (expectedDatabases.size() + options.getMaxItemCount() - 1) / options.getMaxItemCount();

        FeedResponseListValidator<Database> validator = new FeedResponseListValidator.Builder<Database>()
                .totalSize(expectedDatabases.size())
                .exactlyContainsInAnyOrder(expectedDatabases.stream().map(d -> d.getResourceId()).collect(Collectors.toList()))
                .numberOfPages(expectedPageSize)
                .pageSatisfy(0, new FeedResponseValidator.Builder<Database>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();

        validateQuerySuccess(queryObservable, validator, 10000);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryAllDatabase() throws Exception {

        String query = String.format("SELECT * from c where c.id in ('%s', '%s')", 
                DATABASE_ID1,
                DATABASE_ID2);

        FeedOptions options = new FeedOptions();
        options.setMaxItemCount(2);
        Observable<FeedResponse<Database>> queryObservable = client.queryDatabases(query, options);

        List<Database> expectedDatabases = createdDatabases;

        assertThat(expectedDatabases).isNotEmpty();

        int expectedPageSize = (expectedDatabases.size() + options.getMaxItemCount() - 1) / options.getMaxItemCount();

        FeedResponseListValidator<Database> validator = new FeedResponseListValidator.Builder<Database>()
                .totalSize(expectedDatabases.size())
                .exactlyContainsInAnyOrder(expectedDatabases.stream().map(d -> d.getResourceId()).collect(Collectors.toList()))
                .numberOfPages(expectedPageSize)
                .pageSatisfy(0, new FeedResponseValidator.Builder<Database>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();

        validateQuerySuccess(queryObservable, validator, 10000);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryDatabases_NoResults() throws Exception {

        String query = "SELECT * from root r where r.id = '2'";
        FeedOptions options = new FeedOptions();
        options.setEnableCrossPartitionQuery(true);
        Observable<FeedResponse<Database>> queryObservable = client.queryDatabases(query, options);

        FeedResponseListValidator<Database> validator = new FeedResponseListValidator.Builder<Database>()
                .containsExactly(new ArrayList<>())
                .numberOfPages(1)
                .pageSatisfy(0, new FeedResponseValidator.Builder<Database>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();
        validateQuerySuccess(queryObservable, validator);
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() throws Exception {
        client = clientBuilder.build();

        Database d1 = new Database();
        d1.setId(DATABASE_ID1);
        createdDatabases.add(safeCreateDatabase(client, d1));

        Database d2 = new Database();
        d2.setId(DATABASE_ID2);
        safeCreateDatabase(client, d2);
        createdDatabases.add(safeCreateDatabase(client, d2));
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(client, DATABASE_ID1);
        safeDeleteDatabase(client, DATABASE_ID2);

        safeClose(client);
    }
}
