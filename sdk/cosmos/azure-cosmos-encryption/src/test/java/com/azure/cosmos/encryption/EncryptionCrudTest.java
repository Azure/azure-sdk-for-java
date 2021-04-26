// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosException;
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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.data.encryption.cryptography.EncryptionKeyStoreProvider;
import com.microsoft.data.encryption.cryptography.KeyEncryptionKeyAlgorithm;
import com.microsoft.data.encryption.cryptography.MicrosoftDataEncryptionException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class EncryptionCrudTest extends TestSuiteBase {
    private CosmosAsyncClient client;
    private CosmosAsyncDatabase cosmosAsyncDatabase;
    private static final int TIMEOUT = 6000_000;
    private CosmosEncryptionAsyncClient cosmosEncryptionAsyncClient;
    private CosmosEncryptionAsyncDatabase cosmosEncryptionAsyncDatabase;
    private CosmosEncryptionAsyncContainer cosmosEncryptionAsyncContainer;
    private EncryptionKeyWrapMetadata metadata1;
    private EncryptionKeyWrapMetadata metadata2;

    @Factory(dataProvider = "clientBuilders")
    public EncryptionCrudTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"encryption"}, timeOut = SETUP_TIMEOUT)
    public void before_CosmosItemTest() {
        assertThat(this.client).isNull();
        this.client = getClientBuilder().buildAsyncClient();
        cosmosAsyncDatabase = getSharedCosmosDatabase(this.client);
        cosmosEncryptionAsyncClient = CosmosEncryptionAsyncClient.createCosmosEncryptionAsyncClient(this.client,
            new TestEncryptionKeyStoreProvider());
        cosmosEncryptionAsyncDatabase =
            cosmosEncryptionAsyncClient.getCosmosEncryptionAsyncDatabase(cosmosAsyncDatabase);

        metadata1 = new EncryptionKeyWrapMetadata("key1", "tempmetadata1");
        metadata2 = new EncryptionKeyWrapMetadata("key2", "tempmetadata2");
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
    }

    @AfterClass(groups = {"encryption"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        assertThat(this.client).isNotNull();
        this.client.close();
    }

    @Test(groups = {"encryption"}, timeOut = TIMEOUT)
    public void createItemEncrypt_readItemDecrypt() throws MicrosoftDataEncryptionException, IOException {
        Pojo properties = getItem(UUID.randomUUID().toString());
        CosmosItemResponse<Pojo> itemResponse = cosmosEncryptionAsyncContainer.createItem(properties,
            new PartitionKey(properties.mypk), new CosmosItemRequestOptions()).block();
        assertThat(itemResponse.getRequestCharge()).isGreaterThan(0);
        Pojo responseItem = itemResponse.getItem();
        validateResponse(properties, responseItem);

        Pojo readItem = cosmosEncryptionAsyncContainer.readItem(properties.id, new PartitionKey(properties.mypk),
            new CosmosItemRequestOptions(), Pojo.class).block().getItem();
        validateResponse(properties, readItem);

        //Check for max length support of 8000
        properties = getItem(UUID.randomUUID().toString());
        String longString = "";
        for (int i = 0; i < 8000; i++) {
            longString += "a";
        }
        properties.sensitiveString = longString;
        itemResponse = cosmosEncryptionAsyncContainer.createItem(properties,
            new PartitionKey(properties.mypk), new CosmosItemRequestOptions()).block();
        assertThat(itemResponse.getRequestCharge()).isGreaterThan(0);
        responseItem = itemResponse.getItem();
        validateResponse(properties, responseItem);

        //Check for exception for length greater that 8000
        longString += "a";
        properties.sensitiveString = longString;
        try {
            cosmosEncryptionAsyncContainer.createItem(properties,
                new PartitionKey(properties.mypk), new CosmosItemRequestOptions()).block();
            fail("Item create should fail as length of encryption field  is greater than 8000");
        } catch (CosmosException ex) {
            assertThat(ex.getMessage()).contains("Unable to convert JSON to byte[]");
            assertThat(ex.getCause() instanceof MicrosoftDataEncryptionException).isTrue();
        }
    }

    @Test(groups = {"encryption"}, timeOut = TIMEOUT)
    public void upsertItem_readItem() {
        Pojo properties = getItem(UUID.randomUUID().toString());
        CosmosItemResponse<Pojo> itemResponse = cosmosEncryptionAsyncContainer.upsertItem(properties,
            new PartitionKey(properties.mypk), new CosmosItemRequestOptions()).block();
        assertThat(itemResponse.getRequestCharge()).isGreaterThan(0);
        Pojo responseItem = itemResponse.getItem();
        validateResponse(properties, responseItem);

        Pojo readItem = cosmosEncryptionAsyncContainer.readItem(properties.id, new PartitionKey(properties.mypk),
            new CosmosItemRequestOptions(), Pojo.class).block().getItem();
        validateResponse(properties, readItem);
    }

    @Test(groups = {"encryption"}, timeOut = TIMEOUT)
    public void queryItems() {
        Pojo properties = getItem(UUID.randomUUID().toString());
        CosmosItemResponse<Pojo> itemResponse = cosmosEncryptionAsyncContainer.createItem(properties,
            new PartitionKey(properties.mypk), new CosmosItemRequestOptions()).block();
        assertThat(itemResponse.getRequestCharge()).isGreaterThan(0);
        Pojo responseItem = itemResponse.getItem();
        validateResponse(properties, responseItem);

        String query = String.format("SELECT * from c where c.id = '%s'", properties.id);
        CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();

        SqlQuerySpec querySpec = new SqlQuerySpec(query);
        CosmosPagedFlux<Pojo> feedResponseIterator =
            cosmosEncryptionAsyncContainer.queryItems(querySpec, cosmosQueryRequestOptions, Pojo.class);
        List<Pojo> feedResponse = feedResponseIterator.byPage().blockFirst().getResults();
        assertThat(feedResponse.size()).isGreaterThanOrEqualTo(1);
        for (Pojo pojo : feedResponse) {
            if (pojo.id.equals(properties.id)) {
                validateResponse(pojo, responseItem);
            }
        }
    }

    @Test(groups = {"encryption"}, timeOut = TIMEOUT)
    public void queryItemsOnEncryptedProperties() {
        Pojo properties = getItem(UUID.randomUUID().toString());
        CosmosItemResponse<Pojo> itemResponse = cosmosEncryptionAsyncContainer.createItem(properties,
            new PartitionKey(properties.mypk), new CosmosItemRequestOptions()).block();
        assertThat(itemResponse.getRequestCharge()).isGreaterThan(0);
        Pojo responseItem = itemResponse.getItem();
        validateResponse(properties, responseItem);

        String query = String.format("SELECT * FROM c where c.sensitiveString = @sensitiveString and c.nonSensitive =" +
            " " +
            "@nonSensitive and c.sensitiveLong = @sensitiveLong");
        SqlQuerySpec querySpec = new SqlQuerySpec(query);
        SqlParameter parameter1 = new SqlParameter("@nonSensitive", properties.nonSensitive);
        querySpec.getParameters().add(parameter1);

        SqlParameter parameter2 = new SqlParameter("@sensitiveString", properties.sensitiveString);
        SqlParameter parameter3 = new SqlParameter("@sensitiveLong", properties.sensitiveLong);
        SqlQuerySpecWithEncryption sqlQuerySpecWithEncryption = new SqlQuerySpecWithEncryption(querySpec);
        sqlQuerySpecWithEncryption.addEncryptionParameter("/sensitiveString", parameter2);
        sqlQuerySpecWithEncryption.addEncryptionParameter("/sensitiveLong", parameter3);

        CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();
        CosmosPagedFlux<Pojo> feedResponseIterator =
            cosmosEncryptionAsyncContainer.queryItemsOnEncryptedProperties(sqlQuerySpecWithEncryption,
                cosmosQueryRequestOptions, Pojo.class);
        List<Pojo> feedResponse = feedResponseIterator.byPage().blockFirst().getResults();
        assertThat(feedResponse.size()).isGreaterThanOrEqualTo(1);
        for (Pojo pojo : feedResponse) {
            if (pojo.id.equals(properties.id)) {
                validateResponse(pojo, responseItem);
            }
        }
    }

    @Test(groups = {"encryption"}, timeOut = TIMEOUT)
    public void queryItemsOnRandomizedEncryption() {
        Pojo properties = getItem(UUID.randomUUID().toString());
        CosmosItemResponse<Pojo> itemResponse = cosmosEncryptionAsyncContainer.createItem(properties,
            new PartitionKey(properties.mypk), new CosmosItemRequestOptions()).block();
        assertThat(itemResponse.getRequestCharge()).isGreaterThan(0);
        Pojo responseItem = itemResponse.getItem();
        validateResponse(properties, responseItem);

        String query = String.format("SELECT * FROM c where c.sensitiveString = @sensitiveString and c.nonSensitive =" +
            " " +
            "@nonSensitive and c.sensitiveDouble = @sensitiveDouble");
        SqlQuerySpec querySpec = new SqlQuerySpec(query);
        SqlParameter parameter1 = new SqlParameter("@nonSensitive", properties.nonSensitive);
        querySpec.getParameters().add(parameter1);

        SqlParameter parameter2 = new SqlParameter("@sensitiveString", properties.sensitiveString);
        SqlParameter parameter3 = new SqlParameter("@sensitiveDouble", properties.sensitiveDouble);
        SqlQuerySpecWithEncryption sqlQuerySpecWithEncryption = new SqlQuerySpecWithEncryption(querySpec);
        sqlQuerySpecWithEncryption.addEncryptionParameter("/sensitiveString", parameter2);
        sqlQuerySpecWithEncryption.addEncryptionParameter("/sensitiveDouble", parameter3);

        CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();
        CosmosPagedFlux<Pojo> feedResponseIterator =
            cosmosEncryptionAsyncContainer.queryItemsOnEncryptedProperties(sqlQuerySpecWithEncryption,
                cosmosQueryRequestOptions, Pojo.class);
        try {
            List<Pojo> feedResponse = feedResponseIterator.byPage().blockFirst().getResults();
            fail("Query on randomized parameter should fail");
        } catch (IllegalArgumentException ex) {
            assertThat(ex.getMessage()).contains("Path /sensitiveDouble cannot be used in the " +
                "query because of randomized encryption");
        }

    }

    @Test(groups = {"encryption"}, timeOut = TIMEOUT)
    public void queryItemsWithContinuationTokenAndPageSize() throws Exception {
        List<String> actualIds = new ArrayList<>();
        Pojo properties = getItem(UUID.randomUUID().toString());
        cosmosEncryptionAsyncContainer.createItem(properties, new PartitionKey(properties.mypk),
            new CosmosItemRequestOptions()).block();
        actualIds.add(properties.id);
        properties = getItem(UUID.randomUUID().toString());
        cosmosEncryptionAsyncContainer.createItem(properties, new PartitionKey(properties.mypk),
            new CosmosItemRequestOptions()).block();
        actualIds.add(properties.id);
        properties = getItem(UUID.randomUUID().toString());
        cosmosEncryptionAsyncContainer.createItem(properties, new PartitionKey(properties.mypk),
            new CosmosItemRequestOptions()).block();
        actualIds.add(properties.id);


        String query = String.format("SELECT * from c where c.id in ('%s', '%s', '%s')", actualIds.get(0),
            actualIds.get(1), actualIds.get(2));
        CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();
        String continuationToken = null;
        int pageSize = 1;

        int initialDocumentCount = 3;
        int finalDocumentCount = 0;

        CosmosPagedFlux<Pojo> feedResponseIterator =
            cosmosEncryptionAsyncContainer.queryItems(query, cosmosQueryRequestOptions, Pojo.class);

        do {
            Iterable<FeedResponse<Pojo>> feedResponseIterable =
                feedResponseIterator.byPage(1).toIterable();
            for (FeedResponse<Pojo> fr : feedResponseIterable) {
                int resultSize = fr.getResults().size();
                assertThat(resultSize).isEqualTo(pageSize);
                finalDocumentCount += fr.getResults().size();
                continuationToken = fr.getContinuationToken();
            }
        } while (continuationToken != null);

        assertThat(finalDocumentCount).isEqualTo(initialDocumentCount);
    }


    private void validateResponse(Pojo originalItem, Pojo result) {
        assertThat(result.id).isEqualTo(originalItem.id);
        assertThat(result.mypk).isEqualTo(originalItem.mypk);
        assertThat(result.nonSensitive).isEqualTo(originalItem.nonSensitive);
        assertThat(result.sensitiveString).isEqualTo(originalItem.sensitiveString);
        assertThat(result.sensitiveInt).isEqualTo(originalItem.sensitiveInt);
        assertThat(result.sensitiveFloat).isEqualTo(originalItem.sensitiveFloat);
        assertThat(result.sensitiveLong).isEqualTo(originalItem.sensitiveLong);
        assertThat(result.sensitiveDouble).isEqualTo(originalItem.sensitiveDouble);
        assertThat(result.sensitiveBoolean).isEqualTo(originalItem.sensitiveBoolean);
        assertThat(result.sensitiveIntArray).isEqualTo(originalItem.sensitiveIntArray);
        assertThat(result.sensitiveStringArray).isEqualTo(originalItem.sensitiveStringArray);
        assertThat(result.sensitiveString3DArray).isEqualTo(originalItem.sensitiveString3DArray);

        assertThat(result.sensitiveNestedPojo.id).isEqualTo(originalItem.sensitiveNestedPojo.id);
        assertThat(result.sensitiveNestedPojo.mypk).isEqualTo(originalItem.sensitiveNestedPojo.mypk);
        assertThat(result.sensitiveNestedPojo.nonSensitive).isEqualTo(originalItem.sensitiveNestedPojo.nonSensitive);
        assertThat(result.sensitiveNestedPojo.sensitiveString).isEqualTo(originalItem.sensitiveNestedPojo.sensitiveString);
        assertThat(result.sensitiveNestedPojo.sensitiveInt).isEqualTo(originalItem.sensitiveNestedPojo.sensitiveInt);
        assertThat(result.sensitiveNestedPojo.sensitiveFloat).isEqualTo(originalItem.sensitiveNestedPojo.sensitiveFloat);
        assertThat(result.sensitiveNestedPojo.sensitiveLong).isEqualTo(originalItem.sensitiveNestedPojo.sensitiveLong);
        assertThat(result.sensitiveNestedPojo.sensitiveDouble).isEqualTo(originalItem.sensitiveNestedPojo.sensitiveDouble);
        assertThat(result.sensitiveNestedPojo.sensitiveBoolean).isEqualTo(originalItem.sensitiveNestedPojo.sensitiveBoolean);
        assertThat(result.sensitiveNestedPojo.sensitiveIntArray).isEqualTo(originalItem.sensitiveNestedPojo.sensitiveIntArray);
        assertThat(result.sensitiveNestedPojo.sensitiveStringArray).isEqualTo(originalItem.sensitiveNestedPojo.sensitiveStringArray);
        assertThat(result.sensitiveNestedPojo.sensitiveString3DArray).isEqualTo(originalItem.sensitiveNestedPojo.sensitiveString3DArray);

        assertThat(result.sensitiveChildPojoList.size()).isEqualTo(originalItem.sensitiveChildPojoList.size());
        assertThat(result.sensitiveChildPojoList.get(0).sensitiveString).isEqualTo(originalItem.sensitiveChildPojoList.get(0).sensitiveString);
        assertThat(result.sensitiveChildPojoList.get(0).sensitiveInt).isEqualTo(originalItem.sensitiveChildPojoList.get(0).sensitiveInt);
        assertThat(result.sensitiveChildPojoList.get(0).sensitiveFloat).isEqualTo(originalItem.sensitiveChildPojoList.get(0).sensitiveFloat);
        assertThat(result.sensitiveChildPojoList.get(0).sensitiveLong).isEqualTo(originalItem.sensitiveChildPojoList.get(0).sensitiveLong);
        assertThat(result.sensitiveChildPojoList.get(0).sensitiveDouble).isEqualTo(originalItem.sensitiveChildPojoList.get(0).sensitiveDouble);
        assertThat(result.sensitiveChildPojoList.get(0).sensitiveIntArray).isEqualTo(originalItem.sensitiveChildPojoList.get(0).sensitiveIntArray);
        assertThat(result.sensitiveChildPojoList.get(0).sensitiveStringArray).isEqualTo(originalItem.sensitiveChildPojoList.get(0).sensitiveStringArray);
        assertThat(result.sensitiveChildPojoList.get(0).sensitiveString3DArray).isEqualTo(originalItem.sensitiveChildPojoList.get(0).sensitiveString3DArray);

        assertThat(result.sensitiveChildPojo2DArray.length).isEqualTo(originalItem.sensitiveChildPojo2DArray.length);
        assertThat(result.sensitiveChildPojo2DArray[0][0].sensitiveString).isEqualTo(originalItem.sensitiveChildPojo2DArray[0][0].sensitiveString);
        assertThat(result.sensitiveChildPojo2DArray[0][0].sensitiveInt).isEqualTo(originalItem.sensitiveChildPojo2DArray[0][0].sensitiveInt);
        assertThat(result.sensitiveChildPojo2DArray[0][0].sensitiveFloat).isEqualTo(originalItem.sensitiveChildPojo2DArray[0][0].sensitiveFloat);
        assertThat(result.sensitiveChildPojo2DArray[0][0].sensitiveLong).isEqualTo(originalItem.sensitiveChildPojo2DArray[0][0].sensitiveLong);
        assertThat(result.sensitiveChildPojo2DArray[0][0].sensitiveDouble).isEqualTo(originalItem.sensitiveChildPojo2DArray[0][0].sensitiveDouble);
        assertThat(result.sensitiveChildPojo2DArray[0][0].sensitiveIntArray).isEqualTo(originalItem.sensitiveChildPojo2DArray[0][0].sensitiveIntArray);
        assertThat(result.sensitiveChildPojo2DArray[0][0].sensitiveStringArray).isEqualTo(originalItem.sensitiveChildPojo2DArray[0][0].sensitiveStringArray);
        assertThat(result.sensitiveChildPojo2DArray[0][0].sensitiveString3DArray).isEqualTo(originalItem.sensitiveChildPojo2DArray[0][0].sensitiveString3DArray);
    }

    public static Pojo getItem(String documentId) {
        Pojo pojo = new Pojo();
        pojo.id = documentId;
        pojo.mypk = documentId;
        pojo.nonSensitive = UUID.randomUUID().toString();
        pojo.sensitiveString = "testingString";
        pojo.sensitiveDouble = 10.123;
        pojo.sensitiveFloat = 20.0f;
        pojo.sensitiveInt = 30;
        pojo.sensitiveLong = 1234;
        pojo.sensitiveBoolean = true;

        Pojo nestedPojo = new Pojo();
        nestedPojo.id = "nestedPojo";
        nestedPojo.mypk = "nestedPojo";
        nestedPojo.sensitiveString = "nestedPojo";
        nestedPojo.sensitiveDouble = 10.123;
        nestedPojo.sensitiveInt = 123;
        nestedPojo.sensitiveLong = 1234;
        nestedPojo.sensitiveStringArray = new String[]{"str1", "str1"};
        nestedPojo.sensitiveString3DArray = new String[][][]{{{"str1", "str2"}, {"str3", "str4"}}, {{"str5", "str6"}, {
            "str7", "str8"}}};
        nestedPojo.sensitiveBoolean = true;

        pojo.sensitiveNestedPojo = nestedPojo;

        pojo.sensitiveIntArray = new int[]{1, 2};
        pojo.sensitiveStringArray = new String[]{"str1", "str1"};
        pojo.sensitiveString3DArray = new String[][][]{{{"str1", "str2"}, {"str3", "str4"}}, {{"str5", "str6"}, {
            "str7", "str8"}}};

        Pojo childPojo1 = new Pojo();
        childPojo1.id = "childPojo1";
        childPojo1.sensitiveString = "child1TestingString";
        childPojo1.sensitiveDouble = 10.123;
        childPojo1.sensitiveInt = 123;
        childPojo1.sensitiveLong = 1234;
        childPojo1.sensitiveBoolean = true;
        childPojo1.sensitiveStringArray = new String[]{"str1", "str1"};
        childPojo1.sensitiveString3DArray = new String[][][]{{{"str1", "str2"}, {"str3", "str4"}}, {{"str5", "str6"}, {
            "str7", "str8"}}};
        Pojo childPojo2 = new Pojo();
        childPojo2.id = "childPojo2";
        childPojo2.sensitiveString = "child2TestingString";
        childPojo2.sensitiveDouble = 10.123;
        childPojo2.sensitiveInt = 123;
        childPojo2.sensitiveLong = 1234;
        childPojo2.sensitiveBoolean = true;

        pojo.sensitiveChildPojoList = new ArrayList<>();
        pojo.sensitiveChildPojoList.add(childPojo1);
        pojo.sensitiveChildPojoList.add(childPojo2);

        pojo.sensitiveChildPojo2DArray = new Pojo[][]{{childPojo1, childPojo2}, {childPojo1, childPojo2}};

        return pojo;
    }


    public static class Pojo {
        public String id;
        @JsonProperty
        public String mypk;
        @JsonProperty
        public String nonSensitive;
        @JsonProperty
        public String sensitiveString;
        @JsonProperty
        public int sensitiveInt;
        @JsonProperty
        public float sensitiveFloat;
        @JsonProperty
        public long sensitiveLong;
        @JsonProperty
        public double sensitiveDouble;
        @JsonProperty
        public boolean sensitiveBoolean;
        @JsonProperty
        public Pojo sensitiveNestedPojo;
        @JsonProperty
        public int[] sensitiveIntArray;
        @JsonProperty
        public String[] sensitiveStringArray;
        @JsonProperty
        public String[][][] sensitiveString3DArray;
        @JsonProperty
        public Pojo[][] sensitiveChildPojo2DArray;
        @JsonProperty
        public List<Pojo> sensitiveChildPojoList;
    }

    public static class TestEncryptionKeyStoreProvider extends EncryptionKeyStoreProvider {
        Map<String, Integer> keyInfo = new HashMap<>();

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
        public byte[] sign(String s, boolean b) throws MicrosoftDataEncryptionException {
            return new byte[0];
        }

        @Override
        public boolean verify(String s, boolean b, byte[] bytes) throws MicrosoftDataEncryptionException {
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
}
