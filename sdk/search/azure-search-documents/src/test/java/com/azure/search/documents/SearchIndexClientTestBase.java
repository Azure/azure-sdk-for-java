// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.util.Configuration;
import com.azure.search.documents.implementation.SerializationUtil;
import com.azure.search.documents.models.Index;
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

public class SearchIndexClientTestBase extends SearchServiceTestBase {

    private static final String HOTELS_TESTS_INDEX_DATA_JSON = "HotelsTestsIndexData.json";
    static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    protected <T> void uploadDocuments(SearchIndexClient client, List<T> uploadDoc) {
        client.uploadDocuments(uploadDoc);
        waitForIndexing();
    }

    protected <T> void uploadDocuments(SearchIndexAsyncClient client, List<T> uploadDoc) {
        client.uploadDocuments(uploadDoc).block();
        waitForIndexing();
    }

    <T> void uploadDocument(SearchIndexClient client, T uploadDoc) {
        client.uploadDocuments(Collections.singletonList(uploadDoc));
        waitForIndexing();
    }

    <T> void uploadDocument(SearchIndexAsyncClient client, T uploadDoc) {
        client.uploadDocuments(Collections.singletonList(uploadDoc)).block();
        waitForIndexing();
    }

    List<Map<String, Object>> uploadDocumentsJson(SearchIndexClient client, String dataJson) {
        List<Map<String, Object>> documents =
            readJsonFileToList(dataJson, new TypeReference<List<Map<String, Object>>>() {});

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

    SearchIndexClientBuilder getClientBuilder() {
        SearchIndexClientBuilder builder = new SearchIndexClientBuilder().endpoint(endpoint)
            .indexName(SearchTestBase.HOTELS_INDEX_NAME);
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

    protected void setupIndex(Index index) {
        if (!interceptorManager.isPlaybackMode()) {
            new SearchServiceClientBuilder()
                .endpoint(Configuration.getGlobalConfiguration().get("AZURE_SEARCH_SERVICE_ENDPOINT"))
                .credential(new AzureKeyCredential(Configuration.getGlobalConfiguration()
                    .get("AZURE_SEARCH_SERVICE_API_KEY")))
                .buildClient()
                .createOrUpdateIndex(index);
        }
    }

    void setupIndexFromJsonFile(String jsonFile) {
        if (!interceptorManager.isPlaybackMode()) {
            try {
                Reader indexData = new InputStreamReader(Objects.requireNonNull(getClass().getClassLoader()
                    .getResourceAsStream(jsonFile)));
                Index index = new ObjectMapper().readValue(indexData, Index.class);

                new SearchServiceClientBuilder()
                    .endpoint(Configuration.getGlobalConfiguration().get("AZURE_SEARCH_SERVICE_ENDPOINT"))
                    .credential(new AzureKeyCredential(Configuration.getGlobalConfiguration()
                        .get("AZURE_SEARCH_SERVICE_API_KEY")))
                    .buildClient()
                    .createOrUpdateIndex(index);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
    }

    protected void createHotelIndex() {
        setupIndexFromJsonFile(HOTELS_TESTS_INDEX_DATA_JSON);
    }
}
