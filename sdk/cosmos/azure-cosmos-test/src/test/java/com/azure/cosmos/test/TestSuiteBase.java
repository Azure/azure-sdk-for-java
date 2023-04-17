// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.test;


import com.azure.core.credential.AzureKeyCredential;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosBridgeInternal;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.GatewayConnectionConfig;
import com.azure.cosmos.ThrottlingRetryOptions;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.directconnectivity.Protocol;
import com.azure.cosmos.implementation.guava25.base.CaseFormat;
import com.azure.cosmos.implementation.guava25.collect.ImmutableList;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerRequestOptions;
import com.azure.cosmos.models.CosmosDatabaseProperties;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import com.azure.cosmos.models.IncludedPath;
import com.azure.cosmos.models.IndexingPolicy;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.azure.cosmos.BridgeInternal.extractConfigs;
import static com.azure.cosmos.BridgeInternal.injectConfigs;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;

@Listeners({TestNGLogListener.class})
public class TestSuiteBase extends CosmosAsyncClientTest {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    protected static Logger logger = LoggerFactory.getLogger(TestSuiteBase.class.getSimpleName());
    protected static final int TIMEOUT = 40000;
    protected static final int SETUP_TIMEOUT = 60000;
    public static final int SHUTDOWN_TIMEOUT = 24000;

    protected static final int SUITE_SETUP_TIMEOUT = 120000;
    protected static final int SUITE_SHUTDOWN_TIMEOUT = 60000;

    protected final static ConsistencyLevel accountConsistency;
    protected static final ImmutableList<String> preferredLocations;

    protected static final AzureKeyCredential credential;

    protected int subscriberValidationTimeout = TIMEOUT;

    private static CosmosAsyncDatabase SHARED_DATABASE;
    private static CosmosAsyncContainer SHARED_MULTI_PARTITION_COLLECTION_WITH_ID_AS_PARTITION_KEY;
    private static CosmosAsyncContainer SHARED_SINGLE_PARTITION_COLLECTION;

