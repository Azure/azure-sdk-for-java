// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter;

import com.azure.communication.jobrouter.models.AcceptJobOfferResult;
import com.azure.communication.jobrouter.models.CancelJobOptions;
import com.azure.communication.jobrouter.models.ChannelConfiguration;
import com.azure.communication.jobrouter.models.CreateJobOptions;
import com.azure.communication.jobrouter.models.CreateWorkerOptions;
import com.azure.communication.jobrouter.models.DistributionPolicy;
import com.azure.communication.jobrouter.models.LabelValue;
import com.azure.communication.jobrouter.models.QueueAndMatchMode;
import com.azure.communication.jobrouter.models.RouterJob;
import com.azure.communication.jobrouter.models.RouterJobMatchingMode;
import com.azure.communication.jobrouter.models.RouterJobOffer;
import com.azure.communication.jobrouter.models.RouterJobStatus;
import com.azure.communication.jobrouter.models.RouterQueue;
import com.azure.communication.jobrouter.models.RouterQueueAssignment;
import com.azure.communication.jobrouter.models.RouterWorker;
import com.azure.communication.jobrouter.models.ScheduleAndSuspendMode;
import com.azure.communication.jobrouter.models.UnassignJobOptions;
import com.azure.communication.jobrouter.models.UnassignJobResult;
import com.azure.communication.jobrouter.models.UpdateJobOptions;
import com.azure.core.http.HttpClient;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RouterJobLiveTests extends JobRouterTestBase {
    private JobRouterClient jobRouterClient;

    private JobRouterAdministrationClient routerAdminClient;

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void unassignJob(HttpClient httpClient) {
        // Setup
        jobRouterClient = getRouterClient(httpClient);
        routerAdminClient = getRouterAdministrationClient(httpClient);
        String testName = "unassign-job-2";
        /**
         * Setup queue
         */
        String distributionPolicyId = String.format("%s-%s-DistributionPolicy", JAVA_LIVE_TESTS, testName);
        DistributionPolicy distributionPolicy = createDistributionPolicy(routerAdminClient, distributionPolicyId);

        String queueId = String.format("%s-%s-Queue", JAVA_LIVE_TESTS, testName);
        RouterQueue jobQueue = createQueue(routerAdminClient, queueId, distributionPolicy.getId());

        /**
         * Setup worker
         */
        Map<String, LabelValue> labels = new HashMap<String, LabelValue>() {
            {
                put("Label", new LabelValue("Value"));
            }
        };

        Map<String, LabelValue> tags = new HashMap<String, LabelValue>() {
            {
                put("Tag", new LabelValue("Value"));
            }
        };

        ChannelConfiguration channelConfiguration = new ChannelConfiguration(1);
        Map<String, ChannelConfiguration> channelConfigurations = new HashMap<String, ChannelConfiguration>() {
            {
                put("channel1", channelConfiguration);
            }
        };

        Map<String, RouterQueueAssignment> queueAssignments = new HashMap<String, RouterQueueAssignment>() {
            {
                put(jobQueue.getId(), new RouterQueueAssignment());
            }
        };

        String workerId = String.format("%s-%s-Worker", JAVA_LIVE_TESTS, testName);
        CreateWorkerOptions createWorkerOptions = new CreateWorkerOptions(workerId, 10)
            .setLabels(labels)
            .setTags(tags)
            .setAvailableForOffers(true)
            .setChannelConfigurations(channelConfigurations)
            .setQueueAssignments(queueAssignments);

        RouterWorker worker = jobRouterClient.createWorker(createWorkerOptions);

        String jobId = String.format("%s-%s-Job", JAVA_LIVE_TESTS, testName);
        CreateJobOptions createJobOptions = new CreateJobOptions(jobId, "channel1", queueId);

        RouterJob job = jobRouterClient.createJob(createJobOptions);

        List<RouterJobOffer> jobOffers = new ArrayList<>();
        long startTimeMillis = System.currentTimeMillis();
        while (true) {
            worker = jobRouterClient.getWorker(workerId);
            jobOffers = worker.getOffers();
            if (jobOffers.size() > 0 || System.currentTimeMillis() - startTimeMillis > 10000) {
                break;
            }
        }

        assertTrue(jobOffers.size() == 1);

        RouterJobOffer offer = jobOffers.get(0);

        AcceptJobOfferResult acceptJobOfferResult = jobRouterClient.acceptJobOffer(workerId, offer.getOfferId());

        String assignmentId = acceptJobOfferResult.getAssignmentId();

        // Action
        UnassignJobOptions unassignJobOptions = new UnassignJobOptions(jobId, assignmentId);
        UnassignJobResult unassignJobResult = jobRouterClient.unassignJob(unassignJobOptions);

        // Verify
        assertEquals(1, unassignJobResult.getUnassignmentCount());

        // Cleanup
        CancelJobOptions cancelJobOptions = new CancelJobOptions(jobId)
            .setNote("Done.")
            .setDispositionCode("test");
        jobRouterClient.cancelJob(cancelJobOptions);
        jobRouterClient.deleteJob(jobId);
        jobRouterClient.deleteWorker(workerId);
        routerAdminClient.deleteQueue(queueId);
        routerAdminClient.deleteDistributionPolicy(distributionPolicyId);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void jobScheduling(HttpClient httpClient) {
        // Setup
        jobRouterClient = getRouterClient(httpClient);
        routerAdminClient = getRouterAdministrationClient(httpClient);

        String testName = "schedule-job-1";

        String distributionPolicyId = String.format("%s-%s-DistributionPolicy", JAVA_LIVE_TESTS, testName);
        DistributionPolicy distributionPolicy = createDistributionPolicy(routerAdminClient, distributionPolicyId);

        String queueId = String.format("%s-%s-Queue", JAVA_LIVE_TESTS, testName);
        RouterQueue queue = createQueue(routerAdminClient, queueId, distributionPolicy.getId());

        String jobId = String.format("%s-%s-Job", JAVA_LIVE_TESTS, testName);

        RouterJob job = jobRouterClient.createJob(new CreateJobOptions(jobId, testName, queue.getId())
            .setMatchingMode(new RouterJobMatchingMode(new ScheduleAndSuspendMode(
                OffsetDateTime.of(2040, 1, 1, 1, 1, 1, 1, ZoneOffset.UTC)))));

        // Action
        RouterJob job2 = jobRouterClient.updateJob(new UpdateJobOptions(jobId)
            .setMatchingMode(new RouterJobMatchingMode(new QueueAndMatchMode())));

        // Verify
        assertEquals(job.getStatus(), RouterJobStatus.PENDING_SCHEDULE);
        assertEquals(job2.getStatus(), RouterJobStatus.QUEUED);

        // Cleanup
        jobRouterClient.cancelJob(new CancelJobOptions(jobId));
        jobRouterClient.deleteJob(jobId);
        routerAdminClient.deleteQueue(queueId);
        routerAdminClient.deleteDistributionPolicy(distributionPolicyId);
    }
}
