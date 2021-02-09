// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.rx;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.GatewayConnectionConfig;
import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.implementation.CosmosResourceType;
import com.azure.cosmos.implementation.feedranges.FeedRangePartitionKeyImpl;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PermissionMode;
import com.azure.cosmos.implementation.RequestVerb;
import com.azure.cosmos.implementation.Resource;
import com.azure.cosmos.implementation.CosmosAuthorizationTokenResolver;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.Database;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.FailureValidator;
import com.azure.cosmos.implementation.FeedResponseListValidator;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.Permission;
import com.azure.cosmos.implementation.RequestOptions;
import com.azure.cosmos.implementation.ResourceResponse;
import com.azure.cosmos.implementation.ResourceResponseValidator;
import com.azure.cosmos.implementation.StoredProcedure;
import com.azure.cosmos.implementation.StoredProcedureResponse;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.TestSuiteBase;
import com.azure.cosmos.implementation.User;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Ignore("CosmosAuthorizationTokenResolver is removed from public")
public class CosmosAuthorizationTokenResolverTest extends TestSuiteBase {

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
    public CosmosAuthorizationTokenResolverTest(AsyncDocumentClient.Builder clientBuilder) {
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
    public void before_TokenResolverTest() {
        createdDatabase = SHARED_DATABASE;
        createdCollection = SHARED_MULTI_PARTITION_COLLECTION;

        client = clientBuilder().build();

        userWithReadPermission = createUser(client, createdDatabase.getId(), getUserDefinition());
        readPermission = client.createPermission(userWithReadPermission.getSelfLink(), getPermission(createdCollection, "ReadPermissionOnColl", PermissionMode.READ), null).block()
                .getResource();

        userWithAllPermission = createUser(client, createdDatabase.getId(), getUserDefinition());
        allPermission = client.createPermission(userWithAllPermission.getSelfLink(), getPermission(createdCollection, "AllPermissionOnColl", PermissionMode.ALL), null).block()
                .getResource();
    }

    @Test(groups = {"simple"}, dataProvider = "connectionMode", timeOut = TIMEOUT)
    public void readDocumentWithReadPermission(ConnectionMode connectionMode) {
        Document docDefinition = getDocumentDefinition();
        ResourceResponse<Document> resourceResponse = client
                .createDocument(BridgeInternal.getAltLink(createdCollection), docDefinition, null, false).block();
        AsyncDocumentClient asyncClientWithTokenResolver = null;
        try {
            asyncClientWithTokenResolver = buildClient(connectionMode, PermissionMode.READ);
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.setPartitionKey(new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(resourceResponse.getResource(), "mypk")));
            HashMap<String, Object> properties = new HashMap<String, Object>();
            properties.put("UserId", "readUser");
            requestOptions.setProperties(properties);
            Mono<ResourceResponse<Document>> readObservable = asyncClientWithTokenResolver.readDocument(resourceResponse.getResource().getSelfLink(), requestOptions);
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
                .createDocument(BridgeInternal.getAltLink(createdCollection), docDefinition, null, false).block();
        AsyncDocumentClient asyncClientWithTokenResolver = null;
        try {
            asyncClientWithTokenResolver = buildClient(connectionMode, PermissionMode.READ);
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.setPartitionKey(new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(resourceResponse.getResource(), "mypk")));
            Mono<ResourceResponse<Document>> readObservable = asyncClientWithTokenResolver.deleteDocument(resourceResponse.getResource().getSelfLink(), requestOptions);
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
            Mono<ResourceResponse<Document>> readObservable = asyncClientWithTokenResolver.createDocument(createdCollection.getSelfLink(), getDocumentDefinition(), null, true);
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
            asyncClientWithTokenResolver = buildClient(connectionMode, PermissionMode.ALL);
            Document documentDefinition = getDocumentDefinition();
            Mono<ResourceResponse<Document>> readObservable = asyncClientWithTokenResolver.createDocument(createdCollection.getSelfLink(), documentDefinition, null, true);
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
                .createDocument(BridgeInternal.getAltLink(createdCollection), docDefinition, null, false).block();
        AsyncDocumentClient asyncClientWithTokenResolver = null;
        try {
            asyncClientWithTokenResolver = buildClient(connectionMode, PermissionMode.ALL);
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.setPartitionKey(new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(resourceResponse.getResource(), "mypk")));
            Mono<ResourceResponse<Document>> readObservable = asyncClientWithTokenResolver.deleteDocument(resourceResponse.getResource().getSelfLink(), requestOptions);
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
            Mono<ResourceResponse<DocumentCollection>> readObservable = asyncClientWithTokenResolver.readCollection(createdCollection.getSelfLink(), null);
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
            asyncClientWithTokenResolver = buildClient(connectionMode, PermissionMode.READ);
            Mono<ResourceResponse<DocumentCollection>> readObservable = asyncClientWithTokenResolver.deleteCollection(createdCollection.getSelfLink(), null);
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
                .createDocument(BridgeInternal.getAltLink(createdCollection), docDefinition, null, false).block();
        AsyncDocumentClient asyncClientWithTokenResolver = null;
        try {
            ConnectionPolicy connectionPolicy;
            if (connectionMode.equals(ConnectionMode.DIRECT)) {
                connectionPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());
            } else {
                connectionPolicy = new ConnectionPolicy(GatewayConnectionConfig.getDefaultConfig());
            }

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
                    .withContentResponseOnWriteEnabled(true)
                    .build();
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.setPartitionKey(new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(resourceResponse.getResource(), "mypk")));
            Mono<ResourceResponse<Document>> readObservable = asyncClientWithTokenResolver.readDocument(resourceResponse.getResource().getSelfLink(), requestOptions);
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
                    .withContentResponseOnWriteEnabled(true)
                    .build();
            readObservable = asyncClientWithTokenResolver.readDocument(resourceResponse.getResource().getSelfLink(), requestOptions);
            ResourceResponseValidator<Document> sucessValidator = new ResourceResponseValidator.Builder<Document>()
                    .withId(resourceResponse.getResource().getId()).build();
            validateSuccess(readObservable, sucessValidator);


            //Success read operation with valid permission feed, supporting above hypothesis.
            asyncClientWithTokenResolver = new AsyncDocumentClient.Builder()
                    .withServiceEndpoint(TestConfigurations.HOST)
                    .withConnectionPolicy(connectionPolicy)
                    .withConsistencyLevel(ConsistencyLevel.SESSION)
                    .withPermissionFeed(permissionFeed)
                    .withContentResponseOnWriteEnabled(true)
                    .build();
            readObservable = asyncClientWithTokenResolver.readDocument(resourceResponse.getResource().getSelfLink(), requestOptions);
            validateSuccess(readObservable, sucessValidator);


            //Success read operation with valid master key, supporting above hypothesis.
            asyncClientWithTokenResolver = new AsyncDocumentClient.Builder()
                    .withServiceEndpoint(TestConfigurations.HOST)
                    .withConnectionPolicy(connectionPolicy)
                    .withConsistencyLevel(ConsistencyLevel.SESSION)
                    .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                    .withContentResponseOnWriteEnabled(true)
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

            Mono<ResourceResponse<StoredProcedure>> createObservable = asyncClientWithTokenResolver.createStoredProcedure(createdCollection.getSelfLink(), sproc, null);
            ResourceResponseValidator<StoredProcedure> createSucessValidator = new ResourceResponseValidator.Builder<StoredProcedure>()
                    .withId(sprocId).build();
            validateSuccess(createObservable, createSucessValidator);

            RequestOptions options = new RequestOptions();
            options.setPartitionKey(new PartitionKey(""));
            String sprocLink = "dbs/" + createdDatabase.getId() + "/colls/" + createdCollection.getId() + "/sprocs/" + sprocId;
            StoredProcedureResponse result = asyncClientWithTokenResolver.executeStoredProcedure(sprocLink, options, null).block();
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
            Document document1 = asyncClientWithTokenResolver.createDocument(createdCollection.getSelfLink(), new Document("{'id': '" + id1 + "'}"), null, false)
                    .block().getResource();
            Document document2 = asyncClientWithTokenResolver.createDocument(createdCollection.getSelfLink(), new Document("{'id': '" + id2 + "'}"), null, false)
                    .block().getResource();
            List<String> expectedIds = new ArrayList<String>();
            String rid1 = document1.getResourceId();
            String rid2 = document2.getResourceId();
            expectedIds.add(rid1);
            expectedIds.add(rid2);
            String query = "SELECT * FROM r WHERE r._rid=\"" + rid1 + "\" or r._rid=\"" + rid2 + "\"";

            CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

            Flux<FeedResponse<Document>> queryObservable = asyncClientWithTokenResolver.queryDocuments(createdCollection.getSelfLink(), query, options);
            FeedResponseListValidator<Document> validator = new FeedResponseListValidator.Builder<Document>()
                .totalSize(2)
                .exactlyContainsInAnyOrder(expectedIds).build();
            validateQuerySuccess(queryObservable, validator, TIMEOUT);
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
        String partitionKey = createdCollection.getPartitionKey().getPaths().get(0).substring(1);
        String partitionKeyValue = "pk";
        Document document1 = new Document();
        document1.setId(id1);
        BridgeInternal.setProperty(document1, partitionKey, partitionKeyValue);
        Document document2 = new Document();
        document2.setId(id2);
        BridgeInternal.setProperty(document2, partitionKey, partitionKeyValue);
        try {
            asyncClientWithTokenResolver = buildClient(connectionMode, PermissionMode.ALL);
            Instant befTime = Instant.now();
            Thread.sleep(1500);

            document1 = asyncClientWithTokenResolver
                    .createDocument(
                        createdCollection.getSelfLink(),
                        document1,
                        null,
                        false)
                    .block()
                    .getResource();
            document2 = asyncClientWithTokenResolver
                    .createDocument(
                        createdCollection.getSelfLink(),
                        document2,
                        null,
                        false)
                    .block()
                    .getResource();
            List<String> expectedIds = new ArrayList<String>();
            String rid1 = document1.getResourceId();
            String rid2 = document2.getResourceId();
            expectedIds.add(rid1);
            expectedIds.add(rid2);

            FeedRange feedRange = new FeedRangePartitionKeyImpl(
                ModelBridgeInternal.getPartitionKeyInternal(new PartitionKey(partitionKeyValue)));
            CosmosChangeFeedRequestOptions options =
                CosmosChangeFeedRequestOptions.createForProcessingFromPointInTime(befTime, feedRange);

            Thread.sleep(1000);
            Flux<FeedResponse<Document>> queryObservable = asyncClientWithTokenResolver
                    .queryDocumentChangeFeed(createdCollection, options);
            FeedResponseListValidator<Document> validator = new FeedResponseListValidator.Builder<Document>()
                    .exactlyContainsInAnyOrder(expectedIds).build();
            validateQuerySuccess(queryObservable, validator, TIMEOUT);
        } finally {
            safeClose(asyncClientWithTokenResolver);
        }
    }

