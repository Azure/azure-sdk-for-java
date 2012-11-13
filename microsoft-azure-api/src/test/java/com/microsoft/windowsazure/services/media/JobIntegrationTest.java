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

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.media.models.AssetInfo;
import com.microsoft.windowsazure.services.media.models.CreateAssetOptions;
import com.microsoft.windowsazure.services.media.models.CreateJobOptions;
import com.microsoft.windowsazure.services.media.models.CreateTaskOptions;
import com.microsoft.windowsazure.services.media.models.JobInfo;
import com.microsoft.windowsazure.services.media.models.ListJobsOptions;
import com.microsoft.windowsazure.services.media.models.ListJobsResult;

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
        CreateAssetOptions createAssetOptions = new CreateAssetOptions().setName(name);
        AssetInfo assetInfo = service.createAsset(createAssetOptions);
        CreateJobOptions createJobOptions = new CreateJobOptions();
        createJobOptions.setName("My Encoding Job");
        createJobOptions.setPriority(3);
        createJobOptions.addInputMediaAsset(assetInfo.getId());
        CreateTaskOptions createTaskOptions = new CreateTaskOptions();
        createTaskOptions.setConfiguration("H.264 256k DSL CBR");
        createTaskOptions.setMediaProcessorId("nb:mpid:UUID:2f381738-c504-4e4a-a38e-d199e207fcd5");
        createTaskOptions.setName("My encoding Task");
        createTaskOptions.setTaskBody(taskBody);

        List<CreateTaskOptions> createTaskOptionsList = new ArrayList<CreateTaskOptions>();
        createTaskOptionsList.add(createTaskOptions);
        return service.createJob(createJobOptions, createTaskOptionsList);
    }

    @Test
    public void createJobSuccess() throws Exception {
        // Arrange
        CreateAssetOptions createAssetOptions = new CreateAssetOptions();
        AssetInfo assetInfo = service.createAsset(createAssetOptions);
        CreateJobOptions createJobOptions = new CreateJobOptions();
        createJobOptions.setName("My Encoding Job");
        createJobOptions.setPriority(3);
        createJobOptions.addInputMediaAsset(assetInfo.getId());
        CreateTaskOptions createTaskOptions = new CreateTaskOptions();
        createTaskOptions.setConfiguration("H.264 256k DSL CBR");
        createTaskOptions.setMediaProcessorId("nb:mpid:UUID:2f381738-c504-4e4a-a38e-d199e207fcd5");
        createTaskOptions.setName("My encoding Task");
        createTaskOptions.setTaskBody(taskBody);

        List<CreateTaskOptions> createTaskOptionsList = new ArrayList<CreateTaskOptions>();
        createTaskOptionsList.add(createTaskOptions);
        JobInfo expectedJob = createJob("My EncodingJob");
        expectedJob.setName("My Encoding Job");
        expectedJob.setPriority(3);
        expectedJob.setRunningDuration(0.0);
        expectedJob.setState(0);

        // Act
        JobInfo actualJob = service.createJob(createJobOptions, createTaskOptionsList);

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
        JobInfo actualJob = service.getJob(jobId);

        // Assert
        verifyJobInfoEqual("actualJob", expectedJob, actualJob);
    }

    @Test
    public void getJobInvalidIdFailed() throws ServiceException {
        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(500));
        service.getJob(invalidId);
    }

    @Test
    public void listJobSuccess() throws ServiceException {
        // Arrange
        JobInfo jobInfo = createJob("listJobSuccess");
        List<JobInfo> jobInfos = new ArrayList<JobInfo>();
        ListJobsResult expectedListJobsResult = new ListJobsResult();
        jobInfos.add(jobInfo);
        expectedListJobsResult.setJobInfos(jobInfos);

        // Act
        ListJobsResult actualListJobResult = service.listJobs();

        // Assert
        verifyListResultContains("listJobs", expectedListJobsResult.getJobInfos(), actualListJobResult.getJobInfos(),
                new ComponentDelegate() {
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

        ListJobsOptions options = new ListJobsOptions();
        options.getQueryParameters().add("$top", "2");
        ListJobsResult listJobsResult = service.listJobs(options);

        // Assert
        assertEquals(2, listJobsResult.getJobInfos().size());
    }

    @Test
    public void cancelJobSuccess() throws Exception {
        // Arrange
        CreateAssetOptions createAssetOptions = new CreateAssetOptions();
        AssetInfo assetInfo = service.createAsset(createAssetOptions);
        CreateJobOptions createJobOptions = new CreateJobOptions();
        createJobOptions.setName("My Encoding Job");
        createJobOptions.setPriority(3);
        createJobOptions.addInputMediaAsset(assetInfo.getId());
        CreateTaskOptions createTaskOptions = new CreateTaskOptions();
        createTaskOptions.setConfiguration("H.264 256k DSL CBR");
        createTaskOptions.setMediaProcessorId("nb:mpid:UUID:2f381738-c504-4e4a-a38e-d199e207fcd5");
        createTaskOptions.setName("My encoding Task");

        createTaskOptions.setTaskBody(taskBody);
        List<CreateTaskOptions> createTaskOptionsList = new ArrayList<CreateTaskOptions>();
        createTaskOptionsList.add(createTaskOptions);
        JobInfo jobInfo = service.createJob(createJobOptions, createTaskOptionsList);

        // Act
        service.cancelJob(jobInfo.getId());

        // Assert
        JobInfo canceledJob = service.getJob(jobInfo.getId());
        assertEquals((Integer) 6, canceledJob.getState());

    }

    @Test
    public void cancelJobFailedWithInvalidId() throws ServiceException {
        // Arrange 
        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(500));

        // Act
        service.cancelJob(invalidId);

        // Assert
    }

    @Test
    public void deleteJobSuccess() throws ServiceException {
        // Arrange
        JobInfo jobInfo = createJob("deleteJobSuccess");
        service.cancelJob(jobInfo.getId());
        JobInfo cancellingJobInfo = service.getJob(jobInfo.getId());
        while (cancellingJobInfo.getState() == 6) {
            cancellingJobInfo = service.getJob(jobInfo.getId());
        }

        // Act 

        service.deleteJob(jobInfo.getId());

        // Assert
        expectedException.expect(ServiceException.class);
        service.getJob(jobInfo.getId());

    }

    @Test
    public void deleteJobIvalidIdFail() throws ServiceException {
        // Arrange 
        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(500));

        // Act
        service.deleteJob(invalidId);

        // Assert
    }
}
