// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.data.tests;

import com.azure.search.data.SearchIndexAsyncClient;
import com.azure.search.data.customization.Document;
import com.azure.search.data.generated.models.IndexAction;
import com.azure.search.data.generated.models.IndexBatch;
import com.azure.search.data.models.Hotel;
import org.junit.Assert;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.util.HashMap;
import java.util.List;

public class IndexingAsyncTests extends IndexingTestBase {
    private SearchIndexAsyncClient client;

    @Override
    public void countingDocsOfNewIndexGivesZero() {
        Mono<Long> result = client.countDocuments();
        Long expected = 0L;

        StepVerifier.create(result).expectNext(expected).expectComplete().verify();
    }

    @Override
    public void dynamicDocumentDateTimesRoundTripAsUtc() throws Exception {
        HashMap<String, Object> expected = new HashMap<String, Object>();
        expected.put(HOTEL_ID_FIELD, HOTEL_ID);
        expected.put(LAST_RENOVATION_DATE_FIELD, LAST_RENOVATION_DATE);

        client.setIndexName(INDEX_NAME);
        uploadDocuments(expected);

        Mono<Document> actual = client.getDocument(expected.get(HOTEL_ID_FIELD).toString());
        StepVerifier
            .create(actual)
            .assertNext(res -> {
                Assert.assertEquals(expected.get(HOTEL_ID_FIELD), res.get(HOTEL_ID_FIELD));
                Assert.assertEquals(expected.get(LAST_RENOVATION_DATE_FIELD), res.get(LAST_RENOVATION_DATE_FIELD));
            })
            .verifyComplete();
    }

    @Override
    public void staticallyTypedDateTimesRoundTripAsUtc() throws Exception {
        Hotel expected = new Hotel()
            .hotelId(HOTEL_ID)
            .lastRenovationDate(DATE_FORMAT.parse(LAST_RENOVATION_DATE));
        System.out.println(expected.lastRenovationDate());

        client.setIndexName(INDEX_NAME);
        uploadDocuments(expected);

        Mono<Document> actual = client.getDocument(expected.hotelId());

        StepVerifier
            .create(actual)
            .assertNext(res -> {
                Assert.assertEquals(expected.hotelId(), res.get(HOTEL_ID_FIELD));
                Assert.assertEquals(expected.lastRenovationDate(), res.get(LAST_RENOVATION_DATE_FIELD));
            })
            .verifyComplete();

        //Assert.assertEquals(expected, actual);
    }

    @Override
    protected void indexDocuments(List<IndexAction> indexActions) {
        client.index(new IndexBatch().actions(indexActions)).block();
    }

    @Override
    protected void initializeClient() {
        client = builderSetup().indexName(INDEX_NAME).buildAsyncClient();
    }
}
