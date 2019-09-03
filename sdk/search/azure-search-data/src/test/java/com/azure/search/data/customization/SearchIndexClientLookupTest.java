// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.data.customization;

import com.azure.search.data.SearchIndexAsyncClient;
import com.azure.search.data.customization.models.GeoPoint;
import com.azure.search.data.env.SearchIndexClientTestBase;
import com.azure.search.data.env.SearchIndexService;
import org.junit.Assert;
import org.junit.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Double.POSITIVE_INFINITY;
import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.NaN;


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
    public void dynamicallyTypedPrimitiveCollectionsDoNotAllRoundtripCorrectly() {
        client.setIndexName(INDEX_NAME);
        String docKey = "1";
        Map geoPoint = GeoPoint.createWithDefaultCrs(100.0, 1.0).createObjectMap();

        // Creating the document to be indexed
        HashMap<String, Object> document = new HashMap<String, Object>();
        document.put("Key", docKey);
        document.put("Dates", new Object[]{"2019-08-13T14:30:00Z"});
        document.put("Doubles", new Double[]{0.0, 5.8, POSITIVE_INFINITY, NEGATIVE_INFINITY, NaN});
        document.put("Bools", new boolean[]{true, false});
        document.put("Longs", new Long[]{9999999999999999L, 832372345832523L});
        document.put("Strings", new String[]{"hello", "bye"});
        document.put("Ints", new int[]{1, 2, 3, 4, -13, 5, 0});
        document.put("Points", new Object[]{geoPoint});

        // This is the expected document when querying the document later
        HashMap<String, Object> expectedDocument = new HashMap<String, Object>();
        expectedDocument.put("Key", docKey);
        expectedDocument.put("Doubles", Arrays.asList(0.0, 5.8, "INF", "-INF", "NaN"));
        expectedDocument.put("Bools", Arrays.asList(true, false));
        expectedDocument.put("Longs", Arrays.asList(9999999999999999L, 832372345832523L));
        expectedDocument.put("Strings", Arrays.asList("hello", "bye"));
        expectedDocument.put("Ints", Arrays.asList(1, 2, 3, 4, -13, 5, 0));
        expectedDocument.put("Points", Collections.singletonList(geoPoint));

        // Todo: The decision is to support DateTime as string until we support user supplying the concrete class
        // in order to avoid trying to parse every string to a datetime (performance penalty)
        expectedDocument.put("Dates", Collections.singletonList("2019-08-13T14:30:00Z"));

        // Index the document
        indexDocument(client, document);

        // Get the indexed document
        getAndVerifyDoc(client, docKey, expectedDocument);
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
        expectedDocument.put("Doubles", Arrays.asList());
        expectedDocument.put("Bools", Arrays.asList());
        expectedDocument.put("Longs", Arrays.asList());
        expectedDocument.put("Strings", Arrays.asList());
        expectedDocument.put("Ints", Arrays.asList());
        expectedDocument.put("Points", Arrays.asList());
        expectedDocument.put("Dates", Arrays.asList());

        // Index the document
        indexDocument(client, document);

        // Get the indexed document
        getAndVerifyDoc(client, docKey, expectedDocument);
    }

    /**
     * This test verifies our current assumption that uploaded DateTime, when requested
     * returned as String. this test will be removed when the datetime type is handled
     */
    @Test
    public void dateTimeTypeIsReturnedAsString() {
        final String indexName = "datetime-data-type-test-index";
        client.setIndexName(indexName);
        String docKey = "4";

        // Creating the document to be indexed with a datetime field
        HashMap<String, Object> document = new HashMap<String, Object>();
        document.put("Key", docKey);
        document.put("Date", "2019-08-13T14:30:00Z");

        // This is the expected document when querying the document later
        // we expect the converted type to remain string instead of DateTime
        HashMap<String, Object> expectedDocument = new HashMap<String, Object>();
        expectedDocument.put("Key", docKey);
        expectedDocument.put("Date", "2019-08-13T14:30:00Z");

        // Index the document
        indexDocument(client, document);

        // Get the indexed document
        getAndVerifyDoc(client, docKey, expectedDocument);
    }

    /**
     * Retrieve and verify the document
     *
     * @param client
     * @param docKey
     * @param expectedDocument
     */
    private void getAndVerifyDoc(SearchIndexAsyncClient client, String docKey, HashMap<String, Object> expectedDocument) {
        // Get the indexed document
        Mono<Document> futureDoc = client.getDocument(docKey);
        // Verify that for every item we indexed we get the right response
        StepVerifier
                .create(futureDoc)
                .assertNext(result -> {
                    Assert.assertEquals(expectedDocument, result);
                })
                .verifyComplete();
    }
}

