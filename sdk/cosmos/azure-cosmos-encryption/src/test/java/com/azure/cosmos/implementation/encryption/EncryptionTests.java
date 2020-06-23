// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.encryption;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosBridgeInternal;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.implementation.DatabaseForTest;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.encryption.api.CosmosEncryptionAlgorithm;
import com.azure.cosmos.implementation.encryption.api.EncryptionOptions;
import com.azure.cosmos.implementation.guava25.collect.ImmutableList;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.rx.TestSuiteBase;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class EncryptionTests extends TestSuiteBase {

    private static EncryptionKeyWrapMetadata metadata1 = new EncryptionKeyWrapMetadata("metadata1");
    private static EncryptionKeyWrapMetadata metadata2 = new EncryptionKeyWrapMetadata("metadata2");
    private final static String metadataUpdateSuffix = "updated";
    private static Duration cacheTTL = Duration.ofDays(1);

    private final String databaseForTestId = DatabaseForTest.generateId();
    private final String itemContainerId = UUID.randomUUID().toString();
    private final String keyContainerId = UUID.randomUUID().toString();

    private final String dekId = "mydek";

    private static CosmosAsyncClient client;

    private static CosmosAsyncDatabase databaseCore;
    private static DataEncryptionKeyProperties dekProperties;
    //    private static ContainerCore itemContainerCore;
    private static CosmosAsyncContainer itemContainer;
    private static CosmosAsyncContainer keyContainer;
    private static CosmosDataEncryptionKeyProvider dekProvider;
//    private static TestEncryptor encryptor;

    @Factory(dataProvider = "clientBuilders")
    public EncryptionTests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);

        client = clientBuilder.buildAsyncClient();
    }

    @BeforeTest(groups = "emulator")
    public void beforeTest() {
        TestKeyWrapProvider keyWrapProvider = new TestKeyWrapProvider();
        dekProvider = new CosmosDataEncryptionKeyProvider(keyWrapProvider);
        client = CosmosBridgeInternal.setDateKeyProvider(getClientBuilder(), dekProvider).buildAsyncClient();

//        EncryptionTests.encryptor = new TestEncryptor(EncryptionTests.dekProvider);

//        EncryptionTests.client = EncryptionTests.GetClient(EncryptionTests.encryptor);

        client.createDatabaseIfNotExists(databaseForTestId).block();
        databaseCore = client.getDatabase(databaseForTestId);
        databaseCore.createContainerIfNotExists(keyContainerId, "/id", ThroughputProperties.createManualThroughput(400)).block();
        keyContainer = databaseCore.getContainer(keyContainerId);
        databaseCore.createContainerIfNotExists(itemContainerId, "/PK", ThroughputProperties.createManualThroughput(400)).block();
        itemContainer = databaseCore.getContainer(itemContainerId);


        EncryptionTests.dekProperties = EncryptionTests.createDek(EncryptionTests.dekProvider, dekId);
    }

    @BeforeClass(groups = "emulator")
    public void beforeClass() {
        TestUtils.initialized();
    }

    @AfterMethod(groups = "emulator")
    public void afterTest() {
        safeClose(client);
    }

    @AfterClass(groups = "emulator")
    public void afterClass() {
        safeDeleteDatabase(databaseCore);
    }


    static public class TestDoc {

        @JsonProperty("id")
        public String id;
        @JsonProperty("PK")
        public String pk;
        @JsonProperty("NonSensitive")
        public String nonSensitive;
        @JsonProperty("Sensitive")
        public String sensitive;

        public TestDoc() {
        }

        public TestDoc(TestDoc other) {
            this.id = other.id;
            this.pk = other.pk;
            this.nonSensitive = other.nonSensitive;
            this.sensitive = other.sensitive;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TestDoc testDoc = (TestDoc) o;
            return Objects.equals(id, testDoc.id) &&
                Objects.equals(pk, testDoc.pk) &&
                Objects.equals(nonSensitive, testDoc.nonSensitive) &&
                Objects.equals(sensitive, testDoc.sensitive);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, pk, nonSensitive, sensitive);
        }
    }

    @Test(groups = "emulator")
    public void encryptionCreateDek() {
        String dekId = "anotherDek";
        DataEncryptionKeyProperties dekProperties = EncryptionTests.createDek(EncryptionTests.dekProvider, dekId);

        assertThat(dekProperties).isNotNull();
        assertThat(dekProperties.createdTime).isNotNull();
        assertThat(dekProperties.lastModified).isNotNull();
        assertThat(dekProperties.selfLink).isNotNull();
        assertThat(dekProperties).isNotNull();

        assertThat(dekProperties.resourceId).isNotNull();

        assertThat(dekProperties.lastModified).isEqualTo(dekProperties.lastModified);

        assertThat(
            new EncryptionKeyWrapMetadata(EncryptionTests.metadata1.value + EncryptionTests.metadataUpdateSuffix)).isEqualTo(
            dekProperties.encryptionKeyWrapMetadata);

        // Use different DEK provider to avoid (unintentional) cache impact
        CosmosDataEncryptionKeyProvider dekProvider = new CosmosDataEncryptionKeyProvider(new TestKeyWrapProvider());


        dekProvider.initialize(databaseCore, EncryptionTests.keyContainer.getId());

        DataEncryptionKeyProperties readProperties = dekProvider.getDataEncryptionKeyContainer().readDataEncryptionKeyAsync(dekId, null).block().getItem();
        assertThat(dekProperties).isEqualTo(readProperties);
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void createItemEncrypt_readItemDecrypt() throws Exception {
        CosmosItemRequestOptions requestOptions = new CosmosItemRequestOptions();
        EncryptionOptions encryptionOptions = new EncryptionOptions();
        encryptionOptions.setPathsToEncrypt(ImmutableList.of("/Sensitive"));

        encryptionOptions.setDataEncryptionKeyId(dekId);
        encryptionOptions.setEncryptionAlgorithm(CosmosEncryptionAlgorithm.AEAes256CbcHmacSha256Randomized);
        ModelBridgeInternal.setEncryptionOptions(requestOptions, encryptionOptions);

        TestDoc properties = getItem(UUID.randomUUID().toString());
        CosmosItemResponse<TestDoc> itemResponse = itemContainer.createItem(properties, requestOptions).block();
        assertThat(itemResponse.getRequestCharge()).isGreaterThan(0);

        TestDoc responseItem = itemResponse.getItem();
        validateWriteResponseIsValid(properties, responseItem);

        TestDoc readItem = itemContainer.readItem(properties.id, new PartitionKey(properties.pk), requestOptions, TestDoc.class).block().getItem();
        validateReadResponseIsValid(properties, readItem);
    }

    private void validateWriteResponseIsValid(TestDoc originalItem, TestDoc result) {
        assertThat(result.sensitive).isNull();
        assertThat(result.id).isEqualTo(originalItem.id);
        assertThat(result.pk).isEqualTo(originalItem.pk);
        assertThat(result.nonSensitive).isEqualTo(originalItem.nonSensitive);
    }

    private void validateReadResponseIsValid(TestDoc originalItem, TestDoc result) {
        assertThat(result.id).isEqualTo(originalItem.id);
        assertThat(result.pk).isEqualTo(originalItem.pk);
        assertThat(result.nonSensitive).isEqualTo(originalItem.nonSensitive);
        assertThat(result.sensitive).isEqualTo(originalItem.sensitive);
    }

    private void validateQueryResponseIsValid(TestDoc originalItem, TestDoc result) {
        assertThat(result.id).isEqualTo(originalItem.id);
        assertThat(result.pk).isEqualTo(originalItem.pk);
        assertThat(result.nonSensitive).isEqualTo(originalItem.nonSensitive);
        assertThat(result.sensitive).isNull();
    }

    private TestDoc getItem(String documentId) {
        final String uuid = UUID.randomUUID().toString();

        TestDoc pojo = new TestDoc();
        pojo.id = uuid;
        pojo.pk = uuid;
        pojo.nonSensitive = UUID.randomUUID().toString();
        pojo.sensitive = UUID.randomUUID().toString();

        return pojo;
    }

    private static DataEncryptionKeyProperties createDek(CosmosDataEncryptionKeyProvider dekProvider, String dekId) {
        CosmosItemResponse<DataEncryptionKeyProperties> dekResponse = dekProvider.getDataEncryptionKeyContainer().createDataEncryptionKeyAsync(
            dekId,
            CosmosEncryptionAlgorithm.AEAes256CbcHmacSha256Randomized,
            EncryptionTests.metadata1, null).block();

        assertThat(dekResponse.getRequestCharge()).isGreaterThan(0);
        assertThat(dekResponse.getResponseHeaders().get(HttpConstants.HttpHeaders.E_TAG)).isNotNull();

        DataEncryptionKeyProperties dekProperties = dekResponse.getItem();
        assertThat(dekResponse.getResponseHeaders().get(HttpConstants.HttpHeaders.E_TAG)).isEqualTo(dekProperties.eTag);
        assertThat(dekId).isEqualTo(dekProperties.id);
        return dekProperties;
    }

    private class TestKeyWrapProvider implements EncryptionKeyWrapProvider {
        public EncryptionKeyUnwrapResult unwrapKey(byte[] wrappedKey, EncryptionKeyWrapMetadata metadata) {
            int moveBy = StringUtils.equals(metadata.value, EncryptionTests.metadata1.value + EncryptionTests.metadataUpdateSuffix) ? 1 : 2;

            for (int i = 0; i < wrappedKey.length; i++) {
                wrappedKey[i] = (byte) (wrappedKey[i] - moveBy);
            }

            return new EncryptionKeyUnwrapResult(wrappedKey, EncryptionTests.cacheTTL);
        }

        public EncryptionKeyWrapResult wrapKey(byte[] key, EncryptionKeyWrapMetadata metadata) {
            EncryptionKeyWrapMetadata responseMetadata = new EncryptionKeyWrapMetadata(metadata.value + EncryptionTests.metadataUpdateSuffix);
            int moveBy = StringUtils.equals(metadata.value, EncryptionTests.metadata1.value) ? 1 : 2;

            for (int i = 0; i < key.length; i++) {
                key[i] = (byte) (key[i] + moveBy);
            }

            return new EncryptionKeyWrapResult(key, responseMetadata);
        }
    }
}
