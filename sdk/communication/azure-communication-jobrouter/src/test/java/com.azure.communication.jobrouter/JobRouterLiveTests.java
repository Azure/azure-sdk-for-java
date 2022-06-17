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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JobRouterLiveTests extends JobRouterClientTestBase {
    private static final String JAVA_LIVE_TEST = "JAVA_LIVE_TEST";

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
    public void createDistributionPolicyBestWorkerDefaultScoringRule() {
        // Setup
        String bestWorkerModeDistributionPolicyId = String.format("%s-Best-DistributionPolicy", JAVA_LIVE_TEST);
        String bestWorkerModeDistributionPolicyName = String.format("%s-Name", bestWorkerModeDistributionPolicyId);
        DistributionPolicy distributionPolicy = new DistributionPolicy()
            .setMode(new BestWorkerMode()
                .setMinConcurrentOffers(1)
                .setMaxConcurrentOffers(10))
            .setName(bestWorkerModeDistributionPolicyName)
            .setOfferTtlSeconds(10.0);

        // Action
        DistributionPolicy result = routerClient.createDistributionPolicy(bestWorkerModeDistributionPolicyId, distributionPolicy);

        // Verify
        assertEquals(bestWorkerModeDistributionPolicyId, result.getId());

        // Cleanup
        routerClient.deleteDistributionPolicy(bestWorkerModeDistributionPolicyId);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createDistributionPolicyBestWorkerAzureFunctionRule() {
        // Setup
        String bestWorkerModeDistributionPolicyId = String.format("%s-Best-DistributionPolicy", JAVA_LIVE_TEST);
        String bestWorkerModeDistributionPolicyName = String.format("%s-Name", bestWorkerModeDistributionPolicyId);

        AzureFunctionRule azureFunctionRule = new AzureFunctionRule()
            .setFunctionUrl("https://my.function.app/api/myfunction?code=Kg==")
            .setCredential(new AzureFunctionRuleCredential()
                .setAppKey("MyAppKey")
                .setClientId("MyClientId"));

        DistributionPolicy distributionPolicy = new DistributionPolicy()
            .setMode(new BestWorkerMode()
                .setScoringRule(azureFunctionRule)
                .setMinConcurrentOffers(1)
                .setMaxConcurrentOffers(10))
            .setName(bestWorkerModeDistributionPolicyName)
            .setOfferTtlSeconds(10.0);

        // Action
        DistributionPolicy result = routerClient.createDistributionPolicy(bestWorkerModeDistributionPolicyId, distributionPolicy);

        // Verify
        assertEquals(bestWorkerModeDistributionPolicyId, result.getId());

        // Cleanup
        routerClient.deleteDistributionPolicy(bestWorkerModeDistributionPolicyId);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createDistributionPolicyLongestIdle() {
        // Setup
        String longestIdleModeDistributionPolicyId = String.format("%s-Longest-DistributionPolicy", JAVA_LIVE_TEST);
        String longestIdleModeDistributionPolicyName = String.format("%s-Name", longestIdleModeDistributionPolicyId);

        DistributionPolicy distributionPolicy = new DistributionPolicy()
            .setMode(new LongestIdleMode()
                .setMinConcurrentOffers(1)
                .setMaxConcurrentOffers(10))
            .setName(longestIdleModeDistributionPolicyName)
            .setOfferTtlSeconds(10.0);

        // Action
        DistributionPolicy result = routerClient.createDistributionPolicy(longestIdleModeDistributionPolicyId, distributionPolicy);

        // Verify
        assertEquals(longestIdleModeDistributionPolicyId, result.getId());

        // Cleanup
        routerClient.deleteDistributionPolicy(longestIdleModeDistributionPolicyId);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createDistributionPolicyRoundRobin() {
        // Setup
        String roundRobinModeDistributionPolicyId = String.format("%s-RoundRobin-DistributionPolicy", JAVA_LIVE_TEST);
        String roundRobinModeDistributionPolicyName = String.format("%s-Name", roundRobinModeDistributionPolicyId);

        DistributionPolicy distributionPolicy = new DistributionPolicy()
            .setMode(new RoundRobinMode()
                .setMinConcurrentOffers(1)
                .setMaxConcurrentOffers(10))
            .setName(roundRobinModeDistributionPolicyName)
            .setOfferTtlSeconds(10.0);

        // Action
        DistributionPolicy result = routerClient.createDistributionPolicy(roundRobinModeDistributionPolicyId, distributionPolicy);

        // Verify
        assertEquals(roundRobinModeDistributionPolicyId, result.getId());

        // Cleanup
        routerClient.deleteDistributionPolicy(roundRobinModeDistributionPolicyId);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createClassificationPolicy() {
        // Setup
        String distributionPolicyId = String.format("%s-DistributionPolicy", JAVA_LIVE_TEST);
        DistributionPolicy distributionPolicy = createDistributionPolicy(distributionPolicyId);

        String queueId = String.format("%s-Queue", JAVA_LIVE_TEST);
        JobQueue jobQueue = createQueue(queueId, distributionPolicy.getId());

        String classificationPolicyId = String.format("%s-ClassificationPolicy", JAVA_LIVE_TEST);
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
        ClassificationPolicy classificationPolicy = new ClassificationPolicy()
            .setName(classificationPolicyName)
            .setPrioritizationRule(new StaticRule()
                .setValue(1))
            .setFallbackQueueId(jobQueue.getId())
            .setQueueSelectors(queueSelectors)
            .setWorkerSelectors(workerSelectors);

        // Action
        ClassificationPolicy result = routerClient.createClassificationPolicy(classificationPolicyId, classificationPolicy);

        // Verify
        assertEquals(classificationPolicyId, result.getId());

        // Cleanup
        routerClient.deleteClassificationPolicy(classificationPolicyId);
        routerClient.deleteQueue(queueId);
        routerClient.deleteDistributionPolicy(distributionPolicyId);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createExceptionPolicy() {
        // Setup
        String exceptionPolicyId = String.format("%s-ExceptionPolicy", JAVA_LIVE_TEST);
        String exceptionPolicyName = String.format("%s-Name", exceptionPolicyId);

        CancelExceptionAction exceptionAction = new CancelExceptionAction()
            .setDispositionCode("CancelledDueToMaxQueueLengthReached")
            .setNote("Job Cancelled as maximum queue length is reached.");

        Map<String, ExceptionAction> exceptionActions = new HashMap<String, ExceptionAction>() {
            {
                put("CancelledDueToMaxQueueLengthReached", exceptionAction);
            }
        };

        ExceptionRule exceptionRule = new ExceptionRule()
            .setTrigger(new QueueLengthExceptionTrigger()
                .setThreshold(1))
            .setActions(exceptionActions);

        Map<String, ExceptionRule> exceptionRules = new HashMap<String, ExceptionRule>() {
            {
                put(exceptionPolicyId, exceptionRule);
            }
        };

        ExceptionPolicy exceptionPolicy = new ExceptionPolicy()
            .setName(exceptionPolicyName)
            .setExceptionRules(exceptionRules);

        // Action
        ExceptionPolicy result = routerClient.createExceptionPolicy(exceptionPolicyId, exceptionPolicy);

        // Verify
        assertEquals(exceptionPolicyId, result.getId());

        // Cleanup
        routerClient.deleteExceptionPolicy(exceptionPolicyId);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createWorker() {
        // Setup
        /**
         * Setup queue
         */
        String distributionPolicyId = String.format("%s-DistributionPolicy", JAVA_LIVE_TEST);
        DistributionPolicy distributionPolicy = createDistributionPolicy(distributionPolicyId);

        String queueId = String.format("%s-Queue", JAVA_LIVE_TEST);
        JobQueue jobQueue = createQueue(queueId, distributionPolicy.getId());

        /**
         * Setup worker
         */
        String workerId = String.format("%s-Worker", JAVA_LIVE_TEST);

        Map<String, Object> labels = new HashMap<String, Object>() {
            {
                put("Label", "Value");
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

        Map<String, Object> queueAssignments = new HashMap<String, Object>() {
            {
                put(jobQueue.getId(), new Object());
            }
        };

        RouterWorker routerWorker = new RouterWorker()
            .setAvailableForOffers(false)
            .setLabels(labels)
            .setTags(tags)
            .setTotalCapacity(10)
            .setChannelConfigurations(channelConfigurations)
            .setQueueAssignments(queueAssignments);

        // Action
        RouterWorker result = routerClient.createWorker(workerId, routerWorker);

        // Verify
        assertEquals(workerId, result.getId());

        // Cleanup
        routerClient.deleteWorker(workerId);
        routerClient.deleteQueue(queueId);
        routerClient.deleteDistributionPolicy(distributionPolicyId);
    }

    private DistributionPolicy createDistributionPolicy(String id) {
        String distributionPolicyName = String.format("%s-Name", id);

        DistributionPolicy distributionPolicy = new DistributionPolicy()
            .setMode(new LongestIdleMode()
                .setMinConcurrentOffers(1)
                .setMaxConcurrentOffers(10))
            .setName(distributionPolicyName)
            .setOfferTtlSeconds(10.0);

        return routerClient.createDistributionPolicy(id, distributionPolicy);
    }

    private JobQueue createQueue(String queueId, String distributionPolicyId) {
        String queueName = String.format("%s-Name", queueId);
        Map<String, Object> queueLabels = new HashMap<String, Object>() {
            {
                put("Label_1", "Value_1");
            }
        };

        JobQueue jobQueue = new JobQueue()
            .setDistributionPolicyId(distributionPolicyId)
            .setLabels(queueLabels)
            .setName(queueName);

        return routerClient.createQueue(queueId, jobQueue);
    }
}
