// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter;

import com.azure.communication.jobrouter.models.ClassificationPolicy;
import com.azure.communication.jobrouter.models.DistributionPolicy;
import com.azure.communication.jobrouter.models.JobQueue;
import com.azure.communication.jobrouter.models.LabelOperator;
import com.azure.communication.jobrouter.models.QueueSelector;
import com.azure.communication.jobrouter.models.QueueSelectorAttachment;
import com.azure.communication.jobrouter.models.StaticQueueSelector;
import com.azure.communication.jobrouter.models.StaticRule;
import com.azure.communication.jobrouter.models.StaticWorkerSelector;
import com.azure.communication.jobrouter.models.WorkerSelector;
import com.azure.communication.jobrouter.models.WorkerSelectorAttachment;
import com.azure.communication.jobrouter.models.options.CreateClassificationPolicyOptions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClassificationPolicyLiveTests extends JobRouterTestBase {
    private RouterAdministrationClient routerAdminClient;

    @Override
    protected void beforeTest() {
        routerAdminClient = clientSetup(httpPipeline -> new RouterAdministrationClientBuilder()
            .connectionString(getConnectionString())
            .pipeline(httpPipeline)
            .buildClient());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createClassificationPolicy() {
        // Setup
        String distributionPolicyId = String.format("%s-DistributionPolicy", JAVA_LIVE_TESTS);
        DistributionPolicy distributionPolicy = createDistributionPolicy(routerAdminClient, distributionPolicyId);

        String queueId = String.format("%s-Queue", JAVA_LIVE_TESTS);
        JobQueue jobQueue = createQueue(routerAdminClient, queueId, distributionPolicy.getId());

        String classificationPolicyId = String.format("%s-ClassificationPolicy", JAVA_LIVE_TESTS);
        String classificationPolicyName = String.format("%s-Name", classificationPolicyId);

        /**
         * Create queue selectors.
         */
        StaticQueueSelector staticQueueSelector = new StaticQueueSelector()
            .setLabelSelector(new QueueSelector()
                .setKey("queueId")
                .setLabelOperator(LabelOperator.EQUAL)
                .setValue(queueId));

        List<QueueSelectorAttachment> queueSelectors = new ArrayList<QueueSelectorAttachment>() {
            {
                add(staticQueueSelector);
            }
        };


        /**
         * Create worker selectors.
         */
        StaticWorkerSelector staticWorkerSelector = new StaticWorkerSelector()
            .setLabelSelector(new WorkerSelector()
                .setKey("key")
                .setLabelOperator(LabelOperator.EQUAL)
                .setValue("value"));

        List<WorkerSelectorAttachment> workerSelectors = new ArrayList<WorkerSelectorAttachment>() {
            {
                add(staticWorkerSelector);
            }
        };

        /**
         * Create classification policy
         */
        CreateClassificationPolicyOptions createClassificationPolicyOptions = new CreateClassificationPolicyOptions(
            classificationPolicyId,
            classificationPolicyName,
            new StaticRule().setValue(1),
            workerSelectors,
            queueSelectors,
            jobQueue.getId()
        );

        // Action
        ClassificationPolicy result = routerAdminClient.createClassificationPolicy(createClassificationPolicyOptions);

        // Verify
        assertEquals(classificationPolicyId, result.getId());

        // Cleanup
        routerAdminClient.deleteClassificationPolicy(classificationPolicyId);
        routerAdminClient.deleteQueue(queueId);
        routerAdminClient.deleteDistributionPolicy(distributionPolicyId);
    }
}
