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

package com.microsoft.azure.cosmosdb.rx;

import com.microsoft.azure.cosmosdb.BridgeInternal;
import com.microsoft.azure.cosmosdb.ChangeFeedOptions;
import com.microsoft.azure.cosmosdb.ConnectionMode;
import com.microsoft.azure.cosmosdb.ConnectionPolicy;
import com.microsoft.azure.cosmosdb.ConsistencyLevel;
import com.microsoft.azure.cosmosdb.CosmosResourceType;
import com.microsoft.azure.cosmosdb.Database;
import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.FeedOptions;
import com.microsoft.azure.cosmosdb.FeedResponse;
import com.microsoft.azure.cosmosdb.PartitionKey;
import com.microsoft.azure.cosmosdb.Permission;
import com.microsoft.azure.cosmosdb.PermissionMode;
import com.microsoft.azure.cosmosdb.RequestOptions;
import com.microsoft.azure.cosmosdb.Resource;
import com.microsoft.azure.cosmosdb.ResourceResponse;
import com.microsoft.azure.cosmosdb.StoredProcedure;
import com.microsoft.azure.cosmosdb.StoredProcedureResponse;
import com.microsoft.azure.cosmosdb.TokenResolver;
import com.microsoft.azure.cosmosdb.User;
import com.microsoft.azure.cosmosdb.internal.HttpConstants;

import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import rx.Observable;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.Map;

public class TokenResolverTest extends TestSuiteBase {

    private class UserClass {
        public String userName;
        public int userId;
        
        public UserClass(String userName, int userId) {
            this.userName = userName;
            this.userId = userId;
        }
    }
    
    private Database createdDatabase;
    private DocumentCollection createdCollection;
    private User userWithReadPermission;
    private User userWithAllPermission;

    private Permission readPermission;
    private Permission allPermission;

    private AsyncDocumentClient.Builder clientBuilder;
    private AsyncDocumentClient client;

