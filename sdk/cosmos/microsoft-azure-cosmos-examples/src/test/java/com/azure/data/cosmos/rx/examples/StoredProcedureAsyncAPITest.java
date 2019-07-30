// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.rx.examples;

import com.azure.data.cosmos.internal.AsyncDocumentClient;
import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.ConnectionMode;
import com.azure.data.cosmos.ConnectionPolicy;
import com.azure.data.cosmos.ConsistencyLevel;
import com.azure.data.cosmos.DataType;
import com.azure.data.cosmos.internal.Database;
import com.azure.data.cosmos.DocumentClientTest;
import com.azure.data.cosmos.internal.DocumentCollection;
import com.azure.data.cosmos.IncludedPath;
import com.azure.data.cosmos.Index;
import com.azure.data.cosmos.IndexingPolicy;
import com.azure.data.cosmos.PartitionKey;
import com.azure.data.cosmos.PartitionKeyDefinition;
import com.azure.data.cosmos.internal.RequestOptions;
import com.azure.data.cosmos.internal.StoredProcedure;
import com.azure.data.cosmos.internal.HttpConstants;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
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
public class StoredProcedureAsyncAPITest extends DocumentClientTest {
    private final static int TIMEOUT = 60000;

    private Database createdDatabase;
    private DocumentCollection createdCollection;
    private AsyncDocumentClient client;

    @BeforeClass(groups = "samples", timeOut = TIMEOUT)
    public void setUp() {

        ConnectionPolicy connectionPolicy = new ConnectionPolicy().connectionMode(ConnectionMode.DIRECT);

        this.clientBuilder()
            .withServiceEndpoint(TestConfigurations.HOST)
            .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
            .withConnectionPolicy(connectionPolicy)
            .withConsistencyLevel(ConsistencyLevel.SESSION);

        this.client = this.clientBuilder().build();

        createdDatabase = Utils.createDatabaseForTest(client);

        createdCollection = client
                .createCollection("dbs/" + createdDatabase.id(), getMultiPartitionCollectionDefinition(), null)
                .single().block().getResource();
    }

    @AfterClass(groups = "samples", timeOut = TIMEOUT)
    public void shutdown() {
        Utils.safeClean(client, createdDatabase);
        Utils.safeClose(client);
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
                        "            getContext().getResponse().setBody(\"Success!\");" +
                        "        }" +
                        "        catch(err) {" +
                        "            getContext().getResponse().setBody(\"inline err: [\" + err.number + \"] \" + err);" +
                        "        }" +
                        "    }'" +
                        "}");

        storedProcedure = client.createStoredProcedure(getCollectionLink(), storedProcedure, null)
                                .single().block().getResource();

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.setScriptLoggingEnabled(true);
        requestOptions.setPartitionKey(new PartitionKey("Seattle"));

        final CountDownLatch successfulCompletionLatch = new CountDownLatch(1);

        // Execute the stored procedure
        client.executeStoredProcedure(getSprocLink(storedProcedure), requestOptions, new Object[]{})
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
                        "      getContext().getResponse().setBody(" +
                        "          \"2*\" + value + \" is \" + num * 2 );" +
                        "    }'" +
                        "}");

        storedProcedure = client.createStoredProcedure(getCollectionLink(), storedProcedure, null)
                                .single().block().getResource();

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.setPartitionKey(new PartitionKey("Seattle"));

        final CountDownLatch successfulCompletionLatch = new CountDownLatch(1);

        // Execute the stored procedure
        Object[] storedProcedureArgs = new Object[]{"a", 123};
        client.executeStoredProcedure(getSprocLink(storedProcedure), requestOptions, storedProcedureArgs)
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
                        "      getContext().getResponse().setBody(" +
                        "          \"a is \" + value.temp);" +
                        "    }'" +
                        "}");

        storedProcedure = client.createStoredProcedure(getCollectionLink(), storedProcedure, null)
                                .single().block().getResource();

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
        client.executeStoredProcedure(getSprocLink(storedProcedure), requestOptions, storedProcedureArgs)
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
        List<IncludedPath> includedPaths = new ArrayList<IncludedPath>();
        IncludedPath includedPath = new IncludedPath();
        includedPath.path("/*");
        List<Index> indexes = new ArrayList<Index>();
        Index stringIndex = Index.Range(DataType.STRING);
        BridgeInternal.setProperty(stringIndex, "precision", -1);
        indexes.add(stringIndex);

        Index numberIndex = Index.Range(DataType.NUMBER);
        BridgeInternal.setProperty(numberIndex, "precision", -1);
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