// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.DatabaseForTest;
import com.azure.cosmos.implementation.FailureValidator;
import com.azure.cosmos.implementation.FeedResponseListValidator;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.models.ContainerChildResourceType;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosPermissionProperties;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.CosmosUserProperties;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PermissionMode;
import com.azure.cosmos.rx.CosmosItemResponseValidator;
import com.azure.cosmos.rx.TestSuiteBase;
import com.azure.cosmos.util.CosmosPagedFlux;
import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ResourceTokenTestForV4 extends TestSuiteBase {
    public final String databaseId = DatabaseForTest.generateId();

    private CosmosAsyncDatabase createdDatabase;
    private CosmosAsyncContainer createdContainer;
    private CosmosAsyncContainer createdContainerWithPartitionKey;
    private TestObject createdItem;
    private TestObject createdItemWithPartitionKey;
    private TestObject createdItemWithPartitionKey2;
    private CosmosAsyncUser createdUser;
    private CosmosPermissionProperties createdContainerPermission;
    private CosmosPermissionProperties createdItemPermission;
    private CosmosPermissionProperties createdItemPermissionWithPartitionKey;
    private CosmosPermissionProperties createdItemPermissionWithPartitionKey2;
    private CosmosPermissionProperties createdContainerPermissionWithPartitionKey;
    private CosmosPermissionProperties createdContainerPermissionWithPartitionKey2;

    private CosmosAsyncClient client;

    // ALL static string used in below test cases
    private final static String PARTITION_KEY_PATH_1 = "/mypk";
    private final static String PARTITION_KEY_PATH_2 = "/mypk2";
    private static final String PARTITION_KEY_VALUE = "1";
    private static final String PARTITION_KEY_VALUE_2 = "2";
    private static final String USER_NAME = "TestUser";

    @Factory(dataProvider = "clientBuilders")
    public ResourceTokenTestForV4(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"simple"}, timeOut = SETUP_TIMEOUT)
    public void before_ResourceTokenTests() throws Exception {
        client = getClientBuilder().buildAsyncClient();

        client.createDatabaseIfNotExists(databaseId).block();
        createdDatabase = client.getDatabase(databaseId);

        // CREATE collection
        CosmosContainerProperties containerProperties =
            new CosmosContainerProperties(UUID.randomUUID().toString(), PARTITION_KEY_PATH_2);
        createdDatabase.createContainerIfNotExists(containerProperties).block();
        createdContainer = createdDatabase.getContainer(containerProperties.getId());

        // CREATE document
        CosmosItemRequestOptions requestOptions = new CosmosItemRequestOptions();
        requestOptions.setContentResponseOnWriteEnabled(true);
        // TestObject only has mypk which is different than the PARTITION_KEY_PATH_2
        createdItem = createdContainer.createItem(TestObject.create(), requestOptions).block().getItem();

        // CREATE collection with partition getKey
        CosmosContainerProperties container2Properties =
            new CosmosContainerProperties(UUID.randomUUID().toString(), PARTITION_KEY_PATH_1);
        createdDatabase.createContainerIfNotExists(container2Properties).block();
        createdContainerWithPartitionKey = createdDatabase.getContainer(container2Properties.getId());

        // CREATE first document with partition key
        createdItemWithPartitionKey =
            createdContainerWithPartitionKey.createItem(getDocumentDefinitionWithPartitionKey(), requestOptions).block().getItem();
        // CREATE second document with partition getKey
        createdItemWithPartitionKey2 =
            createdContainerWithPartitionKey.createItem(getDocumentDefinitionWithPartitionKey2(), requestOptions).block().getItem();

        // CREATE getUser
        CosmosUserProperties userProperties = new CosmosUserProperties();
        userProperties.setId(USER_NAME);
        createdUser = safeCreateUser(client, createdDatabase.getId(), userProperties);

        // CREATE getPermission for collection
        createdContainerPermission = createdUser.createPermission(getContainerPermission(), null).block().getProperties();

        // CREATE permission for document
        createdItemPermission = createdUser.createPermission(getItemPermission(), null).block().getProperties();

        // CREATE permission for document with partition key
        createdItemPermissionWithPartitionKey = createdUser
            .createPermission(getItemPermissionWithPartitionKey(), null).block().getProperties();

        // CREATE permission for document with partition key 2
        createdItemPermissionWithPartitionKey2 = createdUser
            .createPermission(getItemPermissionWithPartitionKey2(), null).block().getProperties();

        // CREATE permission for collection with partition key
        createdContainerPermissionWithPartitionKey = createdUser
            .createPermission(getContainerPermissionWithPartitionKey(), null).block().getProperties();

        // CREATE permission for collection with partition key
        createdContainerPermissionWithPartitionKey2 = createdUser
            .createPermission(getContainerPermissionWithPartitionKey2(), null).block().getProperties();
    }

    @DataProvider(name = "containerAndPermissionData")
    public Object[][] containerAndPermissionData() {
        return new Object[][]{
            //This test will try to read collection from its own getPermission and validate it.
            {createdContainer.getId(), createdContainerPermission}
        };
    }

    @DataProvider(name = "containerItemAndPermissionData")
    public Object[][] containerItemAndPermissionData() {
        return new Object[][]{
            //These tests will try to read document from its own getPermission and validate it.
            {createdContainer.getId(), createdItem.getId(), createdItemPermission, null},

            //These tests will try to read document from its getPermission having partition getKey 1 and validate it.
            {createdContainerWithPartitionKey.getId(), createdItemWithPartitionKey.getId(), createdItemPermissionWithPartitionKey, PARTITION_KEY_VALUE},

            //These tests will try to read document from its getPermission having partition getKey 2 and validate it.
            {createdContainerWithPartitionKey.getId(), createdItemWithPartitionKey2.getId(), createdItemPermissionWithPartitionKey2, PARTITION_KEY_VALUE_2},

            // These tests will try to read document from its parent collection getPermission and validate it.
            {createdContainer.getId(), createdItem.getId(), createdContainerPermission, null},

            //This test will try to read document from collection getPermission having partition getKey 1 and validate it.
            {createdContainerWithPartitionKey.getId(), createdItemWithPartitionKey.getId(), createdContainerPermissionWithPartitionKey, PARTITION_KEY_VALUE},

            //This test will try to read document from collection getPermission having partition getKey 2 and validate it.
            {createdContainerWithPartitionKey.getId(), createdItemWithPartitionKey2.getId(), createdContainerPermissionWithPartitionKey2, PARTITION_KEY_VALUE_2},
        };
    }

    @DataProvider(name = "containerItemAndPermissionDataForResourceNotFound")
    public Object[][] containerItemAndPermissionDataForResourceNotFound() {
        return new Object[][]{
            //This test will try to read document from its resource token directly and validate it.
            {createdContainerWithPartitionKey.getId(), createdItemWithPartitionKey2.getId(), createdContainerPermissionWithPartitionKey, PARTITION_KEY_VALUE}
        };
    }

    @DataProvider(name = "containerItemAndMultipleCollPermissionData")
    public Object[][] containerItemAndMultipleCollPermissionData() {
        return new Object[][]{
            //These tests will try to read item from partition 1 with two container getPermissions having different partition keys and validate it.
            {createdContainerWithPartitionKey.getId(), createdItemWithPartitionKey.getId(), createdContainerPermissionWithPartitionKey, createdContainerPermissionWithPartitionKey2,
                PARTITION_KEY_VALUE},

            //These tests will try to read item from partition 2 with two container getPermissions having different partition keys and validate it.
            {createdContainerWithPartitionKey.getId(), createdItemWithPartitionKey2.getId(), createdContainerPermissionWithPartitionKey, createdContainerPermissionWithPartitionKey2,
                PARTITION_KEY_VALUE_2},
        };
    }

    @DataProvider(name = "resourceToken")
    public Object[][] resourceToken() {
        return new Object[][]{
            //This test will try to read item from its resource token directly and validate it.
            {createdItemPermission.getToken()},
            //This test will try to read item from its parent container resource token directly and validate it.
            {createdContainerPermission.getToken()}
        };
    }

    @DataProvider(name = "queryItemPermissionData")
    public Object[][] queryItemPermissionData() {
        return new Object[][]{
            //This test will try to query collection from its own getPermission and validate it
            { createdContainerWithPartitionKey, createdContainerPermissionWithPartitionKey, new PartitionKey(PARTITION_KEY_VALUE) },
            { createdContainer, createdContainerPermission, PartitionKey.NONE },
        };
    }

    /**
     * This test will try to read container from permission and validate it.
     *
     * @throws Exception
     */
    @Test(groups = {"simple"}, dataProvider = "containerAndPermissionData", timeOut = TIMEOUT)
    public void readContainerFromPermissionFeed(String containerId, CosmosPermissionProperties permission) throws Exception {
        CosmosAsyncClient asyncClientResourceToken = null;
        try {
            List<CosmosPermissionProperties> permissionFeed = new ArrayList<>();
            permissionFeed.add(permission);

            asyncClientResourceToken = this.createAsyncClientWithPermission(permissionFeed);

            Mono<CosmosContainerResponse> readObservable =
                asyncClientResourceToken.getDatabase(createdDatabase.getId()).getContainer(containerId).read();

            CosmosResponseValidator<CosmosContainerResponse> validator = new CosmosResponseValidator.Builder<CosmosContainerResponse>()
                .withId(createdContainer.getId()).build();
            validateSuccess(readObservable, validator);
        } finally {
            safeClose(asyncClientResourceToken);
        }
    }

    /**
     * This test will try to read item from permission and validate it.
     *
     * @throws Exception
     */
    @Test(groups = {"simple"}, dataProvider = "containerItemAndPermissionData", timeOut = TIMEOUT)
    public void readItemFromPermissionFeed(
        String containerId,
        String itemId,
        CosmosPermissionProperties permission,
        String partitionKey) throws Exception {

        CosmosAsyncClient asyncClientResourceToken = null;
        try {
            List<CosmosPermissionProperties> permissionFeed = new ArrayList<>();
            permissionFeed.add(permission);

            asyncClientResourceToken = this.createAsyncClientWithPermission(permissionFeed);
            CosmosAsyncContainer container = asyncClientResourceToken.getDatabase(createdDatabase.getId()).getContainer(containerId);

            Mono<CosmosItemResponse<TestObject>> readObservable;
            if (StringUtils.isEmpty(partitionKey)) {
                readObservable = container.readItem(itemId, PartitionKey.NONE, TestObject.class);
            } else {
                readObservable = container.readItem(itemId, new PartitionKey(partitionKey), TestObject.class);
            }

            CosmosItemResponseValidator validator = new CosmosItemResponseValidator.Builder<CosmosItemResponse<TestObject>>()
                .withId(itemId).build();
            validateItemSuccess(readObservable, validator);
        } finally {
            safeClose(asyncClientResourceToken);
        }
    }

    /**
     * This test will try to read item from resource token directly and validate it.
     *
     * @throws Exception
     */
    @Test(groups = {"simple"}, dataProvider = "resourceToken", timeOut = TIMEOUT)
    public void readItemFromResourceToken(String resourceToken) throws Exception {
        CosmosAsyncClient asyncClientResourceToken = null;
        try {
            asyncClientResourceToken = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .resourceToken(resourceToken)
                .directMode(DirectConnectionConfig.getDefaultConfig())
                .consistencyLevel(ConsistencyLevel.SESSION)
                .buildAsyncClient();

            Mono<CosmosItemResponse<TestObject>> readObservable =
                asyncClientResourceToken
                    .getDatabase(createdDatabase.getId())
                    .getContainer(createdContainer.getId())
                    .readItem(createdItem.getId(), PartitionKey.NONE, TestObject.class);

            CosmosItemResponseValidator validator = new CosmosItemResponseValidator.Builder<CosmosItemResponse<TestObject>>()
                .withId(createdItem.getId()).build();
            validateItemSuccess(readObservable, validator);
        } finally {
            safeClose(asyncClientResourceToken);
        }
    }

    /**
     * This test will try to read item from multiple collection permissions having different keys and validate it.
     *
     * @throws Exception
     */
    @Test(groups = {"simple"}, dataProvider = "containerItemAndMultipleCollPermissionData", timeOut = TIMEOUT)
    public void readItemOfParKeyFromTwoCollPermissionWithDiffPartitionKeys(
        String containerId,
        String itemId,
        CosmosPermissionProperties collPermission1,
        CosmosPermissionProperties collPermission2,
        String partitionKey) throws Exception {

        CosmosAsyncClient asyncClientResourceToken = null;
        try {
            List<CosmosPermissionProperties> permissionFeed = new ArrayList<>();
            permissionFeed.add(collPermission1);
            permissionFeed.add(collPermission2);

            asyncClientResourceToken = this.createAsyncClientWithPermission(permissionFeed);

            Mono<CosmosItemResponse<TestObject>> readObservable =
                asyncClientResourceToken
                    .getDatabase(createdDatabase.getId())
                    .getContainer(containerId)
                    .readItem(itemId, new PartitionKey(partitionKey), TestObject.class);

            CosmosItemResponseValidator validator = new CosmosItemResponseValidator.Builder<CosmosItemResponse<TestObject>>()
                .withId(itemId).build();
            validateItemSuccess(readObservable, validator);
        } finally {
            safeClose(asyncClientResourceToken);
        }
    }

    /**
     * This test will try to read item with wrong container permission hence
     * expecting resource not found failure.
     *
     * @throws Exception
     */
    @Test(groups = {"simple"}, dataProvider = "containerItemAndPermissionDataForResourceNotFound", timeOut = TIMEOUT)
    public void readItemFromCollPermissionWithDiffPartitionKey_ResourceNotFound(
        String containerId,
        String itemId,
        CosmosPermissionProperties permission,
        String partitionKey) throws Exception {

        CosmosAsyncClient asyncClientResourceToken = null;
        try {
            List<CosmosPermissionProperties> permissionFeed = new ArrayList<>();
            permissionFeed.add(permission);

            asyncClientResourceToken = this.createAsyncClientWithPermission(permissionFeed);

            Mono<CosmosItemResponse<TestObject>> readObservable =
                asyncClientResourceToken
                    .getDatabase(createdDatabase.getId())
                    .getContainer(containerId)
                    .readItem(itemId, new PartitionKey(partitionKey), TestObject.class);

            FailureValidator validator = new FailureValidator.Builder().resourceNotFound().build();
            validateItemFailure(readObservable, validator);
        } finally {
            safeClose(asyncClientResourceToken);
        }
    }

    /**
     * This test will try to read item with container permissions and passing wrong partitionKey hence expecting exception.
     *
     * @throws Exception
     */
    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void readItemFromCollPermissionWithDiffPartitionKey_WithException() throws Exception {
        CosmosAsyncClient asyncClientResourceToken = null;
        try {
            List<CosmosPermissionProperties> permissionFeed = new ArrayList<>();
            permissionFeed.add(createdContainerPermissionWithPartitionKey);

            asyncClientResourceToken = this.createAsyncClientWithPermission(permissionFeed);

            Mono<CosmosItemResponse<TestObject>> readObservable =
                asyncClientResourceToken
                    .getDatabase(createdDatabase.getId())
                    .getContainer(createdContainerWithPartitionKey.getId())
                    .readItem(createdItemWithPartitionKey.getId(), new PartitionKey(PARTITION_KEY_PATH_2), TestObject.class);
            FailureValidator validator = new FailureValidator.Builder().resourceTokenNotFound().build();
            validateItemFailure(readObservable, validator);
        } finally {
            safeClose(asyncClientResourceToken);
        }
    }

    @Test(groups = {"simple"}, dataProvider = "queryItemPermissionData", timeOut = TIMEOUT)
    public void queryItemFromResourceToken(
        CosmosAsyncContainer container,
        CosmosPermissionProperties permission,
        PartitionKey partitionKey) throws Exception {

        CosmosAsyncClient asyncClientResourceToken = null;
        try {
            asyncClientResourceToken = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .gatewayMode()
                .consistencyLevel(ConsistencyLevel.SESSION)
                .resourceToken(permission.getToken())
                .buildAsyncClient();

            CosmosAsyncContainer asyncContainer =
                asyncClientResourceToken
                    .getDatabase(createdDatabase.getId())
                    .getContainer(container.getId());

            CosmosPagedFlux<TestObject> queryObservable;
            CosmosQueryRequestOptions queryRequestOptions = new CosmosQueryRequestOptions();
            queryRequestOptions.setPartitionKey(partitionKey);

            queryObservable = asyncContainer.queryItems("select * from c", queryRequestOptions, TestObject.class);
            FeedResponseListValidator<TestObject> validator = new FeedResponseListValidator.Builder<TestObject>()
                .totalSize(1)
                .numberOfPagesIsGreaterThanOrEqualTo(1)
                .build();

            validateQuerySuccess(queryObservable.byPage(100), validator);
        } finally {
            safeClose(asyncClientResourceToken);
        }
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void readAllItemFromResourceToken() throws Exception {

        CosmosAsyncClient asyncClientResourceToken = null;
        try {
            asyncClientResourceToken = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .gatewayMode()
                .consistencyLevel(ConsistencyLevel.SESSION)
                .resourceToken(createdContainerPermissionWithPartitionKey.getToken())
                .buildAsyncClient();

            CosmosAsyncContainer asyncContainer =
                asyncClientResourceToken
                    .getDatabase(createdDatabase.getId())
                    .getContainer(createdContainerWithPartitionKey.getId());

            CosmosPagedFlux<TestObject> readAllItemObservable =
                asyncContainer.readAllItems(new PartitionKey(PARTITION_KEY_VALUE), TestObject.class);
            FeedResponseListValidator<TestObject> validator = new FeedResponseListValidator.Builder<TestObject>()
                .totalSize(1)
                .numberOfPagesIsGreaterThanOrEqualTo(1)
                .build();

            validateQuerySuccess(readAllItemObservable.byPage(100), validator);
        } finally {
            safeClose(asyncClientResourceToken);
        }
    }

    private CosmosAsyncClient createAsyncClientWithPermission(List<CosmosPermissionProperties> permissions) {
        assertThat(permissions).isNotNull();

        return new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .gatewayMode()
            .consistencyLevel(ConsistencyLevel.SESSION)
            .permissions(permissions)
            .buildAsyncClient();
    }

    @AfterClass(groups = {"simple"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(createdDatabase);
        safeClose(client);
    }

    private CosmosPermissionProperties getContainerPermission() {
        return new CosmosPermissionProperties()
                .setId("PermissionForColl")
                .setPermissionMode(PermissionMode.READ)
                .setContainerName(createdContainer.getId());
    }

    private CosmosPermissionProperties getItemPermission() {
        return new CosmosPermissionProperties()
            .setId("PermissionForDoc")
            .setPermissionMode(PermissionMode.READ)
            .setContainerName(createdContainer.getId())
            .setResourcePath(ContainerChildResourceType.ITEM, createdItem.getId());
    }

    private CosmosPermissionProperties getItemPermissionWithPartitionKey() {
        return new CosmosPermissionProperties()
            .setId("PermissionForDocWithPartitionKey")
            .setPermissionMode(PermissionMode.READ)
            .setContainerName(createdContainerWithPartitionKey.getId())
            .setResourcePath(ContainerChildResourceType.ITEM, createdItemWithPartitionKey.getId())
            .setResourcePartitionKey(new PartitionKey(PARTITION_KEY_VALUE));
    }

    private CosmosPermissionProperties getItemPermissionWithPartitionKey2() {
        return new CosmosPermissionProperties()
            .setId("PermissionForDocWithPartitionKey2")
            .setPermissionMode(PermissionMode.READ)
            .setContainerName(createdContainerWithPartitionKey.getId())
            .setResourcePath(ContainerChildResourceType.ITEM, createdItemWithPartitionKey2.getId())
            .setResourcePartitionKey(new PartitionKey(PARTITION_KEY_VALUE_2));
    }

    private CosmosPermissionProperties getContainerPermissionWithPartitionKey() {
        return new CosmosPermissionProperties()
            .setId("PermissionForColWithPartitionKey")
            .setPermissionMode(PermissionMode.READ)
            .setContainerName(createdContainerWithPartitionKey.getId())
            .setResourcePartitionKey(new PartitionKey(PARTITION_KEY_VALUE));
    }

    private CosmosPermissionProperties getContainerPermissionWithPartitionKey2() {
        return new CosmosPermissionProperties()
            .setId("PermissionForColWithPartitionKey2")
            .setPermissionMode(PermissionMode.READ)
            .setContainerName(createdContainerWithPartitionKey.getId())
            .setResourcePartitionKey(new PartitionKey(PARTITION_KEY_VALUE_2));
    }

    private TestObject getDocumentDefinitionWithPartitionKey() {
        TestObject testObject = TestObject.create();
        testObject.setMypk(PARTITION_KEY_VALUE);

        return testObject;
    }

    private TestObject getDocumentDefinitionWithPartitionKey2() {
        TestObject testObject = TestObject.create();
        testObject.setMypk(PARTITION_KEY_VALUE_2);

        return testObject;
    }
}
