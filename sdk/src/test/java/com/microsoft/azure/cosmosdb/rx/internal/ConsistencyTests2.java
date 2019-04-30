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

package com.microsoft.azure.cosmosdb.rx.internal;

import com.microsoft.azure.cosmosdb.ConnectionMode;
import com.microsoft.azure.cosmosdb.ConnectionPolicy;
import com.microsoft.azure.cosmosdb.ConsistencyLevel;
import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.cosmosdb.DocumentClientException;
import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.FeedOptions;
import com.microsoft.azure.cosmosdb.FeedResponse;
import com.microsoft.azure.cosmosdb.PartitionKey;
import com.microsoft.azure.cosmosdb.ResourceResponse;
import com.microsoft.azure.cosmosdb.internal.HttpConstants;
import com.microsoft.azure.cosmosdb.internal.ISessionToken;
import com.microsoft.azure.cosmosdb.internal.SessionTokenHelper;
import com.microsoft.azure.cosmosdb.internal.directconnectivity.WFConstants;
import com.microsoft.azure.cosmosdb.internal.routing.PartitionKeyInternal;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;
import com.microsoft.azure.cosmosdb.rx.FailureValidator;
import com.microsoft.azure.cosmosdb.rx.TestConfigurations;
import org.apache.commons.lang3.Range;
import org.testng.annotations.Test;
import rx.Completable;
import rx.Observable;
import rx.functions.Action1;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ConsistencyTests2 extends ConsistencyTestsBase {

    @Test(groups = {"direct"}, timeOut = CONSISTENCY_TEST_TIMEOUT)
    public void validateReadSessionOnAsyncReplication() throws InterruptedException {
        ConnectionPolicy connectionPolicy = new ConnectionPolicy();
        connectionPolicy.setConnectionMode(ConnectionMode.Gateway);
        this.writeClient = (RxDocumentClientImpl) new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.Session).build();

        this.readClient = (RxDocumentClientImpl) new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.Session).build();

        Document document = this.initClient.createDocument(createdCollection.getSelfLink(), getDocumentDefinition(), null, false).toBlocking().first().getResource();
        Thread.sleep(5000);//WaitForServerReplication
        boolean readLagging = this.validateReadSession(document);
        //assertThat(readLagging).isTrue(); //Will fail if batch repl is turned off
    }

    @Test(groups = {"direct"}, timeOut = CONSISTENCY_TEST_TIMEOUT)
    public void validateWriteSessionOnAsyncReplication() throws InterruptedException {
        ConnectionPolicy connectionPolicy = new ConnectionPolicy();
        connectionPolicy.setConnectionMode(ConnectionMode.Gateway);
        this.writeClient = (RxDocumentClientImpl) new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.Session).build();

        this.readClient = (RxDocumentClientImpl) new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.Session).build();

        Document document = this.initClient.createDocument(createdCollection.getSelfLink(), getDocumentDefinition(), null, false).toBlocking().first().getResource();
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
        //this.ValidateSessionContainerAfterCollectionDeletion(true, Protocol.Tcp);
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
        //this.validateSessionTokenWithPreConditionFailure(false, Protocol.Tcp);
        this.validateSessionTokenWithPreConditionFailure(false);
        this.validateSessionTokenWithPreConditionFailure(true);
    }

    @Test(groups = {"direct"}, timeOut = CONSISTENCY_TEST_TIMEOUT)
    public void validateSessionTokenWithDocumentNotFound() throws Exception {
        //TODO Need to test with TCP protocol
        // https://msdata.visualstudio.com/CosmosDB/_workitems/edit/355057
        //this.validateSessionTokenWithDocumentNotFoundException(false, Protocol.Tcp);
        this.validateSessionTokenWithDocumentNotFoundException(false);
        this.validateSessionTokenWithDocumentNotFoundException(true);
    }

    @Test(groups = {"direct"}, timeOut = CONSISTENCY_TEST_TIMEOUT)
    public void validateSessionTokenWithExpectedException() throws Exception {
        //TODO Need to test with TCP protocol
        // https://msdata.visualstudio.com/CosmosDB/_workitems/edit/355057
        //this.validateSessionTokenWithExpectedException(false, Protocol.Tcp);
        this.validateSessionTokenWithExpectedException(false);
        this.validateSessionTokenWithExpectedException(true);
    }

    @Test(groups = {"direct"}, timeOut = CONSISTENCY_TEST_TIMEOUT)
    public void validateSessionTokenWithConflictException() {
        //TODO Need to test with TCP protocol
        // https://msdata.visualstudio.com/CosmosDB/_workitems/edit/355057
        //this.validateSessionTokenWithConflictException(false, Protocol.Tcp);
        this.validateSessionTokenWithConflictException(false);
        this.validateSessionTokenWithConflictException(true);
    }

    @Test(groups = {"direct"}, timeOut = CONSISTENCY_TEST_TIMEOUT)
    public void validateSessionTokenMultiPartitionCollection() throws Exception {
        //TODO Need to test with TCP protocol
        // https://msdata.visualstudio.com/CosmosDB/_workitems/edit/355057
        //this.validateSessionTokenMultiPartitionCollection(false, Protocol.Tcp);
        this.validateSessionTokenMultiPartitionCollection(false);
        this.validateSessionTokenMultiPartitionCollection(true);
    }

    @Test(groups = {"direct"}, timeOut = CONSISTENCY_TEST_TIMEOUT)
    public void validateSessionTokenFromCollectionReplaceIsServerToken() {
        //TODO Need to test with TCP protocol
        // https://msdata.visualstudio.com/CosmosDB/_workitems/edit/355057
        //this.validateSessionTokenFromCollectionReplaceIsServerToken(false, Protocol.Tcp);
        this.validateSessionTokenFromCollectionReplaceIsServerToken(false);
        this.validateSessionTokenFromCollectionReplaceIsServerToken(true);
    }

    //TODO ReadFeed is broken, will enable the test case once it get fixed
    //https://msdata.visualstudio.com/CosmosDB/_workitems/edit/358715
    @Test(groups = {"direct"}, enabled = false, timeOut = CONSISTENCY_TEST_TIMEOUT)
    public void validateNoChargeOnFailedSessionRead() throws Exception {
        // Direct clients for read and write operations
        ConnectionPolicy connectionPolicy = new ConnectionPolicy();
        connectionPolicy.setConnectionMode(ConnectionMode.Direct);
        RxDocumentClientImpl writeClient = (RxDocumentClientImpl) new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.Session)
                .build();
        // Client locked to replica for pause/resume
        RxDocumentClientImpl readSecondaryClient = (RxDocumentClientImpl) new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.Session)
                .build();
        try {
            // Create collection
            DocumentCollection parentResource = writeClient.createCollection(createdDatabase.getSelfLink(), getCollectionDefinition(), null).toBlocking().first().getResource();

            // Document to lock pause/resume clients
            Document documentDefinition = getDocumentDefinition();
            documentDefinition.setId("test" + documentDefinition.getId());
            ResourceResponse<Document> childResource = writeClient.createDocument(parentResource.getSelfLink(), documentDefinition, null, true).toBlocking().first();
            logger.info("Created {} child resource", childResource.getResource().getResourceId());

            String token = childResource.getSessionToken().split(":")[0] + ":" + this.createSessionToken(SessionTokenHelper.parse(childResource.getSessionToken()), 100000000).convertToString();

            FeedOptions feedOptions = new FeedOptions();
            feedOptions.setPartitionKey(new PartitionKey(PartitionKeyInternal.Empty.toJson()));
            feedOptions.setSessionToken(token);
            FailureValidator validator = new FailureValidator.Builder().statusCode(HttpConstants.StatusCodes.NOTFOUND).subStatusCode(HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE).build();
            Observable<FeedResponse<Document>> feedObservable = readSecondaryClient.readDocuments(parentResource.getSelfLink(), feedOptions);
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

    // TODO: DANOBLE: Investigate Direct TCP performance issue
    // Note that we need multiple CONSISTENCY_TEST_TIMEOUT
    // SEE: https://msdata.visualstudio.com/CosmosDB/_workitems/edit/367028https://msdata.visualstudio.com/CosmosDB/_workitems/edit/367028

    @Test(groups = {"direct"}, timeOut = 2 * CONSISTENCY_TEST_TIMEOUT)
    public void validateSessionTokenAsync() {
        // Validate that document query never fails
        // with NotFoundException
        List<Document> documents = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            Document documentDefinition = getDocumentDefinition();
            documentDefinition.set(UUID.randomUUID().toString(), UUID.randomUUID().toString());
            documents.add(documentDefinition);
        }

        ConnectionPolicy connectionPolicy = new ConnectionPolicy();
        connectionPolicy.setConnectionMode(ConnectionMode.Direct);
        RxDocumentClientImpl client = (RxDocumentClientImpl) new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.Session)
                .build();

        try {
            Document lastDocument = client.createDocument(createdCollection.getSelfLink(), getDocumentDefinition(), null, true)
                .toBlocking()
                .first()
                .getResource();

            Completable task1 = ParallelAsync.forEachAsync(Range.between(0, 1000), 5, new Action1<Integer>() {
                @Override
                public void call(Integer index) {
                    client.createDocument(createdCollection.getSelfLink(), documents.get(index % documents.size()), null, true).toBlocking().first();
                }
            });

            Completable task2 = ParallelAsync.forEachAsync(Range.between(0, 1000), 5, new Action1<Integer>() {
                @Override
                public void call(Integer index) {
                    try {
                        FeedOptions feedOptions = new FeedOptions();
                        feedOptions.setEnableCrossPartitionQuery(true);
                        FeedResponse<Document> queryResponse = client.queryDocuments(createdCollection.getSelfLink(), "SELECT * FROM c WHERE c.Id = 'foo'", feedOptions).toBlocking().first();
                        String lsnHeaderValue = queryResponse.getResponseHeaders().get(WFConstants.BackendHeaders.LSN);
                        long lsn = Long.valueOf(lsnHeaderValue);
                        String sessionTokenHeaderValue = queryResponse.getResponseHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN);
                        ISessionToken sessionToken = SessionTokenHelper.parse(sessionTokenHeaderValue);
                        logger.info("Session Token = {}, LSN = {}", sessionToken.convertToString(), lsn);
                        assertThat(lsn).isEqualTo(sessionToken.getLSN());
                    } catch (Exception ex) {
                        DocumentClientException clientException = (DocumentClientException) ex.getCause();
                        if (clientException.getStatusCode() != 0) {
                            if (clientException.getStatusCode() == HttpConstants.StatusCodes.REQUEST_TIMEOUT) {
                                // ignore
                            } else if (clientException.getStatusCode() == HttpConstants.StatusCodes.NOTFOUND) {
                                String lsnHeaderValue = clientException.getResponseHeaders().get(WFConstants.BackendHeaders.LSN);
                                long lsn = Long.valueOf(lsnHeaderValue);
                                String sessionTokenHeaderValue = clientException.getResponseHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN);
                                ISessionToken sessionToken = SessionTokenHelper.parse(sessionTokenHeaderValue);

                                logger.info("Session Token = {}, LSN = {}", sessionToken.convertToString(), lsn);
                                assertThat(lsn).isEqualTo(sessionToken.getLSN());
                            } else {
                                throw ex;
                            }
                        } else {
                            throw ex;
                        }
                    }
                }
            });
            Completable.mergeDelayError(task1, task2).await();
        } finally {
            safeClose(client);
        }
    }

}
