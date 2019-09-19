// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.data.customization;

import com.azure.search.data.SearchIndexAsyncClient;
import com.azure.search.data.common.jsonwrapper.JsonWrapper;
import com.azure.search.data.common.jsonwrapper.api.JsonApi;
import com.azure.search.data.common.jsonwrapper.jacksonwrapper.JacksonDeserializer;
import com.azure.search.data.customization.models.GeoPoint;
import com.azure.search.test.environment.setup.SearchIndexService;
import com.azure.search.data.customization.models.ModelWithPrimitiveCollections;
import org.junit.Assert;
import org.junit.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.NaN;
import static java.lang.Double.POSITIVE_INFINITY;


public class SearchIndexClientLookupTest extends SearchIndexClientTestBase {
    private SearchIndexAsyncClient client;
    private static final String INDEX_NAME = "data-types-tests-index";
    private static final String MODEL_WITH_DATA_TYPES_INDEX_JSON = "DataTypesTestsIndexData.json";

    @Override
    protected void beforeTest() {
        super.beforeTest();
        client = builderSetup().indexName(INDEX_NAME).buildAsyncClient();

        if (!interceptorManager.isPlaybackMode()) {
            // In RECORDING mode (only), create a new index:
            SearchIndexService searchIndexService = new SearchIndexService(
                MODEL_WITH_DATA_TYPES_INDEX_JSON, searchServiceName, apiKey);
            try {
                searchIndexService.initialize();
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
        }
    }

    /**
     * This test verify that the same types that were indexed are returned, but some edge cases are not returned as
     * expected such as Double.POSITIVE_INFINITY which returns as a string "INF"
     * This test verifies that both the 'non edge case' types are returned correctly and that
     * the edge cases are behaving as expected
     */
    @Test
    public void dynamicallyTypedPrimitiveCollectionsDoNotAllRoundtripCorrectly() throws Exception {
        JsonApi jsonApi = JsonWrapper.newInstance(JacksonDeserializer.class);
        String docKey = "1";
        String dateTimeString = "2019-08-13T14:30:00Z";
        GeoPoint geoPoint = GeoPoint.createWithDefaultCrs(100.0, 1.0);

        // Creating the document to be indexed
        ModelWithPrimitiveCollections document = new ModelWithPrimitiveCollections()
            .key(docKey)
            .dates(new Date[]{DATE_FORMAT.parse(dateTimeString)})
            .doubles(new Double[]{0.0, 5.8, POSITIVE_INFINITY, NEGATIVE_INFINITY, NaN})
            .bools(new Boolean[]{true, false})
            .longs(new Long[]{9999999999999999L, 832372345832523L})
            .strings(new String[]{"hello", "bye"})
            .ints(new int[]{1, 2, 3, 4, -13, 5, 0})
            .points(new GeoPoint[]{geoPoint});

        // This is the expected document when querying the document later
        HashMap<String, Object> expectedDocument = new HashMap<String, Object>();
        expectedDocument.put("Key", docKey);
        expectedDocument.put("Doubles", Arrays.asList(0.0, 5.8, "INF", "-INF", "NaN"));
        expectedDocument.put("Bools", Arrays.asList(true, false));
        expectedDocument.put("Longs", Arrays.asList(9999999999999999L, 832372345832523L));
        expectedDocument.put("Strings", Arrays.asList("hello", "bye"));
        expectedDocument.put("Ints", Arrays.asList(1, 2, 3, 4, -13, 5, 0));
        expectedDocument.put("Points", Collections.singletonList(jsonApi.convertObjectToType(geoPoint, Map.class)));
        expectedDocument.put("Dates", Collections.singletonList(dateTimeString));

        // Index the document
        uploadDocuments(client, INDEX_NAME, document);

        // Get the indexed document
        getAndVerifyDoc(docKey, expectedDocument);
    }

    @Test
    public void emptyDynamicallyTypedPrimitiveCollectionsRoundtripAsObjectArrays() {
        client.setIndexName(INDEX_NAME);
        String docKey = "3";

        // Creating the document to be indexed
        HashMap<String, Object> document = new HashMap<String, Object>();
        document.put("Key", docKey);
        document.put("Dates", new Object[]{});
        document.put("Doubles", new Double[]{});
        document.put("Bools", new boolean[]{});
        document.put("Longs", new Long[]{});
        document.put("Strings", new String[]{});
        document.put("Ints", new int[]{});
        document.put("Points", new Object[]{});

        // This is the expected document when querying the document later
        HashMap<String, Object> expectedDocument = new HashMap<String, Object>();
        expectedDocument.put("Key", docKey);
        expectedDocument.put("Doubles", Collections.emptyList());
        expectedDocument.put("Bools", Collections.emptyList());
        expectedDocument.put("Longs", Collections.emptyList());
        expectedDocument.put("Strings", Collections.emptyList());
        expectedDocument.put("Ints", Collections.emptyList());
        expectedDocument.put("Points", Collections.emptyList());
        expectedDocument.put("Dates", Collections.emptyList());

        // Index the document
        uploadDocuments(client, INDEX_NAME, document);

        // Get the indexed document
        getAndVerifyDoc(docKey, expectedDocument);
    }

    /**
     * Retrieve and verify the document
     *
     * @param docKey
     * @param expectedDocument
     */
    private void getAndVerifyDoc(String docKey, HashMap<String, Object> expectedDocument) {
        // Get the indexed document
        Mono<Document> futureDoc = client.getDocument(docKey);
        // Verify that for every item we indexed we get the right response
        StepVerifier
                .create(futureDoc)
            .assertNext(result -> Assert.assertEquals(expectedDocument, result))
                .verifyComplete();
    }
}

