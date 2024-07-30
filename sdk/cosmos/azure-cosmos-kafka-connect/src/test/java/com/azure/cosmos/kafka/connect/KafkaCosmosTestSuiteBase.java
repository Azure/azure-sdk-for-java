// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.GatewayConnectionConfig;
import com.azure.cosmos.ThrottlingRetryOptions;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerRequestOptions;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.IncludedPath;
import com.azure.cosmos.models.IndexingPolicy;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.models.ThroughputProperties;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.shaded.com.google.common.base.Strings;
import org.testng.ITest;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Listeners({KafkaCosmosTestNGLogListener.class})
public class KafkaCosmosTestSuiteBase implements ITest {
    protected static Logger logger = LoggerFactory.getLogger(KafkaCosmosTestSuiteBase.class.getSimpleName());
    protected static final int TIMEOUT = 60000;

    protected static final int SUITE_SETUP_TIMEOUT = 120000;
    protected static final int SUITE_SHUTDOWN_TIMEOUT = 60000;

    protected static final AzureKeyCredential credential;
    protected static String databaseName;
    protected static String multiPartitionContainerWithIdAsPartitionKeyName;
    protected static String multiPartitionContainerName;
    protected static String singlePartitionContainerName;
    private String testName;

    protected static CosmosAsyncDatabase getDatabase(CosmosAsyncClient client) {
        return client.getDatabase(databaseName);
    }

    protected static CosmosContainerProperties getMultiPartitionContainerWithIdAsPartitionKey(CosmosAsyncClient client) {
        return client
            .getDatabase(databaseName)
            .getContainer(multiPartitionContainerWithIdAsPartitionKeyName)
            .read()
            .block()
            .getProperties();
    }

    protected static CosmosContainerProperties getMultiPartitionContainer(CosmosAsyncClient client) {
        return client
            .getDatabase(databaseName)
            .getContainer(multiPartitionContainerName)
            .read()
            .block()
            .getProperties();
    }

    protected static CosmosContainerProperties getSinglePartitionContainer(CosmosAsyncClient client) {
        return client
            .getDatabase(databaseName)
            .getContainer(singlePartitionContainerName)
            .read()
            .block()
            .getProperties();
    }

    static {
        credential = new AzureKeyCredential(KafkaCosmosTestConfigurations.MASTER_KEY);
    }

    @BeforeSuite(groups = { "kafka", "kafka-integration", "kafka-emulator" }, timeOut = SUITE_SETUP_TIMEOUT)
    public static void beforeSuite() {

        logger.info("beforeSuite Started");
        try (CosmosAsyncClient houseKeepingClient = createGatewayHouseKeepingDocumentClient(true).buildAsyncClient()) {
            databaseName = createDatabase(houseKeepingClient);

            CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();
            multiPartitionContainerName =
                createCollection(
                    houseKeepingClient,
                    databaseName,
                    getCollectionDefinitionWithRangeRangeIndex(),
                    options,
                    10100);
            multiPartitionContainerWithIdAsPartitionKeyName =
                createCollection(
                    houseKeepingClient,
                    databaseName,
                    getCollectionDefinitionWithRangeRangeIndexWithIdAsPartitionKey(),
                    options,
                    10100);
            singlePartitionContainerName =
                createCollection(
                    houseKeepingClient,
                    databaseName,
                    getCollectionDefinitionWithRangeRangeIndex(),
                    options,
                    6000);
        }
    }

    @BeforeSuite(groups = { "unit" }, timeOut = SUITE_SETUP_TIMEOUT)
    public static void beforeSuiteUnit() {
        logger.info("beforeSuite for unit tests started");

        databaseName =
            StringUtils.isEmpty(databaseName) ? "KafkaCosmosTest-" + UUID.randomUUID() : databaseName;
        multiPartitionContainerName =
            StringUtils.isEmpty(multiPartitionContainerName) ? UUID.randomUUID().toString() : multiPartitionContainerName;
        singlePartitionContainerName =
            StringUtils.isEmpty(singlePartitionContainerName) ? UUID.randomUUID().toString() : singlePartitionContainerName;
    }

