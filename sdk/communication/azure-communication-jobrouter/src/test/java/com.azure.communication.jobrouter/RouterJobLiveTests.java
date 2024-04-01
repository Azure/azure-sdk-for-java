// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter;

import com.azure.communication.jobrouter.models.*;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class RouterJobLiveTests extends JobRouterTestBase {
    private JobRouterClient jobRouterClient;

    private JobRouterAdministrationClient routerAdminClient;

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void unassignJob(HttpClient httpClient) {
        // Setup
        jobRouterClient = getRouterClient(httpClient);
        routerAdminClient = getRouterAdministrationClient(httpClient);
        String testName = "unassign-job-test";

        String distributionPolicyId = String.format("%s-%s-DistributionPolicy", JAVA_LIVE_TESTS, testName);
        DistributionPolicy distributionPolicy = createDistributionPolicy(routerAdminClient, distributionPolicyId);

        String queueId = String.format("%s-%s-Queue", JAVA_LIVE_TESTS, testName);
        RouterQueue jobQueue = createQueue(routerAdminClient, queueId, distributionPolicy.getId());
        String workerId = String.format("%s-%s-Worker", JAVA_LIVE_TESTS, testName);
        String channelId = String.format("%s-%s-Channel", JAVA_LIVE_TESTS, testName);

        RouterWorker createdWorker = jobRouterClient.createWorker(new CreateWorkerOptions(workerId, 10)
            .setLabels(new HashMap<String, RouterValue>() {{
                put("IntKey", new RouterValue(4));
                put("BoolKey", new RouterValue(true));
                put("StringLabel", new RouterValue("test"));
            }})
            .setTags(new HashMap<String, RouterValue>() {{
                put("IntTag", new RouterValue(5));
                put("BoolTag", new RouterValue(false));
                put("StringTag", new RouterValue("test2"));
            }})
            .setAvailableForOffers(true)
            .setChannels(new ArrayList<RouterChannel>() {{ add(new RouterChannel(channelId, 1)); }})
            .setQueues(new ArrayList<String>() {{ add(jobQueue.getId()); }}));

        String jobId = String.format("%s-%s-Job", JAVA_LIVE_TESTS, testName);
        CreateJobOptions createJobOptions = new CreateJobOptions(jobId, channelId, queueId)
            .setLabels(new HashMap<String, RouterValue>() {{
                put("IntLabel", new RouterValue(10));
                put("BoolLabel", new RouterValue(true));
                put("StringLabel", new RouterValue("test"));
            }})
            .setTags(new HashMap<String, RouterValue>() {{
                put("IntTag", new RouterValue(5));
                put("BoolTag", new RouterValue(false));
                put("StringTag", new RouterValue("test2"));
            }})
            .setRequestedWorkerSelectors(new ArrayList<RouterWorkerSelector>() {{
                add(new RouterWorkerSelector("IntKey", LabelOperator.GREATER_THAN).setValue(new RouterValue(2)));
                add(new RouterWorkerSelector("BoolKey", LabelOperator.EQUAL).setValue(new RouterValue(true)));
            }})
            .setNotes(new ArrayList<RouterJobNote>() {{ add(new RouterJobNote("Note1")); }})
            .setDispositionCode("code1")
            .setChannelReference("ref")
            .setPriority(5);

        RouterJob job = jobRouterClient.createJob(createJobOptions);

        // Verify
        assertEquals(jobId, job.getId());
        assertNotNull(job.getEtag());
        assertEquals(3, job.getLabels().size());
        assertEquals(3, job.getTags().size());
        assertEquals(1, job.getNotes().size());
        assertEquals( "code1", job.getDispositionCode());
        assertEquals("ref", job.getChannelReference());
        assertEquals( 5, job.getPriority());
        assertEquals(2, job.getRequestedWorkerSelectors().size());

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
            .setMatchingMode(new ScheduleAndSuspendMode(
                OffsetDateTime.of(2040, 1, 1, 1, 1, 1, 1, ZoneOffset.UTC))));
        assertEquals(RouterJobStatus.PENDING_SCHEDULE, job.getStatus());

        // Action
        RouterJob job2 = jobRouterClient.updateJobWithResponse(jobId,
            BinaryData.fromObject(new RouterJob().setMatchingMode(new QueueAndMatchMode())),
            new RequestOptions()).getValue().toObject(RouterJob.class);

        assertEquals(RouterJobStatus.QUEUED, job2.getStatus());

        // Cleanup
        jobRouterClient.cancelJob(jobId);
        jobRouterClient.deleteJob(jobId);
        routerAdminClient.deleteQueue(queueId);
        routerAdminClient.deleteDistributionPolicy(distributionPolicyId);
    }
}
