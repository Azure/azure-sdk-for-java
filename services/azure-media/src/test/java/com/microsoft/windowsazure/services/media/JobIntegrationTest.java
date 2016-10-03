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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.AnyOf.anyOf;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.BeforeClass;
import org.junit.Test;

import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.services.media.models.Asset;
import com.microsoft.windowsazure.services.media.models.AssetInfo;
import com.microsoft.windowsazure.services.media.models.EndPointType;
import com.microsoft.windowsazure.services.media.models.ErrorDetail;
import com.microsoft.windowsazure.services.media.models.Job;
import com.microsoft.windowsazure.services.media.models.Job.Creator;
import com.microsoft.windowsazure.services.media.models.JobInfo;
import com.microsoft.windowsazure.services.media.models.JobNotificationSubscription;
import com.microsoft.windowsazure.services.media.models.JobState;
import com.microsoft.windowsazure.services.media.models.LinkInfo;
import com.microsoft.windowsazure.services.media.models.ListResult;
import com.microsoft.windowsazure.services.media.models.NotificationEndPoint;
import com.microsoft.windowsazure.services.media.models.NotificationEndPointInfo;
import com.microsoft.windowsazure.services.media.models.TargetJobState;
import com.microsoft.windowsazure.services.media.models.Task;
import com.microsoft.windowsazure.services.media.models.Task.CreateBatchOperation;
import com.microsoft.windowsazure.services.media.models.TaskHistoricalEvent;
import com.microsoft.windowsazure.services.media.models.TaskInfo;
import com.microsoft.windowsazure.services.queue.models.PeekMessagesResult.QueueMessage;

public class JobIntegrationTest extends IntegrationTestBase {

    private static AssetInfo assetInfo;

    private void verifyJobInfoEqual(String message, JobInfo expected,
            JobInfo actual) throws ServiceException {
        verifyJobProperties(message, expected.getName(),
                expected.getPriority(), expected.getRunningDuration(),
                expected.getState(), expected.getTemplateId(),
                expected.getCreated(), expected.getLastModified(),
                expected.getStartTime(), expected.getEndTime(), null, actual);
    }

    private void verifyJobProperties(String message, String testName,
            Integer priority, double runningDuration, JobState state,
            String templateId, Date created, Date lastModified, Date startTime,
            Date endTime, Integer expectedTaskCount, JobInfo actualJob)
            throws ServiceException {
        assertNotNull(message, actualJob);

        assertNotNull(message + "Id", actualJob.getId());

        assertEquals(message + " Name", testName, actualJob.getName());
        // comment out due to issue 464
        // assertEquals(message + " Priority", priority,
        // actualJob.getPriority());
        assertEquals(message + " RunningDuration", runningDuration,
                actualJob.getRunningDuration(), 0.001);
        assertEquals(message + " State", state, actualJob.getState());
        assertEqualsNullEmpty(message + " TemplateId", templateId,
                actualJob.getTemplateId());

        assertDateApproxEquals(message + " Created", created,
                actualJob.getCreated());
        assertDateApproxEquals(message + " LastModified", lastModified,
                actualJob.getLastModified());
        assertDateApproxEquals(message + " StartTime", startTime,
                actualJob.getStartTime());
        assertDateApproxEquals(message + " EndTime", endTime,
                actualJob.getEndTime());

        if (expectedTaskCount != null) {
            LinkInfo<TaskInfo> tasksLink = actualJob.getTasksLink();
            ListResult<TaskInfo> actualTasks = service.list(Task
                    .list(tasksLink));
            assertEquals(message + " tasks size", expectedTaskCount.intValue(),
                    actualTasks.size());
        }
    }

    private JobNotificationSubscription getJobNotificationSubscription(
            String jobNotificationSubscriptionId, TargetJobState targetJobState) {
        return new JobNotificationSubscription(jobNotificationSubscriptionId,
                targetJobState);
    }

    private JobInfo createJob(String name) throws ServiceException {
        return service.create(Job.create().setName(name).setPriority(3)
                .addInputMediaAsset(assetInfo.getId())
                .addTaskCreator(getTaskCreator(0)));
    }

    private CreateBatchOperation getTaskCreator(int outputAssetPosition) {
        return Task
                .create(MEDIA_ENCODER_MEDIA_PROCESSOR_ID,
                        "<taskBody>"
                                + "<inputAsset>JobInputAsset(0)</inputAsset>"
                                + "<outputAsset>JobOutputAsset("
                                + outputAssetPosition + ")</outputAsset>"
                                + "</taskBody>")
                .setConfiguration("H.264 256k DSL CBR")
                .setName("My encoding Task");
    }

