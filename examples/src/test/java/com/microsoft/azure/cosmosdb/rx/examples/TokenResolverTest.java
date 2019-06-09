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

package com.microsoft.azure.cosmosdb.rx.examples;

import com.google.common.collect.ImmutableMap;
import com.microsoft.azure.cosmosdb.ConnectionMode;
import com.microsoft.azure.cosmosdb.ConnectionPolicy;
import com.microsoft.azure.cosmosdb.ConsistencyLevel;
import com.microsoft.azure.cosmosdb.CosmosResourceType;
import com.microsoft.azure.cosmosdb.Database;
import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.PartitionKey;
import com.microsoft.azure.cosmosdb.PartitionKeyDefinition;
import com.microsoft.azure.cosmosdb.Permission;
import com.microsoft.azure.cosmosdb.PermissionMode;
import com.microsoft.azure.cosmosdb.RequestOptions;
import com.microsoft.azure.cosmosdb.ResourceResponse;
import com.microsoft.azure.cosmosdb.TokenResolver;
import com.microsoft.azure.cosmosdb.User;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import rx.Observable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

public class TokenResolverTest {
    private final static int TIMEOUT = 60000;
    private final static String USER_ID = "userId";
    private AsyncDocumentClient asyncClient;
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
        // Sets up the requirements for each test
        ConnectionPolicy connectionPolicy = new ConnectionPolicy();
        connectionPolicy.setConnectionMode(ConnectionMode.Direct);
        asyncClient = new AsyncDocumentClient.Builder()
                .withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.Session)
                .build();

        DocumentCollection collectionDefinition = new DocumentCollection();
        collectionDefinition.setId(UUID.randomUUID().toString());
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<String>();
        paths.add("/mypk");
        partitionKeyDef.setPaths(paths);
        collectionDefinition.setPartitionKey(partitionKeyDef);

        // Create database
        createdDatabase = Utils.createDatabaseForTest(asyncClient);

        // Create collection
        createdCollection = asyncClient
                .createCollection("dbs/" + createdDatabase.getId(), collectionDefinition, null)
                .toBlocking().single().getResource();

        for (int i = 0; i < 10; i++) {
            // Create a document
            Document documentDefinition = new Document();
            documentDefinition.setId(UUID.randomUUID().toString());
            Document createdDocument = asyncClient.createDocument(createdCollection.getSelfLink(), documentDefinition, null, true).toBlocking().first().getResource();

            // Create a User who is meant to only read this document
            User readUserDefinition = new User();
            readUserDefinition.setId(UUID.randomUUID().toString());
            User createdReadUser = asyncClient.createUser(createdDatabase.getSelfLink(), readUserDefinition, null).toBlocking().first().getResource();

            // Create a read only permission for  the above document
            Permission readOnlyPermissionDefinition = new Permission();
            readOnlyPermissionDefinition.setId(UUID.randomUUID().toString());
            readOnlyPermissionDefinition.setResourceLink(createdDocument.getSelfLink());
            readOnlyPermissionDefinition.setPermissionMode(PermissionMode.Read);

            // Assign the permission to the above user
            Permission readOnlyCreatedPermission = asyncClient.createPermission(createdReadUser.getSelfLink(), readOnlyPermissionDefinition, null).toBlocking().first().getResource();
            userToReadOnlyResourceTokenMap.put(createdReadUser.getId(), readOnlyCreatedPermission.getToken());

            documentToReadUserMap.put(createdDocument.getSelfLink(), createdReadUser.getId());

            // Create a User who can both read and write this document
            User readWriteUserDefinition = new User();
            readWriteUserDefinition.setId(UUID.randomUUID().toString());
            User createdReadWriteUser = asyncClient.createUser(createdDatabase.getSelfLink(), readWriteUserDefinition, null).toBlocking().first().getResource();

            // Create a read/write permission for the above document
            Permission readWritePermissionDefinition = new Permission();
            readWritePermissionDefinition.setId(UUID.randomUUID().toString());
            readWritePermissionDefinition.setResourceLink(createdDocument.getSelfLink());
            readWritePermissionDefinition.setPermissionMode(PermissionMode.All);

            // Assign the permission to the above user
            Permission readWriteCreatedPermission = asyncClient.createPermission(createdReadWriteUser.getSelfLink(), readWritePermissionDefinition, null).toBlocking().first().getResource();
            userToReadWriteResourceTokenMap.put(createdReadWriteUser.getId(), readWriteCreatedPermission.getToken());

            documentToReadWriteUserMap.put(createdDocument.getSelfLink(), createdReadWriteUser.getId());
        }
    }

    /**
     * Read a document with a user having read permission
     */
    @Test(groups = "samples", timeOut = TIMEOUT)
    public void readDocumentThroughTokenResolver() throws Exception {
        AsyncDocumentClient asyncClientWithTokenResolver = null;
        try {
            ConnectionPolicy connectionPolicy = new ConnectionPolicy();
            connectionPolicy.setConnectionMode(ConnectionMode.Direct);
            asyncClientWithTokenResolver = new AsyncDocumentClient.Builder()
                    .withServiceEndpoint(TestConfigurations.HOST)
                    .withConnectionPolicy(connectionPolicy)
                    .withConsistencyLevel(ConsistencyLevel.Session)
                    .withTokenResolver(getTokenResolverForRead())
                    .build();
            List<ResourceResponse<Document>> capturedResponse = Collections
                    .synchronizedList(new ArrayList<>());
            for (String documentLink : documentToReadUserMap.keySet()) {
                
                // Each document has one User who can only read it. Pass that User Id in the properties.
                // The token resolver will resolve the token for that User based on 'userId'.
                ImmutableMap<String, Object> properties = ImmutableMap.<String, Object> builder()
                        .put(USER_ID, documentToReadUserMap.get(documentLink))
                        .build();
                RequestOptions requestOptions = new RequestOptions();
                requestOptions.setProperties(properties);
                requestOptions.setPartitionKey(PartitionKey.None);
                Observable<ResourceResponse<Document>> readDocumentObservable = asyncClientWithTokenResolver
                        .readDocument(documentLink, requestOptions);
                readDocumentObservable.subscribe(resourceResponse -> {
                    capturedResponse.add(resourceResponse);
                });
            }
            Thread.sleep(2000);
            System.out.println("capturedResponse.size() = " + capturedResponse.size());
            assertThat(capturedResponse, hasSize(10));
        } finally {
            Utils.safeClose(asyncClientWithTokenResolver);
        }
    }

    /**
     * Delete a document with a user having all permission
     */
    @Test(groups = "samples", timeOut = TIMEOUT, dependsOnMethods = {"readDocumentThroughTokenResolver"})
    public void deleteDocumentThroughTokenResolver() throws Exception {
        AsyncDocumentClient asyncClientWithTokenResolver = null;
        try {
            ConnectionPolicy connectionPolicy = new ConnectionPolicy();
            connectionPolicy.setConnectionMode(ConnectionMode.Direct);
            asyncClientWithTokenResolver = new AsyncDocumentClient.Builder()
                    .withServiceEndpoint(TestConfigurations.HOST)
                    .withConnectionPolicy(connectionPolicy)
                    .withConsistencyLevel(ConsistencyLevel.Session)
                    .withTokenResolver(getTokenResolverForReadWrite())
                    .build();
            List<ResourceResponse<Document>> capturedResponse = Collections
                    .synchronizedList(new ArrayList<>());
            for (String documentLink : documentToReadWriteUserMap.keySet()) {
                
                // Each document has one User who can read and write it. Pass that User Id in the properties.
                // The token resolver will resolve the token for that User based on 'userId'.
                ImmutableMap<String, Object> properties = ImmutableMap.<String, Object> builder()
                        .put(USER_ID, documentToReadWriteUserMap.get(documentLink))
                        .build();

                RequestOptions requestOptions = new RequestOptions();
                requestOptions.setProperties(properties);
                requestOptions.setPartitionKey(PartitionKey.None);
                Observable<ResourceResponse<Document>> readDocumentObservable = asyncClientWithTokenResolver
                        .deleteDocument(documentLink, requestOptions);
                readDocumentObservable.subscribe(resourceResponse -> {
                    capturedResponse.add(resourceResponse);
                });
            }
            Thread.sleep(2000);
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
            connectionPolicy.setConnectionMode(ConnectionMode.Direct);
            asyncClientWithTokenResolver = new AsyncDocumentClient.Builder()
                    .withServiceEndpoint(TestConfigurations.HOST)
                    .withConnectionPolicy(connectionPolicy)
                    .withConsistencyLevel(ConsistencyLevel.Session)
                    .withTokenResolver(getTokenResolverWithBlockList(blockListedUserId, errorMessage))
                    .build();
            
            // Read a document using a block listed user, passing the 'userId' in the properties.
            // Token resolver will throw RuntimeException.
            RequestOptions options = new RequestOptions();
            ImmutableMap<String, Object> properties = ImmutableMap.<String, Object> builder()
                    .put(USER_ID, blockListedUserId)
                    .build();

            options.setProperties(properties);
            Observable<ResourceResponse<DocumentCollection>> readObservable = asyncClientWithTokenResolver.readCollection(createdCollection.getSelfLink(), options);
            List<Throwable> capturedErrors = Collections
                    .synchronizedList(new ArrayList<>());
            readObservable.subscribe(response -> {}, throwable -> capturedErrors.add(throwable));
            Thread.sleep(2000);
            assertThat(capturedErrors, hasSize(1));
            assertThat(capturedErrors.get(0), instanceOf(RuntimeException.class));
            assertThat(capturedErrors.get(0).getMessage(), equalTo(errorMessage));
            
            // Read a document using a valid user, passing the 'userId' in the properties.
            // Token resolver will pass on the correct token for authentication.
            String validUserId = userToReadWriteResourceTokenMap.keySet().iterator().next();
            System.out.println(validUserId);
            properties = ImmutableMap.<String, Object> builder()
                    .put(USER_ID, validUserId)
                    .build();
            options.setProperties(properties);
            readObservable = asyncClientWithTokenResolver.readCollection(createdCollection.getSelfLink(), options);
            List<DocumentCollection> capturedResponse = Collections
                    .synchronizedList(new ArrayList<>());
            readObservable.subscribe(resourceResponse -> capturedResponse.add(resourceResponse.getResource()), error -> error.printStackTrace());
            Thread.sleep(2000);
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
     * is compared to the current user's id, passed into the properties for the request.
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
        Utils.safeClean(asyncClient, createdDatabase);
        Utils.safeClose(asyncClient);
    }
}
