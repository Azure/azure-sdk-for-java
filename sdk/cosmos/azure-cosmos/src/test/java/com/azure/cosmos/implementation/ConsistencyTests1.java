// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.GatewayConnectionConfig;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.models.PartitionKind;
import org.testng.SkipException;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ConsistencyTests1 extends ConsistencyTestsBase {

    @Test(groups = {"direct"}, timeOut = CONSISTENCY_TEST_TIMEOUT)
    public void validateStrongConsistencyOnSyncReplication() throws Exception {
        if (!TestConfigurations.CONSISTENCY.equalsIgnoreCase(ConsistencyLevel.STRONG.toString())) {
            throw new SkipException("Endpoint does not have strong consistency");
        }

        ConnectionPolicy connectionPolicy = new ConnectionPolicy(GatewayConnectionConfig.getDefaultConfig());
        this.writeClient =
                (RxDocumentClientImpl) new AsyncDocumentClient.Builder()
                        .withServiceEndpoint(TestConfigurations.HOST)
                        .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                        .withConnectionPolicy(connectionPolicy)
                        .withConsistencyLevel(ConsistencyLevel.STRONG)
                        .withContentResponseOnWriteEnabled(true)
                        .withClientTelemetryConfig(ClientTelemetryConfig.getDefaultConfig())
                        .build();

        this.readClient =
                (RxDocumentClientImpl) new AsyncDocumentClient.Builder()
                        .withServiceEndpoint(TestConfigurations.HOST)
                        .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                        .withConnectionPolicy(connectionPolicy)
                        .withConsistencyLevel(ConsistencyLevel.STRONG)
                        .withContentResponseOnWriteEnabled(true)
                        .withClientTelemetryConfig(ClientTelemetryConfig.getDefaultConfig())
                        .build();
        User userDefinition = getUserDefinition();
        userDefinition.setId(userDefinition.getId() + "validateStrongConsistencyOnSyncReplication");
        User user = safeCreateUser(this.initClient, createdDatabase.getId(), userDefinition);
        validateStrongConsistency(user);
    }


    @Test(groups = {"direct"}, timeOut = CONSISTENCY_TEST_TIMEOUT, enabled = false)
    public void validateConsistentLSNForDirectTCPClient() {
        //TODO Need to test with TCP protocol
        // https://msdata.visualstudio.com/CosmosDB/_workitems/edit/355057
        ConnectionPolicy connectionPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());
        this.writeClient =
                (RxDocumentClientImpl) new AsyncDocumentClient.Builder()
                        .withServiceEndpoint(TestConfigurations.HOST)
                        .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                        .withConnectionPolicy(connectionPolicy)
                        .withContentResponseOnWriteEnabled(true)
                        .withClientTelemetryConfig(ClientTelemetryConfig.getDefaultConfig())
                        .build();

        this.readClient =
                (RxDocumentClientImpl) new AsyncDocumentClient.Builder()
                        .withServiceEndpoint(TestConfigurations.HOST)
                        .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                        .withConnectionPolicy(connectionPolicy)
                        .withContentResponseOnWriteEnabled(true)
                        .withClientTelemetryConfig(ClientTelemetryConfig.getDefaultConfig())
                        .build();
        validateConsistentLSN();
    }

    @Test(groups = {"direct"}, timeOut = CONSISTENCY_TEST_TIMEOUT)
    public void validateConsistentLSNForDirectHttpsClient() {
        ConnectionPolicy connectionPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());
        this.writeClient =
                (RxDocumentClientImpl) new AsyncDocumentClient.Builder()
                        .withServiceEndpoint(TestConfigurations.HOST)
                        .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                        .withConnectionPolicy(connectionPolicy)
                        .withContentResponseOnWriteEnabled(true)
                        .withClientTelemetryConfig(ClientTelemetryConfig.getDefaultConfig())
                        .build();

        this.readClient =
                (RxDocumentClientImpl) new AsyncDocumentClient.Builder()
                        .withServiceEndpoint(TestConfigurations.HOST)
                        .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                        .withConnectionPolicy(connectionPolicy)
                        .withContentResponseOnWriteEnabled(true)
                        .withClientTelemetryConfig(ClientTelemetryConfig.getDefaultConfig())
                        .build();
        validateConsistentLSN();
    }

    @Test(groups = {"direct"}, timeOut = CONSISTENCY_TEST_TIMEOUT, enabled = false)
    public void validateConsistentLSNAndQuorumAckedLSNForDirectTCPClient() {
        //TODO Need to test with TCP protocol
        //https://msdata.visualstudio.com/CosmosDB/_workitems/edit/355057
        ConnectionPolicy connectionPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());
        this.writeClient =
                (RxDocumentClientImpl) new AsyncDocumentClient.Builder()
                        .withServiceEndpoint(TestConfigurations.HOST)
                        .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                        .withConnectionPolicy(connectionPolicy)
                        .withContentResponseOnWriteEnabled(true)
                        .withClientTelemetryConfig(ClientTelemetryConfig.getDefaultConfig())
                        .build();

        this.readClient =
                (RxDocumentClientImpl) new AsyncDocumentClient.Builder()
                        .withServiceEndpoint(TestConfigurations.HOST)
                        .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                        .withConnectionPolicy(connectionPolicy)
                        .withContentResponseOnWriteEnabled(true)
                        .withClientTelemetryConfig(ClientTelemetryConfig.getDefaultConfig())
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
        ConnectionPolicy connectionPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());
        this.writeClient =
                (RxDocumentClientImpl) new AsyncDocumentClient.Builder()
                        .withServiceEndpoint(TestConfigurations.HOST)
                        .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                        .withConnectionPolicy(connectionPolicy)
                        .withContentResponseOnWriteEnabled(true)
                        .withClientTelemetryConfig(ClientTelemetryConfig.getDefaultConfig())
                        .build();

        this.readClient =
                (RxDocumentClientImpl) new AsyncDocumentClient.Builder()
                        .withServiceEndpoint(TestConfigurations.HOST)
                        .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                        .withConnectionPolicy(connectionPolicy)
                        .withContentResponseOnWriteEnabled(true)
                        .withClientTelemetryConfig(ClientTelemetryConfig.getDefaultConfig())
                        .build();
        validateConsistentLSNAndQuorumAckedLSN();
    }

    // TODO (DANOBLE) test is flaky
    @Test(groups = {"direct"}, timeOut = CONSISTENCY_TEST_TIMEOUT)
    public void validateStrongConsistencyOnAsyncReplicationGW() throws InterruptedException {
        validateStrongConsistencyOnAsyncReplication(true);
    }

    // TODO (DANOBLE) test is flaky
    @Test(groups = {"direct"}, timeOut = CONSISTENCY_TEST_TIMEOUT)
    public void validateStrongConsistencyOnAsyncReplicationDirect() throws InterruptedException {
        validateStrongConsistencyOnAsyncReplication(false);
    }

    // TODO (DANOBLE) test is flaky, fails inconsistently
    @Test(groups = {"direct"}, timeOut = CONSISTENCY_TEST_TIMEOUT)
    public void validateSessionContainerAfterCollectionCreateReplace() {
        //TODO Need to test with TCP protocol
        // https://msdata.visualstudio.com/CosmosDB/_workitems/edit/355057
        //validateSessionContainerAfterCollectionCreateReplace(false, Protocol.TCP);
        validateSessionContainerAfterCollectionCreateReplace(false);
        validateSessionContainerAfterCollectionCreateReplace(true);
    }

    // TODO (DANOBLE) test is flaky
    @Test(groups = {"direct"}, timeOut = CONSISTENCY_TEST_TIMEOUT)
    public void validateConsistentPrefixOnSyncReplication() throws InterruptedException {
        if (!(TestConfigurations.CONSISTENCY.equalsIgnoreCase(ConsistencyLevel.STRONG.toString()) || TestConfigurations.CONSISTENCY.equalsIgnoreCase(ConsistencyLevel.BOUNDED_STALENESS.toString()))) {
            throw new SkipException("Endpoint does not have strong consistency");
        }

        ConnectionPolicy connectionPolicy = new ConnectionPolicy(GatewayConnectionConfig.getDefaultConfig());
        this.writeClient =
                (RxDocumentClientImpl) new AsyncDocumentClient.Builder()
                        .withServiceEndpoint(TestConfigurations.HOST)
                        .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                        .withConnectionPolicy(connectionPolicy)
                        .withConsistencyLevel(ConsistencyLevel.BOUNDED_STALENESS)
                        .withContentResponseOnWriteEnabled(true)
                        .withClientTelemetryConfig(ClientTelemetryConfig.getDefaultConfig())
                        .build();

        this.readClient =
                (RxDocumentClientImpl) new AsyncDocumentClient.Builder()
                        .withServiceEndpoint(TestConfigurations.HOST)
                        .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                        .withConnectionPolicy(connectionPolicy)
                        .withConsistencyLevel(ConsistencyLevel.BOUNDED_STALENESS)
                        .withContentResponseOnWriteEnabled(true)
                        .withClientTelemetryConfig(ClientTelemetryConfig.getDefaultConfig())
                        .build();
        User user = safeCreateUser(this.initClient, createdDatabase.getId(), getUserDefinition());
        boolean readLagging = validateConsistentPrefix(user);
        assertThat(readLagging).isFalse();
    }

    @Test(groups = {"direct"}, timeOut = CONSISTENCY_TEST_TIMEOUT)
    public void validateConsistentPrefixOnAsyncReplication() throws InterruptedException {
        if (!(TestConfigurations.CONSISTENCY.equalsIgnoreCase(ConsistencyLevel.STRONG.toString()) || TestConfigurations.CONSISTENCY.equalsIgnoreCase(ConsistencyLevel.BOUNDED_STALENESS.toString()))) {
            throw new SkipException("Endpoint does not have strong consistency");
        }

        ConnectionPolicy connectionPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());
        this.writeClient =
                (RxDocumentClientImpl) new AsyncDocumentClient.Builder()
                        .withServiceEndpoint(TestConfigurations.HOST)
                        .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                        .withConnectionPolicy(connectionPolicy)
                        .withConsistencyLevel(ConsistencyLevel.BOUNDED_STALENESS)
                        .withContentResponseOnWriteEnabled(true)
                        .withClientTelemetryConfig(ClientTelemetryConfig.getDefaultConfig())
                        .build();

        this.readClient =
                (RxDocumentClientImpl) new AsyncDocumentClient.Builder()
                        .withServiceEndpoint(TestConfigurations.HOST)
                        .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                        .withConnectionPolicy(connectionPolicy)
                        .withConsistencyLevel(ConsistencyLevel.BOUNDED_STALENESS)
                        .withContentResponseOnWriteEnabled(true)
                        .withClientTelemetryConfig(ClientTelemetryConfig.getDefaultConfig())
                        .build();
        Document documentDefinition = getDocumentDefinition();
        Document document = createDocument(this.initClient, createdDatabase.getId(), createdCollection.getId(), documentDefinition);
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

        ConnectionPolicy connectionPolicy;
        if (useGateway) {
            connectionPolicy = new ConnectionPolicy(GatewayConnectionConfig.getDefaultConfig());
        } else {
            connectionPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());
        }
        AsyncDocumentClient client =
                new AsyncDocumentClient.Builder()
                        .withServiceEndpoint(TestConfigurations.HOST)
                        .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                        .withConnectionPolicy(connectionPolicy)
                        .withConsistencyLevel(ConsistencyLevel.SESSION)
                        .withContentResponseOnWriteEnabled(true)
                        .withClientTelemetryConfig(ClientTelemetryConfig.getDefaultConfig())
                        .build();
        try {
            DocumentCollection documentCollection = new DocumentCollection();
            documentCollection.setId(UUID.randomUUID().toString());
            PartitionKeyDefinition partitionKeyDefinition = new PartitionKeyDefinition();
            partitionKeyDefinition.setKind(PartitionKind.HASH);
            ArrayList<String> paths = new ArrayList<String>();
            paths.add("/id");
            partitionKeyDefinition.setPaths(paths);
            documentCollection.setPartitionKey(partitionKeyDefinition);

            DocumentCollection collection = client.createCollection(createdDatabase.getSelfLink(), documentCollection
                    , null).block().getResource();
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.setPartitionKey(new PartitionKey("1"));

            Document documentDefinition = new Document();
            documentDefinition.setId("1");
            Document document = client.createDocument(collection.getSelfLink(), documentDefinition, requestOptions, false).block().getResource();

            Mono<ResourceResponse<Document>> deleteObservable = client.deleteDocument(document.getSelfLink(), requestOptions);
            ResourceResponseValidator<Document> validator = new ResourceResponseValidator.Builder<Document>()
                    .nullResource().build();
            validateSuccess(deleteObservable, validator);
            Mono<ResourceResponse<Document>> readObservable = client.readDocument(document.getSelfLink(), requestOptions);
            FailureValidator notFoundValidator = new FailureValidator.Builder().resourceNotFound().unknownSubStatusCode().build();
            validateFailure(readObservable, notFoundValidator);

        } finally {
            safeClose(client);
        }
    }

    private static User getUserDefinition() {
        User user = new User();
        user.setId(USER_NAME);
        return user;
    }
}
