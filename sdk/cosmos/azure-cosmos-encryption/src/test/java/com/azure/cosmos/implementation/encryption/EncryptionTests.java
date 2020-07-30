// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.encryption;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.encryption.EncryptionCosmosAsyncContainer;
import com.azure.cosmos.encryption.EncryptionItemRequestOptions;
import com.azure.cosmos.encryption.EncryptionKeyUnwrapResult;
import com.azure.cosmos.encryption.EncryptionKeyWrapMetadata;
import com.azure.cosmos.encryption.EncryptionKeyWrapProvider;
import com.azure.cosmos.encryption.EncryptionKeyWrapResult;
import com.azure.cosmos.encryption.Encryptor;
import com.azure.cosmos.encryption.WithEncryption;
import com.azure.cosmos.implementation.DatabaseForTest;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.encryption.CosmosEncryptionAlgorithm;
import com.azure.cosmos.encryption.DataEncryptionKey;
import com.azure.cosmos.encryption.DataEncryptionKeyProvider;
import com.azure.cosmos.encryption.EncryptionOptions;
import com.azure.cosmos.implementation.guava25.collect.ImmutableList;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.rx.TestSuiteBase;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class EncryptionTests extends TestSuiteBase {

    private static EncryptionKeyWrapMetadata metadata1 = new EncryptionKeyWrapMetadata("metadata1");
    private static EncryptionKeyWrapMetadata metadata2 = new EncryptionKeyWrapMetadata("metadata2");
    private final static String metadataUpdateSuffix = "updated";
    private static Duration cacheTTL = Duration.ofDays(1);
    private static TestEncryptor encryptor;
    private static EncryptionCosmosAsyncContainer encryptionContainer;

    private final String databaseForTestId = DatabaseForTest.generateId();
    private final String itemContainerId = UUID.randomUUID().toString();
    private final String keyContainerId = UUID.randomUUID().toString();

    private static final String dekId = "mydek";

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
    }

    @BeforeTest(groups = { "encryption" })
    public void beforeTest() {
        dekProvider = new CosmosDataEncryptionKeyProvider(new TestKeyWrapProvider());

        EncryptionTests.encryptor = new TestEncryptor(EncryptionTests.dekProvider);
        client = getClientBuilder().buildAsyncClient();


        client.createDatabaseIfNotExists(databaseForTestId).block();
        databaseCore = client.getDatabase(databaseForTestId);
        databaseCore.createContainerIfNotExists(keyContainerId, "/id",
            ThroughputProperties.createManualThroughput(400)).block();
        keyContainer = databaseCore.getContainer(keyContainerId);
        databaseCore.createContainerIfNotExists(itemContainerId, "/PK",
            ThroughputProperties.createManualThroughput(400)).block();
        itemContainer = databaseCore.getContainer(itemContainerId);

        dekProvider.initialize(databaseCore, EncryptionTests.keyContainer.getId());

        EncryptionTests.encryptionContainer = WithEncryption.withEncryptor(EncryptionTests.itemContainer, encryptor);
        EncryptionTests.dekProperties = EncryptionTests.createDek(EncryptionTests.dekProvider, dekId);
    }

    @BeforeClass(groups = { "encryption" })
    public void beforeClass() {
        TestUtils.initialized();
        client = getClientBuilder().buildAsyncClient();
    }

    @AfterMethod(groups = { "encryption" })
    public void afterTest() {
        safeClose(client);
    }

    @AfterClass(groups = { "encryption" })
    public void afterClass() {
        safeDeleteDatabase(databaseCore);
    }

    static public class TestDoc {
        public static List<String> PathsToEncrypt = ImmutableList.of("/Sensitive");
        public static List<String> AllPath = ImmutableList.of("/Sensitive", "/id", "/PK", "/NonSensitive");

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
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            TestDoc testDoc = (TestDoc) o;
            return Objects.equals(id, testDoc.id) &&
                Objects.equals(pk, testDoc.pk) &&
                Objects.equals(nonSensitive, testDoc.nonSensitive) &&
                Objects.equals(sensitive, testDoc.sensitive);
        }

        public static TestDoc Create() {
            return TestDoc.Create(null);
        }

        public static TestDoc Create(String partitionKey) {
            TestDoc testDoc = new TestDoc();
            testDoc.id = UUID.randomUUID().toString();
            testDoc.pk = partitionKey == null ? UUID.randomUUID().toString() : partitionKey;
            testDoc.nonSensitive = UUID.randomUUID().toString();
            testDoc.sensitive = UUID.randomUUID().toString();

            return testDoc;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, pk, nonSensitive, sensitive);
        }
    }

    @Test(groups = { "encryption" })
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

        DataEncryptionKeyProperties readProperties =
            dekProvider.getDataEncryptionKeyContainer().readDataEncryptionKeyAsync(dekId, null).block().getItem();
        assertThat(dekProperties).isEqualTo(readProperties);
    }

    @Test(enabled = false)
    public void EncryptionRewrapDek() {
    }

    @Test(enabled = false)
    public void EncryptionDekReadFeed() {
    }

    @Test(groups = { "encryption" }, timeOut = TIMEOUT)
    public void EncryptionCreateItemWithoutEncryptionOptions() {
        TestDoc testDoc = TestDoc.Create();

        CosmosItemResponse<TestDoc> createResponse = EncryptionTests.encryptionContainer.createItem(testDoc,
            new PartitionKey(testDoc.pk), null).block();

        assertThat(createResponse.getStatusCode()).isEqualTo(201);
        assertThat(createResponse.getItem()).isEqualTo(testDoc);
    }

    @Test(groups = { "encryption" }, timeOut = TIMEOUT)
    public void EncryptionCreateItemWithNullEncryptionOptions() {
        TestDoc testDoc = TestDoc.Create();
        CosmosItemResponse<TestDoc> createResponse = EncryptionTests.encryptionContainer.createItem(
            testDoc,
            new PartitionKey(testDoc.pk),
            new EncryptionItemRequestOptions()).block();

        assertThat(createResponse.getStatusCode()).isEqualTo(201);
        assertThat(createResponse.getItem()).isEqualTo(testDoc);
    }

    @Test(groups = { "encryption" }, timeOut = TIMEOUT)
    public void EncryptionCreateItemWithoutPartitionKey() {
        TestDoc testDoc = TestDoc.Create();
        try {
            // TODO: moderakh invoke this without passing null PK
            EncryptionTests.encryptionContainer.createItem(
                testDoc,
                null,
                EncryptionTests.GetRequestOptions(EncryptionTests.dekId, TestDoc.PathsToEncrypt));
            fail("CreateItem should've failed because PartitionKey was not provided.");
        } catch (Exception ex) {
            assertThat(ex.getMessage()).isEqualTo("partitionKey cannot be null for operations using "
                + "EncryptionContainer.");
        }
    }

    @Test(groups = { "encryption" }, timeOut = TIMEOUT)
    public void EncryptionFailsWithUnknownDek() {
    }

    @Test(groups = { "encryption" }, timeOut = TIMEOUT)
    public void EncryptionCreateItem() {
        TestDoc testDoc = EncryptionTests.CreateItemAsync(EncryptionTests.encryptionContainer, EncryptionTests.dekId,
            TestDoc.PathsToEncrypt).getItem();
        EncryptionTests.VerifyItemByReadAsync(EncryptionTests.encryptionContainer, testDoc);
        VerifyDataIsEncrypted(testDoc.id, new PartitionKey(testDoc.pk));

        TestDoc expectedDoc = new TestDoc(testDoc);

        EncryptionTests.ValidateQueryResultsAsync(
            EncryptionTests.encryptionContainer,
            "SELECT * FROM c",
            expectedDoc);

        EncryptionTests.ValidateQueryResultsAsync(
            EncryptionTests.encryptionContainer,
            String.format(
                "SELECT * FROM c where c.PK = '%s' and c.id = '%s' and c.NonSensitive = '%s'",
                expectedDoc.pk,
                expectedDoc.id,
                expectedDoc.nonSensitive),
            expectedDoc);

        EncryptionTests.ValidateQueryResultsAsync(
            EncryptionTests.encryptionContainer,
            String.format("SELECT * FROM c where c.Sensitive = '%s'", testDoc.sensitive),
            null);

        EncryptionTests.ValidateQueryResultsAsync(
            EncryptionTests.encryptionContainer,
            new SqlQuerySpec(
                "select * from c where c.id = @theId and c.PK = @thePK",
                new SqlParameter("@theId", expectedDoc.id),
                new SqlParameter("@thePK", expectedDoc.pk)),
            expectedDoc);

        expectedDoc.sensitive = null;

        EncryptionTests.ValidateQueryResultsAsync(
            EncryptionTests.encryptionContainer,
            "SELECT c.id, c.PK, c.Sensitive, c.NonSensitive FROM c",
            expectedDoc);

        EncryptionTests.ValidateQueryResultsAsync(
            EncryptionTests.encryptionContainer,
            "SELECT c.id, c.PK, c.NonSensitive FROM c",
            expectedDoc);
    }

    // EncryptionChangeFeedDecryptionSuccessful

    @Test(groups = { "encryption" }, timeOut = TIMEOUT)
    public void EncryptionChangeFeedDecryptionSuccessful() {

    }

    @Test(groups = { "encryption" }, timeOut = TIMEOUT)
    public void EncryptionHandleDecryptionFailure() {
    }

    @Test(groups = { "encryption" }, timeOut = TIMEOUT)
    public void EncryptionDecryptQueryResultMultipleDocs() {
        TestDoc testDoc1 =  EncryptionTests.CreateItemAsync(EncryptionTests.encryptionContainer, EncryptionTests.dekId, TestDoc.PathsToEncrypt).getItem();
        TestDoc testDoc2 =  EncryptionTests.CreateItemAsync(EncryptionTests.encryptionContainer, EncryptionTests.dekId, TestDoc.PathsToEncrypt).getItem();
        
        String query = String.format("SELECT * FROM c WHERE c.PK in ('%s', '%s')", testDoc1.pk, testDoc2.pk);
        EncryptionTests.ValidateQueryResultsMultipleDocumentsAsync(EncryptionTests.encryptionContainer, testDoc1, testDoc2, query);

        // ORDER BY query
        query = query + " ORDER BY c._ts";
        EncryptionTests.ValidateQueryResultsMultipleDocumentsAsync(EncryptionTests.encryptionContainer, testDoc1, testDoc2, query);
    }

    @Test(groups = { "encryption" }, timeOut = TIMEOUT)
    public void EncryptionDecryptQueryResultMultipleEncryptedProperties() {

        TestDoc testDoc = EncryptionTests.CreateItemAsync(
            EncryptionTests.encryptionContainer,
            EncryptionTests.dekId,
            ImmutableList.of("/Sensitive", "/NonSensitive")).getItem();

        TestDoc expectedDoc = new TestDoc(testDoc);

        EncryptionTests.ValidateQueryResultsAsync(
            EncryptionTests.encryptionContainer,
            "SELECT * FROM c",
            expectedDoc);
    }

    @Test(groups = { "encryption" }, timeOut = TIMEOUT)
    public void EncryptionDecryptQueryValueResponse() {
        EncryptionTests.CreateItemAsync(EncryptionTests.encryptionContainer,
            EncryptionTests.dekId,
            TestDoc.PathsToEncrypt);
        String query = "SELECT VALUE COUNT(1) FROM c";

        itemContainer.queryItems(new SqlQuerySpec(query),
            new CosmosQueryRequestOptions(),
            Integer.class).collectList().block();

        EncryptionTests.ValidateQueryResponseAsync(EncryptionTests.encryptionContainer, query, Integer.class);
    }

    @Test(groups = { "encryption" }, timeOut = TIMEOUT)
    public void EncryptionDecryptGroupByQueryResultTest() {

        String partitionKey = UUID.randomUUID().toString();

        EncryptionTests.CreateItemAsync(EncryptionTests.encryptionContainer, EncryptionTests.dekId,
            TestDoc.PathsToEncrypt, partitionKey);
        EncryptionTests.CreateItemAsync(EncryptionTests.encryptionContainer, EncryptionTests.dekId,
            TestDoc.PathsToEncrypt, partitionKey);

        String query = String.format("SELECT COUNT(c.Id), c.PK " +
            "FROM c WHERE c.PK = '%s' " +
            "GROUP BY c.PK ", partitionKey);

        EncryptionTests.ValidateQueryResponseAsync(EncryptionTests.encryptionContainer, query, ObjectNode.class);
    }

    @Test(groups = { "encryption" }, timeOut = TIMEOUT)
    public void EncryptionStreamIteratorValidation() {
    }

    @Test(groups = { "encryption" }, timeOut = TIMEOUT)
    public void EncryptionRudItem() {
        TestDoc testDoc = EncryptionTests.UpsertItemAsync(
            EncryptionTests.encryptionContainer,
            TestDoc.Create(),
            EncryptionTests.dekId,
            TestDoc.PathsToEncrypt,
            ResponseStatusCode.CREATED).getItem();

        EncryptionTests.VerifyItemByReadAsync(EncryptionTests.encryptionContainer, testDoc);

        testDoc.nonSensitive = UUID.randomUUID().toString();
        testDoc.sensitive = UUID.randomUUID().toString();

        CosmosItemResponse<TestDoc> upsertResponse = EncryptionTests.UpsertItemAsync(
            EncryptionTests.encryptionContainer,
            testDoc,
            EncryptionTests.dekId,
            TestDoc.PathsToEncrypt,
            ResponseStatusCode.OK);

        TestDoc updatedDoc = upsertResponse.getItem();

        EncryptionTests.VerifyItemByReadAsync(EncryptionTests.encryptionContainer, updatedDoc);

        updatedDoc.nonSensitive = UUID.randomUUID().toString();
        updatedDoc.sensitive = UUID.randomUUID().toString();

        // TODO: replace
        TestDoc replacedDoc = EncryptionTests.ReplaceItemAsync(
            EncryptionTests.encryptionContainer,
            updatedDoc,
            EncryptionTests.dekId,
            TestDoc.PathsToEncrypt,
            upsertResponse.getETag()
        ).getItem();


        EncryptionTests.VerifyItemByReadAsync(EncryptionTests.encryptionContainer, replacedDoc);

        EncryptionTests.DeleteItemAsync(EncryptionTests.encryptionContainer, replacedDoc);
    }

    @Test(groups = { "encryption" }, timeOut = TIMEOUT)
    public void EncryptionResourceTokenAuthRestricted() {
    }

    @Test(groups = { "encryption" }, timeOut = TIMEOUT)
    public void EncryptionResourceTokenAuthAllowed() {
    }

    @Test(groups = { "encryption" }, timeOut = TIMEOUT)
    public void EncryptionRestrictedProperties() {
        try {
            EncryptionTests.CreateItemAsync(EncryptionTests.encryptionContainer, EncryptionTests.dekId,
                ImmutableList.of("/id"));
            fail("Expected item creation with id specified to be encrypted to fail.");
        } catch (CosmosException ex) {
            // when (ex.StatusCode == HttpStatusCode.BadRequest)
            assertThat(ex.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.BADREQUEST);
        }

        try {
            EncryptionTests.CreateItemAsync(EncryptionTests.encryptionContainer, EncryptionTests.dekId,
                ImmutableList.of("/PK"));
            fail("Expected item creation with PK specified to be encrypted to fail.");
        } catch (CosmosException ex) {
            assertThat(ex.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.BADREQUEST);
        }
    }

    @Test(groups = { "encryption" }, timeOut = TIMEOUT)
    public void EncryptionBulkCrud() {
    }

    @Test(groups = { "encryption" }, timeOut = TIMEOUT)
    public void EncryptionTransactionBatchCrud() {
    }

    @Test(groups = { "encryption" }, timeOut = TIMEOUT)
    public void createItemEncrypt_readItemDecrypt() throws Exception {
        EncryptionItemRequestOptions requestOptions = new EncryptionItemRequestOptions();
        EncryptionOptions encryptionOptions = new EncryptionOptions();
        encryptionOptions.setPathsToEncrypt(ImmutableList.of("/Sensitive"));

        encryptionOptions.setDataEncryptionKeyId(dekId);
        encryptionOptions.setEncryptionAlgorithm(CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256_RANDOMIZED);
        requestOptions.setEncryptionOptions(encryptionOptions);

        TestDoc properties = getItem(UUID.randomUUID().toString());
        CosmosItemResponse<TestDoc> itemResponse = encryptionContainer.createItem(properties,
            new PartitionKey(properties.pk), requestOptions).block();
        assertThat(itemResponse.getRequestCharge()).isGreaterThan(0);

        TestDoc responseItem = itemResponse.getItem();
        validateWriteResponseIsValid(properties, responseItem);

        TestDoc readItem = encryptionContainer.readItem(properties.id, new PartitionKey(properties.pk),
            requestOptions, TestDoc.class).block().getItem();
        validateReadResponseIsValid(properties, readItem);

        TestDoc readWithoutDecryption = itemContainer.readItem(properties.id, new PartitionKey(properties.pk),
            requestOptions, TestDoc.class).block().getItem();

        assertThat(readWithoutDecryption.sensitive).isNull();
    }

    private void validateWriteResponseIsValid(TestDoc originalItem, TestDoc result) {
        assertThat(result.sensitive).isEqualTo(originalItem.sensitive);
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

    private static void ValidateQueryResultsMultipleDocumentsAsync(
        EncryptionCosmosAsyncContainer container,
        TestDoc testDoc1,
        TestDoc testDoc2,
        String query)
    {
        ValidateQueryResultsMultipleDocumentsAsync(container, testDoc1, testDoc2, query, null);
    }

    private static void ValidateQueryResultsMultipleDocumentsAsync(
        EncryptionCosmosAsyncContainer container,
        TestDoc testDoc1,
        TestDoc testDoc2,
        String query,
        CosmosQueryRequestOptions requestOptions)
    {
        List<TestDoc> readDocs =
            container.queryItems(new SqlQuerySpec(query), requestOptions, TestDoc.class).collectList().block();


        assertThat(readDocs.size()).isEqualTo(2);
        assertThat(readDocs).containsExactlyInAnyOrder(testDoc1, testDoc2);
    }

    private static <T> void ValidateQueryResponseAsync(EncryptionCosmosAsyncContainer container,
                                                       String query,
                                                       Class<T> classType) {
        container.queryItems(new SqlQuerySpec(query),
            new CosmosQueryRequestOptions(),
            classType).collectList().block();
    }

    private static void ValidateQueryResultsAsync(
        EncryptionCosmosAsyncContainer container,
        String query,
        TestDoc expectedDoc) {
        ValidateQueryResultsAsync(container, new SqlQuerySpec(query), expectedDoc);
    }

    private static void ValidateQueryResultsAsync(
        EncryptionCosmosAsyncContainer container,
        SqlQuerySpec query,
        TestDoc expectedDoc) {
        CosmosQueryRequestOptions requestOptions = expectedDoc != null
            ? new CosmosQueryRequestOptions().setPartitionKey(new PartitionKey(expectedDoc.pk)) : null;


        CosmosPagedFlux<TestDoc> queryResponseIterator = container.queryItems(query, requestOptions, TestDoc.class);
        List<TestDoc> results = queryResponseIterator.collectList().block();

        if (expectedDoc != null) {
            assertThat(results.size()).isEqualTo(1);
            assertThat(results.get(0)).isEqualTo(expectedDoc);

        } else {
            assertThat(results.size()).isEqualTo(0);
        }
    }

    private static void VerifyDataIsEncrypted(String id, PartitionKey partitionKey) {

        TestDoc item = itemContainer.readItem(id, partitionKey, TestDoc.class).block().getItem();
        assertThat(item.sensitive).isEqualTo(null);
        assertThat(item.nonSensitive).isNotNull();
    }

    private static void VerifyItemByReadAsync(EncryptionCosmosAsyncContainer container, TestDoc testDoc) {
        VerifyItemByReadAsync(container, testDoc, null);
    }

    private static void VerifyItemByReadAsync(EncryptionCosmosAsyncContainer container, TestDoc testDoc,
                                              CosmosItemRequestOptions requestOptions) {
        CosmosItemResponse<TestDoc> readResponse = container.readItem(testDoc.id, new PartitionKey(testDoc.pk),
            requestOptions, TestDoc.class).block();

        assertThat(readResponse.getStatusCode()).isEqualTo(200);
        assertThat(readResponse.getItem()).isEqualTo(testDoc);
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
        CosmosItemResponse<DataEncryptionKeyProperties> dekResponse =
            dekProvider.getDataEncryptionKeyContainer().createDataEncryptionKeyAsync(
                dekId,
                CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256_RANDOMIZED,
                EncryptionTests.metadata1, null).block();

        assertThat(dekResponse.getRequestCharge()).isGreaterThan(0);
        assertThat(dekResponse.getResponseHeaders().get(HttpConstants.HttpHeaders.E_TAG)).isNotNull();

        DataEncryptionKeyProperties dekProperties = dekResponse.getItem();
        assertThat(dekResponse.getResponseHeaders().get(HttpConstants.HttpHeaders.E_TAG)).isEqualTo(dekProperties.eTag);
        assertThat(dekId).isEqualTo(dekProperties.id);
        return dekProperties;
    }


    private static EncryptionItemRequestOptions GetRequestOptions(
        String dekId,
        List<String> pathsToEncrypt) {
        return GetRequestOptions(dekId, pathsToEncrypt, null);
    }

    private static EncryptionItemRequestOptions GetRequestOptions(
        String dekId,
        List<String> pathsToEncrypt,
        String ifMatchEtag) {
        EncryptionItemRequestOptions options = new EncryptionItemRequestOptions();
        options.setIfMatchETag(ifMatchEtag);

        EncryptionOptions encryptionOptions = EncryptionTests.GetEncryptionOptions(dekId, pathsToEncrypt);
        options.setEncryptionOptions(encryptionOptions);
        return options;
    }


    private static EncryptionOptions GetEncryptionOptions(
        String dekId,
        List<String> pathsToEncrypt) {
        EncryptionOptions encryptionOptions = new EncryptionOptions();
        encryptionOptions.setPathsToEncrypt(pathsToEncrypt)
                         .setDataEncryptionKeyId(dekId)
                         .setEncryptionAlgorithm(CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256_RANDOMIZED);

        return encryptionOptions;
    }


    private class TestKeyWrapProvider implements EncryptionKeyWrapProvider {
        public EncryptionKeyUnwrapResult unwrapKey(byte[] wrappedKey, EncryptionKeyWrapMetadata metadata) {
            int moveBy = StringUtils.equals(metadata.value,
                EncryptionTests.metadata1.value + EncryptionTests.metadataUpdateSuffix) ? 1 : 2;

            for (int i = 0; i < wrappedKey.length; i++) {
                wrappedKey[i] = (byte) (wrappedKey[i] - moveBy);
            }

            return new EncryptionKeyUnwrapResult(wrappedKey, EncryptionTests.cacheTTL);
        }

        public EncryptionKeyWrapResult wrapKey(byte[] key, EncryptionKeyWrapMetadata metadata) {
            EncryptionKeyWrapMetadata responseMetadata =
                new EncryptionKeyWrapMetadata(metadata.value + EncryptionTests.metadataUpdateSuffix);
            int moveBy = StringUtils.equals(metadata.value, EncryptionTests.metadata1.value) ? 1 : 2;

            for (int i = 0; i < key.length; i++) {
                key[i] = (byte) (key[i] + moveBy);
            }

            return new EncryptionKeyWrapResult(key, responseMetadata);
        }
    }


    // This class is same as CosmosEncryptor but copied so as to induce decryption failure easily for testing.
    public static class TestEncryptor implements Encryptor {
        public final DataEncryptionKeyProvider dataEncryptionKeyProvider;
        public boolean FailDecryption;

        public TestEncryptor(DataEncryptionKeyProvider dataEncryptionKeyProvider) {
            this.dataEncryptionKeyProvider = dataEncryptionKeyProvider;
            this.FailDecryption = false;
        }

        public byte[] decryptAsync(
            byte[] cipherText,
            String dataEncryptionKeyId,
            String encryptionAlgorithm) {
            if (this.FailDecryption && dataEncryptionKeyId.equals("failDek")) {
                throw new IllegalArgumentException("Null {nameof(DataEncryptionKey)} returned.");
            }

            DataEncryptionKey dek = this.dataEncryptionKeyProvider.getDataEncryptionKey(
                dataEncryptionKeyId,
                encryptionAlgorithm);

            if (dek == null) {
                throw new IllegalArgumentException("Null {nameof(DataEncryptionKey)} returned from {nameof(this"
                    + ".DataEncryptionKeyProvider.FetchDataEncryptionKeyAsync)}.");
            }

            return dek.decryptData(cipherText);
        }

        public byte[] encryptAsync(
            byte[] plainText,
            String dataEncryptionKeyId,
            String encryptionAlgorithm) {
            DataEncryptionKey dek = this.dataEncryptionKeyProvider.getDataEncryptionKey(
                dataEncryptionKeyId,
                encryptionAlgorithm);

            return dek.encryptData(plainText);
        }
    }

    private static CosmosItemResponse<TestDoc> CreateItemAsync(EncryptionCosmosAsyncContainer container,
                                                               String dekId,
                                                               List<String> pathsToEncrypt) {
        return CreateItemAsync(container,
            dekId,
            pathsToEncrypt, null);
    }

    private static void ValidateSensitiveDataEncryptedTestDoc(CosmosAsyncContainer container,
                                                              String itemId,
                                                              PartitionKey partitionKey) {
        ValidateSensitiveDataEncrypted(container, itemId, partitionKey, ImmutableList.of("Sensitive"),
            ImmutableList.of("NonSensitive"));
    }

    private static void ValidateSensitiveDataEncrypted(CosmosAsyncContainer container,
                                                       String itemId,
                                                       PartitionKey partitionKey,
                                                       List<String> pathsToBeEncrypted,
                                                       List<String> nonEncryptedPath) {
        ObjectNode item = container.readItem(itemId, partitionKey, ObjectNode.class).block().getItem();

        for (String path : pathsToBeEncrypted) {
            assertThat(item.has(path)).isFalse();
        }

        for (String path : nonEncryptedPath) {
            assertThat(item.has(path)).isTrue();
        }
    }

    private static CosmosItemResponse<TestDoc> CreateItemAsync(
        EncryptionCosmosAsyncContainer container,
        String dekId,
        List<String> pathsToEncrypt,
        String partitionKey) {
        TestDoc testDoc = TestDoc.Create(partitionKey);
        CosmosItemResponse<TestDoc> createResponse = container.createItem(
            testDoc,
            new PartitionKey(testDoc.pk),
            EncryptionTests.GetRequestOptions(dekId, pathsToEncrypt)).block();

        assertThat(createResponse.getStatusCode()).isEqualTo(201);
        assertThat(createResponse.getItem()).isEqualTo(testDoc);

        List<String> nonEncryptedPath = new ArrayList<>(TestDoc.PathsToEncrypt);
        nonEncryptedPath.removeAll(pathsToEncrypt);
        ValidateSensitiveDataEncrypted(itemContainer, testDoc.id, new PartitionKey(testDoc.pk), pathsToEncrypt,
            nonEncryptedPath);

        return createResponse;
    }

    private static CosmosItemResponse<TestDoc> UpsertItemAsync(
        EncryptionCosmosAsyncContainer container,
        TestDoc testDoc,
        String dekId,
        List<String> pathsToEncrypt,
        int expectedStatusCode) {
        CosmosItemResponse<TestDoc> upsertResponse = container.upsertItem(
            testDoc,
            new PartitionKey(testDoc.pk),
            EncryptionTests.GetRequestOptions(dekId, pathsToEncrypt)).block();

        assertThat(upsertResponse.getStatusCode()).isEqualTo(expectedStatusCode);
        assertThat(upsertResponse.getItem()).isEqualTo(testDoc);

        List<String> nonEncryptedPath = new ArrayList<>(TestDoc.PathsToEncrypt);
        nonEncryptedPath.removeAll(pathsToEncrypt);
        ValidateSensitiveDataEncrypted(itemContainer, testDoc.id, new PartitionKey(testDoc.pk), pathsToEncrypt,
            nonEncryptedPath);

        return upsertResponse;
    }

    private static CosmosItemResponse<TestDoc> ReplaceItemAsync(
        EncryptionCosmosAsyncContainer container,
        TestDoc testDoc,
        String dekId,
        List<String> pathsToEncrypt,
        String etag) {
        CosmosItemResponse<TestDoc> replaceItem = container.replaceItem(
            testDoc,
            testDoc.id,
            new PartitionKey(testDoc.pk),
            EncryptionTests.GetRequestOptions(dekId, pathsToEncrypt, etag)).block();

        assertThat(replaceItem.getStatusCode()).isEqualTo(ResponseStatusCode.OK);
        assertThat(replaceItem.getItem()).isEqualTo(testDoc);

        List<String> nonEncryptedPath = new ArrayList<>(TestDoc.PathsToEncrypt);
        nonEncryptedPath.removeAll(pathsToEncrypt);
        ValidateSensitiveDataEncrypted(itemContainer, testDoc.id, new PartitionKey(testDoc.pk), pathsToEncrypt,
            nonEncryptedPath);

        return replaceItem;
    }

    private static CosmosItemResponse<Object> DeleteItemAsync(
        EncryptionCosmosAsyncContainer container,
        TestDoc testDoc) {
        CosmosItemResponse<Object> deleteResponse = container.deleteItem(
            testDoc.id,
            new PartitionKey(testDoc.pk),
            new CosmosItemRequestOptions()).block();

        assertThat(deleteResponse.getStatusCode()).isEqualTo(ResponseStatusCode.NO_CONTENT);
        assertThat(deleteResponse.getItem()).isNull();
        return deleteResponse;
    }

    public static class ResponseStatusCode {
        public static final int OK = 200;
        public static final int CREATED = 201;
        public static final int ACCEPTED = 202;
        public static final int NO_CONTENT = 204;
    }

}
