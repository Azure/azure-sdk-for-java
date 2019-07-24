// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.rx;

import com.azure.data.cosmos.internal.AsyncDocumentClient;
import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.ChangeFeedOptions;
import com.azure.data.cosmos.ConnectionMode;
import com.azure.data.cosmos.ConnectionPolicy;
import com.azure.data.cosmos.ConsistencyLevel;
import com.azure.data.cosmos.CosmosResourceType;
import com.azure.data.cosmos.internal.Database;
import com.azure.data.cosmos.internal.Document;
import com.azure.data.cosmos.internal.DocumentCollection;
import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.PartitionKey;
import com.azure.data.cosmos.internal.*;
import com.azure.data.cosmos.PermissionMode;
import com.azure.data.cosmos.Resource;
import com.azure.data.cosmos.TokenResolver;
import com.azure.data.cosmos.internal.TestSuiteBase;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.testng.annotations.Ignore;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import reactor.core.publisher.Flux;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

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
        super(clientBuilder);
    }

    @DataProvider(name = "connectionMode")
    public Object[][] connectionMode() {
        return new Object[][]{
                {ConnectionMode.GATEWAY},
                {ConnectionMode.DIRECT},
        };
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        createdDatabase = SHARED_DATABASE;
        createdCollection = SHARED_MULTI_PARTITION_COLLECTION;

        client = clientBuilder().build();

        userWithReadPermission = createUser(client, createdDatabase.id(), getUserDefinition());
        readPermission = client.createPermission(userWithReadPermission.selfLink(), getPermission(createdCollection, "ReadPermissionOnColl", PermissionMode.READ), null).single().block()
                .getResource();

        userWithAllPermission = createUser(client, createdDatabase.id(), getUserDefinition());
        allPermission = client.createPermission(userWithAllPermission.selfLink(), getPermission(createdCollection, "AllPermissionOnColl", PermissionMode.ALL), null).single().block()
                .getResource();
    }

    @Test(groups = {"simple"}, dataProvider = "connectionMode", timeOut = TIMEOUT)
    public void readDocumentWithReadPermission(ConnectionMode connectionMode) {
        Document docDefinition = getDocumentDefinition();
        ResourceResponse<Document> resourceResponse = client
                .createDocument(BridgeInternal.getAltLink(createdCollection), docDefinition, null, false).blockFirst();
        AsyncDocumentClient asyncClientWithTokenResolver = null;
        try {
            asyncClientWithTokenResolver = buildClient(connectionMode, PermissionMode.READ);
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.setPartitionKey(new PartitionKey(resourceResponse.getResource().get("mypk")));
            HashMap<String, Object> properties = new HashMap<String, Object>();
            properties.put("UserId", "readUser");
            requestOptions.setProperties(properties);
            Flux<ResourceResponse<Document>> readObservable = asyncClientWithTokenResolver.readDocument(resourceResponse.getResource().selfLink(), requestOptions);
            ResourceResponseValidator<Document> validator = new ResourceResponseValidator.Builder<Document>()
                    .withId(resourceResponse.getResource().id()).build();
            validateSuccess(readObservable, validator);
        } finally {
            safeClose(asyncClientWithTokenResolver);
        }
    }

    @Test(groups = {"simple"}, dataProvider = "connectionMode", timeOut = TIMEOUT)
    public void deleteDocumentWithReadPermission(ConnectionMode connectionMode) {
        Document docDefinition = getDocumentDefinition();
        ResourceResponse<Document> resourceResponse = client
                .createDocument(BridgeInternal.getAltLink(createdCollection), docDefinition, null, false).blockFirst();
        AsyncDocumentClient asyncClientWithTokenResolver = null;
        try {
            asyncClientWithTokenResolver = buildClient(connectionMode, PermissionMode.READ);
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.setPartitionKey(new PartitionKey(resourceResponse.getResource().get("mypk")));
            Flux<ResourceResponse<Document>> readObservable = asyncClientWithTokenResolver.deleteDocument(resourceResponse.getResource().selfLink(), requestOptions);
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
            asyncClientWithTokenResolver = buildClient(connectionMode, PermissionMode.READ);
            Flux<ResourceResponse<Document>> readObservable = asyncClientWithTokenResolver.createDocument(createdCollection.selfLink(), getDocumentDefinition(), null, true);
            FailureValidator validator = new FailureValidator.Builder().statusCode(HttpConstants.StatusCodes.FORBIDDEN).build();
            validateFailure(readObservable, validator);
        } finally {
            safeClose(asyncClientWithTokenResolver);
        }
    }

    //FIXME test is flaky
    @Ignore
    @Test(groups = {"simple"}, dataProvider = "connectionMode", timeOut = TIMEOUT)
    public void writeDocumentWithAllPermission(ConnectionMode connectionMode) {
        AsyncDocumentClient asyncClientWithTokenResolver = null;
        try {
            asyncClientWithTokenResolver = buildClient(connectionMode, PermissionMode.ALL);
            Document documentDefinition = getDocumentDefinition();
            Flux<ResourceResponse<Document>> readObservable = asyncClientWithTokenResolver.createDocument(createdCollection.selfLink(), documentDefinition, null, true);
            ResourceResponseValidator<Document> validator = new ResourceResponseValidator.Builder<Document>()
                    .withId(documentDefinition.id()).build();
            validateSuccess(readObservable, validator);
        } finally {
            safeClose(asyncClientWithTokenResolver);
        }
    }

    @Test(groups = {"simple"}, dataProvider = "connectionMode", timeOut = TIMEOUT)
    public void deleteDocumentWithAllPermission(ConnectionMode connectionMode) {
        Document docDefinition = getDocumentDefinition();
        ResourceResponse<Document> resourceResponse = client
                .createDocument(BridgeInternal.getAltLink(createdCollection), docDefinition, null, false).blockFirst();
        AsyncDocumentClient asyncClientWithTokenResolver = null;
        try {
            asyncClientWithTokenResolver = buildClient(connectionMode, PermissionMode.ALL);
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.setPartitionKey(new PartitionKey(resourceResponse.getResource().get("mypk")));
            Flux<ResourceResponse<Document>> readObservable = asyncClientWithTokenResolver.deleteDocument(resourceResponse.getResource().selfLink(), requestOptions);
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
            asyncClientWithTokenResolver = buildClient(connectionMode, PermissionMode.READ);
            Flux<ResourceResponse<DocumentCollection>> readObservable = asyncClientWithTokenResolver.readCollection(createdCollection.selfLink(), null);
            ResourceResponseValidator<DocumentCollection> validator = new ResourceResponseValidator.Builder<DocumentCollection>()
                    .withId(createdCollection.id()).build();
            validateSuccess(readObservable, validator);
        } finally {
            safeClose(asyncClientWithTokenResolver);
        }
    }

    @Test(groups = {"simple"}, dataProvider = "connectionMode", timeOut = TIMEOUT)
    public void deleteCollectionWithReadPermission(ConnectionMode connectionMode) {
        AsyncDocumentClient asyncClientWithTokenResolver = null;
        try {
            asyncClientWithTokenResolver = buildClient(connectionMode, PermissionMode.READ);
            Flux<ResourceResponse<DocumentCollection>> readObservable = asyncClientWithTokenResolver.deleteCollection(createdCollection.selfLink(), null);
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
                .createDocument(BridgeInternal.getAltLink(createdCollection), docDefinition, null, false).blockFirst();
        AsyncDocumentClient asyncClientWithTokenResolver = null;
        try {
            ConnectionPolicy connectionPolicy = new ConnectionPolicy();
            connectionPolicy.connectionMode(connectionMode);

            //Unauthorized error with invalid token resolver, valid  master key and valid permission feed, making it sure tokenResolver has higher priority than all.
            List<Permission> permissionFeed = new ArrayList<>();
            permissionFeed.add(readPermission);
            asyncClientWithTokenResolver = new AsyncDocumentClient.Builder()
                    .withServiceEndpoint(TestConfigurations.HOST)
                    .withConnectionPolicy(connectionPolicy)
                    .withConsistencyLevel(ConsistencyLevel.SESSION)
                    .withTokenResolver(getTokenResolver(null)) //TokenResolver always generating invalid token.
                    .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                    .withPermissionFeed(permissionFeed)
                    .build();
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.setPartitionKey(new PartitionKey(resourceResponse.getResource().get("mypk")));
            Flux<ResourceResponse<Document>> readObservable = asyncClientWithTokenResolver.readDocument(resourceResponse.getResource().selfLink(), requestOptions);
            FailureValidator failureValidator = new FailureValidator.Builder().statusCode(HttpConstants.StatusCodes.UNAUTHORIZED).build();
            validateFailure(readObservable, failureValidator);

            //Success read operation with valid token resolver, invalid  master key and invalid permission feed, making it sure tokenResolver has higher priority than all.
            asyncClientWithTokenResolver = new AsyncDocumentClient.Builder()
                    .withServiceEndpoint(TestConfigurations.HOST)
                    .withConnectionPolicy(connectionPolicy)
                    .withConsistencyLevel(ConsistencyLevel.SESSION)
                    .withTokenResolver(getTokenResolver(PermissionMode.READ))
                    .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                    .withPermissionFeed(permissionFeed)
                    .build();
            readObservable = asyncClientWithTokenResolver.readDocument(resourceResponse.getResource().selfLink(), requestOptions);
            ResourceResponseValidator<Document> sucessValidator = new ResourceResponseValidator.Builder<Document>()
                    .withId(resourceResponse.getResource().id()).build();
            validateSuccess(readObservable, sucessValidator);


            //Success read operation with valid permission feed, supporting above hypothesis.
            asyncClientWithTokenResolver = new AsyncDocumentClient.Builder()
                    .withServiceEndpoint(TestConfigurations.HOST)
                    .withConnectionPolicy(connectionPolicy)
                    .withConsistencyLevel(ConsistencyLevel.SESSION)
                    .withPermissionFeed(permissionFeed)
                    .build();
            readObservable = asyncClientWithTokenResolver.readDocument(resourceResponse.getResource().selfLink(), requestOptions);
            validateSuccess(readObservable, sucessValidator);


            //Success read operation with valid master key, supporting above hypothesis.
            asyncClientWithTokenResolver = new AsyncDocumentClient.Builder()
                    .withServiceEndpoint(TestConfigurations.HOST)
                    .withConnectionPolicy(connectionPolicy)
                    .withConsistencyLevel(ConsistencyLevel.SESSION)
                    .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                    .build();
            readObservable = asyncClientWithTokenResolver.readDocument(resourceResponse.getResource().selfLink(), requestOptions);
            validateSuccess(readObservable, sucessValidator);

        } finally {
            safeClose(asyncClientWithTokenResolver);
        }
    }

    @Test(groups = {"simple"}, dataProvider = "connectionMode", timeOut = 6000000)
    public void createAndExecuteSprocWithWritePermission(ConnectionMode connectionMode) throws InterruptedException {
        AsyncDocumentClient asyncClientWithTokenResolver = null;
        try {
            asyncClientWithTokenResolver = buildClient(connectionMode, PermissionMode.ALL);
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
            
            Flux<ResourceResponse<StoredProcedure>> createObservable = asyncClientWithTokenResolver.createStoredProcedure(createdCollection.selfLink(), sproc, null);
            ResourceResponseValidator<StoredProcedure> createSucessValidator = new ResourceResponseValidator.Builder<StoredProcedure>()
                    .withId(sprocId).build();
            validateSuccess(createObservable, createSucessValidator);
            
            RequestOptions options = new RequestOptions();
            options.setPartitionKey(new PartitionKey(""));
            String sprocLink = "dbs/" + createdDatabase.id() + "/colls/" + createdCollection.id() + "/sprocs/" + sprocId;
            StoredProcedureResponse result = asyncClientWithTokenResolver.executeStoredProcedure(sprocLink, options, null).single().block();
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
            asyncClientWithTokenResolver = buildClient(connectionMode, PermissionMode.ALL);
            Document document1 = asyncClientWithTokenResolver.createDocument(createdCollection.selfLink(), new Document("{'id': '" + id1 + "'}"), null, false)
                    .single().block().getResource();
            Document document2 = asyncClientWithTokenResolver.createDocument(createdCollection.selfLink(), new Document("{'id': '" + id2 + "'}"), null, false)
                    .single().block().getResource();
            List<String> expectedIds = new ArrayList<String>();
            String rid1 = document1.resourceId();
            String rid2 = document2.resourceId();
            expectedIds.add(rid1);
            expectedIds.add(rid2);
            String query = "SELECT * FROM r WHERE r._rid=\"" + rid1 + "\" or r._rid=\"" + rid2 + "\"";
            
            FeedOptions options = new FeedOptions();
            options.enableCrossPartitionQuery(true);
            Flux<FeedResponse<Document>> queryObservable = asyncClientWithTokenResolver.queryDocuments(createdCollection.selfLink(), query, options);
            FeedResponseListValidator<Document> validator = new FeedResponseListValidator.Builder<Document>()
                .totalSize(2)
                .exactlyContainsInAnyOrder(expectedIds).build();
            validateQuerySuccess(queryObservable, validator, 10000);
        } finally {
            safeClose(asyncClientWithTokenResolver);
        }
    }

    @Test(groups = {"simple"}, dataProvider = "connectionMode", timeOut = TIMEOUT)
    public void readChangeFeedWithAllPermission(ConnectionMode connectionMode) throws InterruptedException {

        //setStartDateTime is not currently supported in multimaster mode. So skipping the test
        if(BridgeInternal.isEnableMultipleWriteLocations(client.getDatabaseAccount().single().block())){
            throw new SkipException("StartTime/IfModifiedSince is not currently supported when EnableMultipleWriteLocations is set");
        }

        AsyncDocumentClient asyncClientWithTokenResolver = null;
        String id1 = UUID.randomUUID().toString();
        String id2 = UUID.randomUUID().toString();
        String partitionKey = createdCollection.getPartitionKey().paths().get(0).substring(1);
        String partitionKeyValue = "pk";
        Document document1 = new Document();
        document1.id(id1);
        BridgeInternal.setProperty(document1, partitionKey, partitionKeyValue);
        Document document2 = new Document();
        document2.id(id2);
        BridgeInternal.setProperty(document2, partitionKey, partitionKeyValue);
        try {
            asyncClientWithTokenResolver = buildClient(connectionMode, PermissionMode.ALL);
            OffsetDateTime befTime = OffsetDateTime.now();
            Thread.sleep(1000);

            document1 = asyncClientWithTokenResolver
                    .createDocument(createdCollection.selfLink(), document1, null, false).single().block()
                    .getResource();
            document2 = asyncClientWithTokenResolver
                    .createDocument(createdCollection.selfLink(), document2, null, false).single().block()
                    .getResource();
            List<String> expectedIds = new ArrayList<String>();
            String rid1 = document1.resourceId();
            String rid2 = document2.resourceId();
            expectedIds.add(rid1);
            expectedIds.add(rid2);

            ChangeFeedOptions options = new ChangeFeedOptions();
            options.partitionKey(new PartitionKey(partitionKeyValue));
            options.startDateTime(befTime);

            Thread.sleep(1000);
            Flux<FeedResponse<Document>> queryObservable = asyncClientWithTokenResolver
                    .queryDocumentChangeFeed(createdCollection.selfLink(), options);
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
            connectionPolicy.connectionMode(connectionMode);
            asyncClientWithTokenResolver = new AsyncDocumentClient.Builder()
                    .withServiceEndpoint(TestConfigurations.HOST)
                    .withConnectionPolicy(connectionPolicy)
                    .withConsistencyLevel(ConsistencyLevel.SESSION)
                    .withTokenResolver(getBadTokenResolver())
                    .build();

            RequestOptions options = new RequestOptions();
            options.setProperties(new HashMap<String, Object>());
            Flux<ResourceResponse<DocumentCollection>> readObservable = asyncClientWithTokenResolver.readCollection(createdCollection.selfLink(), options);
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
            connectionPolicy.connectionMode(connectionMode);
            asyncClientWithTokenResolver = new AsyncDocumentClient.Builder()
                    .withServiceEndpoint(TestConfigurations.HOST)
                    .withConnectionPolicy(connectionPolicy)
                    .withConsistencyLevel(ConsistencyLevel.SESSION)
                    .withTokenResolver(getTokenResolverWithBlockList(PermissionMode.READ, field, blockListedUser, errorMessage))
                    .build();
            
            RequestOptions options = new RequestOptions();
            HashMap<String, Object> properties = new HashMap<String, Object>();
            properties.put(field, blockListedUser);
            options.setProperties(properties);
            Flux<ResourceResponse<DocumentCollection>> readObservable = asyncClientWithTokenResolver.readCollection(createdCollection.selfLink(), options);
            FailureValidator validator = new FailureValidator.Builder().withRuntimeExceptionMessage(errorMessage).build();
            validateFailure(readObservable, validator);

            properties.put(field, new UserClass("valid user", 1));
            options.setProperties(properties);
            readObservable = asyncClientWithTokenResolver.readCollection(createdCollection.selfLink(), options);
            ResourceResponseValidator<DocumentCollection> sucessValidator = new ResourceResponseValidator.Builder<DocumentCollection>()
                    .withId(createdCollection.id()).build();
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
        connectionPolicy.connectionMode(connectionMode);
        return new AsyncDocumentClient.Builder()
                .withServiceEndpoint(TestConfigurations.HOST)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.SESSION)
                .withTokenResolver(getTokenResolver(permissionMode))
                .build();
    }

    private static User getUserDefinition() {
        User user = new User();
        user.id(UUID.randomUUID().toString());
        return user;
    }

    private Permission getPermission(Resource resource, String permissionId, PermissionMode permissionMode) {
        Permission permission = new Permission();
        permission.id(permissionId);
        permission.setPermissionMode(permissionMode);
        permission.setResourceLink(resource.selfLink());
        return permission;
    }

    private TokenResolver getTokenResolver(PermissionMode permissionMode) {
        return (String requestVerb, String resourceIdOrFullName, CosmosResourceType resourceType, Map<String, Object>  properties) -> {
            if (permissionMode == null) {
                return "invalid";
            } else if (permissionMode.equals(PermissionMode.READ)) {
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
                if (permissionMode.equals(PermissionMode.READ)) {
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