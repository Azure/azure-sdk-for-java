// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.encryption;

import com.azure.core.credential.TokenCredential;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.encryption.AzureKeyVaultKeyWrapMetadata;
import com.azure.cosmos.encryption.DecryptableItem;
import com.azure.cosmos.encryption.DecryptionContext;
import com.azure.cosmos.encryption.DecryptionInfo;
import com.azure.cosmos.encryption.EncryptableItem;
import com.azure.cosmos.encryption.EncryptionCosmosAsyncContainer;
import com.azure.cosmos.encryption.EncryptionException;
import com.azure.cosmos.encryption.EncryptionItemRequestOptions;
import com.azure.cosmos.encryption.EncryptionKeyUnwrapResult;
import com.azure.cosmos.encryption.EncryptionKeyWrapMetadata;
import com.azure.cosmos.encryption.EncryptionKeyWrapProvider;
import com.azure.cosmos.encryption.EncryptionKeyWrapResult;
import com.azure.cosmos.encryption.Encryptor;
import com.azure.cosmos.encryption.KeyVaultAccessClientTests;
import com.azure.cosmos.encryption.KeyVaultTokenCredentialFactory;
import com.azure.cosmos.encryption.WithEncryption;
import com.azure.cosmos.implementation.DatabaseForTest;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.encryption.CosmosEncryptionAlgorithm;
import com.azure.cosmos.encryption.DataEncryptionKey;
import com.azure.cosmos.encryption.DataEncryptionKeyProvider;
import com.azure.cosmos.encryption.EncryptionOptions;
import com.azure.cosmos.implementation.directconnectivity.Protocol;
import com.azure.cosmos.implementation.guava25.collect.ImmutableList;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.rx.TestSuiteBase;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.mockito.Mockito;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static com.azure.cosmos.encryption.KeyVaultAccessClientTests.*;
import static com.azure.cosmos.implementation.encryption.ImplementationBridgeHelpers.EncryptionKeyWrapMetadataHelper;