    @Test(groups = {"simple"}, dataProvider = "connectionMode", timeOut = TIMEOUT)
    public void verifyRuntimeExceptionWhenUserModifiesProperties(ConnectionMode connectionMode) {
        AsyncDocumentClient asyncClientWithTokenResolver = null;

        try {
            ConnectionPolicy connectionPolicy;
            if (connectionMode.equals(ConnectionMode.DIRECT)) {
                connectionPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());
            } else {
                connectionPolicy = new ConnectionPolicy(GatewayConnectionConfig.getDefaultConfig());
            }
            asyncClientWithTokenResolver = new AsyncDocumentClient.Builder()
                    .withServiceEndpoint(TestConfigurations.HOST)
                    .withConnectionPolicy(connectionPolicy)
                    .withConsistencyLevel(ConsistencyLevel.SESSION)
                    .withTokenResolver(getBadTokenResolver())
                    .withContentResponseOnWriteEnabled(true)
                    .build();

            RequestOptions options = new RequestOptions();
            options.setProperties(new HashMap<String, Object>());
            Mono<ResourceResponse<DocumentCollection>> readObservable = asyncClientWithTokenResolver.readCollection(createdCollection.getSelfLink(), options);
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
            ConnectionPolicy connectionPolicy;
            if (connectionMode.equals(ConnectionMode.DIRECT)) {
                connectionPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());
            } else {
                connectionPolicy = new ConnectionPolicy(GatewayConnectionConfig.getDefaultConfig());
            }
            asyncClientWithTokenResolver = new AsyncDocumentClient.Builder()
                    .withServiceEndpoint(TestConfigurations.HOST)
                    .withConnectionPolicy(connectionPolicy)
                    .withConsistencyLevel(ConsistencyLevel.SESSION)
                    .withTokenResolver(getTokenResolverWithBlockList(PermissionMode.READ, field, blockListedUser, errorMessage))
                    .withContentResponseOnWriteEnabled(true)
                    .build();

