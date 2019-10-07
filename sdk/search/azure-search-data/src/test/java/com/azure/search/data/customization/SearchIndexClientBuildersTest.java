// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.data.customization;

import com.azure.search.data.SearchIndexAsyncClient;
import com.azure.search.data.SearchIndexClient;
import org.junit.Assert;
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

        SearchIndexClientBuilder clientBuilder = new SearchIndexClientBuilder();
        SearchIndexClient client = clientBuilder
            .serviceName(searchServiceName)
            .indexName(indexName)
            .credential(apiKey)
            .apiVersion(apiVersion)
            .searchDnsSuffix(dnsSuffix)
            .buildClient();

        Assert.assertNotNull(client);
        Assert.assertEquals(client.getClass().getSimpleName(), SearchIndexClientImpl.class.getSimpleName());

        return (SearchIndexClientImpl) client;
    }

    /**
     * Builds a Sync Search Index client
     */
    private SearchIndexClientImpl buildClient(String searchServiceName, String indexName, String apiKey, String dnsSuffix) {

        SearchIndexClientBuilder clientBuilder = new SearchIndexClientBuilder();
        SearchIndexClient client = clientBuilder
            .serviceName(searchServiceName)
            .indexName(indexName)
            .credential(apiKey)
            .searchDnsSuffix(dnsSuffix)
            .buildClient();

        Assert.assertNotNull(client);
        Assert.assertEquals(client.getClass().getSimpleName(), SearchIndexClientImpl.class.getSimpleName());

        return (SearchIndexClientImpl) client;
    }

    /**
     * Builds an Async Search Index client
     */
    private SearchIndexAsyncClientImpl buildAsyncClient(String searchServiceName, String indexName, String apiKey, String apiVersion, String dnsSuffix) {

        SearchIndexClientBuilder clientBuilder = new SearchIndexClientBuilder();
        SearchIndexAsyncClient client = clientBuilder
            .serviceName(searchServiceName)
            .indexName(indexName)
            .credential(apiKey)
            .apiVersion(apiVersion)
            .searchDnsSuffix(dnsSuffix)
            .buildAsyncClient();

        Assert.assertNotNull(client);
        Assert.assertEquals(client.getClass().getSimpleName(), SearchIndexAsyncClientImpl.class.getSimpleName());

        return (SearchIndexAsyncClientImpl) client;
    }

    /**
     * Builds an Async Search Index client
     */
    private SearchIndexAsyncClientImpl buildAsyncClient(String searchServiceName, String indexName, String apiKey, String dnsSuffix) {

        SearchIndexClientBuilder clientBuilder = new SearchIndexClientBuilder();
        SearchIndexAsyncClient client = clientBuilder
            .serviceName(searchServiceName)
            .indexName(indexName)
            .credential(apiKey)
            .searchDnsSuffix(dnsSuffix)
            .buildAsyncClient();

        Assert.assertNotNull(client);
        Assert.assertEquals(client.getClass().getSimpleName(), SearchIndexAsyncClientImpl.class.getSimpleName());

        return (SearchIndexAsyncClientImpl) client;
    }

    private void buildClientAndVerifyInternal(boolean isAsync) {
        SearchIndexBaseClient client;
        if (isAsync) {
            client = buildClient(searchServiceName, indexName, apiKey, apiVersion, dnsSuffix);

        } else {
            client = buildAsyncClient(searchServiceName, indexName, apiKey, apiVersion, dnsSuffix);
        }

        assert (client.getIndexName().equals(indexName));
        assert (client.getSearchServiceName().equals(searchServiceName));
        assert (client.getApiVersion().equals(apiVersion));
        assert (client.getSearchDnsSuffix().equals(dnsSuffix));
    }

    /**
     * Verify that if the property is invalid the client builder throws exception
     */
    private void buildClientAndExpectException(boolean isAsync,
                                               String expectedMsg,
                                               String searchServiceName,
                                               String indexName,
                                               String apiKey,
                                               String apiVersion,
                                               String dnsSuffix) {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(expectedMsg);

        if (isAsync) {
            buildAsyncClient(searchServiceName, indexName, apiKey, apiVersion, dnsSuffix);
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
    public void buildSyncClientUsingDefaultApiVersionTest() {
        buildClient(searchServiceName, indexName, apiKey, dnsSuffix);
    }

    @Test
    public void buildAsyncClientTest() {
        buildAsyncClient(searchServiceName, indexName, apiKey, apiVersion, dnsSuffix);
    }

    @Test
    public void whenApiVersionNotSpecifiedThenDefaultValueExists() {
        SearchIndexClient searchIndexClient = buildClient(searchServiceName, indexName, apiKey, dnsSuffix);
        String expectedVersion = "2019-05-06";
        Assert.assertEquals(expectedVersion, searchIndexClient.getApiVersion());

        SearchIndexAsyncClient searchIndexAsyncClient = buildAsyncClient(searchServiceName, indexName, apiKey, dnsSuffix);
        Assert.assertEquals(expectedVersion, searchIndexAsyncClient.getApiVersion());
    }

    @Test
    public void whenApiVersionSpecifiedThenSpecifiedValueExists() {
        String apiToTest = "abc";
        SearchIndexClient searchIndexClient = buildClient(searchServiceName, indexName, apiKey, apiToTest, dnsSuffix);
        Assert.assertEquals(apiToTest, searchIndexClient.getApiVersion());

        SearchIndexAsyncClient searchIndexAsyncClient = buildAsyncClient(searchServiceName, indexName, apiKey, apiToTest, dnsSuffix);
        Assert.assertEquals(apiToTest, searchIndexAsyncClient.getApiVersion());
    }

    @Test
    public void whenBuildAsyncClientUsingDefaultApiVersionThenSuccess() {
        buildAsyncClient(searchServiceName, indexName, apiKey, dnsSuffix);
    }

    @Test
    public void whenBuildClientAndVerifyPropertiesThenSuccess() {
        buildClientAndVerifyInternal(false);
        buildClientAndVerifyInternal(true);
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();


    @Test
    public void verifyNullSearchServiceNameIsInvalidAsyncTest() {
        buildClientAndExpectException(true, "Invalid searchServiceName", null,
            indexName, apiKey, apiVersion, dnsSuffix);
    }

    @Test
    public void verifyNullSearchServiceNameIsInvalidTest() {
        buildClientAndExpectException(false, "Invalid searchServiceName", null,
            indexName, apiKey, apiVersion, dnsSuffix);
    }

    @Test
    public void verifyEmptySearchServiceNameIsInvalidAsyncTest() {
        buildClientAndExpectException(false, "Invalid searchServiceName", "",
            indexName, apiKey, apiVersion, dnsSuffix);
    }

    @Test
    public void verifyEmptySearchServiceNameIsInvalidTest() {
        buildClientAndExpectException(false, "Invalid searchServiceName", "",
            indexName, apiKey, apiVersion, dnsSuffix);
    }

    @Test
    public void verifyNullIndexNameIsInvalidAsyncTest() {
        buildClientAndExpectException(true, "Invalid indexName", searchServiceName,
            null, apiKey, apiVersion, dnsSuffix);
    }

    @Test
    public void verifyNullIndexNameIsInvalidTest() {
        buildClientAndExpectException(false, "Invalid indexName", searchServiceName,
            null, apiKey, apiVersion, dnsSuffix);
    }

    @Test
    public void verifyEmptyIndexNameIsInvalidAsyncTest() {
        buildClientAndExpectException(true, "Invalid indexName", searchServiceName,
            "", apiKey, apiVersion, dnsSuffix);
    }

    @Test
    public void verifyEmptyIndexNameIsInvalidTest() {
        buildClientAndExpectException(false, "Invalid indexName", searchServiceName,
            "", apiKey, apiVersion, dnsSuffix);
    }

    @Test
    public void verifyNullApiKeyIsInvalidAsyncTest() {
        buildClientAndExpectException(true, "Empty apiKey", searchServiceName,
            indexName, null, apiVersion, dnsSuffix);
    }

    @Test
    public void verifyNullApiKeyIsInvalidTest() {
        buildClientAndExpectException(false, "Empty apiKey", searchServiceName,
            indexName, null, apiVersion, dnsSuffix);
    }

    @Test
    public void verifyEmptyApiKeyIsInvalidAsyncTest() {
        buildClientAndExpectException(true, "Empty apiKey", searchServiceName,
            indexName, "", apiVersion, dnsSuffix);
    }

    @Test
    public void verifyEmptyApiKeyIsInvalidTest() {
        buildClientAndExpectException(false, "Empty apiKey", searchServiceName,
            indexName, "", apiVersion, dnsSuffix);
    }

    @Test
    public void verifyNullApiVersionIsInvalidAsyncTest() {
        buildClientAndExpectException(true, "Invalid apiVersion", searchServiceName,
            indexName, apiKey, null, dnsSuffix);
    }

    @Test
    public void verifyNullApiVersionIsInvalidTest() {
        buildClientAndExpectException(false, "Invalid apiVersion", searchServiceName,
            indexName, apiKey, null, dnsSuffix);
    }

    @Test
    public void verifyEmptyApiVersionIsInvalidAsyncTest() {
        buildClientAndExpectException(true, "Invalid apiVersion", searchServiceName,
            indexName, apiKey, "", dnsSuffix);
    }

    @Test
    public void verifyEmptyApiVersionIsInvalidTest() {
        buildClientAndExpectException(false, "Invalid apiVersion", searchServiceName,
            indexName, apiKey, "", dnsSuffix);
    }

    @Test
    public void verifyNullDnsSuffixIsInvalidAsyncTest() {
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
        SearchIndexClientBuilder clientBuilder = new SearchIndexClientBuilder();
        SearchIndexAsyncClient client = clientBuilder
            .serviceName(searchServiceName)
            .indexName(indexName)
            .credential(apiKey)
            .apiVersion(apiVersion)
            .buildAsyncClient();

        assert (client != null);
        assert (client.getSearchDnsSuffix() == "search.windows.net");
    }

    /**
     * Verify that the index name can be changed after the client was already created
     */
    @Test
    public void verifyIndexNameIsChangeableTest() {

        String originalIndexName = "firstOne";
        SearchIndexAsyncClientImpl client = buildAsyncClient(searchServiceName, originalIndexName, apiKey, apiVersion, dnsSuffix);
        assert (client.getIndexName().equals(originalIndexName));

        String otherIndexName = "ImTheSecond";
        client.setIndexName(otherIndexName);
        assert (client.getIndexName().equals(otherIndexName));
    }
}
