// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter;

import com.azure.communication.jobrouter.models.ChannelConfiguration;
import com.azure.communication.jobrouter.models.DistributionPolicy;
import com.azure.communication.jobrouter.models.JobQueue;
import com.azure.communication.jobrouter.models.LabelValue;
import com.azure.communication.jobrouter.models.QueueAssignment;
import com.azure.communication.jobrouter.models.RouterWorker;
import com.azure.communication.jobrouter.models.options.CreateWorkerOptions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RouterWorkerLiveTests extends JobRouterTestBase {
    private RouterClient routerClient;

    private RouterAdministrationClient routerAdminClient;

    @Override
    protected void beforeTest() {
        routerClient = clientSetup(httpPipeline -> new RouterClientBuilder()
            .connectionString(getConnectionString())
            .pipeline(httpPipeline)
            .buildClient());

        routerAdminClient = clientSetup(httpPipeline -> new RouterAdministrationClientBuilder()
            .connectionString(getConnectionString())
            .pipeline(httpPipeline)
            .buildClient());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createWorker() {
        // Setup
        /**
         * Setup queue
         */
        String distributionPolicyId = String.format("%s-CreateWorker-DistributionPolicy", JAVA_LIVE_TESTS);
        DistributionPolicy distributionPolicy = createDistributionPolicy(routerAdminClient, distributionPolicyId);

        String queueId = String.format("%s-CreateWorker-Queue", JAVA_LIVE_TESTS);
        JobQueue jobQueue = createQueue(routerAdminClient, queueId, distributionPolicy.getId());

        /**
         * Setup worker
         */
        String workerId = String.format("%s-CreateWorker-Worker", JAVA_LIVE_TESTS);

        Map<String, LabelValue> labels = new HashMap<String, LabelValue>() {
            {
                put("Label", new LabelValue("Value"));
            }
        };

        Map<String, Object> tags = new HashMap<String, Object>() {
            {
                put("Tag", "Value");
            }
        };

        ChannelConfiguration channelConfiguration = new ChannelConfiguration();
        channelConfiguration.setCapacityCostPerJob(1);
        Map<String, ChannelConfiguration> channelConfigurations = new HashMap<String, ChannelConfiguration>() {
            {
                put("channel1", channelConfiguration);
            }
        };

        Map<String, QueueAssignment> queueAssignments = new HashMap<String, QueueAssignment>() {
            {
                put(jobQueue.getId(), new QueueAssignment());
            }
        };

        CreateWorkerOptions createWorkerOptions = new CreateWorkerOptions(workerId, 10)
            .setLabels(labels)
            .setTags(tags)
            .setAvailableForOffers(false)
            .setChannelConfigurations(channelConfigurations)
            .setQueueAssignments(queueAssignments);

        // Action
        RouterWorker result = routerClient.createWorker(createWorkerOptions);

        // Verify
        assertEquals(workerId, result.getId());

        // Cleanup
        routerClient.deleteWorker(workerId);
        routerAdminClient.deleteQueue(queueId);
        routerAdminClient.deleteDistributionPolicy(distributionPolicyId);
    }
}
