// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.data.env;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.test.TestBase;
import com.azure.core.util.configuration.ConfigurationManager;
import com.azure.core.util.logging.ClientLogger;
import com.azure.search.data.SearchIndexAsyncClient;
import com.azure.search.data.common.SearchPipelinePolicy;
import com.azure.search.data.customization.SearchIndexClientBuilder;
import com.azure.search.data.generated.models.DocumentIndexResult;
import com.azure.search.data.generated.models.IndexAction;
import com.azure.search.data.generated.models.IndexActionType;
import com.azure.search.data.generated.models.IndexBatch;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import org.junit.Rule;
import org.junit.rules.TestName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;


public class SearchIndexClientTestBase extends TestBase {

    private final ClientLogger logger = new ClientLogger(SearchIndexClientTestBase.class);

    private static final String AZURE_DOMAIN_ID = "AZURE_DOMAIN_ID";
    private static final String AZURE_SUBSCRIPTION_ID = "AZURE_SUBSCRIPTION_ID";
    private static final String AZURE_SERVICE_PRINCIPAL_APP_ID = "AZURE_SERVICE_PRINCIPAL_APP_ID";
    private static final String AZURE_SERVICE_PRINCIPAL_APP_SECRET = "AZURE_SERVICE_PRINCIPAL_APP_SECRET";
    private static final String INDEX_FILE_NAME = "INDEX_FILE_NAME";


    protected String searchServiceName;
    protected String apiKey;
    protected String indexName;

    private AzureSearchResources azureSearchResources;

    @Rule
    public TestName testName = new TestName();

    @Override
    public String testName() {
        return testName.getMethodName();
    }

    /**
     * Add a given document to the index actions list
     * @param indexActions object to be modified
     * @param document the document to be added
     */
    private void addDocumentToIndexActions(List<IndexAction> indexActions, HashMap<String, Object> document) {
        indexActions.add(new IndexAction()
            .actionType(IndexActionType.UPLOAD)
            .additionalProperties(document));
    }

    /**
     * index the given index actions against the search service
     * @param client
     * @param indexActions
     * @return
     */
    protected DocumentIndexResult index(SearchIndexAsyncClient client, List<IndexAction> indexActions) {
        return client.index(
            new IndexBatch().actions(indexActions))
            .block();
    }

    /**
     * Index the document into the search service
     */
    protected DocumentIndexResult indexDocument(SearchIndexAsyncClient client, HashMap<String, Object> document) {
        List<IndexAction> indexActions = new LinkedList<>();
        addDocumentToIndexActions(indexActions, document);
        System.out.println("Indexing " + indexActions.size() + " docs");

        return index(client, indexActions);
    }

    /**
     * Index the documents into the search service
     */
    protected DocumentIndexResult indexDocuments(SearchIndexAsyncClient client, List<Map> documents) {
        List<IndexAction> indexActions = new ArrayList<>();
        assert documents != null;
        documents.forEach(h -> {
            HashMap<String, Object> doc = new HashMap<String, Object>(h);
            addDocumentToIndexActions(indexActions, doc);
        });

        System.out.println("Indexing " + indexActions.size() + " docs");
        return index(client, indexActions);
    }

    public SearchIndexClientBuilder builderSetup() {
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
        String indexFileName = ConfigurationManager.getConfiguration().get(INDEX_FILE_NAME);

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
            SearchIndexService searchIndexService = new SearchIndexService(indexFileName, searchServiceName, apiKey);
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
}
