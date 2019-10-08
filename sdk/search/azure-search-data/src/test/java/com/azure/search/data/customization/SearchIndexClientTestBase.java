// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.data.customization;

import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.test.TestBase;
import com.azure.core.util.configuration.ConfigurationManager;
import com.azure.core.util.configuration.BaseConfigurations;
import com.azure.core.util.logging.ClientLogger;
import com.azure.search.data.common.credentials.ApiKeyCredentials;
import com.azure.search.data.common.jsonwrapper.JsonWrapper;
import com.azure.search.data.common.jsonwrapper.api.JsonApi;
import com.azure.search.data.common.jsonwrapper.api.Type;
import com.azure.search.data.common.jsonwrapper.jacksonwrapper.JacksonDeserializer;
import com.azure.search.test.environment.setup.SearchIndexService;
import com.azure.search.test.environment.setup.AzureSearchResources;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import org.junit.Assert;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class SearchIndexClientTestBase extends TestBase {

    private final ClientLogger logger = new ClientLogger(SearchIndexClientTestBase.class);

    private static final String HOTELS_TESTS_INDEX_DATA_JSON = "HotelsTestsIndexData.json";
    protected static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    protected String searchServiceName;
    protected ApiKeyCredentials apiKeyCredentials;
    protected SearchIndexService searchServiceHotelsIndex;

    private static AzureSearchResources azureSearchResources;
    private JsonApi jsonApi = JsonWrapper.newInstance(JacksonDeserializer.class);

    @Rule
    public TestName testName = new TestName();

    @BeforeClass
    public static void beforeClass() {
        initializeAzureResources();
    }

    @AfterClass
    public static void afterClass() {
        azureSearchResources.deleteResourceGroup();
    }

    @Override
    protected void beforeTest() {
        if (!interceptorManager.isPlaybackMode()) {
            azureSearchResources.initialize();
            azureSearchResources.createResourceGroup();
            azureSearchResources.createService();

            searchServiceName = azureSearchResources.getSearchServiceName();
            apiKeyCredentials = new ApiKeyCredentials(azureSearchResources.getSearchAdminKey());
        }
    }

    @Override
    protected void afterTest() {
        super.afterTest();
        azureSearchResources.deleteService();
    }

    @Override
    public String testName() {
        return testName.getMethodName();
    }

    protected <T> void uploadDocuments(SearchIndexClient client, List<T> uploadDoc) {
        client.uploadDocuments(uploadDoc);
        waitForIndexing();
    }

    protected <T> void uploadDocuments(SearchIndexAsyncClient client, List<T> uploadDoc) {
        client.uploadDocuments(uploadDoc)
            .block();
        waitForIndexing();
    }

    protected <T> void uploadDocument(SearchIndexClient client, T uploadDoc) {
        List<T> docs = new ArrayList<>();
        docs.add(uploadDoc);

        client.uploadDocuments(docs);
        waitForIndexing();
    }

    protected <T> void uploadDocument(SearchIndexAsyncClient client, T uploadDoc) {
        List<T> docs = new ArrayList<>();
        docs.add(uploadDoc);

        client.uploadDocuments(docs)
            .block();
        waitForIndexing();
    }

    protected List<Map<String, Object>> uploadDocumentsJson(
        SearchIndexAsyncClient client, String dataJson) {
        List<Map<String, Object>> documents =
            jsonApi.readJsonFileToList(dataJson, new Type<List<Map<String, Object>>>() {
            });

        uploadDocuments(client, documents);
        return documents;
    }

    protected List<Map<String, Object>> uploadDocumentsJson(
        SearchIndexClient client, String dataJson) {
        List<Map<String, Object>> documents =
            jsonApi.readJsonFileToList(dataJson, new Type<List<Map<String, Object>>>() {
            });

        uploadDocuments(client, documents);

        return documents;
    }

    protected SearchIndexClientBuilder getClientBuilder(String indexName) {
        if (!interceptorManager.isPlaybackMode()) {
            return new SearchIndexClientBuilder()
                .serviceName(searchServiceName)
                .searchDnsSuffix("search.windows.net")
                .indexName(indexName)
                .apiVersion("2019-05-06")
                .httpClient(new NettyAsyncHttpClientBuilder().setWiretap(true).build())
                .credential(apiKeyCredentials)
                .addPolicy(interceptorManager.getRecordPolicy())
                .addPolicy(new RetryPolicy())
                .addPolicy(new HttpLoggingPolicy(HttpLogDetailLevel.BODY_AND_HEADERS));
        } else {
            return new SearchIndexClientBuilder()
                .serviceName("searchServiceName")
                .searchDnsSuffix("search.windows.net")
                .indexName(indexName)
                .apiVersion("2019-05-06")
                .httpClient(interceptorManager.getPlaybackClient());
        }
    }

    protected void createHotelIndex() {
        if (!interceptorManager.isPlaybackMode()) {
            try {
                //Creating Index:
                searchServiceHotelsIndex = new SearchIndexService(HOTELS_TESTS_INDEX_DATA_JSON, searchServiceName, apiKeyCredentials.getApiKey());
                searchServiceHotelsIndex.initialize();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected void setupIndexFromJsonFile(String jsonFile) {
        if (!interceptorManager.isPlaybackMode()) {
            // In RECORDING mode (only), create a new index:
            SearchIndexService searchIndexService = new SearchIndexService(
                jsonFile, searchServiceName, apiKeyCredentials.getApiKey());
            try {
                searchIndexService.initialize();
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
        }
    }

    private static void initializeAzureResources() {
        String appId = ConfigurationManager.getConfiguration().get(BaseConfigurations.AZURE_CLIENT_ID);
        String azureDomainId = ConfigurationManager.getConfiguration().get(BaseConfigurations.AZURE_TENANT_ID);
        String secret = ConfigurationManager.getConfiguration().get(BaseConfigurations.AZURE_CLIENT_SECRET);
        String subscriptionId = ConfigurationManager.getConfiguration().get(BaseConfigurations.AZURE_SUBSCRIPTION_ID);

        ApplicationTokenCredentials applicationTokenCredentials = new ApplicationTokenCredentials(
            appId,
            azureDomainId,
            secret,
            AzureEnvironment.AZURE);

        azureSearchResources = new AzureSearchResources(applicationTokenCredentials, subscriptionId, Region.US_EAST);
    }

    protected void waitForIndexing() {
        // Wait 2 secs to allow index request to finish
        if (!interceptorManager.isPlaybackMode()) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