    @Factory(dataProvider = "clientBuilders")
    public TokenResolverTest(AsyncDocumentClient.Builder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    @DataProvider(name = "connectionMode")
    public Object[][] connectionMode() {
        return new Object[][]{
                {ConnectionMode.Gateway},
                {ConnectionMode.Direct},
        };
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        createdDatabase = SHARED_DATABASE;
        createdCollection = SHARED_SINGLE_PARTITION_COLLECTION;

        client = clientBuilder.build();

        userWithReadPermission = createUser(client, createdDatabase.getId(), getUserDefinition());
        readPermission = client.createPermission(userWithReadPermission.getSelfLink(), getPermission(createdCollection, "ReadPermissionOnColl", PermissionMode.Read), null).toBlocking().single()
                .getResource();

        userWithAllPermission = createUser(client, createdDatabase.getId(), getUserDefinition());
        allPermission = client.createPermission(userWithAllPermission.getSelfLink(), getPermission(createdCollection, "AllPermissionOnColl", PermissionMode.All), null).toBlocking().single()
                .getResource();
    }

    @Test(groups = {"simple"}, dataProvider = "connectionMode", timeOut = TIMEOUT)
    public void readDocumentWithReadPermission(ConnectionMode connectionMode) {
        Document docDefinition = getDocumentDefinition();
        ResourceResponse<Document> resourceResponse = client
                .createDocument(BridgeInternal.getAltLink(createdCollection), docDefinition, null, false).toBlocking().first();
        AsyncDocumentClient asyncClientWithTokenResolver = null;
        try {
            asyncClientWithTokenResolver = buildClient(connectionMode, PermissionMode.Read);
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.setPartitionKey(new PartitionKey(resourceResponse.getResource().get("mypk")));
            HashMap<String, Object> properties = new HashMap<String, Object>();
            properties.put("UserId", "readUser");
            requestOptions.setProperties(properties);
            Observable<ResourceResponse<Document>> readObservable = asyncClientWithTokenResolver.readDocument(resourceResponse.getResource().getSelfLink(), requestOptions);
            ResourceResponseValidator<Document> validator = new ResourceResponseValidator.Builder<Document>()
                    .withId(resourceResponse.getResource().getId()).build();
            validateSuccess(readObservable, validator);
        } finally {
            safeClose(asyncClientWithTokenResolver);
        }
    }

    @Test(groups = {"simple"}, dataProvider = "connectionMode", timeOut = TIMEOUT)
    public void deleteDocumentWithReadPermission(ConnectionMode connectionMode) {
        Document docDefinition = getDocumentDefinition();
        ResourceResponse<Document> resourceResponse = client
                .createDocument(BridgeInternal.getAltLink(createdCollection), docDefinition, null, false).toBlocking().first();
        AsyncDocumentClient asyncClientWithTokenResolver = null;
        try {
            asyncClientWithTokenResolver = buildClient(connectionMode, PermissionMode.Read);
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.setPartitionKey(new PartitionKey(resourceResponse.getResource().get("mypk")));
            Observable<ResourceResponse<Document>> readObservable = asyncClientWithTokenResolver.deleteDocument(resourceResponse.getResource().getSelfLink(), requestOptions);
            FailureValidator validator = new FailureValidator.Builder().statusCode(HttpConstants.StatusCodes.FORBIDDEN).build();
            validateFailure(readObservable, validator);
        } finally {
            safeClose(asyncClientWithTokenResolver);
        }
    }

    @Test(groups = {"simple"}, dataProvider = "connectionMode", timeOut = TIMEOUT)
    public void writeDocumentWithReadPermission(ConnectionMode connectionMode) {
        AsyncDocumentClient asyncClientWithTokenResolver = null;
        try {
            asyncClientWithTokenResolver = buildClient(connectionMode, PermissionMode.Read);
            Observable<ResourceResponse<Document>> readObservable = asyncClientWithTokenResolver.createDocument(createdCollection.getSelfLink(), getDocumentDefinition(), null, true);
            FailureValidator validator = new FailureValidator.Builder().statusCode(HttpConstants.StatusCodes.FORBIDDEN).build();
            validateFailure(readObservable, validator);
        } finally {
            safeClose(asyncClientWithTokenResolver);
        }
    }

    @Test(groups = {"simple"}, dataProvider = "connectionMode", timeOut = TIMEOUT)
    public void writeDocumentWithAllPermission(ConnectionMode connectionMode) {
        AsyncDocumentClient asyncClientWithTokenResolver = null;
        try {
            asyncClientWithTokenResolver = buildClient(connectionMode, PermissionMode.All);
            Document documentDefinition = getDocumentDefinition();
            Observable<ResourceResponse<Document>> readObservable = asyncClientWithTokenResolver.createDocument(createdCollection.getSelfLink(), documentDefinition, null, true);
            ResourceResponseValidator<Document> validator = new ResourceResponseValidator.Builder<Document>()
                    .withId(documentDefinition.getId()).build();
            validateSuccess(readObservable, validator);
        } finally {
            safeClose(asyncClientWithTokenResolver);
        }
    }

    @Test(groups = {"simple"}, dataProvider = "connectionMode", timeOut = TIMEOUT)
    public void deleteDocumentWithAllPermission(ConnectionMode connectionMode) {
        Document docDefinition = getDocumentDefinition();
        ResourceResponse<Document> resourceResponse = client
                .createDocument(BridgeInternal.getAltLink(createdCollection), docDefinition, null, false).toBlocking().first();
        AsyncDocumentClient asyncClientWithTokenResolver = null;
        try {
            asyncClientWithTokenResolver = buildClient(connectionMode, PermissionMode.All);
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.setPartitionKey(new PartitionKey(resourceResponse.getResource().get("mypk")));
            Observable<ResourceResponse<Document>> readObservable = asyncClientWithTokenResolver.deleteDocument(resourceResponse.getResource().getSelfLink(), requestOptions);
            ResourceResponseValidator<Document> validator = new ResourceResponseValidator.Builder<Document>()
                    .nullResource().build();
            validateSuccess(readObservable, validator);
        } finally {
            safeClose(asyncClientWithTokenResolver);
        }
    }

    @Test(groups = {"simple"}, dataProvider = "connectionMode", timeOut = TIMEOUT)
    public void readCollectionWithReadPermission(ConnectionMode connectionMode) {
        AsyncDocumentClient asyncClientWithTokenResolver = null;
        try {
            asyncClientWithTokenResolver = buildClient(connectionMode, PermissionMode.Read);
            Observable<ResourceResponse<DocumentCollection>> readObservable = asyncClientWithTokenResolver.readCollection(createdCollection.getSelfLink(), null);
            ResourceResponseValidator<DocumentCollection> validator = new ResourceResponseValidator.Builder<DocumentCollection>()
                    .withId(createdCollection.getId()).build();
            validateSuccess(readObservable, validator);
        } finally {
            safeClose(asyncClientWithTokenResolver);
        }
    }

    @Test(groups = {"simple"}, dataProvider = "connectionMode", timeOut = TIMEOUT)
    public void deleteCollectionWithReadPermission(ConnectionMode connectionMode) {
        AsyncDocumentClient asyncClientWithTokenResolver = null;
        try {
            asyncClientWithTokenResolver = buildClient(connectionMode, PermissionMode.Read);
            Observable<ResourceResponse<DocumentCollection>> readObservable = asyncClientWithTokenResolver.deleteCollection(createdCollection.getSelfLink(), null);
            FailureValidator validator = new FailureValidator.Builder().statusCode(HttpConstants.StatusCodes.FORBIDDEN).build();
            validateFailure(readObservable, validator);
        } finally {
            safeClose(asyncClientWithTokenResolver);
        }
    }

    @Test(groups = {"simple"}, dataProvider = "connectionMode", timeOut = TIMEOUT)
    public void verifyingAuthTokenAPISequence(ConnectionMode connectionMode) {
        Document docDefinition = getDocumentDefinition();
        ResourceResponse<Document> resourceResponse = client
                .createDocument(BridgeInternal.getAltLink(createdCollection), docDefinition, null, false).toBlocking().first();
        AsyncDocumentClient asyncClientWithTokenResolver = null;
        try {
            ConnectionPolicy connectionPolicy = new ConnectionPolicy();
            connectionPolicy.setConnectionMode(connectionMode);

            //Unauthorized error with invalid token resolver, valid  master key and valid permission feed, making it sure tokenResolver has higher priority than all.
            List<Permission> permissionFeed = new ArrayList<>();
            permissionFeed.add(readPermission);
            asyncClientWithTokenResolver = new AsyncDocumentClient.Builder()
                    .withServiceEndpoint(TestConfigurations.HOST)
                    .withConnectionPolicy(connectionPolicy)
                    .withConsistencyLevel(ConsistencyLevel.Session)
                    .withTokenResolver(getTokenResolver(null)) //TokenResolver always generating invalid token.
                    .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                    .withPermissionFeed(permissionFeed)
                    .build();
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.setPartitionKey(new PartitionKey(resourceResponse.getResource().get("mypk")));
            Observable<ResourceResponse<Document>> readObservable = asyncClientWithTokenResolver.readDocument(resourceResponse.getResource().getSelfLink(), requestOptions);
            FailureValidator failureValidator = new FailureValidator.Builder().statusCode(HttpConstants.StatusCodes.UNAUTHORIZED).build();
            validateFailure(readObservable, failureValidator);

            //Success read operation with valid token resolver, invalid  master key and invalid permission feed, making it sure tokenResolver has higher priority than all.
            asyncClientWithTokenResolver = new AsyncDocumentClient.Builder()
                    .withServiceEndpoint(TestConfigurations.HOST)
                    .withConnectionPolicy(connectionPolicy)
                    .withConsistencyLevel(ConsistencyLevel.Session)
                    .withTokenResolver(getTokenResolver(PermissionMode.Read))
                    .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                    .withPermissionFeed(permissionFeed)
                    .build();
            readObservable = asyncClientWithTokenResolver.readDocument(resourceResponse.getResource().getSelfLink(), requestOptions);
            ResourceResponseValidator<Document> sucessValidator = new ResourceResponseValidator.Builder<Document>()
                    .withId(resourceResponse.getResource().getId()).build();
            validateSuccess(readObservable, sucessValidator);


            //Success read operation with valid permission feed, supporting above hypothesis.
            asyncClientWithTokenResolver = new AsyncDocumentClient.Builder()
                    .withServiceEndpoint(TestConfigurations.HOST)
                    .withConnectionPolicy(connectionPolicy)
                    .withConsistencyLevel(ConsistencyLevel.Session)
                    .withPermissionFeed(permissionFeed)
                    .build();
            readObservable = asyncClientWithTokenResolver.readDocument(resourceResponse.getResource().getSelfLink(), requestOptions);
            validateSuccess(readObservable, sucessValidator);


            //Success read operation with valid master key, supporting above hypothesis.
            asyncClientWithTokenResolver = new AsyncDocumentClient.Builder()
                    .withServiceEndpoint(TestConfigurations.HOST)
                    .withConnectionPolicy(connectionPolicy)
                    .withConsistencyLevel(ConsistencyLevel.Session)
                    .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                    .build();
            readObservable = asyncClientWithTokenResolver.readDocument(resourceResponse.getResource().getSelfLink(), requestOptions);
            validateSuccess(readObservable, sucessValidator);

        } finally {
            safeClose(asyncClientWithTokenResolver);
        }
    }

    @Test(groups = {"simple"}, dataProvider = "connectionMode", timeOut = 6000000)
    public void createAndExecuteSprocWithWritePermission(ConnectionMode connectionMode) throws InterruptedException {
        AsyncDocumentClient asyncClientWithTokenResolver = null;
        try {
            asyncClientWithTokenResolver = buildClient(connectionMode, PermissionMode.All);
            String sprocId = "storedProcedure" + UUID.randomUUID().toString();
            StoredProcedure sproc = new StoredProcedure(
                    "{" +
                    "  'id':'" + sprocId + "'," +
                    "  'body':" +
                    "    'function() {" +
                    "        var mytext = \"x\";" +
                    "        var myval = 1;" +
                    "        try {" +
                    "            getContext().getResponse().setBody(\"Success!\");" +
                    "        }" +
                    "        catch(err) {" +
                    "            getContext().getResponse().setBody(\"inline err: [\" + err.number + \"] \" + err);" +
                    "        }" +
                    "    }'" +
                    "}");
            
            Observable<ResourceResponse<StoredProcedure>> createObservable = asyncClientWithTokenResolver.createStoredProcedure(createdCollection.getSelfLink(), sproc, null);
            ResourceResponseValidator<StoredProcedure> createSucessValidator = new ResourceResponseValidator.Builder<StoredProcedure>()
                    .withId(sprocId).build();
            validateSuccess(createObservable, createSucessValidator);
            
            RequestOptions options = new RequestOptions();
            options.setPartitionKey(new PartitionKey(""));
            String sprocLink = "dbs/" + createdDatabase.getId() + "/colls/" + createdCollection.getId() + "/sprocs/" + sprocId;
            StoredProcedureResponse result = asyncClientWithTokenResolver.executeStoredProcedure(sprocLink, options, null).toBlocking().single();
            assertThat(result.getResponseAsString()).isEqualTo("\"Success!\"");
        } finally {
            safeClose(asyncClientWithTokenResolver);
        }
    }

    @Test(groups = {"simple"}, dataProvider = "connectionMode", timeOut = TIMEOUT)
    public void readDocumentsWithAllPermission(ConnectionMode connectionMode) {
        AsyncDocumentClient asyncClientWithTokenResolver = null;
        String id1 = UUID.randomUUID().toString();
        String id2 = UUID.randomUUID().toString();
        
        try {
            asyncClientWithTokenResolver = buildClient(connectionMode, PermissionMode.All);            
            Document document1 = asyncClientWithTokenResolver.createDocument(createdCollection.getSelfLink(), new Document("{'id': '" + id1 + "'}"), null, false)
                    .toBlocking().single().getResource();
            Document document2 = asyncClientWithTokenResolver.createDocument(createdCollection.getSelfLink(), new Document("{'id': '" + id2 + "'}"), null, false)
                    .toBlocking().single().getResource();
            List<String> expectedIds = new ArrayList<String>();
            String rid1 = document1.getResourceId();
            String rid2 = document2.getResourceId();
            expectedIds.add(rid1);
            expectedIds.add(rid2);
            String query = "SELECT * FROM r WHERE r._rid=\"" + rid1 + "\" or r._rid=\"" + rid2 + "\"";
            
            FeedOptions options = new FeedOptions();
            options.setEnableCrossPartitionQuery(true);
            Observable<FeedResponse<Document>> queryObservable = asyncClientWithTokenResolver.queryDocuments(createdCollection.getSelfLink(), query, options);
            FeedResponseListValidator<Document> validator = new FeedResponseListValidator.Builder<Document>()
                    .numberOfPages(1)
                    .exactlyContainsInAnyOrder(expectedIds).build();
            validateQuerySuccess(queryObservable, validator, 10000);
        } finally {
            safeClose(asyncClientWithTokenResolver);
        }
    }

    @Test(groups = {"simple"}, dataProvider = "connectionMode", timeOut = TIMEOUT)
    public void readChangeFeedWithAllPermission(ConnectionMode connectionMode) throws InterruptedException {
        
        //setStartDateTime is not currently supported in multimaster mode. So skipping the test
        if(BridgeInternal.isEnableMultipleWriteLocations(client.getDatabaseAccount().toBlocking().single())){
            throw new SkipException("StartTime/IfModifiedSince is not currently supported when EnableMultipleWriteLocations is set");
        }

        AsyncDocumentClient asyncClientWithTokenResolver = null;
        String id1 = UUID.randomUUID().toString();
        String id2 = UUID.randomUUID().toString();
        String partitionKey = createdCollection.getPartitionKey().getPaths().get(0).substring(1);
        String partitionKeyValue = "pk";
        Document document1 = new Document();
        document1.setId(id1);
        document1.set(partitionKey, partitionKeyValue);
        Document document2 = new Document();
        document2.setId(id2);
        document2.set(partitionKey, partitionKeyValue);
        try {
            asyncClientWithTokenResolver = buildClient(connectionMode, PermissionMode.All);
            ZonedDateTime befTime = ZonedDateTime.now();
            Thread.sleep(1000);

            document1 = asyncClientWithTokenResolver
                    .createDocument(createdCollection.getSelfLink(), document1, null, false).toBlocking().single()
                    .getResource();
            document2 = asyncClientWithTokenResolver
                    .createDocument(createdCollection.getSelfLink(), document2, null, false).toBlocking().single()
                    .getResource();
            List<String> expectedIds = new ArrayList<String>();
            String rid1 = document1.getResourceId();
            String rid2 = document2.getResourceId();
            expectedIds.add(rid1);
            expectedIds.add(rid2);

            ChangeFeedOptions options = new ChangeFeedOptions();
            options.setPartitionKey(new PartitionKey(partitionKeyValue));
            options.setStartDateTime(befTime);

            Thread.sleep(1000);
            Observable<FeedResponse<Document>> queryObservable = asyncClientWithTokenResolver
                    .queryDocumentChangeFeed(createdCollection.getSelfLink(), options);
            FeedResponseListValidator<Document> validator = new FeedResponseListValidator.Builder<Document>()
                    .exactlyContainsInAnyOrder(expectedIds).build();
            validateQuerySuccess(queryObservable, validator, 10000);
        } finally {
            safeClose(asyncClientWithTokenResolver);
        }
    }

    @Test(groups = {"simple"}, dataProvider = "connectionMode", timeOut = TIMEOUT)
    public void verifyRuntimeExceptionWhenUserModifiesProperties(ConnectionMode connectionMode) {
        AsyncDocumentClient asyncClientWithTokenResolver = null;
        
        try {
            ConnectionPolicy connectionPolicy = new ConnectionPolicy();
            connectionPolicy.setConnectionMode(connectionMode);
            asyncClientWithTokenResolver = new AsyncDocumentClient.Builder()
                    .withServiceEndpoint(TestConfigurations.HOST)
                    .withConnectionPolicy(connectionPolicy)
                    .withConsistencyLevel(ConsistencyLevel.Session)
                    .withTokenResolver(getBadTokenResolver())
                    .build();

            RequestOptions options = new RequestOptions();
            options.setProperties(new HashMap<String, Object>());
            Observable<ResourceResponse<DocumentCollection>> readObservable = asyncClientWithTokenResolver.readCollection(createdCollection.getSelfLink(), options);
            FailureValidator validator = new FailureValidator.Builder().withRuntimeExceptionClass(UnsupportedOperationException.class).build();
            validateFailure(readObservable, validator);            
        } finally {
            safeClose(asyncClientWithTokenResolver);
        }
    }
    
    @Test(groups = {"simple"}, dataProvider = "connectionMode", timeOut = TIMEOUT)
    public void verifyBlockListedUserThrows(ConnectionMode connectionMode) {
        String field = "user";
        UserClass blockListedUser = new UserClass("block listed user", 0);
        String errorMessage = "block listed user! access denied!";

        AsyncDocumentClient asyncClientWithTokenResolver = null;
        try {
            ConnectionPolicy connectionPolicy = new ConnectionPolicy();
            connectionPolicy.setConnectionMode(connectionMode);
            asyncClientWithTokenResolver = new AsyncDocumentClient.Builder()
                    .withServiceEndpoint(TestConfigurations.HOST)
                    .withConnectionPolicy(connectionPolicy)
                    .withConsistencyLevel(ConsistencyLevel.Session)
                    .withTokenResolver(getTokenResolverWithBlockList(PermissionMode.Read, field, blockListedUser, errorMessage))
                    .build();
            
            RequestOptions options = new RequestOptions();
            HashMap<String, Object> properties = new HashMap<String, Object>();
            properties.put(field, blockListedUser);
            options.setProperties(properties);
            Observable<ResourceResponse<DocumentCollection>> readObservable = asyncClientWithTokenResolver.readCollection(createdCollection.getSelfLink(), options);
            FailureValidator validator = new FailureValidator.Builder().withRuntimeExceptionMessage(errorMessage).build();
            validateFailure(readObservable, validator);

            properties.put(field, new UserClass("valid user", 1));
            options.setProperties(properties);
            readObservable = asyncClientWithTokenResolver.readCollection(createdCollection.getSelfLink(), options);
            ResourceResponseValidator<DocumentCollection> sucessValidator = new ResourceResponseValidator.Builder<DocumentCollection>()
                    .withId(createdCollection.getId()).build();
            validateSuccess(readObservable, sucessValidator);
        } finally {
            safeClose(asyncClientWithTokenResolver);
        }
    }

    @AfterClass(groups = {"simple"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        client.close();
    }

    private Document getDocumentDefinition() {
        String uuid = UUID.randomUUID().toString();
        Document doc = new Document(String.format("{ "
                        + "\"id\": \"%s\", "
                        + "\"mypk\": \"%s\", "
                        + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]"
                        + "}"
                , uuid, uuid));
        return doc;
    }

    private AsyncDocumentClient buildClient(ConnectionMode connectionMode, PermissionMode permissionMode) {
        ConnectionPolicy connectionPolicy = new ConnectionPolicy();
        connectionPolicy.setConnectionMode(connectionMode);
        return new AsyncDocumentClient.Builder()
                .withServiceEndpoint(TestConfigurations.HOST)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.Session)
                .withTokenResolver(getTokenResolver(permissionMode))
                .build();
    }

