// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import com.azure.cosmos.CosmosAsyncClient;
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
import com.azure.cosmos.util.CosmosPagedFlux;
import com.microsoft.data.encryption.cryptography.EncryptionKeyStoreProvider;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class EncryptionAsyncApiCrudTest extends TestSuiteBase {
    private CosmosAsyncClient client;
    private CosmosEncryptionAsyncClient cosmosEncryptionAsyncClient;
    private CosmosEncryptionAsyncContainer encryptionContainerWithIncompatiblePolicyVersion;

    CosmosEncryptionAsyncContainer cosmosEncryptionAsyncContainer;
    CosmosEncryptionAsyncDatabase cosmosEncryptionAsyncDatabase;

    @Factory(dataProvider = "clientBuilders")
    public EncryptionAsyncApiCrudTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"encryption"}, timeOut = SETUP_TIMEOUT)
    public void before_CosmosItemTest() {
        assertThat(this.client).isNull();
        this.client = getClientBuilder().buildAsyncClient();
        TestEncryptionKeyStoreProvider encryptionKeyStoreProvider = new TestEncryptionKeyStoreProvider();
        cosmosEncryptionAsyncClient = CosmosEncryptionAsyncClient.createCosmosEncryptionAsyncClient(this.client,
            encryptionKeyStoreProvider);
        cosmosEncryptionAsyncDatabase = getSharedEncryptionDatabase(cosmosEncryptionAsyncClient);
        cosmosEncryptionAsyncContainer = getSharedEncryptionContainer(cosmosEncryptionAsyncClient);

        ClientEncryptionPolicy clientEncryptionWithPolicyFormatVersion2 = new ClientEncryptionPolicy(getPaths());
        ReflectionUtils.setPolicyFormatVersion(clientEncryptionWithPolicyFormatVersion2, 2);
        String containerId = UUID.randomUUID().toString();
        CosmosContainerProperties properties = new CosmosContainerProperties(containerId, "/mypk");
        properties.setClientEncryptionPolicy(clientEncryptionWithPolicyFormatVersion2);
        cosmosEncryptionAsyncDatabase.getCosmosAsyncDatabase().createContainer(properties).block();
        encryptionContainerWithIncompatiblePolicyVersion =
            cosmosEncryptionAsyncDatabase.getCosmosEncryptionAsyncContainer(containerId);
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

    @Test(groups = {"encryption"}, timeOut = TIMEOUT)
    public void invalidDataEncryptionKeyAlgorithm() {
        try {
            TestEncryptionKeyStoreProvider testEncryptionKeyStoreProvider = new TestEncryptionKeyStoreProvider();
            EncryptionKeyWrapMetadata metadata =
                new EncryptionKeyWrapMetadata(testEncryptionKeyStoreProvider.getProviderName(), "key1",
                    "tempmetadata1");
            this.cosmosEncryptionAsyncDatabase.createClientEncryptionKey("key1",
                "InvalidAlgorithm", metadata).block();
            fail("client encryption key create should fail on invalid algorithm");
        } catch (IllegalArgumentException ex) {
            assertThat(ex.getMessage()).isEqualTo("Invalid Encryption Algorithm 'InvalidAlgorithm'");
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
        includedPath.setEncryptionType(CosmosEncryptionType.DETERMINISTIC);
        includedPath.setEncryptionAlgorithm(CosmosEncryptionAlgorithm.AEAD_AES_256_CBC_HMAC_SHA256);

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
        TestEncryptionKeyStoreProvider testEncryptionKeyStoreProvider = new TestEncryptionKeyStoreProvider();
        EncryptionKeyWrapMetadata metadata1 = new EncryptionKeyWrapMetadata(testEncryptionKeyStoreProvider.getProviderName(), "key1", "tempmetadata1");
        EncryptionKeyWrapMetadata metadata2 = new EncryptionKeyWrapMetadata(testEncryptionKeyStoreProvider.getProviderName(), "key2", "tempmetadata2");
        cosmosEncryptionAsyncClient.getCosmosAsyncClient().createDatabase(databaseId).block();
        CosmosEncryptionAsyncDatabase encryptionAsyncDatabase = cosmosEncryptionAsyncClient.getCosmosEncryptionAsyncDatabase(databaseId);
        encryptionAsyncDatabase.createClientEncryptionKey("key1",
            CosmosEncryptionAlgorithm.AEAD_AES_256_CBC_HMAC_SHA256, metadata1).block();
        encryptionAsyncDatabase.createClientEncryptionKey("key2",
            CosmosEncryptionAlgorithm.AEAD_AES_256_CBC_HMAC_SHA256, metadata2).block();
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
