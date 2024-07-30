// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter;

import com.azure.communication.jobrouter.models.CreateWorkerOptions;
import com.azure.communication.jobrouter.models.DistributionPolicy;
import com.azure.communication.jobrouter.models.RouterChannel;
import com.azure.communication.jobrouter.models.RouterQueue;
import com.azure.communication.jobrouter.models.RouterValue;
import com.azure.communication.jobrouter.models.RouterWorker;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class RouterWorkerLiveTests extends JobRouterTestBase {

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createWorker(HttpClient httpClient) {
        // Setup
        JobRouterClient jobRouterClient = getRouterClient(httpClient);
        JobRouterAdministrationClient routerAdminClient = getRouterAdministrationClient(httpClient);
        /**
         * Setup queue
         */
        String distributionPolicyId = String.format("%s-CreateWorker-DistributionPolicy", JAVA_LIVE_TESTS);
        DistributionPolicy distributionPolicy = createDistributionPolicy(routerAdminClient, distributionPolicyId);

        String queueId = String.format("%s-CreateWorker-Queue", JAVA_LIVE_TESTS);
        RouterQueue jobQueue = createQueue(routerAdminClient, queueId, distributionPolicy.getId());

        /**
         * Setup worker
         */
        String workerId = String.format("%s-CreateWorker-Worker", JAVA_LIVE_TESTS);

        Map<String, RouterValue> labels = Collections.singletonMap("Label", new RouterValue("Value"));
        Map<String, RouterValue> tags = Collections.singletonMap("Tag", new RouterValue("Value"));

        RouterChannel channel = new RouterChannel("router-channel", 1);
        List<RouterChannel> channels = Collections.singletonList(channel);
        List<String> queues = Collections.singletonList(jobQueue.getId());

        CreateWorkerOptions createWorkerOptions = new CreateWorkerOptions(workerId, 10)
            .setLabels(labels)
            .setTags(tags)
            .setAvailableForOffers(false)
            .setChannels(channels)
            .setQueues(queues)
            .setMaxConcurrentOffers(1);

        // Action
        RouterWorker result = jobRouterClient.createWorker(createWorkerOptions);

        // Verify
        assertEquals(workerId, result.getId());
        assertEquals(result.isAvailableForOffers(), false);
        assertEquals(labels.size() + 1, result.getLabels().size());
        assertEquals(tags.size(), result.getTags().size());
        assertArrayEquals(queues.toArray(), result.getQueues().toArray());
        assertEquals(channels.size(), result.getChannels().size());
        assertNotNull(result.getEtag());
        assertEquals(1, result.getMaxConcurrentOffers());

        Response<BinaryData> getWorker = jobRouterClient.getWorkerWithResponse(result.getId(), null);
        RouterWorker deserialized = getWorker.getValue().toObject(RouterWorker.class);

        assertEquals(workerId, deserialized.getId());
        assertEquals(deserialized.isAvailableForOffers(), false);
        assertEquals(labels.size() + 1, deserialized.getLabels().size());
        assertEquals(tags.size(), deserialized.getTags().size());
        assertArrayEquals(queues.toArray(), deserialized.getQueues().toArray());
        assertEquals(channels.size(), deserialized.getChannels().size());
        assertEquals(deserialized.getEtag(), result.getEtag());

        sleepIfRunningAgainstService(2000);

        deserialized.setAvailableForOffers(true);
        deserialized.setChannels(Arrays.asList(new RouterChannel("channel1", 5), new RouterChannel("channel2", 5)));
        RouterWorker updatedWorker = jobRouterClient.updateWorker(workerId, deserialized);

        assertEquals(workerId, updatedWorker.getId());
        assertEquals(updatedWorker.isAvailableForOffers(), true);
        assertEquals(labels.size() + 1, updatedWorker.getLabels().size());
        assertEquals(tags.size(), updatedWorker.getTags().size());
        assertArrayEquals(queues.toArray(), updatedWorker.getQueues().toArray());
        assertEquals(deserialized.getChannels().size(), updatedWorker.getChannels().size());
        assertNotEquals(deserialized.getEtag(), updatedWorker.getEtag());

        for (RouterWorker listWorker : jobRouterClient.listWorkers(null, channel.getChannelId(), queueId, null)) {
            assertEquals(workerId, listWorker.getId());
            assertEquals(listWorker.isAvailableForOffers(), true);
            assertEquals(labels.size() + 1, listWorker.getLabels().size());
            assertEquals(tags.size(), listWorker.getTags().size());
            assertArrayEquals(queues.toArray(), listWorker.getQueues().toArray());
            assertEquals(channels.size(), listWorker.getChannels().size());
            assertEquals(updatedWorker.getEtag(), listWorker.getEtag());
        }

        // Cleanup
        jobRouterClient.deleteWorker(workerId);
        routerAdminClient.deleteQueue(queueId);
        routerAdminClient.deleteDistributionPolicy(distributionPolicyId);
    }
}
