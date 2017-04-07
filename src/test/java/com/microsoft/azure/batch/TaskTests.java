/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch;

import com.microsoft.azure.batch.interceptor.BatchClientParallelOptions;
import com.microsoft.azure.batch.protocol.models.*;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;
import java.util.*;

public class TaskTests  extends BatchTestBase {
    private static CloudPool livePool;

    @BeforeClass
    public static void setup() throws Exception {
        createClient(AuthMode.SharedKey);
        String poolId = getStringWithUserNamePrefix("-testpool");
        livePool = createIfNotExistPaaSPool(poolId);
        Assert.assertNotNull(livePool);
    }

    @AfterClass
    public static void cleanup() throws Exception {
        try {
            //batchClient.poolOperations().deletePool(livePool.id());
        } catch (Exception e) {
            // ignore any clean up exception
        }
    }

    @Test
    public void canCRUDTest() throws Exception {
        int TASK_COMPLETE_TIMEOUT = 60; // 60 seconds timeout
        String STANDARD_CONSOLE_OUTPUT_FILENAME = "stdout.txt";
        String BLOB_FILE_NAME = "test.txt";
        String taskId = "mytask";
        File temp = File.createTempFile("tempFile", ".tmp");
        BufferedWriter bw = new BufferedWriter(new FileWriter(temp));
        bw.write("This is an example");
        bw.close();
        temp.deleteOnExit();
        String jobId = getStringWithUserNamePrefix("-Job-" + (new Date()).toString().replace(' ', '-').replace(':', '-').replace('.', '-'));

        PoolInformation poolInfo = new PoolInformation();
        poolInfo.withPoolId(livePool.id());
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
            file.withBlobSource(sas);
            List<ResourceFile> files = new ArrayList<>();
            files.add(file);

            // CREATE
            TaskAddParameter taskToAdd = new TaskAddParameter();
            taskToAdd.withId(taskId).withCommandLine(String.format("cmd /c type %s", BLOB_FILE_NAME)).withResourceFiles(files);

            batchClient.taskOperations().createTask(jobId, taskToAdd);

            // GET
            CloudTask task = batchClient.taskOperations().getTask(jobId, taskId);
            Assert.assertNotNull(task);
            Assert.assertEquals(task.id(), taskId);

            // UPDATE
            TaskConstraints contraint = new TaskConstraints();
            contraint.withMaxTaskRetryCount(5);
            batchClient.taskOperations().updateTask(jobId, taskId, contraint);
            task = batchClient.taskOperations().getTask(jobId, taskId);
            Assert.assertEquals(task.constraints().maxTaskRetryCount(), (Integer)5);

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

            if (waitForTasksToComplete(batchClient, jobId, TASK_COMPLETE_TIMEOUT)) {
                // Get the task command output file
                task = batchClient.taskOperations().getTask(jobId, taskId);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                batchClient.fileOperations().getFileFromTask(jobId, task.id(), STANDARD_CONSOLE_OUTPUT_FILENAME, stream);
                String fileContent = stream.toString("UTF-8");
                Assert.assertEquals(fileContent, "This is an example");
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
        String jobId = getStringWithUserNamePrefix("-Job-" + (new Date()).toString().replace(' ', '-').replace(':', '-').replace('.', '-'));
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
            Assert.assertEquals(task.id(), taskId);
            Assert.assertEquals(task.userIdentity().userName(), "test-user");
            Assert.assertEquals(task.applicationPackageReferences().get(0).applicationId(), "msmpi");

        } finally {
            try {
                batchClient.jobOperations().deleteJob(jobId);
            } catch (Exception e) {
                // Ignore here
            }
        }
    }

    @Test
    public void testAddMultiTasks() throws Exception {
        String jobId = getStringWithUserNamePrefix("-Job1-" + (new Date()).toString().replace(' ', '-').replace(':', '-').replace('.', '-'));

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
            Assert.assertTrue(tasks.size() == 1000);
        } finally {
            try {
                batchClient.jobOperations().deleteJob(jobId);
            } catch (Exception e) {
                // Ignore here
            }
        }
    }
}