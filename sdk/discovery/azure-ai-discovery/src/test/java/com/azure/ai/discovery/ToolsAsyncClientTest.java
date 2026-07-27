// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.discovery;

import com.azure.ai.discovery.models.ComputeUsage;
import com.azure.ai.discovery.models.OperationStatusRunResultError;
import com.azure.ai.discovery.models.PagedOperation;
import com.azure.ai.discovery.models.RunOptions;
import com.azure.ai.discovery.models.RunResult;
import com.azure.core.util.polling.AsyncPollResponse;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.Arrays;

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
}
