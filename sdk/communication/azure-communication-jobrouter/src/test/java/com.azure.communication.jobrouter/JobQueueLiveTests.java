// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter;

import com.azure.communication.jobrouter.models.DistributionPolicy;
import com.azure.communication.jobrouter.models.JobQueue;
import com.azure.communication.jobrouter.models.LabelValue;
import com.azure.communication.jobrouter.models.options.UpdateQueueOptions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JobQueueLiveTests extends JobRouterTestBase {
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

    @Test
    public void createQueue() {
        // Setup
        String distributionPolicyId = String.format("%s-CreateQueue-DistributionPolicy", JAVA_LIVE_TESTS);
        DistributionPolicy distributionPolicy = createDistributionPolicy(routerAdminClient, distributionPolicyId);
        distributionPoliciesToDelete.add(distributionPolicyId);

        String queueId = String.format("%s-CreateQueue-Queue", JAVA_LIVE_TESTS);

        // Action
        JobQueue jobQueue = createQueue(routerAdminClient, queueId, distributionPolicy.getId());
        queuesToDelete.add(queueId);

        // Verify
        assertEquals(queueId, jobQueue.getId());
    }

    @Test
    public void updateQueue() {
        // Setup
        String distributionPolicyId = String.format("%s-CreateQueue-DistributionPolicy", JAVA_LIVE_TESTS);
        DistributionPolicy distributionPolicy = createDistributionPolicy(routerAdminClient, distributionPolicyId);
        distributionPoliciesToDelete.add(distributionPolicyId);

        String queueId = String.format("%s-CreateQueue-Queue", JAVA_LIVE_TESTS);
        JobQueue jobQueue = createQueue(routerAdminClient, queueId, distributionPolicy.getId());
        queuesToDelete.add(queueId);

        Map<String, LabelValue> updatedQueueLabels = new HashMap<String, LabelValue>() {
            {
                put("Label_1", new LabelValue("UpdatedValue"));
            }
        };

        // Action
        jobQueue = routerAdminClient.updateQueue(new UpdateQueueOptions(queueId).setLabels(updatedQueueLabels));

        // Verify
        assertEquals(updatedQueueLabels.get("Label_1").getValue(), jobQueue.getLabels().get("Label_1"));
    }
}
