package com.azure.cosmos;

import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.DatabaseAccountLocation;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.PartitionKeyBasedBloomFilter;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.implementation.guava25.base.Charsets;
import com.azure.cosmos.implementation.guava25.collect.ImmutableList;
import com.azure.cosmos.implementation.guava25.hash.BloomFilter;
import com.azure.cosmos.implementation.guava25.hash.Funnel;
import com.azure.cosmos.implementation.guava25.hash.PrimitiveSink;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.models.ThroughputResponse;
import com.azure.cosmos.rx.TestSuiteBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

public class SessionConsistencyWithRegionScopingTests extends TestSuiteBase {

    private static final Logger logger = LoggerFactory.getLogger(SessionConsistencyWithRegionScopingTests.class);
    private static final ImplementationBridgeHelpers.CosmosClientBuilderHelper.CosmosClientBuilderAccessor cosmosClientBuilderAccessor
        = ImplementationBridgeHelpers.CosmosClientBuilderHelper.getCosmosClientBuilderAccessor();
    private Map<String, String> writeRegionMap;
    private Map<String, String> readRegionMap;

    @BeforeClass
    public void beforeClass() {

        try (CosmosAsyncClient tempClient = createClient()) {

            RxDocumentClientImpl rxDocumentClient = (RxDocumentClientImpl) ReflectionUtils.getAsyncDocumentClient(tempClient);
            GlobalEndpointManager globalEndpointManager = ReflectionUtils.getGlobalEndpointManager(rxDocumentClient);
            DatabaseAccount databaseAccount = globalEndpointManager.getLatestDatabaseAccount();

            this.writeRegionMap = getRegionMap(databaseAccount, true);
            this.readRegionMap = getRegionMap(databaseAccount, false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 1. Set the required boolean to capture region-specific session token.
    // 2. Create a container with 1 physical partition.
    // 3. Perform a point create on the first preferred region.
    // 4. Perform a point read on the item created in step 3 from the first preferred region.
    @Test(groups = {"multi-region"})
    public void pointReadYourPointCreate_BothFromFirstPreferredRegion() throws InterruptedException {
        CosmosClientBuilder clientBuilder = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .preferredRegions(this.readRegionMap.keySet().stream().toList());

        cosmosClientBuilderAccessor.setRegionScopedSessionCapturingEnabled(clientBuilder, true);

        Thread.sleep(10_000);

        CosmosAsyncClient client = clientBuilder.buildAsyncClient();
        CosmosAsyncContainer containerWithSinglePartition = getSharedSinglePartitionCosmosContainer(client);

        try {
            TestObject testObjectToBeCreated = TestObject.create();

            String id = testObjectToBeCreated.getId();
            String pk = testObjectToBeCreated.getMypk();

            containerWithSinglePartition.createItem(testObjectToBeCreated).block();
            CosmosItemResponse<TestObject> testObjectFromRead = containerWithSinglePartition.readItem(id, new PartitionKey(pk), TestObject.class).block();

            assertThat(testObjectFromRead).isNotNull();
            validateTestObjectEquality(testObjectToBeCreated, testObjectFromRead.getItem());
        } catch (Exception ex) {
            logger.error("Exception");
        } finally {
            safeClose(client);
        }
    }

    @Test(groups = {"multi-region"})
    public void pointReadAfterPartitionSplitAndPointCreate_BothFromFirstPreferredRegion() throws InterruptedException {
        CosmosClientBuilder clientBuilder = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .preferredRegions(this.readRegionMap.keySet().stream().toList());

        cosmosClientBuilderAccessor.setRegionScopedSessionCapturingEnabled(clientBuilder, true);

        CosmosAsyncClient client = clientBuilder.buildAsyncClient();

        String databaseId = UUID.randomUUID() + "-" + "db";
        String containerId = UUID.randomUUID() + "-" + "container";

        client.createDatabase(databaseId).block();
        CosmosAsyncDatabase asyncDatabase = client.getDatabase(databaseId);
        asyncDatabase.createContainerIfNotExists(new CosmosContainerProperties(containerId, "/mypk")).block();
        CosmosAsyncContainer asyncContainer = asyncDatabase.getContainer(containerId);

        Thread.sleep(10_000);

        try {
            TestObject testObjectToBeCreated = TestObject.create();

            String id = testObjectToBeCreated.getId();
            String pk = testObjectToBeCreated.getMypk();

            asyncContainer.createItem(testObjectToBeCreated).block();

            ThroughputResponse throughputResponse = asyncContainer.replaceThroughput(ThroughputProperties.createManualThroughput(10_100)).block();

            while (true) {
                assert throughputResponse != null;
                boolean isReplacePending = asyncContainer.readThroughput().block().isReplacePending();
                if (!isReplacePending) {
                    break;
                }
                Thread.sleep(10_000);
                logger.info("Waiting for split to complete...");
            }

            logger.info("Split complete!");
            CosmosItemResponse<TestObject> testObjectFromRead = asyncContainer.readItem(id, new PartitionKey(pk), TestObject.class).block();

            assertThat(testObjectFromRead).isNotNull();
            validateTestObjectEquality(testObjectToBeCreated, testObjectFromRead.getItem());
        } catch (Exception ex) {
            logger.error("Exception : ", ex);
        } finally {
            safeDeleteCollection(asyncContainer);
            safeDeleteDatabase(asyncDatabase);
            safeClose(client);
        }
    }

    @Test(groups = {"multi-region"})
    public void pointReadYourPointCreate_CreateFromFirstPreferredRegionReadFromSecondPreferredRegion() throws InterruptedException {

        List<String> preferredRegions = this.readRegionMap.keySet().stream().toList();

        CosmosClientBuilder clientBuilder = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .preferredRegions(preferredRegions);

        cosmosClientBuilderAccessor.setRegionScopedSessionCapturingEnabled(clientBuilder, true);

        Thread.sleep(10_000);

        CosmosAsyncClient client = clientBuilder.buildAsyncClient();
        CosmosAsyncContainer containerWithSinglePartition = getSharedSinglePartitionCosmosContainer(client);

        try {
            TestObject testObjectToBeCreated = TestObject.create();

            String id = testObjectToBeCreated.getId();
            String pk = testObjectToBeCreated.getMypk();

            containerWithSinglePartition.createItem(testObjectToBeCreated).block();

            Thread.sleep(1_000);

            CosmosItemResponse<TestObject> testObjectFromRead = containerWithSinglePartition.readItem(id, new PartitionKey(pk), new CosmosItemRequestOptions().setExcludedRegions(ImmutableList.of(preferredRegions.get(0))), TestObject.class).block();

            assertThat(testObjectFromRead).isNotNull();
            validateTestObjectEquality(testObjectToBeCreated, testObjectFromRead.getItem());
        } catch (Exception ex) {
            logger.error("Exception");
        } finally {
            safeClose(client);
        }
    }

    @Test(groups = {"multi-region"})
    public void pointReadYourLatestUpsert_UpsertsFromPreferredRegionReadFromPreferredRegion() throws InterruptedException {
        List<String> preferredRegions = this.readRegionMap.keySet().stream().toList();

        CosmosClientBuilder clientBuilder = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .preferredRegions(preferredRegions);

        cosmosClientBuilderAccessor.setRegionScopedSessionCapturingEnabled(clientBuilder, true);

        Thread.sleep(10_000);

        CosmosAsyncClient client = clientBuilder.buildAsyncClient();
        CosmosAsyncContainer containerWithSinglePartition = getSharedSinglePartitionCosmosContainer(client);

        try {
            TestObject testObjectToBeCreated = TestObject.create();

            String id = testObjectToBeCreated.getId();
            String pk = testObjectToBeCreated.getMypk();

            containerWithSinglePartition.createItem(testObjectToBeCreated).block();

            testObjectToBeCreated.setStringProp(UUID.randomUUID().toString());

            TestObject testObjectModified = testObjectToBeCreated;

            containerWithSinglePartition.upsertItem(testObjectModified, new PartitionKey(pk), new CosmosItemRequestOptions()).block();

            CosmosItemResponse<TestObject> testObjectFromRead = containerWithSinglePartition.readItem(id, new PartitionKey(pk), TestObject.class).block();

            assertThat(testObjectFromRead).isNotNull();
            validateTestObjectEquality(testObjectModified, testObjectFromRead.getItem());
        } catch (Exception ex) {
            logger.error("Exception");
        } finally {
            safeClose(client);
        }
    }

    @Test(groups = {"multi-region"})
    public void queryTargetedToLogicalPartitionFollowingCreates_queryFromFirstPreferredRegionCreateInFirstPreferredRegion() throws InterruptedException {
        List<String> preferredRegions = this.readRegionMap.keySet().stream().toList();

        CosmosClientBuilder clientBuilder = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .preferredRegions(preferredRegions);

        cosmosClientBuilderAccessor.setRegionScopedSessionCapturingEnabled(clientBuilder, true);

        Thread.sleep(10_000);

        CosmosAsyncClient client = clientBuilder.buildAsyncClient();
        CosmosAsyncContainer containerWithSinglePartition = getSharedSinglePartitionCosmosContainer(client);

        try {
            TestObject testObjectToBeCreated = TestObject.create();

            String id = testObjectToBeCreated.getId();
            String pk = testObjectToBeCreated.getMypk();

            containerWithSinglePartition.createItem(testObjectToBeCreated).block();

            SqlQuerySpec querySpec = new SqlQuerySpec("SELECT * FROM c WHERE c.id = @param1 and c.mypk = @param2");

            querySpec.setParameters(ImmutableList.of(new SqlParameter("@param1", id), new SqlParameter("@param2", pk)));

            List<TestObject> testObjectsFromQuery = containerWithSinglePartition.queryItems(querySpec, TestObject.class).collectList().block();

            assertThat(testObjectsFromQuery).isNotNull();
            assertThat(testObjectsFromQuery.size()).isEqualTo(1);

            validateTestObjectEquality(testObjectToBeCreated, testObjectsFromQuery.get(0));
        } catch (Exception ex) {
            logger.error("Exception");
        } finally {
            safeClose(client);
        }
    }

    @Test(groups = {"multi-region"})
    public void crossPartitionedQueryFollowingCreates_queryFromFirstPreferredRegionCreatesInFirstPreferredRegion() throws InterruptedException {
        List<String> preferredRegions = this.readRegionMap.keySet().stream().toList();
        Map<String, TestObject> idToTestObjects = new HashMap<>();

        CosmosClientBuilder clientBuilder = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .preferredRegions(preferredRegions)
            .consistencyLevel(ConsistencyLevel.SESSION);

        cosmosClientBuilderAccessor.setRegionScopedSessionCapturingEnabled(clientBuilder, true);

        Thread.sleep(10_000);

        CosmosAsyncClient client = clientBuilder.buildAsyncClient();
        CosmosAsyncContainer containerWithMultiplePartitions = getSharedMultiPartitionCosmosContainer(client);

        try {
            TestObject testObjectToBeCreated1 = TestObject.create();
            idToTestObjects.put(testObjectToBeCreated1.getId(), testObjectToBeCreated1);

            containerWithMultiplePartitions.createItem(testObjectToBeCreated1).block();

            TestObject testObjectToBeCreated2 = TestObject.create();
            idToTestObjects.put(testObjectToBeCreated2.getId(), testObjectToBeCreated2);

            containerWithMultiplePartitions.createItem(testObjectToBeCreated2).block();

            SqlQuerySpec querySpec = new SqlQuerySpec("SELECT * FROM c");

            List<TestObject> testObjectsFromQuery = containerWithMultiplePartitions.queryItems(querySpec, TestObject.class).collectList().block();

            assertThat(testObjectsFromQuery).isNotNull();
            assertThat(testObjectsFromQuery.size()).isEqualTo(2);

            for (TestObject testObjectQueried : testObjectsFromQuery) {
                validateTestObjectEquality(testObjectQueried, idToTestObjects.get(testObjectQueried.getId()));
            }
        } catch (Exception ex) {
            logger.error("Exception");
        } finally {
            safeClose(client);
        }
    }


    @Test
    public void testBloomFilterSetup() {

        Funnel<PartitionKeyBasedBloomFilter.PartitionKeyBasedBloomFilterType> pkBasedTypeFunnel = new Funnel<PartitionKeyBasedBloomFilter.PartitionKeyBasedBloomFilterType>() {
            @Override
            public void funnel(PartitionKeyBasedBloomFilter.PartitionKeyBasedBloomFilterType from, PrimitiveSink into) {
                into
                    .putLong(from.getCollectionRid())
                    .putString(from.getEffectivePartitionKeyString(), Charsets.UTF_8)
                    .putString(from.getRegion(), Charsets.UTF_8);
            }
        };

        BloomFilter<PartitionKeyBasedBloomFilter.PartitionKeyBasedBloomFilterType> partitionKeyBasedBloomFilter = BloomFilter.create(pkBasedTypeFunnel, 10_000, 0.001);

        partitionKeyBasedBloomFilter.put(new PartitionKeyBasedBloomFilter.PartitionKeyBasedBloomFilterType("pk1", "eastus", 1L));

        assertThat(partitionKeyBasedBloomFilter.mightContain(new PartitionKeyBasedBloomFilter.PartitionKeyBasedBloomFilterType("pk1", "eastus", 1L))).isTrue();
        assertThat(partitionKeyBasedBloomFilter.mightContain(new PartitionKeyBasedBloomFilter.PartitionKeyBasedBloomFilterType("pk2", "eastus", 1L))).isFalse();
    }

    @AfterClass
    public void afterClass() {

    }

    private static CosmosAsyncClient createClient() {

        CosmosClientBuilder clientBuilder = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .consistencyLevel(ConsistencyLevel.SESSION)
            .directMode()
            .sessionRetryOptions(
                new SessionRetryOptionsBuilder()
                    .regionSwitchHint(CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED)
                    .build()
            );

        return clientBuilder.buildAsyncClient();
    }

    private Map<String, String> getRegionMap(DatabaseAccount databaseAccount, boolean writeOnly) {
        Iterator<DatabaseAccountLocation> locationIterator =
            writeOnly ? databaseAccount.getWritableLocations().iterator() : databaseAccount.getReadableLocations().iterator();
        Map<String, String> regionMap = new ConcurrentHashMap<>();

        while (locationIterator.hasNext()) {
            DatabaseAccountLocation accountLocation = locationIterator.next();
            regionMap.put(accountLocation.getName(), accountLocation.getEndpoint());
        }

        return regionMap;
    }

    private static void validateTestObjectEquality(TestObject testObject1, TestObject testObject2) {
        assertThat(testObject1.getId()).isEqualTo(testObject2.getId());
        assertThat(testObject1.getMypk()).isEqualTo(testObject2.getMypk());
        assertThat(testObject1.getStringProp()).isEqualTo(testObject2.getStringProp());
    }
}
