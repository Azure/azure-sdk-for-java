package com.microsoft.azure.management.datalake.analytics;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.RestClient;
import com.microsoft.azure.credentials.UserTokenCredentials;
import com.microsoft.azure.management.datalake.analytics.implementation.DataLakeAnalyticsAccountManagementClientImpl;
import com.microsoft.azure.management.datalake.analytics.implementation.DataLakeAnalyticsCatalogManagementClientImpl;
import com.microsoft.azure.management.datalake.analytics.implementation.DataLakeAnalyticsJobManagementClientImpl;
import com.microsoft.azure.management.datalake.analytics.models.JobInformation;
import com.microsoft.azure.management.datalake.analytics.models.JobResult;
import com.microsoft.azure.management.datalake.analytics.models.JobState;
import com.microsoft.azure.management.datalake.analytics.models.JobType;
import com.microsoft.azure.management.datalake.analytics.models.USqlJobProperties;
import com.microsoft.azure.management.datalake.store.implementation.DataLakeStoreAccountManagementClientImpl;
import com.microsoft.azure.management.resources.implementation.ResourceManagementClientImpl;
import com.microsoft.azure.management.storage.implementation.StorageManagementClientImpl;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.junit.Assert;
import retrofit2.Retrofit;

import java.text.MessageFormat;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
                authEnv = new AzureEnvironment(
                        "https://login.windows-ppe.net/",
                        "https://management.core.windows.net/",
                        true,
                        "https://api-dogfood.resources.windows-int.net");
                break;
            case "test":
                armUri = "https://api-dogfood.resources.windows-int.net";
                adlaSuffix = "konaaccountdogfood.net";
                environmentLocation = "westus";
                authEnv = new AzureEnvironment(
                        "https://login.windows-ppe.net/",
                        "https://management.core.windows.net/",
                        true,
                        "https://api-dogfood.resources.windows-int.net");
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

        RestClient restClient = new RestClient.Builder()
                .withBaseUrl(armUri)
                .withCredentials(credentials)
                .withLogLevel(HttpLoggingInterceptor.Level.BODY)
                .build();
        dataLakeAnalyticsAccountManagementClient = new DataLakeAnalyticsAccountManagementClientImpl(restClient);
        dataLakeAnalyticsAccountManagementClient.withSubscriptionId(System.getenv("arm.subscriptionid"));
        RestClient restClientWithTimeout = new RestClient.Builder(new OkHttpClient.Builder().readTimeout(5, TimeUnit.MINUTES), new Retrofit.Builder())
                .withBaseUrl(armUri)
                .withCredentials(credentials)
                .withLogLevel(HttpLoggingInterceptor.Level.BODY)
                .build();
        dataLakeAnalyticsJobManagementClient = new DataLakeAnalyticsJobManagementClientImpl(restClientWithTimeout);
        dataLakeAnalyticsJobManagementClient.withAdlaJobDnsSuffix(adlaSuffix);

        dataLakeAnalyticsCatalogManagementClient = new DataLakeAnalyticsCatalogManagementClientImpl(restClient);
        dataLakeAnalyticsCatalogManagementClient.withAdlaCatalogDnsSuffix(adlaSuffix);

        resourceManagementClient = new ResourceManagementClientImpl(armUri, credentials);
        resourceManagementClient.withSubscriptionId(System.getenv("arm.subscriptionid"));

        dataLakeStoreAccountManagementClient = new DataLakeStoreAccountManagementClientImpl(restClient);
        dataLakeStoreAccountManagementClient.withSubscriptionId(System.getenv("arm.subscriptionid"));

        storageManagementClient = new StorageManagementClientImpl(restClient);
        storageManagementClient.withSubscriptionId(System.getenv("arm.subscriptionid"));
    }

    public static void runJobToCompletion(DataLakeAnalyticsJobManagementClientImpl jobClient, String adlaAcct, UUID jobId, String scriptToRun) throws Exception {
        // submit a job
        JobInformation jobToSubmit = new JobInformation();
        USqlJobProperties jobProperties = new USqlJobProperties();
        jobProperties.withScript(scriptToRun);
        jobToSubmit.withName("java azure sdk data lake analytics job");
        jobToSubmit.withDegreeOfParallelism(2);
        jobToSubmit.withType(JobType.USQL);
        jobToSubmit.withProperties(jobProperties);

        JobInformation jobCreateResponse = jobClient.jobs().create(adlaAcct, jobId, jobToSubmit).getBody();
        Assert.assertNotNull(jobCreateResponse);

        JobInformation getJobResponse = jobClient.jobs().get(adlaAcct, jobCreateResponse.jobId()).getBody();
        Assert.assertNotNull(getJobResponse);

        int maxWaitInSeconds = 2700; // giving it 45 minutes for now.
        int curWaitInSeconds = 0;

        while (getJobResponse.state() != JobState.ENDED && curWaitInSeconds < maxWaitInSeconds) {
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
        int randomSuffix = (int) (Math.random() * 1000);
        return prefix + randomSuffix;
    }
}
