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
 * Azure Search test for document indexing.
 */
public class IndexDocumentsTest extends ServiceTest<SearchPerfStressOptions> {
    private static final AtomicInteger ID_COUNT = new AtomicInteger();
    private final List<Hotel> hotels;

    /**
     * Constructs a new Azure Search test for document indexing.
     *
     * @param options Configuration options for the document indexing test.
     */
    public IndexDocumentsTest(SearchPerfStressOptions options) {
        super((options == null) ? new SearchPerfStressOptions() : options);

        this.hotels = DocumentGenerator.generateHotels(getOptions().getCount(),
            DocumentSize.valueOf(getOptions().getDocumentSize()));
    }

    @Override
    public void run() {
        int[] idOffset = new int[]{ID_COUNT.getAndAdd(getOptions().getCount())};
        searchClient.indexDocuments(new IndexDocumentsBatch<>().addUploadActions(hotels.stream()
            .peek(hotel -> hotel.setHotelId(String.valueOf(idOffset[0]++)))
            .collect(Collectors.toList())));
    }

    @Override
    public Mono<Void> runAsync() {
        int[] idOffset = new int[]{ID_COUNT.getAndAdd(getOptions().getCount())};
        return searchAsyncClient.indexDocuments(new IndexDocumentsBatch<>().addUploadActions(hotels.stream()
            .peek(hotel -> hotel.setHotelId(String.valueOf(idOffset[0]++)))
            .collect(Collectors.toList())))
            .then();
    }
}
