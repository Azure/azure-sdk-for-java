// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.discovery;

import com.azure.ai.discovery.models.ComputeUsage;
import com.azure.ai.discovery.models.OperationStatusRunResultError;
import com.azure.ai.discovery.models.PagedOperation;
import com.azure.ai.discovery.models.RunOptions;
import com.azure.ai.discovery.models.RunResult;
import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollOperationDetails;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for {@link ToolsAsyncClient}, covering the run lifecycle, run-status retrieval, operation listing, and
 * compute-usage retrieval.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public final class ToolsAsyncClientTest extends DiscoveryClientTestBase {

    private RunOptions runOptions() {
        return new RunOptions(projectName, toolId, Arrays.asList(nodePoolId)).setCommand("echo hello");
    }

    @Test
    @Order(1)
    public void testBeginRun() {
        ToolsAsyncClient client = getToolsAsyncClient();
        RunResult result = client.beginRun(runOptions()).last().flatMap(AsyncPollResponse::getFinalResult).block();

        assertNotNull(result);
    }

    @Test
    @Order(3)
    public void testGetOperations() {
        ToolsAsyncClient client = getToolsAsyncClient();
        PagedOperation operations = client.getOperations(projectName).block();

        assertNotNull(operations);
        assertNotNull(operations.getValue());
    }

    @Test
    @Order(2)
    public void testGetRunStatus() {
        ToolsAsyncClient client = getToolsAsyncClient();
        String operationId = client.beginRun(runOptions()).blockLast().getValue().getOperationId();

        OperationStatusRunResultError status = client.getRunStatus(projectName, operationId).block();
        assertNotNull(status);
    }

    @Test
    @Order(4)
    public void testGetComputeUsage() {
        ToolsAsyncClient client = getToolsAsyncClient();
        ComputeUsage usage = client.getComputeUsage(projectName).block();

        assertNotNull(usage);
    }

    @Test
    @Order(5)
    public void testBeginCancelRun() {
        ToolsAsyncClient client = getToolsAsyncClient();
        // Start a long-running command so there is an in-progress operation to cancel.
        RunOptions cancelRunOptions = new RunOptions(projectName, toolId, Arrays.asList(nodePoolId))
            .setCommand("echo \"cancel test\" && sleep 300");
        // Take the operation id from the activation response without polling the run to completion.
        String operationId = client.beginRun(cancelRunOptions).blockFirst().getValue().getOperationId();

        // Cancel the in-progress run; the operation reaches a terminal cancelled state.
        AsyncPollResponse<PollOperationDetails, RunResult> finalResponse
            = client.beginCancelRun(projectName, operationId).blockLast();

        assertEquals(LongRunningOperationStatus.USER_CANCELLED, finalResponse.getStatus());
    }
}
