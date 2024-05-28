// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter;

import com.azure.communication.jobrouter.models.ClassificationPolicy;
import com.azure.communication.jobrouter.models.CreateClassificationPolicyOptions;
import com.azure.communication.jobrouter.models.CreateDistributionPolicyOptions;
import com.azure.communication.jobrouter.models.CreateQueueOptions;
import com.azure.communication.jobrouter.models.DistributionPolicy;
import com.azure.communication.jobrouter.models.LabelOperator;
import com.azure.communication.jobrouter.models.LongestIdleMode;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClassificationPolicyAsyncLiveTests extends JobRouterTestBase {
    private JobRouterAdministrationAsyncClient administrationAsyncClient;

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createClassificationPolicy(HttpClient httpClient) {
        // Setup
        administrationAsyncClient = getRouterAdministrationAsyncClient(httpClient);
        String distributionPolicyId = String.format("%s-DistributionPolicy", JAVA_LIVE_TESTS);

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

        String queueId = String.format("%s-Queue", JAVA_LIVE_TESTS);

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
        ClassificationPolicy result = administrationAsyncClient.createClassificationPolicy(createClassificationPolicyOptions).block();

        // Verify
        assertEquals(classificationPolicyId, result.getId());

        // Cleanup
        administrationAsyncClient.deleteClassificationPolicy(classificationPolicyId).block();
        administrationAsyncClient.deleteQueue(queueId).block();
        administrationAsyncClient.deleteDistributionPolicy(distributionPolicyId).block();
    }
}
