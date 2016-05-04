package com.microsoft.azure.management.datalake.analytics;

import com.microsoft.azure.credentials.AzureEnvironment;
import com.microsoft.azure.credentials.UserTokenCredentials;
import com.microsoft.azure.management.datalake.analytics.implementation.api.DataLakeAnalyticsAccountManagementClientImpl;
import com.microsoft.azure.management.datalake.analytics.implementation.api.DataLakeAnalyticsCatalogManagementClientImpl;
import com.microsoft.azure.management.datalake.analytics.implementation.api.DataLakeAnalyticsJobManagementClientImpl;
import com.microsoft.azure.management.datalake.analytics.implementation.api.JobInformationInner;
import com.microsoft.azure.management.datalake.analytics.implementation.api.JobResult;
import com.microsoft.azure.management.datalake.analytics.implementation.api.JobState;
import com.microsoft.azure.management.datalake.analytics.implementation.api.JobType;
import com.microsoft.azure.management.datalake.analytics.implementation.api.USqlJobProperties;
import com.microsoft.azure.management.datalake.store.implementation.api.DataLakeStoreAccountManagementClientImpl;
import com.microsoft.azure.management.resources.implementation.api.ResourceManagementClientImpl;
import com.microsoft.azure.management.storage.implementation.api.StorageManagementClientImpl;

import com.microsoft.rest.RestClient;
import org.junit.Assert;

import java.text.MessageFormat;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;

public abstract class DataLakeAnalyticsManagementTestBase {
    protected static DataLakeAnalyticsAccountManagementClientImpl dataLakeAnalyticsAccountManagementClient;
    protected static DataLakeAnalyticsJobManagementClientImpl dataLakeAnalyticsJobManagementClient;
    protected static DataLakeAnalyticsCatalogManagementClientImpl dataLakeAnalyticsCatalogManagementClient;
    protected static ResourceManagementClientImpl resourceManagementClient;
    protected static DataLakeStoreAccountManagementClientImpl dataLakeStoreAccountManagementClient;
    protected static StorageManagementClientImpl storageManagementClient;
    protected static String environmentLocation;
    public static void createClients() {
        String environment = System.getenv("arm.environmentType");
        String armUri = "";
        String adlaSuffix = "";
        environmentLocation = "eastus2";
        AzureEnvironment authEnv;
        switch (environment.toLowerCase()) {
            case "production":
                armUri = "https://management.azure.com";
                adlaSuffix = "azuredatalakeanalytics.net";
                authEnv = AzureEnvironment.AZURE;
                break;
            case "ppe":
                armUri = "https://api-dogfood.resources.windows-int.net";
                adlaSuffix = "konaaccountdogfood.net";
                authEnv = new AzureEnvironment("https://login.windows-ppe.net/", "https://management.core.windows.net/", true);
                break;
            case "test":
                armUri = "https://api-dogfood.resources.windows-int.net";
                adlaSuffix = "konaaccountdogfood.net";
                environmentLocation = "westus";
                authEnv = new AzureEnvironment("https://login.windows-ppe.net/", "https://management.core.windows.net/", true);
                break;
            default: // default to production
                armUri = "https://management.azure.com";
                adlaSuffix = "azuredatalakeanalytics.net";
                authEnv = AzureEnvironment.AZURE;
                break;
        }

        UserTokenCredentials credentials = new UserTokenCredentials(
                System.getenv("arm.clientid"),
                System.getenv("arm.domain"),
                System.getenv("arm.username"),
                System.getenv("arm.password"),
                null,
                authEnv);

        RestClient restClient = new RestClient.Builder(armUri)
                .withCredentials(credentials)
                .withLogLevel(HttpLoggingInterceptor.Level.BODY)
                .build();
        dataLakeAnalyticsAccountManagementClient = new DataLakeAnalyticsAccountManagementClientImpl(restClient);
        dataLakeAnalyticsAccountManagementClient.setSubscriptionId(System.getenv("arm.subscriptionid"));
        RestClient restClientWithTimeout = new RestClient.Builder(armUri, new OkHttpClient.Builder().readTimeout(5, TimeUnit.MINUTES), new Retrofit.Builder())
                .withCredentials(credentials)
                .withLogLevel(HttpLoggingInterceptor.Level.BODY)
                .build();
        dataLakeAnalyticsJobManagementClient = new DataLakeAnalyticsJobManagementClientImpl(restClientWithTimeout);
        dataLakeAnalyticsJobManagementClient.setAdlaJobDnsSuffix(adlaSuffix);

        dataLakeAnalyticsCatalogManagementClient = new DataLakeAnalyticsCatalogManagementClientImpl(restClient);
        dataLakeAnalyticsCatalogManagementClient.setAdlaCatalogDnsSuffix(adlaSuffix);

        resourceManagementClient = new ResourceManagementClientImpl(armUri, credentials);
        resourceManagementClient.setSubscriptionId(System.getenv("arm.subscriptionid"));

        dataLakeStoreAccountManagementClient = new DataLakeStoreAccountManagementClientImpl(restClient);
        dataLakeStoreAccountManagementClient.setSubscriptionId(System.getenv("arm.subscriptionid"));

        storageManagementClient = new StorageManagementClientImpl(restClient);
        storageManagementClient.setSubscriptionId(System.getenv("arm.subscriptionid"));
    }

    public static void runJobToCompletion(DataLakeAnalyticsJobManagementClientImpl jobClient, String adlaAcct, UUID jobId, String scriptToRun) throws Exception {
        // submit a job
        JobInformationInner jobToSubmit = new JobInformationInner();
        USqlJobProperties jobProperties = new USqlJobProperties();
        jobProperties.setScript(scriptToRun);
        jobToSubmit.setName("java azure sdk data lake analytics job");
        jobToSubmit.setDegreeOfParallelism(2);
        jobToSubmit.setType(JobType.USQL);
        jobToSubmit.setProperties(jobProperties);

        JobInformationInner jobCreateResponse = jobClient.jobs().create(adlaAcct, jobId, jobToSubmit).getBody();
        Assert.assertNotNull(jobCreateResponse);

        JobInformationInner getJobResponse = jobClient.jobs().get(adlaAcct, jobCreateResponse.jobId()).getBody();
        Assert.assertNotNull(getJobResponse);

        int maxWaitInSeconds = 2700; // giving it 45 minutes for now.
        int curWaitInSeconds = 0;

        while (getJobResponse.state() != JobState.ENDED && curWaitInSeconds < maxWaitInSeconds)
        {
            // wait 5 seconds before polling again
            Thread.sleep(5000);
            curWaitInSeconds += 5;
            getJobResponse = jobClient.jobs().get(adlaAcct, jobCreateResponse.jobId()).getBody();
            Assert.assertNotNull(getJobResponse);
        }

        Assert.assertTrue(curWaitInSeconds <= maxWaitInSeconds);

        // Verify the job completes successfully
        Assert.assertTrue(
                MessageFormat.format("Job: {0} did not return success. Current job state: {1}. Actual result: {2}. Error (if any): {3}",
                        getJobResponse.jobId(), getJobResponse.state(), getJobResponse.result(), getJobResponse.errorMessage()),
                getJobResponse.state() == JobState.ENDED && getJobResponse.result() == JobResult.SUCCEEDED);
    }

    public static String generateName(String prefix) {
        int randomSuffix = (int)(Math.random() * 1000);
        return prefix + randomSuffix;
    }
}
