// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.data.test.customization;

import com.azure.core.http.rest.PagedFlux;
import com.azure.search.data.SearchIndexAsyncClient;
import com.azure.search.data.SearchIndexClientTestBase;
import com.azure.search.data.generated.models.IndexAction;
import com.azure.search.data.generated.models.IndexActionType;
import com.azure.search.data.generated.models.IndexBatch;
import com.azure.search.data.generated.models.SearchParameters;
import com.azure.search.data.generated.models.SearchRequestOptions;
import com.azure.search.data.generated.models.SearchResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;

public class SearchTests extends SearchIndexClientTestBase {

    private SearchIndexAsyncClient asyncClient;
    private List<Map<String, Object>> hotels;

    private static final String INDEX_NAME = "hotels";
    private static final String HOTELS_DATA_JSON = "LargeHotelDataArray.json";

    @Override
    protected void beforeTest() {
        super.beforeTest();
        asyncClient = builderSetup().indexName(INDEX_NAME).buildAsyncClient();
        try {
            uploadDocuments();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void uploadDocuments() throws IOException {
        Reader docsData = new InputStreamReader(
            getClass().getClassLoader().getResourceAsStream(HOTELS_DATA_JSON));
        hotels = new ObjectMapper().readValue(docsData, List.class);
        List<IndexAction> indexActions = new LinkedList<>();

        hotels.forEach(h -> {
            HashMap<String, Object> hotel = new HashMap<String, Object>(h);
            indexActions.add(new IndexAction()
                .actionType(IndexActionType.UPLOAD)
                .additionalProperties(hotel));
        });

        asyncClient.index(
            new IndexBatch().actions(indexActions)).block();
    }

    @Test
    public void testCanSearchDynamicDocuments() {
        PagedFlux<SearchResult> results = asyncClient.search("*", new SearchParameters(), new SearchRequestOptions());
        StepVerifier.create(results.byPage())
            .assertNext(firstPage -> {
                Assert.assertEquals(firstPage.value().size(), 50);
                Assert.assertNotEquals(firstPage.nextLink(), null);
            })
            .assertNext(nextPage -> {
                Assert.assertEquals(nextPage.value().size(), 50);
                Assert.assertEquals(nextPage.nextLink(), null);
            }).verifyComplete();
    }
}
