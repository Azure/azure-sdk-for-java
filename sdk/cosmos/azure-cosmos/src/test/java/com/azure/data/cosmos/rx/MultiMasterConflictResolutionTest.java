// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.rx;

import com.azure.data.cosmos.*;
import com.azure.data.cosmos.CosmosAsyncClient;
import com.azure.data.cosmos.internal.FailureValidator;
import com.azure.data.cosmos.internal.Utils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

// assumes multi master is enabled in endpoint
public class MultiMasterConflictResolutionTest extends TestSuiteBase {
    private static final int TIMEOUT = 40000;

    private final String databaseId = CosmosDatabaseForTest.generateId();

    private PartitionKeyDefinition partitionKeyDef;
    private CosmosAsyncClient client;
    private CosmosAsyncDatabase database;

    @Factory(dataProvider = "clientBuilders")
    public MultiMasterConflictResolutionTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = "multi-master", timeOut = 10 * TIMEOUT)
    public void conflictResolutionPolicyCRUD() {

        // default last writer wins, path _ts
        CosmosContainerProperties collectionSettings = new CosmosContainerProperties(UUID.randomUUID().toString(), partitionKeyDef);
        CosmosAsyncContainer collection = database.createContainer(collectionSettings, new CosmosContainerRequestOptions()).block().getContainer();
        collectionSettings = collection.read().block().getProperties();

        assertThat(collectionSettings.getConflictResolutionPolicy().getMode()).isEqualTo(ConflictResolutionMode.LAST_WRITER_WINS);

        // LWW without getPath specified, should default to _ts
        collectionSettings.setConflictResolutionPolicy(ConflictResolutionPolicy.createLastWriterWinsPolicy());
        collectionSettings = collection.replace(collectionSettings, null).block().getProperties();

        assertThat(collectionSettings.getConflictResolutionPolicy().getMode()).isEqualTo(ConflictResolutionMode.LAST_WRITER_WINS);
        assertThat(collectionSettings.getConflictResolutionPolicy().getConflictResolutionPath()).isEqualTo("/_ts");

        // Tests the following scenarios
        // 1. LWW with valid path
        // 2. LWW with null path, should default to _ts
        // 3. LWW with empty path, should default to _ts
        testConflictResolutionPolicyRequiringPath(ConflictResolutionMode.LAST_WRITER_WINS,
                new String[] { "/a", null, "" }, new String[] { "/a", "/_ts", "/_ts" });

        // LWW invalid getPath
        collectionSettings.setConflictResolutionPolicy(ConflictResolutionPolicy.createLastWriterWinsPolicy("/a/b"));

        try {
            collectionSettings = collection.replace(collectionSettings, null).block().getProperties();
            fail("Expected exception on invalid getPath.");
        } catch (Exception e) {

            // when (e.StatusCode == HttpStatusCode.BadRequest)
            CosmosClientException dce = Utils.as(e.getCause(), CosmosClientException.class);
            if (dce != null && dce.getStatusCode() == 400) {
                assertThat(dce.getMessage()).contains("Invalid path '\\/a\\/b' for last writer wins conflict resolution");
            } else {
                throw e;
            }
        }

        // LWW invalid getPath

        collectionSettings.setConflictResolutionPolicy(ConflictResolutionPolicy.createLastWriterWinsPolicy("someText"));

        try {
            collectionSettings = collection.replace(collectionSettings, null).block().getProperties();
            fail("Expected exception on invalid path.");
        } catch (Exception e) {
            // when (e.StatusCode == HttpStatusCode.BadRequest)
            CosmosClientException dce = Utils.as(e.getCause(), CosmosClientException.class);
            if (dce != null && dce.getStatusCode() == 400) {
                assertThat(dce.getMessage()).contains("Invalid path 'someText' for last writer wins conflict resolution");
            } else {
                throw e;
            }
        }

        // Tests the following scenarios
        // 1. CUSTOM with valid sprocLink
        // 2. CUSTOM with null sprocLink, should default to empty string
        // 3. CUSTOM with empty sprocLink, should default to empty string
        testConflictResolutionPolicyRequiringPath(ConflictResolutionMode.CUSTOM, new String[] { "dbs/mydb/colls" +
            "/mycoll/sprocs/randomSprocName", null, "" }, new String[] { "dbs/mydb/colls/mycoll/sprocs" +
            "/randomSprocName", "", "" });
    }

    private void testConflictResolutionPolicyRequiringPath(ConflictResolutionMode conflictResolutionMode,
            String[] paths, String[] expectedPaths) {
        for (int i = 0; i < paths.length; i++) {
            CosmosContainerProperties collectionSettings = new CosmosContainerProperties(UUID.randomUUID().toString(), partitionKeyDef);

            if (conflictResolutionMode == ConflictResolutionMode.LAST_WRITER_WINS) {
                collectionSettings.setConflictResolutionPolicy(ConflictResolutionPolicy.createLastWriterWinsPolicy(paths[i]));
            } else {
                collectionSettings.setConflictResolutionPolicy(ConflictResolutionPolicy.createCustomPolicy(paths[i]));
            }
            collectionSettings = database.createContainer(collectionSettings, new CosmosContainerRequestOptions()).block().getProperties();
            assertThat(collectionSettings.getConflictResolutionPolicy().getMode()).isEqualTo(conflictResolutionMode);

            if (conflictResolutionMode == ConflictResolutionMode.LAST_WRITER_WINS) {
                assertThat(collectionSettings.getConflictResolutionPolicy().getConflictResolutionPath()).isEqualTo(expectedPaths[i]);
            } else {
                assertThat(collectionSettings.getConflictResolutionPolicy().getConflictResolutionProcedure()).isEqualTo(expectedPaths[i]);
            }
        }
    }

    @Test(groups = "multi-master", timeOut = TIMEOUT)
    public void invalidConflictResolutionPolicy_LastWriterWinsWithStoredProc() throws Exception {
        CosmosContainerProperties collection = new CosmosContainerProperties(UUID.randomUUID().toString(), partitionKeyDef);

        // LWW without path specified, should default to _ts
        ConflictResolutionPolicy policy = BridgeUtils.createConflictResolutionPolicy();
        BridgeUtils.setMode(policy, ConflictResolutionMode.LAST_WRITER_WINS);
        BridgeUtils.setStoredProc(policy,"randomSprocName");
        collection.setConflictResolutionPolicy(policy);

        Mono<CosmosAsyncContainerResponse> createObservable = database.createContainer(
                collection,
                new CosmosContainerRequestOptions());

        FailureValidator validator = new FailureValidator.Builder()
                .instanceOf(CosmosClientException.class)
                .statusCode(400)
                .errorMessageContains("LastWriterWins conflict resolution mode should not have conflict resolution procedure set.")
                .build();
        validateFailure(createObservable, validator);
    }

    @Test(groups = "multi-master", timeOut = TIMEOUT)
    public void invalidConflictResolutionPolicy_CustomWithPath() throws Exception {
        CosmosContainerProperties collection = new CosmosContainerProperties(UUID.randomUUID().toString(), partitionKeyDef);

        // LWW without getPath specified, should default to _ts
        ConflictResolutionPolicy policy = BridgeUtils.createConflictResolutionPolicy();
        BridgeUtils.setMode(policy, ConflictResolutionMode.CUSTOM);
        BridgeUtils.setPath(policy,"/mypath");
        collection.setConflictResolutionPolicy(policy);

        Mono<CosmosAsyncContainerResponse> createObservable = database.createContainer(
                collection,
                new CosmosContainerRequestOptions());

        FailureValidator validator = new FailureValidator.Builder()
                .instanceOf(CosmosClientException.class)
                .statusCode(400)
                .errorMessageContains("Custom conflict resolution mode should not have conflict resolution path set.")
                .build();
        validateFailure(createObservable, validator);
    }

    @BeforeClass(groups = {"multi-master"}, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        // set up the client

        client = clientBuilder().buildAsyncClient();
        database = createDatabase(client, databaseId);
        partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<String>();
        paths.add("/mypk");
        partitionKeyDef.setPaths(paths);
    }

    @AfterClass(groups = {"multi-master"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(database);
        safeClose(client);
    }
}
