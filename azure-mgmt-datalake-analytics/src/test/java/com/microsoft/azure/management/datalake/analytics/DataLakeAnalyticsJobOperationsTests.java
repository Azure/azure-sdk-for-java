/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.analytics;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.datalake.analytics.models.*;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

public class DataLakeAnalyticsJobOperationsTests extends DataLakeAnalyticsManagementTestBase {

    @Test
    public void canSubmitGetListAndCancelJobs() throws Exception {
        // Define two static IDs for use with recordings
        UUID mockedId = UUID.fromString("123a9b88-d8cf-4a5a-9546-882cde67476b");
        UUID mockedId2 = UUID.fromString("b422b92a-ff47-4324-bea1-1d98cb09cce4");
        UUID mockedId3 = UUID.fromString("94521484-9d14-452d-9330-d18d306d78c5");
        UUID mockedId4 = UUID.fromString("5247eb29-9060-4f58-9f61-b63fab547c58");
        UUID mockedId5 = UUID.fromString("4f30649a-95f9-4595-94fe-0a35d7dd0eec");
        UUID mockedId6 = UUID.fromString("204d9164-4e43-48ec-8651-3e13110dfbb4");
        UUID jobId;
        UUID jobId2;
        UUID pipelineId;
        UUID recurrenceId;
        UUID runId;
        UUID runId2;

        if (isRecordMode()) {
            jobId = UUID.fromString(SdkContext.randomUuid());
            jobId2 = UUID.fromString(SdkContext.randomUuid());;
            pipelineId = UUID.fromString(SdkContext.randomUuid());
            runId = UUID.fromString(SdkContext.randomUuid());
            runId2 = UUID.fromString(SdkContext.randomUuid());
            recurrenceId = UUID.fromString(SdkContext.randomUuid());
            addTextReplacementRule(jobId.toString(), mockedId.toString());
            addTextReplacementRule(jobId2.toString(), mockedId2.toString());
            addTextReplacementRule(pipelineId.toString(), mockedId3.toString());
            addTextReplacementRule(recurrenceId.toString(), mockedId4.toString());
            addTextReplacementRule(runId.toString(), mockedId5.toString());
            addTextReplacementRule(runId2.toString(), mockedId6.toString());
        }
        else {
            jobId = mockedId;
            jobId2 = mockedId2;
            pipelineId = mockedId3;
            recurrenceId = mockedId4;
            runId = mockedId5;
            runId2 = mockedId6;
        }

        // Submit a job
        CreateJobParameters jobToSubmit = new CreateJobParameters();
        jobToSubmit.withName("java azure sdk data lake analytics job");
        jobToSubmit.withDegreeOfParallelism(2);
        jobToSubmit.withType(JobType.USQL);

        CreateUSqlJobProperties jobProperties = new CreateUSqlJobProperties();
        jobProperties.withScript("DROP DATABASE IF EXISTS testdb; CREATE DATABASE testdb;");

        JobRelationshipProperties jobRelated = new JobRelationshipProperties();
        jobRelated.withPipelineId(pipelineId);
        jobRelated.withPipelineName("pipeline");
        jobRelated.withPipelineUri("https://pipelineuri.contoso.com/myJob");
        jobRelated.withRecurrenceId(recurrenceId);
        jobRelated.withRecurrenceName("recurrence");
        jobRelated.withRunId(runId);

        jobToSubmit.withProperties(jobProperties);
        jobToSubmit.withRelated(jobRelated);

		// Wait for 5 minutes for the server to restore the account cache
		// Without this, the test will pass non-deterministically
        SdkContext.sleep(300000);

        JobInformation jobCreateResponse = dataLakeAnalyticsJobManagementClient.jobs().create(jobAndCatalogAdlaName, jobId, jobToSubmit);
        Assert.assertNotNull(jobCreateResponse);

        // Cancel the job
        dataLakeAnalyticsJobManagementClient.jobs().cancel(jobAndCatalogAdlaName, jobId);

        // Get the job and ensure it was cancelled
        JobInformation cancelledJobResponse = dataLakeAnalyticsJobManagementClient.jobs().get(jobAndCatalogAdlaName, jobId);
        Assert.assertEquals(JobResult.CANCELLED, cancelledJobResponse.result());
        Assert.assertNotNull(cancelledJobResponse.errorMessage());
        Assert.assertTrue(cancelledJobResponse.errorMessage().size() >= 1);

        // Resubmit and wait for job to finish
        // First update the runId to a new run
        jobToSubmit.related().withRunId(runId2);
        jobCreateResponse = dataLakeAnalyticsJobManagementClient.jobs().create(jobAndCatalogAdlaName, jobId2, jobToSubmit);
        Assert.assertNotNull(jobCreateResponse);

        // Poll the job until it finishes
        JobInformation getJobResponse = dataLakeAnalyticsJobManagementClient.jobs().get(jobAndCatalogAdlaName, jobCreateResponse.jobId());
        Assert.assertNotNull(getJobResponse);

        // 3 minutes should be long enough
        int maxWaitInSeconds = 180;
        int curWaitInSeconds = 0;

        while (getJobResponse.state() != JobState.ENDED && curWaitInSeconds < maxWaitInSeconds)
        {
            // Wait 5 seconds before polling again
            SdkContext.sleep(5 * 1000);
            curWaitInSeconds += 5;
            getJobResponse = dataLakeAnalyticsJobManagementClient.jobs().get(jobAndCatalogAdlaName, jobCreateResponse.jobId());
            Assert.assertNotNull(getJobResponse);
        }

        Assert.assertTrue(curWaitInSeconds <= maxWaitInSeconds);

        // Verify the job completes successfully
        Assert.assertTrue(
                String.format("Job: %s did not return success. Current job state: %s. Actual result: %s. Error (if any): %s",
                getJobResponse.jobId(), getJobResponse.state(), getJobResponse.result(), getJobResponse.errorMessage()),
                getJobResponse.state() == JobState.ENDED && getJobResponse.result() == JobResult.SUCCEEDED);

        // Make sure the job is in the list of jobs
        PagedList<JobInformationBasic> listJobResponse = dataLakeAnalyticsJobManagementClient.jobs().list(jobAndCatalogAdlaName);
        Assert.assertNotNull(listJobResponse);
        boolean foundJob = false;
        for (JobInformationBasic eachJob : listJobResponse) {
            if (eachJob.jobId().equals(jobId2)) {
                foundJob = true;
                break;
            }
        }

        Assert.assertTrue(foundJob);

        // Validate job relationship retrieval (get/list pipeline and get/list recurrence)
        // Get/List pipeline
        JobPipelineInformation getPipelineResponse = dataLakeAnalyticsJobManagementClient.pipelines().get(jobAndCatalogAdlaName, pipelineId);
        Assert.assertEquals(pipelineId, getPipelineResponse.pipelineId());
        Assert.assertEquals("pipeline", getPipelineResponse.pipelineName());
        Assert.assertEquals("https://pipelineuri.contoso.com/myJob", getPipelineResponse.pipelineUri());
        Assert.assertTrue(getPipelineResponse.runs().size() >= 2);

        PagedList<JobPipelineInformation> listPipelineResponse = dataLakeAnalyticsJobManagementClient.pipelines().list(jobAndCatalogAdlaName);
        Assert.assertEquals(1, listPipelineResponse.size());
        boolean foundPipeline = false;
        for (JobPipelineInformation eachPipeline : listPipelineResponse) {
            if (eachPipeline.pipelineId().equals(pipelineId)) {
                foundPipeline = true;
                break;
            }
        }

        Assert.assertTrue(foundPipeline);

        // Get/List recurrence
        JobRecurrenceInformation getRecurrenceResponse = dataLakeAnalyticsJobManagementClient.recurrences().get(jobAndCatalogAdlaName, recurrenceId);
        Assert.assertEquals(recurrenceId, getRecurrenceResponse.recurrenceId());
        Assert.assertEquals("recurrence", getRecurrenceResponse.recurrenceName());

        PagedList<JobRecurrenceInformation> listRecurrenceResponse = dataLakeAnalyticsJobManagementClient.recurrences().list(jobAndCatalogAdlaName);
        Assert.assertEquals(1, listRecurrenceResponse.size());
        boolean foundRecurrence = false;
        for (JobRecurrenceInformation eachRecurrence : listRecurrenceResponse) {
            if (eachRecurrence.recurrenceId().equals(recurrenceId)) {
                foundRecurrence = true;
                break;
            }
        }

        Assert.assertTrue(foundRecurrence);

        // Build a job
        BuildJobParameters jobToBuild = new BuildJobParameters();
        jobToBuild.withName("java azure sdk data lake analytics job");
        jobToBuild.withType(JobType.USQL);

        jobProperties = new CreateUSqlJobProperties();
        jobProperties.withScript("DROP DATABASE IF EXISTS testdb; CREATE DATABASE testdb;");

        jobToBuild.withProperties(jobProperties);

        // Just compile the job, which requires a jobId in the job object.
        JobInformation compileResponse = dataLakeAnalyticsJobManagementClient.jobs().build(jobAndCatalogAdlaName, jobToBuild);
        Assert.assertNotNull(compileResponse);

        // Now compile a broken job and verify diagnostics report an error
        jobToBuild.properties().withScript("DROP DATABASE IF EXIST FOO; CREATE DATABASE FOO;");
        compileResponse = dataLakeAnalyticsJobManagementClient.jobs().build(jobAndCatalogAdlaName, jobToBuild);
        Assert.assertNotNull(compileResponse);

        Assert.assertEquals(1, ((USqlJobProperties) compileResponse.properties()).diagnostics().size());
        Assert.assertEquals(SeverityTypes.ERROR, ((USqlJobProperties) compileResponse.properties()).diagnostics().get(0).severity());
        Assert.assertEquals(18, (int) ((USqlJobProperties) compileResponse.properties()).diagnostics().get(0).columnNumber());
        Assert.assertEquals(22, (int) ((USqlJobProperties) compileResponse.properties()).diagnostics().get(0).end());
        Assert.assertEquals(17, (int) ((USqlJobProperties) compileResponse.properties()).diagnostics().get(0).start());
        Assert.assertEquals(1, (int) ((USqlJobProperties) compileResponse.properties()).diagnostics().get(0).lineNumber());
        Assert.assertTrue(((USqlJobProperties) compileResponse.properties()).diagnostics().get(0).message().contains("E_CSC_USER_SYNTAXERROR"));

        // TODO: re-enable this when the server side is fixed
        // List the jobs both with a hand crafted query string and using the parameters
        /**
        listJobResponse = dataLakeAnalyticsJobManagementClient.jobs().list(jobAndCatalogAdlaName, null, null, null, "jobId",null, null);
        Assert.assertNotNull(listJobResponse);

        foundJob = false;
        for (JobInformationBasic eachJob : listJobResponse) {
            if (eachJob.jobId().equals(jobId2)) {
                foundJob = true;
                break;
            }
        }

        Assert.assertTrue(foundJob);
        **/
    }
}
