/**
 * Copyright 2012 Microsoft Corporation
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

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.core.MultivaluedMap;

import org.junit.BeforeClass;
import org.junit.Test;

import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.media.models.AccessPolicy;
import com.microsoft.windowsazure.services.media.models.AccessPolicyInfo;
import com.microsoft.windowsazure.services.media.models.AccessPolicyPermission;
import com.microsoft.windowsazure.services.media.models.Asset;
import com.microsoft.windowsazure.services.media.models.AssetFile;
import com.microsoft.windowsazure.services.media.models.AssetInfo;
import com.microsoft.windowsazure.services.media.models.Job;
import com.microsoft.windowsazure.services.media.models.JobInfo;
import com.microsoft.windowsazure.services.media.models.JobState;
import com.microsoft.windowsazure.services.media.models.ListResult;
import com.microsoft.windowsazure.services.media.models.LocatorInfo;
import com.microsoft.windowsazure.services.media.models.Task;
import com.microsoft.windowsazure.services.media.models.Task.CreateBatchOperation;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class JobIntegrationTest extends IntegrationTestBase {

    private static AssetInfo assetInfo;
    private static final byte[] testBlobData = new byte[] { 0, 1, 2 };

    private void verifyJobInfoEqual(String message, JobInfo expected, JobInfo actual) {
        verifyJobProperties(message, expected.getName(), expected.getPriority(), expected.getRunningDuration(),
                expected.getState(), expected.getTemplateId(), expected.getCreated(), expected.getLastModified(),
                expected.getStartTime(), expected.getEndTime(), expected.getInputMediaAssets(),
                expected.getOutputMediaAssets(), actual);
    }

    private void verifyJobProperties(String message, String testName, Integer priority, Double runningDuration,
            JobState state, String templateId, Date created, Date lastModified, Date startTime, Date endTime,
            List<String> inputMediaAssets, List<String> outputMediaAssets, JobInfo actualJob) {
        assertNotNull(message, actualJob);

        assertNotNull(message + "Id", actualJob.getId());

        assertEquals(message + " Name", testName, actualJob.getName());
        // comment out due to issue 464
        // assertEquals(message + " Priority", priority, actualJob.getPriority());
        assertEquals(message + " RunningDuration", runningDuration, actualJob.getRunningDuration());
        assertEquals(message + " State", state, actualJob.getState());
        assertEqualsNullEmpty(message + " TemplateId", templateId, actualJob.getTemplateId());

        assertDateApproxEquals(message + " Created", created, actualJob.getCreated());
        assertDateApproxEquals(message + " LastModified", lastModified, actualJob.getLastModified());
        assertDateApproxEquals(message + " StartTime", startTime, actualJob.getStartTime());
        assertDateApproxEquals(message + " EndTime", endTime, actualJob.getEndTime());

        // TODO: Add test for accessing the input and output media assets when fixed:
        // https://github.com/WindowsAzure/azure-sdk-for-java-pr/issues/508
        assertEquals(message + " InputMediaAssets", inputMediaAssets, actualJob.getInputMediaAssets());
        assertEquals(message + " OutputMediaAssets", outputMediaAssets, actualJob.getOutputMediaAssets());

        // TODO: Add test for accessing the tasks when fixed:
        // https://github.com/WindowsAzure/azure-sdk-for-java-pr/issues/531
    }

    private void assertEqualsNullEmpty(String message, String expected, String actual) {
        if ((expected == null || expected.length() == 0) && (actual == null || actual.length() == 0)) {
            // both nullOrEmpty, so match.
        }
        else {
            assertEquals(message, expected, actual);
        }
    }

    private JobInfo createJob(String name) throws ServiceException {
        URI serviceUri = service.getRestServiceUri();
        return service.create(Job.create(serviceUri).setName(name).setPriority(3).addInputMediaAsset(assetInfo.getId())
                .addTaskCreator(getTaskCreator(0)));
    }

    private CreateBatchOperation getTaskCreator(int outputAssetPosition) {
        return Task
                .create()
                .setConfiguration("H.264 256k DSL CBR")
                .setMediaProcessorId("nb:mpid:UUID:2f381738-c504-4e4a-a38e-d199e207fcd5")
                .setName("My encoding Task")
                .setTaskBody(
                        "<taskBody>" + "<inputAsset>JobInputAsset(0)</inputAsset>" + "<outputAsset>JobOutputAsset("
                                + outputAssetPosition + ")</outputAsset>" + "</taskBody>");
    }

    @BeforeClass
    public static void setup() throws Exception {
        IntegrationTestBase.setup();

        String name = UUID.randomUUID().toString();
        String testBlobName = "test" + name + ".bin";
        assetInfo = service.create(Asset.create().setName(testAssetPrefix + name));

        AccessPolicyInfo accessPolicyInfo = service.create(AccessPolicy.create(testPolicyPrefix + name, 10,
                EnumSet.of(AccessPolicyPermission.WRITE)));
        LocatorInfo locator = createLocator(accessPolicyInfo, assetInfo, 5, 10);
        WritableBlobContainerContract blobWriter = service.createBlobWriter(locator);
        InputStream blobContent = new ByteArrayInputStream(testBlobData);
        blobWriter.createBlockBlob(testBlobName, blobContent);

        service.action(AssetFile.createFileInfos(assetInfo.getId()));
    }

    @Test
    public void createJobSuccess() throws Exception {
        // Arrange
        String name = testJobPrefix + "createJobSuccess";
        int priority = 3;
        double duration = 0.0;
        JobState state = JobState.Queued;
        String templateId = null;
        List<String> inputMediaAssets = null;
        List<String> outputMediaAssets = null;
        Date created = new Date();
        Date lastModified = new Date();
        Date stateTime = null;
        Date endTime = null;

        // Act
        JobInfo actualJob = service.create(Job.create(service.getRestServiceUri()).setName(name).setPriority(priority)
                .addInputMediaAsset(assetInfo.getId()).addTaskCreator(getTaskCreator(0)));

        // Assert
        verifyJobProperties("actualJob", name, priority, duration, state, templateId, created, lastModified, stateTime,
                endTime, inputMediaAssets, outputMediaAssets, actualJob);
    }

    @Test
    public void createJobTwoTasksSuccess() throws Exception {
        // Arrange
        String name = testJobPrefix + "createJobSuccess";
        int priority = 3;
        double duration = 0.0;
        JobState state = JobState.Queued;
        String templateId = null;
        List<String> inputMediaAssets = null;
        List<String> outputMediaAssets = null;
        Date created = new Date();
        Date lastModified = new Date();
        Date stateTime = null;
        Date endTime = null;
        List<CreateBatchOperation> tasks = new ArrayList<CreateBatchOperation>();
        tasks.add(getTaskCreator(0));
        tasks.add(getTaskCreator(1));

        // Act
        JobInfo actualJob = service.create(Job.create(service.getRestServiceUri()).setName(name).setPriority(priority)
                .addInputMediaAsset(assetInfo.getId()).addTaskCreator(tasks.get(0)).addTaskCreator(tasks.get(1)));

        // Assert
        verifyJobProperties("actualJob", name, priority, duration, state, templateId, created, lastModified, stateTime,
                endTime, inputMediaAssets, outputMediaAssets, actualJob);
    }

    @Test
    public void getJobSuccess() throws Exception {
        // Arrange
        String name = testJobPrefix + "getJobSuccess";
        int priority = 3;
        double duration = 0.0;
        JobState state = JobState.Queued;
        String templateId = null;
        List<String> inputMediaAssets = null;
        List<String> outputMediaAssets = null;
        String jobId = createJob(name).getId();
        Date created = new Date();
        Date lastModified = new Date();
        Date stateTime = null;
        Date endTime = null;

        // Act
        JobInfo actualJob = service.get(Job.get(jobId));

        // Assert
        verifyJobProperties("actualJob", name, priority, duration, state, templateId, created, lastModified, stateTime,
                endTime, inputMediaAssets, outputMediaAssets, actualJob);
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
        ListResult<JobInfo> expectedListJobsResult = new ListResult<JobInfo>(jobInfos);

        // Act
        ListResult<JobInfo> actualListJobResult = service.list(Job.list());

        // Assert
        verifyListResultContains("listJobs", expectedListJobsResult, actualListJobResult, new ComponentDelegate() {
            @Override
            public void verifyEquals(String message, Object expected, Object actual) {
                verifyJobInfoEqual(message, (JobInfo) expected, (JobInfo) actual);
            }
        });
    }

    @Test
    public void canListJobsWithOptions() throws ServiceException {
        String[] assetNameSuffixes = new String[] { "A", "B", "C", "D" };
        List<JobInfo> expectedJobs = new ArrayList<JobInfo>();
        for (String suffix : assetNameSuffixes) {
            JobInfo jobInfo = createJob(testJobPrefix + "assetListOptions" + suffix);
            expectedJobs.add(jobInfo);
        }

        MultivaluedMap<String, String> queryParameters = new MultivaluedMapImpl();
        queryParameters.add("$top", "2");
        ListResult<JobInfo> listJobsResult = service.list(Job.list(queryParameters));

        // Assert
        assertEquals(2, listJobsResult.size());
    }

    @Test
    public void cancelJobSuccess() throws Exception {
        // Arrange
        JobInfo jobInfo = createJob(testJobPrefix + "cancelJobSuccess");

        // Act
        service.action(Job.cancel(jobInfo.getId()));

        // Assert
        JobInfo canceledJob = service.get(Job.get(jobInfo.getId()));
        assertEquals(JobState.Canceling, canceledJob.getState());

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
    public void deleteJobSuccess() throws ServiceException {
        // Arrange
        JobInfo jobInfo = createJob(testJobPrefix + "deleteJobSuccess");
        service.action(Job.cancel(jobInfo.getId()));
        JobInfo cancellingJobInfo = service.get(Job.get(jobInfo.getId()));
        while (cancellingJobInfo.getState() == JobState.Canceling) {
            cancellingJobInfo = service.get(Job.get(jobInfo.getId()));
        }

        // Act 
        service.delete(Job.delete(jobInfo.getId()));

        // Assert
        expectedException.expect(ServiceException.class);
        service.get(Job.get(jobInfo.getId()));

    }

    @Test
    public void deleteJobIvalidIdFail() throws ServiceException {
        // Arrange 
        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(400));

        // Act
        service.delete(Job.delete(invalidId));

        // Assert
    }
}
