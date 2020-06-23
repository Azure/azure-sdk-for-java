// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.rx.examples;

import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.implementation.RequestVerb;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.implementation.CosmosResourceType;
import com.azure.cosmos.implementation.Database;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.DocumentClientTest;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.implementation.Permission;
import com.azure.cosmos.models.PermissionMode;
import com.azure.cosmos.implementation.RequestOptions;
import com.azure.cosmos.implementation.ResourceResponse;
import com.azure.cosmos.implementation.CosmosAuthorizationTokenResolver;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.User;
import com.google.common.collect.ImmutableMap;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Ignore;
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

@Ignore("CosmosAuthorizationTokenResolver is removed from public")
public class CosmosAuthorizationTokenResolverTest extends DocumentClientTest {

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
    public void before_TokenResolverTest() {

        ConnectionPolicy connectionPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());

        this.clientBuilder()
            .withServiceEndpoint(TestConfigurations.HOST)
            .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
            .withConnectionPolicy(connectionPolicy)
            .withConsistencyLevel(ConsistencyLevel.SESSION)
            .withContentResponseOnWriteEnabled(true);

        this.client = this.clientBuilder().build();

        DocumentCollection collectionDefinition = new DocumentCollection();
        collectionDefinition.setId(UUID.randomUUID().toString());
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<String>();
        paths.add("/mypk");
        partitionKeyDef.setPaths(paths);
        collectionDefinition.setPartitionKey(partitionKeyDef);

        // CREATE getDatabase
        createdDatabase = Utils.createDatabaseForTest(client);

        // CREATE collection
        createdCollection = client
                .createCollection("dbs/" + createdDatabase.getId(), collectionDefinition, null)
                .single().block().getResource();

