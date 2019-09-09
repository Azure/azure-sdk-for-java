// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.data.customization;

import com.azure.search.data.SearchIndexAsyncClient;
import com.azure.search.data.SearchIndexClient;
import com.azure.search.data.common.SearchPipelinePolicy;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SearchIndexClientBuildersTest {


    private String searchServiceName = "";
    private String apiKey = "";
    private String indexName = "";
    private String apiVersion = "";
    private String dnsSuffix = "";

    /**
     * Builds a Sync Search Index client
     */
    private SearchIndexClientImpl buildClient(String searchServiceName, String indexName, String apiKey, String apiVersion, String dnsSuffix) {
        SearchPipelinePolicy policy = new SearchPipelinePolicy(apiKey);

        SearchIndexClientBuilder clientBuilder = new SearchIndexClientBuilder();
        SearchIndexClient client = clientBuilder.serviceName(searchServiceName).indexName(indexName).addPolicy(policy).apiVersion(apiVersion).searchDnsSuffix(dnsSuffix).buildClient();

        assert (client != null);
        assert (client.getClass().getSimpleName().equals(SearchIndexClientImpl.class.getSimpleName()));

        return (SearchIndexClientImpl) client;
    }

    /**
     * Builds an ASync Search Index client
     */
    private SearchIndexAsyncClientImpl buildASyncClient(String searchServiceName, String indexName, String apiKey, String apiVersion, String dnsSuffix) {
        SearchPipelinePolicy policy = new SearchPipelinePolicy(apiKey);

        SearchIndexClientBuilder clientBuilder = new SearchIndexClientBuilder();
        SearchIndexAsyncClient client = clientBuilder.serviceName(searchServiceName).indexName(indexName).addPolicy(policy).apiVersion(apiVersion).searchDnsSuffix(dnsSuffix).buildAsyncClient();

        assert (client != null);
        assert (client.getClass().getSimpleName().equals(SearchIndexAsyncClientImpl.class.getSimpleName()));

        return (SearchIndexAsyncClientImpl) client;
    }

    private void buildClientAndVerifyInternal(boolean isASync) {
        SearchIndexBaseClient client;
        if (isASync) {
            client = buildClient(searchServiceName, indexName, apiKey, apiVersion, dnsSuffix);

        } else {
            client = buildASyncClient(searchServiceName, indexName, apiKey, apiVersion, dnsSuffix);
        }

        assert (client.getIndexName().equals(indexName));
        assert (client.getSearchServiceName().equals(searchServiceName));
        assert (client.getApiVersion().equals(apiVersion));
        assert (client.getSearchDnsSuffix().equals(dnsSuffix));
    }

    /**
     * Verify that if the property is invalid the client builder throws exception
     */
    private void buildClientAndExpectException(boolean isASync,
                                               String expectedMsg,
                                               String searchServiceName,
                                               String indexName,
                                               String apiKey,
                                               String apiVersion,
        String dnsSuffix) {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(expectedMsg);

        if (isASync) {
            buildASyncClient(searchServiceName, indexName, apiKey, apiVersion, dnsSuffix);
        } else {
            buildClient(searchServiceName, indexName, apiKey, apiVersion, dnsSuffix);
        }
    }

    @Before
    public void initialize() {
        searchServiceName = "servicename";
        apiKey = "0123";
        indexName = "myindex";
        apiVersion = "2019-05-06";
        dnsSuffix = "search.windows.net";
    }

    @Test
    public void buildSyncClientTest() {
        buildClient(searchServiceName, indexName, apiKey, apiVersion, dnsSuffix);
    }

    @Test
    public void buildASyncClientTest() {
        buildASyncClient(searchServiceName, indexName, apiKey, apiVersion, dnsSuffix);
    }

    @Test
    public void buildClientAndVerifyPropertiesTest() {
        buildClientAndVerifyInternal(false);
        buildClientAndVerifyInternal(true);
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();


    @Test
    public void verifyNullSearchServiceNameIsInvalidASyncTest() {
        buildClientAndExpectException(true, "Invalid searchServiceName", null,
                indexName, apiKey, apiVersion, dnsSuffix);
    }

    @Test
    public void verifyNullSearchServiceNameIsInvalidTest() {
        buildClientAndExpectException(false, "Invalid searchServiceName", null,
                indexName, apiKey, apiVersion, dnsSuffix);
    }

    @Test
    public void verifyEmptySearchServiceNameIsInvalidASyncTest() {
        buildClientAndExpectException(false, "Invalid searchServiceName", "",
                indexName, apiKey, apiVersion, dnsSuffix);
    }

    @Test
    public void verifyEmptySearchServiceNameIsInvalidTest() {
        buildClientAndExpectException(false, "Invalid searchServiceName", "",
                indexName, apiKey, apiVersion, dnsSuffix);
    }

    @Test
    public void verifyNullIndexNameIsInvalidASyncTest() {
        buildClientAndExpectException(true, "Invalid indexName", searchServiceName,
                null, apiKey, apiVersion, dnsSuffix);
    }

    @Test
    public void verifyNullIndexNameIsInvalidTest() {
        buildClientAndExpectException(false, "Invalid indexName", searchServiceName,
                null, apiKey, apiVersion, dnsSuffix);
    }

    @Test
    public void verifyEmptyIndexNameIsInvalidASyncTest() {
        buildClientAndExpectException(true, "Invalid indexName", searchServiceName,
                "", apiKey, apiVersion, dnsSuffix);
    }

    @Test
    public void verifyEmptyIndexNameIsInvalidTest() {
        buildClientAndExpectException(false, "Invalid indexName", searchServiceName,
                "", apiKey, apiVersion, dnsSuffix);
    }

    @Test
    public void verifyNullApiKeyIsInvalidASyncTest() {
        buildClientAndExpectException(true, "Invalid apiKey", searchServiceName,
                indexName, null, apiVersion, dnsSuffix);
    }

    @Test
    public void verifyNullApiKeyIsInvalidTest() {
        buildClientAndExpectException(false, "Invalid apiKey", searchServiceName,
                indexName, null, apiVersion, dnsSuffix);
    }

    @Test
    public void verifyEmptyApiKeyIsInvalidASyncTest() {
        buildClientAndExpectException(true, "Invalid apiKey", searchServiceName,
                indexName, "", apiVersion, dnsSuffix);
    }

    @Test
    public void verifyEmptyApiKeyIsInvalidTest() {
        buildClientAndExpectException(false, "Invalid apiKey", searchServiceName,
                indexName, "", apiVersion, dnsSuffix);
    }

    @Test
    public void verifyNullApiVersionIsInvalidASyncTest() {
        buildClientAndExpectException(true, "Invalid apiVersion", searchServiceName,
                indexName, apiKey, null, dnsSuffix);
    }

    @Test
    public void verifyNullApiVersionIsInvalidTest() {
        buildClientAndExpectException(false, "Invalid apiVersion", searchServiceName,
                indexName, apiKey, null, dnsSuffix);
    }

    @Test
    public void verifyEmptyApiVersionIsInvalidASyncTest() {
        buildClientAndExpectException(true, "Invalid apiVersion", searchServiceName,
                indexName, apiKey, "", dnsSuffix);
    }

    @Test
    public void verifyEmptyApiVersionIsInvalidTest() {
        buildClientAndExpectException(false, "Invalid apiVersion", searchServiceName,
                indexName, apiKey, "", dnsSuffix);
    }

    @Test
    public void verifyNullDnsSuffixIsInvalidASyncTest() {
        buildClientAndExpectException(true, "Invalid searchDnsSuffix", searchServiceName,
                indexName, apiKey, apiVersion, null);
    }

    @Test
    public void verifyNullDnsSuffixIsInvalidTest() {
        buildClientAndExpectException(false, "Invalid searchDnsSuffix", searchServiceName,
                indexName, apiKey, apiVersion, null);
    }

    @Test
    public void verifyEmptyDnsSuffixIsInvalidAsyncTest() {
        buildClientAndExpectException(true, "Invalid searchDnsSuffix", searchServiceName,
                indexName, apiKey, apiVersion, "");
    }

    @Test
    public void verifyEmptyDnsSuffixIsInvalidTest() {
        buildClientAndExpectException(false, "Invalid searchDnsSuffix", searchServiceName,
                indexName, apiKey, apiVersion, "");
    }

    /**
     * Verify that there is a default for the dns suffix
     */
    @Test
    public void verifyDefaultDnsSuffixIsCorrectTest() {

        SearchPipelinePolicy policy = new SearchPipelinePolicy(apiKey);

        SearchIndexClientBuilder clientBuilder = new SearchIndexClientBuilder();
        SearchIndexAsyncClient client =
                clientBuilder.serviceName(searchServiceName).indexName(indexName).addPolicy(policy).apiVersion(apiVersion).buildAsyncClient();

        assert (client != null);
        assert (client.getSearchDnsSuffix() == "search.windows.net");
    }

    /**
     * Verify that the index name can be changed after the client was already created
     */
    @Test
    public void verifyIndexNameIsChangeableTest() {

        String originalIndexName = "firstOne";
        SearchIndexAsyncClientImpl client = buildASyncClient(searchServiceName, originalIndexName, apiKey, apiVersion, dnsSuffix);
        assert (client.getIndexName().equals(originalIndexName));

        String otherIndexName = "ImTheSecond";
        client.setIndexName(otherIndexName);
        assert (client.getIndexName().equals(otherIndexName));
    }
}
