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
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosPatchItemRequestOptions;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.FeedResponse;
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

import static org.assertj.core.api.Assertions.assertThat;
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
    public void readHits503InFirstPreferredRegion() {

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

            container.createItem(testObject, new PartitionKey(itemIdMappingToUnhealthyPartition), new CosmosItemRequestOptions()).block();

            for (int i = 1; i <= 15; i++) {
                CosmosItemResponse<TestObject> response = container.readItem(itemIdMappingToUnhealthyPartition, new PartitionKey(itemIdMappingToUnhealthyPartition), TestObject.class).block();
                logger.info("Hit count : {}", serviceUnavailableRule.getHitCount());

                assertThat(response).isNotNull();
                assertThat(response.getDiagnostics()).isNotNull();

                response.getDiagnostics().getDiagnosticsContext().getContactedRegionNames().forEach(
                    regionContacted -> logger.info("Region contacted : {}", regionContacted)
                );
            }

            CosmosItemResponse<TestObject> response = container.readItem(itemIdMappingToUnhealthyPartition, new PartitionKey(itemIdMappingToUnhealthyPartition), TestObject.class).block();
            logger.info("Sleep for 60 seconds");

            Thread.sleep(60_000);

            for (int i = 1; i <= 30; i++) {
                response = container.readItem(itemIdMappingToUnhealthyPartition, new PartitionKey(itemIdMappingToUnhealthyPartition), TestObject.class).block();
                logger.info("Hit count : {}", serviceUnavailableRule.getHitCount());

                assertThat(response).isNotNull();
                assertThat(response.getDiagnostics()).isNotNull();

                response.getDiagnostics().getDiagnosticsContext().getContactedRegionNames().forEach(
                    regionContacted -> logger.info("Region contacted : {}", regionContacted)
                );
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

    @Test(groups = {"multi-master"})
    public void upsertHits503InFirstPreferredRegion() {
        List<String> preferredRegions = this.writeRegions;
        CosmosClientBuilder clientBuilder = getClientBuilder().multipleWriteRegionsEnabled(true).preferredRegions(preferredRegions);

        ConnectionPolicy connectionPolicy = ReflectionUtils.getConnectionPolicy(clientBuilder);

        if (connectionPolicy.getConnectionMode() == ConnectionMode.GATEWAY) {
            throw new SkipException("upsertHits503InFirstPreferredRegion test is not applicable to GATEWAY connectivity mode!");
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
                .operationType(FaultInjectionOperationType.UPSERT_ITEM)
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

            container.createItem(testObject, new PartitionKey(itemIdMappingToUnhealthyPartition), new CosmosItemRequestOptions()).block();

            for (int i = 1; i <= 15; i++) {
                CosmosItemResponse<TestObject> response = container.upsertItem(testObject, new PartitionKey(itemIdMappingToUnhealthyPartition), new CosmosItemRequestOptions()).block();
                logger.info("Hit count : {}", serviceUnavailableRule.getHitCount());

                assertThat(response).isNotNull();
                assertThat(response.getDiagnostics()).isNotNull();

                response.getDiagnostics().getDiagnosticsContext().getContactedRegionNames().forEach(
                    regionContacted -> logger.info("Region contacted : {}", regionContacted)
                );
            }

            CosmosItemResponse<TestObject> response = container.upsertItem(testObject, new PartitionKey(itemIdMappingToUnhealthyPartition), new CosmosItemRequestOptions()).block();
            logger.info("Sleep for 60 seconds");

            Thread.sleep(60_000);

            for (int i = 1; i <= 30; i++) {
                response = container.upsertItem(testObject, new PartitionKey(itemIdMappingToUnhealthyPartition), new CosmosItemRequestOptions()).block();
                logger.info("Hit count : {}", serviceUnavailableRule.getHitCount());

                assertThat(response).isNotNull();
                assertThat(response.getDiagnostics()).isNotNull();

                response.getDiagnostics().getDiagnosticsContext().getContactedRegionNames().forEach(
                    regionContacted -> logger.info("Region contacted : {}", regionContacted)
                );
            }


            logger.info("End test");
        } catch (InterruptedException ex) {
            fail("InterruptedException should not have been thrown!");
        } catch (Exception ex) {
            logger.error("Exception thrown :", ex);
            fail("Upsert operations should have passed!");
        } finally {
            System.clearProperty("COSMOS.PARTITION_LEVEL_CIRCUIT_BREAKER_ENABLED");
            safeDeleteCollection(container);
            safeClose(client);
        }
    }

    @Test(groups = {"multi-master"})
    public void createHits503InFirstPreferredRegion() {
        List<String> preferredRegions = this.writeRegions;
        CosmosClientBuilder clientBuilder = getClientBuilder().multipleWriteRegionsEnabled(true).preferredRegions(preferredRegions);

        ConnectionPolicy connectionPolicy = ReflectionUtils.getConnectionPolicy(clientBuilder);

        if (connectionPolicy.getConnectionMode() == ConnectionMode.GATEWAY) {
            throw new SkipException("createHits503InFirstPreferredRegion test is not applicable to GATEWAY connectivity mode!");
        }

        CosmosAsyncClient client = clientBuilder.buildAsyncClient();
        CosmosAsyncDatabase database = getSharedCosmosDatabase(client);

        String multiPartitionContainerId = UUID.randomUUID() + "-single-partition-test-container";

        CosmosAsyncContainer container = null;
        CosmosContainerProperties containerProperties = new CosmosContainerProperties(multiPartitionContainerId, "/id");
        ThroughputProperties throughputProperties = ThroughputProperties.createManualThroughput(5000);

        try {

            System.setProperty("COSMOS.PARTITION_LEVEL_CIRCUIT_BREAKER_ENABLED", "true");

            database.createContainerIfNotExists(containerProperties, throughputProperties).block();
            container = database.getContainer(multiPartitionContainerId);

            Thread.sleep(10_000);

            TestObject testObject = TestObject.create();

            String itemIdMappingToUnhealthyPartition = testObject.getId();

            FaultInjectionCondition faultInjectionCondition = new FaultInjectionConditionBuilder()
                .operationType(FaultInjectionOperationType.CREATE_ITEM)
                .connectionType(FaultInjectionConnectionType.DIRECT)
                .endpoints(new FaultInjectionEndpointBuilder(FeedRange.forFullRange()).build())
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

            container.createItem(testObject, new PartitionKey(itemIdMappingToUnhealthyPartition), new CosmosItemRequestOptions()).block();

            for (int i = 1; i <= 15; i++) {
                testObject = TestObject.create();
                CosmosItemResponse<TestObject> response = container.createItem(testObject, new PartitionKey(testObject.getId()), new CosmosItemRequestOptions()).block();
                logger.info("Hit count : {}", serviceUnavailableRule.getHitCount());

                assertThat(response).isNotNull();
                assertThat(response.getDiagnostics()).isNotNull();

                response.getDiagnostics().getDiagnosticsContext().getContactedRegionNames().forEach(
                    regionContacted -> logger.info("Region contacted : {}", regionContacted)
                );
            }

            testObject = TestObject.create();
            CosmosItemResponse<TestObject> response = container.createItem(testObject, new PartitionKey(testObject.getId()), new CosmosItemRequestOptions()).block();
            logger.info("Sleep for 60 seconds");

            Thread.sleep(60_000);

            for (int i = 1; i <= 30; i++) {
                testObject = TestObject.create();
                response = container.createItem(testObject, new PartitionKey(testObject.getId()), new CosmosItemRequestOptions()).block();
                logger.info("Hit count : {}", serviceUnavailableRule.getHitCount());

                assertThat(response).isNotNull();
                assertThat(response.getDiagnostics()).isNotNull();

                response.getDiagnostics().getDiagnosticsContext().getContactedRegionNames().forEach(
                    regionContacted -> logger.info("Region contacted : {}", regionContacted)
                );
            }

            logger.info("End test");
        } catch (InterruptedException ex) {
            fail("InterruptedException should not have been thrown!");
        } catch (Exception ex) {
            logger.error("Exception thrown :", ex);
            fail("Create operations should have passed!");
        } finally {
            System.clearProperty("COSMOS.PARTITION_LEVEL_CIRCUIT_BREAKER_ENABLED");
            safeDeleteCollection(container);
            safeClose(client);
        }
    }

    @Test(groups = {"multi-master"})
    public void deleteHits503InFirstPreferredRegion() {
        List<String> preferredRegions = this.writeRegions;
        CosmosClientBuilder clientBuilder = getClientBuilder().multipleWriteRegionsEnabled(true).preferredRegions(preferredRegions);

        ConnectionPolicy connectionPolicy = ReflectionUtils.getConnectionPolicy(clientBuilder);

        if (connectionPolicy.getConnectionMode() == ConnectionMode.GATEWAY) {
            throw new SkipException("deleteHits503InFirstPreferredRegion test is not applicable to GATEWAY connectivity mode!");
        }

        CosmosAsyncClient client = clientBuilder.buildAsyncClient();
        CosmosAsyncDatabase database = getSharedCosmosDatabase(client);

        String multiPartitionContainerId = UUID.randomUUID() + "-single-partition-test-container";

        CosmosAsyncContainer container = null;
        CosmosContainerProperties containerProperties = new CosmosContainerProperties(multiPartitionContainerId, "/id");
        ThroughputProperties throughputProperties = ThroughputProperties.createManualThroughput(5000);

        try {

            System.setProperty("COSMOS.PARTITION_LEVEL_CIRCUIT_BREAKER_ENABLED", "true");

            database.createContainerIfNotExists(containerProperties, throughputProperties).block();
            container = database.getContainer(multiPartitionContainerId);

            Thread.sleep(10_000);

            FaultInjectionCondition faultInjectionCondition = new FaultInjectionConditionBuilder()
                .operationType(FaultInjectionOperationType.DELETE_ITEM)
                .connectionType(FaultInjectionConnectionType.DIRECT)
                .endpoints(new FaultInjectionEndpointBuilder(FeedRange.forFullRange()).build())
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

            List<String> idAndPks = new ArrayList<>();

            for (int i = 1; i <= 30; i++) {
                TestObject testObject = TestObject.create();
                CosmosItemResponse<TestObject> response = container.createItem(testObject, new PartitionKey(testObject.getId()), new CosmosItemRequestOptions()).block();

                assertThat(response).isNotNull();
                assertThat(response.getDiagnostics()).isNotNull();

                idAndPks.add(testObject.getId());
            }

            for (int i = 0; i < 15; i++) {
                CosmosItemResponse<Object> response = container.deleteItem(idAndPks.get(i), new PartitionKey(idAndPks.get(i)), new CosmosItemRequestOptions()).block();
                logger.info("Hit count : {}", serviceUnavailableRule.getHitCount());

                assertThat(response).isNotNull();
                assertThat(response.getDiagnostics()).isNotNull();

                response.getDiagnostics().getDiagnosticsContext().getContactedRegionNames().forEach(
                    regionContacted -> logger.info("Region contacted : {}", regionContacted)
                );
            }

            logger.info("Sleeping for a minute!");
            Thread.sleep(60_000);

            for (int i = 15; i < 30; i++) {
                CosmosItemResponse<Object> response = container.deleteItem(idAndPks.get(i), new PartitionKey(idAndPks.get(i)), new CosmosItemRequestOptions()).block();
                logger.info("Hit count : {}", serviceUnavailableRule.getHitCount());

                assertThat(response).isNotNull();
                assertThat(response.getDiagnostics()).isNotNull();

                response.getDiagnostics().getDiagnosticsContext().getContactedRegionNames().forEach(
                    regionContacted -> logger.info("Region contacted : {}", regionContacted)
                );
            }

            logger.info("End test");
        } catch (InterruptedException ex) {
            fail("InterruptedException should not have been thrown!");
        } catch (Exception ex) {
            logger.error("Exception thrown :", ex);
            fail("Create operations should have passed!");
        } finally {
            System.clearProperty("COSMOS.PARTITION_LEVEL_CIRCUIT_BREAKER_ENABLED");
            safeDeleteCollection(container);
            safeClose(client);
        }
    }

    @Test(groups = {"multi-master"})
    public void patchHits503InFirstPreferredRegion() {
        List<String> preferredRegions = this.writeRegions;
        CosmosClientBuilder clientBuilder = getClientBuilder().multipleWriteRegionsEnabled(true).preferredRegions(preferredRegions);

        ConnectionPolicy connectionPolicy = ReflectionUtils.getConnectionPolicy(clientBuilder);

        if (connectionPolicy.getConnectionMode() == ConnectionMode.GATEWAY) {
            throw new SkipException("createHits503InFirstPreferredRegion test is not applicable to GATEWAY connectivity mode!");
        }

        CosmosAsyncClient client = clientBuilder.buildAsyncClient();
        CosmosAsyncDatabase database = getSharedCosmosDatabase(client);

        String multiPartitionContainerId = UUID.randomUUID() + "-single-partition-test-container";

        CosmosAsyncContainer container = null;
        CosmosContainerProperties containerProperties = new CosmosContainerProperties(multiPartitionContainerId, "/id");
        ThroughputProperties throughputProperties = ThroughputProperties.createManualThroughput(5000);

        try {

            System.setProperty("COSMOS.PARTITION_LEVEL_CIRCUIT_BREAKER_ENABLED", "true");

            database.createContainerIfNotExists(containerProperties, throughputProperties).block();
            container = database.getContainer(multiPartitionContainerId);

            Thread.sleep(10_000);

            FaultInjectionCondition faultInjectionCondition = new FaultInjectionConditionBuilder()
                .operationType(FaultInjectionOperationType.PATCH_ITEM)
                .connectionType(FaultInjectionConnectionType.DIRECT)
                .endpoints(new FaultInjectionEndpointBuilder(FeedRange.forFullRange()).build())
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

            TestObject testObject = TestObject.create();
            container.createItem(testObject, new PartitionKey(testObject.getId()), new CosmosItemRequestOptions()).block();

            CosmosPatchOperations patchOperations = CosmosPatchOperations.create().add("/number", 555);

            for (int i = 0; i < 15; i++) {
                CosmosItemResponse<TestObject> response = container.patchItem(testObject.getId(), new PartitionKey(testObject.getId()), patchOperations, new CosmosPatchItemRequestOptions(), TestObject.class).block();
                logger.info("Hit count : {}", serviceUnavailableRule.getHitCount());

                assertThat(response).isNotNull();
                assertThat(response.getDiagnostics()).isNotNull();

                response.getDiagnostics().getDiagnosticsContext().getContactedRegionNames().forEach(
                    regionContacted -> logger.info("Region contacted : {}", regionContacted)
                );
            }

            logger.info("Sleep for 60 seconds!");
            Thread.sleep(60_000);

            for (int i = 0; i < 15; i++) {
                CosmosItemResponse<TestObject> response = container.patchItem(testObject.getId(), new PartitionKey(testObject.getId()), patchOperations, new CosmosPatchItemRequestOptions(), TestObject.class).block();
                logger.info("Hit count : {}", serviceUnavailableRule.getHitCount());

                assertThat(response).isNotNull();
                assertThat(response.getDiagnostics()).isNotNull();

                response.getDiagnostics().getDiagnosticsContext().getContactedRegionNames().forEach(
                    regionContacted -> logger.info("Region contacted : {}", regionContacted)
                );
            }

            logger.info("End test");
        } catch (InterruptedException ex) {
            fail("InterruptedException should not have been thrown!");
        } catch (Exception ex) {
            logger.error("Exception thrown :", ex);
            fail("Patch operations should have passed!");
        } finally {
            System.clearProperty("COSMOS.PARTITION_LEVEL_CIRCUIT_BREAKER_ENABLED");
            safeDeleteCollection(container);
            safeClose(client);
        }
    }

    @Test(groups = {"multi-master"})
    public void replaceHits503InFirstPreferredRegion() {
        List<String> preferredRegions = this.writeRegions;
        CosmosClientBuilder clientBuilder = getClientBuilder().multipleWriteRegionsEnabled(true).preferredRegions(preferredRegions);

        ConnectionPolicy connectionPolicy = ReflectionUtils.getConnectionPolicy(clientBuilder);

        if (connectionPolicy.getConnectionMode() == ConnectionMode.GATEWAY) {
            throw new SkipException("createHits503InFirstPreferredRegion test is not applicable to GATEWAY connectivity mode!");
        }

        CosmosAsyncClient client = clientBuilder.buildAsyncClient();
        CosmosAsyncDatabase database = getSharedCosmosDatabase(client);

        String multiPartitionContainerId = UUID.randomUUID() + "-single-partition-test-container";

        CosmosAsyncContainer container = null;
        CosmosContainerProperties containerProperties = new CosmosContainerProperties(multiPartitionContainerId, "/id");
        ThroughputProperties throughputProperties = ThroughputProperties.createManualThroughput(5000);

        try {

            System.setProperty("COSMOS.PARTITION_LEVEL_CIRCUIT_BREAKER_ENABLED", "true");

            database.createContainerIfNotExists(containerProperties, throughputProperties).block();
            container = database.getContainer(multiPartitionContainerId);

            Thread.sleep(10_000);

            FaultInjectionCondition faultInjectionCondition = new FaultInjectionConditionBuilder()
                .operationType(FaultInjectionOperationType.REPLACE_ITEM)
                .connectionType(FaultInjectionConnectionType.DIRECT)
                .endpoints(new FaultInjectionEndpointBuilder(FeedRange.forFullRange()).build())
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

            TestObject testObject = TestObject.create();
            container.createItem(testObject, new PartitionKey(testObject.getId()), new CosmosItemRequestOptions()).block();

            for (int i = 0; i < 15; i++) {
                CosmosItemResponse<TestObject> response = container.replaceItem(testObject, testObject.getId(), new PartitionKey(testObject.getId()), new CosmosItemRequestOptions()).block();
                logger.info("Hit count : {}", serviceUnavailableRule.getHitCount());

                assertThat(response).isNotNull();
                assertThat(response.getDiagnostics()).isNotNull();

                response.getDiagnostics().getDiagnosticsContext().getContactedRegionNames().forEach(
                    regionContacted -> logger.info("Region contacted : {}", regionContacted)
                );
            }

            logger.info("Sleep for 60 seconds!");
            Thread.sleep(60_000);

            for (int i = 0; i < 15; i++) {
                CosmosItemResponse<TestObject> response = container.replaceItem(testObject, testObject.getId(), new PartitionKey(testObject.getId()), new CosmosItemRequestOptions()).block();
                logger.info("Hit count : {}", serviceUnavailableRule.getHitCount());

                assertThat(response).isNotNull();
                assertThat(response.getDiagnostics()).isNotNull();

                response.getDiagnostics().getDiagnosticsContext().getContactedRegionNames().forEach(
                    regionContacted -> logger.info("Region contacted : {}", regionContacted)
                );
            }

            logger.info("End test");
        } catch (InterruptedException ex) {
            fail("InterruptedException should not have been thrown!");
        } catch (Exception ex) {
            logger.error("Exception thrown :", ex);
            fail("Replace operations should have passed!");
        } finally {
            System.clearProperty("COSMOS.PARTITION_LEVEL_CIRCUIT_BREAKER_ENABLED");
            safeDeleteCollection(container);
            safeClose(client);
        }
    }

    @Test(groups = {"multi-master"})
    public void queryHits503InFirstPreferredRegion() {
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
        ThroughputProperties throughputProperties = ThroughputProperties.createManualThroughput(6_000);

        try {

            System.setProperty("COSMOS.PARTITION_LEVEL_CIRCUIT_BREAKER_ENABLED", "true");

            database.createContainerIfNotExists(containerProperties, throughputProperties).block();
            container = database.getContainer(multiPartitionContainerId);

            Thread.sleep(10_000);

            TestObject testObject = TestObject.create();

            String itemIdMappingToUnhealthyPartition = testObject.getId();

            FaultInjectionCondition faultInjectionCondition = new FaultInjectionConditionBuilder()
                .operationType(FaultInjectionOperationType.QUERY_ITEM)
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

            container.createItem(testObject, new PartitionKey(itemIdMappingToUnhealthyPartition), new CosmosItemRequestOptions()).block();

            for (int i = 1; i <= 15; i++) {
                FeedResponse<TestObject> response = container.queryItems("SELECT * FROM c", TestObject.class).byPage().blockLast();
                logger.info("Hit count : {}", serviceUnavailableRule.getHitCount());

                assertThat(response).isNotNull();
                assertThat(response.getCosmosDiagnostics()).isNotNull();

                response.getCosmosDiagnostics().getDiagnosticsContext().getContactedRegionNames().forEach(
                    regionContacted -> logger.info("Region contacted : {}", regionContacted)
                );

                logger.info("CosmosDiagnostics : {}", response.getCosmosDiagnostics().toString());
            }

            logger.info("Sleep for 60 seconds!");
            Thread.sleep(60_000);

            for (int i = 1; i <= 30; i++) {
                FeedResponse<TestObject> response = container.queryItems("SELECT * FROM c", TestObject.class).byPage().blockLast();
                logger.info("Hit count : {}", serviceUnavailableRule.getHitCount());

                assertThat(response).isNotNull();
                assertThat(response.getCosmosDiagnostics()).isNotNull();

                response.getCosmosDiagnostics().getDiagnosticsContext().getContactedRegionNames().forEach(
                    regionContacted -> logger.info("Region contacted : {}", regionContacted)
                );
            }

            logger.info("End test");
        } catch (InterruptedException ex) {
            fail("InterruptedException should not have been thrown!");
        } catch (Exception ex) {
            logger.error("Exception thrown :", ex);
            fail("Query operations should have passed!");
        } finally {
            System.clearProperty("COSMOS.PARTITION_LEVEL_CIRCUIT_BREAKER_ENABLED");
            safeDeleteCollection(container);
            safeClose(client);
        }
    }

    private static Map<String, String> getRegionMap(DatabaseAccount databaseAccount, boolean writeOnly) {
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