        for (int i = 0; i < 10; i++) {
            // CREATE a document
            Document documentDefinition = new Document();
            documentDefinition.setId(UUID.randomUUID().toString());
            Document createdDocument = client.createDocument(createdCollection.getSelfLink(), documentDefinition, null, true).block().getResource();

            // CREATE a User who is meant to only read this document
            User readUserDefinition = new User();
            readUserDefinition.setId(UUID.randomUUID().toString());
            User createdReadUser = client.createUser(createdDatabase.getSelfLink(), readUserDefinition, null).block().getResource();

            // CREATE a read only getPermission for  the above document
            Permission readOnlyPermissionDefinition = new Permission();
            readOnlyPermissionDefinition.setId(UUID.randomUUID().toString());
            readOnlyPermissionDefinition.setResourceLink(createdDocument.getSelfLink());
            readOnlyPermissionDefinition.setPermissionMode(PermissionMode.READ);

            // Assign the getPermission to the above getUser
            Permission readOnlyCreatedPermission = client.createPermission(createdReadUser.getSelfLink(), readOnlyPermissionDefinition, null).block().getResource();
            userToReadOnlyResourceTokenMap.put(createdReadUser.getId(), readOnlyCreatedPermission.getToken());

            documentToReadUserMap.put(createdDocument.getSelfLink(), createdReadUser.getId());

            // CREATE a User who can both read and write this document
            User readWriteUserDefinition = new User();
            readWriteUserDefinition.setId(UUID.randomUUID().toString());
            User createdReadWriteUser = client.createUser(createdDatabase.getSelfLink(), readWriteUserDefinition, null).block().getResource();

            // CREATE a read/write permission for the above document
            Permission readWritePermissionDefinition = new Permission();
            readWritePermissionDefinition.setId(UUID.randomUUID().toString());
            readWritePermissionDefinition.setResourceLink(createdDocument.getSelfLink());
            readWritePermissionDefinition.setPermissionMode(PermissionMode.ALL);

            // Assign the getPermission to the above getUser
            Permission readWriteCreatedPermission = client.createPermission(createdReadWriteUser.getSelfLink(), readWritePermissionDefinition, null).block().getResource();
            userToReadWriteResourceTokenMap.put(createdReadWriteUser.getId(), readWriteCreatedPermission.getToken());

            documentToReadWriteUserMap.put(createdDocument.getSelfLink(), createdReadWriteUser.getId());
        }
    }

    /**
     * READ a document with a user having read permission
     */
    @Test(groups = "samples", timeOut = TIMEOUT)
    public void readDocumentThroughTokenResolver() throws Exception {
        AsyncDocumentClient asyncClientWithTokenResolver = null;
        try {
            ConnectionPolicy connectionPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());
            asyncClientWithTokenResolver = new AsyncDocumentClient.Builder()
                    .withServiceEndpoint(TestConfigurations.HOST)
                    .withConnectionPolicy(connectionPolicy)
                    .withConsistencyLevel(ConsistencyLevel.SESSION)
                    .withTokenResolver(getTokenResolverForRead())
                    .withContentResponseOnWriteEnabled(true)
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
                requestOptions.setPartitionKey(PartitionKey.NONE);
                Flux<ResourceResponse<Document>> readDocumentObservable = asyncClientWithTokenResolver
                        .readDocument(documentLink, requestOptions).flux();
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
            ConnectionPolicy connectionPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());
            asyncClientWithTokenResolver = new AsyncDocumentClient.Builder()
                    .withServiceEndpoint(TestConfigurations.HOST)
                    .withConnectionPolicy(connectionPolicy)
                    .withConsistencyLevel(ConsistencyLevel.SESSION)
                    .withTokenResolver(getTokenResolverForReadWrite())
                    .withContentResponseOnWriteEnabled(true)
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
                requestOptions.setPartitionKey(PartitionKey.NONE);
                Flux<ResourceResponse<Document>> readDocumentObservable = asyncClientWithTokenResolver
                        .deleteDocument(documentLink, requestOptions).flux();
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
            ConnectionPolicy connectionPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());
            asyncClientWithTokenResolver = new AsyncDocumentClient.Builder()
                    .withServiceEndpoint(TestConfigurations.HOST)
                    .withConnectionPolicy(connectionPolicy)
                    .withConsistencyLevel(ConsistencyLevel.SESSION)
                    .withTokenResolver(getTokenResolverWithBlockList(blockListedUserId, errorMessage))
                    .withContentResponseOnWriteEnabled(true)
                    .build();

            // READ a document using a block listed user, passing the 'userId' in the item.
            // Token resolver will throw RuntimeException.
            RequestOptions options = new RequestOptions();
            ImmutableMap<String, Object> properties = ImmutableMap.<String, Object> builder()
                    .put(USER_ID, blockListedUserId)
                    .build();

            options.setProperties(properties);
            Flux<ResourceResponse<DocumentCollection>> readObservable = asyncClientWithTokenResolver.readCollection(createdCollection.getSelfLink(), options).flux();
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
            readObservable = asyncClientWithTokenResolver.readCollection(createdCollection.getSelfLink(), options).flux();
            List<DocumentCollection> capturedResponse = Collections
                    .synchronizedList(new ArrayList<>());
            readObservable.subscribe(resourceResponse -> capturedResponse.add(resourceResponse.getResource()), error -> error.printStackTrace());
            Thread.sleep(4000);
            assertThat(capturedErrors, hasSize(1));
            assertThat(capturedResponse.get(0).getId(), equalTo(createdCollection.getId()));
        } finally {
            Utils.safeClose(asyncClientWithTokenResolver);
        }
    }

    /**
     * For Reading DatabaseAccount on client initialization, use any User's token.
     * For subsequent Reads, get the correct read only token based on 'userId'.
     */
    private CosmosAuthorizationTokenResolver getTokenResolverForRead() {
        return (RequestVerb requestVerb, String resourceIdOrFullName, CosmosResourceType resourceType, Map<String, Object>  properties) -> {
            if (resourceType.equals(CosmosResourceType.SYSTEM)) {
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
    private CosmosAuthorizationTokenResolver getTokenResolverForReadWrite() {
        return (RequestVerb requestVerb, String resourceIdOrFullName, CosmosResourceType resourceType, Map<String, Object>  properties) -> {
            if (resourceType.equals(CosmosResourceType.SYSTEM)) {
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
    private CosmosAuthorizationTokenResolver getTokenResolverWithBlockList(String blockListedUserId, String errorMessage) {
        return (RequestVerb requestVerb, String resourceIdOrFullName, CosmosResourceType resourceType, Map<String, Object> properties) -> {
            if (resourceType.equals(CosmosResourceType.SYSTEM)) {
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