    private static User getUserDefinition() {
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        return user;
    }

    private Permission getPermission(Resource resource, String permissionId, PermissionMode permissionMode) {
        Permission permission = new Permission();
        permission.setId(permissionId);
        permission.setPermissionMode(permissionMode);
        permission.setResourceLink(resource.getSelfLink());
        return permission;
    }

    private TokenResolver getTokenResolver(PermissionMode permissionMode) {
        return (String requestVerb, String resourceIdOrFullName, CosmosResourceType resourceType, Map<String, Object>  properties) -> {
            if (permissionMode == null) {
                return "invalid";
            } else if (permissionMode.equals(PermissionMode.Read)) {
                return readPermission.getToken();
            } else {
                return allPermission.getToken();
            }
        };
    }

    private TokenResolver getBadTokenResolver() {
        return (String requestVerb, String resourceIdOrFullName, CosmosResourceType resourceType, Map<String, Object>  properties) -> {
            if (resourceType == CosmosResourceType.System) {
                return readPermission.getToken();
            }
            if (properties != null) {
                properties.put("key", "value");
            }
            return null;
        };
    }
    
    private TokenResolver getTokenResolverWithBlockList(PermissionMode permissionMode,  String field, UserClass blockListedUser, String errorMessage) {
        return (String requestVerb, String resourceIdOrFullName, CosmosResourceType resourceType, Map<String, Object>  properties) -> {
            UserClass currentUser = null;
            if (properties != null && properties.get(field) != null) {
                currentUser = (UserClass) properties.get(field);
            }
            
            if (resourceType == CosmosResourceType.System) {
                return readPermission.getToken();
            } else if (currentUser != null &&
                    !currentUser.userName.equals(blockListedUser.userName) &&
                    currentUser.userId != blockListedUser.userId) {
                if (permissionMode.equals(PermissionMode.Read)) {
                    return readPermission.getToken();
                } else {
                    return allPermission.getToken();
                }
            } else {
                throw new RuntimeException(errorMessage);
            }
        };
    }
}
