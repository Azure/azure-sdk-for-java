// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.models.CompositePath;
import com.azure.cosmos.models.CompositePathSortOrder;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerRequestOptions;
import com.azure.cosmos.CosmosDatabaseForTest;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.CosmosResponseValidator;
import com.azure.cosmos.models.IndexingMode;
import com.azure.cosmos.models.IndexingPolicy;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.models.SpatialSpec;
import com.azure.cosmos.models.SpatialType;
import com.azure.cosmos.implementation.Database;
import com.azure.cosmos.implementation.FailureValidator;
import com.azure.cosmos.implementation.RetryAnalyzer;
import com.azure.cosmos.models.ThroughputProperties;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
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

    @Test(groups = { "emulator" }, timeOut = TIMEOUT, dataProvider = "collectionCrudArgProvider")
    public void createCollection(String collectionName) throws InterruptedException {
        CosmosContainerProperties collectionDefinition = getCollectionDefinition(collectionName);

        Mono<CosmosContainerResponse> createObservable = database
                .createContainer(collectionDefinition);

        CosmosResponseValidator<CosmosContainerResponse> validator = new CosmosResponseValidator.Builder<CosmosContainerResponse>()
                .withId(collectionDefinition.getId()).build();

        validateSuccess(createObservable, validator);
        safeDeleteAllCollections(database);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT, dataProvider = "collectionCrudArgProvider")
    public void createCollectionWithTTL(String collectionName) throws InterruptedException {
        CosmosContainerProperties collectionDefinition = getCollectionDefinition(collectionName);

        Integer defaultTimeToLive = 300;
        collectionDefinition.setDefaultTimeToLiveInSeconds(defaultTimeToLive);

        Mono<CosmosContainerResponse> createObservable = database
            .createContainer(collectionDefinition);

        CosmosResponseValidator<CosmosContainerResponse> validator = new CosmosResponseValidator.Builder<CosmosContainerResponse>()
            .withId(collectionDefinition.getId()).withDefaultTimeToLive(defaultTimeToLive).build();

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

            SpatialSpec spec = new SpatialSpec();
            spec.setPath("/path" + index + "/*");

            List<SpatialType> collectionOfSpatialTypes = new ArrayList<SpatialType>(Arrays.asList(spatialTypes).subList(0, index + 3));
            spec.setSpatialTypes(collectionOfSpatialTypes);
            spatialIndexes.add(spec);
        }

        indexingPolicy.setSpatialIndexes(spatialIndexes);

        collection.setIndexingPolicy(indexingPolicy);

        Mono<CosmosContainerResponse> createObservable = database
                .createContainer(collection, new CosmosContainerRequestOptions());

        CosmosResponseValidator<CosmosContainerResponse> validator = new CosmosResponseValidator.Builder<CosmosContainerResponse>()
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

        database.createContainer(collectionDefinition).block();
        CosmosAsyncContainer collection = database.getContainer(collectionDefinition.getId());

        Mono<CosmosContainerResponse> readObservable = collection.read();

        CosmosResponseValidator<CosmosContainerResponse> validator = new CosmosResponseValidator.Builder<CosmosContainerResponse>()
                .withId(collection.getId()).build();
        validateSuccess(readObservable, validator);
        safeDeleteAllCollections(database);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT, dataProvider = "collectionCrudArgProvider")
    public void readCollectionWithTTL(String collectionName) throws InterruptedException {
        CosmosContainerProperties collectionDefinition = getCollectionDefinition(collectionName);

        Integer defaultTimeToLive = 200;
        collectionDefinition.setDefaultTimeToLiveInSeconds(defaultTimeToLive);

        database.createContainer(collectionDefinition).block();
        CosmosAsyncContainer collection = database.getContainer(collectionDefinition.getId());

        Mono<CosmosContainerResponse> readObservable = collection.read();

        CosmosResponseValidator<CosmosContainerResponse> validator = new CosmosResponseValidator.Builder<CosmosContainerResponse>()
            .withId(collection.getId()).withDefaultTimeToLive(defaultTimeToLive).build();
        validateSuccess(readObservable, validator);
        safeDeleteAllCollections(database);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT, dataProvider = "collectionCrudArgProvider")
    public void readCollection_DoesntExist(String collectionName) throws Exception {

        Mono<CosmosContainerResponse> readObservable = database
                .getContainer("I don't exist").read();

        FailureValidator validator = new FailureValidator.Builder()
            .resourceNotFound()
            .documentClientExceptionToStringExcludesHeader(HttpConstants.HttpHeaders.AUTHORIZATION)
            .build();
        validateFailure(readObservable, validator);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT, dataProvider = "collectionCrudArgProvider")
    public void deleteCollection(String collectionName) throws InterruptedException {
        CosmosContainerProperties collectionDefinition = getCollectionDefinition(collectionName);

        database.createContainer(collectionDefinition).block();
        CosmosAsyncContainer collection = database.getContainer(collectionDefinition.getId());

        Mono<CosmosContainerResponse> deleteObservable = collection.delete();

        CosmosResponseValidator<CosmosContainerResponse> validator = new CosmosResponseValidator.Builder<CosmosContainerResponse>()
                .nullResource().build();
        validateSuccess(deleteObservable, validator);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT, dataProvider = "collectionCrudArgProvider")
    public void replaceCollection(String collectionName) throws InterruptedException  {
        // create a collection
        CosmosContainerProperties collectionDefinition = getCollectionDefinition(collectionName);
        database.createContainer(collectionDefinition).block();
        CosmosAsyncContainer collection = database.getContainer(collectionDefinition.getId());
        CosmosContainerProperties collectionSettings = collection.read().block().getProperties();
        // sanity check
        assertThat(collectionSettings.getIndexingPolicy().getIndexingMode()).isEqualTo(IndexingMode.CONSISTENT);

        // replace indexing getMode
        IndexingPolicy indexingMode = new IndexingPolicy();
        indexingMode.setIndexingMode(IndexingMode.CONSISTENT);
        collectionSettings.setIndexingPolicy(indexingMode);
        Mono<CosmosContainerResponse> readObservable = collection.replace(collectionSettings, new CosmosContainerRequestOptions());

        // validate
        CosmosResponseValidator<CosmosContainerResponse> validator = new CosmosResponseValidator.Builder<CosmosContainerResponse>()
                        .indexingMode(IndexingMode.CONSISTENT).build();
        validateSuccess(readObservable, validator);
        safeDeleteAllCollections(database);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT, dataProvider = "collectionCrudArgProvider")
    public void replaceCollectionWithTTL(String collectionName) throws InterruptedException {
        // create a collection
        CosmosContainerProperties collectionDefinition = getCollectionDefinition(collectionName);
        Integer defaultTimeToLive = 120;
        collectionDefinition.setDefaultTimeToLiveInSeconds(defaultTimeToLive);
        database.createContainer(collectionDefinition).block();
        CosmosAsyncContainer collection = database.getContainer(collectionDefinition.getId());
        CosmosContainerProperties collectionSettings = collection.read().block().getProperties();
        // sanity check
        assertThat(collectionSettings.getIndexingPolicy().getIndexingMode()).isEqualTo(IndexingMode.CONSISTENT);
        assertThat(collectionSettings.getDefaultTimeToLiveInSeconds()).isEqualTo(defaultTimeToLive);

        // replace indexing mode
        IndexingPolicy indexingMode = new IndexingPolicy();
        indexingMode.setIndexingMode(IndexingMode.CONSISTENT);
        collectionSettings.setIndexingPolicy(indexingMode);
        collectionSettings.setDefaultTimeToLiveInSeconds(defaultTimeToLive * 2);
        Mono<CosmosContainerResponse> readObservable = collection.replace(collectionSettings, new CosmosContainerRequestOptions());

        // validate
        CosmosResponseValidator<CosmosContainerResponse> validator = new CosmosResponseValidator.Builder<CosmosContainerResponse>()
            .indexingMode(IndexingMode.CONSISTENT).withDefaultTimeToLive(defaultTimeToLive * 2).build();
        validateSuccess(readObservable, validator);
        safeDeleteAllCollections(database);
    }


    @Test(groups = { "emulator" }, timeOut = 10 * TIMEOUT, retryAnalyzer = RetryAnalyzer.class)
    public void sessionTokenConsistencyCollectionDeleteCreateSameName() {
        CosmosAsyncClient client1 = getClientBuilder().buildAsyncClient();
        CosmosAsyncClient client2 = getClientBuilder().buildAsyncClient();

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

            InternalObjectNode document = new InternalObjectNode();
            document.setId("doc");
            BridgeInternal.setProperty(document, "name", "New Document");
            BridgeInternal.setProperty(document, "mypk", "mypkValue");
            createDocument(collection, document);
            CosmosItemRequestOptions options = new CosmosItemRequestOptions();
            CosmosItemResponse<InternalObjectNode> readDocumentResponse =
                collection.readItem(document.getId(), new PartitionKey("mypkValue"), options, InternalObjectNode.class).block();
            logger.info("Client 1 READ Document Client Side Request Statistics {}", readDocumentResponse.getDiagnostics());
            logger.info("Client 1 READ Document Latency {}", readDocumentResponse.getDuration());

            BridgeInternal.setProperty(document, "name", "New Updated Document");
            CosmosItemResponse<InternalObjectNode> upsertDocumentResponse = collection.upsertItem(document).block();
            logger.info("Client 1 Upsert Document Client Side Request Statistics {}", upsertDocumentResponse.getDiagnostics());
            logger.info("Client 1 Upsert Document Latency {}", upsertDocumentResponse.getDuration());

            //  DELETE the existing collection
            deleteCollection(client2, dbId, collectionId);
            //  Recreate the collection with the same name but with different client
            CosmosAsyncContainer collection2 = createCollection(client2, dbId, collectionDefinition);

            InternalObjectNode newDocument = new InternalObjectNode();
            newDocument.setId("doc");
            BridgeInternal.setProperty(newDocument, "name", "New Created Document");
            BridgeInternal.setProperty(newDocument, "mypk", "mypk");
            createDocument(collection2, newDocument);

            readDocumentResponse = client1.getDatabase(dbId)
                                       .getContainer(collectionId)
                                       .readItem(newDocument.getId(),
                                                 new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(newDocument, "mypk")),
                                                 InternalObjectNode.class)
                                       .block();
            logger.info("Client 2 READ Document Client Side Request Statistics {}", readDocumentResponse.getDiagnostics());
            logger.info("Client 2 READ Document Latency {}", readDocumentResponse.getDuration());

            InternalObjectNode readDocument = BridgeInternal.getProperties(readDocumentResponse);

            assertThat(readDocument.getId().equals(newDocument.getId())).isTrue();
            assertThat(ModelBridgeInternal.getObjectFromJsonSerializable(readDocument, "name")
                                          .equals(ModelBridgeInternal.getObjectFromJsonSerializable(newDocument, "name"))).isTrue();
        } finally {
            safeDeleteDatabase(db);
            safeClose(client1);
            safeClose(client2);
        }
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void replaceProvisionedThroughput(){
        CosmosContainerProperties containerProperties = new CosmosContainerProperties("testCol", "/myPk");
        database.createContainer(
            containerProperties,
            ThroughputProperties.createManualThroughput(1000),
            new CosmosContainerRequestOptions()).block();

        CosmosAsyncContainer container = database.getContainer(containerProperties.getId());
        Integer throughput = container.readThroughput().block()
            .getProperties().getManualThroughput();

        assertThat(throughput).isEqualTo(1000);

        throughput = container.replaceThroughput(ThroughputProperties.createManualThroughput(2000))
            .block()
            .getProperties()
            .getManualThroughput();
        assertThat(throughput).isEqualTo(2000);
    }


    @BeforeClass(groups = { "emulator" }, timeOut = SETUP_TIMEOUT)
    public void before_CollectionCrudTest() {
        client = getClientBuilder().buildAsyncClient();
        database = createDatabase(client, databaseId);
    }

    @AfterClass(groups = { "emulator" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(database);
        safeClose(client);
    }
}