            RequestOptions options = new RequestOptions();
            HashMap<String, Object> properties = new HashMap<String, Object>();
            properties.put(field, blockListedUser);
            options.setProperties(properties);
            Mono<ResourceResponse<DocumentCollection>> readObservable = asyncClientWithTokenResolver.readCollection(createdCollection.getSelfLink(), options);
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
        ConnectionPolicy connectionPolicy;
        if (connectionMode.equals(ConnectionMode.DIRECT)) {
            connectionPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());
        } else {
            connectionPolicy = new ConnectionPolicy(GatewayConnectionConfig.getDefaultConfig());
        }
        return new AsyncDocumentClient.Builder()
                .withServiceEndpoint(TestConfigurations.HOST)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.SESSION)
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

    private CosmosAuthorizationTokenResolver getTokenResolver(PermissionMode permissionMode) {
        return (RequestVerb requestVerb, String resourceIdOrFullName, CosmosResourceType resourceType, Map<String, Object>  properties) -> {
            if(resourceType.equals(CosmosResourceType.SYSTEM)) {
                return readPermission.getToken();
            } if (permissionMode == null) {
                return "invalid";
            } else if (permissionMode.equals(PermissionMode.READ)) {
                return readPermission.getToken();
            } else {
                return allPermission.getToken();
            }
        };
    }

    private CosmosAuthorizationTokenResolver getBadTokenResolver() {
        return (RequestVerb requestVerb, String resourceIdOrFullName, CosmosResourceType resourceType, Map<String, Object>  properties) -> {
            if (resourceType.equals(CosmosResourceType.SYSTEM)) {
                return readPermission.getToken();
            }
            if (properties != null) {
                properties.put("key", "value");
            }
            return null;
        };
    }

    private CosmosAuthorizationTokenResolver getTokenResolverWithBlockList(PermissionMode permissionMode, String field, UserClass blockListedUser, String errorMessage) {
        return (RequestVerb requestVerb, String resourceIdOrFullName, CosmosResourceType resourceType, Map<String, Object>  properties) -> {
            UserClass currentUser = null;
            if (properties != null && properties.get(field) != null) {
                currentUser = (UserClass) properties.get(field);
            }

            if (resourceType.equals(CosmosResourceType.SYSTEM)) {
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
