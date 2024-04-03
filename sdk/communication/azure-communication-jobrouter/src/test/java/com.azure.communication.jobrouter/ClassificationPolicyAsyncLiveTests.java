// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter;

import com.azure.communication.jobrouter.models.ClassificationPolicy;
import com.azure.communication.jobrouter.models.CreateClassificationPolicyOptions;
import com.azure.communication.jobrouter.models.CreateDistributionPolicyOptions;
import com.azure.communication.jobrouter.models.CreateJobWithClassificationPolicyOptions;
import com.azure.communication.jobrouter.models.CreateQueueOptions;
import com.azure.communication.jobrouter.models.DistributionPolicy;
import com.azure.communication.jobrouter.models.LabelOperator;
import com.azure.communication.jobrouter.models.LongestIdleMode;
import com.azure.communication.jobrouter.models.QueueSelectorAttachment;
import com.azure.communication.jobrouter.models.RouterJob;
import com.azure.communication.jobrouter.models.RouterQueue;
import com.azure.communication.jobrouter.models.RouterQueueSelector;
import com.azure.communication.jobrouter.models.RouterValue;
import com.azure.communication.jobrouter.models.RouterWorkerSelector;
import com.azure.communication.jobrouter.models.StaticQueueSelectorAttachment;
import com.azure.communication.jobrouter.models.StaticRouterRule;
import com.azure.communication.jobrouter.models.StaticWorkerSelectorAttachment;
import com.azure.communication.jobrouter.models.WorkerSelectorAttachment;
import com.azure.core.http.HttpClient;
import com.azure.core.test.TestMode;
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
    private JobRouterAsyncClient routerAsyncClient;

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createClassificationPolicy(HttpClient httpClient) throws InterruptedException {
        // Setup
        routerAsyncClient = getRouterAsyncClient(httpClient);
        administrationAsyncClient = getRouterAdministrationAsyncClient(httpClient);
        String testName = "classification-policy-test-async";
        String distributionPolicyId = String.format("%s-%s-DistributionPolicy", JAVA_LIVE_TESTS, testName);
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

        String queueId = String.format("%s-%s-Queue", JAVA_LIVE_TESTS, testName);
        String fallbackQueueId = String.format("%s-%s-FallbackQueue", JAVA_LIVE_TESTS, testName);
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
        RouterQueue fallbackQueue = administrationAsyncClient.createQueue(
            new CreateQueueOptions(fallbackQueueId, distributionPolicyId)).block();

        String classificationPolicyId = String.format("%s-%s-ClassificationPolicy", JAVA_LIVE_TESTS, testName);
        String classificationPolicyName = String.format("%s-Name", classificationPolicyId);

        StaticQueueSelectorAttachment staticQueueSelector = new StaticQueueSelectorAttachment(
            new RouterQueueSelector("Id", LabelOperator.EQUAL, new RouterValue(queueId)));

        List<QueueSelectorAttachment> queueSelectors = new ArrayList<QueueSelectorAttachment>() {
            {
                add(staticQueueSelector);
            }
        };

        StaticWorkerSelectorAttachment staticWorkerSelector = new StaticWorkerSelectorAttachment(
            new RouterWorkerSelector("key", LabelOperator.EQUAL, new RouterValue("value")));

        List<WorkerSelectorAttachment> workerSelectors = new ArrayList<WorkerSelectorAttachment>() {
            {
                add(staticWorkerSelector);
            }
        };

        CreateClassificationPolicyOptions createClassificationPolicyOptions = new CreateClassificationPolicyOptions(
            classificationPolicyId)
            .setName(classificationPolicyName)
            .setPrioritizationRule(new StaticRouterRule().setValue(new RouterValue(1)))
            .setWorkerSelectors(workerSelectors)
            .setQueueSelectors(queueSelectors)
            .setFallbackQueueId(fallbackQueue.getId());

        String jobId = String.format("%s-%s-Job", JAVA_LIVE_TESTS, testName);
        String channelId = String.format("%s-%s-Channel", JAVA_LIVE_TESTS, testName);

        // Action
        ClassificationPolicy result = administrationAsyncClient.createClassificationPolicy(createClassificationPolicyOptions).block();
        RouterJob job = routerAsyncClient.createJobWithClassificationPolicy(
            new CreateJobWithClassificationPolicyOptions(jobId, channelId, classificationPolicyId)).block();

        if (this.getTestMode() != TestMode.PLAYBACK) {
            Thread.sleep(5000);
        }

        // Verify
        assertEquals(classificationPolicyId, result.getId());
        assertEquals(classificationPolicyName, result.getName());
        assertEquals(StaticRouterRule.class, result.getPrioritizationRule().getClass());
        assertEquals(1, result.getWorkerSelectorAttachments().size());
        assertEquals(1, result.getQueueSelectorAttachments().size());
        assertEquals(fallbackQueueId, result.getFallbackQueueId());

        assertEquals(jobId, job.getId());
        assertEquals(classificationPolicyId, job.getClassificationPolicyId());
        assertEquals(queueId, job.getQueueId());
        assertEquals(channelId, job.getChannelId());
        assertEquals(1, job.getPriority());
        assertEquals(1, job.getAttachedWorkerSelectors().size());

        // Cleanup
        routerAsyncClient.cancelJob(job.getId()).block();
        routerAsyncClient.deleteJob(job.getId()).block();
        administrationAsyncClient.deleteClassificationPolicy(classificationPolicyId).block();
        administrationAsyncClient.deleteQueue(queueId).block();
        administrationAsyncClient.deleteQueue(fallbackQueueId).block();
        administrationAsyncClient.deleteDistributionPolicy(distributionPolicyId).block();
    }
}
