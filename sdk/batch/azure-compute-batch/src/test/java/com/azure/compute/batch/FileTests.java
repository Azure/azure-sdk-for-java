// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.compute.batch;

import com.azure.compute.batch.models.*;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestMode;
import com.azure.core.util.BinaryData;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.concurrent.TimeoutException;

public class FileTests extends BatchClientTestBase {
    private static String poolId;
    private static BatchPool livePool;

    @Override
    protected void beforeTest() {
        super.beforeTest();
        poolId = getStringIdWithUserNamePrefix("-testpool");
        if (getTestMode() == TestMode.RECORD) {
            try {
                livePool = createIfNotExistIaaSPool(poolId);
            } catch (Exception e) {
                // TODO (catch): Auto-generated catch block
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
        int taskCompleteTimeoutInSeconds = 60; // 60 seconds timeout

        try {
            BatchPoolInfo poolInfo = new BatchPoolInfo();
            poolInfo.setPoolId(poolId);

            batchClient.createJob(new BatchJobCreateContent(jobId, poolInfo));

            BatchTaskCreateContent taskToCreate = new BatchTaskCreateContent(taskId, "/bin/bash -c \"echo hello\"");

            batchClient.createTask(jobId, taskToCreate);

            if (waitForTasksToComplete(batchClient, jobId, taskCompleteTimeoutInSeconds)) {
                PagedIterable<BatchNodeFile> filesIterable = batchClient.listTaskFiles(jobId, taskId);
                boolean found = false;
                for (BatchNodeFile f : filesIterable) {
                    if (f.getName().equals("stdout.txt")) {
                        found = true;
                        break;
                    }
                }
                Assertions.assertTrue(found);

                BinaryData binaryData = batchClient.getTaskFile(jobId, taskId, "stdout.txt");
                String fileContent = new String(binaryData.toBytes(), StandardCharsets.UTF_8);
                Assertions.assertEquals("hello\n", fileContent);

                binaryData = batchClientBuilder.buildAsyncClient().getTaskFileWithResponse(jobId, taskId, "stdout.txt", null).block().getValue();
                Assertions.assertEquals("hello\n", binaryData.toString());

                Response<Void> getFilePropertiesResponse = batchClient.getTaskFilePropertiesWithResponse(jobId, taskId, "stdout.txt", null);
                Assertions.assertEquals("6", getFilePropertiesResponse.getHeaders().getValue(HttpHeaderName.CONTENT_LENGTH));

            } else {
                throw new TimeoutException("Task did not complete within the specified timeout");
            }
        } finally {
            try {
                batchClient.deleteJob(jobId);
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
        int taskCompleteTimeoutInSeconds = 60; // 60 seconds timeout

        try {
            BatchPoolInfo poolInfo = new BatchPoolInfo();
            poolInfo.setPoolId(poolId);

            batchClient.createJob(new BatchJobCreateContent(jobId, poolInfo));
            BatchTaskCreateContent taskToCreate = new BatchTaskCreateContent(taskId, "/bin/bash -c \"echo hello\"");
            batchClient.createTask(jobId, taskToCreate);

            if (waitForTasksToComplete(batchClient, jobId, taskCompleteTimeoutInSeconds)) {
                BatchTask task = batchClient.getTask(jobId, taskId);
                String nodeId = task.getNodeInfo().getNodeId();

                ListBatchNodeFilesOptions options = new ListBatchNodeFilesOptions();
                options.setRecursive(true);
                PagedIterable<BatchNodeFile> files = batchClient.listNodeFiles(poolId, nodeId, options);

                String fileName = null;
                for (BatchNodeFile f : files) {
                    if (f.getName().endsWith("stdout.txt")) {
                        fileName = f.getName();
                        break;
                    }
                }
                Assert.assertNotNull(fileName);

                BinaryData binaryData = batchClient.getNodeFileWithResponse(poolId, nodeId, fileName, null).getValue();
                String fileContent = new String(binaryData.toBytes(), StandardCharsets.UTF_8);
                Assert.assertEquals("hello\n", fileContent);

                binaryData = batchClientBuilder.buildAsyncClient().getNodeFileWithResponse(poolId, nodeId, fileName, null).block().getValue();
                Assertions.assertEquals("hello\n", binaryData.toString());

                FileResponseHeaderProperties fileProperties = batchClient.getNodeFileProperties(poolId, nodeId, fileName);
                Assertions.assertEquals(6, fileProperties.getContentLength());

            } else {
                throw new TimeoutException("Task did not complete within the specified timeout");
            }
        } finally {
            try {
                batchClient.deleteJob(jobId);
            } catch (Exception e) {
                // Ignore here
            }
        }
    }

    @Test
    public void testDeserializationOfFileProperties() throws IOException {
        String jsonResponse = "{"
            + "\"lastModified\":\"2022-01-01T00:00:00Z\","
            + "\"contentLength\":\"1024\","
            + "\"creationTime\":\"2022-01-01T01:00:00Z\","
            + "\"contentType\":\"application/json\","
            + "\"fileMode\":\"rw-r--r--\""
            + "}";

        try (JsonReader jsonReader = JsonProviders.createReader(new StringReader(jsonResponse))) {
            FileProperties fileProperties = FileProperties.fromJson(jsonReader);

            Assertions.assertNotNull(fileProperties);
            Assertions.assertEquals(OffsetDateTime.parse("2022-01-01T00:00:00Z"), fileProperties.getLastModified());
            Assertions.assertEquals(1024, fileProperties.getContentLength());
            Assertions.assertEquals(OffsetDateTime.parse("2022-01-01T01:00:00Z"), fileProperties.getCreationTime());
            Assertions.assertEquals("application/json", fileProperties.getContentType());
            Assertions.assertEquals("rw-r--r--", fileProperties.getFileMode());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
