// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.data.test.customization;

import com.azure.search.data.SearchIndexAsyncClient;
import org.junit.Assert;
import org.junit.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Double.POSITIVE_INFINITY;
import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.NaN;


public class SearchIndexClientLookupTest extends SearchIndexClientTestBase {
    private SearchIndexAsyncClient client;
    private static final String INDEX_NAME = "data-types-tests-index";


    @Override
    protected void beforeTest() {
        super.beforeTest();
        client = builderSetup().indexName(INDEX_NAME).buildAsyncClient();
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

        // Creating the document to be indexed
        HashMap<String, Object> document = new HashMap<String, Object>();
        document.put("Key", docKey);
        document.put("Dates", new Object[]{});
        document.put("Doubles", new Double[]{0.0, 5.8, POSITIVE_INFINITY, NEGATIVE_INFINITY, NaN});
        document.put("Bools", new boolean[]{true, false});
        document.put("Longs", new Long[]{9999999999999999L, 832372345832523L});
        document.put("Strings", new String[]{"hello", "bye"});
        document.put("Ints", new int[]{1, 2, 3, 4, -13, 5, 0});
        document.put("Points", new Object[]{});

        // This is the expected document when querying the document later
        HashMap<String, Object> expectedDocument = new HashMap<String, Object>();
        expectedDocument.put("Key", docKey);
        expectedDocument.put("Doubles", Arrays.asList(0.0, 5.8, "INF", "-INF", "NaN"));
        expectedDocument.put("Bools", Arrays.asList(true, false));
        expectedDocument.put("Longs", Arrays.asList(9999999999999999L, 832372345832523L));
        expectedDocument.put("Strings", Arrays.asList("hello", "bye"));
        expectedDocument.put("Ints", Arrays.asList(1, 2, 3, 4, -13, 5, 0));
        // Todo: fix below 2 items
        expectedDocument.put("Points", Arrays.asList());
        expectedDocument.put("Dates", Arrays.asList());

        // Index the document
        indexDocument(client, document);

        // Get the indexed document
        getAndVerifyDoc(client, docKey, expectedDocument);
    }

    /**
     * This test verifies that sometimes the converted type is not the right one and its by design
     */
    //@Test
    //public void getDynamicDocumentCannotAlwaysDetermineCorrectType() {
        // Todo: Uncomment test, when task 574 is done
        /*client.setIndexName(INDEX_NAME);
        String docKey = "2";

        // Creating the document to be indexed
        // Set a String field to a valid datetime string
        HashMap<String, Object> document = new HashMap<String, Object>();
        document.put("Key", docKey);
        document.put("Strings", Arrays.asList("2015-02-11T12:58:00Z"));

        // This is the expected document when querying the document later
        // Expect that the returned String field is actually converted to DateTime... which is wrong, but, by design.
        HashMap<String, Object> expectedDocument = new HashMap<String, Object>();
        expectedDocument.put("Key", docKey);
        expectedDocument.put("Strings", Arrays.asList(DateTime.parse("2015-02-11T12:58:00Z")));

        // Index the document
        indexDocument(client, document);

        getAndVerifyDoc(client, docKey, expectedDocument);*/
    //}

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
     * Retrieve and verify the document
     * @param client
     * @param docKey
     * @param expectedDocument
     */
    private void getAndVerifyDoc(SearchIndexAsyncClient client, String docKey, HashMap<String, Object> expectedDocument) {
        // Get the indexed document
        Mono<Map<String, Object>> futureDoc = client.getDocument(docKey);
        // Verify that for every item we indexed we get the right response
        StepVerifier
                .create(futureDoc)
                .assertNext(result -> {
                    Assert.assertEquals(expectedDocument, result);
                })
                .verifyComplete();
    }
}

