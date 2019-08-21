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
                                               String dnsSuffix) throws Exception {
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

    /**
     * Build Sync client, verify class is built and no exception was raised
     */
    @Test
    public void buildSyncClientTest() throws Exception {
        buildClient(searchServiceName, indexName, apiKey, apiVersion, dnsSuffix);
    }

    /**
     * Build ASync client, verify class is built and no exception was raised
     */
    @Test
    public void buildASyncClientTest() throws Exception {
        buildASyncClient(searchServiceName, indexName, apiKey, apiVersion, dnsSuffix);
    }


    /**
     * Verify that the client is built and the properties are assigned as expected
     */
    @Test
    public void buildClientAndVerifyPropertiesTest() throws Exception {
        buildClientAndVerifyInternal(false);
        buildClientAndVerifyInternal(true);
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();


    /**
     * Verify that if the property is invalid the client builder throws exception
     */
    @Test
    public void verifyNullSearchServiceNameIsInvalidASyncTest() throws Exception {
        buildClientAndExpectException(true, "Invalid searchServiceName", null,
                indexName, apiKey, apiVersion, dnsSuffix);
    }

    /**
     * Verify that if the property is invalid the client builder throws exception
     */
    @Test
    public void verifyNullSearchServiceNameIsInvalidTest() throws Exception {
        buildClientAndExpectException(false, "Invalid searchServiceName", null,
                indexName, apiKey, apiVersion, dnsSuffix);
    }

    /**
     * Verify that if the property is invalid the client builder throws exception
     */
    @Test
    public void verifyEmptySearchServiceNameIsInvalidASyncTest() throws Exception {
        buildClientAndExpectException(false, "Invalid searchServiceName", "",
                indexName, apiKey, apiVersion, dnsSuffix);
    }

    /**
     * Verify that if the property is invalid the client builder throws exception
     */
    @Test
    public void verifyEmptySearchServiceNameIsInvalidTest() throws Exception {
        buildClientAndExpectException(false, "Invalid searchServiceName", "",
                indexName, apiKey, apiVersion, dnsSuffix);
    }

    /**
     * Verify that if the property is invalid the client builder throws exception
     */
    @Test
    public void verifyNullIndexNameIsInvalidASyncTest() throws Exception {
        buildClientAndExpectException(true, "Invalid indexName", searchServiceName,
                null, apiKey, apiVersion, dnsSuffix);
    }

    /**
     * Verify that if the property is invalid the client builder throws exception
     */
    @Test
    public void verifyNullIndexNameIsInvalidTest() throws Exception {
        buildClientAndExpectException(false, "Invalid indexName", searchServiceName,
                null, apiKey, apiVersion, dnsSuffix);
    }

    /**
     * Verify that if the property is invalid the client builder throws exception
     */
    @Test
    public void verifyEmptyIndexNameIsInvalidASyncTest() throws Exception {
        buildClientAndExpectException(true, "Invalid indexName", searchServiceName,
                "", apiKey, apiVersion, dnsSuffix);
    }

    /**
     * Verify that if the property is invalid the client builder throws exception
     */
    @Test
    public void verifyEmptyIndexNameIsInvalidTest() throws Exception {
        buildClientAndExpectException(false, "Invalid indexName", searchServiceName,
                "", apiKey, apiVersion, dnsSuffix);
    }

    /**
     * Verify that if the property is invalid the client builder throws exception
     */
    @Test
    public void verifyNullApiKeyIsInvalidASyncTest() throws Exception {
        buildClientAndExpectException(true, "Invalid apiKey", searchServiceName,
                indexName, null, apiVersion, dnsSuffix);
    }

    /**
     * Verify that if the property is invalid the client builder throws exception
     */
    @Test
    public void verifyNullApiKeyIsInvalidTest() throws Exception {
        buildClientAndExpectException(false, "Invalid apiKey", searchServiceName,
                indexName, null, apiVersion, dnsSuffix);
    }

    /**
     * Verify that if the property is invalid the client builder throws exception
     */
    @Test
    public void verifyEmptyApiKeyIsInvalidASyncTest() throws Exception {
        buildClientAndExpectException(true, "Invalid apiKey", searchServiceName,
                indexName, "", apiVersion, dnsSuffix);
    }

    /**
     * Verify that if the property is invalid the client builder throws exception
     */
    @Test
    public void verifyEmptyApiKeyIsInvalidTest() throws Exception {
        buildClientAndExpectException(false, "Invalid apiKey", searchServiceName,
                indexName, "", apiVersion, dnsSuffix);
    }

    /**
     * Verify that if the property is invalid the client builder throws exception
     */
    @Test
    public void verifyNullApiVersionIsInvalidASyncTest() throws Exception {
        buildClientAndExpectException(true, "Invalid apiVersion", searchServiceName,
                indexName, apiKey, null, dnsSuffix);
    }

    /**
     * Verify that if the property is invalid the client builder throws exception
     */
    @Test
    public void verifyNullApiVersionIsInvalidTest() throws Exception {
        buildClientAndExpectException(false, "Invalid apiVersion", searchServiceName,
                indexName, apiKey, null, dnsSuffix);
    }

    /**
     * Verify that if the property is invalid the client builder throws exception
     */
    @Test
    public void verifyEmptyApiVersionIsInvalidASyncTest() throws Exception {
        buildClientAndExpectException(true, "Invalid apiVersion", searchServiceName,
                indexName, apiKey, "", dnsSuffix);
    }

    /**
     * Verify that if the property is invalid the client builder throws exception
     */
    @Test
    public void verifyEmptyApiVersionIsInvalidTest() throws Exception {
        buildClientAndExpectException(false, "Invalid apiVersion", searchServiceName,
                indexName, apiKey, "", dnsSuffix);
    }

    /**
     * Verify that if the property is invalid the client builder throws exception
     */
    @Test
    public void verifyNullDnsSuffixIsInvalidASyncTest() throws Exception {
        buildClientAndExpectException(true, "Invalid searchDnsSuffix", searchServiceName,
                indexName, apiKey, apiVersion, null);
    }

    /**
     * Verify that if the property is invalid the client builder throws exception
     */
    @Test
    public void verifyNullDnsSuffixIsInvalidTest() throws Exception {
        buildClientAndExpectException(false, "Invalid searchDnsSuffix", searchServiceName,
                indexName, apiKey, apiVersion, null);
    }

    /**
     * Verify that if the property is invalid the client builder throws exception
     */
    @Test
    public void verifyEmptyDnsSuffixIsInvalidAsyncTest() throws Exception {
        buildClientAndExpectException(true, "Invalid searchDnsSuffix", searchServiceName,
                indexName, apiKey, apiVersion, "");
    }

    /**
     * Verify that if the property is invalid the client builder throws exception
     */
    @Test
    public void verifyEmptyDnsSuffixIsInvalidTest() throws Exception {
        buildClientAndExpectException(false, "Invalid searchDnsSuffix", searchServiceName,
                indexName, apiKey, apiVersion, "");
    }

    /**
     * Verify that there is a default for the dns suffix
     */
    @Test
    public void verifyDefaultDnsSuffixIsCorrectTest() throws Exception {

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
    public void verifyIndexNameIsChangeableTest() throws Exception {

        String originalIndexName = "firstOne";
        SearchIndexAsyncClientImpl client = buildASyncClient(searchServiceName, originalIndexName, apiKey, apiVersion, dnsSuffix);
        assert (client.getIndexName().equals(originalIndexName));

        String otherIndexName = "ImTheSecond";
        client.setIndexName(otherIndexName);
        assert (client.getIndexName().equals(otherIndexName));
    }
}
