// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter;

import com.azure.communication.jobrouter.models.CreateDistributionPolicyOptions;
import com.azure.communication.jobrouter.models.CreateQueueOptions;
import com.azure.communication.jobrouter.models.CreateWorkerOptions;
import com.azure.communication.jobrouter.models.DistributionPolicy;
import com.azure.communication.jobrouter.models.LongestIdleMode;
import com.azure.communication.jobrouter.models.RouterChannel;
import com.azure.communication.jobrouter.models.RouterQueue;
import com.azure.communication.jobrouter.models.RouterValue;
import com.azure.communication.jobrouter.models.RouterWorker;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class RouterWorkerAsyncLiveTests extends JobRouterTestBase {

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createWorker(HttpClient httpClient) {
        // Setup
        JobRouterAsyncClient routerAsyncClient = getRouterAsyncClient(httpClient);
        JobRouterAdministrationAsyncClient administrationAsyncClient = getRouterAdministrationAsyncClient(httpClient);
        /**
         * Setup queue
         */
        String distributionPolicyId = String.format("%s-CreateWorkerAsync-DistributionPolicy", JAVA_LIVE_TESTS);
        String distributionPolicyName = String.format("%s-Name", distributionPolicyId);

        CreateDistributionPolicyOptions createDistributionPolicyOptions = new CreateDistributionPolicyOptions(
            distributionPolicyId, Duration.ofSeconds(10),
            new LongestIdleMode()
                .setMinConcurrentOffers(1)
                .setMaxConcurrentOffers(10))
            .setName(distributionPolicyName);
        DistributionPolicy distributionPolicy = administrationAsyncClient.createDistributionPolicy(createDistributionPolicyOptions).block();

        String queueId = String.format("%s-CreateWorker-Queue", JAVA_LIVE_TESTS);
        String queueName = String.format("%s-Name", queueId);
        Map<String, RouterValue> queueLabels = Collections.singletonMap("Label_1", new RouterValue("Value_1"));

        CreateQueueOptions createQueueOptions = new CreateQueueOptions(queueId, distributionPolicyId)
            .setLabels(queueLabels)
            .setName(queueName);
        RouterQueue jobQueue = administrationAsyncClient.createQueue(createQueueOptions).block();

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
        RouterWorker result = routerAsyncClient.createWorker(createWorkerOptions).block();

        // Verify
        assertEquals(workerId, result.getId());
        assertEquals(1, result.getMaxConcurrentOffers());
        assertEquals(result.isAvailableForOffers(), false);
        assertEquals(labels.size() + 1, result.getLabels().size());
        assertEquals(tags.size(), result.getTags().size());
        assertArrayEquals(queues.toArray(), result.getQueues().toArray());
        assertEquals(channels.size(), result.getChannels().size());
        assertNotNull(result.getEtag());

        Response<BinaryData> getWorker = routerAsyncClient.getWorkerWithResponse(result.getId(), null).block();
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

        RouterWorker updatedWorker = routerAsyncClient.updateWorker(workerId, deserialized).block();

        assertEquals(workerId, updatedWorker.getId());
        assertEquals(updatedWorker.isAvailableForOffers(), true);
        assertEquals(labels.size() + 1, updatedWorker.getLabels().size());
        assertEquals(tags.size(), updatedWorker.getTags().size());
        assertArrayEquals(queues.toArray(), updatedWorker.getQueues().toArray());
        assertEquals(deserialized.getChannels().size(), updatedWorker.getChannels().size());
        assertNotEquals(deserialized.getEtag(), updatedWorker.getEtag());

        routerAsyncClient.listWorkers(null, channel.getChannelId(), queueId, null)
            .subscribe(listWorker -> {
                assertEquals(workerId, listWorker.getId());
                assertEquals(listWorker.isAvailableForOffers(), true);
                assertEquals(labels.size() + 1, listWorker.getLabels().size());
                assertEquals(tags.size(), listWorker.getTags().size());
                assertArrayEquals(queues.toArray(), listWorker.getQueues().toArray());
                assertEquals(channels.size(), listWorker.getChannels().size());
                assertEquals(updatedWorker.getEtag(), listWorker.getEtag());
            });

        // Cleanup
        routerAsyncClient.deleteWorker(workerId).block();
        administrationAsyncClient.deleteQueue(queueId).block();
        administrationAsyncClient.deleteDistributionPolicy(distributionPolicyId).block();
    }
}
