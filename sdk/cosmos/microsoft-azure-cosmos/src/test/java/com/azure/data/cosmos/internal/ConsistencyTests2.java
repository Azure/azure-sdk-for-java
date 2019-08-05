// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal;

import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.ConnectionMode;
import com.azure.data.cosmos.ConnectionPolicy;
import com.azure.data.cosmos.ConsistencyLevel;
import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.PartitionKey;
import com.azure.data.cosmos.internal.directconnectivity.WFConstants;
import com.azure.data.cosmos.internal.routing.PartitionKeyInternal;
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
        ConnectionPolicy connectionPolicy = new ConnectionPolicy();
        connectionPolicy.connectionMode(ConnectionMode.GATEWAY);
        this.writeClient = (RxDocumentClientImpl) new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.SESSION).build();

        this.readClient = (RxDocumentClientImpl) new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.SESSION).build();

        Document document = this.initClient.createDocument(createdCollection.selfLink(), getDocumentDefinition(),
                                                           null, false).blockFirst().getResource();
        Thread.sleep(5000);//WaitForServerReplication
        boolean readLagging = this.validateReadSession(document);
        //assertThat(readLagging).isTrue(); //Will fail if batch repl is turned off
    }

    @Test(groups = {"direct"}, timeOut = CONSISTENCY_TEST_TIMEOUT)
    public void validateWriteSessionOnAsyncReplication() throws InterruptedException {
        ConnectionPolicy connectionPolicy = new ConnectionPolicy();
        connectionPolicy.connectionMode(ConnectionMode.GATEWAY);
        this.writeClient = (RxDocumentClientImpl) new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.SESSION).build();

        this.readClient = (RxDocumentClientImpl) new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.SESSION).build();

        Document document = this.initClient.createDocument(createdCollection.selfLink(), getDocumentDefinition(),
                                                           null, false).blockFirst().getResource();
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
        ConnectionPolicy connectionPolicy = new ConnectionPolicy();
        connectionPolicy.connectionMode(ConnectionMode.DIRECT);
        RxDocumentClientImpl writeClient = (RxDocumentClientImpl) new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.SESSION)
                .build();
        // Client locked to replica for pause/resume
        RxDocumentClientImpl readSecondaryClient = (RxDocumentClientImpl) new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.SESSION)
                .build();
        try {
            // CREATE collection
            DocumentCollection parentResource = writeClient.createCollection(createdDatabase.selfLink(),
                                                                             getCollectionDefinition(), null).blockFirst().getResource();

            // Document to lock pause/resume clients
            Document documentDefinition = getDocumentDefinition();
            documentDefinition.id("test" + documentDefinition.id());
            ResourceResponse<Document> childResource = writeClient.createDocument(parentResource.selfLink(), documentDefinition, null, true).blockFirst();
            logger.info("Created {} child resource", childResource.getResource().resourceId());

            String token = childResource.getSessionToken().split(":")[0] + ":" + this.createSessionToken(SessionTokenHelper.parse(childResource.getSessionToken()), 100000000).convertToString();

            FeedOptions feedOptions = new FeedOptions();
            feedOptions.partitionKey(new PartitionKey(PartitionKeyInternal.Empty.toJson()));
            feedOptions.sessionToken(token);
            FailureValidator validator = new FailureValidator.Builder().statusCode(HttpConstants.StatusCodes.NOTFOUND).subStatusCode(HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE).build();
            Flux<FeedResponse<Document>> feedObservable = readSecondaryClient.readDocuments(parentResource.selfLink(), feedOptions);
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

    @Test(groups = {"direct"}, timeOut = 4 * CONSISTENCY_TEST_TIMEOUT)
    public void validateSessionTokenAsync() {
        // Validate that document query never fails
        // with NotFoundException
        List<Document> documents = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            Document documentDefinition = getDocumentDefinition();
            BridgeInternal.setProperty(documentDefinition, UUID.randomUUID().toString(), UUID.randomUUID().toString());
            documents.add(documentDefinition);
        }

        ConnectionPolicy connectionPolicy = new ConnectionPolicy();
        connectionPolicy.connectionMode(ConnectionMode.DIRECT);
        RxDocumentClientImpl client = (RxDocumentClientImpl) new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.SESSION)
                .build();

        try {
            Document lastDocument = client.createDocument(createdCollection.selfLink(), getDocumentDefinition(),
                                                          null, true)
                    .blockFirst()
                    .getResource();

            Mono<Void> task1 = ParallelAsync.forEachAsync(Range.between(0, 1000), 5, index -> client.createDocument(createdCollection.selfLink(), documents.get(index % documents.size()),
                                  null, true)
                    .blockFirst());

            Mono<Void> task2 = ParallelAsync.forEachAsync(Range.between(0, 1000), 5, index -> {
                try {
                    FeedOptions feedOptions = new FeedOptions();
                    feedOptions.enableCrossPartitionQuery(true);
                    FeedResponse<Document> queryResponse = client.queryDocuments(createdCollection.selfLink(),
                                                                                 "SELECT * FROM c WHERE c.Id = " +
                                                                                         "'foo'", feedOptions)
                            .blockFirst();
                    String lsnHeaderValue = queryResponse.responseHeaders().get(WFConstants.BackendHeaders.LSN);
                    long lsn = Long.valueOf(lsnHeaderValue);
                    String sessionTokenHeaderValue = queryResponse.responseHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN);
                    ISessionToken sessionToken = SessionTokenHelper.parse(sessionTokenHeaderValue);
                    logger.info("SESSION Token = {}, LSN = {}", sessionToken.convertToString(), lsn);
                    assertThat(lsn).isEqualTo(sessionToken.getLSN());
                } catch (Exception ex) {
                    CosmosClientException clientException = (CosmosClientException) ex.getCause();
                    if (clientException.statusCode() != 0) {
                        if (clientException.statusCode() == HttpConstants.StatusCodes.REQUEST_TIMEOUT) {
                            // ignore
                        } else if (clientException.statusCode() == HttpConstants.StatusCodes.NOTFOUND) {
                            String lsnHeaderValue = clientException.responseHeaders().get(WFConstants.BackendHeaders.LSN);
                            long lsn = Long.valueOf(lsnHeaderValue);
                            String sessionTokenHeaderValue = clientException.responseHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN);
                            ISessionToken sessionToken = SessionTokenHelper.parse(sessionTokenHeaderValue);

                            logger.info("SESSION Token = {}, LSN = {}", sessionToken.convertToString(), lsn);
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
