// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.compute.batch;

import com.azure.compute.batch.models.BatchApplicationPackageReference;
import com.azure.compute.batch.models.BatchErrorException;
import com.azure.compute.batch.models.BatchErrorSourceCategory;
import com.azure.compute.batch.models.BatchJob;
import com.azure.compute.batch.models.BatchJobCreateParameters;
import com.azure.compute.batch.models.BatchPoolInfo;
import com.azure.compute.batch.models.BatchTask;
import com.azure.compute.batch.models.BatchTaskBulkCreateOptions;
import com.azure.compute.batch.models.BatchTaskConstraints;
import com.azure.compute.batch.models.BatchTaskCounts;
import com.azure.compute.batch.models.BatchTaskCountsResult;
import com.azure.compute.batch.models.BatchTaskCreateParameters;
import com.azure.compute.batch.models.BatchTaskExecutionResult;
import com.azure.compute.batch.models.BatchTaskSlotCounts;
import com.azure.compute.batch.models.BatchTaskStatistics;
import com.azure.compute.batch.models.OutputFile;
import com.azure.compute.batch.models.OutputFileBlobContainerDestination;
import com.azure.compute.batch.models.OutputFileDestination;
import com.azure.compute.batch.models.OutputFileUploadCondition;
import com.azure.compute.batch.models.OutputFileUploadConfig;
import com.azure.compute.batch.models.ResourceFile;
import com.azure.compute.batch.models.UploadBatchServiceLogsParameters;
import com.azure.compute.batch.models.UploadBatchServiceLogsResult;
import com.azure.compute.batch.models.UserIdentity;
import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.core.test.SyncAsyncExtension;
import com.azure.core.test.TestMode;
import com.azure.core.test.annotation.SyncAsyncTest;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.core.util.polling.SyncPoller;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.storage.blob.BlobContainerClient;

