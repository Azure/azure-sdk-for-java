package com.azure.cosmos;

import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.ImmutablePair;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.implementation.directconnectivity.RntbdTransportClient;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdConnectionStateListener;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdEndpoint;
import com.azure.cosmos.models.CosmosContainerIdentity;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.test.faultinjection.CosmosFaultInjectionHelper;
import com.azure.cosmos.test.faultinjection.FaultInjectionConditionBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionOperationType;
import com.azure.cosmos.test.faultinjection.FaultInjectionResultBuilders;
import com.azure.cosmos.test.faultinjection.FaultInjectionRule;
import com.azure.cosmos.test.faultinjection.FaultInjectionRuleBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorType;
import com.azure.json.implementation.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class HangSimulationTests {

    private static final Logger logger = LoggerFactory.getLogger(HangSimulationTests.class);

    @Test(groups = { "simple" })
    public void readItem_WithFakeReadHangForOneReplica() throws JsonProcessingException {

        String testDbId = "testDb";
        String testContainerId = "testContainer";
        String partitionKey = "/mypk";

        System.clearProperty("COSMOS.MIN_CONNECTION_POOL_SIZE_PER_ENDPOINT");
        System.setProperty("COSMOS.MIN_CONNECTION_POOL_SIZE_PER_ENDPOINT", String.valueOf(3));

        DirectConnectionConfig directConnectionConfig = new DirectConnectionConfig();
        directConnectionConfig.setIdleEndpointTimeout(Duration.ofHours(24));
        directConnectionConfig.setIdleConnectionTimeout(Duration.ofHours(24));

        List<Pair<String, String>> idAndPkVals = new ArrayList<>();

        idAndPkVals.add(new ImmutablePair<>("1", "2"));
        idAndPkVals.add(new ImmutablePair<>("3", "4"));

        CosmosAsyncClient asyncClient = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .directMode(directConnectionConfig)
            .preferredRegions(Arrays.asList("East US"))
            .contentResponseOnWriteEnabled(true)
            .endpointDiscoveryEnabled(true)
            .openConnectionsAndInitCaches(
                new CosmosContainerProactiveInitConfigBuilder(
                    Arrays.asList(
                        new CosmosContainerIdentity(testDbId, testContainerId)
                    )
                ).setProactiveConnectionRegionsCount(1).build()
            )
            .buildAsyncClient();

        asyncClient.createDatabaseIfNotExists(testDbId).block();
        CosmosAsyncDatabase testDb = asyncClient.getDatabase(testDbId);

        testDb.createContainerIfNotExists(testContainerId, partitionKey).block();
        CosmosAsyncContainer testContainer = testDb.getContainer(testContainerId);

        RxDocumentClientImpl docClient = (RxDocumentClientImpl) ReflectionUtils.getAsyncDocumentClient(asyncClient);
        RntbdTransportClient transportClient = (RntbdTransportClient) ReflectionUtils.getTransportClient(docClient);
        RntbdEndpoint.Provider endpointProvider = ReflectionUtils.getRntbdEndpointProvider(transportClient);

        List<RntbdEndpoint> endpoints = endpointProvider.list().collect(Collectors.toList());

        CosmosEndToEndOperationLatencyPolicyConfig e2eConfig =
            new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofSeconds(2)).build();

        // rntbd://cdb-ms-prod-eastus1-fd113.documents.azure.com:14089/apps/f940b465-c113-475d-9039-cc2659a49cb8/services/4407ad83-1c29-4635-8c35-77a1b209e965/partitions/014ca6de-046b-4eb7-b0ce-c30749586c3d/replicas/133353048354009545s/

        int threadCount = 1;

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        AtomicBoolean isStopped = new AtomicBoolean(false);

        Duration workloadExecutionDuration = Duration.ofMinutes(10);

        Flux
            .just(1)
            .delayElements(workloadExecutionDuration)
            .doOnComplete(() -> isStopped.compareAndSet(false, true))
            .subscribe();
        //
        //        String ruleId = "serverErrorRule-" + FaultInjectionServerErrorType.TIMEOUT + "-" + UUID.randomUUID();
        //        FaultInjectionRule serverErrorRule =
        //            new FaultInjectionRuleBuilder(ruleId)
        //                .condition(
        //                    new FaultInjectionConditionBuilder()
        //                        .operationType(FaultInjectionOperationType.READ_ITEM)
        //                        .build()
        //                )
        //                .result(
        //                    FaultInjectionResultBuilders
        //                        .getResultBuilder(FaultInjectionServerErrorType.RESPONSE_DELAY)
        //                        .suppressServiceRequests(true)
        //                        .delay(Duration.ofMinutes(5))
        //                        .build()
        //                )
        //                .duration(Duration.ofMinutes(5))
        //                .build();
        //
        //        CosmosFaultInjectionHelper.configureFaultInjectionRules(testContainer, Arrays.asList
        //        (serverErrorRule)).block();

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> execute(testContainer, idAndPkVals, 2, isStopped, e2eConfig));
        }

        try {
            executorService.awaitTermination(11, TimeUnit.MINUTES);
            executorService.shutdown();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            asyncClient.close();
        }
    }

    private void execute(CosmosAsyncContainer container, List<Pair<String, String>> idAndPkValPairs, int docCount,
                         AtomicBoolean isStopped, CosmosEndToEndOperationLatencyPolicyConfig e2eConfig) {
        while (!isStopped.get()) {
            try {
                CosmosItemRequestOptions itemRequestOptions = new CosmosItemRequestOptions();
                itemRequestOptions.setCosmosEndToEndOperationLatencyPolicyConfig(e2eConfig);
                Pair<String, String> idAndPkValPair =
                    idAndPkValPairs.get(ThreadLocalRandom.current().nextInt(docCount));
                CosmosItemResponse<ObjectNode> response = container.readItem(idAndPkValPair.getLeft(),
                    new PartitionKey(idAndPkValPair.getRight()), itemRequestOptions, ObjectNode.class).block();
//                logger.info(response.getDiagnostics().toString());
//                logger.info("Fetched item");
            } catch (Exception e) {
                if (e instanceof CosmosException) {
                    CosmosException cosmosException = Utils.as(e, CosmosException.class);
                    if (cosmosException.getSubStatusCode() == HttpConstants.SubStatusCodes.CLIENT_OPERATION_TIMEOUT) {
                        logger.error("Operation cancelled");
                    }
                }
            }
        }
    }
}

