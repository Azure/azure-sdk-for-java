package com.microsoft.azure.management.datalake.analytics;

import com.microsoft.azure.management.datalake.analytics.models.DataLakeAnalyticsAccount;
import com.microsoft.azure.management.datalake.analytics.models.DataLakeAnalyticsAccountProperties;
import com.microsoft.azure.management.datalake.analytics.models.DataLakeStoreAccountInfo;
import com.microsoft.azure.management.datalake.analytics.models.JobInformation;
import com.microsoft.azure.management.datalake.analytics.models.JobResult;
import com.microsoft.azure.management.datalake.analytics.models.JobState;
import com.microsoft.azure.management.datalake.analytics.models.JobType;
import com.microsoft.azure.management.datalake.analytics.models.USqlJobProperties;
import com.microsoft.azure.management.datalake.store.models.DataLakeStoreAccount;
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
    private static String location = "eastus2";
    private static String adlsAcct = generateName("javaadlsacct");
    private static String adlaAcct = generateName("javaadlaacct");
    private static String jobScript = "DROP DATABASE IF EXISTS testdb; CREATE DATABASE testdb;";

    @BeforeClass
    public static void setup() throws Exception {
        createClients();
        ResourceGroupInner group = new ResourceGroupInner();
        group.setLocation(location);
        resourceManagementClient.resourceGroups().createOrUpdate(rgName, group);
        // create storage and ADLS accounts, setting the accessKey
        DataLakeStoreAccount adlsAccount = new DataLakeStoreAccount();
        adlsAccount.setLocation(location);
        adlsAccount.setName(adlsAcct);
        dataLakeStoreAccountManagementClient.accounts().create(rgName, adlsAcct, adlsAccount);

        // Create the ADLA acct to use.
        DataLakeAnalyticsAccountProperties createProperties = new DataLakeAnalyticsAccountProperties();
        List<DataLakeStoreAccountInfo> adlsAccts = new ArrayList<DataLakeStoreAccountInfo>();
        DataLakeStoreAccountInfo adlsInfo = new DataLakeStoreAccountInfo();
        adlsInfo.setName(adlsAcct);
        adlsAccts.add(adlsInfo);

        createProperties.setDataLakeStoreAccounts(adlsAccts);
        createProperties.setDefaultDataLakeStoreAccount(adlsAcct);

        DataLakeAnalyticsAccount createParams = new DataLakeAnalyticsAccount();
        createParams.setLocation(location);
        createParams.setName(adlaAcct);
        createParams.setProperties(createProperties);
        dataLakeAnalyticsAccountManagementClient.accounts().create(rgName, adlaAcct, createParams);
        // Sleep for two minutes to ensure the account is totally provisioned.
        Thread.sleep(120000);
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
        jobProperties.setScript(jobScript);
        jobToSubmit.setName("java azure sdk data lake analytics job");
        jobToSubmit.setDegreeOfParallelism(2);
        jobToSubmit.setType(JobType.USQL);
        jobToSubmit.setProperties(jobProperties);
        UUID jobId = UUID.randomUUID();
        UUID secondJobId = UUID.randomUUID();

        JobInformation jobCreateResponse = dataLakeAnalyticsJobManagementClient.jobs().create(jobId, adlaAcct, jobToSubmit).getBody();
        Assert.assertNotNull(jobCreateResponse);

        // cancel the job
        dataLakeAnalyticsJobManagementClient.jobs().cancel(jobId, adlaAcct);

        // Get the job and ensure it was cancelled
        JobInformation cancelledJobResponse = dataLakeAnalyticsJobManagementClient.jobs().get(jobId, adlaAcct).getBody();
        Assert.assertEquals(JobResult.CANCELLED, cancelledJobResponse.getResult());
        Assert.assertNotNull(cancelledJobResponse.getErrorMessage());
        Assert.assertTrue(cancelledJobResponse.getErrorMessage().size() >= 1);

        // Resubmit and wait for job to finish
        jobCreateResponse = dataLakeAnalyticsJobManagementClient.jobs().create(secondJobId, adlaAcct, jobToSubmit).getBody();
        Assert.assertNotNull(jobCreateResponse);

        JobInformation getJobResponse = dataLakeAnalyticsJobManagementClient.jobs().get(jobCreateResponse.getJobId(), adlaAcct).getBody();
        Assert.assertNotNull(getJobResponse);

        int maxWaitInSeconds = 180; // 3 minutes should be long enough
        int curWaitInSeconds = 0;

        while (getJobResponse.getState() != JobState.ENDED && curWaitInSeconds < maxWaitInSeconds)
        {
            // wait 5 seconds before polling again
            Thread.sleep(5000);
            curWaitInSeconds += 5;
            getJobResponse = dataLakeAnalyticsJobManagementClient.jobs().get(jobCreateResponse.getJobId(), adlaAcct).getBody();
            Assert.assertNotNull(getJobResponse);
        }

        Assert.assertTrue(curWaitInSeconds <= maxWaitInSeconds);

        // Verify the job completes successfully
        Assert.assertTrue(
                String.format("Job: %s did not return success. Current job state: %s. Actual result: %s. Error (if any): %s",
                getJobResponse.getJobId(), getJobResponse.getState(), getJobResponse.getResult(), getJobResponse.getErrorMessage()),
                getJobResponse.getState() == JobState.ENDED && getJobResponse.getResult() == JobResult.SUCCEEDED);

        List<JobInformation> listJobResponse = dataLakeAnalyticsJobManagementClient.jobs().list(adlaAcct, null, null, null, null, null, null, null, null, null).getBody();
        Assert.assertNotNull(listJobResponse);
        boolean foundJob = false;
        for(JobInformation eachJob : listJobResponse) {
            if (eachJob.getJobId().equals(secondJobId)) {
                foundJob = true;
                break;
            }
        }

        Assert.assertTrue(foundJob);

        // Just compile the job, which requires a jobId in the job object.
        jobToSubmit.setJobId(getJobResponse.getJobId());
        JobInformation compileResponse = dataLakeAnalyticsJobManagementClient.jobs().build(adlaAcct, jobToSubmit).getBody();
        Assert.assertNotNull(compileResponse);

        // list the jobs both with a hand crafted query string and using the parameters
        listJobResponse = dataLakeAnalyticsJobManagementClient.jobs().list(adlaAcct, null, null, null, null, "jobId", null, null, null, null).getBody();
        Assert.assertNotNull(listJobResponse);

        foundJob = false;
        for(JobInformation eachJob : listJobResponse) {
            if (eachJob.getJobId().equals(secondJobId)) {
                foundJob = true;
                break;
            }
        }

        Assert.assertTrue(foundJob);
    }
}
