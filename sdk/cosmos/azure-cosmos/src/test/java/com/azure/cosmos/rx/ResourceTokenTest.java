// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.implementation.FeedResponseListValidator;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.models.PermissionMode;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.Database;
import com.azure.cosmos.implementation.DatabaseForTest;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.FailureValidator;
import com.azure.cosmos.implementation.Permission;
import com.azure.cosmos.implementation.RequestOptions;
import com.azure.cosmos.implementation.ResourceResponse;
import com.azure.cosmos.implementation.ResourceResponseValidator;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.TestSuiteBase;
import com.azure.cosmos.implementation.TestUtils;
import com.azure.cosmos.implementation.User;
import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * This class try to test different scenario related to fetching various
 * resources from resource token directly or via permission feed .
 *
 */

// TODO change to use external TestSuiteBase
public class ResourceTokenTest extends TestSuiteBase {
    public final String databaseId = DatabaseForTest.generateId();

    private Database createdDatabase;
    private DocumentCollection createdCollection;
    private DocumentCollection createdCollectionWithPartitionKey;
    private Document createdDocument;
    private Document createdDocumentWithPartitionKey;
    private Document createdDocumentWithPartitionKey2;
    private User createdUser;
    private Permission createdCollPermission;
    private Permission createdCollPermissionWithName;
    private Permission createdDocPermission;
    private Permission createdDocPermissionWithName;
    private Permission createdDocPermissionWithPartitionKey;
    private Permission createdDocPermissionWithPartitionKeyWithName;
    private Permission createdDocPermissionWithPartitionKey2;
    private Permission createdDocPermissionWithPartitionKey2WithName;
    private Permission createdColPermissionWithPartitionKey;
    private Permission createdColPermissionWithPartitionKeyWithName;
    private Permission createdColPermissionWithPartitionKey2;
    private Permission createdColPermissionWithPartitionKey2WithName;

    private AsyncDocumentClient client;

