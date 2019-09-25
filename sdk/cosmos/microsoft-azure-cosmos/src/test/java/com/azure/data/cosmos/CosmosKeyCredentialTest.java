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

    private CosmosAsyncClient client;
    private CosmosAsyncDatabase database;
    private CosmosAsyncContainer container;

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
        partitionKeyDef.setPaths(paths);

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
        assertThat(client.cosmosKeyCredential().getKey()).isEqualTo(TestConfigurations.MASTER_KEY);

        cosmosKeyCredential.setKey(TestConfigurations.SECONDARY_MASTER_KEY);
        Mono<CosmosAsyncContainerResponse> createObservable = database
            .createContainer(collectionDefinition);

        CosmosResponseValidator<CosmosAsyncContainerResponse> validator = new CosmosResponseValidator.Builder<CosmosAsyncContainerResponse>()
            .withId(collectionDefinition.getId()).build();

        validateSuccess(createObservable, validator);

        //  sanity check
        assertThat(client.cosmosKeyCredential().getKey()).isEqualTo(TestConfigurations.SECONDARY_MASTER_KEY);
        safeDeleteAllCollections(database);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "crudArgProvider")
    public void readCollectionWithSecondaryKey(String collectionName) throws InterruptedException {
        CosmosContainerProperties collectionDefinition = getCollectionDefinition(collectionName);

        // sanity check
        assertThat(client.cosmosKeyCredential().getKey()).isEqualTo(TestConfigurations.MASTER_KEY);

        Mono<CosmosAsyncContainerResponse> createObservable = database.createContainer(collectionDefinition);
        CosmosAsyncContainer collection = createObservable.block().getContainer();

        cosmosKeyCredential.setKey(TestConfigurations.SECONDARY_MASTER_KEY);
        Mono<CosmosAsyncContainerResponse> readObservable = collection.read();

        CosmosResponseValidator<CosmosAsyncContainerResponse> validator =
            new CosmosResponseValidator.Builder<CosmosAsyncContainerResponse>()
            .withId(collection.getId()).build();
        validateSuccess(readObservable, validator);

        //  sanity check
        assertThat(client.cosmosKeyCredential().getKey()).isEqualTo(TestConfigurations.SECONDARY_MASTER_KEY);
        safeDeleteAllCollections(database);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "crudArgProvider")
    public void deleteCollectionWithSecondaryKey(String collectionName) throws InterruptedException {
        CosmosContainerProperties collectionDefinition = getCollectionDefinition(collectionName);

        // sanity check
        assertThat(client.cosmosKeyCredential().getKey()).isEqualTo(TestConfigurations.MASTER_KEY);

        Mono<CosmosAsyncContainerResponse> createObservable = database.createContainer(collectionDefinition);
        CosmosAsyncContainer collection = createObservable.block().getContainer();

        cosmosKeyCredential.setKey(TestConfigurations.SECONDARY_MASTER_KEY);
        Mono<CosmosAsyncContainerResponse> deleteObservable = collection.delete();

        CosmosResponseValidator<CosmosAsyncContainerResponse> validator = new CosmosResponseValidator.Builder<CosmosAsyncContainerResponse>()
            .nullResource().build();
        validateSuccess(deleteObservable, validator);

        //  sanity check
        assertThat(client.cosmosKeyCredential().getKey()).isEqualTo(TestConfigurations.SECONDARY_MASTER_KEY);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "crudArgProvider")
    public void replaceCollectionWithSecondaryKey(String collectionName) throws InterruptedException  {
        // create a collection
        CosmosContainerProperties collectionDefinition = getCollectionDefinition(collectionName);
        Mono<CosmosAsyncContainerResponse> createObservable = database.createContainer(collectionDefinition);
        CosmosAsyncContainer collection = createObservable.block().getContainer();

        // sanity check
        assertThat(client.cosmosKeyCredential().getKey()).isEqualTo(TestConfigurations.MASTER_KEY);

        CosmosContainerProperties collectionSettings = collection.read().block().getProperties();
        // sanity check
        assertThat(collectionSettings.getIndexingPolicy().getIndexingMode()).isEqualTo(IndexingMode.CONSISTENT);

        cosmosKeyCredential.setKey(TestConfigurations.SECONDARY_MASTER_KEY);

        // replace indexing mode
        IndexingPolicy indexingMode = new IndexingPolicy();
        indexingMode.setIndexingMode(IndexingMode.LAZY);
        collectionSettings.setIndexingPolicy(indexingMode);
        Mono<CosmosAsyncContainerResponse> readObservable = collection.replace(collectionSettings, new CosmosContainerRequestOptions());

        // validate
        CosmosResponseValidator<CosmosAsyncContainerResponse> validator = new CosmosResponseValidator.Builder<CosmosAsyncContainerResponse>()
            .indexingMode(IndexingMode.LAZY).build();
        validateSuccess(readObservable, validator);

        //  sanity check
        assertThat(client.cosmosKeyCredential().getKey()).isEqualTo(TestConfigurations.SECONDARY_MASTER_KEY);
        safeDeleteAllCollections(database);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "crudArgProvider")
    public void createDocumentWithSecondaryKey(String documentId) throws InterruptedException {

        // sanity check
        assertThat(client.cosmosKeyCredential().getKey()).isEqualTo(TestConfigurations.MASTER_KEY);

        cosmosKeyCredential.setKey(TestConfigurations.SECONDARY_MASTER_KEY);

        CosmosItemProperties properties = getDocumentDefinition(documentId);
        Mono<CosmosAsyncItemResponse> createObservable = container.createItem(properties, new CosmosItemRequestOptions());

        CosmosResponseValidator<CosmosAsyncItemResponse> validator = new CosmosResponseValidator.Builder<CosmosAsyncItemResponse>()
            .withId(properties.getId())
            .build();

        validateSuccess(createObservable, validator);

        //  sanity check
        assertThat(client.cosmosKeyCredential().getKey()).isEqualTo(TestConfigurations.SECONDARY_MASTER_KEY);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "crudArgProvider")
    public void readDocumentWithSecondaryKey(String documentId) throws InterruptedException {

        // sanity check
        assertThat(client.cosmosKeyCredential().getKey()).isEqualTo(TestConfigurations.MASTER_KEY);

        cosmosKeyCredential.setKey(TestConfigurations.SECONDARY_MASTER_KEY);

        CosmosItemProperties docDefinition = getDocumentDefinition(documentId);
        CosmosAsyncItem document = container.createItem(docDefinition, new CosmosItemRequestOptions()).block().getItem();

        waitIfNeededForReplicasToCatchUp(clientBuilder());

        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        options.setPartitionKey(new PartitionKey(docDefinition.get("mypk")));
        Mono<CosmosAsyncItemResponse> readObservable = document.read(options);

        CosmosResponseValidator<CosmosAsyncItemResponse> validator = new CosmosResponseValidator.Builder<CosmosAsyncItemResponse>()
            .withId(document.getId())
            .build();

        validateSuccess(readObservable, validator);

        //  sanity check
        assertThat(client.cosmosKeyCredential().getKey()).isEqualTo(TestConfigurations.SECONDARY_MASTER_KEY);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "crudArgProvider")
    public void deleteDocumentWithSecondaryKey(String documentId) throws InterruptedException {

        // sanity check
        assertThat(client.cosmosKeyCredential().getKey()).isEqualTo(TestConfigurations.MASTER_KEY);

        cosmosKeyCredential.setKey(TestConfigurations.SECONDARY_MASTER_KEY);

        CosmosItemProperties docDefinition = getDocumentDefinition(documentId);

        CosmosAsyncItem document = container.createItem(docDefinition, new CosmosItemRequestOptions()).block().getItem();

        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        options.setPartitionKey(new PartitionKey(docDefinition.get("mypk")));
        Mono<CosmosAsyncItemResponse> deleteObservable = document.delete(options);


        CosmosResponseValidator<CosmosAsyncItemResponse> validator = new CosmosResponseValidator.Builder<CosmosAsyncItemResponse>()
            .nullResource().build();
        validateSuccess(deleteObservable, validator);

        // attempt to read document which was deleted
        waitIfNeededForReplicasToCatchUp(clientBuilder());

        Mono<CosmosAsyncItemResponse> readObservable = document.read(options);
        FailureValidator notFoundValidator = new FailureValidator.Builder().resourceNotFound().build();
        validateFailure(readObservable, notFoundValidator);

        //  sanity check
        assertThat(client.cosmosKeyCredential().getKey()).isEqualTo(TestConfigurations.SECONDARY_MASTER_KEY);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void createDatabaseWithSecondaryKey() throws Exception {
        // sanity check
        assertThat(client.cosmosKeyCredential().getKey()).isEqualTo(TestConfigurations.MASTER_KEY);

        cosmosKeyCredential.setKey(TestConfigurations.SECONDARY_MASTER_KEY);

        CosmosDatabaseProperties databaseDefinition = new CosmosDatabaseProperties(CosmosDatabaseForTest.generateId());
        databases.add(databaseDefinition.getId());
        // create the getDatabase
        Mono<CosmosAsyncDatabaseResponse> createObservable = client.createDatabase(databaseDefinition, new CosmosDatabaseRequestOptions());

        // validate
        CosmosResponseValidator<CosmosAsyncDatabaseResponse> validator = new CosmosResponseValidator.Builder<CosmosAsyncDatabaseResponse>()
            .withId(databaseDefinition.getId()).build();
        validateSuccess(createObservable, validator);
        //  sanity check
        assertThat(client.cosmosKeyCredential().getKey()).isEqualTo(TestConfigurations.SECONDARY_MASTER_KEY);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readDatabaseWithSecondaryKey() throws Exception {
        // sanity check
        assertThat(client.cosmosKeyCredential().getKey()).isEqualTo(TestConfigurations.MASTER_KEY);

        cosmosKeyCredential.setKey(TestConfigurations.SECONDARY_MASTER_KEY);

        // read database
        Mono<CosmosAsyncDatabaseResponse> readObservable = client.getDatabase(databaseId).read();

        // validate
        CosmosResponseValidator<CosmosAsyncDatabaseResponse> validator = new CosmosResponseValidator.Builder<CosmosAsyncDatabaseResponse>()
            .withId(databaseId).build();
        validateSuccess(readObservable, validator);
        //  sanity check
        assertThat(client.cosmosKeyCredential().getKey()).isEqualTo(TestConfigurations.SECONDARY_MASTER_KEY);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void deleteDatabaseWithSecondaryKey() throws Exception {
        // sanity check
        assertThat(client.cosmosKeyCredential().getKey()).isEqualTo(TestConfigurations.MASTER_KEY);

        cosmosKeyCredential.setKey(TestConfigurations.SECONDARY_MASTER_KEY);

        // create the database
        CosmosDatabaseProperties databaseDefinition = new CosmosDatabaseProperties(CosmosDatabaseForTest.generateId());
        databases.add(databaseDefinition.getId());

        CosmosAsyncDatabase database = client.createDatabase(databaseDefinition, new CosmosDatabaseRequestOptions()).block().getDatabase();
        // delete the getDatabase
        Mono<CosmosAsyncDatabaseResponse> deleteObservable = database.delete();

        // validate
        CosmosResponseValidator<CosmosAsyncDatabaseResponse> validator = new CosmosResponseValidator.Builder<CosmosAsyncDatabaseResponse>()
            .nullResource().build();
        validateSuccess(deleteObservable, validator);
        //  sanity check
        assertThat(client.cosmosKeyCredential().getKey()).isEqualTo(TestConfigurations.SECONDARY_MASTER_KEY);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT,
        expectedExceptions = IllegalArgumentException.class,
        expectedExceptionsMessageRegExp = "Illegal base64 character .*")
    public void invalidSecondaryKey() throws Exception {
        // sanity check
        assertThat(client.cosmosKeyCredential().getKey()).isEqualTo(TestConfigurations.MASTER_KEY);

        cosmosKeyCredential.setKey("Invalid Secondary Key");

        // create the database, and this should throw Illegal Argument Exception for secondary key
        CosmosDatabaseProperties databaseDefinition = new CosmosDatabaseProperties(CosmosDatabaseForTest.generateId());
        client.createDatabase(databaseDefinition, new CosmosDatabaseRequestOptions()).block().getDatabase();
    }

    @AfterMethod(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void afterMethod() {
        //  Set back master getKey before every test
        cosmosKeyCredential.setKey(TestConfigurations.MASTER_KEY);
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        client = clientBuilder().buildAsyncClient();
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
