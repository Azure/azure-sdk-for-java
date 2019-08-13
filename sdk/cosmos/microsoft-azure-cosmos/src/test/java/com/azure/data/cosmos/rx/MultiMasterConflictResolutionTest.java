// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.rx;

import com.azure.data.cosmos.BridgeUtils;
import com.azure.data.cosmos.ConflictResolutionMode;
import com.azure.data.cosmos.ConflictResolutionPolicy;
import com.azure.data.cosmos.CosmosClient;
import com.azure.data.cosmos.CosmosClientBuilder;
import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.CosmosContainer;
import com.azure.data.cosmos.CosmosContainerProperties;
import com.azure.data.cosmos.CosmosContainerRequestOptions;
import com.azure.data.cosmos.CosmosContainerResponse;
import com.azure.data.cosmos.CosmosDatabase;
import com.azure.data.cosmos.CosmosDatabaseForTest;
import com.azure.data.cosmos.PartitionKeyDefinition;
import com.azure.data.cosmos.internal.FailureValidator;
import com.azure.data.cosmos.internal.TestUtils;
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
    private CosmosClient client;
    private CosmosDatabase database;

    @Factory(dataProvider = "clientBuilders")
    public MultiMasterConflictResolutionTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = "multi-master", timeOut = 10 * TIMEOUT)
    public void conflictResolutionPolicyCRUD() {

        // default last writer wins, path _ts
        CosmosContainerProperties collectionSettings = new CosmosContainerProperties(UUID.randomUUID().toString(), partitionKeyDef);
        CosmosContainer collection = database.createContainer(collectionSettings, new CosmosContainerRequestOptions()).block().container();
        collectionSettings = collection.read().block().properties();

        assertThat(collectionSettings.conflictResolutionPolicy().mode()).isEqualTo(ConflictResolutionMode.LAST_WRITER_WINS);

        // LWW without path specified, should default to _ts
        collectionSettings.conflictResolutionPolicy(ConflictResolutionPolicy.createLastWriterWinsPolicy());
        collectionSettings = collection.replace(collectionSettings, null).block().properties();

        assertThat(collectionSettings.conflictResolutionPolicy().mode()).isEqualTo(ConflictResolutionMode.LAST_WRITER_WINS);
        assertThat(collectionSettings.conflictResolutionPolicy().conflictResolutionPath()).isEqualTo("/_ts");

        // Tests the following scenarios
        // 1. LWW with valid path
        // 2. LWW with null path, should default to _ts
        // 3. LWW with empty path, should default to _ts
        testConflictResolutionPolicyRequiringPath(ConflictResolutionMode.LAST_WRITER_WINS,
                new String[] { "/a", null, "" }, new String[] { "/a", "/_ts", "/_ts" });

        // LWW invalid path
        collectionSettings.conflictResolutionPolicy(ConflictResolutionPolicy.createLastWriterWinsPolicy("/a/b"));

        try {
            collectionSettings = collection.replace(collectionSettings, null).block().properties();
            fail("Expected exception on invalid path.");
        } catch (Exception e) {

            // when (e.StatusCode == HttpStatusCode.BadRequest)
            CosmosClientException dce = Utils.as(e.getCause(), CosmosClientException.class);
            if (dce != null && dce.statusCode() == 400) {
                assertThat(dce.getMessage()).contains("Invalid path '\\/a\\/b' for last writer wins conflict resolution");
            } else {
                throw e;
            }
        }

        // LWW invalid path

        collectionSettings.conflictResolutionPolicy(ConflictResolutionPolicy.createLastWriterWinsPolicy("someText"));

        try {
            collectionSettings = collection.replace(collectionSettings, null).block().properties();
            fail("Expected exception on invalid path.");
        } catch (Exception e) {
            // when (e.StatusCode == HttpStatusCode.BadRequest)
            CosmosClientException dce = Utils.as(e.getCause(), CosmosClientException.class);
            if (dce != null && dce.statusCode() == 400) {
                assertThat(dce.getMessage()).contains("Invalid path 'someText' for last writer wins conflict resolution");
            } else {
                throw e;
            }
        }

        // Tests the following scenarios
        // 1. CUSTOM with valid sprocLink
        // 2. CUSTOM with null sprocLink, should default to empty string
        // 3. CUSTOM with empty sprocLink, should default to empty string
        testConflictResolutionPolicyRequiringPath(ConflictResolutionMode.CUSTOM,
                new String[] { "randomSprocName", null, "" }, new String[] { "randomSprocName", "", "" });
    }

    private void testConflictResolutionPolicyRequiringPath(ConflictResolutionMode conflictResolutionMode,
            String[] paths, String[] expectedPaths) {
        for (int i = 0; i < paths.length; i++) {            
            CosmosContainerProperties collectionSettings = new CosmosContainerProperties(UUID.randomUUID().toString(), partitionKeyDef);
            
            if (conflictResolutionMode == ConflictResolutionMode.LAST_WRITER_WINS) {
                collectionSettings.conflictResolutionPolicy(ConflictResolutionPolicy.createLastWriterWinsPolicy(paths[i]));
            } else {
                collectionSettings.conflictResolutionPolicy(ConflictResolutionPolicy.createCustomPolicy(paths[i]));
            }
            collectionSettings = database.createContainer(collectionSettings, new CosmosContainerRequestOptions()).block().properties();
            assertThat(collectionSettings.conflictResolutionPolicy().mode()).isEqualTo(conflictResolutionMode);
            
            if (conflictResolutionMode == ConflictResolutionMode.LAST_WRITER_WINS) {
                assertThat(collectionSettings.conflictResolutionPolicy().conflictResolutionPath()).isEqualTo(expectedPaths[i]);
            } else {
                assertThat(collectionSettings.conflictResolutionPolicy().conflictResolutionProcedure()).isEqualTo(expectedPaths[i]);
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
        collection.conflictResolutionPolicy(policy);

        Mono<CosmosContainerResponse> createObservable = database.createContainer(
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

        // LWW without path specified, should default to _ts
        ConflictResolutionPolicy policy = BridgeUtils.createConflictResolutionPolicy();
        BridgeUtils.setMode(policy, ConflictResolutionMode.CUSTOM);
        BridgeUtils.setPath(policy,"/mypath");
        collection.conflictResolutionPolicy(policy);

        Mono<CosmosContainerResponse> createObservable = database.createContainer(
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

        client = clientBuilder().build();
        database = createDatabase(client, databaseId);
        partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<String>();
        paths.add("/mypk");
        partitionKeyDef.paths(paths);
    }

    @AfterClass(groups = {"multi-master"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(database);
        safeClose(client);
    }
}
