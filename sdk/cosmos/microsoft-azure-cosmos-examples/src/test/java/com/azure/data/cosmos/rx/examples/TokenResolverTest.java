// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.rx.examples;

import com.azure.data.cosmos.internal.AsyncDocumentClient;
import com.azure.data.cosmos.ConnectionMode;
import com.azure.data.cosmos.ConnectionPolicy;
import com.azure.data.cosmos.ConsistencyLevel;
import com.azure.data.cosmos.CosmosResourceType;
import com.azure.data.cosmos.internal.Database;
import com.azure.data.cosmos.internal.Document;
import com.azure.data.cosmos.DocumentClientTest;
import com.azure.data.cosmos.internal.DocumentCollection;
import com.azure.data.cosmos.PartitionKey;
import com.azure.data.cosmos.PartitionKeyDefinition;
import com.azure.data.cosmos.internal.Permission;
import com.azure.data.cosmos.PermissionMode;
import com.azure.data.cosmos.internal.RequestOptions;
import com.azure.data.cosmos.internal.ResourceResponse;
import com.azure.data.cosmos.TokenResolver;
import com.azure.data.cosmos.internal.User;
import com.google.common.collect.ImmutableMap;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

public class TokenResolverTest extends DocumentClientTest {

    private final static int TIMEOUT = 180000;
    private final static String USER_ID = "userId";
    private AsyncDocumentClient client;
    private Database createdDatabase;
    private DocumentCollection createdCollection;
    private Map<String, String> userToReadOnlyResourceTokenMap = new HashMap<>();
    private Map<String, String> documentToReadUserMap = new HashMap<>();

    private Map<String, String> documentToReadWriteUserMap = new HashMap<>();
    private Map<String, String> userToReadWriteResourceTokenMap = new HashMap<>();


    /**
     * This Example walks you through how to use a token resolver to
     * control authorization and access to Cosmos DB resources.
     */
    @BeforeClass(groups = "samples", timeOut = TIMEOUT)
    public void setUp() {

        ConnectionPolicy connectionPolicy = new ConnectionPolicy().connectionMode(ConnectionMode.DIRECT);

        this.clientBuilder()
            .withServiceEndpoint(TestConfigurations.HOST)
            .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
            .withConnectionPolicy(connectionPolicy)
            .withConsistencyLevel(ConsistencyLevel.SESSION);

        this.client = this.clientBuilder().build();

        DocumentCollection collectionDefinition = new DocumentCollection();
        collectionDefinition.id(UUID.randomUUID().toString());
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<String>();
        paths.add("/mypk");
        partitionKeyDef.paths(paths);
        collectionDefinition.setPartitionKey(partitionKeyDef);

        // CREATE database
        createdDatabase = Utils.createDatabaseForTest(client);

        // CREATE collection
        createdCollection = client
                .createCollection("dbs/" + createdDatabase.id(), collectionDefinition, null)
                .single().block().getResource();

        for (int i = 0; i < 10; i++) {
            // CREATE a document
            Document documentDefinition = new Document();
            documentDefinition.id(UUID.randomUUID().toString());
            Document createdDocument = client.createDocument(createdCollection.selfLink(), documentDefinition, null, true).blockFirst().getResource();

            // CREATE a User who is meant to only read this document
            User readUserDefinition = new User();
            readUserDefinition.id(UUID.randomUUID().toString());
            User createdReadUser = client.createUser(createdDatabase.selfLink(), readUserDefinition, null).blockFirst().getResource();

            // CREATE a read only permission for  the above document
            Permission readOnlyPermissionDefinition = new Permission();
            readOnlyPermissionDefinition.id(UUID.randomUUID().toString());
            readOnlyPermissionDefinition.setResourceLink(createdDocument.selfLink());
            readOnlyPermissionDefinition.setPermissionMode(PermissionMode.READ);

            // Assign the permission to the above user
            Permission readOnlyCreatedPermission = client.createPermission(createdReadUser.selfLink(), readOnlyPermissionDefinition, null).blockFirst().getResource();
            userToReadOnlyResourceTokenMap.put(createdReadUser.id(), readOnlyCreatedPermission.getToken());

            documentToReadUserMap.put(createdDocument.selfLink(), createdReadUser.id());

            // CREATE a User who can both read and write this document
            User readWriteUserDefinition = new User();
            readWriteUserDefinition.id(UUID.randomUUID().toString());
            User createdReadWriteUser = client.createUser(createdDatabase.selfLink(), readWriteUserDefinition, null).blockFirst().getResource();

            // CREATE a read/write permission for the above document
            Permission readWritePermissionDefinition = new Permission();
            readWritePermissionDefinition.id(UUID.randomUUID().toString());
            readWritePermissionDefinition.setResourceLink(createdDocument.selfLink());
            readWritePermissionDefinition.setPermissionMode(PermissionMode.ALL);

            // Assign the permission to the above user
            Permission readWriteCreatedPermission = client.createPermission(createdReadWriteUser.selfLink(), readWritePermissionDefinition, null).blockFirst().getResource();
            userToReadWriteResourceTokenMap.put(createdReadWriteUser.id(), readWriteCreatedPermission.getToken());

            documentToReadWriteUserMap.put(createdDocument.selfLink(), createdReadWriteUser.id());
        }
    }

