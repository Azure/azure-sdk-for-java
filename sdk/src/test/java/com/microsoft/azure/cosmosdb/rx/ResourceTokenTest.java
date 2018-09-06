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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.microsoft.azure.cosmosdb.ConnectionPolicy;
import com.microsoft.azure.cosmosdb.ConsistencyLevel;
import com.microsoft.azure.cosmosdb.Database;
import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.PartitionKey;
import com.microsoft.azure.cosmosdb.PartitionKeyDefinition;
import com.microsoft.azure.cosmosdb.Permission;
import com.microsoft.azure.cosmosdb.PermissionMode;
import com.microsoft.azure.cosmosdb.RequestOptions;
import com.microsoft.azure.cosmosdb.ResourceResponse;
import com.microsoft.azure.cosmosdb.User;

import rx.Observable;

/**
 * This class try to test different scenario related to fetching various
 * resources from resource token directly or via permission feed .
 *
 */
public class ResourceTokenTest extends TestSuiteBase {
    public final static String DATABASE_ID = getDatabaseId(ResourceTokenTest.class);

    private Database createdDatabase;
    private DocumentCollection createdCollection;
    private DocumentCollection createdCollectionWithPartitionKey;
    private Document createdDocument;
    private Document createdDocumentWithPartitionKey;
    private Document createdDocumentWithPartitionKey2;
    private User createdUser;
    private Permission createdCollPermission;
    private Permission createdDocPermission;
    private Permission createdDocPermissionWithPartitionKey;
    private Permission createdDocPermissionWithPartitionKey2;
    private Permission createdColPermissionWithPartitionKey;
    private Permission createdColPermissionWithPartitionKey2;
    private AsyncDocumentClient.Builder clientBuilder;
    private AsyncDocumentClient client;

