// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.guava25.collect.ImmutableList;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.rx.TestSuiteBase;
import com.azure.cosmos.test.faultinjection.FaultInjectionCondition;
import com.azure.cosmos.test.faultinjection.FaultInjectionConditionBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionConnectionType;
import com.azure.cosmos.test.faultinjection.FaultInjectionOperationType;
import com.azure.cosmos.test.faultinjection.FaultInjectionResultBuilders;
import com.azure.cosmos.test.faultinjection.FaultInjectionRule;
import com.azure.cosmos.test.faultinjection.FaultInjectionRuleBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorResultBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorType;
import com.azure.cosmos.test.faultinjection.IFaultInjectionResult;
import com.azure.cosmos.test.implementation.faultinjection.FaultInjectorProvider;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

import static com.azure.cosmos.CosmosDiagnostics.OBJECT_MAPPER;
import static org.assertj.core.api.Assertions.assertThat;

public class EndToEndTimeOutWithSpeculationTest extends TestSuiteBase {
    private static final int DEFAULT_NUM_DOCUMENTS = 100;
    private static final int DEFAULT_PAGE_SIZE = 100;
    private CosmosAsyncContainer createdContainer;
    private final Random random;
    private final List<TestObject> createdDocuments = new ArrayList<>();
    private final CosmosEndToEndOperationLatencyPolicyConfig endToEndOperationLatencyPolicyConfig;

    // These regions should match the ones in test-resources.json
//    private final List<String> regions = ImmutableList.of("West Central US", "Central US");
    private final List<String> regions = ImmutableList.of("West US 2", "East US 2");

