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

import com.microsoft.azure.cosmosdb.BridgeUtils;
import com.microsoft.azure.cosmosdb.ConflictResolutionMode;
import com.microsoft.azure.cosmosdb.ConflictResolutionPolicy;
import com.microsoft.azure.cosmosdb.Database;
import com.microsoft.azure.cosmosdb.DatabaseForTest;
import com.microsoft.azure.cosmosdb.DocumentClientException;
import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.Resource;
import com.microsoft.azure.cosmosdb.ResourceResponse;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import rx.Observable;

import javax.net.ssl.SSLException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

// assumes multi master is enabled in endpoint
public class MultiMasterConflictResolutionTest extends TestSuiteBase {
    private static final int TIMEOUT = 40000;

    private final String databaseId = DatabaseForTest.generateId();

    private AsyncDocumentClient client;
    private Database database;

    @Factory(dataProvider = "clientBuilders")
    public MultiMasterConflictResolutionTest(AsyncDocumentClient.Builder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    @Test(groups = "multi-master", timeOut = TIMEOUT)
    public void conflictResolutionPolicyCRUD() {

        // default last writer wins, path _ts
        DocumentCollection collection = new DocumentCollection();
        collection.setId(UUID.randomUUID().toString());
        collection = getResource(client.createCollection(getDatabaseLink(database), collection, null));

        assertThat(collection.getConflictResolutionPolicy().getConflictResolutionMode()).isEqualTo(ConflictResolutionMode.LastWriterWins);

        // LWW without path specified, should default to _ts
        collection.setConflictResolutionPolicy(ConflictResolutionPolicy.createLastWriterWinsPolicy());
        collection = getResource(client.replaceCollection(collection, null));


        assertThat(collection.getConflictResolutionPolicy().getConflictResolutionMode()).isEqualTo(ConflictResolutionMode.LastWriterWins);
        assertThat(collection.getConflictResolutionPolicy().getConflictResolutionPath()).isEqualTo("/_ts");

        // Tests the following scenarios
        // 1. LWW with valid path
        // 2. LWW with null path, should default to _ts
        // 3. LWW with empty path, should default to _ts
        testConflictResolutionPolicyRequiringPath(ConflictResolutionMode.LastWriterWins,
                new String[] { "/a", null, "" }, new String[] { "/a", "/_ts", "/_ts" });

        // LWW invalid path
        collection.setConflictResolutionPolicy(ConflictResolutionPolicy.createLastWriterWinsPolicy("/a/b"));

        try {
            collection = getResource(client.replaceCollection(collection, null));
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

        collection.setConflictResolutionPolicy(ConflictResolutionPolicy.createLastWriterWinsPolicy("someText"));

        try {
            collection = getResource(client.replaceCollection(collection, null));
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
            DocumentCollection collection = new DocumentCollection();
            collection.setId(UUID.randomUUID().toString());
            
            if (conflictResolutionMode == ConflictResolutionMode.LastWriterWins) {
                collection.setConflictResolutionPolicy(ConflictResolutionPolicy.createLastWriterWinsPolicy(paths[i]));
            } else {
                collection.setConflictResolutionPolicy(ConflictResolutionPolicy.createCustomPolicy(paths[i]));
            }
            collection = getResource(client.createCollection("dbs/" + database.getId(), collection, null));
            assertThat(collection.getConflictResolutionPolicy().getConflictResolutionMode()).isEqualTo(conflictResolutionMode);
            
            if (conflictResolutionMode == ConflictResolutionMode.LastWriterWins) {
                assertThat(collection.getConflictResolutionPolicy().getConflictResolutionPath()).isEqualTo(expectedPaths[i]);
            } else {
                assertThat(collection.getConflictResolutionPolicy().getConflictResolutionProcedure()).isEqualTo(expectedPaths[i]);
            }
        }
    }
    
    @Test(groups = "multi-master", timeOut = TIMEOUT)
    public void invalidConflictResolutionPolicy_LastWriterWinsWithStoredProc() throws Exception {
        DocumentCollection collection = new DocumentCollection();
        collection.setId(UUID.randomUUID().toString());

        // LWW without path specified, should default to _ts
        ConflictResolutionPolicy policy = BridgeUtils.createConflictResolutionPolicy();
        BridgeUtils.setMode(policy, ConflictResolutionMode.LastWriterWins);
        BridgeUtils.setStoredProc(policy,"randomSprocName");
        collection.setConflictResolutionPolicy(policy);

        Observable<ResourceResponse<DocumentCollection>> createObservable = client.createCollection(
                getDatabaseLink(database),
                collection,
                null);

        FailureValidator validator = new FailureValidator.Builder()
                .instanceOf(DocumentClientException.class)
                .statusCode(400)
                .errorMessageContains("LastWriterWins conflict resolution mode should not have conflict resolution procedure set.")
                .build();
        validateFailure(createObservable, validator);
    }

    @Test(groups = "multi-master", timeOut = TIMEOUT)
    public void invalidConflictResolutionPolicy_CustomWithPath() throws Exception {
        DocumentCollection collection = new DocumentCollection();
        collection.setId(UUID.randomUUID().toString());

        // LWW without path specified, should default to _ts
        ConflictResolutionPolicy policy = BridgeUtils.createConflictResolutionPolicy();
        BridgeUtils.setMode(policy, ConflictResolutionMode.Custom);
        BridgeUtils.setPath(policy,"/mypath");
        collection.setConflictResolutionPolicy(policy);

        Observable<ResourceResponse<DocumentCollection>> createObservable = client.createCollection(
                getDatabaseLink(database),
                collection,
                null);

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
    }

    private <T extends Resource> T getResource(Observable<ResourceResponse<T>> obs) {
        return obs.toBlocking().single().getResource();
    }

    @AfterClass(groups = {"multi-master"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(client, database);
        safeClose(client);
    }
}
