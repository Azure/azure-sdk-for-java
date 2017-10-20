/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.windowsazure.services.media;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.services.media.models.Asset;
import com.microsoft.windowsazure.services.media.models.AssetInfo;
import com.microsoft.windowsazure.services.media.models.Job;
import com.microsoft.windowsazure.services.media.models.Job.Creator;
import com.microsoft.windowsazure.services.media.models.JobInfo;
import com.microsoft.windowsazure.services.media.models.JobState;
import com.microsoft.windowsazure.services.media.models.ListResult;
import com.microsoft.windowsazure.services.media.models.Task;
import com.microsoft.windowsazure.services.media.models.Task.CreateBatchOperation;
import com.microsoft.windowsazure.services.media.models.TaskInfo;
import com.microsoft.windowsazure.services.media.models.TaskOption;
import com.microsoft.windowsazure.services.media.models.TaskState;
import com.sun.jersey.core.util.Base64;

public class TaskIntegrationTest extends IntegrationTestBase {
    private static AssetInfo assetInfo;
    private Creator jobCreator;

    private static final String commonConfiguration = "H.264 256k DSL CBR";

    @BeforeClass
    public static void setup() throws Exception {
        IntegrationTestBase.setup();
        assetInfo = setupAssetWithFile();
    }

    @Before
    public void instanceSetup() {
        this.jobCreator = Job.create()
                .setName(testJobPrefix + UUID.randomUUID().toString())
                .setPriority(3).addInputMediaAsset(assetInfo.getId());
    }

    @Test
    public void createTaskSuccess() throws ServiceException,
            UnsupportedEncodingException {
        // Arrange

        // Required
        String mediaProcessorId = MEDIA_ENCODER_MEDIA_PROCESSOR_ID;
        String taskBody = constructTaskBody(0);

        // Optional parameters
        String configuration = new String(Base64.encode(commonConfiguration),
                "UTF8");
        String name = "My encoding Task " + UUID.randomUUID().toString();
        int jobPriority = 3;
        TaskOption options = TaskOption.ProtectedConfiguration;
        // Use a fake id, to simulate real use.
        String encryptionKeyId = "nb:kid:UUID:" + UUID.randomUUID().toString();
        String encryptionScheme = "ConfigurationEncryption";
        String encryptionVersion = "1.0";
        // Use a trivial vector, 16 bytes of zeros, base-64 encoded.
        String initializationVector = new String(Base64.encode(new byte[16]),
                "UTF8");

        CreateBatchOperation taskCreator = Task
                .create(mediaProcessorId, taskBody)
                .setConfiguration(configuration).setName(name)
                .setPriority(jobPriority).setOptions(options)
                .setEncryptionKeyId(encryptionKeyId)
                .setEncryptionScheme(encryptionScheme)
                .setEncryptionVersion(encryptionVersion)
                .setInitializationVector(initializationVector);
        jobCreator.addTaskCreator(taskCreator);

        // Act
        JobInfo job = service.create(jobCreator);
        List<TaskInfo> taskInfos = service.list(Task.list(job.getTasksLink()));

        // Assert
        assertEquals("taskInfos count", 1, taskInfos.size());
        verifyTaskPropertiesJustStarted("taskInfo", mediaProcessorId, options,
                taskBody, configuration, name, jobPriority, encryptionKeyId,
                encryptionScheme, encryptionVersion, initializationVector,
                taskInfos.get(0));
    }

    @Test
    public void createTwoTasksSuccess() throws ServiceException {
        // Arrange

        // Required
        String mediaProcessorId = MEDIA_ENCODER_MEDIA_PROCESSOR_ID;
        String[] taskBodies = new String[] { constructTaskBody(0),
                constructTaskBody(1) };

        // Optional parameters
        String configuration = commonConfiguration;
        String baseName = "My encoding Task " + UUID.randomUUID().toString();
        String[] suffixes = new String[] { " 1", " 2" };
        int jobPriority = 3;
        TaskOption options = TaskOption.None;

        List<CreateBatchOperation> taskCreators = new ArrayList<CreateBatchOperation>();

        for (int i = 0; i < taskBodies.length; i++) {
            CreateBatchOperation taskCreator = Task
                    .create(mediaProcessorId, taskBodies[i])
                    .setConfiguration(configuration)
                    .setName(baseName + suffixes[i]);
            taskCreators.add(taskCreator);
            jobCreator.addTaskCreator(taskCreator);
        }

        // Act
        JobInfo job = service.create(jobCreator);
        List<TaskInfo> taskInfos = service.list(Task.list(job.getTasksLink()));

        // Assert
        assertEquals("taskInfos count", taskCreators.size(), taskInfos.size());
        for (int i = 0; i < taskCreators.size(); i++) {
            verifyTaskPropertiesJustStartedNoEncryption("taskInfo",
                    mediaProcessorId, options, taskBodies[i], configuration,
                    baseName + suffixes[i], jobPriority, taskInfos.get(i));
        }
    }

