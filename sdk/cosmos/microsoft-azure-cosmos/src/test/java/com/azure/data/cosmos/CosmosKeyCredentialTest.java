package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.FailureValidator;
import com.azure.data.cosmos.internal.TestConfigurations;
import com.azure.data.cosmos.rx.TestSuiteBase;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class CosmosKeyCredentialTest extends TestSuiteBase {

    private static final int TIMEOUT = 50000;
    private static final int SETUP_TIMEOUT = 20000;
    private static final int SHUTDOWN_TIMEOUT = 20000;

    private final List<String> databases = new ArrayList<>();
    private final String databaseId = CosmosDatabaseForTest.generateId();

    private CosmosClient client;
    private CosmosDatabase database;
    private CosmosContainer container;

    @Factory(dataProvider = "clientBuildersWithDirect")
    public CosmosKeyCredentialTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
        this.subscriberValidationTimeout = TIMEOUT;
    }

    @DataProvider(name = "crudArgProvider")
    public Object[][] crudArgProvider() {
        return new Object[][] {
            // collection name, is name base
            { UUID.randomUUID().toString()} ,

            // with special characters in the name.
            {"+ -_,:.|~" + UUID.randomUUID().toString() + " +-_,:.|~"} ,
        };
    }

    private CosmosContainerProperties getCollectionDefinition(String collectionName) {
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<>();
        paths.add("/mypk");
        partitionKeyDef.paths(paths);

        return new CosmosContainerProperties(
            collectionName,
            partitionKeyDef);
    }

    private CosmosItemProperties getDocumentDefinition(String documentId) {
        final String uuid = UUID.randomUUID().toString();
        return new CosmosItemProperties(String.format("{ "
                + "\"id\": \"%s\", "
                + "\"mypk\": \"%s\", "
                + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]"
                + "}"
            , documentId, uuid));
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "crudArgProvider")
    public void createCollectionWithSecondaryKey(String collectionName) throws InterruptedException {
        CosmosContainerProperties collectionDefinition = getCollectionDefinition(collectionName);

        // sanity check
        assertThat(client.cosmosKeyCredential().key()).isEqualTo(TestConfigurations.MASTER_KEY);

        cosmosKeyCredential.key(TestConfigurations.SECONDARY_MASTER_KEY);
        Mono<CosmosContainerResponse> createObservable = database
            .createContainer(collectionDefinition);

        CosmosResponseValidator<CosmosContainerResponse> validator = new CosmosResponseValidator.Builder<CosmosContainerResponse>()
            .withId(collectionDefinition.id()).build();

        validateSuccess(createObservable, validator);

        //  sanity check
        assertThat(client.cosmosKeyCredential().key()).isEqualTo(TestConfigurations.SECONDARY_MASTER_KEY);
        safeDeleteAllCollections(database);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "crudArgProvider")
    public void readCollectionWithSecondaryKey(String collectionName) throws InterruptedException {
        CosmosContainerProperties collectionDefinition = getCollectionDefinition(collectionName);

        // sanity check
        assertThat(client.cosmosKeyCredential().key()).isEqualTo(TestConfigurations.MASTER_KEY);

        Mono<CosmosContainerResponse> createObservable = database.createContainer(collectionDefinition);
        CosmosContainer collection = createObservable.block().container();

        cosmosKeyCredential.key(TestConfigurations.SECONDARY_MASTER_KEY);
        Mono<CosmosContainerResponse> readObservable = collection.read();

        CosmosResponseValidator<CosmosContainerResponse> validator =
            new CosmosResponseValidator.Builder<CosmosContainerResponse>()
            .withId(collection.id()).build();
        validateSuccess(readObservable, validator);

        //  sanity check
        assertThat(client.cosmosKeyCredential().key()).isEqualTo(TestConfigurations.SECONDARY_MASTER_KEY);
        safeDeleteAllCollections(database);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "crudArgProvider")
    public void deleteCollectionWithSecondaryKey(String collectionName) throws InterruptedException {
        CosmosContainerProperties collectionDefinition = getCollectionDefinition(collectionName);

        // sanity check
        assertThat(client.cosmosKeyCredential().key()).isEqualTo(TestConfigurations.MASTER_KEY);

        Mono<CosmosContainerResponse> createObservable = database.createContainer(collectionDefinition);
        CosmosContainer collection = createObservable.block().container();

        cosmosKeyCredential.key(TestConfigurations.SECONDARY_MASTER_KEY);
        Mono<CosmosContainerResponse> deleteObservable = collection.delete();

        CosmosResponseValidator<CosmosContainerResponse> validator = new CosmosResponseValidator.Builder<CosmosContainerResponse>()
            .nullResource().build();
        validateSuccess(deleteObservable, validator);

        //  sanity check
        assertThat(client.cosmosKeyCredential().key()).isEqualTo(TestConfigurations.SECONDARY_MASTER_KEY);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "crudArgProvider")
    public void replaceCollectionWithSecondaryKey(String collectionName) throws InterruptedException  {
        // create a collection
        CosmosContainerProperties collectionDefinition = getCollectionDefinition(collectionName);
        Mono<CosmosContainerResponse> createObservable = database.createContainer(collectionDefinition);
        CosmosContainer collection = createObservable.block().container();

        // sanity check
        assertThat(client.cosmosKeyCredential().key()).isEqualTo(TestConfigurations.MASTER_KEY);

        CosmosContainerProperties collectionSettings = collection.read().block().properties();
        // sanity check
        assertThat(collectionSettings.indexingPolicy().indexingMode()).isEqualTo(IndexingMode.CONSISTENT);

        cosmosKeyCredential.key(TestConfigurations.SECONDARY_MASTER_KEY);

        // replace indexing mode
        IndexingPolicy indexingMode = new IndexingPolicy();
        indexingMode.indexingMode(IndexingMode.LAZY);
        collectionSettings.indexingPolicy(indexingMode);
        Mono<CosmosContainerResponse> readObservable = collection.replace(collectionSettings, new CosmosContainerRequestOptions());

        // validate
        CosmosResponseValidator<CosmosContainerResponse> validator = new CosmosResponseValidator.Builder<CosmosContainerResponse>()
            .indexingMode(IndexingMode.LAZY).build();
        validateSuccess(readObservable, validator);

        //  sanity check
        assertThat(client.cosmosKeyCredential().key()).isEqualTo(TestConfigurations.SECONDARY_MASTER_KEY);
        safeDeleteAllCollections(database);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "crudArgProvider")
    public void createDocumentWithSecondaryKey(String documentId) throws InterruptedException {

        // sanity check
        assertThat(client.cosmosKeyCredential().key()).isEqualTo(TestConfigurations.MASTER_KEY);

        cosmosKeyCredential.key(TestConfigurations.SECONDARY_MASTER_KEY);

        CosmosItemProperties properties = getDocumentDefinition(documentId);
        Mono<CosmosItemResponse> createObservable = container.createItem(properties, new CosmosItemRequestOptions());

        CosmosResponseValidator<CosmosItemResponse> validator = new CosmosResponseValidator.Builder<CosmosItemResponse>()
            .withId(properties.id())
            .build();

        validateSuccess(createObservable, validator);

        //  sanity check
        assertThat(client.cosmosKeyCredential().key()).isEqualTo(TestConfigurations.SECONDARY_MASTER_KEY);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "crudArgProvider")
    public void readDocumentWithSecondaryKey(String documentId) throws InterruptedException {

        // sanity check
        assertThat(client.cosmosKeyCredential().key()).isEqualTo(TestConfigurations.MASTER_KEY);

        cosmosKeyCredential.key(TestConfigurations.SECONDARY_MASTER_KEY);

        CosmosItemProperties docDefinition = getDocumentDefinition(documentId);
        CosmosItem document = container.createItem(docDefinition, new CosmosItemRequestOptions()).block().item();

        waitIfNeededForReplicasToCatchUp(clientBuilder());

        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        options.partitionKey(new PartitionKey(docDefinition.get("mypk")));
        Mono<CosmosItemResponse> readObservable = document.read(options);

        CosmosResponseValidator<CosmosItemResponse> validator = new CosmosResponseValidator.Builder<CosmosItemResponse>()
            .withId(document.id())
            .build();

        validateSuccess(readObservable, validator);

        //  sanity check
        assertThat(client.cosmosKeyCredential().key()).isEqualTo(TestConfigurations.SECONDARY_MASTER_KEY);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "crudArgProvider")
    public void deleteDocumentWithSecondaryKey(String documentId) throws InterruptedException {

        // sanity check
        assertThat(client.cosmosKeyCredential().key()).isEqualTo(TestConfigurations.MASTER_KEY);

        cosmosKeyCredential.key(TestConfigurations.SECONDARY_MASTER_KEY);

        CosmosItemProperties docDefinition = getDocumentDefinition(documentId);

        CosmosItem document = container.createItem(docDefinition, new CosmosItemRequestOptions()).block().item();

        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        options.partitionKey(new PartitionKey(docDefinition.get("mypk")));
        Mono<CosmosItemResponse> deleteObservable = document.delete(options);


        CosmosResponseValidator<CosmosItemResponse> validator = new CosmosResponseValidator.Builder<CosmosItemResponse>()
            .nullResource().build();
        validateSuccess(deleteObservable, validator);

        // attempt to read document which was deleted
        waitIfNeededForReplicasToCatchUp(clientBuilder());

        Mono<CosmosItemResponse> readObservable = document.read(options);
        FailureValidator notFoundValidator = new FailureValidator.Builder().resourceNotFound().build();
        validateFailure(readObservable, notFoundValidator);

        //  sanity check
        assertThat(client.cosmosKeyCredential().key()).isEqualTo(TestConfigurations.SECONDARY_MASTER_KEY);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void createDatabaseWithSecondaryKey() throws Exception {
        // sanity check
        assertThat(client.cosmosKeyCredential().key()).isEqualTo(TestConfigurations.MASTER_KEY);

        cosmosKeyCredential.key(TestConfigurations.SECONDARY_MASTER_KEY);

        CosmosDatabaseProperties databaseDefinition = new CosmosDatabaseProperties(CosmosDatabaseForTest.generateId());
        databases.add(databaseDefinition.id());
        // create the database
        Mono<CosmosDatabaseResponse> createObservable = client.createDatabase(databaseDefinition, new CosmosDatabaseRequestOptions());

        // validate
        CosmosResponseValidator<CosmosDatabaseResponse> validator = new CosmosResponseValidator.Builder<CosmosDatabaseResponse>()
            .withId(databaseDefinition.id()).build();
        validateSuccess(createObservable, validator);
        //  sanity check
        assertThat(client.cosmosKeyCredential().key()).isEqualTo(TestConfigurations.SECONDARY_MASTER_KEY);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readDatabaseWithSecondaryKey() throws Exception {
        // sanity check
        assertThat(client.cosmosKeyCredential().key()).isEqualTo(TestConfigurations.MASTER_KEY);

        cosmosKeyCredential.key(TestConfigurations.SECONDARY_MASTER_KEY);

        // read database
        Mono<CosmosDatabaseResponse> readObservable = client.getDatabase(databaseId).read();

        // validate
        CosmosResponseValidator<CosmosDatabaseResponse> validator = new CosmosResponseValidator.Builder<CosmosDatabaseResponse>()
            .withId(databaseId).build();
        validateSuccess(readObservable, validator);
        //  sanity check
        assertThat(client.cosmosKeyCredential().key()).isEqualTo(TestConfigurations.SECONDARY_MASTER_KEY);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void deleteDatabaseWithSecondaryKey() throws Exception {
        // sanity check
        assertThat(client.cosmosKeyCredential().key()).isEqualTo(TestConfigurations.MASTER_KEY);

        cosmosKeyCredential.key(TestConfigurations.SECONDARY_MASTER_KEY);

        // create the database
        CosmosDatabaseProperties databaseDefinition = new CosmosDatabaseProperties(CosmosDatabaseForTest.generateId());
        databases.add(databaseDefinition.id());

        CosmosDatabase database = client.createDatabase(databaseDefinition, new CosmosDatabaseRequestOptions()).block().database();
        // delete the database
        Mono<CosmosDatabaseResponse> deleteObservable = database.delete();

        // validate
        CosmosResponseValidator<CosmosDatabaseResponse> validator = new CosmosResponseValidator.Builder<CosmosDatabaseResponse>()
            .nullResource().build();
        validateSuccess(deleteObservable, validator);
        //  sanity check
        assertThat(client.cosmosKeyCredential().key()).isEqualTo(TestConfigurations.SECONDARY_MASTER_KEY);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT,
        expectedExceptions = IllegalArgumentException.class,
        expectedExceptionsMessageRegExp = "Illegal base64 character .*")
    public void invalidSecondaryKey() throws Exception {
        // sanity check
        assertThat(client.cosmosKeyCredential().key()).isEqualTo(TestConfigurations.MASTER_KEY);

        cosmosKeyCredential.key("Invalid Secondary Key");

        // create the database, and this should throw Illegal Argument Exception for secondary key
        CosmosDatabaseProperties databaseDefinition = new CosmosDatabaseProperties(CosmosDatabaseForTest.generateId());
        client.createDatabase(databaseDefinition, new CosmosDatabaseRequestOptions()).block().database();
    }

    @AfterMethod(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void afterMethod() {
        //  Set back master key before every test
        cosmosKeyCredential.key(TestConfigurations.MASTER_KEY);
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        client = clientBuilder().build();
        database = createDatabase(client, databaseId);
        container = getSharedMultiPartitionCosmosContainer(client);
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(database);
        for(String dbId: databases) {
            safeDeleteDatabase(client.getDatabase(dbId));
        }
        safeClose(client);
    }
}
