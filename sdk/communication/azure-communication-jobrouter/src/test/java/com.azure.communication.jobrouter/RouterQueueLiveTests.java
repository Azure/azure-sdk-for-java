// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter;

import com.azure.communication.jobrouter.models.DistributionPolicy;
import com.azure.communication.jobrouter.models.LabelValue;
import com.azure.communication.jobrouter.models.RouterQueue;
import com.azure.communication.jobrouter.models.UpdateQueueOptions;
import com.azure.core.http.HttpClient;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RouterQueueLiveTests extends JobRouterTestBase {
    private JobRouterClient jobRouterClient;

    private JobRouterAdministrationClient routerAdminClient;

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createQueue(HttpClient httpClient) {
        // Setup
        jobRouterClient = getRouterClient(httpClient);
        routerAdminClient = getRouterAdministrationClient(httpClient);
        String distributionPolicyId = String.format("%s-CreateQueue-DistributionPolicy", JAVA_LIVE_TESTS);
        DistributionPolicy distributionPolicy = createDistributionPolicy(routerAdminClient, distributionPolicyId);

        String queueId = String.format("%s-CreateQueue-Queue", JAVA_LIVE_TESTS);

        // Action
        RouterQueue jobQueue = createQueue(routerAdminClient, queueId, distributionPolicy.getId());

        // Verify
        assertEquals(queueId, jobQueue.getId());

        // Cleanup
        routerAdminClient.deleteQueue(queueId);
        routerAdminClient.deleteDistributionPolicy(distributionPolicyId);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void updateQueue(HttpClient httpClient) {
        // Setup
        jobRouterClient = getRouterClient(httpClient);
        routerAdminClient = getRouterAdministrationClient(httpClient);
        String distributionPolicyId = String.format("%s-CreateQueue-DistributionPolicy", JAVA_LIVE_TESTS);
        DistributionPolicy distributionPolicy = createDistributionPolicy(routerAdminClient, distributionPolicyId);

        String queueId = String.format("%s-CreateQueue-Queue", JAVA_LIVE_TESTS);
        RouterQueue queue = createQueue(routerAdminClient, queueId, distributionPolicy.getId());

        Map<String, LabelValue> updatedQueueLabels = new HashMap<String, LabelValue>() {
            {
                put("Label_1", new LabelValue("UpdatedValue"));
            }
        };

        // Action
        queue = routerAdminClient.updateQueue(new UpdateQueueOptions(queueId).setLabels(updatedQueueLabels));

        // Verify
        assertEquals(updatedQueueLabels.get("Label_1").getValue(), queue.getLabels().get("Label_1").getValue());

        // Cleanup
        routerAdminClient.deleteQueue(queueId);
        routerAdminClient.deleteDistributionPolicy(distributionPolicyId);
    }
}
