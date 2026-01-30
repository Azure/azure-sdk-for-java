// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.perf;

import com.azure.search.documents.models.IndexAction;
import com.azure.search.documents.models.IndexActionType;
import com.azure.search.documents.models.IndexDocumentsBatch;
import com.azure.search.perf.core.DocumentGenerator;
import com.azure.search.perf.core.DocumentSize;
import com.azure.search.perf.core.SearchPerfStressOptions;
import com.azure.search.perf.core.ServiceTest;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Performs document indexing operations.
 */
public class IndexDocumentsTest extends ServiceTest<SearchPerfStressOptions> {
    private static volatile AtomicInteger ID_COUNT = new AtomicInteger();
    private final List<Map<String, Object>> hotels;

    /**
     * Creates the document indexing operations performance test.
     *
     * @param options Performance test configuration options.
     */
    public IndexDocumentsTest(SearchPerfStressOptions options) {
        super(options);

        this.hotels
            = DocumentGenerator.generateHotels(options.getCount(), DocumentSize.valueOf(options.getDocumentSize()));
    }

    @Override
    public void run() {
        int[] idOffset = new int[] { ID_COUNT.getAndAdd(options.getCount()) };
        searchClient.indexDocuments(createBatch(idOffset));
    }

    @Override
    public Mono<Void> runAsync() {
        int[] idOffset = new int[] { ID_COUNT.getAndAdd(options.getCount()) };
        return searchAsyncClient.indexDocuments(createBatch(idOffset)).then();
    }

    private IndexDocumentsBatch createBatch(int[] idOffset) {
        return new IndexDocumentsBatch(hotels.stream()
            .map(hotel -> {
                hotel.put("HotelId", idOffset[0]++);
                return new IndexAction().setActionType(IndexActionType.UPLOAD)
                    .setAdditionalProperties(hotel);
            })
            .collect(Collectors.toList()));
    }
}
