// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter;

import com.azure.communication.jobrouter.models.CreateDistributionPolicyOptions;
import com.azure.communication.jobrouter.models.CreateQueueOptions;
import com.azure.communication.jobrouter.models.CreateWorkerOptions;
import com.azure.communication.jobrouter.models.DistributionPolicy;
import com.azure.communication.jobrouter.models.LongestIdleMode;
import com.azure.communication.jobrouter.models.RouterChannel;
import com.azure.communication.jobrouter.models.RouterQueue;
import com.azure.communication.jobrouter.models.RouterValue;
import com.azure.communication.jobrouter.models.RouterWorker;
import com.azure.core.http.HttpClient;
import com.azure.core.test.annotation.LiveOnly;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RouterWorkerAsyncLiveTests extends JobRouterTestBase {
    private JobRouterAsyncClient routerAsyncClient;

    private JobRouterAdministrationAsyncClient administrationAsyncClient;

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @LiveOnly // Remove after azure-core-test 1.26.0-beta.1 is released.
    public void createWorker(HttpClient httpClient) {
        // Setup
        routerAsyncClient = getRouterAsyncClient(httpClient);
        administrationAsyncClient = getRouterAdministrationAsyncClient(httpClient);
        /**
         * Setup queue
         */
        String distributionPolicyId = String.format("%s-CreateWorker-DistributionPolicy", JAVA_LIVE_TESTS);
        String distributionPolicyName = String.format("%s-Name", distributionPolicyId);

        CreateDistributionPolicyOptions createDistributionPolicyOptions = new CreateDistributionPolicyOptions(
            distributionPolicyId,
            Duration.ofSeconds(10),
            new LongestIdleMode()
                .setMinConcurrentOffers(1)
                .setMaxConcurrentOffers(10)
        )
            .setName(distributionPolicyName);
        DistributionPolicy distributionPolicy = administrationAsyncClient.createDistributionPolicy(createDistributionPolicyOptions).block();

        String queueId = String.format("%s-CreateWorker-Queue", JAVA_LIVE_TESTS);
        String queueName = String.format("%s-Name", queueId);
        Map<String, RouterValue> queueLabels = new HashMap<String, RouterValue>() {
            {
                put("Label_1", new RouterValue("Value_1"));
            }
        };

        CreateQueueOptions createQueueOptions = new CreateQueueOptions(queueId, distributionPolicyId)
            .setLabels(queueLabels)
            .setName(queueName);
        RouterQueue jobQueue = administrationAsyncClient.createQueue(createQueueOptions).block();

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
        RouterWorker result = routerAsyncClient.createWorker(createWorkerOptions).block();

        // Verify
        assertEquals(workerId, result.getId());

        // Cleanup
        routerAsyncClient.deleteWorker(workerId).block();
        administrationAsyncClient.deleteQueue(queueId).block();
        administrationAsyncClient.deleteDistributionPolicy(distributionPolicyId).block();
    }
}