    public TestSuiteBase(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    protected static CosmosAsyncContainer getSharedMultiPartitionCosmosContainerWithIdAsPartitionKey(CosmosAsyncClient client) {
        return CosmosBridgeInternal.getCosmosContainerWithNewClient(SHARED_MULTI_PARTITION_COLLECTION_WITH_ID_AS_PARTITION_KEY, SHARED_DATABASE, client);
    }

    public static CosmosAsyncContainer getSharedSinglePartitionCosmosContainer(CosmosAsyncClient client) {
        return CosmosBridgeInternal.getCosmosContainerWithNewClient(SHARED_SINGLE_PARTITION_COLLECTION, SHARED_DATABASE, client);
    }

    static {
        accountConsistency = parseConsistency(TestConfigurations.CONSISTENCY);
        preferredLocations = immutableListOrNull(parsePreferredLocation(TestConfigurations.PREFERRED_LOCATIONS));

        //  Object mapper configurations
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        objectMapper.configure(JsonParser.Feature.ALLOW_TRAILING_COMMA, true);
        objectMapper.configure(JsonParser.Feature.STRICT_DUPLICATE_DETECTION, true);

        credential = new AzureKeyCredential(TestConfigurations.MASTER_KEY);
    }

    protected TestSuiteBase() {
        logger.debug("Initializing {} ...", this.getClass().getSimpleName());
    }

    private static <T> ImmutableList<T> immutableListOrNull(List<T> list) {
        return list != null ? ImmutableList.copyOf(list) : null;
    }

    private static class DatabaseManagerImpl implements CosmosDatabaseForTest.DatabaseManager {
        public static DatabaseManagerImpl getInstance(CosmosAsyncClient client) {
            return new DatabaseManagerImpl(client);
        }

        private final CosmosAsyncClient client;

        private DatabaseManagerImpl(CosmosAsyncClient client) {
            this.client = client;
        }

        @Override
        public CosmosPagedFlux<CosmosDatabaseProperties> queryDatabases(SqlQuerySpec query) {
            return client.queryDatabases(query, null);
        }

        @Override
        public Mono<CosmosDatabaseResponse> createDatabase(CosmosDatabaseProperties databaseDefinition) {
            return client.createDatabase(databaseDefinition);
        }

        @Override
        public CosmosAsyncDatabase getDatabase(String id) {
            return client.getDatabase(id);
        }
    }

    @BeforeSuite(groups = {"simple", "long", "direct", "multi-region", "multi-master", "emulator"}, timeOut = SUITE_SETUP_TIMEOUT)
    public static void beforeSuite() {

        logger.info("beforeSuite Started");

        try (CosmosAsyncClient houseKeepingClient = createGatewayHouseKeepingDocumentClient(true).buildAsyncClient()) {
            CosmosDatabaseForTest dbForTest = CosmosDatabaseForTest.create(DatabaseManagerImpl.getInstance(houseKeepingClient));
            SHARED_DATABASE = dbForTest.createdDatabase;
            CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();
            SHARED_MULTI_PARTITION_COLLECTION_WITH_ID_AS_PARTITION_KEY = createCollection(SHARED_DATABASE, getCollectionDefinitionWithRangeRangeIndexWithIdAsPartitionKey(), options, 10100);
            SHARED_SINGLE_PARTITION_COLLECTION = createCollection(SHARED_DATABASE, getCollectionDefinitionWithRangeRangeIndex(), options, 6000);
        }
    }

    @AfterSuite(groups = {"simple", "long", "direct", "multi-region", "multi-master", "emulator"}, timeOut = SUITE_SHUTDOWN_TIMEOUT)
    public static void afterSuite() {

        logger.info("afterSuite Started");

        try (CosmosAsyncClient houseKeepingClient = createGatewayHouseKeepingDocumentClient(true).buildAsyncClient()) {
            safeDeleteDatabase(SHARED_DATABASE);
            CosmosDatabaseForTest.cleanupStaleTestDatabases(DatabaseManagerImpl.getInstance(houseKeepingClient));
        }
    }

    public static CosmosAsyncContainer createCollection(CosmosAsyncDatabase database, CosmosContainerProperties cosmosContainerProperties,
                                                        CosmosContainerRequestOptions options, int throughput) {
        database.createContainer(cosmosContainerProperties, ThroughputProperties.createManualThroughput(throughput), options).block();
        return database.getContainer(cosmosContainerProperties.getId());
    }

    static protected CosmosContainerProperties getCollectionDefinitionWithRangeRangeIndexWithIdAsPartitionKey() {
        return getCollectionDefinitionWithRangeRangeIndex(Collections.singletonList("/id"));
    }

    static protected CosmosContainerProperties getCollectionDefinitionWithRangeRangeIndex() {
        return getCollectionDefinitionWithRangeRangeIndex(Collections.singletonList("/mypk"));
    }

    static protected CosmosContainerProperties getCollectionDefinitionWithRangeRangeIndex(List<String> partitionKeyPath) {
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();

        partitionKeyDef.setPaths(partitionKeyPath);
        IndexingPolicy indexingPolicy = new IndexingPolicy();
        List<IncludedPath> includedPaths = new ArrayList<>();
        IncludedPath includedPath = new IncludedPath("/*");
        includedPaths.add(includedPath);
        indexingPolicy.setIncludedPaths(includedPaths);

        CosmosContainerProperties cosmosContainerProperties = new CosmosContainerProperties(UUID.randomUUID().toString(), partitionKeyDef);
        cosmosContainerProperties.setIndexingPolicy(indexingPolicy);

        return cosmosContainerProperties;
    }

    static protected void safeDeleteDatabase(CosmosAsyncDatabase database) {
        if (database != null) {
            try {
                database.delete().block();
            } catch (Exception e) {
            }
        }
    }

    public static void safeClose(CosmosAsyncClient client) {
        if (client != null) {
            try {
                client.close();
            } catch (Exception e) {
                logger.error("failed to close client", e);
            }
        }
    }

    static ConsistencyLevel parseConsistency(String consistency) {
        if (consistency != null) {
            consistency = CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, consistency).trim();
            return ConsistencyLevel.valueOf(consistency);
        }

        logger.error("INVALID configured test consistency [{}].", consistency);
        throw new IllegalStateException("INVALID configured test consistency " + consistency);
    }

