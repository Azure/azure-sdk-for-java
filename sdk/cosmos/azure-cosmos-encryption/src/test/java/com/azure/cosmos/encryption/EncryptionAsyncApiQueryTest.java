// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import com.azure.core.cryptography.KeyEncryptionKeyResolver;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.encryption.models.CosmosEncryptionAlgorithm;
import com.azure.cosmos.encryption.models.CosmosEncryptionType;
import com.azure.cosmos.encryption.models.SqlQuerySpecWithEncryption;
import com.azure.cosmos.models.ClientEncryptionIncludedPath;
import com.azure.cosmos.models.ClientEncryptionPolicy;
import com.azure.cosmos.models.CosmosBatch;
import com.azure.cosmos.models.CosmosBatchResponse;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosPatchItemRequestOptions;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.EncryptionKeyWrapMetadata;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.util.CosmosPagedFlux;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class EncryptionAsyncApiQueryTest extends TestSuiteBase {
    private CosmosAsyncClient client;
    private CosmosEncryptionAsyncClient cosmosEncryptionAsyncClient;

    CosmosEncryptionAsyncContainer cosmosEncryptionAsyncContainer;
    CosmosEncryptionAsyncDatabase cosmosEncryptionAsyncDatabase;

    @Factory(dataProvider = "clientBuildersWithSessionConsistency")
    public EncryptionAsyncApiQueryTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"encryption"}, timeOut = SETUP_TIMEOUT)
    public void before_CosmosItemTest() {
        assertThat(this.client).isNull();
        this.client = getClientBuilder().buildAsyncClient();
        KeyEncryptionKeyResolver keyEncryptionKeyResolver = new TestKeyEncryptionKeyResolver();
        cosmosEncryptionAsyncClient = new CosmosEncryptionClientBuilder().cosmosAsyncClient(this.client).keyEncryptionKeyResolver(
            keyEncryptionKeyResolver).keyEncryptionKeyResolverName("TEST_KEY_RESOLVER").buildAsyncClient();
        cosmosEncryptionAsyncDatabase = getSharedEncryptionDatabase(cosmosEncryptionAsyncClient);
        cosmosEncryptionAsyncContainer = getSharedEncryptionContainer(cosmosEncryptionAsyncClient);

        ClientEncryptionPolicy clientEncryptionPolicy = new ClientEncryptionPolicy(getPaths(1), 1);
        String containerId = UUID.randomUUID().toString();
        CosmosContainerProperties properties = new CosmosContainerProperties(containerId, "/mypk");
        properties.setClientEncryptionPolicy(clientEncryptionPolicy);
        cosmosEncryptionAsyncDatabase.getCosmosAsyncDatabase().createContainer(properties).block();
    }

    @AfterClass(groups = {"encryption"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        assertThat(this.client).isNotNull();
        this.client.close();
    }

    @Test(groups = {"encryption"}, timeOut = TIMEOUT)
    public void queryItems() {
        EncryptionPojo properties = getItem(UUID.randomUUID().toString());
        CosmosItemResponse<EncryptionPojo> itemResponse = cosmosEncryptionAsyncContainer.createItem(properties,
            new PartitionKey(properties.getMypk()), new CosmosItemRequestOptions()).block();
        assertThat(itemResponse.getRequestCharge()).isGreaterThan(0);
        EncryptionPojo responseItem = itemResponse.getItem();
        validateResponse(properties, responseItem);

        String query = String.format("SELECT * from c where c.id = '%s'", properties.getId());
        CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();

        SqlQuerySpec querySpec = new SqlQuerySpec(query);
        CosmosPagedFlux<EncryptionPojo> feedResponseIterator =
            cosmosEncryptionAsyncContainer.queryItems(querySpec, cosmosQueryRequestOptions, EncryptionPojo.class);
        List<EncryptionPojo> feedResponse = feedResponseIterator.byPage().blockFirst().getResults();
        assertThat(feedResponse.size()).isGreaterThanOrEqualTo(1);
        for (EncryptionPojo pojo : feedResponse) {
            if (pojo.getId().equals(properties.getId())) {
                validateResponse(pojo, responseItem);
            }
        }
    }

    @Test(groups = {"encryption"}, timeOut = TIMEOUT)
    public void queryItemsAggregate() {
        long startTime = Instant.now().getEpochSecond();
        List<String> actualIds = new ArrayList<>();
        EncryptionPojo properties = getItem(UUID.randomUUID().toString());
        cosmosEncryptionAsyncContainer.createItem(properties, new PartitionKey(properties.getMypk()),
            new CosmosItemRequestOptions()).block();
        actualIds.add(properties.getId());
        properties = getItem(UUID.randomUUID().toString());
        cosmosEncryptionAsyncContainer.createItem(properties, new PartitionKey(properties.getMypk()),
            new CosmosItemRequestOptions()).block();
        actualIds.add(properties.getId());
        properties = getItem(UUID.randomUUID().toString());
        cosmosEncryptionAsyncContainer.createItem(properties, new PartitionKey(properties.getMypk()),
            new CosmosItemRequestOptions()).block();
        actualIds.add(properties.getId());

        // MAX query
        String query1 = String.format("Select value max(c._ts) from c");
        CosmosQueryRequestOptions cosmosQueryRequestOptions1 = new CosmosQueryRequestOptions();

        SqlQuerySpec querySpec1 = new SqlQuerySpec(query1);
        CosmosPagedFlux<Integer> feedResponseIterator1 =
            cosmosEncryptionAsyncContainer.queryItems(querySpec1, cosmosQueryRequestOptions1, Integer.class);
        List<Integer> feedResponse1 = feedResponseIterator1.byPage().blockFirst().getResults();
        int timeStamp = feedResponse1.get(0);
        long endTime = Instant.now().getEpochSecond();

        assertThat(timeStamp).isGreaterThanOrEqualTo((int)startTime);
        assertThat(timeStamp).isLessThanOrEqualTo((int)endTime);
        assertThat(feedResponse1.size()).isEqualTo(1);

        // COUNT query
        String query2 = String.format("Select top 1 value count(c) from c order by c._ts");
        CosmosQueryRequestOptions cosmosQueryRequestOptions2 = new CosmosQueryRequestOptions();

        SqlQuerySpec querySpec2 = new SqlQuerySpec(query2);
        CosmosPagedFlux<Integer> feedResponseIterator2 =
            cosmosEncryptionAsyncContainer.queryItems(querySpec2, cosmosQueryRequestOptions2, Integer.class);
        List<Integer> feedResponse2 = feedResponseIterator2.byPage().blockFirst().getResults();
        assertThat(feedResponse2.size()).isEqualTo(1);

        // MAX query for String class type
        String query3 = String.format("Select value max(c.sensitiveString) from c");
        CosmosQueryRequestOptions cosmosQueryRequestOptions3 = new CosmosQueryRequestOptions();

        SqlQuerySpec querySpec3 = new SqlQuerySpec(query3);
        CosmosPagedFlux<String> feedResponseIterator3 =
            cosmosEncryptionAsyncContainer.queryItems(querySpec3, cosmosQueryRequestOptions3, String.class);
        List<String> feedResponse3 = feedResponseIterator3.byPage().blockFirst().getResults();
        assertThat(feedResponse3.size()).isEqualTo(1);
    }

    @Test(groups = {"encryption"}, timeOut = TIMEOUT)
    public void queryItemsOnEncryptedProperties() {
        EncryptionPojo properties = getItem(UUID.randomUUID().toString());
        CosmosItemResponse<EncryptionPojo> itemResponse = cosmosEncryptionAsyncContainer.createItem(properties,
            new PartitionKey(properties.getMypk()), new CosmosItemRequestOptions()).block();
        assertThat(itemResponse.getRequestCharge()).isGreaterThan(0);
        EncryptionPojo responseItem = itemResponse.getItem();
        validateResponse(properties, responseItem);

        String query = String.format("SELECT * FROM c where c.sensitiveString = @sensitiveString and c.nonSensitive =" +
            " " +
            "@nonSensitive and c.sensitiveLong = @sensitiveLong");
        SqlQuerySpec querySpec = new SqlQuerySpec(query);
        SqlParameter parameter1 = new SqlParameter("@nonSensitive", properties.getNonSensitive());
        querySpec.getParameters().add(parameter1);

        SqlParameter parameter2 = new SqlParameter("@sensitiveString", properties.getSensitiveString());
        SqlParameter parameter3 = new SqlParameter("@sensitiveLong", properties.getSensitiveLong());
        SqlQuerySpecWithEncryption sqlQuerySpecWithEncryption = new SqlQuerySpecWithEncryption(querySpec);
        sqlQuerySpecWithEncryption.addEncryptionParameter("/sensitiveString", parameter2);
        sqlQuerySpecWithEncryption.addEncryptionParameter("/sensitiveLong", parameter3);

        CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();
        CosmosPagedFlux<EncryptionPojo> feedResponseIterator =
            cosmosEncryptionAsyncContainer.queryItemsOnEncryptedProperties(sqlQuerySpecWithEncryption,
                cosmosQueryRequestOptions, EncryptionPojo.class);
        List<EncryptionPojo> feedResponse = feedResponseIterator.byPage().blockFirst().getResults();
        assertThat(feedResponse.size()).isGreaterThanOrEqualTo(1);
        for (EncryptionPojo pojo : feedResponse) {
            if (pojo.getId().equals(properties.getId())) {
                validateResponse(pojo, responseItem);
            }
        }
    }

    @Test(groups = {"encryption"}, timeOut = TIMEOUT)
    public void queryItemsOnRandomizedEncryption() {
        EncryptionPojo properties = getItem(UUID.randomUUID().toString());
        CosmosItemResponse<EncryptionPojo> itemResponse = cosmosEncryptionAsyncContainer.createItem(properties,
            new PartitionKey(properties.getMypk()), new CosmosItemRequestOptions()).block();
        assertThat(itemResponse.getRequestCharge()).isGreaterThan(0);
        EncryptionPojo responseItem = itemResponse.getItem();
        validateResponse(properties, responseItem);

        String query = String.format("SELECT * FROM c where c.sensitiveString = @sensitiveString and c.nonSensitive =" +
            " " +
            "@nonSensitive and c.sensitiveDouble = @sensitiveDouble");
        SqlQuerySpec querySpec = new SqlQuerySpec(query);
        SqlParameter parameter1 = new SqlParameter("@nonSensitive", properties.getNonSensitive());
        querySpec.getParameters().add(parameter1);

        SqlParameter parameter2 = new SqlParameter("@sensitiveString", properties.getSensitiveString());
        SqlParameter parameter3 = new SqlParameter("@sensitiveDouble", properties.getSensitiveDouble());
        SqlQuerySpecWithEncryption sqlQuerySpecWithEncryption = new SqlQuerySpecWithEncryption(querySpec);
        sqlQuerySpecWithEncryption.addEncryptionParameter("/sensitiveString", parameter2);
        sqlQuerySpecWithEncryption.addEncryptionParameter("/sensitiveDouble", parameter3);

        CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();
        CosmosPagedFlux<EncryptionPojo> feedResponseIterator =
            cosmosEncryptionAsyncContainer.queryItemsOnEncryptedProperties(sqlQuerySpecWithEncryption,
                cosmosQueryRequestOptions, EncryptionPojo.class);
        try {
            List<EncryptionPojo> feedResponse = feedResponseIterator.byPage().blockFirst().getResults();
            fail("Query on randomized parameter should fail");
        } catch (IllegalArgumentException ex) {
            assertThat(ex.getMessage()).contains("Path /sensitiveDouble cannot be used in the " +
                "query because of randomized encryption");
        }
    }

    @Test(groups = {"encryption"}, timeOut = TIMEOUT)
    public void queryItemsWithContinuationTokenAndPageSize() {
        List<String> actualIds = new ArrayList<>();
        EncryptionPojo properties = getItem(UUID.randomUUID().toString());
        cosmosEncryptionAsyncContainer.createItem(properties, new PartitionKey(properties.getMypk()),
            new CosmosItemRequestOptions()).block();
        actualIds.add(properties.getId());
        properties = getItem(UUID.randomUUID().toString());
        cosmosEncryptionAsyncContainer.createItem(properties, new PartitionKey(properties.getMypk()),
            new CosmosItemRequestOptions()).block();
        actualIds.add(properties.getId());
        properties = getItem(UUID.randomUUID().toString());
        cosmosEncryptionAsyncContainer.createItem(properties, new PartitionKey(properties.getMypk()),
            new CosmosItemRequestOptions()).block();
        actualIds.add(properties.getId());

        String query = String.format("SELECT * from c where c.id in ('%s', '%s', '%s')", actualIds.get(0),
            actualIds.get(1), actualIds.get(2));
        CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();
        String continuationToken = null;
        int pageSize = 1;

        int initialDocumentCount = 3;
        int finalDocumentCount = 0;

        CosmosPagedFlux<EncryptionPojo> feedResponseIterator =
            cosmosEncryptionAsyncContainer.queryItems(query, cosmosQueryRequestOptions, EncryptionPojo.class);

        do {
            Iterable<FeedResponse<EncryptionPojo>> feedResponseIterable =
                feedResponseIterator.byPage(continuationToken, 1).toIterable();
            for (FeedResponse<EncryptionPojo> fr : feedResponseIterable) {
                int resultSize = fr.getResults().size();
                assertThat(resultSize).isEqualTo(pageSize);
                finalDocumentCount += fr.getResults().size();
                continuationToken = fr.getContinuationToken();
            }
        } while (continuationToken != null);

        assertThat(finalDocumentCount).isEqualTo(initialDocumentCount);
    }

    @Test(groups = {"encryption"}, timeOut = TIMEOUT)
    public void crudQueryStaleCache() {
        String databaseId = UUID.randomUUID().toString();
        try {
            createNewDatabaseWithClientEncryptionKey(databaseId);
            CosmosAsyncClient asyncClient = getClientBuilder().buildAsyncClient();
            KeyEncryptionKeyResolver keyEncryptionKeyResolver = new TestKeyEncryptionKeyResolver();
            CosmosEncryptionAsyncClient cosmosEncryptionAsyncClient = new CosmosEncryptionClientBuilder().cosmosAsyncClient(asyncClient).keyEncryptionKeyResolver(
                keyEncryptionKeyResolver).keyEncryptionKeyResolverName("TEST_KEY_RESOLVER").buildAsyncClient();
            CosmosEncryptionAsyncDatabase cosmosEncryptionAsyncDatabase =
                cosmosEncryptionAsyncClient.getCosmosEncryptionAsyncDatabase(asyncClient.getDatabase(databaseId));

            String containerId = UUID.randomUUID().toString();
            ClientEncryptionPolicy clientEncryptionPolicy = new ClientEncryptionPolicy(getPaths(1), 1);
            createEncryptionContainer(cosmosEncryptionAsyncDatabase, clientEncryptionPolicy, containerId);
            CosmosEncryptionAsyncContainer encryptionAsyncContainerOriginal =
                cosmosEncryptionAsyncDatabase.getCosmosEncryptionAsyncContainer(containerId);

            EncryptionPojo encryptionPojo = getItem(UUID.randomUUID().toString());
            CosmosItemResponse<EncryptionPojo> createResponse = encryptionAsyncContainerOriginal.createItem(encryptionPojo,
                new PartitionKey(encryptionPojo.getMypk()), new CosmosItemRequestOptions()).block();
            validateResponse(encryptionPojo, createResponse.getItem());

            String query = String.format("SELECT * from c where c.id = '%s'", encryptionPojo.getId());
            SqlQuerySpec querySpec = new SqlQuerySpec(query);
            CosmosPagedFlux<EncryptionPojo> feedResponseIterator =
                encryptionAsyncContainerOriginal.queryItems(querySpec, null, EncryptionPojo.class);
            List<EncryptionPojo> feedResponse = feedResponseIterator.byPage().blockFirst().getResults();

            EncryptionPojo readItem = encryptionAsyncContainerOriginal.readItem(encryptionPojo.getId(),
                new PartitionKey(encryptionPojo.getMypk()),
                new CosmosItemRequestOptions(), EncryptionPojo.class).block().getItem();
            validateResponse(encryptionPojo, readItem);

            //Deleting database and  creating database, container again
            cosmosEncryptionAsyncDatabase.getCosmosAsyncDatabase().delete().block();
            createNewDatabaseWithClientEncryptionKey(databaseId);
            createEncryptionContainer(cosmosEncryptionAsyncDatabase, clientEncryptionPolicy, containerId);

            //Validating create on original encryptionAsyncContainer
            createResponse = encryptionAsyncContainerOriginal.createItem(encryptionPojo,
                new PartitionKey(encryptionPojo.getMypk()), new CosmosItemRequestOptions()).block();
            validateResponse(encryptionPojo, createResponse.getItem());

            //Deleting and creating container
            encryptionAsyncContainerOriginal.getCosmosAsyncContainer().delete().block();
            ClientEncryptionPolicy policyWithOneEncryptionPolicy = new ClientEncryptionPolicy(getPathWithOneEncryptionField());
            createEncryptionContainer(cosmosEncryptionAsyncDatabase, policyWithOneEncryptionPolicy, containerId);
            CosmosEncryptionAsyncContainer encryptionAsyncContainerNew = getNewEncryptionContainerProxyObject(cosmosEncryptionAsyncDatabase.getCosmosAsyncDatabase().getId(), containerId);
            encryptionAsyncContainerNew.createItem(encryptionPojo,
                new PartitionKey(encryptionPojo.getMypk()), new CosmosItemRequestOptions()).block();
            EncryptionPojo pojoWithOneFieldEncrypted = encryptionAsyncContainerNew.getCosmosAsyncContainer().readItem(encryptionPojo.getId(), new PartitionKey(encryptionPojo.getMypk()),
                new CosmosItemRequestOptions(), EncryptionPojo.class).block().getItem();
            validateResponseWithOneFieldEncryption(encryptionPojo, pojoWithOneFieldEncrypted);

            //Validating read on original encryptionAsyncContainer
            readItem = encryptionAsyncContainerOriginal.readItem(encryptionPojo.getId(), new PartitionKey(encryptionPojo.getMypk()),
                new CosmosItemRequestOptions(), EncryptionPojo.class).block().getItem();
            validateResponse(encryptionPojo, readItem);

            //Deleting and creating container
            encryptionAsyncContainerOriginal.getCosmosAsyncContainer().delete().block();
            createEncryptionContainer(cosmosEncryptionAsyncDatabase, clientEncryptionPolicy, containerId);

            CosmosItemResponse<EncryptionPojo> upsertResponse = encryptionAsyncContainerOriginal.upsertItem(encryptionPojo,
                new PartitionKey(encryptionPojo.getMypk()), new CosmosItemRequestOptions()).block();
            assertThat(upsertResponse.getRequestCharge()).isGreaterThan(0);
            EncryptionPojo responseItem = upsertResponse.getItem();
            validateResponse(encryptionPojo, responseItem);

            //Deleting and creating container
            encryptionAsyncContainerOriginal.getCosmosAsyncContainer().delete().block();
            createEncryptionContainer(cosmosEncryptionAsyncDatabase, clientEncryptionPolicy, containerId);
            encryptionAsyncContainerNew = getNewEncryptionContainerProxyObject(cosmosEncryptionAsyncDatabase.getCosmosAsyncDatabase().getId(), containerId);
            encryptionAsyncContainerNew.createItem(encryptionPojo,
                new PartitionKey(encryptionPojo.getMypk()), new CosmosItemRequestOptions()).block();


            CosmosItemResponse<EncryptionPojo> replaceResponse =
                encryptionAsyncContainerOriginal.replaceItem(encryptionPojo, encryptionPojo.getId(),
                    new PartitionKey(encryptionPojo.getMypk()), new CosmosItemRequestOptions()).block();
            assertThat(upsertResponse.getRequestCharge()).isGreaterThan(0);
            responseItem = replaceResponse.getItem();
            validateResponse(encryptionPojo, responseItem);

            // First query fail on core sdk as there will be no pkrange cache, and collection cache have wrong information of collection rid,
            // pkrange call will fail will null pointer, therefore querying before deleting the container making sure we have pkrange cache to begin with
            encryptionAsyncContainerOriginal.queryItems(querySpec, null, EncryptionPojo.class).byPage().blockFirst().getResults();
            //Deleting and creating container
            encryptionAsyncContainerOriginal.getCosmosAsyncContainer().delete().block();
            createEncryptionContainer(cosmosEncryptionAsyncDatabase, clientEncryptionPolicy, containerId);
            CosmosEncryptionAsyncContainer newEncryptionAsyncContainer = getNewEncryptionContainerProxyObject(cosmosEncryptionAsyncDatabase.getCosmosAsyncDatabase().getId(), containerId);

            for (int i = 0; i < 10; i++) {
                EncryptionPojo pojo = getItem(UUID.randomUUID().toString());
                newEncryptionAsyncContainer.createItem(pojo,
                    new PartitionKey(pojo.getMypk()), new CosmosItemRequestOptions()).block();
            }

            feedResponseIterator =
                encryptionAsyncContainerOriginal.queryItems("Select * from C", null, EncryptionPojo.class);
            String continuationToken = null;
            int pageSize = 3;
            int finalDocumentCount = 0;
            do {
                Iterable<FeedResponse<EncryptionPojo>> feedResponseIterable =
                    feedResponseIterator.byPage(continuationToken, pageSize).toIterable();
                for (FeedResponse<EncryptionPojo> fr : feedResponseIterable) {
                    int resultSize = fr.getResults().size();
                    assertThat(resultSize).isLessThanOrEqualTo(pageSize);
                    finalDocumentCount += fr.getResults().size();
                    continuationToken = fr.getContinuationToken();
                }
            } while (continuationToken != null);

            assertThat(finalDocumentCount).isEqualTo(10);


            //Deleting and creating container
            encryptionAsyncContainerOriginal.getCosmosAsyncContainer().delete().block();
            createEncryptionContainer(cosmosEncryptionAsyncDatabase, clientEncryptionPolicy, containerId);
            newEncryptionAsyncContainer = getNewEncryptionContainerProxyObject(cosmosEncryptionAsyncDatabase.getCosmosAsyncDatabase().getId(), containerId);

            EncryptionPojo encryptionPojoForQueryItemsOnEncryptedProperties = getItem(UUID.randomUUID().toString());
            newEncryptionAsyncContainer.createItem(encryptionPojoForQueryItemsOnEncryptedProperties,
                new PartitionKey(encryptionPojoForQueryItemsOnEncryptedProperties.getMypk()), new CosmosItemRequestOptions()).block();

            query = String.format("SELECT * FROM c where c.sensitiveString = @sensitiveString and c.nonSensitive =" +
                " " +
                "@nonSensitive and c.sensitiveLong = @sensitiveLong");
            querySpec = new SqlQuerySpec(query);
            SqlParameter parameter1 = new SqlParameter("@nonSensitive", encryptionPojoForQueryItemsOnEncryptedProperties.getNonSensitive());
            querySpec.getParameters().add(parameter1);

            SqlParameter parameter2 = new SqlParameter("@sensitiveString", encryptionPojoForQueryItemsOnEncryptedProperties.getSensitiveString());
            SqlParameter parameter3 = new SqlParameter("@sensitiveLong", encryptionPojoForQueryItemsOnEncryptedProperties.getSensitiveLong());
            SqlQuerySpecWithEncryption sqlQuerySpecWithEncryption = new SqlQuerySpecWithEncryption(querySpec);
            sqlQuerySpecWithEncryption.addEncryptionParameter("/sensitiveString", parameter2);
            sqlQuerySpecWithEncryption.addEncryptionParameter("/sensitiveLong", parameter3);

            feedResponseIterator =
                encryptionAsyncContainerOriginal.queryItemsOnEncryptedProperties(sqlQuerySpecWithEncryption,
                    null, EncryptionPojo.class);
            feedResponse = feedResponseIterator.byPage().blockFirst().getResults();
            assertThat(feedResponse.size()).isGreaterThanOrEqualTo(1);
            for (EncryptionPojo pojo : feedResponse) {
                if (pojo.getId().equals(encryptionPojoForQueryItemsOnEncryptedProperties.getId())) {
                    validateResponse(encryptionPojoForQueryItemsOnEncryptedProperties, pojo);
                }
            }

            //Deleting and creating container
            encryptionAsyncContainerOriginal.getCosmosAsyncContainer().delete().block();
            createEncryptionContainer(cosmosEncryptionAsyncDatabase, clientEncryptionPolicy, containerId);

            String itemId= UUID.randomUUID().toString();
            EncryptionPojo createPojo = getItem(itemId);
            CosmosBatch cosmosEncryptionBatch = CosmosBatch.createCosmosBatch(new PartitionKey(itemId));
            cosmosEncryptionBatch.createItemOperation(createPojo);
            cosmosEncryptionBatch.readItemOperation(itemId);

            CosmosBatchResponse batchResponse = encryptionAsyncContainerOriginal.executeCosmosBatch(cosmosEncryptionBatch).block();
            assertThat(batchResponse.getResults().size()).isEqualTo(2);
            validateResponse(createPojo, batchResponse.getResults().get(0).getItem(EncryptionPojo.class));
            validateResponse(createPojo, batchResponse.getResults().get(1).getItem(EncryptionPojo.class));

            //Deleting and creating container
            encryptionAsyncContainerOriginal.getCosmosAsyncContainer().delete().block();
            createEncryptionContainer(cosmosEncryptionAsyncDatabase, clientEncryptionPolicy, containerId);

            itemId= UUID.randomUUID().toString();
            createPojo = getItem(itemId);
            CosmosItemResponse<EncryptionPojo> itemResponse = encryptionAsyncContainerOriginal.createItem(createPojo,
                new PartitionKey(createPojo.getMypk()), new CosmosItemRequestOptions()).block();

            int originalSensitiveInt = createPojo.getSensitiveInt();
            int newSensitiveInt = originalSensitiveInt + 1;

            CosmosPatchOperations cosmosPatchOperations = CosmosPatchOperations.create();
            cosmosPatchOperations.add("/sensitiveString", "patched");
            cosmosPatchOperations.remove("/sensitiveDouble");
            cosmosPatchOperations.replace("/sensitiveInt", newSensitiveInt);

            CosmosItemResponse<EncryptionPojo> patchResponse = encryptionAsyncContainerOriginal.patchItem(
                createPojo.getId(),
                new PartitionKey(createPojo.getMypk()),
                cosmosPatchOperations,
                new CosmosPatchItemRequestOptions(),
                EncryptionPojo.class).block();

            CosmosItemResponse<EncryptionPojo> readResponse = encryptionAsyncContainerOriginal.readItem(
                createPojo.getId(),
                new PartitionKey(createPojo.getMypk()),
                new CosmosPatchItemRequestOptions(),
                EncryptionPojo.class).block();

            validateResponse(patchResponse.getItem(), readResponse.getItem());

        } finally {
            try {
                //deleting the database created for this test
                this.client.getDatabase(databaseId).delete().block();
            } catch(Exception ex) {
                // do nothing as we are clearing database created for this test
            }
        }
    }

    static void validateResponseWithOneFieldEncryption(EncryptionPojo originalItem, EncryptionPojo result) {
        assertThat(result.getId()).isEqualTo(originalItem.getId());
        assertThat(result.getNonSensitive()).isEqualTo(originalItem.getNonSensitive());
        assertThat(result.getSensitiveString()).isNotEqualTo(originalItem.getSensitiveString());
        assertThat(result.getSensitiveInt()).isEqualTo(originalItem.getSensitiveInt());
        assertThat(result.getSensitiveFloat()).isEqualTo(originalItem.getSensitiveFloat());
        assertThat(result.getSensitiveLong()).isEqualTo(originalItem.getSensitiveLong());
        assertThat(result.getSensitiveDouble()).isEqualTo(originalItem.getSensitiveDouble());
        assertThat(result.isSensitiveBoolean()).isEqualTo(originalItem.isSensitiveBoolean());
        assertThat(result.getSensitiveIntArray()).isEqualTo(originalItem.getSensitiveIntArray());
        assertThat(result.getSensitiveStringArray()).isEqualTo(originalItem.getSensitiveStringArray());
        assertThat(result.getSensitiveString3DArray()).isEqualTo(originalItem.getSensitiveString3DArray());
    }

    public static List<ClientEncryptionIncludedPath> getPathWithOneEncryptionField() {
        ClientEncryptionIncludedPath includedPath = new ClientEncryptionIncludedPath();
        includedPath.setClientEncryptionKeyId("key1");
        includedPath.setPath("/sensitiveString");
        includedPath.setEncryptionType(CosmosEncryptionType.DETERMINISTIC.toString());
        includedPath.setEncryptionAlgorithm(CosmosEncryptionAlgorithm.AEAD_AES_256_CBC_HMAC_SHA256.getName());

        List<ClientEncryptionIncludedPath> paths = new ArrayList<>();
        paths.add(includedPath);
        return paths;
    }

    private void createEncryptionContainer(CosmosEncryptionAsyncDatabase cosmosEncryptionAsyncDatabase,
                                           ClientEncryptionPolicy clientEncryptionPolicy,
                                           String containerId) {
        CosmosContainerProperties properties = new CosmosContainerProperties(containerId, "/mypk");
        properties.setClientEncryptionPolicy(clientEncryptionPolicy);
        cosmosEncryptionAsyncDatabase.getCosmosAsyncDatabase().createContainer(properties).block();
    }

    private void createNewDatabaseWithClientEncryptionKey(String databaseId){
        EncryptionKeyWrapMetadata metadata1 = new EncryptionKeyWrapMetadata("TEST_KEY_RESOLVER", "key1", "tempmetadata1", "RSA-OAEP");
        EncryptionKeyWrapMetadata metadata2 = new EncryptionKeyWrapMetadata("TEST_KEY_RESOLVER", "key2", "tempmetadata2", "RSA-OAEP");
        cosmosEncryptionAsyncClient.getCosmosAsyncClient().createDatabase(databaseId).block();
        CosmosEncryptionAsyncDatabase encryptionAsyncDatabase = cosmosEncryptionAsyncClient.getCosmosEncryptionAsyncDatabase(databaseId);
        encryptionAsyncDatabase.createClientEncryptionKey("key1",
            CosmosEncryptionAlgorithm.AEAD_AES_256_CBC_HMAC_SHA256.getName(), metadata1).block();
        encryptionAsyncDatabase.createClientEncryptionKey("key2",
            CosmosEncryptionAlgorithm.AEAD_AES_256_CBC_HMAC_SHA256.getName(), metadata2).block();
    }

    private CosmosEncryptionAsyncContainer getNewEncryptionContainerProxyObject(String databaseId, String containerId) {
        CosmosAsyncClient client = getClientBuilder().buildAsyncClient();
        KeyEncryptionKeyResolver keyEncryptionKeyResolver = new TestKeyEncryptionKeyResolver();
        CosmosEncryptionAsyncClient cosmosEncryptionAsyncClient = new CosmosEncryptionClientBuilder().cosmosAsyncClient(client).keyEncryptionKeyResolver(
            keyEncryptionKeyResolver).keyEncryptionKeyResolverName("TEST_KEY_RESOLVER").buildAsyncClient();
        CosmosEncryptionAsyncDatabase cosmosEncryptionAsyncDatabase =
            cosmosEncryptionAsyncClient.getCosmosEncryptionAsyncDatabase(client.getDatabase(databaseId));
        CosmosEncryptionAsyncContainer cosmosEncryptionAsyncContainer = cosmosEncryptionAsyncDatabase.getCosmosEncryptionAsyncContainer(containerId);
        return cosmosEncryptionAsyncContainer;
    }


}