import reactor.core.publisher.Mono;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class TaskTests extends BatchClientTestBase {
    private static String livePoolId;
    private static String liveIaasPoolId;

    @Override
    protected void beforeTest() {
        super.beforeTest();
        livePoolId = getStringIdWithUserNamePrefix("-testpool");
        liveIaasPoolId = getStringIdWithUserNamePrefix("-testIaaSpool");
        try {
            createIfNotExistIaaSPool(livePoolId);
            createIfNotExistIaaSPool(liveIaasPoolId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
    * Test TypeSpec Shared model among GET-PUT Roundtrip operation
    * */
    @SyncAsyncTest
    public void testTaskUnifiedModel() {
        String testModeSuffix = SyncAsyncExtension.execute(() -> "sync", () -> Mono.just("async"));
        String taskId = "task-canPut" + testModeSuffix;
        String jobId = getStringIdWithUserNamePrefix("-SampleJob" + testModeSuffix);
        try {
            //CREATE JOB
            BatchPoolInfo poolInfo = new BatchPoolInfo();
            poolInfo.setPoolId(livePoolId);
            BatchJobCreateParameters jobToCreate = new BatchJobCreateParameters(jobId, poolInfo);
            SyncAsyncExtension.execute(() -> batchClient.createJob(jobToCreate),
                () -> batchAsyncClient.createJob(jobToCreate));

            //CREATE TASK
            BatchTaskCreateParameters taskToCreate = new BatchTaskCreateParameters(taskId, "echo hello world");
            SyncAsyncExtension.execute(() -> batchClient.createTask(jobId, taskToCreate),
                () -> batchAsyncClient.createTask(jobId, taskToCreate));

            // GET
            BatchTask taskBeforeUpdate = SyncAsyncExtension.execute(() -> batchClient.getTask(jobId, taskId),
                () -> batchAsyncClient.getTask(jobId, taskId));

            //UPDATE
            Integer maxRetrycount = 5;
            Duration retentionPeriod = Duration.ofDays(5);
            taskBeforeUpdate.setConstraints(
                new BatchTaskConstraints().setMaxTaskRetryCount(maxRetrycount).setRetentionTime(retentionPeriod));

            SyncAsyncExtension.execute(() -> batchClient.replaceTask(jobId, taskId, taskBeforeUpdate),
                () -> batchAsyncClient.replaceTask(jobId, taskId, taskBeforeUpdate));

            //GET After UPDATE
            BatchTask taskAfterUpdate = SyncAsyncExtension.execute(() -> batchClient.getTask(jobId, taskId),
                () -> batchAsyncClient.getTask(jobId, taskId));

            Assertions.assertEquals(maxRetrycount, taskAfterUpdate.getConstraints().getMaxTaskRetryCount());
            Assertions.assertEquals(retentionPeriod, taskAfterUpdate.getConstraints().getRetentionTime());
        } finally {
            try {
                SyncAsyncExtension.execute(() -> batchClient.deleteTask(jobId, taskId),
                    () -> batchAsyncClient.deleteTask(jobId, taskId));
            } catch (Exception e) {
                System.err.println("Cleanup failed for task: " + taskId);
                e.printStackTrace();
            }

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
    public void testJobUser() {
        String testModeSuffix = SyncAsyncExtension.execute(() -> "sync", () -> Mono.just("async"));
        String jobId = getStringIdWithUserNamePrefix("-testJobUser" + testModeSuffix);
        String taskId = "mytask" + testModeSuffix;

        BatchPoolInfo poolInfo = new BatchPoolInfo();
        poolInfo.setPoolId(livePoolId);
        BatchJobCreateParameters jobToCreate = new BatchJobCreateParameters(jobId, poolInfo);

        SyncAsyncExtension.execute(() -> batchClient.createJob(jobToCreate),
            () -> batchAsyncClient.createJob(jobToCreate));

        try {
            // CREATE
            List<BatchApplicationPackageReference> apps = new ArrayList<>();
            apps.add(new BatchApplicationPackageReference("MSMPI"));
            BatchTaskCreateParameters taskToCreate = new BatchTaskCreateParameters(taskId, "echo hello\"")
                .setUserIdentity(new UserIdentity().setUsername("test-user"))
                .setApplicationPackageReferences(apps);

            SyncAsyncExtension.execute(() -> batchClient.createTask(jobId, taskToCreate),
                () -> batchAsyncClient.createTask(jobId, taskToCreate));

            // GET
            BatchTask task = SyncAsyncExtension.execute(() -> batchClient.getTask(jobId, taskId),
                () -> batchAsyncClient.getTask(jobId, taskId));
            Assertions.assertNotNull(task);
            Assertions.assertEquals(taskId, task.getId());
            Assertions.assertEquals("test-user", task.getUserIdentity().getUsername());

            //Recording file automatically sanitizes Application Id - Only verify App Id in Record Mode
            if (getTestMode() == TestMode.RECORD) {
                Assertions.assertEquals("msmpi", task.getApplicationPackageReferences().get(0).getApplicationId());
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
    public void canCRUDTest() throws Exception {
        String testModeSuffix = SyncAsyncExtension.execute(() -> "sync", () -> Mono.just("async"));
        int taskCompleteTimeoutInSeconds = 60; // 60 seconds timeout
        String standardConsoleOutputFilename = "stdout.txt";
        String blobFileName = "test.txt";
        String taskId = "mytask" + testModeSuffix;
        File temp = File.createTempFile("tempFile", ".tmp");
        BufferedWriter bw = new BufferedWriter(new FileWriter(temp));
        bw.write("This is an example");
        bw.close();
        temp.deleteOnExit();
        String jobId = getStringIdWithUserNamePrefix("-canCRUDTest" + testModeSuffix);

        BatchPoolInfo poolInfo = new BatchPoolInfo();
        poolInfo.setPoolId(liveIaasPoolId);
        BatchJobCreateParameters parameters = new BatchJobCreateParameters(jobId, poolInfo);
        SyncAsyncExtension.execute(() -> batchClient.createJob(parameters),
            () -> batchAsyncClient.createJob(parameters));

        String storageAccountName = Configuration.getGlobalConfiguration().get("STORAGE_ACCOUNT_NAME");
        String storageAccountKey = Configuration.getGlobalConfiguration().get("STORAGE_ACCOUNT_KEY");
        BlobContainerClient container = null;

        try {
            String sas = "";

            //The Storage operations run only in Record mode.
            // Playback mode is configured to test Batch operations only.
            if (getTestMode() != TestMode.PLAYBACK) {
                // Create storage container
                String containerName = "testingtaskcreate" + testModeSuffix;
                container = createBlobContainer(storageAccountName, storageAccountKey, containerName);
                sas = uploadFileToCloud(container, blobFileName, temp.getAbsolutePath());
            } else {
                sas = redacted;
            }

            // Associate resource file with task
            ResourceFile file = new ResourceFile();
            file.setFilePath(blobFileName);
            file.setHttpUrl(sas);
            List<ResourceFile> files = new ArrayList<>();
            files.add(file);

            // CREATE
            BatchTaskCreateParameters taskToCreate
                = new BatchTaskCreateParameters(taskId, String.format("type %s", blobFileName)).setResourceFiles(files);
            SyncAsyncExtension.execute(() -> batchClient.createTask(jobId, taskToCreate),
                () -> batchAsyncClient.createTask(jobId, taskToCreate));

            // GET
            BatchTask taskBeforeUpdate = SyncAsyncExtension.execute(() -> batchClient.getTask(jobId, taskId),
                () -> batchAsyncClient.getTask(jobId, taskId));
            Assertions.assertNotNull(taskBeforeUpdate);
            Assertions.assertEquals(taskId, taskBeforeUpdate.getId());

            // Verify default retention time
            Assertions.assertEquals(Duration.ofDays(7), taskBeforeUpdate.getConstraints().getRetentionTime());

            // UPDATE
            taskBeforeUpdate.setConstraints(new BatchTaskConstraints().setMaxTaskRetryCount(5));
            SyncAsyncExtension.execute(() -> batchClient.replaceTask(jobId, taskId, taskBeforeUpdate),
                () -> batchAsyncClient.replaceTask(jobId, taskId, taskBeforeUpdate));

            BatchTask taskAfterUpdate = SyncAsyncExtension.execute(() -> batchClient.getTask(jobId, taskId),
                () -> batchAsyncClient.getTask(jobId, taskId));

            Assertions.assertEquals((Integer) 5, taskAfterUpdate.getConstraints().getMaxTaskRetryCount());

            // LIST
            Iterable<BatchTask> tasks = SyncAsyncExtension.execute(() -> batchClient.listTasks(jobId),
                () -> Mono.fromCallable(() -> batchAsyncClient.listTasks(jobId).toIterable()));
            Assertions.assertNotNull(tasks);

            boolean found = false;
            for (BatchTask t : tasks) {
                if (t.getId().equals(taskId)) {
                    found = true;
                    break;
                }
            }

            Assertions.assertTrue(found);

            boolean completed = SyncAsyncExtension
                .execute(() -> waitForTasksToComplete(batchClient, jobId, taskCompleteTimeoutInSeconds), () -> Mono
                    .fromCallable(() -> waitForTasksToComplete(batchAsyncClient, jobId, taskCompleteTimeoutInSeconds)));

            if (completed) {
                // Get the task command output file
                BatchTask taskAfterUpdate2 = SyncAsyncExtension.execute(() -> batchClient.getTask(jobId, taskId),
                    () -> batchAsyncClient.getTask(jobId, taskId));

                BinaryData binaryData = SyncAsyncExtension.execute(
                    () -> batchClient.getTaskFile(jobId, taskId, standardConsoleOutputFilename),
                    () -> batchAsyncClient.getTaskFile(jobId, taskId, standardConsoleOutputFilename));

                String fileContent = new String(binaryData.toBytes(), StandardCharsets.UTF_8);
                Assertions.assertEquals("This is an example", fileContent);

                String outputSas = "";

                //The Storage operations run only in Record mode.
                // Playback mode is configured to test Batch operations only.
                if (getTestMode() != TestMode.PLAYBACK) {
                    outputSas = generateContainerSasToken(container);
                } else {
                    outputSas = redacted;
                }
                // UPLOAD LOG
                UploadBatchServiceLogsParameters logsParameters
                    = new UploadBatchServiceLogsParameters(outputSas, OffsetDateTime.now().minusMinutes(-10));
                UploadBatchServiceLogsResult uploadBatchServiceLogsResult = SyncAsyncExtension.execute(
                    () -> batchClient.uploadNodeLogs(liveIaasPoolId, taskAfterUpdate2.getNodeInfo().getNodeId(),
                        logsParameters),
                    () -> batchAsyncClient.uploadNodeLogs(liveIaasPoolId, taskAfterUpdate2.getNodeInfo().getNodeId(),
                        logsParameters));

                Assertions.assertNotNull(uploadBatchServiceLogsResult);
                Assertions.assertTrue(uploadBatchServiceLogsResult.getNumberOfFilesUploaded() > 0);
                Assertions.assertTrue(uploadBatchServiceLogsResult.getVirtualDirectoryName()
                    .toLowerCase()
                    .contains(liveIaasPoolId.toLowerCase()));
            }

            // DELETE
            SyncAsyncExtension.execute(() -> batchClient.deleteTask(jobId, taskId),
                () -> batchAsyncClient.deleteTask(jobId, taskId));
            try {
                SyncAsyncExtension.execute(() -> batchClient.getTask(jobId, taskId),
                    () -> batchAsyncClient.getTask(jobId, taskId));
                Assertions.assertTrue(true, "Shouldn't be here, the job should be deleted");
            } catch (Exception e) {
                if (!e.getMessage().contains("Status code 404")) {
                    throw e;
                }
            }
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                SyncPoller<BatchJob, Void> deletePoller = setPlaybackSyncPollerPollInterval(
                    SyncAsyncExtension.execute(() -> batchClient.beginDeleteJob(jobId),
                        () -> Mono.fromCallable(() -> batchAsyncClient.beginDeleteJob(jobId).getSyncPoller())));

                deletePoller.waitForCompletion();
            } catch (Exception e) {
                System.err.println("Cleanup failed for job: " + jobId);
                e.printStackTrace();
            }
            try {
                container.deleteIfExists();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                SyncAsyncExtension.execute(() -> batchClient.deletePool(liveIaasPoolId),
                    () -> batchAsyncClient.deletePool(liveIaasPoolId));
            } catch (Exception e) {
                System.err.println("Cleanup failed for pool: " + liveIaasPoolId);
                e.printStackTrace();
            }
        }
    }

    @SyncAsyncTest
    public void testAddMultiTasks() {
        String testModeSuffix = SyncAsyncExtension.execute(() -> "sync", () -> Mono.just("async"));
        String jobId = getStringIdWithUserNamePrefix("-testAddMultiTasks" + testModeSuffix);

        BatchPoolInfo poolInfo = new BatchPoolInfo();
        poolInfo.setPoolId(livePoolId);
        BatchJobCreateParameters parameters = new BatchJobCreateParameters(jobId, poolInfo);
        SyncAsyncExtension.execute(() -> batchClient.createJob(parameters),
            () -> batchAsyncClient.createJob(parameters));

        int taskCount = 1000;

        try {
            // CREATE
            List<BatchTaskCreateParameters> tasksToAdd = new ArrayList<>();
            for (int i = 0; i < taskCount; i++) {
                BatchTaskCreateParameters taskCreateParameters = new BatchTaskCreateParameters(
                    String.format("mytask%d", i) + testModeSuffix, String.format("echo hello %d", i));
                tasksToAdd.add(taskCreateParameters);
            }
            BatchTaskBulkCreateOptions option = new BatchTaskBulkCreateOptions();
            option.setMaxConcurrency(10);
            SyncAsyncExtension.execute(() -> batchClient.createTasks(jobId, tasksToAdd, option),
                () -> batchAsyncClient.createTasks(jobId, tasksToAdd, option));

            // Wait to ensure all tasks are visible in listTasks()
            sleepIfRunningAgainstService(15000);

            // LIST
            Iterable<BatchTask> tasks = SyncAsyncExtension.execute(() -> batchClient.listTasks(jobId),
                () -> Mono.fromCallable(() -> batchAsyncClient.listTasks(jobId).toIterable()));
            Assertions.assertNotNull(tasks);
            int taskListCount = 0;
            for (BatchTask task : tasks) {
                ++taskListCount;
            }
            Assertions.assertEquals(taskListCount, taskCount);
        } finally {
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
    public void testAddMultiTasksWithError() {
        String testModeSuffix = SyncAsyncExtension.execute(() -> "sync", () -> Mono.just("async"));
        String accessKey = Configuration.getGlobalConfiguration().get("AZURE_BATCH_ACCESS_KEY");
        accessKey = (accessKey == null || accessKey.isEmpty()) ? "RANDOM_KEY" : accessKey;

        AzureNamedKeyCredential noExistCredentials1 = new AzureNamedKeyCredential("noexistaccount", accessKey);
        batchClientBuilder.credential(noExistCredentials1);

        batchClient = batchClientBuilder.buildClient();
        batchAsyncClient = batchClientBuilder.buildAsyncClient();

        String jobId = getStringIdWithUserNamePrefix("-testAddMultiTasksWithError" + testModeSuffix);
        int taskCount = 1000;

        try {
            // CREATE
            List<BatchTaskCreateParameters> tasksToAdd = new ArrayList<>();
            for (int i = 0; i < taskCount; i++) {
                BatchTaskCreateParameters taskCreateParameters = new BatchTaskCreateParameters(
                    String.format("mytask%d", i) + testModeSuffix, String.format("echo hello %d", i));
                tasksToAdd.add(taskCreateParameters);
            }
            BatchTaskBulkCreateOptions option = new BatchTaskBulkCreateOptions();
            option.setMaxConcurrency(10);
            SyncAsyncExtension.execute(() -> batchClient.createTasks(jobId, tasksToAdd, option),
                () -> batchAsyncClient.createTasks(jobId, tasksToAdd, option));

            Assertions.assertTrue(true, "Should not here");
        } catch (RuntimeException ex) {
            System.out.printf("Expect exception %s", ex.toString());
        }
    }

    @Test
    public void failIfPoisonTaskTooLargeSync() throws Exception {
        //This test will temporarily only run in Live/Record mode. It runs fine in Playback mode too on Mac and Windows machines.
        // Linux machines are causing issues. This issue is under investigation.
        Assumptions.assumeFalse(getTestMode() == TestMode.PLAYBACK, "This Test only runs in Live/Record mode");

        String jobId = getStringIdWithUserNamePrefix("-failIfPoisonTaskTooLarge-sync");
        String taskId = "mytask-sync";

        BatchPoolInfo poolInfo = new BatchPoolInfo();
        poolInfo.setPoolId(liveIaasPoolId);
        BatchJobCreateParameters parameters = new BatchJobCreateParameters(jobId, poolInfo);
        batchClient.createJob(parameters);

        List<BatchTaskCreateParameters> tasksToAdd = new ArrayList<BatchTaskCreateParameters>();
        BatchTaskCreateParameters taskToAdd = new BatchTaskCreateParameters(taskId, "sleep 1");
        List<ResourceFile> resourceFiles = new ArrayList<ResourceFile>();
        ResourceFile resourceFile;

        // If this test fails try increasing the size of the Task in case maximum size increase
        for (int i = 0; i < 10000; i++) {
            resourceFile
                = new ResourceFile().setHttpUrl("https://mystorageaccount.blob.core.windows.net/files/resourceFile" + i)
                    .setFilePath("resourceFile" + i);
            resourceFiles.add(resourceFile);
        }

        taskToAdd.setResourceFiles(resourceFiles);
        tasksToAdd.add(taskToAdd);

        try {
            batchClient.createTasks(jobId, tasksToAdd);
            try {
                SyncPoller<BatchJob, Void> deletePoller
                    = setPlaybackSyncPollerPollInterval(batchClient.beginDeleteJob(jobId));
                deletePoller.waitForCompletion();
            } catch (Exception e) {
                System.err.println("Cleanup failed for job: " + jobId);
                e.printStackTrace();
            }
            Assertions.fail("Expected RequestBodyTooLarge error");
        } catch (BatchErrorException err) {
            // DELETE
            try {
                SyncPoller<BatchJob, Void> deletePoller
                    = setPlaybackSyncPollerPollInterval(batchClient.beginDeleteJob(jobId));

                deletePoller.waitForCompletion();
            } catch (Exception e) {
                System.err.println("Cleanup failed for job: " + jobId);
                e.printStackTrace();
            }
            Assertions.assertEquals(413, err.getResponse().getStatusCode());
        } catch (Exception err) {
            // DELETE
            try {
                SyncPoller<BatchJob, Void> deletePoller
                    = setPlaybackSyncPollerPollInterval(batchClient.beginDeleteJob(jobId));

                deletePoller.waitForCompletion();
            } catch (Exception e) {
                System.err.println("Cleanup failed for job: " + jobId);
                e.printStackTrace();
            }
            Assertions.fail("Expected RequestBodyTooLarge error");
        }
    }

    @Test
    public void failIfPoisonTaskTooLargeAsync() throws Exception {
        //This test will temporarily only run in Live/Record mode. It runs fine in Playback mode too on Mac and Windows machines.
        // Linux machines are causing issues. This issue is under investigation.
        Assumptions.assumeFalse(getTestMode() == TestMode.PLAYBACK, "This Test only runs in Live/Record mode");

        String jobId = getStringIdWithUserNamePrefix("-failIfPoisonTaskTooLarge-async");
        String taskId = "mytask-async";

        BatchPoolInfo poolInfo = new BatchPoolInfo();
        poolInfo.setPoolId(liveIaasPoolId);
        BatchJobCreateParameters parameters = new BatchJobCreateParameters(jobId, poolInfo);
        batchAsyncClient.createJob(parameters).block();

        List<BatchTaskCreateParameters> tasksToAdd = new ArrayList<BatchTaskCreateParameters>();
        BatchTaskCreateParameters taskToAdd = new BatchTaskCreateParameters(taskId, "sleep 1");
        List<ResourceFile> resourceFiles = new ArrayList<ResourceFile>();
        ResourceFile resourceFile;

        // If this test fails try increasing the size of the Task in case maximum size increase
        for (int i = 0; i < 10000; i++) {
            resourceFile
                = new ResourceFile().setHttpUrl("https://mystorageaccount.blob.core.windows.net/files/resourceFile" + i)
                    .setFilePath("resourceFile" + i);
            resourceFiles.add(resourceFile);
        }

        taskToAdd.setResourceFiles(resourceFiles);
        tasksToAdd.add(taskToAdd);

        try {
            batchAsyncClient.createTasks(jobId, tasksToAdd).block();

            try {
                SyncPoller<BatchJob, Void> deletePoller
                    = setPlaybackSyncPollerPollInterval(batchAsyncClient.beginDeleteJob(jobId).getSyncPoller());
                deletePoller.waitForCompletion();
            } catch (Exception e) {
                System.err.println("Cleanup failed for job: " + jobId);
                e.printStackTrace();
            }
            Assertions.fail("Expected RequestBodyTooLarge error");
        } catch (BatchErrorException err) {
            // DELETE
            try {
                SyncPoller<BatchJob, Void> deletePoller
                    = setPlaybackSyncPollerPollInterval(batchAsyncClient.beginDeleteJob(jobId).getSyncPoller());

                deletePoller.waitForCompletion();
            } catch (Exception e) {
                System.err.println("Cleanup failed for job: " + jobId);
                e.printStackTrace();
            }
            Assertions.assertEquals(413, err.getResponse().getStatusCode());
        } catch (Exception err) {
            Assertions.fail("Expected RequestBodyTooLarge error");
        }
    }

    @Test
    public void succeedWithRetrySync() {
        //This test does not run in Playback mode. It only runs in Record/Live mode.
        // This test uses multi threading. Playing back the test doesn't match its recorded sequence always.
        // Hence Playback of this test is disabled.
        Assumptions.assumeFalse(getTestMode() == TestMode.PLAYBACK, "This Test only runs in Live/Record mode");

        String jobId = getStringIdWithUserNamePrefix("-succeedWithRetry-sync");
        String taskId = "mytask-sync";

        BatchPoolInfo poolInfo = new BatchPoolInfo();
        poolInfo.setPoolId(liveIaasPoolId);
        BatchJobCreateParameters jobToCreate = new BatchJobCreateParameters(jobId, poolInfo);
        batchClient.createJob(jobToCreate);

        List<BatchTaskCreateParameters> tasksToAdd = new ArrayList<BatchTaskCreateParameters>();
        BatchTaskCreateParameters taskToAdd;
        List<ResourceFile> resourceFiles = new ArrayList<ResourceFile>();
        ResourceFile resourceFile;

        BatchTaskBulkCreateOptions option = new BatchTaskBulkCreateOptions();
        option.setMaxConcurrency(6);

        // Num Resource Files * Max Chunk Size should be greater than or equal to the limit which triggers the PoisonTask test to ensure we encounter the error in the initial chunk.
        for (int i = 0; i < 80; i++) {
            resourceFile
                = new ResourceFile().setHttpUrl("https://mystorageaccount.blob.core.windows.net/files/resourceFile" + i)
                    .setFilePath("resourceFile" + i);
            resourceFiles.add(resourceFile);
        }
        // Num tasks to add
        for (int i = 0; i < 600; i++) {
            taskToAdd = new BatchTaskCreateParameters(taskId + i, "sleep 1");
            taskToAdd.setResourceFiles(resourceFiles);
            tasksToAdd.add(taskToAdd);
        }

        try {
            batchClient.createTasks(jobId, tasksToAdd, option);
            try {
                SyncPoller<BatchJob, Void> deletePoller
                    = setPlaybackSyncPollerPollInterval(batchClient.beginDeleteJob(jobId));
                deletePoller.waitForCompletion();
            } catch (Exception e) {
                System.err.println("Cleanup failed for job: " + jobId);
                e.printStackTrace();
            }
        } catch (Exception err) {
            Assertions.fail("Expected Success");
        }
    }

    @Test
    public void succeedWithRetryAsync() {
        //This test does not run in Playback mode. It only runs in Record/Live mode.
        // This test uses multi threading. Playing back the test doesn't match its recorded sequence always.
        // Hence Playback of this test is disabled.
        Assumptions.assumeFalse(getTestMode() == TestMode.PLAYBACK, "This Test only runs in Live/Record mode");

        String jobId = getStringIdWithUserNamePrefix("-succeedWithRetry-async");
        String taskId = "mytask-async";

        BatchPoolInfo poolInfo = new BatchPoolInfo();
        poolInfo.setPoolId(liveIaasPoolId);
        BatchJobCreateParameters jobToCreate = new BatchJobCreateParameters(jobId, poolInfo);
        batchAsyncClient.createJob(jobToCreate).block();

        List<BatchTaskCreateParameters> tasksToAdd = new ArrayList<BatchTaskCreateParameters>();
        BatchTaskCreateParameters taskToAdd;
        List<ResourceFile> resourceFiles = new ArrayList<ResourceFile>();
        ResourceFile resourceFile;

        BatchTaskBulkCreateOptions option = new BatchTaskBulkCreateOptions();
        option.setMaxConcurrency(6);

        // Num Resource Files * Max Chunk Size should be greater than or equal to the limit which triggers the PoisonTask test to ensure we encounter the error in the initial chunk.
        for (int i = 0; i < 80; i++) {
            resourceFile
                = new ResourceFile().setHttpUrl("https://mystorageaccount.blob.core.windows.net/files/resourceFile" + i)
                    .setFilePath("resourceFile" + i);
            resourceFiles.add(resourceFile);
        }
        // Num tasks to add
        for (int i = 0; i < 800; i++) {
            taskToAdd = new BatchTaskCreateParameters(taskId + i, "sleep 1");
            taskToAdd.setResourceFiles(resourceFiles);
            tasksToAdd.add(taskToAdd);
        }

        try {
            batchAsyncClient.createTasks(jobId, tasksToAdd, option).block();
            try {
                SyncPoller<BatchJob, Void> deletePoller
                    = setPlaybackSyncPollerPollInterval(batchAsyncClient.beginDeleteJob(jobId).getSyncPoller());
                deletePoller.waitForCompletion();
            } catch (Exception e) {
                System.err.println("Cleanup failed for job: " + jobId);
                e.printStackTrace();
            }
        } catch (Exception err) {
            Assertions.fail("Expected Success");
        }
    }

    @SyncAsyncTest
    public void testGetTaskCounts() {
        String testModeSuffix = SyncAsyncExtension.execute(() -> "sync", () -> Mono.just("async"));
        String jobId = getStringIdWithUserNamePrefix("-testGetTaskCounts" + testModeSuffix);

        BatchPoolInfo poolInfo = new BatchPoolInfo();
        poolInfo.setPoolId(livePoolId);
        BatchJobCreateParameters jobToCreate = new BatchJobCreateParameters(jobId, poolInfo);
        SyncAsyncExtension.execute(() -> batchClient.createJob(jobToCreate),
            () -> batchAsyncClient.createJob(jobToCreate));

        int taskCount = 1000;

        try {
            // Test Job count
            BatchTaskCountsResult countResult = SyncAsyncExtension.execute(() -> batchClient.getJobTaskCounts(jobId),
                () -> batchAsyncClient.getJobTaskCounts(jobId));

            BatchTaskCounts counts = countResult.getTaskCounts();
            int all = counts.getActive() + counts.getCompleted() + counts.getRunning();
            Assertions.assertEquals(0, all);

            BatchTaskSlotCounts slotCounts = countResult.getTaskSlotCounts();
            int allSlots = slotCounts.getActive() + slotCounts.getCompleted() + slotCounts.getRunning();
            Assertions.assertEquals(0, allSlots);

            // CREATE
            List<BatchTaskCreateParameters> tasksToAdd = new ArrayList<>();
            for (int i = 0; i < taskCount; i++) {
                BatchTaskCreateParameters taskCreateParameters
                    = new BatchTaskCreateParameters(String.format("mytask%d", i), String.format("echo hello %d", i));
                tasksToAdd.add(taskCreateParameters);
            }
            BatchTaskBulkCreateOptions option = new BatchTaskBulkCreateOptions();
            option.setMaxConcurrency(10);
            SyncAsyncExtension.execute(() -> batchClient.createTasks(jobId, tasksToAdd, option),
                () -> batchAsyncClient.createTasks(jobId, tasksToAdd, option));

            //The Waiting period is only needed in record mode.
            sleepIfRunningAgainstService(30 * 1000);

            // Test Job count
            countResult = SyncAsyncExtension.execute(() -> batchClient.getJobTaskCounts(jobId),
                () -> batchAsyncClient.getJobTaskCounts(jobId));
            counts = countResult.getTaskCounts();
            all = counts.getActive() + counts.getCompleted() + counts.getRunning();
            Assertions.assertEquals(taskCount, all);

            slotCounts = countResult.getTaskSlotCounts();
            allSlots = slotCounts.getActive() + slotCounts.getCompleted() + slotCounts.getRunning();
            // One slot per task
            Assertions.assertEquals(taskCount, allSlots);
        } finally {
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
    public void testOutputFiles() {
        String testModeSuffix = SyncAsyncExtension.execute(() -> "sync", () -> Mono.just("async"));
        int taskCompleteTimeoutInSeconds = 60; // 60 seconds timeout
        String jobId = getStringIdWithUserNamePrefix("-testOutputFiles" + testModeSuffix);
        String taskId = "mytask" + testModeSuffix;
        String badTaskId = "mytask1" + testModeSuffix;
        String storageAccountName = System.getenv("STORAGE_ACCOUNT_NAME");
        String storageAccountKey = System.getenv("STORAGE_ACCOUNT_KEY");

        BatchPoolInfo poolInfo = new BatchPoolInfo();
        poolInfo.setPoolId(liveIaasPoolId);
        BatchJobCreateParameters jobToCreate = new BatchJobCreateParameters(jobId, poolInfo);
        SyncAsyncExtension.execute(() -> batchClient.createJob(jobToCreate),
            () -> batchAsyncClient.createJob(jobToCreate));
        BlobContainerClient containerClient = null;
        String containerUrl = "";

        //The Storage operations run only in Record mode.
        // Playback mode is configured to test Batch operations only.
        if (getTestMode() == TestMode.RECORD) {
            containerClient = createBlobContainer(storageAccountName, storageAccountKey, "output");
            containerUrl = generateContainerSasToken(containerClient);
        }

        try {
            // CREATE
            List<OutputFile> outputs = new ArrayList<>();
            OutputFileBlobContainerDestination fileBlobContainerDestination
                = new OutputFileBlobContainerDestination(containerUrl);
            fileBlobContainerDestination.setPath("taskLogs/output.txt");

            OutputFileDestination fileDestination = new OutputFileDestination();
            fileDestination.setContainer(fileBlobContainerDestination);

            outputs.add(new OutputFile("../stdout.txt", fileDestination,
                new OutputFileUploadConfig(OutputFileUploadCondition.TASK_COMPLETION)));

            OutputFileBlobContainerDestination fileBlobErrContainerDestination
                = new OutputFileBlobContainerDestination(containerUrl);
            fileBlobErrContainerDestination.setPath("taskLogs/err.txt");

            outputs.add(new OutputFile("../stderr.txt",
                new OutputFileDestination().setContainer(fileBlobErrContainerDestination),
                new OutputFileUploadConfig(OutputFileUploadCondition.TASK_FAILURE)));

            BatchTaskCreateParameters taskToCreate = new BatchTaskCreateParameters(taskId, "echo hello");
            taskToCreate.setOutputFiles(outputs);

            SyncAsyncExtension.execute(() -> batchClient.createTask(jobId, taskToCreate),
                () -> batchAsyncClient.createTask(jobId, taskToCreate));

            if (waitForTasksToComplete(batchClient, jobId, taskCompleteTimeoutInSeconds)) {
                BatchTask task = SyncAsyncExtension.execute(() -> batchClient.getTask(jobId, taskId),
                    () -> batchAsyncClient.getTask(jobId, taskId));
                Assertions.assertNotNull(task);
                Assertions.assertEquals(BatchTaskExecutionResult.SUCCESS, task.getExecutionInfo().getResult());
                Assertions.assertNull(task.getExecutionInfo().getFailureInfo());

                if (getTestMode() == TestMode.RECORD) {
                    // Get the task command output file
                    String result = getContentFromContainer(containerClient, "taskLogs/output.txt");
                    Assertions.assertEquals("hello", result.trim());
                }
            }

            BatchTaskCreateParameters badTask
                = new BatchTaskCreateParameters(badTaskId, "badcommand").setOutputFiles(outputs);

            SyncAsyncExtension.execute(() -> batchClient.createTask(jobId, badTask),
                () -> batchAsyncClient.createTask(jobId, badTask));

            if (waitForTasksToComplete(batchClient, jobId, taskCompleteTimeoutInSeconds)) {
                BatchTask task = SyncAsyncExtension.execute(() -> batchClient.getTask(jobId, badTaskId),
                    () -> batchAsyncClient.getTask(jobId, badTaskId));
                Assertions.assertNotNull(task);
                Assertions.assertEquals(BatchTaskExecutionResult.FAILURE, task.getExecutionInfo().getResult());
                Assertions.assertNotNull(task.getExecutionInfo().getFailureInfo());
                Assertions.assertEquals(BatchErrorSourceCategory.USER_ERROR.toString().toLowerCase(),
                    task.getExecutionInfo().getFailureInfo().getCategory().toString().toLowerCase());
                Assertions.assertEquals("FailureExitCode", task.getExecutionInfo().getFailureInfo().getCode());

                //The Storage operations run only in Record mode.
                // Playback mode is configured to test Batch operations only.
                if (getTestMode() == TestMode.RECORD) {
                    // Get the task command output file
                    String result = getContentFromContainer(containerClient, "taskLogs/err.txt");
                    Assertions.assertTrue(result.toLowerCase().contains("not recognized"));
                }
            }

        } finally {
            try {
                if (getTestMode() == TestMode.RECORD) {
                    containerClient.deleteIfExists();
                }
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
    public void testCreateTasks() {
        String testModeSuffix = SyncAsyncExtension.execute(() -> "sync", () -> Mono.just("async"));
        String jobId = getStringIdWithUserNamePrefix("-testCreateTasks" + testModeSuffix);
        BatchPoolInfo poolInfo = new BatchPoolInfo();
        poolInfo.setPoolId(livePoolId);
        BatchJobCreateParameters jobToCreate = new BatchJobCreateParameters(jobId, poolInfo);
        SyncAsyncExtension.execute(() -> batchClient.createJob(jobToCreate),
            () -> batchAsyncClient.createJob(jobToCreate));

        try {
            // Prepare tasks to add
            int taskCount = 10; // Adjust the number of tasks as needed
            List<BatchTaskCreateParameters> tasksToAdd = new ArrayList<>();
            for (int i = 0; i < taskCount; i++) {
                String taskId = "task" + i;
                String commandLine = String.format("echo Task %d", i);
                tasksToAdd.add(new BatchTaskCreateParameters(taskId, commandLine));
            }

            // Call createTasks method
            SyncAsyncExtension.execute(() -> batchClient.createTasks(jobId, tasksToAdd),
                () -> batchAsyncClient.createTasks(jobId, tasksToAdd));

            // Wait to ensure all tasks are visible in listTasks()
            sleepIfRunningAgainstService(15000);

            // Verify tasks are created
            Iterable<BatchTask> tasks = SyncAsyncExtension.execute(() -> batchClient.listTasks(jobId),
                () -> Mono.fromCallable(() -> batchAsyncClient.listTasks(jobId).toIterable()));
            int createdTaskCount = 0;
            for (BatchTask task : tasks) {
                createdTaskCount++;
            }
            Assertions.assertEquals(taskCount, createdTaskCount);

        } finally {
            // Clean up
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
    public void testDeserializationOfBatchTaskStatistics() {
        // Simulated JSON response with numbers as strings
        String jsonResponse = "{" + "\"url\":\"http://example.com/statistics\","
            + "\"startTime\":\"2022-01-01T00:00:00Z\"," + "\"lastUpdateTime\":\"2022-01-01T01:00:00Z\","
            + "\"userCPUTime\":\"PT1H\"," + "\"kernelCPUTime\":\"PT2H\"," + "\"wallClockTime\":\"PT3H\","
            + "\"readIOps\":\"1000\"," + "\"writeIOps\":\"500\"," + "\"readIOGiB\":0.5," + "\"writeIOGiB\":0.25,"
            + "\"waitTime\":\"PT30M\"" + "}";

        // Deserialize JSON response using JsonReader from JsonProviders
        try (JsonReader jsonReader = JsonProviders.createReader(new StringReader(jsonResponse))) {
            BatchTaskStatistics stats = BatchTaskStatistics.fromJson(jsonReader);

            // Assertions
            Assertions.assertNotNull(stats);
            Assertions.assertEquals("http://example.com/statistics", stats.getUrl());
            Assertions.assertEquals(OffsetDateTime.parse("2022-01-01T00:00:00Z"), stats.getStartTime());
            Assertions.assertEquals(OffsetDateTime.parse("2022-01-01T01:00:00Z"), stats.getLastUpdateTime());
            Assertions.assertEquals(Duration.parse("PT1H"), stats.getUserCpuTime());
            Assertions.assertEquals(Duration.parse("PT2H"), stats.getKernelCpuTime());
            Assertions.assertEquals(Duration.parse("PT3H"), stats.getWallClockTime());
            Assertions.assertEquals(1000, stats.getReadIops());
            Assertions.assertEquals(500, stats.getWriteIops());
            Assertions.assertEquals(0.5, stats.getReadIoGiB());
            Assertions.assertEquals(0.25, stats.getWriteIoGiB());
            Assertions.assertEquals(Duration.parse("PT30M"), stats.getWaitTime());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
