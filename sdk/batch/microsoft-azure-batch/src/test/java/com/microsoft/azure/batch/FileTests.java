// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.batch;

import com.microsoft.azure.batch.protocol.models.*;
import org.junit.*;
import org.apache.commons.io.IOUtils;
import rx.exceptions.Exceptions;
import rx.functions.Func1;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class FileTests extends BatchIntegrationTestBase {
    private static CloudPool livePool;
    private static String poolId;

    @BeforeClass
    public static void setup() throws Exception {
        poolId = getStringIdWithUserNamePrefix("-testpool");
        if(isRecordMode()) {
            createClient(AuthMode.AAD);
            livePool = createIfNotExistIaaSPool(poolId);
            Assert.assertNotNull(livePool);
        }
    }

    @AfterClass
    public static void cleanup() throws Exception {
        try {
            //batchClient.poolOperations().deletePool(livePool.id());
        }
        catch (Exception e) {
            // ignore any clean up exception
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
            poolInfo.withPoolId(poolId);

            batchClient.jobOperations().createJob(jobId, poolInfo);
            TaskAddParameter taskToAdd = new TaskAddParameter();
            taskToAdd.withId(taskId)
                    .withCommandLine("/bin/bash -c \"echo hello\"");
            batchClient.taskOperations().createTask(jobId, taskToAdd);

            if (waitForTasksToComplete(batchClient, jobId, TASK_COMPLETE_TIMEOUT_IN_SECONDS)) {
                List<NodeFile> files = batchClient.fileOperations().listFilesFromTask(jobId, taskId);
                boolean found = false;
                for (NodeFile f : files) {
                    if (f.name().equals("stdout.txt")) {
                        found = true;
                        break;
                    }
                }
                Assert.assertTrue(found);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                batchClient.fileOperations().getFileFromTask(jobId, taskId, "stdout.txt", stream);
                String fileContent = stream.toString("UTF-8");
                Assert.assertEquals("hello\n", fileContent);
                stream.close();

                String output = batchClient.protocolLayer().files().getFromTaskAsync(jobId, taskId, "stdout.txt").map(new Func1<InputStream, String>() {
                    @Override
                    public String call(InputStream input) {
                        try {
                            return IOUtils.toString(input, "UTF-8");
                        } catch (IOException e) {
                            throw Exceptions.propagate(e);
                        }
                    }
                }).toBlocking().single();
                Assert.assertEquals("hello\n", output);

                // Temporarily disabling this test due to issue with how Java SDK handles casing mismatches between
                // the swagger definition ("Content-Length") and the actual API response ("content-length")
                /* if(isRecordMode()) {
                    FileProperties properties = batchClient.fileOperations().getFilePropertiesFromTask(jobId, taskId, "stdout.txt");
                    Assert.assertEquals(6, properties.contentLength());
                } */
            } else {
                throw new TimeoutException("Task did not complete within the specified timeout");
            }
        }
        finally {
            try {
                batchClient.jobOperations().deleteJob(jobId);
            }
            catch (Exception e) {
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
            poolInfo.withPoolId(poolId);

            batchClient.jobOperations().createJob(jobId, poolInfo);
            TaskAddParameter taskToAdd = new TaskAddParameter();
            taskToAdd.withId(taskId)
                    .withCommandLine("/bin/bash -c \"echo hello\"");
            batchClient.taskOperations().createTask(jobId, taskToAdd);

            if (waitForTasksToComplete(batchClient, jobId, TASK_COMPLETE_TIMEOUT_IN_SECONDS)) {
                CloudTask task = batchClient.taskOperations().getTask(jobId, taskId);
                String nodeId = task.nodeInfo().nodeId();
                List<NodeFile> files = batchClient.fileOperations().listFilesFromComputeNode(poolId, nodeId, true, null);
                String fileName = null;
                for (NodeFile f : files) {
                    if (f.name().endsWith("stdout.txt")) {
                        fileName = f.name();
                        break;
                    }
                }
                Assert.assertNotNull(fileName);


                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                batchClient.fileOperations().getFileFromComputeNode(poolId, nodeId, fileName, stream);
                String fileContent = stream.toString("UTF-8");
                Assert.assertEquals("hello\n", fileContent);
                stream.close();

                String output = batchClient.protocolLayer().files().getFromComputeNodeAsync(poolId, nodeId, fileName).map(new Func1<InputStream, String>() {
                    @Override
                    public String call(InputStream input) {
                        try {
                            return IOUtils.toString(input, "UTF-8");
                        } catch (IOException e) {
                            throw Exceptions.propagate(e);
                        }
                    }
                }).toBlocking().single();
                Assert.assertEquals("hello\n", output);

                // Temporarily disabling this test due to issue with how Java SDK handles casing mismatches between
                // the swagger definition ("Content-Length") and the actual API response ("content-length")
                /* if(isRecordMode()) {
                    FileProperties properties = batchClient.fileOperations().getFilePropertiesFromComputeNode(poolId, nodeId, fileName);
                    Assert.assertEquals(6, properties.contentLength());
                } */
            } else {
                throw new TimeoutException("Task did not complete within the specified timeout");
            }
        }
        finally {
            try {
                batchClient.jobOperations().deleteJob(jobId);
            }
            catch (Exception e) {
                // Ignore here
            }
        }
    }
}
