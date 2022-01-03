/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.azure.cosmos;

import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.implementation.feedranges.FeedRangeInternal;
import com.azure.cosmos.implementation.feedranges.FeedRangePartitionKeyRangeImpl;
import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.models.ChangeFeedPolicy;
import com.azure.cosmos.models.ClientEncryptionIncludedPath;
import com.azure.cosmos.models.ClientEncryptionPolicy;
import com.azure.cosmos.models.CosmosClientEncryptionKeyProperties;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerRequestOptions;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.EncryptionKeyWrapMetadata;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.IndexingMode;
import com.azure.cosmos.models.IndexingPolicy;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.models.PartitionKeyDefinitionVersion;
import com.azure.cosmos.models.PartitionKind;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.rx.TestSuiteBase;
import com.azure.cosmos.util.CosmosPagedIterable;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class CosmosContainerTest extends TestSuiteBase {

    private String preExistingDatabaseId = CosmosDatabaseForTest.generateId();
    private CosmosClient client;
    private CosmosDatabase createdDatabase;
    private CosmosContainer createdContainer;

    @Factory(dataProvider = "clientBuilders")
    public CosmosContainerTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"emulator"}, timeOut = SETUP_TIMEOUT)
    public void before_CosmosContainerTest() {
        client = getClientBuilder().buildClient();
        createdDatabase = createSyncDatabase(client, preExistingDatabaseId);
        createEncryptionKey();
    }

    @AfterClass(groups = {"emulator"}, timeOut = 3 * SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        logger.info("starting ....");
        safeDeleteSyncDatabase(createdDatabase);
        safeCloseSyncClient(client);
    }

    @BeforeMethod(groups = { "emulator" })
    public void beforeTest() throws Exception {
        this.createdContainer = null;
    }

    @AfterMethod(groups = { "emulator" })
    public void afterTest() throws Exception {
        if (this.createdContainer != null) {
            try {
                this.createdContainer.delete();
            } catch (CosmosException error) {
                if (error.getStatusCode() != 404) {
                    throw error;
                }
            }
        }
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void createContainer_withProperties() throws Exception {
        String collectionName = UUID.randomUUID().toString();
        CosmosContainerProperties containerProperties = getCollectionDefinition(collectionName);

        CosmosContainerResponse containerResponse = createdDatabase.createContainer(containerProperties);
        this.createdContainer = createdDatabase.getContainer(collectionName);
        assertThat(containerResponse.getRequestCharge()).isGreaterThan(0);
        validateContainerResponse(containerProperties, containerResponse);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void createContainer_withEncryption() {
        String collectionName = UUID.randomUUID().toString();
        CosmosContainerProperties containerProperties = getCollectionDefinition(collectionName);

        ClientEncryptionIncludedPath path1 = new ClientEncryptionIncludedPath();
        path1.setPath("/path1");
        path1.setEncryptionAlgorithm("AEAD_AES_256_CBC_HMAC_SHA256");
        path1.setEncryptionType("Randomized");
        path1.setClientEncryptionKeyId("containerTestKey1");

        ClientEncryptionIncludedPath path2 = new ClientEncryptionIncludedPath();
        path2.setPath("/path2");
        path2.setEncryptionAlgorithm("AEAD_AES_256_CBC_HMAC_SHA256");
        path2.setEncryptionType("Deterministic");
        path2.setClientEncryptionKeyId("containerTestKey2");

        List<ClientEncryptionIncludedPath> paths = new ArrayList<>();
        paths.add(path1);
        paths.add(path2);

        ClientEncryptionPolicy clientEncryptionPolicy = new ClientEncryptionPolicy(paths);
        containerProperties.setClientEncryptionPolicy(clientEncryptionPolicy);

        CosmosContainerResponse containerResponse = createdDatabase.createContainer(containerProperties);
        assertThat(containerResponse.getRequestCharge()).isGreaterThan(0);
        validateContainerResponseWithEncryption(containerProperties, containerResponse, clientEncryptionPolicy);
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void createContainer_withPartitionKeyInEncryption() {
        String collectionName = UUID.randomUUID().toString();
        CosmosContainerProperties containerProperties = getCollectionDefinition(collectionName);

        ClientEncryptionIncludedPath path1 = new ClientEncryptionIncludedPath();
        path1.setPath("/mypk");
        path1.setEncryptionAlgorithm("AEAD_AES_256_CBC_HMAC_SHA256");
        path1.setEncryptionType("Randomized");
        path1.setClientEncryptionKeyId("containerTestKey1");

        ClientEncryptionIncludedPath path2 = new ClientEncryptionIncludedPath();
        path2.setPath("/path2");
        path2.setEncryptionAlgorithm("AEAD_AES_256_CBC_HMAC_SHA256");
        path2.setEncryptionType("Deterministic");
        path2.setClientEncryptionKeyId("containerTestKey2");

        List<ClientEncryptionIncludedPath> paths = new ArrayList<>();
        paths.add(path1);
        paths.add(path2);

        ClientEncryptionPolicy clientEncryptionPolicy = new ClientEncryptionPolicy(paths);
        CosmosContainerResponse containerResponse = null;

        //Verify partition key in CosmosContainerProperties constructor with encrypted field.
        try {
            containerProperties.setClientEncryptionPolicy(clientEncryptionPolicy);
            containerResponse = createdDatabase.createContainer(containerProperties);
            fail("createContainer should fail as mypk which is part of the partition key cannot be included in the " +
                "ClientEncryptionPolicy.");
        } catch (IllegalArgumentException ex) {
            assertThat(ex.getMessage()).isEqualTo("Path mypk which is part of the partition key cannot be included in" +
                " the ClientEncryptionPolicy.");
        }


        //Verify for composite key
        collectionName = UUID.randomUUID().toString();
        containerProperties = new CosmosContainerProperties(collectionName, "/mypk/mypk1");
        try {
            containerProperties.setClientEncryptionPolicy(clientEncryptionPolicy);
            containerResponse = createdDatabase.createContainer(containerProperties);
            fail("createContainer should fail as mypk which is part of the partition key cannot be included in the " +
                "ClientEncryptionPolicy.");
        } catch (IllegalArgumentException ex) {
            assertThat(ex.getMessage()).isEqualTo("Path mypk which is part of the partition key cannot be included in" +
                " the ClientEncryptionPolicy.");
        }


        //Verify setPartitionKeyDefinition with encrypted field.
        collectionName = UUID.randomUUID().toString();
        containerProperties = new CosmosContainerProperties(collectionName, "/differentKey");
        try {
            containerProperties.setClientEncryptionPolicy(clientEncryptionPolicy);
            PartitionKeyDefinition partitionKeyDefinition = new PartitionKeyDefinition();
            List<String> keyPaths = new ArrayList<>();
            keyPaths.add("/mypk");
            partitionKeyDefinition.setPaths(keyPaths);
            containerProperties.setPartitionKeyDefinition(partitionKeyDefinition);
            containerResponse = createdDatabase.createContainer(containerProperties);
            fail("createContainer should fail as mypk which is part of the partition key cannot be included in the " +
                "ClientEncryptionPolicy.");
        } catch (IllegalArgumentException ex) {
            assertThat(ex.getMessage()).isEqualTo("Path mypk which is part of the partition key cannot be included in" +
                " the ClientEncryptionPolicy.");
        }

        //This should pass as we check only the first key of the composite key.
        collectionName = UUID.randomUUID().toString();
        containerProperties = new CosmosContainerProperties(collectionName, "/mypk1/mypk");
        containerProperties.setClientEncryptionPolicy(clientEncryptionPolicy);
        containerResponse = createdDatabase.createContainer(containerProperties);
        assertThat(containerResponse.getRequestCharge()).isGreaterThan(0);
        validateContainerResponseWithEncryption(containerProperties, containerResponse, clientEncryptionPolicy);
    }

    @DataProvider
    public static Object[][] analyticalTTLProvider() {
        return new Object[][]{
            // analytical ttl >= collection default ttl
            {-1},  // infinite ttl for data stored in analytical storage
            {0},   // analytics disabled
            {10},
            {null}
        };
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT, dataProvider = "analyticalTTLProvider", enabled = false)
    public void createContainer_withAnalyticalTTL(Integer analyticalTTL) throws Exception {
        // not working with emulator yet. TODO: enable when emulator has support for this.
        String collectionName = UUID.randomUUID().toString();
        CosmosContainerProperties containerProperties = new CosmosContainerProperties(collectionName, "/id");

        containerProperties.setAnalyticalStoreTimeToLiveInSeconds(analyticalTTL);
        if (analyticalTTL != null && analyticalTTL > 0) {
            containerProperties.setDefaultTimeToLiveInSeconds(analyticalTTL - 1);
        }

        CosmosContainerResponse containerResponse = createdDatabase.createContainer(containerProperties);
        this.createdContainer = createdDatabase.getContainer(collectionName);
        assertThat(containerResponse.getRequestCharge()).isGreaterThan(0);
        validateContainerResponse(containerProperties, containerResponse);

        assertThat(containerResponse.getProperties().getAnalyticalStoreTimeToLiveInSeconds()).isEqualTo(analyticalTTL);
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void createContainer_alreadyExists() throws Exception {
        String collectionName = UUID.randomUUID().toString();
        CosmosContainerProperties containerProperties = getCollectionDefinition(collectionName);

        CosmosContainerResponse containerResponse = createdDatabase.createContainer(containerProperties);
        this.createdContainer = createdDatabase.getContainer(collectionName);
        validateContainerResponse(containerProperties, containerResponse);

        try {
            createdDatabase.createContainer(containerProperties);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(CosmosException.class);
            assertThat(((CosmosException) e).getStatusCode()).isEqualTo(HttpConstants.StatusCodes.CONFLICT);
        }
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void createContainer_withThroughput() throws Exception {
        String collectionName = UUID.randomUUID().toString();
        CosmosContainerProperties containerProperties = getCollectionDefinition(collectionName);
        int throughput = 1000;

        CosmosContainerResponse containerResponse = createdDatabase.createContainer(containerProperties,
            ThroughputProperties.createManualThroughput(throughput));
        this.createdContainer = createdDatabase.getContainer(collectionName);
        validateContainerResponse(containerProperties, containerResponse);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void createContainer_withFullFidelityChangeFeedPolicy() throws Exception {
        String collectionName = UUID.randomUUID().toString();
        CosmosContainerProperties containerProperties = getCollectionDefinition(collectionName);
        containerProperties.setChangeFeedPolicy(
            ChangeFeedPolicy.createFullFidelityPolicy(
                Duration.ofMinutes(8)));
        int throughput = 1000;

        CosmosContainerResponse containerResponse = createdDatabase.createContainer(containerProperties,
            ThroughputProperties.createManualThroughput(throughput));
        this.createdContainer = createdDatabase.getContainer(collectionName);
        validateContainerResponse(containerProperties, containerResponse);
        assertThat(containerResponse.getProperties()).isNotNull();
        assertThat(containerResponse.getProperties().getChangeFeedPolicy()).isNotNull();
        assertThat(containerResponse.getProperties().getChangeFeedPolicy().getFullFidelityRetentionDuration())
            .isEqualTo(Duration.ofMinutes(8));
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void createContainer_withIncrementalChangeFeedPolicy() throws Exception {
        String collectionName = UUID.randomUUID().toString();
        CosmosContainerProperties containerProperties = getCollectionDefinition(collectionName);
        containerProperties.setChangeFeedPolicy(ChangeFeedPolicy.createIncrementalPolicy());
        int throughput = 1000;

        CosmosContainerResponse containerResponse = createdDatabase.createContainer(containerProperties,
            ThroughputProperties.createManualThroughput(throughput));
        this.createdContainer = createdDatabase.getContainer(collectionName);
        validateContainerResponse(containerProperties, containerResponse);
        assertThat(containerResponse.getProperties()).isNotNull();
        assertThat(containerResponse.getProperties().getChangeFeedPolicy()).isNotNull();
        assertThat(containerResponse.getProperties().getChangeFeedPolicy().getFullFidelityRetentionDuration())
            .isEqualTo(Duration.ZERO);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void createContainer_withDefaultChangeFeedPolicy() throws Exception {
        String collectionName = UUID.randomUUID().toString();
        CosmosContainerProperties containerProperties = getCollectionDefinition(collectionName);
        int throughput = 1000;

        CosmosContainerResponse containerResponse = createdDatabase.createContainer(containerProperties,
            ThroughputProperties.createManualThroughput(throughput));
        this.createdContainer = createdDatabase.getContainer(collectionName);
        validateContainerResponse(containerProperties, containerResponse);
        assertThat(containerResponse.getProperties()).isNotNull();
        assertThat(containerResponse.getProperties().getChangeFeedPolicy()).isNotNull();
        assertThat(containerResponse.getProperties().getChangeFeedPolicy().getFullFidelityRetentionDuration())
            .isEqualTo(Duration.ZERO);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void createContainer_withOptions() throws Exception {
        String collectionName = UUID.randomUUID().toString();
        CosmosContainerProperties containerProperties = getCollectionDefinition(collectionName);
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();

        CosmosContainerResponse containerResponse = createdDatabase.createContainer(containerProperties, options);
        this.createdContainer = createdDatabase.getContainer(collectionName);
        validateContainerResponse(containerProperties, containerResponse);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void createContainer_withThroughputAndOptions() throws Exception {
        String collectionName = UUID.randomUUID().toString();
        CosmosContainerProperties containerProperties = getCollectionDefinition(collectionName);
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();
        int throughput = 1000;

        CosmosContainerResponse containerResponse = createdDatabase.createContainer(containerProperties,
            throughput, options);
        this.createdContainer = createdDatabase.getContainer(collectionName);
        validateContainerResponse(containerProperties, containerResponse);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void createContainer_withNameAndPartitionKeyPath() throws Exception {
        String collectionName = UUID.randomUUID().toString();
        String partitionKeyPath = "/mypk";

        CosmosContainerResponse containerResponse = createdDatabase.createContainer(collectionName, partitionKeyPath);
        this.createdContainer = createdDatabase.getContainer(collectionName);
        validateContainerResponse(new CosmosContainerProperties(collectionName, partitionKeyPath), containerResponse);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void createContainer_withNamePartitionPathAndThroughput() throws Exception {
        String collectionName = UUID.randomUUID().toString();
        String partitionKeyPath = "/mypk";
        int throughput = 1000;

        CosmosContainerResponse containerResponse = createdDatabase.createContainer(collectionName,
            partitionKeyPath, ThroughputProperties.createManualThroughput(throughput));
        this.createdContainer = createdDatabase.getContainer(collectionName);
        validateContainerResponse(new CosmosContainerProperties(collectionName, partitionKeyPath), containerResponse);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void readContainer() throws Exception {
        String collectionName = UUID.randomUUID().toString();
        CosmosContainerProperties containerProperties = getCollectionDefinition(collectionName);
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();

        CosmosContainerResponse containerResponse = createdDatabase.createContainer(containerProperties);
        this.createdContainer = createdDatabase.getContainer(collectionName);

        CosmosContainer syncContainer = createdDatabase.getContainer(collectionName);

        CosmosContainerResponse read = syncContainer.read();
        validateContainerResponse(containerProperties, read);

        CosmosContainerResponse read1 = syncContainer.read(options);
        validateContainerResponse(containerProperties, read1);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void getFeedRanges() throws Exception {
        String collectionName = UUID.randomUUID().toString();
        CosmosContainerProperties containerProperties = getCollectionDefinition(collectionName);
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();

        CosmosContainerResponse containerResponse = createdDatabase.createContainer(containerProperties);
        this.createdContainer = createdDatabase.getContainer(collectionName);

        CosmosContainer syncContainer = createdDatabase.getContainer(collectionName);

        List<FeedRange> feedRanges = syncContainer.getFeedRanges();
        assertThat(feedRanges)
            .isNotNull()
            .hasSize(1);

        assertFeedRange(feedRanges.get(0), "{\"Range\":{\"min\":\"\",\"max\":\"FF\"}}");
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void trySplitRanges_for_NonExistingContainer() throws Exception {
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();
        CosmosAsyncContainer nonExistingContainer =
            createdDatabase.getContainer("NonExistingContainer").asyncContainer;

        CosmosException cosmosException = null;
        try {
            List<FeedRangeEpkImpl> splitFeedRanges = nonExistingContainer.trySplitFeedRange(
                FeedRange.forFullRange(),
                3
            ).block();
        } catch (CosmosException error) {
            cosmosException = error;
        }

        assertThat(cosmosException).isNotNull();
        assertThat(cosmosException.getStatusCode()).isEqualTo(404);
    }

    private void assertFeedRange(FeedRange feedRange, String expectedJson)
    {
        assertThat(((FeedRangeInternal)feedRange).toJson())
            .isNotNull()
            .isEqualTo(expectedJson);

        assertThat(feedRange.toString())
            .isNotNull()
            .isEqualTo(Base64.getUrlEncoder().encodeToString(expectedJson.getBytes(StandardCharsets.UTF_8)
            ));
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void getNormalizedFeedRanges_HashV1() {
        String collectionName = UUID.randomUUID().toString();
        CosmosContainerProperties containerProperties = getCollectionDefinition(collectionName);
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();
        createdDatabase.createContainer(containerProperties, options);
        this.createdContainer = createdDatabase.getContainer(collectionName);

        CosmosContainer syncContainer = createdDatabase.getContainer(collectionName);

        FeedRange fullRange = FeedRange.forFullRange();
        assertThat(syncContainer.asyncContainer.getNormalizedEffectiveRange(fullRange).block())
            .isNotNull()
            .isEqualTo(new Range<>("", "FF", true, false));

        Range<String> expectedRange = new Range<>("AA", "BB", true, false);
        FeedRange epkRange = new FeedRangeEpkImpl(expectedRange);
        assertThat(syncContainer.asyncContainer.getNormalizedEffectiveRange(epkRange).block())
            .isNotNull()
            .isEqualTo(expectedRange);

        FeedRange pointEpkRange = new FeedRangeEpkImpl(
            new Range<>("05C1D5AB55AB54", "05C1D5AB55AB54", true, true));
        assertThat(syncContainer.asyncContainer.getNormalizedEffectiveRange(pointEpkRange).block())
            .isNotNull()
            .isEqualTo(new Range<>("05C1D5AB55AB54", "05C1D5AB55AB55", true, false));

        FeedRange pkRangeIdRange = new FeedRangePartitionKeyRangeImpl("0");
        assertThat(syncContainer.asyncContainer.getNormalizedEffectiveRange(pkRangeIdRange).block())
            .isNotNull()
            .isEqualTo(new Range<>("", "FF", true, false));

        FeedRange logicalPartitionFeedRange = FeedRange.forLogicalPartition(new PartitionKey("Hello World"));
        assertThat(syncContainer.asyncContainer.getNormalizedEffectiveRange(logicalPartitionFeedRange).block())
            .isNotNull()
            .isEqualTo(new Range<>(
                "05C1C5D58F13B00849666D6D70215870736D6500",
                "05C1C5D58F13B00849666D6D70215870736D6501",
                true,
                false));
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void getNormalizedFeedRanges_HashV2() {
        String collectionName = UUID.randomUUID().toString();
        CosmosContainerProperties containerProperties = getCollectionDefinitionForHashV2(collectionName);
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();
        createdDatabase.createContainer(containerProperties, options);
        this.createdContainer = createdDatabase.getContainer(collectionName);

        CosmosContainer syncContainer = createdDatabase.getContainer(collectionName);

        FeedRange fullRange = FeedRange.forFullRange();
        assertThat(syncContainer.asyncContainer.getNormalizedEffectiveRange(fullRange).block())
            .isNotNull()
            .isEqualTo(new Range<>("", "FF", true, false));

        Range<String> expectedRange = new Range<>("AA", "BB", true, false);
        FeedRange epkRange = new FeedRangeEpkImpl(expectedRange);
        assertThat(syncContainer.asyncContainer.getNormalizedEffectiveRange(epkRange).block())
            .isNotNull()
            .isEqualTo(expectedRange);

        FeedRange pointEpkRange = new FeedRangeEpkImpl(
            new Range<>("05C1D5AB55AB54", "05C1D5AB55AB54", true, true));
        assertThat(syncContainer.asyncContainer.getNormalizedEffectiveRange(pointEpkRange).block())
            .isNotNull()
            .isEqualTo(new Range<>("05C1D5AB55AB54", "05C1D5AB55AB55", true, false));

        FeedRange pkRangeIdRange = new FeedRangePartitionKeyRangeImpl("0");
        assertThat(syncContainer.asyncContainer.getNormalizedEffectiveRange(pkRangeIdRange).block())
            .isNotNull()
            .isEqualTo(new Range<>("", "FF", true, false));

        FeedRange logicalPartitionFeedRange = FeedRange.forLogicalPartition(new PartitionKey("Hello World"));
        assertThat(syncContainer.asyncContainer.getNormalizedEffectiveRange(logicalPartitionFeedRange).block())
            .isNotNull()
            .isEqualTo(new Range<>(
                "306C52B42DECB3AE9D3C7586975E30B9",
                "306C52B42DECB3AE9D3C7586975E30BA",
                true,
                false));
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void getFeedRanges_withMultiplePartitions() throws Exception {
        String collectionName = UUID.randomUUID().toString();
        CosmosContainerProperties containerProperties = getCollectionDefinition(collectionName);
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();
        CosmosContainerResponse containerResponse = createdDatabase.createContainer(
            containerProperties,
            ThroughputProperties.createManualThroughput(18000));
        this.createdContainer = createdDatabase.getContainer(collectionName);

        CosmosContainer syncContainer = createdDatabase.getContainer(collectionName);

        List<FeedRange> feedRanges = syncContainer.getFeedRanges();
        assertThat(feedRanges)
            .isNotNull()
            .hasSize(3);

        assertFeedRange(feedRanges.get(0), "{\"Range\":{\"min\":\"\",\"max\":\"05C1D5AB55AB54\"}}");
        assertFeedRange(feedRanges.get(1), "{\"Range\":{\"min\":\"05C1D5AB55AB54\",\"max\":\"05C1E5AB55AB54\"}}");
        assertFeedRange(feedRanges.get(2), "{\"Range\":{\"min\":\"05C1E5AB55AB54\",\"max\":\"FF\"}}");

        Range<String> firstEpkRange = getEffectiveRange(syncContainer, feedRanges.get(0));
        Range<String> secondEpkRange = getEffectiveRange(syncContainer, feedRanges.get(1));
        Range<String> thirdEpkRange = getEffectiveRange(syncContainer, feedRanges.get(2));

        List<FeedRangeEpkImpl> feedRangesAfterSplit = syncContainer
            .asyncContainer
            .trySplitFeedRange(FeedRange.forFullRange(), 3)
            .block();
        assertThat(feedRangesAfterSplit)
            .isNotNull()
            .hasSize(3);

        String leftMin = getEffectiveRange(syncContainer, feedRangesAfterSplit.get(0)).getMin();
        String rightMin = firstEpkRange.getMin();
        String leftMax = getEffectiveRange(syncContainer, feedRangesAfterSplit.get(0)).getMax();
        String rightMax = firstEpkRange.getMax();

        assertThat(getEffectiveRange(syncContainer, feedRangesAfterSplit.get(0)).equals(firstEpkRange))
            .isTrue();

        assertThat(getEffectiveRange(syncContainer, feedRangesAfterSplit.get(1)).equals(secondEpkRange))
            .isTrue();

        assertThat(getEffectiveRange(syncContainer, feedRangesAfterSplit.get(2)).equals(thirdEpkRange))
            .isTrue();
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void getFeedRanges_withMultiplePartitions_HashV2() throws Exception {
        String collectionName = UUID.randomUUID().toString();
        CosmosContainerProperties containerProperties = getCollectionDefinitionForHashV2(collectionName);
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();
        CosmosContainerResponse containerResponse = createdDatabase.createContainer(
            containerProperties,
            ThroughputProperties.createManualThroughput(18000));
        this.createdContainer = createdDatabase.getContainer(collectionName);

        CosmosContainer syncContainer = createdDatabase.getContainer(collectionName);

        List<FeedRange> feedRanges = syncContainer.getFeedRanges();
        assertThat(feedRanges)
            .isNotNull()
            .hasSize(3);

        assertFeedRange(
            feedRanges.get(0),
            "{\"Range\":{\"min\":\"\",\"max\":\"15555555555555555555555555555555\"}}");
        assertFeedRange(
            feedRanges.get(1),
            "{\"Range\":{\"min\":\"15555555555555555555555555555555\"," +
            "\"max\":\"2AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"}}");
        assertFeedRange(
            feedRanges.get(2),
            "{\"Range\":{\"min\":\"2AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"max\":\"FF\"}}");

        Range<String> firstEpkRange = getEffectiveRange(syncContainer, feedRanges.get(0));
        Range<String> secondEpkRange = getEffectiveRange(syncContainer, feedRanges.get(1));
        Range<String> thirdEpkRange = getEffectiveRange(syncContainer, feedRanges.get(2));

        List<FeedRangeEpkImpl> feedRangesAfterSplit = syncContainer
            .asyncContainer
            .trySplitFeedRange(FeedRange.forFullRange(), 3)
            .block();
        assertThat(feedRangesAfterSplit)
            .isNotNull()
            .hasSize(3);

        String leftMin = getEffectiveRange(syncContainer, feedRangesAfterSplit.get(0)).getMin();
        String rightMin = firstEpkRange.getMin();
        String leftMax = getEffectiveRange(syncContainer, feedRangesAfterSplit.get(0)).getMax();
        String rightMax = firstEpkRange.getMax();

        assertThat(getEffectiveRange(syncContainer, feedRangesAfterSplit.get(0)).equals(firstEpkRange))
            .isTrue();

        assertThat(getEffectiveRange(syncContainer, feedRangesAfterSplit.get(1)).equals(secondEpkRange))
            .isTrue();

        assertThat(getEffectiveRange(syncContainer, feedRangesAfterSplit.get(2)).equals(thirdEpkRange))
            .isTrue();
    }

    private static Range<String> getEffectiveRange(CosmosContainer container, FeedRange feedRange) {
        AsyncDocumentClient clientWrapper = container.asyncContainer.getDatabase().getDocClientWrapper();
        return FeedRangeInternal
            .convert(feedRange)
            .getNormalizedEffectiveRange(
                clientWrapper.getPartitionKeyRangeCache(),
                null,
                Mono.just(Utils.ValueHolder.initialize(
                    clientWrapper.getCollectionCache().resolveByNameAsync(
                        null,
                        container.asyncContainer.getLink(),
                        null
                    ).block()))).block();
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void deleteContainer() throws Exception {
        String collectionName = UUID.randomUUID().toString();
        CosmosContainerProperties containerProperties = getCollectionDefinition(collectionName);
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();
        CosmosContainerResponse containerResponse = createdDatabase.createContainer(containerProperties);

        CosmosContainer syncContainer = createdDatabase.getContainer(collectionName);
        CosmosContainerResponse deleteResponse = syncContainer.delete();

    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void deleteContainer_withOptions() throws Exception {
        String collectionName = UUID.randomUUID().toString();
        CosmosContainerProperties containerProperties = getCollectionDefinition(collectionName);
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();
        CosmosContainerResponse containerResponse = createdDatabase.createContainer(containerProperties);

        CosmosContainer syncContainer = createdDatabase.getContainer(collectionName);
        CosmosContainerResponse deleteResponse = syncContainer.delete(options);

    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void replace() throws Exception {
        String collectionName = UUID.randomUUID().toString();
        CosmosContainerProperties containerProperties = getCollectionDefinition(collectionName);
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();

        CosmosContainerResponse containerResponse = createdDatabase.createContainer(containerProperties);
        validateContainerResponse(containerProperties, containerResponse);
        this.createdContainer = createdDatabase.getContainer(collectionName);

        assertThat(containerResponse.getProperties().getIndexingPolicy().getIndexingMode()).isEqualTo(IndexingMode.CONSISTENT);

        CosmosContainerResponse replaceResponse = createdDatabase.getContainer(containerProperties.getId())
                                                          .replace(containerResponse.getProperties().setIndexingPolicy(
                                                              new IndexingPolicy().setAutomatic(false).setIndexingMode(IndexingMode.NONE)));
        assertThat(replaceResponse.getProperties().getIndexingPolicy().getIndexingMode())
            .isEqualTo(IndexingMode.NONE);
        assertThat(replaceResponse.getProperties().getIndexingPolicy().isAutomatic())
            .isEqualTo(false);

        replaceResponse = createdDatabase.getContainer(containerProperties.getId())
                                                          .replace(containerResponse.getProperties().setIndexingPolicy(
                                                              new IndexingPolicy().setAutomatic(true).setIndexingMode(IndexingMode.CONSISTENT)),
                                                              options);
        assertThat(replaceResponse.getProperties().getIndexingPolicy().getIndexingMode())
            .isEqualTo(IndexingMode.CONSISTENT);
        assertThat(replaceResponse.getProperties().getIndexingPolicy().isAutomatic())
            .isEqualTo(true);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void enableFullFidelityChangeFeedForExistingContainer() throws Exception {
        String collectionName = UUID.randomUUID().toString();
        CosmosContainerProperties containerProperties = getCollectionDefinition(collectionName);
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();

        CosmosContainerResponse containerResponse = createdDatabase.createContainer(containerProperties);
        validateContainerResponse(containerProperties, containerResponse);
        this.createdContainer = createdDatabase.getContainer(collectionName);
        assertThat(containerResponse.getProperties()).isNotNull();
        assertThat(containerResponse.getProperties().getChangeFeedPolicy()).isNotNull();
        assertThat(containerResponse.getProperties().getChangeFeedPolicy().getFullFidelityRetentionDuration())
            .isEqualTo(Duration.ZERO);

        CosmosContainerResponse replaceResponse =
            createdDatabase.getContainer(containerProperties.getId())
                           .replace(containerResponse
                                 .getProperties()
                                 .setChangeFeedPolicy(
                                     ChangeFeedPolicy.createFullFidelityPolicy(Duration.ofMinutes(4))));
        assertThat(containerResponse.getProperties()).isNotNull();
        assertThat(containerResponse.getProperties().getChangeFeedPolicy()).isNotNull();
        assertThat(containerResponse.getProperties().getChangeFeedPolicy().getFullFidelityRetentionDuration())
            .isEqualTo(Duration.ofMinutes(4));
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void changeFullFidelityChangeFeedRetentionDurationForExistingContainer() throws Exception {
        String collectionName = UUID.randomUUID().toString();
        CosmosContainerProperties containerProperties = getCollectionDefinition(collectionName);
        containerProperties.setChangeFeedPolicy(ChangeFeedPolicy.createFullFidelityPolicy(Duration.ofMinutes(3)));
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();

        CosmosContainerResponse containerResponse = createdDatabase.createContainer(containerProperties);
        this.createdContainer = createdDatabase.getContainer(collectionName);
        validateContainerResponse(containerProperties, containerResponse);
        assertThat(containerResponse.getProperties()).isNotNull();
        assertThat(containerResponse.getProperties().getChangeFeedPolicy()).isNotNull();
        assertThat(containerResponse.getProperties().getChangeFeedPolicy().getFullFidelityRetentionDuration())
            .isEqualTo(Duration.ofMinutes(3));

        CosmosContainerResponse replaceResponse =
            createdDatabase.getContainer(containerProperties.getId())
                           .replace(containerResponse
                               .getProperties()
                               .setChangeFeedPolicy(
                                   ChangeFeedPolicy.createFullFidelityPolicy(Duration.ofMinutes(6))));
        assertThat(containerResponse.getProperties()).isNotNull();
        assertThat(containerResponse.getProperties().getChangeFeedPolicy()).isNotNull();
        assertThat(containerResponse.getProperties().getChangeFeedPolicy().getFullFidelityRetentionDuration())
            .isEqualTo(Duration.ofMinutes(6));
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void readAllContainers() throws Exception{
        String collectionName = UUID.randomUUID().toString();
        CosmosContainerProperties containerProperties = getCollectionDefinition(collectionName);
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();

        CosmosContainerResponse containerResponse = createdDatabase.createContainer(containerProperties);
        this.createdContainer = createdDatabase.getContainer(collectionName);
        CosmosPagedIterable<CosmosContainerProperties> feedResponseIterator = createdDatabase.readAllContainers();
        // Very basic validation
        assertThat(feedResponseIterator.iterator().hasNext()).isTrue();

        CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();
        CosmosPagedIterable<CosmosContainerProperties> feedResponseIterator1 = createdDatabase.readAllContainers(cosmosQueryRequestOptions);
        assertThat(feedResponseIterator1.iterator().hasNext()).isTrue();
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void crudMultiHashContainer() throws Exception {
        String collectionName = UUID.randomUUID().toString();

        PartitionKeyDefinition partitionKeyDefinition = new PartitionKeyDefinition();
        partitionKeyDefinition.setKind(PartitionKind.MULTI_HASH);
        partitionKeyDefinition.setVersion(PartitionKeyDefinitionVersion.V2);
        ArrayList<String> paths = new ArrayList<>();
        paths.add("/city");
        paths.add("/zipcode");
        partitionKeyDefinition.setPaths(paths);

        CosmosContainerProperties containerProperties = getCollectionDefinition(collectionName, partitionKeyDefinition);

        //MultiHash collection create
        CosmosContainerResponse containerResponse = createdDatabase.createContainer(containerProperties);
        validateContainerResponse(containerProperties, containerResponse);
        assertThat(containerResponse.getProperties().getPartitionKeyDefinition().getKind() == PartitionKind.MULTI_HASH);
        assertThat(containerResponse.getProperties().getPartitionKeyDefinition().getPaths().size() == paths.size());
        assertThat(containerResponse.getProperties().getPartitionKeyDefinition().getPaths().get(0) == paths.get(0));
        assertThat(containerResponse.getProperties().getPartitionKeyDefinition().getPaths().get(1) == paths.get(1));

        //MultiHash collection read
        CosmosContainer multiHashContainer = createdDatabase.getContainer(collectionName);
        containerResponse = multiHashContainer.read();
        validateContainerResponse(containerProperties, containerResponse);
        assertThat(containerResponse.getProperties().getPartitionKeyDefinition().getKind() == PartitionKind.MULTI_HASH);
        assertThat(containerResponse.getProperties().getPartitionKeyDefinition().getPaths().size() == paths.size());
        assertThat(containerResponse.getProperties().getPartitionKeyDefinition().getPaths().get(0) == paths.get(0));
        assertThat(containerResponse.getProperties().getPartitionKeyDefinition().getPaths().get(1) == paths.get(1));

        //MultiHash collection delete
        CosmosContainerResponse deleteResponse = multiHashContainer.delete();
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void queryContainer() throws Exception{
        String collectionName = UUID.randomUUID().toString();
        CosmosContainerProperties containerProperties = getCollectionDefinition(collectionName);
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();

        CosmosContainerResponse containerResponse = createdDatabase.createContainer(containerProperties);
        this.createdContainer = createdDatabase.getContainer(collectionName);
        String query = String.format("SELECT * from c where c.id = '%s'", collectionName);
        CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();

        CosmosPagedIterable<CosmosContainerProperties> feedResponseIterator = createdDatabase.queryContainers(query);
        // Very basic validation
        assertThat(feedResponseIterator.iterator().hasNext()).isTrue();

        CosmosPagedIterable<CosmosContainerProperties> feedResponseIterator1 =
            createdDatabase.queryContainers(query, cosmosQueryRequestOptions);
        // Very basic validation
        assertThat(feedResponseIterator1.iterator().hasNext()).isTrue();

        SqlQuerySpec querySpec = new SqlQuerySpec(query);
        CosmosPagedIterable<CosmosContainerProperties> feedResponseIterator2 =
            createdDatabase.queryContainers(querySpec);
        assertThat(feedResponseIterator2.iterator().hasNext()).isTrue();

        CosmosPagedIterable<CosmosContainerProperties> feedResponseIterator3 =
            createdDatabase.queryContainers(querySpec, cosmosQueryRequestOptions);
        assertThat(feedResponseIterator3.iterator().hasNext()).isTrue();
    }

    private void validateContainerResponse(CosmosContainerProperties containerProperties,
                                           CosmosContainerResponse createResponse) {
        // Basic validation
        assertThat(createResponse.getProperties().getId()).isNotNull();
        assertThat(createResponse.getProperties().getId())
            .as("check Resource Id")
            .isEqualTo(containerProperties.getId());

    }

    private void validateContainerResponseWithEncryption(CosmosContainerProperties containerProperties,
                                                         CosmosContainerResponse createResponse,
                                                         ClientEncryptionPolicy clientEncryptionPolicy) {
        validateContainerResponse(containerProperties, createResponse);
        assertThat(createResponse.getProperties().getClientEncryptionPolicy()).isNotNull();
        assertThat(createResponse.getProperties().getClientEncryptionPolicy().getIncludedPaths().size()).isEqualTo(clientEncryptionPolicy.getIncludedPaths().size());
        for (ClientEncryptionIncludedPath clientEncryptionIncludedPath :
            createResponse.getProperties().getClientEncryptionPolicy().getIncludedPaths()) {
            for (ClientEncryptionIncludedPath includedPath : clientEncryptionPolicy.getIncludedPaths()) {
                if (clientEncryptionIncludedPath.getPath().equals(includedPath.getPath())) {
                    assertThat(clientEncryptionIncludedPath.getClientEncryptionKeyId()).isEqualTo(includedPath.getClientEncryptionKeyId());
                    assertThat(clientEncryptionIncludedPath.getEncryptionAlgorithm()).isEqualTo(includedPath.getEncryptionAlgorithm());
                    assertThat(clientEncryptionIncludedPath.getEncryptionType()).isEqualTo(includedPath.getEncryptionType());
                    break;
                }
            }
        }
    }

    private void createEncryptionKey() {
        EncryptionKeyWrapMetadata encryptionKeyWrapMetadata = new EncryptionKeyWrapMetadata("key1", "tempmetadata1", "custom");
        byte[] key = decodeHexString(("34 62 52 77 f9 ee 11 9f 04 8c 6f 50 9c e4 c2 5b b3 39 f4 d0 4d c1 6a 32 fa 2b 3b aa " +
            "ae 1e d9 1c").replace(" ", ""));

        CosmosClientEncryptionKeyProperties cosmosClientEncryptionKeyProperties1 =
            new CosmosClientEncryptionKeyProperties("containerTestKey1", "AEAD_AES_256_CBC_HMAC_SHA256", key,
                encryptionKeyWrapMetadata);
        CosmosClientEncryptionKeyProperties cosmosClientEncryptionKeyProperties2 =
            new CosmosClientEncryptionKeyProperties("containerTestKey2", "AEAD_AES_256_CBC_HMAC_SHA256", key,
                encryptionKeyWrapMetadata);
        client.asyncClient().getDatabase(createdDatabase.getId()).createClientEncryptionKey(cosmosClientEncryptionKeyProperties1).block();
        client.asyncClient().getDatabase(createdDatabase.getId()).createClientEncryptionKey(cosmosClientEncryptionKeyProperties2).block();
    }
}
