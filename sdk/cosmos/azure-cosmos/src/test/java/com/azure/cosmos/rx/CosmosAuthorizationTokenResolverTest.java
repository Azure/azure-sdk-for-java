// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.rx;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosAsyncStoredProcedure;
import com.azure.cosmos.CosmosAsyncUser;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosResponseValidator;
import com.azure.cosmos.implementation.CosmosResourceType;
import com.azure.cosmos.implementation.FailureValidator;
import com.azure.cosmos.implementation.FeedResponseListValidator;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.models.CosmosAuthorizationTokenResolver;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerRequestOptions;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosPermissionProperties;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.CosmosStoredProcedureProperties;
import com.azure.cosmos.models.CosmosStoredProcedureRequestOptions;
import com.azure.cosmos.models.CosmosStoredProcedureResponse;
import com.azure.cosmos.models.CosmosUserProperties;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PermissionMode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class CosmosAuthorizationTokenResolverTest extends TestSuiteBase {
    private final static Logger log = LoggerFactory.getLogger(CosmosAuthorizationTokenResolverTest.class);
    private static final ObjectMapper OBJECT_MAPPER = Utils.getSimpleObjectMapper();

    private class UserClass {
        public String userName;
        public int userId;

        public UserClass(String userName, int userId) {
            this.userName = userName;
            this.userId = userId;
        }
    }

    private CosmosAsyncDatabase createdDatabase;
    private CosmosAsyncContainer createdCollection;
    private CosmosAsyncUser userWithReadPermission;
    private CosmosAsyncUser userWithAllPermission;

    private CosmosPermissionProperties readPermission;
    private CosmosPermissionProperties allPermission;

    private CosmosAsyncClient client;

    @Factory(dataProvider = "clientBuilders")
    public CosmosAuthorizationTokenResolverTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @DataProvider(name = "connectionMode")
    public Object[][] connectionMode() {
        return new Object[][]{
                {ConnectionMode.GATEWAY},
                {ConnectionMode.DIRECT},
        };
    }

    @BeforeClass(groups = { "emulator" }, timeOut = SETUP_TIMEOUT)
    public void before_TokenResolverTest() throws InterruptedException {
        this.client = getClientBuilder().buildAsyncClient();

        createdDatabase = getSharedCosmosDatabase(this.client); // SHARED_DATABASE
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();
        CosmosContainerProperties collectionDefinition = new CosmosContainerProperties(
            "monitor_" + UUID.randomUUID(),
            "/mypk");
        createdCollection = createCollection(createdDatabase, collectionDefinition, options);


        userWithReadPermission = createUser(client, createdDatabase.getId(), getUserDefinition());

        //create read permission
        Thread.sleep(1000);
        CosmosPermissionProperties permissionReadSettings = new CosmosPermissionProperties()
            .setId("ReadPermissionOnColl")
            .setPermissionMode(PermissionMode.READ)
            .setContainerName(createdCollection.getId());

        readPermission = userWithReadPermission.createPermission(permissionReadSettings, null).block().getProperties();

        userWithAllPermission = createUser(client, createdDatabase.getId(), getUserDefinition());
        //create all permission
        Thread.sleep(1000);
        CosmosPermissionProperties permissionAllSettings = new CosmosPermissionProperties()
            .setId("AllPermissionOnColl")
            .setPermissionMode(PermissionMode.ALL)
            .setContainerName(createdCollection.getId());

        allPermission = userWithAllPermission.createPermission(permissionAllSettings, null).block().getProperties();
    }

    @Test(groups = { "emulator" }, dataProvider = "connectionMode", timeOut = TIMEOUT)
    public void readDocumentWithReadPermission(ConnectionMode connectionMode) {
        InternalObjectNode docDef = getDocumentDefinition();
        InternalObjectNode doc = createdCollection.createItem(docDef).block().getItem();

        CosmosAsyncClient asyncClientWithTokenResolver = null;

        try {

            asyncClientWithTokenResolver = buildClientWithAuthTokenResolver(connectionMode, PermissionMode.READ);

            Mono<CosmosItemResponse<InternalObjectNode>> readItemObservable = asyncClientWithTokenResolver
                .getDatabase(createdDatabase.getId())
                .getContainer(createdCollection.getId())
                .readItem(doc.getId(), new PartitionKey(doc.getId()), InternalObjectNode.class);

            CosmosItemResponseValidator validator =
                new CosmosItemResponseValidator.Builder<CosmosItemResponse<InternalObjectNode>>()
                    .withId(doc.getId())
                    .build();
            this.validateItemSuccess(readItemObservable, validator);
        } finally {
            safeClose(asyncClientWithTokenResolver);
        }
    }

    @Test(groups = { "emulator" }, dataProvider = "connectionMode", timeOut = TIMEOUT)
    public void deleteDocumentWithReadPermission(ConnectionMode connectionMode) {
        InternalObjectNode docDef = getDocumentDefinition();
        InternalObjectNode doc = createdCollection.createItem(docDef).block().getItem();

        CosmosAsyncClient asyncClientWithTokenResolver = null;

        try {
            asyncClientWithTokenResolver = buildClientWithAuthTokenResolver(connectionMode, PermissionMode.READ);

            Mono<CosmosItemResponse<Object>> deleteItemObservable = asyncClientWithTokenResolver
                .getDatabase(createdDatabase.getId())
                .getContainer(createdCollection.getId())
                .deleteItem(doc.getId(), new PartitionKey(doc.getId()));

            FailureValidator validator = new FailureValidator
                .Builder()
                .statusCode(HttpConstants.StatusCodes.FORBIDDEN)
                .build();
            validateItemFailure(deleteItemObservable, validator);
        } finally {
            safeClose(asyncClientWithTokenResolver);
        }
    }

    @Test(groups = { "emulator" }, dataProvider = "connectionMode", timeOut = TIMEOUT)
    public void writeDocumentWithReadPermission(ConnectionMode connectionMode) {
        InternalObjectNode docDef = getDocumentDefinition();
        CosmosAsyncClient asyncClientWithTokenResolver = null;

        try {
            asyncClientWithTokenResolver = buildClientWithAuthTokenResolver(connectionMode, PermissionMode.READ);

            Mono<CosmosItemResponse<Object>> createItemObservable = asyncClientWithTokenResolver
                .getDatabase(createdDatabase.getId())
                .getContainer(createdCollection.getId())
                .createItem(docDef);

            FailureValidator validator = new FailureValidator
                .Builder()
                .statusCode(HttpConstants.StatusCodes.FORBIDDEN)
                .build();
            validateItemFailure(createItemObservable, validator);
        } finally {
            safeClose(asyncClientWithTokenResolver);
        }
    }

    @Test(groups = { "emulator" }, dataProvider = "connectionMode", timeOut = TIMEOUT)
    public void writeDocumentWithAllPermission(ConnectionMode connectionMode) {
        InternalObjectNode docDef = getDocumentDefinition();

        CosmosAsyncClient asyncClientWithTokenResolver = null;

        try {

            asyncClientWithTokenResolver = buildClientWithAuthTokenResolver(connectionMode, PermissionMode.ALL);

            Mono<CosmosItemResponse<InternalObjectNode>> createItemObservable = asyncClientWithTokenResolver
                .getDatabase(createdDatabase.getId())
                .getContainer(createdCollection.getId())
                .createItem(docDef);

            CosmosItemResponseValidator validator =
                new CosmosItemResponseValidator.Builder<CosmosItemResponse<InternalObjectNode>>()
                    .withId(docDef.getId())
                    .build();
            this.validateItemSuccess(createItemObservable, validator);
        } finally {
            safeClose(asyncClientWithTokenResolver);
        }
    }

    @Test(groups = { "emulator" }, dataProvider = "connectionMode", timeOut = TIMEOUT)
    public void deleteDocumentWithAllPermission(ConnectionMode connectionMode) {
        InternalObjectNode docDef = getDocumentDefinition();
        InternalObjectNode doc = createdCollection.createItem(docDef).block().getItem();

        CosmosAsyncClient asyncClientWithTokenResolver = null;

        try {
            asyncClientWithTokenResolver = buildClientWithAuthTokenResolver(connectionMode, PermissionMode.ALL);

            Mono<CosmosItemResponse<Object>> deleteItemObservable = asyncClientWithTokenResolver
                .getDatabase(createdDatabase.getId())
                .getContainer(createdCollection.getId())
                .deleteItem(doc.getId(), new PartitionKey(doc.getId()));

            CosmosItemResponseValidator validator =
                new CosmosItemResponseValidator.Builder<CosmosItemResponse<Object>>()
                    .nullResource()
                    .build();
            this.validateItemSuccess(deleteItemObservable, validator);
        } finally {
            safeClose(asyncClientWithTokenResolver);
        }
    }

    @Test(groups = { "emulator" }, dataProvider = "connectionMode", timeOut = TIMEOUT)
    public void readCollectionWithReadPermission(ConnectionMode connectionMode) {
        CosmosAsyncClient asyncClientWithTokenResolver = null;

        try {
            asyncClientWithTokenResolver = buildClientWithAuthTokenResolver(connectionMode, PermissionMode.READ);

            Mono<CosmosContainerResponse> readCollectionObservable = asyncClientWithTokenResolver
                .getDatabase(createdDatabase.getId())
                .getContainer(createdCollection.getId())
                .read();

            CosmosResponseValidator<CosmosContainerResponse> validator =
                new CosmosResponseValidator.Builder<CosmosContainerResponse>()
                    .withId(createdCollection.getId())
                    .build();
            this.validateSuccess(readCollectionObservable, validator);
        } finally {
            safeClose(asyncClientWithTokenResolver);
        }
    }

    @Test(groups = { "emulator" }, dataProvider = "connectionMode", timeOut = TIMEOUT)
    public void deleteCollectionWithReadPermission(ConnectionMode connectionMode) throws InterruptedException{
        CosmosAsyncClient asyncClientWithTokenResolver = null;

        try {
            asyncClientWithTokenResolver = buildClientWithAuthTokenResolver(connectionMode, PermissionMode.READ);

            Mono<CosmosContainerResponse> deleteCollectionObservable = asyncClientWithTokenResolver
                .getDatabase(createdDatabase.getId())
                .getContainer(createdCollection.getId())
                .delete();

            FailureValidator validator = new FailureValidator
                .Builder()
                .statusCode(HttpConstants.StatusCodes.FORBIDDEN)
                .build();
            validateFailure(deleteCollectionObservable, validator);
        } finally {
            safeClose(asyncClientWithTokenResolver);
        }
    }

    // TBD: Validate if deleting the current container using a resource token is allowed and expected.
//    @Test(groups = { "emulator" }, dataProvider = "connectionMode", timeOut = TIMEOUT)
    public void deleteCollectionWithAllPermission(ConnectionMode connectionMode) throws InterruptedException{
        InternalObjectNode docDef = getDocumentDefinition();
        InternalObjectNode doc = createdCollection.createItem(docDef).block().getItem();

        CosmosAsyncClient asyncClientWithTokenResolver = null;

        try {
            asyncClientWithTokenResolver = buildClientWithAuthTokenResolver(connectionMode, PermissionMode.ALL);

            Mono<CosmosContainerResponse> deleteCollectionObservable = asyncClientWithTokenResolver
                .getDatabase(createdDatabase.getId())
                .getContainer(createdCollection.getId())
                .delete();

            FailureValidator validator = new FailureValidator
                .Builder()
                .statusCode(HttpConstants.StatusCodes.FORBIDDEN)
                .build();
            validateFailure(deleteCollectionObservable, validator);
        } finally {
            safeClose(asyncClientWithTokenResolver);
        }
    }

    @Test(groups = { "emulator" }, dataProvider = "connectionMode", timeOut = 6000000)
    public void createAndExecuteSprocWithWritePermission(ConnectionMode connectionMode) throws InterruptedException {
        CosmosAsyncClient asyncClientWithTokenResolver = null;

        try {
            asyncClientWithTokenResolver = buildClientWithAuthTokenResolver(connectionMode, PermissionMode.ALL);
            String sprocId = "storedProcedure" + UUID.randomUUID().toString();
            CosmosStoredProcedureProperties storedProcedureDef = ModelBridgeInternal
                .createCosmosStoredProcedureProperties("{" + "  'id': '" + sprocId + "',"
                    + "  'body':" + "    'function () {" + "      for (var i = 0; i < 10; i++) {"
                    + "        getContext().getResponse().appendValue(\"Body\", i);" + "      }" + "    }'" + "}");

            Mono<CosmosStoredProcedureResponse> createObservable = asyncClientWithTokenResolver
                .getDatabase(createdDatabase.getId())
                .getContainer(createdCollection.getId())
                .getScripts()
                .createStoredProcedure(storedProcedureDef, new CosmosStoredProcedureRequestOptions());

            CosmosResponseValidator<CosmosStoredProcedureResponse> validator = new CosmosResponseValidator.Builder<CosmosStoredProcedureResponse>()
                .withId(storedProcedureDef.getId())
                .notNullEtag()
                .build();

            validateSuccess(createObservable, validator);

            Thread.sleep(1000);

            CosmosAsyncStoredProcedure storedProcedure = null;
            storedProcedure = asyncClientWithTokenResolver
                .getDatabase(createdDatabase.getId())
                .getContainer(createdCollection.getId())
                .getScripts()
                .getStoredProcedure(storedProcedureDef.getId());

            String result = null;

            CosmosStoredProcedureRequestOptions options = new CosmosStoredProcedureRequestOptions();
            options.setPartitionKey(PartitionKey.NONE);
            result = storedProcedure.execute(null, options).block().getResponseAsString();

            assertThat(result).isEqualTo("\"0123456789\"");

        } finally {
            safeClose(asyncClientWithTokenResolver);
        }
    }

    @Test(groups = { "emulator" }, dataProvider = "connectionMode", timeOut = TIMEOUT)
    public void readDocumentsWithAllPermission(ConnectionMode connectionMode) {
        InternalObjectNode docDef1 = getDocumentDefinition();
        InternalObjectNode docDef2 = getDocumentDefinition();

        CosmosAsyncClient asyncClientWithTokenResolver = null;

        try {

            asyncClientWithTokenResolver = buildClientWithAuthTokenResolver(connectionMode, PermissionMode.ALL);
            InternalObjectNode createItem1 = asyncClientWithTokenResolver
                .getDatabase(createdDatabase.getId())
                .getContainer(createdCollection.getId())
                .createItem(docDef1)
                .block()
                .getItem();
            InternalObjectNode createItem2 = asyncClientWithTokenResolver
                .getDatabase(createdDatabase.getId())
                .getContainer(createdCollection.getId())
                .createItem(docDef2)
                .block()
                .getItem();

            List<String> expectedIds = new ArrayList<String>();
            String rid1 = createItem1.getResourceId();
            String rid2 = createItem2.getResourceId();
            expectedIds.add(rid1);
            expectedIds.add(rid2);
            String query = "SELECT * FROM r WHERE r._rid=\"" + rid1 + "\" or r._rid=\"" + rid2 + "\"";

            CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
            Flux<FeedResponse<InternalObjectNode>> queryObservable = asyncClientWithTokenResolver
                .getDatabase(createdDatabase.getId())
                .getContainer(createdCollection.getId())
                .queryItems(query, InternalObjectNode.class)
                .byPage();

            FeedResponseListValidator<InternalObjectNode> validator = new FeedResponseListValidator.Builder<InternalObjectNode>()
                .totalSize(2)
                .exactlyContainsInAnyOrder(expectedIds).build();
            validateQuerySuccess(queryObservable, validator, TIMEOUT);
        } finally {
            safeClose(asyncClientWithTokenResolver);
        }
    }

    @Test(groups = { "emulator" }, dataProvider = "connectionMode", timeOut = TIMEOUT)
    public void readChangeFeedWithAllPermission(ConnectionMode connectionMode) throws InterruptedException {
        String uuid = UUID.randomUUID().toString();
        String uuid1 = UUID.randomUUID().toString();
        InternalObjectNode docDef1 = new InternalObjectNode(String.format("{ "
                + "\"id\": \"%s\", "
                + "\"mypk\": \"%s\", "
                + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]"
                + "}"
            , uuid1, uuid));

        String uuid2= UUID.randomUUID().toString();
        InternalObjectNode docDef2 = new InternalObjectNode(String.format("{ "
                + "\"id\": \"%s\", "
                + "\"mypk\": \"%s\", "
                + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]"
                + "}"
            , uuid2, uuid));

        CosmosAsyncClient asyncClientWithTokenResolver = null;

        try {

            asyncClientWithTokenResolver = buildClientWithAuthTokenResolver(connectionMode, PermissionMode.ALL);
            InternalObjectNode createItem1 = asyncClientWithTokenResolver
                .getDatabase(createdDatabase.getId())
                .getContainer(createdCollection.getId())
                .createItem(docDef1)
                .block()
                .getItem();
            InternalObjectNode createItem2 = asyncClientWithTokenResolver
                .getDatabase(createdDatabase.getId())
                .getContainer(createdCollection.getId())
                .createItem(docDef2)
                .block()
                .getItem();

            List<String> expectedIds = new ArrayList<String>();
            String id1 = createItem1.getId();
            String id2 = createItem2.getId();
            expectedIds.add(id1);
            expectedIds.add(id2);

            CosmosChangeFeedRequestOptions options =
                CosmosChangeFeedRequestOptions.createForProcessingFromBeginning(FeedRange.forLogicalPartition(new PartitionKey(uuid)));

            Thread.sleep(1000);
            Flux<FeedResponse<InternalObjectNode>> queryObservable = asyncClientWithTokenResolver
                .getDatabase(createdDatabase.getId())
                .getContainer(createdCollection.getId())
                .queryChangeFeed(options, InternalObjectNode.class)
                .byPage();
            FeedResponseListValidator<InternalObjectNode> validator = new FeedResponseListValidator.Builder<InternalObjectNode>()
                    .exactlyContainsIdsInAnyOrder(expectedIds).build();
            validateQuerySuccess(queryObservable, validator, TIMEOUT);
        } finally {
            safeClose(asyncClientWithTokenResolver);
        }
    }

    @Test(groups = { "emulator" }, dataProvider = "connectionMode", timeOut = TIMEOUT)
    public void verifyRuntimeExceptionWhenUserModifiesProperties(ConnectionMode connectionMode) throws InterruptedException {
        CosmosAsyncClient asyncClientWithTokenResolver = null;

        try {
            CosmosClientBuilder builder = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .authorizationTokenResolver(getBadTokenResolver())
                .consistencyLevel(ConsistencyLevel.SESSION)
                .contentResponseOnWriteEnabled(true);

            if (connectionMode.equals(ConnectionMode.DIRECT)) {
                builder = builder.directMode();
            } else {
                builder = builder.gatewayMode();
            }
            asyncClientWithTokenResolver = builder.buildAsyncClient();

            Mono<CosmosContainerResponse> readCollectionObservable = asyncClientWithTokenResolver
                .getDatabase(createdDatabase.getId())
                .getContainer(createdCollection.getId())
                .read();

            FailureValidator validator = new FailureValidator.Builder()
                .statusCode(HttpConstants.StatusCodes.UNAUTHORIZED)
                .build();
            validateFailure(readCollectionObservable, validator);
        } finally {
            safeClose(asyncClientWithTokenResolver);
        }
    }


    @AfterClass(groups = { "emulator" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        client.close();
    }

    private CosmosAsyncClient buildClientWithAuthTokenResolver(ConnectionMode connectionMode, PermissionMode permissionMode) {
        CosmosClientBuilder builder = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .authorizationTokenResolver(getTokenResolver(permissionMode))
            .consistencyLevel(ConsistencyLevel.SESSION)
            .contentResponseOnWriteEnabled(true);

        if (connectionMode.equals(ConnectionMode.DIRECT)) {
            builder = builder.directMode();
        } else {
            builder = builder.gatewayMode();
        }

        return builder
            .buildAsyncClient();
    }

    private static CosmosUserProperties getUserDefinition() {
        CosmosUserProperties user = new CosmosUserProperties();
        user.setId(UUID.randomUUID().toString());
        return user;
    }

    private CosmosAuthorizationTokenResolver getTokenResolver(PermissionMode permissionMode) {
        return (String requestVerb, String resourceIdOrFullName, String resourceType, Map<String, Object>  properties) -> {
            if(resourceType.equals(CosmosResourceType.SYSTEM.toString())) {
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
        return (String requestVerb, String resourceIdOrFullName, String resourceType, Map<String, Object>  properties) -> {
            if (resourceType.equals(CosmosResourceType.DOCUMENT_COLLECTION.toString())) {
                return "";
            }
            return readPermission.getToken();
        };
    }

    private InternalObjectNode getDocumentDefinition() {
        String uuid = UUID.randomUUID().toString();
        InternalObjectNode doc = new InternalObjectNode(String.format("{ "
                + "\"id\": \"%s\", "
                + "\"mypk\": \"%s\", "
                + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]"
                + "}"
            , uuid, uuid));
        return doc;
    }
}
