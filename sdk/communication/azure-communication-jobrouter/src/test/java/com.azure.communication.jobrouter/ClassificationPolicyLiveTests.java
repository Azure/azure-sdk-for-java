// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter;

import com.azure.communication.jobrouter.models.ClassificationPolicy;
import com.azure.communication.jobrouter.models.CreateClassificationPolicyOptions;
import com.azure.communication.jobrouter.models.DistributionPolicy;
import com.azure.communication.jobrouter.models.LabelOperator;
import com.azure.communication.jobrouter.models.QueueSelectorAttachment;
import com.azure.communication.jobrouter.models.RouterQueue;
import com.azure.communication.jobrouter.models.RouterQueueSelector;
import com.azure.communication.jobrouter.models.RouterValue;
import com.azure.communication.jobrouter.models.RouterWorkerSelector;
import com.azure.communication.jobrouter.models.StaticQueueSelectorAttachment;
import com.azure.communication.jobrouter.models.StaticRouterRule;
import com.azure.communication.jobrouter.models.StaticWorkerSelectorAttachment;
import com.azure.communication.jobrouter.models.WorkerSelectorAttachment;
import com.azure.core.http.HttpClient;
import com.azure.core.test.annotation.LiveOnly;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClassificationPolicyLiveTests extends JobRouterTestBase {
    private JobRouterAdministrationClient routerAdminClient;

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @LiveOnly // Remove after azure-core-test 1.26.0-beta.1 is released.
    public void createClassificationPolicy(HttpClient httpClient) {
        // Setup
        routerAdminClient = getRouterAdministrationClient(httpClient);
        String distributionPolicyId = String.format("%s-DistributionPolicy", JAVA_LIVE_TESTS);
        DistributionPolicy distributionPolicy = createDistributionPolicy(routerAdminClient, distributionPolicyId);

        String queueId = String.format("%s-Queue", JAVA_LIVE_TESTS);
        RouterQueue jobQueue = createQueue(routerAdminClient, queueId, distributionPolicy.getId());

        String classificationPolicyId = String.format("%s-ClassificationPolicy", JAVA_LIVE_TESTS);
        String classificationPolicyName = String.format("%s-Name", classificationPolicyId);

        /**
         * Create queue selectors.
         */
        StaticQueueSelectorAttachment staticQueueSelector = new StaticQueueSelectorAttachment(
            new RouterQueueSelector("queueId", LabelOperator.EQUAL)
                .setValue(new RouterValue(queueId)));

        List<QueueSelectorAttachment> queueSelectors = new ArrayList<QueueSelectorAttachment>() {
            {
                add(staticQueueSelector);
            }
        };


        /**
         * Create worker selectors.
         */
        StaticWorkerSelectorAttachment staticWorkerSelector = new StaticWorkerSelectorAttachment(
            new RouterWorkerSelector("key", LabelOperator.EQUAL)
                .setValue(new RouterValue("value")));

        List<WorkerSelectorAttachment> workerSelectors = new ArrayList<WorkerSelectorAttachment>() {
            {
                add(staticWorkerSelector);
            }
        };

        /**
         * Create classification policy
         */
        CreateClassificationPolicyOptions createClassificationPolicyOptions = new CreateClassificationPolicyOptions(
            classificationPolicyId)
            .setName(classificationPolicyName)
            .setPrioritizationRule(new StaticRouterRule().setValue(new RouterValue(1)))
            .setWorkerSelectors(workerSelectors)
            .setQueueSelectors(queueSelectors)
            .setFallbackQueueId(jobQueue.getId());

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