    // All static string used in below test cases
    private final static String DOCUMENT_DEFINITION = "{ 'id': 'doc%d', 'counter': '%d'}";
    private final static String DOCUMENT_DEFINITION_WITH_PERMISSION_KEY = "{ " + "\"id\": \"%s\", "
            + "\"mypk\": \"%s\", " + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]" + "}";
    private final static String PARTITION_KEY_PATH = "/mypk";

    private static final String PARTITION_KEY_VALUE = "1";
    private static final String PARTITION_KEY_VALUE_2 = "2";
    private static final String PERMISSION_DEFINITION = "{" + "    'id': 'PermissionForDocWithPartitionKey',"
            + "    'permissionMode': 'read'," + "    'resource': '%s'," + "    'resourcePartitionKey': ['%s']" + "}";
    private static final String COLLECTION_PERMISSION_DEFINITION = "{" + "    'id': 'PermissionForColWithPartitionKey',"
            + "    'permissionMode': 'read'," + "    'resource': '%s'," + "    'resourcePartitionKey': ['%s']" + "}";
    private static final String USER_NAME = "TestUser";
    private static final String PERMISSION_FOR_COLL = "PermissionForColl";
    private static final String PERMISSION_FOR_DOC = "PermissionForDoc";

    @Factory(dataProvider = "clientBuilders")
    public ResourceTokenTest(AsyncDocumentClient.Builder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() throws Exception {
        client = clientBuilder.build();
        Database d = new Database();
        d.setId(DATABASE_ID);
        createdDatabase = safeCreateDatabase(client, d);
        // Create collection
        createdCollection = createCollection(client, createdDatabase.getId(), getCollectionDefinition());
        // Create document
        createdDocument = createDocument(client, createdDatabase.getId(),createdCollection.getId(), getDocument());
        // Create collection with partition key
        createdCollectionWithPartitionKey = createCollection(client, createdDatabase.getId(), getCollectionDefinitionWithPartitionKey());
        // Create document with partition key
        createdDocumentWithPartitionKey = createDocument(client, createdDatabase.getId(), createdCollectionWithPartitionKey.getId(),
                getDocumentDefinitionWithPartitionKey());
        // Create second document with partition key
        createdDocumentWithPartitionKey2 = createDocument(client, createdDatabase.getId(),createdCollectionWithPartitionKey.getId(),
                getDocumentDefinitionWithPartitionKey2());
        // Create user
        createdUser = createUser(client, createdDatabase.getId(), getUserDefinition());
        // Create permission for collection
        createdCollPermission = client.createPermission(getUserLink(), getCollPermission(), null).toBlocking().single()
                .getResource();
        // Create permission for document
        createdDocPermission = client.createPermission(getUserLink(), getDocPermission(), null).toBlocking().single()
                .getResource();
        // Create permission for document with partition key
        createdDocPermissionWithPartitionKey = client
                .createPermission(getUserLink(), getDocPermissionWithPartitionKey(), null).toBlocking().single()
                .getResource();
        // Create permission for document with partition key 2
        createdDocPermissionWithPartitionKey2 = client
                .createPermission(getUserLink(), getDocPermissionWithPartitionKey2(), null).toBlocking().single()
                .getResource();
        // Create permission for collection with partition key
        createdColPermissionWithPartitionKey = client
                .createPermission(getUserLink(), getColPermissionWithPartitionKey(), null).toBlocking().single()
                .getResource();
        // Create permission for collection with partition key
        createdColPermissionWithPartitionKey2 = client
                .createPermission(getUserLink(), getColPermissionWithPartitionKey2(), null).toBlocking().single()
                .getResource();
    }

    /**
     * This test will try to read collection from its own permission and validate it
     * .
     * 
     * @throws Exception
     */
    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readCollectionFromPermissionFeed() throws Exception {
        AsyncDocumentClient asyncClientResourceToken = null ;
        try {
            List<Permission> permissionFeed = new ArrayList<>();
            permissionFeed.add(createdCollPermission);
            asyncClientResourceToken = new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                    .withPermissionFeed(permissionFeed).withConnectionPolicy(ConnectionPolicy.GetDefault())
                    .withConsistencyLevel(ConsistencyLevel.Session).build();
            Observable<ResourceResponse<DocumentCollection>> readObservable = asyncClientResourceToken
                    .readCollection(createdCollection.getSelfLink(), null);

            ResourceResponseValidator<DocumentCollection> validator = new ResourceResponseValidator.Builder<DocumentCollection>()
                   .withId(createdCollection.getId()).build();
            validateSuccess(readObservable, validator);
        } finally {
            safeClose(asyncClientResourceToken);
        }
    }

    /**
     * This test will try to read document from its own permission and validate it.
     * 
     * @throws Exception
     */
    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readDocumentFromPermissionFeed() throws Exception {
        AsyncDocumentClient asyncClientResourceToken = null;
        try {
            List<Permission> permissionFeed = new ArrayList<>();
            permissionFeed.add(createdDocPermission);
            asyncClientResourceToken = new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                    .withPermissionFeed(permissionFeed).withConnectionPolicy(ConnectionPolicy.GetDefault())
                    .withConsistencyLevel(ConsistencyLevel.Session).build();
            Observable<ResourceResponse<Document>> readObservable = asyncClientResourceToken
                    .readDocument(createdDocument.getSelfLink(), null);
            asyncClientResourceToken = new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                    .withPermissionFeed(permissionFeed)
                    .withConnectionPolicy(ConnectionPolicy.GetDefault())
                    .withConsistencyLevel(ConsistencyLevel.Session).build();
            ResourceResponseValidator<Document> validator = new ResourceResponseValidator.Builder<Document>()
                    .withId(createdDocument.getId()).build();
            validateSuccess(readObservable, validator);
        } finally {
            safeClose(asyncClientResourceToken);
        }
    }

    /**
     * This test will try to read document from its parent collection permission and
     * validate it.
     * 
     * @throws Exception
     */
    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readDocumentFromCollPermissionFeed() throws Exception {
        AsyncDocumentClient asyncClientResourceToken = null;
        try {
            List<Permission> permissionFeed = new ArrayList<>();
            permissionFeed.add(createdCollPermission);
            asyncClientResourceToken = new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                    .withPermissionFeed(permissionFeed).withConnectionPolicy(ConnectionPolicy.GetDefault())
                    .withConsistencyLevel(ConsistencyLevel.Session).build();
            RequestOptions options = new RequestOptions();
            options.setPartitionKey(new PartitionKey(createdDocument.get("mypk")));
            Observable<ResourceResponse<Document>> readObservable = asyncClientResourceToken
                    .readDocument(createdDocument.getSelfLink(), null);
            ResourceResponseValidator<Document> validator = new ResourceResponseValidator.Builder<Document>()
                    .withId(createdDocument.getId()).build();
            validateSuccess(readObservable, validator);
        } finally {
            safeClose(asyncClientResourceToken);
        }
    }

    /**
     * This test will try to read document from its resource token directly and validate
     * it.
     * 
     * @throws Exception
     */
    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readDocumentFromDocResouceToken() throws Exception {
        AsyncDocumentClient asyncClientResourceToken = null;
        try {
            asyncClientResourceToken = new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                    .withMasterKeyOrResourceToken(createdDocPermission.getToken())
                    .withConnectionPolicy(ConnectionPolicy.GetDefault()).withConsistencyLevel(ConsistencyLevel.Session)
                    .build();
            Observable<ResourceResponse<Document>> readObservable = asyncClientResourceToken
                    .readDocument(createdDocument.getSelfLink(), null);
            ResourceResponseValidator<Document> validator = new ResourceResponseValidator.Builder<Document>()
                    .withId(createdDocument.getId()).build();
            validateSuccess(readObservable, validator);
        } finally {
            safeClose(asyncClientResourceToken);
        }
    }

    /**
     * This test will try to read document from its parent collection resource token and
     * validate it.
     * 
     * @throws Exception
     */
    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readDocumentFromCollResouceToken() throws Exception {
        AsyncDocumentClient asyncClientResourceToken = null;
        try {
            asyncClientResourceToken = new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                    .withMasterKeyOrResourceToken(createdDocPermission.getToken())
                    .withConnectionPolicy(ConnectionPolicy.GetDefault()).withConsistencyLevel(ConsistencyLevel.Session)
                    .build();
            Observable<ResourceResponse<Document>> readObservable = asyncClientResourceToken
                    .readDocument(createdDocument.getSelfLink(), null);
            ResourceResponseValidator<Document> validator = new ResourceResponseValidator.Builder<Document>()
                    .withId(createdDocument.getId()).build();
            validateSuccess(readObservable, validator);
        } finally {
            safeClose(asyncClientResourceToken);
        }
    }

    /**
     * This test will try to read document from its permission having partition key 1 and
     * validate it.
     * 
     * @throws Exception
     */
    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readDocumentFromPermissionWithPartitionKey() throws Exception {
        AsyncDocumentClient asyncClientResourceToken = null;
        try {
            List<Permission> permissionFeed = new ArrayList<>();
            permissionFeed.add(createdDocPermissionWithPartitionKey);
            asyncClientResourceToken = new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                    .withPermissionFeed(permissionFeed).withConnectionPolicy(ConnectionPolicy.GetDefault())
                    .withConsistencyLevel(ConsistencyLevel.Session).build();
            RequestOptions options = new RequestOptions();
            options.setPartitionKey(new PartitionKey(PARTITION_KEY_VALUE));
            Observable<ResourceResponse<Document>> readObservable = asyncClientResourceToken
                    .readDocument(createdDocumentWithPartitionKey.getSelfLink(), options);
            ResourceResponseValidator<Document> validator = new ResourceResponseValidator.Builder<Document>()
                    .withId(createdDocumentWithPartitionKey.getId()).build();
            validateSuccess(readObservable, validator);
        } finally {
            safeClose(asyncClientResourceToken);
        }
    }

    /**
     * This test will try to read document from its permission having partition key 2 and
     * validate it.
     * 
     * @throws Exception
     */
    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readDocumentFromPermissionWithPartitionKey2() throws Exception {
        AsyncDocumentClient asyncClientResourceToken = null;
        try {
            List<Permission> permissionFeed = new ArrayList<>();
            permissionFeed.add(createdDocPermissionWithPartitionKey2);
            asyncClientResourceToken = new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                    .withPermissionFeed(permissionFeed).withConnectionPolicy(ConnectionPolicy.GetDefault())
                    .withConsistencyLevel(ConsistencyLevel.Session).build();
            RequestOptions options = new RequestOptions();
            options.setPartitionKey(new PartitionKey(PARTITION_KEY_VALUE_2));
            Observable<ResourceResponse<Document>> readObservable = asyncClientResourceToken
                    .readDocument(createdDocumentWithPartitionKey2.getSelfLink(), options);
            ResourceResponseValidator<Document> validator = new ResourceResponseValidator.Builder<Document>()
                    .withId(createdDocumentWithPartitionKey2.getId()).build();
            validateSuccess(readObservable, validator);
        } finally {
            safeClose(asyncClientResourceToken);
        }
    }

    /**
     * This test will try to read document from collection permission having partition
     * key 1 and validate it.
     * 
     * @throws Exception
     */
    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readDocumentFromCollPermissionWithPartitionKey() throws Exception {
        AsyncDocumentClient asyncClientResourceToken = null;
        try {
            List<Permission> permissionFeed = new ArrayList<>();
            permissionFeed.add(createdColPermissionWithPartitionKey);
            asyncClientResourceToken = new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                    .withPermissionFeed(permissionFeed).withConnectionPolicy(ConnectionPolicy.GetDefault())
                    .withConsistencyLevel(ConsistencyLevel.Session).build();
            RequestOptions options = new RequestOptions();
            options.setPartitionKey(new PartitionKey(PARTITION_KEY_VALUE));
            Observable<ResourceResponse<Document>> readObservable = asyncClientResourceToken
                    .readDocument(createdDocumentWithPartitionKey.getSelfLink(), options);
            ResourceResponseValidator<Document> validator = new ResourceResponseValidator.Builder<Document>()
                    .withId(createdDocumentWithPartitionKey.getId()).build();
            validateSuccess(readObservable, validator);
        } finally {
            safeClose(asyncClientResourceToken);
        }
    }

    /**
     * This test will try to read document from collection permission having partition
     * key 2 and validate it.
     * 
     * @throws Exception
     */
    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readDocumentFromCollPermissionWithPartitionKey2() throws Exception {
        AsyncDocumentClient asyncClientResourceToken = null;
        try {
            List<Permission> permissionFeed = new ArrayList<>();
            permissionFeed.add(createdColPermissionWithPartitionKey2);
            asyncClientResourceToken = new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                    .withPermissionFeed(permissionFeed).withConnectionPolicy(ConnectionPolicy.GetDefault())
                    .withConsistencyLevel(ConsistencyLevel.Session).build();
            RequestOptions options = new RequestOptions();
            options.setPartitionKey(new PartitionKey(PARTITION_KEY_VALUE_2));
            Observable<ResourceResponse<Document>> readObservable = asyncClientResourceToken
                    .readDocument(createdDocumentWithPartitionKey2.getSelfLink(), options);
            ResourceResponseValidator<Document> validator = new ResourceResponseValidator.Builder<Document>()
                    .withId(createdDocumentWithPartitionKey2.getId()).build();
            validateSuccess(readObservable, validator);
        } finally {
            safeClose(asyncClientResourceToken);
        }
    }

    /**
     * This test will try to read document from partition 1 with two collection permissions having different partition
     * keys and validate it.
     *
     * @throws Exception
     */
    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readDocumentOfParKey1FromTwoCollPermissionWithDiffPartitionKeys() throws Exception {
        AsyncDocumentClient asyncClientResourceToken = null;
        try {
            List<Permission> permissionFeed = new ArrayList<>();
            permissionFeed.add(createdColPermissionWithPartitionKey);
            permissionFeed.add(createdColPermissionWithPartitionKey2);
            asyncClientResourceToken = new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                    .withPermissionFeed(permissionFeed).withConnectionPolicy(ConnectionPolicy.GetDefault())
                    .withConsistencyLevel(ConsistencyLevel.Session).build();
            RequestOptions options = new RequestOptions();
            options.setPartitionKey(new PartitionKey(PARTITION_KEY_VALUE));
            Observable<ResourceResponse<Document>> readObservable = asyncClientResourceToken
                    .readDocument(createdDocumentWithPartitionKey.getSelfLink(), options);
            ResourceResponseValidator<Document> validator = new ResourceResponseValidator.Builder<Document>()
                    .withId(createdDocumentWithPartitionKey.getId()).build();
            validateSuccess(readObservable, validator);
        } finally {
            safeClose(asyncClientResourceToken);
        }
    }

    /**
     * This test will try to read document from partition 2 with two collection permissions having different partition
     * keys and validate it.
     *
     * @throws Exception
     */
    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readDocumentOfParKey2FromTwoCollPermissionWithDiffPartitionKeys() throws Exception {
        AsyncDocumentClient asyncClientResourceToken = null;
        try {
            List<Permission> permissionFeed = new ArrayList<>();
            permissionFeed.add(createdColPermissionWithPartitionKey);
            permissionFeed.add(createdColPermissionWithPartitionKey2);
            asyncClientResourceToken = new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                    .withPermissionFeed(permissionFeed).withConnectionPolicy(ConnectionPolicy.GetDefault())
                    .withConsistencyLevel(ConsistencyLevel.Session).build();
            RequestOptions options = new RequestOptions();
            options.setPartitionKey(new PartitionKey(PARTITION_KEY_VALUE_2));
            Observable<ResourceResponse<Document>> readObservable = asyncClientResourceToken
                    .readDocument(createdDocumentWithPartitionKey2.getSelfLink(), options);
            ResourceResponseValidator<Document> validator = new ResourceResponseValidator.Builder<Document>()
                    .withId(createdDocumentWithPartitionKey2.getId()).build();
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
    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readDocumentFromCollPermissionWithDiffPartitionKey_ResourceNotFound() throws Exception {
        AsyncDocumentClient asyncClientResourceToken = null;
        try {
            List<Permission> permissionFeed = new ArrayList<>();
            permissionFeed.add(createdColPermissionWithPartitionKey);
            asyncClientResourceToken = new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                    .withPermissionFeed(permissionFeed).withConnectionPolicy(ConnectionPolicy.GetDefault())
                    .withConsistencyLevel(ConsistencyLevel.Session).build();
            RequestOptions options = new RequestOptions();
            options.setPartitionKey(new PartitionKey(PARTITION_KEY_VALUE));
            Observable<ResourceResponse<Document>> readObservable = asyncClientResourceToken
                    .readDocument(createdDocumentWithPartitionKey2.getSelfLink(), options);
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
            asyncClientResourceToken = new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                    .withPermissionFeed(permissionFeed).withConnectionPolicy(ConnectionPolicy.GetDefault())
                    .withConsistencyLevel(ConsistencyLevel.Session).build();
            RequestOptions options = new RequestOptions();
            options.setPartitionKey(new PartitionKey(PARTITION_KEY_VALUE_2));
            Observable<ResourceResponse<Document>> readObservable = asyncClientResourceToken
                    .readDocument(createdDocumentWithPartitionKey.getSelfLink(), options);
            FailureValidator validator = new FailureValidator.Builder().resourceTokenNotFound().build();
            validateFailure(readObservable, validator);
        } finally {
            safeClose(asyncClientResourceToken);
        }
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(client, createdDatabase.getId());
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
        permission.setPermissionMode(PermissionMode.Read);
        permission.setResourceLink(createdCollection.getSelfLink());
        return permission;
    }

    private Permission getDocPermission() {
        Permission permission = new Permission();
        permission.setId(PERMISSION_FOR_DOC);
        permission.setPermissionMode(PermissionMode.Read);
        permission.setResourceLink(createdDocument.getSelfLink());
        return permission;
    }

    private Permission getDocPermissionWithPartitionKey() {
        String permissionStr = String.format(PERMISSION_DEFINITION, createdDocumentWithPartitionKey.getSelfLink(),
                PARTITION_KEY_VALUE);
        Permission permission = new Permission(permissionStr);
        return permission;
    }
    private Permission getDocPermissionWithPartitionKey2() {
        String permissionStr = String.format(PERMISSION_DEFINITION, createdDocumentWithPartitionKey2.getSelfLink(),
                PARTITION_KEY_VALUE_2);
        Permission permission = new Permission(permissionStr);
        permission.setId("PermissionForDocWithPartitionKey2");
        return permission;
    }

    private Permission getColPermissionWithPartitionKey() {
        String permissionStr = String.format(COLLECTION_PERMISSION_DEFINITION, createdCollectionWithPartitionKey.getSelfLink(),
                PARTITION_KEY_VALUE);
        Permission permission = new Permission(permissionStr);
        return permission;
    }
    private Permission getColPermissionWithPartitionKey2() {
        String permissionStr = String.format(COLLECTION_PERMISSION_DEFINITION, createdCollectionWithPartitionKey.getSelfLink(),
                PARTITION_KEY_VALUE_2);
        Permission permission = new Permission(permissionStr);
        permission.setId("PermissionForColWithPartitionKey2");
        return permission;
    }
    private String getUserLink() {
        return createdUser.getSelfLink();
    }

    static protected DocumentCollection getCollectionDefinition() {
        DocumentCollection collectionDefinition = new DocumentCollection();
        collectionDefinition.setId(UUID.randomUUID().toString());
        return collectionDefinition;
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

    private DocumentCollection getCollectionDefinitionWithPartitionKey() {
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<String>();
        paths.add(PARTITION_KEY_PATH);
        partitionKeyDef.setPaths(paths);

        DocumentCollection collectionDefinition = new DocumentCollection();
        collectionDefinition.setId(UUID.randomUUID().toString());
        collectionDefinition.setPartitionKey(partitionKeyDef);

        return collectionDefinition;
    }
}