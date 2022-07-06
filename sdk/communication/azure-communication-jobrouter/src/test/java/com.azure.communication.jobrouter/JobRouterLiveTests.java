// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter;

import com.azure.communication.jobrouter.models.AzureFunctionRule;
import com.azure.communication.jobrouter.models.AzureFunctionRuleCredential;
import com.azure.communication.jobrouter.models.BestWorkerMode;
import com.azure.communication.jobrouter.implementation.models.CancelExceptionAction;
import com.azure.communication.jobrouter.models.ChannelConfiguration;
import com.azure.communication.jobrouter.models.ClassificationPolicy;
import com.azure.communication.jobrouter.models.CreateDistributionPolicyOptions;
import com.azure.communication.jobrouter.models.CreateJobOptions;
import com.azure.communication.jobrouter.models.CreateQueueOptions;
import com.azure.communication.jobrouter.models.CreateWorkerOptions;
import com.azure.communication.jobrouter.models.DistributionPolicy;
import com.azure.communication.jobrouter.implementation.models.ExceptionAction;
import com.azure.communication.jobrouter.models.ExceptionPolicy;
import com.azure.communication.jobrouter.models.ExceptionRule;
import com.azure.communication.jobrouter.models.JobQueue;
import com.azure.communication.jobrouter.implementation.models.LabelOperator;
import com.azure.communication.jobrouter.models.LongestIdleMode;
import com.azure.communication.jobrouter.implementation.models.QueueLengthExceptionTrigger;
import com.azure.communication.jobrouter.implementation.models.QueueSelector;
import com.azure.communication.jobrouter.implementation.models.QueueSelectorAttachment;
import com.azure.communication.jobrouter.models.RoundRobinMode;
import com.azure.communication.jobrouter.models.LabelValue;
import com.azure.communication.jobrouter.models.QueueAssignment;
import com.azure.communication.jobrouter.models.RouterJob;
import com.azure.communication.jobrouter.models.RouterWorker;
import com.azure.communication.jobrouter.implementation.models.StaticQueueSelector;
import com.azure.communication.jobrouter.models.StaticRule;
import com.azure.communication.jobrouter.models.StaticWorkerSelector;
import com.azure.communication.jobrouter.models.WorkerSelector;
import com.azure.communication.jobrouter.models.WorkerSelectorAttachment;
import com.azure.communication.jobrouter.models.CreateClassificationPolicyOptions;
import com.azure.communication.jobrouter.models.CreateExceptionPolicyOptions;
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

        CreateDistributionPolicyOptions createDistributionPolicyOptions = new CreateDistributionPolicyOptions(
            bestWorkerModeDistributionPolicyId,
            10.0,
            new BestWorkerMode()
                .setMinConcurrentOffers(1)
                .setMaxConcurrentOffers(10)
        )
            .setName(bestWorkerModeDistributionPolicyName);

        // Action
        DistributionPolicy result = routerClient.createDistributionPolicy(createDistributionPolicyOptions);

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

        CreateDistributionPolicyOptions createDistributionPolicyOptions = new CreateDistributionPolicyOptions(
            bestWorkerModeDistributionPolicyId,
            10.0,
            new BestWorkerMode()
                .setScoringRule(azureFunctionRule)
                .setMinConcurrentOffers(1)
                .setMaxConcurrentOffers(10)
        )
            .setName(bestWorkerModeDistributionPolicyName);

        // Action
        DistributionPolicy result = routerClient.createDistributionPolicy(createDistributionPolicyOptions);

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

        CreateDistributionPolicyOptions createDistributionPolicyOptions = new CreateDistributionPolicyOptions(
            longestIdleModeDistributionPolicyId,
            10.0,
            new LongestIdleMode()
                .setMinConcurrentOffers(1)
                .setMaxConcurrentOffers(10)
        )
            .setName(longestIdleModeDistributionPolicyName);

        // Action
        DistributionPolicy result = routerClient.createDistributionPolicy(createDistributionPolicyOptions);

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

        CreateDistributionPolicyOptions createDistributionPolicyOptions = new CreateDistributionPolicyOptions(
            roundRobinModeDistributionPolicyId,
            10.0,
            new RoundRobinMode()
                .setMinConcurrentOffers(1)
                .setMaxConcurrentOffers(10)
        )
            .setName(roundRobinModeDistributionPolicyName);

        // Action
        DistributionPolicy result = routerClient.createDistributionPolicy(createDistributionPolicyOptions);

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
        CreateClassificationPolicyOptions createClassificationPolicyOptions = new CreateClassificationPolicyOptions(
            classificationPolicyId,
            classificationPolicyName,
            new StaticRule().setValue(1),
            workerSelectors,
            queueSelectors,
            jobQueue.getId()
        );

        // Action
        ClassificationPolicy result = routerClient.createClassificationPolicy(createClassificationPolicyOptions);

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

        CreateExceptionPolicyOptions createExceptionPolicyOptions = new CreateExceptionPolicyOptions(
            exceptionPolicyId, exceptionRules)
            .setName(exceptionPolicyName);

        // Action
        ExceptionPolicy result = routerClient.createExceptionPolicy(createExceptionPolicyOptions);

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
                put(jobQueue.getId(), new QueueAssignment(new Object()));
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
        routerClient.deleteQueue(queueId);
        routerClient.deleteDistributionPolicy(distributionPolicyId);
    }

    private DistributionPolicy createDistributionPolicy(String id) {
        String distributionPolicyName = String.format("%s-Name", id);

        CreateDistributionPolicyOptions createDistributionPolicyOptions = new CreateDistributionPolicyOptions(
            id,
            10.0,
            new LongestIdleMode()
                .setMinConcurrentOffers(1)
                .setMaxConcurrentOffers(10)
        )
            .setName(distributionPolicyName);

        return routerClient.createDistributionPolicy(createDistributionPolicyOptions);
    }

    private JobQueue createQueue(String queueId, String distributionPolicyId) {
        String queueName = String.format("%s-Name", queueId);
        Map<String, LabelValue> queueLabels = new HashMap<String, LabelValue>() {
            {
                put("Label_1", new LabelValue("Value_1"));
            }
        };

        CreateQueueOptions createQueueOptions = new CreateQueueOptions(queueId, distributionPolicyId)
            .setLabels(queueLabels)
            .setName(queueName);

        return routerClient.createQueue(createQueueOptions);
    }

    private RouterJob createJob(String queueId) {
        CreateJobOptions createJobOptions = new CreateJobOptions("job-id", "chat-channel", queueId)
            .setPriority(1)
            .setChannelReference("12345")
            .setRequestedWorkerSelectors(
                new ArrayList<WorkerSelector>() {
                    {
                        new WorkerSelector()
                            .setKey("Some-skill")
                            .setLabelOperator(LabelOperator.GREATER_THAN)
                            .setValue(10);
                    }
                }
            );
        return routerClient.createJob(createJobOptions);
    }
}
