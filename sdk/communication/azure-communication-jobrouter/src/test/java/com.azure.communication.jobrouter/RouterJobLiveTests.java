package com.azure.communication.jobrouter;

import com.azure.communication.jobrouter.models.AcceptJobOfferResult;
import com.azure.communication.jobrouter.models.ChannelConfiguration;
import com.azure.communication.jobrouter.models.DistributionPolicy;
import com.azure.communication.jobrouter.models.JobOffer;
import com.azure.communication.jobrouter.models.JobQueue;
import com.azure.communication.jobrouter.models.LabelValue;
import com.azure.communication.jobrouter.models.QueueAssignment;
import com.azure.communication.jobrouter.models.RouterJob;
import com.azure.communication.jobrouter.models.RouterWorker;
import com.azure.communication.jobrouter.models.UnassignJobResult;
import com.azure.communication.jobrouter.models.options.CreateJobOptions;
import com.azure.communication.jobrouter.models.options.CreateWorkerOptions;
import com.azure.communication.jobrouter.models.options.UnassignJobOptions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RouterJobLiveTests extends JobRouterTestBase {
    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void unassignJob() {
        String testName = "UnassignJob";
        // Setup
        /**
         * Setup queue
         */
        String distributionPolicyId = String.format("%s-%s-DistributionPolicy", JAVA_LIVE_TESTS, testName);
        DistributionPolicy distributionPolicy = createDistributionPolicy(routerAdminClient, distributionPolicyId);
        distributionPoliciesToDelete.add(distributionPolicyId);

        String queueId = String.format("%s-%s-Queue", JAVA_LIVE_TESTS, testName);
        JobQueue jobQueue = createQueue(routerAdminClient, queueId, distributionPolicy.getId());
        queuesToDelete.add(queueId);

        /**
         * Setup worker
         */
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
                put(jobQueue.getId(), new QueueAssignment());
            }
        };

        String workerId = String.format("%s-%s-Worker", JAVA_LIVE_TESTS, testName);
        CreateWorkerOptions createWorkerOptions = new CreateWorkerOptions(workerId, 10)
            .setLabels(labels)
            .setTags(tags)
            .setAvailableForOffers(true)
            .setChannelConfigurations(channelConfigurations)
            .setQueueAssignments(queueAssignments);

        RouterWorker worker = routerClient.createWorker(createWorkerOptions);
        workersToDelete.add(workerId);

        String jobId = String.format("%s-%s-Job", JAVA_LIVE_TESTS, testName);
        CreateJobOptions createJobOptions = new CreateJobOptions(jobId, "channel1", queueId);

        RouterJob job = routerClient.createJob(createJobOptions);
        jobsToDelete.add(job.getId());

        List<JobOffer> jobOffers = new ArrayList<>();
        long startTimeMillis = System.currentTimeMillis();
        while (true) {
            worker = routerClient.getWorker(workerId);
            jobOffers = worker.getOffers();
            if (jobOffers.size() > 0 || System.currentTimeMillis() - startTimeMillis > 10000) break;
        }

        assertTrue(jobOffers.size() == 1);

        JobOffer offer = jobOffers.get(0);

        AcceptJobOfferResult acceptJobOfferResult = routerClient.acceptJobOffer(workerId, offer.getId());

        String assignmentId = acceptJobOfferResult.getAssignmentId();

        // Action
        UnassignJobOptions unassignJobOptions = new UnassignJobOptions(jobId, assignmentId);
        UnassignJobResult unassignJobResult = routerClient.unassignJob(unassignJobOptions);

        // Verify
        assertEquals(1, unassignJobResult.getUnassignmentCount());
    }
}
