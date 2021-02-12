// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.encryption.implementation.CosmosEncryptionAlgorithm;
import com.azure.cosmos.encryption.implementation.CosmosEncryptionType;
import com.azure.cosmos.models.ClientEncryptionIncludedPath;
import com.azure.cosmos.models.ClientEncryptionPolicy;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.EncryptionKeyWrapMetadata;
import com.azure.cosmos.models.EncryptionSqlQuerySpec;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class EncryptionCrudTest extends TestSuiteBase {
    private CosmosAsyncClient client;
    private CosmosAsyncDatabase cosmosAsyncDatabase;
    private static final int TIMEOUT = 6000_000;
    private EncryptionCosmosAsyncClient encryptionCosmosAsyncClient;
    private EncryptionCosmosAsyncDatabase encryptionCosmosAsyncDatabase;
    private EncryptionCosmosAsyncContainer encryptionCosmosAsyncContainer;
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
        encryptionCosmosAsyncClient = EncryptionCosmosAsyncClient.buildEncryptionCosmosAsyncClient(this.client,
            new TestEncryptionKeyStoreProvider());
        encryptionCosmosAsyncDatabase =
            encryptionCosmosAsyncClient.getEncryptedCosmosAsyncDatabase(cosmosAsyncDatabase);

        ClientEncryptionPolicy clientEncryptionPolicy = new ClientEncryptionPolicy(getPaths());
        String containerId = UUID.randomUUID().toString();
        CosmosContainerProperties properties = new CosmosContainerProperties(containerId, "/mypk");
        properties.setClientEncryptionPolicy(clientEncryptionPolicy);
        encryptionCosmosAsyncDatabase.createEncryptionContainerAsync(properties).block();
        encryptionCosmosAsyncContainer = encryptionCosmosAsyncDatabase.getEncryptedCosmosAsyncContainer(containerId);
        metadata1 = new EncryptionKeyWrapMetadata("key1", "tempmetadata1");
        metadata2 = new EncryptionKeyWrapMetadata("key2", "tempmetadata2");
        encryptionCosmosAsyncDatabase.createClientEncryptionKey("key1",
            CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256, metadata1).block();
        encryptionCosmosAsyncDatabase.createClientEncryptionKey("key2",
            CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256, metadata2).block();
        encryptionCosmosAsyncDatabase.rewrapClientEncryptionKey("key2", metadata2).block();
    }

    @AfterClass(groups = {"encryption"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        assertThat(this.client).isNotNull();
        this.client.close();
    }

    @Test(groups = {"encryption"}, timeOut = TIMEOUT)
    public void createItemEncrypt_readItemDecrypt() {
        Pojo properties = getItem(UUID.randomUUID().toString());
        CosmosItemResponse<Pojo> itemResponse = encryptionCosmosAsyncContainer.createItem(properties,
            new PartitionKey(properties.mypk), new CosmosItemRequestOptions()).block();
        assertThat(itemResponse.getRequestCharge()).isGreaterThan(0);
        Pojo responseItem = itemResponse.getItem();
        validateResponse(properties, responseItem);

        Pojo readItem = encryptionCosmosAsyncContainer.readItem(properties.id, new PartitionKey(properties.mypk),
            new CosmosItemRequestOptions(), Pojo.class).block().getItem();
        validateResponse(properties, readItem);
    }

    @Test(groups = {"encryption"}, timeOut = TIMEOUT)
    public void upsertItem_readItem() throws Exception {
        Pojo properties = getItem(UUID.randomUUID().toString());
        CosmosItemResponse<Pojo> itemResponse = encryptionCosmosAsyncContainer.upsertItem(properties,
            new PartitionKey(properties.mypk), new CosmosItemRequestOptions()).block();
        assertThat(itemResponse.getRequestCharge()).isGreaterThan(0);
        Pojo responseItem = itemResponse.getItem();
        validateResponse(properties, responseItem);

        Pojo readItem = encryptionCosmosAsyncContainer.readItem(properties.id, new PartitionKey(properties.mypk),
            new CosmosItemRequestOptions(), Pojo.class).block().getItem();
        validateResponse(properties, readItem);
    }


    @Test(groups = {"encryption"}, timeOut = TIMEOUT)
    public void queryItems() {
        Pojo properties = getItem(UUID.randomUUID().toString());
        CosmosItemResponse<Pojo> itemResponse = encryptionCosmosAsyncContainer.createItem(properties,
            new PartitionKey(properties.mypk), new CosmosItemRequestOptions()).block();
        assertThat(itemResponse.getRequestCharge()).isGreaterThan(0);
        Pojo responseItem = itemResponse.getItem();
        validateResponse(properties, responseItem);

        String query = String.format("SELECT * from c where c.id = '%s'", properties.id);
        CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();

        SqlQuerySpec querySpec = new SqlQuerySpec(query);
        CosmosPagedFlux<Pojo> feedResponseIterator =
            encryptionCosmosAsyncContainer.queryItems(querySpec, cosmosQueryRequestOptions, Pojo.class);
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
        CosmosItemResponse<Pojo> itemResponse = encryptionCosmosAsyncContainer.createItem(properties,
            new PartitionKey(properties.mypk), new CosmosItemRequestOptions()).block();
        assertThat(itemResponse.getRequestCharge()).isGreaterThan(0);
        Pojo responseItem = itemResponse.getItem();
        validateResponse(properties, responseItem);

        String query = String.format("SELECT * FROM c where c.sensitiveString = @sensitiveString and c.nonSensitive = " +
            "@nonSensitive");
        SqlQuerySpec querySpec = new SqlQuerySpec(query);
        SqlParameter parameter1 = new SqlParameter("@nonSensitive", properties.nonSensitive);
        List<SqlParameter> parameters = new ArrayList<>();
        parameters.add(parameter1);
        querySpec.setParameters(parameters);

        SqlParameter parameter2 = new SqlParameter("@sensitiveString", properties.sensitiveString);
        EncryptionSqlQuerySpec encryptionSqlQuerySpec = new EncryptionSqlQuerySpec(querySpec,
            encryptionCosmosAsyncContainer);
        encryptionSqlQuerySpec.addEncryptionParameterAsync(parameter2, "/sensitiveString").block();

        CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();

        CosmosPagedFlux<Pojo> feedResponseIterator =
            encryptionCosmosAsyncContainer.queryItemsOnEncryptedProperties(encryptionSqlQuerySpec,
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
    public void queryItemsWithContinuationTokenAndPageSize() throws Exception {
        List<String> actualIds = new ArrayList<>();
        Pojo properties = getItem(UUID.randomUUID().toString());
        encryptionCosmosAsyncContainer.createItem(properties, new PartitionKey(properties.mypk),
            new CosmosItemRequestOptions()).block();
        actualIds.add(properties.id);
        properties = getItem(UUID.randomUUID().toString());
        encryptionCosmosAsyncContainer.createItem(properties, new PartitionKey(properties.mypk),
            new CosmosItemRequestOptions()).block();
        actualIds.add(properties.id);
        properties = getItem(UUID.randomUUID().toString());
        encryptionCosmosAsyncContainer.createItem(properties, new PartitionKey(properties.mypk),
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
            encryptionCosmosAsyncContainer.queryItems(query, cosmosQueryRequestOptions, Pojo.class);

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

    private Pojo getItem(String documentId) {
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
        nestedPojo.sensitiveString = "nestedPojo";
        nestedPojo.sensitiveDouble = 10.123;
        nestedPojo.sensitiveInt = 123;
        nestedPojo.sensitiveLong = 1234;
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

        pojo.sensitiveChildPojo2DArray = new Pojo[][]{{childPojo1,childPojo2}, {childPojo1,childPojo2}};

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

    private List<ClientEncryptionIncludedPath> getPaths() {
        ClientEncryptionIncludedPath includedPath1 = new ClientEncryptionIncludedPath();
        includedPath1.clientEncryptionKeyId = "key1";
        includedPath1.path = "/sensitiveString";
        includedPath1.encryptionType = CosmosEncryptionType.DETERMINISTIC;
        includedPath1.encryptionAlgorithm = CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256;

        ClientEncryptionIncludedPath includedPath2 = new ClientEncryptionIncludedPath();
        includedPath2.clientEncryptionKeyId = "key2";
        includedPath2.path = "/nonValidPath";
        includedPath2.encryptionType = CosmosEncryptionType.DETERMINISTIC;
        includedPath2.encryptionAlgorithm = CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256;

        ClientEncryptionIncludedPath includedPath3 = new ClientEncryptionIncludedPath();
        includedPath3.clientEncryptionKeyId = "key1";
        includedPath3.path = "/sensitiveInt";
        includedPath3.encryptionType = CosmosEncryptionType.DETERMINISTIC;
        includedPath3.encryptionAlgorithm = CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256;

        ClientEncryptionIncludedPath includedPath4 = new ClientEncryptionIncludedPath();
        includedPath4.clientEncryptionKeyId = "key2";
        includedPath4.path = "/sensitiveFloat";
        includedPath4.encryptionType = CosmosEncryptionType.DETERMINISTIC;
        includedPath4.encryptionAlgorithm = CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256;

        ClientEncryptionIncludedPath includedPath5 = new ClientEncryptionIncludedPath();
        includedPath5.clientEncryptionKeyId = "key1";
        includedPath5.path = "/sensitiveLong";
        includedPath5.encryptionType = CosmosEncryptionType.DETERMINISTIC;
        includedPath5.encryptionAlgorithm = CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256;

        ClientEncryptionIncludedPath includedPath6 = new ClientEncryptionIncludedPath();
        includedPath6.clientEncryptionKeyId = "key2";
        includedPath6.path = "/sensitiveDouble";
        includedPath6.encryptionType = CosmosEncryptionType.DETERMINISTIC;
        includedPath6.encryptionAlgorithm = CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256;

        ClientEncryptionIncludedPath includedPath7 = new ClientEncryptionIncludedPath();
        includedPath7.clientEncryptionKeyId = "key1";
        includedPath7.path = "/sensitiveBoolean";
        includedPath7.encryptionType = CosmosEncryptionType.DETERMINISTIC;
        includedPath7.encryptionAlgorithm = CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256;

        ClientEncryptionIncludedPath includedPath8 = new ClientEncryptionIncludedPath();
        includedPath8.clientEncryptionKeyId = "key1";
        includedPath8.path = "/sensitiveNestedPojo";
        includedPath8.encryptionType = CosmosEncryptionType.DETERMINISTIC;
        includedPath8.encryptionAlgorithm = CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256;


        ClientEncryptionIncludedPath includedPath9 = new ClientEncryptionIncludedPath();
        includedPath9.clientEncryptionKeyId = "key1";
        includedPath9.path = "/sensitiveIntArray";
        includedPath9.encryptionType = CosmosEncryptionType.DETERMINISTIC;
        includedPath9.encryptionAlgorithm = CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256;

        ClientEncryptionIncludedPath includedPath10 = new ClientEncryptionIncludedPath();
        includedPath10.clientEncryptionKeyId = "key2";
        includedPath10.path = "/sensitiveString3DArray";
        includedPath10.encryptionType = CosmosEncryptionType.DETERMINISTIC;
        includedPath10.encryptionAlgorithm = CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256;

        ClientEncryptionIncludedPath includedPath11 = new ClientEncryptionIncludedPath();
        includedPath11.clientEncryptionKeyId = "key1";
        includedPath11.path = "/sensitiveStringArray";
        includedPath11.encryptionType = CosmosEncryptionType.DETERMINISTIC;
        includedPath11.encryptionAlgorithm = CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256;

        ClientEncryptionIncludedPath includedPath12 = new ClientEncryptionIncludedPath();
        includedPath12.clientEncryptionKeyId = "key1";
        includedPath12.path = "/sensitiveChildPojoList";
        includedPath12.encryptionType = CosmosEncryptionType.DETERMINISTIC;
        includedPath12.encryptionAlgorithm = CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256;

        ClientEncryptionIncludedPath includedPath13 = new ClientEncryptionIncludedPath();
        includedPath13.clientEncryptionKeyId = "key1";
        includedPath13.path = "/sensitiveChildPojo2DArray";
        includedPath13.encryptionType = CosmosEncryptionType.DETERMINISTIC;
        includedPath13.encryptionAlgorithm = CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256;

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
