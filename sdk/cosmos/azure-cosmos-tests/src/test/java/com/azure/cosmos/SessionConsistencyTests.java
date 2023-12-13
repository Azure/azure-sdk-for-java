package com.azure.cosmos;

import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.DatabaseAccountLocation;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.PartitionKeyBasedBloomFilter;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.TestSuiteBase;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.implementation.guava25.base.Charsets;
import com.azure.cosmos.implementation.guava25.hash.BloomFilter;
import com.azure.cosmos.implementation.guava25.hash.Funnel;
import com.azure.cosmos.implementation.guava25.hash.Funnels;
import com.azure.cosmos.implementation.guava25.hash.PrimitiveSink;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import org.assertj.core.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SessionConsistencyTests extends TestSuiteBase {

    private static final Logger logger = LoggerFactory.getLogger(SessionConsistencyTests.class);
    private Map<String, String> writeRegionMap;

    @BeforeClass
    public void beforeClass() {

        try (CosmosAsyncClient tempClient = createClient()) {

            RxDocumentClientImpl rxDocumentClient = (RxDocumentClientImpl) ReflectionUtils.getAsyncDocumentClient(tempClient);
            GlobalEndpointManager globalEndpointManager = ReflectionUtils.getGlobalEndpointManager(rxDocumentClient);
            DatabaseAccount databaseAccount = globalEndpointManager.getLatestDatabaseAccount();

            this.writeRegionMap = getRegionMap(databaseAccount, true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 1. execute a create operation
    // 2. validate where the session token is injected in direct / gateway mode
    // 3. specific to direct mode -> validate the session token part of request headers
//    @Test
//    public void validateSessionTokenAtRntbdLayer() {
//
//        System.setProperty("COSMOS.PARTITION_KEY_SCOPED_SESSION_TOKEN_CAPTURING_ENABLED", "true");
//
//        TestObject testObjectToCreate = TestObject.create();
//
//        try {
//            CosmosItemResponse<TestObject> createResponse = container.createItem(testObjectToCreate).block();
//
//            Assertions.assertThat(createResponse).isNotNull();
//            Assertions.assertThat(createResponse.getSessionToken()).isNotNull();
//
//            logger.info("Session token from creation : {}", createResponse.getSessionToken());
//
//            CosmosItemResponse<TestObject> readResponse = container
//                .readItem(testObjectToCreate.getId(), new PartitionKey(testObjectToCreate.getMypk()), TestObject.class)
//                .block();
//
//            Assertions.assertThat(readResponse).isNotNull();
//            Assertions.assertThat(readResponse.getSessionToken()).isNotNull();
//
//            logger.info("Session token from read : {}", readResponse.getSessionToken());
//        } catch (Exception e) {
//
//        } finally {
//            System.clearProperty("COSMOS.PARTITION_KEY_SCOPED_SESSION_TOKEN_CAPTURING_ENABLED");
//        }
//    }

//    @Test
//    public void validatePkScopedSessionTokenMapUsage() {
//        System.setProperty("COSMOS.PARTITION_KEY_SCOPED_SESSION_TOKEN_CAPTURING_ENABLED", "true");
//
//        TestObject testObjectToCreate = TestObject.create();
//
//        try {
//            CosmosItemResponse<TestObject> createResponse = container.createItem(testObjectToCreate).block();
//
//            Assertions.assertThat(createResponse).isNotNull();
//            Assertions.assertThat(createResponse.getSessionToken()).isNotNull();
//
//            logger.info("Session token from creation : {}", createResponse.getSessionToken());
//
//            CosmosItemResponse<TestObject> readResponse = container
//                .readItem(testObjectToCreate.getId(), new PartitionKey(testObjectToCreate.getMypk()), TestObject.class)
//                .block();
//
//            Assertions.assertThat(readResponse).isNotNull();
//            Assertions.assertThat(readResponse.getSessionToken()).isNotNull();
//
//            logger.info("Session token from read : {}", readResponse.getSessionToken());
//        } catch (Exception e) {
//
//        } finally {
//            System.clearProperty("COSMOS.PARTITION_KEY_SCOPED_SESSION_TOKEN_CAPTURING_ENABLED");
//        }
//    }

    // 1. Create a client which allows PK tracking in a bloom filter
    @Test
    public void testSessionTokenUsageWithBloomFilterPkTracking() {
        CosmosClientBuilder clientBuilder = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .partitionKeyScopedSessionCapturingEnabled(true)
            .multipleWriteRegionsEnabled(true)
            .consistencyLevel(ConsistencyLevel.SESSION)
            .preferredRegions(this.writeRegionMap.keySet().stream().toList())
            .sessionRetryOptions(new SessionRetryOptionsBuilder()
                .regionSwitchHint(CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED)
                .build());

        CosmosAsyncDatabase database = null;
        CosmosAsyncContainer container = null;
        String databaseId = UUID.randomUUID().toString();
        String containerId = UUID.randomUUID().toString();

        try (CosmosAsyncClient client = clientBuilder.buildAsyncClient()) {

            logger.info("Creating database with id : {}", databaseId);

            client.createDatabaseIfNotExists(databaseId).block();
            database = client.getDatabase(databaseId);

            logger.info("Creating container with id : {}", containerId);

            database.createContainerIfNotExists(containerId, "/mypk").block();
            container = database.getContainer(containerId);

            // allow some time for collection to be available for read
            Thread.sleep(30_000);

            TestObject testObjectToCreate = TestObject.create();

            CosmosItemResponse<TestObject> createResponse = container.createItem(testObjectToCreate).block();

            Assertions.assertThat(createResponse).isNotNull();
            Assertions.assertThat(createResponse.getSessionToken()).isNotNull();

            logger.info("Session token from creation : {}", createResponse.getSessionToken());

            CosmosItemResponse<TestObject> readResponse = container
                .readItem(testObjectToCreate.getId(), new PartitionKey(testObjectToCreate.getMypk()), TestObject.class)
                .block();

            Assertions.assertThat(readResponse).isNotNull();
            Assertions.assertThat(readResponse.getSessionToken()).isNotNull();

            logger.info("Session token from read : {}", readResponse.getSessionToken());
        } catch (Exception e) {

            logger.error("Exception thrown : ", e);

        } finally {

            if (container != null) {
                container.delete().block();
            }

            if (database != null) {
                database.delete().block();
            }

        }
    }

    @Test
    public void testBloomFilterSetup() {

        Funnel<PartitionKeyBasedBloomFilter.PartitionKeyBasedBloomFilterType> pkBasedTypeFunnel = new Funnel<PartitionKeyBasedBloomFilter.PartitionKeyBasedBloomFilterType>() {
            @Override
            public void funnel(PartitionKeyBasedBloomFilter.PartitionKeyBasedBloomFilterType from, PrimitiveSink into) {
                into
                    .putLong(from.getCollectionRid())
                    .putString(from.getPartitionKeyAsStringifiedJson(), Charsets.UTF_8)
                    .putString(from.getRegion(), Charsets.UTF_8);
            }
        };

        BloomFilter<PartitionKeyBasedBloomFilter.PartitionKeyBasedBloomFilterType> stringBasedBloomFilter = BloomFilter.create(pkBasedTypeFunnel, 10_000, 0.001);

        stringBasedBloomFilter.put(new PartitionKeyBasedBloomFilter.PartitionKeyBasedBloomFilterType("pk1", "eastus", 1L));

        Assertions.assertThat(stringBasedBloomFilter.mightContain(new PartitionKeyBasedBloomFilter.PartitionKeyBasedBloomFilterType("pk1", "eastus", 1L))).isTrue();
        Assertions.assertThat(stringBasedBloomFilter.mightContain(new PartitionKeyBasedBloomFilter.PartitionKeyBasedBloomFilterType("pk2", "eastus", 1L))).isFalse();
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
}
