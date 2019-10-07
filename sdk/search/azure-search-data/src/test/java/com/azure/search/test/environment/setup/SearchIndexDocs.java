// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.test.environment.setup;

import com.azure.search.data.customization.SearchIndexAsyncClient;
import com.azure.search.data.common.credentials.ApiKeyCredentials;
import com.azure.search.data.customization.SearchIndexClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Map;

public class SearchIndexDocs {

    public static final String HOTELS_DATA_JSON = "HotelsDataArray.json";

    private String searchServiceName;
    private ApiKeyCredentials apiAdminKey;
    private String indexName;
    private String dnsSuffix;
    private String apiVersion;

    private SearchIndexAsyncClient searchIndexAsyncClient;

    /**
     * Creates an instance of SearchIndexASyncClient to be used in uploading documents to a certain index,
     * to be used in tests.
     *
     * @param searchServiceName The name of the Search service
     * @param apiAdminKey       The Admin key of the Search service
     * @param indexName         The name of the index
     * @param dnsSuffix         DNS suffix of the Search service
     * @param apiVersion        used API version
     */
    public SearchIndexDocs(
        String searchServiceName, ApiKeyCredentials apiAdminKey, String indexName,
        String dnsSuffix, String apiVersion) {
        this.searchServiceName = searchServiceName;
        this.apiAdminKey = apiAdminKey;
        this.indexName = indexName;
        this.dnsSuffix = dnsSuffix;
        this.apiVersion = apiVersion;
    }

    /**
     * Created new documents in the index. The new documents are retrieved from HotelsDataArray.json
     *
     * @throws IOException If the file in HOTELS_DATA_JSON is not existing or invalid.
     */
    public void initialize() throws IOException {
        if (searchIndexAsyncClient == null) {
            searchIndexAsyncClient = new SearchIndexClientBuilder()
                .serviceName(searchServiceName)
                .searchDnsSuffix(dnsSuffix)
                .indexName(indexName)
                .apiVersion(apiVersion)
                .credential(apiAdminKey)
                .buildAsyncClient();
        }
        addDocsData();
    }

    private void addDocsData() throws IOException {
        Reader docsData = new InputStreamReader(
            getClass().getClassLoader().getResourceAsStream(HOTELS_DATA_JSON));
        List<Map> hotels = new ObjectMapper().readValue(docsData, List.class);

        System.out.println("Indexing " + hotels.size() + " docs");
        System.out.println("Indexing Results:");
        searchIndexAsyncClient.uploadDocuments(hotels)
            .doOnSuccess(documentIndexResult ->
                documentIndexResult
                    .results().forEach(
                        result ->
                        System.out.println("key:" + result.key() + (result.succeeded() ? " Succeeded" : " Error: " + result.errorMessage()))))
            .doOnError(e -> System.out.println(e.getMessage()))
            .block();
    }
}