    @Factory(dataProvider = "clientBuildersWithDirectTcpSession")
    public EndToEndTimeOutWithSpeculationTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
        random = new Random();
        endToEndOperationLatencyPolicyConfig = new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofSeconds(10))
            .build();
    }

    @BeforeClass(groups = {"multi-region"}, timeOut = SETUP_TIMEOUT * 100)
    public void beforeClass() throws Exception {
        System.setProperty(Configs.SPECULATION_TYPE, "1");
        CosmosClientBuilder builder = this.getClientBuilder();
        builder.preferredRegions(regions);
        CosmosAsyncClient client = builder.buildAsyncClient();
        CosmosAsyncDatabase createdDatabase = getSharedCosmosDatabase(client);
        createdContainer = getSharedMultiPartitionCosmosContainer(client);
        truncateCollection(createdContainer);

        createdDocuments.addAll(this.insertDocuments(DEFAULT_NUM_DOCUMENTS, null, createdContainer));
    }

    @Test(groups = {"multi-region"}, timeOut = 10000L)
    public void readItemWithEndToEndTimeoutPolicyInOptionWithSpeculationShouldNotTimeout() {
        if (getClientBuilder().buildConnectionPolicy().getConnectionMode() != ConnectionMode.DIRECT) {
            throw new SkipException("Failure injection only supported for DIRECT mode");
        }

        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        options.setCosmosEndToEndOperationLatencyPolicyConfig(endToEndOperationLatencyPolicyConfig);
        TestObject itemToRead = createdDocuments.get(random.nextInt(createdDocuments.size()));
        FaultInjectionRule rule = injectFailure(createdContainer, FaultInjectionOperationType.READ_ITEM, null);

        Mono<CosmosItemResponse<TestObject>> cosmosItemResponseMono =
            createdContainer.readItem(itemToRead.id, new PartitionKey(itemToRead.mypk), options, TestObject.class);

        verifySuccess(cosmosItemResponseMono);
        rule.disable();
    }

    private void verifySuccess(Mono<CosmosItemResponse<TestObject>> cosmosItemResponseMono) {
        StepVerifier.create(cosmosItemResponseMono)
            .expectNextMatches(cosmosItemResponse -> {
                ObjectNode diagnosticsNode = null;
                try {
                    // Since we are injecting fault in region 0, we make sure response is from region 1 in the list above
                    diagnosticsNode = (ObjectNode) OBJECT_MAPPER.readTree(cosmosItemResponse.getDiagnostics().toString());
                    assertResponseFromSpeculatedRegion(diagnosticsNode);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                return true;
            })
            .verifyComplete();
    }

    private void assertResponseFromSpeculatedRegion(ObjectNode diagnosticsNode) {
        JsonNode responseStatisticsList = diagnosticsNode.get("responseStatisticsList");
        assertThat(responseStatisticsList.isArray()).isTrue();
        assertThat(responseStatisticsList.size()).isGreaterThan(0);
        JsonNode storeResult = responseStatisticsList.get(0).get("storeResult");
        assertThat(storeResult.get("storePhysicalAddress").toString()).contains(StringUtils.deleteWhitespace(regions.get(1).toLowerCase(Locale.ROOT)));
    }

    @Test(groups = {"multi-region"}, timeOut = 10000L)
    public void queryItemWithEndToEndTimeoutPolicyWithSpeculationShouldNotTimeout() {
        if (getClientBuilder().buildConnectionPolicy().getConnectionMode() != ConnectionMode.DIRECT) {
            throw new SkipException("Failure injection only supported for DIRECT mode");
        }

        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        options.setCosmosEndToEndOperationLatencyPolicyConfig(endToEndOperationLatencyPolicyConfig);

        TestObject itemToQuery = createdDocuments.get(random.nextInt(createdDocuments.size()));

        String queryText = "select top 1 * from c";
        SqlQuerySpec sqlQuerySpec = new SqlQuerySpec(queryText);

        FaultInjectionRule faultInjectionRule = injectFailure(createdContainer, FaultInjectionOperationType.QUERY_ITEM, null);
        CosmosPagedFlux<TestObject> queryPagedFlux = createdContainer.queryItems(sqlQuerySpec, options, TestObject.class);

        StepVerifier.create(queryPagedFlux.byPage())
            .expectNextMatches(response -> {
                ObjectNode diagnosticsNode = null;
                // Since we are injecting fault in region 0, we make sure response is from region 1 in the list above
                assertThat(response.getCosmosDiagnostics().getClientSideRequestStatistics().iterator().next().getResponseStatisticsList().get(0).getRegionName())
                    .isEqualTo(regions.get(1).toLowerCase(Locale.ROOT));

                return true;
            })
            .verifyComplete();
        faultInjectionRule.disable();
    }

    private FaultInjectionRule injectFailure(
        CosmosAsyncContainer container,
        FaultInjectionOperationType operationType,
        Boolean suppressServiceRequests) {

        FaultInjectionServerErrorResultBuilder faultInjectionResultBuilder = FaultInjectionResultBuilders
            .getResultBuilder(FaultInjectionServerErrorType.RESPONSE_DELAY)
            .delay(Duration.ofMillis(5000))
            .times(1);

        if (suppressServiceRequests != null) {
            faultInjectionResultBuilder.suppressServiceRequests(suppressServiceRequests);
        }

        IFaultInjectionResult result = faultInjectionResultBuilder.build();
        logger.info("Injecting fault: {}", regions.get(0));
        FaultInjectionCondition condition = new FaultInjectionConditionBuilder()
            .operationType(operationType)
            .region(regions.get(0))
            .connectionType(FaultInjectionConnectionType.DIRECT)
            .build();

        FaultInjectionRule rule = new FaultInjectionRuleBuilder("InjectedResponseDelay")
            .condition(condition)
            .result(result)
            .build();

        FaultInjectorProvider injectorProvider = (FaultInjectorProvider) container
            .getOrConfigureFaultInjectorProvider(() -> new FaultInjectorProvider(container));

        injectorProvider.configureFaultInjectionRules(Arrays.asList(rule)).block();

        return rule;
    }

    private TestObject getDocumentDefinition(String documentId, String partitionKey) {
        // Doing NUM_DOCUMENTS/2 just to ensure there will be good number of repetetions for int value.
        int randInt = random.nextInt(DEFAULT_NUM_DOCUMENTS / 2);

        TestObject doc = new TestObject(documentId, "name" + randInt, randInt, partitionKey);
        return doc;
    }

    private List<TestObject> insertDocuments(int documentCount, List<String> partitionKeys, CosmosAsyncContainer container) {
        List<TestObject> documentsToInsert = new ArrayList<>();

        for (int i = 0; i < documentCount; i++) {
            documentsToInsert.add(
                getDocumentDefinition(
                    UUID.randomUUID().toString(),
                    partitionKeys == null ? UUID.randomUUID().toString() : partitionKeys.get(random.nextInt(partitionKeys.size()))));
        }

        List<TestObject> documentInserted = bulkInsertBlocking(container, documentsToInsert);

        waitIfNeededForReplicasToCatchUp(this.getClientBuilder());

        return documentInserted;
    }

    static class TestObject {
        String id;
        String name;
        int prop;
        String mypk;
        String constantProp = "constantProp";

        public TestObject() {
        }

        public TestObject(String id, String name, int prop, String mypk) {
            this.id = id;
            this.name = name;
            this.prop = prop;
            this.mypk = mypk;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getProp() {
            return prop;
        }

        public void setProp(final int prop) {
            this.prop = prop;
        }

        public String getMypk() {
            return mypk;
        }

        public void setMypk(String mypk) {
            this.mypk = mypk;
        }

        public String getConstantProp() {
            return constantProp;
        }
    }
}