    /**
     * READ a document with a user having read permission
     */
    @Test(groups = "samples", timeOut = TIMEOUT)
    public void readDocumentThroughTokenResolver() throws Exception {
        AsyncDocumentClient asyncClientWithTokenResolver = null;
        try {
            ConnectionPolicy connectionPolicy = new ConnectionPolicy();
            connectionPolicy.connectionMode(ConnectionMode.DIRECT);
            asyncClientWithTokenResolver = new AsyncDocumentClient.Builder()
                    .withServiceEndpoint(TestConfigurations.HOST)
                    .withConnectionPolicy(connectionPolicy)
                    .withConsistencyLevel(ConsistencyLevel.SESSION)
                    .withTokenResolver(getTokenResolverForRead())
                    .build();
            List<ResourceResponse<Document>> capturedResponse = Collections
                    .synchronizedList(new ArrayList<>());
            for (String documentLink : documentToReadUserMap.keySet()) {
                
                // Each document has one User who can only read it. Pass that User Id in the item.
                // The token resolver will resolve the token for that User based on 'userId'.
                ImmutableMap<String, Object> properties = ImmutableMap.<String, Object> builder()
                        .put(USER_ID, documentToReadUserMap.get(documentLink))
                        .build();
                RequestOptions requestOptions = new RequestOptions();
                requestOptions.setProperties(properties);
                requestOptions.setPartitionKey(PartitionKey.None);
                Flux<ResourceResponse<Document>> readDocumentObservable = asyncClientWithTokenResolver
                        .readDocument(documentLink, requestOptions);
                readDocumentObservable.collectList().block().forEach(capturedResponse::add);
            }
            System.out.println("capturedResponse.size() = " + capturedResponse.size());
            assertThat(capturedResponse, hasSize(10));
        } finally {
            Utils.safeClose(asyncClientWithTokenResolver);
        }
    }

    /**
     * DELETE a document with a user having all permission
     */
    @Test(groups = "samples", timeOut = TIMEOUT, dependsOnMethods = {"readDocumentThroughTokenResolver"})
    public void deleteDocumentThroughTokenResolver() throws Exception {
        AsyncDocumentClient asyncClientWithTokenResolver = null;
        try {
            ConnectionPolicy connectionPolicy = new ConnectionPolicy();
            connectionPolicy.connectionMode(ConnectionMode.DIRECT);
            asyncClientWithTokenResolver = new AsyncDocumentClient.Builder()
                    .withServiceEndpoint(TestConfigurations.HOST)
                    .withConnectionPolicy(connectionPolicy)
                    .withConsistencyLevel(ConsistencyLevel.SESSION)
                    .withTokenResolver(getTokenResolverForReadWrite())
                    .build();
            List<ResourceResponse<Document>> capturedResponse = Collections
                    .synchronizedList(new ArrayList<>());
            for (String documentLink : documentToReadWriteUserMap.keySet()) {
                
                // Each document has one User who can read and write it. Pass that User Id in the item.
                // The token resolver will resolve the token for that User based on 'userId'.
                ImmutableMap<String, Object> properties = ImmutableMap.<String, Object> builder()
                        .put(USER_ID, documentToReadWriteUserMap.get(documentLink))
                        .build();

                RequestOptions requestOptions = new RequestOptions();
                requestOptions.setProperties(properties);
                requestOptions.setPartitionKey(PartitionKey.None);
                Flux<ResourceResponse<Document>> readDocumentObservable = asyncClientWithTokenResolver
                        .deleteDocument(documentLink, requestOptions);
                readDocumentObservable.collectList().block().forEach(capturedResponse::add);
            }
            assertThat(capturedResponse, hasSize(10));
        } finally {
            Utils.safeClose(asyncClientWithTokenResolver);
        }
    }

