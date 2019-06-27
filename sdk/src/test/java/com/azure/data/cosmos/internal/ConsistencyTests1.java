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

package com.azure.data.cosmos.internal;

import com.azure.data.cosmos.AsyncDocumentClient;
import com.azure.data.cosmos.ConnectionMode;
import com.azure.data.cosmos.ConnectionPolicy;
import com.azure.data.cosmos.ConsistencyLevel;
import com.azure.data.cosmos.Document;
import com.azure.data.cosmos.DocumentCollection;
import com.azure.data.cosmos.PartitionKey;
import com.azure.data.cosmos.PartitionKeyDefinition;
import com.azure.data.cosmos.PartitionKind;
import com.azure.data.cosmos.rx.FailureValidator;
import com.azure.data.cosmos.rx.ResourceResponseValidator;
import com.azure.data.cosmos.rx.TestConfigurations;
import org.testng.SkipException;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ConsistencyTests1 extends ConsistencyTestsBase {


    @Test(groups = {"direct"}, timeOut = CONSISTENCY_TEST_TIMEOUT)
    public void validateStrongConsistencyOnSyncReplication() throws Exception {
        if (!TestConfigurations.CONSISTENCY.equalsIgnoreCase(ConsistencyLevel.STRONG.toString())) {
            throw new SkipException("Endpoint does not have strong consistency");
        }

        ConnectionPolicy connectionPolicy = new ConnectionPolicy();
        connectionPolicy.connectionMode(ConnectionMode.GATEWAY);
        this.writeClient = (RxDocumentClientImpl) new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.STRONG).build();

        this.readClient = (RxDocumentClientImpl) new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.STRONG).build();
        User userDefinition = getUserDefinition();
        userDefinition.id(userDefinition.id() + "validateStrongConsistencyOnSyncReplication");
        User user = safeCreateUser(this.initClient, createdDatabase.id(), userDefinition);
        validateStrongConsistency(user);
    }


    @Test(groups = {"direct"}, timeOut = CONSISTENCY_TEST_TIMEOUT, enabled = false)
    public void validateConsistentLSNForDirectTCPClient() {
        //TODO Need to test with TCP protocol
        // https://msdata.visualstudio.com/CosmosDB/_workitems/edit/355057
        ConnectionPolicy connectionPolicy = new ConnectionPolicy();
        connectionPolicy.connectionMode(ConnectionMode.DIRECT);
        this.writeClient = (RxDocumentClientImpl) new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .build();

        this.readClient = (RxDocumentClientImpl) new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .build();
        validateConsistentLSN();
    }

    @Test(groups = {"direct"}, timeOut = CONSISTENCY_TEST_TIMEOUT)
    public void validateConsistentLSNForDirectHttpsClient() {
        ConnectionPolicy connectionPolicy = new ConnectionPolicy();
        connectionPolicy.connectionMode(ConnectionMode.DIRECT);
        this.writeClient = (RxDocumentClientImpl) new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .build();

        this.readClient = (RxDocumentClientImpl) new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .build();
        validateConsistentLSN();
    }

    @Test(groups = {"direct"}, timeOut = CONSISTENCY_TEST_TIMEOUT, enabled = false)
    public void validateConsistentLSNAndQuorumAckedLSNForDirectTCPClient() {
        //TODO Need to test with TCP protocol
        //https://msdata.visualstudio.com/CosmosDB/_workitems/edit/355057
        ConnectionPolicy connectionPolicy = new ConnectionPolicy();
        connectionPolicy.connectionMode(ConnectionMode.DIRECT);
        this.writeClient = (RxDocumentClientImpl) new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .build();

        this.readClient = (RxDocumentClientImpl) new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .build();
        validateConsistentLSNAndQuorumAckedLSN();
    }

    @Test(groups = {"direct"}, timeOut = CONSISTENCY_TEST_TIMEOUT)
    public void validateStrongDynamicQuorum() {
        if (!TestConfigurations.CONSISTENCY.equalsIgnoreCase(ConsistencyLevel.STRONG.toString())) {
            throw new SkipException("Endpoint does not have strong consistency");
        }

        validateReadQuorum(ConsistencyLevel.STRONG, ResourceType.Document, false);
    }

    @Test(groups = {"direct"}, timeOut = CONSISTENCY_TEST_TIMEOUT)
    public void validateBoundedStalenessDynamicQuorumSyncReplication() {
        if (!(TestConfigurations.CONSISTENCY.equalsIgnoreCase(ConsistencyLevel.STRONG.toString()) || TestConfigurations.CONSISTENCY.equalsIgnoreCase(ConsistencyLevel.BOUNDED_STALENESS.toString()))) {
            throw new SkipException("Endpoint does not have strong consistency");
        }

        validateReadQuorum(ConsistencyLevel.BOUNDED_STALENESS, ResourceType.Document, true);
    }

    @Test(groups = {"direct"}, timeOut = CONSISTENCY_TEST_TIMEOUT)
    public void validateConsistentLSNAndQuorumAckedLSNForDirectHttpsClient() {
        ConnectionPolicy connectionPolicy = new ConnectionPolicy();
        connectionPolicy.connectionMode(ConnectionMode.DIRECT);
        this.writeClient = (RxDocumentClientImpl) new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .build();

        this.readClient = (RxDocumentClientImpl) new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .build();
        validateConsistentLSNAndQuorumAckedLSN();
    }

    @Test(groups = {"direct"}, timeOut = CONSISTENCY_TEST_TIMEOUT)
    public void validateStrongConsistencyOnAsyncReplicationGW() throws InterruptedException {
        validateStrongConsistencyOnAsyncReplication(true);
    }

    @Test(groups = {"direct"}, timeOut = CONSISTENCY_TEST_TIMEOUT)
    public void validateStrongConsistencyOnAsyncReplicationDirect() throws InterruptedException {
        validateStrongConsistencyOnAsyncReplication(false);
    }

    @Test(groups = {"direct"}, timeOut = CONSISTENCY_TEST_TIMEOUT)
    public void validateSessionContainerAfterCollectionCreateReplace() {
        //TODO Need to test with TCP protocol
        // https://msdata.visualstudio.com/CosmosDB/_workitems/edit/355057
        //validateSessionContainerAfterCollectionCreateReplace(false, Protocol.TCP);
        validateSessionContainerAfterCollectionCreateReplace(false);
        validateSessionContainerAfterCollectionCreateReplace(true);
    }

    @Test(groups = {"direct"}, timeOut = CONSISTENCY_TEST_TIMEOUT)
    public void validateConsistentPrefixOnSyncReplication() throws InterruptedException {
        if (!(TestConfigurations.CONSISTENCY.equalsIgnoreCase(ConsistencyLevel.STRONG.toString()) || TestConfigurations.CONSISTENCY.equalsIgnoreCase(ConsistencyLevel.BOUNDED_STALENESS.toString()))) {
            throw new SkipException("Endpoint does not have strong consistency");
        }

        ConnectionPolicy connectionPolicy = new ConnectionPolicy();
        connectionPolicy.connectionMode(ConnectionMode.GATEWAY);
        this.writeClient = (RxDocumentClientImpl) new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.BOUNDED_STALENESS).build();

        this.readClient = (RxDocumentClientImpl) new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.BOUNDED_STALENESS).build();
        User user = safeCreateUser(this.initClient, createdDatabase.id(), getUserDefinition());
        boolean readLagging = validateConsistentPrefix(user);
        assertThat(readLagging).isFalse();
    }

    @Test(groups = {"direct"}, timeOut = CONSISTENCY_TEST_TIMEOUT)
    public void validateConsistentPrefixOnAsyncReplication() throws InterruptedException {
        if (!(TestConfigurations.CONSISTENCY.equalsIgnoreCase(ConsistencyLevel.STRONG.toString()) || TestConfigurations.CONSISTENCY.equalsIgnoreCase(ConsistencyLevel.BOUNDED_STALENESS.toString()))) {
            throw new SkipException("Endpoint does not have strong consistency");
        }

        ConnectionPolicy connectionPolicy = new ConnectionPolicy();
        connectionPolicy.connectionMode(ConnectionMode.DIRECT);
        this.writeClient = (RxDocumentClientImpl) new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.BOUNDED_STALENESS)
                .build();

        this.readClient = (RxDocumentClientImpl) new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.BOUNDED_STALENESS)
                .build();
        Document documentDefinition = getDocumentDefinition();
        Document document = createDocument(this.initClient, createdDatabase.id(), createdCollection.id(), documentDefinition);
        boolean readLagging = validateConsistentPrefix(document);
        //assertThat(readLagging).isTrue(); //Will fail if batch repl is turned off
    }

    @Test(groups = {"direct"}, timeOut = CONSISTENCY_TEST_TIMEOUT, enabled = false)
    public void validateConsistentPrefixWithReplicaRestartWithPause() {
        //TODO this need to complete once we implement emulator container in java, and the we can do operation 
        // like pause, resume, stop, recycle on it needed for this test.
        // https://msdata.visualstudio.com/CosmosDB/_workitems/edit/355053
    }

    @Test(groups = {"direct"}, timeOut = CONSISTENCY_TEST_TIMEOUT, enabled = false)
    public void validateConsistentPrefixWithReplicaRestart() {
        //TODO this need to complete once we implement emulator container in java, and the we can do operation 
        // like pause, resume, stop, recycle on it needed for this test.
        // https://msdata.visualstudio.com/CosmosDB/_workitems/edit/355053
    }

    @Test(groups = {"direct"}, timeOut = CONSISTENCY_TEST_TIMEOUT)
    public void validateSubstatusCodeOnNotFoundExceptionInSessionReadAsync() {
        validateSubstatusCodeOnNotFoundExceptionInSessionReadAsync(false);
        validateSubstatusCodeOnNotFoundExceptionInSessionReadAsync(true);
    }

    @Test(groups = {"direct"}, timeOut = CONSISTENCY_TEST_TIMEOUT, enabled = false)
    public void validateBarrierStrongConsistencyForMasterResources() {
        //TODO this need to complete once we implement emulator container in java, and the we can do operation 
        // like pause, resume, stop, recycle on it needed for this test.
        // https://msdata.visualstudio.com/CosmosDB/_workitems/edit/355053
    }

    private void validateSubstatusCodeOnNotFoundExceptionInSessionReadAsync(boolean useGateway) {

        ConnectionPolicy connectionPolicy = new ConnectionPolicy();
        if (useGateway) {
            connectionPolicy.connectionMode(ConnectionMode.GATEWAY);
        } else {
            connectionPolicy.connectionMode(ConnectionMode.DIRECT);
        }
        AsyncDocumentClient client = new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.SESSION)
                .build();
        try {
            DocumentCollection documentCollection = new DocumentCollection();
            documentCollection.id(UUID.randomUUID().toString());
            PartitionKeyDefinition partitionKeyDefinition = new PartitionKeyDefinition();
            partitionKeyDefinition.kind(PartitionKind.HASH);
            ArrayList<String> paths = new ArrayList<String>();
            paths.add("/id");
            partitionKeyDefinition.paths(paths);
            documentCollection.setPartitionKey(partitionKeyDefinition);

            DocumentCollection collection = client.createCollection(createdDatabase.selfLink(), documentCollection
                    , null).blockFirst().getResource();
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.setPartitionKey(new PartitionKey("1"));

            Document documentDefinition = new Document();
            documentDefinition.id("1");
            Document document = client.createDocument(collection.selfLink(), documentDefinition, requestOptions, false).blockFirst().getResource();

            Flux<ResourceResponse<Document>> deleteObservable = client.deleteDocument(document.selfLink(), requestOptions);
            ResourceResponseValidator<Document> validator = new ResourceResponseValidator.Builder<Document>()
                    .nullResource().build();
            validateSuccess(deleteObservable, validator);
            Flux<ResourceResponse<Document>> readObservable = client.readDocument(document.selfLink(), requestOptions);
            FailureValidator notFoundValidator = new FailureValidator.Builder().resourceNotFound().unknownSubStatusCode().build();
            validateFailure(readObservable, notFoundValidator);

        } finally {
            safeClose(client);
        }
    }

    private static User getUserDefinition() {
        User user = new User();
        user.id(USER_NAME);
        return user;
    }
}