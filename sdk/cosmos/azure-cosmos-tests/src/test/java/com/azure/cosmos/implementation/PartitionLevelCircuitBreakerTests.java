// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;


import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.TestObject;
import com.azure.cosmos.faultinjection.FaultInjectionTestBase;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.test.faultinjection.CosmosFaultInjectionHelper;
import com.azure.cosmos.test.faultinjection.FaultInjectionCondition;
import com.azure.cosmos.test.faultinjection.FaultInjectionConditionBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionConnectionType;
import com.azure.cosmos.test.faultinjection.FaultInjectionEndpointBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionOperationType;
import com.azure.cosmos.test.faultinjection.FaultInjectionResultBuilders;
import com.azure.cosmos.test.faultinjection.FaultInjectionRule;
import com.azure.cosmos.test.faultinjection.FaultInjectionRuleBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorResult;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorType;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.testng.Assert.fail;


public class PartitionLevelCircuitBreakerTests extends FaultInjectionTestBase {

    private List<String> writeRegions;

    @Factory(dataProvider = "clientBuildersWithDirectTcpSession")
    public PartitionLevelCircuitBreakerTests(CosmosClientBuilder cosmosClientBuilder) {
        super(cosmosClientBuilder);
    }

    @BeforeClass(groups = {"multi-master"})
    public void beforeClass() {
        try (CosmosAsyncClient testClient = getClientBuilder().buildAsyncClient()) {
            RxDocumentClientImpl documentClient = (RxDocumentClientImpl) ReflectionUtils.getAsyncDocumentClient(testClient);
            GlobalEndpointManager globalEndpointManager = documentClient.getGlobalEndpointManager();

            DatabaseAccount databaseAccount = globalEndpointManager.getLatestDatabaseAccount();
            this.writeRegions = new ArrayList<>(this.getRegionMap(databaseAccount, true).keySet());
        } finally {
            logger.debug("beforeClass executed...");
        }
    }

    @Test(groups = {"multi-master"})
    public void readHits503InPrimaryRegion() {

        List<String> preferredRegions = this.writeRegions;
        CosmosClientBuilder clientBuilder = getClientBuilder().multipleWriteRegionsEnabled(true).preferredRegions(preferredRegions);

        ConnectionPolicy connectionPolicy = ReflectionUtils.getConnectionPolicy(clientBuilder);

        if (connectionPolicy.getConnectionMode() == ConnectionMode.GATEWAY) {
            throw new SkipException("readHits503InPrimaryRegion test is not applicable to GATEWAY connectivity mode!");
        }

        CosmosAsyncClient client = clientBuilder.buildAsyncClient();
        CosmosAsyncDatabase database = getSharedCosmosDatabase(client);

        String multiPartitionContainerId = UUID.randomUUID() + "-multi-partition-test-container";

        CosmosAsyncContainer container = null;
        CosmosContainerProperties containerProperties = new CosmosContainerProperties(multiPartitionContainerId, "/id");
        ThroughputProperties throughputProperties = ThroughputProperties.createManualThroughput(50_000);

        try {

            System.setProperty("COSMOS.PARTITION_LEVEL_CIRCUIT_BREAKER_ENABLED", "true");

            database.createContainerIfNotExists(containerProperties, throughputProperties).block();
            container = database.getContainer(multiPartitionContainerId);

            Thread.sleep(10_000);

            TestObject testObject = TestObject.create();

            String itemIdMappingToUnhealthyPartition = testObject.getId();

            FaultInjectionCondition faultInjectionCondition = new FaultInjectionConditionBuilder()
                .operationType(FaultInjectionOperationType.READ_ITEM)
                .connectionType(FaultInjectionConnectionType.DIRECT)
                .endpoints(new FaultInjectionEndpointBuilder(FeedRange.forLogicalPartition(new PartitionKey(itemIdMappingToUnhealthyPartition))).build())
                .region(preferredRegions.get(0))
                .build();

            FaultInjectionServerErrorResult faultInjectionServerErrorResult = FaultInjectionResultBuilders
                .getResultBuilder(FaultInjectionServerErrorType.SERVICE_UNAVAILABLE)
                .build();

            FaultInjectionRule serviceUnavailableRule = new FaultInjectionRuleBuilder("service-unavailable-rule-" + UUID.randomUUID())
                .condition(faultInjectionCondition)
                .result(faultInjectionServerErrorResult)
                .hitLimit(13)
                .build();

            CosmosFaultInjectionHelper
                .configureFaultInjectionRules(container, Arrays.asList(serviceUnavailableRule))
                .block();

            container.createItem(testObject).block();

            for (int i = 1; i <= 15; i++) {
                CosmosItemResponse<TestObject> response = container.readItem(itemIdMappingToUnhealthyPartition, new PartitionKey(itemIdMappingToUnhealthyPartition), TestObject.class).block();
                logger.info("Hit count : {}", serviceUnavailableRule.getHitCount());
            }

            CosmosItemResponse<TestObject> response = container.readItem(itemIdMappingToUnhealthyPartition, new PartitionKey(itemIdMappingToUnhealthyPartition), TestObject.class).block();
            logger.info("Sleep for 60 seconds");

            Thread.sleep(60_000);

            for (int i = 1; i <= 30; i++) {
                response = container.readItem(itemIdMappingToUnhealthyPartition, new PartitionKey(itemIdMappingToUnhealthyPartition), TestObject.class).block();
                logger.info("Hit count : {}", serviceUnavailableRule.getHitCount());
            }


            logger.info("End test");
        } catch (InterruptedException ex) {
            fail("InterruptedException should not have been thrown!");
        } catch (Exception ex) {
            logger.error("Exception thrown :", ex);
            fail("Read operations should have passed!");
        } finally {
            System.clearProperty("COSMOS.PARTITION_LEVEL_CIRCUIT_BREAKER_ENABLED");
            safeDeleteCollection(container);
            safeClose(client);
        }
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
