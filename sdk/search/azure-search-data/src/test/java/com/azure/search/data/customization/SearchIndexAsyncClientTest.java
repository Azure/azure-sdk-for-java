// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.data.customization;

import com.azure.search.data.SearchIndexAsyncClient;
import com.azure.search.data.env.SearchIndexClientTestBase;
import com.azure.search.data.env.SearchIndexDocs;
import com.azure.search.data.generated.models.DocumentIndexResult;
import com.azure.search.data.generated.models.IndexAction;
import com.azure.search.data.generated.models.IndexActionType;
import com.azure.search.data.generated.models.IndexBatch;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchIndexAsyncClientTest extends SearchIndexClientTestBase {

    private SearchIndexAsyncClient searchIndexAsyncClient;
    private static final String HOTELS_DATA_JSON = "HotelsDataArray.json";
    private static final String INDEX_NAME = "hotels";

    private void addDocsData() throws IOException {

        Reader docsData = new InputStreamReader(
                getClass().getClassLoader().getResourceAsStream(SearchIndexDocs.HOTELS_DATA_JSON));
        List<Map> hotels = new ObjectMapper().readValue(docsData, List.class);
        DocumentIndexResult documentIndexResult  = indexDocuments(searchIndexAsyncClient, hotels);

        System.out.println("Indexing Results:");
        assert documentIndexResult != null;
        documentIndexResult.results().forEach(result ->
                System.out.println(
                        "key:" + result.key() + (result.succeeded() ? " Succeeded" : " Error: " + result.errorMessage()))
        );
    }

    @Override
    protected void beforeTest() {
        super.beforeTest();
        searchIndexAsyncClient = builderSetup().indexName(INDEX_NAME).buildAsyncClient();

        try {
            addDocsData();
        } catch (IOException exc) {

        }
    }

    @Test
    public void indexResultSucceeds() throws Exception {

        List<Map> hotels = loadHotels();

        Map<String, Object> hotel = new HashMap<String, Object>(hotels.get(1));
        List<IndexAction> indexActions = new ArrayList<>();
        indexActions.add(new IndexAction()
            .actionType(IndexActionType.UPLOAD)
            .additionalProperties(hotel)
        );

        DocumentIndexResult result = searchIndexAsyncClient.index(new IndexBatch().actions(indexActions)).block();

        assert result.results().get(0).statusCode() == 200;

    }

    private List<Map> loadHotels() throws IOException {
        Reader docsData = new InputStreamReader(
            getClass().getClassLoader().getResourceAsStream(HOTELS_DATA_JSON));
        List<Map> hotels = new ObjectMapper().readValue(docsData, List.class);
        assert hotels != null;

        return hotels;
    }
}
