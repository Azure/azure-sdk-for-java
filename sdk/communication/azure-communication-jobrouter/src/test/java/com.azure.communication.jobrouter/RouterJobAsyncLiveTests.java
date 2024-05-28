// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter;

import com.azure.communication.jobrouter.models.AcceptJobOfferResult;
import com.azure.communication.jobrouter.models.CancelJobOptions;
import com.azure.communication.jobrouter.models.CreateDistributionPolicyOptions;
import com.azure.communication.jobrouter.models.CreateJobOptions;
import com.azure.communication.jobrouter.models.CreateQueueOptions;
import com.azure.communication.jobrouter.models.CreateWorkerOptions;
import com.azure.communication.jobrouter.models.DistributionPolicy;
import com.azure.communication.jobrouter.models.LabelOperator;
import com.azure.communication.jobrouter.models.LongestIdleMode;
import com.azure.communication.jobrouter.models.RouterChannel;
import com.azure.communication.jobrouter.models.RouterJob;
import com.azure.communication.jobrouter.models.RouterJobOffer;
import com.azure.communication.jobrouter.models.RouterQueue;
import com.azure.communication.jobrouter.models.RouterValue;
import com.azure.communication.jobrouter.models.RouterWorker;
import com.azure.communication.jobrouter.models.RouterWorkerSelector;
import com.azure.communication.jobrouter.models.UnassignJobResult;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RouterJobAsyncLiveTests extends JobRouterTestBase {
    private JobRouterAsyncClient jobRouterAsyncClient;

    private JobRouterAdministrationAsyncClient administrationAsyncClient;

//    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void unassignJob(HttpClient httpClient) {
        // Setup
        jobRouterAsyncClient = getRouterAsyncClient(httpClient);
        administrationAsyncClient = getRouterAdministrationAsyncClient(httpClient);
        String testName = "unassign-job-test";
        /**
         * Setup queue
         */
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

        /**
         * Setup worker
         */
        Map<String, RouterValue> labels = new HashMap<String, RouterValue>() {
            {
                put("Label", new RouterValue("Value"));
            }
        };

        Map<String, RouterValue> tags = new HashMap<String, RouterValue>() {
            {
                put("Tag", new RouterValue("Value"));
            }
        };

        RouterChannel channel = new RouterChannel("router-channel", 1);
        List<RouterChannel> channels = new ArrayList<RouterChannel>() {
            {
                add(channel);
            }
        };

        List<String> queues = new ArrayList<String>() {
            {
                add(jobQueue.getId());
            }
        };

        String workerId = String.format("%s-%s-Worker", JAVA_LIVE_TESTS, testName);
        CreateWorkerOptions createWorkerOptions = new CreateWorkerOptions(workerId, 10)
            .setLabels(labels)
            .setTags(tags)
            .setAvailableForOffers(true)
            .setChannels(channels)
            .setQueues(queues);

        RouterWorker routerWorker = jobRouterAsyncClient.createWorker(createWorkerOptions).block();

        String jobId = String.format("%s-%s-Job", JAVA_LIVE_TESTS, testName);
        List<RouterWorkerSelector> requestedWorkerSelectors = new ArrayList<RouterWorkerSelector>() {
            {
                add(new RouterWorkerSelector("Label", LabelOperator.EQUAL)
                    .setValue(new RouterValue("Value")));
            }
        };
        CreateJobOptions createJobOptions = new CreateJobOptions(jobId, channel.getChannelId(), queueId)
            .setLabels(labels)
            .setTags(tags)
            .setRequestedWorkerSelectors(requestedWorkerSelectors);

        RouterJob routerJob = jobRouterAsyncClient.createJob(createJobOptions).block();

        List<RouterJobOffer> jobOffers = new ArrayList<RouterJobOffer>();
        long startTimeMillis = System.currentTimeMillis();
        while (true) {
            RouterWorker worker = jobRouterAsyncClient.getWorker(workerId).block();
            jobOffers = worker.getOffers();
            if (jobOffers.size() > 0 || System.currentTimeMillis() - startTimeMillis > 10000) {
                break;
            }
        }

        assertTrue(jobOffers.size() == 1);

        RouterJobOffer offer = jobOffers.get(0);

        AcceptJobOfferResult acceptJobOfferResult = jobRouterAsyncClient.acceptJobOffer(workerId, offer.getOfferId()).block();

        String assignmentId = acceptJobOfferResult.getAssignmentId();

        // Action
        UnassignJobResult unassignJobResult = jobRouterAsyncClient.unassignJob(jobId, assignmentId).block();

        // Verify
        assertEquals(1, unassignJobResult.getUnassignmentCount());

        RequestOptions requestOptions = new RequestOptions();
        CancelJobOptions cancelJobOptions = new CancelJobOptions()
            .setDispositionCode("dispositionCode")
            .setNote("note");
        requestOptions.setBody(BinaryData.fromObject(cancelJobOptions));
        // Cleanup
        jobRouterAsyncClient.cancelJob(jobId, requestOptions).block();
        jobRouterAsyncClient.deleteJob(jobId).block();
        jobRouterAsyncClient.deleteWorker(workerId).block();
        administrationAsyncClient.deleteQueue(queueId).block();
        administrationAsyncClient.deleteDistributionPolicy(distributionPolicyId).block();
    }
}