    @AfterSuite(groups = { "kafka", "kafka-integration", "kafka-emulator" }, timeOut = SUITE_SHUTDOWN_TIMEOUT)
    public static void afterSuite() {

        logger.info("afterSuite Started");

        try (CosmosAsyncClient houseKeepingClient = createGatewayHouseKeepingDocumentClient(true).buildAsyncClient()) {
            safeDeleteDatabase(houseKeepingClient, databaseName);
        }
    }


    static protected CosmosClientBuilder createGatewayHouseKeepingDocumentClient(boolean contentResponseOnWriteEnabled) {
        ThrottlingRetryOptions options = new ThrottlingRetryOptions();
        options.setMaxRetryWaitTime(Duration.ofSeconds(SUITE_SETUP_TIMEOUT));
        GatewayConnectionConfig gatewayConnectionConfig = new GatewayConnectionConfig();
        return new CosmosClientBuilder().endpoint(KafkaCosmosTestConfigurations.HOST)
            .credential(credential)
            .gatewayMode(gatewayConnectionConfig)
            .throttlingRetryOptions(options)
            .contentResponseOnWriteEnabled(contentResponseOnWriteEnabled)
            .consistencyLevel(ConsistencyLevel.SESSION);
    }

    private static String createDatabase(CosmosAsyncClient cosmosAsyncClient) {
        String databaseName = "KafkaCosmosTest-" + UUID.randomUUID();
        cosmosAsyncClient.createDatabase(databaseName).block();

        return databaseName;
    }

    private static String createCollection(
        CosmosAsyncClient cosmosAsyncClient,
        String database,
        CosmosContainerProperties cosmosContainerProperties,
        CosmosContainerRequestOptions options,
        int throughput) {

        cosmosAsyncClient
            .getDatabase(database)
            .createContainer(
                cosmosContainerProperties,
                ThroughputProperties.createManualThroughput(throughput),
                options)
            .block();

        // Creating a container is async - especially on multi-partition or multi-region accounts
        boolean isMultiRegional = ImplementationBridgeHelpers
            .CosmosAsyncClientHelper
            .getCosmosAsyncClientAccessor()
            .getPreferredRegions(cosmosAsyncClient).size() > 1;

        if (throughput > 6000 || isMultiRegional) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        return cosmosContainerProperties.getId();
    }

    static protected CosmosContainerProperties getCollectionDefinitionWithRangeRangeIndex() {
        return getCollectionDefinitionWithRangeRangeIndex(Collections.singletonList("/mypk"));
    }

    static protected CosmosContainerProperties getCollectionDefinitionWithRangeRangeIndexWithIdAsPartitionKey() {
        return getCollectionDefinitionWithRangeRangeIndex(Collections.singletonList("/id"));
    }

    public static void cleanUpContainer(CosmosAsyncClient client, String databaseName, String containerName) {
        CosmosAsyncContainer container = client.getDatabase(databaseName).getContainer(containerName);
        List<JsonNode> allItems =
            container.queryItems("select * from c", JsonNode.class)
                .byPage()
                .flatMapIterable(feedResponse -> feedResponse.getResults())
                .collectList()
                .block();

        // do a batch delete
        for (JsonNode item : allItems) {
            container.deleteItem(item, new CosmosItemRequestOptions()).block();
        }
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

    private static void safeDeleteDatabase(CosmosAsyncClient client, String database) {
        if (StringUtils.isNotEmpty(database)) {
            try {
                client.getDatabase(database).delete().block();
            } catch (Exception e) {
                logger.error("Failed to delete database {}", database, e);
            }
        }
    }

    @BeforeMethod(alwaysRun = true)
    public final void setTestName(Method method, Object[] row) {
        this.testName = Strings.lenientFormat("%s::%s",
            method.getDeclaringClass().getSimpleName(),
            method.getName());
    }

    @AfterMethod(alwaysRun = true)
    public final void unsetTestName() {
        this.testName = null;
    }

    @Override
    public String getTestName() {
        return this.testName;
    }
}
