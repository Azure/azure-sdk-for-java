// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.compute.batch;

import com.azure.compute.batch.models.BatchFileProperties;
import com.azure.compute.batch.models.BatchJob;
import com.azure.compute.batch.models.BatchJobCreateParameters;
import com.azure.compute.batch.models.BatchNodeFile;
import com.azure.compute.batch.models.BatchPool;
import com.azure.compute.batch.models.BatchPoolInfo;
import com.azure.compute.batch.models.BatchTask;
import com.azure.compute.batch.models.BatchTaskCreateParameters;
import com.azure.compute.batch.models.FileProperties;
import com.azure.compute.batch.models.BatchNodeFilesListOptions;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.rest.Response;
import com.azure.core.test.SyncAsyncExtension;
import com.azure.core.test.annotation.SyncAsyncTest;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.SyncPoller;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;

import reactor.core.publisher.Mono;

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
        try {
            livePool = createIfNotExistIaaSPool(poolId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Assertions.assertNotNull(livePool);
    }

    @SyncAsyncTest
    public void canReadFromTaskFile() throws Exception {
        // Generate a jobId that is unique per test mode (sync vs async)
        String testModeSuffix = SyncAsyncExtension.execute(() -> "sync", () -> Mono.just("async"));
        String jobId = getStringIdWithUserNamePrefix("-Job-canReadFromTaskFile-" + testModeSuffix);
        String taskId = "mytask";
        int taskCompleteTimeoutInSeconds = 60; // 60 seconds timeout

        try {
            BatchPoolInfo poolInfo = new BatchPoolInfo();
            poolInfo.setPoolId(poolId);

            // Create job
            SyncAsyncExtension.execute(() -> batchClient.createJob(new BatchJobCreateParameters(jobId, poolInfo)),
                () -> batchAsyncClient.createJob(new BatchJobCreateParameters(jobId, poolInfo)));

            // Create task
            BatchTaskCreateParameters taskToCreate
                = new BatchTaskCreateParameters(taskId, "/bin/bash -c \"echo hello\"");

            SyncAsyncExtension.execute(() -> batchClient.createTask(jobId, taskToCreate),
                () -> batchAsyncClient.createTask(jobId, taskToCreate));

            // Use matching client for wait logic
            boolean completed = SyncAsyncExtension
                .execute(() -> waitForTasksToComplete(batchClient, jobId, taskCompleteTimeoutInSeconds), () -> Mono
                    .fromCallable(() -> waitForTasksToComplete(batchAsyncClient, jobId, taskCompleteTimeoutInSeconds)));

            if (completed) {
                // List task files
                Iterable<BatchNodeFile> filesIterable
                    = SyncAsyncExtension.execute(() -> batchClient.listTaskFiles(jobId, taskId),
                        () -> Mono.fromCallable(() -> batchAsyncClient.listTaskFiles(jobId, taskId).toIterable()));

                boolean found = false;
                for (BatchNodeFile f : filesIterable) {
                    if (f.getName().equals("stdout.txt")) {
                        found = true;
                        break;
                    }
                }
                Assertions.assertTrue(found);

                // Get task file content
                BinaryData binaryData
                    = SyncAsyncExtension.execute(() -> batchClient.getTaskFile(jobId, taskId, "stdout.txt"),
                        () -> batchAsyncClient.getTaskFile(jobId, taskId, "stdout.txt"));

                String fileContent = new String(binaryData.toBytes(), StandardCharsets.UTF_8);
                Assertions.assertEquals("hello\n", fileContent);

                // Get task file properties
                Response<Void> getFilePropertiesResponse = SyncAsyncExtension.execute(
                    () -> batchClient.getTaskFilePropertiesWithResponse(jobId, taskId, "stdout.txt", null),
                    () -> batchAsyncClient.getTaskFilePropertiesWithResponse(jobId, taskId, "stdout.txt", null));

                Assertions.assertEquals("6",
                    getFilePropertiesResponse.getHeaders().getValue(HttpHeaderName.CONTENT_LENGTH));
            } else {
                throw new TimeoutException("Task did not complete within the specified timeout");
            }

        } finally {
            // DELETE
            try {
                SyncPoller<BatchJob, Void> deletePoller = setPlaybackSyncPollerPollInterval(
                    SyncAsyncExtension.execute(() -> batchClient.beginDeleteJob(jobId),
                        () -> Mono.fromCallable(() -> batchAsyncClient.beginDeleteJob(jobId).getSyncPoller())));

                deletePoller.waitForCompletion();
            } catch (Exception e) {
                System.err.println("Cleanup failed for job: " + jobId);
                e.printStackTrace();
            }
        }
    }

    @SyncAsyncTest
    public void canReadFromNode() throws Exception {
        // Generate unique IDs to prevent test clashes
        String testModeSuffix = SyncAsyncExtension.execute(() -> "sync", () -> Mono.just("async"));
        String jobId = getStringIdWithUserNamePrefix("-Job-canReadFromNode-" + testModeSuffix);
        String taskId = "mytask-";
        int taskCompleteTimeoutInSeconds = 60; // 60 seconds timeout

        try {
            BatchPoolInfo poolInfo = new BatchPoolInfo();
            poolInfo.setPoolId(poolId);

            // Create job
            SyncAsyncExtension.execute(() -> batchClient.createJob(new BatchJobCreateParameters(jobId, poolInfo)),
                () -> batchAsyncClient.createJob(new BatchJobCreateParameters(jobId, poolInfo)));

            // Create task
            BatchTaskCreateParameters taskToCreate
                = new BatchTaskCreateParameters(taskId, "/bin/bash -c \"echo hello\"");

            SyncAsyncExtension.execute(() -> batchClient.createTask(jobId, taskToCreate),
                () -> batchAsyncClient.createTask(jobId, taskToCreate));

            // Wait for task completion
            boolean completed = SyncAsyncExtension
                .execute(() -> waitForTasksToComplete(batchClient, jobId, taskCompleteTimeoutInSeconds), () -> Mono
                    .fromCallable(() -> waitForTasksToComplete(batchAsyncClient, jobId, taskCompleteTimeoutInSeconds)));

            if (completed) {
                // Get task
                BatchTask task = SyncAsyncExtension.execute(() -> batchClient.getTask(jobId, taskId),
                    () -> batchAsyncClient.getTask(jobId, taskId));

                String nodeId = task.getNodeInfo().getNodeId();

                // List node files
                BatchNodeFilesListOptions options = new BatchNodeFilesListOptions();
                options.setRecursive(true);

                Iterable<BatchNodeFile> files
                    = SyncAsyncExtension.execute(() -> batchClient.listNodeFiles(poolId, nodeId, options), () -> Mono
                        .fromCallable(() -> batchAsyncClient.listNodeFiles(poolId, nodeId, options).toIterable()));

                String fileName = null;
                for (BatchNodeFile f : files) {
                    if (f.getName().endsWith("stdout.txt")) {
                        fileName = f.getName();
                        break;
                    }
                }
                Assertions.assertNotNull(fileName);

                final String finalFileName = fileName;

                // Get node file content
                BinaryData binaryData = SyncAsyncExtension.execute(
                    () -> batchClient.getNodeFileWithResponse(poolId, nodeId, finalFileName, null).getValue(),
                    () -> Mono.fromCallable(
                        () -> batchAsyncClient.getNodeFileWithResponse(poolId, nodeId, finalFileName, null)
                            .block()
                            .getValue()));

                String fileContent = new String(binaryData.toBytes(), StandardCharsets.UTF_8);
                Assertions.assertEquals("hello\n", fileContent);

                // Get node file properties
                BatchFileProperties fileProperties
                    = SyncAsyncExtension.execute(() -> batchClient.getNodeFileProperties(poolId, nodeId, finalFileName),
                        () -> batchAsyncClient.getNodeFileProperties(poolId, nodeId, finalFileName));

                Assertions.assertEquals(6, fileProperties.getContentLength());

            } else {
                throw new TimeoutException("Task did not complete within the specified timeout");
            }

        } finally {
            // DELETE
            try {
                SyncPoller<BatchJob, Void> deletePoller = setPlaybackSyncPollerPollInterval(
                    SyncAsyncExtension.execute(() -> batchClient.beginDeleteJob(jobId),
                        () -> Mono.fromCallable(() -> batchAsyncClient.beginDeleteJob(jobId).getSyncPoller())));

                deletePoller.waitForCompletion();
            } catch (Exception e) {
                System.err.println("Cleanup failed for job: " + jobId);
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testDeserializationOfFileProperties() {
        String jsonResponse = "{" + "\"lastModified\":\"2022-01-01T00:00:00Z\"," + "\"contentLength\":\"1024\","
            + "\"creationTime\":\"2022-01-01T01:00:00Z\"," + "\"contentType\":\"application/json\","
            + "\"fileMode\":\"rw-r--r--\"" + "}";

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
