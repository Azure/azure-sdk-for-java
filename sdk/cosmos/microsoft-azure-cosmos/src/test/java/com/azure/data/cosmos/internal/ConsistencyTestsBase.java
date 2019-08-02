// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.


package com.azure.data.cosmos.internal;

import com.azure.data.cosmos.AccessCondition;
import com.azure.data.cosmos.AccessConditionType;
import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.ConnectionMode;
import com.azure.data.cosmos.ConnectionPolicy;
import com.azure.data.cosmos.ConsistencyLevel;
import com.azure.data.cosmos.PartitionKey;
import com.azure.data.cosmos.PartitionKeyDefinition;
import com.azure.data.cosmos.PartitionKind;
import com.azure.data.cosmos.Resource;
import com.azure.data.cosmos.internal.directconnectivity.WFConstants;
import com.azure.data.cosmos.internal.routing.PartitionKeyInternalHelper;
import com.azure.data.cosmos.internal.routing.Range;
import org.apache.commons.collections4.map.UnmodifiableMap;
import org.apache.commons.lang3.StringUtils;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import reactor.core.publisher.Flux;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

public class ConsistencyTestsBase extends TestSuiteBase {
    static final int CONSISTENCY_TEST_TIMEOUT = 120000;
    static final String USER_NAME = "TestUser";
    RxDocumentClientImpl writeClient;
    RxDocumentClientImpl readClient;
    AsyncDocumentClient initClient;
    Database createdDatabase;
    DocumentCollection createdCollection;

    @BeforeClass(groups = {"direct"}, timeOut = SETUP_TIMEOUT)
    public void beforeClass() throws Exception {
        initClient = createGatewayRxDocumentClient().build();
        createdDatabase = SHARED_DATABASE;
        createdCollection = SHARED_MULTI_PARTITION_COLLECTION;
    }

    void validateStrongConsistency(Resource resourceToWorkWith) throws Exception {
        int numberOfTestIteration = 5;
        Resource writeResource = resourceToWorkWith;
        while (numberOfTestIteration-- > 0) //Write from a client and do point read through second client and ensure TS matches.
        {
            OffsetDateTime sourceTimestamp = writeResource.timestamp();
            Thread.sleep(1000); //Timestamp is in granularity of seconds.
            Resource updatedResource = null;
            if (resourceToWorkWith instanceof User) {
                updatedResource = this.writeClient.upsertUser(createdDatabase.selfLink(), (User) writeResource, null).blockFirst().getResource();
            } else if (resourceToWorkWith instanceof Document) {
                RequestOptions options = new RequestOptions();
                options.setPartitionKey(new PartitionKey(resourceToWorkWith.get("mypk")));
                updatedResource = this.writeClient.upsertDocument(createdCollection.selfLink(), (Document) writeResource, options, false).blockFirst().getResource();
            }
            assertThat(updatedResource.timestamp().isAfter(sourceTimestamp)).isTrue();

            User readResource = this.readClient.readUser(resourceToWorkWith.selfLink(), null).blockFirst().getResource();
            assertThat(updatedResource.timestamp().equals(readResource.timestamp()));
        }
    }

    void validateConsistentLSN() {
        Document documentDefinition = getDocumentDefinition();
        RequestOptions options = new RequestOptions();
        options.setPartitionKey(new PartitionKey(documentDefinition.get("mypk")));
        Document document = createDocument(this.writeClient, createdDatabase.id(), createdCollection.id(), documentDefinition);
        ResourceResponse response = this.writeClient.deleteDocument(document.selfLink(), options).single().block();
        assertThat(response.getStatusCode()).isEqualTo(204);

        long quorumAckedLSN = Long.parseLong((String) response.getResponseHeaders().get(WFConstants.BackendHeaders.QUORUM_ACKED_LSN));
        assertThat(quorumAckedLSN > 0).isTrue();
        FailureValidator validator = new FailureValidator.Builder().statusCode(404).lsnGreaterThan(quorumAckedLSN).build();
        Flux<ResourceResponse<Document>> readObservable = this.readClient.readDocument(document.selfLink(), options);
        validateFailure(readObservable, validator);
    }

    void validateConsistentLSNAndQuorumAckedLSN() {
        Document documentDefinition = getDocumentDefinition();
        RequestOptions options = new RequestOptions();
        options.setPartitionKey(new PartitionKey(documentDefinition.get("mypk")));
        Document document = createDocument(this.writeClient, createdDatabase.id(), createdCollection.id(), documentDefinition);
        ResourceResponse response = this.writeClient.deleteDocument(document.selfLink(), options).single().block();
        assertThat(response.getStatusCode()).isEqualTo(204);

        long quorumAckedLSN = Long.parseLong((String) response.getResponseHeaders().get(WFConstants.BackendHeaders.QUORUM_ACKED_LSN));
        assertThat(quorumAckedLSN > 0).isTrue();

        FailureValidator validator = new FailureValidator.Builder().statusCode(404).lsnGreaterThanEqualsTo(quorumAckedLSN).exceptionQuorumAckedLSNInNotNull().build();
        Flux<ResourceResponse<Document>> readObservable = this.readClient.deleteDocument(document.selfLink(), options);
        validateFailure(readObservable, validator);

    }

