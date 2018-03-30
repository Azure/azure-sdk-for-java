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

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.microsoft.azure.cosmosdb.Database;
import com.microsoft.azure.cosmosdb.DocumentClientException;
import com.microsoft.azure.cosmosdb.FeedOptions;
import com.microsoft.azure.cosmosdb.FeedResponse;

import rx.Observable;

public class ReadFeedDatabasesTest extends TestSuiteBase {

    public final static String DATABASE_ID = getDatabaseId(ReadFeedDatabasesTest.class);
    private List<Database> createdDatabases = new ArrayList<>();
    private List<Database> allDatabases = new ArrayList<>();

    private AsyncDocumentClient.Builder clientBuilder;
    private AsyncDocumentClient client;

    @Factory(dataProvider = "clientBuilders")
    public ReadFeedDatabasesTest(AsyncDocumentClient.Builder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    @Test(groups = { "simple" }, timeOut = FEED_TIMEOUT)
    public void readDatabases() throws Exception {

        FeedOptions options = new FeedOptions();
        options.setMaxItemCount(2);

        Observable<FeedResponse<Database>> feedObservable = client.readDatabases(options);

        int expectedPageSize = (allDatabases.size() + options.getMaxItemCount() - 1) / options.getMaxItemCount();
        FeedResponseListValidator<Database> validator = new FeedResponseListValidator.Builder<Database>()
                .totalSize(allDatabases.size())
                .exactlyContainsInAnyOrder(allDatabases.stream().map(d -> d.getResourceId()).collect(Collectors.toList()))
                .numberOfPages(expectedPageSize)
                .pageSatisfy(0, new FeedResponseValidator.Builder<Database>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();

        validateQuerySuccess(feedObservable, validator, FEED_TIMEOUT);
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() throws URISyntaxException, DocumentClientException {
        client = clientBuilder.build();
        allDatabases = client.readDatabases(null)
                             .map(frp -> frp.getResults())
                             .toList()
                             .map(list -> list.stream().flatMap(x -> x.stream()).collect(Collectors.toList()))
                             .toBlocking()
                             .single();
        for(int i = 0; i < 5; i++) {
            createdDatabases.add(createDatabase(client));
        }
        allDatabases.addAll(createdDatabases);
    }

    public Database createDatabase(AsyncDocumentClient client) throws DocumentClientException {
        Database db = new Database();
        db.setId(UUID.randomUUID().toString());
        return client.createDatabase(db, null).toBlocking().single().getResource();
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        for (int i = 0; i < 5; i ++) {
            safeDeleteDatabase(client, createdDatabases.get(i).getId());
        }
        safeClose(client);
    }
}
