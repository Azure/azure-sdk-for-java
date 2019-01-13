// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.batch;

import com.microsoft.azure.batch.auth.BatchSharedKeyCredentials;
import com.microsoft.azure.batch.interceptor.BatchClientParallelOptions;
import com.microsoft.azure.batch.protocol.models.*;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.junit.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class TaskTests  extends BatchTestBase {
    private static CloudPool livePool;
    private static CloudPool liveIaaSPool;

    @BeforeClass
    public static void setup() throws Exception {
        String testMode = getTestMode();
        Assume.assumeTrue("Tests only run in Record/Live mode", testMode.equals("RECORD"));
        try {
        createClient(AuthMode.SharedKey);
        String poolId = getStringIdWithUserNamePrefix("-testpool");
        livePool = createIfNotExistPaaSPool(poolId);
        poolId = getStringIdWithUserNamePrefix("-testIaaSpool");
        liveIaaSPool = createIfNotExistIaaSPool(poolId);
        Assert.assertNotNull(livePool);
        } catch (BatchErrorException e) {
            cleanup();
            throw e;
        }
    }

    @AfterClass
    public static void cleanup() throws Exception {
        try {
            // batchClient.poolOperations().deletePool(livePool.id());
        } catch (Exception e) {
            // ignore any clean up exception
        }
        try {
            // batchClient.poolOperations().deletePool(liveIaasPool.id());
        } catch (Exception e) {
            // ignore any clean up exception
        }
    }

    @Test
    public void canCRUDTest() throws Exception {
        int TASK_COMPLETE_TIMEOUT_IN_SECONDS = 60; // 60 seconds timeout
        String STANDARD_CONSOLE_OUTPUT_FILENAME = "stdout.txt";
        String BLOB_FILE_NAME = "test.txt";
        String taskId = "mytask";
        File temp = File.createTempFile("tempFile", ".tmp");
        BufferedWriter bw = new BufferedWriter(new FileWriter(temp));
        bw.write("This is an example");
        bw.close();
        temp.deleteOnExit();
        String jobId = getStringIdWithUserNamePrefix("-canCRUDTest-" + (new Date()).toString().replace(' ', '-').replace(':', '-').replace('.', '-'));

        PoolInformation poolInfo = new PoolInformation();
        poolInfo.withPoolId(liveIaaSPool.id());
        batchClient.jobOperations().createJob(jobId, poolInfo);

        String storageAccountName = System.getenv("STORAGE_ACCOUNT_NAME");
        String storageAccountKey = System.getenv("STORAGE_ACCOUNT_KEY");

        try {
            // Create storage container
            CloudBlobContainer container = createBlobContainer(storageAccountName, storageAccountKey, "testaddtask");
            String sas = uploadFileToCloud(container, BLOB_FILE_NAME, temp.getAbsolutePath());

            // Associate resource file with task
            ResourceFile file = new ResourceFile();
            file.withFilePath(BLOB_FILE_NAME);
            file.withHttpUrl(sas);
            List<ResourceFile> files = new ArrayList<>();
            files.add(file);

            // CREATE
            TaskAddParameter taskToAdd = new TaskAddParameter();
            taskToAdd.withId(taskId).withCommandLine(String.format("/bin/bash -c 'set -e; set -o pipefail; cat %s'", BLOB_FILE_NAME)).withResourceFiles(files);

            batchClient.taskOperations().createTask(jobId, taskToAdd);

            // GET
            CloudTask task = batchClient.taskOperations().getTask(jobId, taskId);
            Assert.assertNotNull(task);
            Assert.assertEquals(taskId, task.id());
            
            // Verify default retention time
            Assert.assertEquals(Period.days(7), task.constraints().retentionTime());

            // UPDATE
            TaskConstraints contraint = new TaskConstraints();
            contraint.withMaxTaskRetryCount(5);
            batchClient.taskOperations().updateTask(jobId, taskId, contraint);
            task = batchClient.taskOperations().getTask(jobId, taskId);
            Assert.assertEquals((Integer)5, task.constraints().maxTaskRetryCount());

            // LIST
            List<CloudTask> tasks = batchClient.taskOperations().listTasks(jobId);
            Assert.assertNotNull(tasks);
            Assert.assertTrue(tasks.size() > 0);

            boolean found = false;
            for (CloudTask t : tasks) {
                if (t.id().equals(taskId)) {
                    found = true;
                    break;
                }
            }

            Assert.assertTrue(found);

            if (waitForTasksToComplete(batchClient, jobId, TASK_COMPLETE_TIMEOUT_IN_SECONDS)) {
                // Get the task command output file
                task = batchClient.taskOperations().getTask(jobId, taskId);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                batchClient.fileOperations().getFileFromTask(jobId, task.id(), STANDARD_CONSOLE_OUTPUT_FILENAME, stream);
                String fileContent = stream.toString("UTF-8");
                Assert.assertEquals("This is an example", fileContent);

                // UPLOAD LOG
                String outputSas = generateContainerSasToken(container);
                UploadBatchServiceLogsResult uploadBatchServiceLogsResult = batchClient.computeNodeOperations().uploadBatchServiceLogs(liveIaaSPool.id(), task.nodeInfo().nodeId(), outputSas, DateTime.now().minusMinutes(-10));
                Assert.assertNotNull(uploadBatchServiceLogsResult);
                Assert.assertTrue(uploadBatchServiceLogsResult.numberOfFilesUploaded() > 0);
                Assert.assertTrue(uploadBatchServiceLogsResult.virtualDirectoryName().toLowerCase().contains(liveIaaSPool.id().toLowerCase()));
            }

            // DELETE
            batchClient.taskOperations().deleteTask(jobId, taskId);
            try {
                batchClient.taskOperations().getTask(jobId, taskId);
                Assert.assertTrue("Shouldn't be here, the job should be deleted", true);
            } catch (BatchErrorException err) {
                if (!err.body().code().equals(BatchErrorCodeStrings.TaskNotFound)) {
                    throw err;
                }
            }
        } finally {
            try {
                batchClient.jobOperations().deleteJob(jobId);
            } catch (Exception e) {
                // Ignore here
            }
        }
    }

    @Test
    public void testJobUser() throws Exception {
        String jobId = getStringIdWithUserNamePrefix("-testJobUser-" + (new Date()).toString().replace(' ', '-').replace(':', '-').replace('.', '-'));
        String taskId = "mytask";

        PoolInformation poolInfo = new PoolInformation();
        poolInfo.withPoolId(livePool.id());
        batchClient.jobOperations().createJob(jobId, poolInfo);

        try {
            // CREATE
            List<ApplicationPackageReference> apps = new ArrayList<>();
            apps.add(new ApplicationPackageReference().withApplicationId("MSMPI"));
            TaskAddParameter taskToAdd = new TaskAddParameter();
            taskToAdd.withId(taskId)
                    .withCommandLine("cmd /c echo hello")
                    .withUserIdentity(new UserIdentity()
                            .withUserName("test-user"))
                    .withApplicationPackageReferences(apps);

            batchClient.taskOperations().createTask(jobId, taskToAdd);

            // GET
            CloudTask task = batchClient.taskOperations().getTask(jobId, taskId);
            Assert.assertNotNull(task);
            Assert.assertEquals(taskId, task.id());
            Assert.assertEquals("test-user", task.userIdentity().userName());
            Assert.assertEquals("msmpi", task.applicationPackageReferences().get(0).applicationId());

        } finally {
            try {
                batchClient.jobOperations().deleteJob(jobId);
            } catch (Exception e) {
                // Ignore here
            }
        }
    }

    @Test
    public void testOutputFiles() throws Exception {
        int TASK_COMPLETE_TIMEOUT_IN_SECONDS = 60; // 60 seconds timeout
        String jobId = getStringIdWithUserNamePrefix("-testOutputFiles-" + (new Date()).toString().replace(' ', '-').replace(':', '-').replace('.', '-'));
        String taskId = "mytask";
        String badTaskId = "mytask1";
        String storageAccountName = System.getenv("STORAGE_ACCOUNT_NAME");
        String storageAccountKey = System.getenv("STORAGE_ACCOUNT_KEY");

        PoolInformation poolInfo = new PoolInformation();
        poolInfo.withPoolId(liveIaaSPool.id());
        batchClient.jobOperations().createJob(jobId, poolInfo);
        CloudBlobContainer container = createBlobContainer(storageAccountName, storageAccountKey, "output");
        String containerUrl = generateContainerSasToken(container);

        try {
            // CREATE
            List<OutputFile> outputs = new ArrayList<>();
            outputs.add(new OutputFile()
                    .withFilePattern("../stdout.txt")
                    .withDestination(new OutputFileDestination()
                            .withContainer(new OutputFileBlobContainerDestination()
                                    .withContainerUrl(containerUrl)
                                    .withPath("taskLogs/output.txt")))
                    .withUploadOptions(new OutputFileUploadOptions()
                            .withUploadCondition(OutputFileUploadCondition.TASK_COMPLETION)));
            outputs.add(new OutputFile()
                    .withFilePattern("../stderr.txt")
                    .withDestination(new OutputFileDestination()
                            .withContainer(new OutputFileBlobContainerDestination()
                                    .withContainerUrl(containerUrl)
                                    .withPath("taskLogs/err.txt")))
                    .withUploadOptions(new OutputFileUploadOptions()
                            .withUploadCondition(OutputFileUploadCondition.TASK_FAILURE)));
            TaskAddParameter taskToAdd = new TaskAddParameter();
            taskToAdd.withId(taskId)
                    .withCommandLine("bash -c \"echo hello\"")
                    .withOutputFiles(outputs);

            batchClient.taskOperations().createTask(jobId, taskToAdd);

            if (waitForTasksToComplete(batchClient, jobId, TASK_COMPLETE_TIMEOUT_IN_SECONDS)) {
                CloudTask task = batchClient.taskOperations().getTask(jobId, taskId);
                Assert.assertNotNull(task);
                Assert.assertEquals(TaskExecutionResult.SUCCESS, task.executionInfo().result());
                Assert.assertNull(task.executionInfo().failureInfo());

                // Get the task command output file
                String result = getContentFromContainer(container, "taskLogs/output.txt");
                Assert.assertEquals("hello\n", result);
            }

            taskToAdd = new TaskAddParameter();
            taskToAdd.withId(badTaskId)
                    .withCommandLine("bash -c \"bad command\"")
                    .withOutputFiles(outputs);

            batchClient.taskOperations().createTask(jobId, taskToAdd);

            if (waitForTasksToComplete(batchClient, jobId, TASK_COMPLETE_TIMEOUT_IN_SECONDS)) {
                CloudTask task = batchClient.taskOperations().getTask(jobId, badTaskId);
                Assert.assertNotNull(task);
                Assert.assertEquals(TaskExecutionResult.FAILURE, task.executionInfo().result());
                Assert.assertNotNull(task.executionInfo().failureInfo());
                Assert.assertEquals(ErrorCategory.USER_ERROR, task.executionInfo().failureInfo().category());
                Assert.assertEquals("FailureExitCode", task.executionInfo().failureInfo().code());

                // Get the task command output file
                String result = getContentFromContainer(container, "taskLogs/err.txt");
                Assert.assertEquals("bash: bad: command not found\n", result);
            }

        } finally {
            try {
                container.delete();
                batchClient.jobOperations().deleteJob(jobId);
            } catch (Exception e) {
                // Ignore here
            }
        }
    }

    @Test
    public void testAddMultiTasks() throws Exception {
        String jobId = getStringIdWithUserNamePrefix("-testAddMultiTasks-" + (new Date()).toString().replace(' ', '-').replace(':', '-').replace('.', '-'));

        PoolInformation poolInfo = new PoolInformation();
        poolInfo.withPoolId(livePool.id());
        batchClient.jobOperations().createJob(jobId, poolInfo);


        int TASK_COUNT=1000;

        try {
            // CREATE
            List<TaskAddParameter> tasksToAdd = new ArrayList<>();
            for (int i=0; i<TASK_COUNT; i++)
            {
                TaskAddParameter addParameter = new TaskAddParameter();
                addParameter.withId(String.format("mytask%d", i)).withCommandLine(String.format("cmd /c echo hello %d",i));
                tasksToAdd.add(addParameter);
            }
            BatchClientParallelOptions option = new BatchClientParallelOptions(10);
            Collection<BatchClientBehavior> behaviors = new HashSet<>();
            behaviors.add(option);
            batchClient.taskOperations().createTasks(jobId, tasksToAdd, behaviors);

            // LIST
            List<CloudTask> tasks = batchClient.taskOperations().listTasks(jobId);
            Assert.assertNotNull(tasks);
            Assert.assertTrue(tasks.size() == TASK_COUNT);
        } finally {
            try {
                batchClient.jobOperations().deleteJob(jobId);
            } catch (Exception e) {
                // Ignore here
            }
        }
    }


    @Test
    public void testAddMultiTasksWithError() throws Exception {

        BatchSharedKeyCredentials noExistCredentials1 = new BatchSharedKeyCredentials(
                "https://noexistaccount.westus.batch.azure.com",
                "noexistaccount",
                System.getenv("AZURE_BATCH_ACCESS_KEY"));
        BatchClient testBatchClient = BatchClient.open(noExistCredentials1);

        String jobId = getStringIdWithUserNamePrefix("-testAddMultiTasksWithError-" + (new Date()).toString().replace(' ', '-').replace(':', '-').replace('.', '-'));

        int TASK_COUNT=1000;

        try {
            // CREATE
            List<TaskAddParameter> tasksToAdd = new ArrayList<>();
            for (int i=0; i<TASK_COUNT; i++)
            {
                TaskAddParameter addParameter = new TaskAddParameter();
                addParameter.withId(String.format("mytask%d", i)).withCommandLine(String.format("cmd /c echo hello %d",i));
                tasksToAdd.add(addParameter);
            }
            BatchClientParallelOptions option = new BatchClientParallelOptions(10);
            Collection<BatchClientBehavior> behaviors = new HashSet<>();
            behaviors.add(option);
            testBatchClient.taskOperations().createTasks(jobId, tasksToAdd, behaviors);
            Assert.assertTrue("Should not here", true);
        } catch (RuntimeException ex) {
            System.out.printf("Expect exception %s", ex.toString());
        }
    }

    @Test
    public void testGetTaskCounts() throws Exception {
        String jobId = getStringIdWithUserNamePrefix("-testGetTaskCounts-" + (new Date()).toString().replace(' ', '-').replace(':', '-').replace('.', '-'));

        PoolInformation poolInfo = new PoolInformation();
        poolInfo.withPoolId(livePool.id());
        batchClient.jobOperations().createJob(jobId, poolInfo);


        int TASK_COUNT=1000;

        try {
            // Test Job count
            TaskCounts counts = batchClient.jobOperations().getTaskCounts(jobId);
            int all = counts.active() + counts.completed() + counts.running();
            Assert.assertEquals(0, all);

            // CREATE
            List<TaskAddParameter> tasksToAdd = new ArrayList<>();
            for (int i=0; i<TASK_COUNT; i++)
            {
                TaskAddParameter addParameter = new TaskAddParameter();
                addParameter.withId(String.format("mytask%d", i)).withCommandLine(String.format("cmd /c echo hello %d",i));
                tasksToAdd.add(addParameter);
            }
            BatchClientParallelOptions option = new BatchClientParallelOptions(10);
            Collection<BatchClientBehavior> behaviors = new HashSet<>();
            behaviors.add(option);
            batchClient.taskOperations().createTasks(jobId, tasksToAdd, behaviors);

            TimeUnit.SECONDS.sleep(30);

            // Test Job count
            counts = batchClient.jobOperations().getTaskCounts(jobId);
            all = counts.active() + counts.completed() + counts.running();
            Assert.assertEquals(TASK_COUNT, all);
        } finally {
            try {
                batchClient.jobOperations().deleteJob(jobId);
            } catch (Exception e) {
                // Ignore here
            }
        }
    }

    @Test
    public void failCreateContainerTaskWithRegularPool() throws Exception {
        String jobId = getStringIdWithUserNamePrefix("-failCreateContainerRegPool-" + (new Date()).toString().replace(' ', '-').replace(':', '-').replace('.', '-'));
        String taskId = "mytask";

        PoolInformation poolInfo = new PoolInformation();
        poolInfo.withPoolId(liveIaaSPool.id());
        batchClient.jobOperations().createJob(jobId, poolInfo);

        TaskAddParameter taskToAdd = new TaskAddParameter();
        taskToAdd.withId(taskId)
                .withContainerSettings(new TaskContainerSettings()
                        .withContainerRunOptions("--rm")
                        .withImageName("centos"))
                .withCommandLine("bash -c \"echo hello\"");

        try
        {
            batchClient.taskOperations().createTask(jobId, taskToAdd);
        }
        catch (BatchErrorException err) {
            if (err.body().code().equals("InvalidPropertyValue")) {
                // Accepted Error
                for (int i = 0; i < err.body().values().size(); i++) {
                    if (err.body().values().get(i).key().equals("Reason")) {
                        Assert.assertEquals("The specified imageReference with publisher Canonical offer UbuntuServer sku 16.04-LTS does not support container feature.", err.body().values().get(i).value());
                        return;
                    }
                }
                throw new Exception("Couldn't find expect error reason");
            } else {
                throw err;
            }
        }
        finally {
            try {
                batchClient.jobOperations().deleteJob(jobId);
            } catch (Exception e) {
                // Ignore here
            }
        }
    }
    
    @Test
    public void failIfPoisonTaskTooLarge() throws Exception {
        String jobId = getStringIdWithUserNamePrefix("-failIfPoisonTaskTooLarge-" + (new Date()).toString().replace(' ', '-').replace(':', '-').replace('.', '-'));
        String taskId = "mytask";

        PoolInformation poolInfo = new PoolInformation();
        poolInfo.withPoolId(liveIaaSPool.id());
        batchClient.jobOperations().createJob(jobId, poolInfo);

        List<TaskAddParameter> tasksToAdd = new ArrayList<TaskAddParameter>();
        TaskAddParameter taskToAdd = new TaskAddParameter();
        List<ResourceFile> resourceFiles = new ArrayList<ResourceFile>();
        ResourceFile resourceFile;
        // If this test fails try increasing the size of the Task in case maximum size increase
        for(int i = 0; i < 10000; i++) {
            resourceFile = new ResourceFile().withHttpUrl("https://mystorageaccount.blob.core.windows.net/files/resourceFile"+i).withFilePath("resourceFile"+i);
            resourceFiles.add(resourceFile);
        }
        taskToAdd.withId(taskId).withResourceFiles(resourceFiles).withCommandLine("sleep 1");
        tasksToAdd.add(taskToAdd);

        try
        {
            batchClient.taskOperations().createTasks(jobId, tasksToAdd);
            try {
                batchClient.jobOperations().deleteJob(jobId);
            } catch (Exception e) {
                // Ignore here
            }
            Assert.fail("Expected RequestBodyTooLarge error");
        }
        catch (BatchErrorException err) {
            try {
                batchClient.jobOperations().deleteJob(jobId);
            } catch (Exception e) {
                // Ignore here
            }
            Assert.assertEquals(err.body().code(), BatchErrorCodeStrings.RequestBodyTooLarge);
        }
        catch (Exception err) {
            try {
                batchClient.jobOperations().deleteJob(jobId);
            } catch (Exception e) {
                // Ignore here
            }
            Assert.fail("Expected RequestBodyTooLarge error");
        }
    }

    @Test
    public void succeedWithRetry() throws Exception {
        String jobId = getStringIdWithUserNamePrefix("-succeedWithRetry-" + (new Date()).toString().replace(' ', '-').replace(':', '-').replace('.', '-'));
        String taskId = "mytask";

        PoolInformation poolInfo = new PoolInformation();
        poolInfo.withPoolId(liveIaaSPool.id());
        batchClient.jobOperations().createJob(jobId, poolInfo);

        List<TaskAddParameter> tasksToAdd = new ArrayList<TaskAddParameter>();
        TaskAddParameter taskToAdd;
        List<ResourceFile> resourceFiles = new ArrayList<ResourceFile>();
        ResourceFile resourceFile;
        
        BatchClientParallelOptions option = new BatchClientParallelOptions(10);
        Collection<BatchClientBehavior> behaviors = new HashSet<>();
        behaviors.add(option);

        // Num Resource Files * Max Chunk Size should be greater than or equal to the limit which triggers the PoisonTask test to ensure we encounter the error in the initial chunk.
        for(int i = 0; i < 100; i++) {
            resourceFile = new ResourceFile().withHttpUrl("https://mystorageaccount.blob.core.windows.net/files/resourceFile"+i).withFilePath("resourceFile"+i);
            resourceFiles.add(resourceFile);
        }
        // Num tasks to add
        for(int i = 0; i < 1500; i++) {
            taskToAdd = new TaskAddParameter();
            taskToAdd.withId(taskId+i).withResourceFiles(resourceFiles).withCommandLine("sleep 1");
            tasksToAdd.add(taskToAdd);
        }

        try
        {
            batchClient.taskOperations().createTasks(jobId, tasksToAdd, behaviors);
            try {
                batchClient.jobOperations().deleteJob(jobId);
            } catch (Exception e) {
                // Ignore here
            }
        }
        catch (Exception err) {
            try {
                batchClient.jobOperations().deleteJob(jobId);
            } catch (Exception e) {
                // Ignore here
            }
            Assert.fail("Expected Success");
        }
    }

}