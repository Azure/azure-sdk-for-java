// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter;

import com.azure.communication.jobrouter.models.CreateWorkerOptions;
import com.azure.communication.jobrouter.models.DistributionPolicy;
import com.azure.communication.jobrouter.models.RouterChannel;
import com.azure.communication.jobrouter.models.RouterQueue;
import com.azure.communication.jobrouter.models.RouterValue;
import com.azure.communication.jobrouter.models.RouterWorker;
import com.azure.core.http.HttpClient;
import com.azure.core.test.annotation.LiveOnly;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RouterWorkerLiveTests extends JobRouterTestBase {
    private JobRouterClient jobRouterClient;

    private JobRouterAdministrationClient routerAdminClient;

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @LiveOnly // Remove after azure-core-test 1.26.0-beta.1 is released.
    public void createWorker(HttpClient httpClient) {
        // Setup
        jobRouterClient = getRouterClient(httpClient);
        routerAdminClient = getRouterAdministrationClient(httpClient);
        /**
         * Setup queue
         */
        String distributionPolicyId = String.format("%s-CreateWorker-DistributionPolicy", JAVA_LIVE_TESTS);
        DistributionPolicy distributionPolicy = createDistributionPolicy(routerAdminClient, distributionPolicyId);

        String queueId = String.format("%s-CreateWorker-Queue", JAVA_LIVE_TESTS);
        RouterQueue jobQueue = createQueue(routerAdminClient, queueId, distributionPolicy.getId());

        /**
         * Setup worker
         */
        String workerId = String.format("%s-CreateWorker-Worker", JAVA_LIVE_TESTS);

        Map<String, RouterValue> labels = new HashMap<String, RouterValue>() {
            {
                put("Label", new RouterValue("Value"));
            }
        };

        Map<String, RouterValue> tags = new HashMap<String, RouterValue>() {
            {
                put("Tag", new RouterValue("Value"));
            }
        };

        RouterChannel channel = new RouterChannel("router-channel", 1);
        List<RouterChannel> channels = new ArrayList<RouterChannel>() {
            {
                add(channel);
            }
        };

        List<String> queues = new ArrayList<String>() {
            {
                add(jobQueue.getId());
            }
        };

        CreateWorkerOptions createWorkerOptions = new CreateWorkerOptions(workerId, 10)
            .setLabels(labels)
            .setTags(tags)
            .setAvailableForOffers(false)
            .setChannels(channels)
            .setQueues(queues);

        // Action
        RouterWorker result = jobRouterClient.createWorker(createWorkerOptions);

        // Verify
        assertEquals(workerId, result.getId());

        // Cleanup
        jobRouterClient.deleteWorker(workerId);
        routerAdminClient.deleteQueue(queueId);
        routerAdminClient.deleteDistributionPolicy(distributionPolicyId);
    }
}
