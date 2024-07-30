// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter;

import com.azure.communication.jobrouter.models.CreateDistributionPolicyOptions;
import com.azure.communication.jobrouter.models.CreateQueueOptions;
import com.azure.communication.jobrouter.models.DistributionPolicy;
import com.azure.communication.jobrouter.models.LongestIdleMode;
import com.azure.communication.jobrouter.models.RouterQueue;
import com.azure.communication.jobrouter.models.RouterValue;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class RouterQueueAsyncLiveTests extends JobRouterTestBase {
    private JobRouterAdministrationAsyncClient administrationAsyncClient;

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createQueue(HttpClient httpClient) {
        // Setup
        administrationAsyncClient = getRouterAdministrationAsyncClient(httpClient);
        String distributionPolicyId = String.format("%s-CreateQueueAsync-DistributionPolicy", JAVA_LIVE_TESTS);
        String distributionPolicyName = String.format("%s-Name", distributionPolicyId);

        CreateDistributionPolicyOptions createDistributionPolicyOptions = new CreateDistributionPolicyOptions(
            distributionPolicyId,
            Duration.ofSeconds(10),
            new LongestIdleMode()
                .setMinConcurrentOffers(1)
                .setMaxConcurrentOffers(10))
            .setName(distributionPolicyName);
        DistributionPolicy distributionPolicy = administrationAsyncClient.createDistributionPolicy(createDistributionPolicyOptions).block();

        String queueId = String.format("%s-CreateQueueAsync-Queue", JAVA_LIVE_TESTS);

        // Action
        String queueName = String.format("%s-Name", queueId);
        Map<String, RouterValue> queueLabels = Collections.singletonMap("Label_1", new RouterValue("Value_1"));

        CreateQueueOptions createQueueOptions = new CreateQueueOptions(queueId, distributionPolicyId)
            .setLabels(queueLabels)
            .setName(queueName);
        RouterQueue jobQueue = administrationAsyncClient.createQueue(createQueueOptions).block();

        // Verify
        assertEquals(queueId, jobQueue.getId());
        assertNotNull(jobQueue.getEtag());
        assertEquals(queueId + "-Name", jobQueue.getName());
        assertEquals(distributionPolicyId, jobQueue.getDistributionPolicyId());
        assertEquals(2, jobQueue.getLabels().size());

        BinaryData binaryData = administrationAsyncClient.getQueueWithResponse(queueId, null).block().getValue();
        RouterQueue deserialized = binaryData.toObject(RouterQueue.class);

        assertEquals(queueId, deserialized.getId());
        assertEquals(jobQueue.getEtag(), deserialized.getEtag());
        assertEquals(queueId + "-Name", deserialized.getName());
        assertEquals(distributionPolicyId, deserialized.getDistributionPolicyId());
        assertEquals(2, deserialized.getLabels().size());

        // Cleanup
        administrationAsyncClient.deleteQueue(queueId).block();
        administrationAsyncClient.deleteDistributionPolicy(distributionPolicyId).block();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void updateQueue(HttpClient httpClient) {
        // Setup
        administrationAsyncClient = getRouterAdministrationAsyncClient(httpClient);
        String distributionPolicyId = String.format("%s-UpdateQueueAsync-DistributionPolicy", JAVA_LIVE_TESTS);
        String distributionPolicyName = String.format("%s-Name", distributionPolicyId);

        String queueId = String.format("%s-UpdateQueueAsync-Queue", JAVA_LIVE_TESTS);

        CreateDistributionPolicyOptions createDistributionPolicyOptions = new CreateDistributionPolicyOptions(
            distributionPolicyId,
            Duration.ofSeconds(10),
            new LongestIdleMode()
                .setMinConcurrentOffers(1)
                .setMaxConcurrentOffers(10))
            .setName(distributionPolicyName);
        DistributionPolicy distributionPolicy = administrationAsyncClient.createDistributionPolicy(createDistributionPolicyOptions).block();

        String queueName = String.format("%s-Name", queueId);
        Map<String, RouterValue> queueLabels = Collections.singletonMap("Label_1", new RouterValue("Value_1"));

        CreateQueueOptions createQueueOptions = new CreateQueueOptions(queueId, distributionPolicyId)
            .setLabels(queueLabels)
            .setName(queueName);
        RouterQueue jobQueue = administrationAsyncClient.createQueue(createQueueOptions).block();

        // Update Label
        jobQueue.getLabels().put("Label_1", new RouterValue("UpdatedValue"));

        // Action
        Response<BinaryData> binaryData = administrationAsyncClient.updateQueueWithResponse(queueId, BinaryData.fromObject(jobQueue), new RequestOptions()).block();
        RouterQueue updatedQueue = binaryData.getValue().toObject(RouterQueue.class);

        // Verify
        assertEquals("UpdatedValue", updatedQueue.getLabels().get("Label_1").getStringValue());

        // Cleanup
        administrationAsyncClient.deleteQueue(queueId).block();
        administrationAsyncClient.deleteDistributionPolicy(distributionPolicyId).block();
    }
}
