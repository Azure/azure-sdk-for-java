// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.GatewayConnectionConfig;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.implementation.directconnectivity.WFConstants;
import com.azure.cosmos.implementation.routing.PartitionKeyInternal;
import org.apache.commons.lang3.Range;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ConsistencyTests2 extends ConsistencyTestsBase {

    @Test(groups = {"direct"}, timeOut = CONSISTENCY_TEST_TIMEOUT)
    public void validateReadSessionOnAsyncReplication() throws InterruptedException {
        ConnectionPolicy connectionPolicy = new ConnectionPolicy(GatewayConnectionConfig.getDefaultConfig());
        this.writeClient = (RxDocumentClientImpl) new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.SESSION).withContentResponseOnWriteEnabled(true).build();

        this.readClient = (RxDocumentClientImpl) new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.SESSION).withContentResponseOnWriteEnabled(true).build();

        Document document = this.initClient.createDocument(createdCollection.getSelfLink(), getDocumentDefinition(),
                                                           null, false).block().getResource();
        Thread.sleep(5000);//WaitForServerReplication
        boolean readLagging = this.validateReadSession(document);
        //assertThat(readLagging).isTrue(); //Will fail if batch repl is turned off
    }

    @Test(groups = {"direct"}, timeOut = CONSISTENCY_TEST_TIMEOUT)
    public void validateWriteSessionOnAsyncReplication() throws InterruptedException {
        ConnectionPolicy connectionPolicy = new ConnectionPolicy(GatewayConnectionConfig.getDefaultConfig());
        this.writeClient = (RxDocumentClientImpl) new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.SESSION).withContentResponseOnWriteEnabled(true).build();

        this.readClient = (RxDocumentClientImpl) new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.SESSION).withContentResponseOnWriteEnabled(true).build();

        Document document = this.initClient.createDocument(createdCollection.getSelfLink(), getDocumentDefinition(),
                                                           null, false).block().getResource();
        Thread.sleep(5000);//WaitForServerReplication
        boolean readLagging = this.validateWriteSession(document);
        //assertThat(readLagging).isTrue(); //Will fail if batch repl is turned off
    }

    @Test(groups = {"direct"}, timeOut = CONSISTENCY_TEST_TIMEOUT, enabled = false)
    public void validateEventualConsistencyOnAsyncReplicationDirect() {
        //TODO this need to complete once we implement emulator container in java, and the we can do operation
        // like pause, resume, stop, recycle on it needed for this test.
        // https://msdata.visualstudio.com/CosmosDB/_workitems/edit/355053
    }

    @Test(groups = {"direct"}, timeOut = CONSISTENCY_TEST_TIMEOUT, enabled = false)
    public void validateEventualConsistencyOnAsyncReplicationGateway() {
        //TODO this need to complete once we implement emulator container in java, and the we can do operation
        // like pause, resume, stop, recycle on it needed for this test.
        // https://msdata.visualstudio.com/CosmosDB/_workitems/edit/355053
    }

    @Test(groups = {"direct"}, timeOut = CONSISTENCY_TEST_TIMEOUT)
    public void validateSessionContainerAfterCollectionDeletion() throws Exception {
        //TODO Need to test with TCP protocol
        // https://msdata.visualstudio.com/CosmosDB/_workitems/edit/355057
        // Verify the collection deletion will trigger the session token clean up (even for different client)
        //this.ValidateSessionContainerAfterCollectionDeletion(true, Protocol.TCP);
        this.validateSessionContainerAfterCollectionDeletion(true);
        this.validateSessionContainerAfterCollectionDeletion(false);
    }

    @Test(groups = {"direct"}, timeOut = CONSISTENCY_TEST_TIMEOUT, enabled = false)
    public void validateReadDistributionForSessionReads() {
        // .NET uses lock method which is eventfully using LastReadAddress only for the test case to pass, we are not implementing this in java
    }

    @Test(groups = {"direct"}, timeOut = CONSISTENCY_TEST_TIMEOUT)
    public void validateSessionTokenWithPreConditionFailure() throws Exception {
        //TODO Need to test with TCP protocol
        // https://msdata.visualstudio.com/CosmosDB/_workitems/edit/355057
        //this.validateSessionTokenWithPreConditionFailure(false, Protocol.TCP);
        this.validateSessionTokenWithPreConditionFailure(false);
        this.validateSessionTokenWithPreConditionFailure(true);
    }

    @Test(groups = {"direct"}, timeOut = CONSISTENCY_TEST_TIMEOUT)
    public void validateSessionTokenWithDocumentNotFound() throws Exception {
        //TODO Need to test with TCP protocol
        // https://msdata.visualstudio.com/CosmosDB/_workitems/edit/355057
        //this.validateSessionTokenWithDocumentNotFoundException(false, Protocol.TCP);
        this.validateSessionTokenWithDocumentNotFoundException(false);
        this.validateSessionTokenWithDocumentNotFoundException(true);
    }

    @Test(groups = {"direct"}, timeOut = CONSISTENCY_TEST_TIMEOUT)
    public void validateSessionTokenWithExpectedException() throws Exception {
        //TODO Need to test with TCP protocol
        // https://msdata.visualstudio.com/CosmosDB/_workitems/edit/355057
        //this.validateSessionTokenWithExpectedException(false, Protocol.TCP);
        this.validateSessionTokenWithExpectedException(false);
        this.validateSessionTokenWithExpectedException(true);
    }

    @Test(groups = {"direct"}, timeOut = CONSISTENCY_TEST_TIMEOUT)
    public void validateSessionTokenWithConflictException() {
        //TODO Need to test with TCP protocol
        // https://msdata.visualstudio.com/CosmosDB/_workitems/edit/355057
        //this.validateSessionTokenWithConflictException(false, Protocol.TCP);
        this.validateSessionTokenWithConflictException(false);
        this.validateSessionTokenWithConflictException(true);
    }

    @Test(groups = {"direct"}, timeOut = CONSISTENCY_TEST_TIMEOUT)
    public void validateSessionTokenMultiPartitionCollection() throws Exception {
        //TODO Need to test with TCP protocol
        // https://msdata.visualstudio.com/CosmosDB/_workitems/edit/355057
        //this.validateSessionTokenMultiPartitionCollection(false, Protocol.TCP);
        this.validateSessionTokenMultiPartitionCollection(false);
        this.validateSessionTokenMultiPartitionCollection(true);
    }

    @Test(groups = {"direct"}, timeOut = CONSISTENCY_TEST_TIMEOUT)
    public void validateSessionTokenFromCollectionReplaceIsServerToken() {
        //TODO Need to test with TCP protocol
        // https://msdata.visualstudio.com/CosmosDB/_workitems/edit/355057
        //this.validateSessionTokenFromCollectionReplaceIsServerToken(false, Protocol.TCP);
        this.validateSessionTokenFromCollectionReplaceIsServerToken(false);
        this.validateSessionTokenFromCollectionReplaceIsServerToken(true);
    }

    //TODO ReadFeed is broken, will enable the test case once it get fixed
    //https://msdata.visualstudio.com/CosmosDB/_workitems/edit/358715
    @Test(groups = {"direct"}, enabled = false, timeOut = CONSISTENCY_TEST_TIMEOUT)
    public void validateNoChargeOnFailedSessionRead() throws Exception {
        // DIRECT clients for read and write operations
        ConnectionPolicy connectionPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());
        RxDocumentClientImpl writeClient = (RxDocumentClientImpl) new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.SESSION).withContentResponseOnWriteEnabled(true)
                .build();
        // Client locked to replica for pause/resume
        RxDocumentClientImpl readSecondaryClient = (RxDocumentClientImpl) new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.SESSION).withContentResponseOnWriteEnabled(true)
                .build();
        try {
            // CREATE collection
            DocumentCollection parentResource = writeClient.createCollection(createdDatabase.getSelfLink(),
                                                                             getCollectionDefinition(), null).block().getResource();

            // Document to lock pause/resume clients
            Document documentDefinition = getDocumentDefinition();
            documentDefinition.setId("test" + documentDefinition.getId());
            ResourceResponse<Document> childResource = writeClient.createDocument(parentResource.getSelfLink(), documentDefinition, null, true).block();
            logger.info("Created {} child resource", childResource.getResource().getResourceId());

            String token = childResource.getSessionToken().split(":")[0] + ":" + ConsistencyTestsBase.createSessionToken(SessionTokenHelper.parse(childResource.getSessionToken()), 100000000).convertToString();

            CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();
            cosmosQueryRequestOptions.setPartitionKey(new PartitionKey(PartitionKeyInternal.Empty.toJson()));
            cosmosQueryRequestOptions.setSessionToken(token);
            FailureValidator validator = new FailureValidator.Builder().statusCode(HttpConstants.StatusCodes.NOTFOUND).subStatusCode(HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE).build();
            Flux<FeedResponse<Document>> feedObservable = readSecondaryClient.readDocuments(parentResource.getSelfLink(), cosmosQueryRequestOptions);
            validateQueryFailure(feedObservable, validator);
        } finally {
            safeClose(writeClient);
            safeClose(readSecondaryClient);
        }
    }

    @Test(groups = {"direct"}, enabled = false, timeOut = CONSISTENCY_TEST_TIMEOUT)
    public void validateStrongReadOnOldDocument() {
        //TODO this need to complete once we implement emulator container in java, and the we can do operation
        // like pause, resume, stop, recycle on it needed for this test.
        // https://msdata.visualstudio.com/CosmosDB/_workitems/edit/355053
    }

    // TODO: DANOBLE: Investigate DIRECT TCP performance issue
    // Note that we need multiple CONSISTENCY_TEST_TIMEOUT
    // SEE: https://msdata.visualstudio.com/CosmosDB/_workitems/edit/367028https://msdata.visualstudio.com/CosmosDB/_workitems/edit/367028

    @Test(groups = {"direct"}, timeOut = 8 * CONSISTENCY_TEST_TIMEOUT)
    public void validateSessionTokenAsync() {
        // Validate that document query never fails
        // with NotFoundException
        List<Document> documents = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            Document documentDefinition = getDocumentDefinition();
            BridgeInternal.setProperty(documentDefinition, UUID.randomUUID().toString(), UUID.randomUUID().toString());
            documents.add(documentDefinition);
        }

        ConnectionPolicy connectionPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());
        RxDocumentClientImpl client = (RxDocumentClientImpl) new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.SESSION).withContentResponseOnWriteEnabled(true)
                .build();

        try {
            Document lastDocument = client.createDocument(createdCollection.getSelfLink(), getDocumentDefinition(),
                                                          null, true)
                    .block()
                    .getResource();

            Mono<Void> task1 = ParallelAsync.forEachAsync(Range.between(0, 1000), 5, index -> client.createDocument(createdCollection.getSelfLink(), documents.get(index % documents.size()),
                                  null, true)
                    .block());

            Mono<Void> task2 = ParallelAsync.forEachAsync(Range.between(0, 1000), 5, index -> {
                try {
                    CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();
                    ModelBridgeInternal.setQueryRequestOptionsEmptyPagesAllowed(cosmosQueryRequestOptions, true);
                    FeedResponse<Document> queryResponse = client.queryDocuments(createdCollection.getSelfLink(),
                                                                                 "SELECT * FROM c WHERE c.Id = " +
                                                                                         "'foo'", cosmosQueryRequestOptions)
                            .blockFirst();
                    String lsnHeaderValue = queryResponse.getResponseHeaders().get(WFConstants.BackendHeaders.LSN);
                    long lsn = Long.valueOf(lsnHeaderValue);
                    String sessionTokenHeaderValue = queryResponse.getResponseHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN);
                    ISessionToken sessionToken = SessionTokenHelper.parse(sessionTokenHeaderValue);
                    assertThat(lsn).isEqualTo(sessionToken.getLSN());
                } catch (Exception ex) {
                    CosmosException clientException = (CosmosException) ex.getCause();
                    if (clientException.getStatusCode() != 0) {
                        if (clientException.getStatusCode() == HttpConstants.StatusCodes.REQUEST_TIMEOUT) {
                            // ignore
                        } else if (clientException.getStatusCode() == HttpConstants.StatusCodes.NOTFOUND) {
                            String lsnHeaderValue = clientException.getResponseHeaders().get(WFConstants.BackendHeaders.LSN);
                            long lsn = Long.valueOf(lsnHeaderValue);
                            String sessionTokenHeaderValue = clientException.getResponseHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN);
                            ISessionToken sessionToken = SessionTokenHelper.parse(sessionTokenHeaderValue);
                            assertThat(lsn).isEqualTo(sessionToken.getLSN());
                        } else {
                            throw ex;
                        }
                    } else {
                        throw ex;
                    }
                }
            });
            Mono.whenDelayError(task1, task2).block();
        } finally {
            safeClose(client);
        }
    }

}
