/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.analytics;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.datalake.analytics.models.*;
import com.microsoft.azure.management.datalake.store.models.DataLakeStoreAccount;
import com.microsoft.azure.management.resources.core.AzureTestCredentials;
import com.microsoft.azure.management.resources.core.TestBase;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.microsoft.azure.management.storage.implementation.StorageManager;
import com.microsoft.rest.LogLevel;
import com.microsoft.rest.RestClient;
import com.microsoft.azure.credentials.UserTokenCredentials;
import com.microsoft.azure.management.datalake.analytics.implementation.DataLakeAnalyticsAccountManagementClientImpl;
import com.microsoft.azure.management.datalake.analytics.implementation.DataLakeAnalyticsCatalogManagementClientImpl;
import com.microsoft.azure.management.datalake.analytics.implementation.DataLakeAnalyticsJobManagementClientImpl;
import com.microsoft.azure.management.datalake.store.implementation.DataLakeStoreAccountManagementClientImpl;
import com.microsoft.azure.management.resources.implementation.ResourceManagementClientImpl;
import com.microsoft.azure.management.storage.implementation.StorageManagementClientImpl;
import okhttp3.OkHttpClient;
import org.junit.Assert;
import retrofit2.Retrofit;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class DataLakeAnalyticsManagementTestBase extends TestBase {
    protected static DataLakeAnalyticsAccountManagementClientImpl dataLakeAnalyticsAccountManagementClient;
    protected static DataLakeAnalyticsJobManagementClientImpl dataLakeAnalyticsJobManagementClient;
    protected static DataLakeAnalyticsCatalogManagementClientImpl dataLakeAnalyticsCatalogManagementClient;
    protected static ResourceManager resourceManagementClient;
    protected static DataLakeStoreAccountManagementClientImpl dataLakeStoreAccountManagementClient;
    protected static StorageManager storageManagementClient;
    protected static Region environmentLocation;
    protected static String rgName;
    protected static String adlsName;
    protected static String jobAndCatalogAdlaName;

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        rgName = generateRandomResourceName("adlarg",15);
        adlsName = generateRandomResourceName("adls",15);
        jobAndCatalogAdlaName = generateRandomResourceName("secondadla",15);

        environmentLocation = Region.US_EAST2;

        dataLakeAnalyticsAccountManagementClient = new DataLakeAnalyticsAccountManagementClientImpl(restClient)
            .withSubscriptionId(defaultSubscription);


        // TODO: in the future this needs to be dynamic depending on the Azure environment
        // the tests are running in.
        String adlaSuffix = "azuredatalakeanalytics.net";

        addTextReplacementRule("https://(.*)." + adlaSuffix, MOCK_URI);

        // Generate creds and a set of rest clients for catalog and job
        ApplicationTokenCredentials credentials = new AzureTestCredentials();
        if (IS_RECORD) {
            final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));
            try {
                credentials = ApplicationTokenCredentials.fromFile(credFile);
            }
            catch (IOException e) {
                Assert.fail("Failed to read credentials from file: " + credFile + " with error: " + e.getMessage());
            }
        }
        if (IS_RECORD) {
            RestClient restClientWithTimeout = buildRestClient(new RestClient.Builder()
                    .withConnectionTimeout(5, TimeUnit.MINUTES)
                    .withBaseUrl("https://{accountName}.{adlaJobDnsSuffix}")
                    .withCredentials(credentials)
                    .withLogLevel(LogLevel.BODY_AND_HEADERS)
                    .withNetworkInterceptor(interceptor), IS_MOCKED);


            dataLakeAnalyticsJobManagementClient = new DataLakeAnalyticsJobManagementClientImpl(restClientWithTimeout)
                    .withAdlaJobDnsSuffix(adlaSuffix);


            RestClient catalogRestClient = buildRestClient(new RestClient.Builder()
                    .withBaseUrl("https://{accountName}.{adlaCatalogDnsSuffix}")
                    .withCredentials(credentials)
                    .withLogLevel(LogLevel.BODY_AND_HEADERS)
                    .withNetworkInterceptor(interceptor), IS_MOCKED);

            dataLakeAnalyticsCatalogManagementClient = new DataLakeAnalyticsCatalogManagementClientImpl(catalogRestClient)
                    .withAdlaCatalogDnsSuffix(adlaSuffix);
        }
        else {
            // for mocked clients, we can just use the basic rest client, since the DNS is replaced.
            dataLakeAnalyticsCatalogManagementClient = new DataLakeAnalyticsCatalogManagementClientImpl(restClient);
            dataLakeAnalyticsJobManagementClient = new DataLakeAnalyticsJobManagementClientImpl(restClient);
        }

        resourceManagementClient = ResourceManager
                .authenticate(restClient)
                .withSubscription(defaultSubscription);

        dataLakeStoreAccountManagementClient = new DataLakeStoreAccountManagementClientImpl(restClient)
            .withSubscriptionId(defaultSubscription);

        storageManagementClient = StorageManager
                .authenticate(restClient, defaultSubscription);

        // create the resource group, ADLS account and ADLA account for job and catalog use.
        resourceManagementClient.resourceGroups()
                .define(rgName)
                .withRegion(environmentLocation)
                .create();

        DataLakeStoreAccount createParams = new DataLakeStoreAccount();
        createParams.withLocation(environmentLocation.name());
        dataLakeStoreAccountManagementClient.accounts().create(rgName, adlsName, createParams);

        List<DataLakeStoreAccountInfo> adlsAccts = new ArrayList<DataLakeStoreAccountInfo>();
        DataLakeStoreAccountInfo adlsInfo = new DataLakeStoreAccountInfo();
        adlsInfo.withName(adlsName);
        adlsAccts.add(adlsInfo);

        DataLakeAnalyticsAccount adlaCreateParams = new DataLakeAnalyticsAccount();
        adlaCreateParams.withLocation(environmentLocation.name());
        adlaCreateParams.withDataLakeStoreAccounts(adlsAccts);
        adlaCreateParams.withDefaultDataLakeStoreAccount(adlsName);

        dataLakeAnalyticsAccountManagementClient.accounts().create(rgName, jobAndCatalogAdlaName, adlaCreateParams);
    }

    @Override
    protected void cleanUpResources() {
        resourceManagementClient.resourceGroups().deleteByName(rgName);
    }

    protected void runJobToCompletion(String adlaAcct, UUID jobId, String scriptToRun) throws Exception {
        JobInformation jobToSubmit = new JobInformation();
        USqlJobProperties jobProperties = new USqlJobProperties();
        jobProperties.withScript(scriptToRun);
        jobToSubmit.withName("java azure sdk data lake analytics job");
        jobToSubmit.withDegreeOfParallelism(2);
        jobToSubmit.withType(JobType.USQL);
        jobToSubmit.withProperties(jobProperties);

        JobInformation jobCreateResponse = dataLakeAnalyticsJobManagementClient.jobs().create(adlaAcct, jobId, jobToSubmit);
        Assert.assertNotNull(jobCreateResponse);

        JobInformation getJobResponse = dataLakeAnalyticsJobManagementClient.jobs().get(adlaAcct, jobCreateResponse.jobId());
        Assert.assertNotNull(getJobResponse);

        int maxWaitInSeconds = 5 * 60; // giving it 5 minutes for now.
        int curWaitInSeconds = 0;

        while (getJobResponse.state() != JobState.ENDED && curWaitInSeconds < maxWaitInSeconds) {
            // wait 5 seconds before polling again
            SdkContext.sleep(5 * 1000);
            curWaitInSeconds += 5;
            getJobResponse = dataLakeAnalyticsJobManagementClient.jobs().get(adlaAcct, jobCreateResponse.jobId());
            Assert.assertNotNull(getJobResponse);
        }

        Assert.assertTrue(curWaitInSeconds <= maxWaitInSeconds);

        // Verify the job completes successfully
        Assert.assertTrue(
                MessageFormat.format("Job: {0} did not return success. Current job state: {1}. Actual result: {2}. Error (if any): {3}",
                        getJobResponse.jobId(), getJobResponse.state(), getJobResponse.result(), getJobResponse.errorMessage()),
                getJobResponse.state() == JobState.ENDED && getJobResponse.result() == JobResult.SUCCEEDED);
    }
}
