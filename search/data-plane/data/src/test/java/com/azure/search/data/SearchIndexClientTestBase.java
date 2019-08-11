// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.data;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.test.TestBase;
import com.azure.core.util.configuration.ConfigurationManager;
import com.azure.core.util.logging.ClientLogger;
import com.azure.search.data.common.SearchPipelinePolicy;
import com.azure.search.data.customization.SearchIndexClientBuilder;
import com.azure.search.data.env.AzureSearchResources;
import com.azure.search.data.env.SearchIndexDocs;
import com.azure.search.data.env.SearchIndexService;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import org.junit.Rule;
import org.junit.rules.TestName;


public class SearchIndexClientTestBase extends TestBase {

    private final ClientLogger logger = new ClientLogger(SearchIndexClientTestBase.class);

    private static final String AZURE_DOMAIN_ID = "AZURE_DOMAIN_ID";
    private static final String AZURE_SUBSCRIPTION_ID = "AZURE_SUBSCRIPTION_ID";
    private static final String AZURE_SERVICE_PRINCIPAL_APP_ID = "AZURE_SERVICE_PRINCIPAL_APP_ID";
    private static final String AZURE_SERVICE_PRINCIPAL_APP_SECRET = "AZURE_SERVICE_PRINCIPAL_APP_SECRET";

    private static String searchServiceName;
    private static String apiKey;
    private static String indexName;

    private AzureSearchResources azureSearchResources;

    @Rule
    public TestName testName = new TestName();

    @Override
    public String testName() {
        return testName.getMethodName();
    }

    SearchIndexClientBuilder builderSetup() {
        if (!interceptorManager.isPlaybackMode()) {
            createAzureTestEnvironment();

            return new SearchIndexClientBuilder()
                .serviceName(searchServiceName)
                .searchDnsSuffix("search.windows.net")
                .indexName(indexName)
                .apiVersion("2019-05-06")
                .httpClient(HttpClient.createDefault().wiretap(true))
                .addPolicy(new SearchPipelinePolicy(apiKey))
                .addPolicy(interceptorManager.getRecordPolicy())
                .addPolicy(new RetryPolicy())
                .addPolicy(new HttpLoggingPolicy(HttpLogDetailLevel.BODY_AND_HEADERS));
        } else {
            return new SearchIndexClientBuilder()
                .serviceName("searchServiceName")
                .searchDnsSuffix("search.windows.net")
                .indexName("hotels")
                .apiVersion("2019-05-06")
                .httpClient(interceptorManager.getPlaybackClient())
                .addPolicy(new SearchPipelinePolicy("apiKey"))
                .addPolicy(new HttpLoggingPolicy(HttpLogDetailLevel.BODY_AND_HEADERS));
        }
    }

    @Override
    protected void afterTest() {
        super.afterTest();
        deleteAzureTestEnvironment();
    }

    private void createAzureTestEnvironment() {
        String appId = ConfigurationManager.getConfiguration().get(AZURE_SERVICE_PRINCIPAL_APP_ID);
        String azureDomainId = ConfigurationManager.getConfiguration().get(AZURE_DOMAIN_ID);
        String secret = ConfigurationManager.getConfiguration().get(AZURE_SERVICE_PRINCIPAL_APP_SECRET);
        String subscriptionId = ConfigurationManager.getConfiguration().get(AZURE_SUBSCRIPTION_ID);

        ApplicationTokenCredentials applicationTokenCredentials = new ApplicationTokenCredentials(
            appId,
            azureDomainId,
            secret,
            AzureEnvironment.AZURE);

        azureSearchResources = new AzureSearchResources(
            applicationTokenCredentials, subscriptionId, Region.US_EAST);
        azureSearchResources.initialize();

        searchServiceName = azureSearchResources.getSearchServiceName();
        apiKey = azureSearchResources.getSearchAdminKey();

        try {
            //Creating Index:
            SearchIndexService searchIndexService = new SearchIndexService(searchServiceName, apiKey);
            searchIndexService.initialize();
            indexName = searchIndexService.indexName();

            // Uploading Documents:
            SearchIndexDocs searchIndexDocs = new SearchIndexDocs(searchServiceName, apiKey,
                indexName,
                "search.windows.net",
                "2019-05-06");
            searchIndexDocs.initialize();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteAzureTestEnvironment() {
        if (azureSearchResources != null) {
            azureSearchResources.cleanup();
        }
    }
}
