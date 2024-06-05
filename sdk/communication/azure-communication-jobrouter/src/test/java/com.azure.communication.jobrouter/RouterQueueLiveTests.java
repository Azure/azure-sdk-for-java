// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter;

import com.azure.communication.jobrouter.models.DistributionPolicy;
import com.azure.communication.jobrouter.models.RouterQueue;
import com.azure.communication.jobrouter.models.RouterValue;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class RouterQueueLiveTests extends JobRouterTestBase {
    private JobRouterAdministrationClient routerAdminClient;

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createQueue(HttpClient httpClient) {
        // Setup
        routerAdminClient = getRouterAdministrationClient(httpClient);
        String distributionPolicyId = String.format("%s-CreateQueueAsync-DistributionPolicy", JAVA_LIVE_TESTS);
        DistributionPolicy distributionPolicy = createDistributionPolicy(routerAdminClient, distributionPolicyId);

        String queueId = String.format("%s-CreateQueue-Queue", JAVA_LIVE_TESTS);

        // Action
        RouterQueue jobQueue = createQueue(routerAdminClient, queueId, distributionPolicy.getId());

        // Verify
        assertEquals(queueId, jobQueue.getId());
        assertNotNull(jobQueue.getEtag());
        assertEquals(queueId + "-Name", jobQueue.getName());
        assertEquals(distributionPolicyId, jobQueue.getDistributionPolicyId());
        assertEquals(2, jobQueue.getLabels().size());

        BinaryData binaryData = routerAdminClient.getQueueWithResponse(queueId, null).getValue();
        RouterQueue deserialized = binaryData.toObject(RouterQueue.class);

        assertEquals(queueId, deserialized.getId());
        assertEquals(jobQueue.getEtag(), deserialized.getEtag());
        assertEquals(queueId + "-Name", deserialized.getName());
        assertEquals(distributionPolicyId, deserialized.getDistributionPolicyId());
        assertEquals(2, deserialized.getLabels().size());

        // Cleanup
        routerAdminClient.deleteQueue(queueId);
        routerAdminClient.deleteDistributionPolicy(distributionPolicyId);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void updateQueue(HttpClient httpClient) {
        // Setup
        routerAdminClient = getRouterAdministrationClient(httpClient);
        String distributionPolicyId = String.format("%s-UpdateQueue-DistributionPolicy", JAVA_LIVE_TESTS);
        DistributionPolicy distributionPolicy = createDistributionPolicy(routerAdminClient, distributionPolicyId);

        String queueId = String.format("%s-CreateQueue-Queue", JAVA_LIVE_TESTS);
        RouterQueue queue = createQueue(routerAdminClient, queueId, distributionPolicy.getId());

        Map<String, RouterValue> updatedQueueLabels = Collections.singletonMap("Label_1", new RouterValue("UpdatedValue"));
        queue.setLabels(updatedQueueLabels);

        // Action
        Response<BinaryData> binaryData = routerAdminClient.updateQueueWithResponse(queueId, BinaryData.fromObject(queue), new RequestOptions());
        RouterQueue updatedQueue = binaryData.getValue().toObject(RouterQueue.class);

        // Verify
        assertEquals("UpdatedValue", updatedQueue.getLabels().get("Label_1").getStringValue());

        // Cleanup
        routerAdminClient.deleteQueue(queueId);
        routerAdminClient.deleteDistributionPolicy(distributionPolicyId);
    }
}