    /**
     * Block list an user and throw error from token resolver
     */
    @Test(groups = "samples", timeOut = TIMEOUT)
    public void blockListUserThroughTokenResolver() throws Exception {
        String blockListedUserId = "block listed user";
        String errorMessage = "block listed user! access denied!";

        AsyncDocumentClient asyncClientWithTokenResolver = null;

        try {
            ConnectionPolicy connectionPolicy = new ConnectionPolicy();
            connectionPolicy.connectionMode(ConnectionMode.DIRECT);
            asyncClientWithTokenResolver = new AsyncDocumentClient.Builder()
                    .withServiceEndpoint(TestConfigurations.HOST)
                    .withConnectionPolicy(connectionPolicy)
                    .withConsistencyLevel(ConsistencyLevel.SESSION)
                    .withTokenResolver(getTokenResolverWithBlockList(blockListedUserId, errorMessage))
                    .build();
            
            // READ a document using a block listed user, passing the 'userId' in the item.
            // Token resolver will throw RuntimeException.
            RequestOptions options = new RequestOptions();
            ImmutableMap<String, Object> properties = ImmutableMap.<String, Object> builder()
                    .put(USER_ID, blockListedUserId)
                    .build();

            options.setProperties(properties);
            Flux<ResourceResponse<DocumentCollection>> readObservable = asyncClientWithTokenResolver.readCollection(createdCollection.selfLink(), options);
            List<Throwable> capturedErrors = Collections
                    .synchronizedList(new ArrayList<>());
            readObservable.subscribe(response -> {}, throwable -> capturedErrors.add(throwable));
            Thread.sleep(4000);
            assertThat(capturedErrors, hasSize(1));
            assertThat(capturedErrors.get(0), instanceOf(RuntimeException.class));
            assertThat(capturedErrors.get(0).getMessage(), equalTo(errorMessage));
            
            // READ a document using a valid user, passing the 'userId' in the item.
            // Token resolver will pass on the correct token for authentication.
            String validUserId = userToReadWriteResourceTokenMap.keySet().iterator().next();
            System.out.println(validUserId);
            properties = ImmutableMap.<String, Object> builder()
                    .put(USER_ID, validUserId)
                    .build();
            options.setProperties(properties);
            readObservable = asyncClientWithTokenResolver.readCollection(createdCollection.selfLink(), options);
            List<DocumentCollection> capturedResponse = Collections
                    .synchronizedList(new ArrayList<>());
            readObservable.subscribe(resourceResponse -> capturedResponse.add(resourceResponse.getResource()), error -> error.printStackTrace());
            Thread.sleep(4000);
            assertThat(capturedErrors, hasSize(1));
            assertThat(capturedResponse.get(0).id(), equalTo(createdCollection.id()));
        } finally {
            Utils.safeClose(asyncClientWithTokenResolver);
        }
    }
    
    /**
     * For Reading DatabaseAccount on client initialization, use any User's token.
     * For subsequent Reads, get the correct read only token based on 'userId'.
     */
    private TokenResolver getTokenResolverForRead() {
        return (String requestVerb, String resourceIdOrFullName, CosmosResourceType resourceType, Map<String, Object>  properties) -> {
            if (resourceType.equals(CosmosResourceType.System)) {
                //Choose any token it should have the read access on database account
                for (String token : userToReadOnlyResourceTokenMap.values()) {
                    return token;
                }
            } else {
                return userToReadOnlyResourceTokenMap.get(properties.get(USER_ID));
            }
            return null;
        };
    }

    /**
     * For Reading DatabaseAccount on client initialization, use any User's token.
     * For subsequent Reads/Writes, get the correct read/write token based on 'userId'.
     */
    private TokenResolver getTokenResolverForReadWrite() {
        return (String requestVerb, String resourceIdOrFullName, CosmosResourceType resourceType, Map<String, Object>  properties) -> {
            if (resourceType.equals(CosmosResourceType.System)) {
                //Choose any token it should have the read access on database account
                for (String token : userToReadWriteResourceTokenMap.values()) {
                    return token;
                }
            } else {
                return userToReadWriteResourceTokenMap.get(properties.get(USER_ID));
            }
            return null;
        };
    }

    /**
     * For Reading DatabaseAccount on client initialization, use any User's token.
     * For subsequent Reads, get the correct read/write token based on 'userId',
     * only if user is not block listed. In this scenario, the block listed user id
     * is compared to the current user's id, passed into the item for the request.
     */
    private TokenResolver getTokenResolverWithBlockList(String blockListedUserId, String errorMessage) {
        return (String requestVerb, String resourceIdOrFullName, CosmosResourceType resourceType, Map<String, Object> properties) -> {
            if (resourceType == CosmosResourceType.System) {
                return userToReadWriteResourceTokenMap.values().iterator().next();
            } else if (!properties.get(USER_ID).toString().equals(blockListedUserId)) {
                return userToReadWriteResourceTokenMap.get(properties.get(USER_ID));
            } else {
                throw new RuntimeException(errorMessage);
            }
        };
    }

    @AfterClass(groups = "samples", timeOut = TIMEOUT)
    public void shutdown() {
        Utils.safeClean(client, createdDatabase);
        Utils.safeClose(client);
    }
}