    @BeforeClass
    public static void setup() throws Exception {
        IntegrationTestBase.setup();
        assetInfo = setupAssetWithFile();
    }

    @Test
    public void createJobSuccess() throws ServiceException {
        // Arrange
        String name = testJobPrefix + "createJobSuccess";
        int priority = 3;
        double duration = 0.0;
        JobState state = JobState.Queued;
        String templateId = null;
        Date created = new Date();
        Date lastModified = new Date();
        Date stateTime = null;
        Date endTime = null;

        // Act
        JobInfo actualJob = service.create(Job.create().setName(name)
                .setPriority(priority).addInputMediaAsset(assetInfo.getId())
                .addTaskCreator(getTaskCreator(0)));

        // Assert
        verifyJobProperties("actualJob", name, priority, duration, state,
                templateId, created, lastModified, stateTime, endTime, 1,
                actualJob);
    }

    @Test
    public void createJobWithFreshMediaContractSuccess() throws ServiceException {
        // Arrange
        String name = testJobPrefix + "createJobSuccess";
        int priority = 3;
        double duration = 0.0;
        JobState state = JobState.Queued;
        String templateId = null;
        Date created = new Date();
        Date lastModified = new Date();
        Date stateTime = null;
        Date endTime = null;

        MediaContract freshService = MediaService.create(config);

        // Act
        JobInfo actualJob = freshService.create(Job.create().setName(name)
                .setPriority(priority).addInputMediaAsset(assetInfo.getId())
                .addTaskCreator(getTaskCreator(0)));

        // Assert
        verifyJobProperties("actualJob", name, priority, duration, state,
                templateId, created, lastModified, stateTime, endTime, 1,
                actualJob);
    }