    // ALL static string used in below test cases
    private final static String DOCUMENT_DEFINITION = "{ 'id': 'doc%d', 'counter': '%d'}";
    private final static String DOCUMENT_DEFINITION_WITH_PERMISSION_KEY = "{ " + "\"id\": \"%s\", "
            + "\"mypk\": \"%s\", " + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]" + "}";
    private final static String PARTITION_KEY_PATH_1 = "/mypk";
    private final static String PARTITION_KEY_PATH_2 = "/mypk2";

    private static final String PARTITION_KEY_VALUE = "1";
    private static final String PARTITION_KEY_VALUE_2 = "2";
    private static final String PERMISSION_DEFINITION = "{" + "    'id': 'PermissionForDocWithPartitionKey',"
            + "    'permissionMode': 'read'," + "    'resource': '%s'," + "    'resourcePartitionKey': ['%s']" + "}";
    private static final String COLLECTION_PERMISSION_DEFINITION = "{" + "    'id': 'PermissionForColWithPartitionKey',"
            + "    'permissionMode': 'read'," + "    'resource': '%s'," + "    'resourcePartitionKey': ['%s']" + "}";
    private static final String USER_NAME = "TestUser";
    private static final String PERMISSION_FOR_COLL = "PermissionForColl";
    private static final String PERMISSION_FOR_COLL_WITH_NAME = "PermissionForCollWithName";
    private static final String PERMISSION_FOR_DOC = "PermissionForDoc";
    private static final String PERMISSION_FOR_DOC_WITH_NAME = "PermissionForDocWithName";

    @Factory(dataProvider = "clientBuilders")
    public ResourceTokenTest(AsyncDocumentClient.Builder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void before_ResourceTokenTest() throws Exception {
        client = clientBuilder().build();
        Database d = new Database();
        d.setId(databaseId);
        createdDatabase = createDatabase(client, d);
        // CREATE collection
        createdCollection = createCollection(client, createdDatabase.getId(), getCollectionDefinitionWithPartitionKey(PARTITION_KEY_PATH_2));
        // CREATE document
        createdDocument = createDocument(client, createdDatabase.getId(),createdCollection.getId(), getDocument());
        // CREATE collection with partition getKey
        createdCollectionWithPartitionKey = createCollection(client, createdDatabase.getId(), getCollectionDefinitionWithPartitionKey(PARTITION_KEY_PATH_1));
        // CREATE document with partition getKey
        createdDocumentWithPartitionKey = createDocument(client, createdDatabase.getId(), createdCollectionWithPartitionKey.getId(),
                getDocumentDefinitionWithPartitionKey());
        // CREATE second document with partition getKey
        createdDocumentWithPartitionKey2 = createDocument(client, createdDatabase.getId(),createdCollectionWithPartitionKey.getId(),
                getDocumentDefinitionWithPartitionKey2());
        // CREATE getUser
        createdUser = createUser(client, createdDatabase.getId(), getUserDefinition());
        // CREATE getPermission for collection
        createdCollPermission = client.createPermission(getUserLink(), getCollPermission(), null).block()
                .getResource();
        createdCollPermissionWithName = client.createPermission(getUserLink(), getCollPermissionWithName(), null).block()
                .getResource();
        // CREATE permission for document
        createdDocPermission = client.createPermission(getUserLink(), getDocPermission(), null).block()
                .getResource();
        createdDocPermissionWithName = client.createPermission(getUserLink(), getDocPermissionWithName(), null).block()
                .getResource();
        // CREATE permission for document with partition key
        createdDocPermissionWithPartitionKey = client
                .createPermission(getUserLink(), getDocPermissionWithPartitionKey(), null).block()
                .getResource();
        createdDocPermissionWithPartitionKeyWithName = client
                .createPermission(getUserLink(), getDocPermissionWithPartitionKeyWithName(), null).block()
                .getResource();
        // CREATE permission for document with partition key 2
        createdDocPermissionWithPartitionKey2 = client
                .createPermission(getUserLink(), getDocPermissionWithPartitionKey2(), null).block()
                .getResource();
        createdDocPermissionWithPartitionKey2WithName = client
                .createPermission(getUserLink(), getDocPermissionWithPartitionKey2WithName(), null).block()
                .getResource();
        // CREATE permission for collection with partition key
        createdColPermissionWithPartitionKey = client
                .createPermission(getUserLink(), getColPermissionWithPartitionKey(), null).block()
                .getResource();
        createdColPermissionWithPartitionKeyWithName = client
                .createPermission(getUserLink(), getColPermissionWithPartitionKeyWithName(), null).block()
                .getResource();
        // CREATE permission for collection with partition key
        createdColPermissionWithPartitionKey2 = client
                .createPermission(getUserLink(), getColPermissionWithPartitionKey2(), null).block()
                .getResource();
        createdColPermissionWithPartitionKey2WithName = client
                .createPermission(getUserLink(), getColPermissionWithPartitionKey2WithName(), null).block()
                .getResource();
    }

    @DataProvider(name = "collectionAndPermissionData")
    public Object[][] collectionAndPermissionData() {
        return new Object[][]{
                //This test will try to read collection from its own getPermission and validate it, both with request Id and getName.
                {createdCollection.getSelfLink(), createdCollPermission},
                {TestUtils.getCollectionNameLink(createdDatabase.getId(), createdCollection.getId()), createdDocPermissionWithName},
        };
    }

    @DataProvider(name = "documentAndPermissionData")
    public Object[][] documentAndPermissionData() {
        return new Object[][]{
                 //These tests will try to read document from its own getPermission and validate it, both with request Id and getName.
                {createdDocument.getSelfLink(), createdDocPermission, createdDocument.getId(), null},
                {TestUtils.getDocumentNameLink(createdDatabase.getId(), createdCollection.getId(), createdDocument.getId()), createdDocPermissionWithName, createdDocument.getId(), null},

                //These tests will try to read document from its getPermission having partition getKey 1 and validate it, both with request Id and getName.
                {createdDocumentWithPartitionKey.getSelfLink(), createdDocPermissionWithPartitionKey, createdDocumentWithPartitionKey.getId(), PARTITION_KEY_VALUE},
                {TestUtils.getDocumentNameLink(createdDatabase.getId(), createdCollectionWithPartitionKey.getId(), createdDocumentWithPartitionKey.getId()), createdDocPermissionWithPartitionKeyWithName
                        , createdDocumentWithPartitionKey.getId(), PARTITION_KEY_VALUE},

                //These tests will try to read document from its getPermission having partition getKey 2 and validate it, both with request Id and getName.
                {createdDocumentWithPartitionKey2.getSelfLink(), createdDocPermissionWithPartitionKey2, createdDocumentWithPartitionKey2.getId(), PARTITION_KEY_VALUE_2},
                {TestUtils.getDocumentNameLink(createdDatabase.getId(), createdCollectionWithPartitionKey.getId(), createdDocumentWithPartitionKey2.getId()),
                        createdDocPermissionWithPartitionKey2WithName, createdDocumentWithPartitionKey2.getId(), PARTITION_KEY_VALUE_2},

                // These tests will try to read document from its parent collection getPermission and validate it, both with request Id and getName.
                {createdDocument.getSelfLink(), createdCollPermission, createdDocument.getId(), null},
                {TestUtils.getDocumentNameLink(createdDatabase.getId(), createdCollection.getId(), createdDocument.getId()), createdCollPermissionWithName, createdDocument.getId(), null},

                //This test will try to read document from collection getPermission having partition getKey 1 and validate it, both with request Id and getName.
                {createdDocumentWithPartitionKey.getSelfLink(), createdColPermissionWithPartitionKey, createdDocumentWithPartitionKey.getId(), PARTITION_KEY_VALUE},
                {TestUtils.getDocumentNameLink(createdDatabase.getId(), createdCollectionWithPartitionKey.getId(), createdDocumentWithPartitionKey.getId()), createdColPermissionWithPartitionKeyWithName, createdDocumentWithPartitionKey.getId(), PARTITION_KEY_VALUE},

                //This test will try to read document from collection getPermission having partition getKey 2 and validate it, both with request Id and getName.
                {createdDocumentWithPartitionKey2.getSelfLink(), createdColPermissionWithPartitionKey2, createdDocumentWithPartitionKey2.getId(), PARTITION_KEY_VALUE_2},
                {TestUtils.getDocumentNameLink(createdDatabase.getId(), createdCollectionWithPartitionKey.getId(), createdDocumentWithPartitionKey2.getId()), createdColPermissionWithPartitionKey2WithName, createdDocumentWithPartitionKey2.getId(), PARTITION_KEY_VALUE_2}

        };
    }

    @DataProvider(name = "documentAndPermissionDataForResourceNotFound")
    public Object[][] documentAndPermissionDataForResourceNotFound() {
        return new Object[][]{
                //This test will try to read document from its resource token directly and validate it.
                {createdDocumentWithPartitionKey2.getSelfLink(), createdColPermissionWithPartitionKey, PARTITION_KEY_VALUE},
                //This test will try to read document from its parent collection resource token directly and validate it.
                {TestUtils.getDocumentNameLink(createdDatabase.getId(), createdCollectionWithPartitionKey.getId(), createdDocumentWithPartitionKey2.getId()),
                        createdColPermissionWithPartitionKeyWithName, PARTITION_KEY_VALUE}
        };
    }

    @DataProvider(name = "documentAndMultipleCollPermissionData")
    public Object[][] documentAndMultipleCollPermissionData() {
        return new Object[][]{
                //These tests will try to read document from partition 1 with two collection getPermissions having different partition keys and validate it, both with request Id and getName.
                {createdDocumentWithPartitionKey.getSelfLink(), createdColPermissionWithPartitionKey, createdColPermissionWithPartitionKey2, createdDocumentWithPartitionKey.getId(),
                PARTITION_KEY_VALUE},
                {TestUtils.getDocumentNameLink(createdDatabase.getId(), createdCollectionWithPartitionKey.getId(), createdDocumentWithPartitionKey.getId()), createdColPermissionWithPartitionKeyWithName
                        , createdColPermissionWithPartitionKey2WithName, createdDocumentWithPartitionKey.getId(), PARTITION_KEY_VALUE},

                //These tests will try to read document from partition 1 with two collection getPermissions having different partition keys and validate it, both with request Id and getName.
                {createdDocumentWithPartitionKey2.getSelfLink(), createdColPermissionWithPartitionKey, createdColPermissionWithPartitionKey2, createdDocumentWithPartitionKey2.getId(),
                        PARTITION_KEY_VALUE_2},
                {TestUtils.getDocumentNameLink(createdDatabase.getId(), createdCollectionWithPartitionKey.getId(), createdDocumentWithPartitionKey2.getId()), createdColPermissionWithPartitionKeyWithName
                        , createdColPermissionWithPartitionKey2WithName, createdDocumentWithPartitionKey2.getId(), PARTITION_KEY_VALUE_2}
        };
    }

    @DataProvider(name = "resourceToken")
    public Object[][] resourceToken() {
        return new Object[][]{
                //This test will try to read document from its resource token directly and validate it.
                {createdDocPermission.getToken()},
                //This test will try to read document from its parent collection resource token directly and validate it.
                {createdCollPermission.getToken()}
        };
    }

    @DataProvider(name = "queryItemPermissionData")
    public Object[][] queryItemPermissionData() {
        return new Object[][]{
            //This test will try to query collection from its own getPermission and validate it, both with request Id and getName.
            { createdCollectionWithPartitionKey, createdColPermissionWithPartitionKey, new PartitionKey(PARTITION_KEY_VALUE) },
            { createdCollectionWithPartitionKey, createdColPermissionWithPartitionKeyWithName, new PartitionKey(PARTITION_KEY_VALUE) },
            { createdCollection, createdCollPermission, PartitionKey.NONE },
        };
    }

    /**
     * This test will try to read collection from permission and validate it.
     *
     * @throws Exception
     */
    @Test(groups = { "simple" }, dataProvider = "collectionAndPermissionData", timeOut = TIMEOUT)
    public void readCollectionFromPermissionFeed(String collectionUrl, Permission permission) throws Exception {
        AsyncDocumentClient asyncClientResourceToken = null ;
        try {
            List<Permission> permissionFeed = new ArrayList<>();
            permissionFeed.add(permission);
            asyncClientResourceToken = new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                    .withPermissionFeed(permissionFeed).withConnectionPolicy(ConnectionPolicy.getDefaultPolicy())
                    .withConsistencyLevel(ConsistencyLevel.SESSION).build();
            Mono<ResourceResponse<DocumentCollection>> readObservable = asyncClientResourceToken
                    .readCollection(collectionUrl, null);

            ResourceResponseValidator<DocumentCollection> validator = new ResourceResponseValidator.Builder<DocumentCollection>()
                   .withId(createdCollection.getId()).build();
            validateSuccess(readObservable, validator);
        } finally {
            safeClose(asyncClientResourceToken);
        }
    }

    /**
     * This test will try to read document from permission and validate it.
     *
     * @throws Exception
     */
    @Test(groups = { "simple" }, dataProvider = "documentAndPermissionData", timeOut = TIMEOUT)
    public void readDocumentFromPermissionFeed(String documentUrl, Permission permission, String documentId, String partitionKey) throws Exception {
        AsyncDocumentClient asyncClientResourceToken = null;
        try {
            List<Permission> permissionFeed = new ArrayList<>();
            permissionFeed.add(permission);
            ConnectionPolicy defaultPolicy = ConnectionPolicy.getDefaultPolicy();
            defaultPolicy.setConnectionMode(ConnectionMode.GATEWAY);
            asyncClientResourceToken = new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                    .withPermissionFeed(permissionFeed).withConnectionPolicy(defaultPolicy)
                    .withConsistencyLevel(ConsistencyLevel.SESSION).build();
            RequestOptions options = new RequestOptions();
            if (StringUtils.isNotEmpty(partitionKey)) {
                options.setPartitionKey(new PartitionKey(partitionKey));
            } else {
                options.setPartitionKey(PartitionKey.NONE);
            }
            Mono<ResourceResponse<Document>> readObservable = asyncClientResourceToken
                    .readDocument(documentUrl, options);
            ResourceResponseValidator<Document> validator = new ResourceResponseValidator.Builder<Document>()
                    .withId(documentId).build();
            validateSuccess(readObservable, validator);
        } finally {
            safeClose(asyncClientResourceToken);
        }
    }

    /**
     * This test will try to read document from resource token directly and validate it.
     *
     * @throws Exception
     */
    @Test(groups = { "simple" }, dataProvider = "resourceToken", timeOut = TIMEOUT)
    public void readDocumentFromResouceToken(String resourceToken) throws Exception {
        AsyncDocumentClient asyncClientResourceToken = null;
        try {
            asyncClientResourceToken = new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                    .withMasterKeyOrResourceToken(resourceToken)
                    .withConnectionPolicy(ConnectionPolicy.getDefaultPolicy()).withConsistencyLevel(ConsistencyLevel.SESSION)
                    .build();
            RequestOptions options = new RequestOptions();
            options.setPartitionKey(PartitionKey.NONE);
            Mono<ResourceResponse<Document>> readObservable = asyncClientResourceToken
                    .readDocument(createdDocument.getSelfLink(), options);
            ResourceResponseValidator<Document> validator = new ResourceResponseValidator.Builder<Document>()
                    .withId(createdDocument.getId()).build();
            validateSuccess(readObservable, validator);
        } finally {
            safeClose(asyncClientResourceToken);
        }
    }

    /**
     * This test will try to read document from multiple collection permissions having different keys and validate it.
     *
     * @throws Exception
     */
    @Test(groups = {"simple"}, dataProvider = "documentAndMultipleCollPermissionData", timeOut = TIMEOUT)
    public void readDocumentOfParKeyFromTwoCollPermissionWithDiffPartitionKeys(String documentUrl, Permission collPermission1, Permission collPermission2, String documentId, String partitionKey) throws Exception {
        AsyncDocumentClient asyncClientResourceToken = null;
        try {
            List<Permission> permissionFeed = new ArrayList<>();
            permissionFeed.add(collPermission1);
            permissionFeed.add(collPermission2);
            ConnectionPolicy defaultPolicy = ConnectionPolicy.getDefaultPolicy();
            defaultPolicy.setConnectionMode(ConnectionMode.GATEWAY);
            asyncClientResourceToken = new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                    .withPermissionFeed(permissionFeed).withConnectionPolicy(defaultPolicy)
                    .withConsistencyLevel(ConsistencyLevel.SESSION).build();
            RequestOptions options = new RequestOptions();
            options.setPartitionKey(new PartitionKey(partitionKey));
            Mono<ResourceResponse<Document>> readObservable = asyncClientResourceToken
                    .readDocument(documentUrl, options);
            ResourceResponseValidator<Document> validator = new ResourceResponseValidator.Builder<Document>()
                    .withId(documentId).build();
            validateSuccess(readObservable, validator);
        } finally {
            safeClose(asyncClientResourceToken);
        }
    }

    /**
     * This test will try to read document with wrong collection permission hence
     * expecting resource not found failure.
     *
     * @throws Exception
     */
    @Test(groups = { "simple" },dataProvider = "documentAndPermissionDataForResourceNotFound", timeOut = TIMEOUT)
    public void readDocumentFromCollPermissionWithDiffPartitionKey_ResourceNotFound(String documentUrl, Permission permission, String partitionKey) throws Exception {
        AsyncDocumentClient asyncClientResourceToken = null;
        try {
            List<Permission> permissionFeed = new ArrayList<>();
            permissionFeed.add(permission);
            ConnectionPolicy defaultPolicy = ConnectionPolicy.getDefaultPolicy();
            defaultPolicy.setConnectionMode(ConnectionMode.GATEWAY);
            asyncClientResourceToken = new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                    .withPermissionFeed(permissionFeed).withConnectionPolicy(defaultPolicy)
                    .withConsistencyLevel(ConsistencyLevel.SESSION).build();
            RequestOptions options = new RequestOptions();
            options.setPartitionKey(new PartitionKey(partitionKey));
            Mono<ResourceResponse<Document>> readObservable = asyncClientResourceToken
                    .readDocument(documentUrl, options);
            FailureValidator validator = new FailureValidator.Builder().resourceNotFound().build();
            validateFailure(readObservable, validator);
        } finally {
            safeClose(asyncClientResourceToken);
        }
    }

    /**
     * This test will try to read document with collection permissions and passing wrong partitionkey
     * in request options hence expecting exception.
     *
     * @throws Exception
     */
    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readDocumentFromCollPermissionWithDiffPartitionKey_WithException() throws Exception {
        AsyncDocumentClient asyncClientResourceToken = null;
        try {
            List<Permission> permissionFeed = new ArrayList<>();
            permissionFeed.add(createdColPermissionWithPartitionKey);
            asyncClientResourceToken = new AsyncDocumentClient.Builder()
                .withServiceEndpoint(TestConfigurations.HOST)
                .withConnectionPolicy(ConnectionPolicy.getDefaultPolicy())
                .withConsistencyLevel(ConsistencyLevel.SESSION)
                .withPermissionFeed(permissionFeed)
                .build();
            RequestOptions options = new RequestOptions();
            options.setPartitionKey(new PartitionKey(PARTITION_KEY_VALUE_2));
            Mono<ResourceResponse<Document>> readObservable = asyncClientResourceToken
                    .readDocument(createdDocumentWithPartitionKey.getSelfLink(), options);
            FailureValidator validator = new FailureValidator.Builder().resourceTokenNotFound().build();
            validateFailure(readObservable, validator);
        } finally {
            safeClose(asyncClientResourceToken);
        }
    }

    @Test(groups = { "simple" }, dataProvider = "queryItemPermissionData", timeOut = TIMEOUT)
    public void queryItemFromResourceToken(DocumentCollection documentCollection, Permission permission, PartitionKey partitionKey) throws Exception {

        AsyncDocumentClient asyncClientResourceToken = null;
        try {
            List<Permission> permissionFeed = new ArrayList<>();
            permissionFeed.add(permission);

            asyncClientResourceToken = new AsyncDocumentClient.Builder()
                .withServiceEndpoint(TestConfigurations.HOST)
                .withConnectionPolicy(ConnectionPolicy.getDefaultPolicy())
                .withConsistencyLevel(ConsistencyLevel.SESSION)
                .withMasterKeyOrResourceToken(permission.getToken())
                .build();

            CosmosQueryRequestOptions queryRequestOptions = new CosmosQueryRequestOptions();
            queryRequestOptions.setPartitionKey(partitionKey);
            Flux<FeedResponse<Document>> queryObservable =
                asyncClientResourceToken.queryDocuments(
                    documentCollection.getAltLink(),
                    "select * from c",
                    queryRequestOptions);

            FeedResponseListValidator<Document> validator = new FeedResponseListValidator.Builder<Document>()
                .totalSize(1)
                .numberOfPagesIsGreaterThanOrEqualTo(1)
                .build();

            validateQuerySuccess(queryObservable, validator);
        } finally {
            safeClose(asyncClientResourceToken);
        }
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(client, databaseId);
        safeClose(client);
    }

    private static User getUserDefinition() {
        User user = new User();
        user.setId(USER_NAME);
        return user;
    }

    private static Document getDocument() {
        Document doc = new Document(String.format(DOCUMENT_DEFINITION, 1, 1));
        return doc;
    }

    private Permission getCollPermission() {
        Permission permission = new Permission();
        permission.setId(PERMISSION_FOR_COLL);
        permission.setPermissionMode(PermissionMode.READ);
        permission.setResourceLink(createdCollection.getSelfLink());
        return permission;
    }

    private Permission getCollPermissionWithName() {
        Permission permission = new Permission();
        permission.setId(PERMISSION_FOR_COLL_WITH_NAME);
        permission.setPermissionMode(PermissionMode.READ);
        permission.setResourceLink(TestUtils.getCollectionNameLink(createdDatabase.getId(), createdCollection.getId()));
        return permission;
    }

    private Permission getDocPermission() {
        Permission permission = new Permission();
        permission.setId(PERMISSION_FOR_DOC);
        permission.setPermissionMode(PermissionMode.READ);
        permission.setResourceLink(createdDocument.getSelfLink());
        return permission;
    }
    private Permission getDocPermissionWithName() {
        Permission permission = new Permission();
        permission.setId(PERMISSION_FOR_DOC_WITH_NAME);
        permission.setPermissionMode(PermissionMode.READ);
        permission.setResourceLink(TestUtils.getDocumentNameLink(createdDatabase.getId(),createdCollection.getId(),createdDocument.getId()));
        return permission;
    }

    private Permission getDocPermissionWithPartitionKey() {
        String permissionStr = String.format(PERMISSION_DEFINITION, createdDocumentWithPartitionKey.getSelfLink(),
                PARTITION_KEY_VALUE);
        Permission permission = new Permission(permissionStr);
        return permission;
    }

    private Permission getDocPermissionWithPartitionKeyWithName() {
        String permissionStr = String.format(PERMISSION_DEFINITION, TestUtils.getDocumentNameLink(createdDatabase.getId(), createdCollectionWithPartitionKey.getId(), createdDocumentWithPartitionKey.getId()),
                PARTITION_KEY_VALUE);
        Permission permission = new Permission(permissionStr);
        permission.setId("PermissionForDocWithPartitionKeyWithName");
        return permission;
    }

    private Permission getDocPermissionWithPartitionKey2() {
        String permissionStr = String.format(PERMISSION_DEFINITION, createdDocumentWithPartitionKey2.getSelfLink(),
                PARTITION_KEY_VALUE_2);
        Permission permission = new Permission(permissionStr);
        permission.setId("PermissionForDocWithPartitionKey2");
        return permission;
    }

    private Permission getDocPermissionWithPartitionKey2WithName() {
        String permissionStr = String.format(PERMISSION_DEFINITION, TestUtils.getDocumentNameLink(createdDatabase.getId(),createdCollectionWithPartitionKey.getId(),createdDocumentWithPartitionKey2.getId()),
                PARTITION_KEY_VALUE_2);
        Permission permission = new Permission(permissionStr);
        permission.setId("PermissionForDocWithPartitionKey2WithName");
        return permission;
    }

    private Permission getColPermissionWithPartitionKey() {
        String permissionStr = String.format(COLLECTION_PERMISSION_DEFINITION, createdCollectionWithPartitionKey.getSelfLink(),
                PARTITION_KEY_VALUE);
        Permission permission = new Permission(permissionStr);
        return permission;
    }

    private Permission getColPermissionWithPartitionKeyWithName() {
        String permissionStr = String.format(COLLECTION_PERMISSION_DEFINITION, TestUtils.getCollectionNameLink(createdDatabase.getId(), createdCollectionWithPartitionKey.getId()),
                PARTITION_KEY_VALUE);
        Permission permission = new Permission(permissionStr);
        permission.setId("PermissionForColWithPartitionKeyWithName");
        return permission;
    }

    private Permission getColPermissionWithPartitionKey2() {
        String permissionStr = String.format(COLLECTION_PERMISSION_DEFINITION, createdCollectionWithPartitionKey.getSelfLink(),
                PARTITION_KEY_VALUE_2);
        Permission permission = new Permission(permissionStr);
        permission.setId("PermissionForColWithPartitionKey2");
        return permission;
    }

    private Permission getColPermissionWithPartitionKey2WithName() {
        String permissionStr = String.format(COLLECTION_PERMISSION_DEFINITION, TestUtils.getCollectionNameLink(createdDatabase.getId(), createdCollectionWithPartitionKey.getId()),
                PARTITION_KEY_VALUE_2);
        Permission permission = new Permission(permissionStr);
        permission.setId("PermissionForColWithPartitionKey2WithName");
        return permission;
    }

    private String getUserLink() {
        return createdUser.getSelfLink();
    }

    private Document getDocumentDefinitionWithPartitionKey() {
        String uuid = UUID.randomUUID().toString();
        Document doc = new Document(String.format(DOCUMENT_DEFINITION_WITH_PERMISSION_KEY, uuid, PARTITION_KEY_VALUE));
        return doc;
    }
       private Document getDocumentDefinitionWithPartitionKey2() {
            String uuid = UUID.randomUUID().toString();
            Document doc = new Document(String.format(DOCUMENT_DEFINITION_WITH_PERMISSION_KEY, uuid, PARTITION_KEY_VALUE_2));
            return doc;
        }

    private DocumentCollection getCollectionDefinitionWithPartitionKey(String pkDefPath) {
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<String>();
        paths.add(pkDefPath);
        partitionKeyDef.setPaths(paths);

        DocumentCollection collectionDefinition = new DocumentCollection();
        collectionDefinition.setId(UUID.randomUUID().toString());
        collectionDefinition.setPartitionKey(partitionKeyDef);

        return collectionDefinition;
    }
}
