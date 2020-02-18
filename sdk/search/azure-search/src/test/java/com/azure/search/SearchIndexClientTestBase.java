// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.search.implementation.SerializationUtil;
import com.azure.search.models.Index;
import com.azure.search.test.environment.setup.SearchIndexService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Assert;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SearchIndexClientTestBase extends SearchServiceTestBase {

    private static final String HOTELS_TESTS_INDEX_DATA_JSON = "HotelsTestsIndexData.json";
    static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    protected <T> void uploadDocuments(SearchIndexClient client, List<T> uploadDoc) {
        client.uploadDocuments(uploadDoc);
        waitForIndexing();
    }

    protected <T> void uploadDocuments(SearchIndexAsyncClient client, List<T> uploadDoc) {
        client.uploadDocuments(uploadDoc)
            .block();
        waitForIndexing();
    }

    <T> void uploadDocument(SearchIndexClient client, T uploadDoc) {
        List<T> docs = new ArrayList<>();
        docs.add(uploadDoc);

        client.uploadDocuments(docs);
        waitForIndexing();
    }

    <T> void uploadDocument(SearchIndexAsyncClient client, T uploadDoc) {
        List<T> docs = new ArrayList<>();
        docs.add(uploadDoc);

        client.uploadDocuments(docs)
            .block();
        waitForIndexing();
    }

    List<Map<String, Object>> uploadDocumentsJson(
        SearchIndexAsyncClient client, String dataJson) throws IOException {
        List<Map<String, Object>> documents =
            readJsonFileToList(dataJson, new TypeReference<List<Map<String, Object>>>() {
            });

        uploadDocuments(client, documents);
        return documents;
    }

    List<Map<String, Object>> uploadDocumentsJson(
        SearchIndexClient client, String dataJson) throws IOException {
        List<Map<String, Object>> documents =
            readJsonFileToList(dataJson, new TypeReference<List<Map<String, Object>>>() {
            });

        uploadDocuments(client, documents);

        return documents;
    }

    private List<Map<String, Object>> readJsonFileToList(String filename, TypeReference<List<Map<String, Object>>> listTypeReference) throws IOException {
        Reader reader = new InputStreamReader(
            getClass().getClassLoader().getResourceAsStream(filename));

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        SerializationUtil.configureMapper(objectMapper);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper.readValue(reader, listTypeReference);
    }

    SearchIndexClientBuilder getClientBuilder(String indexName) {
        if (!interceptorManager.isPlaybackMode()) {
            return new SearchIndexClientBuilder()
                .endpoint(endpoint)
                .indexName(indexName)
                .httpClient(new NettyAsyncHttpClientBuilder().wiretap(true).build())
                .credential(searchApiKeyCredential)
                .addPolicy(interceptorManager.getRecordPolicy())
                .addPolicy(new RetryPolicy())
                .addPolicy(new HttpLoggingPolicy(
                    new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS)));
        } else {
            return new SearchIndexClientBuilder()
                .endpoint(endpoint)
                .indexName(indexName)
                .httpClient(interceptorManager.getPlaybackClient());
        }
    }

    protected void setupIndex(Index index) {
        if (!interceptorManager.isPlaybackMode()) {
            // In RECORDING mode (only), create a new index:
            SearchIndexService searchIndexService = new SearchIndexService(
                endpoint,
                searchApiKeyCredential.getApiKey());

            searchIndexService.initializeAndCreateIndex(index);
        }
    }

    void setupIndexFromJsonFile(String jsonFile) {
        if (!interceptorManager.isPlaybackMode()) {
            // In RECORDING mode (only), create a new index:
            SearchIndexService searchIndexService = new SearchIndexService(
                endpoint,
                searchApiKeyCredential.getApiKey());
            try {
                searchIndexService.initializeAndCreateIndex(jsonFile);
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
        }
    }

    protected void createHotelIndex() {
        setupIndexFromJsonFile(HOTELS_TESTS_INDEX_DATA_JSON);
    }
}
