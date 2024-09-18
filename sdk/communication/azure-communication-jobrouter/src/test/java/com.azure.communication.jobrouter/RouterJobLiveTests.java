// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter;

import com.azure.communication.jobrouter.models.AcceptJobOfferResult;
import com.azure.communication.jobrouter.models.CancelJobOptions;
import com.azure.communication.jobrouter.models.CreateJobOptions;
import com.azure.communication.jobrouter.models.CreateWorkerOptions;
import com.azure.communication.jobrouter.models.DistributionPolicy;
import com.azure.communication.jobrouter.models.QueueAndMatchMode;
import com.azure.communication.jobrouter.models.RouterChannel;
import com.azure.communication.jobrouter.models.RouterJob;
import com.azure.communication.jobrouter.models.RouterJobOffer;
import com.azure.communication.jobrouter.models.RouterJobStatus;
import com.azure.communication.jobrouter.models.RouterQueue;
import com.azure.communication.jobrouter.models.RouterValue;
import com.azure.communication.jobrouter.models.RouterWorker;
import com.azure.communication.jobrouter.models.ScheduleAndSuspendMode;
import com.azure.communication.jobrouter.models.UnassignJobResult;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
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

//    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void unassignJob(HttpClient httpClient) {
        // Setup
        jobRouterClient = getRouterClient(httpClient);
        routerAdminClient = getRouterAdministrationClient(httpClient);
        String testName = "unassign-job-test";
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

        jobRouterClient.createWorker(createWorkerOptions);

        String jobId = String.format("%s-%s-Job", JAVA_LIVE_TESTS, testName);
        CreateJobOptions createJobOptions = new CreateJobOptions(jobId, "channel1", queueId);

        jobRouterClient.createJob(createJobOptions);

        List<RouterJobOffer> jobOffers = new ArrayList<RouterJobOffer>();
        long startTimeMillis = System.currentTimeMillis();
        while (true) {
            RouterWorker worker = jobRouterClient.getWorker(workerId);
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
        UnassignJobResult unassignJobResult = jobRouterClient.unassignJob(jobId, assignmentId);

        // Verify
        assertEquals(1, unassignJobResult.getUnassignmentCount());

        RequestOptions requestOptions = new RequestOptions();
        CancelJobOptions cancelJobOptions = new CancelJobOptions()
            .setDispositionCode("dispositionCode")
            .setNote("note");
        requestOptions.setBody(BinaryData.fromObject(cancelJobOptions));
        // Cleanup
        jobRouterClient.cancelJob(jobId, requestOptions);
        jobRouterClient.deleteJob(jobId);
        jobRouterClient.deleteWorker(workerId);
        routerAdminClient.deleteQueue(queueId);
        routerAdminClient.deleteDistributionPolicy(distributionPolicyId);
    }

//    @ParameterizedTest
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
            .setMatchingMode(new ScheduleAndSuspendMode(
                OffsetDateTime.of(2040, 1, 1, 1, 1, 1, 1, ZoneOffset.UTC))));

        // Action
        RouterJob job2 = jobRouterClient.updateJobWithResponse(jobId,
            BinaryData.fromObject(new RouterJob().setMatchingMode(new QueueAndMatchMode())),
            new RequestOptions()).getValue().toObject(RouterJob.class);

        // Verify
        assertEquals(job.getStatus(), RouterJobStatus.PENDING_SCHEDULE);
        assertEquals(job2.getStatus(), RouterJobStatus.QUEUED);

        // Cleanup
        jobRouterClient.cancelJob(jobId);
        jobRouterClient.deleteJob(jobId);
        routerAdminClient.deleteQueue(queueId);
        routerAdminClient.deleteDistributionPolicy(distributionPolicyId);
    }
}
