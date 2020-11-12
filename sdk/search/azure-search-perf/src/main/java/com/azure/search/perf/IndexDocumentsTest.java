// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.perf;

import com.azure.search.documents.indexes.models.IndexDocumentsBatch;
import com.azure.search.perf.core.DocumentGenerator;
import com.azure.search.perf.core.DocumentSize;
import com.azure.search.perf.core.Hotel;
import com.azure.search.perf.core.SearchPerfStressOptions;
import com.azure.search.perf.core.ServiceTest;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Performs document indexing operations.
 */
public class IndexDocumentsTest extends ServiceTest<SearchPerfStressOptions> {
    private static volatile AtomicInteger ID_COUNT = new AtomicInteger();
    private final List<Hotel> hotels;

    public IndexDocumentsTest(SearchPerfStressOptions options) {
        super(options);

        this.hotels = DocumentGenerator.generateHotels(options.getCount(),
            DocumentSize.valueOf(options.getDocumentSize()));
    }

    @Override
    public void run() {
        int[] idOffset = new int[] { ID_COUNT.getAndAdd(options.getCount()) };
        searchClient.indexDocuments(new IndexDocumentsBatch<>().addUploadActions(hotels.stream()
            .peek(hotel -> hotel.hotelId = String.valueOf(idOffset[0]++))
            .collect(Collectors.toList())));
    }

    @Override
    public Mono<Void> runAsync() {
        int[] idOffset = new int[] { ID_COUNT.getAndAdd(options.getCount()) };
        return searchAsyncClient.indexDocuments(new IndexDocumentsBatch<>().addUploadActions(hotels.stream()
            .peek(hotel -> hotel.hotelId = String.valueOf(idOffset[0]++))
            .collect(Collectors.toList())))
            .then();
    }
}
