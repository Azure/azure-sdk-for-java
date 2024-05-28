// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter;

import com.azure.communication.jobrouter.models.DistributionPolicy;
import com.azure.communication.jobrouter.models.RouterQueue;
import com.azure.communication.jobrouter.models.RouterValue;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RouterQueueLiveTests extends JobRouterTestBase {
    private JobRouterClient jobRouterClient;

    private JobRouterAdministrationClient routerAdminClient;

    private static final Logger LOGGER = LoggerFactory.getLogger(RouterQueueLiveTests.class);

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @LiveOnly // Remove after azure-core-test 1.26.0-beta.1 is released.
    public void createQueue(HttpClient httpClient) {
        // Setup
        jobRouterClient = getRouterClient(httpClient);
        routerAdminClient = getRouterAdministrationClient(httpClient);
        String distributionPolicyId = String.format("%s-CreateQueue-DistributionPolicy", JAVA_LIVE_TESTS);
        DistributionPolicy distributionPolicy = createDistributionPolicy(routerAdminClient, distributionPolicyId);

        String queueId = String.format("%s-CreateQueue-Queue", JAVA_LIVE_TESTS);

        // Action
        RouterQueue jobQueue = createQueue(routerAdminClient, queueId, distributionPolicy.getId());

        // Verify
        assertEquals(queueId, jobQueue.getId());

        // Cleanup
        routerAdminClient.deleteQueue(queueId);
        routerAdminClient.deleteDistributionPolicy(distributionPolicyId);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void updateQueue(HttpClient httpClient) {
        // Setup
        jobRouterClient = getRouterClient(httpClient);
        routerAdminClient = getRouterAdministrationClient(httpClient);
        String distributionPolicyId = String.format("%s-CreateQueue-DistributionPolicy", JAVA_LIVE_TESTS);
        DistributionPolicy distributionPolicy = createDistributionPolicy(routerAdminClient, distributionPolicyId);

        String queueId = String.format("%s-CreateQueue-Queue", JAVA_LIVE_TESTS);
        RouterQueue queue = createQueue(routerAdminClient, queueId, distributionPolicy.getId());

        String updatedRouterQueue = "{\"name\":\"JAVA_LIVE_TEST-CreateQueue-Queue\",\"distributionPolicyId\":\"JAVA_LIVE_TEST-CreateQueue-DistributionPolicy\",\"labels\":{\"Label_1\":\"UpdatedValue\"}}";

        Map<String, RouterValue> updatedQueueLabels = new HashMap<String, RouterValue>() {
            {
                put("Label_1", new RouterValue("UpdatedValue"));
            }
        };
        // Action
        BinaryData updatedQueue = routerAdminClient.updateQueueWithResponse(queueId, BinaryData.fromString(updatedRouterQueue), new RequestOptions())
            .getValue();
        LOGGER.info(updatedQueue.toString());

        // Verify
        assertTrue(updatedQueue.toString().contains("\"Label_1\":\"UpdatedValue\""));

        // Cleanup
        routerAdminClient.deleteQueue(queueId);
        routerAdminClient.deleteDistributionPolicy(distributionPolicyId);
    }
}
