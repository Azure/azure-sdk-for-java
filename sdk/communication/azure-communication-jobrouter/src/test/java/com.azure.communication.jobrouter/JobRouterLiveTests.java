// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter;

import com.azure.communication.jobrouter.models.AzureFunctionRule;
import com.azure.communication.jobrouter.models.AzureFunctionRuleCredential;
import com.azure.communication.jobrouter.models.BestWorkerMode;
import com.azure.communication.jobrouter.models.CancelExceptionAction;
import com.azure.communication.jobrouter.models.ChannelConfiguration;
import com.azure.communication.jobrouter.models.ClassificationPolicy;
import com.azure.communication.jobrouter.models.DistributionPolicy;
import com.azure.communication.jobrouter.models.ExceptionAction;
import com.azure.communication.jobrouter.models.ExceptionPolicy;
import com.azure.communication.jobrouter.models.ExceptionRule;
import com.azure.communication.jobrouter.models.JobQueue;
import com.azure.communication.jobrouter.models.LabelOperator;
import com.azure.communication.jobrouter.models.LongestIdleMode;
import com.azure.communication.jobrouter.models.QueueLengthExceptionTrigger;
import com.azure.communication.jobrouter.models.QueueSelector;
import com.azure.communication.jobrouter.models.QueueSelectorAttachment;
import com.azure.communication.jobrouter.models.RoundRobinMode;
import com.azure.communication.jobrouter.models.RouterWorker;
import com.azure.communication.jobrouter.models.StaticQueueSelector;
import com.azure.communication.jobrouter.models.StaticRule;
import com.azure.communication.jobrouter.models.StaticWorkerSelector;
import com.azure.communication.jobrouter.models.WorkerSelector;
import com.azure.communication.jobrouter.models.WorkerSelectorAttachment;
import com.azure.core.http.rest.Response;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JobRouterLiveTests extends JobRouterClientTestBase {
    private RouterClient routerClient;

    @Override
    protected void beforeTest() {
        routerClient = clientSetup(httpPipeline -> new RouterClientBuilder()
            .connectionString(getConnectionString())
            .pipeline(httpPipeline)
            .buildClient());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createDistributionPolicy_BestWorker_DefaultScoringRule() {
        // Setup
        String bestWorkerModeDistributionPolicyId = String.format("%s-Best-DistributionPolicy", UUID.randomUUID());
        String bestWorkerModeDistributionPolicyName = String.format("%s-Name", bestWorkerModeDistributionPolicyId);
        DistributionPolicy distributionPolicy = new DistributionPolicy();
        BestWorkerMode bestWorkerMode = new BestWorkerMode();
        bestWorkerMode.setMinConcurrentOffers(1);
        bestWorkerMode.setMaxConcurrentOffers(10);

        distributionPolicy.setMode(bestWorkerMode);
        distributionPolicy.setName(bestWorkerModeDistributionPolicyName);
        distributionPolicy.setOfferTtlSeconds(10.0);

        // Action
        DistributionPolicy result = routerClient.upsertDistributionPolicy(bestWorkerModeDistributionPolicyId, distributionPolicy);

        // Verify
        assertEquals(bestWorkerModeDistributionPolicyId, result.getId());

        // Cleanup
        routerClient.deleteDistributionPolicy(bestWorkerModeDistributionPolicyId);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createDistributionPolicy_BestWorker_AzureFunctionRule() {
        // Setup
        String bestWorkerModeDistributionPolicyId = String.format("%s-Best-DistributionPolicy", UUID.randomUUID());
        String bestWorkerModeDistributionPolicyName = String.format("%s-Name", bestWorkerModeDistributionPolicyId);
        DistributionPolicy distributionPolicy = new DistributionPolicy();
        BestWorkerMode bestWorkerMode = new BestWorkerMode();


        AzureFunctionRule azureFunctionRule = new AzureFunctionRule();
        azureFunctionRule.setFunctionUrl("https://my.function.app/api/myfunction?code=Kg==");
        AzureFunctionRuleCredential azureFunctionRuleCredential = new AzureFunctionRuleCredential();
        azureFunctionRuleCredential.setAppKey("MyAppKey");
        azureFunctionRuleCredential.setClientId("MyClientId");
        azureFunctionRule.setCredential(azureFunctionRuleCredential);

        bestWorkerMode.setScoringRule(azureFunctionRule);
        bestWorkerMode.setMinConcurrentOffers(1);
        bestWorkerMode.setMaxConcurrentOffers(10);

        distributionPolicy.setMode(bestWorkerMode);
        distributionPolicy.setName(bestWorkerModeDistributionPolicyName);
        distributionPolicy.setOfferTtlSeconds(10.0);

        // Action
        DistributionPolicy result = routerClient.upsertDistributionPolicy(bestWorkerModeDistributionPolicyId, distributionPolicy);

        // Verify
        assertEquals(bestWorkerModeDistributionPolicyId, result.getId());

        // Cleanup
        routerClient.deleteDistributionPolicy(bestWorkerModeDistributionPolicyId);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void CreateDistributionPolicy_LongestIdle() {
        // Setup
        String longestIdleModeDistributionPolicyId = String.format("%s-Longest-DistributionPolicy", UUID.randomUUID());
        String longestIdleModeDistributionPolicyName = String.format("%s-Name", longestIdleModeDistributionPolicyId);
        DistributionPolicy distributionPolicy = new DistributionPolicy();
        LongestIdleMode longestIdleMode = new LongestIdleMode();

        longestIdleMode.setMinConcurrentOffers(1);
        longestIdleMode.setMaxConcurrentOffers(10);

        distributionPolicy.setMode(longestIdleMode);
        distributionPolicy.setName(longestIdleModeDistributionPolicyName);
        distributionPolicy.setOfferTtlSeconds(10.0);

        // Action
        DistributionPolicy result = routerClient.upsertDistributionPolicy(longestIdleModeDistributionPolicyId, distributionPolicy);

        // Verify
        assertEquals(longestIdleModeDistributionPolicyId, result.getId());

        // Cleanup
        routerClient.deleteDistributionPolicy(longestIdleModeDistributionPolicyId);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void CreateDistributionPolicy_RoundRobin() {
        // Setup
        String roundRobinModeDistributionPolicyId = String.format("%s-RoundRobin-DistributionPolicy", UUID.randomUUID());
        String roundRobinModeDistributionPolicyName = String.format("%s-Name", roundRobinModeDistributionPolicyId);
        DistributionPolicy distributionPolicy = new DistributionPolicy();
        RoundRobinMode roundRobinMode = new RoundRobinMode();

        roundRobinMode.setMinConcurrentOffers(1);
        roundRobinMode.setMaxConcurrentOffers(10);

        distributionPolicy.setMode(roundRobinMode);
        distributionPolicy.setName(roundRobinModeDistributionPolicyName);
        distributionPolicy.setOfferTtlSeconds(10.0);

        // Action
        DistributionPolicy result = routerClient.upsertDistributionPolicy(roundRobinModeDistributionPolicyId, distributionPolicy);

        // Verify
        assertEquals(roundRobinModeDistributionPolicyId, result.getId());

        // Cleanup
        routerClient.deleteDistributionPolicy(roundRobinModeDistributionPolicyId);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void CreateClassificationPolicy() {
        // Setup
        String distributionPolicyId = String.format("%s-DistributionPolicy", UUID.randomUUID());
        DistributionPolicy distributionPolicy = createDistributionPolicy(distributionPolicyId);

        String queueId = String.format("%s-Queue", UUID.randomUUID());
        JobQueue jobQueue = createQueue(queueId, distributionPolicy.getId());

        String classificationPolicyId = String.format("%s-ClassificationPolicy", UUID.randomUUID());
        String classificationPolicyName = String.format("%s-Name", classificationPolicyId);

        /**
         * Create queue selectors.
         */
        QueueSelector queueSelector = new QueueSelector();
        queueSelector.setKey("queueId");
        queueSelector.setLabelOperator(LabelOperator.EQUAL);
        queueSelector.setValue(queueId);

        StaticQueueSelector staticQueueSelector = new StaticQueueSelector();
        staticQueueSelector.setLabelSelector(queueSelector);

        List<QueueSelectorAttachment> queueSelectors = new ArrayList<QueueSelectorAttachment>() {{
            add(staticQueueSelector);
        }};


        /**
         * Create worker selectors.
         */
        WorkerSelector workerSelector = new WorkerSelector();
        workerSelector.setKey("key");
        workerSelector.setLabelOperator(LabelOperator.EQUAL);
        workerSelector.setValue("value");

        StaticWorkerSelector staticWorkerSelector = new StaticWorkerSelector();
        staticWorkerSelector.setLabelSelector(workerSelector);

        List<WorkerSelectorAttachment> workerSelectors = new ArrayList<WorkerSelectorAttachment>() {{
            add(staticWorkerSelector);
        }};

        StaticRule prioritizationRule = new StaticRule();
        prioritizationRule.setValue(1);

        ClassificationPolicy classificationPolicy = new ClassificationPolicy();
        classificationPolicy.setName(classificationPolicyName);
        classificationPolicy.setPrioritizationRule(prioritizationRule);
        classificationPolicy.setFallbackQueueId(jobQueue.getId());
        classificationPolicy.setQueueSelectors(queueSelectors);
        classificationPolicy.setWorkerSelectors(workerSelectors);

        // Action
        ClassificationPolicy result = routerClient.upsertClassificationPolicy(classificationPolicyId, classificationPolicy);

        // Verify
        assertEquals(classificationPolicyId, result.getId());

        // Cleanup
        routerClient.deleteClassificationPolicy(classificationPolicyId);
        routerClient.deleteQueue(queueId);
        routerClient.deleteDistributionPolicy(distributionPolicyId);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void CreateExceptionPolicy() {
        // Setup
        String exceptionPolicyId = String.format("%s-ExceptionPolicy", UUID.randomUUID());
        String exceptionPolicyName = String.format("%s-Name", exceptionPolicyId);

        QueueLengthExceptionTrigger queueLengthExceptionTrigger = new QueueLengthExceptionTrigger();
        queueLengthExceptionTrigger.setThreshold(1);

        CancelExceptionAction exceptionAction = new CancelExceptionAction();
        exceptionAction.setDispositionCode("CancelledDueToMaxQueueLengthReached");
        exceptionAction.setNote("Job Cancelled as maximum queue length is reached.");

        Map<String, ExceptionAction> exceptionActions = new HashMap<String, ExceptionAction>() {{
            put("CancelledDueToMaxQueueLengthReached", exceptionAction);
        }};

        ExceptionRule exceptionRule = new ExceptionRule();
        exceptionRule.setTrigger(queueLengthExceptionTrigger);
        exceptionRule.setActions(exceptionActions);

        Map<String, ExceptionRule> exceptionRules = new HashMap<String, ExceptionRule>() {{
            put(exceptionPolicyId, exceptionRule);
        }};

        ExceptionPolicy exceptionPolicy = new ExceptionPolicy();
        exceptionPolicy.setName(exceptionPolicyName);
        exceptionPolicy.setExceptionRules(exceptionRules);

        // Action
        ExceptionPolicy result = routerClient.upsertExceptionPolicy(exceptionPolicyId, exceptionPolicy);

        // Verify
        assertEquals(exceptionPolicyId, result.getId());

        // Cleanup
        routerClient.deleteExceptionPolicy(exceptionPolicyId);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void CreateWorker() {
        // Setup
        /**
         * Setup queue
         */
        String distributionPolicyId = String.format("%s-DistributionPolicy", UUID.randomUUID());
        DistributionPolicy distributionPolicy = createDistributionPolicy(distributionPolicyId);

        String queueId = String.format("%s-Queue", UUID.randomUUID());
        JobQueue jobQueue = createQueue(queueId, distributionPolicy.getId());

        /**
         * Setup worker
         */
        String workerId = String.format("%s-Worker", UUID.randomUUID());

        Map<String, Object> labels = new HashMap<String, Object>() {{
            put("Label", "Value");
        }};

        Map<String, Object> tags = new HashMap<String, Object>() {{
            put("Tag", "Value");
        }};

        ChannelConfiguration channelConfiguration = new ChannelConfiguration();
        channelConfiguration.setCapacityCostPerJob(1);
        Map<String, ChannelConfiguration> channelConfigurations = new HashMap<String, ChannelConfiguration>() {{
            put("channel1", channelConfiguration);
        }};

        Map<String, Object> queueAssignments = new HashMap<String, Object>() {{
            put(jobQueue.getId(), new Object());
        }};

        RouterWorker routerWorker = new RouterWorker();
        routerWorker.setAvailableForOffers(false);
        routerWorker.setLabels(labels);
        routerWorker.setTags(tags);
        routerWorker.setTotalCapacity(10);
        routerWorker.setChannelConfigurations(channelConfigurations);
        routerWorker.setQueueAssignments(queueAssignments);

        // Action
        RouterWorker result = routerClient.upsertWorker(workerId, routerWorker);

        // Verify
        assertEquals(workerId, result.getId());

        // Cleanup
        routerClient.deleteWorker(workerId);
        routerClient.deleteQueue(queueId);
        routerClient.deleteDistributionPolicy(distributionPolicyId);
    }

    private DistributionPolicy createDistributionPolicy(String id) {
        String distributionPolicyName = String.format("%s-Name", id);

        LongestIdleMode longestIdleMode = new LongestIdleMode();

        longestIdleMode.setMinConcurrentOffers(1);
        longestIdleMode.setMaxConcurrentOffers(10);

        DistributionPolicy distributionPolicy = new DistributionPolicy();
        distributionPolicy.setMode(longestIdleMode);
        distributionPolicy.setName(distributionPolicyName);
        distributionPolicy.setOfferTtlSeconds(10.0);

        return routerClient.upsertDistributionPolicy(id, distributionPolicy);
    }

    private JobQueue createQueue(String queueId, String distributionPolicyId) {
        String queueName = String.format("%s-Name", queueId);
        Map<String, Object> queueLabels = new HashMap<String, Object>() {{ put("Label_1", "Value_1"); }};

        JobQueue jobQueue = new JobQueue();
        jobQueue.setDistributionPolicyId(distributionPolicyId);
        jobQueue.setLabels(queueLabels);
        jobQueue.setName(queueName);

        return routerClient.upsertQueue(queueId, jobQueue);
    }
}
