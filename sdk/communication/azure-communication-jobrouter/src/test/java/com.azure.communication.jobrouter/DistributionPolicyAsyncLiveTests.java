package com.azure.communication.jobrouter;

import com.azure.communication.jobrouter.models.BestWorkerMode;
import com.azure.communication.jobrouter.models.CreateDistributionPolicyOptions;
import com.azure.communication.jobrouter.models.DistributionPolicy;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.RequestOptions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DistributionPolicyAsyncLiveTests extends JobRouterTestBase {
    private JobRouterAdministrationAsyncClient administrationAsyncClient;


    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createDistributionPolicyBestWorkerDefaultScoringRule(HttpClient httpClient) {
        // Setup
        administrationAsyncClient = getRouterAdministrationAsyncClient(httpClient);
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
        DistributionPolicy result = administrationAsyncClient.createDistributionPolicy(createDistributionPolicyOptions, new RequestOptions()).block();

        // Verify
        assertEquals(bestWorkerModeDistributionPolicyId, result.getId());

        // Cleanup
        administrationAsyncClient.deleteDistributionPolicy(bestWorkerModeDistributionPolicyId).block();
    }
}
