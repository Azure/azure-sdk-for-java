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
import java.util.UUID;
import java.util.stream.Collectors;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.microsoft.azure.cosmosdb.Database;
import com.microsoft.azure.cosmosdb.DocumentClientException;
import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.FeedOptions;
import com.microsoft.azure.cosmosdb.FeedResponse;
import com.microsoft.azure.cosmosdb.StoredProcedure;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient.Builder;

import rx.Observable;


public class StoredProcedureQueryTest extends TestSuiteBase {

    public final static String DATABASE_ID = getDatabaseId(StoredProcedureQueryTest.class);

    private Database createdDatabase;
    private DocumentCollection createdCollection;
    private List<StoredProcedure> createdStoredProcs = new ArrayList<>();

    private Builder clientBuilder;
    private AsyncDocumentClient client;

    public String getCollectionLink() {
        return Utils.getCollectionNameLink(createdDatabase.getId(), createdCollection.getId());
    }

    static protected DocumentCollection getCollectionDefinition() {

        DocumentCollection collectionDefinition = new DocumentCollection();
        collectionDefinition.setId(UUID.randomUUID().toString());

        return collectionDefinition;
    }

    @Factory(dataProvider = "clientBuilders")
    public StoredProcedureQueryTest(AsyncDocumentClient.Builder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryWithFilter() throws Exception {

        String filterId = createdStoredProcs.get(0).getId();
        String query = String.format("SELECT * from c where c.id = '%s'", filterId);

        FeedOptions options = new FeedOptions();
        options.setMaxItemCount(5);
        Observable<FeedResponse<StoredProcedure>> queryObservable = client
                .queryStoredProcedures(getCollectionLink(), query, options);

        List<StoredProcedure> expectedDocs = createdStoredProcs.stream().filter(sp -> filterId.equals(sp.getId()) ).collect(Collectors.toList());
        assertThat(expectedDocs).isNotEmpty();

        int expectedPageSize = (expectedDocs.size() + options.getMaxItemCount() - 1) / options.getMaxItemCount();

        FeedResponseListValidator<StoredProcedure> validator = new FeedResponseListValidator.Builder<StoredProcedure>()
                .totalSize(expectedDocs.size())
                .exactlyContainsInAnyOrder(expectedDocs.stream().map(d -> d.getResourceId()).collect(Collectors.toList()))
                .numberOfPages(expectedPageSize)
                .pageSatisfy(0, new FeedResponseValidator.Builder<StoredProcedure>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();

        validateQuerySuccess(queryObservable, validator, 10000);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void query_NoResults() throws Exception {

        String query = "SELECT * from root r where r.id = '2'";
        FeedOptions options = new FeedOptions();
        options.setEnableCrossPartitionQuery(true);
        Observable<FeedResponse<StoredProcedure>> queryObservable = client
                .queryStoredProcedures(getCollectionLink(), query, options);

        FeedResponseListValidator<StoredProcedure> validator = new FeedResponseListValidator.Builder<StoredProcedure>()
                .containsExactly(new ArrayList<>())
                .numberOfPages(1)
                .pageSatisfy(0, new FeedResponseValidator.Builder<StoredProcedure>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();
        validateQuerySuccess(queryObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryAll() throws Exception {

        String query = "SELECT * from root";
        FeedOptions options = new FeedOptions();
        options.setMaxItemCount(3);
        options.setEnableCrossPartitionQuery(true);
        Observable<FeedResponse<StoredProcedure>> queryObservable = client
                .queryStoredProcedures(getCollectionLink(), query, options);

        List<StoredProcedure> expectedDocs = createdStoredProcs;      
        
        int expectedPageSize = (expectedDocs.size() + options.getMaxItemCount() - 1) / options.getMaxItemCount();

        FeedResponseListValidator<StoredProcedure> validator = new FeedResponseListValidator
                .Builder<StoredProcedure>()
                .exactlyContainsInAnyOrder(expectedDocs
                        .stream()
                        .map(d -> d.getResourceId())
                        .collect(Collectors.toList()))
                .numberOfPages(expectedPageSize)
                .allPagesSatisfy(new FeedResponseValidator.Builder<StoredProcedure>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();
        validateQuerySuccess(queryObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void invalidQuerySytax() throws Exception {
        String query = "I am an invalid query";
        FeedOptions options = new FeedOptions();
        options.setEnableCrossPartitionQuery(true);
        Observable<FeedResponse<StoredProcedure>> queryObservable = client
                .queryStoredProcedures(getCollectionLink(), query, options);

        FailureValidator validator = new FailureValidator.Builder()
                .instanceOf(DocumentClientException.class)
                .statusCode(400)
                .notNullActivityId()
                .build();
        validateQueryFailure(queryObservable, validator);
    }

    public StoredProcedure createStoredProc(AsyncDocumentClient client) throws DocumentClientException {
        StoredProcedure storedProcedure = getStoredProcedureDef();
        return client.createStoredProcedure(getCollectionLink(), storedProcedure, null).toBlocking().single().getResource();
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() throws Exception {
        client = clientBuilder.build();
        Database d = new Database();
        d.setId(DATABASE_ID);
        createdDatabase = safeCreateDatabase(client, d);
        createdCollection = createCollection(client, createdDatabase.getId(), getCollectionDefinition());

        for(int i = 0; i < 5; i++) {
            createdStoredProcs.add(createStoredProc(client));
        }
        
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(client, createdDatabase.getId());
        safeClose(client);
    }

    private static StoredProcedure getStoredProcedureDef() {
        StoredProcedure storedProcedureDef = new StoredProcedure();
        storedProcedureDef.setId(UUID.randomUUID().toString());
        storedProcedureDef.setBody("function() {var x = 10;}");
        return storedProcedureDef;
    }
}
