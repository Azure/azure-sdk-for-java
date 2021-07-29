// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.encryption.implementation.ReflectionUtils;
import com.azure.cosmos.encryption.models.CosmosEncryptionAlgorithm;
import com.azure.cosmos.encryption.models.CosmosEncryptionType;
import com.azure.cosmos.encryption.models.SqlQuerySpecWithEncryption;
import com.azure.cosmos.models.ClientEncryptionIncludedPath;
import com.azure.cosmos.models.ClientEncryptionPolicy;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.EncryptionKeyWrapMetadata;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.rx.TestSuiteBase;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.microsoft.data.encryption.cryptography.EncryptionKeyStoreProvider;
import com.microsoft.data.encryption.cryptography.KeyEncryptionKeyAlgorithm;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class EncryptionAsyncApiCrudTest extends TestSuiteBase {
    private CosmosAsyncClient client;
    private CosmosAsyncDatabase cosmosAsyncDatabase;
    private static final int TIMEOUT = 6000_000;
    private CosmosEncryptionAsyncClient cosmosEncryptionAsyncClient;
    private CosmosEncryptionAsyncDatabase cosmosEncryptionAsyncDatabase;
    private CosmosEncryptionAsyncContainer cosmosEncryptionAsyncContainer;
    private CosmosEncryptionAsyncContainer encryptionContainerWithIncompatiblePolicyVersion;
    private EncryptionKeyWrapMetadata metadata1;
    private EncryptionKeyWrapMetadata metadata2;

    @Factory(dataProvider = "clientBuilders")
    public EncryptionAsyncApiCrudTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"encryption"}, timeOut = SETUP_TIMEOUT)
    public void before_CosmosItemTest() {
        assertThat(this.client).isNull();
        this.client = getClientBuilder().buildAsyncClient();
        TestEncryptionKeyStoreProvider encryptionKeyStoreProvider = new TestEncryptionKeyStoreProvider();
        cosmosAsyncDatabase = getSharedCosmosDatabase(this.client);
        cosmosEncryptionAsyncClient = CosmosEncryptionAsyncClient.createCosmosEncryptionAsyncClient(this.client,
            encryptionKeyStoreProvider);
        cosmosEncryptionAsyncDatabase =
            cosmosEncryptionAsyncClient.getCosmosEncryptionAsyncDatabase(cosmosAsyncDatabase);

        metadata1 = new EncryptionKeyWrapMetadata(encryptionKeyStoreProvider.getProviderName(), "key1", "tempmetadata1");
        metadata2 = new EncryptionKeyWrapMetadata(encryptionKeyStoreProvider.getProviderName(), "key2", "tempmetadata2");
        cosmosEncryptionAsyncDatabase.createClientEncryptionKey("key1",
            CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256, metadata1).block();
        cosmosEncryptionAsyncDatabase.createClientEncryptionKey("key2",
            CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256, metadata2).block();

        ClientEncryptionPolicy clientEncryptionPolicy = new ClientEncryptionPolicy(getPaths());
        String containerId = UUID.randomUUID().toString();
        CosmosContainerProperties properties = new CosmosContainerProperties(containerId, "/mypk");
        properties.setClientEncryptionPolicy(clientEncryptionPolicy);
        cosmosEncryptionAsyncDatabase.getCosmosAsyncDatabase().createContainer(properties).block();
        cosmosEncryptionAsyncContainer = cosmosEncryptionAsyncDatabase.getCosmosEncryptionAsyncContainer(containerId);

        ClientEncryptionPolicy clientEncryptionWithPolicyFormatVersion2 = new ClientEncryptionPolicy(getPaths());
        ReflectionUtils.setPolicyFormatVersion(clientEncryptionWithPolicyFormatVersion2, 2);
        containerId = UUID.randomUUID().toString();
        properties = new CosmosContainerProperties(containerId, "/mypk");
        properties.setClientEncryptionPolicy(clientEncryptionWithPolicyFormatVersion2);
        cosmosEncryptionAsyncDatabase.getCosmosAsyncDatabase().createContainer(properties).block();
        encryptionContainerWithIncompatiblePolicyVersion = cosmosEncryptionAsyncDatabase.getCosmosEncryptionAsyncContainer(containerId);
    }

    @AfterClass(groups = {"encryption"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        assertThat(this.client).isNotNull();
        this.client.close();
    }

    @Test(groups = {"encryption"}, timeOut = TIMEOUT)
    public void createItemEncrypt_readItemDecrypt() {
        EncryptionPojo properties = getItem(UUID.randomUUID().toString());
        CosmosItemResponse<EncryptionPojo> itemResponse = cosmosEncryptionAsyncContainer.createItem(properties,
            new PartitionKey(properties.getMypk()), new CosmosItemRequestOptions()).block();
        assertThat(itemResponse.getRequestCharge()).isGreaterThan(0);
        EncryptionPojo responseItem = itemResponse.getItem();
        validateResponse(properties, responseItem);

        EncryptionPojo readItem = cosmosEncryptionAsyncContainer.readItem(properties.getId(), new PartitionKey(properties.getMypk()),
            new CosmosItemRequestOptions(), EncryptionPojo.class).block().getItem();
        validateResponse(properties, readItem);

        //Check for length support greater than 8000
        properties = getItem(UUID.randomUUID().toString());
        String longString = "";
        for (int i = 0; i < 10000; i++) {
            longString += "a";
        }
        properties.setSensitiveString(longString);
        itemResponse = cosmosEncryptionAsyncContainer.createItem(properties,
            new PartitionKey(properties.getMypk()), new CosmosItemRequestOptions()).block();
        assertThat(itemResponse.getRequestCharge()).isGreaterThan(0);
        responseItem = itemResponse.getItem();
        validateResponse(properties, responseItem);
    }

    @Test(groups = {"encryption"}, timeOut = TIMEOUT)
    public void createItemEncryptWithContentResponseOnWriteEnabledFalse() {
        CosmosItemRequestOptions requestOptions = new CosmosItemRequestOptions();
        requestOptions.setContentResponseOnWriteEnabled(false);
        EncryptionPojo properties = getItem(UUID.randomUUID().toString());
        CosmosItemResponse<EncryptionPojo> itemResponse = cosmosEncryptionAsyncContainer.createItem(properties,
            new PartitionKey(properties.getMypk()), requestOptions).block();
        assertThat(itemResponse.getRequestCharge()).isGreaterThan(0);
        assertThat(itemResponse.getItem()).isNull();
    }

    @Test(groups = {"encryption"}, timeOut = TIMEOUT)
    public void upsertItem_readItem() {
        EncryptionPojo properties = getItem(UUID.randomUUID().toString());
        CosmosItemResponse<EncryptionPojo> itemResponse = cosmosEncryptionAsyncContainer.upsertItem(properties,
            new PartitionKey(properties.getMypk()), new CosmosItemRequestOptions()).block();
        assertThat(itemResponse.getRequestCharge()).isGreaterThan(0);
        EncryptionPojo responseItem = itemResponse.getItem();
        validateResponse(properties, responseItem);

        EncryptionPojo readItem = cosmosEncryptionAsyncContainer.readItem(properties.getId(), new PartitionKey(properties.getMypk()),
            new CosmosItemRequestOptions(), EncryptionPojo.class).block().getItem();
        validateResponse(properties, readItem);
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

    @Ignore("Ignoring it temporarily because server always returning policyFormatVersion 0")
    @Test(groups = {"encryption"}, timeOut = TIMEOUT)
    public void incompatiblePolicyFormatVersion() {
        try {
            EncryptionPojo properties = getItem(UUID.randomUUID().toString());
            encryptionContainerWithIncompatiblePolicyVersion.createItem(properties,
                new PartitionKey(properties.getMypk()), new CosmosItemRequestOptions()).block();
            fail("encryptionContainerWithIncompatiblePolicyVersion crud operation should fail on client encryption " +
                "policy " +
                "fetch because of policy format version greater than 1");
        } catch (UnsupportedOperationException ex) {
            assertThat(ex.getMessage()).isEqualTo("This version of the Encryption library cannot be used with this " +
                "container. Please upgrade to the latest version of the same.");
        }
    }

    @Test(groups = {"encryption"}, timeOut = TIMEOUT)
    public void crudQueryStaleCache() {
        String databaseId = UUID.randomUUID().toString();
        try {
            createNewDatabaseWithClientEncryptionKey(databaseId);
            CosmosAsyncClient asyncClient = getClientBuilder().buildAsyncClient();
            EncryptionKeyStoreProvider encryptionKeyStoreProvider = new TestEncryptionKeyStoreProvider();
            CosmosEncryptionAsyncClient cosmosEncryptionAsyncClient = CosmosEncryptionAsyncClient.createCosmosEncryptionAsyncClient(asyncClient,
                encryptionKeyStoreProvider);
            CosmosEncryptionAsyncDatabase cosmosEncryptionAsyncDatabase =
                cosmosEncryptionAsyncClient.getCosmosEncryptionAsyncDatabase(asyncClient.getDatabase(databaseId));

            String containerId = UUID.randomUUID().toString();
            ClientEncryptionPolicy clientEncryptionPolicy = new ClientEncryptionPolicy(getPaths());
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
        } finally {
            try {
                //deleting the database created for this test
                this.client.getDatabase(databaseId).delete().block();
            } catch(Exception ex) {
                // do nothing as we are clearing database created for this test
            }
        }
    }

    static void validateResponse(EncryptionPojo originalItem, EncryptionPojo result) {
        assertThat(result.getId()).isEqualTo(originalItem.getId());
        assertThat(result.getNonSensitive()).isEqualTo(originalItem.getNonSensitive());
        assertThat(result.getSensitiveString()).isEqualTo(originalItem.getSensitiveString());
        assertThat(result.getSensitiveInt()).isEqualTo(originalItem.getSensitiveInt());
        assertThat(result.getSensitiveFloat()).isEqualTo(originalItem.getSensitiveFloat());
        assertThat(result.getSensitiveLong()).isEqualTo(originalItem.getSensitiveLong());
        assertThat(result.getSensitiveDouble()).isEqualTo(originalItem.getSensitiveDouble());
        assertThat(result.isSensitiveBoolean()).isEqualTo(originalItem.isSensitiveBoolean());
        assertThat(result.getSensitiveIntArray()).isEqualTo(originalItem.getSensitiveIntArray());
        assertThat(result.getSensitiveStringArray()).isEqualTo(originalItem.getSensitiveStringArray());
        assertThat(result.getSensitiveString3DArray()).isEqualTo(originalItem.getSensitiveString3DArray());

        assertThat(result.getSensitiveNestedPojo().getId()).isEqualTo(originalItem.getSensitiveNestedPojo().getId());
        assertThat(result.getSensitiveNestedPojo().getMypk()).isEqualTo(originalItem.getSensitiveNestedPojo().getMypk());
        assertThat(result.getSensitiveNestedPojo().getNonSensitive()).isEqualTo(originalItem.getSensitiveNestedPojo().getNonSensitive());
        assertThat(result.getSensitiveNestedPojo().getSensitiveString()).isEqualTo(originalItem.getSensitiveNestedPojo().getSensitiveString());
        assertThat(result.getSensitiveNestedPojo().getSensitiveInt()).isEqualTo(originalItem.getSensitiveNestedPojo().getSensitiveInt());
        assertThat(result.getSensitiveNestedPojo().getSensitiveFloat()).isEqualTo(originalItem.getSensitiveNestedPojo().getSensitiveFloat());
        assertThat(result.getSensitiveNestedPojo().getSensitiveLong()).isEqualTo(originalItem.getSensitiveNestedPojo().getSensitiveLong());
        assertThat(result.getSensitiveNestedPojo().getSensitiveDouble()).isEqualTo(originalItem.getSensitiveNestedPojo().getSensitiveDouble());
        assertThat(result.getSensitiveNestedPojo().isSensitiveBoolean()).isEqualTo(originalItem.getSensitiveNestedPojo().isSensitiveBoolean());
        assertThat(result.getSensitiveNestedPojo().getSensitiveIntArray()).isEqualTo(originalItem.getSensitiveNestedPojo().getSensitiveIntArray());
        assertThat(result.getSensitiveNestedPojo().getSensitiveStringArray()).isEqualTo(originalItem.getSensitiveNestedPojo().getSensitiveStringArray());
        assertThat(result.getSensitiveNestedPojo().getSensitiveString3DArray()).isEqualTo(originalItem.getSensitiveNestedPojo().getSensitiveString3DArray());

        assertThat(result.getSensitiveChildPojoList().size()).isEqualTo(originalItem.getSensitiveChildPojoList().size());
        assertThat(result.getSensitiveChildPojoList().get(0).getSensitiveString()).isEqualTo(originalItem.getSensitiveChildPojoList().get(0).getSensitiveString());
        assertThat(result.getSensitiveChildPojoList().get(0).getSensitiveInt()).isEqualTo(originalItem.getSensitiveChildPojoList().get(0).getSensitiveInt());
        assertThat(result.getSensitiveChildPojoList().get(0).getSensitiveFloat()).isEqualTo(originalItem.getSensitiveChildPojoList().get(0).getSensitiveFloat());
        assertThat(result.getSensitiveChildPojoList().get(0).getSensitiveLong()).isEqualTo(originalItem.getSensitiveChildPojoList().get(0).getSensitiveLong());
        assertThat(result.getSensitiveChildPojoList().get(0).getSensitiveDouble()).isEqualTo(originalItem.getSensitiveChildPojoList().get(0).getSensitiveDouble());
        assertThat(result.getSensitiveChildPojoList().get(0).getSensitiveIntArray()).isEqualTo(originalItem.getSensitiveChildPojoList().get(0).getSensitiveIntArray());
        assertThat(result.getSensitiveChildPojoList().get(0).getSensitiveStringArray()).isEqualTo(originalItem.getSensitiveChildPojoList().get(0).getSensitiveStringArray());
        assertThat(result.getSensitiveChildPojoList().get(0).getSensitiveString3DArray()).isEqualTo(originalItem.getSensitiveChildPojoList().get(0).getSensitiveString3DArray());

        assertThat(result.getSensitiveChildPojo2DArray().length).isEqualTo(originalItem.getSensitiveChildPojo2DArray().length);
        assertThat(result.getSensitiveChildPojo2DArray()[0][0].getSensitiveString()).isEqualTo(originalItem.getSensitiveChildPojo2DArray()[0][0].getSensitiveString());
        assertThat(result.getSensitiveChildPojo2DArray()[0][0].getSensitiveInt()).isEqualTo(originalItem.getSensitiveChildPojo2DArray()[0][0].getSensitiveInt());
        assertThat(result.getSensitiveChildPojo2DArray()[0][0].getSensitiveFloat()).isEqualTo(originalItem.getSensitiveChildPojo2DArray()[0][0].getSensitiveFloat());
        assertThat(result.getSensitiveChildPojo2DArray()[0][0].getSensitiveLong()).isEqualTo(originalItem.getSensitiveChildPojo2DArray()[0][0].getSensitiveLong());
        assertThat(result.getSensitiveChildPojo2DArray()[0][0].getSensitiveDouble()).isEqualTo(originalItem.getSensitiveChildPojo2DArray()[0][0].getSensitiveDouble());
        assertThat(result.getSensitiveChildPojo2DArray()[0][0].getSensitiveIntArray()).isEqualTo(originalItem.getSensitiveChildPojo2DArray()[0][0].getSensitiveIntArray());
        assertThat(result.getSensitiveChildPojo2DArray()[0][0].getSensitiveStringArray()).isEqualTo(originalItem.getSensitiveChildPojo2DArray()[0][0].getSensitiveStringArray());
        assertThat(result.getSensitiveChildPojo2DArray()[0][0].getSensitiveString3DArray()).isEqualTo(originalItem.getSensitiveChildPojo2DArray()[0][0].getSensitiveString3DArray());
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

    public static EncryptionPojo getItem(String documentId) {
        EncryptionPojo pojo = new EncryptionPojo();
        pojo.setId(documentId);
        pojo.setMypk(documentId);
        pojo.setNonSensitive(UUID.randomUUID().toString());
        pojo.setSensitiveString("testingString");
        pojo.setSensitiveDouble(10.123);
        pojo.setSensitiveFloat(20.0f);
        pojo.setSensitiveInt(30);
        pojo.setSensitiveLong(1234);
        pojo.setSensitiveBoolean(true);

        EncryptionPojo nestedPojo = new EncryptionPojo();
        nestedPojo.setId("nestedPojo");
        nestedPojo.setMypk("nestedPojo");
        nestedPojo.setSensitiveString("nestedPojo");
        nestedPojo.setSensitiveDouble(10.123);
        nestedPojo.setSensitiveInt(123);
        nestedPojo.setSensitiveLong(1234);
        nestedPojo.setSensitiveStringArray(new String[]{"str1", "str1"});
        nestedPojo.setSensitiveString3DArray(new String[][][]{{{"str1", "str2"}, {"str3", "str4"}}, {{"str5", "str6"}, {
            "str7", "str8"}}});
        nestedPojo.setSensitiveBoolean(true);

        pojo.setSensitiveNestedPojo(nestedPojo);

        pojo.setSensitiveIntArray(new int[]{1, 2});
        pojo.setSensitiveStringArray(new String[]{"str1", "str1"});
        pojo.setSensitiveString3DArray(new String[][][]{{{"str1", "str2"}, {"str3", "str4"}}, {{"str5", "str6"}, {
            "str7", "str8"}}});

        EncryptionPojo childPojo1 = new EncryptionPojo();
        childPojo1.setId("childPojo1");
        childPojo1.setSensitiveString("child1TestingString");
        childPojo1.setSensitiveDouble(10.123);
        childPojo1.setSensitiveInt(123);
        childPojo1.setSensitiveLong(1234);
        childPojo1.setSensitiveBoolean(true);
        childPojo1.setSensitiveStringArray(new String[]{"str1", "str1"});
        childPojo1.setSensitiveString3DArray(new String[][][]{{{"str1", "str2"}, {"str3", "str4"}}, {{"str5", "str6"}, {
            "str7", "str8"}}});
        EncryptionPojo childPojo2 = new EncryptionPojo();
        childPojo2.setId("childPojo2");
        childPojo2.setSensitiveString("child2TestingString");
        childPojo2.setSensitiveDouble(10.123);
        childPojo2.setSensitiveInt(123);
        childPojo2.setSensitiveLong(1234);
        childPojo2.setSensitiveBoolean(true);

        pojo.setSensitiveChildPojoList(new ArrayList<>());
        pojo.getSensitiveChildPojoList().add(childPojo1);
        pojo.getSensitiveChildPojoList().add(childPojo2);

        pojo.setSensitiveChildPojo2DArray(new EncryptionPojo[][]{{childPojo1, childPojo2}, {childPojo1, childPojo2}});

        return pojo;
    }

    public static class TestEncryptionKeyStoreProvider extends EncryptionKeyStoreProvider {
        Map<String, Integer> keyInfo = new HashMap<>();
        String providerName = "TEST_KEY_STORE_PROVIDER";

        @Override
        public String getProviderName() {
            return providerName;
        }

        public TestEncryptionKeyStoreProvider() {
            keyInfo.put("tempmetadata1", 1);
            keyInfo.put("tempmetadata2", 2);
        }

        @Override
        public byte[] unwrapKey(String s, KeyEncryptionKeyAlgorithm keyEncryptionKeyAlgorithm, byte[] encryptedBytes) {
            int moveBy = this.keyInfo.get(s);
            byte[] plainkey = new byte[encryptedBytes.length];
            for (int i = 0; i < encryptedBytes.length; i++) {
                plainkey[i] = (byte) (encryptedBytes[i] - moveBy);
            }
            return plainkey;
        }

        @Override
        public byte[] wrapKey(String s, KeyEncryptionKeyAlgorithm keyEncryptionKeyAlgorithm, byte[] key) {
            int moveBy = this.keyInfo.get(s);
            byte[] encryptedBytes = new byte[key.length];
            for (int i = 0; i < key.length; i++) {
                encryptedBytes[i] = (byte) (key[i] + moveBy);
            }
            return encryptedBytes;
        }

        @Override
        public byte[] sign(String s, boolean b) {
            return new byte[0];
        }

        @Override
        public boolean verify(String s, boolean b, byte[] bytes) {
            return true;
        }
    }

    public static List<ClientEncryptionIncludedPath> getPaths() {
        ClientEncryptionIncludedPath includedPath1 = new ClientEncryptionIncludedPath();
        includedPath1.setClientEncryptionKeyId("key1");
        includedPath1.setPath("/sensitiveString");
        includedPath1.setEncryptionType(CosmosEncryptionType.DETERMINISTIC);
        includedPath1.setEncryptionAlgorithm(CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256);

        ClientEncryptionIncludedPath includedPath2 = new ClientEncryptionIncludedPath();
        includedPath2.setClientEncryptionKeyId("key2");
        includedPath2.setPath("/nonValidPath");
        includedPath2.setEncryptionType(CosmosEncryptionType.DETERMINISTIC);
        includedPath2.setEncryptionAlgorithm(CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256);

        ClientEncryptionIncludedPath includedPath3 = new ClientEncryptionIncludedPath();
        includedPath3.setClientEncryptionKeyId("key1");
        includedPath3.setPath("/sensitiveInt");
        includedPath3.setEncryptionType(CosmosEncryptionType.DETERMINISTIC);
        includedPath3.setEncryptionAlgorithm(CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256);

        ClientEncryptionIncludedPath includedPath4 = new ClientEncryptionIncludedPath();
        includedPath4.setClientEncryptionKeyId("key2");
        includedPath4.setPath("/sensitiveFloat");
        includedPath4.setEncryptionType(CosmosEncryptionType.DETERMINISTIC);
        includedPath4.setEncryptionAlgorithm(CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256);

        ClientEncryptionIncludedPath includedPath5 = new ClientEncryptionIncludedPath();
        includedPath5.setClientEncryptionKeyId("key1");
        includedPath5.setPath("/sensitiveLong");
        includedPath5.setEncryptionType(CosmosEncryptionType.DETERMINISTIC);
        includedPath5.setEncryptionAlgorithm(CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256);

        ClientEncryptionIncludedPath includedPath6 = new ClientEncryptionIncludedPath();
        includedPath6.setClientEncryptionKeyId("key2");
        includedPath6.setPath("/sensitiveDouble");
        includedPath6.setEncryptionType(CosmosEncryptionType.RANDOMIZED);
        includedPath6.setEncryptionAlgorithm(CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256);

        ClientEncryptionIncludedPath includedPath7 = new ClientEncryptionIncludedPath();
        includedPath7.setClientEncryptionKeyId("key1");
        includedPath7.setPath("/sensitiveBoolean");
        includedPath7.setEncryptionType(CosmosEncryptionType.DETERMINISTIC);
        includedPath7.setEncryptionAlgorithm(CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256);

        ClientEncryptionIncludedPath includedPath8 = new ClientEncryptionIncludedPath();
        includedPath8.setClientEncryptionKeyId("key1");
        includedPath8.setPath("/sensitiveNestedPojo");
        includedPath8.setEncryptionType(CosmosEncryptionType.DETERMINISTIC);
        includedPath8.setEncryptionAlgorithm(CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256);

        ClientEncryptionIncludedPath includedPath9 = new ClientEncryptionIncludedPath();
        includedPath9.setClientEncryptionKeyId("key1");
        includedPath9.setPath("/sensitiveIntArray");
        includedPath9.setEncryptionType(CosmosEncryptionType.DETERMINISTIC);
        includedPath9.setEncryptionAlgorithm(CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256);

        ClientEncryptionIncludedPath includedPath10 = new ClientEncryptionIncludedPath();
        includedPath10.setClientEncryptionKeyId("key2");
        includedPath10.setPath("/sensitiveString3DArray");
        includedPath10.setEncryptionType(CosmosEncryptionType.DETERMINISTIC);
        includedPath10.setEncryptionAlgorithm(CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256);

        ClientEncryptionIncludedPath includedPath11 = new ClientEncryptionIncludedPath();
        includedPath11.setClientEncryptionKeyId("key1");
        includedPath11.setPath("/sensitiveStringArray");
        includedPath11.setEncryptionType(CosmosEncryptionType.DETERMINISTIC);
        includedPath11.setEncryptionAlgorithm(CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256);

        ClientEncryptionIncludedPath includedPath12 = new ClientEncryptionIncludedPath();
        includedPath12.setClientEncryptionKeyId("key1");
        includedPath12.setPath("/sensitiveChildPojoList");
        includedPath12.setEncryptionType(CosmosEncryptionType.DETERMINISTIC);
        includedPath12.setEncryptionAlgorithm(CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256);

        ClientEncryptionIncludedPath includedPath13 = new ClientEncryptionIncludedPath();
        includedPath13.setClientEncryptionKeyId("key1");
        includedPath13.setPath("/sensitiveChildPojo2DArray");
        includedPath13.setEncryptionType(CosmosEncryptionType.DETERMINISTIC);
        includedPath13.setEncryptionAlgorithm(CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256);

        List<ClientEncryptionIncludedPath> paths = new ArrayList<>();
        paths.add(includedPath1);
        paths.add(includedPath2);
        paths.add(includedPath3);
        paths.add(includedPath4);
        paths.add(includedPath5);
        paths.add(includedPath6);
        paths.add(includedPath7);
        paths.add(includedPath8);
        paths.add(includedPath9);
        paths.add(includedPath10);
        paths.add(includedPath11);
        paths.add(includedPath12);
        paths.add(includedPath13);

        return paths;
    }

    public static List<ClientEncryptionIncludedPath> getPathWithOneEncryptionField() {
        ClientEncryptionIncludedPath includedPath = new ClientEncryptionIncludedPath();
        includedPath.setClientEncryptionKeyId("key1");
        includedPath.setPath("/sensitiveString");
        includedPath.setEncryptionType(CosmosEncryptionType.DETERMINISTIC);
        includedPath.setEncryptionAlgorithm(CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256);

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
        cosmosEncryptionAsyncClient.getCosmosAsyncClient().createDatabase(databaseId).block();
        CosmosEncryptionAsyncDatabase encryptionAsyncDatabase = cosmosEncryptionAsyncClient.getCosmosEncryptionAsyncDatabase(databaseId);
        encryptionAsyncDatabase.createClientEncryptionKey("key1",
            CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256, metadata1).block();
        encryptionAsyncDatabase.createClientEncryptionKey("key2",
            CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256, metadata2).block();
    }

    private CosmosEncryptionAsyncContainer getNewEncryptionContainerProxyObject(String databaseId, String containerId) {
        CosmosAsyncClient client = getClientBuilder().buildAsyncClient();
        EncryptionKeyStoreProvider encryptionKeyStoreProvider = new TestEncryptionKeyStoreProvider();
        CosmosEncryptionAsyncClient cosmosEncryptionAsyncClient = CosmosEncryptionAsyncClient.createCosmosEncryptionAsyncClient(client,
            encryptionKeyStoreProvider);
        CosmosEncryptionAsyncDatabase cosmosEncryptionAsyncDatabase =
            cosmosEncryptionAsyncClient.getCosmosEncryptionAsyncDatabase(client.getDatabase(databaseId));
        CosmosEncryptionAsyncContainer cosmosEncryptionAsyncContainer = cosmosEncryptionAsyncDatabase.getCosmosEncryptionAsyncContainer(containerId);
        return cosmosEncryptionAsyncContainer;
    }
}