    @Test
    public void canListTasksWithOptions() throws ServiceException {
        // Arrange
        String mediaProcessorId = MEDIA_ENCODER_MEDIA_PROCESSOR_ID;
        String configuration = commonConfiguration;
        String[] taskNameSuffixes = new String[] { "A", "B", "C", "D" };
        String baseName = "My encoding Task " + UUID.randomUUID().toString();
        int taskCounter = 0;
        for (String suffix : taskNameSuffixes) {
            CreateBatchOperation taskCreator = Task
                    .create(mediaProcessorId, constructTaskBody(taskCounter++))
                    .setConfiguration(configuration).setName(baseName + suffix);
            jobCreator.addTaskCreator(taskCreator);
        }

        service.create(jobCreator);

        // Act
        ListResult<TaskInfo> listTaskResult1 = service.list(Task.list().set(
                "$filter", "startswith(Name, '" + baseName + "') eq true"));
        ListResult<TaskInfo> listTaskResult2 = service.list(Task.list()
                .set("$filter", "startswith(Name, '" + baseName + "') eq true")
                .setTop(2));

        // Assert
        assertEquals("listTaskResult1.size", 4, listTaskResult1.size());
        assertEquals("listTaskResult2.size", 2, listTaskResult2.size());
    }

    @Test
    public void cancelTaskSuccess() throws ServiceException,
            InterruptedException {
        // Arrange
        String mediaProcessorId = MEDIA_ENCODER_MEDIA_PROCESSOR_ID;
        String taskBody = constructTaskBody(0);
        String configuration = commonConfiguration;
        String name = "My encoding Task " + UUID.randomUUID().toString();
        CreateBatchOperation taskCreator = Task
                .create(mediaProcessorId, taskBody)
                .setConfiguration(configuration).setName(name);
        jobCreator.addTaskCreator(taskCreator);

        JobInfo jobInfo = service.create(jobCreator);

        // Act
        service.action(Job.cancel(jobInfo.getId()));
        JobInfo cancellingJobInfo = service.get(Job.get(jobInfo.getId()));
        while (cancellingJobInfo.getState() == JobState.Canceling) {
            Thread.sleep(2000);
            cancellingJobInfo = service.get(Job.get(jobInfo.getId()));
        }

        // Assert
        List<TaskInfo> taskInfos = service.list(Task.list(cancellingJobInfo
                .getTasksLink()));
        for (TaskInfo taskInfo : taskInfos) {
            verifyTaskPropertiesNoEncryption("canceled task", mediaProcessorId,
                    TaskOption.None, taskBody, configuration, name, 3,
                    null, null, 0.0, 0.0, null, TaskState.Canceled,
                    taskInfo);
        }
    }

