/*
 * The MIT License (MIT)
 * Copyright (c) 2017 Microsoft Corporation
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

package com.microsoft.azure.cosmosdb.rx.examples;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.azure.cosmosdb.ConnectionPolicy;
import com.microsoft.azure.cosmosdb.ConsistencyLevel;
import com.microsoft.azure.cosmosdb.FeedResponse;
import com.microsoft.azure.cosmosdb.SqlParameter;
import com.microsoft.azure.cosmosdb.SqlParameterCollection;
import com.microsoft.azure.cosmosdb.SqlQuerySpec;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;
import com.microsoft.azure.cosmosdb.DataType;
import com.microsoft.azure.cosmosdb.Database;
import com.microsoft.azure.cosmosdb.DocumentClientException;
import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.IncludedPath;
import com.microsoft.azure.cosmosdb.Index;
import com.microsoft.azure.cosmosdb.IndexingPolicy;
import com.microsoft.azure.cosmosdb.PartitionKey;
import com.microsoft.azure.cosmosdb.PartitionKeyDefinition;
import com.microsoft.azure.cosmosdb.RequestOptions;
import com.microsoft.azure.cosmosdb.StoredProcedure;
import com.microsoft.azure.cosmosdb.internal.HttpConstants;

/**
 * This integration test class demonstrates how to use Async API to create
 * and execute Stored Procedures.
 */
public class StoredProcedureAsyncAPITest {
    private static final Logger LOGGER = LoggerFactory.getLogger(StoredProcedureAsyncAPITest.class);

    private static final String DATABASE_ID = "async-test-db";
    private Database createdDatabase;
    private DocumentCollection createdCollection;

    private AsyncDocumentClient asyncClient;

