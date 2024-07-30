// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter;

import com.azure.communication.jobrouter.models.ClassificationPolicy;
import com.azure.communication.jobrouter.models.ConditionalWorkerSelectorAttachment;
import com.azure.communication.jobrouter.models.CreateClassificationPolicyOptions;
import com.azure.communication.jobrouter.models.CreateDistributionPolicyOptions;
import com.azure.communication.jobrouter.models.CreateJobWithClassificationPolicyOptions;
import com.azure.communication.jobrouter.models.CreateQueueOptions;
import com.azure.communication.jobrouter.models.DistributionPolicy;
import com.azure.communication.jobrouter.models.LabelOperator;
import com.azure.communication.jobrouter.models.LongestIdleMode;
import com.azure.communication.jobrouter.models.PassThroughWorkerSelectorAttachment;
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
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ClassificationPolicyAsyncLiveTests extends JobRouterTestBase {

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createClassificationPolicy(HttpClient httpClient) {
        // Setup
        JobRouterAsyncClient routerAsyncClient = getRouterAsyncClient(httpClient);
        JobRouterAdministrationAsyncClient administrationAsyncClient = getRouterAdministrationAsyncClient(httpClient);
        String testName = "classification-policy-test-async";
        String distributionPolicyId = String.format("%s-%s-DistributionPolicy", JAVA_LIVE_TESTS, testName);
        String distributionPolicyName = String.format("%s-Name", distributionPolicyId);

        CreateDistributionPolicyOptions createDistributionPolicyOptions = new CreateDistributionPolicyOptions(
            distributionPolicyId,
            Duration.ofSeconds(10),
            new LongestIdleMode()
                .setMinConcurrentOffers(1)
                .setMaxConcurrentOffers(10))
            .setName(distributionPolicyName);

        DistributionPolicy distributionPolicy = administrationAsyncClient.createDistributionPolicy(createDistributionPolicyOptions).block();

        String queueId = String.format("%s-%s-Queue", JAVA_LIVE_TESTS, testName);
        String fallbackQueueId = String.format("%s-%s-FallbackQueue", JAVA_LIVE_TESTS, testName);
        String queueName = String.format("%s-Name", queueId);

        Map<String, RouterValue> queueLabels = new HashMap<>();
        queueLabels.put("Label_1", new RouterValue("Value_1"));

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

        List<QueueSelectorAttachment> queueSelectors = new ArrayList<>();
        queueSelectors.add(staticQueueSelector);
        queueSelectors.add(new StaticQueueSelectorAttachment(
            new RouterQueueSelector("Name", LabelOperator.NOT_EQUAL, new RouterValue(true))));

        StaticWorkerSelectorAttachment staticWorkerSelector = new StaticWorkerSelectorAttachment(
            new RouterWorkerSelector("key", LabelOperator.EQUAL, new RouterValue("value"))
                .setExpedite(true).setExpiresAfter(Duration.ofSeconds(10)));

        List<WorkerSelectorAttachment> workerSelectors = new ArrayList<>();
        workerSelectors.add(staticWorkerSelector);
        workerSelectors.add(new ConditionalWorkerSelectorAttachment(new StaticRouterRule().setValue(new RouterValue(true)),
            Collections.singletonList(new RouterWorkerSelector("Name", LabelOperator.NOT_EQUAL, new RouterValue(true)))));
        workerSelectors.add(new PassThroughWorkerSelectorAttachment("Key", LabelOperator.NOT_EQUAL));

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
        ClassificationPolicy policy = administrationAsyncClient.createClassificationPolicy(createClassificationPolicyOptions).block();
        RouterJob job = routerAsyncClient.createJobWithClassificationPolicy(
            new CreateJobWithClassificationPolicyOptions(jobId, channelId, classificationPolicyId)
                .setLabels(Collections.singletonMap("Key", new RouterValue("abc"))))
            .block();

        sleepIfRunningAgainstService(5000);

        // Verify
        assertEquals(classificationPolicyId, policy.getId());
        assertEquals(classificationPolicyName, policy.getName());
        assertEquals(StaticRouterRule.class, policy.getPrioritizationRule().getClass());
        assertNotNull(policy.getEtag());
        assertEquals(1, ((StaticRouterRule) policy.getPrioritizationRule()).getValue().getIntValue());
        assertEquals(3, policy.getWorkerSelectorAttachments().size());
        assertEquals(Duration.ofSeconds(10), ((StaticWorkerSelectorAttachment) policy.getWorkerSelectorAttachments().get(0)).getWorkerSelector().getExpiresAfter());
        assertEquals(2, policy.getQueueSelectorAttachments().size());
        assertEquals(fallbackQueueId, policy.getFallbackQueueId());

        Response<BinaryData> binaryData = administrationAsyncClient.getClassificationPolicyWithResponse(policy.getId(), null).block();
        ClassificationPolicy deserialized = binaryData.getValue().toObject(ClassificationPolicy.class);

        assertEquals(classificationPolicyId, deserialized.getId());
        assertEquals(classificationPolicyName, deserialized.getName());
        assertEquals(StaticRouterRule.class, deserialized.getPrioritizationRule().getClass());
        assertEquals(policy.getEtag(), deserialized.getEtag());
        assertEquals(1, ((StaticRouterRule) deserialized.getPrioritizationRule()).getValue().getIntValue());
        assertEquals(3, deserialized.getWorkerSelectorAttachments().size());
        assertEquals(Duration.ofSeconds(10), ((StaticWorkerSelectorAttachment) deserialized.getWorkerSelectorAttachments().get(0)).getWorkerSelector().getExpiresAfter());
        assertEquals(2, deserialized.getQueueSelectorAttachments().size());
        assertEquals(fallbackQueueId, deserialized.getFallbackQueueId());

        job = routerAsyncClient.getJob(job.getId()).block();

        assertEquals(jobId, job.getId());
        assertEquals(classificationPolicyId, job.getClassificationPolicyId());
        assertEquals(queueId, job.getQueueId());
        assertEquals(channelId, job.getChannelId());
        assertEquals(1, job.getPriority());
        assertEquals(3, job.getAttachedWorkerSelectors().size());

        deserialized.setPrioritizationRule(null);
        deserialized.setQueueSelectorAttachments(new ArrayList<>());
        ClassificationPolicy updatedPolicy = administrationAsyncClient.updateClassificationPolicy(
            deserialized.getId(), deserialized).block();

        assertEquals(classificationPolicyId, updatedPolicy.getId());
        assertEquals(classificationPolicyName, updatedPolicy.getName());
        assertEquals(StaticRouterRule.class, updatedPolicy.getPrioritizationRule().getClass());
        assertNotEquals(policy.getEtag(), updatedPolicy.getEtag());
        assertEquals(1, ((StaticRouterRule) updatedPolicy.getPrioritizationRule()).getValue().getIntValue());
        assertEquals(3, updatedPolicy.getWorkerSelectorAttachments().size());
        assertEquals(Duration.ofSeconds(10), ((StaticWorkerSelectorAttachment) updatedPolicy.getWorkerSelectorAttachments().get(0)).getWorkerSelector().getExpiresAfter());
        assertEquals(0, updatedPolicy.getQueueSelectorAttachments().size());
        assertEquals(fallbackQueueId, updatedPolicy.getFallbackQueueId());

        // Cleanup
        routerAsyncClient.cancelJob(job.getId()).block();
        routerAsyncClient.deleteJob(job.getId()).block();
        administrationAsyncClient.deleteClassificationPolicy(classificationPolicyId).block();
        administrationAsyncClient.deleteQueue(queueId).block();
        administrationAsyncClient.deleteQueue(fallbackQueueId).block();
        administrationAsyncClient.deleteDistributionPolicy(distributionPolicyId).block();
    }
}
