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
package com.azure.data.cosmos.rx;

import com.azure.data.cosmos.AsyncDocumentClient;
import com.azure.data.cosmos.ConnectionPolicy;
import com.azure.data.cosmos.ConsistencyLevel;
import com.azure.data.cosmos.Database;
import com.azure.data.cosmos.DatabaseForTest;
import com.azure.data.cosmos.Document;
import com.azure.data.cosmos.DocumentCollection;
import com.azure.data.cosmos.PartitionKey;
import com.azure.data.cosmos.PartitionKeyDefinition;
import com.azure.data.cosmos.Permission;
import com.azure.data.cosmos.PermissionMode;
import com.azure.data.cosmos.RequestOptions;
import com.azure.data.cosmos.ResourceResponse;
import com.azure.data.cosmos.User;
import com.azure.data.cosmos.internal.TestSuiteBase;
import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import rx.Observable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * This class try to test different scenario related to fetching various
 * resources from resource token directly or via permission feed .
 *
 */

// TODO: change to use external TestSuiteBase
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
    public void beforeClass() throws Exception {
        client = clientBuilder().build();
        Database d = new Database();
        d.id(databaseId);
        createdDatabase = createDatabase(client, d);
        // CREATE collection
        createdCollection = createCollection(client, createdDatabase.id(), getCollectionDefinitionWithPartitionKey(PARTITION_KEY_PATH_2));
        // CREATE document
        createdDocument = createDocument(client, createdDatabase.id(),createdCollection.id(), getDocument());
        // CREATE collection with partition key
        createdCollectionWithPartitionKey = createCollection(client, createdDatabase.id(), getCollectionDefinitionWithPartitionKey(PARTITION_KEY_PATH_1));
        // CREATE document with partition key
        createdDocumentWithPartitionKey = createDocument(client, createdDatabase.id(), createdCollectionWithPartitionKey.id(),
                getDocumentDefinitionWithPartitionKey());
        // CREATE second document with partition key
        createdDocumentWithPartitionKey2 = createDocument(client, createdDatabase.id(),createdCollectionWithPartitionKey.id(),
                getDocumentDefinitionWithPartitionKey2());
        // CREATE user
        createdUser = createUser(client, createdDatabase.id(), getUserDefinition());
        // CREATE permission for collection
        createdCollPermission = client.createPermission(getUserLink(), getCollPermission(), null).toBlocking().single()
                .getResource();
        createdCollPermissionWithName = client.createPermission(getUserLink(), getCollPermissionWithName(), null).toBlocking().single()
                .getResource();
        // CREATE permission for document
        createdDocPermission = client.createPermission(getUserLink(), getDocPermission(), null).toBlocking().single()
                .getResource();
        createdDocPermissionWithName = client.createPermission(getUserLink(), getDocPermissionWithName(), null).toBlocking().single()
                .getResource();
        // CREATE permission for document with partition key
        createdDocPermissionWithPartitionKey = client
                .createPermission(getUserLink(), getDocPermissionWithPartitionKey(), null).toBlocking().single()
                .getResource();
        createdDocPermissionWithPartitionKeyWithName = client
                .createPermission(getUserLink(), getDocPermissionWithPartitionKeyWithName(), null).toBlocking().single()
                .getResource();
        // CREATE permission for document with partition key 2
        createdDocPermissionWithPartitionKey2 = client
                .createPermission(getUserLink(), getDocPermissionWithPartitionKey2(), null).toBlocking().single()
                .getResource();
        createdDocPermissionWithPartitionKey2WithName = client
                .createPermission(getUserLink(), getDocPermissionWithPartitionKey2WithName(), null).toBlocking().single()
                .getResource();
        // CREATE permission for collection with partition key
        createdColPermissionWithPartitionKey = client
                .createPermission(getUserLink(), getColPermissionWithPartitionKey(), null).toBlocking().single()
                .getResource();
        createdColPermissionWithPartitionKeyWithName = client
                .createPermission(getUserLink(), getColPermissionWithPartitionKeyWithName(), null).toBlocking().single()
                .getResource();
        // CREATE permission for collection with partition key
        createdColPermissionWithPartitionKey2 = client
                .createPermission(getUserLink(), getColPermissionWithPartitionKey2(), null).toBlocking().single()
                .getResource();
        createdColPermissionWithPartitionKey2WithName = client
                .createPermission(getUserLink(), getColPermissionWithPartitionKey2WithName(), null).toBlocking().single()
                .getResource();
    }

    @DataProvider(name = "collectionAndPermissionData")
    public Object[][] collectionAndPermissionData() {
        return new Object[][]{
                //This test will try to read collection from its own permission and validate it, both with request Id and name.
                {createdCollection.selfLink(), createdCollPermission},
                {Utils.getCollectionNameLink(createdDatabase.id(), createdCollection.id()), createdDocPermissionWithName},
        };
    }

    @DataProvider(name = "documentAndPermissionData")
    public Object[][] documentAndPermissionData() {
        return new Object[][]{
                 //These tests will try to read document from its own permission and validate it, both with request Id and name.
                {createdDocument.selfLink(), createdDocPermission, createdDocument.id(), null},
                {Utils.getDocumentNameLink(createdDatabase.id(), createdCollection.id(), createdDocument.id()), createdDocPermissionWithName, createdDocument.id(), null},

                //These tests will try to read document from its permission having partition key 1 and validate it, both with request Id and name.
                {createdDocumentWithPartitionKey.selfLink(), createdDocPermissionWithPartitionKey, createdDocumentWithPartitionKey.id(), PARTITION_KEY_VALUE},
                {Utils.getDocumentNameLink(createdDatabase.id(), createdCollectionWithPartitionKey.id(), createdDocumentWithPartitionKey.id()), createdDocPermissionWithPartitionKeyWithName
                        , createdDocumentWithPartitionKey.id(), PARTITION_KEY_VALUE},

                //These tests will try to read document from its permission having partition key 2 and validate it, both with request Id and name.
                {createdDocumentWithPartitionKey2.selfLink(), createdDocPermissionWithPartitionKey2, createdDocumentWithPartitionKey2.id(), PARTITION_KEY_VALUE_2},
                {Utils.getDocumentNameLink(createdDatabase.id(), createdCollectionWithPartitionKey.id(), createdDocumentWithPartitionKey2.id()),
                        createdDocPermissionWithPartitionKey2WithName, createdDocumentWithPartitionKey2.id(), PARTITION_KEY_VALUE_2},

                // These tests will try to read document from its parent collection permission and validate it, both with request Id and name.
                {createdDocument.selfLink(), createdCollPermission, createdDocument.id(), null},
                {Utils.getDocumentNameLink(createdDatabase.id(), createdCollection.id(), createdDocument.id()), createdCollPermissionWithName, createdDocument.id(), null},

                //This test will try to read document from collection permission having partition key 1 and validate it, both with request Id and name.
                {createdDocumentWithPartitionKey.selfLink(), createdColPermissionWithPartitionKey, createdDocumentWithPartitionKey.id(), PARTITION_KEY_VALUE},
                {Utils.getDocumentNameLink(createdDatabase.id(), createdCollectionWithPartitionKey.id(), createdDocumentWithPartitionKey.id()), createdColPermissionWithPartitionKeyWithName, createdDocumentWithPartitionKey.id(), PARTITION_KEY_VALUE},

                //This test will try to read document from collection permission having partition key 2 and validate it, both with request Id and name.
                {createdDocumentWithPartitionKey2.selfLink(), createdColPermissionWithPartitionKey2, createdDocumentWithPartitionKey2.id(), PARTITION_KEY_VALUE_2},
                {Utils.getDocumentNameLink(createdDatabase.id(), createdCollectionWithPartitionKey.id(), createdDocumentWithPartitionKey2.id()), createdColPermissionWithPartitionKey2WithName, createdDocumentWithPartitionKey2.id(), PARTITION_KEY_VALUE_2}

        };
    }

    @DataProvider(name = "documentAndPermissionDataForResourceNotFound")
    public Object[][] documentAndPermissionDataForResourceNotFound() {
        return new Object[][]{
                //This test will try to read document from its resource token directly and validate it.
                {createdDocumentWithPartitionKey2.selfLink(), createdColPermissionWithPartitionKey, PARTITION_KEY_VALUE},
                //This test will try to read document from its parent collection resource token directly and validate it.
                {Utils.getDocumentNameLink(createdDatabase.id(), createdCollectionWithPartitionKey.id(), createdDocumentWithPartitionKey2.id()),
                        createdColPermissionWithPartitionKeyWithName, PARTITION_KEY_VALUE}
        };
    }

    @DataProvider(name = "documentAndMultipleCollPermissionData")
    public Object[][] documentAndMultipleCollPermissionData() {
        return new Object[][]{
                //These tests will try to read document from partition 1 with two collection permissions having different partition keys and validate it, both with request Id and name.
                {createdDocumentWithPartitionKey.selfLink(), createdColPermissionWithPartitionKey, createdColPermissionWithPartitionKey2, createdDocumentWithPartitionKey.id(),
                PARTITION_KEY_VALUE},
                {Utils.getDocumentNameLink(createdDatabase.id(), createdCollectionWithPartitionKey.id(), createdDocumentWithPartitionKey.id()), createdColPermissionWithPartitionKeyWithName
                        , createdColPermissionWithPartitionKey2WithName, createdDocumentWithPartitionKey.id(), PARTITION_KEY_VALUE},

                //These tests will try to read document from partition 1 with two collection permissions having different partition keys and validate it, both with request Id and name.
                {createdDocumentWithPartitionKey2.selfLink(), createdColPermissionWithPartitionKey, createdColPermissionWithPartitionKey2, createdDocumentWithPartitionKey2.id(),
                        PARTITION_KEY_VALUE_2},
                {Utils.getDocumentNameLink(createdDatabase.id(), createdCollectionWithPartitionKey.id(), createdDocumentWithPartitionKey2.id()), createdColPermissionWithPartitionKeyWithName
                        , createdColPermissionWithPartitionKey2WithName, createdDocumentWithPartitionKey2.id(), PARTITION_KEY_VALUE_2}
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
                    .withPermissionFeed(permissionFeed).withConnectionPolicy(ConnectionPolicy.defaultPolicy())
                    .withConsistencyLevel(ConsistencyLevel.SESSION).build();
            Observable<ResourceResponse<DocumentCollection>> readObservable = asyncClientResourceToken
                    .readCollection(collectionUrl, null);

            ResourceResponseValidator<DocumentCollection> validator = new ResourceResponseValidator.Builder<DocumentCollection>()
                   .withId(createdCollection.id()).build();
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
            asyncClientResourceToken = new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                    .withPermissionFeed(permissionFeed).withConnectionPolicy(ConnectionPolicy.defaultPolicy())
                    .withConsistencyLevel(ConsistencyLevel.SESSION).build();
            RequestOptions options = new RequestOptions();
            if (StringUtils.isNotEmpty(partitionKey)) {
                options.setPartitionKey(new PartitionKey((String)partitionKey));
            } else {
                options.setPartitionKey(PartitionKey.None);
            }
            Observable<ResourceResponse<Document>> readObservable = asyncClientResourceToken
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
                    .withConnectionPolicy(ConnectionPolicy.defaultPolicy()).withConsistencyLevel(ConsistencyLevel.SESSION)
                    .build();
            RequestOptions options = new RequestOptions();
            options.setPartitionKey(PartitionKey.None);
            Observable<ResourceResponse<Document>> readObservable = asyncClientResourceToken
                    .readDocument(createdDocument.selfLink(), options);
            ResourceResponseValidator<Document> validator = new ResourceResponseValidator.Builder<Document>()
                    .withId(createdDocument.id()).build();
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
            asyncClientResourceToken = new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                    .withPermissionFeed(permissionFeed).withConnectionPolicy(ConnectionPolicy.defaultPolicy())
                    .withConsistencyLevel(ConsistencyLevel.SESSION).build();
            RequestOptions options = new RequestOptions();
            options.setPartitionKey(new PartitionKey(partitionKey));
            Observable<ResourceResponse<Document>> readObservable = asyncClientResourceToken
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
            asyncClientResourceToken = new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                    .withPermissionFeed(permissionFeed).withConnectionPolicy(ConnectionPolicy.defaultPolicy())
                    .withConsistencyLevel(ConsistencyLevel.SESSION).build();
            RequestOptions options = new RequestOptions();
            options.setPartitionKey(new PartitionKey(partitionKey));
            Observable<ResourceResponse<Document>> readObservable = asyncClientResourceToken
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
                .withConnectionPolicy(ConnectionPolicy.defaultPolicy())
                .withConsistencyLevel(ConsistencyLevel.SESSION)
                .withPermissionFeed(permissionFeed)
                .build();
            RequestOptions options = new RequestOptions();
            options.setPartitionKey(new PartitionKey(PARTITION_KEY_VALUE_2));
            Observable<ResourceResponse<Document>> readObservable = asyncClientResourceToken
                    .readDocument(createdDocumentWithPartitionKey.selfLink(), options);
            FailureValidator validator = new FailureValidator.Builder().resourceTokenNotFound().build();
            validateFailure(readObservable, validator);
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
        user.id(USER_NAME);
        return user;
    }

    private static Document getDocument() {
        Document doc = new Document(String.format(DOCUMENT_DEFINITION, 1, 1));
        return doc;
    }

    private Permission getCollPermission() {
        Permission permission = new Permission();
        permission.id(PERMISSION_FOR_COLL);
        permission.setPermissionMode(PermissionMode.READ);
        permission.setResourceLink(createdCollection.selfLink());
        return permission;
    }

    private Permission getCollPermissionWithName() {
        Permission permission = new Permission();
        permission.id(PERMISSION_FOR_COLL_WITH_NAME);
        permission.setPermissionMode(PermissionMode.READ);
        permission.setResourceLink(Utils.getCollectionNameLink(createdDatabase.id(), createdCollection.id()));
        return permission;
    }

    private Permission getDocPermission() {
        Permission permission = new Permission();
        permission.id(PERMISSION_FOR_DOC);
        permission.setPermissionMode(PermissionMode.READ);
        permission.setResourceLink(createdDocument.selfLink());
        return permission;
    }
    private Permission getDocPermissionWithName() {
        Permission permission = new Permission();
        permission.id(PERMISSION_FOR_DOC_WITH_NAME);
        permission.setPermissionMode(PermissionMode.READ);
        permission.setResourceLink(Utils.getDocumentNameLink(createdDatabase.id(),createdCollection.id(),createdDocument.id()));
        return permission;
    }

    private Permission getDocPermissionWithPartitionKey() {
        String permissionStr = String.format(PERMISSION_DEFINITION, createdDocumentWithPartitionKey.selfLink(),
                PARTITION_KEY_VALUE);
        Permission permission = new Permission(permissionStr);
        return permission;
    }

    private Permission getDocPermissionWithPartitionKeyWithName() {
        String permissionStr = String.format(PERMISSION_DEFINITION, Utils.getDocumentNameLink(createdDatabase.id(), createdCollectionWithPartitionKey.id(), createdDocumentWithPartitionKey.id()),
                PARTITION_KEY_VALUE);
        Permission permission = new Permission(permissionStr);
        permission.id("PermissionForDocWithPartitionKeyWithName");
        return permission;
    }

    private Permission getDocPermissionWithPartitionKey2() {
        String permissionStr = String.format(PERMISSION_DEFINITION, createdDocumentWithPartitionKey2.selfLink(),
                PARTITION_KEY_VALUE_2);
        Permission permission = new Permission(permissionStr);
        permission.id("PermissionForDocWithPartitionKey2");
        return permission;
    }

    private Permission getDocPermissionWithPartitionKey2WithName() {
        String permissionStr = String.format(PERMISSION_DEFINITION, Utils.getDocumentNameLink(createdDatabase.id(),createdCollectionWithPartitionKey.id(),createdDocumentWithPartitionKey2.id()),
                PARTITION_KEY_VALUE_2);
        Permission permission = new Permission(permissionStr);
        permission.id("PermissionForDocWithPartitionKey2WithName");
        return permission;
    }

    private Permission getColPermissionWithPartitionKey() {
        String permissionStr = String.format(COLLECTION_PERMISSION_DEFINITION, createdCollectionWithPartitionKey.selfLink(),
                PARTITION_KEY_VALUE);
        Permission permission = new Permission(permissionStr);
        return permission;
    }

    private Permission getColPermissionWithPartitionKeyWithName() {
        String permissionStr = String.format(COLLECTION_PERMISSION_DEFINITION, Utils.getCollectionNameLink(createdDatabase.id(), createdCollectionWithPartitionKey.id()),
                PARTITION_KEY_VALUE);
        Permission permission = new Permission(permissionStr);
        permission.id("PermissionForColWithPartitionKeyWithName");
        return permission;
    }

    private Permission getColPermissionWithPartitionKey2() {
        String permissionStr = String.format(COLLECTION_PERMISSION_DEFINITION, createdCollectionWithPartitionKey.selfLink(),
                PARTITION_KEY_VALUE_2);
        Permission permission = new Permission(permissionStr);
        permission.id("PermissionForColWithPartitionKey2");
        return permission;
    }

    private Permission getColPermissionWithPartitionKey2WithName() {
        String permissionStr = String.format(COLLECTION_PERMISSION_DEFINITION, Utils.getCollectionNameLink(createdDatabase.id(), createdCollectionWithPartitionKey.id()),
                PARTITION_KEY_VALUE_2);
        Permission permission = new Permission(permissionStr);
        permission.id("PermissionForColWithPartitionKey2WithName");
        return permission;
    }

    private String getUserLink() {
        return createdUser.selfLink();
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
        partitionKeyDef.paths(paths);

        DocumentCollection collectionDefinition = new DocumentCollection();
        collectionDefinition.id(UUID.randomUUID().toString());
        collectionDefinition.setPartitionKey(partitionKeyDef);

        return collectionDefinition;
    }
}