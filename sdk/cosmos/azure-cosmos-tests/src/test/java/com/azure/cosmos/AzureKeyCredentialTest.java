package com.azure.cosmos;

import com.azure.cosmos.implementation.FailureValidator;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.implementation.RetryAnalyzer;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerRequestOptions;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosDatabaseProperties;
import com.azure.cosmos.models.CosmosDatabaseRequestOptions;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosResponse;
import com.azure.cosmos.models.IndexingMode;
import com.azure.cosmos.models.IndexingPolicy;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.rx.CosmosItemResponseValidator;
import com.azure.cosmos.rx.TestSuiteBase;
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

public class AzureKeyCredentialTest extends TestSuiteBase {

    private static final int TIMEOUT = 50000;
    private static final int SETUP_TIMEOUT = 20000;
    private static final int SHUTDOWN_TIMEOUT = 20000;

    private final List<String> databases = new ArrayList<>();
    private final String databaseId = CosmosDatabaseForTest.generateId();

    private CosmosAsyncClient client;
    private CosmosAsyncDatabase database;
    private CosmosAsyncContainer container;

    @Factory(dataProvider = "clientBuildersWithDirectSession")
    public AzureKeyCredentialTest(CosmosClientBuilder clientBuilder) {
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

    private InternalObjectNode getDocumentDefinition(String documentId) {
        final String uuid = UUID.randomUUID().toString();
        return new InternalObjectNode(String.format("{ "
                + "\"id\": \"%s\", "
                + "\"mypk\": \"%s\", "
                + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]"
                + "}"
            , documentId, uuid));
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "crudArgProvider", retryAnalyzer = RetryAnalyzer.class)
    public void createCollectionWithSecondaryKey(String collectionName) throws InterruptedException {
        CosmosContainerProperties collectionDefinition = getCollectionDefinition(collectionName);

        // sanity check
        assertThat(client.credential().getKey()).isEqualTo(TestConfigurations.MASTER_KEY);

        credential.update(TestConfigurations.SECONDARY_MASTER_KEY);
        Mono<CosmosContainerResponse> createObservable = database
            .createContainer(collectionDefinition);

        CosmosResponseValidator<CosmosContainerResponse> validator = new CosmosResponseValidator.Builder<CosmosContainerResponse>()
            .withId(collectionDefinition.getId()).build();

        validateSuccess(createObservable, validator);

        //  sanity check
        assertThat(client.credential().getKey()).isEqualTo(TestConfigurations.SECONDARY_MASTER_KEY);
        safeDeleteAllCollections(database);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "crudArgProvider", retryAnalyzer = RetryAnalyzer.class)
    public void readCollectionWithSecondaryKey(String collectionName) throws InterruptedException {
        CosmosContainerProperties collectionDefinition = getCollectionDefinition(collectionName);

        // sanity check
        assertThat(client.credential().getKey()).isEqualTo(TestConfigurations.MASTER_KEY);

        database.createContainer(collectionDefinition).block();
        CosmosAsyncContainer collection = database.getContainer(collectionDefinition.getId());

        credential.update(TestConfigurations.SECONDARY_MASTER_KEY);
        Mono<CosmosContainerResponse> readObservable = collection.read();

        CosmosResponseValidator<CosmosContainerResponse> validator =
            new CosmosResponseValidator.Builder<CosmosContainerResponse>()
            .withId(collection.getId()).build();
        validateSuccess(readObservable, validator);

        //  sanity check
        assertThat(client.credential().getKey()).isEqualTo(TestConfigurations.SECONDARY_MASTER_KEY);
        safeDeleteAllCollections(database);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "crudArgProvider", retryAnalyzer = RetryAnalyzer.class)
    public void deleteCollectionWithSecondaryKey(String collectionName) throws InterruptedException {
        CosmosContainerProperties collectionDefinition = getCollectionDefinition(collectionName);

        // sanity check
        assertThat(client.credential().getKey()).isEqualTo(TestConfigurations.MASTER_KEY);

        database.createContainer(collectionDefinition).block();
        CosmosAsyncContainer collection = database.getContainer(collectionDefinition.getId());

        credential.update(TestConfigurations.SECONDARY_MASTER_KEY);
        Mono<CosmosContainerResponse> deleteObservable = collection.delete();

        CosmosResponseValidator<CosmosContainerResponse> validator = new CosmosResponseValidator.Builder<CosmosContainerResponse>()
            .nullResource().build();
        validateSuccess(deleteObservable, validator);

        //  sanity check
        assertThat(client.credential().getKey()).isEqualTo(TestConfigurations.SECONDARY_MASTER_KEY);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "crudArgProvider", retryAnalyzer = RetryAnalyzer.class)
    public void replaceCollectionWithSecondaryKey(String collectionName) throws InterruptedException  {
        // create a collection
        CosmosContainerProperties collectionDefinition = getCollectionDefinition(collectionName);
        database.createContainer(collectionDefinition).block();
        CosmosAsyncContainer collection = database.getContainer(collectionDefinition.getId());

        // sanity check
        assertThat(client.credential().getKey()).isEqualTo(TestConfigurations.MASTER_KEY);

        CosmosContainerProperties collectionSettings = collection.read().block().getProperties();
        // sanity check
        assertThat(collectionSettings.getIndexingPolicy().getIndexingMode()).isEqualTo(IndexingMode.CONSISTENT);

        credential.update(TestConfigurations.SECONDARY_MASTER_KEY);

        // replace indexing mode
        IndexingPolicy indexingMode = new IndexingPolicy();
        indexingMode.setIndexingMode(IndexingMode.CONSISTENT);
        collectionSettings.setIndexingPolicy(indexingMode);
        Mono<CosmosContainerResponse> readObservable = collection.replace(collectionSettings, new CosmosContainerRequestOptions());

        // validate
        CosmosResponseValidator<CosmosContainerResponse> validator = new CosmosResponseValidator.Builder<CosmosContainerResponse>()
            .indexingMode(IndexingMode.CONSISTENT).build();
        validateSuccess(readObservable, validator);

        //  sanity check
        assertThat(client.credential().getKey()).isEqualTo(TestConfigurations.SECONDARY_MASTER_KEY);
        safeDeleteAllCollections(database);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "crudArgProvider", retryAnalyzer = RetryAnalyzer.class)
    public void createDocumentWithSecondaryKey(String documentId) throws InterruptedException {

        // sanity check
        assertThat(client.credential().getKey()).isEqualTo(TestConfigurations.MASTER_KEY);

        credential.update(TestConfigurations.SECONDARY_MASTER_KEY);

        InternalObjectNode properties = getDocumentDefinition(documentId);
        Mono<CosmosItemResponse<InternalObjectNode>> createObservable = container.createItem(properties, new CosmosItemRequestOptions());

        CosmosItemResponseValidator validator =
            new CosmosItemResponseValidator.Builder<CosmosItemResponse<InternalObjectNode>>()
                .withId(properties.getId())
                .build();
        validateItemSuccess(createObservable, validator);

        //  sanity check
        assertThat(client.credential().getKey()).isEqualTo(TestConfigurations.SECONDARY_MASTER_KEY);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "crudArgProvider", retryAnalyzer = RetryAnalyzer.class)
    public void readDocumentWithSecondaryKey(String documentId) throws InterruptedException {

        // sanity check
        assertThat(client.credential().getKey()).isEqualTo(TestConfigurations.MASTER_KEY);

        credential.update(TestConfigurations.SECONDARY_MASTER_KEY);

        InternalObjectNode docDefinition = getDocumentDefinition(documentId);
        container.createItem(docDefinition, new CosmosItemRequestOptions()).block();

        waitIfNeededForReplicasToCatchUp(getClientBuilder());

        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        ModelBridgeInternal.setPartitionKey(options, new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(docDefinition, "mypk")));
        Mono<CosmosItemResponse<InternalObjectNode>> readObservable = container.readItem(docDefinition.getId(),
                                                                                                new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(docDefinition, "mypk")),
                                                                                                options,
                                                                                                InternalObjectNode.class);

        CosmosItemResponseValidator validator =
            new CosmosItemResponseValidator.Builder<CosmosItemResponse<InternalObjectNode>>()
                .withId(docDefinition.getId())
                .build();
        validateItemSuccess(readObservable, validator);

        //  sanity check
        assertThat(client.credential().getKey()).isEqualTo(TestConfigurations.SECONDARY_MASTER_KEY);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "crudArgProvider", retryAnalyzer = RetryAnalyzer.class)
    public void deleteDocumentWithSecondaryKey(String documentId) throws InterruptedException {

        // sanity check
        assertThat(client.credential().getKey()).isEqualTo(TestConfigurations.MASTER_KEY);

        credential.update(TestConfigurations.SECONDARY_MASTER_KEY);

        InternalObjectNode docDefinition = getDocumentDefinition(documentId);

        container.createItem(docDefinition, new CosmosItemRequestOptions()).block();

        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        ModelBridgeInternal.setPartitionKey(options, new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(docDefinition, "mypk")));
        Mono<CosmosItemResponse<Object>> deleteObservable = container.deleteItem(docDefinition.getId(),
                                                                              new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(docDefinition, "mypk")), options);

        CosmosItemResponseValidator validator =
            new CosmosItemResponseValidator.Builder<CosmosItemResponse<InternalObjectNode>>()
                .nullResource()
                .build();
        validateItemSuccess(deleteObservable, validator);

        // attempt to read document which was deleted
        waitIfNeededForReplicasToCatchUp(getClientBuilder());

        Mono<CosmosItemResponse<InternalObjectNode>> readObservable = container.readItem(documentId,
                                                                          new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(docDefinition, "mypk")),
                                                                          options, InternalObjectNode.class);
        FailureValidator notFoundValidator = new FailureValidator.Builder().resourceNotFound().build();
        validateItemFailure(readObservable, notFoundValidator);

        //  sanity check
        assertThat(client.credential().getKey()).isEqualTo(TestConfigurations.SECONDARY_MASTER_KEY);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, retryAnalyzer = RetryAnalyzer.class)
    public void createDatabaseWithSecondaryKey() throws Exception {
        // sanity check
        assertThat(client.credential().getKey()).isEqualTo(TestConfigurations.MASTER_KEY);

        credential.update(TestConfigurations.SECONDARY_MASTER_KEY);

        CosmosDatabaseProperties databaseDefinition = new CosmosDatabaseProperties(CosmosDatabaseForTest.generateId());
        databases.add(databaseDefinition.getId());
        // create the getDatabase
        Mono<CosmosDatabaseResponse> createObservable = client.createDatabase(databaseDefinition, new CosmosDatabaseRequestOptions());

        // validate
        CosmosResponseValidator<CosmosDatabaseResponse> validator = new CosmosResponseValidator.Builder<CosmosDatabaseResponse>()
            .withId(databaseDefinition.getId()).build();
        validateSuccess(createObservable, validator);
        //  sanity check
        assertThat(client.credential().getKey()).isEqualTo(TestConfigurations.SECONDARY_MASTER_KEY);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, retryAnalyzer = RetryAnalyzer.class)
    public void readDatabaseWithSecondaryKey() throws Exception {
        // sanity check
        assertThat(client.credential().getKey()).isEqualTo(TestConfigurations.MASTER_KEY);

        credential.update(TestConfigurations.SECONDARY_MASTER_KEY);

        // read database
        Mono<CosmosDatabaseResponse> readObservable = client.getDatabase(databaseId).read();

        // validate
        CosmosResponseValidator<CosmosDatabaseResponse> validator = new CosmosResponseValidator.Builder<CosmosDatabaseResponse>()
            .withId(databaseId).build();
        validateSuccess(readObservable, validator);
        //  sanity check
        assertThat(client.credential().getKey()).isEqualTo(TestConfigurations.SECONDARY_MASTER_KEY);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, retryAnalyzer = RetryAnalyzer.class)
    public void deleteDatabaseWithSecondaryKey() throws Exception {
        // sanity check
        assertThat(client.credential().getKey()).isEqualTo(TestConfigurations.MASTER_KEY);

        credential.update(TestConfigurations.SECONDARY_MASTER_KEY);

        // create the database
        CosmosDatabaseProperties databaseDefinition = new CosmosDatabaseProperties(CosmosDatabaseForTest.generateId());
        databases.add(databaseDefinition.getId());

        client.createDatabase(databaseDefinition, new CosmosDatabaseRequestOptions()).block();
        CosmosAsyncDatabase database = client.getDatabase(databaseDefinition.getId());
        // delete the getDatabase
        Mono<CosmosDatabaseResponse> deleteObservable = database.delete();

        // validate
        CosmosResponseValidator<CosmosDatabaseResponse> validator = new CosmosResponseValidator.Builder<CosmosDatabaseResponse>()
            .nullResource().build();
        validateSuccess(deleteObservable, validator);
        //  sanity check
        assertThat(client.credential().getKey()).isEqualTo(TestConfigurations.SECONDARY_MASTER_KEY);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT,
        expectedExceptions = IllegalArgumentException.class,
        expectedExceptionsMessageRegExp = "Illegal base64 character .*")
    public void invalidSecondaryKey() throws Exception {
        // sanity check
        assertThat(client.credential().getKey()).isEqualTo(TestConfigurations.MASTER_KEY);

        credential.update("Invalid Secondary Key");

        // create the database, and this should throw Illegal Argument Exception for secondary key
        CosmosDatabaseProperties databaseDefinition = new CosmosDatabaseProperties(CosmosDatabaseForTest.generateId());
        client.createDatabase(databaseDefinition, new CosmosDatabaseRequestOptions()).block();
        client.getDatabase(databaseDefinition.getId());
    }

    @AfterMethod(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void afterMethod() {
        //  Set back master getKey before every test
        credential.update(TestConfigurations.MASTER_KEY);
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void before_AzureKeyCredentialTest() {
        client = getClientBuilder().buildAsyncClient();
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

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T extends CosmosResponse> void validateSuccess(Mono<T> single, CosmosResponseValidator<T> validator) {
        validateSuccess(single, validator, TIMEOUT);
    }
}
