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
import com.azure.communication.jobrouter.models.RouterJobNote;
import com.azure.communication.jobrouter.models.RouterJobOffer;
import com.azure.communication.jobrouter.models.RouterQueue;
import com.azure.communication.jobrouter.models.RouterValue;
import com.azure.communication.jobrouter.models.RouterWorker;
import com.azure.communication.jobrouter.models.RouterWorkerSelector;
import com.azure.communication.jobrouter.models.UnassignJobResult;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.test.TestMode;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class RouterJobAsyncLiveTests extends JobRouterTestBase {
    private JobRouterAsyncClient jobRouterAsyncClient;

    private JobRouterAdministrationAsyncClient administrationAsyncClient;

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void unassignJob(HttpClient httpClient) throws InterruptedException {
        // Setup
        jobRouterAsyncClient = getRouterAsyncClient(httpClient);
        administrationAsyncClient = getRouterAdministrationAsyncClient(httpClient);
        String testName = "unassign-job-async-test";

        String distributionPolicyId = String.format("%s-%s-DistributionPolicy", JAVA_LIVE_TESTS, testName);
        DistributionPolicy distributionPolicy = administrationAsyncClient.createDistributionPolicy(
            new CreateDistributionPolicyOptions(distributionPolicyId, Duration.ofSeconds(100), new LongestIdleMode())).block();

        String queueId = String.format("%s-%s-Queue", JAVA_LIVE_TESTS, testName);
        RouterQueue jobQueue = administrationAsyncClient.createQueue(new CreateQueueOptions(queueId, distributionPolicy.getId())).block();
        String workerId = String.format("%s-%s-Worker", JAVA_LIVE_TESTS, testName);
        String channelId = String.format("%s-%s-Channel", JAVA_LIVE_TESTS, testName);

        RouterWorker createdWorker = jobRouterAsyncClient.createWorker(new CreateWorkerOptions(workerId, 10)
            .setLabels(new HashMap<String, RouterValue>() {
                {
                    put("IntKey", new RouterValue(4));
                    put("BoolKey", new RouterValue(true));
                    put("StringLabel", new RouterValue("test"));
                }
            })
            .setTags(new HashMap<String, RouterValue>() {
                {
                    put("IntTag", new RouterValue(5));
                    put("BoolTag", new RouterValue(false));
                    put("StringTag", new RouterValue("test2"));
                }
            })
            .setAvailableForOffers(true)
            .setChannels(new ArrayList<RouterChannel>() {
                {
                    add(new RouterChannel(channelId, 1));
                }
            })
            .setQueues(new ArrayList<String>() {
                {
                    add(jobQueue.getId());
                }
            })).block();

        String jobId = String.format("%s-%s-Job", JAVA_LIVE_TESTS, testName);
        CreateJobOptions createJobOptions = new CreateJobOptions(jobId, channelId, queueId)
            .setLabels(new HashMap<String, RouterValue>() {
                {
                    put("IntLabel", new RouterValue(10));
                    put("BoolLabel", new RouterValue(true));
                    put("StringLabel", new RouterValue("test"));
                }
            })
            .setTags(new HashMap<String, RouterValue>() {
                {
                    put("IntTag", new RouterValue(5));
                    put("BoolTag", new RouterValue(false));
                    put("StringTag", new RouterValue("test2"));
                }
            })
            .setRequestedWorkerSelectors(new ArrayList<RouterWorkerSelector>() {
                {
                    add(new RouterWorkerSelector("IntKey", LabelOperator.GREATER_THAN)
                        .setValue(new RouterValue(2)));
                    add(new RouterWorkerSelector("BoolKey", LabelOperator.EQUAL)
                        .setValue(new RouterValue(true)));
                }
            })
            .setNotes(new ArrayList<RouterJobNote>() {
                {
                    add(new RouterJobNote("Note1"));
                }
            })
            .setDispositionCode("code1")
            .setChannelReference("ref")
            .setPriority(5);

        RouterJob job = jobRouterAsyncClient.createJob(createJobOptions).block();

        // Verify
        assertEquals(jobId, job.getId());
        assertNotNull(job.getEtag());
        assertEquals(3, job.getLabels().size());
        assertEquals(3, job.getTags().size());
        assertEquals(1, job.getNotes().size());
        assertEquals("code1", job.getDispositionCode());
        assertEquals("ref", job.getChannelReference());
        assertEquals(5, job.getPriority());
        assertEquals(2, job.getRequestedWorkerSelectors().size());

        List<RouterJobOffer> jobOffers = new ArrayList<RouterJobOffer>();
        long startTimeMillis = System.currentTimeMillis();
        while (true) {
            RouterWorker worker = jobRouterAsyncClient.getWorker(workerId).block();
            jobOffers = worker.getOffers();
            if (jobOffers.size() > 0 || System.currentTimeMillis() - startTimeMillis > 10000) {
                break;
            }
        }

        assertEquals(1, jobOffers.size());

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
        if (this.getTestMode() != TestMode.PLAYBACK) {
            Thread.sleep(10000);
        }
        administrationAsyncClient.deleteQueue(queueId).block();
        administrationAsyncClient.deleteDistributionPolicy(distributionPolicyId).block();
    }
}
