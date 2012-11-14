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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

import org.junit.Test;

import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.media.models.Asset;
import com.microsoft.windowsazure.services.media.models.AssetInfo;
import com.microsoft.windowsazure.services.media.models.Job;
import com.microsoft.windowsazure.services.media.models.JobInfo;
import com.microsoft.windowsazure.services.media.models.ListResult;
import com.microsoft.windowsazure.services.media.models.Task;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class JobIntegrationTest extends IntegrationTestBase {

    private final String testJobPrefix = "testJobPrefix";
    private final String taskBody = "<?xml version=\"1.0\" encoding=\"utf-8\"?><taskBody>"
            + "<inputAsset>JobInputAsset(0)</inputAsset><outputAsset>JobOutputAsset(0)</outputAsset>" + "</taskBody>";

    private void verifyJobInfoEqual(String message, JobInfo expected, JobInfo actual) {
        verifyJobProperties(message, expected.getName(), expected.getPriority(), expected.getRunningDuration(),
                expected.getState(), expected.getTemplateId(), expected.getInputMediaAssets(),
                expected.getOutputMediaAssets(), actual);
    }

    private void verifyJobProperties(String message, String testName, Integer priority, Double runningDuration,
            Integer state, String templateId, List<String> inputMediaAssets, List<String> outputMediaAssets,
            JobInfo actualJob) {
        assertNotNull(message, actualJob);
        assertEquals(message + " Name", testName, actualJob.getName());
        // comment out due to issue 464
        // assertEquals(message + " Priority", priority, actualJob.getPriority());
        assertEquals(message + " RunningDuration", runningDuration, actualJob.getRunningDuration());
        assertEquals(message + " State", state, actualJob.getState());
        // commented out due to issue 463
        // assertEquals(message + " TemplateId", templateId, actualJob.getTemplateId());
        assertEquals(message + " InputMediaAssets", inputMediaAssets, actualJob.getInputMediaAssets());
        assertEquals(message + " OutputMediaAssets", outputMediaAssets, actualJob.getOutputMediaAssets());
    }

    private JobInfo createJob(String name) throws ServiceException {
        AssetInfo assetInfo = service.create(Asset.create().setName(name));
        URI serviceUri = service.getRestServiceUri();
        return service.create(Job
                .create(serviceUri)
                .setName("My Encoding Job")
                .setPriority(3)
                .addInputMediaAsset(assetInfo.getId())
                .addTaskCreator(
                        Task.create().setConfiguration("H.264 256k DSL CBR")
                                .setMediaProcessorId("nb:mpid:UUID:2f381738-c504-4e4a-a38e-d199e207fcd5")
                                .setName("My encoding Task").setTaskBody(taskBody)));
    }

    @Test
    public void createJobSuccess() throws Exception {
        // Arrange
        AssetInfo assetInfo = service.create(Asset.create());

        JobInfo expectedJob = new JobInfo();
        expectedJob.setName("My Encoding Job");
        expectedJob.setPriority(3);
        expectedJob.setRunningDuration(0.0);
        expectedJob.setState(0);
        URI serviceURI = service.getRestServiceUri();

        // Act
        JobInfo actualJob = service.create(Job
                .create(serviceURI)
                .setName("My Encoding Job")
                .setPriority(3)
                .addInputMediaAsset(assetInfo.getId())
                .addTaskCreator(
                        Task.create().setConfiguration("H.264 256k DSL CBR")
                                .setMediaProcessorId("nb:mpid:UUID:2f381738-c504-4e4a-a38e-d199e207fcd5")
                                .setName("My encoding Task").setTaskBody(taskBody)));

        // Assert
        verifyJobInfoEqual("actualJob", expectedJob, actualJob);
    }

    @Test
    public void getJobSuccess() throws Exception {
        // Arrange

        JobInfo expectedJob = new JobInfo();
        expectedJob.setName("My Encoding Job");
        expectedJob.setPriority(3);
        expectedJob.setRunningDuration(0.0);
        expectedJob.setState(0);
        String jobId = createJob("getJobSuccess").getId();

        // Act
        JobInfo actualJob = service.get(Job.get(jobId));

        // Assert
        verifyJobInfoEqual("actualJob", expectedJob, actualJob);
    }

    @Test
    public void getJobInvalidIdFailed() throws ServiceException {
        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(500));
        service.get(Job.get(invalidId));
    }

    @Test
    public void listJobSuccess() throws ServiceException {
        // Arrange
        JobInfo jobInfo = createJob("listJobSuccess");
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
        String[] assetNames = new String[] { testJobPrefix + "assetListOptionsA", testJobPrefix + "assetListOptionsB",
                testJobPrefix + "assetListOptionsC", testJobPrefix + "assetListOptionsD" };
        List<JobInfo> expectedJobs = new ArrayList<JobInfo>();
        for (int i = 0; i < assetNames.length; i++) {
            String name = assetNames[i];
            JobInfo jobInfo = createJob(name);
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
        JobInfo jobInfo = createJob("cancelJobSuccess");

        // Act
        service.action(Job.cancel(jobInfo.getId()));

        // Assert
        JobInfo canceledJob = service.get(Job.get(jobInfo.getId()));
        assertEquals((Integer) 6, canceledJob.getState());

    }

    @Test
    public void cancelJobFailedWithInvalidId() throws ServiceException {
        // Arrange 
        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(500));

        // Act
        service.action(Job.cancel(invalidId));

        // Assert
    }

    @Test
    public void deleteJobSuccess() throws ServiceException {
        // Arrange
        JobInfo jobInfo = createJob("deleteJobSuccess");
        service.action(Job.cancel(jobInfo.getId()));
        JobInfo cancellingJobInfo = service.get(Job.get(jobInfo.getId()));
        while (cancellingJobInfo.getState() == 6) {
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
        expectedException.expect(new ServiceExceptionMatcher(500));

        // Act
        service.delete(Job.delete(invalidId));

        // Assert
    }
}
