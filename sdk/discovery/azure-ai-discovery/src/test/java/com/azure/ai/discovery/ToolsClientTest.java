// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.discovery;

import com.azure.ai.discovery.models.ComputeUsage;
import com.azure.ai.discovery.models.OperationStatusRunResultError;
import com.azure.ai.discovery.models.PagedOperation;
import com.azure.ai.discovery.models.RunOptions;
import com.azure.ai.discovery.models.RunResult;
import com.azure.core.util.polling.PollOperationDetails;
import com.azure.core.util.polling.SyncPoller;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for {@link ToolsClient}, covering the run lifecycle (begin/cancel), run-status retrieval, operation listing,
 * and compute-usage retrieval.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public final class ToolsClientTest extends DiscoveryClientTestBase {

    @Test
    @Order(1)
    public void testBeginRun() {
        ToolsClient client = getToolsClient();
        SyncPoller<PollOperationDetails, RunResult> poller
            = client.beginRun(new RunOptions(projectName, toolId, Arrays.asList(nodePoolId)).setCommand("echo hello"));
        RunResult result = poller.getFinalResult();

        assertNotNull(result);
    }

    @Test
    @Order(3)
    public void testGetOperations() {
        ToolsClient client = getToolsClient();
        PagedOperation operations = client.getOperations(projectName);

        assertNotNull(operations);
        assertNotNull(operations.getValue());
    }

    @Test
    @Order(2)
    public void testGetRunStatus() {
        ToolsClient client = getToolsClient();
        // Start a run so there is an operation to query.
        SyncPoller<PollOperationDetails, RunResult> poller
            = client.beginRun(new RunOptions(projectName, toolId, Arrays.asList(nodePoolId)).setCommand("echo hello"));
        String operationId = poller.poll().getValue().getOperationId();

        OperationStatusRunResultError status = client.getRunStatus(projectName, operationId);
        assertNotNull(status);
    }

    @Test
    @Order(4)
    public void testGetComputeUsage() {
        ToolsClient client = getToolsClient();
        ComputeUsage usage = client.getComputeUsage(projectName);

        assertNotNull(usage);
    }
}
