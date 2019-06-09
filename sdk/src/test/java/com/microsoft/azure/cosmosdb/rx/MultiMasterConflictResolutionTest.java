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

import com.microsoft.azure.cosmos.CosmosClient;
import com.microsoft.azure.cosmos.CosmosClientBuilder;
import com.microsoft.azure.cosmos.CosmosContainer;
import com.microsoft.azure.cosmos.CosmosContainerRequestOptions;
import com.microsoft.azure.cosmos.CosmosContainerResponse;
import com.microsoft.azure.cosmos.CosmosContainerSettings;
import com.microsoft.azure.cosmos.CosmosDatabase;
import com.microsoft.azure.cosmos.CosmosDatabaseForTest;
import com.microsoft.azure.cosmosdb.BridgeUtils;
import com.microsoft.azure.cosmosdb.ConflictResolutionMode;
import com.microsoft.azure.cosmosdb.ConflictResolutionPolicy;
import com.microsoft.azure.cosmosdb.DocumentClientException;
import com.microsoft.azure.cosmosdb.PartitionKeyDefinition;

import reactor.core.publisher.Mono;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

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
        this.clientBuilder = clientBuilder;
    }

    @Test(groups = "multi-master", timeOut = 10 * TIMEOUT)
    public void conflictResolutionPolicyCRUD() {

        // default last writer wins, path _ts
        CosmosContainerSettings collectionSettings = new CosmosContainerSettings(UUID.randomUUID().toString(), partitionKeyDef);
        CosmosContainer collection = database.createContainer(collectionSettings, new CosmosContainerRequestOptions()).block().getContainer();
        collectionSettings = collection.read().block().getCosmosContainerSettings();

        assertThat(collectionSettings.getConflictResolutionPolicy().getConflictResolutionMode()).isEqualTo(ConflictResolutionMode.LastWriterWins);

        // LWW without path specified, should default to _ts
        collectionSettings.setConflictResolutionPolicy(ConflictResolutionPolicy.createLastWriterWinsPolicy());
        collectionSettings = collection.replace(collectionSettings, null).block().getCosmosContainerSettings();

        assertThat(collectionSettings.getConflictResolutionPolicy().getConflictResolutionMode()).isEqualTo(ConflictResolutionMode.LastWriterWins);
        assertThat(collectionSettings.getConflictResolutionPolicy().getConflictResolutionPath()).isEqualTo("/_ts");

        // Tests the following scenarios
        // 1. LWW with valid path
        // 2. LWW with null path, should default to _ts
        // 3. LWW with empty path, should default to _ts
        testConflictResolutionPolicyRequiringPath(ConflictResolutionMode.LastWriterWins,
                new String[] { "/a", null, "" }, new String[] { "/a", "/_ts", "/_ts" });

        // LWW invalid path
        collectionSettings.setConflictResolutionPolicy(ConflictResolutionPolicy.createLastWriterWinsPolicy("/a/b"));

        try {
            collectionSettings = collection.replace(collectionSettings, null).block().getCosmosContainerSettings();
            fail("Expected exception on invalid path.");
        } catch (Exception e) {

            // when (e.StatusCode == HttpStatusCode.BadRequest)
            DocumentClientException dce = com.microsoft.azure.cosmosdb.rx.internal.Utils.as(e.getCause(), DocumentClientException.class);
            if (dce != null && dce.getStatusCode() == 400) {
                assertThat(dce.getMessage()).contains("Invalid path '\\/a\\/b' for last writer wins conflict resolution");
            } else {
                throw e;
            }
        }

        // LWW invalid path

        collectionSettings.setConflictResolutionPolicy(ConflictResolutionPolicy.createLastWriterWinsPolicy("someText"));

        try {
            collectionSettings = collection.replace(collectionSettings, null).block().getCosmosContainerSettings();
            fail("Expected exception on invalid path.");
        } catch (Exception e) {
            // when (e.StatusCode == HttpStatusCode.BadRequest)
            DocumentClientException dce = com.microsoft.azure.cosmosdb.rx.internal.Utils.as(e.getCause(), DocumentClientException.class);
            if (dce != null && dce.getStatusCode() == 400) {
                assertThat(dce.getMessage()).contains("Invalid path 'someText' for last writer wins conflict resolution");
            } else {
                throw e;
            }
        }

        // Tests the following scenarios
        // 1. Custom with valid sprocLink
        // 2. Custom with null sprocLink, should default to empty string
        // 3. Custom with empty sprocLink, should default to empty string
        testConflictResolutionPolicyRequiringPath(ConflictResolutionMode.Custom,
                new String[] { "randomSprocName", null, "" }, new String[] { "randomSprocName", "", "" });
    }

    private void testConflictResolutionPolicyRequiringPath(ConflictResolutionMode conflictResolutionMode,
            String[] paths, String[] expectedPaths) {
        for (int i = 0; i < paths.length; i++) {            
            CosmosContainerSettings collectionSettings = new CosmosContainerSettings(UUID.randomUUID().toString(), partitionKeyDef);
            
            if (conflictResolutionMode == ConflictResolutionMode.LastWriterWins) {
                collectionSettings.setConflictResolutionPolicy(ConflictResolutionPolicy.createLastWriterWinsPolicy(paths[i]));
            } else {
                collectionSettings.setConflictResolutionPolicy(ConflictResolutionPolicy.createCustomPolicy(paths[i]));
            }
            collectionSettings = database.createContainer(collectionSettings, new CosmosContainerRequestOptions()).block().getCosmosContainerSettings();
            assertThat(collectionSettings.getConflictResolutionPolicy().getConflictResolutionMode()).isEqualTo(conflictResolutionMode);
            
            if (conflictResolutionMode == ConflictResolutionMode.LastWriterWins) {
                assertThat(collectionSettings.getConflictResolutionPolicy().getConflictResolutionPath()).isEqualTo(expectedPaths[i]);
            } else {
                assertThat(collectionSettings.getConflictResolutionPolicy().getConflictResolutionProcedure()).isEqualTo(expectedPaths[i]);
            }
        }
    }
    
    @Test(groups = "multi-master", timeOut = TIMEOUT)
    public void invalidConflictResolutionPolicy_LastWriterWinsWithStoredProc() throws Exception {
        CosmosContainerSettings collection = new CosmosContainerSettings(UUID.randomUUID().toString(), partitionKeyDef);

        // LWW without path specified, should default to _ts
        ConflictResolutionPolicy policy = BridgeUtils.createConflictResolutionPolicy();
        BridgeUtils.setMode(policy, ConflictResolutionMode.LastWriterWins);
        BridgeUtils.setStoredProc(policy,"randomSprocName");
        collection.setConflictResolutionPolicy(policy);

        Mono<CosmosContainerResponse> createObservable = database.createContainer(
                collection,
                new CosmosContainerRequestOptions());

        FailureValidator validator = new FailureValidator.Builder()
                .instanceOf(DocumentClientException.class)
                .statusCode(400)
                .errorMessageContains("LastWriterWins conflict resolution mode should not have conflict resolution procedure set.")
                .build();
        validateFailure(createObservable, validator);
    }

    @Test(groups = "multi-master", timeOut = TIMEOUT)
    public void invalidConflictResolutionPolicy_CustomWithPath() throws Exception {
        CosmosContainerSettings collection = new CosmosContainerSettings(UUID.randomUUID().toString(), partitionKeyDef);

        // LWW without path specified, should default to _ts
        ConflictResolutionPolicy policy = BridgeUtils.createConflictResolutionPolicy();
        BridgeUtils.setMode(policy, ConflictResolutionMode.Custom);
        BridgeUtils.setPath(policy,"/mypath");
        collection.setConflictResolutionPolicy(policy);

        Mono<CosmosContainerResponse> createObservable = database.createContainer(
                collection,
                new CosmosContainerRequestOptions());

        FailureValidator validator = new FailureValidator.Builder()
                .instanceOf(DocumentClientException.class)
                .statusCode(400)
                .errorMessageContains("Custom conflict resolution mode should not have conflict resolution path set.")
                .build();
        validateFailure(createObservable, validator);
    }

    @BeforeClass(groups = {"multi-master"}, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        // set up the client

        client = clientBuilder.build();
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
