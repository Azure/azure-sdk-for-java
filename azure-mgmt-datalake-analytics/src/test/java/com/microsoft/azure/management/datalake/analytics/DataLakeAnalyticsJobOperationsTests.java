package com.microsoft.azure.management.datalake.analytics;

import com.microsoft.azure.management.datalake.analytics.models.DataLakeAnalyticsAccount;
import com.microsoft.azure.management.datalake.analytics.models.DataLakeStoreAccountInfo;
import com.microsoft.azure.management.datalake.analytics.models.JobInformation;
import com.microsoft.azure.management.datalake.analytics.models.JobResult;
import com.microsoft.azure.management.datalake.analytics.models.JobState;
import com.microsoft.azure.management.datalake.analytics.models.JobType;
import com.microsoft.azure.management.datalake.analytics.models.USqlJobProperties;
import com.microsoft.azure.management.datalake.store.models.DataLakeStoreAccount;
import com.microsoft.azure.management.resources.implementation.ResourceGroupInner;
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
        group.withLocation(location);
        resourceManagementClient.resourceGroups().createOrUpdate(rgName, group);
        // create storage and ADLS accounts, setting the accessKey
        DataLakeStoreAccount adlsAccount = new DataLakeStoreAccount();
        adlsAccount.withLocation(location);
        dataLakeStoreAccountManagementClient.accounts().create(rgName, adlsAcct, adlsAccount);

        // Create the ADLA acct to use.
        List<DataLakeStoreAccountInfo> adlsAccts = new ArrayList<DataLakeStoreAccountInfo>();
        DataLakeStoreAccountInfo adlsInfo = new DataLakeStoreAccountInfo();
        adlsInfo.withName(adlsAcct);
        adlsAccts.add(adlsInfo);

        DataLakeAnalyticsAccount createParams = new DataLakeAnalyticsAccount();
        createParams.withLocation(location);
        createParams.withDataLakeStoreAccounts(adlsAccts);
        createParams.withDefaultDataLakeStoreAccount(adlsAcct);

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
        JobInformation jobToSubmit = new JobInformation();
        USqlJobProperties jobProperties = new USqlJobProperties();
        jobProperties.withScript(jobScript);
        jobToSubmit.withName("java azure sdk data lake analytics job");
        jobToSubmit.withDegreeOfParallelism(2);
        jobToSubmit.withType(JobType.USQL);
        jobToSubmit.withProperties(jobProperties);
        UUID jobId = UUID.randomUUID();
        UUID secondJobId = UUID.randomUUID();

        JobInformation jobCreateResponse = dataLakeAnalyticsJobManagementClient.jobs().create(adlaAcct, jobId, jobToSubmit);
        Assert.assertNotNull(jobCreateResponse);

        // cancel the job
        dataLakeAnalyticsJobManagementClient.jobs().cancel(adlaAcct, jobId);

        // Get the job and ensure it was cancelled
        JobInformation cancelledJobResponse = dataLakeAnalyticsJobManagementClient.jobs().get(adlaAcct, jobId);
        Assert.assertEquals(JobResult.CANCELLED, cancelledJobResponse.result());
        Assert.assertNotNull(cancelledJobResponse.errorMessage());
        Assert.assertTrue(cancelledJobResponse.errorMessage().size() >= 1);

        // Resubmit and wait for job to finish
        jobCreateResponse = dataLakeAnalyticsJobManagementClient.jobs().create(adlaAcct, secondJobId, jobToSubmit);
        Assert.assertNotNull(jobCreateResponse);

        JobInformation getJobResponse = dataLakeAnalyticsJobManagementClient.jobs().get(adlaAcct, jobCreateResponse.jobId());
        Assert.assertNotNull(getJobResponse);

        int maxWaitInSeconds = 180; // 3 minutes should be long enough
        int curWaitInSeconds = 0;

        while (getJobResponse.state() != JobState.ENDED && curWaitInSeconds < maxWaitInSeconds)
        {
            // wait 5 seconds before polling again
            Thread.sleep(5000);
            curWaitInSeconds += 5;
            getJobResponse = dataLakeAnalyticsJobManagementClient.jobs().get(adlaAcct, jobCreateResponse.jobId());
            Assert.assertNotNull(getJobResponse);
        }

        Assert.assertTrue(curWaitInSeconds <= maxWaitInSeconds);

        // Verify the job completes successfully
        Assert.assertTrue(
                String.format("Job: %s did not return success. Current job state: %s. Actual result: %s. Error (if any): %s",
                getJobResponse.jobId(), getJobResponse.state(), getJobResponse.result(), getJobResponse.errorMessage()),
                getJobResponse.state() == JobState.ENDED && getJobResponse.result() == JobResult.SUCCEEDED);

        List<JobInformation> listJobResponse = dataLakeAnalyticsJobManagementClient.jobs().list(adlaAcct);
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
        JobInformation compileResponse = dataLakeAnalyticsJobManagementClient.jobs().build(adlaAcct, jobToSubmit);
        Assert.assertNotNull(compileResponse);

        // list the jobs both with a hand crafted query string and using the parameters
        listJobResponse = dataLakeAnalyticsJobManagementClient.jobs().list(adlaAcct, null, null, null, null,"jobId", null);
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
