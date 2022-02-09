// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.models.ConflictResolutionMode;
import com.azure.cosmos.models.ConflictResolutionPolicy;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerRequestOptions;
import com.azure.cosmos.CosmosDatabaseForTest;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.implementation.FailureValidator;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.models.ModelBridgeUtils;
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
        CosmosContainerProperties containerSettings = new CosmosContainerProperties(UUID.randomUUID().toString(), partitionKeyDef);
        database.createContainer(containerSettings, new CosmosContainerRequestOptions()).block();
        CosmosAsyncContainer container = database.getContainer(containerSettings.getId());

        containerSettings = container.read().block().getProperties();

        assertThat(containerSettings.getConflictResolutionPolicy().getMode()).isEqualTo(ConflictResolutionMode.LAST_WRITER_WINS);

        // LWW without getPath specified, should default to _ts
        containerSettings.setConflictResolutionPolicy(ConflictResolutionPolicy.createLastWriterWinsPolicy());
        containerSettings = container.replace(containerSettings, null).block().getProperties();

        assertThat(containerSettings.getConflictResolutionPolicy().getMode()).isEqualTo(ConflictResolutionMode.LAST_WRITER_WINS);
        assertThat(containerSettings.getConflictResolutionPolicy().getConflictResolutionPath()).isEqualTo("/_ts");

        // Tests the following scenarios
        // 1. LWW with valid path
        // 2. LWW with null path, should default to _ts
        // 3. LWW with empty path, should default to _ts
        testConflictResolutionPolicyRequiringPath(ConflictResolutionMode.LAST_WRITER_WINS,
                new String[] { "/a", null, "" }, new String[] { "/a", "/_ts", "/_ts" });

        // LWW invalid getPath
        containerSettings.setConflictResolutionPolicy(ConflictResolutionPolicy.createLastWriterWinsPolicy("/a/b"));

        try {
            containerSettings = container.replace(containerSettings, null).block().getProperties();
            fail("Expected exception on invalid getPath.");
        } catch (Exception e) {

            // when (e.StatusCode == HttpStatusCode.BadRequest)
            CosmosException dce = Utils.as(e, CosmosException.class);
            if (dce != null && dce.getStatusCode() == 400) {
                assertThat(dce.getMessage()).contains("Invalid path '\\\\/a\\\\/b' for last writer wins conflict resolution");
            } else {
                throw e;
            }
        }

        // LWW invalid getPath

        containerSettings.setConflictResolutionPolicy(ConflictResolutionPolicy.createLastWriterWinsPolicy("someText"));

        try {
            containerSettings = container.replace(containerSettings, null).block().getProperties();
            fail("Expected exception on invalid path.");
        } catch (Exception e) {
            // when (e.StatusCode == HttpStatusCode.BadRequest)
            CosmosException dce = Utils.as(e, CosmosException.class);
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
        ConflictResolutionPolicy policy = ModelBridgeUtils.createConflictResolutionPolicy();
        ModelBridgeUtils.setMode(policy, ConflictResolutionMode.LAST_WRITER_WINS);
        ModelBridgeUtils.setStoredProc(policy,"randomSprocName");
        collection.setConflictResolutionPolicy(policy);

        Mono<CosmosContainerResponse> createObservable = database.createContainer(
                collection,
                new CosmosContainerRequestOptions());

        FailureValidator validator = new FailureValidator.Builder()
                .instanceOf(CosmosException.class)
                .statusCode(400)
                .errorMessageContains("LastWriterWins conflict resolution mode should not have conflict resolution procedure set.")
                .build();
        validateFailure(createObservable, validator);
    }

    @Test(groups = "multi-master", timeOut = TIMEOUT)
    public void invalidConflictResolutionPolicy_CustomWithPath() throws Exception {
        CosmosContainerProperties collection = new CosmosContainerProperties(UUID.randomUUID().toString(), partitionKeyDef);

        // LWW without getPath specified, should default to _ts
        ConflictResolutionPolicy policy = ModelBridgeUtils.createConflictResolutionPolicy();
        ModelBridgeUtils.setMode(policy, ConflictResolutionMode.CUSTOM);
        ModelBridgeUtils.setPath(policy,"/mypath");
        collection.setConflictResolutionPolicy(policy);

        Mono<CosmosContainerResponse> createObservable = database.createContainer(
                collection,
                new CosmosContainerRequestOptions());

        FailureValidator validator = new FailureValidator.Builder()
                .instanceOf(CosmosException.class)
                .statusCode(400)
                .errorMessageContains("Custom conflict resolution mode should not have conflict resolution path set.")
                .build();
        validateFailure(createObservable, validator);
    }

    @Test(groups = "multi-master", timeOut = 10 * TIMEOUT)
    public void updateConflictResolutionWithException() {
        CosmosContainerProperties containerSettings = new CosmosContainerProperties(UUID.randomUUID().toString(), partitionKeyDef);
        database.createContainer(containerSettings, new CosmosContainerRequestOptions()).block();
        CosmosAsyncContainer container = database.getContainer(containerSettings.getId());
        containerSettings = container.read().block().getProperties();
        assertThat(containerSettings.getConflictResolutionPolicy().getMode()).isEqualTo(ConflictResolutionMode.LAST_WRITER_WINS);

        //Update on resolution property is currently not allowed from BE service
        containerSettings.setConflictResolutionPolicy(ConflictResolutionPolicy.createLastWriterWinsPolicy("/userProvidedField"));
        try {
            container.replace(containerSettings, null).block().getProperties();
            fail("Updating conflict resolution policy should");
        } catch (CosmosException ex) {
            assertThat(ex.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.BADREQUEST);
            assertThat(ex.getMessage()).contains("Updating conflict resolution policy is currently not supported");
        }
    }

    @BeforeClass(groups = {"multi-master"}, timeOut = SETUP_TIMEOUT)
    public void before_MultiMasterConflictResolutionTest() {
        // set up the client

        client = getClientBuilder().buildAsyncClient();
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
