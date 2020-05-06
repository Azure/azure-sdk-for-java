// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.search.documents.implementation.SerializationUtil;
import com.azure.search.documents.models.SearchIndex;
import com.azure.search.documents.test.environment.setup.SearchIndexService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.fail;

public class SearchIndexClientTestBase extends SearchServiceTestBase {

    private static final String HOTELS_TESTS_INDEX_DATA_JSON = "HotelsTestsIndexData.json";
    static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    protected <T> void uploadDocuments(SearchClient client, List<T> uploadDoc) {
        client.uploadDocuments(uploadDoc);
        waitForIndexing();
    }

    protected <T> void uploadDocuments(SearchAsyncClient client, List<T> uploadDoc) {
        client.uploadDocuments(uploadDoc).block();
        waitForIndexing();
    }

    <T> void uploadDocument(SearchClient client, T uploadDoc) {
        client.uploadDocuments(Collections.singletonList(uploadDoc));
        waitForIndexing();
    }

    <T> void uploadDocument(SearchAsyncClient client, T uploadDoc) {
        client.uploadDocuments(Collections.singletonList(uploadDoc)).block();
        waitForIndexing();
    }

    List<Map<String, Object>> uploadDocumentsJson(SearchAsyncClient client, String dataJson) {
        List<Map<String, Object>> documents =
            readJsonFileToList(dataJson, new TypeReference<List<Map<String, Object>>>() { });

        uploadDocuments(client, documents);
        return documents;
    }

    List<Map<String, Object>> uploadDocumentsJson(SearchClient client, String dataJson) {
        List<Map<String, Object>> documents =
            readJsonFileToList(dataJson, new TypeReference<List<Map<String, Object>>>() { });

        uploadDocuments(client, documents);

        return documents;
    }

    private List<Map<String, Object>> readJsonFileToList(String filename,
        TypeReference<List<Map<String, Object>>> listTypeReference) {
        Reader reader = new InputStreamReader(Objects.requireNonNull(getClass().getClassLoader()
            .getResourceAsStream(filename)));

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        SerializationUtil.configureMapper(objectMapper);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            return objectMapper.readValue(reader, listTypeReference);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    SearchClientBuilder getClientBuilder(String indexName) {
        SearchClientBuilder builder = new SearchClientBuilder().endpoint(endpoint)
            .indexName(indexName);
        if (interceptorManager.isPlaybackMode()) {
            return builder.httpClient(interceptorManager.getPlaybackClient());
        }
        builder.httpClient(new NettyAsyncHttpClientBuilder().wiretap(true).build())
            .credential(searchApiKeyCredential);
        if (!liveMode()) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }
        return builder;
    }

    protected void setupIndex(SearchIndex index) {
        if (!interceptorManager.isPlaybackMode()) {
            // In RECORDING mode (only), create a new index:
            SearchIndexService searchIndexService = new SearchIndexService(
                endpoint,
                searchApiKeyCredential.getKey());

            searchIndexService.initializeAndCreateIndex(index);
        }
    }

    void setupIndexFromJsonFile(String jsonFile) {
        if (!interceptorManager.isPlaybackMode()) {
            // In RECORDING mode (only), create a new index:
            SearchIndexService searchIndexService = new SearchIndexService(
                endpoint,
                searchApiKeyCredential.getKey());
            try {
                searchIndexService.initializeAndCreateIndex(jsonFile);
            } catch (IOException e) {
                fail(e.getMessage());
            }
        }
    }

    protected void createHotelIndex() {
        setupIndexFromJsonFile(HOTELS_TESTS_INDEX_DATA_JSON);
    }
}
