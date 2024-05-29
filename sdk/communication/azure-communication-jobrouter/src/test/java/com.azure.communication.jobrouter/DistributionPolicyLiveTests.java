// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter;

import com.azure.communication.jobrouter.models.BestWorkerMode;
import com.azure.communication.jobrouter.models.CreateDistributionPolicyOptions;
import com.azure.communication.jobrouter.models.DistributionPolicy;
import com.azure.communication.jobrouter.models.FunctionRouterRule;
import com.azure.communication.jobrouter.models.FunctionRouterRuleCredential;
import com.azure.communication.jobrouter.models.LongestIdleMode;
import com.azure.communication.jobrouter.models.RoundRobinMode;
import com.azure.core.http.HttpClient;
import com.azure.core.test.annotation.LiveOnly;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DistributionPolicyLiveTests extends JobRouterTestBase {
    private JobRouterAdministrationClient routerAdminClient;

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @LiveOnly // Remove after azure-core-test 1.26.0-beta.1 is released.
    public void createDistributionPolicyBestWorkerDefaultScoringRule(HttpClient httpClient) {
        // Setup
        routerAdminClient = getRouterAdministrationClient(httpClient);
        String bestWorkerModeDistributionPolicyId = String.format("%s-BestWorkerDefaultScoringRule-DistributionPolicy", JAVA_LIVE_TESTS);
        String bestWorkerModeDistributionPolicyName = String.format("%s-Name", bestWorkerModeDistributionPolicyId);

        CreateDistributionPolicyOptions createDistributionPolicyOptions = new CreateDistributionPolicyOptions(
            bestWorkerModeDistributionPolicyId,
            Duration.ofSeconds(10),
            new BestWorkerMode()
                .setMinConcurrentOffers(1)
                .setMaxConcurrentOffers(10)
        )
            .setName(bestWorkerModeDistributionPolicyName);

        // Action
        DistributionPolicy result = routerAdminClient.createDistributionPolicy(createDistributionPolicyOptions);

        // Verify
        assertEquals(bestWorkerModeDistributionPolicyId, result.getId());

        // Cleanup
        routerAdminClient.deleteDistributionPolicy(bestWorkerModeDistributionPolicyId);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @LiveOnly // Remove after azure-core-test 1.26.0-beta.1 is released.
    public void createDistributionPolicyBestWorkerAzureFunctionRule(HttpClient httpClient) {
        // Setup
        routerAdminClient = getRouterAdministrationClient(httpClient);
        String bestWorkerModeDistributionPolicyId = String.format("%s-BestWorkerAzureFunctionRule-DistributionPolicy", JAVA_LIVE_TESTS);
        String bestWorkerModeDistributionPolicyName = String.format("%s-Name", bestWorkerModeDistributionPolicyId);

        FunctionRouterRule azureFunctionRule = new FunctionRouterRule("https://my.function.app/api/myfunction?code=Kg==")
            .setCredential(new FunctionRouterRuleCredential()
                .setAppKey("MyAppKey")
                .setClientId("MyClientId"));

        CreateDistributionPolicyOptions createDistributionPolicyOptions = new CreateDistributionPolicyOptions(
            bestWorkerModeDistributionPolicyId,
            Duration.ofSeconds(10),
            new BestWorkerMode()
                .setScoringRule(azureFunctionRule)
                .setMinConcurrentOffers(1)
                .setMaxConcurrentOffers(10)
        )
            .setName(bestWorkerModeDistributionPolicyName);

        // Action
        DistributionPolicy result = routerAdminClient.createDistributionPolicy(createDistributionPolicyOptions);

        // Verify
        assertEquals(bestWorkerModeDistributionPolicyId, result.getId());

        // Cleanup
        routerAdminClient.deleteDistributionPolicy(bestWorkerModeDistributionPolicyId);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @LiveOnly // Remove after azure-core-test 1.26.0-beta.1 is released.
    public void createDistributionPolicyLongestIdle(HttpClient httpClient) {
        // Setup
        routerAdminClient = getRouterAdministrationClient(httpClient);
        String longestIdleModeDistributionPolicyId = String.format("%s-LongestIdle-DistributionPolicy", JAVA_LIVE_TESTS);
        String longestIdleModeDistributionPolicyName = String.format("%s-Name", longestIdleModeDistributionPolicyId);

        CreateDistributionPolicyOptions createDistributionPolicyOptions = new CreateDistributionPolicyOptions(
            longestIdleModeDistributionPolicyId,
            Duration.ofSeconds(10),
            new LongestIdleMode()
                .setMinConcurrentOffers(1)
                .setMaxConcurrentOffers(10)
            )
            .setName(longestIdleModeDistributionPolicyName);

        // Action
        DistributionPolicy result = routerAdminClient.createDistributionPolicy(createDistributionPolicyOptions);

        // Verify
        assertEquals(longestIdleModeDistributionPolicyId, result.getId());

        // Cleanup
        routerAdminClient.deleteDistributionPolicy(longestIdleModeDistributionPolicyId);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @LiveOnly // Remove after azure-core-test 1.26.0-beta.1 is released.
    public void createDistributionPolicyRoundRobin(HttpClient httpClient) {
        // Setup
        routerAdminClient = getRouterAdministrationClient(httpClient);
        String roundRobinModeDistributionPolicyId = String.format("%s-RoundRobin-DistributionPolicy", JAVA_LIVE_TESTS);
        String roundRobinModeDistributionPolicyName = String.format("%s-Name", roundRobinModeDistributionPolicyId);

        CreateDistributionPolicyOptions createDistributionPolicyOptions = new CreateDistributionPolicyOptions(
            roundRobinModeDistributionPolicyId,
            Duration.ofSeconds(10),
            new RoundRobinMode()
                .setMinConcurrentOffers(1)
                .setMaxConcurrentOffers(10)
        )
            .setName(roundRobinModeDistributionPolicyName);

        // Action
        DistributionPolicy result = routerAdminClient.createDistributionPolicy(createDistributionPolicyOptions);

        // Verify
        assertEquals(roundRobinModeDistributionPolicyId, result.getId());

        // Cleanup
        routerAdminClient.deleteDistributionPolicy(roundRobinModeDistributionPolicyId);
    }
}
