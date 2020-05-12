// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.rx.examples;

import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.models.DataType;
import com.azure.cosmos.implementation.Database;
import com.azure.cosmos.DocumentClientTest;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.models.IncludedPath;
import com.azure.cosmos.models.Index;
import com.azure.cosmos.models.IndexingPolicy;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.implementation.RequestOptions;
import com.azure.cosmos.implementation.StoredProcedure;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.TestConfigurations;
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
    public void before_StoredProcedureAsyncAPITest() {

        ConnectionPolicy connectionPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());

        this.clientBuilder()
            .withServiceEndpoint(TestConfigurations.HOST)
            .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
            .withConnectionPolicy(connectionPolicy)
            .withConsistencyLevel(ConsistencyLevel.SESSION)
            .withContentResponseOnWriteEnabled(true);

        this.client = this.clientBuilder().build();

        createdDatabase = Utils.createDatabaseForTest(client);

        createdCollection = client
                .createCollection("dbs/" + createdDatabase.getId(), getMultiPartitionCollectionDefinition(), null)
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
        collectionDefinition.setId(UUID.randomUUID().toString());

        // Set the partitionKeyDefinition for a partitioned collection
        // Here, we are setting the partitionKey of the Collection to be /city
        PartitionKeyDefinition partitionKeyDefinition = new PartitionKeyDefinition();
        List<String> paths = new ArrayList<String>();
        paths.add("/city");
        partitionKeyDefinition.setPaths(paths);
        collectionDefinition.setPartitionKey(partitionKeyDefinition);

        // Set indexing policy to be range range for string and number
        IndexingPolicy indexingPolicy = new IndexingPolicy();
        List<IncludedPath> includedPaths = new ArrayList<IncludedPath>();
        IncludedPath includedPath = new IncludedPath();
        includedPath.setPath("/*");
        List<Index> indexes = new ArrayList<Index>();
        Index stringIndex = Index.range(DataType.STRING);
        BridgeInternal.setProperty(ModelBridgeInternal.getJsonSerializableFromIndex(stringIndex), "precision", -1);
        indexes.add(stringIndex);

        Index numberIndex = Index.range(DataType.NUMBER);
        BridgeInternal.setProperty(ModelBridgeInternal.getJsonSerializableFromIndex(numberIndex), "precision", -1);
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
}
