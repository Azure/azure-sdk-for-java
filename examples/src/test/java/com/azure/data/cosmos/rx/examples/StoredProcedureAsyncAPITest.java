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

package com.azure.data.cosmos.rx.examples;

import com.azure.data.cosmos.ConnectionMode;
import com.azure.data.cosmos.ConnectionPolicy;
import com.azure.data.cosmos.ConsistencyLevel;
import com.azure.data.cosmos.DataType;
import com.azure.data.cosmos.Database;
import com.azure.data.cosmos.DocumentCollection;
import com.azure.data.cosmos.IncludedPath;
import com.azure.data.cosmos.Index;
import com.azure.data.cosmos.IndexingPolicy;
import com.azure.data.cosmos.PartitionKey;
import com.azure.data.cosmos.PartitionKeyDefinition;
import com.azure.data.cosmos.RequestOptions;
import com.azure.data.cosmos.StoredProcedure;
import com.azure.data.cosmos.internal.HttpConstants;
import com.azure.data.cosmos.AsyncDocumentClient;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;

/**
 * This integration test class demonstrates how to use Async API to create
 * and execute Stored Procedures.
 */
public class StoredProcedureAsyncAPITest {
    private final static int TIMEOUT = 60000;

    private Database createdDatabase;
    private DocumentCollection createdCollection;
    private AsyncDocumentClient asyncClient;

    @BeforeClass(groups = "samples", timeOut = TIMEOUT)
    public void setUp() {
        ConnectionPolicy connectionPolicy = new ConnectionPolicy();
        connectionPolicy.connectionMode(ConnectionMode.DIRECT);
        asyncClient = new AsyncDocumentClient.Builder()
                .withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.SESSION)
                .build();

        createdDatabase = Utils.createDatabaseForTest(asyncClient);

        createdCollection = asyncClient
                .createCollection("dbs/" + createdDatabase.id(), getMultiPartitionCollectionDefinition(), null)
                .toBlocking().single().getResource();
    }

    @AfterClass(groups = "samples", timeOut = TIMEOUT)
    public void shutdown() {
        Utils.safeClean(asyncClient, createdDatabase);
        Utils.safeClose(asyncClient);
    }

    /**
     * Execute Stored Procedure and retrieve the Script Log
     */
    @Test(groups = "samples", timeOut = TIMEOUT)
    public void scriptConsoleLogEnabled() throws Exception {
        // CREATE a stored procedure
        StoredProcedure storedProcedure = new StoredProcedure(
                "{" +
                        "  'id':'storedProcedureSample'," +
                        "  'body':" +
                        "    'function() {" +
                        "        var mytext = \"x\";" +
                        "        var myval = 1;" +
                        "        try {" +
                        "            console.log(\"The value of %s is %s.\", mytext, myval);" +
                        "            getContext().getResponse().body(\"Success!\");" +
                        "        }" +
                        "        catch(err) {" +
                        "            getContext().getResponse().body(\"inline err: [\" + err.number + \"] \" + err);" +
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
        asyncClient.executeStoredProcedure(getSprocLink(storedProcedure), requestOptions, new Object[]{})
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
    @Test(groups = "samples", timeOut = TIMEOUT)
    public void executeStoredProcWithArgs() throws Exception {
        // CREATE stored procedure
        StoredProcedure storedProcedure = new StoredProcedure(
                "{" +
                        "  'id': 'multiplySample'," +
                        "  'body':" +
                        "    'function (value, num) {" +
                        "      getContext().getResponse().body(" +
                        "          \"2*\" + value + \" is \" + num * 2 );" +
                        "    }'" +
                        "}");

        storedProcedure = asyncClient.createStoredProcedure(getCollectionLink(), storedProcedure, null)
                .toBlocking().single().getResource();

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.setPartitionKey(new PartitionKey("Seattle"));

        final CountDownLatch successfulCompletionLatch = new CountDownLatch(1);

        // Execute the stored procedure
        Object[] storedProcedureArgs = new Object[]{"a", 123};
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
    @Test(groups = "samples", timeOut = TIMEOUT)
    public void executeStoredProcWithPojoArgs() throws Exception {
        // create stored procedure
        StoredProcedure storedProcedure = new StoredProcedure(
                "{" +
                        "  'id': 'storedProcedurePojoSample'," +
                        "  'body':" +
                        "    'function (value) {" +
                        "      getContext().getResponse().body(" +
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
        Object[] storedProcedureArgs = new Object[]{samplePojo};
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

    private static DocumentCollection getMultiPartitionCollectionDefinition() {
        DocumentCollection collectionDefinition = new DocumentCollection();
        collectionDefinition.id(UUID.randomUUID().toString());

        // Set the partitionKeyDefinition for a partitioned collection
        // Here, we are setting the partitionKey of the Collection to be /city
        PartitionKeyDefinition partitionKeyDefinition = new PartitionKeyDefinition();
        List<String> paths = new ArrayList<String>();
        paths.add("/city");
        partitionKeyDefinition.paths(paths);
        collectionDefinition.setPartitionKey(partitionKeyDefinition);

        // Set indexing policy to be range range for string and number
        IndexingPolicy indexingPolicy = new IndexingPolicy();
        Collection<IncludedPath> includedPaths = new ArrayList<IncludedPath>();
        IncludedPath includedPath = new IncludedPath();
        includedPath.path("/*");
        Collection<Index> indexes = new ArrayList<Index>();
        Index stringIndex = Index.Range(DataType.STRING);
        stringIndex.set("precision", -1);
        indexes.add(stringIndex);

        Index numberIndex = Index.Range(DataType.NUMBER);
        numberIndex.set("precision", -1);
        indexes.add(numberIndex);
        includedPath.indexes(indexes);
        includedPaths.add(includedPath);
        indexingPolicy.setIncludedPaths(includedPaths);
        collectionDefinition.setIndexingPolicy(indexingPolicy);

        return collectionDefinition;
    }

    private String getCollectionLink() {
        return "dbs/" + createdDatabase.id() + "/colls/" + createdCollection.id();
    }

    private String getSprocLink(StoredProcedure sproc) {
        return "dbs/" + createdDatabase.id() + "/colls/" + createdCollection.id() + "/sprocs/" + sproc.id();
    }
}