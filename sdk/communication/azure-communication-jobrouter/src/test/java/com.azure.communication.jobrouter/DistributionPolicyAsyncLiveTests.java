// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter;

import com.azure.communication.jobrouter.models.BestWorkerMode;
import com.azure.communication.jobrouter.models.CreateDistributionPolicyOptions;
import com.azure.communication.jobrouter.models.DistributionModeKind;
import com.azure.communication.jobrouter.models.DistributionPolicy;
import com.azure.communication.jobrouter.models.RouterValue;
import com.azure.communication.jobrouter.models.ScoringRuleOptions;
import com.azure.communication.jobrouter.models.ScoringRuleParameterSelector;
import com.azure.communication.jobrouter.models.StaticRouterRule;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DistributionPolicyAsyncLiveTests extends JobRouterTestBase {

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createDistributionPolicyBestWorkerDefaultScoringRule(HttpClient httpClient) {
        // Setup
        JobRouterAdministrationAsyncClient administrationAsyncClient = getRouterAdministrationAsyncClient(httpClient);
        String bestWorkerModeDistributionPolicyId = String.format("%s-BestWorkerDefaultScoringRuleAsync-DistributionPolicy", JAVA_LIVE_TESTS);
        String bestWorkerModeDistributionPolicyName = String.format("%s-Name", bestWorkerModeDistributionPolicyId);

        CreateDistributionPolicyOptions createDistributionPolicyOptions = new CreateDistributionPolicyOptions(
            bestWorkerModeDistributionPolicyId,
            Duration.ofSeconds(10),
            new BestWorkerMode()
                .setMinConcurrentOffers(1)
                .setMaxConcurrentOffers(10)
                .setScoringRule(new StaticRouterRule().setValue(new RouterValue(5)))
                .setBypassSelectors(true)
                .setScoringRuleOptions(new ScoringRuleOptions()
                    .setBatchScoringEnabled(true)
                    .setScoringParameters(Collections.singletonList(ScoringRuleParameterSelector.JOB_LABELS))
                    .setBatchSize(30)
                    .setDescendingOrder(true)))
            .setName(bestWorkerModeDistributionPolicyName);

        // Action
        DistributionPolicy result = administrationAsyncClient.createDistributionPolicy(createDistributionPolicyOptions).block();

        // Verify
        assertEquals(bestWorkerModeDistributionPolicyId, result.getId());
        assertNotNull(result.getEtag());
        assertEquals(Duration.ofSeconds(10), result.getOfferExpiresAfter());
        assertEquals(DistributionModeKind.BEST_WORKER, result.getMode().getKind());
        assertEquals(bestWorkerModeDistributionPolicyName, result.getName());
        assertEquals(1, result.getMode().getMinConcurrentOffers());
        assertEquals(10, result.getMode().getMaxConcurrentOffers());
        assertEquals(true, result.getMode().isBypassSelectors());
        assertEquals(5, ((StaticRouterRule) ((BestWorkerMode) result.getMode()).getScoringRule()).getValue().getIntValue());
        assertEquals(30, ((BestWorkerMode) result.getMode()).getScoringRuleOptions().getBatchSize());
        assertEquals(true, ((BestWorkerMode) result.getMode()).getScoringRuleOptions().isDescendingOrder());
        assertEquals(true, ((BestWorkerMode) result.getMode()).getScoringRuleOptions().isBatchScoringEnabled());
        assertEquals(1, ((BestWorkerMode) result.getMode()).getScoringRuleOptions().getScoringParameters().size());

        Response<BinaryData> binaryResponse = administrationAsyncClient.getDistributionPolicyWithResponse(result.getId(), null).block();
        DistributionPolicy deserialized = binaryResponse.getValue().toObject(DistributionPolicy.class);

        assertEquals(bestWorkerModeDistributionPolicyId, deserialized.getId());
        assertEquals(result.getEtag(), deserialized.getEtag());
        assertEquals(Duration.ofSeconds(10), deserialized.getOfferExpiresAfter());
        assertEquals(DistributionModeKind.BEST_WORKER, deserialized.getMode().getKind());
        assertEquals(bestWorkerModeDistributionPolicyName, deserialized.getName());
        assertEquals(1, deserialized.getMode().getMinConcurrentOffers());
        assertEquals(10, deserialized.getMode().getMaxConcurrentOffers());
        assertEquals(true, deserialized.getMode().isBypassSelectors());
        assertEquals(5, ((StaticRouterRule) ((BestWorkerMode) deserialized.getMode()).getScoringRule()).getValue().getIntValue());
        assertEquals(30, ((BestWorkerMode) deserialized.getMode()).getScoringRuleOptions().getBatchSize());
        assertEquals(true, ((BestWorkerMode) deserialized.getMode()).getScoringRuleOptions().isDescendingOrder());
        assertEquals(true, ((BestWorkerMode) deserialized.getMode()).getScoringRuleOptions().isBatchScoringEnabled());
        assertEquals(1, ((BestWorkerMode) deserialized.getMode()).getScoringRuleOptions().getScoringParameters().size());

        ((BestWorkerMode) deserialized.getMode()).getScoringRuleOptions().setScoringParameters(new ArrayList<>());
        deserialized.setOfferExpiresAfter(Duration.ofMinutes(5));
        DistributionPolicy updatedPolicy = administrationAsyncClient.updateDistributionPolicy(
            deserialized.getId(), deserialized).block();

        assertEquals(bestWorkerModeDistributionPolicyId, updatedPolicy.getId());
        assertNotEquals(result.getEtag(), updatedPolicy.getEtag());
        assertEquals(Duration.ofMinutes(5), updatedPolicy.getOfferExpiresAfter());
        assertEquals(DistributionModeKind.BEST_WORKER, updatedPolicy.getMode().getKind());
        assertEquals(bestWorkerModeDistributionPolicyName, updatedPolicy.getName());
        assertEquals(1, updatedPolicy.getMode().getMinConcurrentOffers());
        assertEquals(10, updatedPolicy.getMode().getMaxConcurrentOffers());
        assertEquals(true, updatedPolicy.getMode().isBypassSelectors());
        assertEquals(5, ((StaticRouterRule) ((BestWorkerMode) updatedPolicy.getMode()).getScoringRule()).getValue().getIntValue());
        assertEquals(30, ((BestWorkerMode) updatedPolicy.getMode()).getScoringRuleOptions().getBatchSize());
        assertEquals(true, ((BestWorkerMode) updatedPolicy.getMode()).getScoringRuleOptions().isDescendingOrder());
        assertEquals(true, ((BestWorkerMode) updatedPolicy.getMode()).getScoringRuleOptions().isBatchScoringEnabled());
        assertEquals(0, ((BestWorkerMode) updatedPolicy.getMode()).getScoringRuleOptions().getScoringParameters().size());

        // Cleanup
        administrationAsyncClient.deleteDistributionPolicy(bestWorkerModeDistributionPolicyId).block();
    }
}
