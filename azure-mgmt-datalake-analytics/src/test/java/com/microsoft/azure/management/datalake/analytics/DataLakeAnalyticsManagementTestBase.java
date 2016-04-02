package com.microsoft.azure.management.datalake.analytics;

import com.microsoft.azure.credentials.AzureEnvironment;
import com.microsoft.azure.credentials.UserTokenCredentials;
import com.microsoft.azure.management.datalake.analytics.models.JobInformation;
import com.microsoft.azure.management.datalake.analytics.models.JobResult;
import com.microsoft.azure.management.datalake.analytics.models.JobState;
import com.microsoft.azure.management.datalake.analytics.models.JobType;
import com.microsoft.azure.management.datalake.analytics.models.USqlJobProperties;
import com.microsoft.azure.management.datalake.store.DataLakeStoreAccountManagementClient;
import com.microsoft.azure.management.datalake.store.DataLakeStoreAccountManagementClientImpl;
import com.microsoft.azure.management.resources.client.ResourceManagementClient;
import com.microsoft.azure.management.resources.client.implementation.ResourceManagementClientImpl;
import com.microsoft.azure.management.storage.StorageManagementClient;
import com.microsoft.azure.management.storage.StorageManagementClientImpl;
import okhttp3.logging.HttpLoggingInterceptor;
import org.junit.Assert;

import java.util.UUID;

public abstract class DataLakeAnalyticsManagementTestBase {
    protected static DataLakeAnalyticsAccountManagementClient dataLakeAnalyticsAccountManagementClient;
    protected static DataLakeAnalyticsJobManagementClient dataLakeAnalyticsJobManagementClient;
    protected static DataLakeAnalyticsCatalogManagementClient dataLakeAnalyticsCatalogManagementClient;
    protected static ResourceManagementClient resourceManagementClient;
    protected static DataLakeStoreAccountManagementClient dataLakeStoreAccountManagementClient;
    protected static StorageManagementClient storageManagementClient;

    public static void createClients() {
        UserTokenCredentials credentials = new UserTokenCredentials(
                System.getenv("arm.clientid"),
                System.getenv("arm.domain"),
                System.getenv("arm.username"),
                System.getenv("arm.password"),
                null,
                AzureEnvironment.AZURE);

        dataLakeAnalyticsAccountManagementClient = new DataLakeAnalyticsAccountManagementClientImpl(credentials);
        dataLakeAnalyticsAccountManagementClient.setLogLevel(HttpLoggingInterceptor.Level.BODY);
        dataLakeAnalyticsAccountManagementClient.setSubscriptionId(System.getenv("arm.subscriptionid"));
        dataLakeAnalyticsJobManagementClient = new DataLakeAnalyticsJobManagementClientImpl(credentials);
        dataLakeAnalyticsJobManagementClient.setSubscriptionId(System.getenv("arm.subscriptionid"));
        dataLakeAnalyticsJobManagementClient.setLogLevel(HttpLoggingInterceptor.Level.BODY);

        dataLakeAnalyticsCatalogManagementClient = new DataLakeAnalyticsCatalogManagementClientImpl(credentials);
        dataLakeAnalyticsCatalogManagementClient.setSubscriptionId(System.getenv("arm.subscriptionid"));
        dataLakeAnalyticsCatalogManagementClient.setLogLevel(HttpLoggingInterceptor.Level.BODY);

        resourceManagementClient = new ResourceManagementClientImpl(credentials);
        resourceManagementClient.setSubscriptionId(System.getenv("arm.subscriptionid"));
        resourceManagementClient.setLogLevel(HttpLoggingInterceptor.Level.BODY);

        dataLakeStoreAccountManagementClient = new DataLakeStoreAccountManagementClientImpl(credentials);
        dataLakeStoreAccountManagementClient.setLogLevel(HttpLoggingInterceptor.Level.BODY);
        dataLakeStoreAccountManagementClient.setSubscriptionId(System.getenv("arm.subscriptionid"));

        storageManagementClient = new StorageManagementClientImpl(credentials);
        storageManagementClient.setLogLevel(HttpLoggingInterceptor.Level.BODY);
        storageManagementClient.setSubscriptionId(System.getenv("arm.subscriptionid"));
    }

    public static void runJobToCompletion(DataLakeAnalyticsJobManagementClient jobClient, String adlaAcct, UUID jobId, String scriptToRun) throws Exception {
        // submit a job
        JobInformation jobToSubmit = new JobInformation();
        USqlJobProperties jobProperties = new USqlJobProperties();
        jobProperties.setScript(scriptToRun);
        jobToSubmit.setName("java azure sdk data lake analytics job");
        jobToSubmit.setDegreeOfParallelism(2);
        jobToSubmit.setType(JobType.USQL);
        jobToSubmit.setProperties(jobProperties);

        JobInformation jobCreateResponse = jobClient.jobs().create(jobId, adlaAcct, jobToSubmit).getBody();
        Assert.assertNotNull(jobCreateResponse);

        JobInformation getJobResponse = jobClient.jobs().get(jobCreateResponse.getJobId(), adlaAcct).getBody();
        Assert.assertNotNull(getJobResponse);

        int maxWaitInSeconds = 180; // 3 minutes should be long enough
        int curWaitInSeconds = 0;

        while (getJobResponse.getState() != JobState.ENDED && curWaitInSeconds < maxWaitInSeconds)
        {
            // wait 5 seconds before polling again
            Thread.sleep(5000);
            curWaitInSeconds += 5;
            getJobResponse = jobClient.jobs().get(jobCreateResponse.getJobId(), adlaAcct).getBody();
            Assert.assertNotNull(getJobResponse);
        }

        Assert.assertTrue(curWaitInSeconds <= maxWaitInSeconds);

        // Verify the job completes successfully
        Assert.assertTrue(
                String.format("Job: {0} did not return success. Current job state: {1}. Actual result: {2}. Error (if any): {3}",
                        getJobResponse.getJobId(), getJobResponse.getState(), getJobResponse.getResult(), getJobResponse.getErrorMessage()),
                getJobResponse.getState() == JobState.ENDED && getJobResponse.getResult() == JobResult.SUCCEEDED);
    }

    public static String generateName(String prefix) {
        int randomSuffix = (int)(Math.random() * 1000);
        return prefix + randomSuffix;
    }
}
