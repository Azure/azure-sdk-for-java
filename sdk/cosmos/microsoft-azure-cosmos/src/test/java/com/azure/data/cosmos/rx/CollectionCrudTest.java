// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.rx;

import com.azure.data.cosmos.*;
import com.azure.data.cosmos.CosmosAsyncClient;
import com.azure.data.cosmos.internal.Database;
import com.azure.data.cosmos.internal.FailureValidator;
import com.azure.data.cosmos.internal.RetryAnalyzer;
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
        partitionKeyDef.paths(paths);

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
                .withId(collectionDefinition.id()).build();

        validateSuccess(createObservable, validator);
        safeDeleteAllCollections(database);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void createCollectionWithCompositeIndexAndSpatialSpec() throws InterruptedException {
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<String>();
        paths.add("/mypk");
        partitionKeyDef.paths(paths);

        CosmosContainerProperties collection = new CosmosContainerProperties(
                UUID.randomUUID().toString(),
                partitionKeyDef);

        IndexingPolicy indexingPolicy = new IndexingPolicy();
        CompositePath compositePath1 = new CompositePath();
        compositePath1.path("/path1");
        compositePath1.order(CompositePathSortOrder.ASCENDING);
        CompositePath compositePath2 = new CompositePath();
        compositePath2.path("/path2");
        compositePath2.order(CompositePathSortOrder.DESCENDING);
        CompositePath compositePath3 = new CompositePath();
        compositePath3.path("/path3");
        CompositePath compositePath4 = new CompositePath();
        compositePath4.path("/path4");
        compositePath4.order(CompositePathSortOrder.ASCENDING);
        CompositePath compositePath5 = new CompositePath();
        compositePath5.path("/path5");
        compositePath5.order(CompositePathSortOrder.DESCENDING);
        CompositePath compositePath6 = new CompositePath();
        compositePath6.path("/path6");

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
        indexingPolicy.compositeIndexes(compositeIndexes);

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
            spec.path("/path" + index + "/*");

            for (int i = index; i < index + 3; i++) {
                collectionOfSpatialTypes.add(spatialTypes[i]);
            }
            spec.spatialTypes(collectionOfSpatialTypes);
            spatialIndexes.add(spec);
        }

        indexingPolicy.spatialIndexes(spatialIndexes);

        collection.indexingPolicy(indexingPolicy);
        
        Mono<CosmosAsyncContainerResponse> createObservable = database
                .createContainer(collection, new CosmosContainerRequestOptions());

        CosmosResponseValidator<CosmosAsyncContainerResponse> validator = new CosmosResponseValidator.Builder<CosmosAsyncContainerResponse>()
                .withId(collection.id())
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
        CosmosAsyncContainer collection = createObservable.block().container();

        Mono<CosmosAsyncContainerResponse> readObservable = collection.read();

        CosmosResponseValidator<CosmosAsyncContainerResponse> validator = new CosmosResponseValidator.Builder<CosmosAsyncContainerResponse>()
                .withId(collection.id()).build();
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
        CosmosAsyncContainer collection = createObservable.block().container();

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
        CosmosAsyncContainer collection = createObservable.block().container();
        CosmosContainerProperties collectionSettings = collection.read().block().properties();
        // sanity check
        assertThat(collectionSettings.indexingPolicy().indexingMode()).isEqualTo(IndexingMode.CONSISTENT);
        
        // replace indexing mode
        IndexingPolicy indexingMode = new IndexingPolicy();
        indexingMode.indexingMode(IndexingMode.LAZY);
        collectionSettings.indexingPolicy(indexingMode);
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
            databaseDefinition.id(dbId);
            db = createDatabase(client1, dbId);

            PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
            ArrayList<String> paths = new ArrayList<String>();
            paths.add("/mypk");
            partitionKeyDef.paths(paths);

            CosmosContainerProperties collectionDefinition = new CosmosContainerProperties(collectionId, partitionKeyDef);
            CosmosAsyncContainer collection = createCollection(db, collectionDefinition, new CosmosContainerRequestOptions());

            CosmosItemProperties document = new CosmosItemProperties();
            document.id("doc");
            BridgeInternal.setProperty(document, "name", "New Document");
            BridgeInternal.setProperty(document, "mypk", "mypkValue");
            CosmosAsyncItem item = createDocument(collection, document);
            CosmosItemRequestOptions options = new CosmosItemRequestOptions();
            options.partitionKey(new PartitionKey("mypkValue"));
            CosmosAsyncItemResponse readDocumentResponse = item.read(options).block();
            logger.info("Client 1 READ Document Client Side Request Statistics {}", readDocumentResponse.cosmosResponseDiagnosticsString());
            logger.info("Client 1 READ Document Latency {}", readDocumentResponse.requestLatency());

            BridgeInternal.setProperty(document, "name", "New Updated Document");
            CosmosAsyncItemResponse upsertDocumentResponse = collection.upsertItem(document).block();
            logger.info("Client 1 Upsert Document Client Side Request Statistics {}", upsertDocumentResponse.cosmosResponseDiagnosticsString());
            logger.info("Client 1 Upsert Document Latency {}", upsertDocumentResponse.requestLatency());

            //  DELETE the existing collection
            deleteCollection(client2, dbId, collectionId);
            //  Recreate the collection with the same name but with different client
            CosmosAsyncContainer collection2 = createCollection(client2, dbId, collectionDefinition);

            CosmosItemProperties newDocument = new CosmosItemProperties();
            newDocument.id("doc");
            BridgeInternal.setProperty(newDocument, "name", "New Created Document");
            BridgeInternal.setProperty(newDocument, "mypk", "mypk");
            createDocument(collection2, newDocument);

            readDocumentResponse = client1.getDatabase(dbId).getContainer(collectionId).getItem(newDocument.id(), newDocument.get("mypk")).read().block();
            logger.info("Client 2 READ Document Client Side Request Statistics {}", readDocumentResponse.cosmosResponseDiagnosticsString());
            logger.info("Client 2 READ Document Latency {}", readDocumentResponse.requestLatency());

            CosmosItemProperties readDocument = readDocumentResponse.properties();

            assertThat(readDocument.id().equals(newDocument.id())).isTrue();
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