    private void verifyTaskProperties(String message, String mediaProcessorId,
            TaskOption options, String taskBody, String configuration,
            String name, int priority, String encryptionKeyId,
            String encryptionScheme, String encryptionVersion,
            String initializationVector, Date endTime, String errorDetails,
            double progress, double runningDuration, Date startTime,
            TaskState state, TaskInfo actual) throws ServiceException {
        assertNotNull(message, actual);
        assertNotNull(message + " id", actual.getId());

        // Required fields
        assertEquals(message + " getMediaProcessorId", mediaProcessorId,
                actual.getMediaProcessorId());
        assertEquals(message + " getOptions", options, actual.getOptions());
        assertEquals(message + " getTaskBody", taskBody, actual.getTaskBody());

        // Optional fields
        assertEquals(message + " getConfiguration", configuration,
                actual.getConfiguration());
        assertEquals(message + " getName", name, actual.getName());
        assertEquals(message + " getPriority", priority, actual.getPriority());

        // Optional encryption fields
        assertEqualsNullEmpty(message + " getEncryptionKeyId", encryptionKeyId,
                actual.getEncryptionKeyId());
        assertEqualsNullEmpty(message + " getEncryptionScheme",
                encryptionScheme, actual.getEncryptionScheme());
        assertEqualsNullEmpty(message + " getEncryptionVersion",
                encryptionVersion, actual.getEncryptionVersion());
        assertEqualsNullEmpty(message + " getInitializationVector",
                initializationVector, actual.getInitializationVector());

        // Read-only fields
        assertNotNull(message + " getErrorDetails", actual.getErrorDetails());
        assertEquals(message + " getErrorDetails.size", 0, actual
                .getErrorDetails().size());

        ListResult<AssetInfo> inputAssets = service.list(Asset.list(actual
                .getInputAssetsLink()));
        ListResult<AssetInfo> outputAssets = service.list(Asset.list(actual
                .getOutputAssetsLink()));

        assertEquals(message + " inputAssets.size", 1, inputAssets.size());
        assertEquals(message + " inputAssets.get(0).getId", assetInfo.getId(),
                inputAssets.get(0).getId());

        assertEquals(message + " outputAssets.size", 1, outputAssets.size());
        // Because this is a new asset, there is not much else to test
        assertTrue(message + " outputAssets.get(0).getId != assetInfo.getId",
                !assetInfo.getId().equals(outputAssets.get(0).getId()));

        assertEquals(message + " getProgress", progress, actual.getProgress(),
                0.01);
        assertEquals(message + " getRunningDuration", runningDuration,
                actual.getRunningDuration(), 0.01);
        assertDateApproxEquals(message + " getStartTime", startTime,
                actual.getStartTime());
        assertEquals(message + " getState", state, actual.getState());

        // Note: The PerfMessage is not validated because it is server
        // generated.
    }

    private void verifyTaskPropertiesJustStarted(String message,
            String mediaProcessorId, TaskOption options, String taskBody,
            String configuration, String name, int priority,
            String encryptionKeyId, String encryptionScheme,
            String encryptionVersion, String initializationVector,
            TaskInfo actual) throws ServiceException {

        // Read-only
        Date endTime = null;
        String errorDetails = null;
        double progress = 0.0;
        int runningDuration = 0;
        Date startTime = null;
        TaskState state = TaskState.None;

        verifyTaskProperties(message, mediaProcessorId, options, taskBody,
                configuration, name, priority, encryptionKeyId,
                encryptionScheme, encryptionVersion, initializationVector,
                endTime, errorDetails, progress, runningDuration, startTime,
                state, actual);
    }

    private void verifyTaskPropertiesJustStartedNoEncryption(String message,
            String mediaProcessorId, TaskOption options, String taskBody,
            String configuration, String name, int priority, TaskInfo actual)
            throws ServiceException {
        String encryptionKeyId = null;
        String encryptionScheme = "None";
        String encryptionVersion = null;
        String initializationVector = null;

        verifyTaskPropertiesJustStarted(message, mediaProcessorId, options,
                taskBody, configuration, name, priority, encryptionKeyId,
                encryptionScheme, encryptionVersion, initializationVector,
                actual);
    }

    private void verifyTaskPropertiesNoEncryption(String message,
            String mediaProcessorId, TaskOption options, String taskBody,
            String configuration, String name, int priority, Date endTime,
            String errorDetails, double progress, double runningDuration,
            Date startTime, TaskState state, TaskInfo actual)
            throws ServiceException {
        String encryptionKeyId = null;
        String encryptionScheme = "None";
        String encryptionVersion = null;
        String initializationVector = null;

        verifyTaskProperties(message, mediaProcessorId, options, taskBody,
                configuration, name, priority, encryptionKeyId,
                encryptionScheme, encryptionVersion, initializationVector,
                endTime, errorDetails, progress, runningDuration, startTime,
                state, actual);
    }

    private String constructTaskBody(int outputIndex) {
        return "<taskBody><inputAsset>JobInputAsset(0)</inputAsset>"
                + "<outputAsset>JobOutputAsset(" + outputIndex
                + ")</outputAsset></taskBody>";
    }
}
