// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.models.CosmosReadManyByPartitionKeysRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.test.faultinjection.CosmosFaultInjectionHelper;
import com.azure.cosmos.test.faultinjection.FaultInjectionConditionBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionEndpointBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionOperationType;
import com.azure.cosmos.test.faultinjection.FaultInjectionResultBuilders;
import com.azure.cosmos.test.faultinjection.FaultInjectionRule;
import com.azure.cosmos.test.faultinjection.FaultInjectionRuleBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorType;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.TestConfigurations;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.ArrayUtils;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class FITests_readManyByPartitionKeysAfterCreation
    extends FaultInjectionWithAvailabilityStrategyTestsBase {

    @Test(groups = {"fi-multi-master"}, dataProvider = "testConfigs_readManyByPartitionKeysAfterCreation", retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void readManyByPartitionKeysAfterCreation(
        String testCaseId,
        Duration endToEndTimeout,
        ThresholdBasedAvailabilityStrategy availabilityStrategy,
        CosmosRegionSwitchHint regionSwitchHint,
        ConnectionMode connectionMode,
        Function<ItemOperationInvocationParameters, CosmosResponseWrapper> readManyByPkOperation,
        BiConsumer<CosmosAsyncContainer, FaultInjectionOperationType> faultInjectionCallback,
        BiConsumer<Integer, Integer> validateStatusCode,
        int expectedDiagnosticsContextCount,
        Consumer<CosmosDiagnosticsContext>[] firstDiagnosticsContextValidations,
        Consumer<CosmosDiagnosticsContext>[] otherDiagnosticsContextValidations,
        Consumer<CosmosResponseWrapper> responseValidator,
        int numberOfOtherDocumentsWithSameId,
        int numberOfOtherDocumentsWithSamePk,
        boolean shouldInjectPreferredRegionsInClient) {

        execute(
            testCaseId,
            endToEndTimeout,
            availabilityStrategy,
            regionSwitchHint,
            null,
            notSpecifiedWhetherIdempotentWriteRetriesAreEnabled,
            ArrayUtils.toArray(FaultInjectionOperationType.QUERY_ITEM),
            readManyByPkOperation,
            faultInjectionCallback,
            validateStatusCode,
            expectedDiagnosticsContextCount,
            firstDiagnosticsContextValidations,
            otherDiagnosticsContextValidations,
            responseValidator,
            numberOfOtherDocumentsWithSameId,
            numberOfOtherDocumentsWithSamePk,
            false,
            connectionMode,
            shouldInjectPreferredRegionsInClient);
    }

    /**
     * Validates continuation-token resume after fault injection causes a deterministic error.
     *
     * Strategy: use FeedRange-scoped fault injection so that queries against one physical
     * partition fail while queries against other partitions succeed. With batch size 1,
     * readManyByPartitionKeys processes one PK-batch at a time sequentially. The first
     * batch(es) targeting the non-faulted partition succeed and emit pages with continuation
     * tokens. When iteration reaches the faulted partition, the error surfaces to the caller.
     *
     * 1. Create documents across multiple PKs (spread across partitions)
     * 2. Collect baseline (no faults) — all ids
     * 3. Get feed ranges; pick the second one to fault
     * 4. Inject sustained SERVICE_UNAVAILABLE scoped to that feed range
     * 5. Iterate page-by-page; collect items + continuation tokens from successful pages
     * 6. When error occurs: validate it's expected, capture last good continuation
     * 7. Disable the fault injection rule
     * 8. Resume from the last good continuation token
     * 9. Assert: union of items before error + items from resume = all baseline items, no duplicates
     */
    @Test(groups = {"fi-multi-master"}, timeOut = 180000, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void readManyByPartitionKeys_continuationResumeAfterFaultInjection() {

        String originalBatchSize = System.getProperty("COSMOS.READ_MANY_BY_PK_MAX_BATCH_SIZE");
        try {
            // batch size 1 = one PK per batch = sequential processing across partitions
            System.setProperty("COSMOS.READ_MANY_BY_PK_MAX_BATCH_SIZE", "1");

            CosmosAsyncClient client = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .contentResponseOnWriteEnabled(true)
                .directMode()
                .buildAsyncClient();

            try {
                CosmosAsyncContainer container = client
                    .getDatabase(this.getTestDatabaseId())
                    .getContainer(this.getTestContainerId());

                String uniqueTag = UUID.randomUUID().toString().substring(0, 8);

                // Create items across 3 PKs, 3 items each = 9 items total
                List<String> pkValues = Arrays.asList(
                    "ctResumePk1_" + uniqueTag,
                    "ctResumePk2_" + uniqueTag,
                    "ctResumePk3_" + uniqueTag);

                List<ObjectNode> allCreatedItems = new ArrayList<>();
                for (String pk : pkValues) {
                    for (int i = 0; i < 3; i++) {
                        ObjectNode item = com.azure.cosmos.implementation.Utils
                            .getSimpleObjectMapper().createObjectNode();
                        item.put("id", UUID.randomUUID().toString());
                        item.put("mypk", pk);
                        container.createItem(item).block();
                        allCreatedItems.add(item);
                    }
                }

                List<PartitionKey> partitionKeys = pkValues.stream()
                    .map(PartitionKey::new)
                    .collect(Collectors.toList());

                // Step 1: Baseline — drain all pages without faults to know the complete set of ids
                List<FeedResponse<ObjectNode>> baselinePages = container
                    .readManyByPartitionKeys(partitionKeys, ObjectNode.class)
                    .byPage()
                    .collectList()
                    .block();

                assertThat(baselinePages).isNotNull();
                assertThat(baselinePages.size()).isGreaterThan(1); // with batch size 1 there must be multiple pages

                List<String> baselineIds = baselinePages.stream()
                    .flatMap(p -> p.getResults().stream())
                    .map(n -> n.get("id").asText())
                    .sorted()
                    .collect(Collectors.toList());
                assertThat(baselineIds).hasSize(9);
                assertThat(baselineIds).doesNotHaveDuplicates();

                // Step 2: Get feed ranges and pick the LAST one to fault.
                // With batch size 1, readManyByPartitionKeys processes batches sorted by EPK.
                // Faulting the last feed range ensures the first batches succeed (giving us
                // pages with continuation tokens) before the faulted partition is reached.
                List<FeedRange> feedRanges = container.getFeedRanges().block();
                assertThat(feedRanges).isNotNull();
                assertThat(feedRanges.size()).isGreaterThanOrEqualTo(1);
                FeedRange faultedFeedRange = feedRanges.get(feedRanges.size() - 1);

                // Step 3: Inject sustained SERVICE_UNAVAILABLE scoped to the last feed range
                FaultInjectionRule partitionScopedRule = new FaultInjectionRuleBuilder(
                    "readManyByPk-ct-resume-partition-scoped")
                    .condition(new FaultInjectionConditionBuilder()
                        .operationType(FaultInjectionOperationType.QUERY_ITEM)
                        .endpoints(new FaultInjectionEndpointBuilder(faultedFeedRange)
                            .replicaCount(4)
                            .includePrimary(true)
                            .build())
                        .build())
                    .result(FaultInjectionResultBuilders
                        .getResultBuilder(FaultInjectionServerErrorType.SERVICE_UNAVAILABLE)
                        .build())
                    .duration(Duration.ofSeconds(120))
                    .build();

                CosmosFaultInjectionHelper
                    .configureFaultInjectionRules(container, Collections.singletonList(partitionScopedRule))
                    .block();

                // Step 4: Drain page-by-page. Pages from non-faulted partitions succeed;
                // when the faulted partition is reached, the error surfaces.
                List<String> itemsBeforeError = new ArrayList<>();
                String lastGoodContinuation = null;
                boolean errorOccurred = false;

                CosmosEndToEndOperationLatencyPolicyConfig e2ePolicy =
                    new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofSeconds(10))
                        .enable(true)
                        .build();

                CosmosReadManyByPartitionKeysRequestOptions faultOptions =
                    new CosmosReadManyByPartitionKeysRequestOptions();
                faultOptions.setCosmosEndToEndOperationLatencyPolicyConfig(e2ePolicy);

                try {
                    // Use Flux iteration (toIterable) so we can capture per-page state
                    for (FeedResponse<ObjectNode> page : container
                        .readManyByPartitionKeys(partitionKeys, faultOptions, ObjectNode.class)
                        .byPage()
                        .toIterable()) {

                        for (ObjectNode item : page.getResults()) {
                            itemsBeforeError.add(item.get("id").asText());
                        }
                        if (page.getContinuationToken() != null) {
                            lastGoodContinuation = page.getContinuationToken();
                        }
                    }
                } catch (Exception e) {
                    errorOccurred = true;
                }

                // Step 5: The fault injection MUST have caused an error — pages from the
                // faulted partition cannot succeed with SERVICE_UNAVAILABLE on all replicas.
                assertThat(errorOccurred)
                    .as("Fault injection on the last feed range must cause an error")
                    .isTrue();

                // We must have captured at least one continuation token from successful pages
                assertThat(lastGoodContinuation)
                    .as("At least one page must have succeeded before the faulted partition")
                    .isNotNull();

                // Items collected so far must be a strict subset of the baseline
                assertThat(itemsBeforeError).doesNotHaveDuplicates();
                assertThat(itemsBeforeError.size()).isGreaterThan(0);
                assertThat(itemsBeforeError.size()).isLessThan(baselineIds.size());

                // Step 6: Disable fault injection rule
                partitionScopedRule.disable();

                // Step 7: Resume from the last good continuation token
                CosmosReadManyByPartitionKeysRequestOptions resumeOptions =
                    new CosmosReadManyByPartitionKeysRequestOptions();
                resumeOptions.setContinuationToken(lastGoodContinuation);

                List<FeedResponse<ObjectNode>> resumedPages = container
                    .readManyByPartitionKeys(partitionKeys, resumeOptions, ObjectNode.class)
                    .byPage()
                    .collectList()
                    .block();

                assertThat(resumedPages).isNotNull();

                List<String> resumedIds = resumedPages.stream()
                    .flatMap(p -> p.getResults().stream())
                    .map(n -> n.get("id").asText())
                    .collect(Collectors.toList());

                // Step 8: Assert completeness — union of before + after = all baseline items
                List<String> combined = new ArrayList<>(itemsBeforeError);
                combined.addAll(resumedIds);

                assertThat(combined).doesNotHaveDuplicates();
                assertThat(combined).hasSameElementsAs(baselineIds);

                // Cleanup
                for (ObjectNode item : allCreatedItems) {
                    try {
                        container.deleteItem(
                            item.get("id").asText(),
                            new PartitionKey(item.get("mypk").asText())).block();
                    } catch (Exception ignore) { }
                }

            } finally {
                safeClose(client);
            }
        } finally {
            if (originalBatchSize != null) {
                System.setProperty("COSMOS.READ_MANY_BY_PK_MAX_BATCH_SIZE", originalBatchSize);
            } else {
                System.clearProperty("COSMOS.READ_MANY_BY_PK_MAX_BATCH_SIZE");
            }
        }
    }

    // Helper to access testDatabaseId from base class
    private String getTestDatabaseId() {
        try {
            java.lang.reflect.Field f = FaultInjectionWithAvailabilityStrategyTestsBase.class.getDeclaredField("testDatabaseId");
            f.setAccessible(true);
            return (String) f.get(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getTestContainerId() {
        try {
            java.lang.reflect.Field f = FaultInjectionWithAvailabilityStrategyTestsBase.class.getDeclaredField("testContainerId");
            f.setAccessible(true);
            return (String) f.get(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