    void validateReadQuorum(ConsistencyLevel consistencyLevel, ResourceType childResourceType, boolean isBoundedStaleness) {
        //TODO this need to complete once we implement emulator container in java, and then we can do operation 
        // like pause, resume, stop, recycle on it needed for this test.
        // https://msdata.visualstudio.com/CosmosDB/_workitems/edit/355053
    }

    void validateStrongConsistencyOnAsyncReplication(boolean useGateway) throws InterruptedException {
        if (!TestConfigurations.CONSISTENCY.equalsIgnoreCase(ConsistencyLevel.STRONG.toString())) {
            throw new SkipException("Endpoint does not have strong consistency");
        }

        ConnectionPolicy connectionPolicy = new ConnectionPolicy();
        if (useGateway) {
            connectionPolicy.connectionMode(ConnectionMode.GATEWAY);
        }

        this.writeClient = (RxDocumentClientImpl) new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.STRONG).build();

        this.readClient = (RxDocumentClientImpl) new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.STRONG).build();

        Document documentDefinition = getDocumentDefinition();
        Document document = createDocument(this.writeClient, createdDatabase.id(), createdCollection.id(), documentDefinition);
        validateStrongConsistency(document);
    }

    void validateStrongConsistency(Document documentToWorkWith) throws InterruptedException {
        int numberOfTestIteration = 5;
        Document writeDocument = documentToWorkWith;
        while (numberOfTestIteration-- > 0) {
            OffsetDateTime sourceTimestamp = writeDocument.timestamp();
            Thread.sleep(1000);//Timestamp is in granularity of seconds.
            RequestOptions options = new RequestOptions();
            options.setPartitionKey(new PartitionKey(documentToWorkWith.get("mypk")));
            Document updatedDocument = this.writeClient.replaceDocument(writeDocument, options).blockFirst().getResource();
            assertThat(updatedDocument.timestamp().isAfter(sourceTimestamp)).isTrue();

            Document readDocument = this.readClient.readDocument(documentToWorkWith.selfLink(), options).blockFirst().getResource();
            assertThat(updatedDocument.timestamp().equals(readDocument.timestamp()));
        }
    }

    void validateSessionContainerAfterCollectionCreateReplace(boolean useGateway) {
        // DIRECT clients for read and write operations
        ConnectionPolicy connectionPolicy = new ConnectionPolicy();
        if (useGateway) {
            connectionPolicy.connectionMode(ConnectionMode.GATEWAY);
        } else {
            connectionPolicy.connectionMode(ConnectionMode.DIRECT);
        }

        RxDocumentClientImpl writeClient = (RxDocumentClientImpl) new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.SESSION).build();

        try {
            PartitionKeyDefinition partitionKey = new PartitionKeyDefinition();
            partitionKey.paths(Arrays.asList("/customerid"));
            partitionKey.kind(PartitionKind.HASH);
            DocumentCollection coll = null;
            {
                // self link
                ResourceResponse<DocumentCollection> collection = writeClient.createCollection(createdDatabase.selfLink(), getCollectionDefinition(), null).blockFirst();
                String globalSessionToken1 = ((SessionContainer) writeClient.getSession()).getSessionToken(collection.getResource().selfLink());
                String globalSessionToken2 = ((SessionContainer) writeClient.getSession()).getSessionToken(BridgeInternal.getAltLink(collection.getResource()));
                System.out.println("BridgeInternal.getAltLink(collection.getResource()) " + BridgeInternal.getAltLink(collection.getResource()));
                assertThat(collection.getSessionToken()).isEqualTo(globalSessionToken1);
                assertThat(collection.getSessionToken()).isEqualTo(globalSessionToken2);

                coll = collection.getResource();
                ResourceResponse<DocumentCollection> collectionRead = writeClient.readCollection(coll.selfLink(), null).blockFirst();
                // timesync might bump the version, comment out the check
                //assertThat(collection.sessionToken()).isEqualTo(collectionRead.sessionToken());
            }
            {
                // name link
                ResourceResponse<DocumentCollection> collection = writeClient.createCollection(BridgeInternal.getAltLink(createdDatabase), getCollectionDefinition(), null).blockFirst();

                String globalSessionToken1 = ((SessionContainer) writeClient.getSession()).getSessionToken(collection.getResource().selfLink());
                String globalSessionToken2 = ((SessionContainer) writeClient.getSession()).getSessionToken(BridgeInternal.getAltLink(collection.getResource()));
                assertThat(collection.getSessionToken()).isEqualTo(globalSessionToken1);
                //assertThat(collection.sessionToken()).isEqualTo(globalSessionToken2);

                ResourceResponse<DocumentCollection> collectionRead =
                        writeClient.readCollection(BridgeInternal.getAltLink(collection.getResource()), null).blockFirst();
                // timesync might bump the version, comment out the check
                //assertThat(collection.sessionToken()).isEqualTo(collectionRead.sessionToken());
            }
            {
                Document document2 = new Document();
                document2.id("test" + UUID.randomUUID().toString());
                BridgeInternal.setProperty(document2, "customerid", 2);
                // name link
                ResourceResponse<Document> document = writeClient.createDocument(BridgeInternal.getAltLink(coll),
                                                                                 document2, null, false)
                        .blockFirst();
                String globalSessionToken1 = ((SessionContainer) writeClient.getSession()).getSessionToken(coll.selfLink());
                String globalSessionToken2 = ((SessionContainer) writeClient.getSession()).getSessionToken(BridgeInternal.getAltLink(coll));

                assertThat(globalSessionToken1.indexOf(document.getSessionToken())).isNotNegative();
                assertThat(globalSessionToken2.indexOf(document.getSessionToken())).isNotNegative();
            }
            {
                Document document2 = new Document();
                document2.id("test" + UUID.randomUUID().toString());
                BridgeInternal.setProperty(document2, "customerid", 3);
                // name link
                ResourceResponse<Document> document = writeClient.createDocument(BridgeInternal.getAltLink(coll),
                                                                                 document2, null, false)
                        .blockFirst();
                String globalSessionToken1 = ((SessionContainer) writeClient.getSession()).getSessionToken(coll.selfLink());
                String globalSessionToken2 = ((SessionContainer) writeClient.getSession()).getSessionToken(BridgeInternal.getAltLink(coll));

                assertThat(globalSessionToken1.indexOf(document.getSessionToken())).isNotNegative();
                assertThat(globalSessionToken2.indexOf(document.getSessionToken())).isNotNegative();
            }
        } finally {
            safeClose(writeClient);
        }
    }

    boolean validateConsistentPrefix(Resource resourceToWorkWith) throws InterruptedException {
        int numberOfTestIteration = 5;
        OffsetDateTime lastReadDateTime = resourceToWorkWith.timestamp();
        boolean readLagging = false;
        Resource writeResource = resourceToWorkWith;

        while (numberOfTestIteration-- > 0) { //Write from a client and do point read through second client and ensure TS monotonically increases.
            OffsetDateTime sourceTimestamp = writeResource.timestamp();
            Thread.sleep(1000); //Timestamp is in granularity of seconds.
            Resource updatedResource = null;
            if (resourceToWorkWith instanceof User) {
                updatedResource = this.writeClient.upsertUser(createdDatabase.selfLink(), (User) writeResource,
                                                              null)
                        .blockFirst()
                        .getResource();
            } else if (resourceToWorkWith instanceof Document) {
                updatedResource = this.writeClient.upsertDocument(createdCollection.selfLink(),
                                                                  (Document) writeResource, null, false)
                        .blockFirst()
                        .getResource();
            }
            assertThat(updatedResource.timestamp().isAfter(sourceTimestamp)).isTrue();
            writeResource = updatedResource;

            Resource readResource = null;
            if (resourceToWorkWith instanceof User) {
                readResource = this.readClient.readUser(resourceToWorkWith.selfLink(), null)
                        .blockFirst()
                        .getResource();
            } else if (resourceToWorkWith instanceof Document) {
                RequestOptions options = new RequestOptions();
                options.setPartitionKey(new PartitionKey(resourceToWorkWith.get("mypk")));
                readResource = this.readClient.readDocument(resourceToWorkWith.selfLink(), options)
                        .blockFirst()
                        .getResource();
            }
            assertThat(readResource.timestamp().compareTo(lastReadDateTime) >= 0).isTrue();
            lastReadDateTime = readResource.timestamp();
            if (readResource.timestamp().isBefore(updatedResource.timestamp())) {
                readLagging = true;
            }
        }
        return readLagging;
    }

    boolean validateReadSession(Resource resourceToWorkWith) throws InterruptedException {
        int numberOfTestIteration = 5;
        OffsetDateTime lastReadDateTime = OffsetDateTime.MIN;
        boolean readLagging = false;
        Resource writeResource = resourceToWorkWith;

        while (numberOfTestIteration-- > 0) {
            OffsetDateTime sourceTimestamp = writeResource.timestamp();
            Thread.sleep(1000);
            Resource updatedResource = null;
            if (resourceToWorkWith instanceof Document) {
                updatedResource = this.writeClient.upsertDocument(createdCollection.selfLink(), writeResource,
                                                                  null, false)
                        .single()
                        .block()
                        .getResource();
            }
            assertThat(updatedResource.timestamp().isAfter(sourceTimestamp)).isTrue();
            writeResource = updatedResource;

            Resource readResource = null;
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.setPartitionKey(new PartitionKey(resourceToWorkWith.get("mypk")));
            if (resourceToWorkWith instanceof Document) {
                readResource = this.readClient.readDocument(resourceToWorkWith.selfLink(), requestOptions).blockFirst().getResource();
            }
            assertThat(readResource.timestamp().compareTo(lastReadDateTime) >= 0).isTrue();
            lastReadDateTime = readResource.timestamp();

            if (readResource.timestamp().isBefore(updatedResource.timestamp())) {
                readLagging = true;
            }
        }
        return readLagging;
    }

    boolean validateWriteSession(Resource resourceToWorkWith) throws InterruptedException {
        int numberOfTestIteration = 5;
        OffsetDateTime lastReadDateTime = OffsetDateTime.MIN;
        boolean readLagging = false;
        Resource writeResource = resourceToWorkWith;

        while (numberOfTestIteration-- > 0) {
            OffsetDateTime sourceTimestamp = writeResource.timestamp();
            Thread.sleep(1000);
            Resource updatedResource = null;
            if (resourceToWorkWith instanceof Document) {
                updatedResource = this.writeClient.upsertDocument(createdCollection.selfLink(), writeResource, null, false).single().block().getResource();
            }
            assertThat(updatedResource.timestamp().isAfter(sourceTimestamp)).isTrue();
            writeResource = updatedResource;

            Resource readResource = null;
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.setPartitionKey(new PartitionKey(resourceToWorkWith.get("mypk")));
            if (resourceToWorkWith instanceof Document) {
                readResource =
                        this.readClient.readDocument(resourceToWorkWith.selfLink(), requestOptions)
                                .blockFirst()
                                .getResource();
            }
            assertThat(readResource.timestamp().compareTo(lastReadDateTime) >= 0).isTrue();
            lastReadDateTime = readResource.timestamp();

            if (readResource.timestamp().isBefore(updatedResource.timestamp())) {
                readLagging = true;
            }

            //Now perform write on session and update our session token and lastReadTS
            Thread.sleep(1000);
            if (resourceToWorkWith instanceof Document) {
                readResource = this.writeClient.upsertDocument(createdCollection.selfLink(), readResource,
                                                               requestOptions, false)
                        .blockFirst()
                        .getResource();
                //Now perform write on session
            }
            assertThat(readResource.timestamp().isAfter(lastReadDateTime));

            this.readClient.setSession(this.writeClient.getSession());
        }
        return readLagging;
    }

    void validateSessionContainerAfterCollectionDeletion(boolean useGateway) throws Exception {
        ConnectionPolicy connectionPolicy = new ConnectionPolicy();
        if (useGateway) {
            connectionPolicy.connectionMode(ConnectionMode.GATEWAY);
        } else {
            connectionPolicy.connectionMode(ConnectionMode.DIRECT);
        }
        RxDocumentClientImpl client1 = (RxDocumentClientImpl) new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.SESSION)
                .build();
        RxDocumentClientImpl client2 = (RxDocumentClientImpl) new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.SESSION)
                .build();

        String collectionId = UUID.randomUUID().toString();
        try {
            DocumentCollection collectionDefinition = getCollectionDefinition();
            collectionDefinition.id(collectionId);
            DocumentCollection collection = createCollection(client2, createdDatabase.id(), collectionDefinition, null);
            ResourceResponseValidator<DocumentCollection> successValidatorCollection = new ResourceResponseValidator.Builder<DocumentCollection>()
                    .withId(collection.id())
                    .build();
            Flux<ResourceResponse<DocumentCollection>> readObservableCollection = client2.readCollection(collection.selfLink(), null);
            validateSuccess(readObservableCollection, successValidatorCollection);

            for (int i = 0; i < 5; i++) {
                String documentId = "Generation1-" + i;
                Document documentDefinition = getDocumentDefinition();
                documentDefinition.id(documentId);
                Document documentCreated = client2.createDocument(collection.selfLink(), documentDefinition, null, true).blockFirst().getResource();
                RequestOptions requestOptions = new RequestOptions();
                requestOptions.setPartitionKey(new PartitionKey(documentCreated.get("mypk")));
                client2.readDocument(BridgeInternal.getAltLink(documentCreated), requestOptions).blockFirst();
                client2.readDocument(documentCreated.selfLink(), requestOptions).blockFirst();
            }

            {
                // just create the second for fun
                DocumentCollection collection2 = createCollection(client2, createdDatabase.id(), getCollectionDefinition(), null);
                successValidatorCollection = new ResourceResponseValidator.Builder<DocumentCollection>()
                        .withId(collection2.id())
                        .build();
                readObservableCollection = client2.readCollection(collection2.selfLink(), null);
                validateSuccess(readObservableCollection, successValidatorCollection);
            }
            // verify the client2 has the same token.
            {
                String token1 = ((SessionContainer) client2.getSession()).getSessionToken(BridgeInternal.getAltLink(collection));
                String token2 = ((SessionContainer) client2.getSession()).getSessionToken(collection.selfLink());
                assertThat(token1).isEqualTo(token2);
            }

            // now delete collection use different client
            client1.deleteCollection(collection.selfLink(), null).blockFirst();

            DocumentCollection collectionRandom1 = createCollection(client2, createdDatabase.id(), getCollectionDefinition());
            DocumentCollection documentCollection = getCollectionDefinition();
            collectionDefinition.id(collectionId);
            DocumentCollection collectionSameName = createCollection(client2, createdDatabase.id(), collectionDefinition);
            String documentId1 = "Generation2-" + 0;
            Document databaseDefinition2 = getDocumentDefinition();
            databaseDefinition2.id(documentId1);
            Document createdDocument = client1.createDocument(collectionSameName.selfLink(), databaseDefinition2, null, true).blockFirst().getResource();
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.setPartitionKey(new PartitionKey(createdDocument.get("mypk")));
            ResourceResponseValidator<Document> successValidator = new ResourceResponseValidator.Builder<Document>()
                    .withId(createdDocument.id())
                    .build();
            Flux<ResourceResponse<Document>> readObservable = client1.readDocument(createdDocument.selfLink(), requestOptions);
            validateSuccess(readObservable, successValidator);
            {
                String token1 = ((SessionContainer) client1.getSession()).getSessionToken(BridgeInternal.getAltLink(collectionSameName));
                String token2 = ((SessionContainer) client1.getSession()).getSessionToken(collectionSameName.selfLink());
                assertThat(token1).isEqualTo(token2);
            }

            {
                // Client2 read using name link should fail with higher LSN.
                String token = ((SessionContainer) client1.getSession()).getSessionToken(collectionSameName.selfLink());
                // artificially bump to higher LSN
                String higherLsnToken = this.getDifferentLSNToken(token, 2000);
                RequestOptions requestOptions1 = new RequestOptions();
                requestOptions1.setSessionToken(higherLsnToken);
                requestOptions1.setPartitionKey(new PartitionKey(createdDocument.get("mypk")));
                readObservable = client2.readDocument(BridgeInternal.getAltLink(createdDocument), requestOptions1);
                FailureValidator failureValidator = new FailureValidator.Builder().subStatusCode(1002).build();
                validateFailure(readObservable, failureValidator);
            }
            // this will trigger client2 to clear the token
            {
                // verify token by altlink is gone!
                String token1 = ((SessionContainer) client2.getSession()).getSessionToken(BridgeInternal.getAltLink(collectionSameName));
                String token2 = ((SessionContainer) client2.getSession()).getSessionToken(collection.selfLink());
                assertThat(token1).isEmpty();
                //assertThat(token2).isNotEmpty(); In java both SelfLink and AltLink token remain in sync.
            }
            {
                // second read should succeed!
                readObservable = client2.readDocument(BridgeInternal.getAltLink(createdDocument), requestOptions);
                validateSuccess(readObservable, successValidator);
            }
            // verify deleting indeed delete the collection session token
            {
                Document documentTest =
                        client1.createDocument(BridgeInternal.getAltLink(collectionSameName), getDocumentDefinition(), null, true).blockFirst().getResource();
                RequestOptions options = new RequestOptions();
                options.setPartitionKey(new PartitionKey(documentTest.get("mypk")));
                successValidator = new ResourceResponseValidator.Builder<Document>()
                        .withId(documentTest.id())
                        .build();
                readObservable = client1.readDocument(documentTest.selfLink(), options);
                validateSuccess(readObservable, successValidator);

                client1.deleteCollection(collectionSameName.selfLink(), null).blockFirst();
                String token1 = ((SessionContainer) client2.getSession()).getSessionToken(BridgeInternal.getAltLink(collectionSameName));
                String token2 = ((SessionContainer) client2.getSession()).getSessionToken(collectionSameName.selfLink());
                // currently we can't delete the token from Altlink when deleting using selflink
                assertThat(token1).isNotEmpty();
                //assertThat(token2).isEmpty(); In java both SelfLink and AltLink token remain in sync.
            }
        } finally {
            safeClose(client1);
            safeClose(client2);
        }

    }

    void validateSessionTokenWithPreConditionFailure(boolean useGateway) throws Exception {
        ConnectionPolicy connectionPolicy = new ConnectionPolicy();
        if (useGateway) {
            connectionPolicy.connectionMode(ConnectionMode.GATEWAY);
        } else {
            connectionPolicy.connectionMode(ConnectionMode.DIRECT);
        }
        RxDocumentClientImpl writeClient = (RxDocumentClientImpl) new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.SESSION)
                .build();
        RxDocumentClientImpl validationClient = (RxDocumentClientImpl) new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.SESSION)
                .build();
        try {
            // write a document, and upsert to it to update etag.
            ResourceResponse<Document> documentResponse = writeClient.createDocument(BridgeInternal.getAltLink(createdCollection), getDocumentDefinition(), null, true).blockFirst();
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.setPartitionKey(new PartitionKey(documentResponse.getResource().get("mypk")));
            ResourceResponse<Document> upsertResponse =
                    writeClient.upsertDocument(BridgeInternal.getAltLink(createdCollection), documentResponse.getResource(), requestOptions, true).blockFirst();

            // create a conditioned read request, with first write request's etag, so the read fails with PreconditionFailure
            AccessCondition ac = new AccessCondition();
            ac.condition(documentResponse.getResource().etag());
            ac.type(AccessConditionType.IF_MATCH);
            RequestOptions requestOptions1 = new RequestOptions();
            requestOptions.setPartitionKey(new PartitionKey(documentResponse.getResource().get("mypk")));
            requestOptions1.setAccessCondition(ac);
            Flux<ResourceResponse<Document>> preConditionFailureResponseObservable = validationClient.upsertDocument(BridgeInternal.getAltLink(createdCollection),
                    documentResponse.getResource(), requestOptions1, true);
            FailureValidator failureValidator = new FailureValidator.Builder().statusCode(HttpConstants.StatusCodes.PRECONDITION_FAILED).build();
            validateFailure(preConditionFailureResponseObservable, failureValidator);
            assertThat(isSessionEqual(((SessionContainer) validationClient.getSession()), (SessionContainer) writeClient.getSession())).isTrue();

        } finally {
            safeClose(writeClient);
            safeClose(validationClient);
        }
    }

    void validateSessionTokenWithDocumentNotFoundException(boolean useGateway) throws Exception {
        ConnectionPolicy connectionPolicy = new ConnectionPolicy();
        if (useGateway) {
            connectionPolicy.connectionMode(ConnectionMode.GATEWAY);
        } else {
            connectionPolicy.connectionMode(ConnectionMode.DIRECT);
        }
        RxDocumentClientImpl writeClient = (RxDocumentClientImpl) new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.SESSION)
                .build();
        RxDocumentClientImpl validationClient = (RxDocumentClientImpl) new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.SESSION)
                .build();
        try {
            DocumentCollection collectionDefinition = getCollectionDefinition();
            collectionDefinition.id("TestCollection");

            ResourceResponse<Document> documentResponse = writeClient.createDocument(BridgeInternal.getAltLink(createdCollection), getDocumentDefinition(), null, true).blockFirst();

            FailureValidator failureValidator = new FailureValidator.Builder().statusCode(HttpConstants.StatusCodes.NOTFOUND).build();
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.setPartitionKey(new PartitionKey(documentResponse.getResource().get("mypk")));
            // try to read a non existent document in the same partition that we previously wrote to
            Flux<ResourceResponse<Document>> readObservable = validationClient.readDocument(BridgeInternal.getAltLink(documentResponse.getResource()) + "dummy", requestOptions);
            validateFailure(readObservable, failureValidator);
            assertThat(isSessionEqual(((SessionContainer) validationClient.getSession()), (SessionContainer) writeClient.getSession())).isTrue();
        } finally {
            safeClose(writeClient);
            safeClose(validationClient);
        }
    }

    void validateSessionTokenWithExpectedException(boolean useGateway) throws Exception {
        ConnectionPolicy connectionPolicy = new ConnectionPolicy();
        if (useGateway) {
            connectionPolicy.connectionMode(ConnectionMode.GATEWAY);
        } else {
            connectionPolicy.connectionMode(ConnectionMode.DIRECT);
        }
        RxDocumentClientImpl writeClient = (RxDocumentClientImpl) new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.SESSION)
                .build();
        try {
            ResourceResponse<Document> documentResponse =
                    writeClient.createDocument(BridgeInternal.getAltLink(createdCollection), getDocumentDefinition(), null, false).blockFirst();
            String token = documentResponse.getResponseHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN);

            // artificially bump to higher LSN
            String higherLsnToken = this.getDifferentLSNToken(token, 2000);
            FailureValidator failureValidator = new FailureValidator.Builder().subStatusCode(HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE).build();
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.setPartitionKey(new PartitionKey(documentResponse.getResource().get("mypk")));
            requestOptions.setSessionToken(higherLsnToken);
            // try to read a non existent document in the same partition that we previously wrote to
            Flux<ResourceResponse<Document>> readObservable = writeClient.readDocument(BridgeInternal.getAltLink(documentResponse.getResource()),
                    requestOptions);
            validateFailure(readObservable, failureValidator);

        } finally {
            safeClose(writeClient);
        }
    }

    void validateSessionTokenWithConflictException(boolean useGateway) {
        ConnectionPolicy connectionPolicy = new ConnectionPolicy();
        if (useGateway) {
            connectionPolicy.connectionMode(ConnectionMode.GATEWAY);
        } else {
            connectionPolicy.connectionMode(ConnectionMode.DIRECT);
        }
        RxDocumentClientImpl writeClient = (RxDocumentClientImpl) new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.SESSION)
                .build();
        RxDocumentClientImpl validationClient = (RxDocumentClientImpl) new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.SESSION)
                .build();
        try {
            Document documentDefinition = getDocumentDefinition();
            ResourceResponse<Document> documentResponse =
                    writeClient.createDocument(BridgeInternal.getAltLink(createdCollection), documentDefinition, null, true).blockFirst();

            FailureValidator failureValidator = new FailureValidator.Builder().statusCode(HttpConstants.StatusCodes.CONFLICT).build();
            Flux<ResourceResponse<Document>> conflictDocumentResponse = validationClient.createDocument(BridgeInternal.getAltLink(createdCollection),
                    documentDefinition, null,
                    true);
            validateFailure(conflictDocumentResponse, failureValidator);
        } finally {
            safeClose(writeClient);
            safeClose(validationClient);
        }
    }

    void validateSessionTokenMultiPartitionCollection(boolean useGateway) throws Exception {
        ConnectionPolicy connectionPolicy = new ConnectionPolicy();
        if (useGateway) {
            connectionPolicy.connectionMode(ConnectionMode.GATEWAY);
        } else {
            connectionPolicy.connectionMode(ConnectionMode.DIRECT);
        }
        RxDocumentClientImpl writeClient = (RxDocumentClientImpl) new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.SESSION)
                .build();
        try {

            Range<String> fullRange = new Range<String>(PartitionKeyInternalHelper.MinimumInclusiveEffectivePartitionKey,
                    PartitionKeyInternalHelper.MaximumExclusiveEffectivePartitionKey, true, false);

            IRoutingMapProvider routingMapProvider = writeClient.getPartitionKeyRangeCache();
            // assertThat(routingMapProvider.tryGetOverlappingRangesAsync(collection.resourceId(), fullRange, false).toBlocking().value().size()).isEqualTo(5);

            // Document to lock pause/resume clients
            Document document1 = new Document();
            document1.id("test" + UUID.randomUUID().toString());
            BridgeInternal.setProperty(document1, "mypk", 1);
            ResourceResponse<Document> childResource1 = writeClient.createDocument(createdCollection.selfLink(), document1, null, true).blockFirst();
            logger.info("Created {} child resource", childResource1.getResource().resourceId());
            assertThat(childResource1.getSessionToken()).isNotNull();
            assertThat(childResource1.getSessionToken().contains(":")).isTrue();
            String globalSessionToken1 = ((SessionContainer) writeClient.getSession()).getSessionToken(createdCollection.selfLink());
            assertThat(globalSessionToken1.contains(childResource1.getSessionToken()));

            // Document to lock pause/resume clients
            Document document2 = new Document();
            document2.id("test" + UUID.randomUUID().toString());
            BridgeInternal.setProperty(document2, "mypk", 2);
            ResourceResponse<Document> childResource2 = writeClient.createDocument(createdCollection.selfLink(), document2, null, true).blockFirst();
            assertThat(childResource2.getSessionToken()).isNotNull();
            assertThat(childResource2.getSessionToken().contains(":")).isTrue();
            String globalSessionToken2 = ((SessionContainer) writeClient.getSession()).getSessionToken(createdCollection.selfLink());
            logger.info("globalsessiontoken2 {}, childtoken1 {}, childtoken2 {}", globalSessionToken2, childResource1.getSessionToken(), childResource2.getSessionToken());
            assertThat(globalSessionToken2.contains(childResource2.getSessionToken())).isTrue();

            // this token can read childresource2 but not childresource1
            String sessionToken =
                    StringUtils.split(childResource1.getSessionToken(), ':')[0] + ":" + createSessionToken(SessionTokenHelper.parse(childResource1.getSessionToken()), 100000000).convertToString() + "," + childResource2.getSessionToken();

            RequestOptions option = new RequestOptions();
            option.setSessionToken(sessionToken);
            option.setPartitionKey(new PartitionKey(2));
            writeClient.readDocument(childResource2.getResource().selfLink(), option).blockFirst();

            option = new RequestOptions();
            option.setSessionToken(StringUtils.EMPTY);
            option.setPartitionKey(new PartitionKey(1));
            writeClient.readDocument(childResource1.getResource().selfLink(), option).blockFirst();

            option = new RequestOptions();
            option.setSessionToken(sessionToken);
            option.setPartitionKey(new PartitionKey(1));
            Flux<ResourceResponse<Document>> readObservable = writeClient.readDocument(childResource1.getResource().selfLink(), option);
            FailureValidator failureValidator =
                    new FailureValidator.Builder().statusCode(HttpConstants.StatusCodes.NOTFOUND).subStatusCode(HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE).build();
            validateFailure(readObservable, failureValidator);

            readObservable = writeClient.readDocument(childResource2.getResource().selfLink(), option);
            failureValidator =
                    new FailureValidator.Builder().statusCode(HttpConstants.StatusCodes.NOTFOUND).subStatusCode(HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE).build();
            validateFailure(readObservable, failureValidator);

            assertThat(((SessionContainer) writeClient.getSession()).getSessionToken(createdCollection.selfLink())).isEqualTo
                    (((SessionContainer) writeClient.getSession()).getSessionToken(BridgeInternal.getAltLink(createdCollection)));

            assertThat(((SessionContainer) writeClient.getSession()).getSessionToken("asdfasdf")).isEmpty();
            assertThat(((SessionContainer) writeClient.getSession()).getSessionToken(createdDatabase.selfLink())).isEmpty();
        } finally {
            safeClose(writeClient);
        }
    }

    void validateSessionTokenFromCollectionReplaceIsServerToken(boolean useGateway) {
        ConnectionPolicy connectionPolicy = new ConnectionPolicy();
        if (useGateway) {
            connectionPolicy.connectionMode(ConnectionMode.GATEWAY);
        } else {
            connectionPolicy.connectionMode(ConnectionMode.DIRECT);
        }
        RxDocumentClientImpl client1 = (RxDocumentClientImpl) new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.SESSION)
                .build();
        RxDocumentClientImpl client2 = null;
        try {
            Document doc = client1.createDocument(createdCollection.selfLink(), getDocumentDefinition(), null, true).blockFirst().getResource();
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.setPartitionKey(new PartitionKey(doc.get("mypk")));
            Document doc1 = client1.readDocument(BridgeInternal.getAltLink(doc), requestOptions).blockFirst().getResource();

            String token1 = ((SessionContainer) client1.getSession()).getSessionToken(createdCollection.selfLink());
            client2 = (RxDocumentClientImpl) new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                    .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                    .withConnectionPolicy(connectionPolicy)
                    .withConsistencyLevel(ConsistencyLevel.SESSION)
                    .build();
            client2.replaceCollection(createdCollection, null).blockFirst();
            String token2 = ((SessionContainer) client2.getSession()).getSessionToken(createdCollection.selfLink());

            logger.info("Token after document and after collection replace {} = {}", token1, token2);
        } finally {
            safeClose(client1);
            safeClose(client2);
        }
    }

    @AfterClass(groups = {"direct"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(this.initClient);
        safeClose(this.writeClient);
        safeClose(this.readClient);
    }

    private String getDifferentLSNToken(String token, long lsnDifferent) throws Exception {
        String[] tokenParts = StringUtils.split(token, ':');
        ISessionToken sessionToken = SessionTokenHelper.parse(tokenParts[1]);
        ISessionToken differentSessionToken = createSessionToken(sessionToken, sessionToken.getLSN() + lsnDifferent);
        return String.format("%s:%s", tokenParts[0], differentSessionToken.convertToString());
    }

    public static ISessionToken createSessionToken(ISessionToken from, long globalLSN) throws Exception {
        // Creates session token with specified GlobalLSN
        if (from instanceof VectorSessionToken) {
            VectorSessionToken fromSessionToken = (VectorSessionToken) from;
            Field fieldVersion = VectorSessionToken.class.getDeclaredField("version");
            fieldVersion.setAccessible(true);
            Long version = (Long) fieldVersion.get(fromSessionToken);

            Field fieldLocalLsnByRegion = VectorSessionToken.class.getDeclaredField("localLsnByRegion");
            fieldLocalLsnByRegion.setAccessible(true);
            UnmodifiableMap<Integer, Long> localLsnByRegion = (UnmodifiableMap<Integer, Long>) fieldLocalLsnByRegion.get(fromSessionToken);

            Constructor<VectorSessionToken> constructor = VectorSessionToken.class.getDeclaredConstructor(long.class, long.class, UnmodifiableMap.class);
            constructor.setAccessible(true);
            VectorSessionToken vectorSessionToken = constructor.newInstance(version, globalLSN, localLsnByRegion);
            return vectorSessionToken;
        } else {
            throw new IllegalArgumentException();
        }
    }

    Document getDocumentDefinition() {
        String uuid = UUID.randomUUID().toString();
        Document doc = new Document(String.format("{ "
                        + "\"id\": \"%s\", "
                        + "\"mypk\": \"%s\", "
                        + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]"
                        + "}"
                , uuid, uuid));
        return doc;
    }

    private boolean isSessionEqual(SessionContainer sessionContainer1, SessionContainer sessionContainer2) throws Exception {
        if (sessionContainer1 == null) {
            return false;
        }

        if (sessionContainer2 == null) {
            return false;
        }

        if (sessionContainer1 == sessionContainer2) {
            return true;
        }

        Field fieldCollectionResourceIdToSessionTokens1 = SessionContainer.class.getDeclaredField("collectionResourceIdToSessionTokens");
        Field fieldCollectionNameToCollectionResourceId1 = SessionContainer.class.getDeclaredField("collectionNameToCollectionResourceId");
        fieldCollectionResourceIdToSessionTokens1.setAccessible(true);
        fieldCollectionNameToCollectionResourceId1.setAccessible(true);
        ConcurrentHashMap<Long, ConcurrentHashMap<String, ISessionToken>> collectionResourceIdToSessionTokens1 =
                (ConcurrentHashMap<Long, ConcurrentHashMap<String, ISessionToken>>) fieldCollectionResourceIdToSessionTokens1.get(sessionContainer1);
        ConcurrentHashMap<String, Long> collectionNameToCollectionResourceId1 = (ConcurrentHashMap<String, Long>) fieldCollectionNameToCollectionResourceId1.get(sessionContainer1);


        Field fieldCollectionResourceIdToSessionTokens2 = SessionContainer.class.getDeclaredField("collectionResourceIdToSessionTokens");
        Field fieldCollectionNameToCollectionResourceId2 = SessionContainer.class.getDeclaredField("collectionNameToCollectionResourceId");
        fieldCollectionResourceIdToSessionTokens2.setAccessible(true);
        fieldCollectionNameToCollectionResourceId2.setAccessible(true);
        ConcurrentHashMap<Long, ConcurrentHashMap<String, ISessionToken>> collectionResourceIdToSessionTokens2 =
                (ConcurrentHashMap<Long, ConcurrentHashMap<String, ISessionToken>>) fieldCollectionResourceIdToSessionTokens2.get(sessionContainer2);
        ConcurrentHashMap<String, Long> collectionNameToCollectionResourceId2 = (ConcurrentHashMap<String, Long>) fieldCollectionNameToCollectionResourceId2.get(sessionContainer2);

        if (collectionResourceIdToSessionTokens1.size() != collectionResourceIdToSessionTokens2.size() ||
                collectionNameToCollectionResourceId1.size() != collectionNameToCollectionResourceId2.size()) {
            return false;
        }

        // get keys, and compare entries
        for (Long resourceId : collectionResourceIdToSessionTokens1.keySet()) {
            if (!collectionResourceIdToSessionTokens1.get(resourceId).equals(collectionResourceIdToSessionTokens2.get(resourceId))) {
                return false;
            }
        }

        for (String collectionName : collectionNameToCollectionResourceId1.keySet()) {
            if (!collectionNameToCollectionResourceId1.get(collectionName).equals(collectionNameToCollectionResourceId2.get(collectionName))) {
                return false;
            }
        }

        return true;
    }
}