// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CompositePath;
import com.azure.cosmos.CompositePathSortOrder;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncContainerResponse;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosAsyncItem;
import com.azure.cosmos.CosmosAsyncItemResponse;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainerProperties;
import com.azure.cosmos.CosmosContainerRequestOptions;
import com.azure.cosmos.CosmosDatabaseForTest;
import com.azure.cosmos.CosmosItemProperties;
import com.azure.cosmos.CosmosItemRequestOptions;
import com.azure.cosmos.CosmosResponseValidator;
import com.azure.cosmos.IndexingMode;
import com.azure.cosmos.IndexingPolicy;
import com.azure.cosmos.PartitionKey;
import com.azure.cosmos.PartitionKeyDefinition;
import com.azure.cosmos.SpatialSpec;
import com.azure.cosmos.SpatialType;
import com.azure.cosmos.internal.Database;
import com.azure.cosmos.internal.FailureValidator;
import com.azure.cosmos.internal.RetryAnalyzer;
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

public class CollectionCrudTest extends TestSuiteBase {

    private static final int TIMEOUT = 50000;
    private static final int SETUP_TIMEOUT = 20000;
    private static final int SHUTDOWN_TIMEOUT = 20000;
    private final String databaseId = CosmosDatabaseForTest.generateId();

    private CosmosAsyncClient client;
    private CosmosAsyncDatabase database;