    @Before
    public void setUp() throws DocumentClientException {

        asyncClient = new AsyncDocumentClient.Builder()
                .withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKey(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(ConnectionPolicy.GetDefault())
                .withConsistencyLevel(ConsistencyLevel.Session)
                .build();

        // Clean up before setting up
        this.cleanUpGeneratedDatabases();

        createdDatabase = new Database();
        createdDatabase.setId(DATABASE_ID);
        createdDatabase = asyncClient.createDatabase(createdDatabase, null).toBlocking().single().getResource();

        createdCollection = asyncClient
                .createCollection("dbs/" + createdDatabase.getId(), getMultiPartitionCollectionDefinition(), null)
                .toBlocking().single().getResource();
    }

    @After
    public void shutdown() throws DocumentClientException {
        asyncClient.close();
    }

    /**
     * Execute Stored Procedure and retrieve the Script Log
     */
    @Test
    public void testScriptConsoleLogEnabled() throws Exception {
        // Create a stored procedure
        StoredProcedure storedProcedure = new StoredProcedure( 
                "{" +
                "  'id':'storedProcedureSample'," +
                "  'body':" +
                "    'function() {" +
                "        var mytext = \"x\";" +
                "        var myval = 1;" +
                "        try {" +
                "            console.log(\"The value of %s is %s.\", mytext, myval);" +
                "            getContext().getResponse().setBody(\"Success!\");" +
                "        }" +
                "        catch(err) {" +
                "            getContext().getResponse().setBody(\"inline err: [\" + err.number + \"] \" + err);" +
                "        }" +
                "    }'" +
                "}");

        storedProcedure = asyncClient.createStoredProcedure(getCollectionLink(), storedProcedure, null)
                .toBlocking().single().getResource();

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.setScriptLoggingEnabled(true);
        requestOptions.setPartitionKey(new PartitionKey("Seattle"));

        final CountDownLatch successfulCompletionLatch = new CountDownLatch(1);

        // Execute the stored procedure
        asyncClient.executeStoredProcedure(getSprocLink(storedProcedure), requestOptions, new Object[] {})
                .subscribe(storedProcedureResponse -> {
                    String logResult = "The value of x is 1.";
                    try {
                        assertThat(URLDecoder.decode(storedProcedureResponse.getScriptLog(), "UTF-8"), is(logResult));
                        assertThat(URLDecoder.decode(storedProcedureResponse.getResponseHeaders()
                                .get(HttpConstants.HttpHeaders.SCRIPT_LOG_RESULTS), "UTF-8"), is(logResult));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    successfulCompletionLatch.countDown();
                    System.out.println(storedProcedureResponse.getActivityId());
                }, error -> {
                    System.err.println("an error occurred while executing the stored procedure: actual cause: "
                            + error.getMessage());
                });

        successfulCompletionLatch.await();
    }

    /**
     * Execute Stored Procedure that takes arguments
     */
    @Test
    public void testExecuteStoredProcWithArgs() throws Exception {
        // Create stored procedure
        StoredProcedure storedProcedure = new StoredProcedure(
                "{" +
                        "  'id': 'multiplySample'," +
                        "  'body':" +
                        "    'function (value, num) {" +
                        "      getContext().getResponse().setBody(" +
                        "          \"2*\" + value + \" is \" + num * 2 );" +
                        "    }'" +
                "}");

        storedProcedure = asyncClient.createStoredProcedure(getCollectionLink(), storedProcedure, null)
                .toBlocking().single().getResource();

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.setPartitionKey(new PartitionKey("Seattle"));

        final CountDownLatch successfulCompletionLatch = new CountDownLatch(1);

        // Execute the stored procedure
        Object[] storedProcedureArgs = new Object[] { "a", 123 };
        asyncClient.executeStoredProcedure(getSprocLink(storedProcedure), requestOptions, storedProcedureArgs)
                .subscribe(storedProcedureResponse -> {
                    String storedProcResultAsString = storedProcedureResponse.getResponseAsString();
                    assertThat(storedProcResultAsString, equalTo("\"2*a is 246\""));
                    successfulCompletionLatch.countDown();
                    System.out.println(storedProcedureResponse.getActivityId());
                }, error -> {
                    System.err.println("an error occurred while executing the stored procedure: actual cause: "
                            + error.getMessage());
                });

        successfulCompletionLatch.await();
    }

    /**
     * Execute Stored Procedure that takes arguments, passing a Pojo object
     */
    @Test
    public void testExecuteStoredProcWithPojoArgs() throws Exception {
        // create stored procedure
        StoredProcedure storedProcedure = new StoredProcedure(
                "{" +
                        "  'id': 'storedProcedurePojoSample'," +
                        "  'body':" +
                        "    'function (value) {" +
                        "      getContext().getResponse().setBody(" +
                        "          \"a is \" + value.temp);" +
                        "    }'" +
                "}");

        storedProcedure = asyncClient.createStoredProcedure(getCollectionLink(), storedProcedure, null)
                .toBlocking().single().getResource();

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.setPartitionKey(new PartitionKey("Seattle"));

        final CountDownLatch successfulCompletionLatch = new CountDownLatch(1);

        // POJO
        class SamplePojo {
            public String temp = "my temp value";
        }
        SamplePojo samplePojo = new SamplePojo();

        // Execute the stored procedure
        Object[] storedProcedureArgs = new Object[] { samplePojo };
        asyncClient.executeStoredProcedure(getSprocLink(storedProcedure), requestOptions, storedProcedureArgs)
                .subscribe(storedProcedureResponse -> {
                    String storedProcResultAsString = storedProcedureResponse.getResponseAsString();
                    assertThat(storedProcResultAsString, equalTo("\"a is my temp value\""));
                    successfulCompletionLatch.countDown();
                    System.out.println(storedProcedureResponse.getActivityId());
                }, error -> {
                    System.err.println("an error occurred while executing the stored procedure: actual cause: "
                            + error.getMessage());
                });

        successfulCompletionLatch.await();
    }

    private DocumentCollection getMultiPartitionCollectionDefinition() {
        DocumentCollection collectionDefinition = new DocumentCollection();
        collectionDefinition.setId(UUID.randomUUID().toString());

        // Set the partitionKeyDefinition for a partitioned collection
        // Here, we are setting the partitionKey of the Collection to be /city
        PartitionKeyDefinition partitionKeyDefinition = new PartitionKeyDefinition();
        Collection<String> paths = new ArrayList<String>();
        paths.add("/city");
        partitionKeyDefinition.setPaths(paths);
        collectionDefinition.setPartitionKey(partitionKeyDefinition);

        // Set indexing policy to be range range for string and number
        IndexingPolicy indexingPolicy = new IndexingPolicy();
        Collection<IncludedPath> includedPaths = new ArrayList<IncludedPath>();
        IncludedPath includedPath = new IncludedPath();
        includedPath.setPath("/*");
        Collection<Index> indexes = new ArrayList<Index>();
        Index stringIndex = Index.Range(DataType.String);
        stringIndex.set("precision", -1);
        indexes.add(stringIndex);

        Index numberIndex = Index.Range(DataType.Number);
        numberIndex.set("precision", -1);
        indexes.add(numberIndex);
        includedPath.setIndexes(indexes);
        includedPaths.add(includedPath);
        indexingPolicy.setIncludedPaths(includedPaths);
        collectionDefinition.setIndexingPolicy(indexingPolicy);

        return collectionDefinition;
    }

    private String getCollectionLink() {
        return "dbs/" + createdDatabase.getId() + "/colls/" + createdCollection.getId();
    }

    private String getSprocLink(StoredProcedure sproc) {
        return "dbs/" + createdDatabase.getId() + "/colls/" + createdCollection.getId() + "/sprocs/" + sproc.getId();
    }

    private void cleanUpGeneratedDatabases() throws DocumentClientException {
        LOGGER.info("cleanup databases invoked");

        String[] allDatabaseIds = { DATABASE_ID };

        for (String id : allDatabaseIds) {
            try {
                List<FeedResponse<Database>> feedResponsePages = asyncClient
                        .queryDatabases(new SqlQuerySpec("SELECT * FROM root r WHERE r.id=@id",
                                new SqlParameterCollection(new SqlParameter("@id", id))), null)
                        .toList().toBlocking().single();

                if (!feedResponsePages.get(0).getResults().isEmpty()) {
                    Database res = feedResponsePages.get(0).getResults().get(0);
                    LOGGER.info("deleting a database " + feedResponsePages.get(0));
                    asyncClient.deleteDatabase("dbs/" + res.getId(), null).toBlocking().single();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}