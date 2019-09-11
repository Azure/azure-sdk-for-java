// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.data.env;

import com.azure.core.http.HttpClient;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.test.TestBase;
import com.azure.core.util.configuration.ConfigurationManager;
import com.azure.core.util.logging.ClientLogger;
import com.azure.search.data.SearchIndexAsyncClient;
import com.azure.search.data.SearchIndexClient;
import com.azure.search.data.common.SearchPipelinePolicy;
import com.azure.search.data.common.jsonwrapper.JsonWrapper;
import com.azure.search.data.common.jsonwrapper.api.JsonApi;
import com.azure.search.data.common.jsonwrapper.api.Type;
import com.azure.search.data.common.jsonwrapper.jacksonwrapper.JacksonDeserializer;
import com.azure.search.data.customization.SearchIndexClientBuilder;
import com.azure.search.data.generated.models.IndexAction;
import com.azure.search.data.generated.models.IndexActionType;
import com.azure.search.data.generated.models.IndexBatch;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import org.junit.Rule;
import org.junit.rules.TestName;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class SearchIndexClientTestBase extends TestBase {

    private final ClientLogger logger = new ClientLogger(SearchIndexClientTestBase.class);

    private static final String AZURE_DOMAIN_ID = "AZURE_DOMAIN_ID";
    private static final String AZURE_SUBSCRIPTION_ID = "AZURE_SUBSCRIPTION_ID";
    private static final String AZURE_SERVICE_PRINCIPAL_APP_ID = "AZURE_SERVICE_PRINCIPAL_APP_ID";
    private static final String AZURE_SERVICE_PRINCIPAL_APP_SECRET = "AZURE_SERVICE_PRINCIPAL_APP_SECRET";

    private static final String HOTELS_TESTS_INDEX_DATA_JSON = "HotelsTestsIndexData.json";
    protected static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    protected String searchServiceName;
    protected String apiKey;
    protected String indexName;

    private AzureSearchResources azureSearchResources;
    private JsonApi jsonApi = JsonWrapper.newInstance(JacksonDeserializer.class);

    @Rule
    public TestName testName = new TestName();

    @Override
    public String testName() {
        return testName.getMethodName();
    }

    protected <T> void uploadDocuments(SearchIndexClient client, String indexName, T uploadDoc) {
        client.setIndexName(indexName)
            .index(new IndexBatch().actions(createIndexActions(uploadDoc)));
        waitForIndexing();
    }

    protected <T> void uploadDocuments(SearchIndexAsyncClient client, String indexName, T uploadDoc) {
        client.setIndexName(indexName)
            .index(new IndexBatch().actions(createIndexActions(uploadDoc)))
            .block();
        waitForIndexing();
    }

    protected List<Map<String, Object>> uploadDocumentsJson(
        SearchIndexAsyncClient client, String indexName, String dataJson) {
        List<Map<String, Object>> documents =
            jsonApi.readJsonFileToList(dataJson, new Type<List<Map<String, Object>>>() {
            });

        uploadDocuments(client, indexName, documents);
        return documents;
    }

    protected List<Map<String, Object>> uploadDocumentsJson(
        SearchIndexClient client, String indexName, String dataJson) {
        List<Map<String, Object>> documents =
            jsonApi.readJsonFileToList(dataJson, new Type<List<Map<String, Object>>>() {
            });

        uploadDocuments(client, indexName, documents);

        return documents;
    }

    private <T> List<IndexAction> createIndexActions(T uploadDoc) {
        jsonApi.configureTimezone();
        List<IndexAction> indexActions = new LinkedList<>();
        if (uploadDoc instanceof List) {
            ((List) uploadDoc)
                .forEach(d -> addDocumentToIndexActions(indexActions, jsonApi.convertObjectToType(d, Map.class)));
        } else {
            addDocumentToIndexActions(indexActions, jsonApi.convertObjectToType(uploadDoc, Map.class));
        }
        return indexActions;
    }

    protected SearchIndexClientBuilder builderSetup() {
        if (!interceptorManager.isPlaybackMode()) {
            createAzureTestEnvironment();

            return new SearchIndexClientBuilder()
                .serviceName(searchServiceName)
                .searchDnsSuffix("search.windows.net")
                .indexName(indexName)
                .apiVersion("2019-05-06")
                .httpClient(new NettyAsyncHttpClientBuilder().setWiretap(true).build())
                .addPolicy(new SearchPipelinePolicy(apiKey))
                .addPolicy(interceptorManager.getRecordPolicy())
                .addPolicy(new RetryPolicy())
                .addPolicy(new HttpLoggingPolicy(HttpLogDetailLevel.BODY_AND_HEADERS));
        } else {
            return new SearchIndexClientBuilder()
                .serviceName("searchServiceName")
                .searchDnsSuffix("search.windows.net")
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
            SearchIndexService searchIndexService =
                new SearchIndexService(HOTELS_TESTS_INDEX_DATA_JSON, searchServiceName, apiKey);
            searchIndexService.initialize();
            indexName = searchIndexService.indexName();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteAzureTestEnvironment() {
        if (azureSearchResources != null) {
            azureSearchResources.cleanup();
        }
    }

    /**
     * Add a given document to the index actions list
     *
     * @param indexActions object to be modified
     * @param document     the document to be added
     */
    private void addDocumentToIndexActions(List<IndexAction> indexActions, Map<String, Object> document) {
        indexActions.add(new IndexAction()
            .actionType(IndexActionType.UPLOAD)
            .additionalProperties(document));
    }

    private void waitForIndexing() {
        // Wait 2 secs to allow index request to finish
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
