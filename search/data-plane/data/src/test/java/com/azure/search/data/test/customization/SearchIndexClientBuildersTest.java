// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.data.test.customization;

import com.azure.search.data.common.SearchPipelinePolicy;
import com.azure.search.data.SearchIndexASyncClient;
import com.azure.search.data.SearchIndexClient;
import com.azure.search.data.customization.SearchIndexBaseClientImpl;
import com.azure.search.data.customization.SearchIndexClientBuilderImpl;
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
    private SearchIndexClient buildClient(String searchServiceName, String indexName, String apiKey, String apiVersion, String dnsSuffix) {
        SearchPipelinePolicy policy = new SearchPipelinePolicy(apiKey);

        SearchIndexClientBuilderImpl clientBuilder = new SearchIndexClientBuilderImpl();
        SearchIndexClient client =
                clientBuilder.serviceName(searchServiceName).indexName(indexName).policy(policy).apiVersion(apiVersion).searchDnsSuffix(dnsSuffix).buildClient();

        assert (client != null);
        assert (client.getClass().getSimpleName().equals("SearchIndexClientImpl"));

        return client;
    }

    /**
     * Builds an ASync Search Index client
     */
    private SearchIndexASyncClient buildASyncClient(String searchServiceName, String indexName, String apiKey, String apiVersion, String dnsSuffix) {
        SearchPipelinePolicy policy = new SearchPipelinePolicy(apiKey);

        SearchIndexClientBuilderImpl clientBuilder = new SearchIndexClientBuilderImpl();
        SearchIndexASyncClient client =
                clientBuilder.serviceName(searchServiceName).indexName(indexName).policy(policy).apiVersion(apiVersion).searchDnsSuffix(dnsSuffix).buildAsyncClient();

        assert (client != null);
        assert (client.getClass().getSimpleName().equals("SearchIndexASyncClientImpl"));

        return client;
    }

    private void buildClientAndVerifyInternal(boolean isASync) {
        SearchIndexBaseClientImpl client;
        if (isASync) {
            client = (SearchIndexBaseClientImpl) buildClient(searchServiceName, indexName, apiKey, apiVersion, dnsSuffix);

        } else {
            client = (SearchIndexBaseClientImpl) buildASyncClient(searchServiceName, indexName, apiKey, apiVersion, dnsSuffix);
        }

        assert (client.getIndexName().equals(indexName));
        assert (client.getSearchServiceName().equals(searchServiceName));
        assert (client.getApiVersion().equals(apiVersion));
        assert (client.getSearchDnsSuffix().equals(dnsSuffix));
    }

    /**
     * Verify that if the property is invalid the client builder throws exception
     */
    private void BuildClientAndExpectException(boolean isASync,
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
        searchServiceName = "";
        apiKey = "";
        indexName = "hotels";
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
    public void VerifyNullSearchServiceNameIsInvalidASyncTest() throws Exception {
        BuildClientAndExpectException(true, "Invalid searchServiceName", null,
                indexName, apiKey, apiVersion, dnsSuffix);
    }

    /**
     * Verify that if the property is invalid the client builder throws exception
     */
    @Test
    public void VerifyNullSearchServiceNameIsInvalidTest() throws Exception {
        BuildClientAndExpectException(false, "Invalid searchServiceName", null,
                indexName, apiKey, apiVersion, dnsSuffix);
    }

    /**
     * Verify that if the property is invalid the client builder throws exception
     */
    @Test
    public void VerifyEmptySearchServiceNameIsInvalidASyncTest() throws Exception {
        BuildClientAndExpectException(false, "Invalid searchServiceName", "",
                indexName, apiKey, apiVersion, dnsSuffix);
    }

    /**
     * Verify that if the property is invalid the client builder throws exception
     */
    @Test
    public void VerifyEmptySearchServiceNameIsInvalidTest() throws Exception {
        BuildClientAndExpectException(false, "Invalid searchServiceName", "",
                indexName, apiKey, apiVersion, dnsSuffix);
    }

    /**
     * Verify that if the property is invalid the client builder throws exception
     */
    @Test
    public void VerifyNullIndexNameIsInvalidASyncTest() throws Exception {
        BuildClientAndExpectException(true, "Invalid indexName", searchServiceName,
                null, apiKey, apiVersion, dnsSuffix);
    }

    /**
     * Verify that if the property is invalid the client builder throws exception
     */
    @Test
    public void VerifyNullIndexNameIsInvalidTest() throws Exception {
        BuildClientAndExpectException(false, "Invalid indexName", searchServiceName,
                null, apiKey, apiVersion, dnsSuffix);
    }

    /**
     * Verify that if the property is invalid the client builder throws exception
     */
    @Test
    public void VerifyEmptyIndexNameIsInvalidASyncTest() throws Exception {
        BuildClientAndExpectException(true, "Invalid indexName", searchServiceName,
                "", apiKey, apiVersion, dnsSuffix);
    }

    /**
     * Verify that if the property is invalid the client builder throws exception
     */
    @Test
    public void VerifyEmptyIndexNameIsInvalidTest() throws Exception {
        BuildClientAndExpectException(false, "Invalid indexName", searchServiceName,
                "", apiKey, apiVersion, dnsSuffix);
    }

    /**
     * Verify that if the property is invalid the client builder throws exception
     */
    @Test
    public void VerifyNullApiKeyIsInvalidASyncTest() throws Exception {
        BuildClientAndExpectException(true, "Invalid apiKey", searchServiceName,
                indexName, null, apiVersion, dnsSuffix);
    }

    /**
     * Verify that if the property is invalid the client builder throws exception
     */
    @Test
    public void VerifyNullApiKeyIsInvalidTest() throws Exception {
        BuildClientAndExpectException(false, "Invalid apiKey", searchServiceName,
                indexName, null, apiVersion, dnsSuffix);
    }

    /**
     * Verify that if the property is invalid the client builder throws exception
     */
    @Test
    public void VerifyEmptyApiKeyIsInvalidASyncTest() throws Exception {
        BuildClientAndExpectException(true, "Invalid apiKey", searchServiceName,
                indexName, "", apiVersion, dnsSuffix);
    }

    /**
     * Verify that if the property is invalid the client builder throws exception
     */
    @Test
    public void VerifyEmptyApiKeyIsInvalidTest() throws Exception {
        BuildClientAndExpectException(false, "Invalid apiKey", searchServiceName,
                indexName, "", apiVersion, dnsSuffix);
    }

    /**
     * Verify that if the property is invalid the client builder throws exception
     */
    @Test
    public void VerifyNullApiVersionIsInvalidASyncTest() throws Exception {
        BuildClientAndExpectException(true, "Invalid apiVersion", searchServiceName,
                indexName, apiKey, null, dnsSuffix);
    }

    /**
     * Verify that if the property is invalid the client builder throws exception
     */
    @Test
    public void VerifyNullApiVersionIsInvalidTest() throws Exception {
        BuildClientAndExpectException(false, "Invalid apiVersion", searchServiceName,
                indexName, apiKey, null, dnsSuffix);
    }

    /**
     * Verify that if the property is invalid the client builder throws exception
     */
    @Test
    public void VerifyEmptyApiVersionIsInvalidASyncTest() throws Exception {
        BuildClientAndExpectException(true, "Invalid apiVersion", searchServiceName,
                indexName, apiKey, "", dnsSuffix);
    }

    /**
     * Verify that if the property is invalid the client builder throws exception
     */
    @Test
    public void VerifyEmptyApiVersionIsInvalidTest() throws Exception {
        BuildClientAndExpectException(false, "Invalid apiVersion", searchServiceName,
                indexName, apiKey, "", dnsSuffix);
    }

    /**
     * Verify that if the property is invalid the client builder throws exception
     */
    @Test
    public void VerifyNullDnsSuffixIsInvalidASyncTest() throws Exception {
        BuildClientAndExpectException(true, "Invalid searchDnsSuffix", searchServiceName,
                indexName, apiKey, apiVersion, null);
    }

    /**
     * Verify that if the property is invalid the client builder throws exception
     */
    @Test
    public void VerifyNullDnsSuffixIsInvalidTest() throws Exception {
        BuildClientAndExpectException(false, "Invalid searchDnsSuffix", searchServiceName,
                indexName, apiKey, apiVersion, null);
    }

    /**
     * Verify that if the property is invalid the client builder throws exception
     */
    @Test
    public void VerifyEmptyDnsSuffixIsInvalidAsyncTest() throws Exception {
        BuildClientAndExpectException(true, "Invalid searchDnsSuffix", searchServiceName,
                indexName, apiKey, apiVersion, "");
    }

    /**
     * Verify that if the property is invalid the client builder throws exception
     */
    @Test
    public void VerifyEmptyDnsSuffixIsInvalidTest() throws Exception {
        BuildClientAndExpectException(false, "Invalid searchDnsSuffix", searchServiceName,
                indexName, apiKey, apiVersion, "");
    }

    /**
     * Verify that there is a default for the dns suffix
     */
    @Test
    public void VerifyDefaultDnsSuffixIsCorrectTest() throws Exception {

        SearchPipelinePolicy policy = new SearchPipelinePolicy(apiKey);

        SearchIndexClientBuilderImpl clientBuilder = new SearchIndexClientBuilderImpl();
        SearchIndexASyncClient client =
                clientBuilder.serviceName(searchServiceName).indexName(indexName).policy(policy).apiVersion(apiVersion).buildAsyncClient();

        assert (client != null);
        assert (client.getSearchDnsSuffix() == "search.windows.net");
    }

    /**
     * Verify that the index name can be changed after the client was already created
     */
    @Test
    public void VerifyIndexNameIsChangeableTest() throws Exception {

        String originalIndexName = "firstOne";
        SearchIndexASyncClient client = buildASyncClient(searchServiceName, originalIndexName, apiKey, apiVersion, dnsSuffix);
        assert (client.getIndexName().equals(originalIndexName));

        String otherIndexName = "ImTheSecond";
        client.setIndexName(otherIndexName);
        assert (client.getIndexName().equals(otherIndexName));
    }
}