// TODO: moderakh fix/update the comments on the key wrap/unwrap tests.
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
    private String decryptionFailedDocId;

    private static byte[] rawDekForKeyVault;
    private static URI keyVaultKeyUri;
    private static AzureKeyVaultKeyWrapMetadata azureKeyVaultKeyWrapMetadata;
    private static AzureKeyVaultKeyWrapProvider azureKeyVaultKeyWrapProvider;
    private static EncryptionTestsTokenCredentialFactory encryptionTestsTokenCredentialFactory;

    @DataProvider
    public static Object[][] directClientBuilderWithSessionConsistency() {
        return new Object[][]{
            {createDirectRxDocumentClient(ConsistencyLevel.SESSION, Protocol.TCP, false, null, true)},
        };
    }

    @Factory(dataProvider = "directClientBuilderWithSessionConsistency")
    public EncryptionTests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeTest(groups = { "encryption" })
    public void beforeTest() throws Exception {
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
        EncryptionTests.rawDekForKeyVault = DataEncryptionKey.generate(CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256_RANDOMIZED);

        EncryptionTests.encryptionTestsTokenCredentialFactory = new EncryptionTestsTokenCredentialFactory();
        EncryptionTests.azureKeyVaultKeyWrapProvider = new AzureKeyVaultKeyWrapProvider(encryptionTestsTokenCredentialFactory,
            new KeyVaultAccessClientTests.KeyClientTestFactory(),
            new KeyVaultAccessClientTests.CryptographyClientFactoryTestFactory());
        keyVaultKeyUri = new URI("https://testdemo1.vault.azure.net/keys/testkey1/47d306aeaae74baab294672354603ca3");
        EncryptionTests.azureKeyVaultKeyWrapMetadata = new AzureKeyVaultKeyWrapMetadata(keyVaultKeyUri.toURL());

        EncryptionTests.encryptor.failDecryption = false;
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

        public static TestDoc create() {
            return TestDoc.create(null);
        }

        public static TestDoc create(String partitionKey) {
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
            dekProvider.getDataEncryptionKeyContainer().readDataEncryptionKey(dekId, null).block().getItem();
        assertThat(dekProperties).isEqualTo(readProperties);
    }

    @Test(groups = {"encryption"}, timeOut = TIMEOUT)
    public void encryptionRewrapDek() {
        String dekId = "randomDek";
        DataEncryptionKeyProperties dekProperties =  EncryptionTests.createDek(EncryptionTests.dekProvider, dekId);
        assertThat(dekProperties.encryptionKeyWrapMetadata
            ).isEqualTo(new EncryptionKeyWrapMetadata(EncryptionTests.metadata1.value + EncryptionTests.metadataUpdateSuffix));

        CosmosItemResponse<DataEncryptionKeyProperties> dekResponse =  EncryptionTests.dekProvider.getDataEncryptionKeyContainer().rewrapDataEncryptionKey(
            dekId,
            EncryptionTests.metadata2, null).block();

        assertThat(dekResponse.getStatusCode()).isEqualTo(ResponseStatusCode.OK);
        dekProperties = EncryptionTests.verifyDekResponse(
            dekResponse,
            dekId);
        assertThat(dekProperties.encryptionKeyWrapMetadata).isEqualTo(
            new EncryptionKeyWrapMetadata(EncryptionTests.metadata2.value + EncryptionTests.metadataUpdateSuffix));

        // Use different DEK provider to avoid (unintentional) cache impact
        CosmosDataEncryptionKeyProvider dekProvider = new CosmosDataEncryptionKeyProvider(new TestKeyWrapProvider());
         dekProvider.initialize(EncryptionTests.databaseCore, EncryptionTests.keyContainer.getId());
        DataEncryptionKeyProperties readProperties =  dekProvider.getDataEncryptionKeyContainer().readDataEncryptionKey(dekId, null).block().getItem();
        assertThat(readProperties).isEqualTo(readProperties);
    }

    @Test(groups = {"encryption"}, enabled = false, timeOut = TIMEOUT)
    public void encryptionDekReadFeed() {
    }

    @Test(groups = { "encryption" }, timeOut = TIMEOUT)
    public void encryptionCreateItemWithoutEncryptionOptions() {
        TestDoc testDoc = TestDoc.create();

        CosmosItemResponse<TestDoc> createResponse = EncryptionTests.encryptionContainer.createItem(testDoc,
            new PartitionKey(testDoc.pk), null).block();

        assertThat(createResponse.getStatusCode()).isEqualTo(201);
        assertThat(createResponse.getItem()).isEqualTo(testDoc);
    }

    @Test(groups = { "encryption" }, timeOut = TIMEOUT)
    public void encryptionCreateItemWithNullEncryptionOptions() {
        TestDoc testDoc = TestDoc.create();
        CosmosItemResponse<TestDoc> createResponse = EncryptionTests.encryptionContainer.createItem(
            testDoc,
            new PartitionKey(testDoc.pk),
            new EncryptionItemRequestOptions()).block();

        assertThat(createResponse.getStatusCode()).isEqualTo(201);
        assertThat(createResponse.getItem()).isEqualTo(testDoc);
    }

    @Test(groups = { "encryption" }, timeOut = TIMEOUT)
    public void encryptionCreateItemWithoutPartitionKey() {
        TestDoc testDoc = TestDoc.create();
        try {
            // TODO: moderakh invoke this without passing null PK
            EncryptionTests.encryptionContainer.createItem(
                testDoc,
                null,
                EncryptionTests.getRequestOptions(EncryptionTests.dekId, TestDoc.PathsToEncrypt));
            fail("CreateItem should've failed because PartitionKey was not provided.");
        } catch (Exception ex) {
            assertThat(ex.getMessage()).isEqualTo("partitionKey cannot be null for operations using "
                + "EncryptionContainer.");
        }
    }

    @Test(groups = { "encryption" }, timeOut = TIMEOUT, enabled = false)
    public void encryptionFailsWithUnknownDek() {
        String unknownDek = "unknownDek";

        try {
            EncryptionTests.createItem(EncryptionTests.encryptionContainer, unknownDek, TestDoc.PathsToEncrypt);
        } catch (Exception ex) {
            assertThat(ex.getMessage()).isEqualTo("Failed to retrieve Data Encryption Key with id: 'unknownDek'.");
            assertThat(ex.getCause() instanceof CosmosException);
        }
    }

    @Test(groups = { "encryption" }, timeOut = TIMEOUT)
    public void encryptionCreateItem() {
        TestDoc testDoc = EncryptionTests.createItem(EncryptionTests.encryptionContainer, EncryptionTests.dekId,
            TestDoc.PathsToEncrypt).getItem();
        EncryptionTests.verifyItemByRead(EncryptionTests.encryptionContainer, testDoc);
        verifyDataIsEncrypted(testDoc.id, new PartitionKey(testDoc.pk));

        TestDoc expectedDoc = new TestDoc(testDoc);

        EncryptionTests.validateQueryResults(
            EncryptionTests.encryptionContainer,
            "SELECT * FROM c",
            expectedDoc);

        EncryptionTests.validateQueryResults(
            EncryptionTests.encryptionContainer,
            String.format(
                "SELECT * FROM c where c.PK = '%s' and c.id = '%s' and c.NonSensitive = '%s'",
                expectedDoc.pk,
                expectedDoc.id,
                expectedDoc.nonSensitive),
            expectedDoc);

        EncryptionTests.validateQueryResults(
            EncryptionTests.encryptionContainer,
            String.format("SELECT * FROM c where c.Sensitive = '%s'", testDoc.sensitive),
            null);

        EncryptionTests.validateQueryResults(
            EncryptionTests.encryptionContainer,
            new SqlQuerySpec(
                "select * from c where c.id = @theId and c.PK = @thePK",
                new SqlParameter("@theId", expectedDoc.id),
                new SqlParameter("@thePK", expectedDoc.pk)),
            expectedDoc);

        expectedDoc.sensitive = null;

        EncryptionTests.validateQueryResults(
            EncryptionTests.encryptionContainer,
            "SELECT c.id, c.PK, c.Sensitive, c.NonSensitive FROM c",
            expectedDoc);

        EncryptionTests.validateQueryResults(
            EncryptionTests.encryptionContainer,
            "SELECT c.id, c.PK, c.NonSensitive FROM c",
            expectedDoc);
    }

    @Test(groups = {"encryption"}, enabled = false, timeOut = TIMEOUT)
    public void encryptionChangeFeedDecryptionSuccessful() {
    }

    @Test(groups = {"encryption"}, timeOut = TIMEOUT)
    public void ValidateDecryptableContent() {
        TestDoc testDoc = TestDoc.create();
        EncryptableItem<TestDoc> encryptableItem = new EncryptableItem<TestDoc>(testDoc);

        try {
            encryptableItem.getDecryptableItem().getDecryptionResult(TestDoc.class);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(RuntimeException.class);
            assertThat(e.getMessage()).isEqualTo("Decryptable content is not initialized.");
        }
    }

    @Test(groups = {"encryption"}, timeOut = TIMEOUT)
    public void EncryptionCreateItemWithLazyDecryption() throws Exception
    {
        TestDoc testDoc = TestDoc.create();
        CosmosItemResponse<EncryptableItem<TestDoc>> createResponse = EncryptionTests.encryptionContainer.createItem(
            new EncryptableItem<>(testDoc),
            new PartitionKey(testDoc.pk),
            EncryptionTests.getRequestOptions(EncryptionTests.dekId, TestDoc.PathsToEncrypt)).block();

        assertThat(createResponse.getStatusCode()).isEqualTo(ResponseStatusCode.CREATED);
        assertThat(createResponse.getItem()).isNotNull();

        EncryptionTests.validateDecryptableItem(createResponse.getItem().getDecryptableItem(), testDoc);

        // TODO: stream or byte array support?
        // stream
//        TestDoc testDoc1 = TestDoc.create();
//        ItemResponse<EncryptableI> createResponseStream = EncryptionTests.encryptionContainer.createItem(
//        new EncryptableItemStream(TestCommon.ToStream(testDoc1)),
//        new PartitionKey(testDoc1.PK),
//        EncryptionTests.GetRequestOptions(EncryptionTests.dekId, TestDoc.PathsToEncrypt));
//
//        Assert.AreEqual(HttpStatusCode.Created, createResponseStream.StatusCode);
//        Assert.IsNotNull(createResponseStream.Resource);
//
//        await EncryptionTests.ValidateDecryptableItem(createResponseStream.Resource.DecryptableItem, testDoc1);
    }

    @Test(groups = {"encryption"}, timeOut = TIMEOUT)
    public void EncryptionHandleDecryptionFailure() {
        String dek2 = "failDek";
         EncryptionTests.createDek(EncryptionTests.dekProvider, dek2);

        TestDoc testDoc1 =  EncryptionTests.createItem(EncryptionTests.encryptionContainer, dek2, TestDoc.PathsToEncrypt).getItem();
        TestDoc testDoc2 =  EncryptionTests.createItem(EncryptionTests.encryptionContainer, EncryptionTests.dekId, TestDoc.PathsToEncrypt).getItem();

        String query = String.format("SELECT * FROM c WHERE c.PK in ('%s', '%s')", testDoc1.pk, testDoc2.pk);
        // success
         EncryptionTests.validateQueryResultsMultipleDocuments(EncryptionTests.encryptionContainer, testDoc1, testDoc2, query);

        // induce failure for one document
        EncryptionTests.encryptor.failDecryption = true;
        testDoc1.sensitive = null;

        CosmosPagedFlux<DecryptableItem> queryPageFlux =
            EncryptionTests.encryptionContainer.queryItems(new SqlQuerySpec(query), null, DecryptableItem.class);
        FeedResponse<DecryptableItem> readDocsLazily = queryPageFlux.byPage().blockFirst();
         this.ValidateLazyDecryptionResponse(readDocsLazily, dek2);

         // TODO: add test for changefeed
//        // validate changeFeed handling
//        FeedIterator<DecryptableItem> changeIterator = EncryptionTests.encryptionContainer.GetChangeFeedIterator<DecryptableItem>(
//            continuationToken: null,
//        new ChangeFeedRequestOptions()
//        {
//            StartTime = DateTime.MinValue.ToUniversalTime()
//        });

//        while (changeIterator.HasMoreResults)
//        {
//            readDocsLazily = await changeIterator.ReadNextAsync();
//            if (readDocsLazily.Resource != null)
//            {
//                await this.ValidateLazyDecryptionResponse(readDocsLazily, dek2);
//            }
//        }

        // await this.ValidateChangeFeedProcessorResponse(EncryptionTests.itemContainerCore, testDoc1, testDoc2, false);
        EncryptionTests.encryptor.failDecryption = false;
    }

    @Test(groups = { "encryption" }, timeOut = TIMEOUT)
    public void encryptionDecryptQueryResultMultipleDocs() {
        TestDoc testDoc1 =  EncryptionTests.createItem(EncryptionTests.encryptionContainer, EncryptionTests.dekId, TestDoc.PathsToEncrypt).getItem();
        TestDoc testDoc2 =  EncryptionTests.createItem(EncryptionTests.encryptionContainer, EncryptionTests.dekId, TestDoc.PathsToEncrypt).getItem();

        String query = String.format("SELECT * FROM c WHERE c.PK in ('%s', '%s')", testDoc1.pk, testDoc2.pk);
        EncryptionTests.validateQueryResultsMultipleDocuments(EncryptionTests.encryptionContainer, testDoc1, testDoc2, query);

        // ORDER BY query
        query = query + " ORDER BY c._ts";
        EncryptionTests.validateQueryResultsMultipleDocuments(EncryptionTests.encryptionContainer, testDoc1, testDoc2, query);
    }

    @Test(groups = { "encryption" }, timeOut = TIMEOUT)
    public void encryptionDecryptChangeFeedResultMultipleDocs() {
        String partitionKey = UUID.randomUUID().toString();
        TestDoc testDoc1 =  EncryptionTests
            .createItem(
                EncryptionTests.encryptionContainer,
                EncryptionTests.dekId,
                TestDoc.PathsToEncrypt,
                partitionKey)
            .getItem();
        TestDoc testDoc2 =  EncryptionTests
            .createItem(
                EncryptionTests.encryptionContainer,
                EncryptionTests.dekId,
                TestDoc.PathsToEncrypt,
                partitionKey)
            .getItem();

        CosmosChangeFeedRequestOptions options =
            CosmosChangeFeedRequestOptions.createForProcessingFromBeginning(
                FeedRange.forLogicalPartition(new PartitionKey(partitionKey)));

        EncryptionTests.validateChangeFeedResultsMultipleDocuments(
            EncryptionTests.encryptionContainer,
            testDoc1,
            testDoc2,
            options);
    }

    @Test(groups = { "encryption" }, timeOut = TIMEOUT)
    public void encryptionDecryptQueryResultMultipleEncryptedProperties() {
        List<String> pathsEncrypted = ImmutableList.of("/Sensitive", "/NonSensitive");

        TestDoc testDoc = EncryptionTests.createItem(
            EncryptionTests.encryptionContainer,
            EncryptionTests.dekId,
            pathsEncrypted).getItem();

        TestDoc expectedDoc = new TestDoc(testDoc);

        EncryptionTests.validateQueryResults(
            EncryptionTests.encryptionContainer,
            "SELECT * FROM c",
            expectedDoc,
            pathsEncrypted);
    }

    @Test(groups = { "encryption" }, timeOut = TIMEOUT)
    public void encryptionDecryptQueryValueResponse() {
        EncryptionTests.createItem(EncryptionTests.encryptionContainer,
            EncryptionTests.dekId,
            TestDoc.PathsToEncrypt);
        String query = "SELECT VALUE COUNT(1) FROM c";
        EncryptionTests.validateQueryResponse(EncryptionTests.encryptionContainer, query, Integer.class);

        int value1 = itemContainer.queryItems(new SqlQuerySpec(query),
            new CosmosQueryRequestOptions(),
            Integer.class).collectList().block().stream().mapToInt(i -> i).sum();

        int value2 = encryptionContainer.queryItems(new SqlQuerySpec(query),
            new CosmosQueryRequestOptions(),
            Integer.class).collectList().block().stream().mapToInt(i -> i).sum();

        assertThat(value2).isEqualTo(value1);

        EncryptionTests.ValidateQueryResponseWithLazyDecryption(EncryptionTests.encryptionContainer, query);
    }

    @Test(groups = { "encryption" }, timeOut = TIMEOUT)
    public void encryptionDecryptGroupByQueryResult() {

        String partitionKey = UUID.randomUUID().toString();

        EncryptionTests.createItem(EncryptionTests.encryptionContainer, EncryptionTests.dekId,
            TestDoc.PathsToEncrypt, partitionKey);
        EncryptionTests.createItem(EncryptionTests.encryptionContainer, EncryptionTests.dekId,
            TestDoc.PathsToEncrypt, partitionKey);

        String query = String.format("SELECT COUNT(c.Id), c.PK " +
            "FROM c WHERE c.PK = '%s' " +
            "GROUP BY c.PK ", partitionKey);

        EncryptionTests.validateQueryResponse(EncryptionTests.encryptionContainer, query, ObjectNode.class);
    }

    @Test(groups = {"encryption"}, enabled = false, timeOut = TIMEOUT)
    public void encryptionStreamIteratorValidation() {
    }

    @Test(groups = { "encryption" }, timeOut = TIMEOUT)
    public void encryptionRudItem() {
        TestDoc testDoc = EncryptionTests.upsertItem(
            EncryptionTests.encryptionContainer,
            TestDoc.create(),
            EncryptionTests.dekId,
            TestDoc.PathsToEncrypt,
            ResponseStatusCode.CREATED).getItem();

        EncryptionTests.verifyItemByRead(EncryptionTests.encryptionContainer, testDoc);

        testDoc.nonSensitive = UUID.randomUUID().toString();
        testDoc.sensitive = UUID.randomUUID().toString();

        CosmosItemResponse<TestDoc> upsertResponse = EncryptionTests.upsertItem(
            EncryptionTests.encryptionContainer,
            testDoc,
            EncryptionTests.dekId,
            TestDoc.PathsToEncrypt,
            ResponseStatusCode.OK);

        TestDoc updatedDoc = upsertResponse.getItem();

        EncryptionTests.verifyItemByRead(EncryptionTests.encryptionContainer, updatedDoc);

        updatedDoc.nonSensitive = UUID.randomUUID().toString();
        updatedDoc.sensitive = UUID.randomUUID().toString();

        // TODO: replace
        TestDoc replacedDoc = EncryptionTests.replaceItem(
            EncryptionTests.encryptionContainer,
            updatedDoc,
            EncryptionTests.dekId,
            TestDoc.PathsToEncrypt,
            upsertResponse.getETag()
        ).getItem();

        EncryptionTests.verifyItemByRead(EncryptionTests.encryptionContainer, replacedDoc);
        EncryptionTests.deleteItem(EncryptionTests.encryptionContainer, replacedDoc);
    }


    @Test(groups = { "encryption" }, timeOut = TIMEOUT)
    public void EncryptionRudItemLazyDecryption() {
        TestDoc testDoc = TestDoc.create();
        // Upsert (item doesn't exist)
        CosmosItemResponse <EncryptableItem<TestDoc>> upsertResponse =  EncryptionTests.encryptionContainer.upsertItem(
        new EncryptableItem<TestDoc>(testDoc),
        new PartitionKey(testDoc.pk),
        EncryptionTests.getRequestOptions(EncryptionTests.dekId, TestDoc.PathsToEncrypt)).block();

        assertThat(upsertResponse.getStatusCode()).isEqualTo(ResponseStatusCode.CREATED);
        assertThat(upsertResponse.getItem()).isNotNull();

         EncryptionTests.validateDecryptableItem(upsertResponse.getItem().getDecryptableItem(), testDoc);
         EncryptionTests.verifyItemByRead(EncryptionTests.encryptionContainer, testDoc);

        // Upsert with stream (item exists)
        testDoc.nonSensitive = UUID.randomUUID().toString();
        testDoc.sensitive = UUID.randomUUID().toString();

        // stream
//        ItemResponse<EncryptableItemStream> upsertResponseStream =  EncryptionTests.encryptionContainer.UpsertItemAsync(
//        new EncryptableItemStream(TestCommon.ToStream(testDoc)),
//        new PartitionKey(testDoc.PK),
//        EncryptionTests.GetRequestOptions(EncryptionTests.dekId, TestDoc.PathsToEncrypt));
//
//        Assert.AreEqual(HttpStatusCode.OK, upsertResponseStream.StatusCode);
//        Assert.IsNotNull(upsertResponseStream.Resource);

//         EncryptionTests.ValidateDecryptableItem(upsertResponseStream.Resource.DecryptableItem, testDoc);
//         EncryptionTests.VerifyItemByReadAsync(EncryptionTests.encryptionContainer, testDoc);

        // replace
        testDoc.nonSensitive = UUID.randomUUID().toString();
        testDoc.sensitive = UUID.randomUUID().toString();

        CosmosItemResponse<EncryptableItem<TestDoc>> replaceResponseStream =
            EncryptionTests.encryptionContainer.replaceItem(
            new EncryptableItem<>(testDoc),
            testDoc.id,
            new PartitionKey(testDoc.pk),
            EncryptionTests.getRequestOptions(EncryptionTests.dekId, TestDoc.PathsToEncrypt,
                upsertResponse.getETag())).block();

        assertThat(replaceResponseStream.getStatusCode()).isEqualTo(ResponseStatusCode.OK);
        assertThat(replaceResponseStream.getItem()).isNotNull();

        EncryptionTests.validateDecryptableItem(replaceResponseStream.getItem().getDecryptableItem(), testDoc);
        EncryptionTests.verifyItemByRead(EncryptionTests.encryptionContainer, testDoc);
        EncryptionTests.deleteItem(EncryptionTests.encryptionContainer, testDoc);
    }

    @Test(groups = {"encryption"}, enabled = false, timeOut = TIMEOUT)
    public void encryptionResourceTokenAuthRestricted() {
    }

    @Test(groups = {"encryption"}, enabled = false, timeOut = TIMEOUT)
    public void encryptionResourceTokenAuthAllowed() {
    }

    @Test(groups = { "encryption" }, timeOut = TIMEOUT)
    public void encryptionRestrictedProperties() {
        try {
            EncryptionTests.createItem(EncryptionTests.encryptionContainer, EncryptionTests.dekId,
                ImmutableList.of("/id"));
            fail("Expected item creation with id specified to be encrypted to fail.");
        } catch (CosmosException ex) {
            // when (ex.StatusCode == HttpStatusCode.BadRequest)
            assertThat(ex.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.BADREQUEST);
        }

        try {
            EncryptionTests.createItem(EncryptionTests.encryptionContainer, EncryptionTests.dekId,
                ImmutableList.of("/PK"));
            fail("Expected item creation with PK specified to be encrypted to fail.");
        } catch (CosmosException ex) {
            assertThat(ex.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.BADREQUEST);
        }
    }

    @Test(groups = {"encryption"}, enabled = false, timeOut = TIMEOUT)
    public void encryptionBulkCrud() {
    }

    @Test(groups = {"encryption"}, enabled = false, timeOut = TIMEOUT)
    public void encryptionTransactionBatchCrud() {
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

    // async write, upsert, read
    @Test(groups = { "encryption" }, timeOut = TIMEOUT)
    public void asyncCreateItemEncrypt_readItemDecrypt() throws Exception {
        EncryptionItemRequestOptions requestOptions = new EncryptionItemRequestOptions();
        EncryptionOptions encryptionOptions = new EncryptionOptions();
        encryptionOptions.setPathsToEncrypt(ImmutableList.of("/Sensitive"));

        encryptionOptions.setDataEncryptionKeyId(dekId);
        encryptionOptions.setEncryptionAlgorithm(CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256_RANDOMIZED);
        requestOptions.setEncryptionOptions(encryptionOptions);

        TestDoc properties = getItem(UUID.randomUUID().toString());

        StepVerifier.create(itemContainer.createItem(
            properties,
            new PartitionKey(properties.pk),
            requestOptions)
                     .flatMap(
                         createResponse -> {
                             logger.info("1 on thread [{}]",
                                 Thread.currentThread().getName());
                             return encryptionContainer.upsertItem(
                                 properties,
                                 new PartitionKey(properties.pk),
                                 requestOptions);
                         })
                     .flatMap(
                         response -> {
                             logger.info("2 on thread [{}]",
                                 Thread.currentThread().getName());
                             Mono<CosmosItemResponse<TestDoc>> readItem = encryptionContainer.readItem(properties.id,
                                 new PartitionKey(properties.pk),
                                 requestOptions, TestDoc.class);

                             return readItem;
                         })
                     .flatMap(
                         readItem -> {
                             logger.info("3 on thread [{}]",
                                 Thread.currentThread().getName());
                             return itemContainer.readItem(properties.id, new PartitionKey(properties.pk), TestDoc.class);

                         })
                     .flatMap(
                         readItem -> {
                             logger.info("4 on thread [{}]",
                                 Thread.currentThread().getName());
                             return encryptionContainer.readItem(properties.id, new PartitionKey(properties.pk),
                                 requestOptions,
                                 TestDoc.class);
                         }
                     ))
                    .expectNextMatches(testDocCosmosItemResponse ->
                        {
                            TestDoc item = testDocCosmosItemResponse.getItem();
                            return item.sensitive != null;
                        })
                    .verifyComplete();
    }

    // key wrap/unwrap
    /// Validates UnWrapKey via KeyVault Wrap Provider.
    @Test(groups = { "encryption" }, timeOut = TIMEOUT)
    public void unWrapKeyUsingKeyVault() {
        EncryptionKeyWrapResult wrappedKey = EncryptionTests.wrapDekKeyVaultAsync(rawDekForKeyVault, azureKeyVaultKeyWrapMetadata).block();
        byte[] wrappedDek = wrappedKey.getWrappedDataEncryptionKey();
        EncryptionKeyWrapMetadata wrappedKeyVaultMetaData = wrappedKey.getEncryptionKeyWrapMetadata();
        EncryptionKeyUnwrapResult keyUnwrapResponse = EncryptionTests.unwrapDekKeyVaultAsync(wrappedDek, wrappedKeyVaultMetaData).block();

        assertThat(keyUnwrapResponse).isNotNull();
        assertThat(keyUnwrapResponse.getClientCacheTimeToLive()).isNotNull();
        assertThat(keyUnwrapResponse.getDataEncryptionKey()).isNotNull();

        assertThat(keyUnwrapResponse.getDataEncryptionKey()).isEqualTo(rawDekForKeyVault);
    }

    /// Validates handling of PurgeProtection Settings.
    @Test(groups = { "encryption" }, timeOut = TIMEOUT)
    public void setKeyVaultValidatePurgeProtectionAndSoftDeleteSettingsAsync() throws Exception {
        KeyVaultAccessClient keyVaultAccessClient = new KeyVaultAccessClient(encryptionTestsTokenCredentialFactory, new KeyClientTestFactory(),
            new KeyVaultAccessClientTests.CryptographyClientFactoryTestFactory());

        keyVaultKeyUri = new URI("https://testdemo3.vault.azure.net/keys/testkey1/47d306aeaae74baab294672354603ca3");
        AzureKeyVaultKeyWrapMetadata wrapKeyVaultMetaData = new AzureKeyVaultKeyWrapMetadata(keyVaultKeyUri.toURL());

        AtomicReference<KeyVaultKeyUriProperties> keyVaultKeyUriPropertiesRef = new AtomicReference<KeyVaultKeyUriProperties>();
        KeyVaultKeyUriProperties.tryParse(new URI(wrapKeyVaultMetaData.value), keyVaultKeyUriPropertiesRef);
        boolean validatepurgeprotection =
            keyVaultAccessClient.validatePurgeProtectionAndSoftDeleteSettings(keyVaultKeyUriPropertiesRef.get()).block();

        assertThat(validatepurgeprotection).isEqualTo(true);
    }

    /// Validates handling of PurgeProtection Settings.
    @Test(groups = { "encryption" }, timeOut = TIMEOUT)
    public void notSetKeyVaultValidatePurgeProtectionAndSoftDeleteSettingsAsync2() throws Exception {
        KeyVaultAccessClient keyVaultAccessClient = new KeyVaultAccessClient(encryptionTestsTokenCredentialFactory, new KeyClientTestFactory(), new CryptographyClientFactoryTestFactory());

        URI keyVaultKeyUriPurgeTest = new URI("https://testdemo2.vault.azure.net/keys/testkey2/ad47829797dc46489223cc5da3cba3ca");
        AzureKeyVaultKeyWrapMetadata wrapKeyVaultMetaData = new AzureKeyVaultKeyWrapMetadata(keyVaultKeyUriPurgeTest.toURL());
        AtomicReference<KeyVaultKeyUriProperties> keyVaultUriPropertiesRef = new AtomicReference<>();
        KeyVaultKeyUriProperties.tryParse(new URI(wrapKeyVaultMetaData.value), keyVaultUriPropertiesRef);

        boolean validatepurgeprotection =
            keyVaultAccessClient.validatePurgeProtectionAndSoftDeleteSettings(keyVaultUriPropertiesRef.get()).block();

        assertThat(validatepurgeprotection).isEqualTo(false);
    }


    /// Validates handling of Null Wrapped Key Returned from Key Vault
    //        [ExpectedException(typeof(ArgumentNullException),
    //        "ArgumentNullException when provided with null key.")]
    @Test(groups = { "encryption" }, timeOut = TIMEOUT,
        expectedExceptions = { IllegalArgumentException.class },
        expectedExceptionsMessageRegExp = "keyVaultAccessClient.wrapKey returned no bytes: wrappedDataEncryptionKey is null")
    public void validateNullWrappedKeyResult() throws Exception {
        URI keyUri =
            new URI("https://testdemo.vault.azure.net/keys/testkey1/" + KeyVaultTestConstants.ValidateNullWrappedKey);
        EncryptionKeyWrapMetadata invalidWrapMetadata = EncryptionKeyWrapMetadataHelper.create("akv", keyUri.toString());

        EncryptionKeyWrapResult keyWrapResponse = azureKeyVaultKeyWrapProvider.wrapKey(
            rawDekForKeyVault,
            invalidWrapMetadata).block();
    }

    /// Validates handling of Null Unwrapped Key from Key Vault
    @Test(groups = { "encryption" }, timeOut = TIMEOUT,
        expectedExceptions = {IllegalArgumentException.class},
        expectedExceptionsMessageRegExp = "keyVaultAccessClient.unwrapKey returned no bytes: dataEncryptionKey is null")
    public void validateNullUnwrappedKeyResult() throws Exception {
        URI keyUri = new URI("https://testdemo.vault.azure.net/keys/testkey1/" + KeyVaultTestConstants.ValidateNullUnwrappedKey);
        EncryptionKeyWrapMetadata invalidWrapMetadata = EncryptionKeyWrapMetadataHelper.create("akv", keyUri.toString(), KeyVaultConstants.RsaOaep256.toString());

        EncryptionKeyWrapResult wrappedKey = EncryptionTests.wrapDekKeyVaultAsync(rawDekForKeyVault, azureKeyVaultKeyWrapMetadata).block();
        byte[] wrappedDek = wrappedKey.getWrappedDataEncryptionKey();

        EncryptionKeyUnwrapResult keyWrapResponse = azureKeyVaultKeyWrapProvider.unwrapKey(
            wrappedDek,
            invalidWrapMetadata).block();
    }

    /// Validates Null Response from KeyVault
    //        [ExpectedException(typeof(NullReferenceException),
    //        "ArgumentNullException Method should catch Null References sent back by GetKeyAsync")]
    @Test(groups = { "encryption" }, timeOut = TIMEOUT, expectedExceptions = { IllegalArgumentException.class },
        expectedExceptionsMessageRegExp = "Key Vault https://testdemo.vault.azure"
            + ".net/keys/nullKeyVaultKey/47d306aeaae74baab294672354603ca3 provided must have soft delete and purge "
            + "protection enabled.")
    public void validateKeyClientReturnsNullKeyVaultResponse() throws Exception {
        URI keyUri = new URI("https://testdemo.vault.azure.net/keys/" + KeyVaultTestConstants.ValidateNullKeyVaultKey + "47d306aeaae74baab294672354603ca3");
        EncryptionKeyWrapMetadata invalidWrapMetadata = EncryptionKeyWrapMetadataHelper.create("akv", keyUri.toString());
        EncryptionKeyWrapResult keyWrapResponse =  EncryptionTests.wrapDekKeyVaultAsync(rawDekForKeyVault, invalidWrapMetadata).block();
    }

    /// Validates handling of Wrapping of Dek.
    @Test(groups = { "encryption" }, timeOut = TIMEOUT)
    public void wrapKeyUsingKeyVault() {
        EncryptionKeyWrapResult keyWrapResponse =  EncryptionTests.wrapDekKeyVaultAsync(rawDekForKeyVault, azureKeyVaultKeyWrapMetadata).block();

        assertThat(keyWrapResponse).isNotNull();
        assertThat(keyWrapResponse.getEncryptionKeyWrapMetadata()).isNotNull();
        assertThat(keyWrapResponse.getWrappedDataEncryptionKey()).isNotNull();
    }

    /// Validates handling of KeyClient returning a Request Failed.
    //        [ExpectedException(typeof(KeyVaultAccessException),
    @Test(groups = { "encryption" }, timeOut = TIMEOUT, expectedExceptions = { IllegalArgumentException.class },
        expectedExceptionsMessageRegExp = "Key Vault https://testdemo.vault.azure"
            + ".net/keys/requestFailed/47d306aeaae74baab294672354603ca3 provided must have soft delete and purge "
            + "protection enabled.")
    //
    //        "ArgumentNullException Method catches and returns RequestFailed Exception KeyVaultAccess Client to
    //        throw inner exception")]
    public void validateKeyClientReturnRequestFailed() throws Exception {
        URI keyUri =
            new URI("https://testdemo.vault.azure.net/keys/" + KeyVaultTestConstants.ValidateRequestFailedEx +
                "47d306aeaae74baab294672354603ca3");
        EncryptionKeyWrapMetadata invalidWrapMetadata = EncryptionKeyWrapMetadataHelper.create("akv", keyUri.toString());
        EncryptionKeyWrapResult keyWrapResponse = EncryptionTests.wrapDekKeyVaultAsync(rawDekForKeyVault,
            invalidWrapMetadata).block();
    }

    /// Integration validation of DEK Provider with KeyVault Wrap Provider.
    @Test(groups = { "encryption" }, timeOut = TIMEOUT)
    public void encryptionCreateDekKeyVaultWrapProvider() {
        String dekId = "DekWithKeyVault";
        DataEncryptionKeyProperties dekProperties = EncryptionTests.createDek(EncryptionTests.dekProvider, dekId);
        assertThat(new EncryptionKeyWrapMetadata(EncryptionTests.metadata1.value + EncryptionTests.metadataUpdateSuffix))
            .isEqualTo(dekProperties.encryptionKeyWrapMetadata);
        CosmosDataEncryptionKeyProvider dekProvider = new CosmosDataEncryptionKeyProvider(azureKeyVaultKeyWrapProvider);

        dekProvider.initialize(EncryptionTests.databaseCore, EncryptionTests.keyContainer.getId());
        DataEncryptionKeyProperties readProperties =
            dekProvider.getDataEncryptionKeyContainer().readDataEncryptionKey(dekId,
                new CosmosItemRequestOptions()).block().getItem();

        assertThat(readProperties).isEqualTo(dekProperties);
    }

    /// Integration testing for Rewrapping Dek with KeyVault Wrap Provider.
    @Test(groups = { "encryption" }, timeOut = TIMEOUT)
    public void encryptionRewrapDekWithKeyVaultWrapProvider() {
        String dekId = "randomDekKeyVault";
        DataEncryptionKeyProperties dekProperties = EncryptionTests.createDek(EncryptionTests.dekProvider, dekId);
        assertThat(new EncryptionKeyWrapMetadata(EncryptionTests.metadata1.value + EncryptionTests.metadataUpdateSuffix))
            .isEqualTo(dekProperties.encryptionKeyWrapMetadata);

        CosmosItemResponse<DataEncryptionKeyProperties> dekResponse =
            EncryptionTests.dekProvider.getDataEncryptionKeyContainer().rewrapDataEncryptionKey(
            dekId,
            EncryptionTests.metadata2, new CosmosItemRequestOptions()).block();

        assertThat(dekResponse.getStatusCode()).isEqualTo(ResponseStatusCode.OK);

        //        Assert.AreEqual(HttpStatusCode.OK, dekResponse.StatusCode);
        dekProperties = EncryptionTests.verifyDekResponse(
            dekResponse,
            dekId);

        assertThat(new EncryptionKeyWrapMetadata(EncryptionTests.metadata2.value + EncryptionTests.metadataUpdateSuffix)).isEqualTo(dekProperties.encryptionKeyWrapMetadata);
        //        Assert.AreEqual(
        //            EncryptionKeyWrapMetadataHelper.create(EncryptionTests.metadata2.Value + EncryptionTests
        //            .metadataUpdateSuffix),
        //            dekProperties.EncryptionKeyWrapMetadata);

        CosmosDataEncryptionKeyProvider dekProvider = new CosmosDataEncryptionKeyProvider(azureKeyVaultKeyWrapProvider);
        dekProvider.initialize(EncryptionTests.databaseCore, EncryptionTests.keyContainer.getId());
        DataEncryptionKeyProperties readProperties =
            dekProvider.getDataEncryptionKeyContainer().readDataEncryptionKey(dekId,
                new CosmosItemRequestOptions()).block().getItem();

        assertThat(readProperties).isEqualTo(dekProperties);
    }

    /// Validates handling of Null Key Passed to Wrap Provider.
    //    expectedExceptionsMessageRegExp = "Key is Null.")
    @Test(groups = { "encryption" }, timeOut = TIMEOUT, expectedExceptions = { RuntimeException.class })
    // TODO: fix the error message validation and the type
    public void wrapNullKeyUsingKeyVault() {
        EncryptionKeyWrapResult keyWrapResponse = EncryptionTests.wrapDekKeyVaultAsync(null, azureKeyVaultKeyWrapMetadata).block();
    }


    /// Validates handling of Incorrect Wrap Meta to Wrap Provider.
    //        [ExpectedException(typeof(ArgumentException),
    //        "ArgumentException when provided with incorrect WrapMetaData TypeConstants")]
    @Test(groups = { "encryption" }, timeOut = TIMEOUT, expectedExceptions = { RuntimeException.class })
    // TODO: fix the error message validation and the type
    public void wrapKeyUsingKeyVaultInValidTypeConstants() throws Exception {
        URI keyUri = new URI("https://testdemo.vault.azure.net/keys/testkey1/47d306aeaae74baab294672354603ca3");
        EncryptionKeyWrapMetadata invalidWrapMetadata = EncryptionKeyWrapMetadataHelper.create("incorrectConstant",
            keyUri.toString());
        EncryptionKeyWrapResult keyWrapResponse = EncryptionTests.wrapDekKeyVaultAsync(rawDekForKeyVault,
            invalidWrapMetadata).block();
    }

    /// Simulates a KeyClient Constructor returning an ArgumentNullException.
    //        [ExpectedException(typeof(ArgumentNullException),
    //        "ArgumentNullException Method catches and returns NullException")]
    // TODO: check error message
    @Test(groups = { "encryption" }, timeOut = TIMEOUT, expectedExceptions = {RuntimeException.class})
    public void validateKeyClientReturnNullArgument() {
        EncryptionKeyWrapMetadata invalidWrapMetadata = EncryptionKeyWrapMetadataHelper.create("akv", null);
        EncryptionKeyWrapResult keyWrapResponse = EncryptionTests.wrapDekKeyVaultAsync(rawDekForKeyVault, invalidWrapMetadata).block();
    }

    /// Validate Azure Key Wrap Provider handling of Incorrect KeyVault Uris.
    //        [ExpectedException(typeof(ArgumentException),
    //        "ArgumentException when provided with incorrect WrapMetaData Value")]
    @Test(groups = { "encryption" }, timeOut = TIMEOUT, expectedExceptions = {RuntimeException.class})
    public void wrapKeyUsingKeyVaultInValidValue() throws Exception {
        URI keyUri = new URI("https://testdemo.vault.azure.net/key/testkey1/47d306aeaae74baab294672354603ca3");
        EncryptionKeyWrapMetadata invalidWrapMetadata = EncryptionKeyWrapMetadataHelper.create("akv", keyUri.toString());
        EncryptionKeyWrapResult keyWrapResponse = EncryptionTests.wrapDekKeyVaultAsync(rawDekForKeyVault, invalidWrapMetadata).block();
    }

    private static void validateDecryptableItem(DecryptableItem decryptableItem,
                                                TestDoc testDoc,
                                                String dekId,
                                                List<String> pathsEncrypted,
                                                boolean isDocDecrypted) {
        DecryptableItem.DecryptionResult<TestDoc> res = decryptableItem.getDecryptionResult(TestDoc.class).block();
        TestDoc readDoc = res.getDecryptedItem();
        DecryptionContext decryptionContext = res.getContext();
        assertThat(readDoc).isEqualTo(testDoc);
        if (isDocDecrypted && testDoc.sensitive != null) {
            EncryptionTests.validateDecryptionContext(decryptionContext, dekId, pathsEncrypted);
        } else {
            assertThat(decryptionContext).isNull();
        }
    }

    private static void validateDecryptableItem(DecryptableItem decryptableItem,
                                                TestDoc testDoc) {
        validateDecryptableItem(decryptableItem, testDoc, null, null, true);
    }

    private static void validateDecryptionContext(
        DecryptionContext decryptionContext) {
        validateDecryptionContext(decryptionContext, null, null);
    }

    private static void validateDecryptionContext(DecryptionContext decryptionContext,
                                                  String dekId,
                                                  List<String> pathsEncrypted) {
        assertThat(decryptionContext.getDecryptionInfoList()).isNotNull();
        assertThat(decryptionContext.getDecryptionInfoList()).hasSize(1);
        
        DecryptionInfo decryptionInfo = decryptionContext.getDecryptionInfoList().get(0);
        assertThat(decryptionInfo.getDataEncryptionKeyId()).isEqualTo(dekId != null ? dekId : EncryptionTests.dekId);

        if (pathsEncrypted == null) {
            pathsEncrypted = TestDoc.PathsToEncrypt;
        }

        assertThat(decryptionInfo.getPathsDecrypted()).hasSameSizeAs(pathsEncrypted);
        assertThat(pathsEncrypted.stream().filter(path -> !decryptionInfo.getPathsDecrypted().contains(path)).findAny()).isNotPresent();
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

    private static void validateQueryResultsMultipleDocuments(
        EncryptionCosmosAsyncContainer container,
        TestDoc testDoc1,
        TestDoc testDoc2,
        String query) {
        validateQueryResultsMultipleDocuments(container, testDoc1, testDoc2, query, null);
    }

    private static void validateChangeFeedResultsMultipleDocuments(
        EncryptionCosmosAsyncContainer container,
        TestDoc testDoc1,
        TestDoc testDoc2,
        CosmosChangeFeedRequestOptions requestOptions) {
        CosmosPagedFlux<TestDoc> pageFlux = container.queryChangeFeed(requestOptions, TestDoc.class);
        List<TestDoc> readDocs = pageFlux.collectList().block();

        assertThat(readDocs.size()).isEqualTo(2);
        assertThat(readDocs).containsExactlyInAnyOrder(testDoc1, testDoc2);


        CosmosPagedFlux<DecryptableItem> lazyDecryptablePageFlux = container.queryChangeFeed(requestOptions, DecryptableItem.class);
        List<DecryptableItem> lazyDecryptableItems = lazyDecryptablePageFlux.collectList().block();

        assertThat(readDocs.size()).isEqualTo(2);
        assertThat(readDocs).containsExactlyInAnyOrder(testDoc1, testDoc2);


        assertThat(lazyDecryptableItems.size()).isEqualTo(2);
        assertThat(lazyDecryptableItems.stream().map(ldi -> ldi.getDecryptionResult(TestDoc.class).block().getDecryptedItem())).containsExactlyInAnyOrder(testDoc1, testDoc2);
    }
    
    private static void validateQueryResultsMultipleDocuments(
        EncryptionCosmosAsyncContainer container,
        TestDoc testDoc1,
        TestDoc testDoc2,
        String query,
        CosmosQueryRequestOptions requestOptions) {
        CosmosPagedFlux<TestDoc> pageFlux = container.queryItems(new SqlQuerySpec(query), requestOptions, TestDoc.class);
        List<TestDoc> readDocs = pageFlux.collectList().block();

        assertThat(readDocs.size()).isEqualTo(2);
        assertThat(readDocs).containsExactlyInAnyOrder(testDoc1, testDoc2);


        CosmosPagedFlux<DecryptableItem> lazyDecryptablePageFlux = container.queryItems(new SqlQuerySpec(query), requestOptions, DecryptableItem.class);
        List<DecryptableItem> lazyDecryptableItems = lazyDecryptablePageFlux.collectList().block();

        assertThat(readDocs.size()).isEqualTo(2);
        assertThat(readDocs).containsExactlyInAnyOrder(testDoc1, testDoc2);


        assertThat(lazyDecryptableItems.size()).isEqualTo(2);
        assertThat(lazyDecryptableItems.stream().map(ldi -> ldi.getDecryptionResult(TestDoc.class).block().getDecryptedItem())).containsExactlyInAnyOrder(testDoc1, testDoc2);
    }

    private static <T> void validateQueryResponse(EncryptionCosmosAsyncContainer container,
                                                  String query,
                                                  Class<T> classType) {
        container.queryItems(new SqlQuerySpec(query),
            new CosmosQueryRequestOptions(),
            classType).collectList().block();
    }

    private static void validateQueryResults(
        EncryptionCosmosAsyncContainer container,
        String query,
        TestDoc expectedDoc) {
        validateQueryResults(container, query, expectedDoc, null);
    }

    private static void validateQueryResults(
        EncryptionCosmosAsyncContainer container,
        String query,
        TestDoc expectedDoc,
        List<String> pathsEncrypted) {
        validateQueryResults(container, new SqlQuerySpec(query), expectedDoc, pathsEncrypted);
    }

    private static void validateQueryResults(
        EncryptionCosmosAsyncContainer container,
        SqlQuerySpec query,
        TestDoc expectedDoc) {
        validateQueryResults(container, query, expectedDoc, null);
    }

    private static void validateQueryResults(
        EncryptionCosmosAsyncContainer container,
        SqlQuerySpec query,
        TestDoc expectedDoc,
        List<String> pathsEncrypted) {

        CosmosQueryRequestOptions requestOptions = expectedDoc != null
            ? new CosmosQueryRequestOptions().setPartitionKey(new PartitionKey(expectedDoc.pk)) : null;


        CosmosPagedFlux<TestDoc> queryResponseIterator = container.queryItems(query, requestOptions, TestDoc.class);
        CosmosPagedFlux<DecryptableItem> queryResponseIteratorForLazyDecryption = container.queryItems(query, requestOptions, DecryptableItem.class);

        List<TestDoc> results = queryResponseIterator.collectList().block();
        List<DecryptableItem> lazyResults = queryResponseIteratorForLazyDecryption.collectList().block();

        if (expectedDoc != null) {
            assertThat(results.size()).isEqualTo(1);
            assertThat(results.get(0)).isEqualTo(expectedDoc);

            assertThat(lazyResults.size()).isEqualTo(1);
            EncryptionTests.validateDecryptableItem(lazyResults.get(0), expectedDoc, null, pathsEncrypted, true);

        } else {
            assertThat(results.size()).isEqualTo(0);
        }
    }

    private static void ValidateQueryResponseWithLazyDecryption(EncryptionCosmosAsyncContainer container,
                                                                           String query) {
        CosmosPagedFlux<DecryptableItem> queryResponseIteratorForLazyDecryption = container.queryItems(new SqlQuerySpec(query), null, DecryptableItem.class);
        FeedResponse<DecryptableItem> readDocsLazily = queryResponseIteratorForLazyDecryption.byPage().blockFirst();
        assertThat(readDocsLazily.getContinuationToken()).isNull();
        assertThat(readDocsLazily.getElements()).hasSize(1);
        DecryptableItem.DecryptionResult<Long> result =
            readDocsLazily.getResults().get(0).getDecryptionResult(Long.class).block();

        assertThat(result.getContext()).isNull();
        assertThat(result.getDecryptedItem()).isGreaterThanOrEqualTo(1l);
    }

    private static void verifyDataIsEncrypted(String id, PartitionKey partitionKey) {

        TestDoc item = itemContainer.readItem(id, partitionKey, TestDoc.class).block().getItem();
        assertThat(item.sensitive).isEqualTo(null);
        assertThat(item.nonSensitive).isNotNull();
    }

    private static void verifyItemByRead(EncryptionCosmosAsyncContainer container, TestDoc testDoc) {
        verifyItemByRead(container, testDoc, null);
    }

    private static void verifyItemByRead(EncryptionCosmosAsyncContainer container, TestDoc testDoc,
                                         CosmosItemRequestOptions requestOptions) {
        CosmosItemResponse<TestDoc> readResponse = container.readItem(testDoc.id, new PartitionKey(testDoc.pk),
            requestOptions, TestDoc.class).block();

        assertThat(readResponse.getStatusCode()).isEqualTo(ResponseStatusCode.OK);
        assertThat(readResponse.getItem()).isEqualTo(testDoc);
    }

    private static DataEncryptionKeyProperties createDek(CosmosDataEncryptionKeyProvider dekProvider, String dekId)
    {
        CosmosItemResponse<DataEncryptionKeyProperties> dekResponse =  dekProvider.getDataEncryptionKeyContainer().createDataEncryptionKey(
        dekId,
        CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256_RANDOMIZED,
        EncryptionTests.metadata1,
        new CosmosItemRequestOptions()
        ).block();

        assertThat(dekResponse.getStatusCode()).isEqualTo(ResponseStatusCode.CREATED);

        return verifyDekResponse(dekResponse,
            dekId);
    }

    private static DataEncryptionKeyProperties verifyDekResponse(
        CosmosItemResponse<DataEncryptionKeyProperties> dekResponse,
        String dekId)
    {
        assertThat(dekResponse.getRequestCharge()).isGreaterThan(0);
        assertThat(dekResponse.getETag()).isNotNull();

        DataEncryptionKeyProperties dekProperties = dekResponse.getItem();
        assertThat(dekProperties).isNotNull();
        assertThat(dekResponse.getETag()).isEqualTo(dekProperties.eTag);
        assertThat(dekProperties.id).isEqualTo(dekProperties.id);
        assertThat(dekProperties.selfLink).isNotNull();
        assertThat(dekProperties.createdTime).isNotNull();
        assertThat(dekProperties.lastModified).isNotNull();

        return dekProperties;
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

    private static EncryptionItemRequestOptions getRequestOptions(
        String dekId,
        List<String> pathsToEncrypt) {
        return getRequestOptions(dekId, pathsToEncrypt, null);
    }

    private static EncryptionItemRequestOptions getRequestOptions(
        String dekId,
        List<String> pathsToEncrypt,
        String ifMatchEtag) {
        EncryptionItemRequestOptions options = new EncryptionItemRequestOptions();
        options.setIfMatchETag(ifMatchEtag);

        EncryptionOptions encryptionOptions = EncryptionTests.getEncryptionOptions(dekId, pathsToEncrypt);
        options.setEncryptionOptions(encryptionOptions);
        return options;
    }

    private static EncryptionOptions getEncryptionOptions(
        String dekId,
        List<String> pathsToEncrypt) {
        EncryptionOptions encryptionOptions = new EncryptionOptions();
        encryptionOptions.setPathsToEncrypt(pathsToEncrypt)
                         .setDataEncryptionKeyId(dekId)
                         .setEncryptionAlgorithm(CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256_RANDOMIZED);

        return encryptionOptions;
    }

    private class TestKeyWrapProvider implements EncryptionKeyWrapProvider {
        public Mono<EncryptionKeyUnwrapResult> unwrapKey(byte[] wrappedKey, EncryptionKeyWrapMetadata metadata) {
            int moveBy = StringUtils.equals(metadata.value,
                EncryptionTests.metadata1.value + EncryptionTests.metadataUpdateSuffix) ? 1 : 2;

            for (int i = 0; i < wrappedKey.length; i++) {
                wrappedKey[i] = (byte) (wrappedKey[i] - moveBy);
            }

            return Mono.just(new EncryptionKeyUnwrapResult(wrappedKey, EncryptionTests.cacheTTL));
        }

        public Mono<EncryptionKeyWrapResult> wrapKey(byte[] key, EncryptionKeyWrapMetadata metadata) {
            EncryptionKeyWrapMetadata responseMetadata =
                new EncryptionKeyWrapMetadata(metadata.value + EncryptionTests.metadataUpdateSuffix);
            int moveBy = StringUtils.equals(metadata.value, EncryptionTests.metadata1.value) ? 1 : 2;

            for (int i = 0; i < key.length; i++) {
                key[i] = (byte) (key[i] + moveBy);
            }

            return Mono.just(new EncryptionKeyWrapResult(key, responseMetadata));
        }
    }

    // This class is same as CosmosEncryptor but copied so as to induce decryption failure easily for testing.
    public static class TestEncryptor implements Encryptor {
        public final DataEncryptionKeyProvider dataEncryptionKeyProvider;
        public boolean failDecryption;

        public TestEncryptor(DataEncryptionKeyProvider dataEncryptionKeyProvider) {
            this.dataEncryptionKeyProvider = dataEncryptionKeyProvider;
            this.failDecryption = false;
        }

        public Mono<byte[]> decrypt(
            byte[] cipherText,
            String dataEncryptionKeyId,
            String encryptionAlgorithm) {
            if (this.failDecryption && dataEncryptionKeyId.equals("failDek")) {
                throw new IllegalArgumentException("Null DataEncryptionKey returned.");
            }

            Mono<DataEncryptionKey> dekMono = this.dataEncryptionKeyProvider.getDataEncryptionKey(
                dataEncryptionKeyId,
                encryptionAlgorithm);

            return dekMono.switchIfEmpty(
                Mono.error(
             new IllegalArgumentException("Null DataEncryptionKey returned from this"
                + ".DataEncryptionKeyProvider.FetchDataEncryptionKeyAsync}.")
                )

            ).map(
                dek -> {
                    return dek.decryptData(cipherText);
                }
            );
        }

        public Mono<byte[]> encrypt(
            byte[] plainText,
            String dataEncryptionKeyId,
            String encryptionAlgorithm) {
            Mono<DataEncryptionKey> dekMono = this.dataEncryptionKeyProvider.getDataEncryptionKey(
                dataEncryptionKeyId,
                encryptionAlgorithm);

            return dekMono.map(
                dek -> {
                     return dek.encryptData(plainText);
                }
            );
        }
    }

    private static CosmosItemResponse<TestDoc> createItem(EncryptionCosmosAsyncContainer container,
                                                          String dekId,
                                                          List<String> pathsToEncrypt) {
        return createItem(container,
            dekId,
            pathsToEncrypt, null);
    }

    private static void validateSensitiveDataEncrypted(CosmosAsyncContainer container,
                                                       String itemId,
                                                       PartitionKey partitionKey,
                                                       List<String> pathsToBeEncrypted,
                                                       List<String> nonEncryptedPath) {
        ObjectNode item = container.readItem(itemId, partitionKey, ObjectNode.class).block().getItem();

        for (String path : pathsToBeEncrypted) {
            assertThat(contains(item, path)).isFalse();
        }

        for (String path : nonEncryptedPath) {
            assertThat(contains(item, path)).isTrue();
        }
    }

    private static JsonNode get(JsonNode item, String path) {
        String[] parts = StringUtils.split(path, "/");

        for (String part: parts) {
            assertThat(item).isNotNull();
            item = item.get(part);
        }

        return item;
    }

    private static boolean contains(JsonNode item, String path) {
        String[] parts = StringUtils.split(path, "/");

        for (String part: parts) {
            assertThat(item).isNotNull();
            item = item.get(part);
        }

        return item != null;
    }

    private void ValidateLazyDecryptionResponse(FeedResponse<DecryptableItem> readDocsLazily,
                                                String failureDek) {
        int decryptedDoc = 0;
        int failedDoc = 0;

        for (DecryptableItem doc : readDocsLazily.getElements()) {
            try {
                DecryptableItem.DecryptionResult<ObjectNode> result = doc.getDecryptionResult(ObjectNode.class).block();
                decryptedDoc++;
            } catch (EncryptionException encryptionException) {
                failedDoc++;
                this.validateEncryptionException(encryptionException, failureDek);
            }
        }

        assertThat(decryptedDoc >= 1).isTrue();
        assertThat(failedDoc).isEqualTo(1);
    }

    private void validateEncryptionException(
        EncryptionException encryptionException,
        String failureDek) {

        assertThat(encryptionException.getDataEncryptionKeyId()).isEqualTo(failureDek);
        assertThat(encryptionException.getEncryptedContent()).isNotNull();
        assertThat(encryptionException.getCause()).isNotNull();
        assertThat(encryptionException.getCause()).isInstanceOf(IllegalArgumentException.class);
        assertThat(encryptionException.getCause().getMessage()).isEqualTo("Null DataEncryptionKey returned.");
    }

    private static CosmosItemResponse<TestDoc> createItem(
        EncryptionCosmosAsyncContainer container,
        String dekId,
        List<String> pathsToEncrypt,
        String partitionKey) {
        TestDoc testDoc = TestDoc.create(partitionKey);
        CosmosItemResponse<TestDoc> createResponse = container.createItem(
            testDoc,
            new PartitionKey(testDoc.pk),
            EncryptionTests.getRequestOptions(dekId, pathsToEncrypt)).block();

        assertThat(createResponse.getStatusCode()).isEqualTo(201);
        assertThat(createResponse.getItem()).isEqualTo(testDoc);

        List<String> nonEncryptedPath = new ArrayList<>(TestDoc.AllPath);
        nonEncryptedPath.removeAll(pathsToEncrypt);
        validateSensitiveDataEncrypted(itemContainer, testDoc.id, new PartitionKey(testDoc.pk), pathsToEncrypt,
            nonEncryptedPath);

        return createResponse;
    }

    private static CosmosItemResponse<TestDoc> upsertItem(
        EncryptionCosmosAsyncContainer container,
        TestDoc testDoc,
        String dekId,
        List<String> pathsToEncrypt,
        int expectedStatusCode) {
        CosmosItemResponse<TestDoc> upsertResponse = container.upsertItem(
            testDoc,
            new PartitionKey(testDoc.pk),
            EncryptionTests.getRequestOptions(dekId, pathsToEncrypt)).block();

        assertThat(upsertResponse.getStatusCode()).isEqualTo(expectedStatusCode);
        assertThat(upsertResponse.getItem()).isEqualTo(testDoc);

        List<String> nonEncryptedPath = new ArrayList<>(TestDoc.PathsToEncrypt);
        nonEncryptedPath.removeAll(pathsToEncrypt);
        validateSensitiveDataEncrypted(itemContainer, testDoc.id, new PartitionKey(testDoc.pk), pathsToEncrypt,
            nonEncryptedPath);

        return upsertResponse;
    }

    private static CosmosItemResponse<TestDoc> replaceItem(
        EncryptionCosmosAsyncContainer container,
        TestDoc testDoc,
        String dekId,
        List<String> pathsToEncrypt,
        String etag) {
        CosmosItemResponse<TestDoc> replaceItem = container.replaceItem(
            testDoc,
            testDoc.id,
            new PartitionKey(testDoc.pk),
            EncryptionTests.getRequestOptions(dekId, pathsToEncrypt, etag)).block();

        assertThat(replaceItem.getStatusCode()).isEqualTo(ResponseStatusCode.OK);
        assertThat(replaceItem.getItem()).isEqualTo(testDoc);

        List<String> nonEncryptedPath = new ArrayList<>(TestDoc.PathsToEncrypt);
        nonEncryptedPath.removeAll(pathsToEncrypt);
        validateSensitiveDataEncrypted(itemContainer, testDoc.id, new PartitionKey(testDoc.pk), pathsToEncrypt,
            nonEncryptedPath);

        return replaceItem;
    }

    private static CosmosItemResponse<Object> deleteItem(
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

    private static Mono<EncryptionKeyWrapResult> wrapDekKeyVaultAsync(byte[] rawDek, EncryptionKeyWrapMetadata wrapMetaData) {
        return azureKeyVaultKeyWrapProvider.wrapKey(
            rawDek,
            wrapMetaData);
    }

    private static Mono<EncryptionKeyUnwrapResult> unwrapDekKeyVaultAsync(byte[] wrappedDek, EncryptionKeyWrapMetadata unwrapMetaData) {
        return azureKeyVaultKeyWrapProvider.unwrapKey(
            wrappedDek,
            unwrapMetaData);
    }

    class EncryptionTestsTokenCredentialFactory extends KeyVaultTokenCredentialFactory {
        public Mono<TokenCredential> getTokenCredential(URI keyVaultKeyUri) {

            // TODO: DefaultAzureCredentials
            return Mono.just(Mockito.mock(TokenCredential.class));
        }
    }

    public static class ResponseStatusCode {
        public static final int OK = 200;
        public static final int CREATED = 201;
        public static final int ACCEPTED = 202;
        public static final int NO_CONTENT = 204;
    }
}
