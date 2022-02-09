// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.


package com.azure.cosmos.implementation;

import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.GatewayConnectionConfig;
import com.azure.cosmos.implementation.apachecommons.collections.map.UnmodifiableMap;
import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.models.PartitionKind;
import com.azure.cosmos.implementation.directconnectivity.WFConstants;
import com.azure.cosmos.implementation.routing.PartitionKeyInternalHelper;
import com.azure.cosmos.implementation.routing.Range;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import reactor.core.publisher.Mono;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.time.Instant;
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
    public void before_ConsistencyTestsBase() throws Exception {
        initClient = createGatewayRxDocumentClient().build();
        createdDatabase = SHARED_DATABASE;
        createdCollection = SHARED_MULTI_PARTITION_COLLECTION;
    }

    void validateStrongConsistency(Resource resourceToWorkWith) throws Exception {
        int numberOfTestIteration = 5;
        Resource writeResource = resourceToWorkWith;
        while (numberOfTestIteration-- > 0) //Write from a client and do point read through second client and ensure TS matches.
        {
            Instant sourceTimestamp = writeResource.getTimestamp();
            Thread.sleep(1000); //Timestamp is in granularity of seconds.
            Resource updatedResource = null;
            if (resourceToWorkWith instanceof User) {
                updatedResource = this.writeClient.upsertUser(createdDatabase.getSelfLink(), (User) writeResource, null).block().getResource();
            } else if (resourceToWorkWith instanceof Document) {
                RequestOptions options = new RequestOptions();
                options.setPartitionKey(new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(resourceToWorkWith, "mypk")));
                updatedResource = this.writeClient.upsertDocument(createdCollection.getSelfLink(), (Document) writeResource, options, false).block().getResource();
            }
            assertThat(updatedResource.getTimestamp().isAfter(sourceTimestamp)).isTrue();

            User readResource = this.readClient.readUser(resourceToWorkWith.getSelfLink(), null).block().getResource();
            assertThat(updatedResource.getTimestamp().equals(readResource.getTimestamp()));
        }
    }

    void validateConsistentLSN() {
        Document documentDefinition = getDocumentDefinition();
        RequestOptions options = new RequestOptions();
        options.setPartitionKey(new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(documentDefinition, "mypk")));
        Document document = createDocument(this.writeClient, createdDatabase.getId(), createdCollection.getId(), documentDefinition);
        ResourceResponse<Document> response = this.writeClient.deleteDocument(document.getSelfLink(), options).block();
        assertThat(response.getStatusCode()).isEqualTo(204);

        long quorumAckedLSN = Long.parseLong(response.getResponseHeaders().get(WFConstants.BackendHeaders.QUORUM_ACKED_LSN));
        assertThat(quorumAckedLSN > 0).isTrue();
        FailureValidator validator = new FailureValidator.Builder().statusCode(404).lsnGreaterThan(quorumAckedLSN).build();
        Mono<ResourceResponse<Document>> readObservable = this.readClient.readDocument(document.getSelfLink(), options);
        validateFailure(readObservable, validator);
    }

    void validateConsistentLSNAndQuorumAckedLSN() {
        Document documentDefinition = getDocumentDefinition();
        RequestOptions options = new RequestOptions();
        options.setPartitionKey(new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(documentDefinition, "mypk")));
        Document document = createDocument(this.writeClient, createdDatabase.getId(), createdCollection.getId(), documentDefinition);
        ResourceResponse<Document> response = this.writeClient.deleteDocument(document.getSelfLink(), options).block();
        assertThat(response.getStatusCode()).isEqualTo(204);

        long quorumAckedLSN = Long.parseLong(response.getResponseHeaders().get(WFConstants.BackendHeaders.QUORUM_ACKED_LSN));
        assertThat(quorumAckedLSN > 0).isTrue();

        FailureValidator validator = new FailureValidator.Builder().statusCode(404).lsnGreaterThanEqualsTo(quorumAckedLSN).exceptionQuorumAckedLSNInNotNull().build();
        Mono<ResourceResponse<Document>> readObservable = this.readClient.deleteDocument(document.getSelfLink(), options);
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

        ConnectionPolicy connectionPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());
        if (useGateway) {
            connectionPolicy = new ConnectionPolicy(GatewayConnectionConfig.getDefaultConfig());
        }

        this.writeClient = (RxDocumentClientImpl) new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.STRONG).withContentResponseOnWriteEnabled(true).build();

        this.readClient = (RxDocumentClientImpl) new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.STRONG).withContentResponseOnWriteEnabled(true).build();

        Document documentDefinition = getDocumentDefinition();
        Document document = createDocument(this.writeClient, createdDatabase.getId(), createdCollection.getId(), documentDefinition);
        validateStrongConsistency(document);
    }

    void validateStrongConsistency(Document documentToWorkWith) throws InterruptedException {
        int numberOfTestIteration = 5;
        Document writeDocument = documentToWorkWith;
        while (numberOfTestIteration-- > 0) {
            Instant sourceTimestamp = writeDocument.getTimestamp();
            Thread.sleep(1000);//Timestamp is in granularity of seconds.
            RequestOptions options = new RequestOptions();
            options.setPartitionKey(new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(documentToWorkWith, "mypk")));
            Document updatedDocument = this.writeClient.replaceDocument(writeDocument, options).block().getResource();
            assertThat(updatedDocument.getTimestamp().isAfter(sourceTimestamp)).isTrue();

            Document readDocument = this.readClient.readDocument(documentToWorkWith.getSelfLink(), options).block().getResource();
            assertThat(updatedDocument.getTimestamp().equals(readDocument.getTimestamp()));
        }
    }

    void validateSessionContainerAfterCollectionCreateReplace(boolean useGateway) {
        // DIRECT clients for read and write operations
        ConnectionPolicy connectionPolicy;
        if (useGateway) {
            connectionPolicy = new ConnectionPolicy(GatewayConnectionConfig.getDefaultConfig());
        } else {
            connectionPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());
        }

        RxDocumentClientImpl writeClient = (RxDocumentClientImpl) new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.SESSION).withContentResponseOnWriteEnabled(true).build();

        try {
            PartitionKeyDefinition partitionKey = new PartitionKeyDefinition();
            partitionKey.setPaths(Arrays.asList("/customerid"));
            partitionKey.setKind(PartitionKind.HASH);
            DocumentCollection coll = null;
            {
                // self link
                ResourceResponse<DocumentCollection> collection = writeClient.createCollection(createdDatabase.getSelfLink(), getCollectionDefinition(), null).block();
                String globalSessionToken1 = ((SessionContainer) writeClient.getSession()).getSessionToken(collection.getResource().getSelfLink());
                String globalSessionToken2 = ((SessionContainer) writeClient.getSession()).getSessionToken(BridgeInternal.getAltLink(collection.getResource()));
                System.out.println("BridgeInternal.getAltLink(collection.getResource()) " + BridgeInternal.getAltLink(collection.getResource()));
                assertThat(collection.getSessionToken()).isEqualTo(globalSessionToken1);
                assertThat(collection.getSessionToken()).isEqualTo(globalSessionToken2);

                coll = collection.getResource();
                ResourceResponse<DocumentCollection> collectionRead = writeClient.readCollection(coll.getSelfLink(), null).block();
                // timesync might bump the getVersion, comment out the check
                //assertThat(collection.getSessionToken()).isEqualTo(collectionRead.getSessionToken());
            }
            {
                // name link
                ResourceResponse<DocumentCollection> collection = writeClient.createCollection(BridgeInternal.getAltLink(createdDatabase), getCollectionDefinition(), null).block();

                String globalSessionToken1 = ((SessionContainer) writeClient.getSession()).getSessionToken(collection.getResource().getSelfLink());
                String globalSessionToken2 = ((SessionContainer) writeClient.getSession()).getSessionToken(BridgeInternal.getAltLink(collection.getResource()));
                assertThat(collection.getSessionToken()).isEqualTo(globalSessionToken1);
                //assertThat(collection.getSessionToken()).isEqualTo(globalSessionToken2);

                ResourceResponse<DocumentCollection> collectionRead =
                        writeClient.readCollection(BridgeInternal.getAltLink(collection.getResource()), null).block();
                // timesync might bump the version, comment out the check
                //assertThat(collection.sessionToken()).isEqualTo(collectionRead.sessionToken());
            }
            {
                Document document2 = new Document();
                document2.setId("test" + UUID.randomUUID().toString());
                BridgeInternal.setProperty(document2, "customerid", 2);
                // name link
                ResourceResponse<Document> document = writeClient.createDocument(BridgeInternal.getAltLink(coll),
                                                                                 document2, null, false)
                        .block();
                String globalSessionToken1 = ((SessionContainer) writeClient.getSession()).getSessionToken(coll.getSelfLink());
                String globalSessionToken2 = ((SessionContainer) writeClient.getSession()).getSessionToken(BridgeInternal.getAltLink(coll));

                Assertions.assertThat(globalSessionToken1.indexOf(document.getSessionToken())).isNotNegative();
                Assertions.assertThat(globalSessionToken2.indexOf(document.getSessionToken())).isNotNegative();
            }
            {
                Document document2 = new Document();
                document2.setId("test" + UUID.randomUUID().toString());
                BridgeInternal.setProperty(document2, "customerid", 3);
                // name link
                ResourceResponse<Document> document = writeClient.createDocument(BridgeInternal.getAltLink(coll),
                                                                                 document2, null, false)
                        .block();
                String globalSessionToken1 = ((SessionContainer) writeClient.getSession()).getSessionToken(coll.getSelfLink());
                String globalSessionToken2 = ((SessionContainer) writeClient.getSession()).getSessionToken(BridgeInternal.getAltLink(coll));

                Assertions.assertThat(globalSessionToken1.indexOf(document.getSessionToken())).isNotNegative();
                Assertions.assertThat(globalSessionToken2.indexOf(document.getSessionToken())).isNotNegative();
            }
        } finally {
            safeClose(writeClient);
        }
    }

    boolean validateConsistentPrefix(Resource resourceToWorkWith) throws InterruptedException {
        int numberOfTestIteration = 5;
        Instant lastReadDateTime = resourceToWorkWith.getTimestamp();
        boolean readLagging = false;
        Resource writeResource = resourceToWorkWith;

        while (numberOfTestIteration-- > 0) { //Write from a client and do point read through second client and ensure TS monotonically increases.
            Instant sourceTimestamp = writeResource.getTimestamp();
            Thread.sleep(1000); //Timestamp is in granularity of seconds.
            Resource updatedResource = null;
            if (resourceToWorkWith instanceof User) {
                updatedResource = this.writeClient.upsertUser(createdDatabase.getSelfLink(), (User) writeResource,
                                                              null)
                        .block()
                        .getResource();
            } else if (resourceToWorkWith instanceof Document) {
                updatedResource = this.writeClient.upsertDocument(createdCollection.getSelfLink(),
                                                                  (Document) writeResource, null, false)
                        .block()
                        .getResource();
            }
            assertThat(updatedResource.getTimestamp().isAfter(sourceTimestamp)).isTrue();
            writeResource = updatedResource;

            Resource readResource = null;
            if (resourceToWorkWith instanceof User) {
                readResource = this.readClient.readUser(resourceToWorkWith.getSelfLink(), null)
                        .block()
                        .getResource();
            } else if (resourceToWorkWith instanceof Document) {
                RequestOptions options = new RequestOptions();
                options.setPartitionKey(new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(resourceToWorkWith, "mypk")));
                readResource = this.readClient.readDocument(resourceToWorkWith.getSelfLink(), options)
                        .block()
                        .getResource();
            }
            assertThat(readResource.getTimestamp().compareTo(lastReadDateTime) >= 0).isTrue();
            lastReadDateTime = readResource.getTimestamp();
            if (readResource.getTimestamp().isBefore(updatedResource.getTimestamp())) {
                readLagging = true;
            }
        }
        return readLagging;
    }

    boolean validateReadSession(Resource resourceToWorkWith) throws InterruptedException {
        int numberOfTestIteration = 5;
        Instant lastReadDateTime = Instant.MIN;
        boolean readLagging = false;
        Resource writeResource = resourceToWorkWith;

        while (numberOfTestIteration-- > 0) {
            Instant sourceTimestamp = writeResource.getTimestamp();
            Thread.sleep(1000);
            Resource updatedResource = null;
            if (resourceToWorkWith instanceof Document) {
                updatedResource = this.writeClient.upsertDocument(createdCollection.getSelfLink(), writeResource,
                                                                  null, false)
                        .block()
                        .getResource();
            }
            assertThat(updatedResource.getTimestamp().isAfter(sourceTimestamp)).isTrue();
            writeResource = updatedResource;

            Resource readResource = null;
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.setPartitionKey(new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(resourceToWorkWith, "mypk")));
            if (resourceToWorkWith instanceof Document) {
                readResource = this.readClient.readDocument(resourceToWorkWith.getSelfLink(), requestOptions).block().getResource();
            }
            assertThat(readResource.getTimestamp().compareTo(lastReadDateTime) >= 0).isTrue();
            lastReadDateTime = readResource.getTimestamp();

            if (readResource.getTimestamp().isBefore(updatedResource.getTimestamp())) {
                readLagging = true;
            }
        }
        return readLagging;
    }

    boolean validateWriteSession(Resource resourceToWorkWith) throws InterruptedException {
        int numberOfTestIteration = 5;
        Instant lastReadDateTime = Instant.MIN;
        boolean readLagging = false;
        Resource writeResource = resourceToWorkWith;

        while (numberOfTestIteration-- > 0) {
            Instant sourceTimestamp = writeResource.getTimestamp();
            Thread.sleep(1000);
            Resource updatedResource = null;
            if (resourceToWorkWith instanceof Document) {
                updatedResource = this.writeClient.upsertDocument(createdCollection.getSelfLink(), writeResource, null, false).block().getResource();
            }
            assertThat(updatedResource.getTimestamp().isAfter(sourceTimestamp)).isTrue();
            writeResource = updatedResource;

            Resource readResource = null;
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.setPartitionKey(new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(resourceToWorkWith, "mypk")));
            if (resourceToWorkWith instanceof Document) {
                readResource =
                        this.readClient.readDocument(resourceToWorkWith.getSelfLink(), requestOptions)
                                .block()
                                .getResource();
            }
            assertThat(readResource.getTimestamp().compareTo(lastReadDateTime) >= 0).isTrue();
            lastReadDateTime = readResource.getTimestamp();

            if (readResource.getTimestamp().isBefore(updatedResource.getTimestamp())) {
                readLagging = true;
            }

            //Now perform write on session and update our session token and lastReadTS
            Thread.sleep(1000);
            if (resourceToWorkWith instanceof Document) {
                readResource = this.writeClient.upsertDocument(createdCollection.getSelfLink(), readResource,
                                                               requestOptions, false)
                        .block()
                        .getResource();
                //Now perform write on session
            }
            assertThat(readResource.getTimestamp().isAfter(lastReadDateTime));

            this.readClient.setSession(this.writeClient.getSession());
        }
        return readLagging;
    }

    void validateSessionContainerAfterCollectionDeletion(boolean useGateway) throws Exception {
        ConnectionPolicy connectionPolicy;
        if (useGateway) {
            connectionPolicy = new ConnectionPolicy(GatewayConnectionConfig.getDefaultConfig());
        } else {
            connectionPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());
        }
        RxDocumentClientImpl client1 = (RxDocumentClientImpl) new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.SESSION).withContentResponseOnWriteEnabled(true)
                .build();
        RxDocumentClientImpl client2 = (RxDocumentClientImpl) new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.SESSION).withContentResponseOnWriteEnabled(true)
                .build();

        String collectionId = UUID.randomUUID().toString();
        try {
            DocumentCollection collectionDefinition = getCollectionDefinition();
            collectionDefinition.setId(collectionId);
            DocumentCollection collection = createCollection(client2, createdDatabase.getId(), collectionDefinition, null);
            ResourceResponseValidator<DocumentCollection> successValidatorCollection = new ResourceResponseValidator.Builder<DocumentCollection>()
                    .withId(collection.getId())
                    .build();
            Mono<ResourceResponse<DocumentCollection>> readObservableCollection = client2.readCollection(collection.getSelfLink(), null);
            validateSuccess(readObservableCollection, successValidatorCollection);

            for (int i = 0; i < 5; i++) {
                String documentId = "Generation1-" + i;
                Document documentDefinition = getDocumentDefinition();
                documentDefinition.setId(documentId);
                Document documentCreated = client2.createDocument(collection.getSelfLink(), documentDefinition, null, true).block().getResource();
                RequestOptions requestOptions = new RequestOptions();
                requestOptions.setPartitionKey(new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(documentCreated, "mypk")));
                client2.readDocument(BridgeInternal.getAltLink(documentCreated), requestOptions).block();
                client2.readDocument(documentCreated.getSelfLink(), requestOptions).block();
            }

            {
                // just create the second for fun
                DocumentCollection collection2 = createCollection(client2, createdDatabase.getId(), getCollectionDefinition(), null);
                successValidatorCollection = new ResourceResponseValidator.Builder<DocumentCollection>()
                        .withId(collection2.getId())
                        .build();
                readObservableCollection = client2.readCollection(collection2.getSelfLink(), null);
                validateSuccess(readObservableCollection, successValidatorCollection);
            }
            // verify the client2 has the same token.
            {
                String token1 = ((SessionContainer) client2.getSession()).getSessionToken(BridgeInternal.getAltLink(collection));
                String token2 = ((SessionContainer) client2.getSession()).getSessionToken(collection.getSelfLink());
                assertThat(token1).isEqualTo(token2);
            }

            // now delete collection use different client
            client1.deleteCollection(collection.getSelfLink(), null).block();

            DocumentCollection collectionRandom1 = createCollection(client2, createdDatabase.getId(), getCollectionDefinition());
            DocumentCollection documentCollection = getCollectionDefinition();
            collectionDefinition.setId(collectionId);
            DocumentCollection collectionSameName = createCollection(client2, createdDatabase.getId(), collectionDefinition);
            String documentId1 = "Generation2-" + 0;
            Document databaseDefinition2 = getDocumentDefinition();
            databaseDefinition2.setId(documentId1);
            Document createdDocument = client1.createDocument(collectionSameName.getSelfLink(), databaseDefinition2, null, true).block().getResource();
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.setPartitionKey(new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(createdDocument, "mypk")));
            ResourceResponseValidator<Document> successValidator = new ResourceResponseValidator.Builder<Document>()
                    .withId(createdDocument.getId())
                    .build();
            Mono<ResourceResponse<Document>> readObservable = client1.readDocument(createdDocument.getSelfLink(), requestOptions);
            validateSuccess(readObservable, successValidator);
            {
                String token1 = ((SessionContainer) client1.getSession()).getSessionToken(BridgeInternal.getAltLink(collectionSameName));
                String token2 = ((SessionContainer) client1.getSession()).getSessionToken(collectionSameName.getSelfLink());
                assertThat(token1).isEqualTo(token2);
            }

            {
                // Client2 read using getName link should fail with higher LSN.
                String token = ((SessionContainer) client1.getSession()).getSessionToken(collectionSameName.getSelfLink());
                // artificially bump to higher LSN
                String higherLsnToken = this.getDifferentLSNToken(token, 2000);
                RequestOptions requestOptions1 = new RequestOptions();
                requestOptions1.setSessionToken(higherLsnToken);
                requestOptions1.setPartitionKey(new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(createdDocument, "mypk")));
                readObservable = client2.readDocument(BridgeInternal.getAltLink(createdDocument), requestOptions1);
                FailureValidator failureValidator = new FailureValidator.Builder().subStatusCode(1002).build();
                validateFailure(readObservable, failureValidator);
            }
            // this will trigger client2 to clear the token
            {
                // verify token by altlink is gone!
                String token1 = ((SessionContainer) client2.getSession()).getSessionToken(BridgeInternal.getAltLink(collectionSameName));
                String token2 = ((SessionContainer) client2.getSession()).getSessionToken(collection.getSelfLink());
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
                        client1.createDocument(BridgeInternal.getAltLink(collectionSameName), getDocumentDefinition(), null, true).block().getResource();
                RequestOptions options = new RequestOptions();
                options.setPartitionKey(new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(documentTest, "mypk")));
                successValidator = new ResourceResponseValidator.Builder<Document>()
                        .withId(documentTest.getId())
                        .build();
                readObservable = client1.readDocument(documentTest.getSelfLink(), options);
                validateSuccess(readObservable, successValidator);

                client1.deleteCollection(collectionSameName.getSelfLink(), null).block();
                String token1 = ((SessionContainer) client2.getSession()).getSessionToken(BridgeInternal.getAltLink(collectionSameName));
                String token2 = ((SessionContainer) client2.getSession()).getSessionToken(collectionSameName.getSelfLink());
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
        ConnectionPolicy connectionPolicy;
        if (useGateway) {
            connectionPolicy = new ConnectionPolicy(GatewayConnectionConfig.getDefaultConfig());
        } else {
            connectionPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());
        }
        RxDocumentClientImpl writeClient = (RxDocumentClientImpl) new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.SESSION).withContentResponseOnWriteEnabled(true)
                .build();
        RxDocumentClientImpl validationClient = (RxDocumentClientImpl) new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.SESSION).withContentResponseOnWriteEnabled(true)
                .build();
        try {
            // write a document, and upsert to it to update etag.
            ResourceResponse<Document> documentResponse = writeClient.createDocument(BridgeInternal.getAltLink(createdCollection), getDocumentDefinition(), null, true).block();
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.setPartitionKey(new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(documentResponse.getResource(), "mypk")));
            ResourceResponse<Document> upsertResponse =
                    writeClient.upsertDocument(BridgeInternal.getAltLink(createdCollection), documentResponse.getResource(), requestOptions, true).block();

            // create a conditioned read request, with first write request's etag, so the read fails with PreconditionFailure
            RequestOptions requestOptions1 = new RequestOptions();
            requestOptions.setPartitionKey(new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(documentResponse.getResource(), "mypk")));
            requestOptions1.setIfMatchETag(documentResponse.getResource().getETag());
            Mono<ResourceResponse<Document>> preConditionFailureResponseObservable = validationClient.upsertDocument(BridgeInternal.getAltLink(createdCollection),
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
        ConnectionPolicy connectionPolicy;
        if (useGateway) {
            connectionPolicy = new ConnectionPolicy(GatewayConnectionConfig.getDefaultConfig());
        } else {
            connectionPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());
        }
        RxDocumentClientImpl writeClient = (RxDocumentClientImpl) new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.SESSION).withContentResponseOnWriteEnabled(true)
                .build();
        RxDocumentClientImpl validationClient = (RxDocumentClientImpl) new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.SESSION).withContentResponseOnWriteEnabled(true)
                .build();
        try {
            DocumentCollection collectionDefinition = getCollectionDefinition();
            collectionDefinition.setId("TestCollection");

            ResourceResponse<Document> documentResponse = writeClient.createDocument(BridgeInternal.getAltLink(createdCollection), getDocumentDefinition(), null, true).block();

            FailureValidator failureValidator = new FailureValidator.Builder().statusCode(HttpConstants.StatusCodes.NOTFOUND).build();
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.setPartitionKey(new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(documentResponse.getResource(), "mypk")));
            // try to read a non existent document in the same partition that we previously wrote to
            Mono<ResourceResponse<Document>> readObservable = validationClient.readDocument(BridgeInternal.getAltLink(documentResponse.getResource()) + "dummy", requestOptions);
            validateFailure(readObservable, failureValidator);
            assertThat(isSessionEqual(((SessionContainer) validationClient.getSession()), (SessionContainer) writeClient.getSession())).isTrue();
        } finally {
            safeClose(writeClient);
            safeClose(validationClient);
        }
    }

    void validateSessionTokenWithExpectedException(boolean useGateway) throws Exception {
        ConnectionPolicy connectionPolicy;
        if (useGateway) {
            connectionPolicy = new ConnectionPolicy(GatewayConnectionConfig.getDefaultConfig());
        } else {
            connectionPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());
        }
        RxDocumentClientImpl writeClient = (RxDocumentClientImpl) new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.SESSION).withContentResponseOnWriteEnabled(true)
                .build();
        try {
            ResourceResponse<Document> documentResponse =
                    writeClient.createDocument(BridgeInternal.getAltLink(createdCollection), getDocumentDefinition(), null, false).block();
            String token = documentResponse.getResponseHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN);

            // artificially bump to higher LSN
            String higherLsnToken = this.getDifferentLSNToken(token, 2000);
            FailureValidator failureValidator = new FailureValidator.Builder().subStatusCode(HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE).build();
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.setPartitionKey(new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(documentResponse.getResource(), "mypk")));
            requestOptions.setSessionToken(higherLsnToken);
            // try to read a non existent document in the same partition that we previously wrote to
            Mono<ResourceResponse<Document>> readObservable = writeClient.readDocument(BridgeInternal.getAltLink(documentResponse.getResource()),
                    requestOptions);
            validateFailure(readObservable, failureValidator);

        } finally {
            safeClose(writeClient);
        }
    }

    void validateSessionTokenWithConflictException(boolean useGateway) {
        ConnectionPolicy connectionPolicy;
        if (useGateway) {
            connectionPolicy = new ConnectionPolicy(GatewayConnectionConfig.getDefaultConfig());
        } else {
            connectionPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());
        }
        RxDocumentClientImpl writeClient = (RxDocumentClientImpl) new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.SESSION).withContentResponseOnWriteEnabled(true)
                .build();
        RxDocumentClientImpl validationClient = (RxDocumentClientImpl) new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.SESSION).withContentResponseOnWriteEnabled(true)
                .build();
        try {
            Document documentDefinition = getDocumentDefinition();
            ResourceResponse<Document> documentResponse =
                    writeClient.createDocument(BridgeInternal.getAltLink(createdCollection), documentDefinition, null, true).block();

            FailureValidator failureValidator = new FailureValidator.Builder().statusCode(HttpConstants.StatusCodes.CONFLICT).build();
            Mono<ResourceResponse<Document>> conflictDocumentResponse = validationClient.createDocument(BridgeInternal.getAltLink(createdCollection),
                    documentDefinition, null,
                    true);
            validateFailure(conflictDocumentResponse, failureValidator);
        } finally {
            safeClose(writeClient);
            safeClose(validationClient);
        }
    }

    void validateSessionTokenMultiPartitionCollection(boolean useGateway) throws Exception {
        ConnectionPolicy connectionPolicy;
        if (useGateway) {
            connectionPolicy = new ConnectionPolicy(GatewayConnectionConfig.getDefaultConfig());
        } else {
            connectionPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());
        }
        RxDocumentClientImpl writeClient = (RxDocumentClientImpl) new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.SESSION).withContentResponseOnWriteEnabled(true)
                .build();
        try {

            Range<String> fullRange = new Range<String>(PartitionKeyInternalHelper.MinimumInclusiveEffectivePartitionKey,
                    PartitionKeyInternalHelper.MaximumExclusiveEffectivePartitionKey, true, false);

            IRoutingMapProvider routingMapProvider = writeClient.getPartitionKeyRangeCache();
            // assertThat(routingMapProvider.tryGetOverlappingRangesAsync(collection.resourceId(), fullRange, false).toBlocking().value().size()).isEqualTo(5);

            // Document to lock pause/resume clients
            Document document1 = new Document();
            document1.setId("test" + UUID.randomUUID().toString());
            BridgeInternal.setProperty(document1, "mypk", 1);
            ResourceResponse<Document> childResource1 = writeClient.createDocument(createdCollection.getSelfLink(), document1, null, true).block();
            logger.info("Created {} child resource", childResource1.getResource().getResourceId());
            assertThat(childResource1.getSessionToken()).isNotNull();
            assertThat(childResource1.getSessionToken().contains(":")).isTrue();
            String globalSessionToken1 = ((SessionContainer) writeClient.getSession()).getSessionToken(createdCollection.getSelfLink());
            assertThat(globalSessionToken1.contains(childResource1.getSessionToken()));

            // Document to lock pause/resume clients
            Document document2 = new Document();
            document2.setId("test" + UUID.randomUUID().toString());
            BridgeInternal.setProperty(document2, "mypk", 2);
            ResourceResponse<Document> childResource2 = writeClient.createDocument(createdCollection.getSelfLink(), document2, null, true).block();
            assertThat(childResource2.getSessionToken()).isNotNull();
            assertThat(childResource2.getSessionToken().contains(":")).isTrue();
            String globalSessionToken2 = ((SessionContainer) writeClient.getSession()).getSessionToken(createdCollection.getSelfLink());
            logger.info("globalsessiontoken2 {}, childtoken1 {}, childtoken2 {}", globalSessionToken2, childResource1.getSessionToken(), childResource2.getSessionToken());
            assertThat(globalSessionToken2.contains(childResource2.getSessionToken())).isTrue();

            // this token can read childresource2 but not childresource1
            String sessionToken =
                    StringUtils.split(childResource1.getSessionToken(), ':')[0] + ":" + createSessionToken(SessionTokenHelper.parse(childResource1.getSessionToken()), 100000000).convertToString() + "," + childResource2.getSessionToken();

            RequestOptions option = new RequestOptions();
            option.setSessionToken(sessionToken);
            option.setPartitionKey(new PartitionKey(2));
            writeClient.readDocument(childResource2.getResource().getSelfLink(), option).block();

            option = new RequestOptions();
            option.setSessionToken(StringUtils.EMPTY);
            option.setPartitionKey(new PartitionKey(1));
            writeClient.readDocument(childResource1.getResource().getSelfLink(), option).block();

            option = new RequestOptions();
            option.setSessionToken(sessionToken);
            option.setPartitionKey(new PartitionKey(1));
            Mono<ResourceResponse<Document>> readObservable = writeClient.readDocument(childResource1.getResource().getSelfLink(), option);
            FailureValidator failureValidator =
                    new FailureValidator.Builder().statusCode(HttpConstants.StatusCodes.NOTFOUND).subStatusCode(HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE).build();
            validateFailure(readObservable, failureValidator);

            readObservable = writeClient.readDocument(childResource2.getResource().getSelfLink(), option);
            failureValidator =
                    new FailureValidator.Builder().statusCode(HttpConstants.StatusCodes.NOTFOUND).subStatusCode(HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE).build();
            validateFailure(readObservable, failureValidator);

            assertThat(((SessionContainer) writeClient.getSession()).getSessionToken(createdCollection.getSelfLink())).isEqualTo
                    (((SessionContainer) writeClient.getSession()).getSessionToken(BridgeInternal.getAltLink(createdCollection)));

            assertThat(((SessionContainer) writeClient.getSession()).getSessionToken("asdfasdf")).isEmpty();
            assertThat(((SessionContainer) writeClient.getSession()).getSessionToken(createdDatabase.getSelfLink())).isEmpty();
        } finally {
            safeClose(writeClient);
        }
    }

    void validateSessionTokenFromCollectionReplaceIsServerToken(boolean useGateway) {
        ConnectionPolicy connectionPolicy;
        if (useGateway) {
            connectionPolicy = new ConnectionPolicy(GatewayConnectionConfig.getDefaultConfig());
        } else {
            connectionPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());
        }
        RxDocumentClientImpl client1 = (RxDocumentClientImpl) new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.SESSION).withContentResponseOnWriteEnabled(true)
                .build();
        RxDocumentClientImpl client2 = null;
        try {
            Document doc = client1.createDocument(createdCollection.getSelfLink(), getDocumentDefinition(), null, true).block().getResource();
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.setPartitionKey(new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(doc, "mypk")));
            Document doc1 = client1.readDocument(BridgeInternal.getAltLink(doc), requestOptions).block().getResource();

            String token1 = ((SessionContainer) client1.getSession()).getSessionToken(createdCollection.getSelfLink());
            client2 = (RxDocumentClientImpl) new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                    .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                    .withConnectionPolicy(connectionPolicy)
                    .withConsistencyLevel(ConsistencyLevel.SESSION).withContentResponseOnWriteEnabled(true)
                    .build();
            client2.replaceCollection(createdCollection, null).block();
            String token2 = ((SessionContainer) client2.getSession()).getSessionToken(createdCollection.getSelfLink());

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

    @SuppressWarnings({"unchecked", "rawtypes"})
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

    @SuppressWarnings({"unchecked", "rawtypes"})
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
