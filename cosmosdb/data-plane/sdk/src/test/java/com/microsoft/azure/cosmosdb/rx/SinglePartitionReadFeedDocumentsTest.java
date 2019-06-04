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

import com.microsoft.azure.cosmosdb.Database;
import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.FeedOptions;
import com.microsoft.azure.cosmosdb.FeedResponse;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import rx.Observable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class SinglePartitionReadFeedDocumentsTest extends TestSuiteBase {
    private Database createdDatabase;
    private DocumentCollection createdCollection;
    private List<Document> createdDocuments;

    private AsyncDocumentClient client;

    @Factory(dataProvider = "clientBuildersWithDirect")
    public SinglePartitionReadFeedDocumentsTest(AsyncDocumentClient.Builder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    @Test(groups = { "simple" }, timeOut = FEED_TIMEOUT)
    public void readDocuments() {
        final FeedOptions options = new FeedOptions();
        options.setMaxItemCount(2);
        final Observable<FeedResponse<Document>> feedObservable = client.readDocuments(getCollectionLink(), options);
        final int expectedPageSize = (createdDocuments.size() + options.getMaxItemCount() - 1) / options.getMaxItemCount();

        FeedResponseListValidator<Document> validator = new FeedResponseListValidator.Builder<Document>()
                .totalSize(createdDocuments.size())
                .numberOfPages(expectedPageSize)
                .exactlyContainsInAnyOrder(createdDocuments.stream().map(d -> d.getResourceId()).collect(Collectors.toList()))
                .allPagesSatisfy(new FeedResponseValidator.Builder<Document>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();
        validateQuerySuccess(feedObservable, validator, FEED_TIMEOUT);
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        client = clientBuilder.build();
        createdDatabase = SHARED_DATABASE;
        createdCollection = SHARED_SINGLE_PARTITION_COLLECTION_WITHOUT_PARTITION_KEY;
        truncateCollection(SHARED_SINGLE_PARTITION_COLLECTION_WITHOUT_PARTITION_KEY);

        List<Document> docDefList = new ArrayList<>();

        for(int i = 0; i < 5; i++) {
            docDefList.add(getDocumentDefinition());
        }

        createdDocuments = bulkInsertBlocking(client, getCollectionLink(), docDefList);
        waitIfNeededForReplicasToCatchUp(clientBuilder);
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }

    private Document getDocumentDefinition() {
        String uuid = UUID.randomUUID().toString();
        Document doc = new Document(String.format("{ "
            + "\"id\": \"%s\", "
            + "\"mypk\": \"%s\", "
            + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]"
            + "}"
            , uuid, uuid));
        return doc;
    }

    public String getCollectionLink() {
        return "dbs/" + getDatabaseId() + "/colls/" + getCollectionId();
    }

    private String getCollectionId() {
        return createdCollection.getId();
    }

    private String getDatabaseId() {
        return createdDatabase.getId();
    }
}