    static List<String> parsePreferredLocation(String preferredLocations) {
        if (StringUtils.isEmpty(preferredLocations)) {
            return null;
        }

        try {
            return objectMapper.readValue(preferredLocations, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            logger.error("INVALID configured test preferredLocations [{}].", preferredLocations);
            throw new IllegalStateException("INVALID configured test preferredLocations " + preferredLocations);
        }
    }

    @DataProvider
    public static Object[][] simpleClientBuildersWithJustDirectTcp() {
        return simpleClientBuildersWithDirect(false, true, true, Protocol.TCP);
    }

    private static Object[][] simpleClientBuildersWithDirect(
        boolean includeGateway,
        boolean contentResponseOnWriteEnabled,
        boolean retryOnThrottledRequests,
        Protocol... protocols) {

        logger.info("Max test consistency to use is [{}]", accountConsistency);
        List<ConsistencyLevel> testConsistencies = ImmutableList.of(ConsistencyLevel.EVENTUAL);

        boolean isMultiMasterEnabled = preferredLocations != null && accountConsistency == ConsistencyLevel.SESSION;

        List<CosmosClientBuilder> cosmosConfigurations = new ArrayList<>();

        for (Protocol protocol : protocols) {
            testConsistencies.forEach(consistencyLevel -> cosmosConfigurations.add(createDirectRxDocumentClient(
                consistencyLevel,
                protocol,
                isMultiMasterEnabled,
                preferredLocations,
                contentResponseOnWriteEnabled,
                retryOnThrottledRequests)));
        }

        cosmosConfigurations.forEach(c -> {
            ConnectionPolicy connectionPolicy = CosmosBridgeInternal.getConnectionPolicy(c);
            ConsistencyLevel consistencyLevel = CosmosBridgeInternal.getConsistencyLevel(c);
            logger.info("Will Use ConnectionMode [{}], Consistency [{}], Protocol [{}]",
                connectionPolicy.getConnectionMode(),
                consistencyLevel,
                extractConfigs(c).getProtocol()
            );
        });

        if (includeGateway) {
            cosmosConfigurations.add(
                createGatewayRxDocumentClient(
                    ConsistencyLevel.SESSION,
                    false,
                    null,
                    contentResponseOnWriteEnabled,
                    retryOnThrottledRequests));
        }

        return cosmosConfigurations.stream().map(b -> new Object[]{b}).collect(Collectors.toList()).toArray(new Object[0][]);
    }

    static protected CosmosClientBuilder createGatewayHouseKeepingDocumentClient(boolean contentResponseOnWriteEnabled) {
        ThrottlingRetryOptions options = new ThrottlingRetryOptions();
        options.setMaxRetryWaitTime(Duration.ofSeconds(SUITE_SETUP_TIMEOUT));
        GatewayConnectionConfig gatewayConnectionConfig = new GatewayConnectionConfig();
        return new CosmosClientBuilder().endpoint(TestConfigurations.HOST)
            .credential(credential)
            .gatewayMode(gatewayConnectionConfig)
            .throttlingRetryOptions(options)
            .contentResponseOnWriteEnabled(contentResponseOnWriteEnabled)
            .consistencyLevel(ConsistencyLevel.SESSION);
    }

    static protected CosmosClientBuilder createGatewayRxDocumentClient(
        ConsistencyLevel consistencyLevel,
        boolean multiMasterEnabled,
        List<String> preferredRegions,
        boolean contentResponseOnWriteEnabled,
        boolean retryOnThrottledRequests) {

        return createGatewayRxDocumentClient(
            TestConfigurations.HOST,
            consistencyLevel,
            multiMasterEnabled,
            preferredRegions,
            contentResponseOnWriteEnabled,
            retryOnThrottledRequests);
    }

    static protected CosmosClientBuilder createGatewayRxDocumentClient(
        String endpoint,
        ConsistencyLevel consistencyLevel,
        boolean multiMasterEnabled,
        List<String> preferredRegions,
        boolean contentResponseOnWriteEnabled,
        boolean retryOnThrottledRequests) {

        GatewayConnectionConfig gatewayConnectionConfig = new GatewayConnectionConfig();
        CosmosClientBuilder builder = new CosmosClientBuilder().endpoint(endpoint)
            .credential(credential)
            .gatewayMode(gatewayConnectionConfig)
            .multipleWriteRegionsEnabled(multiMasterEnabled)
            .preferredRegions(preferredRegions)
            .contentResponseOnWriteEnabled(contentResponseOnWriteEnabled)
            .consistencyLevel(consistencyLevel);

        if (!retryOnThrottledRequests) {
            builder.throttlingRetryOptions(new ThrottlingRetryOptions().setMaxRetryAttemptsOnThrottledRequests(0));
        }

        return builder;
    }

    static protected CosmosClientBuilder createDirectRxDocumentClient(ConsistencyLevel consistencyLevel,
                                                                      Protocol protocol,
                                                                      boolean multiMasterEnabled,
                                                                      List<String> preferredRegions,
                                                                      boolean contentResponseOnWriteEnabled,
                                                                      boolean retryOnThrottledRequests) {
        CosmosClientBuilder builder = new CosmosClientBuilder().endpoint(TestConfigurations.HOST)
            .credential(credential)
            .directMode(DirectConnectionConfig.getDefaultConfig())
            .contentResponseOnWriteEnabled(contentResponseOnWriteEnabled)
            .consistencyLevel(consistencyLevel);

        if (preferredRegions != null) {
            builder.preferredRegions(preferredRegions);
        }

        if (multiMasterEnabled && consistencyLevel == ConsistencyLevel.SESSION) {
            builder.multipleWriteRegionsEnabled(true);
        }

        if (!retryOnThrottledRequests) {
            builder.throttlingRetryOptions(new ThrottlingRetryOptions().setMaxRetryAttemptsOnThrottledRequests(0));
        }

        Configs configs = spy(new Configs());
        doAnswer((Answer<Protocol>) invocation -> protocol).when(configs).getProtocol();

        return injectConfigs(builder, configs);
    }
}
