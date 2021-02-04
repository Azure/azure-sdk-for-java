// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.core.http.ProxyOptions;
import com.azure.cosmos.encryption.CosmosEncryptionAlgorithm;
import com.azure.cosmos.encryption.CosmosEncryptionType;
import com.azure.cosmos.encryption.DataEncryptionKey;
import com.azure.cosmos.encryption.EncryptionAsyncCosmosClient;
import com.azure.cosmos.encryption.EncryptionCosmosAsyncContainer;
import com.azure.cosmos.encryption.EncryptionCosmosAsyncDatabase;
import com.azure.cosmos.implementation.encryption.SimpleInMemoryProvider;
import com.azure.cosmos.implementation.encryption.TestUtils;
import com.azure.cosmos.models.ClientEncryptionIncludedPath;
import com.azure.cosmos.models.ClientEncryptionPolicy;
import com.azure.cosmos.models.CosmosClientEncryptionKeyProperties;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.EncryptionKeyWrapMetadata;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.rx.TestSuiteBase;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.data.encryption.cryptography.EncryptionKeyStoreProvider;
import com.microsoft.data.encryption.cryptography.KeyEncryptionKeyAlgorithm;
import com.microsoft.data.encryption.cryptography.MicrosoftDataEncryptionException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class EncryptionTest2 extends TestSuiteBase {
    static SimpleInMemoryProvider simpleInMemoryProvider = new SimpleInMemoryProvider();

    private CosmosAsyncClient client;
    private CosmosAsyncContainer cosmosAsyncContainer;
    private CosmosAsyncDatabase cosmosAsyncDatabase;
    private static final int TIMEOUT = 60_000;
    private EncryptionAsyncCosmosClient encryptionAsyncCosmosClient;
    private EncryptionCosmosAsyncDatabase encryptionCosmosAsyncDatabase;
    private EncryptionCosmosAsyncContainer encryptionCosmosAsyncContainer;
    private CosmosAsyncClientEncryptionKey cosmosAsyncClientEncryptionKey1;
    private CosmosAsyncClientEncryptionKey cosmosAsyncClientEncryptionKey2;
    private EncryptionKeyWrapMetadata metadata1;
    private EncryptionKeyWrapMetadata metadata2;
    CosmosClientEncryptionKeyProperties clientEncryptionKeyProperties1;
    CosmosClientEncryptionKeyProperties clientEncryptionKeyProperties2;
    private CosmosContainer container;
    private CosmosAsyncDatabase dotNetDB;

    @Factory(dataProvider = "clientBuilders")
    public EncryptionTest2(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"encryption"}/*, timeOut = SETUP_TIMEOUT*/)
    public void before_CosmosItemTest() {
        GatewayConnectionConfig gatewayConnectionConfig = new GatewayConnectionConfig();
        gatewayConnectionConfig.setProxy(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("127.0.0.1",
            8888)));
        assertThat(this.client).isNull();
        this.client = getClientBuilder().gatewayMode(gatewayConnectionConfig).buildAsyncClient();
        cosmosAsyncDatabase = getSharedCosmosDatabase(this.client);
        encryptionAsyncCosmosClient = EncryptionAsyncCosmosClient.buildEncryptionAsyncClient(this.client,
            new TestEncryptionKeyStoreProvider());
        encryptionCosmosAsyncDatabase =
            encryptionAsyncCosmosClient.getEncryptedCosmosAsyncDatabase(cosmosAsyncDatabase);

        ClientEncryptionIncludedPath includedPath1 = new ClientEncryptionIncludedPath();
        includedPath1.clientEncryptionKeyId = "key1";
        includedPath1.path = "/sensitive";
        includedPath1.encryptionType = CosmosEncryptionType.DETERMINISTIC;
        includedPath1.encryptionAlgorithm = CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256;

        ClientEncryptionIncludedPath includedPath2 = new ClientEncryptionIncludedPath();
        includedPath2.clientEncryptionKeyId = "key2";
        includedPath2.path = "/nonValidPath";
        includedPath2.encryptionType = CosmosEncryptionType.DETERMINISTIC;
        includedPath2.encryptionAlgorithm = CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256;

        List<ClientEncryptionIncludedPath> paths = new ArrayList<>();
        paths.add(includedPath1);
        paths.add(includedPath2);

        ClientEncryptionPolicy clientEncryptionPolicy = new ClientEncryptionPolicy(paths);
        String containerId = UUID.randomUUID().toString();
        CosmosContainerProperties properties = new CosmosContainerProperties(containerId, "/mypk");
        properties.setClientEncryptionPolicy(clientEncryptionPolicy);
        encryptionCosmosAsyncDatabase.createEncryptionContainerAsync(properties).block();
        encryptionCosmosAsyncContainer = encryptionCosmosAsyncDatabase.getEncryptedCosmosAsyncContainer(containerId);
        metadata1 = new EncryptionKeyWrapMetadata("key1", "tempmetadata1");
        metadata2 = new EncryptionKeyWrapMetadata("key2", "tempmetadata2");
        clientEncryptionKeyProperties1 = encryptionCosmosAsyncDatabase.createClientEncryptionKey("key1",
            CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256, metadata1).block().getProperties();
        clientEncryptionKeyProperties2 = encryptionCosmosAsyncDatabase.createClientEncryptionKey("key2",
            CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256, metadata2).block().getProperties();

        cosmosAsyncClientEncryptionKey1 = encryptionCosmosAsyncDatabase.getClientEncryptionKey("key1");
        cosmosAsyncClientEncryptionKey2 = encryptionCosmosAsyncDatabase.getClientEncryptionKey("key2");
    }

    @BeforeClass(groups = "encryption")
    public void beforeClass() {
        TestUtils.initialized();
    }

    @AfterClass(groups = {"encryption"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        assertThat(this.client).isNotNull();
        this.client.close();
    }

    @Test(groups = {"encryption"}/*, timeOut = TIMEOUT*/)
    public void createItemEncrypt_readItemDecrypt() throws Exception {
        Pojo properties = getItem(UUID.randomUUID().toString());
        CosmosItemResponse<Pojo> itemResponse = encryptionCosmosAsyncContainer.createItem(properties,
            new PartitionKey(properties.mypk), new CosmosItemRequestOptions()).block();
        assertThat(itemResponse.getRequestCharge()).isGreaterThan(0);
        Pojo responseItem = itemResponse.getItem();
        validateWriteResponseIsValid(properties, responseItem);

        Pojo readItem = encryptionCosmosAsyncContainer.readItem(properties.id, new PartitionKey(properties.mypk),
            new CosmosItemRequestOptions(), Pojo.class).block().getItem();
        validateReadResponseIsValid(properties, readItem);
    }

    @Test(groups = {"encryption"}, timeOut = TIMEOUT)
    public void upsertItem_readItem() throws Exception {
        Pojo properties = getItem(UUID.randomUUID().toString());
        CosmosItemResponse<Pojo> itemResponse = encryptionCosmosAsyncContainer.upsertItem(properties,
            new PartitionKey(properties.mypk), new CosmosItemRequestOptions()).block();
        assertThat(itemResponse.getRequestCharge()).isGreaterThan(0);
        Pojo responseItem = itemResponse.getItem();
        validateWriteResponseIsValid(properties, responseItem);

        Pojo readItem = encryptionCosmosAsyncContainer.readItem(properties.id, new PartitionKey(properties.mypk),
            new CosmosItemRequestOptions(), Pojo.class).block().getItem();
        validateReadResponseIsValid(properties, readItem);
    }


    private void validateWriteResponseIsValid(Pojo originalItem, Pojo result) {
        assertThat(result.sensitive).isEqualTo(originalItem.sensitive);
        assertThat(result.id).isEqualTo(originalItem.id);
        assertThat(result.mypk).isEqualTo(originalItem.mypk);
        assertThat(result.nonSensitive).isEqualTo(originalItem.nonSensitive);
    }

    private void validateReadResponseIsValid(Pojo originalItem, Pojo result) {
        assertThat(result.id).isEqualTo(originalItem.id);
        assertThat(result.mypk).isEqualTo(originalItem.mypk);
        assertThat(result.nonSensitive).isEqualTo(originalItem.nonSensitive);
        assertThat(result.sensitive).isEqualTo(originalItem.sensitive);
    }

    private void validateQueryResponseIsValid(Pojo originalItem, Pojo result) {
        assertThat(result.id).isEqualTo(originalItem.id);
        assertThat(result.mypk).isEqualTo(originalItem.mypk);
        assertThat(result.nonSensitive).isEqualTo(originalItem.nonSensitive);
        assertThat(result.sensitive).isNull();
    }

    @Test(groups = {"encryption"}, timeOut = TIMEOUT)
    public void readAllItems() throws Exception {
        Pojo properties = getItem(UUID.randomUUID().toString());
        CosmosItemResponse<Pojo> itemResponse = container.createItem(properties);

        CosmosQueryRequestOptions CosmosQueryRequestOptions = new CosmosQueryRequestOptions();

        CosmosPagedIterable<Pojo> feedResponseIterator3 =
            container.readAllItems(CosmosQueryRequestOptions, Pojo.class);
        assertThat(feedResponseIterator3.iterator().hasNext()).isTrue();
    }


    @Test(groups = {"encryption"}, timeOut = TIMEOUT)
    public void queryItems() throws Exception {
        Pojo properties = getItem(UUID.randomUUID().toString());
        CosmosItemResponse<Pojo> itemResponse = encryptionCosmosAsyncContainer.createItem(properties,
            new PartitionKey(properties.mypk), new CosmosItemRequestOptions()).block();
        assertThat(itemResponse.getRequestCharge()).isGreaterThan(0);
        Pojo responseItem = itemResponse.getItem();
        validateWriteResponseIsValid(properties, responseItem);

        String query = String.format("SELECT * from c where c.id = '%s'", properties.id);
        CosmosQueryRequestOptions CosmosQueryRequestOptions = new CosmosQueryRequestOptions();



        SqlQuerySpec querySpec = new SqlQuerySpec(query);
        CosmosPagedFlux<Pojo> feedResponseIterator =
            encryptionCosmosAsyncContainer.queryItems(querySpec, CosmosQueryRequestOptions, Pojo.class);
        List<Pojo> feedResponse = feedResponseIterator.byPage().blockFirst().getResults();
        assertThat(feedResponse.size()).isEqualTo(1);
        validateWriteResponseIsValid(feedResponse.get(0), responseItem);
    }

    @Test(groups = {"encryption"}, timeOut = TIMEOUT)
    public void queryItemsWithContinuationTokenAndPageSize() throws Exception {
        List<String> actualIds = new ArrayList<>();
        Pojo properties = getItem(UUID.randomUUID().toString());
        container.createItem(properties);
        actualIds.add(properties.id);
        properties = getItem(UUID.randomUUID().toString());
        container.createItem(properties);
        actualIds.add(properties.id);
        properties = getItem(UUID.randomUUID().toString());
        container.createItem(properties);
        actualIds.add(properties.id);


        String query = String.format("SELECT * from c where c.id in ('%s', '%s', '%s')", actualIds.get(0),
            actualIds.get(1), actualIds.get(2));
        CosmosQueryRequestOptions CosmosQueryRequestOptions = new CosmosQueryRequestOptions();
        String continuationToken = null;
        int pageSize = 1;

        int initialDocumentCount = 3;
        int finalDocumentCount = 0;

        CosmosPagedIterable<Pojo> feedResponseIterator1 =
            container.queryItems(query, CosmosQueryRequestOptions, Pojo.class);

        do {
            Iterable<FeedResponse<Pojo>> feedResponseIterable =
                feedResponseIterator1.iterableByPage(continuationToken, pageSize);
            for (FeedResponse<Pojo> fr : feedResponseIterable) {
                int resultSize = fr.getResults().size();
                assertThat(resultSize).isEqualTo(pageSize);
                finalDocumentCount += fr.getResults().size();
                continuationToken = fr.getContinuationToken();
            }
        } while (continuationToken != null);

        assertThat(finalDocumentCount).isEqualTo(initialDocumentCount);

    }


    private Pojo getItem(String documentId) {
        final String uuid = UUID.randomUUID().toString();

        Pojo pojo = new Pojo();
        pojo.id = uuid;
        pojo.mypk = uuid;
        pojo.nonSensitive = UUID.randomUUID().toString();
        pojo.sensitive = "sdhjgcweuydNAVEEN";

        return pojo;
    }

    private void validateItemResponse(Pojo containerProperties,
                                      CosmosItemResponse<Pojo> createResponse) {
        // Basic validation
        assertThat(BridgeInternal.getProperties(createResponse).getId()).isNotNull();
        assertThat(BridgeInternal.getProperties(createResponse).getId())
            .as("check Resource Id")
            .isEqualTo(containerProperties.id);
    }

    public static class Pojo {
        public String id;
        @JsonProperty
        public String mypk;
        @JsonProperty
        public String sensitive;
        @JsonProperty
        public String nonSensitive;
    }

    private DataEncryptionKey createDataEncryptionKey() throws Exception {
        return TestUtils.createDataEncryptionKey();
    }

    class TestEncryptionKeyStoreProvider extends EncryptionKeyStoreProvider {
        Map<String, Integer> keyInfo = new HashMap<>();

        TestEncryptionKeyStoreProvider() {
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
}
