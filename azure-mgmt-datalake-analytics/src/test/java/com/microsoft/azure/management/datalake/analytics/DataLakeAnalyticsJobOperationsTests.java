package com.microsoft.azure.management.datalake.analytics;

import com.microsoft.azure.management.datalake.analytics.implementation.api.DataLakeAnalyticsAccountInner;
import com.microsoft.azure.management.datalake.analytics.implementation.api.DataLakeAnalyticsAccountProperties;
import com.microsoft.azure.management.datalake.analytics.implementation.api.DataLakeStoreAccountInfoInner;
import com.microsoft.azure.management.datalake.analytics.implementation.api.JobInformationInner;
import com.microsoft.azure.management.datalake.analytics.implementation.api.JobResult;
import com.microsoft.azure.management.datalake.analytics.implementation.api.JobState;
import com.microsoft.azure.management.datalake.analytics.implementation.api.JobType;
import com.microsoft.azure.management.datalake.analytics.implementation.api.USqlJobProperties;
import com.microsoft.azure.management.datalake.store.implementation.api.DataLakeStoreAccountInner;
import com.microsoft.azure.management.resources.implementation.api.ResourceGroupInner;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DataLakeAnalyticsJobOperationsTests extends DataLakeAnalyticsManagementTestBase {
    private static String rgName = generateName("javaadlarg");
    private static String location;
    private static String adlsAcct = generateName("javaadlsacct");
    private static String adlaAcct = generateName("javaadlaacct");
    private static String jobScript = "DROP DATABASE IF EXISTS testdb; CREATE DATABASE testdb;";

    @BeforeClass
    public static void setup() throws Exception {
        createClients();
        location = environmentLocation;
        ResourceGroupInner group = new ResourceGroupInner();
        group.setLocation(location);
        resourceManagementClient.resourceGroups().createOrUpdate(rgName, group);
        // create storage and ADLS accounts, setting the accessKey
        DataLakeStoreAccountInner adlsAccount = new DataLakeStoreAccountInner();
        adlsAccount.setLocation(location);
        adlsAccount.setName(adlsAcct);
        dataLakeStoreAccountManagementClient.accounts().create(rgName, adlsAcct, adlsAccount);

        // Create the ADLA acct to use.
        DataLakeAnalyticsAccountProperties createProperties = new DataLakeAnalyticsAccountProperties();
        List<DataLakeStoreAccountInfoInner> adlsAccts = new ArrayList<DataLakeStoreAccountInfoInner>();
        DataLakeStoreAccountInfoInner adlsInfo = new DataLakeStoreAccountInfoInner();
        adlsInfo.setName(adlsAcct);
        adlsAccts.add(adlsInfo);

        createProperties.setDataLakeStoreAccounts(adlsAccts);
        createProperties.setDefaultDataLakeStoreAccount(adlsAcct);

        DataLakeAnalyticsAccountInner createParams = new DataLakeAnalyticsAccountInner();
        createParams.setLocation(location);
        createParams.setName(adlaAcct);
        createParams.setProperties(createProperties);
        dataLakeAnalyticsAccountManagementClient.accounts().create(rgName, adlaAcct, createParams);
        // Sleep for two minutes to ensure the account is totally provisioned.
        Thread.sleep(180000);
    }

    @AfterClass
    public static void cleanup() throws Exception {
        try {
            dataLakeAnalyticsAccountManagementClient.accounts().delete(rgName, adlaAcct);
            resourceManagementClient.resourceGroups().delete(rgName);
        }
        catch (Exception e) {
            // ignore failures during cleanup, as it is best effort
        }
    }
    @Test
    public void canSubmitGetListAndCancelJobs() throws Exception {
        // submit a job
        JobInformationInner jobToSubmit = new JobInformationInner();
        USqlJobProperties jobProperties = new USqlJobProperties();
        jobProperties.setScript(jobScript);
        jobToSubmit.setName("java azure sdk data lake analytics job");
        jobToSubmit.setDegreeOfParallelism(2);
        jobToSubmit.setType(JobType.USQL);
        jobToSubmit.setProperties(jobProperties);
        UUID jobId = UUID.randomUUID();
        UUID secondJobId = UUID.randomUUID();

        JobInformationInner jobCreateResponse = dataLakeAnalyticsJobManagementClient.jobs().create(adlaAcct, jobId, jobToSubmit).getBody();
        Assert.assertNotNull(jobCreateResponse);

        // cancel the job
        dataLakeAnalyticsJobManagementClient.jobs().cancel(adlaAcct, jobId);

        // Get the job and ensure it was cancelled
        JobInformationInner cancelledJobResponse = dataLakeAnalyticsJobManagementClient.jobs().get(adlaAcct, jobId).getBody();
        Assert.assertEquals(JobResult.CANCELLED, cancelledJobResponse.result());
        Assert.assertNotNull(cancelledJobResponse.errorMessage());
        Assert.assertTrue(cancelledJobResponse.errorMessage().size() >= 1);

        // Resubmit and wait for job to finish
        jobCreateResponse = dataLakeAnalyticsJobManagementClient.jobs().create(adlaAcct, secondJobId, jobToSubmit).getBody();
        Assert.assertNotNull(jobCreateResponse);

        JobInformationInner getJobResponse = dataLakeAnalyticsJobManagementClient.jobs().get(adlaAcct, jobCreateResponse.jobId()).getBody();
        Assert.assertNotNull(getJobResponse);

        int maxWaitInSeconds = 180; // 3 minutes should be long enough
        int curWaitInSeconds = 0;

        while (getJobResponse.state() != JobState.ENDED && curWaitInSeconds < maxWaitInSeconds)
        {
            // wait 5 seconds before polling again
            Thread.sleep(5000);
            curWaitInSeconds += 5;
            getJobResponse = dataLakeAnalyticsJobManagementClient.jobs().get(adlaAcct, jobCreateResponse.jobId()).getBody();
            Assert.assertNotNull(getJobResponse);
        }

        Assert.assertTrue(curWaitInSeconds <= maxWaitInSeconds);

        // Verify the job completes successfully
        Assert.assertTrue(
                String.format("Job: %s did not return success. Current job state: %s. Actual result: %s. Error (if any): %s",
                getJobResponse.jobId(), getJobResponse.state(), getJobResponse.result(), getJobResponse.errorMessage()),
                getJobResponse.state() == JobState.ENDED && getJobResponse.result() == JobResult.SUCCEEDED);

        List<JobInformationInner> listJobResponse = dataLakeAnalyticsJobManagementClient.jobs().list(adlaAcct, null, null, null, null, null, null, null, null, null).getBody();
        Assert.assertNotNull(listJobResponse);
        boolean foundJob = false;
        for(JobInformationInner eachJob : listJobResponse) {
            if (eachJob.jobId().equals(secondJobId)) {
                foundJob = true;
                break;
            }
        }

        Assert.assertTrue(foundJob);

        // Just compile the job, which requires a jobId in the job object.
        jobToSubmit.setJobId(getJobResponse.jobId());
        JobInformationInner compileResponse = dataLakeAnalyticsJobManagementClient.jobs().build(adlaAcct, jobToSubmit).getBody();
        Assert.assertNotNull(compileResponse);

        // list the jobs both with a hand crafted query string and using the parameters
        listJobResponse = dataLakeAnalyticsJobManagementClient.jobs().list(adlaAcct, null, null, null, null, "jobId", null, null, null, null).getBody();
        Assert.assertNotNull(listJobResponse);

        foundJob = false;
        for(JobInformationInner eachJob : listJobResponse) {
            if (eachJob.jobId().equals(secondJobId)) {
                foundJob = true;
                break;
            }
        }

        Assert.assertTrue(foundJob);
    }
}