    @Factory(dataProvider = "clientBuildersWithDirect")
    public CollectionCrudTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
        this.subscriberValidationTimeout = TIMEOUT;
    }

    @DataProvider(name = "collectionCrudArgProvider")
    public Object[][] collectionCrudArgProvider() {
        return new Object[][] {
                // collection name, is name base
                {UUID.randomUUID().toString()} ,

                // with special characters in the name.
                {"+ -_,:.|~" + UUID.randomUUID().toString() + " +-_,:.|~"} ,
        };
    }

    private CosmosContainerProperties getCollectionDefinition(String collectionName) {
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<String>();
        paths.add("/mypk");
        partitionKeyDef.setPaths(paths);

        CosmosContainerProperties collectionDefinition = new CosmosContainerProperties(
                collectionName,
                partitionKeyDef);

        return collectionDefinition;
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT, dataProvider = "collectionCrudArgProvider")
    public void createCollection(String collectionName) throws InterruptedException {
        CosmosContainerProperties collectionDefinition = getCollectionDefinition(collectionName);
        
        Mono<CosmosAsyncContainerResponse> createObservable = database
                .createContainer(collectionDefinition);

        CosmosResponseValidator<CosmosAsyncContainerResponse> validator = new CosmosResponseValidator.Builder<CosmosAsyncContainerResponse>()
                .withId(collectionDefinition.getId()).build();

        validateSuccess(createObservable, validator);
        safeDeleteAllCollections(database);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void createCollectionWithCompositeIndexAndSpatialSpec() throws InterruptedException {
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<String>();
        paths.add("/mypk");
        partitionKeyDef.setPaths(paths);

        CosmosContainerProperties collection = new CosmosContainerProperties(
                UUID.randomUUID().toString(),
                partitionKeyDef);

        IndexingPolicy indexingPolicy = new IndexingPolicy();
        CompositePath compositePath1 = new CompositePath();
        compositePath1.setPath("/path1");
        compositePath1.setOrder(CompositePathSortOrder.ASCENDING);
        CompositePath compositePath2 = new CompositePath();
        compositePath2.setPath("/path2");
        compositePath2.setOrder(CompositePathSortOrder.DESCENDING);
        CompositePath compositePath3 = new CompositePath();
        compositePath3.setPath("/path3");
        CompositePath compositePath4 = new CompositePath();
        compositePath4.setPath("/path4");
        compositePath4.setOrder(CompositePathSortOrder.ASCENDING);
        CompositePath compositePath5 = new CompositePath();
        compositePath5.setPath("/path5");
        compositePath5.setOrder(CompositePathSortOrder.DESCENDING);
        CompositePath compositePath6 = new CompositePath();
        compositePath6.setPath("/path6");

        ArrayList<CompositePath> compositeIndex1 = new ArrayList<CompositePath>();
        compositeIndex1.add(compositePath1);
        compositeIndex1.add(compositePath2);
        compositeIndex1.add(compositePath3);

        ArrayList<CompositePath> compositeIndex2 = new ArrayList<CompositePath>();
        compositeIndex2.add(compositePath4);
        compositeIndex2.add(compositePath5);
        compositeIndex2.add(compositePath6);

        List<List<CompositePath>> compositeIndexes = new ArrayList<>();
        compositeIndexes.add(compositeIndex1);
        compositeIndexes.add(compositeIndex2);
        indexingPolicy.setCompositeIndexes(compositeIndexes);

        SpatialType[] spatialTypes = new SpatialType[] {
                SpatialType.POINT,
                SpatialType.LINE_STRING,
                SpatialType.POLYGON,
                SpatialType.MULTI_POLYGON
        };
        List<SpatialSpec> spatialIndexes = new ArrayList<SpatialSpec>();
        for (int index = 0; index < 2; index++) {
            List<SpatialType> collectionOfSpatialTypes = new ArrayList<SpatialType>();

            SpatialSpec spec = new SpatialSpec();
            spec.setPath("/path" + index + "/*");

            for (int i = index; i < index + 3; i++) {
                collectionOfSpatialTypes.add(spatialTypes[i]);
            }
            spec.setSpatialTypes(collectionOfSpatialTypes);
            spatialIndexes.add(spec);
        }

        indexingPolicy.setSpatialIndexes(spatialIndexes);

        collection.setIndexingPolicy(indexingPolicy);
        
        Mono<CosmosAsyncContainerResponse> createObservable = database
                .createContainer(collection, new CosmosContainerRequestOptions());

        CosmosResponseValidator<CosmosAsyncContainerResponse> validator = new CosmosResponseValidator.Builder<CosmosAsyncContainerResponse>()
                .withId(collection.getId())
                .withCompositeIndexes(compositeIndexes)
                .withSpatialIndexes(spatialIndexes)
                .build();

        validateSuccess(createObservable, validator);
        safeDeleteAllCollections(database);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT, dataProvider = "collectionCrudArgProvider")
    public void readCollection(String collectionName) throws InterruptedException {
        CosmosContainerProperties collectionDefinition = getCollectionDefinition(collectionName);
        
        Mono<CosmosAsyncContainerResponse> createObservable = database.createContainer(collectionDefinition);
        CosmosAsyncContainer collection = createObservable.block().getContainer();

        Mono<CosmosAsyncContainerResponse> readObservable = collection.read();

        CosmosResponseValidator<CosmosAsyncContainerResponse> validator = new CosmosResponseValidator.Builder<CosmosAsyncContainerResponse>()
                .withId(collection.getId()).build();
        validateSuccess(readObservable, validator);
        safeDeleteAllCollections(database);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT, dataProvider = "collectionCrudArgProvider")
    public void readCollection_DoesntExist(String collectionName) throws Exception {

        Mono<CosmosAsyncContainerResponse> readObservable = database
                .getContainer("I don't exist").read();

        FailureValidator validator = new FailureValidator.Builder().resourceNotFound().build();
        validateFailure(readObservable, validator);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT, dataProvider = "collectionCrudArgProvider")
    public void deleteCollection(String collectionName) throws InterruptedException {
        CosmosContainerProperties collectionDefinition = getCollectionDefinition(collectionName);
        
        Mono<CosmosAsyncContainerResponse> createObservable = database.createContainer(collectionDefinition);
        CosmosAsyncContainer collection = createObservable.block().getContainer();

        Mono<CosmosAsyncContainerResponse> deleteObservable = collection.delete();

        CosmosResponseValidator<CosmosAsyncContainerResponse> validator = new CosmosResponseValidator.Builder<CosmosAsyncContainerResponse>()
                .nullResource().build();
        validateSuccess(deleteObservable, validator);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT, dataProvider = "collectionCrudArgProvider")
    public void replaceCollection(String collectionName) throws InterruptedException  {
        // create a collection
        CosmosContainerProperties collectionDefinition = getCollectionDefinition(collectionName);
        Mono<CosmosAsyncContainerResponse> createObservable = database.createContainer(collectionDefinition);
        CosmosAsyncContainer collection = createObservable.block().getContainer();
        CosmosContainerProperties collectionSettings = collection.read().block().getProperties();
        // sanity check
        assertThat(collectionSettings.getIndexingPolicy().getIndexingMode()).isEqualTo(IndexingMode.CONSISTENT);
        
        // replace indexing getMode
        IndexingPolicy indexingMode = new IndexingPolicy();
        indexingMode.setIndexingMode(IndexingMode.LAZY);
        collectionSettings.setIndexingPolicy(indexingMode);
        Mono<CosmosAsyncContainerResponse> readObservable = collection.replace(collectionSettings, new CosmosContainerRequestOptions());
        
        // validate
        CosmosResponseValidator<CosmosAsyncContainerResponse> validator = new CosmosResponseValidator.Builder<CosmosAsyncContainerResponse>()
                        .indexingMode(IndexingMode.LAZY).build();
        validateSuccess(readObservable, validator);
        safeDeleteAllCollections(database);
    }

    @Test(groups = { "emulator" }, timeOut = 10 * TIMEOUT, retryAnalyzer = RetryAnalyzer.class)
    public void sessionTokenConsistencyCollectionDeleteCreateSameName() {
        CosmosAsyncClient client1 = clientBuilder().buildAsyncClient();
        CosmosAsyncClient client2 = clientBuilder().buildAsyncClient();

        String dbId = CosmosDatabaseForTest.generateId();
        String collectionId = "coll";
        CosmosAsyncDatabase db = null;
        try {
            Database databaseDefinition = new Database();
            databaseDefinition.setId(dbId);
            db = createDatabase(client1, dbId);

            PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
            ArrayList<String> paths = new ArrayList<String>();
            paths.add("/mypk");
            partitionKeyDef.setPaths(paths);

            CosmosContainerProperties collectionDefinition = new CosmosContainerProperties(collectionId, partitionKeyDef);
            CosmosAsyncContainer collection = createCollection(db, collectionDefinition, new CosmosContainerRequestOptions());

            CosmosItemProperties document = new CosmosItemProperties();
            document.setId("doc");
            BridgeInternal.setProperty(document, "name", "New Document");
            BridgeInternal.setProperty(document, "mypk", "mypkValue");
            CosmosAsyncItem item = createDocument(collection, document);
            CosmosItemRequestOptions options = new CosmosItemRequestOptions();
            options.setPartitionKey(new PartitionKey("mypkValue"));
            CosmosAsyncItemResponse readDocumentResponse = item.read(options).block();
            logger.info("Client 1 READ Document Client Side Request Statistics {}", readDocumentResponse.getCosmosResponseDiagnosticsString());
            logger.info("Client 1 READ Document Latency {}", readDocumentResponse.getRequestLatency());

            BridgeInternal.setProperty(document, "name", "New Updated Document");
            CosmosAsyncItemResponse upsertDocumentResponse = collection.upsertItem(document).block();
            logger.info("Client 1 Upsert Document Client Side Request Statistics {}", upsertDocumentResponse.getCosmosResponseDiagnosticsString());
            logger.info("Client 1 Upsert Document Latency {}", upsertDocumentResponse.getRequestLatency());

            //  DELETE the existing collection
            deleteCollection(client2, dbId, collectionId);
            //  Recreate the collection with the same name but with different client
            CosmosAsyncContainer collection2 = createCollection(client2, dbId, collectionDefinition);

            CosmosItemProperties newDocument = new CosmosItemProperties();
            newDocument.setId("doc");
            BridgeInternal.setProperty(newDocument, "name", "New Created Document");
            BridgeInternal.setProperty(newDocument, "mypk", "mypk");
            createDocument(collection2, newDocument);

            readDocumentResponse = client1.getDatabase(dbId).getContainer(collectionId).getItem(newDocument.getId(), newDocument.get("mypk")).read().block();
            logger.info("Client 2 READ Document Client Side Request Statistics {}", readDocumentResponse.getCosmosResponseDiagnosticsString());
            logger.info("Client 2 READ Document Latency {}", readDocumentResponse.getRequestLatency());

            CosmosItemProperties readDocument = readDocumentResponse.getProperties();

            assertThat(readDocument.getId().equals(newDocument.getId())).isTrue();
            assertThat(readDocument.get("name").equals(newDocument.get("name"))).isTrue();
        } finally {
            safeDeleteDatabase(db);
            safeClose(client1);
            safeClose(client2);
        }
    }

    @BeforeClass(groups = { "emulator" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        client = clientBuilder().buildAsyncClient();
        database = createDatabase(client, databaseId);
    }

    @AfterClass(groups = { "emulator" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(database);
        safeClose(client);
    }
}
