/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.analytics;

import com.microsoft.azure.management.datalake.analytics.models.JobInformation;
import com.microsoft.azure.management.datalake.analytics.models.JobResult;
import com.microsoft.azure.management.datalake.analytics.models.JobState;
import com.microsoft.azure.management.datalake.analytics.models.JobType;
import com.microsoft.azure.management.datalake.analytics.models.USqlJobProperties;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

public class DataLakeAnalyticsJobOperationsTests extends DataLakeAnalyticsManagementTestBase {
    private static String jobScript = "DROP DATABASE IF EXISTS testdb; CREATE DATABASE testdb;";
    
    @Test
    public void canSubmitGetListAndCancelJobs() throws Exception {
        // submit a job
        JobInformation jobToSubmit = new JobInformation();
        USqlJobProperties jobProperties = new USqlJobProperties();
        jobProperties.withScript(jobScript);
        jobToSubmit.withName("java azure sdk data lake analytics job");
        jobToSubmit.withDegreeOfParallelism(2);
        jobToSubmit.withType(JobType.USQL);
        jobToSubmit.withProperties(jobProperties);
        // define two static IDs for use with recordings.
        UUID mockedId = UUID.fromString("123a9b88-d8cf-4a5a-9546-882cde67476b");
        UUID mockedId2 = UUID.fromString("b422b92a-ff47-4324-bea1-1d98cb09cce4");
        UUID jobId;
        UUID secondJobId;

        if (isRecordMode()) {
            jobId = UUID.randomUUID();
            secondJobId = UUID.randomUUID();
            addTextReplacementRule(jobId.toString(), mockedId.toString());
            addTextReplacementRule(secondJobId.toString(), mockedId2.toString());
        }
        else {
            jobId = mockedId;
            secondJobId = mockedId2;
        }

        JobInformation jobCreateResponse = dataLakeAnalyticsJobManagementClient.jobs().create(jobAndCatalogAdlaName, jobId, jobToSubmit);
        Assert.assertNotNull(jobCreateResponse);

        // cancel the job
        dataLakeAnalyticsJobManagementClient.jobs().cancel(jobAndCatalogAdlaName, jobId);

        // Get the job and ensure it was cancelled
        JobInformation cancelledJobResponse = dataLakeAnalyticsJobManagementClient.jobs().get(jobAndCatalogAdlaName, jobId);
        Assert.assertEquals(JobResult.CANCELLED, cancelledJobResponse.result());
        Assert.assertNotNull(cancelledJobResponse.errorMessage());
        Assert.assertTrue(cancelledJobResponse.errorMessage().size() >= 1);

        // Resubmit and wait for job to finish
        jobCreateResponse = dataLakeAnalyticsJobManagementClient.jobs().create(jobAndCatalogAdlaName, secondJobId, jobToSubmit);
        Assert.assertNotNull(jobCreateResponse);

        JobInformation getJobResponse = dataLakeAnalyticsJobManagementClient.jobs().get(jobAndCatalogAdlaName, jobCreateResponse.jobId());
        Assert.assertNotNull(getJobResponse);

        int maxWaitInSeconds = 180; // 3 minutes should be long enough
        int curWaitInSeconds = 0;

        while (getJobResponse.state() != JobState.ENDED && curWaitInSeconds < maxWaitInSeconds)
        {
            // wait 5 seconds before polling again
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

        List<JobInformation> listJobResponse = dataLakeAnalyticsJobManagementClient.jobs().list(jobAndCatalogAdlaName);
        Assert.assertNotNull(listJobResponse);
        boolean foundJob = false;
        for(JobInformation eachJob : listJobResponse) {
            if (eachJob.jobId().equals(secondJobId)) {
                foundJob = true;
                break;
            }
        }

        Assert.assertTrue(foundJob);

        // Just compile the job, which requires a jobId in the job object.
        JobInformation compileResponse = dataLakeAnalyticsJobManagementClient.jobs().build(jobAndCatalogAdlaName, jobToSubmit);
        Assert.assertNotNull(compileResponse);

        // list the jobs both with a hand crafted query string and using the parameters
        listJobResponse = dataLakeAnalyticsJobManagementClient.jobs().list(jobAndCatalogAdlaName, null, null, null, null,"jobId", null);
        Assert.assertNotNull(listJobResponse);

        foundJob = false;
        for(JobInformation eachJob : listJobResponse) {
            if (eachJob.jobId().equals(secondJobId)) {
                foundJob = true;
                break;
            }
        }

        Assert.assertTrue(foundJob);
    }
}
