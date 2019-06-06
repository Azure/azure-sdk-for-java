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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.microsoft.azure.cosmosdb.DatabaseForTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.microsoft.azure.cosmosdb.Database;
import com.microsoft.azure.cosmosdb.DocumentClientException;
import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.FeedOptions;
import com.microsoft.azure.cosmosdb.FeedResponse;

import rx.Observable;

import javax.net.ssl.SSLException;

public class ReadFeedCollectionsTest extends TestSuiteBase {

    protected static final int FEED_TIMEOUT = 60000;
    protected static final int SETUP_TIMEOUT = 60000;
    protected static final int SHUTDOWN_TIMEOUT = 20000;

    public final String databaseId = DatabaseForTest.generateId();

    private Database createdDatabase;
    private List<DocumentCollection> createdCollections = new ArrayList<>();

    private AsyncDocumentClient client;

    @Factory(dataProvider = "clientBuilders")
    public ReadFeedCollectionsTest(AsyncDocumentClient.Builder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    @Test(groups = { "simple" }, timeOut = FEED_TIMEOUT)
    public void readCollections() throws Exception {

        FeedOptions options = new FeedOptions();
        options.setMaxItemCount(2);

        Observable<FeedResponse<DocumentCollection>> feedObservable = client.readCollections(getDatabaseLink(), options);

        int expectedPageSize = (createdCollections.size() + options.getMaxItemCount() - 1) / options.getMaxItemCount();

        FeedResponseListValidator<DocumentCollection> validator = new FeedResponseListValidator.Builder<DocumentCollection>()
                .totalSize(createdCollections.size())
                .exactlyContainsInAnyOrder(createdCollections.stream().map(d -> d.getResourceId()).collect(Collectors.toList()))
                .numberOfPages(expectedPageSize)
                .pageSatisfy(0, new FeedResponseValidator.Builder<DocumentCollection>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();

        validateQuerySuccess(feedObservable, validator, FEED_TIMEOUT);

    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        client = clientBuilder.build();
        createdDatabase = createDatabase(client, databaseId);

        for(int i = 0; i < 3; i++) {
            createdCollections.add(createCollections(client));
        }
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(client, createdDatabase);
        safeClose(client);
    }

    public DocumentCollection createCollections(AsyncDocumentClient client) {
        DocumentCollection collection = new DocumentCollection();
        collection.setId(UUID.randomUUID().toString());
        return client.createCollection(getDatabaseLink(), collection, null).toBlocking().single().getResource();
    }

    private String getDatabaseLink() {
        return "dbs/" + createdDatabase.getId();
    }
}
