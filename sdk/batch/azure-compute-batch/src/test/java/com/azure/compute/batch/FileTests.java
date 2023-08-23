package com.azure.compute.batch;

import com.azure.compute.batch.models.*;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestMode;
import com.azure.core.util.BinaryData;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class FileTests extends BatchServiceClientTestBase {
    private static String poolId;
    private static BatchPool livePool;

    @Override
    protected void beforeTest() {
        super.beforeTest();
        poolId = getStringIdWithUserNamePrefix("-testpool");
        if(getTestMode() == TestMode.RECORD) {
            try {
                livePool = createIfNotExistIaaSPool(poolId);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            Assertions.assertNotNull(livePool);
        }
    }

    @Test
    public void canReadFromTaskFile() throws Exception {
        // CREATE
        String jobId = getStringIdWithUserNamePrefix("-Job-canReadFromTaskFile");
        String taskId = "mytask";
        int TASK_COMPLETE_TIMEOUT_IN_SECONDS = 60; // 60 seconds timeout

        try {
            PoolInformation poolInfo = new PoolInformation();
            poolInfo.setPoolId(poolId);

            jobClient.create(new BatchJobCreateParameters(jobId, poolInfo));

            BatchTaskCreateParameters taskToAdd = new BatchTaskCreateParameters(taskId, "/bin/bash -c \"echo hello\"");
            taskClient.create(jobId, taskToAdd);

            if (waitForTasksToComplete(taskClient, jobId, TASK_COMPLETE_TIMEOUT_IN_SECONDS)) {
                PagedIterable<NodeFile> filesIterable = taskClient.listFilesFromTask(jobId, taskId);
                boolean found = false;
                for (NodeFile f : filesIterable) {
                    if (f.getName().equals("stdout.txt")) {
                        found = true;
                        break;
                    }
                }
                Assertions.assertTrue(found);

                BinaryData binaryData = taskClient.getFileFromTask(jobId, taskId, "stdout.txt");
                String fileContent = new String(binaryData.toBytes(), StandardCharsets.UTF_8);
                Assertions.assertEquals("hello\n", fileContent);

                binaryData = batchClientBuilder.buildTaskAsyncClient().getFileFromTaskWithResponse(jobId, taskId, "stdout.txt", null).block().getValue();
                Assertions.assertEquals("hello\n", binaryData.toString());

                Response<Void> getFilePropertiesResponse = taskClient.getFilePropertiesFromTaskWithResponse(jobId, taskId, "stdout.txt", null);
                Assertions.assertEquals("6", getFilePropertiesResponse.getHeaders().getValue(HttpHeaderName.CONTENT_LENGTH));

            } else {
                throw new TimeoutException("Task did not complete within the specified timeout");
            }
        } finally {
            try {
                jobClient.delete(jobId);
            } catch (Exception e) {
                // Ignore here
            }
        }
    }

    @Test
    public void canReadFromNode() throws Exception {
        // CREATE
        String jobId = getStringIdWithUserNamePrefix("-Job-canReadFromNode");
        String taskId = "mytask";
        int TASK_COMPLETE_TIMEOUT_IN_SECONDS = 60; // 60 seconds timeout

        try {
            PoolInformation poolInfo = new PoolInformation();
            poolInfo.setPoolId(poolId);

            jobClient.create(new BatchJobCreateParameters(jobId, poolInfo));
            BatchTaskCreateParameters taskToAdd = new BatchTaskCreateParameters(taskId, "/bin/bash -c \"echo hello\"");
            taskClient.create(jobId, taskToAdd);

            if (waitForTasksToComplete(taskClient, jobId, TASK_COMPLETE_TIMEOUT_IN_SECONDS)) {
                BatchTask task = taskClient.get(jobId, taskId);
                String nodeId = task.getNodeInfo().getNodeId();
                PagedIterable<NodeFile> files = nodesClient.listFilesFromBatchNode(poolId, nodeId, null, null, null, null, true);
                String fileName = null;
                for (NodeFile f : files) {
                    if (f.getName().endsWith("stdout.txt")) {
                        fileName = f.getName();
                        break;
                    }
                }
                Assert.assertNotNull(fileName);

                BinaryData binaryData = nodesClient.getFileFromBatchNode(poolId, nodeId, fileName);
                String fileContent = new String(binaryData.toBytes(), StandardCharsets.UTF_8);
                Assert.assertEquals("hello\n", fileContent);

                binaryData = batchClientBuilder.buildBatchNodesAsyncClient().getFileFromBatchNodeWithResponse(poolId, nodeId, fileName, null).block().getValue();
                Assertions.assertEquals("hello\n", binaryData.toString());

                Response<Void> getFilePropertiesResponse = nodesClient.getFilePropertiesFromBatchNodeWithResponse(poolId, nodeId, fileName, null);
                Assertions.assertEquals("6", getFilePropertiesResponse.getHeaders().getValue(HttpHeaderName.CONTENT_LENGTH));

            } else {
                throw new TimeoutException("Task did not complete within the specified timeout");
            }
        }
        finally {
            try {
                jobClient.delete(jobId);
            }
            catch (Exception e) {
                // Ignore here
            }
        }
    }
}