    @Test
    public void createJobWithNotificationSuccess() throws ServiceException {
        // Arrange
        String name = testJobPrefix + "createJobWithNotificationSuccess";
        String queueName = testQueuePrefix + "createjobwithnotificationsuccess";
        int priority = 3;
        double duration = 0.0;
        JobState state = JobState.Queued;
        String templateId = null;
        Date created = new Date();
        Date lastModified = new Date();
        Date stateTime = null;
        Date endTime = null;

        queueService.createQueue(queueName);
        String notificationEndPointName = UUID.randomUUID().toString();

        service.create(NotificationEndPoint.create(notificationEndPointName,
                EndPointType.AzureQueue, queueName));
        ListResult<NotificationEndPointInfo> listNotificationEndPointInfos = service
                .list(NotificationEndPoint.list());
        String notificationEndPointId = null;

        for (NotificationEndPointInfo notificationEndPointInfo : listNotificationEndPointInfos) {
            if (notificationEndPointInfo.getName().equals(
                    notificationEndPointName)) {
                notificationEndPointId = notificationEndPointInfo.getId();
            }
        }

        JobNotificationSubscription jobNotificationSubcription = getJobNotificationSubscription(
                notificationEndPointId, TargetJobState.All);

        Creator creator = Job.create().setName(name).setPriority(priority)
                .addInputMediaAsset(assetInfo.getId())
                .addTaskCreator(getTaskCreator(0))
                .addJobNotificationSubscription(jobNotificationSubcription);

        // Act
        JobInfo actualJob = service.create(creator);

        // Assert
        JobInfo pendingJobInfo = service.get(Job.get(actualJob.getId()));
        int retryCounter = 0;
        while (pendingJobInfo.getState() != JobState.Error
                && retryCounter < 100) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }
            pendingJobInfo = service.get(Job.get(actualJob.getId()));
            retryCounter++;
        }
        verifyJobProperties("actualJob", name, priority, duration, state,
                templateId, created, lastModified, stateTime, endTime, 1,
                actualJob);
        List<QueueMessage> queueMessages = queueService.peekMessages(queueName)
                .getQueueMessages();
        assertEquals(1, queueMessages.size());
    }

    @Test
    public void createJobTwoTasksSuccess() throws ServiceException {
        // Arrange
        String name = testJobPrefix + "createJobSuccess";
        int priority = 3;
        double duration = 0.0;
        JobState state = JobState.Queued;
        String templateId = null;
        Date created = new Date();
        Date lastModified = new Date();
        Date stateTime = null;
        Date endTime = null;
        List<CreateBatchOperation> tasks = new ArrayList<CreateBatchOperation>();
        tasks.add(getTaskCreator(0));
        tasks.add(getTaskCreator(1));

        // Act
        JobInfo actualJob = service.create(Job.create().setName(name)
                .setPriority(priority).addInputMediaAsset(assetInfo.getId())
                .addTaskCreator(tasks.get(0)).addTaskCreator(tasks.get(1)));

        // Assert
        verifyJobProperties("actualJob", name, priority, duration, state,
                templateId, created, lastModified, stateTime, endTime, 2,
                actualJob);
    }

    @Test
    public void getJobSuccess() throws ServiceException {
        // Arrange
        String name = testJobPrefix + "getJobSuccess";
        int priority = 3;
        double duration = 0.0;
        JobState state = JobState.Queued;
        String templateId = null;
        String jobId = createJob(name).getId();
        Date created = new Date();
        Date lastModified = new Date();
        Date stateTime = null;
        Date endTime = null;

        // Act
        JobInfo actualJob = service.get(Job.get(jobId));

        // Assert
        verifyJobProperties("actualJob", name, priority, duration, state,
                templateId, created, lastModified, stateTime, endTime, 1,
                actualJob);
    }

    @Test
    public void getJobInvalidIdFailed() throws ServiceException {
        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(400));
        service.get(Job.get(invalidId));
    }

    @Test
    public void listJobSuccess() throws ServiceException {
        // Arrange
        JobInfo jobInfo = createJob(testJobPrefix + "listJobSuccess");
        List<JobInfo> jobInfos = new ArrayList<JobInfo>();
        jobInfos.add(jobInfo);
        ListResult<JobInfo> expectedListJobsResult = new ListResult<JobInfo>(
                jobInfos);

        // Act
        ListResult<JobInfo> actualListJobResult = service.list(Job.list());

        // Assert
        verifyListResultContains("listJobs", expectedListJobsResult,
                actualListJobResult, new ComponentDelegate() {
                    @Override
                    public void verifyEquals(String message, Object expected,
                            Object actual) {
                        try {
                            verifyJobInfoEqual(message, (JobInfo) expected,
                                    (JobInfo) actual);
                        } catch (ServiceException e) {
                            fail(e.getMessage());
                        }
                    }
                });
    }

    @Test
    public void canListJobsWithOptions() throws ServiceException {
        String[] assetNameSuffixes = new String[] { "A", "B", "C", "D" };
        List<JobInfo> expectedJobs = new ArrayList<JobInfo>();
        for (String suffix : assetNameSuffixes) {
            JobInfo jobInfo = createJob(testJobPrefix + "assetListOptions"
                    + suffix);
            expectedJobs.add(jobInfo);
        }

        ListResult<JobInfo> listJobsResult = service.list(Job.list().setTop(2));

        // Assert
        assertEquals(2, listJobsResult.size());
    }

    @Test
    public void cancelJobSuccess() throws ServiceException {
        // Arrange
        JobInfo jobInfo = createJob(testJobPrefix + "cancelJobSuccess");

        // Act
        service.action(Job.cancel(jobInfo.getId()));

        // Assert
        JobInfo canceledJob = service.get(Job.get(jobInfo.getId()));
        assertThat(canceledJob.getState(), anyOf(is(JobState.Canceling), is(JobState.Canceled)));
    }

    @Test
    public void cancelJobFailedWithInvalidId() throws ServiceException {
        // Arrange
        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(400));

        // Act
        service.action(Job.cancel(invalidId));

        // Assert
    }

    @Test
    public void deleteJobSuccess() throws ServiceException,
            InterruptedException {
        // Arrange
        JobInfo jobInfo = createJob(testJobPrefix + "deleteJobSuccess");
        service.action(Job.cancel(jobInfo.getId()));
        JobInfo cancellingJobInfo = service.get(Job.get(jobInfo.getId()));
        int retryCounter = 0;
        while (cancellingJobInfo.getState() == JobState.Canceling
                && retryCounter < 100) {
            Thread.sleep(2000);
            cancellingJobInfo = service.get(Job.get(jobInfo.getId()));
            retryCounter++;
        }

        // Act
        service.delete(Job.delete(jobInfo.getId()));

        // Assert
        expectedException.expect(ServiceException.class);
        service.get(Job.get(jobInfo.getId()));

    }

    @Test
    public void deleteJobInvalidIdFail() throws ServiceException {
        // Arrange
        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(400));

        // Act
        service.delete(Job.delete(invalidId));

        // Assert
    }

    @Test
    public void canGetInputOutputAssetsFromJob() throws Exception {
        String name = testJobPrefix + "canGetInputOutputAssetsFromJob";
        int priority = 3;

        JobInfo actualJob = service.create(Job.create().setName(name)
                .setPriority(priority).addInputMediaAsset(assetInfo.getId())
                .addTaskCreator(getTaskCreator(0)));

        ListResult<AssetInfo> inputs = service.list(Asset.list(actualJob
                .getInputAssetsLink()));
        ListResult<AssetInfo> outputs = service.list(Asset.list(actualJob
                .getOutputAssetsLink()));

        assertEquals(1, inputs.size());
        assertEquals(assetInfo.getId(), inputs.get(0).getId());

        assertEquals(1, outputs.size());
        assertTrue(outputs.get(0).getName().contains(name));
    }

    @Test
    public void canGetTasksFromJob() throws Exception {
        String name = testJobPrefix + "canGetTaskAssetsFromJob";
        int priority = 3;

        JobInfo actualJob = service.create(Job.create().setName(name)
                .setPriority(priority).addInputMediaAsset(assetInfo.getId())
                .addTaskCreator(getTaskCreator(0)));

        ListResult<TaskInfo> tasks = service.list(Task.list(actualJob
                .getTasksLink()));

        assertEquals(1, tasks.size());
    }

    @Test
    public void canGetErrorDetailsFromTask() throws Exception {
        String name = testJobPrefix + "canGetErrorDetailsFromTask";

        JobInfo actualJob = service.create(Job.create().setName(name)
                .addInputMediaAsset(assetInfo.getId())
                .addTaskCreator(getTaskCreator(0)));

        JobInfo currentJobInfo = actualJob;
        int retryCounter = 0;
        while (currentJobInfo.getState().getCode() < 3 && retryCounter < 30) {
            Thread.sleep(10000);
            currentJobInfo = service.get(Job.get(actualJob.getId()));
            retryCounter++;
        }

        ListResult<TaskInfo> tasks = service.list(Task.list(actualJob
                .getTasksLink()));
        TaskInfo taskInfo = tasks.get(0);
        List<ErrorDetail> errorDetails = taskInfo.getErrorDetails();

        assertEquals(1, errorDetails.size());
        ErrorDetail errorDetail = errorDetails.get(0);
        assertNotNull(errorDetail.getCode());
        assertNotNull(errorDetail.getMessage());
    }

    @Test
    public void canGetInputOutputAssetsFromTask() throws Exception {
        String name = testJobPrefix + "canGetInputOutputAssetsFromTask";
        int priority = 3;

        JobInfo actualJob = service.create(Job.create().setName(name)
                .setPriority(priority).addInputMediaAsset(assetInfo.getId())
                .addTaskCreator(getTaskCreator(0)));

        ListResult<TaskInfo> tasks = service.list(Task.list(actualJob
                .getTasksLink()));
        ListResult<AssetInfo> inputs = service.list(Asset.list(tasks.get(0)
                .getInputAssetsLink()));
        ListResult<AssetInfo> outputs = service.list(Asset.list(tasks.get(0)
                .getOutputAssetsLink()));

        assertEquals(1, inputs.size());
        assertEquals(assetInfo.getId(), inputs.get(0).getId());

        assertEquals(1, outputs.size());
        assertTrue(outputs.get(0).getName().contains(name));
    }

    @Test
    public void canGetTaskHistoricalEventsFromTask() throws Exception {
        // Arrange
        String jobName = testJobPrefix + "canGetTaskHistoricalEventsFromTask";
        int priority = 3;
        int retryCounter = 0;

        // Act
        JobInfo actualJobInfo = service.create(Job.create().setName(jobName)
                .setPriority(priority).addInputMediaAsset(assetInfo.getId())
                .addTaskCreator(getTaskCreator(0)));

        while (actualJobInfo.getState().getCode() < 3 && retryCounter < 30) {
            Thread.sleep(10000);
            actualJobInfo = service.get(Job.get(actualJobInfo.getId()));
            retryCounter++;
        }
        ListResult<TaskInfo> tasks = service.list(Task.list(actualJobInfo
                .getTasksLink()));
        TaskInfo taskInfo = tasks.get(0);
        List<TaskHistoricalEvent> historicalEvents = taskInfo
                .getHistoricalEvents();
        TaskHistoricalEvent historicalEvent = historicalEvents.get(0);

        // Assert
        assertTrue(historicalEvents.size() >= 5);
        assertNotNull(historicalEvent.getCode());
        assertNotNull(historicalEvent.getTimeStamp());
        assertNull(historicalEvent.getMessage());
    }

}
